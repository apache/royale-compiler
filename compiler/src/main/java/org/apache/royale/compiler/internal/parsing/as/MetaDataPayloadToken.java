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

package org.apache.royale.compiler.internal.parsing.as;

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.parsing.ICMToken;

/**
 * This token represents a valid set of metadata, and includes all of the
 * indivdual tokens that make up that set
 */
public class MetaDataPayloadToken extends ASToken
{
    private List<MetadataToken> payload;
    private MetadataTokenizer tokenTransformer;

    /**
     * Creates a new payload token from the first token that we have determined
     * to be metadata.
     * 
     * @param token The '[' token to begin metadata
     */
    public MetaDataPayloadToken(ASToken token)
    {
        super(ASTokenTypes.TOKEN_ATTRIBUTE, token.getStart(), token.getEnd(), token.getLine(), token.getColumn(), "");
        setSourcePath(token.getSourcePath());
        payload = new ArrayList<MetadataToken>(5);
        tokenTransformer = new MetadataTokenizer();
        addToken(token);
    }

    /**
     * Copy constructor
     * 
     * @param token The other token.
     */
    public MetaDataPayloadToken(MetaDataPayloadToken token)
    {
        super(ASTokenTypes.TOKEN_ATTRIBUTE, token.getStart(), token.getEnd(), token.getLine(), token.getColumn(), "");
        setSourcePath(token.getSourcePath());
        payload = token.payload;
    }

    @Override
    public ASToken clone()
    {
        return new MetaDataPayloadToken(this);
    }

    /**
     * Adds a token to the underlying metadata payload
     * 
     * @param token The token to be added.
     */
    public void addToken(ASToken token)
    {
        final MetadataToken metadataToken = tokenTransformer.transformToken(token);
        if (metadataToken != null)
        {
            payload.add(metadataToken);
            //modify the end of this payload token to be end of the last token 
            setEnd(token.getEnd());
        }
        else
        {
            //TODO "null" means "token" is invalid - handle this
        }
    }

    @Override
    public String getText()
    {
        //prepare the text from all the other tokens
        StringBuilder builder = new StringBuilder();
        for (MetadataToken token : payload)
        {
            builder.append(token.getText());
        }
        return builder.toString();
    }

    /**
     * @return the list of tokens that have been collected. This will be a full
     * set of metadata, such as: '[', 'Event', ']'
     */
    public List<MetadataToken> getPayload()
    {
        return payload;
    }

    @Override
    public ICMToken changeType(int type)
    {
        return this;
    }

    @Override
    public void adjustLocation(int offsetAdjustment, int lineAdjustment, int columnAdjustment)
    {
        super.adjustLocation(offsetAdjustment, lineAdjustment, columnAdjustment);

        // Adjust the source location of all the subtokens of this token.
        for (MetadataToken token : getPayload())
        {
            token.adjustLocation(offsetAdjustment, lineAdjustment, columnAdjustment);
        }
    }
}
