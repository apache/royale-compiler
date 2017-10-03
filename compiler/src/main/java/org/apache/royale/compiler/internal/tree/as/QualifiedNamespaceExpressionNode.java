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
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;

public class QualifiedNamespaceExpressionNode extends FullNameNode implements INamespaceDecorationNode
{
    /**
     * Constructor.
     * 
     * @param left The expression to the left of the operator.
     * @param operator The token representing the operator.
     * @param right The expressoin to the right of the operator.
     */
    public QualifiedNamespaceExpressionNode(ExpressionNodeBase left, IASToken operator, ExpressionNodeBase right)
    {
        super(left, operator, right);
    }

    /**
     * Copy constructor.
     * 
     * @param child The node to copy.
     */
    public QualifiedNamespaceExpressionNode(MemberAccessExpressionNode child)
    {
        super(child);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected QualifiedNamespaceExpressionNode(QualifiedNamespaceExpressionNode other)
    {
        super(other);
    }
    
    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.QualifiedNamespaceExpressionID;
    }
    
    @Override
    public IASNode getParent()
    {
        return (parent == null) ? null : parent.getParent();
    }
    
    @Override
    // TODO This seems strange.
    public void setParent(NodeBase parent)
    {
        if (this.parent instanceof IDefinitionNode && !(parent instanceof IDefinitionNode)) //we have a "parent" already
            return;
        
        super.setParent(parent);
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    protected QualifiedNamespaceExpressionNode copy()
    {
        return new QualifiedNamespaceExpressionNode(this);
    }
    
    //
    // INamespaceDecorationNode implementations
    //

    @Override
    public IDefinitionNode getDecoratedDefinitionNode()
    {
        if (parent instanceof IDefinitionNode)
            return (IDefinitionNode)parent;
        
        return null;
    }

    @Override
    public NamespaceDecorationKind getNamespaceDecorationKind()
    {
        return NamespaceDecorationKind.QUALIFIED_NAME;
    }
    
    //
    // Other methods
    //

    public void setDecorationTarget(IDefinitionNode decoratingParent)
    {
        if (rightOperandNode instanceof NamespaceIdentifierNode)
            ((NamespaceIdentifierNode)rightOperandNode).setDecorationTarget(decoratingParent);
        
        // To save space, use the parent slot for the definition we are decorating.
        // We can use that to resolve our actual parent.
        this.parent = decoratingParent;
    }

    public boolean isExpressionQualifier()
    {
        IASNode p = getParent();
        if( p instanceof NamespaceAccessExpressionNode )
        {
            return ((NamespaceAccessExpressionNode) p).getLeftOperandNode() == this;
        }
        return false;
    }
}
