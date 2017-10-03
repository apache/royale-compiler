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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.scopes.IDefinitionSet;

/**
 * An implementation of {@link IDefinitionStore} for large numbers of definitions.
 * <p>
 * This implementation extends HashMap, instead of having a HashMap, in order to save memory.
 * The keys of the map are the base names of the definitions.
 * The values of the map are definition sets containing definitions with the same base name.
 */
@SuppressWarnings("serial")
public final class LargeDefinitionStore extends HashMap<String, IDefinitionSet> implements IDefinitionStore
{
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    /**
     * Constructor.
     */
    public LargeDefinitionStore()
    {
        // Default HashMap has initial capacity of 16,
        // which works nicely as the next step up from
        // SmallDefinitionStore1/2/4/8.
        super();
    }
    
    /**
     * Copy constructor.
     */
    public LargeDefinitionStore(SmallDefinitionStore8 store)
    {
        int n = store.getCapacity();
        for (int i = 0; i < n; i++)
        {
            IDefinitionSet definitionSet = store.getDefinitionSet(i);
            if (definitionSet != null)
            {
                int m = definitionSet.getSize();
                for (int j = 0; j < m; j++)
                {
                    IDefinition definition = definitionSet.getDefinition(j);
                    add(definition);
                }
            }
        }
    }
    
    @Override
    public int getCapacity()
    {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public IDefinitionStore createLargerStore()
    {
        // This store can already store an unlimited number of definition sets.
        return this;
    }
    
    @Override
    public boolean add(IDefinition definition)
    {
        // Look in the map for a definition set with the same base name.
        String baseName = definition.getBaseName();
        IDefinitionSet oldDefinitionSet = get(baseName);
        
        // Add the new definition to the old set. This may create a new set.
        IDefinitionSet newDefinitionSet =
            SmallDefinitionStoreBase.addDefinitionToSet(oldDefinitionSet, definition);
        
        // If we got a new set, put it into the map.
        if (newDefinitionSet != oldDefinitionSet)
            put(baseName, newDefinitionSet);
        
        return true;
    }

    @Override
    public boolean remove(IDefinition definition)
    {
        // Look in the map for a definition set with the same base name.
        String baseName = definition.getBaseName();
        IDefinitionSet oldDefinitionSet = get(baseName);
        
        // If not found, return false to indicate that the definition wasn't found
        // in this store.
        if (oldDefinitionSet == null)
            return false;
        
        // Remove the definition from the set,
        // and perhaps remove the set from the map.
        if (SmallDefinitionStoreBase.removeDefinitionFromSet(oldDefinitionSet, definition))
            remove(baseName);
        
        return true;
    }

    @Override
    public IDefinitionSet getDefinitionSetByName(String baseName)
    {
        // Just look the definition set up in the map.
        return get(baseName);
    }

    @Override
    public void putDefinitionSetByName(String baseName, IDefinitionSet set)
    {
        // Just put the definition set into the map.
        put(baseName, set);
    }

    @Override
    public Collection<String> getAllNames()
    {
        // The base names are the keys of the map.
        return keySet();
    }

    @Override
    public Collection<IDefinitionSet> getAllDefinitionSets()
    {
        // The definition sets are the values of the map.
        return values();
    }
    
    @Override
    public Collection<IDefinition> getAllDefinitions()
    {
        List<IDefinition> list = new ArrayList<IDefinition>();
        
        // Loop over the definition sets, which are the values of the map.
        for (IDefinitionSet definitionSet : values())
        {
            // Add all the definitions in the set to the list.
            SmallDefinitionStoreBase.addDefinitionsToList(list, definitionSet);
        }
        
        return list;
    }
    
    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        // Display base names in alphabetical order.
        String[] names = getAllNames().toArray(EMPTY_STRING_ARRAY);
        Arrays.sort(names);
        
        for (String name : names)
        {
            sb.append(name);
            sb.append('\n');
            
            IDefinitionSet set = getDefinitionSetByName(name);
            int n = set.getSize();
            for (int i = 0; i < n; i++)
            {
                IDefinition d = set.getDefinition(i);
                sb.append("  ");
                sb.append(d.toString());
                sb.append('\n');
            }
        }
        
        return sb.toString();
    }
}
