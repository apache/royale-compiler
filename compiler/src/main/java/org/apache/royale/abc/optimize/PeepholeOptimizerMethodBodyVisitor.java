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

package org.apache.royale.abc.optimize;

import static org.apache.royale.abc.ABCConstants.*;

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.ECMASupport;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.InstructionFactory;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.visitors.DelegatingMethodBodyVisitor;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * IMethodBodyVisitor that will do peephole optimization of the method body.
 * TODO: Not copmlete yet - should be equivalent to the peephole optimizer in
 * MXMLC 4.5 Optimizations implemented: Eliminate unnecessary convert_d
 * instructions Eliminate unnecessary convert_b instructions Eliminate
 * unnecessary convert_i instructions Eliminate unnecessary convert_s
 * instructions Eliminate unnecessary convert_u instructions Replace callprops
 * where the return value is unused, with callpropvoid
 */
public class PeepholeOptimizerMethodBodyVisitor extends DelegatingMethodBodyVisitor
{
    /**
     * How many instructions we can look back on
     */
    private static final int PEEPHOLE_WINDOW_SIZE = 4;

    /**
     * Constant that means we don't have any labels in the peephole window
     */
    private static final int NO_LABEL = -1;

    public PeepholeOptimizerMethodBodyVisitor(IMethodBodyVisitor delegate)
    {
        super(delegate);
    }

    /**
     * Hold a small window of previous instructions so that the optimizer can
     * rewind and rewrite small sections of ABC
     */
    private List<InstructionInfo> instructions = new LinkedList<InstructionInfo>();

    /**
     * The index of the instruction with the most recently seen label - we can't
     * look back past a label.
     */
    private int lastLabelSeen = NO_LABEL;

    /**
     * If we delete instructions that have labeling actions associated with
     * them, we have to apply those labeling instructions to whatever
     * instruction ends up replacing the deleted instruction(s). This list holds
     * those labels.
     */
    private List<Label> labelsFromDeletedInsns = null;

    /**
     * basically holds a function object that sends instructions on to the right
     * place once the peephole optimizer is finished. This will either send the
     * instructions on to the next IMethodBodyVisitor, or to a new
     * InstructionList
     */
    private InstructionFinisher finisher = new DelegateFinisher();

    /**
     * Resets the entire method body at once. The Peephole optimizer will
     * process the entire instruction list, performing it's optimizations,
     * before passing the InstructionList down to the next IMethodBodyVisitor. So
     * any MethodBodyVisitors that the optimizer delegates to will receive a new
     * InstructionList, with optimizations, instead of the InstructionList
     * passed in.
     * 
     * @param new_list The new Instructions for the method.
     */
    @Override
    public void visitInstructionList(InstructionList new_list)
    {
        // for the delegates that run after this IVisitor, they will be processing
        // a new copy of the instruction list, which will have the optimized opcodes
        InstructionList newInstructions = new InstructionList();
        InstructionFinisher old = this.finisher;

        // Create a new finisher, which will put the instruction into a new InstructionList
        // instead of passing them on to the next delegate
        this.finisher = new InstructionListFinisher(newInstructions);
        for (Instruction inst : new_list.getInstructions())
        {
            visitInstruction(inst);
        }
        // Make sure we flush any remaining instructions to the IL
        flush();

        // delegate the call with the new instruction list
        super.visitInstructionList(newInstructions);

        // Reset the finisher to the delegating one
        this.finisher = old;
    }

    @Override
    public void visitInstruction(int opcode)
    {
        visitInstruction(InstructionFactory.getInstruction(opcode));

    }

    @Override
    public void visitInstruction(int opcode, int immediate_operand)
    {
        visitInstruction(InstructionFactory.getInstruction(opcode, immediate_operand));
    }

    @Override
    public void visitInstruction(int opcode, Object[] operands)
    {
        visitInstruction(InstructionFactory.getInstruction(opcode, operands));
    }

    @Override
    public void visitInstruction(int opcode, Object single_operand)
    {
        visitInstruction(InstructionFactory.getInstruction(opcode, single_operand));
    }

    @Override
    /**
     * Add the instruction to our peephole window and performs any optimizations.
     *
     * The optimizations are performed on the instructions already in the window - we do them
     * now, and not when they are added, because we need to know if any labels targeted the instructions
     * and we don't know that when we first see the instruction (but we do when we see the next instruction)
     * @param instruction  the Instruction to add
     */
    public void visitInstruction(Instruction instruction)
    {
        // Optimize the instructions we already have
        processPreviousInstructions();

        // add the instruction, but don't do any optimizations yet
        addInstruction(instruction);
    }

    /**
     * Perform optimizations on the instructions currently held in the window.
     * This is so we don't do the optimizations until we are sure a particular
     * instruction was or was not the target of a jump. If it was a target, then
     * we don't do the optimizations as we don't have enough information to
     * determine all the ways control might flow to that instruction.
     */
    private void processPreviousInstructions()
    {
        InstructionInfo last = previous(0);
        InstructionInfo secondLast = previous(1);

        // Check if we have a labelCurrent on the last instruction, or a labelNext on the previous instruction
        if (last.getLabelCurrents().isEmpty() && secondLast.getLabelNexts().isEmpty())
        {
            switch (last.getOpcode())
            {
                case OP_convert_b:
                {
                    op_convert_b(last);
                    break;
                }
                case OP_convert_d:
                {
                    op_convert_d(last);
                    break;
                }
                case OP_convert_i:
                {
                    op_convert_i(last);
                    break;
                }
                case OP_convert_u:
                {
                    op_convert_u(last);
                    break;
                }
                case OP_convert_s:
                {
                    op_convert_s(last);
                    break;
                }
                case OP_getproperty:
                {
                    op_getproperty(last);
                    break;
                }
                case OP_iffalse:
                {
                    op_iffalse(last);
                    break;
                }
                case OP_iftrue:
                {
                    op_iftrue(last);
                    break;
                }
                case OP_pop:
                {
                    op_pop(last);
                    break;
                }
                case OP_nop:
                {
                    delete(0);
                    break;
                }
                case OP_getlocal:
                {
                    OP_getlocal(last);
                    break;
                }
                case OP_returnvoid:
                {
                    op_returnvoid(last);
                    break;
                }
                default:
                {
                    break;
                }
            }
        }
    }

    public void visitEnd()
    {
        flush();
        super.visitEnd();
    }

    /**
     * Flush any remaining instructions
     */
    private void flush()
    {
        processPreviousInstructions();
        
        // finish any remaining instructions
        for (InstructionInfo info : instructions)
        {
            finishInstruction(info);
        }
        
        instructions.clear();
    }

    /**
     * Bind a Label object (not to be confused with the AVM OP_label
     * instruction) to the last-visited ABC instruction in this method.
     * 
     * @pre visitInstruction() must have been called at least once, i.e., there
     * must be a last-visited ABC instruction.
     */
    public void labelCurrent(Label l)
    {
        jumpOptimizations(l, LabelKind.LABEL_CURRENT);

        int idx = getLastInstructionIndex();
        InstructionInfo instructionInfo = instructions.get(idx);
        lastLabelSeen = idx;
        instructionInfo.addLabelCurrent(l);
    }

    /**
     * Enum to pass around to indicate what kind of labeling operation we are
     * performing
     */
    private static enum LabelKind
    {
        LABEL_CURRENT,
        LABEL_NEXT
    }

    /**
     * Helper method to perform optimizations around jumps when we get a
     * labeling operation. Shared with labelNext and labelCurrent methods
     * 
     * @param l the Label object passed into the label operation
     * @param kind what kind of labeling operation are we doing (labelNext or
     * labelCurrent)
     */
    private void jumpOptimizations(Label l, LabelKind kind)
    {
        // Need to start looking at different indexes for label current vs. label next
        // this is because for a label next, the next instruction won't have come in yet.
        int idx = kind == LabelKind.LABEL_CURRENT ? 1 : 0;

        InstructionInfo prev = previous(idx);
        switch (prev.getOpcode())
        {
            case OP_jump:
            {
                InstructionInfo prev2 = previous(idx + 1);
                Instruction insn = prev2.getInstruction();
                if (insn != null && insn.isBranch() && insn.getTarget() == l)
                {
                    // If the previous instructions were an if that jumped here, and it
                    // only jumped over another jump, then we can invert the if instruction
                    // and save a jump
                    //   iffalse L1, jump L2, L1    -> iftrue L2, L1
                    Instruction newIf = invertIf(prev2, prev);
                    if (newIf != null)
                    {
                        if (kind == LabelKind.LABEL_CURRENT)
                        {
                            // labelCurrent, so we need to preserve the last instruction
                            Instruction[] newInsns = {newIf, previous(0).getInstruction()};
                            replace(idx + 1, newInsns);
                        }
                        else
                        {
                            // labelNext so we can just delete the last instruction
                            replace(idx + 1, newIf);
                        }
                    }
                }
                // If the previous instruction was a jump, and it just jumped
                // to the next instruction, then we can remove the jump and just fall
                // through
                //   jump L1, L1 -> L1
                else if (prev.getOperand(0) == l)
                {
                    if (kind == LabelKind.LABEL_NEXT)
                        // can just delete the jump because we don't have the next instruction yet
                        delete(idx);
                    else
                        // replace the jump with its target
                        replace(idx, previous(0).getInstruction());
                }
            }
        }
    }

    /**
     * Helper method to invert if expr target jump other-target to if not expr
     * other-target Used to optimize some common patterns with if's and jumps
     */
    private Instruction invertIf(InstructionInfo oldIf, InstructionInfo oldJump)
    {
        Instruction newIf = null;
        switch (oldIf.getOpcode())
        {
            case OP_ifeq:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_ifne, oldJump.getInstruction());
                break;
            }
            case OP_ifstricteq:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_ifstrictne, oldJump.getInstruction());
                break;
            }
            case OP_ifstrictne:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_ifstricteq, oldJump.getInstruction());
                break;
            }
            case OP_ifge:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_ifnge, oldJump.getInstruction());
                break;
            }
            case OP_ifgt:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_ifngt, oldJump.getInstruction());
                break;
            }
            case OP_iflt:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_ifnlt, oldJump.getInstruction());
                break;
            }
            case OP_ifle:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_ifnle, oldJump.getInstruction());
                break;
            }
            case OP_ifne:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_ifeq, oldJump.getInstruction());
                break;
            }
            case OP_ifnge:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_ifge, oldJump.getInstruction());
                break;
            }
            case OP_ifngt:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_ifgt, oldJump.getInstruction());
                break;
            }
            case OP_ifnlt:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_iflt, oldJump.getInstruction());
                break;
            }
            case OP_ifnle:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_ifle, oldJump.getInstruction());
                break;
            }
            case OP_iftrue:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_iffalse, oldJump.getInstruction());
                break;
            }
            case OP_iffalse:
            {
                newIf = InstructionFactory.createModifiedInstruction(OP_iftrue, oldJump.getInstruction());
                break;
            }
        }
        
        return newIf;
    }

    /**
     * Helper method to get the index of the last instruction in the window
     * 
     * @return the index of the last instruction
     */
    private int getLastInstructionIndex()
    {
        return instructions.size() - 1;
    }

    /**
     * Bind a Label object (not to be confused with the AVM OP_label
     * instruction) to the next ABC instruction that gets visited in this method
     * 
     * @post visitInstruction() must be called at least once after labelNext,
     * i.e., there must be a next instruction.
     */
    public void labelNext(Label l)
    {
        jumpOptimizations(l, LabelKind.LABEL_NEXT);

        int idx = getLastInstructionIndex();
        instructions.get(idx).addLabelNext(l);
        lastLabelSeen = idx + 1;
    }

    /**
     * Fetch a previous instruction from the peephole window
     * 
     * @param i the index of the previous instruction. 0 is the last instruction
     * processed (so the immediately preceeding instruction), and
     * PEEPHOLE_WINDOW_SIZE-1 is the oldest instruction that can be fetched.
     * @return the InstructionInfo for some previous instruction, or the
     * noInstruction object if i falls outside of the peephole window
     */
    private InstructionInfo previous(int i)
    {
        int size = instructions.size();
        int idx = i + 1;
        if (size >= idx)
        {
            int realIdx = size - idx;

            // Can't look back past a label
            if (indexBeforeLabel(realIdx))
                return noInstruction;

            return instructions.get(realIdx);
        }
        return noInstruction;
    }

    /**
     * Helper method to determine if the index passed in occurrs before the last
     * label instruction in the window
     */
    private boolean indexBeforeLabel(int idx)
    {
        return lastLabelSeen != NO_LABEL
                // if we did a label next we can still look back until that
                // next instruction comes in
                && lastLabelSeen < PEEPHOLE_WINDOW_SIZE
                && idx < lastLabelSeen;
    }

    /**
     * Replace the previous instruction(s), from the end of the peephole window
     * up to i with a new Instruction. It will replace all the previous
     * instructions through i. Must not be called with an index that falls
     * outside of the peephole window.
     * 
     * @param i the index of the previous instruction to replace. Follows the
     * same indexing strategy as the previous() method 0 is the last instruction
     * in the window, 1 is two instructions ago, up to PEEPHOLE_WINDOW_SIZE-1
     * which is the oldest instruction that can be replaced.
     * @param insn The new instruction to insert at i
     */
    private void replace(int i, Instruction insn)
    {
        Instruction[] newInsns = {insn};
        replace(i, newInsns);
    }

    /**
     * Replace the previous instruction(s), from the end of the peephole window
     * up to i with a new Instruction sequence. It will replace all the previous
     * instructions through i. Must not be called with an index that falls
     * outside of the peephole window.
     * 
     * @param i the index of the previous instruction to replace. Follows the
     * same indexing strategy as the previous() method 0 is the last instruction
     * in the window, 1 is two instructions ago, up to PEEPHOLE_WINDOW_SIZE-1
     * which is the oldest instruction that can be replaced.
     * @param insns The new instruction sequence to insert at i
     */
    private void replace(int i, Instruction[] insns)
    {
        if (insns.length == 0)
            return;

        int size = instructions.size();
        int idx = i + 1;
        if (size >= idx)
        {
            int realIdx = size - idx;

            assert !indexBeforeLabel(realIdx) : "Attempting to replace instruction sequence that spans a label";

            // replace the instruction
            InstructionInfo info = instructions.get(realIdx);
            info.setInstruction(insns[0]);

            // Delete any remaining instructions, saving the list of labelNexts from the last deleted
            // instruction
            List<Label> labelNexts = null;
            for (int r = size - 1; r >= (realIdx + 1); --r)
            {
                InstructionInfo temp = instructions.remove(r);
                if (r == size - 1)
                {
                    // Save the labelNexts from the last deleted instruction
                    labelNexts = temp.getLabelNexts();
                }
            }

            // Add the remaining instructions
            for (int r = 1, l = insns.length; r < l; ++r)
                addInstruction(insns[r]);

            if (labelNexts != null)
            {
                // Apply any saved labelNexts to the last instruction added
                for (Label l : labelNexts)
                    labelNext(l);
            }
        }
        else
        {
            assert false : "Trying to replace instructions that fall outside the peephole window";
        }
    }

    /**
     * Delete the previous instruction(s), from the end of the peephole window
     * up to i. Must not be called with an index that falls outside of the
     * peephole window.
     * 
     * @param i the index of the previous instruction to replace. Follows the
     * same indexing strategy as the previous() method 0 is the last instruction
     * in the window, 1 is two instructions ago, up to PEEPHOLE_WINDOW_SIZE-1
     * which is the oldest instruction that can be replaced.
     */
    private void delete(int i)
    {
        int size = instructions.size();
        int idx = i + 1;
        if (size >= idx)
        {
            int realIdx = size - idx;

            assert !indexBeforeLabel(realIdx) : "Attempting to delete instruction sequence that spans a label";

            List<Label> labels = null;

            for (int r = size - 1; r >= realIdx; --r)
            {
                InstructionInfo temp = instructions.remove(r);

                if (r == size - 1)
                {
                    // Save any label nexts from the last deleted instruction
                    List<Label> labelNexts = temp.getLabelNexts();
                    if (!labelNexts.isEmpty())
                    {
                        if (labels == null)
                            labels = new ArrayList<Label>();
                        labels.addAll(labelNexts);
                    }
                }
                else if (r == realIdx)
                {
                    // save any label currents from the first deleted instruction
                    List<Label> labelCurrents = temp.getLabelCurrents();
                    if (!labelCurrents.isEmpty())
                    {
                        if (labels == null)
                            labels = new ArrayList<Label>();
                        labels.addAll(labelCurrents);
                    }
                }
            }
            if (labels != null)
            {
                if (labelsFromDeletedInsns == null)
                    labelsFromDeletedInsns = labels;
                else
                    labelsFromDeletedInsns.addAll(labels);
            }
        }
        else
        {
            assert false : "Trying to delete instructions that fall outside the peephole window";
        }
    }

    /**
     * InstructionInfo to represent noInstruction - used when we look back past
     * the size of the peephole window
     */
    private static InstructionInfo noInstruction = new InstructionInfo();

    /**
     * Optimizations for OP_convert_b: equals, convert_b -> equals strictequals,
     * convert_b -> strictequals not, convert_b -> not greaterthan, convert_b ->
     * greaterthan lessthan, convert_b -> lessthan greaterequals, convert_b ->
     * greaterequals lessequals, convert_b -> lessequals istype, convert_b ->
     * istype istypelate, convert_b -> istypelate instanceof, convert_b ->
     * instanceof deleteproperty, convert_d -> deleteproperty in, convert_d ->
     * in convert_b, convert_b -> convert_b pushtrue, convert_b -> pushtrue
     * pushfalse, convert_b -> pushfalse
     * 
     * @param i The convert_b instruction
     */
    private void op_convert_b(InstructionInfo i)
    {
        InstructionInfo prev = previous(1);
        switch (prev.getOpcode())
        {
            case OP_equals:
            case OP_strictequals:
            case OP_not:
            case OP_greaterthan:
            case OP_lessthan:
            case OP_greaterequals:
            case OP_lessequals:
            case OP_istype:
            case OP_istypelate:
            case OP_instanceof:
            case OP_deleteproperty:
            case OP_in:
            case OP_convert_b:
            case OP_pushtrue:
            case OP_pushfalse:
            {
                // result is already a boolean, just erase the op_convert_b
                delete(0);
                break;
            }
            default:
            {
                // nothing to do - instruction has already been added
            }
        }
    }

    /**
     * Optimizations for OP_convert_d: convert_d, convert_d -> convert_d
     * pushbyte n, convert_d -> pushdouble n pushint n, convert_d -> pushdouble
     * n pushuint n, convert_d -> pushdouble n pushdouble n, convert_d ->
     * pushdouble n pushnan, convert_d -> pushnan lf32, convert_d -> lf32 lf64,
     * convert_d -> lf64
     * 
     * @param i The convert_d instruction
     */
    private void op_convert_d(InstructionInfo i)
    {
        InstructionInfo prev = previous(1);
        switch (prev.getOpcode())
        {
            case OP_pushbyte:
            {
                // replace pushbyte, convert d with pushdouble - should be faster
                replace(1, InstructionFactory.getInstruction(OP_pushdouble, new Double(convertByteImmediateToDouble(prev.getImmediate()))));
                break;
            }
            case OP_pushint:
            case OP_pushuint:
            {
                // replace pushint , convert d with pushdouble - should be faster
                replace(1, InstructionFactory.getInstruction(OP_pushdouble, new Double(((Number)prev.getOperand(0)).doubleValue())));
                break;
            }
            case OP_pushdouble:
            case OP_pushnan:
            case OP_lf32:
            case OP_lf64:
            case OP_convert_d:
            {
                // result is already a double, just erase the op_convert_d
                delete(0);
                break;
            }
            default:
            {
                // nothing to do - instruction has already been added
            }
        }
    }

    /**
     * Optimizations for OP_convert_i: convert_i, convert_i -> convert_i
     * coerce_i, convert_i -> coerce_i OP_bitand, convert_i -> OP_bitand
     * OP_bitor, convert_i -> OP_bitor OP_bitxor, convert_i -> OP_bitxor lshift,
     * convert_i -> lshift rshift, convert_i -> rshift add_i, convert_i -> add_i
     * subtract_i, convert_i -> subtract_i increment_i, convert_i -> increment_i
     * decrement_i, convert_i -> decrement_i multiply_i, convert_i -> multiply_i
     * pushbyte, convert_i -> pushbyte pushshort, convert_i -> pushshort
     * pushint, convert_i -> pushint li8, convert_i -> li8 li16, convert_i ->
     * li16 li32, convert_i -> li32 sxi1, convert_i -> sxi1 sxi8, convert_i ->
     * sxi8 sxi16, convert_i -> sxi16
     * 
     * @param i The convert_b instruction
     */
    private void op_convert_i(InstructionInfo i)
    {
        InstructionInfo prev = previous(1);
        switch (prev.getOpcode())
        {
            case OP_convert_i:
            case OP_coerce_i:
            case OP_bitand:
            case OP_bitor:
            case OP_bitxor:
            case OP_lshift:
            case OP_rshift:
            case OP_add_i:
            case OP_subtract_i:
            case OP_increment_i:
            case OP_decrement_i:
            case OP_multiply_i:
            case OP_pushbyte:
            case OP_pushshort:
            case OP_pushint:
            case OP_li8:
            case OP_li16:
            case OP_li32:
            case OP_sxi1:
            case OP_sxi8:
            case OP_sxi16:
            {
                // result is already an int, just erase the op_convert_i
                delete(0);
                break;
            }
            default:
            {
                // nothing to do - instruction has already been added
            }
        }
    }

    /**
     * Optimizations for OP_convert_s: coerce_s, convert_s -> coerce_s
     * convert_s, convert_s -> convert_s pushstring, convert_s -> pushstring
     * typeof, convert_s -> typeof
     * 
     * @param i The convert_b instruction
     */
    private void op_convert_s(InstructionInfo i)
    {
        InstructionInfo prev = previous(1);
        switch (prev.getOpcode())
        {
            case OP_coerce_s:
            case OP_convert_s:
            case OP_pushstring:
            case OP_typeof:
            {
                // result is already a String, just erase the op_convert_s
                delete(0);
                break;
            }
            default:
            {
                // nothing to do - instruction has already been added
            }
        }
    }

    /**
     * Optimizations for OP_convert_i: convert_u, convert_u -> convert_u
     * pushuint, convert_u -> pushuint
     * 
     * @param i The convert_b instruction
     */
    private void op_convert_u(InstructionInfo i)
    {
        InstructionInfo prev = previous(1);
        switch (prev.getOpcode())
        {
            case OP_convert_u:
            case OP_pushuint:
            {
                // result is already a uint, just erase the op_convert_u
                delete(0);
                break;
            }
            default:
            {
                // nothing to do - instruction has already been added
            }
        }
    }

    /**
     * Optimizations for OP_getproperty: findpropstrict name, getproperty name
     * -> getlex name
     * 
     * @param i the getproperty instruction
     */
    private void op_getproperty(InstructionInfo i)
    {
        InstructionInfo prev = previous(1);
        switch (prev.getOpcode())
        {
            case OP_findpropstrict:
            {
                // can optimize findpropstrict, followed by getprop of the same name
                // if there are any instructions btwn the findpropstrict and getprop (such as to compute a runtime
                // multiname), we won't match this pattern
                if (i.getOperand(0).equals(prev.getOperand(0)))
                    replace(1, InstructionFactory.createModifiedInstruction(OP_getlex, prev.getInstruction()));
                break;
            }
        }
    }

    /**
     * Optimizations for OP_iffalse: convert_b, iffalse -> iffalse equals,
     * iffalse -> ifne strictequals, iffalse -> strictne lessthen, iffalse ->
     * ifnlt lessequals, iffalse -> ifnle greaterthan, iffalse -> ifngt
     * greaterequals, iffalse -> ifnge pushfalse, iffalse -> jump pushtrue,
     * iffalse -> nothing strictequals, not, iffalse -> ifstrictequals equals,
     * not, iffalse -> ifeq lessthan, not, iffalse -> iflt lessequals, not,
     * iffalse -> ifle greaterthan, not, iffalse -> ifgt greaterequals,
     * not,iffalse -> ifge not, iffalse -> iftrue
     * 
     * @param i the iffalse instruction
     */
    private void op_iffalse(InstructionInfo i)
    {
        InstructionInfo prev = previous(1);  
        
        // Check if we know what's on the stack. If so, we don't need
        // to do a conditional branch
        switch(isTrueInstructionInfo(prev))
        {
            case TRUE:
                delete(1);          // if we can't branch, just eat the push / iffalse instructions
                return;
            case FALSE:
                replace(1, InstructionFactory.createModifiedInstruction(OP_jump, i.getInstruction()));
                    // If we always branch, then replace the push / iffalse with an uncontidional jump
                return;
            case DONT_KNOW:
                break;             // continue on if we don't know it's a constant boolean
            default:
                assert false;
                
        }
        switch (prev.getOpcode())
        {
            case OP_convert_b:
            {
                // replace with just iffalse
                replace(1, i.getInstruction());
                break;
            }
            case OP_equals:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_ifne, i.getInstruction()));
                break;
            }
            case OP_strictequals:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_ifstrictne, i.getInstruction()));
                break;
            }
            case OP_lessthan:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_ifnlt, i.getInstruction()));
                break;
            }
            case OP_lessequals:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_ifnle, i.getInstruction()));
                break;
            }
            case OP_greaterthan:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_ifngt, i.getInstruction()));
                break;
            }
            case OP_greaterequals:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_ifnge, i.getInstruction()));
                break;
            }
           
            case OP_not:
            {
                InstructionInfo prev2 = previous(2);
                switch (prev2.getOpcode())
                {
                    case OP_strictequals:
                    {
                        replace(2, InstructionFactory.createModifiedInstruction(OP_ifstricteq, i.getInstruction()));
                        break;
                    }
                    case OP_equals:
                    {
                        replace(2, InstructionFactory.createModifiedInstruction(OP_ifeq, i.getInstruction()));
                        break;
                    }
                    case OP_lessthan:
                    {
                        replace(2, InstructionFactory.createModifiedInstruction(OP_iflt, i.getInstruction()));
                        break;
                    }
                    case OP_lessequals:
                    {
                        replace(2, InstructionFactory.createModifiedInstruction(OP_ifle, i.getInstruction()));
                        break;
                    }
                    case OP_greaterthan:
                    {
                        replace(2, InstructionFactory.createModifiedInstruction(OP_ifgt, i.getInstruction()));
                        break;
                    }
                    case OP_greaterequals:
                    {
                        replace(2, InstructionFactory.createModifiedInstruction(OP_ifge, i.getInstruction()));
                        break;
                    }
                    default:
                    {
                        replace(1, InstructionFactory.createModifiedInstruction(OP_iftrue, i.getInstruction()));
                        break;
                    }
                }
                break;
            }

            default:
            {
                // nothing to do, instruction was already added
            }
        }
    }
    
  
    /**
     * Optimizations for OP_iftrue: convert_b, iftrue -> iftrue equals, iftrue
     * -> ifeq strictequals, iftrue -> ifstricteq lessthen, iftrue -> iflt
     * lessequals, iftrue -> ifle greaterthan, iftrue -> ifgt greaterequals,
     * iftrue -> ifge pushfalse, iftrue -> jump pushtrue, iftrue -> nothing
     * strictequals, not, iftrue -> ifstrictne equals, not, iftrue -> ifne
     * lessthan, not, iftrue -> ifnlt lessequals, not, iftrue -> ifnle
     * greaterthan, not, iftrue -> ifngt greaterequals, not,iftrue -> ifnge not,
     * iftrue -> iffalse
     * 
     * @param i the iftrue instruction
     */
    private void op_iftrue(InstructionInfo i)
    {
        InstructionInfo prev = previous(1);
        
        
        switch(isTrueInstructionInfo(prev))
        {
            case FALSE:
                delete(1);
                return;
            case TRUE:
                replace(1, InstructionFactory.createModifiedInstruction(OP_jump, i.getInstruction()));
                return;
            case DONT_KNOW:
                break;             // continue on if we don't know it's a constant boolean
            default:
                assert false;
                
        }
        switch (prev.getOpcode())
        {
            case OP_convert_b:
            {
                replace(1, i.getInstruction());
                break;
            }
            case OP_equals:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_ifeq, i.getInstruction()));
                break;
            }
            case OP_strictequals:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_ifstricteq, i.getInstruction()));
                break;
            }
            case OP_lessthan:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_iflt, i.getInstruction()));
                break;
            }
            case OP_lessequals:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_ifle, i.getInstruction()));
                break;
            }
            case OP_greaterthan:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_ifgt, i.getInstruction()));
                break;
            }
            case OP_greaterequals:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_ifge, i.getInstruction()));
                break;
            }
           
            case OP_not:
            {
                InstructionInfo prev2 = previous(2);
                switch (prev2.getOpcode())
                {
                    case OP_strictequals:
                    {
                        replace(2, InstructionFactory.createModifiedInstruction(OP_ifstrictne, i.getInstruction()));
                        break;
                    }
                    case OP_equals:
                    {
                        replace(2, InstructionFactory.createModifiedInstruction(OP_ifne, i.getInstruction()));
                        break;
                    }
                    case OP_lessthan:
                    {
                        replace(2, InstructionFactory.createModifiedInstruction(OP_ifnlt, i.getInstruction()));
                        break;
                    }
                    case OP_lessequals:
                    {
                        replace(2, InstructionFactory.createModifiedInstruction(OP_ifnle, i.getInstruction()));
                        break;
                    }
                    case OP_greaterthan:
                    {
                        replace(2, InstructionFactory.createModifiedInstruction(OP_ifngt, i.getInstruction()));
                        break;
                    }
                    case OP_greaterequals:
                    {
                        replace(2, InstructionFactory.createModifiedInstruction(OP_ifnge, i.getInstruction()));
                        break;
                    }
                    default:
                    {
                        replace(1, InstructionFactory.createModifiedInstruction(OP_iffalse, i.getInstruction()));
                        break;
                    }
                }
                break;
            }

            default:
                // nothing to do, instruction was already added
        }
    }
    
    private enum ConstantBoolean { TRUE, FALSE, DONT_KNOW }
    /**
     * evaluates an instruction and determines if it will result in true or false on TOS.
     * 
     * @param i is an instruction to analyze
     * @return whether TOS is true, false, or not known
     */
    private static ConstantBoolean isTrueInstructionInfo(InstructionInfo i)
    {
        ConstantBoolean ret = ConstantBoolean.DONT_KNOW;
        switch(i.getOpcode())
        {
            case OP_pushtrue:
                ret = ConstantBoolean.TRUE;
                break;
            case OP_pushfalse:
                ret = ConstantBoolean.FALSE;
                break;
            case OP_pushbyte:
            {        
                int value = i.getImmediate();
                assert value >= 0;
                ret =  ECMASupport.toBoolean(value) ? ConstantBoolean.TRUE : ConstantBoolean.FALSE;
                break;
            }
            case OP_pushint:
            {        
                int value = (Integer)i.getOperand(0);
                ret =  ECMASupport.toBoolean(value) ? ConstantBoolean.TRUE : ConstantBoolean.FALSE;
                break;
            }
            case OP_pushuint:
            {        
                long value = (Long)i.getOperand(0);
                ret =  ECMASupport.toBoolean(value) ? ConstantBoolean.TRUE : ConstantBoolean.FALSE;
                break;
            } 
            case OP_pushstring:
            {        
                String value = i.getOperand(0).toString();
                ret =  ECMASupport.toBoolean(value) ? ConstantBoolean.TRUE : ConstantBoolean.FALSE;
                break;
            }
            case OP_pushnull:
                ret = ConstantBoolean.FALSE;
                break;
        }
        return ret;
    }


    /**
     * Optimizations for OP_pop: callprop, pop -> callpropvoid callsuper, pop ->
     * callsupervoid
     */
    private void op_pop(InstructionInfo i)
    {
        InstructionInfo prev = previous(1);
        switch (prev.getOpcode())
        {
            case OP_callproperty:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_callpropvoid, prev.getInstruction()));
                break;
            }
            case OP_callsuper:
            {
                replace(1, InstructionFactory.createModifiedInstruction(OP_callsupervoid, prev.getInstruction()));
                break;
            }
            default:
            {
                // nothing to do, instruction has already been added
            }
        }
    }

    /**
     * Optimizations for OP_returnvoid: returnvoid, returnvoid -> returnvoid
     * getlocal0, pushscope, returnvoid -> returnvoid (insn without side
     * effect), returnvoid -> returnvoid
     */
    private void op_returnvoid(InstructionInfo i)
    {
        InstructionInfo prev = previous(1);
        switch (prev.getOpcode())
        {
            case OP_returnvoid:
            {
                delete(0);
                break;
            }
            //  Any instruction without side effects that
            //  the CG is likely to generate can go here.
            case OP_pop:
            {
                replace(1, i.getInstruction());
                break;
            }
            case OP_pushscope:
            {
                if (previous(2).getOpcode() == OP_getlocal0)
                    //  Trivial function.
                    replace(2, i.getInstruction());
                break;
            }
            default:
            {
                // nothing to do, instruction has already been added
            }
        }
    }

    /**
     * Optimizations for OP_getlocal: setlocal N, getlocal N -> dup, setlocal N
     */
    private void OP_getlocal(InstructionInfo i)
    {
        InstructionInfo prev = previous(1);
        InstructionInfo cur = previous(0);

        switch (prev.getOpcode())
        {
            case OP_setlocal:
            {
                if (cur.getInstruction().getImmediate() != prev.getInstruction().getImmediate())
                    break; // set,get of different locals

                Instruction[] newInsns = {InstructionFactory.getInstruction(OP_dup), prev.getInstruction()};
                replace(1, newInsns);
                break;
            }
            default:
            {
                // nothing to do, instruction has already been added
            }
        }
    }

    /**
     * convert the immediate of a pushbyte to a double value.
     */
    double convertByteImmediateToDouble(int i)
    {
        byte b = (byte)i;
        return (double)b;
    }

    /**
     * Add an instruction to the peephole window. If the window is already full
     * then the first instruction will be removed, and passed on to the next
     * delegate to make room for the new instruction
     * 
     * @param insn the Instruction to add
     */
    private void addInstruction(Instruction insn)
    {
        int size = instructions.size();
        InstructionInfo info = null;
        if (size == PEEPHOLE_WINDOW_SIZE)
        {
            // reuse the InstructionInfo if we can
            info = instructions.remove(0);
            finishInstruction(info);

            if (lastLabelSeen != NO_LABEL)
                --lastLabelSeen;
        }
        else
        {
            info = new InstructionInfo();
        }
        info.reset(insn);
        instructions.add(info);

        if (labelsFromDeletedInsns != null)
        {
            // If we saved any labels from a deleted instruction sequence,
            // apply them here.
            // They will always be label currents no matter what the started out
            // as - if we deleted something with a labelNext, this is still the next instruction
            // and if we deleted something with a labelCurrent then this instruction is the new current instruction
            for (Label l : labelsFromDeletedInsns)
                labelCurrent(l);
            labelsFromDeletedInsns = null;
        }
    }

    /**
     * Called when the peephole optimizer is finished with an instruction. This
     * method will pass the instruction through to the next IMethodBodyVisitor,
     * and call any needed labeling methods.
     * 
     * @param info The InstructionInfo that the optimizer is done with
     */
    private void finishInstruction(InstructionInfo info)
    {
        finisher.visitInstruction(info.getInstruction());
        
        for (Label l : info.getLabelCurrents())
        {
            finisher.labelCurrent(l);
        }
        
        for (Label l : info.getLabelNexts())
        {
            finisher.labelNext(l);
        }
    }

    /**
     * Helper class to hold info about an Instruction. In addition to the
     * Instruction, it also keeps track of any labelNext, or labelCurrent
     * instructions that must be applied to the Instruction once it is passed on
     * to the next IMethodBodyVisitor
     */
    private static class InstructionInfo
    {
        InstructionInfo()
        {
        }

        /**
         * Hold the instruction
         */
        private Instruction insn;

        /**
         * label(s) to use for a labelNext operation once we are done with the
         * instruction
         */
        private List<Label> labelNexts = new ArrayList<Label>();

        /**
         * label(s) to use for a labelCurrent operation once we are done with
         * the instruction
         */
        private List<Label> labelCurrents = new ArrayList<Label>();

        /**
         * Reset the Instruction info to its default state with a new
         * Instruction. This method will clear any labelNext/labelCurrent data.
         * This is so we can reuse the InstructionInfo instances.
         */
        void reset(Instruction i)
        {
            this.insn = i;
            labelNexts.clear();
            labelCurrents.clear();
        }

        /**
         * Change the Instruction this InstructionInfo points at. This is useful
         * for rewriting instructions - instead of deleting and adding a new
         * InstructionInfo, we can just rest the Instruction. This will not
         * clear any labelNext/labelCurrent data - If we are replacing an
         * Instruction then the label methods should still happen on the new
         * instruction that replaced the old one.
         */
        void setInstruction(Instruction i)
        {
            this.insn = i;
        }

        /**
         * @return the Instruction this instance holds
         */
        public Instruction getInstruction()
        {
            return insn;
        }

        /**
         * Add a Label to be used as an argument for labelCurrent when this
         * instruction is passed on to the next visitor.
         * 
         * @param l the label
         */
        public void addLabelCurrent(Label l)
        {
            this.labelCurrents.add(l);
        }

        /**
         * Add a Label to be used as an argument for labelNext when this
         * instruction is passed on to the next visitor.
         * 
         * @param l the label
         */
        public void addLabelNext(Label l)
        {
            this.labelNexts.add(l);
        }

        /**
         * @return A List of Labels to be used for labelCurrent, returns the
         * empty list if there are none
         */
        public List<Label> getLabelCurrents()
        {
            return labelCurrents;
        }

        /**
         * @return A List of Labels to be used for labelNext, returns the empty
         * list if there are none
         */
        public List<Label> getLabelNexts()
        {
            return labelNexts;
        }

        /**
         * @return the opcode of the Instruction, or -1 if there is no
         * instruction
         */
        public int getOpcode()
        {
            return insn != null ? insn.getOpcode() : -1;
        }

        /**
         * @return the immediate for the instruction, or -1 if there is no
         * instruction
         */
        public int getImmediate()
        {
            return insn != null ? insn.getImmediate() : -1;
        }

        /**
         * Get the operand at index i from the underlying instruction
         * 
         * @param i the index of the operand to fetch
         * @return the operand at index i, or null if there is no instruction
         */
        public Object getOperand(int i)
        {
            return insn != null ? insn.getOperand(i) : null;
        }
    }

    /**
     * Interface so we can do different things when we are done with an
     * instruction
     */
    static interface InstructionFinisher
    {
        void visitInstruction(Instruction i);

        void labelNext(Label l);

        void labelCurrent(Label l);
    }

    /**
     * Passes the instruction and any label calls on to the next
     * IMethodBodyVisitor
     */
    class DelegateFinisher implements InstructionFinisher
    {
        public void visitInstruction(Instruction i)
        {
            PeepholeOptimizerMethodBodyVisitor.super.visitInstruction(i);
        }

        public void labelNext(Label l)
        {
            PeepholeOptimizerMethodBodyVisitor.super.labelNext(l);
        }

        public void labelCurrent(Label l)
        {
            PeepholeOptimizerMethodBodyVisitor.super.labelCurrent(l);
        }
    }

    /**
     * Puts the instructions into a InstructionList instead of passing them on
     * to the next IMethodBodyVisitor
     */
    static class InstructionListFinisher implements InstructionFinisher
    {
        public InstructionListFinisher(InstructionList list)
        {
            this.list = list;
        }

        private InstructionList list;

        public void visitInstruction(Instruction i)
        {
            list.addInstruction(i);
        }

        public void labelNext(Label l)
        {
            list.labelNext(l);
        }

        public void labelCurrent(Label l)
        {
            list.labelCurrent(l);
        }
    }
}
