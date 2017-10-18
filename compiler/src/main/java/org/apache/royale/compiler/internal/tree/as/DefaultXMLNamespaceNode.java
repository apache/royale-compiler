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

import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefaultXMLNamespaceNode;

/**
 * Represents a default xml namespace statement
 */
public class DefaultXMLNamespaceNode extends FixedChildrenNode implements IDefaultXMLNamespaceNode
{
    /**
     * Constructor.
     */
    public DefaultXMLNamespaceNode(KeywordNode keywordNode)
    {
        this.keywordNode = keywordNode;
    }

    protected KeywordNode keywordNode;

    /**
     * The namespace expression on the right-hand-side of the equals sign.
     */
    protected ExpressionNodeBase expressionNode;

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.DefaultXMLStatementID;
    }

    @Override
    public int getChildCount()
    {
        return expressionNode != null ? 1 : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0)
            return expressionNode;
        
        return null;
    }
    
    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (expressionNode != null)
        {
            expressionNode.normalize(fillInOffsets);
            expressionNode.setParent(this);
        }
    }
    
    //
    // IDefaultXMLNamespaceNode implementations
    //
    
    @Override
    public KeywordNode getKeywordNode()
    {
        return keywordNode;
    }

    @Override
    public ExpressionNodeBase getExpressionNode()
    {
        return expressionNode;
    }

    //
    // Other methods
    //

    public void setExpressionNode(ExpressionNodeBase expressionNode)
    {
        if (expressionNode != null)
            this.expressionNode = expressionNode;
    }
}
