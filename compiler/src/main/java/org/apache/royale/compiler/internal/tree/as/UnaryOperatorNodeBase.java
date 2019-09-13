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

import static org.apache.royale.compiler.internal.parsing.as.ASTokenTypes.*;

import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IOperatorNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;

/**
 * Abstract base class for all unary operator nodes.
 */
public abstract class UnaryOperatorNodeBase extends OperatorNodeBase implements IUnaryOperatorNode
{
    /**
     * Factory method for various kinds of unary prefix operator nodes.
     */
    public static UnaryOperatorNodeBase createPrefix(IASToken operatorToken, ExpressionNodeBase operand)
    {
        switch (operatorToken.getType())
        {
            case TOKEN_OPERATOR_PLUS:
                return new UnaryOperatorPlusNode(operatorToken, operand);
                
            case TOKEN_OPERATOR_MINUS:
                return new UnaryOperatorMinusNode(operatorToken, operand);
                
            case TOKEN_OPERATOR_INCREMENT:
                return new UnaryOperatorPreIncrementNode(operatorToken, operand);
                
            case TOKEN_OPERATOR_DECREMENT:
                return new UnaryOperatorPreDecrementNode(operatorToken, operand);
                
            case TOKEN_OPERATOR_LOGICAL_NOT:
                return new UnaryOperatorLogicalNotNode(operatorToken, operand);
                
            case TOKEN_OPERATOR_BITWISE_NOT:
                return new UnaryOperatorBitwiseNotNode(operatorToken, operand);
                
            case TOKEN_OPERATOR_ATSIGN:
                return new UnaryOperatorAtNode(operatorToken, operand);

            case TOKEN_KEYWORD_DELETE:
                return new UnaryOperatorDeleteNode(operatorToken, operand);
                
            case TOKEN_KEYWORD_TYPEOF:
                return new UnaryOperatorTypeOfNode(operatorToken, operand);
                
            case TOKEN_KEYWORD_VOID:
                return new UnaryOperatorVoidNode(operatorToken, operand);  
        }
        
        assert false : "Token '" + operatorToken.getText() + "' unexpected in UnaryOperatorNodeBase.createPrefix()";
        return null;
    }

    /**
     * Factory method for various kinds of unary prefix operator nodes.
     */
    public static UnaryOperatorNodeBase createPostfix(IASToken operatorToken, ExpressionNodeBase operand)
    {
        switch (operatorToken.getType())
        {
            case TOKEN_OPERATOR_INCREMENT:
                return new UnaryOperatorPostIncrementNode(operatorToken, operand);
                
            case TOKEN_OPERATOR_DECREMENT:
                return new UnaryOperatorPostDecrementNode(operatorToken, operand);                
        }
        
        assert false : "Token '" + operatorToken.getText() + "' unexpected in UnaryOperatorNodeBase.createPostfix()";
        return null;
    }

    /**
     * Constructor.
     * 
     * @param operatorToken The token representing the unary operator.
     * @param operandNode The expresson node representing the operand.
     */
    public UnaryOperatorNodeBase(IASToken operatorToken, ExpressionNodeBase operandNode)
    {
        super(operatorToken);
        this.operandNode = operandNode;
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected UnaryOperatorNodeBase(UnaryOperatorNodeBase other)
    {
        super(other);
        this.operandNode = other.operandNode != null ? other.operandNode.copy() : null;
    }

    private ExpressionNodeBase operandNode;

    //
    // NodeBase overrides
    //
    
    @Override
    public int getChildCount()
    {
        return operandNode != null ? 1 : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 ? operandNode : null;
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (operandNode != null)
            operandNode.setParent(this);
    }
    
    @Override
    protected void fillInOffsets()
    {
        if (operandNode == null && operatorStart != -1)
        {
            span(operatorStart, operatorStart + 1, -1, -1, -1, -1);
        }
        else
        {
            super.fillInOffsets();
        }
    }

    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        // Various subclasses override this default behavior.
        return project.getBuiltinType(BuiltinType.ANY_TYPE);
    }
    
    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        return false;
    }

    /**
     * Determine if e is part of an attribue expression (@name).
     * 
     * @param e the expression to test
     * @return true if e if the sub-expr of the unary expression, and the
     * operator is '@'
     */
    @Override
    boolean isAttributeExpr(ExpressionNodeBase e)
    {
        return this.getOperator() == IOperatorNode.OperatorType.AT &&
               e == this.getOperandNode();
    }

    /*
     * Get the ExpressionNodeBase representing the Base of the operand of an @
     * operator. This would be the Node for 'a' in the expression 'a.@b',
     * assuming you asked for the base of 'b'.
     * 
     * @param e The ExpressionNodeBase representing b.
     * @return The ExpressionNodeBase representing the base expression of e, or null
     * if there is no base
     */
    @Override
    ExpressionNodeBase getBaseForMemberRef(ExpressionNodeBase e)
    {
        if (isAttributeExpr(e))
        {
            ExpressionNodeBase p = getParentExpression();
            if (p != null)
                return p.getBaseForMemberRef(this);
        }
        
        return null;
    }
    //
    // OperatorNodeBase overrides
    //
    
    @Override
    public ExpressionType getExpressionType()
    {
        // Most unary operators are prefix.
        // The two that aren't (a++ and a--) override this method to return POSTFIX.
        return ExpressionType.PREFIX;
    }
    
    //
    // IUnaryOperatorNode implementations
    //
    
    @Override
    public IExpressionNode getOperandNode()
    {
        return operandNode;
    }
    
    //
    // Other methods
    //

    public void setExpression(ExpressionNodeBase operandNode)
    {
        this.operandNode = operandNode;
    }

    /**
     * Utility method called by {@link #resolveType(ICompilerProject)} in subclasses
     * for the <code>++</code> and <code>--</code> operators.
     */
    protected ITypeDefinition resolveIncrementOrDecrementType(ICompilerProject project)
    {
        ITypeDefinition exprType = operandNode.resolveType(project);
        
        if (exprType != null)
        {
            ITypeDefinition intType = project.getBuiltinType(BuiltinType.INT);
            ITypeDefinition uintType = project.getBuiltinType(BuiltinType.UINT);

            if (exprType.equals(intType))
                return intType;
            else if (exprType.equals(uintType))
                return uintType;
        }
        
        return project.getBuiltinType(BuiltinType.NUMBER);
    }
}
