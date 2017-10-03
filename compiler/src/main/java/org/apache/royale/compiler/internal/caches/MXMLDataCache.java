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

import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.util.List;

import org.apache.commons.io.IOUtils;

import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.mxml.MXMLData;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLTokenizer;

/**
 * Concurrent cache for parsed MXML models. The cache is a list of key-value pairs.
 * The key is an {@code IFileSpecificaton} for an MXML file.
 * The value is a {@link SoftReference} to a DOM-like {@link MXMLData} object.
 */
public class MXMLDataCache extends ConcurrentCacheStoreBase<MXMLData>
{
    private static class MXMLDataCacheKey extends CacheStoreKeyBase
    {
        public MXMLDataCacheKey(IFileSpecification fileSpec)
        {
            assert fileSpec != null;
            this.fileSpec = fileSpec;
        }
        
        private IFileSpecification fileSpec; // non-null
        
        @Override
        public String generateKey()
        {
            return String.valueOf(fileSpec.isOpenDocument()) + ":" + fileSpec.getPath();
        }
    }

    public static MXMLDataCacheKey createKey(IFileSpecification fileSpec)
    {
        return new MXMLDataCacheKey(fileSpec);
    }

    /**
     * Constructor.
     */
    public MXMLDataCache()
    {
        super();
    }
    
    /**
     * Get the {@code MXMLData} to be associated with the key.
     * 
     * @param key The key is an {@code MXMLDataCacheKey} object,
     * which encapsulates an {@code IFileSpecification} for an MXML file.
     */
    @Override
    protected MXMLData createEntryValue(CacheStoreKeyBase key)
    {
       final IFileSpecification fileSpec = ((MXMLDataCacheKey)key).fileSpec;
    
        // Tokenize the MXML file. 
        final MXMLTokenizer tokenizer = new MXMLTokenizer(fileSpec);
        try
        {
            List<MXMLToken> tokens = tokenizer.parseTokens(fileSpec.createReader());
            
            // Build tags and attributes from the tokens.
            final MXMLData mxmlData = new MXMLData(tokens, tokenizer.getPrefixMap(), fileSpec);
            if (tokenizer.hasTokenizationProblems())
            	mxmlData.getProblems().addAll(tokenizer.getTokenizationProblems());
            return mxmlData;
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        finally
        {
            IOUtils.closeQuietly(tokenizer);
        }
    }
}
