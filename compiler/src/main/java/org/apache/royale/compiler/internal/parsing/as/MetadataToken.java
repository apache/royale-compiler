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

import org.apache.royale.compiler.internal.parsing.TokenBase;
import org.apache.royale.compiler.parsing.ICMToken;

/**
 * Metadata token (output unit of MetadataTokenizer, input unit of
 * MetadataParser)
 */
public class MetadataToken extends TokenBase
{

    /**
     * Builds a new token for Metadata
     * 
     * @param tokenType type of Metadata token
     * @param start start offset of the token
     * @param end end of token
     * @param line line of token
     * @param tokenText text of token
     */
    public MetadataToken(int tokenType, String sourcePath, int start, int end, int line, int column, String tokenText)
    {
        super(tokenType, start, end, line, column, tokenText);
        setSourcePath(sourcePath);
    }

    public MetadataToken(TokenBase other)
    {
        super(other);
    }

    @Override
    public ICMToken changeType(int type)
    {
        return new MetadataToken(type, getSourcePath(), getStart(), getEnd(), getLine(), getColumn(), getText());
    }

    /**
     * Get the display string for the token type
     * 
     * @return display string for the token type
     */
    @Override
    protected String getTypeString()
    {
        switch (getType())
        {
            case MetadataTokenTypes.TOKEN_BINDABLE_KEYWORD:
                return "TOKEN_BINDABLE";
            case MetadataTokenTypes.TOKEN_EQUALS:
                return "TOKEN_EQUALS";
            case MetadataTokenTypes.TOKEN_EVENT_KEYWORD:
                return "TOKEN_EVENT";
            case MetadataTokenTypes.TOKEN_ICONFILE_KEYWORD:
                return "TOKEN_ICONFILE";
            case MetadataTokenTypes.TOKEN_ID:
                return "TOKEN_ID";
            case MetadataTokenTypes.TOKEN_INSPECTABLE_KEYWORD:
                return "TOKEN_INSPECTABLE";
            case MetadataTokenTypes.TOKEN_NAME:
                return "TOKEN_NAME";
            case MetadataTokenTypes.TOKEN_NUMBER:
                return "TOKEN_NUMBER";
            case MetadataTokenTypes.TOKEN_OPEN_PAREN:
                return "TOKEN_OPEN_PAREN";
            case MetadataTokenTypes.TOKEN_OPEN_BRACE:
                return "TOKEN_OPEN_BRACE";
            case MetadataTokenTypes.TOKEN_STRING:
                return "TOKEN_STRING";
            case MetadataTokenTypes.TOKEN_VALUE:
                return "TOKEN_VALUE";
            default:
                return "unknown type";
        }
    }
}
