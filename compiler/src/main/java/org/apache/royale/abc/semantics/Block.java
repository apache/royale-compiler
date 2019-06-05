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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.graph.IBasicBlock;

/**
 * Block implements {@link IBasicBlock} and is
 * the normal representation of a vertex in a
 * method's flowgraph.
 */
public class Block implements IBasicBlock
{
    /**
     * Instructions in this block. A Block created by a ControlFlowGraph has one
     * targetable instruction as its last instruction. A Block that has been
     * modified to have a targetable instruction in its interior will have
     * unpredictable results until it's serialized into a new InstructionList
     * and the ControlFlowGraph rebuilt.
     */
    private ArrayList<Instruction> instructions = new ArrayList<Instruction>();

    /**
     * Successors to this block.
     */
    private Collection<IBasicBlock> successors = Collections.emptySet();

    /**
     * @return successors of this block.
     */
    public Collection<IBasicBlock> getSuccessors()
    {
        return this.successors;
    }

    /**
     * block number assigned by ControlFlowGraph.  So far,
     * only used to try to guarantee order in lists/collections
     */
    public int blocknum;
    
    /**
     * Add a successor to this block.
     * 
     * @param succ - the successor block.
     */
    void addSuccessor(IBasicBlock succ)
    {
        if (this.successors.size() == 0)
            this.successors = new ArrayList<IBasicBlock>();

        this.successors.add(succ);
    }

    void add(Instruction insn)
    {
        this.instructions.add(insn);
    }

    /**
     * @return this Block's instructions' size.
     */
    public int size()
    {
        return this.instructions.size();
    }

    /**
     * @return the Instruction at the specified index.
     */
    public Instruction get(int idx)
    {
        return this.instructions.get(idx);
    }

    /**
     * @return this Block's instructions as a mutable list.
     */
    public List<Instruction> getInstructions()
    {
        return this.instructions;
    }

    /**
     * Determines if control can fall through the end of the Block.
     * This will look back through the Block's instructions,
     * skipping any non-exectuable instructions such as debug instructions.
     * Returns, throws, and unconditional branches will not fall through, but
     * conditional branches and all other instructions will fall through.
     * 
     * @return true if control can fall through this Block.
     */
    public boolean canFallThrough()
    {
        //  Look for the last executable instruction;
        //  skip non-executable instructions, e.g., debug
        for ( int i = this.instructions.size() -1; i >= 0; i-- )
        {
            Instruction insn = this.instructions.get(i);

            if (insn.isExecutable())
            {
                int prev_op = insn.getOpcode();
                if (
                    prev_op == ABCConstants.OP_jump ||
                    prev_op == ABCConstants.OP_throw ||
                    insn.isReturn())
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
        }

        //  No executable instructions in this block.
        return true;
    }
}
