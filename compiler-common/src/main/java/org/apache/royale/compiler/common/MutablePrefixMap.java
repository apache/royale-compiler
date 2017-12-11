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

package org.apache.royale.compiler.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is a mutable version of a {@link PrefixMap} that allows additions and
 * removals of namespaces
 */
public class MutablePrefixMap extends PrefixMap
{
    public MutablePrefixMap()
    {
        super();
    }

    public MutablePrefixMap(PrefixMap map)
    {
        PrefixMap clone = map.clone();
        namespaceToPrefixMap = new HashMap<String, Set<String>>(clone.namespaceToPrefixMap);
        prefixes = new HashSet<String>(clone.prefixes);
    }

    public MutablePrefixMap(String ns, String prefix)
    {
        add(ns, prefix);
    }

    /**
     * Creates an immutable version of this prefix map
     * 
     * @return an immutable prefix map
     */
    public PrefixMap toImmutable()
    {
        PrefixMap map = new PrefixMap();
        map.namespaceToPrefixMap = new HashMap<String, Set<String>>(namespaceToPrefixMap);
        map.prefixes = new HashSet<String>(prefixes);
        return map;
    }

    /**
     * Add a prefix and its uri to the map.
     * 
     * @param prefix A namespace prefix string, such as <code>"fx"</code>.
     * @param uri The corresponding namespace URI, such as <code>http://ns.adobe.com/mxml/2009</code>.
     */
    public void add(String prefix, String uri)
    {
        add(prefix, uri, false);
    }

    /**
     * Adds a prefix and uri to this namespace map.
     * 
     * @param prefix the prefix to add
     * @param uri the namespace to add for the given prefix
     * @param onlyIfUnique If the mapping is not unique, it will be added only
     * if this flag is set to false. A mapping is considering unique if a prefix
     * has not already been defined
     */
    public void add(String prefix, String uri, boolean onlyIfUnique)
    {
        Set<String> list = namespaceToPrefixMap.get(uri);
        if (list == null)
        {
            list = new LinkedHashSet<String>(1);
            namespaceToPrefixMap.put(uri, list);
        }
        if (onlyIfUnique)
        {
            if (!list.contains(prefix))
            {
                list.add(prefix);
                prefixes.add(prefix);
            }
        }
        else
        {
            list.add(prefix);
            prefixes.add(prefix);
        }
    }

    /**
     * Adds all information from one {@link PrefixMap} to another
     * 
     * @param map the {@link PrefixMap} where our data is coming from
     * @param onlyIfUnique If the mapping is not unique, it will be added only
     * if this flag is set to false. A mapping is considering unique if a prefix
     * has not already been defined
     */
    public void addAll(PrefixMap map, boolean onlyIfUnique)
    {
        Iterator<String> it = map.namespaceToPrefixMap.keySet().iterator();
        while (it.hasNext())
        {
            String ns = it.next();
            Iterator<String> sItr = map.namespaceToPrefixMap.get(ns).iterator();
            while (sItr.hasNext())
            {
                add(sItr.next(), ns, onlyIfUnique);
            }
        }
    }

    /**
     * Adds all information from one {@link PrefixMap} to another
     * 
     * @param map the {@link PrefixMap} where our data is coming from
     */
    public void addAll(PrefixMap map)
    {
        Iterator<String> it = map.namespaceToPrefixMap.keySet().iterator();
        while (it.hasNext())
        {
            String ns = it.next();
            Iterator<String> sItr = map.namespaceToPrefixMap.get(ns).iterator();
            while (sItr.hasNext())
            {
                add(sItr.next(), ns);
            }
        }
    }

    public void remove(String prefix)
    {
        Iterator<String> it = namespaceToPrefixMap.keySet().iterator();
        while (it.hasNext())
        {
            String next = it.next();
            Set<String> set = namespaceToPrefixMap.get(next);
            if (set != null)
            {
                set.remove(prefix);
            }
        }
        prefixes.remove(prefix);
    }
}
