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

import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.ITerminalNode;

/**
 * Represents a terminal expression in a conditional node. A terminal node is
 * the last path in a conditional that can be reached.
 */
public class TerminalNode extends BaseStatementNode implements ITerminalNode
{
    /**
     * Constructor.
     * 
     * @param token The token representing the <code>else</code>,
     * <code>default</code>, or <code>finally</code> keyword.
     */
    public TerminalNode(IASToken token)
    {
        super();
        
        startBefore(token);
        endAfter(token);
        
        switch (token.getType())
        {
            case ASTokenTypes.TOKEN_KEYWORD_FINALLY:
            {
                kind = TerminalKind.FINALLY;
                break;
            }
            case ASTokenTypes.TOKEN_KEYWORD_DEFAULT:
            {
                kind = TerminalKind.DEFAULT;
                break;
            }
            case ASTokenTypes.TOKEN_KEYWORD_ELSE:
            {
                kind = TerminalKind.ELSE;
                break;
            }
        }
    }

    /**
     * The type of terminal node this represents.
     */
    protected TerminalKind kind;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        switch (kind)
        {
            case FINALLY:
                return ASTNodeID.FinallyID;

            case DEFAULT:
                return ASTNodeID.DefaultID;

            case ELSE:
                return ASTNodeID.ElseID;
        }

        return ASTNodeID.TerminalID;
    }

    @Override
    public int getChildCount()
    {
        return (contentsNode != null) ? 1 : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0)
            return contentsNode;
        
        return null;
    }
    
    //
    // ITerminalNode implementations
    //

    @Override
    public TerminalKind getKind()
    {
        return kind;
    }
}
