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
 * A Label represents the target of a branch instruction; it is not the ABC
 * OP_Label instruction. Note: this class has a natural ordering that is
 * inconsistent with equals.
 */
public final class Label implements Comparable<Label>, Cloneable
{
    /**
     * The inital offset of a label. This offset must be set to a known value
     * before the Label can be active.
     */
    public static final int NO_POSITION = -1;

    public Label(String label_text)
    {
        this(label_text, LabelKind.EXECUTABLE_INSTRUCTION);
    }

    public Label(String label_text, LabelKind mustTargetExectuable)
    {
        this.labelText = label_text;
        this.mustTargetExecutable = mustTargetExectuable;
    }

    public Label()
    {
        this.labelText = null;
    }

    /**
     * copy constructor used by clone()
     */
    private Label(Label src)
    {
        this.instructionPosition = src.instructionPosition;
        this.labelText = src.labelText;
    }

    /**
     * Descriptive text for a Label. This is a debugging aid that may be going
     * away soon.
     */
    private String labelText;

    /**
     * Does this label have to fall on an executable instruction, or can it fall
     * on any old instruction (such as a OP_debugline).
     */
    private LabelKind mustTargetExecutable;

    /**
     * The label's offset in its owning InstructionList.
     * 
     * @see InstructionList#labelFirst()
     * @see InstructionList#labelCurrent()
     * @see InstructionList#labelNext() 
     * which does not explicitly set the
     * position, but adds the label to the InstructionList's pendingLabels set,
     * which will convert the label to an active state with a known position
     * when the next instruction is added to the list.
     */
    private int instructionPosition = NO_POSITION;

    /**
     * The natural sort order for a Label is its instruction's offset.
     * <p>
     * @note Comparison is only valid between two Labels in the same
     * InstructionList.
     */
    @Override
    public int compareTo(Label o)
    {
        return this.instructionPosition - o.instructionPosition;
    }

    @Override
    public String toString()
    {
        return labelText != null ? labelText : super.toString() + " => " + instructionPosition;
    }

    /**
     * Set this Label's offset in its initial InstructionList.
     * 
     * @see #adjustOffset
     * , which the InstructionList keeps this position updated.
     * @param pos - the Label's offset in the InstructionList.
     */
    public void setPosition(int pos)
    {
        assert (this.instructionPosition == NO_POSITION) : "setPostition() after a position set: " + toString();
        this.instructionPosition = pos;
    }

    /**
     * @return this Label's offset in its owning InstructionList.
     */
    public int getPosition()
    {
        return this.instructionPosition;
    }

    /**
     * This Label has been inherited by a new InstructionList, so its offset
     * must be adjusted to point to its new position within the inheriting
     * InstructionList. (Or, the owning InstructionList's order was perturbed by
     * a prepend() or similar ad-hoc adjustment.)
     * 
     * @param base_offset - the amount to adjust the position. Think of the
     * position as a displacement, and the inherting list's size as a base.
     */
    public void adjustOffset(int base_offset)
    {
        assert (this.instructionPosition != NO_POSITION) : "adjustOffset() before any position set";
        this.instructionPosition += base_offset;
    }

    /**
     * Clone this label.
     * 
     * @return a shallow copy of the label (it doesn't have any deep state).
     */
    @Override
    public Object clone()
    {
        return new Label(this);
    }

    /**
     * Does this label have to fall on an executable instruction, or can it fall
     * on any old instruction (such as a OP_debugline).
     */
    public boolean targetMustBeExecutable()
    {
        return this.mustTargetExecutable == LabelKind.EXECUTABLE_INSTRUCTION;
    }
    
    public static enum LabelKind
    {
        // The label must label an executable instruction
        EXECUTABLE_INSTRUCTION,
        
        // the label may target any instruction, even a debug one
        ANY_INSTRUCTION
    }
}
