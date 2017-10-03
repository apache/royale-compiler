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

import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.ICompoundAssignmentNode;

/**
 * Final subclass of {@link BinaryOperatorNodeBase} for the '<code>>>>=</code>' operator.
 */
public final class BinaryOperatorBitwiseUnsignedRightShiftAssignmentNode extends BinaryOperatorBitwiseUnsignedRightShiftNode
    implements ICompoundAssignmentNode
{
    /**
     * Constructor.
     */
    public BinaryOperatorBitwiseUnsignedRightShiftAssignmentNode(IASToken operatorToken,
                                                                 ExpressionNodeBase leftOperand,
                                                                 ExpressionNodeBase rightOperand)
    {
        super(operatorToken, leftOperand, rightOperand);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected BinaryOperatorBitwiseUnsignedRightShiftAssignmentNode(BinaryOperatorBitwiseUnsignedRightShiftAssignmentNode other)
    {
        super(other);
    }

    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.Op_UnsignedRightShiftAssignID;
    }

    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        // The type of a >>>= b is the type of a.
        return resolveAssignmentType(project);
    }

    @Override
    protected BinaryOperatorBitwiseUnsignedRightShiftAssignmentNode copy()
    {
        return new BinaryOperatorBitwiseUnsignedRightShiftAssignmentNode(this);
    }

    //
    // OperatorNodeBase overrides
    //

    @Override
    public OperatorType getOperator()
    {
        return OperatorType.BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT;
    }
    
    //
    // ICompoundAssignmentNode implementations
    //
    
     @Override
    public ITypeDefinition resolveTypeOfRValue(ICompilerProject project)
    {
        // Use superclass to determine type of a >>> b.
        return super.resolveType(project);
    }
}
