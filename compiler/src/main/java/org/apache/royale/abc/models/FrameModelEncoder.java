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

import static org.apache.royale.abc.ABCConstants.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.IFlowgraph;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.visitors.IDiagnosticsVisitor;
import org.apache.royale.abc.visitors.IFlowGraphVisitor;

/**
 *  The FrameModelEncoder abstracts ABC instructions into
 *  a sequence of operations on the method's "frame"
 *  (i.e., its locals, scope stack, and value stack).
 */
public class FrameModelEncoder implements IFlowGraphVisitor
{
    /**
     *  Construct a new FrameModelEncoder.
     *  @param mbi - the MethodBodyInfo to analyze.
     *  @param visitor - the FrameModelVisitor analyzing the method.
     *  @param diagnosticsVisitor - a sink for diagnostics.
     */
    public FrameModelEncoder(MethodBodyInfo mbi, FrameModelVisitor<?> visitor, IDiagnosticsVisitor diagnosticsVisitor)
    {
        this.cfg = mbi.getCfg();
        this.visitor = visitor;
    }

    private Set<IBasicBlock> visitedBlocks = new HashSet<IBasicBlock>();

    /**
     *  The visitor this encoder is driving.
     */
    private final FrameModelVisitor<?> visitor;

    /**
     *  The method's control flow graph, denormalized from its 
     */
    private final IFlowgraph cfg;

    /**
     *  Visit a Block.
     *  @param b - the IBasicBlock to visit.
     *  @return the visitor's decision re: visiting the block.
     */
    @Override
    public boolean visitBlock(IBasicBlock b)
    {   
        this.instructionIndex = 0;
        return this.visitedBlocks.add(b) && this.visitor.visitBlock(b);
    }

    /** 
     * The index of the current instruction in the current block.
     * This is maintained as a convenience for visitor clients.
     */
    private int instructionIndex = -1;

    /**
     * Get the index of the current instruction in the current block.
     * @return the index of the current instruction in the current block.
     */
    public int getInstructionIndex()
    {
        return this.instructionIndex;
    }

    /**
     *  Visit an Instruction within the most recently-visited Block.
     *  @param i - the Instruction.
     */
    @Override
    public void visitInstruction(Instruction i)
    {
        switch (i.getOpcode())
        {
            case OP_iflt:
            case OP_ifle:
            case OP_ifnlt:
            case OP_ifnle:
            case OP_ifgt:
            case OP_ifge:
            case OP_ifngt:
            case OP_ifnge:
            case OP_ifeq:
            case OP_ifstricteq:
            case OP_ifne:
            case OP_ifstrictne:
                visitor.consumeValue(i, 2);
                visitor.branch(i, cfg.getBlock(i.getTarget()));
                break;

            case OP_iftrue:
            case OP_iffalse:
                visitor.consumeValue(i, 1);
                visitor.branch(i, cfg.getBlock(i.getTarget()));
                break;

            case OP_jump:
                visitor.branch(i, cfg.getBlock(i.getTarget()));
                break;

            case OP_lookupswitch:
            {
                visitor.consumeValue(i,1);
                ArrayList<IBasicBlock> targets = new ArrayList<IBasicBlock>();

                for ( int j = 0; j < i.getOperandCount(); j++ )
                    if( i.getOperand(j) instanceof Label )
                    {
                        targets.add(cfg.getBlock((Label)i.getOperand(j)));
                    }

                visitor.multiwayBranch(i, targets);
            }
            break;

            case OP_throw:
                visitor.consumeValue(i, 1);
                break;

            case OP_returnvalue:
                visitor.consumeValue(i, 1);
                break;

            case OP_returnvoid:
                visitor.noFrameEffect(i);
                break;

            case OP_pushnull:
                visitor.produceValue(i);
                break;

            case OP_pushundefined:
                visitor.produceValue(i);
                break;

            case OP_pushtrue:
            case OP_pushfalse:
                visitor.produceValue(i);
                break;

            case OP_pushnan:
                visitor.produceValue(i);
                break;

            case OP_pushbyte:
            case OP_pushshort:
            case OP_pushint:
                visitor.produceValue(i);
                break;

            case OP_debugfile:
                visitor.noFrameEffect(i);
                break;

            case OP_dxns:
                visitor.noFrameEffect(i);
                break;

            case OP_dxnslate:
                visitor.consumeValue(i, 1);
                break;

            case OP_pushstring:
                visitor.produceValue(i);
                break;

            case OP_pushuint:
                visitor.produceValue(i);
                break;

            case OP_pushdouble:
                visitor.produceValue(i);
                break;

            case OP_pushnamespace:
                visitor.produceValue(i);
                break;

            case OP_setlocal0:
                visitor.setlocal(i, 0);
                break;
            case OP_setlocal1:
                visitor.setlocal(i, 1);
                break;
            case OP_setlocal2:
                visitor.setlocal(i, 2);
                break;
            case OP_setlocal3:
                visitor.setlocal(i, 3);
                break;
            case OP_setlocal:
                visitor.setlocal(i, i.getImmediate());
                break;

            case OP_getlocal0:
                visitor.getlocal(i, 0);
                break;
            case OP_getlocal1:
                visitor.getlocal(i, 1);
                break;
            case OP_getlocal2:
                visitor.getlocal(i, 2);
                break;
            case OP_getlocal3:
                visitor.getlocal(i, 3);
                break;
            case OP_getlocal:
                visitor.getlocal(i, i.getImmediate());
                break;

            case OP_kill:
                visitor.modifyLocal(i, i.getImmediate());
                break;

            case OP_inclocal:
            case OP_inclocal_i:
            case OP_declocal:
            case OP_declocal_i:
                visitor.modifyLocal(i, i.getImmediate());
                break;

            case OP_newfunction:
                visitor.produceValue(i);
                break;

            case OP_getlex:
                visitor.produceValue(i);
                break;

            case OP_findpropstrict:
            case OP_findproperty:
                visitor.consumeAndProduceValue(i, runtimeNameAllowance(i.getOperand(0)));
                break;

            case OP_newclass:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_finddef:
                visitor.consumeAndProduceValue(i, runtimeNameAllowance(i.getOperand(0)));
                break;

            case OP_setproperty:
            case OP_initproperty:
                visitor.consumeValue(i, 2 + runtimeNameAllowance(i.getOperand(0)));
                break;

            case OP_getproperty:
                visitor.consumeAndProduceValue(i, 1 + runtimeNameAllowance(i.getOperand(0)));
                break;

            case OP_getdescendants:
                visitor.consumeAndProduceValue(i, 1 + runtimeNameAllowance(i.getOperand(0)));
                break;

            case OP_checkfilter:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_deleteproperty:
                visitor.consumeAndProduceValue(i, 1 + runtimeNameAllowance(i.getOperand(0)));
                break;

            case OP_astype:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_astypelate:
                visitor.consumeAndProduceValue(i, 2);
                break;

            case OP_coerce:
                visitor.consumeAndProduceValue(i, 1);
                break;
            
            case OP_convert_b:
            case OP_coerce_b:
                visitor.consumeAndProduceValue(i, 1);
                break;
            
            case OP_coerce_o:
                visitor.consumeAndProduceValue(i, 1);
                break;
            
            case OP_coerce_a:
                visitor.consumeAndProduceValue(i, 1);
                break;
            
            case OP_convert_i:
            case OP_coerce_i:
                visitor.consumeAndProduceValue(i, 1);
                break;
            
            case OP_convert_u:
            case OP_coerce_u:
                visitor.consumeAndProduceValue(i, 1);
                break;
            
            case OP_convert_d:
            case OP_coerce_d:
                visitor.consumeAndProduceValue(i, 1);
                break;
            
            case OP_unplus:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_coerce_s:
                visitor.consumeAndProduceValue(i, 1);
                break;
            
            case OP_istype:
                visitor.consumeAndProduceValue(i, 1 + runtimeNameAllowance(i.getOperand(0)));
                break;

            case OP_istypelate:
                visitor.consumeAndProduceValue(i, 2);
                break;

            case OP_convert_o:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_convert_s:
            case OP_esc_xelem:
            case OP_esc_xattr:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_callstatic:
            {
                int argc = (Integer)i.getOperand(1);
                visitor.consumeAndProduceValue(i, argc+1);
            }
            break;

            case OP_call:
                visitor.consumeAndProduceValue(i, 2 + i.getImmediate());
                break;

            case OP_construct:
                visitor.consumeAndProduceValue(i, 1 + i.getImmediate());
                break;

            case OP_callmethod:
                visitor.consumeAndProduceValue(i, 1 + i.getImmediate());
                break;

            case OP_callproperty:
            case OP_callproplex:
                visitor.consumeAndProduceValue(i, 1 + runtimeNameAllowance(i.getOperand(0)) + (Integer)i.getOperand(1));
                break;

            case OP_callpropvoid:
                visitor.consumeValue(i, 1 + runtimeNameAllowance(i.getOperand(0)) + (Integer)i.getOperand(1));
                break;

            case OP_constructprop:
                visitor.consumeAndProduceValue(i, 1 + runtimeNameAllowance(i.getOperand(0)) + (Integer)i.getOperand(1));
                break;

            case OP_applytype:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_callsuper:
                visitor.consumeAndProduceValue(i, 1 + runtimeNameAllowance(i.getOperand(0)) + (Integer)i.getOperand(1));
                break;

            case OP_callsupervoid:
                visitor.consumeValue(i, 1 + runtimeNameAllowance(i.getOperand(0)) + (Integer)i.getOperand(1));
                break;

            case OP_getsuper:
                visitor.consumeAndProduceValue(i, 1 + runtimeNameAllowance(i.getOperand(0)));
                break;

            case OP_setsuper:
                visitor.consumeValue(i, 2 + runtimeNameAllowance(i.getOperand(0)));
                break;

            case OP_constructsuper:
                visitor.consumeAndProduceValue(i, 1 + i.getImmediate());
                break;

            case OP_newobject:
                visitor.consumeAndProduceValue(i, 2*i.getImmediate());
                break;

            case OP_newarray:
                visitor.consumeAndProduceValue(i, i.getImmediate());
                break;

            case OP_pushscope:
            case OP_pushwith:
                visitor.moveValueToScopeStack(i);
                break;

            case OP_newactivation:
                visitor.produceValue(i);
                break;

            case OP_newcatch:
                visitor.produceValue(i);
                break;
            
            case OP_popscope:
                visitor.popscope(i);
                break;

            case OP_getscopeobject:
                visitor.getScopeobject(i, i.getImmediate());
                break;

            case OP_getouterscope:
                // TODO: Need to model this correctly.
                visitor.getScopeobject(i, i.getImmediate());
                break;

            case OP_getglobalscope:
                visitor.getScopeobject(i, 0);
                break;

            case OP_getglobalslot:
                visitor.produceValue(i);
                break;

            case OP_setglobalslot:
                visitor.consumeValue(i, 1);
                break;

            case OP_getslot:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_setslot:
                visitor.consumeValue(i, 2);
                break;

            case OP_pop:
                visitor.consumeValue(i, 1);
                break;

            case OP_dup:
                visitor.dup(i);
                break;

            case OP_swap:
                visitor.swap(i);
                break;

            case OP_lessthan:
            case OP_greaterthan:
            case OP_lessequals:
            case OP_greaterequals:
            case OP_equals:
            case OP_strictequals:
            case OP_instanceof:
            case OP_in:
                visitor.consumeAndProduceValue(i, 2);
                break;

            case OP_not:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_add:
                visitor.consumeAndProduceValue(i, 2);
                break;

            case OP_modulo:
            case OP_subtract:
            case OP_divide:
            case OP_multiply:
                visitor.consumeAndProduceValue(i, 2);
                break;

            case OP_negate:
            case OP_increment:
            case OP_decrement:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_increment_i:
            case OP_decrement_i:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_add_i:
            case OP_subtract_i:
            case OP_multiply_i:
                visitor.consumeAndProduceValue(i, 2);
                break;

            case OP_negate_i:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_bitand:
            case OP_bitor:
            case OP_bitxor:
                visitor.consumeAndProduceValue(i, 2);
                break;

            case OP_lshift:
            case OP_rshift:
                visitor.consumeAndProduceValue(i, 2);
                break;

            case OP_urshift:
                visitor.consumeAndProduceValue(i, 2);
                break;

            case OP_bitnot:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_typeof:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_nop:
            case OP_bkpt:
            case OP_bkptline:
            case OP_timestamp:
            case OP_debug:
            case OP_label:
            case OP_debugline:
                visitor.noFrameEffect(i);
                break;

            case OP_nextvalue:
            case OP_nextname:
                visitor.consumeAndProduceValue(i, 2);
                break;

            case OP_hasnext:
                visitor.consumeAndProduceValue(i, 2);
                break;

            case OP_hasnext2:
                visitor.hasnext2(i);
                visitor.produceValue(i);
                break;
            
            case OP_sxi1:
            case OP_sxi8:
            case OP_sxi16:
                visitor.consumeAndProduceValue(i, 1);
                break;

            case OP_li8:
            case OP_li16:
            case OP_li32:
                visitor.consumeAndProduceValue(i, 1);
                break;
                
            case OP_lf32:
            case OP_lf64:
                visitor.consumeAndProduceValue(i, 1);
                break;

            /*
            case OP_lf32x4: 
                visitor.consumeAndProduceValue(i, 1);
                break;
            */

            case OP_si8:
            case OP_si16:
            case OP_si32:
            case OP_sf32:
            case OP_sf64:
                visitor.consumeValue(i, 2);
                break;

            /*
            case OP_sf32x4: 
                visitor.consumeValue(i, 2);
                break;
            */
                    
        case OP_callinterface:
        case OP_callsuperid:
        case OP_deletepropertylate:
        case OP_setpropertylate:
            assert false : "internal only instruction:" + i; //$NON-NLS-1$
            break;
        default:
            assert false : "unknown instruction:" + i; //$NON-NLS-1$
            break;
        }

        this.instructionIndex++;
    }

    @Override
    public void visitEnd(IBasicBlock b)
    {
        for ( IBasicBlock succ: b.getSuccessors() )
            visitor.visitEdge(b, succ);

        visitor.visitEndBlock(b);
    }
    
    
    /**
     *  Examine a Name and compute the number of
     *  value stack elements it will need 
     *  in its evaluation.
     *  @param operand - the runtime name.  May
     *    be null if the operation can function
     *    without a name operand.
     *  @return the number of value stack elements
     *    evaluating this Name requires.
     */
    private int runtimeNameAllowance(Object operand)
    {
        return operand instanceof Name?
            ((Name)operand).runtimeNameAllowance():
            0;
    }
}
