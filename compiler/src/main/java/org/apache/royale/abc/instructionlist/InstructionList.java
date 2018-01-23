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

package org.apache.royale.abc.instructionlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.InstructionFactory;
import org.apache.royale.abc.semantics.Label;
import static org.apache.royale.abc.ABCConstants.*;

/**
 * An InstructionList is a structure that holds a sequence of Instructions, and
 * manages sets of labels, which act as relocatable address constants, to
 * compose control-flow constructs from their constituent parts.
 */
public class InstructionList implements Cloneable
{
    /**
     * Manifest constant returned when a search for an executable instruction
     * doesn't find one.
     */
    private static final int NO_EXECUTABLE_INSTRUCTIONS = -1;

    /**
     * Default constructor.
     */
    public InstructionList()
    {
    }

    /**
     * Construct an InstructionList capable of holding a specified number of
     * instructions.
     * 
     * @param capacity - the required capacity.
     */
    public InstructionList(int capacity)
    {
        this();
        leafInstructions = new ArrayList<Instruction>(capacity);
    }

    /**
     * This InstructionList's storage configuration; which elements of the fixed
     * or variable storage are active.
     */
    private enum StorageState
    {
        Variable, Ins2, Ins1, Empty, Ins3
    };

    /**
     * Storage for a variable number of instructions.
     */
    private ArrayList<Instruction> leafInstructions = null;

    /**
     * Fixed storage, used when the InstructionList contains 1-3 instructions.
     */
    private Instruction insn1 = null;
    
    /**
     * Fixed storage, used when the InstructionList contains 2-3 instructions.
     */
    private Instruction insn2 = null;
    
    /**
     * Fixed storage, used when the InstructionList contains 3 instructions.
     */
    private Instruction insn3 = null;

    /**
     * Labels resolved to known positions in this InstructionList.
     */
    private ArrayList<Label> activeLabels = null;

    /**
     * Labels that reference "the next instruction past the current end of this
     * InstructionList." These labels will be resolved when another instruction
     * is added to this list, either by addInstruction() or by addAll() when the
     * other list has at least one insn. If no such instruction ever arrives,
     * this list's pending labels will be inherited by any list it's added to
     * via addAll().
     */
    private ArrayList<Label> pendingLabels = null;

    /**
     * An InstructionList is valid from its creation until it is the operand of
     * an addAll() operation to another InstructionList. At that point, the
     * InstructionList's data becomes out-of-date and it is invalid.
     */
    private boolean isValid = true;

    /**
     * Copy operation, used by clone()
     */
    private void copyInstructionList(InstructionList src)
    {
        src.checkValidity();

        // updateInstructionLabel() will update the relevant active/pending label
        // collection with any cloned labels when needed.
        ArrayList<Label> remainingActiveLabels = null;
        if (src.activeLabels != null)
        {
            activeLabels = new ArrayList<Label>(src.activeLabels.size());
            remainingActiveLabels = new ArrayList<Label>(src.activeLabels);
        }

        ArrayList<Label> remainingPendingLabels = null;
        if (src.pendingLabels != null)
        {
            pendingLabels = new ArrayList<Label>(src.pendingLabels.size());
            remainingPendingLabels = new ArrayList<Label>(src.pendingLabels);
        }

        // Shallow-copy the instructions.
        if (!src.isEmpty())
        {
            insn1 = updateInstructionLabel(src.insn1, remainingActiveLabels, remainingPendingLabels);
            insn2 = updateInstructionLabel(src.insn2, remainingActiveLabels, remainingPendingLabels);
            insn3 = updateInstructionLabel(src.insn3, remainingActiveLabels, remainingPendingLabels);

            if (src.leafInstructions != null)
            {
                leafInstructions = new ArrayList<Instruction>(src.leafInstructions.size());

                for (int i = 0; i < src.leafInstructions.size(); i++)
                    leafInstructions.add(updateInstructionLabel(src.leafInstructions.get(i), remainingActiveLabels, remainingPendingLabels));
            }
        }

        // clone any remaining labels from the src InstructionList which aren't
        // referenced by an Instruction
        if (remainingActiveLabels != null)
        {
            for (Label label : remainingActiveLabels)
            {
                activeLabels.add((Label)label.clone());
            }
        }

        if (remainingPendingLabels != null)
        {
            for (Label label : remainingPendingLabels)
            {
                pendingLabels.add((Label)label.clone());
            }
        }
    }

    /**
     * @return the first Instruction in this list.
     * @throws NoSuchElementException if there are no instructions.
     */
    public Instruction firstElement()
    {
        checkValidity();
        
        switch (getStorageState())
        {
            case Variable:
            {
                return leafInstructions.get(0);
            }
            case Ins1:
            case Ins2:
            case Ins3:
            {
                return insn1;
            }
            default:
            {
                throw new NoSuchElementException();
            }
        }
    }

    /**
     * @return the last element found.
     * @throws NoSuchElementException if there are no instructions.
     */
    public Instruction lastElement()
    {
        checkValidity();
        
        switch (getStorageState())
        {
            case Variable:
            {
                return leafInstructions.get(leafInstructions.size() - 1);
            }
            case Ins3:
            {
                return insn3;
            }
            case Ins2:
            {
                return insn2;
            }
            case Ins1:
            {
                return insn1;
            }
            default:
            {
                throw new NoSuchElementException();
            }
        }
    }

    /**
     * @return true if no Instructions are found.
     */
    public boolean isEmpty()
    {
        checkValidity();
        return size() == 0;
    }

    /**
     * Fetch an InstructionList to which instructions can be appended.
     * Synthesizes one if not already present.
     * 
     * @return said list.
     */
    public ArrayList<Instruction> getInstructions()
    {
        checkValidity();

        switch (getStorageState())
        {
            case Ins3:
            {
                leafInstructions = new ArrayList<Instruction>();
                leafInstructions.add(insn1);
                insn1 = null;
                leafInstructions.add(insn2);
                insn2 = null;
                leafInstructions.add(insn3);
                insn3 = null;
                break;
            }
            case Ins2:
            {
                leafInstructions = new ArrayList<Instruction>();
                leafInstructions.add(insn1);
                insn1 = null;
                leafInstructions.add(insn2);
                insn2 = null;
                break;
            }
            case Ins1:
            {
                leafInstructions = new ArrayList<Instruction>();
                leafInstructions.add(insn1);
                insn1 = null;
                break;
            }
            case Empty:
            {
                leafInstructions = new ArrayList<Instruction>();
                break;
            }
            case Variable:
            {
                //  Nothing to do.
                break;
            }
            default:
            {
                assert false : "Unknown storage state " + getStorageState();
            }
        }

        return leafInstructions;
    }

    /**
     * Fetch this InstructionList's label.
     * 
     * @return a synthetic Label that identifies this InstructionList.
     */
    public Label getLabel()
    {
        checkValidity();
        Label result = new Label();
        labelFirst(result);
        return result;
    }

    /**
     * Add an instruction to the sequence.
     * 
     * @param insn the instruction to be added.
     * @return the input insn.
     */
    public Instruction addInstruction(Instruction insn)
    {
        checkValidity();

        //  If the incoming instruction is executable,
        //  then any pending labels can be resolved to
        //  its location.
        if (insn.isExecutable())
            resolvePendingLabels();

        switch (getStorageState())
        {
            //  Adding an instruction to a full fixed-storage
            //  configuration spills into variable-length storage.
            case Ins3:
            case Variable:
            {
                getInstructions().add(insn);
                break;
            }
            case Empty:
            {
                insn1 = insn;
                break;
            }
            case Ins1:
            {
                insn2 = insn;
                break;
            }
            case Ins2:
            {
                insn3 = insn;
                break;
            }
            default:
            {
                assert false : "Unknown storage state " + getStorageState();
            }
        }

        return insn;
    }

    /**
     * Convenience method adds an instruction with no operands.
     * 
     * @param opcode - the instruction's opcode.
     * @return the generated instruction.
     */
    public Instruction addInstruction(int opcode)
    {
        return addInstruction(InstructionFactory.getInstruction(opcode));
    }

    /**
     * Convenience method adds an instruction with an immediate operand.
     * 
     * @param opcode - the instruction's opcode.
     * @param immed - the immediate operand.
     */
    public Instruction addInstruction(int opcode, int immed)
    {
        return addInstruction(InstructionFactory.getInstruction(opcode, immed));
    }

    /**
     * Convenience method adds an instruction with operands.
     * 
     * @param opcode - the instruction's opcode.
     * @param operands - the  operands.
     */
    public Instruction addInstruction(int opcode, Object[] operands)
    {
        return addInstruction(InstructionFactory.getInstruction(opcode, operands));
    }

    /**
     * Convenience method adds an instruction with a single operand.
     * 
     * @param opcode - the instruction's opcode.
     * @param operand - the operand.
     */
    public Instruction addInstruction(int opcode, Object operand)
    {
        return addInstruction(InstructionFactory.getInstruction(opcode, operand));
    }

    /**
     * Add another InstructionList to the sequence.
     * 
     * @param src_list the InstructionList to be added.
     * @post instructions and pending labels from src_list appended to this
     * list.
     * @post src_list is invalidated.
     * @post if this list had any pending labels, and src_list contained at
     * least one instruction, the pending labels from this list are resolved to
     * the first instruction contributed by src_list.
     */
    public void addAll(InstructionList src_list)
    {
        checkValidity();
        src_list.checkValidity();

        //  Inherit active labels from the other list, and
        //  adjust their positions relative to this list.
        if (src_list.activeLabels != null)
        {
            if (!isEmpty())
            {
                for (Label l : src_list.activeLabels)
                {
                    l.adjustOffset(size());
                }
            }

            getActiveLabels().addAll(src_list.activeLabels);
        }

        if (!src_list.isEmpty())
        {
            //  If the new instruction sequence contains any executable instructions,
            //  resolve this list's pending labels to the position of the first new
            //  executable instruction in the merged list.
            int firstExecutableOffset = src_list.firstExecutableOffset();
            if (firstExecutableOffset != NO_EXECUTABLE_INSTRUCTIONS)
                resolvePendingLabels(size() + firstExecutableOffset);

            //  This state machine copies src_list's fixed or variable
            //  storage into this list with as few calls to getInstructions()
            //  as feasible, since getInstructions() always converts this
            //  list to relatively expensive variable-length storage.
            StorageState this_state = getStorageState();
            StorageState src_state = src_list.getStorageState();

            switch (this_state)
            {
                case Empty:
                {
                    switch (src_state)
                    {
                        case Empty:
                        {
                            break;
                        }
                        case Ins1:
                        case Ins2:
                        case Ins3:
                        {
                            insn1 = src_list.insn1;
                            insn2 = src_list.insn2;
                            insn3 = src_list.insn3;
                            break;
                        }
                        case Variable:
                        {
                            //  Note: shared src_list objects will
                            //  clone their leafInstructions list.
                            leafInstructions = src_list.leafInstructions;
                            break;
                        }
                        default:
                        {
                            assert false : "Unknown storage state " + src_state;
                        }
                    }

                    break;
                }
                case Ins1:
                {
                    switch (src_state)
                    {
                        case Empty:
                        {
                            break;
                        }
                        case Ins1:
                        case Ins2:
                        {
                            insn2 = src_list.insn1;
                            insn3 = src_list.insn2;
                            break;
                        }
                        case Ins3:
                        {
                            getInstructions().add(src_list.insn1);
                            getInstructions().add(src_list.insn2);
                            getInstructions().add(src_list.insn3);
                            break;
                        }
                        case Variable:
                        {
                            getInstructions().addAll(src_list.getInstructions());
                            break;
                        }
                        default:
                        {
                            assert (false) : "Unknown storage state " + src_state;
                        }
                    }

                    break;
                }
                case Ins2:
                {
                    switch (src_state)
                    {
                        case Empty:
                        {
                            break;
                        }
                        case Ins1:
                        {
                            insn3 = src_list.insn1;
                            break;
                        }
                        case Ins2:
                        {
                            getInstructions().add(src_list.insn1);
                            getInstructions().add(src_list.insn2);
                            break;
                        }
                        case Ins3:
                        {
                            getInstructions().add(src_list.insn1);
                            getInstructions().add(src_list.insn2);
                            getInstructions().add(src_list.insn3);
                            break;
                        }
                        case Variable:
                        {
                            getInstructions().addAll(src_list.getInstructions());
                            break;
                        }
                        default:
                        {
                            assert (false) : "Unknown storage state " + src_state;
                        }
                    }

                    break;
                }
                case Ins3:
                {
                    switch (src_state)
                    {
                        case Empty:
                        {
                            break;
                        }
                        case Ins1:
                        {
                            getInstructions().add(src_list.insn1);
                            break;
                        }
                        case Ins2:
                        {
                            getInstructions().add(src_list.insn1);
                            getInstructions().add(src_list.insn2);
                            break;
                        }
                        case Ins3:
                        {
                            getInstructions().add(src_list.insn1);
                            getInstructions().add(src_list.insn2);
                            getInstructions().add(src_list.insn3);
                            break;
                        }
                        case Variable:
                        {
                            getInstructions().addAll(src_list.getInstructions());
                            break;
                        }
                        default:
                        {
                            assert (false) : "Unknown storage state " + src_state;
                        }
                    }

                    break;
                }
                case Variable:
                {
                    switch (src_state)
                    {
                        case Empty:
                        {
                            break;
                        }
                        case Ins1:
                        {
                            leafInstructions.add(src_list.insn1);
                            break;
                        }
                        case Ins2:
                        {
                            leafInstructions.add(src_list.insn1);
                            leafInstructions.add(src_list.insn2);
                            break;
                        }
                        case Ins3:
                        {
                            leafInstructions.add(src_list.insn1);
                            leafInstructions.add(src_list.insn2);
                            leafInstructions.add(src_list.insn3);
                            break;
                        }
                        case Variable:
                        {
                            leafInstructions.addAll(src_list.getInstructions());
                            break;
                        }
                        default:
                        {
                            assert (false) : "Unknown storage state " + src_state;
                        }
                    }
                    break;
                }
                default:
                {
                    assert false : "Unknown storage state " + this_state;
                }
            }

        }

        //  Inherit any pending labels from the other list.
        if (src_list.pendingLabels != null)
        {
            if (pendingLabels == null)
                pendingLabels = new ArrayList<Label>();

            pendingLabels.addAll(src_list.pendingLabels);
        }

        //  Invalidate the source list.
        src_list.isValid = false;
    }

    /**
     * Label the first executable instruction in this InstructionList.
     * 
     * @param l - the Label to be associated with the first executable
     * instruction in the list.
     */
    public void labelFirst(Label l)
    {
        if (firstExecutableOffset() != NO_EXECUTABLE_INSTRUCTIONS)
            addLabelAt(l, firstExecutableOffset());

        else
            labelNext(l);
    }

    /**
     * Label the last executable instruction in this InstructionList.
     * 
     * @param l - the Label to be associated with the last executable
     * instruction in the list.
     */
    public void labelCurrent(Label l)
    {
        if (l.targetMustBeExecutable() && lastExecutableOffset() != NO_EXECUTABLE_INSTRUCTIONS)
            addLabelAt(l, lastExecutableOffset());

        else if (!l.targetMustBeExecutable() && size() > 0)
            addLabelAt(l, size() - 1);

        else
            labelNext(l);
    }

    /**
     * Add a label at an arbitrary position.
     * 
     * @param l - the label.
     * @param pos - the label's position.
     */
    public void addLabelAt(Label l, int pos)
    {
        checkValidity();
        
        if (!isEmpty())
        {
            assert (pos < size());

            if (l.getPosition() == Label.NO_POSITION)
                l.setPosition(pos);
            else
                assert (l.getPosition() == pos) : "Label position " + l.getPosition() + " != " + pos;
            addLabel(l);
        }
        else
        {
            assert (pos == 0);
            labelNext(l);
        }
    }

    private void addLabel(Label l)
    {
        getActiveLabels().add(l);
    }

    /**
     * @return this InstructionList's active labels (i.e., labels resolved to a
     * known offset within this InstructionList).
     */
    public ArrayList<Label> getActiveLabels()
    {
        checkValidity();
        
        if (null == activeLabels)
            activeLabels = new ArrayList<Label>();
        
        return activeLabels;

    }

    /**
     * @return a Label bound to the last position in this InstructionList.
     * @pre the list cannot be empty.
     */
    public Label getLastLabel()
    {
        checkValidity();

        Label result = new Label();
        //  The InstructionList is never empty
        //  when this routine is called.
        labelCurrent(result);
        return result;
    }

    /**
     * Add a Label to this InstructionList's pendingLabels.
     * 
     * @param l - the Label to be set pending.
     */
    public void labelNext(Label l)
    {
        checkValidity();

        if (null == pendingLabels)
            pendingLabels = new ArrayList<Label>();
        
        pendingLabels.add(l);
    }

    /**
     * A suitable target instruction has presented itself; resolve all pending
     * labels.
     */
    private void resolvePendingLabels()
    {
        resolvePendingLabels(size());
    }

    private void resolvePendingLabels(int offset)
    {
        if (pendingLabels != null)
        {
            for (Label l : pendingLabels)
            {
                l.setPosition(offset);
            }

            //  Move the pending labels to active status.
            getActiveLabels().addAll(pendingLabels);
            pendingLabels = null;
        }
    }

    /**
     * Search this InstructionList for the first executable instruction (i.e.,
     * not a debugging instruction).
     * 
     * @return the offset of this instruction, or NO_EXECUTABLE_INSTRUCTIONS
     */
    private int firstExecutableOffset()
    {
        switch (getStorageState())
        {
            case Variable:
            {
                int size = leafInstructions.size();

                for (int offset = 0; offset < size; offset++)
                {
                    if (leafInstructions.get(offset).isExecutable())
                        return offset;
                }
                break;
            }
            case Ins3:
            {
                if (insn1.isExecutable())
                    return 0;
                else if (insn2.isExecutable())
                    return 1;
                else if (insn3.isExecutable())
                    return 2;
                break;
            }
            case Ins2:
            {
                if (insn1.isExecutable())
                    return 0;
                if (insn2.isExecutable())
                    return 1;
                break;
            }
            case Ins1:
            {
                if (insn1.isExecutable())
                    return 0;
                break;
            }
            default:
                assert (false) : "Unknown storage state " + getStorageState();
                // fall through
            case Empty:
                break;
        }

        return NO_EXECUTABLE_INSTRUCTIONS;
    }

    /**
     * Search this InstructionList for the last executable instruction (i.e.,
     * not a debugging instruction).
     * 
     * @return the offset of this instruction, or NO_EXECUTABLE_INSTRUCTIONS
     */
    private int lastExecutableOffset()
    {
        switch (getStorageState())
        {
            case Variable:
            {
                for (int offset = size() - 1; offset >= 0; offset--)
                    if (leafInstructions.get(offset).isExecutable())
                        return offset;
                break;
            }
            case Ins3:
            {
                if (insn3.isExecutable())
                    return 2;
                else if (insn2.isExecutable())
                    return 1;
                else if (insn1.isExecutable())
                    return 0;
                break;
            }
            case Ins2:
            {
                if (insn2.isExecutable())
                    return 1;
                else if (insn1.isExecutable())
                    return 0;
                break;
            }
            case Ins1:
            {
                if (insn1.isExecutable())
                    return 0;
                break;
            }
            default:
                assert (false) : "Unknown storage state " + getStorageState();
                // fall through
            case Empty:
                break;
        }

        return NO_EXECUTABLE_INSTRUCTIONS;
    }

    /**
     * @return the size of this InstructionList; zero if the list has no
     * instructions, or the number of instructions.
     */
    public int size()
    {
        //  Note: Explicitly does not call checkValidity(),
        //  size() is called even after the list is invalidated.
        switch (getStorageState())
        {
            case Variable:
                return leafInstructions.size();
                
            case Ins3:
                return 3;
                
            case Ins2:
                return 2;
                
            case Ins1:
                return 1;
                
            default:
                assert false : "Unknown storage state " + getStorageState();
                // fall through
            case Empty:
                return 0;
        }
    }

    /**
     * Query the storage configuration.
     * 
     * @return one of the values of StorageState that describes the present
     * storage configuration.
     */
    private StorageState getStorageState()
    {
        if (leafInstructions != null)
        {
            return StorageState.Variable;
        }
        else if (insn3 != null)
        {
            assert (insn1 != null && insn2 != null);
            return StorageState.Ins3;
        }
        else if (insn2 != null)
        {
            assert (insn1 != null);
            return StorageState.Ins2;
        }
        else if (insn1 != null)
        {
            return StorageState.Ins1;
        }
        else
        {
            return StorageState.Empty;
        }
    }

    /**
     * @return true if this InstructionList has unresolved pending labels.
     */
    public boolean hasPendingLabels()
    {
        checkValidity();
        
        return pendingLabels != null && !pendingLabels.isEmpty();
    }

    /**
     * This InstructionList is the "body" of a statement that needs to add more
     * logic. Any pending labels from the body statement(s) need to be
     * re-assigned to the owning InstructionList at a later time.
     * 
     * @return this list's [former] pendingLabels.
     * @post pendingLabels is null.
     */
    public Collection<Label> stripPendingLabels()
    {
        checkValidity();
        
        ArrayList<Label> pending_labels = pendingLabels;
        pendingLabels = null;
        return pending_labels;
    }

    /**
     * Add pending labels acquired from a component list to this list's
     * pendingLabels collection.
     * 
     * @param prev_pending - pending labels returned by a call to
     * stripPendingLabels().
     */
    public void addAllPendingLabels(Collection<Label> prev_pending)
    {
        checkValidity();
        
        if (prev_pending != null)
        {
            if (null == pendingLabels)
                pendingLabels = new ArrayList<Label>();

            pendingLabels.addAll(prev_pending);
        }
    }

    /**
     * @return true if the given InstructionList does not have an unconditional
     * transfer of control as its last instruction.
     */
    public boolean canFallThrough()
    {
        checkValidity();
        
        boolean can_fall_through = true;

        if (size() > 0)
        {
            //  Look for an unconditional transfer of control.
            int last_opcode = lastElement().getOpcode();

            can_fall_through = ABCConstants.OP_returnvoid != last_opcode &&
                               ABCConstants.OP_returnvalue != last_opcode &&
                               ABCConstants.OP_jump != last_opcode &&
                               ABCConstants.OP_throw != last_opcode;
        }

        return can_fall_through;
    }

    /**
     * Ensure an InstructionList is not used after it's been invalidated.
     */
    private void checkValidity()
    {
        if (!isValid)
            throw new IllegalStateException("Invalid InstructionList");
    }

    /**
     * If the src Instruction contains a Label operand, return a new Instruction
     * with a new cloned Label, otherwise the same Instruction will be returned.
     * When a new Label is introduced, the relevant active or pending labels
     * collection will also be updated.
     */
    private Instruction updateInstructionLabel(Instruction src, Collection<Label> srcActiveLabels, Collection<Label> srcPendingLabels)
    {
        if (src == null)
            return null;

        // non-targetable instructions don't have labels
        if (!src.isTargetableInstruction())
            return src;

        final int operandCount = src.getOperandCount();
        if (operandCount == 0)
            return src;

        Object[] newOperands = new Object[operandCount];
        for (int i = 0; i < operandCount; i++)
        {
            Object operand = src.getOperand(i);
            if (!(operand instanceof Label))
            {
                newOperands[i] = operand;
                continue;
            }

            Label srcLabel = (Label)operand;
            Label clonedLabel = (Label)(srcLabel).clone();
            newOperands[i] = clonedLabel;

            // Remove the src Label from the active or pending set, and add
            // in the new cloned Label into the set from whence in came.
            if (srcActiveLabels != null && srcActiveLabels.remove(srcLabel))
                activeLabels.add(clonedLabel);
            else if (srcPendingLabels != null && srcPendingLabels.remove(srcLabel))
                pendingLabels.add(clonedLabel);
        }

        if (operandCount == 1)
            return InstructionFactory.getInstruction(src.getOpcode(), newOperands[0]);
        else
            return InstructionFactory.getInstruction(src.getOpcode(), newOperands);
    }

    /**
     * Format the InstructionList for debugging purposes.
     */
    @Override
    public String toString()
    {
        StringBuffer result = new StringBuffer();

        if (insn1 != null)
        {
            result.append(insn1.toString());
            result.append('\n');
        }

        if (insn2 != null)
        {
            result.append(insn2.toString());
            result.append('\n');
        }

        if (insn3 != null)
        {
            result.append(insn3.toString());
            result.append('\n');
        }

        if (leafInstructions != null)
        {
            // TODO: List out the labels.
            for (Instruction insn : leafInstructions)
            {
                result.append(insn.toString());
                result.append('\n');
            }
        }
        return result.toString();
    }

    /**
     * Clone this InstructionList; make a shallow copy of the Instructions, deep
     * copies of the labels.
     * 
     * @return a cloned InstructionList.
     */
    @Override
    public Object clone()
    {
        InstructionList newb = null;
        try
        {
            newb = (InstructionList)super.clone();
        }
        catch (Exception cantHappen)
        {
            //  Compiler appeasement.
            assert false : cantHappen;
        }

        if(newb != null) {
            newb.copyInstructionList(this);
        }
        return newb;
    }

    /**
     * Search for an Instruction with a specific opcode.
     * 
     * @param opcode - the opcode of interest.
     * @return true if an instruction with this opcode is part of this
     * InstructionList.
     */
    public boolean hasSuchInstruction(int opcode)
    {
        return findOccurrences(opcode, true) > 0;
    }

    /**
     * Count occurrences of an Instruction with a specific opcode.
     * 
     * @param opcode - the opcode of interest.
     * @return the count of occurrences.
     */
    public int countOccurrences(int opcode)
    {
        return findOccurrences(opcode, false);
    }

    /**
     * Find occurrences of an Instruction with a specific opcode.
     * 
     * @param opcode - the opcode of interest.
     * @param stop_after_first - return a nonzero count after finding at least
     * one occurence (1-3 occurences in the fixed storage, the first occurence
     * in variable storage).
     * @return the count of occurrences, or 0/1/2/3 if stop_after_first is set.
     */
    public int findOccurrences(int opcode, boolean stop_after_first)
    {
        int result = 0;
        
        switch (getStorageState())
        {
            case Empty:
                break;
                
            case Ins3:
                if (insn3.getOpcode() == opcode)
                    result++;
                //  fall through
                
            case Ins2:
                if (insn2.getOpcode() == opcode)
                    result++;
                // fall through
                
            case Ins1:
                if (insn1.getOpcode() == opcode)
                    result++;
                break;
                
            case Variable:
                for (Instruction insn : leafInstructions)
                {
                    if (insn.getOpcode() == opcode)
                    {
                        result++;
                        if (stop_after_first)
                            break;
                    }
                }
        }

        return result;
    }

    /**
     * Add an Instruction to this InstructionList to push a numeric constant
     * onto the value stack.
     * 
     * @param value - the value to push.
     */
    public void pushNumericConstant(final long value)
    {
        if (value >= -128 && value < 128)
            addInstruction(OP_pushbyte, (int)(value));
        
        else if (value > 0 && value < 32768)
            addInstruction(OP_pushshort, (int)value);
        
        else if (value > -0xFFFFFFFF && value < 0XFFFFFFFE)
            addInstruction(OP_pushint, Integer.valueOf((int)value));
        
        else
            addInstruction(OP_pushdouble, Double.valueOf(value));
    }

    /**
     * Interface used by code-gen clients that need a chance to adjust the
     * instruction list after it is generated. Clients are discouraged from
     * doing this, and use this interface at their own risk
     */
    public interface IFilter
    {
        public InstructionList filter(InstructionList il);
    }
}
