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

import antlr.Token;

import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDynamicAccessNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;

/**
 * Abstract base class for all binary operator nodes.
 */
public abstract class BinaryOperatorNodeBase extends OperatorNodeBase implements IBinaryOperatorNode
{
    /**
     * Factory method for various kinds of binary operator nodes.
     */
    public static BinaryOperatorNodeBase create(IASToken operatorToken, ExpressionNodeBase leftOperand, ExpressionNodeBase rightOperand)
    {
        switch (operatorToken.getType())
        {
            // simple assignment
            
            case TOKEN_OPERATOR_ASSIGNMENT:
                return new BinaryOperatorAssignmentNode(operatorToken, leftOperand, rightOperand);
            
            // arithmetic
            
            case TOKEN_OPERATOR_PLUS:
                return new BinaryOperatorPlusNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_PLUS_ASSIGNMENT:
                return new BinaryOperatorPlusAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_MINUS:
                return new BinaryOperatorMinusNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_MINUS_ASSIGNMENT:
                return new BinaryOperatorMinusAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_STAR:
                return new BinaryOperatorMultiplicationNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_MULTIPLICATION_ASSIGNMENT:
                return new BinaryOperatorMultiplicationAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_DIVISION:
                return new BinaryOperatorDivisionNode(operatorToken, leftOperand, rightOperand);
            
            case TOKEN_OPERATOR_DIVISION_ASSIGNMENT:
                return new BinaryOperatorDivisionAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_MODULO:
                return new BinaryOperatorModuloNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_MODULO_ASSIGNMENT:
                return new BinaryOperatorModuloAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            // equality
                
            case TOKEN_OPERATOR_EQUAL:
                return new BinaryOperatorEqualNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_NOT_EQUAL:
                return new BinaryOperatorNotEqualNode(operatorToken, leftOperand, rightOperand);
                    
            case TOKEN_OPERATOR_STRICT_EQUAL:
                return new BinaryOperatorStrictEqualNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_STRICT_NOT_EQUAL:
                return new BinaryOperatorStrictNotEqualNode(operatorToken, leftOperand, rightOperand);
                
            // comparison
            
            case TOKEN_OPERATOR_LESS_THAN:
                return new BinaryOperatorLessThanNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_LESS_THAN_EQUALS:
                return new BinaryOperatorLessThanEqualsNode(operatorToken, leftOperand, rightOperand);
                    
            case TOKEN_OPERATOR_GREATER_THAN:
                return new BinaryOperatorGreaterThanNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_GREATER_THAN_EQUALS:
                return new BinaryOperatorGreaterThanEqualsNode(operatorToken, leftOperand, rightOperand);
                
            // logical
                
            case TOKEN_OPERATOR_LOGICAL_AND:
                return new BinaryOperatorLogicalAndNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_LOGICAL_AND_ASSIGNMENT:
                return new BinaryOperatorLogicalAndAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_LOGICAL_OR:
                return new BinaryOperatorLogicalOrNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_LOGICAL_OR_ASSIGNMENT:
                return new BinaryOperatorLogicalOrAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            // bitwise logical
                
            case TOKEN_OPERATOR_BITWISE_AND:
                return new BinaryOperatorBitwiseAndNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_BITWISE_AND_ASSIGNMENT:
                return new BinaryOperatorBitwiseAndAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_BITWISE_OR:
                return new BinaryOperatorBitwiseOrNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_BITWISE_OR_ASSIGNMENT:
                return new BinaryOperatorBitwiseOrAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_BITWISE_XOR:
                return new BinaryOperatorBitwiseXorNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_BITWISE_XOR_ASSIGNMENT:
                return new BinaryOperatorBitwiseXorAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            // shift
                
            case TOKEN_OPERATOR_BITWISE_LEFT_SHIFT:
                return new BinaryOperatorBitwiseLeftShiftNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_BITWISE_LEFT_SHIFT_ASSIGNMENT:
                return new BinaryOperatorBitwiseLeftShiftAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT:
                return new BinaryOperatorBitwiseRightShiftNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT_ASSIGNMENT:
                return new BinaryOperatorBitwiseRightShiftAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT:
                return new BinaryOperatorBitwiseUnsignedRightShiftNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                return new BinaryOperatorBitwiseUnsignedRightShiftAssignmentNode(operatorToken, leftOperand, rightOperand);
                
            // other
            
            case TOKEN_KEYWORD_AS:
                return new BinaryOperatorAsNode(operatorToken, leftOperand, rightOperand);
                
            case TOKEN_COMMA:
                return new BinaryOperatorCommaNode(operatorToken, leftOperand, rightOperand);
            
            case TOKEN_KEYWORD_IN:
                return new BinaryOperatorInNode(operatorToken, leftOperand, rightOperand);
            
            case TOKEN_KEYWORD_INSTANCEOF:
                return new BinaryOperatorInstanceOfNode(operatorToken, leftOperand, rightOperand);
            
            case TOKEN_KEYWORD_IS:
                return new BinaryOperatorIsNode(operatorToken, leftOperand, rightOperand);
        }
        
        assert false : "Token '" + operatorToken.getText() + "' unexpected in BinaryOperatorNodeBase.create()";
        return null;
    }
    
    /**
     * Constructor.
     * <p>
     * Creates a {@code BinaryOperatorNode} from an operator and both operands.
     * If either operand is {@code null}, we will synthesize
     * empty {@link IdentifierNode} to repair the tree.
     * 
     * @param op operator token
     * @param left first operand
     * @param right second operand
     */
    public BinaryOperatorNodeBase(IASToken op, ExpressionNodeBase left, ExpressionNodeBase right)
    {
        super(op);

        // Synthesize an empty ID node if either operand is null. (Fix for CMP-883)

        if (left == null)
            leftOperandNode = IdentifierNode.createEmptyIdentifierNodeAfterToken((Token)op);
        else
            leftOperandNode = left;

        if (right == null)
            rightOperandNode = IdentifierNode.createEmptyIdentifierNodeAfterToken((Token)op);
        else
            rightOperandNode = right;
        
        span(leftOperandNode, rightOperandNode);
    }
    
    /**
     * Copy constructor.
     * 
     * @param other The node to copy.
     */
    protected BinaryOperatorNodeBase(BinaryOperatorNodeBase other)
    {
        super(other);
        
        this.leftOperandNode = other.leftOperandNode != null ? other.leftOperandNode.copy() : null;
        this.rightOperandNode = other.rightOperandNode != null ? other.rightOperandNode.copy() : null;
    }

    /**
     * The expression to the left of the operator
     */
    protected ExpressionNodeBase leftOperandNode;

    /**
     * The expression to the right of the operator
     */
    protected ExpressionNodeBase rightOperandNode;
    
    //
    // NodeBase overrides
    //
    
    @Override
    public int getChildCount()
    {
        int count = 0;
        
        if (leftOperandNode != null)
            count++;
        
        if (rightOperandNode != null)
            count++;
        
        return count;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0)
            return leftOperandNode != null ? leftOperandNode : rightOperandNode;
        
        else if (i == 1)
            return rightOperandNode;
        
        return null;
    }
    
    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (leftOperandNode != null)
            leftOperandNode.setParent(this);

        if (rightOperandNode != null)
            rightOperandNode.setParent(this);
    }

    @Override
    protected void fillInOffsets()
    {
        if (rightOperandNode == null && leftOperandNode == null && operatorStart != -1)
            span(operatorStart, operatorStart + 1, -1, -1, -1, -1);
        else
            super.fillInOffsets();
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
    
    //
    // OperatorNodeBase overrides
    //
    
    @Override
    public ExpressionType getExpressionType()
    {
        return ExpressionType.BINARY;
    }
    
    //
    // IBinaryOperatorNode implementations
    //
    
    @Override
    public IExpressionNode getLeftOperandNode()
    {
        return leftOperandNode;
    }

    @Override
    public IExpressionNode getRightOperandNode()
    {
        return rightOperandNode;
    }

    //
    // Other methods
    //

    /**
     * Sets the left-hand side of the expression.
     * 
     * @param leftOperandNode Left-hand side expression.
     */
    public void setLeftOperandNode(ExpressionNodeBase leftOperandNode)
    {
        this.leftOperandNode = leftOperandNode;
    }

    /**
     * Sets the right side of the expression
     * 
     * @param rightOperandNode ExpressionNodeBase to the right of the operator
     */
    public void setRightOperandNode(ExpressionNodeBase rightOperandNode)
    {
        this.rightOperandNode = rightOperandNode;
    }
    
    public boolean isOperatingOnArray()
    {
        if (leftOperandNode instanceof IDynamicAccessNode || rightOperandNode instanceof IDynamicAccessNode)
            return true;
        
        if (leftOperandNode instanceof IBinaryOperatorNode)
        {
            boolean onArray = ((BinaryOperatorNodeBase)leftOperandNode).isOperatingOnArray();
            if (onArray)
                return true;
        }
        
        if (rightOperandNode instanceof IBinaryOperatorNode)
            return ((BinaryOperatorNodeBase)rightOperandNode).isOperatingOnArray();

        return false;
    }

    /**
     * Utility method called by {@link #resolveType(ICompilerProject)}
     * in subclasses for assignment operators.
     */
    protected ITypeDefinition resolveAssignmentType(ICompilerProject project)
    {
        // The type of any assignment expression is the type
        // of what we are assigning to.
        return getLeftOperandNode().resolveType(project);
    }

    /**
     * Utility method called by {@link #resolveType(ICompilerProject)}
     * in subclasses for the <code>&&</code> and <code>||</code> operators.
     */
    protected ITypeDefinition resolveLogicalType(ICompilerProject project)
    {
        // The old compiler says the type of && or || is one of the operand types,
        // but only checks if the types are equivalent.
        // TODO: Could probably be smarter - calculate common base class and use that as the type?
        ITypeDefinition leftType = getLeftOperandNode().resolveType(project);
        ITypeDefinition rightType = getRightOperandNode().resolveType(project);
        if (leftType != null && leftType.equals(rightType))
            return leftType;
        
        return project.getBuiltinType(BuiltinType.ANY_TYPE);
    }
}
