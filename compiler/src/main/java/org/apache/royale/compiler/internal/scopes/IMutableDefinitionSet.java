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

package org.apache.royale.compiler.internal.scopes;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.as.IDefinitionNode;

/**
 * This interface extends {@link IDefinitionSet} to add mutation methods.
 * <p>
 * It is implemented by {@link SmallDefinitionSet} and {@link LargeDefinitionSet}.
 * However, a single {@link IDefinition} acts as its own {@link IDefinitionSet}
 * of size 1, but is not a {@link IMutableDefinitionSet}.
 */
public interface IMutableDefinitionSet extends IDefinitionSet
{
    /**
     * Adds a definition to this set.
     * 
     * @param definition The {@link IDefinition} to add.
     */
    void addDefinition(IDefinition definition);

    /**
     * Removes a definition from this set.
     * 
     * @param definition The {@link IDefinitionNode} to remove.
     */
    void removeDefinition(IDefinition definition);

    /**
     * Replaces the {@link IDefinition} at the specified index with a new
     * definition.
     * 
     * @param i The index of the {@link IDefinition} to replace.
     * @param newDef The new {@link IDefinition} to store in the set.
     */
    void replaceDefinition(int i, IDefinition newDef);
}
