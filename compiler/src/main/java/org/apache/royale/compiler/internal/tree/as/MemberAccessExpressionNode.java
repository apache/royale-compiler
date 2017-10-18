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

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;

/**
 * ActionScript parse tree node representing a member access expression of one
 * of the following types:
 * <ul>
 * <li><b>Member access</b> - {@code object.fieldName}</li>
 * <li><b>Query</b> - {@code object..descendantsName}</li>
 * <li><b>E4X filter</b> - {@code xmlObject.(expr)}</li>
 * </ul>
 */
public class MemberAccessExpressionNode extends BinaryOperatorNodeBase implements IMemberAccessExpressionNode
{
    /**
     * Constructor.
     * 
     * @param left the expression on the left of the member access (the object)
     * @param operatorToken the ASToken holding the member access operator (".")
     * @param right the expression on the right of the member access (the
     * member)
     */
    public MemberAccessExpressionNode(ExpressionNodeBase left, IASToken operatorToken, ExpressionNodeBase right)
    {
        super(operatorToken, left, right);
        
        if (operatorToken != null)
        {
            int tokenType = operatorToken.getType();
            if (tokenType == ASTokenTypes.TOKEN_OPERATOR_MEMBER_ACCESS)
            	operator = OperatorType.MEMBER_ACCESS;
            else if (tokenType == ASTokenTypes.TOKEN_OPERATOR_DESCENDANT_ACCESS)
            	operator = OperatorType.DESCENDANT_ACCESS;
            else
            	assert false : "Unexpected token '" + operatorToken.getText() + "' for MemberAccessExpressionNode";
        }
    }
    
    /**
     * Copy constructor.
     * 
     * @param other The node to copy.
     */
    public MemberAccessExpressionNode(MemberAccessExpressionNode other)
    {
        super(other);
        
        this.operator = other.operator;
    }

    private OperatorType operator = OperatorType.MEMBER_ACCESS;

    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        final ASTNodeID nodeID;
        if (getOperator() == OperatorType.DESCENDANT_ACCESS)
        {
            nodeID = ASTNodeID.Op_DescendantsID;
        }
        else if (rightOperandNode != null && rightOperandNode.hasParenthesis())
        {
            nodeID = ASTNodeID.E4XFilterID;
        }
        else
        {
            nodeID = ASTNodeID.MemberAccessExpressionID;
        }
        return nodeID;
    }
    
    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    public IDefinition resolve(ICompilerProject project)
    {
        // A reference to a package won't resolve to anything.
        if (isPackageReference())
            return null;

        // The definition that a member access expression resolves to will
        // always be the definition that its rightmost child resolves to
        // (e.g., the definition of c in a.b.c).
        return this.rightOperandNode.resolve(project);
    }

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        // If the node is a E4XFilterID, the resolved type is XMLList.
        // Otherwise, the resolved type is the same as its
        // right-hand-side expression.
        if (ASTNodeID.E4XFilterID == getNodeID())
            return project.getBuiltinType(IASLanguageConstants.BuiltinType.XMLLIST);
        else
            return rightOperandNode.resolveType(project);
    }
    
    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        // If this is a package reference, we're not dynamic.
        if (isPackageReference())
            return false;

        return super.isDynamicExpression(project);
    }

    @Override
    protected MemberAccessExpressionNode copy()
    {
        return new MemberAccessExpressionNode(this);
    }
    
    @Override
    public Name getMName(ICompilerProject project)
    {
        return rightOperandNode.getMName(project);
    }
    
    @Override
    String computeSimpleReference()
    {
        return getDisplayString();
    }

    @Override
    IReference computeTypeReference()
    {
        return rightOperandNode.computeTypeReference();
    }

    @Override
    public INamespaceReference computeNamespaceReference()
    {
        return rightOperandNode.computeNamespaceReference();
    }


    @Override
    boolean isPartOfMemberRef(ExpressionNodeBase e)
    {
        // If e is the rhs, then this it is always a member ref.
        // If e is the lhs, then it is a member ref
        // if the MemberAccessExpression is a member ref.
        if (e == this.rightOperandNode)
        {
            return true;
        }
        else if (e == this.leftOperandNode)
        {
            IASNode p = getParent();
            if (p instanceof ExpressionNodeBase)
                return ((ExpressionNodeBase)p).isPartOfMemberRef(this);
        }
        return false;
    }
    
    @Override
    ExpressionNodeBase getBaseForMemberRef(ExpressionNodeBase e)
    {
        // If e is the rhs, then the base is the lhs.
        // Of e is the lhs, then the base is determined by the parent.
        if (e == this.rightOperandNode)
        {
            return this.leftOperandNode;
        }
        else if (e == this.leftOperandNode)
        {
            ExpressionNodeBase p = getParentExpression();
            if (p != null)
                return p.getBaseForMemberRef(this);
        }
        return null;
    }

    //
    // OperatorNodeBase overrides
    //
    
    @Override
    public OperatorType getOperator()
    {
        return operator;
    }

    @Override
    public String getOperatorText()
    {
        switch (getNodeID())
        {
            case E4XFilterID:
                return ".()";
                
            default:
                return super.getOperatorText();
        }
    }
    
    //
    // IMemberAccessExpressionNode implementations
    //
    
    @Override
    public String getDisplayString()
    {
        StringBuilder builder = new StringBuilder();
        if (leftOperandNode != null && leftOperandNode instanceof IIdentifierNode)
        {
            builder.append(((IIdentifierNode)leftOperandNode).getName());
        }
        else if (leftOperandNode != null && leftOperandNode instanceof MemberAccessExpressionNode)
        {
            builder.append(((MemberAccessExpressionNode)leftOperandNode).getDisplayString());
        }
        if (builder.length() > 0)
            builder.append(".");
        if (rightOperandNode != null && rightOperandNode instanceof IIdentifierNode)
            builder.append(((IIdentifierNode)rightOperandNode).getName());
        return builder.toString();
    }

    //
    // Other methods
    //

    public boolean isSuper(ExpressionNodeBase node)
    {
        if (!(node instanceof ILanguageIdentifierNode))
            return false;

        ILanguageIdentifierNode idBase = (ILanguageIdentifierNode)node;
        if (idBase.getKind() != LanguageIdentifierKind.SUPER)
            return false;

        return true;
    }

    /**
     * Determine if the left side of this Node refers to a package name
     * 
     * @return true if the left side is a package name
     */
    public boolean stemIsPackage()
    {
        return leftOperandNode.isPackageReference();
    }

    /**
     * Tell the MemberAccessExpression that it's "stem" ('a.b' in 'a.b.Foo')
     * should always be treated as a package, regardless of whether it is
     * imported or not. This is needed by MXML because sometimes it does not
     * require users to import a package before using it as a package (such as
     * in MXMLClassNode).
     */
    public void setStemAsPackage(boolean b)
    {
        leftOperandNode.setTreatAsPackage(b);
    }

    /**
     *  Is the given node this node's member reference?
     *  @param node - the node of interest.
     *  @return true iff node is this node's member reference.
     */
    public boolean isMemberReference(IASNode node)
    {
        return node == this.rightOperandNode;
    }
}
