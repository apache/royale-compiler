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

package org.apache.royale.compiler.internal.definitions.references;

import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import com.google.common.collect.MapMaker;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A cache to manage instances of {@link IReference}.
 * <p>
 * Clients should use {@link ReferenceFactory} to get {@link IReference}s; this
 * class is just an implementation detail.
 */
public class ReferenceCache
{
    public ReferenceCache()
    {
        threadLocalLexicalCache = new ThreadLocal<Map<String, WeakReference<LexicalReference>>>()
        {
            @Override
            protected Map<String, WeakReference<LexicalReference>> initialValue()
            {
                // Use a WeakHashMap with WeakRefs as values - the Strings and LexicalRefs
                // will be kept alive or not by the shared cache
                return new WeakHashMap<String, WeakReference<LexicalReference>>();
            }
        };
    }

    /**
     * Map of name to LexicalReference.
     */
    private final ConcurrentMap<String, LexicalReference> lexicalCache =
            new MapMaker().weakValues().makeMap();

    /**
     * Thread local cache for lexical references - improves performance, as we
     * can look here without worrying about locking or contention, and only look
     * at the shared cache if we have a miss here.
     */
    private final ThreadLocal<Map<String, WeakReference<LexicalReference>>> threadLocalLexicalCache;

    /**
     * Gets a {@link LexicalReference} representing <code>name</code>. If one
     * already exists, it is returned. If one does not exist a new one is
     * created, cached, and returned.
     * 
     * @param name the name you want a lexical ref for
     * @return the LexicalReference representing name
     */
    public LexicalReference getLexicalReference(String name)
    {
        Map<String, WeakReference<LexicalReference>> cache = threadLocalLexicalCache.get();
        WeakReference<LexicalReference> weakRef = cache.get(name);
        LexicalReference ref = weakRef != null ? weakRef.get() : null;
        if (ref != null)
            return ref;

        LexicalReference newref = new LexicalReference(name);
        ref = lexicalCache.putIfAbsent(name, newref);
        if (ref == null)
            ref = newref;

        cache.put(name, new WeakReference<LexicalReference>(ref));
        return ref;
    }
}
