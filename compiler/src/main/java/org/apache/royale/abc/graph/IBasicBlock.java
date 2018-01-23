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

package org.apache.royale.abc.graph;

import java.util.Collection;
import java.util.List;

import org.apache.royale.abc.semantics.Instruction;

/**
 *  IBasicBlock defines the minimum necessary semantics
 *  of a vertex in an AET method body's {@link IFlowgraph}.
 */
public interface IBasicBlock
{
    /**
     * Get this block's successors; this defines the set of
     * edges in the {@link IFlowgraph flowgraph}.
     * @return this block's successors in the control-flow graph.
     */
    Collection<? extends IBasicBlock> getSuccessors();

    /**
     * Get the contents of the block.
     * @return this block's instructions as a mutable List.
     */
    List<Instruction> getInstructions();

    /**
     * How big is this block?
     * @return the size of getInstructions.size().
     */
    int size();

    /**
     * Fetch the Instruction at the specified position.
     * @param idx - the Instruction's position.
     * @return the Instruction at that position.
     */
    Instruction get(int idx);

    /**
     * Can this block's control fall through the end?
     * @return true if the block doesn't unconditionally transfer control.
     */
    boolean canFallThrough();
}
