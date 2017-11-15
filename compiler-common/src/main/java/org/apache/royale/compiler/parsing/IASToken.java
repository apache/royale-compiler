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

package org.apache.royale.compiler.parsing;

import org.apache.royale.compiler.common.ISourceLocation;

/**
 * A token returned back by an {@link IASTokenizer} created when we scan text
 */
public interface IASToken extends ICMToken, ISourceLocation
{
    /**
     * Types of tokens within the AS language
     */
    static enum ASTokenKind
    {
        STRING_LITERAL,
        REGEX_LITERAL,
        NUMBER_LITERAL,
        OPERATOR,
        XML_LITERAL,
        XMLLIST_LITERAL,
        BOOLEAN_LITERAL,
        OBJECT_LITERAL,
        SCOPE_OPEN,
        SCOPE_CLOSE,
        E4X_BINDING_OPEN,
        E4X_BINDING_CLOSE,
        KEYWORD,
        IDENTIFIER,
        PAREN_OPEN,
        PAREN_CLOSE,
        TYPED_COLLECTION_OPEN,
        TYPED_COLLECTION_CLOSE,
        BRACKET_OPEN,
        BRACKET_CLOSE,
        NAMESPACE,
        MODIFIER,
        COMMENT,
        EOF,
        METADATA,
        SEMICOLON,
        COLON,
        DEFAULT_XML_STATEMENT,
        UNKNOWN,
        INCLUDE
    }

    /**
     * Returns the kind of token that we represent.
     * 
     * @return a token kind, seen in TokenKind
     */
    ASTokenKind getTokenKind();

    /**
     * @return True if this token is a multi-line comment such as
     * ASTokenTypes#TOKEN_ASDOC_COMMENT and
     * ASTokenTypes#HIDDEN_TOKEN_MULTI_LINE_COMMENT.
     */
    boolean isMultiLineComment();
    
    /**
     * @return Local start offset.
     */
    int getLocalStart();
    
    /**
     * @return Local end offset.
     */
    int getLocalEnd();


}
