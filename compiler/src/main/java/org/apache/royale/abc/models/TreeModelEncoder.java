/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.abc.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.royale.abc.semantics.ExceptionInfo;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.visitors.IDiagnosticsVisitor;
import org.apache.royale.abc.visitors.NilDiagnosticsVisitor;
import org.apache.royale.abc.graph.IFlowgraph;
import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.algorithms.DominatorTree.Multimap;

/**
 *  The TreeModelEncoder translates the stack-oriented semantics
 *  of ABC bytecode into a tree-oriented model.
 */
public class TreeModelEncoder<T>
{
    /**
     *  Construct  a new TreeModelEncoder.
     *  @param mbi - the method body of interest.
     *  @param visitor - the TreeModelVisitor that's interested.
     *  @param diagnosticsVisitor - a handler for any diagnostics generated.
     */
    public TreeModelEncoder( MethodBodyInfo mbi, TreeModelVisitor<T> visitor, IDiagnosticsVisitor diagnosticsVisitor)
    {
        this.mbi = mbi;
        this.visitor = visitor;
        this.diagnosticsVisitor = diagnosticsVisitor;

        this.visitor.visit(this);

        setUpFrames();
        placePhiNodes();
        visitFrames();

        this.visitor.visitEnd();
    }

    /**
     *  The Method of interest.
     */
    private final MethodBodyInfo mbi;

    /**
     *  The model visitor we're driving.
     */
    private final TreeModelVisitor<T> visitor;

    /**
     *  Receiver of any diagnostic output.
     */
    private final IDiagnosticsVisitor diagnosticsVisitor;

    /**
     *  The Block currently under examination.
     */
    IBasicBlock currentBlock;

    /**
     *  The FrameModelEncoder that's driving this TreeModelEncoder.
     */
    private FrameModelEncoder encoder;

    /**
     *  The symbolic representations of this method's locals.
     */
    ArrayList<Object> localSymbolicReferences = new ArrayList<Object>();

    /**
     *  The symbolic representations of this method's value stack slots.
     *  Note: There is only one symbol for each element of the stack itself,
     *  not a symbol for each value the stack will hold over its lifetime.
     */
    ArrayList<Object> valueSymbolicReferences = new ArrayList<Object>();

    /**
     *  The symbolic representations of this method's scope stack slots.
     *  Note: There is only one symbol for each element of the stack itself,
     *  not a symbol for each scope the stack will hold over its lifetime.
     */
    ArrayList<Object> scopeSymbolicReferences = new ArrayList<Object>();
    
    /**
     *  Blocks modifying frame elements, keyed by
     *  the symbolic representation of the frame element.
     */
    private Map<Object,Set<IBasicBlock>> a = new HashMap<Object,Set<IBasicBlock>>();

    /**
     * Get the MethodBodyInfo of the method being analyzed.
     * @return the MethodBodyInfo being analyzed.
     */
    public MethodBodyInfo getMethodBodyInfo()
    {
        return this.mbi;
    }

    /**
     * Get the method's flowgraph.
     * @return the IFlowGraph of the method being analyzed.
     */
    public IFlowgraph getCfg()
    {
        return this.mbi.getCfg();
    }

    /**
     * Get the block currently being analyzed.
     * @return the block currently being analyzed.
     */
    public IBasicBlock getCurrentBlock()
    {
        return this.currentBlock;
    }

    /**
     * Get the index of the instruction currently being analyzed.
     *  @return the intra-Block index of the instruction currently being analyzed.
     */
    public int getInstructionIndex()
    {
        return this.encoder.getInstructionIndex();
    }

    /**
     *  Do a preliminary pass over the method to set up
     *  the frames' extents and live-out sets.
     */
    private void setUpFrames()
    {
        mbi.getCfg().traverseGraph(
            new FrameModelEncoder(
                this.mbi,
                new FrameSetupVisitor(),
                new NilDiagnosticsVisitor()
            )
        );
    }

    /**
     *  Place phi-nodes at blocks' dominance frontiers
     *  to model dataflow merges in frame state.
     */
    private void placePhiNodes()
    {
        int iterCount = 0;

        Multimap<IBasicBlock> df = getCfg().getDominatorTree().getDominanceFrontiers();

        Map<IBasicBlock, Integer> hasAlready = new HashMap<IBasicBlock,Integer>();
        Map<IBasicBlock, Integer> work       = new HashMap<IBasicBlock, Integer>();

        for ( Object local: localSymbolicReferences )
            iterCount = placePhiNodes(iterCount, local, df, a, hasAlready, work);
        for ( Object scope: scopeSymbolicReferences )
            iterCount = placePhiNodes(iterCount, scope, df, a, hasAlready, work);
        for ( Object value: valueSymbolicReferences )
            iterCount = placePhiNodes(iterCount, value, df, a, hasAlready, work);
    }

    /**
     *  Place phi-nodes for one particular element of the frame.
     *  @param initialIteration - the initial value of the iteration counter.
     *  @param v - the frame element being analyzed.
     *  @param df - the method's dominance frontiers.
     *  @param a - the map of Blocks that assign to various frame elements.
     *  @param hasAlready - a map of Block-to-iteration-count values, used
     *    to note a Block that already has a phi node for the element.
     *  @param work - a map of Block-to-iteration-count values, used
     *    to find Blocks that need further analysis.
     */
    private int placePhiNodes(
        final int initialIteration, 
        final Object v, 
        Multimap<IBasicBlock> df,
        Map<Object,Set<IBasicBlock>> a,
        Map<IBasicBlock,Integer> hasAlready,
        Map<IBasicBlock,Integer> work
        )
    {
        if ( ! a.containsKey(v) )
            return initialIteration;

        int iterCount = initialIteration + 1;

        HashSet<IBasicBlock> w = new HashSet<IBasicBlock>();

        for ( IBasicBlock x: a.get(v) )
        {
            work.put(x, iterCount);
            w.add(x);
        }

        while ( ! w.isEmpty() )
        {
            Iterator<IBasicBlock> it = w.iterator();
            IBasicBlock x = it.next();
            it.remove();

            for ( IBasicBlock y: df.get(x) )
            {
                if ( !hasAlready.containsKey(y) || hasAlready.get(y).intValue() < iterCount )
                {
                    placePhiNode(y,v);
                    hasAlready.put(y, iterCount);

                    if ( !work.containsKey(y) || work.get(y).intValue() < iterCount )
                    {
                        work.put(y, iterCount);
                        w.add(y);
                    }
                }
            }
        }

        return iterCount;
    }

    /**
     *  Place a phi-node for a particular (Block,frame element) tuple.
     *  @param target - the Block.
     *  @param frameKey - the variable.
     */
    void placePhiNode(IBasicBlock target, Object frameKey)
    {
        Frame targetFrame = getFrame(target);

        //  Figure out which value this is.

        int idx = this.localSymbolicReferences.indexOf(frameKey);

        if ( idx != -1 )
        {
            if ( needsInitializer(targetFrame.locals, idx) )
                setFrameElement(targetFrame.locals, idx, visitor.addMergePoint(currentBlock));
        }
        else
        {
            idx = this.valueSymbolicReferences.indexOf(frameKey);

            if ( idx != -1 )
            {
                if ( needsInitializer(targetFrame.values, idx) )
                    setFrameElement(targetFrame.values, idx, visitor.addMergePoint(currentBlock));
            }
            else
            {
                idx = scopeSymbolicReferences.indexOf(frameKey);
                assert idx != -1;
                if ( needsInitializer(targetFrame.scopes, idx) )
                    setFrameElement(targetFrame.scopes, idx, visitor.addMergePoint(currentBlock));
            }
        }
    }

    /**
     *  Determine if a particular frame element needs an initializer.
     *  @param elements - the frame elements (locals, scope stack, or value stack).
     *  @param idx - the index of the frame element of interest.
     *  @return true if the given index has no initializer object.
     */
    private boolean needsInitializer(ArrayList<? extends Object> elements, int idx)
    {
        return ( elements.size() <= idx || elements.get(idx) == null );
    }

    /**
     *  Initialize a frame element with an anonymous marker object if necessary.
     *  @param elements - the frame elements (locals, scope stack, or value stack).
     *  @param idx - the index of the frame element of interest.
     */
    private void touchFrameElement(ArrayList<Object> elements, int idx)
    {
        if ( needsInitializer(elements, idx) )
            setFrameElement(elements, idx, new Object());
    }

    /**
     *  Set a frame element.
     *  @param elements - the frame elements (locals, scope stack, or value stack).
     *  @param idx - the index of the frame element of interest.
     *  @param value - the value to set.
     */
    private <E> void setFrameElement(ArrayList<E> elements, int idx, final E value)
    {
        while ( elements.size() <= idx )
            elements.add(null);
        elements.set(idx, value);
    }

    /**
     *  Modify a frame element; touch it and add the modifying Block to its
     *  set of assigning blocks.
     *  @param elements - the frame elements (locals, scope stack, or value stack).
     *  @param idx - the index of the frame element of interest.
     *  @param b - the Block that modifies the element.
     */
    private void modifyFrameElement(ArrayList<Object> elements, int idx, IBasicBlock b)
    {
        touchFrameElement(elements,idx);

        if ( ! a.containsKey(elements.get(idx)) )
            a.put(elements.get(idx), new HashSet<IBasicBlock>());
        a.get(elements.get(idx)).add(b);
    }

    /**
     *  Visit each block, its associated Frame, and the 
     *  instructions in each block, showing each in turn
     *  to the TreeModelVisitor and recording its results
     *  in the Frame.
     */
    private void visitFrames()
    {
        ArrayList<T> parameters = new ArrayList<T>();

        //  Load the initial frame's locals with parameter information.
        for ( int i = 0; i < this.mbi.getMethodInfo().getParamCount(); i++ )
            parameters.add(visitor.translateParameter(i));

        Frame startFrame = getFrame(this.mbi.getCfg().getStartBlock());
        startFrame.locals.addAll(parameters);

        //  Initialize exception-handling targets with the exception variable,
        //  and set up a dataflow merge node for each local that may be read
        //  in the block.
        //  TODO: This encoder makes pessimistic assumptions about dataflow
        //  into the locals, i.e., it assumes every exception handler is 
        //  globally reachable.
        for ( ExceptionInfo ex: this.mbi.getExceptions() )
        {
            IBasicBlock catchTarget = this.mbi.getCfg().getBlock(ex.getTarget());
            Frame catchFrame = getFrame(catchTarget);

            setFrameElement(catchFrame.values, 0, visitor.translateExceptionVariable(ex.getCatchVar(), ex.getExceptionType()));

            for ( int i = 0; i < parameters.size(); i++ )
            {
                catchFrame.locals.add(visitor.addMergePoint(catchTarget));
                @SuppressWarnings("unchecked")
                TreeModelVisitor.IMergePoint<T> mergeNode = (TreeModelVisitor.IMergePoint<T>)catchFrame.locals.get(i);
                mergeNode.addValue(parameters.get(i));
            }

            //  Initialize the other locals' merge nodes, which are initially empty.
            for ( int i = parameters.size(); i < localSymbolicReferences.size(); i++ )
            {
                catchFrame.locals.add(visitor.addMergePoint(catchTarget));
            }
        }

        this.encoder = new FrameModelEncoder( this.mbi, new ModelDrivingVisitor(this.visitor), this.diagnosticsVisitor );
        this.mbi.getCfg().traverseGraph( this.encoder );
    }

    /**
     *  A representation of an AVM "frame," the local variables, scope stack slots,
     *  and value stack slots used in a particular Block of the method's flowgraph.
     */
    public class Frame
    {
        /**
         *  The local variables used in the Block.
         */
        public final ArrayList<T> locals = new ArrayList<T>();

        /**
         *  The scope stack slots used in the Block.
         */
        public final ArrayList<T> scopes = new ArrayList<T>();

        /**
         *  The value stack slots used in the Block.
         */
        public final ArrayList<T> values = new ArrayList<T>();

        /**
         *  @return the value on top of the value stack.
         */
        public T tos()
        {
            return this.values.get(valueStackDepth());
        }

        /**
         *  Remove a value from the value stack.
         *  Visitors must modify the Frame, but should
         *  maintain their own modifiable view of it.  
         */
        private T popValue()
        {
            return popElement(this.values);
        }

        /**
         *  Push a value onto the value stack.
         *  Visitors must modify the Frame, but should
         *  maintain their own modifiable view of it.  
         */
        private T pushValue(T value)
        {
            this.values.add(value);
            return value;
        }
        
        /**
         *  Push a value onto the scope stack.
         *  Visitors must modify the Frame, but should
         *  maintain their own modifiable view of it.  
         */
        private T pushScope(T scope)
        {
            this.scopes.add(scope);
            return scope;
        }

        /**
         *  Pop a value off the scope stack.
         *  Visitors must modify the Frame, but should
         *  maintain their own modifiable view of it.  
         */
        private T popScope()
        {
            return popElement(this.scopes);
        }

        /**
         *  Get a local variable.
         *  Visitors must modify the Frame, but should
         *  maintain their own modifiable view of it.  
         */
        private T getlocal(int idx)
        {
            adjustSize(this.locals, idx+1);
            return this.locals.get(idx);
        }

        /**
         *  Set a local variable.
         *  Visitors must modify the Frame, but should
         *  maintain their own modifiable view of it.  
         */
        private T setlocal(int idx, T value)
        {
            adjustSize(this.locals, idx+1);
            this.locals.set(idx, value);
            propagateLocalToCatchBlocks(idx,value);

            return value;
        }

        /**
         *  Get a scope object.
         *  Visitors must modify the Frame, but should
         *  maintain their own modifiable view of it.  
         */
        private T getscopeobject(int idx)
        {
            adjustSize(this.scopes, idx+1);
            return this.scopes.get(idx);
        }

        /**
         *  Verify that the value stack contains
         *  at least the required number of live values.
         *  @param required - the numbe of values required.
         *  @return true if the value stack has the required number of values.
         */
        private boolean verifyStackDepth(int required)
        {
            return this.values.size() >= required;
        }

        /**
         *  @return the number of live values on the value stack.
         */
        private int valueStackDepth()
        {
            return this.values.size() - 1;
        }

        /**
         *  Verify that the scope stack contains
         *  at least the required number of live values.
         *  @param required - the numbe of values required.
         *  @return true if the scope stack has the required number of values.
         */
        private boolean verifyScopeDepth(int required)
        {
             return this.scopes.size() >= required;
        }

        /**
         *  @return the number of live values on the scope stack.
         */
        @SuppressWarnings("unused")
        private int scopeStackDepth()
        {
            return this.scopes.size() - 1;
        }
    }

    /**  
     * Propagate a local variable's value to all catch blocks.
     * TODO: this should only propagate to catch blocks reachable
     * from the current block; this code errs on the side of pessimism.
     * @param idx the index of the local.
     * @param value the value to propagate.
     */
    void propagateLocalToCatchBlocks(int idx, T value)
    {
        for ( ExceptionInfo ex: mbi.getExceptions() )
        {
            IBasicBlock catchTarget = mbi.getCfg().getBlock(ex.getTarget());
            Frame catchFrame = getFrame(catchTarget);

            if (  catchFrame.locals.get(idx) instanceof TreeModelVisitor.IMergePoint )
            {
                @SuppressWarnings("unchecked")
                TreeModelVisitor.IMergePoint<T> mergeNode = (TreeModelVisitor.IMergePoint<T>)catchFrame.locals.get(idx);
                mergeNode.addValue(value);
            }
            // else it's an exception variable.
        }
    }

    /**
     *  Active Frames, keyed by their generating Block.
     */
    private Map<IBasicBlock,Frame> framesByBlock = new HashMap<IBasicBlock,Frame>();

    /**
     *  Get the Frame that corresponds to a Block.
     *  @param b - the Block of interest.
     *  @return the Frame mapped to the Block.
     */
    public Frame getFrame(IBasicBlock b)
    {
        if ( ! this.framesByBlock.containsKey(b) )
            this.framesByBlock.put(b, new Frame());
        return this.framesByBlock.get(b);
    }

    /**
     *  The FrameSetupVisitor creates Frame objects for the Blocks,
     *  and drives the modifyFrameElement calls that initialize
     *  the map of Blocks that assign values to specific frame elements.
     */
    private class FrameSetupVisitor implements FrameModelVisitor<T>
    {
        IBasicBlock currentBlock = null;
        BlockState blockState = null;

        public void visit(FrameModelEncoder encoder)
        {
        }

        public void visitEnd()
        {
        }

        public T noFrameEffect(Instruction i)
        {
            return null;
        }

        public T consumeValue(Instruction i, int count)
        {
            //  The model driving visitor detects stack underflow.
            blockState.stackDepth = Math.max(blockState.stackDepth - count, 0);
            return null;
        }

        /**
         *  Handle an instruction that pushes a value onto the stack.
         *  @param i - the Instruction.
         */
        public T produceValue(Instruction i)
        {
            modifyFrameElement(valueSymbolicReferences, blockState.stackDepth, currentBlock);
            blockState.stackDepth++;
            return null;
        }

        public T consumeAndProduceValue(Instruction i, int consumeCount)
        {
            consumeValue(null, consumeCount);
            produceValue(null);
            return null;
        }

        public T branch(Instruction i, IBasicBlock target)
        {
            return null;
        }

        public T multiwayBranch(Instruction i, Collection<IBasicBlock> targets)
        {
            return null;
        }

        public T getlocal(Instruction i, int idx)
        {
            touchFrameElement(localSymbolicReferences, idx);
            return null;
        }

        public T setlocal(Instruction i, int idx)
        {
            modifyFrameElement(localSymbolicReferences, idx, currentBlock);
            return null;
        }

        public void modifyLocal(Instruction i, int idx)
        {
            modifyFrameElement(localSymbolicReferences, idx, currentBlock);
        }

        public T moveValueToScopeStack(Instruction i)
        {
            consumeValue(null, 1);
            modifyFrameElement(scopeSymbolicReferences, blockState.scopeDepth, currentBlock);
            blockState.scopeDepth++;
            return null;
        }

        public T popscope(Instruction i)
        {
            blockState.scopeDepth = Math.max(blockState.scopeDepth - 1, 0);
            return null;
        }

        public T getScopeobject(Instruction i, int idx)
        {
            touchFrameElement(scopeSymbolicReferences, idx);
            return null;
        }

        public T hasnext2(Instruction i)
        {
            modifyFrameElement(localSymbolicReferences, (Integer)i.getOperand(0), currentBlock);
            modifyFrameElement(localSymbolicReferences, (Integer)i.getOperand(1), currentBlock);
            return null;
        }

        public T dup(Instruction i)
        {
            produceValue(null);
            return null;
        }

        public T swap(Instruction i)
        {
            //  No effect on the frame setup.
            return null;
        }

        public boolean visitBlock(IBasicBlock b)
        {
            if ( visited.add(b) )
            {
                assert this.currentBlock == null;
                this.currentBlock = b;
                this.blockState = getBlockState(b);
                return true;
            }
            else
            {
                return false;
            }
        }

        /**
         *  Blocks visisted so far.
         */
        private Set<IBasicBlock> visited = new HashSet<IBasicBlock>();

        /**
         *  End the visit to a block; ensure that all its
         *  frame elements are in place.
         */
        public void visitEndBlock(IBasicBlock b)
        {
            for ( int i = 0; i < this.blockState.stackDepth; i++ )
                touchFrameElement(valueSymbolicReferences, i);

            for ( int i = 0; i < this.blockState.scopeDepth; i++ )
                touchFrameElement(scopeSymbolicReferences, i);

            this.currentBlock = null;
            this.blockState = null;
        }

        /**
         *  Propagate value/scope stack depth information
         *  from one Block to its target block.
         */
        public void visitEdge(IBasicBlock from, IBasicBlock target)
        {
            assert(from == this.currentBlock);
            BlockState targetState = getBlockState(target);

            targetState.stackDepth = blockState.stackDepth;
            targetState.scopeDepth = blockState.scopeDepth;
        }


        /**
         *  Get the scope/value stack depth tracker for a Block.
         *  @param b - the Block of interest.
         *  @return the BlockState tracker mapped to it.
         */
        private BlockState getBlockState(IBasicBlock b)
        {
            if ( ! this.statesByBlock.containsKey(b) )
                this.statesByBlock.put(b, new BlockState());
            return this.statesByBlock.get(b);
        }

        private Map<IBasicBlock, BlockState> statesByBlock = new HashMap<IBasicBlock,BlockState>();
    }

    private static class BlockState
    {
        int stackDepth = 0;
        int scopeDepth = 0;
    }

    /**
     *  The ModelDrivingVisitor makes a second pass over the method's
     *  control flow graph, after the Frames have been initialized and
     *  dataflow merge points placed, and drives the visitor's traversal
     *  of the method.
     */
    private class ModelDrivingVisitor implements FrameModelVisitor<T>
    {
        ModelDrivingVisitor(TreeModelVisitor<T> visitor)
        {
            this.visitor = visitor;
        }

        final TreeModelVisitor<T> visitor;

        public void visit(FrameModelEncoder encoder)
        {
            assert(encoder == TreeModelEncoder.this.encoder);
        }

        public void visitEnd()
        {
            TreeModelEncoder.this.encoder = null;
        }

        /**
         *  The Frame that corresponds to the block being visited.
         */
        Frame currentFrame = null;

        @Override
        public T noFrameEffect(Instruction i)
        {
            return visitor.translate(i, noOperands());
        }

        /**
         *  Handle an instruction that consumes value stack elements.
         *  @param i - the Instruction.
         *  @param count - the number of value stack elements consumed.
         */
        public T consumeValue(Instruction i, int count)
        {
            if ( this.currentFrame.verifyStackDepth(count) )
            {
                ArrayList<T> operands = new ArrayList<T>(count);
                for ( int j = 0; j < count; j++ )
                    operands.add(this.currentFrame.popValue());
                return visitor.translate(i, operands);
            }
            else
            {
                return visitor.valueStackUnderflow(i, count);
            }
        }

        /**
         *  Handle an instruction that pushes a value onto the stack.
         *  @param i - the Instruction.
         */
        public T produceValue(Instruction i)
        {
            return this.currentFrame.pushValue(visitor.translate(i, noOperands()));
        }

        /**
         *  Handle an instruction that consumes value stack elements,
         *  and then pushes a new value onto the stack.
         *  @param i - the Instruction.
         *  @param consumeCount - the number of value stack elements consumed.
         */
        public T consumeAndProduceValue(Instruction i, int consumeCount)
        {
            if ( this.currentFrame.verifyStackDepth(consumeCount) )
            {
                ArrayList<T> operands = new ArrayList<T>(consumeCount);
                for ( int j = 0; j < consumeCount; j++ )
                    operands.add(this.currentFrame.popValue());
                return this.currentFrame.pushValue(visitor.translate(i, operands));
            }
            else
            {
                return visitor.valueStackUnderflow(i, consumeCount);
            }
        }

        /**
         *  Handle a branch instruction.
         *  @param i - the Instruction.
         *  @param target - the Instruction's target.  Instructions with
         *    fall-through semantics also implicitly target the next Block.
         */
        public T branch(Instruction i, IBasicBlock target)
        {
            return visitor.translateBranch(i, singleOperand(target));
        }

        /**
         *  Handle a multibranch instruction.
         *  @param i - the Instruction.
         *  @param targets - the Instruction's targets.
         */
        public T multiwayBranch(Instruction i, Collection<IBasicBlock> targets)
        {
            return visitor.translateBranch(i, targets);
        }

        /**
         *  Get a local variable, leaving its value on the stack.
         *  @param i - the Instruction.
         *  @param idx - the variable's index.
         */
        public T getlocal(Instruction i, int idx)
        {
            adjustSize(currentFrame.locals, idx + 1);
            T result = visitor.translate(i, singleOperand(this.currentFrame.getlocal(idx)));
            this.currentFrame.pushValue(result);
            return result;
        }

        /**
         *  Set a local variable, comsuming a value from the stack.
         *  @param i - the Instruction.
         *  @param idx - the variable's index.
         */
        public T setlocal(Instruction i, int idx)
        {
            adjustSize(currentFrame.locals, idx + 1);
            if ( this.currentFrame.verifyStackDepth(1) )
            {
                T result = visitor.translate(i, singleOperand(this.currentFrame.popValue()));
                return this.currentFrame.setlocal(idx, result);
            }
            else
            {
                 return this.currentFrame.setlocal(idx, visitor.valueStackUnderflow(i, 1));
            }
        }

        /**
         *  Modify a local variable.
         *  @param i - the Instruction.
         *  @param idx - the variable's index.
         */
        public void modifyLocal(Instruction i, int idx)
        {
            visitor.translate(i, noOperands());
        }

        /**
         *  Pop a value off the value stack and push it on the scope stack.
         *  @param i - the Instruction (an OP_pushscope).
         */
        public T moveValueToScopeStack(Instruction i)
        {
            if ( this.currentFrame.verifyStackDepth(1) )
                return this.currentFrame.pushScope(visitor.translate(i, singleOperand(this.currentFrame.popValue())));
            else
                 return visitor.valueStackUnderflow(i, 1);
        }

        /**
         *  Pop a value off the scope stack.
         *  @param i - the Instruction (an OP_popscope).
         */
        public T popscope(Instruction i)
        {
            if ( this.currentFrame.verifyScopeDepth(1) )
                return visitor.translate(i, singleOperand(this.currentFrame.popScope()));
            else
                return visitor.scopeStackUnderflow(i, 1);

        }

        /**
         *  Get a particular scope stack element.
         *  @param i - the Instruction.
         *  @param idx - the index of the scope element.
         */
        public T getScopeobject(Instruction i, int idx)
        {
            if ( this.currentFrame.verifyScopeDepth(idx+1) )
                return this.currentFrame.pushValue(visitor.translate(i, singleOperand(this.currentFrame.scopes.get(idx))));
            else
                return visitor.scopeStackUnderflow(i, 1);
        }

        /**
         *  Handle the special-case hasnext2 instruction.
         *  @param i - the Instruction.
         */
        public T hasnext2(Instruction i)
        {
            return null;
        }

        /**
         *  Handle the stack-maintenance dup instruction.
         *  @param i - the Instruction.
         */
        public T dup(Instruction i)
        {
            if ( this.currentFrame.verifyStackDepth(1) )
            {
                return this.currentFrame.pushValue(visitor.translate(i, singleOperand(this.currentFrame.tos())));
            }
            else
            {
                return visitor.valueStackUnderflow(i, 1);
            }
            
        }

        /**
         *  Handle the stack-maintenance swap instruction.
         *  @param i - the Instruction.
         */
        public T swap(Instruction i)
        {
            if ( this.currentFrame.verifyStackDepth(2) )
            {
                int stackDepth = this.currentFrame.valueStackDepth();
                T temp = visitor.translate(i, singleOperand(this.currentFrame.tos()));
                this.currentFrame.values.set( stackDepth, this.currentFrame.values.get(stackDepth-1) );
                this.currentFrame.values.set( stackDepth - 1, temp);
                return this.currentFrame.tos();
            }
            else
            {
                return visitor.valueStackUnderflow(i, 2);
            }
        }

        @Override
        public boolean visitBlock(IBasicBlock b)
        {
            Frame frame = getFrame(b);
            currentBlock = b;
            this.currentFrame = frame;

            if ( visitor.visitBlock(b) )
            {
                return true;
            }
            else
            {
                //  Tear down.
                this.currentFrame = null;
                currentBlock = null;
                return false;
            }

        }

        @Override
        public void visitEndBlock(IBasicBlock b)
        {
            visitor.visitEnd(b);
            this.currentFrame = null;
            currentBlock = null;
        }

        @Override
        public void visitEdge(IBasicBlock from, IBasicBlock target)
        {
            assert getFrame(from) == this.currentFrame;

            Frame targetFrame = getFrame(target);

            for ( int i = 0; i < this.currentFrame.locals.size(); i++ )
                addInitializer(i, targetFrame.locals, this.currentFrame.getlocal(i));
            for ( int i = 0; i < this.currentFrame.values.size(); i++ )
                addInitializer(i, targetFrame.values, this.currentFrame.values.get(i));
            for ( int i = 0; i < this.currentFrame.scopes.size(); i++ )
                addInitializer(i, targetFrame.scopes, this.currentFrame.getscopeobject(i));
        }


        private void addInitializer(final int i, ArrayList<T> target, T value)
        {
            if ( target.size() <= i )
            {
                adjustSize(target, i);
                target.add(value);
            }
            else if ( target.get(i) instanceof TreeModelVisitor.IMergePoint<?> )
            {
                @SuppressWarnings("unchecked")
                TreeModelVisitor.IMergePoint<T> phi = (TreeModelVisitor.IMergePoint<T>) target.get(i);
                phi.addValue(value);
            }
            else if ( target.get(i) == null )
            {
                target.set(i, value);
            }
            else
            {
                // TODO: Verify that the existing value and the current value are the same.
            }
        }

        /**
         *  Build an operands Collection from a single operand.
         *  @param operand - the operand.
         *  @return the operand, wrapped in a Collection.
         */
        private <X> Collection<X> singleOperand(X operand)
        {
             ArrayList<X> result = new ArrayList<X>(1);
             result.add(operand);
             return result;
        }

        /**
         *  @return an empty list, approprately cast.
         */
        private Collection<T> noOperands()
        {
            return Collections.emptyList();
        }
    }

    /**
     *  Adjust the size of a collection of frame elements.
     *  @param frameElements - the frame elements of interest.
     *  @param idx - the minimum size of the frame element.
     */
    private static <X> void adjustSize(ArrayList<X> frameElements, int idx)
    {
        while(frameElements.size() < idx )
            frameElements.add(null);
    }

    /**
     *  Pop a value off a collection of frame elements.
     *  @param frameElements - the frame elements of interest.
     *  @return the last element in the collection, which
     *    has been removed from the collection.
     */
    static <X> X popElement(ArrayList<X> frameElements)
    {
        int lastIdx = frameElements.size() - 1;
        return frameElements.remove(lastIdx);
    }
}
