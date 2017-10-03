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
 * up to 8 definitions using 8 fields rather than a map.
 * Many scopes contain only a handful of definitions.
 * <p>
 * The methods of this class are optimized to use unrolled loops.
 */
public final class SmallDefinitionStore8 extends SmallDefinitionStoreBase
{
    /**
     * Constructor.
     */
    public SmallDefinitionStore8()
    {
    }
    
    /**
     * Copy constructor.
     */
    public SmallDefinitionStore8(SmallDefinitionStore4 other)
    {
        definitionSet0 = other.definitionSet0;
        definitionSet1 = other.definitionSet1;
        definitionSet2 = other.definitionSet2;
        definitionSet3 = other.definitionSet3;
    }
    
    // Fields for storing up to 8 definition sets.
    IDefinitionSet definitionSet0;
    IDefinitionSet definitionSet1;
    IDefinitionSet definitionSet2;
    IDefinitionSet definitionSet3;
    IDefinitionSet definitionSet4;
    IDefinitionSet definitionSet5;
    IDefinitionSet definitionSet6;
    IDefinitionSet definitionSet7;
    
    @Override
    public int getCapacity()
    {
        return 8;
    }
    
    @Override
    public IDefinitionStore createLargerStore()
    {
        // Graduate from being able to store 8 definition sets
        // to being able to store an unlimited number of definition sets.
        return new LargeDefinitionStore(this);
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
        if (baseName.equals(getBaseName(definitionSet4)))
            return definitionSet4;
        if (baseName.equals(getBaseName(definitionSet5)))
            return definitionSet5;
        if (baseName.equals(getBaseName(definitionSet6)))
            return definitionSet6;
        if (baseName.equals(getBaseName(definitionSet7)))
            return definitionSet7;

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
        if (baseName.equals(getBaseName(definitionSet4)))
            definitionSet4 = set;
        if (baseName.equals(getBaseName(definitionSet5)))
            definitionSet5 = set;
        if (baseName.equals(getBaseName(definitionSet6)))
            definitionSet6 = set;
        if (baseName.equals(getBaseName(definitionSet7)))
            definitionSet7 = set;
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
        addBaseNameToList(list, definitionSet4);
        addBaseNameToList(list, definitionSet5);
        addBaseNameToList(list, definitionSet6);
        addBaseNameToList(list, definitionSet7);
        
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
        addDefinitionSetToList(list, definitionSet4);
        addDefinitionSetToList(list, definitionSet5);
        addDefinitionSetToList(list, definitionSet6);
        addDefinitionSetToList(list, definitionSet7);

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
        addDefinitionsToList(list, definitionSet4);
        addDefinitionsToList(list, definitionSet5);
        addDefinitionsToList(list, definitionSet6);
        addDefinitionsToList(list, definitionSet7);

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
        if (baseName.equals(getBaseName(definitionSet4)))
            return 4;
        if (baseName.equals(getBaseName(definitionSet5)))
            return 5;
        if (baseName.equals(getBaseName(definitionSet6)))
            return 6;
        if (baseName.equals(getBaseName(definitionSet7)))
            return 7;

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
        if (definitionSet4 == null)
            return 4;
        if (definitionSet5 == null)
            return 5;
        if (definitionSet6 == null)
            return 6;
        if (definitionSet7 == null)
            return 7;
        
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
            case 4:
                return definitionSet4;
            case 5:
                return definitionSet5;
            case 6:
                return definitionSet6;
            case 7:
                return definitionSet7;
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
            case 4:
                definitionSet4 = definitionSet;
                break;
            case 5:
                definitionSet5 = definitionSet;
                break;
            case 6:
                definitionSet6 = definitionSet;
                break;
            case 7:
                definitionSet7 = definitionSet;
                break;
        }
    }
}
