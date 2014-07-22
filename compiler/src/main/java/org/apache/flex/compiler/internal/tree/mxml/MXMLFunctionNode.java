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

package org.apache.flex.compiler.internal.tree.mxml;

import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.tree.as.NodeBase;
import org.apache.flex.compiler.mxml.IMXMLTagData;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFunctionNode;

/**
 * Implementation of the {@code IMXMLFunctionNode} interface.
 */
class MXMLFunctionNode extends MXMLExpressionNodeBase implements IMXMLFunctionNode
{
    private static final Object DEFAULT = null;

    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLFunctionNode(NodeBase parent)
    {
        super(parent);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLFunctionID;
    }

    @Override
    public String getName()
    {
        return IASLanguageConstants.Function;
    }

    @Override
    public IFunctionDefinition getValue(ICompilerProject project)
    {
        assert getExpressionNode() instanceof IExpressionNode : "getValue() shouldn't be getting called on a non-expression MXMLFunctionNode";

        IExpressionNode expressionNode = (IExpressionNode)getExpressionNode();

        if (expressionNode != null)
        {
            IDefinition d = expressionNode.resolve(project);
            if (d instanceof IFunctionDefinition)
                return (IFunctionDefinition)d;
        }

        return null;
    }

    @Override
    public ExpressionType getExpressionType()
    {
        return ExpressionType.FUNCTION;
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder,
                                          IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        NodeBase expressionNode = parseExpressionNodeFromFragments(builder, tag, info, DEFAULT);
        setExpressionNode(expressionNode);

        ITypeDefinition functionType = builder.getBuiltinType(IASLanguageConstants.Function);
        checkExpressionType(builder, functionType);

        super.initializationComplete(builder, tag, info);
    }
}
