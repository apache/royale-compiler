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

package org.apache.royale.compiler.internal.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A singleton class that counts the number of various objects (such as tokens,
 * nodes, definitions, and scopes) created during a compilation.
 * <p>
 * You should not expect the counts to be exactly reproducible when doing the
 * same compilation multiple times. Some objects may get garbage collected
 * and recreated.
 */
public class Counter
{
    /**
     * Flag that enables counting of instances of TokenBase.
     */
    public static final boolean COUNT_TOKENS = false;
    
    /**
     * Flag that enables counting of instances of NodeBase.
     */
    public static final boolean COUNT_NODES = false;
    
    /**
     * Flag that enables counting of instances of DefinitionBase.
     */
    public static final boolean COUNT_DEFINITIONS = false;
    
    /**
     * Flag that enables counting of instances of ASScopeBase.
     */
    public static final boolean COUNT_SCOPES = false;
    
    /**
     * Gets the singleton instance of this class.
     */
    public static Counter getInstance()
    {
        return instance;
    }
    
    // Storage for the singleton instance.
    private static Counter instance = new Counter();
    
    // Private constructor.
    private Counter()
    {
        reset();
    }
    
    // Storage for various named counts.
    private Map<String, Integer> map;
    
    /**
     * Resets the counter so that there are no named counts.
     */
    public synchronized void reset()
    {
        map = new HashMap<String, Integer>();
    }
    
    /**
     * Increments the count with the specified name.
     */
    public synchronized boolean incrementCount(String name)
    {
        Integer n = map.get(name);
        if (n == null)
            n = 0;
        n++;
        map.put(name, n);
        return true;
    }
    
    /**
     * Decrements the count with the specified name.
     * A count is not allowed to go negative.
     */
    public synchronized boolean decrementCount(String name)
    {
        Integer n = map.get(name);
        if (n == null)
            n = 0;
        n--;
        if (n < 0)
            n = 0;
        map.put(name, n);
        return true;
    }
    
    /**
     * Gets the count with the specified name.
     */
    public int getCount(String name)
    {
        Integer n = map.get(name);
        return n != null ? n : 0;
    }
    
    /**
     * Dumps all counts in alphabetical order to System.out.
     */
    public void dumpCounts()
    {
        if (map.isEmpty())
            return;
        
        String[] keys = map.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        
        for (String key  : keys)
        {
            System.out.println(getCount(key) + "\t" + key);
        }
    }
}
