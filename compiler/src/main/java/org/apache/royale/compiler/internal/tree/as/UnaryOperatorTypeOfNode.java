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

/**
 * Final subclass of {@link UnaryOperatorNodeBase} for the '<code>typeof</code>' operator.
 */
public final class UnaryOperatorTypeOfNode extends UnaryOperatorNodeBase
{
    /**
     * Constructor.
     */
    public UnaryOperatorTypeOfNode(IASToken operatorToken, ExpressionNodeBase operand)
    {
        super(operatorToken, operand);
    }

    /**
     * Copy constructor.
     *
     * @param other the node to copy
     */
    protected UnaryOperatorTypeOfNode(UnaryOperatorTypeOfNode other)
    {
        super(other);
    }

    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.Op_TypeOfID;
    }
    
    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        return project.getBuiltinType(BuiltinType.STRING);
    }

    @Override
    protected UnaryOperatorTypeOfNode copy()
    {
        return new UnaryOperatorTypeOfNode(this);
    }

    //
    // OperatorNodeBase overrides
    //

    @Override
    public OperatorType getOperator()
    {
        return OperatorType.TYPEOF;
    }
}
