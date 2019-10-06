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

package org.apache.royale.compiler.internal.units.requests;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.royale.compiler.embedding.IEmbedData;
import org.apache.royale.compiler.internal.embedding.transcoders.TranscoderBase;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.tags.DoABCTag;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;

/**
 * Create a {@code DoABC} SWF tag for the given ABC byte code. The
 * {@link #addToFrame(SWFFrame)} call back method will add the {@code DoABC} tag
 * to a frame.
 */
public class SWFTagsRequestResult implements ISWFTagsRequestResult
{
    /**
     * Create a {@code SWFTagsRequestResult}.
     * 
     * @param abcData Raw ABC byte array.
     * @param tagName SWF tag name.
     */
    public SWFTagsRequestResult(final byte[] abcData, final String tagName)
    {
        this(abcData, tagName, Collections.<IEmbedData>emptySet());
    }

    /**
     * Create a {@code SWFTagsRequestResult}.
     * 
     * @param abcData Raw ABC byte array.
     * @param tagName SWF tag name.
     * @param embeddedAssets embedded assets
     */
    public SWFTagsRequestResult(final byte[] abcData, final String tagName,
                         final Collection<IEmbedData> embeddedAssets)
    {
        this.abcData = abcData;
        this.tagName = tagName;
        this.problems = new LinkedList<ICompilerProblem>();
        this.additionalTags = new LinkedList<ITag>();
        this.assetTags = new LinkedHashMap<String, ICharacterTag>();

        for (IEmbedData embedData : embeddedAssets)
        {
            TranscoderBase transcoder = (TranscoderBase)embedData.getTranscoder();
            Map<String, ICharacterTag> tags = transcoder.getTags(additionalTags, this.problems);

            if (tags != null)
                assetTags.putAll(tags);
        }
    }

    private final byte[] abcData;
    private final String tagName;
    private final List<ITag> additionalTags;
    private final Map<String, ICharacterTag> assetTags;
    private final Collection<ICompilerProblem> problems;
    private DoABCTag doABC;

    @Override
    public ICompilerProblem[] getProblems()
    {
        return problems.toArray(new ICompilerProblem[problems.size()]);
    }

    @Override
    public boolean addToFrame(SWFFrame f)
    {
        if (abcData == null)
            return false;

        if (doABC == null)
        {
        	doABC = new DoABCTag();
	        doABC.setABCData(abcData);
	        doABC.setName(tagName);
        }
        
        for (ITag tag : additionalTags)
        {
            f.addTag(tag);
        }

        f.addTag(doABC);

        for (Entry<String, ICharacterTag> entrySet : assetTags.entrySet())
        {
            ICharacterTag assetTag = entrySet.getValue();
            f.addTag(assetTag);
            f.defineSymbol(assetTag, entrySet.getKey());
        }

        return true;
    }

    @Override
    public String getDoABCTagName()
    {
        return tagName;
    }
    
    @Override
    public DoABCTag getDoABCTag()
    {
        if (doABC == null)
        {
        	doABC = new DoABCTag();
	        doABC.setABCData(abcData);
	        doABC.setName(tagName);
        }        
        return doABC;
    }

}
