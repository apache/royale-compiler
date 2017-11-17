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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.royale.compiler.caches.ISWFCache;
import org.apache.royale.compiler.problems.FileInLibraryNotFoundProblem;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.SWC;
import org.apache.royale.swc.SWCManager;
import org.apache.royale.swc.io.SWCReader;
import org.apache.royale.swf.ITagContainer;
import org.apache.royale.swf.io.SWFReader;
import org.apache.royale.swf.tags.DoABCTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.swf.tags.SymbolClassTag;

/**
 * Cache for parsed SWF models. The cache is a list of key-value pairs. The key
 * is the URI to the SWF file. The value is a {@link SoftReference} to a
 * collection of SWF tags.
 */
public class SWFCache extends ConcurrentCacheStoreBase<ITagContainer> implements ISWFCache
{

    private static abstract class SWFCacheKey extends CacheStoreKeyBase
    {
        public SWFCacheKey()
        {
        }
        
        abstract void readSWF(SWFReader swfReader) throws IOException;
    }
    
    /**
     * Key object for {@code SWFCache}. It has two forms:
     * <ol>
     * <li>When {@code swcFile} is null, the key is an absolute path to an
     * arbitrary SWF file.</li>
     * <li>When {@code swcFile} is not null, the key is a URI to a library SWF
     * inside a SWC file.</li>
     * </ol>
     */
    protected static class SWFInSWCCacheKey extends SWFCacheKey
    {
        protected ISWC swc; // null-able
        protected String swfPath; // non-null

        public SWFInSWCCacheKey()
        {
        }

        @Override
        public String generateKey()
        {
            return String.format("%s:%s", swc.getSWCFile().getAbsolutePath(), swfPath).intern();
        }

        @Override
        void readSWF(SWFReader swfReader) throws IOException
        {
            ZipFile zipFile = null;
            try
            {
                // Load library SWF inside a SWC.
                zipFile = new ZipFile(swc.getSWCFile(), ZipFile.OPEN_READ);
                InputStream swfInputStream = SWCReader.getInputStream(zipFile, swfPath);
                if (swfInputStream != null)
                {
                    swfInputStream = new BufferedInputStream(swfInputStream);
                    swfReader.readFrom(swfInputStream, SWCReader.getReportingPath( 
                            swc.getSWCFile().getAbsolutePath(), swfPath));
                }
                else if (swc instanceof SWC)
                {
                    ((SWC)swc).addProblem(new FileInLibraryNotFoundProblem(swfPath, 
                            swc.getSWCFile().getAbsolutePath()));
                }
            }
            finally
            {
                // closes the input steam as well as the zip file.
                zipFile.close();
            }
        }
    }
    
    /**
     * Key object for {@code SWFCache}. It has two forms:
     * <ol>
     * <li>When {@code swcFile} is null, the key is an absolute path to an
     * arbitrary SWF file.</li>
     * <li>When {@code swcFile} is not null, the key is a URI to a library SWF
     * inside a SWC file.</li>
     * </ol>
     */
    protected static class SWFFileCacheKey extends SWFCacheKey
    {
        private String fileName;

        public SWFFileCacheKey()
        {
        }

        @Override
        public String generateKey()
        {
            return fileName.intern();
        }

        @Override
        void readSWF(SWFReader swfReader) throws IOException
        {
            InputStream swfInputStream =  new FileInputStream(fileName);
            try
            {
                swfInputStream = new BufferedInputStream(swfInputStream);
                swfReader.readFrom(swfInputStream, fileName);
            }
            finally
            {
                swfInputStream.close();
            }
        }
    }

    /**
     * Factory method for creating a key object for {@code SWCCache}.
     * 
     * @param swc SWC file (optional)
     * @param swfPath path to a SWF file
     * @return key
     */
    public static SWFInSWCCacheKey createKey(ISWC swc, String swfPath)
    {
        assert swc != null;
        assert swfPath != null;
        final SWFInSWCCacheKey key = new SWFInSWCCacheKey();
        key.swc = swc;
        key.swfPath = swfPath;
        return key;
    }
    
    /**
     * Factory method for creating a key object for {@code SWCCache}.
     * 
     * @param swfFileName path to a SWF file
     * @return key
     */
    public static SWFFileCacheKey createKey(String swfFileName)
    {
        final SWFFileCacheKey key = new SWFFileCacheKey();
        key.fileName = swfFileName.intern();
        return key;
    }

    /**
     * Find {@code DoABC} tag by name.
     * 
     * @param tags list of tags
     * @param abcName abc name
     * @return {@code DoABCTag} or null
     */
    public static DoABCTag findDoABCTagByName(ITagContainer tags, String abcName)
    {
        assert (abcName != null && !"".equals(abcName)) : "expect ABC name.";

        for (final ITag tag : tags)
        {
            if (tag instanceof DoABCTag)
            {
                final DoABCTag abcTag = (DoABCTag)tag;
                if (abcTag.getName().equals(abcName))
                {
                    return abcTag;
                }
            }
        }

        return null;
    }

    /**
     * Find all {@code SymbolClassTag} contained in the SWF.
     * 
     * @param tags list of tags
     * @return A Collection of {@code SymbolClassTag}
     */
    public static Collection<SymbolClassTag> findAllSymbolClassTags(ITagContainer tags)
    {
        List<SymbolClassTag> symbolTags = new ArrayList<SymbolClassTag>();
        for (final ITag tag : tags)
        {
            if (tag instanceof SymbolClassTag)
                symbolTags.add((SymbolClassTag)tag);
        }

        return symbolTags;
    }

    /**
     * @param swcManager The object that manages SWC files.
     */
    public SWFCache(SWCManager swcManager)
    {
        super();
    }

    /**
     * Get all the tags from a SWF file.
     * 
     * @param key {@code ICacheStoreKey} key object
     * @return {@link ITagContainer} contains all the tags in the SWF file.
     */
    @Override
    protected ITagContainer createEntryValue(CacheStoreKeyBase key)
    {
        if (!(key instanceof SWFCacheKey))
            throw new IllegalArgumentException("expect SWFCacheKey but got " + key.getClass().getSimpleName());

        try
        {
            final SWFReader swfReader = new SWFReader(false); // Need not to build SWF frames.
            readInputStream(swfReader, (SWFCacheKey)key);
            return swfReader;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read the {@code InputStream} from SWF file URI.
     * 
     * @param swfReader {@code SWFReader} object
     * @param swfCacheKey {@code SWFCacheKey} object
     * @throws IOException error
     */
    private void readInputStream(SWFReader swfReader, SWFCacheKey swfCacheKey) throws IOException
    {
        swfCacheKey.readSWF(swfReader);
    }
}
