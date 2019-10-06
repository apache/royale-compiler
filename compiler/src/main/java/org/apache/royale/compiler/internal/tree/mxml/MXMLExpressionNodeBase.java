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

import java.util.EnumSet;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.mxml.MXMLDialect.TextParsingFlags;
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.ImplicitCoercionToUnrelatedTypeProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLExpressionNode;

abstract class MXMLExpressionNodeBase extends MXMLInstanceNode implements IMXMLExpressionNode
{
    protected static final EnumSet<TextParsingFlags> FLAGS = EnumSet.of(
            TextParsingFlags.ALLOW_BINDING,
            TextParsingFlags.ALLOW_COMPILER_DIRECTIVE);

    protected static enum ExpressionType
    {
        BOOLEAN,
        INT,
        UINT,
        NUMBER,
        STRING,
        CLASS,
        FUNCTION,
        REGEXP
    }

    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLExpressionNodeBase(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The LiteralNode or MXMLDataBindingNode which represents the ActionScript
     * value for this node.
     */
    private IASNode expressionNode;

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 ? expressionNode : null;
    }

    @Override
    public int getChildCount()
    {
        return expressionNode == null ? 0 : 1;
    }

    @Override
    public IASNode getExpressionNode()
    {
        return expressionNode;
    }

    public void setExpressionNode(NodeBase value)
    {
        this.expressionNode = value;
        if (value != null)
            value.setParent(this);
    }

    public abstract ExpressionType getExpressionType();

    /**
     * This initialization method is used when implicit <int> etc. nodes are
     * created, such as for property values.
     */
    public void initialize(MXMLTreeBuilder builder, ISourceLocation location,
                           String type, NodeBase expressionNode)
    {
        setLocation(location);
        setClassReference(builder.getProject(), type);
        setExpressionNode(expressionNode);
    }

    @Override
    protected void processChildWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                              IMXMLTextData text, MXMLNodeInfo info)
    {
        accumulateTextFragments(builder, text, info);
    }

    @Override
    protected void processChildNonWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                                 IMXMLTextData text, MXMLNodeInfo info)
    {
        info.hasDualContent = true;

        accumulateTextFragments(builder, text, info);
    }

    /**
     * Processes the source fragments that were gathered on the
     * {@link MXMLNodeInfo} from the child text units of a
     * <code>&lt;Boolean&gt;</code>, <code>&lt;int&gt;</code>,
     * <code>&lt;uint&gt;</code>, <code>&lt;Number&gt;</code>, or
     * <code>&lt;String&gt;</code> tag.
     * <p>
     * They get parsed to create a child node which is a databinding node, a
     * compiler directive node, or a literal node of the tag's type.
     */
    protected NodeBase createExpressionNodeFromFragments(MXMLTreeBuilder builder,
                                                         IMXMLTagData tag,
                                                         MXMLNodeInfo info,
                                                         Object defaultValue)
    {
        ITypeDefinition type = builder.getBuiltinType(getName());

        ISourceFragment[] fragments = info.getSourceFragments();

        ISourceLocation location = info.getSourceLocation();
        if (location == null)
            location = tag.getLocationOfChildUnits();

        MXMLClassDefinitionNode classNode =
                (MXMLClassDefinitionNode)getClassDefinitionNode();

        return builder.createExpressionNode(
                null, type, fragments, location, FLAGS, defaultValue, classNode);
    }

    /**
     * Processes the source fragments that were gathered on the
     * {@link MXMLNodeInfo} from the child text units of a
     * <code>&lt;Class&gt;</code>, <code>&lt;Function&gt;</code>, or
     * <code>&lt;RegExp&gt;</code> tag.
     * <p>
     * They get parsed to create the child node. TODO: Check for
     * databindings and compiler directives.
     */
    protected NodeBase parseExpressionNodeFromFragments(MXMLTreeBuilder builder,
                                                        IMXMLTagData tag,
                                                        MXMLNodeInfo info,
                                                        Object defaultValue)
    {
        ITypeDefinition type = builder.getBuiltinType(getName());

        ISourceFragment[] fragments = info.getSourceFragments();

        ISourceLocation location = info.getSourceLocation();
        if (location == null)
            location = tag.getLocationOfChildUnits();

        MXMLClassDefinitionNode classNode =
                (MXMLClassDefinitionNode)getClassDefinitionNode();

        return builder.parseExpressionNode(
                type, fragments, location, FLAGS, defaultValue, classNode, true);
    }

    protected void checkExpressionType(MXMLTreeBuilder builder, IDefinition expectedType)
    {
        IExpressionNode expressionNode = (IExpressionNode)getExpressionNode();
        if (expressionNode != null)
        {
            ICompilerProject project = builder.getProject();
            IDefinition exprType = expressionNode.resolveType(project);
            if (exprType != null)
            {
                if (!SemanticUtils.isValidTypeConversion(expectedType, exprType, project, builder.getCompilationUnit().isInvisible()))
                {
                    ICompilerProblem problem = new ImplicitCoercionToUnrelatedTypeProblem(
                            expressionNode, exprType.getQualifiedName(), expectedType.getQualifiedName());
                    builder.addProblem(problem);
                }
            }
        }
    }
}
