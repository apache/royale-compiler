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


/**
 * A OneOperandInstruction is an instruction that is known to have only one
 * operand.
 */
public class OneOperandInstruction extends Instruction
{
    /**
     * Construct a OneOperandInstruction.
     * 
     * @param opcode - the instruction's opcode.
     * @param operand - the instruction's operand.
     */
    public OneOperandInstruction(int opcode, Object operand)
    {
        super(opcode);
        this.operand = operand;
    }

    /**
     * Construct a OneOperandInstruction whose operand is to be filled in later.
     * 
     * @param opcode - the instruction's opcode. Must be one of the approved
     * opcodes.
     */
    public OneOperandInstruction(int opcode)
    {
        super(opcode);
        assert (isTargetableInstruction()) : String.format("Invalid deferred operand type %s", super.toString());
    }

    /**
     * The instruction's operand.
     */
    public Object operand;

    @Override
    public String toString()
    {
        return String.format("%s[%s]", super.toString(), this.operand);
    }

    @Override
    public int getOperandCount()
    {
        return 1;
    }

    @Override
    public boolean hasOperands()
    {
        return true;
    }

    @Override
    public Object getOperand(int index)
    {
        assert index == 0 : String.format("%s only has one operand, requested %d", super.toString(), index);
        return this.operand;
    }

    /**
     * Get the target of a branch instruction.
     * 
     * @return the target label, or null if not present.
     */
    @Override
    public Label getTarget()
    {
        return this.operand instanceof Label ? (Label)this.operand : null;
    }

    /**
     * Set the target of a branch instruction.
     * 
     * @param target - the AET Label this instruction targets.
     */
    @Override
    public void setTarget(Label target)
    {
        assert this.operand == null : String.format("%s target already set", this);
        this.operand = target;
    }
}
