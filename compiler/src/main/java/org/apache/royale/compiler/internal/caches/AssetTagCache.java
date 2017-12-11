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

import java.util.Collection;
import java.util.LinkedList;

import org.apache.royale.compiler.caches.IAssetTagCache;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCScript;
import org.apache.royale.swc.SWCManager;
import org.apache.royale.swf.ITagContainer;
import org.apache.royale.swf.TagType;
import org.apache.royale.swf.tags.ICharacterReferrer;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.swf.tags.SymbolClassTag;

/**
 * A class definition might have associated assets embedded in the library as a
 * SWF tag. For example, the following class definition will generate a DoABC
 * tag and a DefineJPEG tag. SymbolClass tag will have an entry to bind these
 * two tags together.
 * <p>
 * 
 * <pre>
 * [Embed(src='me.jpg')]
 * public class Me {}
 * </pre>
 * 
 * When linking against class Me, we will need both the DoABC tag and the
 * DefineJPEG tag. This cache stores all the asset SWF tags needed for a
 * definition. The cache is a table of key-value pairs.
 * <p>
 * The key is a string in the form "swc/library/script/qname". The value is a
 * soft reference to an array of non-DoABC SWF tags.
 * <p>
 * When linking, the library manager will query the cache with a key like
 * swc/library/script/qname. It asks {@link FileScopeCache} for public
 * definitions. Then it asks {@code AssetTagCache} for related character tags
 * (assets). The cache associate asset tags with QNames by looking into
 * {@link SymbolClassTag} entries.
 */
public class AssetTagCache extends ConcurrentCacheStoreBase<AssetTagCache.AssetTagCacheValue> implements IAssetTagCache
{

    /**
     * Key object for {@code AssetTagCache}. It has 4 properties:
     * <ol>
     * <li>absolute path to a SWC file</li>
     * <li>library path - relative to the root of the SWC file archive</li>
     * <li>script name - script in the library</li>
     * <li>qname - QName of the definition</li>
     * </ol>
     */
    protected static class AssetTagCacheKey extends FileScopeCache.FileScopeCacheKey
    {
        protected String qname; // non-null;

        @Override
        public String generateKey()
        {
            return String.format(
                    "%s:%s:%s:%s",
                    swc.getSWCFile().getAbsolutePath(),
                    swfPath,
                    scriptName,
                    qname).intern();
        }
    }

    /**
     * Value object for {@code AssetTagCache}.
     */
    public static class AssetTagCacheValue
    {
        public final ICharacterTag assetTag;
        public final Collection<ITag> referredTags;

        private AssetTagCacheValue(ICharacterTag assetTag)
        {
            this.assetTag = assetTag;
            this.referredTags = new LinkedList<ITag>();
        }
    }

    
    /**
     * Factory method for creating a key object for {@code AssetTagCacheKey}.
     * 
     * @param swc SWC file (optional)
     * @param swfPath path to a SWF file
     * @param script script information
     * @param qname QName of the definition
     * @return key
     */
    public static AssetTagCacheKey createKey(ISWC swc, String swfPath, ISWCScript script, String qname)
    {
        final AssetTagCacheKey key = new AssetTagCacheKey();
        key.swc = swc;
        key.swfPath = swfPath;
        key.scriptName = script.getName();
        key.qname = qname;
        return key;
    }
    
    /**
     * @param swcManager The object that manages SWC files.
     */
    public AssetTagCache(SWCManager swcManager)
    {
        this.swcManager = swcManager;
    }
    
    private final SWCManager swcManager;

    /**
     * Asset tags are associated with definitions by entries in
     * {@code SymbolClassTag}. All the referenced tags by that asset tag in the
     * {@code SymbolClassTag} are needed as well.
     */
    @Override
    protected AssetTagCacheValue createEntryValue(CacheStoreKeyBase key)
    {
        if (!(key instanceof AssetTagCacheKey))
            throw new IllegalArgumentException("expect AssetTagCacheKey but got " + key.getClass().getSimpleName());

        final AssetTagCacheKey assetTagCacheKey = (AssetTagCacheKey)key;
        final ITagContainer tagContainer = ((SWFCache)swcManager.getSWFCache()).get(SWFCache.createKey(assetTagCacheKey.swc, assetTagCacheKey.swfPath));
        final SymbolClassTag symbolClassTag = getSymbolClass(tagContainer);
        if (symbolClassTag == null)
            return new AssetTagCacheValue(null);

        final ICharacterTag assetTag = symbolClassTag.getSymbol(assetTagCacheKey.qname);
        AssetTagCacheValue result = new AssetTagCacheValue(assetTag);
        getAllReferredTags(assetTag, result.referredTags);

        return result;
    }

    /**
     * Find {@code SymbolClass} tag.
     * 
     * @param tagContainer list of tags
     * @return {@link SymbolClassTag}
     */
    private static SymbolClassTag getSymbolClass(ITagContainer tagContainer)
    {
        for (ITag tag : tagContainer)
        {
            if (tag.getTagType() == TagType.SymbolClass)
                return (SymbolClassTag)tag;
        }

        return null;
    }

    /**
     * Recursively find all the tags referred by the character tag and its
     * referred tags.
     * 
     * @param tag character tag
     * @param referredTags all the referred tags
     */
    private static void getAllReferredTags(final ITag tag, final Collection<ITag> referredTags)
    {
        if (tag instanceof ICharacterReferrer)
        {
            for (final ITag referredTag : ((ICharacterReferrer)tag).getReferences())
            {
                assert referredTag != null;
                referredTags.add(referredTag);
                getAllReferredTags(referredTag, referredTags);
            }
        }
    }
}
