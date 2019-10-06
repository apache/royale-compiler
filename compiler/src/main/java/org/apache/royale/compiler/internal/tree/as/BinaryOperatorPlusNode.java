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
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;

/**
 * Subclass of {@link BinaryOperatorNodeBase} for the '<code>+</code>' operator.
 */
public class BinaryOperatorPlusNode extends BinaryOperatorNodeBase
{
    /**
     * Constructor.
     */
    public BinaryOperatorPlusNode(IASToken operatorToken,
                                  ExpressionNodeBase leftOperand,
                                  ExpressionNodeBase rightOperand)
    {
        super(operatorToken, leftOperand, rightOperand);
    }

    /**
     * Copy constructor.
     *
     * @param other the node to copy
     */
    protected BinaryOperatorPlusNode(BinaryOperatorPlusNode other)
    {
        super(other);
    }

    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.Op_AddID;
    }
    
    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        ITypeDefinition leftType = getLeftOperandNode().resolveType(project);
        ITypeDefinition rightType = getRightOperandNode().resolveType(project);

        // If we're adding numeric values (Number, int, or uint),
        // then the result is Number.
        if (isNumericType(leftType, project) && isNumericType(rightType, project))
            return project.getBuiltinType(BuiltinType.NUMBER);
        
        // If one of our operands is String,
        // then the result is String.
        ITypeDefinition stringType = project.getBuiltinType(BuiltinType.STRING);
        if (stringType.equals(leftType) || stringType.equals(rightType))
            return stringType;
        
        // If we're adding two XML-ish (i.e., XML or XMLList) objects,
        // then the result is XMLList.
        if (SemanticUtils.isXMLish(leftType, project) &&
            SemanticUtils.isXMLish(rightType, project))
        {
            return project.getBuiltinType(BuiltinType.XMLLIST);
        }
        
        // Otherwise, the result is *.
        return project.getBuiltinType(BuiltinType.ANY_TYPE);
    }

    @Override
    protected BinaryOperatorPlusNode copy()
    {
        return new BinaryOperatorPlusNode(this);
    }

    //
    // OperatorNodeBase overrides
    //
    
    @Override
    public OperatorType getOperator()
    {
        return OperatorType.PLUS;
    }
    
    //
    // Other methods
    //
    
    /**
     * Determines if a specified type is either <code>Number</code>,
     * <code>int</code>, or <code>uint</code>.
     * 
     * @param type An {@link ITypeDefinition} specifying a type.
     * @return <code>true</code> if <code>type</code> is the
     * <code>ITypeDefinition</code> for <code>Number</code>,
     * <code>int</code>, or <code>uint</code>.
     */
    private boolean isNumericType(ITypeDefinition type, ICompilerProject project)
    {
        if (project.getBuiltinType(BuiltinType.NUMBER).equals(type))
            return true;
    
        if (project.getBuiltinType(BuiltinType.INT).equals(type))
            return true;
    
        if (project.getBuiltinType(BuiltinType.UINT).equals(type))
            return true;
    
        return false;
    }
}
