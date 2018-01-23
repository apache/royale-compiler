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

import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.ITernaryOperatorNode;

/**
 * AST node of a ternary expression.
 */
public class TernaryOperatorNode extends BinaryOperatorNodeBase implements ITernaryOperatorNode
{
    /**
     * Create a ternary node from its components.
     * 
     * @param op Ternary operator {@code ?}.
     * @param conditionalNode Conditional expression.
     */
    public TernaryOperatorNode(IASToken op, ExpressionNodeBase conditionalNode,
                               ExpressionNodeBase leftOperandNode,
                               ExpressionNodeBase rightOperandNode)
    {
        super(op, null, null);
        
        this.leftOperandNode = leftOperandNode;
        this.rightOperandNode = rightOperandNode;
        this.conditionalNode = conditionalNode;

        final IASNode lastChildren;
        if (rightOperandNode != null)
            lastChildren = rightOperandNode;
        else if (leftOperandNode != null)
            lastChildren = leftOperandNode;
        else
            lastChildren = conditionalNode;
        
        if (conditionalNode != null)
        {
            // This explicit call to NodeBase.fillInOffsets() is not good
            // practice. It's a workaround for CMP-1973. After the AST building
            // is normalized, we can take out this logic.
            if (conditionalNode.getStart() == UNKNOWN)
                conditionalNode.fillInOffsets();
            span(conditionalNode, lastChildren);
        }
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected TernaryOperatorNode(TernaryOperatorNode other)
    {
        super(other);
        
        this.conditionalNode = other.conditionalNode != null ? other.conditionalNode.copy() : null;
    }
    
    private ExpressionNodeBase conditionalNode;

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.TernaryExpressionID;
    }
    
    @Override
    public int getChildCount()
    {
        if (conditionalNode == null)
            return 0;

        else if (leftOperandNode == null)
            return 1;

        else if (rightOperandNode == null)
            return 2;

        else
            return 3;
    }

     @Override
    public IASNode getChild(int i)
    {
        switch (i)
        {
            case 0:
                return conditionalNode;

            case 1:
                return leftOperandNode;

            case 2:
                return rightOperandNode;
        }

        return null;
    }
     
    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (conditionalNode != null)
            conditionalNode.setParent(this);
         
        super.setChildren(fillInOffsets);
    }

    //
    // ExpressionNodeBase overrides
    //
     
    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        // The old compiler simply considered the type of (a ? b : c) to be *.
        // We'll do a bit better: if b and c have identical type
        // then we'll consider (a ? b : c) to be that type.
        // We should probably determine the common base type of b and c,
        // but if either or both types are interfaces rather than classes,
        // it isn't obvious what this means.
        ITypeDefinition leftType = getLeftOperandNode().resolveType(project);
        ITypeDefinition rightType = getRightOperandNode().resolveType(project);
        if (leftType == rightType)
            return leftType;

        return project.getBuiltinType(BuiltinType.ANY_TYPE);
    }

    @Override
    protected TernaryOperatorNode copy()
    {
        return new TernaryOperatorNode(this);
    }

    //
    // OperatorNodeBase overrides
    //

    @Override
    public OperatorType getOperator()
    {
        return OperatorType.CONDITIONAL;
    }
    
    //
    // ITernaryOperatorNode implementations
    //

    @Override
    public IExpressionNode getConditionalNode()
    {
        return conditionalNode;
    }
}
