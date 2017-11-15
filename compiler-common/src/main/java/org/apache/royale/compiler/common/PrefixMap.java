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
 * A PrefixMap contains a collection of prefix to namespace mappings found in
 * MXML documents. This object is <b>immutable</b>. For a mutable version, look
 * at {@link MutablePrefixMap}
 */
public class PrefixMap implements Cloneable
{
    public PrefixMap()
    {
        super();
    }

    public PrefixMap(PrefixMap map)
    {
        namespaceToPrefixMap = new HashMap<String, Set<String>>(map.namespaceToPrefixMap);
        prefixes = new HashSet<String>(map.prefixes);
    }

    protected HashMap<String, Set<String>> namespaceToPrefixMap = new HashMap<String, Set<String>>();

    protected HashSet<String> prefixes = new HashSet<String>();

    @Override
    public PrefixMap clone()
    {
        PrefixMap result = new PrefixMap();

        result.namespaceToPrefixMap = new HashMap<String, Set<String>>();

        // copy values here as there is an internal set involved here
        Iterator<String> it = namespaceToPrefixMap.keySet().iterator();
        while (it.hasNext())
        {
            String next = it.next();
            Set<String> set = namespaceToPrefixMap.get(next);
            if (set != null)
            {
                Set<String> prefixSet = new LinkedHashSet<String>();
                Iterator<String> setIt = set.iterator();
                while (setIt.hasNext())
                {
                    String prefix = setIt.next();
                    prefixSet.add(prefix);
                }
                result.namespaceToPrefixMap.put(next, prefixSet);
            }
        }

        result.prefixes = new HashSet<String>(prefixes);
        return result;
    }

    /**
     * Creates a mutable copy of this PrefixMap, based on its values
     * 
     * @return a mutable version of this prefix map
     */
    public MutablePrefixMap toMutable()
    {
        return new MutablePrefixMap(this.clone());
    }

    /**
     * Returns the namespace for the given prefix. The first prefix found in the
     * map will win
     * 
     * @param prefix the prefix to find out namespace for
     * @return a namespace URI or null
     */
    public String getNamespaceForPrefix(String prefix)
    {
        Iterator<String> it = namespaceToPrefixMap.keySet().iterator();
        while (it.hasNext())
        {
            String next = it.next();
            Set<String> set = namespaceToPrefixMap.get(next);
            if (set != null)
            {
                if (set.contains(prefix))
                {
                    return next;
                }
            }
        }
        return null;
    }

    /**
     * Returns true if the given prefix exists somewhere in this map
     * 
     * @param prefix A namespace prefix string such as <code>"fx"</code>.
     */
    public boolean containsPrefix(String prefix)
    {
        return prefixes.contains(prefix);
    }

    /**
     * Checks whether the map contains a reference to the given namespace.
     * 
     * @param namespace URI of a namespace.
     */
    public boolean containsNamespace(String namespace)
    {
        return namespaceToPrefixMap.containsKey(namespace);
    }

    /**
     * Returns all the prefixes known to this map.
     */
    public String[] getAllPrefixes()
    {
        return prefixes.toArray(new String[0]);
    }

    /**
     * Returns all the namespace URIs known to this map
     */
    public String[] getAllNamespaces()
    {
        return namespaceToPrefixMap.keySet().toArray(new String[0]);
    }

    /**
     * Returns the prefix that is used to reference the given namespace URI.
     * 
     * @param uri A namespace URI such as <code>"http://ns.adobe.com/mxml/2009"</code>.
     */
    public String[] getPrefixesForNamespace(String uri)
    {
        Set<String> set = namespaceToPrefixMap.get(uri);
        if (set != null)
            return set.toArray(new String[0]);
        return new String[0];
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof PrefixMap)
        {
            PrefixMap right = (PrefixMap)obj;
            if (right.namespaceToPrefixMap.size() != namespaceToPrefixMap.size())
            {
                return false;
            }
            if (right.prefixes.size() != prefixes.size())
            {
                return false;
            }

            Iterator<String> prefixItr = right.prefixes.iterator();
            while (prefixItr.hasNext())
            {
                if (!prefixes.contains(prefixItr.next()))
                {
                    return false;
                }
            }

            Iterator<String> nsItr = right.namespaceToPrefixMap.keySet().iterator();
            while (nsItr.hasNext())
            {
                String next = nsItr.next();
                Set<String> set = namespaceToPrefixMap.get(next);
                Set<String> rSet = right.namespaceToPrefixMap.get(next);
                if (set == null)
                {
                    return false;
                }
                Iterator<String> pItr = rSet.iterator();
                while (pItr.hasNext())
                {
                    if (!set.contains(pItr.next()))
                    {
                        return false;
                    }
                }
            }
            return true;
        }

        return super.equals(obj);
    }
}
