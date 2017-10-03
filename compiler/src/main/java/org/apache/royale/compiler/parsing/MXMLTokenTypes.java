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

/**
 * MXML token type constants.
 */
public interface MXMLTokenTypes
{
    /**
     * This type of MXML token represents an ASDoc comment.
     * <p>
     * Example: {@code <!--- Hello -->}
     */
    public static final int TOKEN_ASDOC_COMMENT = 1;
    
    /**
     * This type of MXML token represents a CDATA block.
     * <p>
     * Example: {@code <![CDATA[Hello]]>}
     */
    public static final int TOKEN_CDATA = 2;
    
    /**
     * This type of MXML token represents the start of a close tag.
     * <p>
     * Example: {@code </s:color.over}
     * <p>
     * It includes the  left-angle-bracket, the slash,
     * and the tag name (including a possible prefix and state suffix).
     * It does not include the closing right-angle-bracket.
     */
    public static final int TOKEN_CLOSE_TAG_START = 3;
    
    /**
     * This type of MXML token represents a regular (i.e., non-ASDoc) comment.
     * <p>
     * Example: {@code <!-- Hello -->}
     */
    public static final int TOKEN_COMMENT = 4;
    
    /**
     * This type of MXML token represents the slash and right-angle-bracket
     * that end an empty tag.
     * <p>
     * Example: {@code />}
     */
    public static final int TOKEN_EMPTY_TAG_END = 5;
    
    /**
     * This type of MXML token represents the equals sign in an attribute.
     * <p>
     * Example: {@code =}
     */
    public static final int TOKEN_EQUALS = 6;
    
    /**
     * This type of MXML token represents an attribute name,
     * including a possible prefix and state suffix.
     * <p>
     * Example: {@code s:color.over}
     */
    public static final int TOKEN_NAME = 7;
    
    /**
     * This type of MXML token represents the start of an open tag.
     * <p>
     * Example: {@code <s:color.over}
     * <p>
     * It includes the  left-angle-bracket and the tag name
     * (including a possible prefix and state suffix).
     * It does not include the closing right-angle-bracket.
     */
    public static final int TOKEN_OPEN_TAG_START = 8;
    
    /**
     * This type of MXML token represents an entire processing instruction.
     * <p>
     * Example: {@code <?xml version="1.0" encoding="utf-8"?>}
     */
    public static final int TOKEN_PROCESSING_INSTRUCTION = 9;
    
    /**
     * This type of MXML token represents an attribute value.
     * <p>
     * Example: {@code "100"}<br>
     * Example: <code>'{a}'</code>
     */
    public static final int TOKEN_STRING = 10;
    
    /**
     * This type of MXML token represents the right-angle-bracket
     * that ends an open tag or a close tag.
     * <p>
     * Example: {@code >}
     */
    public static final int TOKEN_TAG_END = 11;
    
    /**
     * This type of MXML token represents character data that appears between tags.
     * <p>
     * Example: {@code Hello, World}
     * <p>
     * It may contain leading, trailing, or internal whitespace
     * but it will not be entirely whitespace.
     * It may also contain XML entities but will not contain
     * CDATA blocks, comments, or processing instructions.
     */
    public static final int TOKEN_TEXT = 12;
    
    /**
     * This type of MXML token represents a run of character data that is entirely whitespace.
     * <p>
     * Example:
     */
    public static final int TOKEN_WHITESPACE = 13;
    
    /**
     * This type of MXML token represents the name of a namespace attribute.
     * <p>
     * Example: {@code xmlns:s}
     * <br>
     * Example: {@code xmlns}
     */
    public static final int TOKEN_XMLNS = 14;
}
