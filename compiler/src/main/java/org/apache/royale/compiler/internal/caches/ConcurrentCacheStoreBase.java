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

package org.apache.royale.compiler.internal.caches;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Joiner;

/**
 * A key-value pair cache store that supports concurrent access.
 */
public abstract class ConcurrentCacheStoreBase<T>
{
    /**
     * Initialize the cache store.
     */
    protected ConcurrentCacheStoreBase()
    {
        this.cache = new ConcurrentHashMap<CacheStoreKeyBase, SoftReference<T>>();
    }

    private final ConcurrentMap<CacheStoreKeyBase, SoftReference<T>> cache;

    /**
     * Get a value from the cache store. If the cache doesn't have a valid
     * entry, it will obtain the value, add to the cache and return the value.
     * <p>
     * The implementation is thread-safe. The API looks like a "read" access,
     * but when the cache doesn't have a hit, it will write an entry to the
     * cache table. The first attempt queries the cache, so the read-lock allows
     * parallelized read if no other thread is writing the cache. If there's no
     * hit, the second attempt is to create a valid entry, store it in the cache
     * and return it. This requires upgrading the read-lock to a write-lock.
     * 
     * @param key cache key
     * @return cached value
     */
    public final T get(CacheStoreKeyBase key)
    {
        T result = null;

        if (cache.containsKey(key))
        {
            result = cache.get(key).get();
        }

        // create entry and store in cache
        if (result == null)
        {
            result = createEntryValue(key);
            if (result == null)
                throw new NullPointerException("Null value not allowed in cache store.");
            cache.put(key, new SoftReference<T>(result));

        }

        assert result != null : "Expected non-null value from cache.";
        return result;

    }

    /**
     * Remove a value from the cache
     * 
     * @param key cache key
     * @return value for removed entry if an entry was removed.
     */
    public final T remove(CacheStoreKeyBase key)
    {
        SoftReference<T> entryRef = cache.remove(key);
        if (entryRef == null)
            return null;
        return entryRef.get();
    }

    /**
     * Get size of the cache table.
     * 
     * @return size
     */
    public int size()
    {
        return cache.size();
    }

    /**
     * Concrete class must implement this method to create a cache value object.
     * Do NOT add the value to the cache in this method. {@link #get(CacheStoreKeyBase)} is
     * responsible for reading and writing the cache table.
     * 
     * @param key cache key
     * @return object to be cached.
     */
    protected abstract T createEntryValue(CacheStoreKeyBase key);

    @Override
    public String toString()
    {
        final StringBuilder result = new StringBuilder();
        result.append(getClass().getSimpleName());
        result.append(" (").append(cache.size()).append(") {");
        result.append(Joiner.on(", ").join(cache.keySet()));
        result.append("} ");
        return result.toString();
    }
}
