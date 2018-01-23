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

import java.util.Collection;

import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.semantics.Instruction;

/**
 *  The FrameModelVisitor interface presents an abstract view
 *  of ABC bytecode semantics, focused on maintenance of the
 *  AVM "frame" (the local variable slots, the scope stack, 
 *  and the value stack).
 */
public interface FrameModelVisitor<T>
{
    /**
     *  Begin a visit to a method body.
     *  @param encoder - the FrameModelEncoder driving the visit.
     */
    public void visit(FrameModelEncoder encoder);

    /**
     *  Finish visiting a method body.
     */
    public void visitEnd();

    /**
     *  Handle an instruction that does not affect the frame.
     *  @param i - the Instruction.
     *  @return a representation of the Instruction, which may
     *    be null if the visitor works by internal side effect.
     */
    public T noFrameEffect(Instruction i);

    /**
     *  Handle an instruction that consumes value stack elements.
     *  @param i - the Instruction.
     *  @param count - the number of value stack elements consumed.
     *  @return a representation of the Instruction and its operands,
     *  which may be null if the visitor works by internal side effect.
     */
    public T consumeValue(Instruction i, int count);

    /**
     *  Handle an instruction that pushes a value onto the stack.
     *  @param i - the Instruction.
     *  @return a representation of the Instruction, which may
     *    be null if the visitor works by internal side effect.
     */
    public T produceValue(Instruction i);

    /**
     *  Handle an instruction that consumes value stack elements,
     *  and then pushes a new value onto the stack.
     *  @param i - the Instruction.
     *  @param consumeCount - the number of value stack elements consumed.
     *  @return a representation of the Instruction and its operands,
     *  which may be null if the visitor works by internal side effect.
     */
    public T consumeAndProduceValue(Instruction i, int consumeCount);

    /**
     *  Handle a branch instruction.
     *  @param i - the Instruction.
     *  @param target - the Instruction's target.  Instructions with
     *    fall-through semantics also implicitly target the next Block.
     *  @return a representation of the Instruction, which may
     *    be null if the visitor works by internal side effect.
     */
    public T branch(Instruction i, IBasicBlock target);

    /**
     *  Handle a multibranch instruction.
     *  @param i - the Instruction.
     *  @param targets - the Instruction's targets.
     *  @return a representation of the Instruction, which may
     *    be null if the visitor works by internal side effect.
     */
    public T multiwayBranch(Instruction i, Collection<IBasicBlock> targets);

    /**
     *  Get a local variable, leaving its value on the stack.
     *  @param i - the Instruction.
     *  @param idx - the variable's index.
     *  @return a representation of the Instruction, which may
     *    be null if the visitor works by internal side effect.
     */
    public T getlocal(Instruction i, int idx);

    /**
     *  Set a local variable, comsuming a value from the stack.
     *  @param i - the Instruction.
     *  @param idx - the variable's index.
     *  @return a representation of the Instruction, which may
     *    be null if the visitor works by internal side effect.
     */
    public T setlocal(Instruction i, int idx);

    /**
     *  Modify (i.e., increment, decrement, or kill)
     *  a local variable.  Note that this does not
     *  yield an Instruction, it's a notification.
     *  @param i - the Instruction.
     *  @param idx - the variable's index.
     */
    public void modifyLocal(Instruction i, int idx);

    /**
     *  Move the top of the value stack to the top
     *  of the scope stack.
     *  @param i - the Instruction.
     *  @return a representation of the Instruction, which may
     *    be null if the visitor works by internal side effect.
     */
    public T moveValueToScopeStack(Instruction i);

    /**
     *  Pop the top of the scope stack.
     *  @param i - the Instruction.
     *  @return a representation of the Instruction, which may
     *    be null if the visitor works by internal side effect.
     */
    public T popscope(Instruction i);

    /**
     *  Get a value from the scope stack.
     *  @param i - the Instruction.
     *  @param idx - the index of the object in the scope stack.
     *  @return a representation of the Instruction, which may
     *    be null if the visitor works by internal side effect.
     */
    public T getScopeobject(Instruction i, int idx);

    /**
     *  Special-case the hasnext2 instruction.
     *  @param i - the Instruction.
     *  @return a representation of the Instruction, which may
     *    be null if the visitor works by internal side effect.
     */
    public T hasnext2(Instruction i);

    /**
     *  Duplicate the top of the stack.
     *  @param i - the Instruction.
     *  @return a representation of the Instruction, which may
     *    be null if the visitor works by internal side effect.
     */
    public T dup(Instruction i);


    /**
     *  Swap the two top values on the stack.
     *  @param i - the Instruction.
     *  @return a representation of the Instruction, which may
     *    be null if the visitor works by internal side effect.
     */
    public T swap(Instruction i);

    /**
     *  Visit a new Block.
     *  @param b - the Block.
     *  @return true if the stack encoder should continue visiting the Block.
     */
    public boolean visitBlock(IBasicBlock b);

    /**
     *  End a visit to a block.
     *  @param b - the Block.  Must be the block passed
     *    to the most recent visitBlock(b) call that returned true.
     */
    public void visitEndBlock(IBasicBlock b);

    /**
     *  Visit a control-flow edge from one Block to another.
     */
    public void visitEdge(IBasicBlock from, IBasicBlock to);
}
