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

import static org.apache.royale.abc.ABCConstants.*;

/**
 * The InstructionFactory creates implementation Instruction subclasses for
 * particular types of Instructions, and also handles caching and pooling of
 * shared Instructions.
 */
public abstract class InstructionFactory
{
    /*
     * Singleton instructions. Any Instruction without operands can be shared as
     * a singleton.
     */
    private static final Instruction s_add = new NoOperandsInstruction(OP_add);
    private static final Instruction s_astypelate = new NoOperandsInstruction(OP_astypelate);
    private static final Instruction s_bitand = new NoOperandsInstruction(OP_bitand);
    private static final Instruction s_bitnot = new NoOperandsInstruction(OP_bitnot);
    private static final Instruction s_bitor = new NoOperandsInstruction(OP_bitor);
    private static final Instruction s_bitxor = new NoOperandsInstruction(OP_bitxor);
    private static final Instruction s_checkfilter = new NoOperandsInstruction(OP_checkfilter);
    private static final Instruction s_coerce = new NoOperandsInstruction(OP_coerce);
    private static final Instruction s_coerce_a = new NoOperandsInstruction(OP_coerce_a);
    private static final Instruction s_coerce_s = new NoOperandsInstruction(OP_coerce_s);
    private static final Instruction s_convert_b = new NoOperandsInstruction(OP_convert_b);
    private static final Instruction s_convert_d = new NoOperandsInstruction(OP_convert_d);
    private static final Instruction s_convert_i = new NoOperandsInstruction(OP_convert_i);
    private static final Instruction s_convert_o = new NoOperandsInstruction(OP_convert_o);
    private static final Instruction s_convert_s = new NoOperandsInstruction(OP_convert_s);
    private static final Instruction s_convert_u = new NoOperandsInstruction(OP_convert_u);
    private static final Instruction s_decrement = new NoOperandsInstruction(OP_decrement);
    private static final Instruction s_decrement_i = new NoOperandsInstruction(OP_decrement_i);
    private static final Instruction s_divide = new NoOperandsInstruction(OP_divide);
    private static final Instruction s_dup = new NoOperandsInstruction(OP_dup);
    private static final Instruction s_dxnslate = new NoOperandsInstruction(OP_dxnslate);
    private static final Instruction s_equals = new NoOperandsInstruction(OP_equals);
    private static final Instruction s_esc_xattr = new NoOperandsInstruction(OP_esc_xattr);
    private static final Instruction s_esc_xelem = new NoOperandsInstruction(OP_esc_xelem);
    private static final Instruction s_getglobalscope = new NoOperandsInstruction(OP_getglobalscope);
    private static final Instruction s_getlocal0 = new NoOperandsInstruction(OP_getlocal0);
    private static final Instruction s_getlocal1 = new NoOperandsInstruction(OP_getlocal1);
    private static final Instruction s_getlocal2 = new NoOperandsInstruction(OP_getlocal2);
    private static final Instruction s_getlocal3 = new NoOperandsInstruction(OP_getlocal3);
    private static final Instruction s_greaterequals = new NoOperandsInstruction(OP_greaterequals);
    private static final Instruction s_greaterthan = new NoOperandsInstruction(OP_greaterthan);
    private static final Instruction s_hasnext = new NoOperandsInstruction(OP_hasnext);
    private static final Instruction s_in = new NoOperandsInstruction(OP_in);
    private static final Instruction s_increment = new NoOperandsInstruction(OP_increment);
    private static final Instruction s_increment_i = new NoOperandsInstruction(OP_increment_i);
    private static final Instruction s_instanceof = new NoOperandsInstruction(OP_instanceof);
    private static final Instruction s_istypelate = new NoOperandsInstruction(OP_istypelate);
    private static final Instruction s_label = new NoOperandsInstruction(OP_label);
    private static final Instruction s_lessequals = new NoOperandsInstruction(OP_lessequals);
    private static final Instruction s_lessthan = new NoOperandsInstruction(OP_lessthan);
    private static final Instruction s_lshift = new NoOperandsInstruction(OP_lshift);
    private static final Instruction s_modulo = new NoOperandsInstruction(OP_modulo);
    private static final Instruction s_multiply = new NoOperandsInstruction(OP_multiply);
    private static final Instruction s_multiply_i = new NoOperandsInstruction(OP_multiply_i);
    private static final Instruction s_negate = new NoOperandsInstruction(OP_negate);
    private static final Instruction s_negate_i = new NoOperandsInstruction(OP_negate_i);
    private static final Instruction s_newactivation = new NoOperandsInstruction(OP_newactivation);
    private static final Instruction s_nextname = new NoOperandsInstruction(OP_nextname);
    private static final Instruction s_nextvalue = new NoOperandsInstruction(OP_nextvalue);
    private static final Instruction s_nop = new NoOperandsInstruction(OP_nop);
    private static final Instruction s_not = new NoOperandsInstruction(OP_not);
    private static final Instruction s_pop = new NoOperandsInstruction(OP_pop);
    private static final Instruction s_popscope = new NoOperandsInstruction(OP_popscope);
    private static final Instruction s_pushfalse = new NoOperandsInstruction(OP_pushfalse);
    private static final Instruction s_pushnan = new NoOperandsInstruction(OP_pushnan);
    private static final Instruction s_pushnull = new NoOperandsInstruction(OP_pushnull);
    private static final Instruction s_pushscope = new NoOperandsInstruction(OP_pushscope);
    private static final Instruction s_pushtrue = new NoOperandsInstruction(OP_pushtrue);
    private static final Instruction s_pushundefined = new NoOperandsInstruction(OP_pushundefined);
    private static final Instruction s_pushwith = new NoOperandsInstruction(OP_pushwith);
    private static final Instruction s_returnvalue = new NoOperandsInstruction(OP_returnvalue);
    private static final Instruction s_returnvoid = new NoOperandsInstruction(OP_returnvoid);
    private static final Instruction s_rshift = new NoOperandsInstruction(OP_rshift);
    private static final Instruction s_setlocal0 = new NoOperandsInstruction(OP_setlocal0);
    private static final Instruction s_setlocal1 = new NoOperandsInstruction(OP_setlocal1);
    private static final Instruction s_setlocal2 = new NoOperandsInstruction(OP_setlocal2);
    private static final Instruction s_setlocal3 = new NoOperandsInstruction(OP_setlocal3);
    private static final Instruction s_strictequals = new NoOperandsInstruction(OP_strictequals);
    private static final Instruction s_subtract = new NoOperandsInstruction(OP_subtract);
    private static final Instruction s_subtract_i = new NoOperandsInstruction(OP_subtract_i);
    private static final Instruction s_swap = new NoOperandsInstruction(OP_swap);
    private static final Instruction s_throw = new NoOperandsInstruction(OP_throw);
    private static final Instruction s_typeof = new NoOperandsInstruction(OP_typeof);
    private static final Instruction s_urshift = new NoOperandsInstruction(OP_urshift);
    private static final Instruction s_unplus = new NoOperandsInstruction(OP_unplus);

    private static final Object[] NO_OPERANDS = new Object[0];

    /**
     * @return the singleton instance of an instruction with no operands, or a
     * "blank" instruction whose operands are to be filled in.
     */
    public static final Instruction getInstruction(int opcode)
    {
        switch (opcode)
        {
            case OP_add:
                return s_add;
            case OP_astypelate:
                return s_astypelate;
            case OP_bitand:
                return s_bitand;
            case OP_bitnot:
                return s_bitnot;
            case OP_bitor:
                return s_bitor;
            case OP_bitxor:
                return s_bitxor;
            case OP_checkfilter:
                return s_checkfilter;
            case OP_coerce:
                return s_coerce;
            case OP_coerce_a:
                return s_coerce_a;
            case OP_coerce_s:
                return s_coerce_s;
            case OP_convert_b:
                return s_convert_b;
            case OP_convert_d:
                return s_convert_d;
            case OP_convert_i:
                return s_convert_i;
            case OP_convert_o:
                return s_convert_o;
            case OP_convert_s:
                return s_convert_s;
            case OP_convert_u:
                return s_convert_u;
            case OP_decrement:
                return s_decrement;
            case OP_decrement_i:
                return s_decrement_i;
            case OP_divide:
                return s_divide;
            case OP_dup:
                return s_dup;
            case OP_dxnslate:
                return s_dxnslate;
            case OP_equals:
                return s_equals;
            case OP_esc_xattr:
                return s_esc_xattr;
            case OP_esc_xelem:
                return s_esc_xelem;
            case OP_getglobalscope:
                return s_getglobalscope;
            case OP_getlocal0:
                return s_getlocal0;
            case OP_getlocal1:
                return s_getlocal1;
            case OP_getlocal2:
                return s_getlocal2;
            case OP_getlocal3:
                return s_getlocal3;
            case OP_greaterequals:
                return s_greaterequals;
            case OP_greaterthan:
                return s_greaterthan;
            case OP_hasnext:
                return s_hasnext;
            case OP_in:
                return s_in;
            case OP_increment:
                return s_increment;
            case OP_increment_i:
                return s_increment_i;
            case OP_instanceof:
                return s_instanceof;
            case OP_istypelate:
                return s_istypelate;
            case OP_label:
                return s_label;
            case OP_lessequals:
                return s_lessequals;
            case OP_lessthan:
                return s_lessthan;
            case OP_lshift:
                return s_lshift;
            case OP_modulo:
                return s_modulo;
            case OP_multiply:
                return s_multiply;
            case OP_multiply_i:
                return s_multiply_i;
            case OP_negate:
                return s_negate;
            case OP_negate_i:
                return s_negate_i;
            case OP_newactivation:
                return s_newactivation;
            case OP_nextname:
                return s_nextname;
            case OP_nextvalue:
                return s_nextvalue;
            case OP_nop:
                return s_nop;
            case OP_not:
                return s_not;
            case OP_pop:
                return s_pop;
            case OP_popscope:
                return s_popscope;
            case OP_pushfalse:
                return s_pushfalse;
            case OP_pushnan:
                return s_pushnan;
            case OP_pushnull:
                return s_pushnull;
            case OP_pushscope:
                return s_pushscope;
            case OP_pushtrue:
                return s_pushtrue;
            case OP_pushundefined:
                return s_pushundefined;
            case OP_pushwith:
                return s_pushwith;
            case OP_returnvalue:
                return s_returnvalue;
            case OP_returnvoid:
                return s_returnvoid;
            case OP_rshift:
                return s_rshift;
            case OP_setlocal0:
                return s_setlocal0;
            case OP_setlocal1:
                return s_setlocal1;
            case OP_setlocal2:
                return s_setlocal2;
            case OP_setlocal3:
                return s_setlocal3;
            case OP_strictequals:
                return s_strictequals;
            case OP_subtract:
                return s_subtract;
            case OP_subtract_i:
                return s_subtract_i;
            case OP_swap:
                return s_swap;
            case OP_throw:
                return s_throw;
            case OP_typeof:
                return s_typeof;
            case OP_urshift:
                return s_urshift;
            case OP_unplus:
                return s_unplus;
            default:
                //  TODO: This can be tightened up by isolating the
                //  instructions that require a "blank" instruction
                //  (find/get/set/deleteproperty variants) and 
                //  asserting on others.
                return new ArbitraryOperandsInstruction(opcode, NO_OPERANDS);
        }
    }

    /**
     * @return an Instruction with an immediate operand.
     */
    public static final Instruction getInstruction(int opcode, int immediate)
    {
        return new ImmediateOperandInstruction(opcode, immediate);
    }

    /**
     * @return an immediate-operand instruction in cases where the immediate
     * operand is not yet known.
     */
    public static final Instruction getDeferredImmediateInstruction(int opcode)
    {
        return new ImmediateOperandInstruction(opcode);
    }

    /**
     * @return an Instruction with a single operand.
     */
    public static final Instruction getInstruction(int opcode, Object singleOperand)
    {
        return new OneOperandInstruction(opcode, singleOperand);
    }

    /**
     * @return a single-operand instruction, with the operand TBA.
     */
    public static final Instruction getTargetableInstruction(int opcode)
    {
        return new OneOperandInstruction(opcode);
    }

    /**
     * @return an Instruction with an arbitrary number of operands.
     */
    public static final Instruction getInstruction(int opcode, Object[] operands)
    {
        return new ArbitraryOperandsInstruction(opcode, operands);
    }

    /**
     * @return a hasNext2 instruction.
     */
    public static final Instruction getHasnext2Instruction()
    {
        return new ArbitraryOperandsInstruction(OP_hasnext2, NO_OPERANDS);
    }

    /**
     * Copy an instruction's operands as necessary and substitute a new opcode.
     * 
     * @param opcode - the opcode required.
     * @param original - the original instruction.
     * @return an instruction with the original instruction's operands, whatever
     * they may be, and the specified novel opcode.
     */
    public static final Instruction createModifiedInstruction(int opcode, Instruction original)
    {
        if (original instanceof ArbitraryOperandsInstruction)
            return new ArbitraryOperandsInstruction(opcode, (ArbitraryOperandsInstruction)original);

        else if (original instanceof OneOperandInstruction)
            return getInstruction(opcode, original.getOperand(0));

        else if (original instanceof ImmediateOperandInstruction)
            return getInstruction(opcode, original.getImmediate());

        else
             return getInstruction(opcode);
    }
}
