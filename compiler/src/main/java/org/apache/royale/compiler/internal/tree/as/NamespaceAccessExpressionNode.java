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

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.INamespaceAccessExpressionNode;

public class NamespaceAccessExpressionNode extends BinaryOperatorNodeBase implements INamespaceAccessExpressionNode
{
    /**
     * Constructor.
     * 
     * @param left the expression on the left of the member access (the object)
     * @param operator the ASToken holding the member access operator ("::")
     * @param right the expression on the right of the member access (the
     * member)
     */
    public NamespaceAccessExpressionNode(ExpressionNodeBase left, IASToken operator, ExpressionNodeBase right)
    {
        super(operator, left instanceof IdentifierNode ? new NamespaceIdentifierNode((IdentifierNode)left) : left, right);
        
        leftOperandNode.span(left.getAbsoluteStart(), left.getAbsoluteEnd(), left.getLine(), left.getColumn(), left.getEndLine(), left.getEndColumn());
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected NamespaceAccessExpressionNode(NamespaceAccessExpressionNode other)
    {
        super(other);
    }
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.NamespaceAccessExpressionID;
    }
    
    //
    // ExpressionNodeBase overrides
    //

    @Override
    public IDefinition resolve(ICompilerProject project)
    {
        return rightOperandNode.resolve(project);
    }

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        return rightOperandNode.resolveType(project);
    }

     @Override
    protected NamespaceAccessExpressionNode copy()
    {
        return new NamespaceAccessExpressionNode(this);
    }
    
    @Override
    boolean isQualifiedExpr(ExpressionNodeBase e)
    {
        return e == this.rightOperandNode;
    }

    @Override
    ExpressionNodeBase getQualifier(ExpressionNodeBase e)
    {
        if (e == this.rightOperandNode)
            return this.leftOperandNode;

        return null;
    }

    @Override
    boolean isAttributeExpr(ExpressionNodeBase e)
    {
        // Determine if e is part of an attribute expression; it is if e is the rhs
        // of the NamespaceAccessExpression, and the NamespaceAccessExpression is
        // part of an attribute expression
        if (e == this.rightOperandNode)
        {
            ExpressionNodeBase p = getParentExpression();
            if (p != null)
                return p.isAttributeExpr(this);
        }
        
        return false;
    }

    @Override
    boolean isPartOfMemberRef(ExpressionNodeBase e)
    {
        // Determine if the expression passed in is part of a Member Reference. In
        // this case, we return true if the expression is the rhs of the
        // NamesapceAccessExpression and the NamespaceAccessExpression is part of a
        // member reference. The node for b in a.c::b would return true if
        // passed to this method.

        if (e == this.rightOperandNode)
        {
            ExpressionNodeBase p = getParentExpression();
            if (p != null)
                return p.isPartOfMemberRef(this);
        }
        
        return false;
    }
    
    @Override
    ExpressionNodeBase getBaseForMemberRef(ExpressionNodeBase e)
    {
        // Get the ExpressionNodeBase representing the Base of a Member access
        // expression. This would be the Node for 'a' in the expression 'a.c::b',
        // assuming you asked for the base of 'b'.

        if (e == this.rightOperandNode)
        {
            ExpressionNodeBase p = getParentExpression();
            if (p != null)
                return p.getBaseForMemberRef(this);
        }
        
        return null;
    }

    @Override
    public INamespaceReference computeNamespaceReference()
    {
        return rightOperandNode.computeNamespaceReference();
    }

    //
    // OperatorNodebase overrides
    //

    @Override
    public OperatorType getOperator()
    {
        return OperatorType.NAMESPACE_ACCESS;
    }
}
