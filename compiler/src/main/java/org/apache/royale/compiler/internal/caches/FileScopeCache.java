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

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Collection;

import org.apache.royale.compiler.caches.IFileScopeCache;
import org.apache.royale.compiler.internal.abc.ABCScopeBuilder;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.SWCFileScopeProvider;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCScript;
import org.apache.royale.swc.SWCManager;
import org.apache.royale.swf.ITagContainer;
import org.apache.royale.swf.tags.DoABCTag;

/**
 * Each {@code <script>} tag maps to a {@code DoABC} tag in the library
 * SWF. The tag contains an ABC script which has a top-level file scope with one
 * or many public definitions inside. The {@code FileScopeCache} is a table of
 * key-value pairs. The key is a string in the form: {@code swc/library/script}.
 * The value is a {@link SoftReference} to a collection of {@link ASFileScope}s.
 */
public class FileScopeCache extends ConcurrentCacheStoreBase<Collection<IASScope>> implements IFileScopeCache
{
    
    /**
     * Key object for {@code FileScopeCache}. It has 3 properties:
     * <ol>
     * <li>absolute path to a SWC file</li>
     * <li>library path - relative to the root of the SWC file archive</li>
     * <li>script name - script in the library</li>
     * </ol>
     */
    protected static class FileScopeCacheKey extends SWFCache.SWFInSWCCacheKey
    {
        protected String scriptName; // non-null

        @Override
        public String generateKey()
        {
            return String.format(
                    "%s:%s:%s",
                    swc.getSWCFile().getAbsolutePath(),
                    swfPath,
                    scriptName).intern();
        }
    }

    /**
     * Factory method for creating a key object for {@code SWCCache}.
     * 
     * @param swc SWC file (optional)
     * @param librarySWFPath path to a library SWF file
     * @return key
     */
    public static FileScopeCacheKey createKey(ISWC swc, String librarySWFPath, ISWCScript script)
    {
        final FileScopeCacheKey key = new FileScopeCacheKey();
        key.swc = swc;
        key.swfPath = librarySWFPath;
        key.scriptName = script.getName();
        return key;
    }

    public FileScopeCache(SWCManager swcManager)
    {
        super();
        this.swcManager = swcManager;
    }
    
    private final SWCManager swcManager;

    /**
     * Get the {@link ASFileScope}s associated with the key.
     * 
     * @param key a key is a string of pattern: <code>{path/to/file.swc:
     */
    @Override
    protected Collection<IASScope> createEntryValue(CacheStoreKeyBase key)
    {
        if (!(key instanceof FileScopeCacheKey))
            throw new IllegalArgumentException("expect FileScopeCacheKey but got " + key.getClass().getSimpleName());

        final FileScopeCacheKey fileScopeCacheKey = (FileScopeCacheKey)key;
        final CacheStoreKeyBase swfCacheKey = SWFCache.createKey(fileScopeCacheKey.swc, fileScopeCacheKey.swfPath);
        final ITagContainer tags = ((SWFCache)swcManager.getSWFCache()).get(swfCacheKey);

        final DoABCTag abcTag = SWFCache.findDoABCTagByName(tags, fileScopeCacheKey.scriptName);
        if (abcTag != null)
        {
            try
            {
                final ABCScopeBuilder abcScopeBuilder = new ABCScopeBuilder(
                        swcManager.getWorkspace(), 
                        abcTag.getABCData(), 
                        fileScopeCacheKey.swc.getSWCFile().getCanonicalPath(),
                        SWCFileScopeProvider.getInstance());
                return abcScopeBuilder.build();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            throw new IllegalStateException("can't create entry in FileScopeCache for key: " + key.toString());
        }
    }

}
