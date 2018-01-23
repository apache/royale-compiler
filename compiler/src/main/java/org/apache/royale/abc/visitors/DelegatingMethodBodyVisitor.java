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
 * Base class for various method body visitors that wish to modify the method
 * body this class just passes all calls through to the delegate
 */
public class DelegatingMethodBodyVisitor implements IMethodBodyVisitor
{
    public DelegatingMethodBodyVisitor(IMethodBodyVisitor delegate)
    {
        this.delegate = delegate;
    }

    private final IMethodBodyVisitor delegate;

    @Override
    public void visit()
    {
        delegate.visit();
    }

    @Override
    public void visitEnd()
    {
        delegate.visitEnd();
    }

    @Override
    public ITraitsVisitor visitTraits()
    {
        return delegate.visitTraits();
    }

    @Override
    public void visitInstructionList(InstructionList new_list)
    {
        delegate.visitInstructionList(new_list);
    }

    @Override
    public void visitInstruction(int opcode)
    {
        delegate.visitInstruction(opcode);
    }

    @Override
    public void visitInstruction(int opcode, int immediate_operand)
    {
        delegate.visitInstruction(opcode, immediate_operand);
    }

    @Override
    public void visitInstruction(int opcode, Object[] operands)
    {
        delegate.visitInstruction(opcode, operands);
    }

    @Override
    public void visitInstruction(int opcode, Object single_operand)
    {
        delegate.visitInstruction(opcode, single_operand);
    }

    public void visitInstruction(Instruction instruction)
    {
        delegate.visitInstruction(instruction);
    }

    @Override
    public int visitException(Label from, Label to, Label target, Name exception_type, Name catch_var)
    {
        return delegate.visitException(from, to, target, exception_type, catch_var);
    }

    @Override
    public void labelCurrent(Label l)
    {
        delegate.labelCurrent(l);
    }

    @Override
    public void labelNext(Label l)
    {
        delegate.labelNext(l);
    }
}
