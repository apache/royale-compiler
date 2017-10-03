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
import org.apache.royale.compiler.tree.as.IASNode;

/**
 * Represents the base type for expression statements, such as a throw statement
 * or a return statement
 */
public abstract class BaseStatementExpressionNode extends ExpressionNodeBase
{
    /**
     * Constructor.
     * 
     * @param token
     */
    protected BaseStatementExpressionNode(IASToken token)
    {
        if (token != null)
        {
            startBefore(token);
            endAfter(token);
        }
    }
    
    /**'
     * Copy constructor.
     * 
     * @param other The node to copy.
     */
    protected BaseStatementExpressionNode(BaseStatementExpressionNode other)
    {
        super(other);
        
        this.expressionNode = other.expressionNode.copy();
    }

    /**
     * The expression to be acted upon by this statement expression.
     */
    protected ExpressionNodeBase expressionNode;
    
    //
    // NodeBase overrides
    //

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
    
    //
    // Other methods
    //

    /**
     * Gets the expression contained within this statement.
     * This is the sole child node.
     */
    public ExpressionNodeBase getExpressionNode()
    {
        return expressionNode;
    }
    
    /**
     * Sets the expression that is acted upon by this statement.
     * 
     * @param returnExp The expression being set.
     */
    // TODO Rename to match getter.
    public void setStatementExpression(ExpressionNodeBase returnExp)
    {
        expressionNode = returnExp;
        if (expressionNode != null)
        {
            expressionNode.setParent(this);
            endAfter(expressionNode);
        }
    }
}
