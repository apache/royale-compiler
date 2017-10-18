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

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IVariableExpressionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;

/**
 * Expression that represents a variable contained within an iterative loop.
 * These loops can contain "in" statements, such as:
 * <code> for(var x:* in myArray)
 */
public class VariableExpressionNode extends ExpressionNodeBase implements IVariableExpressionNode
{
    /**
     * Constructor.
     * 
     * @param variableNode The node representing the variable declaration.
     */
    public VariableExpressionNode(VariableNode variableNode)
    {
        super();
        this.variableNode = variableNode;
        variableNode.setParent(this);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected VariableExpressionNode(VariableExpressionNode other)
    {
        super(other);
    }

    /**
     * The variable that is contained within this expression
     */
    protected VariableNode variableNode = null;

    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.VariableExpressionID;
    }

    @Override
    public int getChildCount()
    {
        return variableNode != null ? 1 : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0)
            return variableNode;
        
        return null;
    }

    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    protected VariableExpressionNode copy()
    {
        return new VariableExpressionNode(this);
    }

    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        if (variableNode != null &&
            IASLanguageConstants.DYNAMIC_TYPES_SET.contains(variableNode.getQualifiedName()))
        {
            return true;
        }
        
        return false;
    }
    
    //
    // IVariableExpressionNode implementation
    //
    
    @Override
    public IVariableNode getTargetVariable()
    {
        return variableNode;
    }
    
    //
    // Other methods
    //

    /**
     * Sets the target variable found within this expression. Used during
     * parsing
     * 
     * @param variableNode a variable node
     */
    public void setTargetVariable(VariableNode variableNode)
    {
        this.variableNode = variableNode;
    }
}
