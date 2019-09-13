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
import org.apache.royale.compiler.tree.as.IIdentifierNode;

/**
 * Node for a fully qualified name (e.g. mx.core.Button) used in a package or
 * class definition. This is really just a MemberAccessExpressionNode where all
 * of the pieces are guaranteed to be simple IdentifierNodes.
 */
public class FullNameNode extends MemberAccessExpressionNode implements IIdentifierNode
{
    /**
     * If the {@code node} is a {@link FullNameNode}, recursively convert its
     * subtree into {@link MemberAccessExpressionNode}. Otherwise, return
     * {@code node}.
     * 
     * @param node Expression node.
     * @return If {@code node} is a {@link FullNameNode}, the result is a
     * {@link MemberAccessExpressionNode}; Otherwise, {@code node} is returned
     * unchanged.
     */
    public static ExpressionNodeBase toMemberAccessExpressionNode(final ExpressionNodeBase node)
    {
        if (node instanceof FullNameNode)
        {
            final FullNameNode fullnameNode = (FullNameNode)node;
            final ExpressionNodeBase left = toMemberAccessExpressionNode(fullnameNode.leftOperandNode);
            final ExpressionNodeBase right = toMemberAccessExpressionNode(fullnameNode.rightOperandNode);
            final MemberAccessExpressionNode result = new MemberAccessExpressionNode(left, null, right);
            result.operatorStart = fullnameNode.operatorStart;
            result.setHasParenthesis(fullnameNode.hasParenthesis());
            return result;
        }
        else
        {
            return node;
        }
    }

    /**
     * Constructor.
     * 
     * @param left the expression on the left of the member access (the object)
     * @param operator the ASToken holding the member access operator (".")
     * @param right the expression on the right of the member access (the
     * member)
     */
    public FullNameNode(ExpressionNodeBase left, IASToken operator, ExpressionNodeBase right)
    {
        super(left, operator, right);
        
        if (right != null)
        {
            setEnd(right.getAbsoluteEnd());
            setEndLine(right.getEndLine());
            setEndColumn(right.getEndColumn());
        }
    }

    protected FullNameNode(MemberAccessExpressionNode node)
    {
        super(node.leftOperandNode, null, node.rightOperandNode);
        
        operatorStart = node.operatorStart;
        setHasParenthesis(node.hasParenthesis());
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected FullNameNode(FullNameNode other)
    {
        super(other);
    }
    
    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.FullNameID;
    }
    
    /*
     * For debugging only.
     * Builds a string such as <code>"flash.display"</code>
     * from the full name.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getName());
        sb.append('"');

        return true;
    }

    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    protected FullNameNode copy()
    {
        return new FullNameNode(this);
    }
    
    @Override
    public String computeSimpleReference()
    {
        return getName();
    }

    //
    // BinaryOperatorNodeBase overrides
    //
    
    @Override
    // TODO Why does this class have to override this method
    // when others don't?
    public void setRightOperandNode(ExpressionNodeBase right)
    {
        super.setRightOperandNode(right);
        
        if (right != null)
        {
            setEnd(right.getAbsoluteEnd());
            setEndLine(right.getEndLine());
            setEndColumn(right.getEndColumn());
        }
    }
    
    //
    // MemberAccessExpressionNode overrides
    //
    
    @Override
    public String getName()
    {
        if (leftOperandNode != null && leftOperandNode != this &&
            (leftOperandNode instanceof IdentifierNode || leftOperandNode instanceof FullNameNode) &&
            rightOperandNode != null && rightOperandNode != this &&
            (rightOperandNode instanceof IdentifierNode || rightOperandNode instanceof FullNameNode))
        {
            StringBuilder builder = new StringBuilder();
            builder.append(((IIdentifierNode)leftOperandNode).getName());
            builder.append(".");
            builder.append(((IIdentifierNode)rightOperandNode).getName());
            return builder.toString();
        }

        // This shouldn't ever happen.
        return "";
    }

    // 
    // IIdentifierNode implementations
    //
    
    @Override
    public IdentifierType getIdentifierType()
    {
        return IdentifierType.FULLY_QUALIFIED_NAME;
    }
}
