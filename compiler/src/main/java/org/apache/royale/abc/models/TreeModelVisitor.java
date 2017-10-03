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
import org.apache.royale.abc.semantics.Name;

/**
 *  A TreeModelVisitor translates stack-oriented
 *  operations into a operation/operands view of
 *  an ABC method body.
 */
public interface TreeModelVisitor<T>
{
    /**
     *  Begin visiting a method's frames.
     *  @param model - the TreeModelEncoder that's
     *  driving the visitor.
     */
    public void visit(TreeModelEncoder<T> model);

    /**
     *  Finish visiting a method and its frames.
     */
    public void visitEnd();

    /**
     * Begin a visit to a block.
     * @param b the IBasicBlock to be visited.
     * @return true if the encoder should continue visiting the block.
     */
    public boolean visitBlock(IBasicBlock b);

    /**
     * Finish visiting a block.
     * @param b the last IBasicBlock to be confirmed to-be-visited by visitBlock().
     */
    public void visitEnd(IBasicBlock b);

    /**
     *  Translate an Instruction and its operands into 
     *  an intermediate form.
     *  @param insn - the Instruction that encoded this operation.
     *  @param operands - the Instruction's operands, usually from
     *    the ABC value stack.
     *  @return an equivalent representation of the 
     *    operation denoted by the instruction/operands tuple.
     */
    public T translate(Instruction insn, Collection<T> operands);

    /**
     *  Translate a branch Instruction and its targets into 
     *  an intermediate form.
     *  @param insn - the Instruction that encoded this operation.
     *  @param targets - the Instruction's targets.
     *  @return an equivalent representation of the 
     *    operation denoted by the instruction/targets tuple.
     */
    public T translateBranch(Instruction insn, Collection<IBasicBlock> targets);

    /**
     *  Recover from a value stack underflow condition.
     *  @param insn - the Instruction that attempted to encode
     *    this operation.
     *  @param count - the number of stack values required.
     *  @return a representation of the recovery action;
     *    returning some type of "bottom value" is recommended.
     */
    public T valueStackUnderflow(Instruction insn, int count);

    /**
     *  Recover from a scope stack underflow condition.
     *  @param insn - the Instruction that attempted to encode
     *    this operation.
     *  @param count - the number of scope stack values required.
     *  @return a representation of the recovery action;
     *    returning some type of "bottom value" is recommended.
     */
    public T scopeStackUnderflow(Instruction insn, int count);

    /**
     *  Get a representation of a function actual parameter.
     *  @param paramNumber - the parameter number.
     *  @return a representation of the parameter as an
     *    initializing value.
     */
    public T translateParameter(int paramNumber);

    /**
     * Get a representation of an exception variable.
     * @param varName the exception variable's name.
     * @param varType the exception variable's type.
     * @return a representation of the exception 
     *  variable as an initializing value.
     */
    public T translateExceptionVariable(Name varName, Name varType);

    /**
     *  Add a merge node, where values from several blocks
     *  combine in the dataflow graph.
     *  @param toInit - the Block where these values combine.
     *  @return a representation of the merge point; this
     *    object must implement @code{IMergePoint<T>}.
     */
    public T /* must implement IMergePoint<T> */ addMergePoint(IBasicBlock toInit);

    /**
     *  IMergePoint models a point where several predecessors'
     *  values combine in the dataflow graph.
     */
    public interface IMergePoint<T>
    {
        /**
         *  Add a predecessor's value to the collection
         *  of predecessor values.  The implementation
         *  is allowed to merge equal values, but may
         *  also choose not to do so.
         *  @param value - the new precedecessor value.
         */
        public void addValue(T value);

        /**
         * Get the values that predecessor blocks contributed
         * to this dataflow merge point.
         * @return the collection of predecessor values.
         */
        public Collection<T> getValues();
    }
}
