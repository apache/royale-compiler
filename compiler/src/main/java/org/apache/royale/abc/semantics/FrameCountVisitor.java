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

import java.util.HashMap;
import java.util.Map;

import static org.apache.royale.abc.ABCConstants.*;
import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.IFlowgraph;
import org.apache.royale.abc.visitors.IDiagnosticsVisitor;
import org.apache.royale.abc.visitors.IFlowGraphVisitor;

/**
 * A FrameCountVisitor tracks the stack, scope, local, and slot numbers
 * encountered in a method body, so that the MethodBodyInfo can set its
 * max_stack, max_scope, max_local, and max_slot values.
 */
public class FrameCountVisitor implements IFlowGraphVisitor
{
    /**
     * Construct a new FrameCountVisitor.
     * 
     * @param mbi - the MethodBodyInfo to analyze.
     * @param diagnosticsVisitor - a sink for diagnostics.
     * @param initial_scope - caller's a priori initial scope depth.
     */
    FrameCountVisitor(MethodBodyInfo mbi, IDiagnosticsVisitor diagnosticsVisitor, int initial_scope)
    {
        this.diagnosticsVisitor = diagnosticsVisitor;
        this.cfg = mbi.getCfg();
        this.mbi = mbi;
        this.initial_scope = initial_scope;
        this.exceptions = mbi.getExceptions();
        this.instructionIndex = 0;
    }

    /**
     * The MethodBodyInfo that generated the IFlowgraph.
     */
    final MethodBodyInfo mbi;

    /**
     * Receiver of any diagnostic output.
     */
    final IDiagnosticsVisitor diagnosticsVisitor;

    /**
     * The control flow graph, denormalized from the MethodBodyInfo.
     */
    final IFlowgraph cfg;

    /**
     * Caller's a priori initial scope depth.
     */
    final int initial_scope;

    /**
     * Exception information from the method body.
     */
    final Iterable<ExceptionInfo> exceptions;

    int max_stack;
    int max_local;
    int max_scope;
    int max_slot;

    /**
     * Set if we encounter a newclass instruction.
     */
    boolean hasNewclass = false;

    /**
     * Stack depths on entry to blocks
     */
    Map<IBasicBlock, Integer> stkin = new HashMap<IBasicBlock, Integer>();

    /**
     * Scope depths on entry to blocks
     */
    Map<IBasicBlock, Integer> scpin = new HashMap<IBasicBlock, Integer>();

    int stkdepth = 0;
    int scpdepth = 0;

    int instructionIndex;

    IBasicBlock currentBlock;

    /**
     * Visit a new Block.
     * 
     * @param b - the Block to visit.
     * @return true if the walker should continue visiting the block.
     */
    public boolean visitBlock(IBasicBlock b)
    {
        this.currentBlock = b;

        if (this.cfg.isCatchTarget(b))
        {
            stkdepth = 1;
            scpdepth = 0;
        }
        else if (stkin.containsKey(b))
        {
            stkdepth = stkin.get(b);
            scpdepth = scpin.get(b);
        }
        // else use the current values.
        return true;
    }

    /**
     * Visit an Instruction within the most recently-visited Block.
     * 
     * @param i - the Instruction.
     */
    public void visitInstruction(Instruction i)
    {
        switch (i.opcode)
        {
            case OP_add:
            case OP_add_i:
            case OP_astypelate:
            case OP_bitand:
            case OP_bitor:
            case OP_bitxor:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;

            case OP_applytype:
            case OP_astype:
            case OP_bitnot:
            case OP_bkpt:
            case OP_bkptline:
            case OP_checkfilter:
            case OP_coerce:
            case OP_coerce_a:
            case OP_coerce_i:
            case OP_coerce_d:
            case OP_coerce_s:
            case OP_coerce_u:
            case OP_convert_b:
            case OP_convert_d:
            case OP_convert_i:
            case OP_convert_o:
            case OP_convert_s:
            case OP_convert_u:
            case OP_debug:
            case OP_debugfile:
            case OP_debugline:
            case OP_declocal:
            case OP_declocal_i:
            case OP_decrement:
            case OP_decrement_i:
            case OP_dxns:
            case OP_esc_xattr:
            case OP_esc_xelem:
            case OP_inclocal:
            case OP_inclocal_i:
            case OP_increment:
            case OP_increment_i:
            case OP_jump:
            case OP_kill:
            case OP_label:
            case OP_negate:
            case OP_negate_i:
            case OP_returnvoid:
            case OP_nop:
            case OP_not:
            case OP_swap:
            case OP_timestamp:
            case OP_typeof:
            case OP_unplus:
                // Net effect zero.
                break;

            case OP_call:
                stkdepth = adjustValueStack(stkdepth, -(i.getImmediate() + 1));
                break;

            case OP_callmethod:
                assert false : "internal only instruction!";
                stkdepth = adjustValueStack(stkdepth, -i.getImmediate());
                break;

            case OP_callstatic:
                stkdepth = adjustValueStack(stkdepth, -((Integer)i.getOperand(1)));
                break;

            case OP_callproperty:
            case OP_callproplex:
            case OP_callsuper:
            case OP_constructprop:
                stkdepth = adjustValueStack(stkdepth, -((Integer)i.getOperand(1)) + runtimeNameAllowance((Name)i.getOperand(0)));
                break;

            case OP_callpropvoid:
            case OP_callsupervoid:
                // void calls do not push a result
                stkdepth = adjustValueStack(stkdepth, -((Integer)i.getOperand(1)) + runtimeNameAllowance((Name)i.getOperand(0)) - 1);
                break;

            case OP_construct:
                stkdepth = adjustValueStack(stkdepth, -i.getImmediate());
                break;

            case OP_constructsuper:
                stkdepth = adjustValueStack(stkdepth, -i.getImmediate() - 1);
                break;

            case OP_deleteproperty:
                stkdepth = adjustValueStack(stkdepth, runtimeNameAllowance((Name)i.getOperand(0)));
                break;

            case OP_divide:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;
            case OP_dup:
                stkdepth = adjustValueStack(stkdepth, 1);
                break;
            case OP_dxnslate:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;
            case OP_equals:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;
            case OP_finddef:
            case OP_findproperty:
            case OP_findpropstrict:
                stkdepth = adjustValueStack(stkdepth, 1 + runtimeNameAllowance((Name)i.getOperand(0)));
                break;

            case OP_getdescendants:
            case OP_getproperty:
            case OP_getsuper:
                stkdepth = adjustValueStack(stkdepth, runtimeNameAllowance((Name)i.getOperand(0)));
                break;

            case OP_getglobalscope:
            case OP_getglobalslot:
            case OP_getlex:
            case OP_getouterscope:
                stkdepth = adjustValueStack(stkdepth, 1);
                break;

            case OP_getlocal:
                stkdepth = adjustValueStack(stkdepth, 1);
                adjustMaxLocal(i.getImmediate());
                if (i.getImmediate() < 4)
                    i.opcode = OP_getlocal0 + i.getImmediate();
                break;
            case OP_getlocal0:
            case OP_getlocal1:
            case OP_getlocal2:
            case OP_getlocal3:
                stkdepth = adjustValueStack(stkdepth, 1);
                adjustMaxLocal(i.opcode - OP_getlocal0);
                break;

            case OP_getslot:
                if (i.getImmediate() > max_slot)
                    max_slot = i.getImmediate();
                break;

            case OP_getscopeobject:
                stkdepth = adjustValueStack(stkdepth, 1);
                break;

            case OP_greaterequals:
            case OP_greaterthan:
            case OP_hasnext:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;
            case OP_hasnext2:
            {
                //  Both hasnext2 operands are locals.
                adjustMaxLocal((Integer)i.getOperand(0));
                adjustMaxLocal((Integer)i.getOperand(1));
                stkdepth = adjustValueStack(stkdepth, 1);
                break;
            }
            case OP_ifeq:
            case OP_ifge:
            case OP_ifgt:
            case OP_ifle:
            case OP_iflt:
            case OP_ifnge:
            case OP_ifngt:
            case OP_ifnle:
            case OP_ifnlt:
            case OP_ifne:
            case OP_ifstricteq:
            case OP_ifstrictne:
                stkdepth = adjustValueStack(stkdepth, -2);
                break;
            case OP_iffalse:
            case OP_iftrue:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;

            case OP_in:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;

            case OP_initproperty:
                stkdepth = adjustValueStack(stkdepth, -2 + runtimeNameAllowance((Name)i.getOperand(0)));
                break;

            case OP_instanceof:
            case OP_istypelate:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;

            case OP_istype:
                stkdepth = adjustValueStack(stkdepth, runtimeNameAllowance((Name)i.getOperand(0)));
                break;

            case OP_lessequals:
            case OP_lessthan:
            case OP_lookupswitch:
            case OP_lshift:
            case OP_modulo:
            case OP_multiply:
            case OP_multiply_i:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;

            case OP_newactivation:
            case OP_newcatch:
            case OP_newfunction:
                stkdepth = adjustValueStack(stkdepth, 1);
                break;

            case OP_newarray:
                stkdepth = adjustValueStack(stkdepth, 1 - i.getImmediate());
                break;

            case OP_newobject:
                //  The operands are name-value pairs.
                stkdepth = adjustValueStack(stkdepth, 1 - (i.getImmediate() * 2));
                break;

            case OP_nextname:
            case OP_nextvalue:
            case OP_pop:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;

            case OP_popscope:
                scpdepth = adjustScopeStack(scpdepth, -1);
                break;

            case OP_pushbyte:
            case OP_pushdouble:
            case OP_pushfalse:
            case OP_pushint:
            case OP_pushnamespace:
            case OP_pushnan:
            case OP_pushnull:
            case OP_pushshort:
            case OP_pushstring:
            case OP_pushtrue:
            case OP_pushuint:
            case OP_pushundefined:
                stkdepth = adjustValueStack(stkdepth, 1);
                break;

            case OP_pushscope:
            case OP_pushwith:
                stkdepth = adjustValueStack(stkdepth, -1);
                scpdepth = adjustScopeStack(scpdepth, 1);
                break;

            case OP_returnvalue:
            case OP_rshift:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;

            case OP_setlocal:
                stkdepth = adjustValueStack(stkdepth, -1);
                adjustMaxLocal(i.getImmediate());
                if (i.getImmediate() < 4)
                    i.opcode = OP_setlocal0 + i.getImmediate();
                break;
            case OP_setlocal0:
            case OP_setlocal1:
            case OP_setlocal2:
            case OP_setlocal3:
                stkdepth = adjustValueStack(stkdepth, -1);
                adjustMaxLocal(i.opcode - OP_setlocal0);
                break;

            case OP_setglobalslot:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;

            case OP_setproperty:
            case OP_setsuper:
                stkdepth = adjustValueStack(stkdepth, -2 + runtimeNameAllowance((Name)i.getOperand(0)));
                break;

            case OP_setslot:
                stkdepth = adjustValueStack(stkdepth, -2);
                if (max_slot < i.getImmediate())
                    max_slot = i.getImmediate();
                break;

            case OP_strictequals:
            case OP_subtract:
            case OP_subtract_i:
            case OP_throw:
            case OP_urshift:
                stkdepth = adjustValueStack(stkdepth, -1);
                break;

            case OP_newclass:
                this.hasNewclass = true;
                break;

            case OP_li8:
            case OP_li16:
            case OP_li32:
            case OP_lf32:
            case OP_lf64:
            case OP_sxi1:
            case OP_sxi8:
            case OP_sxi16:
                // consume one stack entry, and produce 1 stack entry.
                break;
            case OP_si8:
            case OP_si16:
            case OP_si32:
            case OP_sf32:
            case OP_sf64:
                // consume two stack entries
                stkdepth = adjustValueStack(stkdepth, -2);
                break;

            case OP_callinterface:
            case OP_callsuperid:
            case OP_deletepropertylate:
            case OP_setpropertylate:
                assert false : "internal only instruction!";
                break;
            default:
                assert false : "unknown instruction!";
                break;
        }
        this.instructionIndex++;
    }

    public void visitEnd(IBasicBlock b)
    {
        for (IBasicBlock s : b.getSuccessors())
        {
            if (!stkin.containsKey(s))
            {
                stkin.put(s, stkdepth);
                scpin.put(s, scpdepth);
            }
        }
    }

    private int adjustValueStack(int stkdepth, int incr)
    {
        stkdepth += incr;

        if (stkdepth < 0)
            this.diagnosticsVisitor.operandStackUnderflow(this.mbi, this.cfg, this.currentBlock, instructionIndex);

        if (stkdepth > this.max_stack)
            this.max_stack = stkdepth;
        return stkdepth;
    }

    private int adjustScopeStack(int scpdepth, int incr)
    {
        scpdepth += incr;

        if (scpdepth < 0)
            this.diagnosticsVisitor.scopeStackUnderflow(this.mbi, this.cfg, this.currentBlock, instructionIndex);

        if (scpdepth > (this.max_scope - this.initial_scope))
        {
            this.max_scope = scpdepth + this.initial_scope;
        }
        return scpdepth;
    }

    private void adjustMaxLocal(int idx)
    {
        if (max_local <= idx)
            max_local = idx + 1;
    }

    /**
     * Examine a Name and compute the number of value stack elements it will
     * need in its evaluation.
     * 
     * @param operand - the runtime name. May be null if the operation can
     * function without a name operand.
     * @return the number of value stack elements evaluating this Name requires.
     */
    private int runtimeNameAllowance(Object operand)
    {
        return operand instanceof Name ?
                -((Name)operand).runtimeNameAllowance() :
                0;
    }

    public boolean hasNewclassInstruction()
    {
        return this.hasNewclass;
    }
}
