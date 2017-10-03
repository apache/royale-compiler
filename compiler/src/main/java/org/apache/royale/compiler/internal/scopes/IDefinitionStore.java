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

import java.util.Collection;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.scopes.IDefinitionSet;

/**
 * An {@code IDefinitionStore} is used by a scope to store a collection
 * of definitions, organized into sets of definition sthat have the same
 * base name.
 * <p>
 * The {@code IDefinitionStore} is completely private to the scope
 * and is not exposed by any scope APIs.
 * <p>
 * Since many scopes contain only a few definitions, there are multiple
 * implementations of this interface, optimized for storing 0, 1, 2, 4, 8,
 * and an unlimited number of definitions.
 */
public interface IDefinitionStore
{
    /**
     * Gets the maximum number of definition sets that this
     * definition store can hold.
     * 
     * @return The maximum number of definition sets.
     */
    int getCapacity();
    
    /**
     * Creates another definition store with a greater capacity.
     * <p>
     * When a call to {@link #add}() on this store returns <code>false</code>
     * (because the store has reached its capacity), call this method
     * to create a larger store and then call <code>add()</code>) on it.
     * The second <code>add()</code> is guaranteed to succeed.
     * 
     * @return An {@link IDefinitionStore} with a greater capacity.
     */
    IDefinitionStore createLargerStore();

    /**
     * Adds a definition to this store.
     * 
     * @param definition The {@link IDefinition} to add.
     * @return A flag indicating whether the definition was added. It may not be
     * possible to add the definition because some implementations of
     * {@link IDefinitionStore} have limited storage capacity, as a memory
     * optimization.
     */
    boolean add(IDefinition definition);

    /**
     * Removes a definition from this store.
     * 
     * @param definition The {@link IDefinition} to remove.
     * @return A flag indicating whether the definition was found.
     */
    boolean remove(IDefinition definition);

    /**
     * Gets the set of definitions in this store that have a specified base name.
     * <p>
     * This method is called very frequently as part of name resolution.
     * All implementations should be fast and avoid creating temporary objects.
     * 
     * @param baseName The base name of the definitions you want to retrieve
     * from the store.
     * @return An {@link IDefinitionSet} containing the definition with that
     * base name, or <code>null</code> if there are none.
     */
    IDefinitionSet getDefinitionSetByName(String baseName);
    
    /**
     * Puts a specified set of definitions with a specified base name
     * into this store.
     * <p>
     * This is only used when a project scope has to replace a definition set
     * in its store because it has converted definition promises in the
     * set to actual definitions.
     * 
     * @param baseName The base name of the definitions in the set.
     * @param set The {@link IDefinitionSet} of definitions to put
     * into the store.
     */
    void putDefinitionSetByName(String baseName, IDefinitionSet set);
    
    /**
     * Gets the base names of all the definition sets in this store.
     * 
     * @return An unordered collection of Strings for the base names.
     */
    Collection<String> getAllNames();

    /**
     * Gets all the definitions sets in this store.
     * 
     * @return An unordered collection of {@link IDefinitionSet} objects.
     */
    Collection<IDefinitionSet> getAllDefinitionSets();
    
    /**
     * Gets all the definitions in this store.
     * 
     * @return An ordered collection of {@link IDefinition} objects.
     */
    Collection<IDefinition> getAllDefinitions();
}
