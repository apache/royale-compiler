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

package org.apache.royale.compiler.scopes;

import org.apache.royale.compiler.definitions.IDefinition;

/**
 * This interface represents an immutable collection of definitions with the
 * same base name in a particular scope.
 * <p>
 * It's really more of a List than a Set because it is indexable.
 */
public interface IDefinitionSet
{
    /**
     * Determines if this definition set is empty.
     * 
     * @return <code>true</code> if the definition set is empty.
     */
    boolean isEmpty();

    /**
     * @return The size of the definition set.
     */
    int getSize();
    
    /**
     * @return The maximum size of the definition set.
     */
    int getMaxSize();

    /**
     * Gets a definition in this set, by index.
     * 
     * @param i The index of the definition.
     * @return An {@link IDefinition} for the definition.
     */
    IDefinition getDefinition(int i);
}
