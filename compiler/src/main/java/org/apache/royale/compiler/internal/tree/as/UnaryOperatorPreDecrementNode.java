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

/**
 * Final subclass of {@link UnaryOperatorNodeBase} for the prefix '<code>--</code>' operator.
 */
public final class UnaryOperatorPreDecrementNode extends UnaryOperatorNodeBase
{
    /**
     * Constructor.
     */
    public UnaryOperatorPreDecrementNode(IASToken operatorToken, ExpressionNodeBase operand)
    {
        super(operatorToken, operand);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected UnaryOperatorPreDecrementNode(UnaryOperatorPreDecrementNode other)
    {
        super(other);
    }

    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.Op_PreDecrID;
    }
    
    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        return resolveIncrementOrDecrementType(project);
    }

    @Override
    protected UnaryOperatorPreDecrementNode copy()
    {
        return new UnaryOperatorPreDecrementNode(this);
    }

    //
    // OperatorNodeBase overrides
    //
    
    @Override
    public OperatorType getOperator()
    {
        return OperatorType.DECREMENT;
    }
    
    // TODO Shouldn't there be an override of getExpressionType()? 
}
