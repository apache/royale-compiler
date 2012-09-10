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

package org.apache.flex.compiler.parsing;

import org.apache.flex.compiler.internal.parsing.as.ASTokenTypes;

/**
 * MXML token type constants.
 */
public interface MXMLTokenTypes
{
    public static final int HIDDEN_TOKEN_COMMENT = ASTokenTypes.HIDDEN_TOKEN_COMMENT;
    public static final int HIDDEN_TOKEN_CDATA = ASTokenTypes.TOKEN_E4X_CDATA;
    public static final int TOKEN_ASDOC_COMMENT = ASTokenTypes.TOKEN_ASDOC_COMMENT;
    public static final int TOKEN_CDATA = ASTokenTypes.TOKEN_E4X_CDATA;
    public static final int TOKEN_CLOSE_TAG_START = ASTokenTypes.TOKEN_E4X_CLOSE_TAG_START;
    public static final int TOKEN_COMMENT = ASTokenTypes.TOKEN_E4X_COMMENT;
    public static final int TOKEN_DATABINDING = 1128;
    public static final int TOKEN_DATABINDING_END = ASTokenTypes.TOKEN_E4X_BINDING_CLOSE;
    public static final int TOKEN_DATABINDING_START = ASTokenTypes.TOKEN_E4X_BINDING_OPEN;
    public static final int TOKEN_DECIMAL_ENTITY = ASTokenTypes.TOKEN_E4X_DECIMAL_ENTITY;
    public static final int TOKEN_EMPTY_TAG_END = ASTokenTypes.TOKEN_E4X_EMPTY_TAG_END;
    public static final int TOKEN_ENTITY = ASTokenTypes.TOKEN_E4X_ENTITY;
    public static final int TOKEN_EQUALS = ASTokenTypes.TOKEN_E4X_EQUALS;
    public static final int TOKEN_HEX_ENTITY = ASTokenTypes.TOKEN_E4X_HEX_ENTITY;
    public static final int TOKEN_MXML_BLOB = 30;
    public static final int TOKEN_NAME = ASTokenTypes.TOKEN_E4X_NAME;
    public static final int TOKEN_OPEN_TAG_START = ASTokenTypes.TOKEN_E4X_OPEN_TAG_START;
    public static final int TOKEN_PROCESSING_INSTRUCTION = ASTokenTypes.TOKEN_E4X_PROCESSING_INSTRUCTION;
    public static final int TOKEN_STATE_NAME = ASTokenTypes.TOKEN_E4X_DOTTED_NAME_PART;
    public static final int TOKEN_STATE_OPERATOR = ASTokenTypes.TOKEN_E4X_NAME_DOT;
    public static final int TOKEN_STRING = ASTokenTypes.TOKEN_E4X_STRING;
    public static final int TOKEN_TAG_END = ASTokenTypes.TOKEN_E4X_TAG_END;
    public static final int TOKEN_TEXT = ASTokenTypes.TOKEN_E4X_TEXT;
    public static final int TOKEN_WHITESPACE = ASTokenTypes.TOKEN_E4X_WHITESPACE;
    public static final int TOKEN_XMLNS = ASTokenTypes.TOKEN_E4X_XMLNS;
}
