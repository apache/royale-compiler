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

package org.apache.royale.compiler.internal.units;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * Maps {@link File}'s to collections of {@link ICompilationUnit}'s whose root
 * files are those files.
 */
public final class StringToCompilationUnitMap
{
    // would like to use a HashSet to store weak references, but that doesn't
    // work, so use a WeakHashMap where the value is always PRESENT.
    private static final Object PRESENT = new Object();

    private static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

    private final Lock readLock;
    private final Lock writeLock;

    private Map<String, Object> map;
    
    /**
     * Constructor.
     */
    public StringToCompilationUnitMap()
    {
        this(EMPTY_MAP);
    }

    private StringToCompilationUnitMap(Map<String, Object> map)
    {
        this.map = map;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }
    
    /**
     * Gets a {@link Collection} of invisible {@link ICompilationUnit}'s that refer to
     * the specified file as their root source file.
     * @param sortKey String to search the map for.
     * @return Array of {@link ICompilationUnit}'s that refer to
     * the specified file as their root source file.
     */
    public Collection<WeakReference<ICompilationUnit>> getInvisible(String sortKey)
    {
        readLock.lock();
        try
        {
            return valueToCollection(map.get(sortKey), null, Visibility.INVISIBLE_ONLY);
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Gets a {@link Collection} of visible and invisible {@link ICompilationUnit}'s that refer to
     * the specified file as their root source file.
     * @param sortKey String to search the map for.
     * @return Array of {@link ICompilationUnit}'s that refer to
     * the specified file as their root source file.
     */
    public Collection<WeakReference<ICompilationUnit>> getVisibleAndInvisible(String sortKey)
    {
        readLock.lock();
        try
        {
            return valueToCollection(map.get(sortKey), null, Visibility.ALL);
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Gets a {@link Collection} of {@link ICompilationUnit}'s that refer to
     * the specified file as their root source file.
     * @param sortKey String to search the map for.
     * @return Array of {@link ICompilationUnit}'s that refer to
     * the specified file as their root source file.
     */
    public Collection<WeakReference<ICompilationUnit>> get(String sortKey)
    {
        readLock.lock();
        try
        {
            return valueToCollection(map.get(sortKey), null, Visibility.VISIBLE_ONLY);
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Gets the a {@link Collection} of {@link ICompilationUnit}'s that refer to
     * the specified file as their root source file for a specific project
     * @param sortKey String to search the map for.
     * @param project Project to search the map for.
     * @return Array of {@link ICompilationUnit}'s that refer to
     * the specified file as their root source file.
     */
    public Collection<WeakReference<ICompilationUnit>> get(String sortKey, ICompilerProject project)
    {
        readLock.lock();
        try
        {
            return valueToCollection(map.get(sortKey), project, Visibility.VISIBLE_ONLY);
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Gets the a {@link Collection} of invisible {@link ICompilationUnit}'s that refer to
     * the specified file as their root source file for a specific project
     * @param sortKey String to search the map for.
     * @param project Project to search the map for.
     * @return Array of {@link ICompilationUnit}'s that refer to
     * the specified file as their root source file.
     */
    public Collection<WeakReference<ICompilationUnit>> getInvisible(String sortKey, ICompilerProject project)
    {
        readLock.lock();
        try
        {
            return valueToCollection(map.get(sortKey), project, Visibility.INVISIBLE_ONLY);
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Adds entries for multiple file names all referenced from the same compilation unit.
     * @param files An array of file paths.
     * @param cu A compilation unit.
     */
    public void add(String[] files, ICompilationUnit cu)
    {
        writeLock.lock();
        try
        {
            for (String file : files)
            {
                add(file, cu);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Adds the specified {@link ICompilationUnit} whose root source file
     * is the specified File to the map.
     * @param sortKey String to add to the map.
     * @param cu {@link ICompilationUnit} to add to the map.
     */
    @SuppressWarnings("unchecked")
    public void add(String sortKey, ICompilationUnit cu)
    {
        writeLock.lock();
        try
        {
            if (map == EMPTY_MAP)
                map = new HashMap<String, Object>();
    
            Object existing = map.get(sortKey);
            if (existing == null)
            {
                map.put(sortKey, new WeakReference<ICompilationUnit>(cu));
            }
            else if (existing instanceof WeakHashMap<?,?>)
            {
                WeakHashMap<ICompilationUnit,Object> collection = (WeakHashMap<ICompilationUnit,Object>)existing;
                collection.put(cu, PRESENT);
            }
            else
            {
                assert existing instanceof WeakReference<?>;
                WeakHashMap<ICompilationUnit,Object> collection = new WeakHashMap<ICompilationUnit,Object>(2);
                collection.put(((WeakReference<ICompilationUnit>)existing).get(), PRESENT);
                collection.put(cu, PRESENT);
                map.put(sortKey, collection);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Removes the specified File from the map.
     * @param sortKey String to remove from the map.
     */
    void remove(String sortKey)
    {
        writeLock.lock();
        try
        {
            map.remove(sortKey);
            if (map.isEmpty())
                map = EMPTY_MAP;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Removes the specified File within a project from the map.
     * @param sortKey String to remove from the map.
     * @param project Project to remove from the map.
     * @return Array of {@link ICompilationUnit}'s that refer to
     * the specified file as their root source file.
     */
    @SuppressWarnings("unchecked")
    Collection<ICompilationUnit> remove(String sortKey, ICompilerProject project)
    {
        assert project != null;
        writeLock.lock();
        try
        {
            Object mapValue = map.remove(sortKey);
            if (mapValue == null)
            {
                return Collections.emptyList();
            }
            else if (mapValue instanceof WeakHashMap<?,?>)
            {
                WeakHashMap<ICompilationUnit, Object> collection = (WeakHashMap<ICompilationUnit, Object>)mapValue;
                List<ICompilationUnit> result = new ArrayList<ICompilationUnit>(collection.size());
                Iterator<Map.Entry<ICompilationUnit, Object>> iter = collection.entrySet().iterator();
                while (iter.hasNext())
                {
                    ICompilationUnit compilationUnit = iter.next().getKey();
                    // We are dealing with a WeakHashMap, here so it
                    // is possible that the CompilationUnit has been gc'd.
                    if (compilationUnit != null)
                    {
                        // The CompilationUnit might not be in a project
                        // any more.  We should only return CompilationUnit's
                        // that are in a project.
                        ICompilerProject projFromCU = compilationUnit.getProject();
                        if ((projFromCU != null) && (projFromCU == project))
                        {
                            result.add(compilationUnit);
                            iter.remove();
                        }   
                    }
                }
                
                if (!collection.isEmpty())
                    map.put(sortKey, collection);
                
                return Collections.unmodifiableList(result);
            }
            else
            {
                ICompilationUnit compilationUnit = ((WeakReference<ICompilationUnit>)mapValue).get();
                if (compilationUnit == null)
                    return Collections.emptyList();

                // if projects don't match, add the map value back in, otherwise return the
                // removed CU
                if ((compilationUnit != null) && (compilationUnit.getProject() == project))
                    return Collections.singletonList(compilationUnit);
                else
                    map.put(sortKey, mapValue);

                return Collections.emptyList();
            }
        }
        finally
        {
            if (map.isEmpty())
                map = EMPTY_MAP;
            writeLock.unlock();
        }
    }

    /**
     * Removes the specified CompilationUnit from the path to compilation unit map, removing
     * the path only when there are no compilation units remaining
     * @param sortKey String to remove from the map.
     * @param cuToRemove CU to remove from the map.
     * @return Array of {@link ICompilationUnit}'s that refer to
     * the specified file as their root source file.  An empty collection
     * if no more CompilationUnits refer to this path
     */
    @SuppressWarnings("unchecked")
    public Collection<ICompilationUnit> remove(String sortKey, ICompilationUnit cuToRemove)
    {
        assert cuToRemove != null;
        writeLock.lock();
        try
        {
            Object mapValue = map.remove(sortKey);
            if (mapValue == null)
            {
                return Collections.emptyList();
            }
            else if (mapValue instanceof WeakHashMap<?,?>)
            {
                WeakHashMap<ICompilationUnit, Object> collection = (WeakHashMap<ICompilationUnit, Object>)mapValue;
                List<ICompilationUnit> result = new ArrayList<ICompilationUnit>(collection.size());
                Iterator<Map.Entry<ICompilationUnit, Object>> iter = collection.entrySet().iterator();
                while (iter.hasNext())
                {
                    ICompilationUnit compilationUnit = iter.next().getKey();
                    // We are dealing with a WeakHashMap, here so it
                    // is possible that the CompilationUnit has been gc'd.
                    if ((compilationUnit != null) && (compilationUnit == cuToRemove))
                    {
                        result.add(compilationUnit);
                        iter.remove();
                    }
                }

                if (!collection.isEmpty())
                    map.put(sortKey, collection);

                return Collections.unmodifiableList(result);
            }
            else
            {
                ICompilationUnit compilationUnit = ((WeakReference<ICompilationUnit>)mapValue).get();
                if (compilationUnit == null)
                    return Collections.emptyList();

                // if compilation units don't match, add the map value back in, otherwise
                // return the removed CU
                if ((compilationUnit != null) && (compilationUnit == cuToRemove))
                    return Collections.singletonList(compilationUnit);
                else
                    map.put(sortKey, mapValue);

                return Collections.emptyList();
            }
        }
        finally
        {
            if (map.isEmpty())
                map = EMPTY_MAP;
            writeLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private static Collection<WeakReference<ICompilationUnit>> valueToCollection(Object value, ICompilerProject project, Visibility visibility)
    {
        if (value instanceof WeakHashMap<?,?>)
        {
            WeakHashMap<ICompilationUnit, Object> collection = (WeakHashMap<ICompilationUnit, Object>)value;
            List<WeakReference<ICompilationUnit>> result = new ArrayList<WeakReference<ICompilationUnit>>(collection.size());
            for (ICompilationUnit compilationUnit : collection.keySet())
            {
                // We are dealing with a WeakHashMap, here so it
                // is possible that the CompilationUnit has been gc'd.
                if (compilationUnit != null)
                {
                    // The CompilationUnit might not be in a project
                    // any more.  We should only return CompilationUnit's
                    // that are in a project.
                    ICompilerProject projFromCU = compilationUnit.getProject();
                    if ((projFromCU != null) && ((project == null) || (projFromCU == project)) && visibility.match(compilationUnit))
                        result.add(new WeakReference<ICompilationUnit>(compilationUnit));
                }
            }

            return Collections.unmodifiableList(result);
        }
        else if (value != null)
        {
            assert value instanceof WeakReference<?>;
            WeakReference<ICompilationUnit> cuRef = (WeakReference<ICompilationUnit>)value;
            ICompilationUnit compilationUnit = cuRef.get();
            // either not limiting search to projects, or projects match so add the CU
            if ((compilationUnit != null) && ((project == null) || (compilationUnit.getProject() == project)) && visibility.match(compilationUnit))
                return Collections.singletonList(cuRef);
            else
                return Collections.emptyList();
        }
        else
        {
            return Collections.emptyList();
        }
    }
    
    private static enum Visibility
    {
        INVISIBLE_ONLY,
        VISIBLE_ONLY,
        ALL;
        
        private boolean match(ICompilationUnit cu)
        {
            switch (this)
            {
                case INVISIBLE_ONLY:
                    return cu.isInvisible();
                case VISIBLE_ONLY:
                    return !cu.isInvisible();
                case ALL:
                    return true;
                default:
                    assert false : "Unhandled visibility!";
            }
            return false;
        }
    }

}
