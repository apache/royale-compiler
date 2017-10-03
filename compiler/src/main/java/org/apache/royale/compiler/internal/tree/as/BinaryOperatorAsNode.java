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
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;

/**
 * Final subclass of {@link BinaryOperatorNodeBase} for the '<code>as</code>' operator.
 */
public final class BinaryOperatorAsNode extends BinaryOperatorNodeBase
{
    /**
     * Constructor.
     */
    public BinaryOperatorAsNode(IASToken operatorToken,
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
    protected BinaryOperatorAsNode(BinaryOperatorAsNode other)
    {
        super(other);
    }
    
    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.Op_AsID;
    }
    
    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        // The old compiler just says the type of 'a as B' is *.
        // We do better and say it is B if B is a type.
        IDefinition rightType = getRightOperandNode().resolve(project);
        if (rightType instanceof ITypeDefinition)
            return (ITypeDefinition)rightType;

        return project.getBuiltinType(BuiltinType.ANY_TYPE);
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    protected BinaryOperatorAsNode copy()
    {
        return new BinaryOperatorAsNode(this);
    }
    
    //
    // OperatorNode overrides
    //
    
    @Override
    public OperatorType getOperator()
    {
        return OperatorType.AS;
    }
}
