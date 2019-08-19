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

package org.apache.royale.abc.semantics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.IFlowgraph;
import org.apache.royale.abc.graph.algorithms.DepthFirstPreorderIterator;
import org.apache.royale.abc.graph.algorithms.DominatorTree;
import org.apache.royale.abc.visitors.IFlowGraphVisitor;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.internal.projects.LibraryPathManager;

/**
 * A ControlFlowGraph represents the flow of control through a sequence of
 * instructions. The instructions are organized into a set of Blocks --
 * sequences of instructions where normal control flow proceeds linearly from
 * one instruction to the next -- and a set of edges, representing the
 * discontinuous transfer of control points.

 */
public class ControlFlowGraph implements IFlowgraph
{
    /**
     * Search direction in the initial block of a block-by-block search
     * (typically for debug information).
     */
    private enum SearchDirection { Forward, Backward };

    /**
     * Create a ControlFlowGraph from the instructions in the MethodBodyInfo.
     * @param mbi  - the MethodBodyInfo of the method whose control flow graph is to be computed.
     */
    ControlFlowGraph(MethodBodyInfo mbi)
    {
        this.mbi = mbi;
        this.startBlock = newBlock();
        buildCfg();
    }

    /**
     *  This ControlFlowGraph's MethodBodyInfo.
     */
    private final MethodBodyInfo mbi;

    /**
     *  Blocks of the flow graph in entry order.
     */
    private ArrayList<IBasicBlock> blocks = new ArrayList<IBasicBlock>();

    /**
     * The entry point of this routine; known in
     * graph theory as the start block.
     */
    private Block startBlock;

    /**
     *  Blocks keyed by their Labels.  Multiple Labels may map to a Block.
     */
    private Map<Label, IBasicBlock> blocksByLabel = new HashMap<Label, IBasicBlock>();

    /**
     *  Catch targets defined in this method.
     */
    private ArrayList<IBasicBlock> catchTargets = new ArrayList<IBasicBlock>();

    /**
     *  The graph's dominator tree, built on demand.
     */
    private DominatorTree dominatorTree = null;

    /**
     * Build the CFG.
     */
    private void buildCfg()
    {
        Block current_block = startBlock;

        boolean last_block_transferred_control = false;

        //  Sort the labels by position.
        Collections.sort(mbi.getInstructionList().getActiveLabels());
        Iterator<Label> labels = mbi.getInstructionList().getActiveLabels().iterator();
        Label next_label = getNextLabel(labels);

        //  Labels of a Block's successors, keyed by the "from" block.
        //  These are turned into edges from the "from" block once all
        //  blocks have been seen.
        Map<Block, Collection<Label>> successor_labels = new HashMap<Block, Collection<Label>>();

        //  Iterate through the method's instructions by position; the active Labels
        //  have embedded position information that corresponds to this zero-based 
        //  enumeration of the instructions.
        List<Instruction> instructions = mbi.getInstructionList().getInstructions();

        for (int i = 0; i < instructions.size(); i++)
        {
            if (i == next_label.getPosition() || last_block_transferred_control)
            {
                if ( current_block.size() > 0 )
                {
                    Block prev_block = current_block;
                    current_block = newBlock();

                    if (prev_block.canFallThrough())
                    {
                        // Add an edge from the prev block to the current block, since
                        // control can fall through from that block to the current one.
                        prev_block.addSuccessor(current_block);
                    }
                }
                else
                {
                    // If the first instruction is a label target,
                    // then the start block will still be empty;
                    // this is actually OK for the flowgraph, but
                    // would create a special case empty block
                    // and complicate clients' block processing.
                    assert current_block == startBlock;
                }

                //  Add catch targets to the CFG's mapping.
                if (i == next_label.getPosition() && this.mbi.isCatchTarget(next_label))
                    this.catchTargets.add(current_block);

                //  Map all labels targeting this block.
                while (next_label.getPosition() == i) 
                {
                    blocksByLabel.put(next_label, current_block);
                    next_label = getNextLabel(labels);
                }
            }
            
            Instruction insn = instructions.get(i);
            current_block.add(insn);

            //  Begin a new block after any transfer of control.
            last_block_transferred_control = insn.isTransferOfControl();

            if (insn.isBranch())
            {
                Collection<Label> successors = successor_labels.get(current_block);
                if (successors == null)
                {
                    successors = new ArrayList<Label>();
                    successor_labels.put(current_block, successors);
                }
                
                // The target may be a forward jump to a block we haven't seen,
                // just store the label for now, and we'll get its block later after
                // we've seen all the instructions.
                if (insn.getOpcode() == ABCConstants.OP_lookupswitch)
                {
                    for (int j = 0; j < insn.getOperandCount(); j++)
                    {
                        if (insn.getOperand(j) instanceof Label)
                            successors.add((Label)insn.getOperand(j));
                    }
                }
                else
                {
                    successors.add(insn.getTarget());
                }
            }
        }

        // We've seen all the instructions now, so we can compute the blocks that
        // each label corresponds to, and fill in the rest of the graph
        Set<Entry<Block, Collection<Label>>> entries = successor_labels.entrySet();
        ArrayList<Entry<Block, Collection<Label>>> listOfEntries = new ArrayList<Entry<Block, Collection<Label>>>();
        for (Map.Entry<Block, Collection<Label>> entry : entries)
        	listOfEntries.add(entry);
        Collections.sort(listOfEntries, new Comparator<Entry<Block, Collection<Label>>>()
        {
            /**
             * Sort by blocknum.
             */
            @Override
            public int compare(Entry<Block, Collection<Label>> o1, Entry<Block, Collection<Label>> o2)
            {
            	int block1 = o1.getKey().blocknum;
            	int block2 = o2.getKey().blocknum;
                return block1 - block2;
            }
        });
        for (Map.Entry<Block, Collection<Label>> entry : listOfEntries)
            for (Label target_label : entry.getValue())
            {
                IBasicBlock target_block = getBlock(target_label);
                if ( target_block != null )
                    entry.getKey().addSuccessor(target_block);
            }
    }
    
    /**
     *  Get a Label's target Block.
     * 
     *  @param l - the Label of interest.
     *  @return the corresponding Block, or null if not found.
     */
    public IBasicBlock getBlock(Label l)
    {
        return blocksByLabel.get(l);
    }

    /**
     *  Get the start block.
     *  @return the start block.
     */
    public IBasicBlock getStartBlock()
    {
        return this.startBlock;
    }

    /**
     *  Is the given Block a catch target?
     *  @param b - the Block of interest.
     *  @return true if the Block is a catch target.
     *  @see MethodBodyInfo#isCatchTarget(Label)
     *    which differs from this routine in that it uses 
     *    positional information, which is not valid if the
     *    ControlFlowGraph has been edited.
     */
    public boolean isCatchTarget(IBasicBlock b)
    {
        return this.catchTargets.contains(b);
    }

    /**
     * Get an iterator that will iterate over the blocks in the graph.
     * in depth-first preorder. This will traverse each edge in the graph
     * once, but may return the same block multiple times if multiple edges lead
     * to it.
     */
    public Iterable<IBasicBlock> blocksInControlFlowOrder()
    {
        return new Iterable<IBasicBlock>()
        {
            @Override
            public Iterator<IBasicBlock> iterator()
            {
                return new DepthFirstPreorderIterator(ControlFlowGraph.this.getRoots());
            }
        };
    }

    /**
     *  Start a new block.
     * 
     *  @return the new Block.
     */
    private Block newBlock()
    {
        Block result = new Block();
        result.blocknum = blocks.size();
        blocks.add(result);
        return result;
    }
    
    /**
     *  @param labels - an Iterator of Labels.
     *  @return the next Label in the sequence, or a marker
     *  Label if the sequence is exhausted.
     */
    private Label getNextLabel(Iterator<Label> labels)
    {
        if (labels.hasNext())
            return labels.next();
        else
            return END_OF_LABEL_SEQUENCE;
    }

    private static final Label END_OF_LABEL_SEQUENCE = new Label();

    /**
     *  Get the graph's blocks in their original order.
     * 
     *  @return an immutable List that presents the blocks in entry order.
     */
    public List<IBasicBlock> getBlocksInEntryOrder()
    {
        return Collections.unmodifiableList(this.blocks);
    }

    /**
     *  Get the graph's blocks as a mutable list.
     *  @return the blocks in a list.
     */
    List<IBasicBlock> getBlocks()
    {
        return this.blocks;
    }

    /**
     * Walk a IFlowGraphVisitor over this CFG.
     * 
     *  @param visitor - the visitor.
     */
    public void traverseGraph(IFlowGraphVisitor visitor)
    {
        for (IBasicBlock b : this.blocksInControlFlowOrder())
        {
            if (visitor.visitBlock(b))
            {
                for (Instruction i : b.getInstructions())
                    visitor.visitInstruction(i);

                visitor.visitEnd(b);
            }
        }
    }

    /**
     *  Touch the graph's dominator tree and fetch it.
     *  @return the graph's dominator tree.
     */
    public DominatorTree getDominatorTree()
    {
        if ( this.dominatorTree == null )
            this.dominatorTree = new DominatorTree(getRoots());

        return this.dominatorTree;
    }

    /**
     * Synthesize this flowgraph's "roots."  The flowgraph actually
     * has only one root, the start block, but the exception handlers
     * aren't reachable by normal control flow so for the moment they're
     * considered alternate roots.
     * @return the Collection of the start block and any exception handlers.
     */
    private Collection<IBasicBlock> getRoots()
    {
        Collection<IBasicBlock> roots = new ArrayList<IBasicBlock>();
        roots.add(this.startBlock);
        roots.addAll(this.catchTargets);

        return roots;
    }

    /**
     *  Remove an unreachable block from the CFG.
     *  @param b - the Block to remove.  
     *  @pre b must be unreachable.
     */
    public void removeUnreachableBlock(IBasicBlock b)
    {
        assert(!isReachable(b));

        //  The CFG is naive about reachability 
        //  of exception-handling targets, so
        //  this procedure may remove blocks
        //  that were formerly "reachable."
        boolean removedFormerlyReachable = false;

        //  Edit any exception-handlers that reference b.
        for ( ExceptionInfo ex: this.mbi.getExceptions() )
        {
            if ( b.equals(getBlock(ex.getFrom())) )
            {
                if ( b.equals(getBlock(ex.getTo())) )
                {
                    //  This exception-handler is now dead.
                    ex.setLive(false);
                    this.catchTargets.remove(getBlock(ex.getTarget()));
                    removedFormerlyReachable = true;
                }
                else
                {
                    //  Move the mapping of the From label
                    //  to the next block in the covered region.
                    int bIdx = this.blocks.indexOf(b);
                    assert bIdx >= 0 && bIdx < this.blocks.size();

                    this.blocksByLabel.put(ex.getFrom(), this.blocks.get(bIdx+1));
                }
            }
            else if ( b.equals(getBlock(ex.getTo())) )
            {
                if ( b.equals(getBlock(ex.getFrom())) )
                {
                    //  This exception-handler is now dead.
                    ex.setLive(false);
                    this.catchTargets.remove(getBlock(ex.getTarget()));
                    removedFormerlyReachable = true;
                }
                else
                {
                    //  Move the mapping of the To label
                    //  to the previous block in the covered region.
                    int bIdx = this.blocks.indexOf(b);
                    assert bIdx >= 1 && bIdx < this.blocks.size();

                    this.blocksByLabel.put(ex.getTo(), this.blocks.get(bIdx-1));
                }
            }
        }

        if ( removedFormerlyReachable )
        {
            //  The dominator tree needs to be recomputed.
            this.dominatorTree = null;
        }

        //  Finally, remove the block itself.
        this.blocks.remove(b);
    }

    /**
     *  Is the given Block reachable?
     *  @param b - the block of interest.
     *  @return true if a path exists from
     *    any "entry" block to b.
     */
    public boolean isReachable(IBasicBlock b)
    {
        return getDominatorTree().topologicalTraversal().contains(b);
    }

    /**
     * Find the closest matching line number to the start of a block.
     * @param b the block of interest.
     * @return any initial debugline within the block, or the nearest
     * debugline in the preceeding (entry-order) blocks.
     */
    public int findLineNumber(IBasicBlock b)
    {
        return findLineNumber(b, 0, SearchDirection.Forward);
    }

    /**
     * Find the nearest debugline instruction preceeding the given
     * (Block,offset) position and fetch its line number.
     * @param b - the Block of interest.
     * @param initialOffset - the start offset in the block.
     * @return the closest debugline instruction's line number,
     * or -1 if not found.
     */
    public int findLineNumber(IBasicBlock b, int initialOffset)
    {
        return findLineNumber(b, b.size()-1, SearchDirection.Backward);
    }

    /**
     * Find the nearest debugline instruction preceeding the given
     * (Block,offset) position and fetch its line number.
     * @param b - the Block of interest.
     * @param initialOffset - the start offset in the block.
     * @param initialDirection Search the first block Forward or Backward.
     * @return the closest debugline instruction's line number,
     * or -1 if not found.
     */
    private int findLineNumber(IBasicBlock b, int initialOffset, SearchDirection initialDirection)
    {
        assert(this.blocks.contains(b));
        // Start searching in the block given; the offset may
        // be a pseudo-offset that specifies an initial forward
        // search through non-executable instructions.
        Instruction result = searchBlock(b, ABCConstants.OP_debugline, initialOffset, initialDirection);

        if ( result != null )
        {
            //  Found a good line number match; return it as a zero-based offset.
            return result.getImmediate() - 1;
        }
        else
        {
            //  Search backwards through the instruction stream; bump any line
            //  number found during this search since the dead block is probably
            //  on the next line.
            for ( int i = this.blocks.indexOf(b) - 1; i >= 0 && result == null; i-- )
            {
                IBasicBlock candidate = this.blocks.get(i);
                result = searchBlock( candidate, ABCConstants.OP_debugline, candidate.size() - 1, SearchDirection.Backward);
            }
        }

        return result != null? result.getImmediate(): -1;
    }

    /**
     * Find the nearest debugfile instruction to the start of
     * the given block and fetch its source path.
     * @param b - the Block of interest.
     * @return the closest debugfile instruction's source path,
     * or null if not found.
     */
    public String findSourcePath(IBasicBlock b)
    {
        return findSourcePath(b, 0, SearchDirection.Forward);
    }
    /**
     * Find the nearest instruction preceeding the given
     * (Block,offset) position and fetch its source path.
     * @param b - the Block of interest.
     * @param initialOffset - the start offset in the block.
     * @return the closest debugfile instruction's source path,
     * or null if not found.
     */
    public String findSourcePath(IBasicBlock b, int initialOffset)
    {
        return findSourcePath(b, initialOffset, SearchDirection.Backward);
    }

    /**
     * Find the nearest debugline instruction to the given (Block, offset)
     * position and fetch its source path.
     * @param b the block of interest.
     * @param initialOffset the start offset in the block.
     * @param initialDirection Search the first block Forward or Backward.
     * @return the closest debugfile instruction's source path,
     * or null if not found.
     */
    private String findSourcePath(IBasicBlock b, int initialOffset, SearchDirection initialDirection)
    {
        assert(this.blocks.contains(b));

        Instruction result = searchBlock(b, ABCConstants.OP_debugfile, initialOffset, initialDirection);

        //  Search backwards through the instruction stream if necessary.
        if ( result == null )
        {
            for ( int i = this.blocks.indexOf(b) - 1; i >= 0 && result == null; i-- )
            {
                IBasicBlock candidate = this.blocks.get(i);
                result = searchBlock(candidate, ABCConstants.OP_debugfile, candidate.size() - 1, SearchDirection.Backward);
            }
        }

        if (result == null)
            return null;

        // The debug filename can sometimes be in the format: sourcepath;package;filename so
        // need to translate that back to an absolute path by replacing the ;'s with system
        // path separators, making sure to normailize path separators.
        String debugFilename = result.getOperand(0).toString();
        char otherSeparator = File.separatorChar == '/' ? '\\' : '/';
        debugFilename = debugFilename.replace(otherSeparator, File.separatorChar);
        return debugFilename.replace(';', File.separatorChar);
    }

    /**
     * Find an instruction in the given block, working 
     * forwards or backwards from the specified offset.
     * @param b the block of interest.
     * @param opcode the opcode of the desired Instruction.
     * @param initialOffset the offset at which to begin searching.
     * @param direction Forward or Backward.
     * @return the first instruction encountered with the specified
     * opcode, or null if none found or the offset is out of range.
     */
    private Instruction searchBlock(IBasicBlock b, int opcode, int initialOffset, SearchDirection direction)
    {
        Instruction result = null;
        int blockSize = b.size();

        assert initialOffset >= 0 && initialOffset < blockSize: String.format("invalid initialOffset %d", initialOffset);

        int offset = initialOffset;
        while ( result == null && offset >= 0 && offset < blockSize )
        {
            Instruction candidate = b.get(offset);

            if ( candidate.getOpcode() == opcode )
                result = candidate;
            else if ( direction == SearchDirection.Forward )
                offset++;
            else
                offset--;
        }

        return result;
    }
}
