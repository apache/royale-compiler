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
import java.util.Collections;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.scopes.IDefinitionSet;

/**
 * An implementation of {@link IDefinitionStore} that stores no definitions.
 * Many scopes do not need to store any definitions.
 * (For example, consider a typical getter.)
 * <p>
 * If scopes always have a non-null store, it makes the code simpler.
 * However, every empty store behaves the same and therefore scopes with
 * empty stores all share the singleton instance provided by
 * {@code EmptyDefinitionStore.SINGLETON}.
 */
public final class EmptyDefinitionStore implements IDefinitionStore
{
    /**
     * The singleton instance of this class.
     */
    public static final IDefinitionStore SINGLETON = new EmptyDefinitionStore();
        
    // Private constructor
    private EmptyDefinitionStore()
    {
    }
    
    @Override
    public int getCapacity()
    {
        return 0;
    }
    
    @Override
    public IDefinitionStore createLargerStore()
    {
        return new SmallDefinitionStore1();
    }
    
    @Override
    public boolean add(IDefinition definition)
    {
        return false;
    }

    @Override
    public boolean remove(IDefinition definition)
    {
        return false;
    }

    @Override
    public IDefinitionSet getDefinitionSetByName(String baseName)
    {
        return null;
    }

    @Override
    public void putDefinitionSetByName(String baseName, IDefinitionSet set)
    {
    }

    @Override
    public Collection<String> getAllNames()
    {
        return Collections.emptyList();
    }

    @Override
    public Collection<IDefinitionSet> getAllDefinitionSets()
    {
        return Collections.emptyList();
    }
    
    @Override
    public Collection<IDefinition> getAllDefinitions()
    {
        return Collections.emptyList();
    }
}
