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

import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IConditionalNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;

/**
 * Conditional node, that contains a condition to be met and a block to execute
 * if the condition is met
 */
public class ConditionalNode extends BaseStatementNode implements IConditionalNode
{
    /**
     * Constructor.
     */
    public ConditionalNode(IASToken keyword)
    {
        super();
        
        if (keyword != null)
            startBefore(keyword);
    }

    /**
     * The conditional node of this statement
     */
    protected ExpressionNodeBase conditionalNode;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.ConditionalID;
    }

    @Override
    public int getChildCount()
    {
        int count = 0;
        
        if (conditionalNode != null)
            count++;
        
        if (contentsNode != null)
            count++;
        
        return count;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0 && conditionalNode != null)
            return conditionalNode;
        
        else if (i == 0 || (i == 1 && conditionalNode != null))
            return contentsNode;
        
        return null;
    }
    
    //
    // IConditionalNode implementations
    //
    
    @Override
    public IExpressionNode getConditionalExpressionNode()
    {
        return conditionalNode;
    }

    //
    // Other methods
    //

    // TODO Rename to match getter
    public void setConditionalExpression(ExpressionNodeBase conditionalNode)
    {
        this.conditionalNode = conditionalNode;
    }

    // TODO Eliminate this.
    public ExpressionNodeBase getConditionalNode()
    {
        return conditionalNode;
    }
}
