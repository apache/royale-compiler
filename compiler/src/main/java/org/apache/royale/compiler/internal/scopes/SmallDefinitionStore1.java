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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.scopes.IDefinitionSet;

/**
 * An implementation of {@link IDefinitionStore} for storing
 * a single definition set using a field rather than a map.
 * Many scopes contain only a single definition set.
 */
public final class SmallDefinitionStore1 extends SmallDefinitionStoreBase
{    
    /**
     * Constructor.
     */
    public SmallDefinitionStore1()
    {
    }
    
    // Field for storing a single definition set.
    IDefinitionSet definitionSet0;
    
    @Override
    public int getCapacity()
    {
        return 1;
    }
    
    @Override
    public IDefinitionStore createLargerStore()
    {
        // Graduate from being able to store 1 definition set
        // to being able to store 2 definition sets.
        return new SmallDefinitionStore2(this);
    }
    
    @Override
    public IDefinitionSet getDefinitionSetByName(String baseName)
    {
        assert baseName != null : "The baseName of a definition must be non-null";

        if (baseName.equals(getBaseName(definitionSet0)))
            return definitionSet0;

        return null;
    }
    
    @Override
    public void putDefinitionSetByName(String baseName, IDefinitionSet set)
    {
        if (baseName.equals(getBaseName(definitionSet0)))
            definitionSet0 = set;
    }
    
    @Override
    public Collection<String> getAllNames()
    {
        int n = getCapacity();
        List<String> list = new ArrayList<String>(n);
        
        addBaseNameToList(list, definitionSet0);
        
        return list;
    }

    @Override
    public Collection<IDefinitionSet> getAllDefinitionSets()
    {
        int n = getCapacity();
        List<IDefinitionSet> list = new ArrayList<IDefinitionSet>(n);

        addDefinitionSetToList(list, definitionSet0);

        return list;
    }
    
    @Override
    public Collection<IDefinition> getAllDefinitions()
    {
        int n = getCapacity();
        List<IDefinition> list = new ArrayList<IDefinition>(n);

        addDefinitionsToList(list, definitionSet0);

        return list;
    }
        
    @Override
    protected int findIndexForBaseName(String baseName)
    {
        assert baseName != null : "The baseName of a definition must be non-null";

        if (baseName.equals(getBaseName(definitionSet0)))
            return 0;

        return -1;
    }

    @Override
    protected int findAvailableIndex()
    {
        if (definitionSet0 == null)
            return 0;
         
        return -1;
        
    }

    @Override
    public IDefinitionSet getDefinitionSet(int i)
    {
        assert i >= 0 && i < getCapacity() : "The index must be within the capacity of the store";
        
        switch (i)
        {
            case 0:
                return definitionSet0;
        }
        
        return null;
    }

    @Override
    public void setDefinitionSet(int i, IDefinitionSet definitionSet)
    {
        assert i >= 0 && i < getCapacity() : "The index must be within the capacity of the store";

        switch (i)
        {
            case 0:
                definitionSet0 = definitionSet;
        }
    }
}
