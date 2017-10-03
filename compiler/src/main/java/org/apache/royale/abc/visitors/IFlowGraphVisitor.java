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

import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.semantics.Instruction;

/**
 * A IFlowGraphVisitor defines the behavior of a visitor over a control flow
 * graph.
 */
public interface IFlowGraphVisitor
{
    /**
     * Visit a new Block.
     * 
     * @param b - the Block to visit.
     * @return true if the walker should continue visiting the block.
     */
    boolean visitBlock(IBasicBlock b);

    /**
     * Finish visiting a Block.
     * 
     * @param b - the Block. It must be the same block last visited by
     * visitBlock() where that call returned true.
     */
    void visitEnd(IBasicBlock b);

    /**
     * Visit an Instruction within the most recently-visited Block.
     * 
     * @param insn - the Instruction.
     */
    void visitInstruction(Instruction insn);
}
