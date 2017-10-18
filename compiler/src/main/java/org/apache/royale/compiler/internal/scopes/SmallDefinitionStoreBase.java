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

import java.util.Arrays;
import java.util.List;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.scopes.IDefinitionSet;

/**
 * An abstract base class for implementations of {@link IDefinitionStore}
 * that use a small number of fields, rather than a map, to store definition sets.
 */
public abstract class SmallDefinitionStoreBase implements IDefinitionStore
{
    protected static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    /**
     * Gets the base name of the definitions stored in the specified definition set.
     * 
     * @param definitionSet An {@link IDefinitionSet} or <code>null</code>.
     * @return The base name, or <code>null</code> if the definition set
     * was <code>null</code> or empty.
     */
    protected static String getBaseName(IDefinitionSet definitionSet)
    {
        if (definitionSet == null || definitionSet.getSize() == 0)
            return null;
        
        IDefinition firstDefinition = definitionSet.getDefinition(0);
        return firstDefinition.getBaseName();
    }
        
    /**
     * Adds a new definition to a definition set, possibly creating a new
     * definition set in the process (if the old definition set was
     * <code>null</code> or couldn't hold the new definition).
     * <p>
     * This method handles the fact that we use different optimized implementations
     * of {@link IDefinitionSet} for different set sizes.
     * 
     * @param oldDefinitionSet An {@link IDefinitionSet} containing definitions.
     * This may be null if we are adding the first definition with a particular base name.
     * @param definition The {@link IDefinition} to add to the set.
     * @return Either the definition set that was passed in or a new definition set.
     * In either case, the definition has been added.
     */
    public static IDefinitionSet addDefinitionToSet(IDefinitionSet oldDefinitionSet,
                                                    IDefinition definition)
    {
        assert definition != null: "Definition cannot be null";
        assert getBaseName(oldDefinitionSet) == null || getBaseName(oldDefinitionSet).equals(definition.getBaseName()) : "Base name must match";
        
        // We will probably have to create a new set,
        // but we might be able to re-use the old one.
        IDefinitionSet newDefinitionSet = oldDefinitionSet;

        if (oldDefinitionSet == null)
        {
            // There was no old set. Use the new definition itself as a new set-of-size-1.
            // This is possible because DefinitionBase implements IDefinitionSet.
            newDefinitionSet = (DefinitionBase)definition;
        }
        else if (oldDefinitionSet.getMaxSize() == 1)
        {
            // There is one old definition acting as a set-of-size-1.
            // Replace it with a SmallDefinitionSet of size 2
            // containing the old definition and the new definition.
            newDefinitionSet = new SmallDefinitionSet((IDefinition)oldDefinitionSet, definition);
        }
        else if (oldDefinitionSet.getMaxSize() == 2)
        {
            // There is a SmallDefinition set with 2 (or perhaps 1 or 0) definitions.
            // If the SmallDefinitionSet is full, replace it with an LargeDefinitionSet
            // which can hold any number of definitions.
            if (oldDefinitionSet.getSize() == oldDefinitionSet.getMaxSize())
                newDefinitionSet = new LargeDefinitionSet(oldDefinitionSet);
            ((IMutableDefinitionSet)newDefinitionSet).addDefinition(definition);
        }
        else
        {
            // We already have an LargeDefinitionSet that can hold any number of definitions.
            // Just add the new one.
            ((LargeDefinitionSet)newDefinitionSet).addDefinition(definition);
        }
 
        // Return the possibly-new definition set with the new definition.
        return newDefinitionSet;
    }
    
    /**
     * Removes a definition from a definition set.
     * <p>
     * This method handles the fact that we use different optimized implementations
     * of {@link IDefinitionSet} for different set sizes.
     * 
     * @param definitionSet An {@link IDefinitionSet} containing definitions.
     * @param definition The {@link IDefinition} to be removed.
     * @return <code>true</code> if the definition set is now empty
     * and must be removed from the store.
     */
    public static boolean removeDefinitionFromSet(IDefinitionSet definitionSet,
                                                  IDefinition definition)
    {
        assert definitionSet != null : "Definition set cannot be null";
        assert definition != null: "Definition cannot be null";
        assert definition.getBaseName().equals(getBaseName(definitionSet)) : "Base name must match";
        
        // If the definition set is a single definition, we can't remove the
        // definition from itself, but by returning true we indicate that it
        // must removed from the store.
        if (definitionSet.getMaxSize() == 1)
            return true;
            
        // If the definition set is a SmallDefinitionSet or an LargeDefinitionSet,
        // remove the definition from the set, and, if the set is then empty,
        // return true to indicate that the empty set should be removed from
        //the store.
        else
        {
            ((IMutableDefinitionSet)definitionSet).removeDefinition(definition);
            if (definitionSet.isEmpty())
                return true;
        }
        
        return false;
    }
    
    /**
     * If the specified definition set is non-null and contains definitions,
     * add the base name of those definitions to the specified list.
     * <p>
     * This is a helper function used by subclasses to implement {@link #getAllNames}().
     * Subclasses call this method for each field that can store a definition set.
     * 
     * @param list A list of base names being built up.
     * @param definitionSet An {@link IDefinitionSet}, which may be null or empty.
     */
    protected static void addBaseNameToList(List<String> list, IDefinitionSet definitionSet)
    {
        String baseName = getBaseName(definitionSet);
        if (baseName != null)
            list.add(baseName);
    }
    
    /**
     * If the specified definition set is non-null, add it to the specified list.
     * <p>
     * This is a helper function used by subclasses to implement {@link #getAllDefinitionSets}().
     * Subclasses call this method for each field that can store a definition set.
     * The field may be null or may contain an empty definition set.
     * 
     * @param list A list of definition sets being built up.
     * @param definitionSet An {@link IDefinitionSet}, which may be null or empty.
     */
    protected static void addDefinitionSetToList(List<IDefinitionSet> list, IDefinitionSet definitionSet)
    {
        if (definitionSet != null)
            list.add(definitionSet);
    }
    
    /**
     * If the specified definition set is non-null and contains definitions,
     * add those definitions to the specified list.
     * <p>
     * This is a helper function used by subclasses to implement {@link #getAllDefinitions}().
     * Subclasses call this method for each field that can store a definition set.
     * 
     * @param list A list of definitions being built up.
     * @param definitionSet An {@link IDefinitionSet}, which may be null or empty.
     */
    protected static void addDefinitionsToList(List<IDefinition> list, IDefinitionSet definitionSet)
    {
        if (definitionSet == null)
            return;
        
        int n = definitionSet.getSize();
        for (int i = 0; i < n; i++)
        {
            IDefinition definition = definitionSet.getDefinition(i);
            list.add(definition);
        }
    }
    
    /**
     * Constructor.
     */
    public SmallDefinitionStoreBase()
    {
    }
    
    @Override
    public boolean add(IDefinition definition)
    {
        assert definition != null : "A null definition cannot be added to a store";

        // Look for a field storing a definition set with the same base name.
        String baseName = definition.getBaseName();
        int i = findIndexForBaseName(baseName);
        
        // If not found, find a field where we can store a new definition set.
        if (i == -1)
            i = findAvailableIndex();
        
        // If there are no more fields available,
        // return false to indicate that this store is full.
        if (i == -1)
            return false;

        // Get the definition set from the field we've found. It might be null.
        IDefinitionSet oldDefinitionSet = getDefinitionSet(i);
        
        // Add the new definition to the old set. This may create a new set.
        IDefinitionSet newDefinitionSet = addDefinitionToSet(oldDefinitionSet, definition);
        
        // Store the new set into the field.
        if (newDefinitionSet != oldDefinitionSet)
            setDefinitionSet(i, newDefinitionSet);

        return true;
    }

    @Override
    public boolean remove(IDefinition definition)
    {
        assert definition != null : "A null definition cannot be removed from a store";
        
        // Look for a field storing a definition set with the same base name.
        String baseName = definition.getBaseName();
        int i = findIndexForBaseName(baseName);

        // If not found, return false to indicate that the definition
        // doesn't exist in this store.
        if (i == -1)
            return false;
        
        // Get the definition set from the field we've found. It will not be null.
        IDefinitionSet oldDefinitionSet = getDefinitionSet(i);
        
        // Remove the definition from the set,
        // and perhaps remove the set from this store.
        if (removeDefinitionFromSet(oldDefinitionSet, definition))
            setDefinitionSet(i, null);

        return true;
    }

    @Override
    public abstract IDefinitionSet getDefinitionSetByName(String baseName);

    /**
     * Gets the number of fields for storing definition sets.
     * 
     * @return The number of fields.
     */
    public abstract int getCapacity();

    /**
     * Gets the definition set stored in the specified field.
     * 
     * @param i The index of the field.
     * @return The {@link IDefinitionSet} store there.
     */
    protected abstract IDefinitionSet getDefinitionSet(int i);

    /**
     * Sets the specified definition set into the specified field.
     * 
     * @param i The index of the field.
     * @param definitionSet The {@link IDefinitionSet} to store there.
     */
    protected abstract void setDefinitionSet(int i, IDefinitionSet definitionSet);
    
    /**
     * Returns the index of the field storing a definition set with the
     * specified base name, or -1 if there is no such field.
     * 
     * @param baseName The base name to look for.
     * @return The index of the field whose definition set has this base name.
     */
    protected abstract int findIndexForBaseName(String baseName);
    
    /**
     * Returns the index of the first null field where a new definition set
     * can be stored.
     * 
     * @return The index of a null field, or - is there is no such field.
     */
    protected abstract int findAvailableIndex();
    
    
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
