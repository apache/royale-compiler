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

import static org.apache.royale.compiler.common.ISourceLocation.UNKNOWN;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import com.google.common.collect.ImmutableMap;

/**
 * Tokenizes sequences of metadata attributes (e.g. [Event(name="click")]) in
 * ActionScript. Uses RawMetadataTokenizer to get raw tokens. Does not attempt
 * to identify keywords.
 */
public class MetadataTokenizer
{
    /**
     * Are we in an attribute list?
     */
    protected boolean inAttrList;

    protected int adjust = 0;

    private boolean unknownKeyword;

    private RawASTokenizer tokenizer;

    private static final Map<String, Integer> keywordToTokenMap = new ImmutableMap.Builder<String, Integer>()
            .put(IMetaAttributeConstants.ATTRIBUTE_BINDABLE, MetadataTokenTypes.TOKEN_BINDABLE_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_EVENT, MetadataTokenTypes.TOKEN_EVENT_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_EFFECT, MetadataTokenTypes.TOKEN_EFFECT_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_STYLE, MetadataTokenTypes.TOKEN_STYLE_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_ARRAYELEMENTTYPE, MetadataTokenTypes.TOKEN_ARRAYELEMENTTYPE_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_DEFAULTPROPERTY, MetadataTokenTypes.TOKEN_DEFAULTPROPERTY_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_INSPECTABLE, MetadataTokenTypes.TOKEN_INSPECTABLE_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_INSTANCETYPE, MetadataTokenTypes.TOKEN_INSTANCETYPE_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_NONCOMMITTING, MetadataTokenTypes.TOKEN_NONCOMMITTINGCHANGE_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_ACCESSIBIlITY_CLASS, MetadataTokenTypes.TOKEN_ACCESSIBILITY_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_STATES, MetadataTokenTypes.TOKEN_STATES_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_RESOURCEBUNDLE, MetadataTokenTypes.TOKEN_RESOURCEBUNDLE_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_HOST_COMPONENT, MetadataTokenTypes.TOKEN_HOST_COMPONENT_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_SKIN_CLASS, MetadataTokenTypes.TOKEN_SKINCLASS_KEYWORD)
            .put(IMetaAttributeConstants.ATTRIBUTE_ALTERNATIVE, MetadataTokenTypes.TOKEN_ALTERNATIVE_KEYWORD)
            .build();

    private static final Map<String, Integer> attrToTokenMap = new ImmutableMap.Builder<String, Integer>()
            .put(IMetaAttributeConstants.NAME_STYLE_NAME, MetadataTokenTypes.TOKEN_ATTR_NAME)
            .put(IMetaAttributeConstants.NAME_STYLE_TYPE, MetadataTokenTypes.TOKEN_ATTR_TYPE)
            .put(IMetaAttributeConstants.NAME_STYLE_ARRAYTYPE, MetadataTokenTypes.TOKEN_ATTR_ARRAY_TYPE)
            .put(IMetaAttributeConstants.NAME_STYLE_FORMAT, MetadataTokenTypes.TOKEN_ATTR_FORMAT)
            .put(IMetaAttributeConstants.NAME_STYLE_ENUMERATION, MetadataTokenTypes.TOKEN_ATTR_ENUM)
            .put(IMetaAttributeConstants.NAME_STYLE_INHERIT, MetadataTokenTypes.TOKEN_ATTR_INHERITS)
            .put(IMetaAttributeConstants.NAME_EFFECT_EVENT, MetadataTokenTypes.TOKEN_ATTR_EVENT)
            .put(IMetaAttributeConstants.NAME_INSPECTABLE_ENVIRONMENT, MetadataTokenTypes.TOKEN_ATTR_ENV)
            .put(IMetaAttributeConstants.NAME_INSPECTABLE_VERBOSE, MetadataTokenTypes.TOKEN_ATTR_VERBOSE)
            .put(IMetaAttributeConstants.NAME_INSPECTABLE_CATEGORY, MetadataTokenTypes.TOKEN_ATTR_CATEGORY)
            .put(IMetaAttributeConstants.NAME_INSPECTABLE_VARIABLE, MetadataTokenTypes.TOKEN_ATTR_VARIABLE)
            .put(IMetaAttributeConstants.NAME_INSPECTABLE_DEFAULT_VALUE, MetadataTokenTypes.TOKEN_ATTR_DEFAULT_VALUE)
            .put(IMetaAttributeConstants.VALUE_SKIN_PART_REQUIRED_TRUE, MetadataTokenTypes.TOKEN_STRING)
            .put(IMetaAttributeConstants.VALUE_SKIN_PART_REQUIRED_FALSE, MetadataTokenTypes.TOKEN_STRING)
            .put(IMetaAttributeConstants.NAME_STYLE_STATES, MetadataTokenTypes.TOKEN_ATTR_STATES)
            .put(IMetaAttributeConstants.NAME_ALTERNATIVE_REPLACEMENT, MetadataTokenTypes.TOKEN_ATTR_TYPE)
            .put(IMetaAttributeConstants.NAME_ACCESSIBILITY_IMPLEMENTATION, MetadataTokenTypes.TOKEN_ATTR_IMPLEMENTATION)
            .build();

    /**
     * Constructor
     */
    public MetadataTokenizer(Reader reader)
    {
        tokenizer = new RawASTokenizer(reader);
        unknownKeyword = true;
        inAttrList = false;
    }

    public MetadataTokenizer()
    {
        unknownKeyword = true;
        inAttrList = false;
    }

    /**
     * Sets the reader that will be used as the input for the data that will be
     * tokenized.
     * 
     * @param reader a Reader pointing to a source that will yield text
     */
    public void setInput(Reader reader) throws IOException
    {
        if (tokenizer != null)
            close();
        tokenizer = new RawASTokenizer(reader);
    }

    public void setAdjust(int offset)
    {
        adjust = offset;
    }

    public MetadataToken next()
    {
        ASToken token = null;
        boolean needToken = true;
        while (needToken)
        {
            try
            {
                token = tokenizer.nextToken();
                needToken = false;
            }
            catch (Exception e)
            {
                needToken = true;
            }
            if (token == null)
                return null;
            switch (token.getType())
            {
                case ASTokenTypes.TOKEN_IDENTIFIER:
                case ASTokenTypes.TOKEN_LITERAL_NUMBER:
                case ASTokenTypes.TOKEN_LITERAL_STRING:
                case ASTokenTypes.TOKEN_SQUARE_OPEN:
                case ASTokenTypes.TOKEN_SQUARE_CLOSE:
                case ASTokenTypes.TOKEN_PAREN_OPEN:
                case ASTokenTypes.TOKEN_PAREN_CLOSE:
                    return transformToken(token);
                case ASTokenTypes.TOKEN_COMMA: //skip comma
                    needToken = true;
                    break;
                case ASTokenTypes.TOKEN_OPERATOR_ASSIGNMENT:
                    break;
                default:
                    //TODO log errors here
                    break;
            }
        }

        return null;
    }

    /**
     * Closes the underlying reader
     */
    public void close() throws IOException
    {
        if (tokenizer != null)
            tokenizer.yyclose();
    }

    /**
     * Parse the sequence of metadata attributes in input and return a list of
     * MetadataTokens
     * 
     * @return list of MetadataTokens
     */
    public List<MetadataToken> parseTokens()
    {
        ArrayList<MetadataToken> tokenList = new ArrayList<MetadataToken>(100);
        MetadataToken token = null;
        do
        {
            token = next();
            if (token == null)
                return tokenList;
            tokenList.add(token);
        }
        while (true);
    }

    /**
     * transforms an ASToken to a MetaDataToken. Keeps state, should only be
     * called during parsing or with a list of tokens
     * 
     * @param token the token to transform
     * @return a MetadataToken or null if the token, while valid, cannot be
     * tranformed such as TOKEN_COMMA
     */
    public final MetadataToken transformToken(ASToken token)
    {
        String tokenString = token.getText();
        String sourcePath = token.getSourcePath();
        int startOffset = token.getStart() + adjust;
        int endOffset = token.getEnd() + adjust;
        int line = token.getLine();
        int column = token.getColumn();

        switch (token.getType())
        {
            case ASTokenTypes.TOKEN_IDENTIFIER:
            {
                if (!inAttrList)
                {
                    unknownKeyword = true;
                    Object object = keywordToTokenMap.get(token.getText());
                    int num = MetadataTokenTypes.TOKEN_UNKNOWN_KEYWORD;
                    if (object != null)
                    {
                        num = ((Integer)object).intValue();
                        unknownKeyword = false;
                    }
                    return new MetadataToken(num, sourcePath, startOffset, endOffset,
                                             line, column, tokenString);
                }
                else
                {
                    Object object = attrToTokenMap.get(token.getText());
                    int num = MetadataTokenTypes.TOKEN_ATTR_UNKNOWN;
                    if (object != null && !unknownKeyword)
                        num = ((Integer)object).intValue();
                    return new MetadataToken(num, sourcePath, startOffset, endOffset,
                                             line, column, tokenString);
                }
            }

            case ASTokenTypes.TOKEN_LITERAL_NUMBER:
            case ASTokenTypes.TOKEN_LITERAL_STRING:
            {
                if (tokenString.length() > 0 &&
                    ((tokenString.charAt(0) == '"') ||
                     (tokenString.charAt(0) == '\'')))
                {
                    startOffset++;
                    tokenString = tokenString.substring(1);
                }

                if (tokenString.length() > 0 &&
                    ((tokenString.charAt(tokenString.length() - 1) == '"') ||
                      tokenString.charAt(tokenString.length() - 1) == '\''))
                {
                    endOffset--;
                    tokenString = tokenString.substring(0, tokenString.length() - 1);
                }
                return new MetadataToken(MetadataTokenTypes.TOKEN_STRING,
                                         sourcePath, startOffset, endOffset,
                                         line, column, tokenString);
            }

            case ASTokenTypes.TOKEN_SQUARE_OPEN:
            {
                return new MetadataToken(MetadataTokenTypes.TOKEN_OPEN_BRACE,
                                         sourcePath, startOffset, endOffset,
                                         line, column, tokenString);
            }

            case ASTokenTypes.TOKEN_SQUARE_CLOSE:
            {
                return new MetadataToken(MetadataTokenTypes.TOKEN_CLOSE_BRACE,
                                         sourcePath, startOffset, endOffset,
                                         line, column, tokenString);
            }

            case ASTokenTypes.TOKEN_PAREN_OPEN:
            {
                inAttrList = true;
                return new MetadataToken(MetadataTokenTypes.TOKEN_OPEN_PAREN,
                                         sourcePath, startOffset, endOffset,
                                         line, column, tokenString);
            }

            case ASTokenTypes.TOKEN_PAREN_CLOSE:
            {
                inAttrList = false;
                return new MetadataToken(MetadataTokenTypes.TOKEN_CLOSE_PAREN,
                                         sourcePath, startOffset, endOffset,
                                         line, column, tokenString);
            }
            case ASTokenTypes.TOKEN_OPERATOR_ASSIGNMENT:
            case ASTokenTypes.TOKEN_COMMA:
            {
                return null;
            }
            case ASTokenTypes.TOKEN_OPERATOR_NS_QUALIFIER:
            {
                if (inAttrList)
                {
                    return new MetadataToken(MetadataTokenTypes.TOKEN_ATTR_OPERATOR_NS_QUALIFIER,
                                         sourcePath, startOffset, endOffset,
                                         line, column, tokenString);
                }
            }
        }

        return null;
    }

    public static final MetadataToken buildNameToken(String name)
    {
        Object object = keywordToTokenMap.get(name);
        int num = MetadataTokenTypes.TOKEN_UNKNOWN_KEYWORD;
        if (object != null)
        {
            num = ((Integer)object).intValue();
        }
        return new MetadataToken(num, null, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, name);
    }

    public static final MetadataToken buildIdentifierToken(String identifier, int parentID)
    {
        Object object = attrToTokenMap.get(identifier);
        int num = MetadataTokenTypes.TOKEN_ATTR_UNKNOWN;
        if (object != null && parentID != MetadataTokenTypes.TOKEN_UNKNOWN_KEYWORD)
        {
            num = ((Integer)object).intValue();
        }
        return new MetadataToken(num, null, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, identifier);
    }

    public static final MetadataToken buildOpenBraceToken()
    {
        return new MetadataToken(MetadataTokenTypes.TOKEN_OPEN_BRACE, null, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, "[");
    }

    public static final MetadataToken buildCloseBraceToken()
    {
        return new MetadataToken(MetadataTokenTypes.TOKEN_CLOSE_BRACE, null, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, "]");
    }

    public static final MetadataToken buildOpenParenToken()
    {
        return new MetadataToken(MetadataTokenTypes.TOKEN_OPEN_PAREN, null, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, "(");
    }

    public static final MetadataToken buildCloseParenToken()
    {
        return new MetadataToken(MetadataTokenTypes.TOKEN_CLOSE_PAREN, null, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, ")");
    }

    public static final MetadataToken buildStringToken(String identifier)
    {
        return new MetadataToken(MetadataTokenTypes.TOKEN_STRING, null, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, identifier);
    }

    public static final MetadataToken buildIdentifierToken(String identifier)
    {
        return new MetadataToken(MetadataTokenTypes.TOKEN_ID, null, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, identifier);
    }
}
