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
 * up to 4 definitions using 4 fields rather than a map.
 * Many scopes contain only a few definitions.
 * <p>
 * The methods of this class are optimized to use unrolled loops.
 */
public final class SmallDefinitionStore4 extends SmallDefinitionStoreBase
{
    /**
     * Constructor.
     */
    public SmallDefinitionStore4()
    {
    }
    
    /**
     * Copy constructor.
     */
    public SmallDefinitionStore4(SmallDefinitionStore2 other)
    {
        definitionSet0 = other.definitionSet0;
        definitionSet1 = other.definitionSet1;
    }
    
    // Fields for storing up to 4 definition sets.
    IDefinitionSet definitionSet0;
    IDefinitionSet definitionSet1;
    IDefinitionSet definitionSet2;
    IDefinitionSet definitionSet3;
    
    @Override
    public int getCapacity()
    {
        return 4;
    }
    
    @Override
    public IDefinitionStore createLargerStore()
    {
        // Graduate from being able to store 4 definition sets
        // to being able to store 8 definition sets.
        return new SmallDefinitionStore8(this);
    }
    
    @Override
    public IDefinitionSet getDefinitionSetByName(String baseName)
    {
        assert baseName != null : "The baseName of a definition must be non-null";

        if (baseName.equals(getBaseName(definitionSet0)))
            return definitionSet0;
        if (baseName.equals(getBaseName(definitionSet1)))
            return definitionSet1;
        if (baseName.equals(getBaseName(definitionSet2)))
            return definitionSet2;
        if (baseName.equals(getBaseName(definitionSet3)))
            return definitionSet3;

        return null;
    }
    
    @Override
    public void putDefinitionSetByName(String baseName, IDefinitionSet set)
    {
        if (baseName.equals(getBaseName(definitionSet0)))
            definitionSet0 = set;
        if (baseName.equals(getBaseName(definitionSet1)))
            definitionSet1 = set;
        if (baseName.equals(getBaseName(definitionSet2)))
            definitionSet2 = set;
        if (baseName.equals(getBaseName(definitionSet3)))
            definitionSet3 = set;
    }
    
    @Override
    public Collection<String> getAllNames()
    {
        int n = getCapacity();
        List<String> list = new ArrayList<String>(n);
        
        addBaseNameToList(list, definitionSet0);
        addBaseNameToList(list, definitionSet1);
        addBaseNameToList(list, definitionSet2);
        addBaseNameToList(list, definitionSet3);
        
        return list;
    }

    @Override
    public Collection<IDefinitionSet> getAllDefinitionSets()
    {
        int n = getCapacity();
        List<IDefinitionSet> list = new ArrayList<IDefinitionSet>(n);

        addDefinitionSetToList(list, definitionSet0);
        addDefinitionSetToList(list, definitionSet1);
        addDefinitionSetToList(list, definitionSet2);
        addDefinitionSetToList(list, definitionSet3);

        return list;
    }
    
    @Override
    public Collection<IDefinition> getAllDefinitions()
    {
        int n = getCapacity();
        List<IDefinition> list = new ArrayList<IDefinition>(n);

        addDefinitionsToList(list, definitionSet0);
        addDefinitionsToList(list, definitionSet1);
        addDefinitionsToList(list, definitionSet2);
        addDefinitionsToList(list, definitionSet3);

        return list;
    }
    
    @Override
    protected int findIndexForBaseName(String baseName)
    {
        assert baseName != null : "The baseName of a definition must be non-null";

        if (baseName.equals(getBaseName(definitionSet0)))
            return 0;
        if (baseName.equals(getBaseName(definitionSet1)))
            return 1;
        if (baseName.equals(getBaseName(definitionSet2)))
            return 2;
        if (baseName.equals(getBaseName(definitionSet3)))
            return 3;

        return -1;
    }

    @Override
    protected int findAvailableIndex()
    {
        if (definitionSet0 == null)
            return 0;
        if (definitionSet1 == null)
            return 1;
        if (definitionSet2 == null)
            return 2;
        if (definitionSet3 == null)
            return 3;
        
        return -1;
        
    }

    @Override
    protected IDefinitionSet getDefinitionSet(int i)
    {
        assert i >= 0 && i < getCapacity() : "The index must be within the capacity of the store";

        switch (i)
        {
            case 0:
                return definitionSet0;
            case 1:
                return definitionSet1;
            case 2:
                return definitionSet2;
            case 3:
                return definitionSet3;
        }
        
        return null;
    }

    @Override
    protected void setDefinitionSet(int i, IDefinitionSet definitionSet)
    {
        assert i >= 0 && i < getCapacity() : "The index must be within the capacity of the store";

        switch (i)
        {
            case 0:
                definitionSet0 = definitionSet;
                break;
            case 1:
                definitionSet1 = definitionSet;
                break;
            case 2:
                definitionSet2 = definitionSet;
                break;
            case 3:
                definitionSet3 = definitionSet;
                break;
        }
    }
}
