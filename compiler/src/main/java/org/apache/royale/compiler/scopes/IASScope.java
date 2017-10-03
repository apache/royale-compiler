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

import java.util.Collection;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IScopedDefinition;
import org.apache.royale.compiler.tree.as.IScopedNode;

/**
 * {@link IASScope} represents a scope found within ActionScript.
 * Together with {@link IDefinition}, it is the basis of the compiler's
 * symbol table.
 * <p>
 * Scopes keep track of the definitions defined within a section of source code
 * (and also any <code>import</code> and <code>use namespace</code> directives).
 * <p>
 * It is common for a definition contained within a scope to contain another
 * scope, so scopes and definitions form a hierarchical structure.
 * <p>
 * Scopes exist at the level of projects, files, packages, classes, interfaces,
 * functions, <code>catch</code> blocks, and <code>with</code> blocks.
 * <p>
 * A file scope (which may contain package, class, interface, function,
 * <code>catch</code>, and <code>with</code> scopes) is the local symbol table
 * for resolving references in a compilation unit to definitions within that
 * same compilation unit.
 * <p>
 * A project scope, populated with any externally visible definitions from each
 * file scope, is the global symbol table for resolving reference in a compilation
 * unit to definitions in other compilation units.
 * <p>
 * Each {@link IASScope} supports the following behavior:
 * <ul>
 * <li>Getting the containing scope.</li>
 * <li>Getting the scoped definition that produced the scope.</li>
 * <li>Getting the scoped node that produced the scope.</li>
 * <li>Getting the definitions in the scope that have a specified base name.</li>
 * <li>Getting all the base names of the definitions in the scope.</li>
 * <li>Getting all the sets of definitions in the scope that have a common base name.</li>
 * <li>Getting all the definitions in the scope.</li>
 * </ul>
 */
public interface IASScope
{
    /**
     * Gets the scope that lexically contains this scope.
     * 
     * @return The {@link IASScope} that contains this scope,
     * or <code>null</code> if there is no such scope.
     */
    IASScope getContainingScope();

    /**
     * Gets the scoped definition that produced this scope.
     * 
     * @return The {@link IScopedDefinition} that contains this scope.
     */
    IScopedDefinition getDefinition();

    /**
     * Gets the scoped node corresponding to this scope.
     * 
     * @return The {@link IScopedNode} corresponding to this scope.
     */
    IScopedNode getScopeNode();
    
    /**
     * Gets the set of definitions in this one scope that have the specified base name.
     * 
     * @param baseName A String specifying a base name.
     * @return An {@link IDefinitionSet} containing definitions with that base name,
     * or <code>null</code> if there are no such definitions.
     */
    IDefinitionSet getLocalDefinitionSetByName(String baseName);

    /**
     * Gets all the base names of definitions in this one scope.
     * 
     * @return The base names as a sorted array of Strings.
     */
    Collection<String> getAllLocalNames();
    
    /**
     * Gets all the definition sets in this one scope.
     * 
     * @return A Collection of {@link IDefinitionSet} objects.
     */
    Collection<IDefinitionSet> getAllLocalDefinitionSets();
    
    /**
     * Gets all the definitions in this one scope.
     * 
     * @return A Collection of {@link IDefinition} objects.
     */
    Collection<IDefinition> getAllLocalDefinitions();
}
