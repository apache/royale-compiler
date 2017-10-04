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

package org.apache.royale.compiler.internal.tree.mxml;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassNode;

/**
 * This AST node represents an MXML &lt;Class&gt; tag.
 * <p>
 * An {@code IMXMLClassNode} has exactly one child node: an
 * {@code IExpressionNode} representing a {@code Class} value. It will be either
 * an {@code ILiteralNode} or an {@code IMXMLDataBindingExpressionNode}.
 */
class MXMLClassNode extends MXMLExpressionNodeBase implements IMXMLClassNode
{
    private static final Object DEFAULT = null;

    /**
     * Constructor.
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLClassNode(NodeBase parent)
    {
        super(parent);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLClassID;
    }

    @Override
    public String getName()
    {
        return IASLanguageConstants.Class;
    }

    @Override
    public ITypeDefinition getValue(ICompilerProject project)
    {
        assert getExpressionNode() instanceof IExpressionNode : "getValue() shouldn't be getting called on a non-expression MXMLClassNode";

        IExpressionNode expressionNode = (IExpressionNode)getExpressionNode();

        if (expressionNode != null)
        {
            IDefinition d = expressionNode.resolve(project);
            if (d instanceof ITypeDefinition)
                return (ITypeDefinition)d;
        }

        return null;
    }

    @Override
    public ExpressionType getExpressionType()
    {
        return ExpressionType.CLASS;
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        NodeBase expressionNode = parseExpressionNodeFromFragments(builder, tag, info, DEFAULT);
        setExpressionNode(expressionNode);

        // Class tags don't require importing the package
        if (expressionNode instanceof MemberAccessExpressionNode)
            ((MemberAccessExpressionNode)expressionNode).setStemAsPackage(true);

        // TODO: re-enable - turned off because of CMP-891, 
        // TODO: we don't get the right type back for references to Classes
        //        ITypeDefinition classType = builder.getBuiltinType(IASLanguageConstants.Class);
        //        checkExpressionType(builder, classType);

        super.initializationComplete(builder, tag, info);
    }

    protected void initializeFromFragments(MXMLTreeBuilder builder,
                                           ISourceLocation location,
                                           ISourceFragment[] fragments)
    {
        setLocation(location);

        RoyaleProject project = builder.getProject();
        setClassReference(project, IASLanguageConstants.Class);

        ITypeDefinition type = builder.getBuiltinType(getName());

        MXMLClassDefinitionNode classNode =
                (MXMLClassDefinitionNode)getClassDefinitionNode();

        NodeBase expressionNode = builder.parseExpressionNode(
                type, fragments, location, FLAGS, DEFAULT, classNode, true);
        setExpressionNode(expressionNode);

        // The class tag does not require importing the package
        if (expressionNode instanceof MemberAccessExpressionNode)
            ((MemberAccessExpressionNode)expressionNode).setStemAsPackage(true);

        // TODO: re-enable - turned off because of CMP-891, 
        // TODO: we don't get the right type back for references to Classes
        //      ITypeDefinition classType = builder.getBuiltinType(IASLanguageConstants.Class);
        //      checkExpressionType(builder, classType);
    }
    
    @Override
    public void initialize(MXMLTreeBuilder builder, ISourceLocation location,
                           String type, NodeBase expressionNode)
    {
        super.initialize(builder, location, type, expressionNode);

        // The class tag does not require importing the package
        if (expressionNode instanceof MemberAccessExpressionNode)
            ((MemberAccessExpressionNode)expressionNode).setStemAsPackage(true);
    }
}
