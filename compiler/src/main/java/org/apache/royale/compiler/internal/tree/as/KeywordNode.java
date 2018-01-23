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

package org.apache.royale.compiler.internal.tree.as;

import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IKeywordNode;

public class KeywordNode extends FixedChildrenNode implements IKeywordNode
{
    /**
     * Constructor.
     */
    protected KeywordNode()
    {
    }

    /**
     * Constructor.
     */
    public KeywordNode(IASToken keyword)
    {
        span(keyword);
        keywordType = keyword.getType();
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected KeywordNode(KeywordNode other)
    {
        keywordType = other.keywordType;
    }

    /**
     * Type of keyword.
     */
    private int keywordType;

    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        switch (keywordType)
        {
            case ASTokenTypes.TOKEN_KEYWORD_FUNCTION:
                return ASTNodeID.KeywordFunctionID;

            case ASTokenTypes.TOKEN_RESERVED_WORD_EXTENDS:
                return ASTNodeID.KeywordExtendsID;

            case ASTokenTypes.TOKEN_RESERVED_WORD_IMPLEMENTS:
                return ASTNodeID.KeywordImplementsID;

            case ASTokenTypes.TOKEN_KEYWORD_VAR:
                return ASTNodeID.KeywordVarID;

            case ASTokenTypes.TOKEN_RESERVED_WORD_GET:
                return ASTNodeID.KeywordGetID;

            case ASTokenTypes.TOKEN_RESERVED_WORD_SET:
                return ASTNodeID.KeywordSetID;

            case ASTokenTypes.TOKEN_KEYWORD_NEW:
                return ASTNodeID.KeywordNewID;

            case ASTokenTypes.TOKEN_KEYWORD_CONST:
                return ASTNodeID.KeywordConstID;

            case ASTokenTypes.TOKEN_KEYWORD_CLASS:
                return ASTNodeID.KeywordClassID;

            case ASTokenTypes.TOKEN_KEYWORD_INTERFACE:
                return ASTNodeID.KeywordInterfaceID;
                
            case ASTokenTypes.TOKEN_DIRECTIVE_DEFAULT_XML:
                return ASTNodeID.KeywordDefaultXMLNamespaceID;
        }
        
        return ASTNodeID.UnknownID;
    }
    
    /*
     * For debugging only. Builds a string such as <code>"extends"</code> from
     * the keyword.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getKeyword());
        sb.append('"');

        return true;
    }
    
    //
    // Other methods
    //
    
    // TODO Rename to getKeywordType() and add to interface.
    public int getKeywordId()
    {
        return keywordType;
    }

    // TODO Add to interface.
    public String getKeyword()
    {
        switch (keywordType)
        {
            case ASTokenTypes.TOKEN_KEYWORD_FUNCTION:
                return IASKeywordConstants.FUNCTION;
                
            case ASTokenTypes.TOKEN_RESERVED_WORD_EXTENDS:
                return IASKeywordConstants.EXTENDS;
                
            case ASTokenTypes.TOKEN_RESERVED_WORD_IMPLEMENTS:
                return IASKeywordConstants.IMPLEMENTS;
                
            case ASTokenTypes.TOKEN_KEYWORD_VAR:
                return IASKeywordConstants.VAR;
                
            case ASTokenTypes.TOKEN_RESERVED_WORD_GET:
                return IASKeywordConstants.GET;
                
            case ASTokenTypes.TOKEN_RESERVED_WORD_SET:
                return IASKeywordConstants.SET;
                
            case ASTokenTypes.TOKEN_KEYWORD_NEW:
                return IASKeywordConstants.NEW;
                
            case ASTokenTypes.TOKEN_KEYWORD_CONST:
                return IASKeywordConstants.CONST;
                
            case ASTokenTypes.TOKEN_KEYWORD_CLASS:
                return IASKeywordConstants.CLASS;
                
            case ASTokenTypes.TOKEN_KEYWORD_INTERFACE:
                return IASKeywordConstants.INTERFACE;
                
            case ASTokenTypes.TOKEN_DIRECTIVE_DEFAULT_XML:
                return IASKeywordConstants.DEFAULT_XML_NAMESPACE;
        }

        assert false : "Unknown keyword type " + keywordType;
        return "";
    }
}
