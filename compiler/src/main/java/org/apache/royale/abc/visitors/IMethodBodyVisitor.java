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

package org.apache.royale.abc.visitors;

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.Name;

/**
 * An IMethodBodyVisitor defines a method's local variables and ABC instructions.
 */
public interface IMethodBodyVisitor extends IVisitor
{
    /**
     * Begin processing a method body.
     */
    void visit();

    /**
     * Visit the method body's traits (local variables).
     * 
     * @return a traits visitor, or null if the traits aren't of interest.
     */
    ITraitsVisitor visitTraits();

    /**
     * Reset the method body's instruction list en bloc.
     */
    void visitInstructionList(InstructionList newList);

    /**
     * Visit an instruction with no operands.
     * 
     * @param opcode - the instruction's opcode.
     */
    void visitInstruction(int opcode);

    /**
     * Visit an instruction with an immediate operand.
     * 
     * @param opcode - the instruction's opcode.
     * @param immediateOperand - the operand.
     */
    void visitInstruction(int opcode, int immediateOperand);

    /**
     * Visit an instruction.
     * 
     * @param opcode - the instruction's opcode.
     * @param operands - the instruction's operands.
     */
    void visitInstruction(int opcode, Object[] operands);

    /**
     * Visit an instruction with a single operand (convenience method).
     * 
     * @param opcode - the instruction's opcode.
     * @param singleOperand - the instruction's operand.
     */
    void visitInstruction(int opcode, Object singleOperand);

    /**
     * Vist an instruction
     * 
     * @param instruction the Instruction to visit
     */
    void visitInstruction(Instruction instruction);

    /**
     * Visit an exception handler.
     * 
     * @param from - the label that starts the "try" region.
     * @param to - the label that ends the "try" region.
     * @param target - the "catch" target.
     * @param exceptionType - the type of exception to be handled. "*" is valid
     * and handles any exception.
     * @param catchVar - the name of the exception variable. May be null if no
     * exception variable is desired.
     * @return the exception's exception number.
     */
    int visitException(Label from, Label to, Label target, Name exceptionType, Name catchVar);

    /**
     * Bind a Label object (not to be confused with the AVM OP_label
     * instruction) to the last-visited ABC instruction in this method.
     * 
     * @pre visitInstruction() must have been called at least once, i.e., there
     * must be a last-visited ABC instruction.
     */
    void labelCurrent(Label l);

    /**
     * Bind a Label object (not to be confused with the AVM OP_label
     * instruction) to the next ABC instruction that gets visited in this method
     * 
     * @pre visitInstruction() must be called at least once after labelNext,
     * i.e., there must be a next instruction.
     */
    void labelNext(Label l);
}
