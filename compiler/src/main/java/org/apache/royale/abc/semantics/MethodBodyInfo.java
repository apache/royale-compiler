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

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.IFlowgraph;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.visitors.IDiagnosticsVisitor;

import static org.apache.royale.abc.ABCConstants.*;

/**
 * A representation of a method's <a href="http://learn.adobe.com/wiki/display/AVM2/4.11+Method+body">method body information</a>.
 */
public class MethodBodyInfo
{
    /**
     * Construct a new MethodBodyInfo.
     */
    public MethodBodyInfo()
    {
    }

    MethodInfo methodInfo;

    public int max_stack;
    public int max_scope;
    public int initial_scope;
    public int max_local;
    public int max_slot;
    public int code_len;

    Integer explicit_max_stack;
    Integer explicit_init_scope;
    Integer explicit_max_scope;
    Integer explicit_max_local;
    Integer explicit_max_slot;

    Traits traits = new Traits();

    /**
     * Does this method body contain a newclass instruction? Higher-level code
     * needs to know this because the ABC may have its class info structures
     * sorted into dependency order, which would invalidate the index in the
     * newclass instruction if the method body were serialized to bytecode.
     */
    private boolean hasNewclass = false;

    private Vector<ExceptionInfo> exceptions = new Vector<ExceptionInfo>();

    /**
     * The method's instructions as a simple list.
     */
    InstructionList instructions = new InstructionList();

    /**
     * The method's control flow graph. The ControlFlowGraph is only available
     * and valid when the method's instructions are in their deserialized form.
     */
    IFlowgraph cfg = null;

    /**
     * The method's instructions after being converted into ABC bytecode.
     */
    byte[] bytecode = null;

    public MethodInfo getMethodInfo()
    {
        return this.methodInfo;
    }

    public void setMethodInfo(MethodInfo minfo)
    {
        this.methodInfo = minfo;
    }

    /**
     * @return the method's traits.
     */
    public Traits getTraits()
    {
        return traits;
    }

    /**
     * Set the method's traits.
     * 
     * @param t - the method's traits.
     */
    public void setTraits(Traits t)
    {
        this.traits = t;
    }

    /**
     * Set this method's serialized ABC.
     * 
     * @param bytecode - the method's instructions serialized to ABC bytecode.
     * @post the method's InstructionList based bytecode is no longer available.
     */
    public void setBytecode(final byte[] bytecode)
    {
        this.bytecode = bytecode;
        this.instructions = null;
        this.cfg = null;
    }

    /**
     * Retrieve this method's bytecode.
     * 
     * @return previously serialized bytecode.
     * @pre setBytecode() called to store the bytecode.
     */
    public byte[] getBytecode()
    {
        assert this.bytecode != null;
        return this.bytecode;
    }

    /**
     * Has this method been serialized to bytecode?
     * 
     * @return true if the method's serialized bytecode is set.
     */
    public boolean hasBytecode()
    {
        return this.bytecode != null;
    }

    /**
     * Get the method's control flow graph, building it as necessary.
     * 
     * @return the method's control flow graph.
     * @pre the method must not have been serialized to ABC.
     */
    public IFlowgraph getCfg()
    {
        if (null == this.cfg)
            rebuildCfg();

        return this.cfg;
    }

    /**
     * Rebuild the control flow graph.
     */
    private void rebuildCfg()
    {
        //  Note that it would be possible to deserialize the method and then construct a CFG.
        assert instructions != null : "Unable to build control flow graph after serialization to ABC";
        this.cfg = new ControlFlowGraph(this);
    }

    public Instruction insn(int opcode)
    {
        return this.instructions.addInstruction(opcode);
    }

    public Instruction insn(int opcode, Object[] operands)
    {
        Instruction result = InstructionFactory.getInstruction(opcode, operands);
        this.instructions.addInstruction(result);
        return result;
    }

    public Instruction insn(int opcode, Object pooledValue)
    {
        Instruction i = InstructionFactory.getInstruction(opcode, pooledValue);
        this.instructions.addInstruction(i);
        return i;
    }

    public Instruction insn(int opcode, int immed)
    {
        Instruction i = InstructionFactory.getInstruction(opcode, immed);
        this.instructions.addInstruction(i);
        return i;
    }

    public void insn(Instruction instruction)
    {
        this.instructions.addInstruction(instruction);
    }

    /**
     * Compute a functions's max_stack, max_scope, and slot count.
     * 
     * @note noop if all counts were explicitly provided.
     */
    public void computeFrameCounts(IDiagnosticsVisitor diagnosticsVisitor)
    {
        if (this.explicit_max_stack != null && this.explicit_max_scope != null && this.explicit_max_local != null && this.explicit_max_slot != null)
        {
            //  All counts explicitly provided.
            return;
        }

        FrameCountVisitor counts = new FrameCountVisitor(this, diagnosticsVisitor, this.initial_scope);
        getCfg().traverseGraph(counts);

        // Grrr..  TODO when we tighten up the max_stack,
        // max_scope, etc for code we read out of the flex
        // framework we seem to break that code.
        // For now, never let the max_stack, etc go down.
        this.max_stack = Math.max(counts.max_stack, this.max_stack);
        this.max_scope = Math.max(counts.max_scope, this.max_scope);
        this.max_local = Math.max(counts.max_local, this.max_local);
        this.max_slot = Math.max(counts.max_slot, this.max_slot);
        this.hasNewclass |= counts.hasNewclassInstruction();
    }

    public int getMaxStack()
    {
        if (explicit_max_stack != null)
            return explicit_max_stack;
        else
            return max_stack;
    }

    public int getLocalCount()
    {
        if (explicit_max_local != null)
            return explicit_max_local;
        else if ((this.methodInfo.getFlags() & NEED_REST) != 0)
            return max_local + 1;
        else
            return max_local;
    }

    public int getMaxSlotCount()
    {
        if (explicit_max_slot != null)
            return explicit_max_slot;
        else if (max_slot < traits.getTraitCount())
            return traits.getTraitCount();
        else
            return max_slot;
    }

    public int getMaxScopeDepth()
    {
        if (explicit_max_scope != null)
            return explicit_max_scope;
        else
            return max_scope;
    }

    public int addExceptionInfo(ExceptionInfo ex)
    {
        this.exceptions.add(ex);
        return this.exceptions.size() - 1; //  zero-based exception numbers
    }

    public int getInitScopeDepth()
    {
        if (this.explicit_init_scope != null)
            return explicit_init_scope;
        else
            return this.initial_scope;
    }

    /**
     * Get the block that the label corresponds to. Will throw an
     * IllegalArgumentException if the label does not correspond to any known
     * block.
     * 
     * @param target the label you want the block for
     * @return the block that the label refers to.
     */
    public IBasicBlock getBlock(Label target)
    {
        return getBlock(target, true);
    }

    /**
     * Get the block that the label corresponds to. Will throw an
     * IllegalArgumentException if the label does not correspond to any known
     * block, and throwOnError is true.
     * 
     * @param target the label you want the block for
     * @param throwOnError false if you do not want this method to throw an
     * IllegalArgumentException - in this case the method will return null
     * instead.
     * @return the block that the label refers to.
     */
    public IBasicBlock getBlock(Label target, boolean throwOnError)
    {
        IBasicBlock result = cfg.getBlock(target);

        if (result == null && throwOnError)
            throw new IllegalArgumentException("Label " + target.toString() + " was referenced, but never defined.");

        return result;
    }

    /**
     * @return exception handlers defined in this method.
     */
    public List<ExceptionInfo> getExceptions()
    {
        if (this.exceptions != null)
            return exceptions;
        else
            return Collections.emptyList();
    }

    /**
     * Does the given Label reference a catch target?
     * @param l - the Label of interest.
     * @return true if the Label references a catch target,
     *   either by identity or by position.
     */
    public boolean isCatchTarget(Label l)
    {
        for (ExceptionInfo e : exceptions)
        {
            Label target = e.getTarget();
            if ( l.getPosition() == target.getPosition() || l == target )
                return true;
        }
        return false;
    }

    /**
     * Set this method body's InstructionList. Used by code generators that
     * build up an InstructionList from smaller parts; not relevant if the
     * InstructionList is built by visitInstruction() calls on this object.
     */
    public void setInstructionList(InstructionList new_list)
    {
        this.instructions = new_list;

        if (this.cfg != null)
            rebuildCfg();
    }

    /**
     * @return this method body's InstructionList.
     */
    public InstructionList getInstructionList()
    {
        assert (this.instructions != null) : "No active InstructionList";
        return this.instructions;
    }

    /**
     * Pass through a labelCurrent() request to the resident InstructionList.
     */
    public void labelCurrent(Label l)
    {
        assert (this.instructions != null) : "No active InstructionList";
        this.instructions.labelCurrent(l);
    }

    /**
     * Pass through a labelNext() request to the resident InstructionList.
     */
    public void labelNext(Label l)
    {
        assert (this.instructions != null) : "No active InstructionList";
        this.instructions.labelNext(l);
    }

    /**
     * Does this method body contain a newclass instruction?
     * 
     * @pre computeFrameCounts() must have been called.
     */
    public boolean hasNewclassInstruction()
    {
        return this.hasNewclass;
    }
}
