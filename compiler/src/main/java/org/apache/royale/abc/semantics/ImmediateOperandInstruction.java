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
 * An Instruction implementation with one immediate operand.
 */
public class ImmediateOperandInstruction extends Instruction
{
    /**
     * Set this to allow any immediate instruction to set its immediate field
     * after construction.
     */
    public static final boolean allowAnyDeferredIntermediate = false;

    public ImmediateOperandInstruction(int opcode, int immediate)
    {
        super(opcode);
        this.immediate = immediate;
    }

    public ImmediateOperandInstruction(int opcode)
    {
        super(opcode);
        verifyDeferredImmediate("%s requires an immediate operand.");
    }

    /**
     * The instruction's immediate operand.
     */
    private int immediate;

    @Override
    public String toString()
    {
        return String.format("%s(%d)", super.toString(), this.immediate);
    }

    /**
     * @return this Instruction's immediate operand.
     */
    @Override
    public int getImmediate()
    {
        return this.immediate;
    }

    /**
     * determine if instruction has immediate operands.
     * 
     * @return true, this type of Instruction has an immediate operand.
     */
    @Override
    public boolean isImmediate()
    {
        return true;
    }

    /**
     * Set the immediate field of a local register access instruction.
     * 
     * @pre only valid for get/set/inc/declocal, hasnext, and kill.
     */
    @Override
    public void setImmediate(final int immediate)
    {
        verifyDeferredImmediate("Cannot set immediate field on %s");
        this.immediate = immediate;
    }

    private void verifyDeferredImmediate(String diagnostic)
    {
        switch (opcode)
        {
            case ABCConstants.OP_getlocal:
            case ABCConstants.OP_setlocal:
            case ABCConstants.OP_inclocal:
            case ABCConstants.OP_inclocal_i:
            case ABCConstants.OP_declocal:
            case ABCConstants.OP_declocal_i:
            case ABCConstants.OP_hasnext:
            case ABCConstants.OP_kill:
            {
                //  Allow these.
                break;
            }
            default:
            {
                assert allowAnyDeferredIntermediate : String.format(diagnostic, toString());
            }
        }
    }
}
