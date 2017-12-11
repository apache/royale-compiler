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

package org.apache.royale.swc;

import java.io.File;
import java.io.IOException;

import org.apache.royale.compiler.caches.IAssetTagCache;
import org.apache.royale.compiler.caches.ICSSDocumentCache;
import org.apache.royale.compiler.caches.IFileScopeCache;
import org.apache.royale.compiler.caches.ISWFCache;
import org.apache.royale.compiler.internal.caches.AssetTagCache;
import org.apache.royale.compiler.internal.caches.CSSDocumentCache;
import org.apache.royale.compiler.internal.caches.CacheStoreKeyBase;
import org.apache.royale.compiler.internal.caches.ConcurrentCacheStoreBase;
import org.apache.royale.compiler.internal.caches.FileScopeCache;
import org.apache.royale.compiler.internal.caches.SWFCache;
import org.apache.royale.compiler.workspaces.IWorkspace;
import org.apache.royale.swc.io.SWCReader;

/**
 * This is a cached implementation for {@link ISWCManager} based on
 * {@link ConcurrentCacheStoreBase}.
 */
public class SWCManager extends ConcurrentCacheStoreBase<ISWC> implements ISWCManager
{
    /**
     * Key class for SWC cache.
     */
    public static class SWCCacheKey extends CacheStoreKeyBase
    {
        private final File file;

        public SWCCacheKey(final File file)
        {
            assert file != null : "SWC file can't be null.";
            this.file = file;
        }

        @Override
        public String generateKey()
        {
            try
            {
                return file.getCanonicalPath();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

    }


    /**
     * Initialize the {@code SWCManager}.
     */
    public SWCManager(IWorkspace workspace)
    {
        swfCache = new SWFCache(this);
        fileScopeCache = new FileScopeCache(this);
        assetTagCache = new AssetTagCache(this);
        cssDocumentCache = new CSSDocumentCache();
        this.workspace = workspace;
    }

    private final SWFCache swfCache;
    private final FileScopeCache fileScopeCache;
    private final AssetTagCache assetTagCache;
    private final CSSDocumentCache cssDocumentCache;
    private final IWorkspace workspace;



    /**
     * @return the swcCache
     */
    @Override
    public ISWFCache getSWFCache()
    {
        return swfCache;
    }

    /**
     * @return the fileScopeCache
     */
    @Override
    public IFileScopeCache getFileScopeCache()
    {
        return fileScopeCache;
    }

    /**
     * @return the assetTagCache
     */
    @Override
    public IAssetTagCache getAssetTagCache()
    {
        return assetTagCache;
    }

    public IWorkspace getWorkspace()
    {
        return workspace;
    }

    @Override
    protected ISWC createEntryValue(CacheStoreKeyBase key)
    {
        if (key instanceof SWCCacheKey)
        {
            final SWCCacheKey cacheKey = (SWCCacheKey)key;
            final SWCReader reader = new SWCReader(cacheKey.file, workspace.getASDocDelegate().getPackageDitaParser());
            final ISWC swc = reader.getSWC();
            
            assert swc != null : "Expect a SWC model object.";
            return swc;
        }
        else
        {
            throw new IllegalArgumentException("Expected a SWCCacheKey.");
        }
    }

    @Override
    public ISWC get(File file)
    {
        final ISWC result = this.get(new SWCCacheKey(file));
        return result;
    }

    @Override
    public void remove(File file)
    {
        ISWC removedSWC = this.remove(new SWCCacheKey(file));
        if (removedSWC == null)
            return;
        for (ISWCLibrary lib : removedSWC.getLibraries())
        {
            String librarySWFPath = lib.getPath();
            for (ISWCScript script : lib.getScripts())
            {
                fileScopeCache.remove(FileScopeCache.createKey(removedSWC, librarySWFPath, script));
                for (String scriptDefQName : script.getDefinitions())
                    assetTagCache.remove(AssetTagCache.createKey(removedSWC, librarySWFPath, script, scriptDefQName));
            }

            swfCache.remove(SWFCache.createKey(removedSWC, lib.getPath()));
        }

        for (String defaultsCSSFileName : CSSDocumentCache.ALL_DEFAULTS_CSS_FILENAMES)
            cssDocumentCache.remove(CSSDocumentCache.createKey(removedSWC, defaultsCSSFileName));

        for (String fileInSWC : removedSWC.getFiles().keySet())
            swfCache.remove(SWFCache.createKey(removedSWC, fileInSWC));
    }

    @Override
    public ICSSDocumentCache getCSSDocumentCache()
    {
        return cssDocumentCache;
    }
}
