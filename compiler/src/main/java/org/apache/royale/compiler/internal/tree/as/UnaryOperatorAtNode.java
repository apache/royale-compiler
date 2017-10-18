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
import org.apache.royale.compiler.tree.ASTNodeID;

/**
 * Final subclass of {@link UnaryOperatorNodeBase} for the '<code>@</code>' operator.
 */
public final class UnaryOperatorAtNode extends UnaryOperatorNodeBase
{
    /**
     * Constructor.
     */
    public UnaryOperatorAtNode(IASToken operatorToken, ExpressionNodeBase operand)
    {
        super(operatorToken, operand);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected UnaryOperatorAtNode(UnaryOperatorAtNode other)
    {
        super(other);
    }

    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.Op_AtID;
    }
    
    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    protected UnaryOperatorAtNode copy()
    {
        return new UnaryOperatorAtNode(this);
    }

    @Override
    boolean isPartOfMemberRef(ExpressionNodeBase e)
    {
        if (e == this.getOperandNode())
        {
            // Return true if the node passed in
            // is this node's expression node,
            // and this node is part of a member ref.
            // This is to handle cases like:
            //     a.@foo
            ExpressionNodeBase expr = getParentExpression();
            if (expr != null)
                return expr.isPartOfMemberRef(this);
        }
        return false;
    }

    @Override
    boolean isQualifiedExpr(ExpressionNodeBase e)
    {
        if (e == this.getOperandNode())
        {
            // Return true if the node passed in
            // is this node's expression node,
            // and this node is a qualified expression.
            // This is to handle cases like:
            //    a.ns::@foo
            ExpressionNodeBase expr = getParentExpression();
            if (expr != null)
                return expr.isQualifiedExpr(this);
        }
        return false;
    }

    //
    // OperatorNodeBase overrides
    //
    
    @Override
    public OperatorType getOperator()
    {
        return OperatorType.AT;
    }
}
