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
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IIterationFlowNode;

/**
 * Represents a statement that changes the flow within an iterative statement.
 * This node can either represent a break or a continue statement.
 */
public class IterationFlowNode extends FixedChildrenNode implements IIterationFlowNode
{
    /**
     * Constructor.
     */
    public IterationFlowNode(IASToken token)
    {
        if (token != null)
        {
            startBefore(token);
            
            switch (token.getType())
            {
                case ASTokenTypes.TOKEN_KEYWORD_BREAK:
                {
                    kind = IterationFlowKind.BREAK;
                    break;
                }
                case ASTokenTypes.TOKEN_KEYWORD_CONTINUE:
                {
                    kind = IterationFlowKind.CONTINUE;
                    break;
                }
                case ASTokenTypes.TOKEN_RESERVED_WORD_GOTO:
                {
                    kind = IterationFlowKind.GOTO;
                }
            }
            
            endAfter(token);
        }
    }

    private IdentifierNode labelNode;

    private IterationFlowKind kind;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        switch (kind)
        {
            case BREAK:
                return ASTNodeID.BreakID;
                
            case CONTINUE:
                return ASTNodeID.ContinueID;
                
            case GOTO:
                return ASTNodeID.GotoID;
        }
        
        assert false : "Unknown control flow type!";
        return ASTNodeID.UnknownID;
    }

    @Override
    public int getChildCount()
    {
        return labelNode != null ? 1 : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0)
            return labelNode;
        
        return null;
    }
    
    //
    // IIterationFlowNode implementations
    //

    @Override
    public IterationFlowKind getKind()
    {
        return kind;
    }

    @Override
    public IIdentifierNode getLabelNode()
    {
        return labelNode;
    }
    
    //
    // Other methods
    //

    public void setLabel(IdentifierNode labelNode)
    {
        this.labelNode = labelNode;
        labelNode.setParent(this);
        endAfter(labelNode);
    }
}
