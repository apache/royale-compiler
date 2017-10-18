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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;

import org.apache.royale.abc.ABCConstants;

/**
 * A representation of an ABC instruction.
 * Note that an instance of Instruction may be shared or reused.
 */
public abstract class Instruction
{
    /*
     * Opcode mappings extracted from ABCConstants
     */
    private static Map<String, Integer> opcodeNameToOpcode;
    private static Map<Integer, String> opcodeToOpcodeName;

    //  Populate the opcode maps.
    static
    {
        opcodeNameToOpcode = new HashMap<String, Integer>();
        opcodeToOpcodeName = new HashMap<Integer, String>();
        loadOpcodes();
    }

    /**
     * Decode an opcode name into a numeric constant.
     * 
     * @param opcodeName - the name of the opcode.
     * @return the opcode's value, or -1 if it's not an opcode.
     */
    public static int decodeOpcodeName(String opcodeName)
    {
        String opcodeKey = opcodeName.toLowerCase();

        if (opcodeNameToOpcode.size() == 0)
            loadOpcodes();

        if (opcodeNameToOpcode.containsKey(opcodeKey))
            return opcodeNameToOpcode.get(opcodeKey);
        else
            return -1;
    }

    /**
     * Get the symbolic name of an opcode.
     * 
     * @return the opcode's symbolic name, or a hex representation if none
     * exists.
     */
    public static String decodeOp(int opcode)
    {
        if (opcodeToOpcodeName.containsKey(opcode))
            return opcodeToOpcodeName.get(opcode);
        else
            return "OP_" + Integer.toHexString(opcode);
    }

    /**
     * Look at fields of ABCConstants and get the mappings of OP_foo names to
     * their values.
     */
    private static void loadOpcodes()
    {
        //  Traverse the names of the OP_foo constants
        //  in ABCConstants and load their values.
        for (Field f : ABCConstants.class.getFields())
        {
            String field_name = f.getName();

            if (field_name.startsWith("OP_"))
            {
                String opcode = field_name.substring(3);
                try
                {
                    int field_value = f.getInt(null);
                    opcodeNameToOpcode.put(opcode, field_value);
                    opcodeToOpcodeName.put(field_value, opcode);
                }
                catch (Exception noFieldValue)
                {
                    //  Ignore, continue...
                }

            }
        }
    }

    /**
     * Constructor.
     */
    protected Instruction(int opcode)
    {
        this.opcode = opcode;
    }

    /**
     * @see ABCConstants
     */
    protected int opcode;

    /**
     * @return this instruction's opcode.
     * @see ABCConstants for opcodes.
     */
    public int getOpcode()
    {
        return this.opcode;
    }

    /**
     * determine if instruction has immediate operands.
     * 
     * @return true if the instruction has an immediate operand.
     */
    public boolean isImmediate()
    {
        return false;
    }

    /**
     * @return this Instruction's immediate operand.
     */
    public int getImmediate()
    {
        unsupported("%s has no immediate operand.");
        return -1;
    }

    /**
     * Get the target of a branch instruction.
     * 
     * @return the target label, or null if not present.
     */
    public Label getTarget()
    {
        return null;
    }

    /**
     * Set the target of a branch instruction.
     * 
     * @param target - the AET Label this instruction targets.
     */
    public void setTarget(Label target)
    {
        unsupported("Cannot set target on %s");
    }

    /**
     * Get one of the instruction's non-immediate operands.
     * 
     * @param index - the index of the desired operand.
     * @return the operand at the given index.
     * @throws IllegalStateException if the instruction has no operands.
     */
    public Object getOperand(int index)
    {
        unsupported("%s has no operands");
        return null;
    }

    /**
     * @return the number of operands this instruction has.
     */
    public int getOperandCount()
    {
        return 0;
    }

    /**
     * @return true if this instruction has operands (it may have zero operands,
     * but it supports them).
     */
    public boolean hasOperands()
    {
        return false;
    }

    /**
     * Set this Instruction's operands.
     * 
     * @pre hasOperands() must be true.
     */
    public void setOperands(Object[] operands)
    {
        unsupported("%s has no operands");
    }

    /**
     * Is this a branch instruction?
     * 
     * @return true if the instruction is a branch.
     */
    public boolean isBranch()
    {
        return ABCConstants.OP_lookupswitch == getOpcode() || getTarget() != null;
    }

    /**
     * Is this one of the return instructions?
     * 
     * @return true if this instruction is one of the return instructions
     */
    public boolean isReturn()
    {
        int opcode = getOpcode();
        return ABCConstants.OP_returnvalue == opcode || ABCConstants.OP_returnvoid == opcode;
    }

    /**
     *  Is this a transfer of control?
     *  @return true if isBranch() or isReturn() is true, or the opcode is OP_throw.
     */
    public boolean isTransferOfControl()
    {
        return isBranch() || isReturn() || ABCConstants.OP_throw == opcode;
    }

    /**
     * Set the immediate field of a local register access instruction.
     * 
     * @pre only valid for get/set/inc/declocal, hasnext, and kill.
     */
    public void setImmediate(final int immediate)
    {
        unsupported("%s has no immediate operand.");
    }

    /**
     * Set the temporary register operands of a hasnext2 instruction.
     */
    public void setTempRegisters(Object[] tempregs)
    {
        unsupported("cannot set temp registers of %s");
    }

    /**
     * @return true if this instruction is an executable (non-debug)
     * instruction.
     */
    public boolean isExecutable()
    {
        return this.opcode != ABCConstants.OP_debugline && this.opcode != ABCConstants.OP_debugfile;
    }

    /**
     * Test whether this instructions opcode is targetable
     * 
     * @return true if the instructions opcode is targetable
     */
    public boolean isTargetableInstruction()
    {
        switch (opcode)
        {
            case ABCConstants.OP_ifnlt:
            case ABCConstants.OP_ifnle:
            case ABCConstants.OP_ifngt:
            case ABCConstants.OP_ifnge:
            case ABCConstants.OP_iftrue:
            case ABCConstants.OP_iffalse:
            case ABCConstants.OP_ifeq:
            case ABCConstants.OP_ifne:
            case ABCConstants.OP_iflt:
            case ABCConstants.OP_ifle:
            case ABCConstants.OP_ifgt:
            case ABCConstants.OP_ifge:
            case ABCConstants.OP_ifstricteq:
            case ABCConstants.OP_ifstrictne:
            case ABCConstants.OP_jump:
            case ABCConstants.OP_lookupswitch:
                return true;
        }

        return false;
    }

    /**
     * @return a string representation of the opcode.
     */
    @Override
    public String toString()
    {
        return decodeOp(opcode);
    }

    protected void unsupported(String diagnostic)
    {
        throw new UnsupportedOperationException(String.format(diagnostic, toString()));
    }
}
