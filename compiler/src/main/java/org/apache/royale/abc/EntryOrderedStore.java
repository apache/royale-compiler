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

package org.apache.royale.abc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A custom container with the following properties: 
 * <li>Ordered by entry, so iteration is in the order objects are added.
 * <li>Optimized for reverse lookup of entry to index.
 */
public class EntryOrderedStore<T> implements Iterable<T>
{
    /**
     * This list holds the data in insertion order
     */
    private final List<T> data = new ArrayList<T>();

    /**
     * This map enables the fast lookup: maps data records to index
     */
    private final Map<T, Integer> index = new HashMap<T, Integer>();

    @Override
    public Iterator<T> iterator()
    {
        return data.iterator();
    }

    /**
     * adds things
     */
    public void add(T record)
    {
        int nextIndex = data.size();
        data.add(record);
        index.put(record, nextIndex);
    }

    /**
     * Looks up a record, and returns the index
     */
    public int getId(T record)
    {
        // Historic convention. Don't know if this is ever done any more 
        if (null == record)
            return 0;

        Integer id = index.get(record);
        if (id != null)
            return id;

        throw new IllegalArgumentException("Unable to find record index for " + record);
    }

    public int size()
    {
        assert data.size() == index.size();
        return data.size();
    }
}
