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

import org.apache.royale.abc.ABCConstants;

/**
 * An ArbitraryOperandsInstruction holds an array of operands as specified by
 * the caller, or in some special cases as set by a fixup pass.
 */
public class ArbitraryOperandsInstruction extends Instruction
{
    /**
     * Construct an ArbitraryOperandsInstruction.
     * 
     * @param opcode - the opcode. Passed to superclass constructor.
     * @param operands - the operands. May be null.
     */
    public ArbitraryOperandsInstruction(int opcode, Object[] operands)
    {
        super(opcode);
        this.operands = operands;
    }

    /**
     * "Copy" construct an ArbitraryOperandsInstruction with a new opcode.
     * 
     * @param opcode - the opcode.
     * @param original - the original ArbitraryOperandsInstruction whose
     * operands are to be incorporated into the new instruction.
     */
    public ArbitraryOperandsInstruction(int opcode, ArbitraryOperandsInstruction original)
    {
        this(opcode, original.operands);
    }

    /**
     * The instruction's operands.
     */
    private Object[] operands;

    @Override
    public String toString()
    {
        StringBuffer result = new StringBuffer(super.toString());

        result.append("[");

        for (Object x : operands)
        {
            result.append(x);
            result.append(",");
        }
        result.append("]");

        return result.toString();
    }

    /**
     * Fetch an operand.
     * 
     * @param index - the index of the operand of interest.
     * @return the operand at the specified index.
     */
    @Override
    public Object getOperand(int index)
    {
        return this.operands[index];
    }

    /**
     * @return the instruction's operand count; if the operands are not yet
     * present, then return 0.
     */
    @Override
    public int getOperandCount()
    {
        return this.operands != null ? this.operands.length : 0;
    }

    /**
     * Set this instruction's operands in a fixup pass.
     * 
     * @param operands - the instruction's operands.
     */
    @Override
    public void setOperands(Object[] operands)
    {
        this.operands = operands;
    }

    /**
     * Set the temporary register operands of a hasnext2 instruction.
     */
    @Override
    public void setTempRegisters(Object[] tempregs)
    {
        assert opcode == ABCConstants.OP_hasnext2 : "Cannot set temp registers of " + this;
        this.operands = tempregs;
    }
}
