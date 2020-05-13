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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.mxml.MXMLDialect.TextParsingFlags;
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.parsing.SourceFragmentsReader;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLIncompatibleArrayElementProblem;
import org.apache.royale.compiler.problems.MXMLUnexpectedTagProblem;
import org.apache.royale.compiler.problems.MXMLUnresolvedTagProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;

/**
 * This AST node represents an MXML &lt;Array&gt; tag.
 */
class MXMLArrayNode extends MXMLInstanceNode implements IMXMLArrayNode
{
    protected static final EnumSet<TextParsingFlags> FLAGS = EnumSet.of(
            TextParsingFlags.ALLOW_BINDING,
            TextParsingFlags.ALLOW_COMPILER_DIRECTIVE);

    /**
     * Constructor.
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLArrayNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * Caches the name of the property being set, if this array node is the
     * value of a property node.
     */
    private String propertyName;

    /**
     * Caches the type specified by [ArrayElementType] metadata in the case that
     * this array node is the value of a property node.
     */
    private String arrayElementType;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLArrayID;
    }

    @Override
    public String getName()
    {
        return IASLanguageConstants.Array;
    }

    @Override
    protected final void initializeFromTag(MXMLTreeBuilder builder,
                                           IMXMLTagData tag)
    {
        // If this array node is the value of a property node,
        // cache the propertName and type specified by [ArrayElementType]
        // metadata on the property, so that we can report problems
        // with incompatible elements.
        IMXMLNode parent = (IMXMLNode)getParent();
        if (parent instanceof IMXMLPropertySpecifierNode)
        {
            propertyName = ((IMXMLPropertySpecifierNode)parent).getName();
            IVariableDefinition propertyDefinition =
                    (IVariableDefinition)((IMXMLPropertySpecifierNode)parent).getDefinition();
            // propertyDefinition will be null in the case of a property of an <Object> tag
            RoyaleProject project = builder.getProject();
            arrayElementType = propertyDefinition != null ?
            		           propertyDefinition.getArrayElementType(project) :
            		           IASLanguageConstants.Object;
        }

        super.initializeFromTag(builder, tag);
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag,
                                   MXMLNodeInfo info)
    {
        // Process fragments gathered since the last child tag.
        processFragments(builder, info);

        // report problem to keep script tags out of array tags
        if (builder.getFileScope().isScriptTag(childTag))
        {
            builder.addProblem(new MXMLUnexpectedTagProblem(childTag));
            return;
        }

        // report problem if it isn't an instance

        RoyaleProject project = builder.getProject();

        String tagName = builder.getFileScope().resolveTagToQualifiedName(childTag);
        IDefinition definition = project.getScope().findDefinitionByName(tagName);
        if (definition instanceof IClassDefinition)
        {
            MXMLInstanceNode instanceNode =
                    MXMLInstanceNode.createInstanceNode(builder, tagName, this);
            instanceNode.setClassReference(project, (IClassDefinition)definition); // TODO Move this logic to initializeFromTag().
            instanceNode.initializeFromTag(builder, childTag);
            info.addChildNode(instanceNode);

            // Report problem if actual type of array element is incompatible with
            // the [ArrayElementType] of the property of type Array that's being set.
            if (arrayElementType != null)
            {
                if (!((IClassDefinition)definition).isInstanceOf(arrayElementType, project))
                {
                    ICompilerProblem problem = new MXMLIncompatibleArrayElementProblem(
                            childTag, propertyName, arrayElementType, definition.getQualifiedName());
                    builder.addProblem(problem);
                }
            }
        }
        else
        {
            String uri = childTag.getURI();
            if (uri != null && uri.equals(IMXMLLanguageConstants.NAMESPACE_MXML_2009))
            {
                MXMLInstanceNode instanceNode = MXMLInstanceNode.createInstanceNode(
                        builder, childTag.getShortName(), this);
                instanceNode.setClassReference(project, childTag.getShortName());
                instanceNode.initializeFromTag(builder, childTag);
                info.addChildNode(instanceNode);
            }
            else
            {
                builder.addProblem(new MXMLUnresolvedTagProblem(childTag));
                return;
            }
        }
    }

    @Override
    protected void processChildWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                              IMXMLTextData text,
                                              MXMLNodeInfo info)
    {
        accumulateTextFragments(builder, text, info);
    }

    @Override
    protected void processChildNonWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                                 IMXMLTextData text,
                                                 MXMLNodeInfo info)
    {
        accumulateTextFragments(builder, text, info);
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        // Process fragments gathered since the last child tag.
        processFragments(builder, info);

        setChildren(info.getChildNodeList().toArray(new MXMLInstanceNode[0]));
    }

    private void processFragments(MXMLTreeBuilder builder, MXMLNodeInfo info)
    {
        ISourceFragment[] fragments = info.getSourceFragments();
        String text = SourceFragmentsReader.concatLogicalText(fragments);
        if (!builder.getMXMLDialect().isWhitespace(text))
        {
            ITypeDefinition type = builder.getBuiltinType(IASLanguageConstants.ANY_TYPE);
            SourceLocation location = info.getSourceLocation();
            MXMLClassDefinitionNode classNode =
                    (MXMLClassDefinitionNode)getClassDefinitionNode();

            MXMLInstanceNode instanceNode =
                    builder.createInstanceNode(this, type, fragments, location, FLAGS, classNode);
            info.addChildNode(instanceNode);
        }

        info.clearFragments();
    }

    void initializeDefaultProperty(MXMLTreeBuilder builder, IVariableDefinition defaultPropertyDefinition,
                                   List<IMXMLUnitData> contentUnits)
    {
        RoyaleProject project = builder.getProject();

        // Set the location of the implicit array node
        // to span the tags that specify the default property value.
        setLocation(builder, contentUnits);

        setClassReference(project, IASLanguageConstants.Array);

        propertyName = defaultPropertyDefinition.getBaseName();
        arrayElementType = defaultPropertyDefinition.getArrayElementType(project);

        List<IMXMLNode> children = new ArrayList<IMXMLNode>();
        for (IMXMLUnitData unit : contentUnits)
        {
            if (unit instanceof IMXMLTagData)
            {
                IMXMLTagData tag = (IMXMLTagData)unit;
                // While it is normally illegal to put
                // script tags in an array, a default property
                // tag sequence can contain script nodes which need
                // to be children of the implicit array node to
                // keep the tree in file offset order.
                // See: http://bugs.adobe.com/jira/browse/CMP-955
                if (builder.getFileScope().isScriptTag(tag))
                {
                    MXMLScriptNode scriptNode = new MXMLScriptNode(this);
                    scriptNode.initializeFromTag(builder, tag);
                    children.add(scriptNode);
                }
                else
                {
                    IDefinition definition = builder.getFileScope().resolveTagToDefinition(tag);
                    if (definition instanceof IClassDefinition)
                    {
                        MXMLInstanceNode childNode = MXMLInstanceNode.createInstanceNode(
                                builder, definition.getQualifiedName(), this);
                        childNode.setClassReference(project, (IClassDefinition)definition); // TODO Move this logic to initializeFromTag().
                        childNode.initializeFromTag(builder, tag);
                        children.add(childNode);

                        // Report problem if actual type of array element is incompatible with
                        // the [ArrayElementType] of the property of type Array that's being set.
                        if (arrayElementType != null)
                        {
                            if (!((IClassDefinition)definition).isInstanceOf(arrayElementType, project))
                            {
                                ICompilerProblem problem = new MXMLIncompatibleArrayElementProblem(
                                    tag, propertyName, arrayElementType, definition.getQualifiedName());
                                builder.addProblem(problem);
                            }
                        }
                    }
                    else
                    {
                        builder.getProblems().add(new MXMLUnresolvedTagProblem(tag));
                    }
                }
            }
        }
        setChildren(children.toArray(new IMXMLNode[0]));
    }

    public void initialize(MXMLTreeBuilder builder, ISourceLocation location,
                           List<?> value)
    {
        setLocation(location);
        setClassReference(builder.getProject(), IASLanguageConstants.Array);

        List<IMXMLNode> childList = new ArrayList<IMXMLNode>();

        for (Object element : value)
        {
            if (element instanceof Boolean)
            {
                MXMLBooleanNode booleanNode = new MXMLBooleanNode(this);
                MXMLLiteralNode literalNode = new MXMLLiteralNode(booleanNode, element);
                literalNode.setLocation(location);
                booleanNode.initialize(builder, location, IASLanguageConstants.Boolean, literalNode);
                childList.add(booleanNode);
            }
            else if (element instanceof Integer)
            {
                MXMLIntNode intNode = new MXMLIntNode(this);
                MXMLLiteralNode literalNode = new MXMLLiteralNode(intNode, element);
                literalNode.setLocation(location);
                intNode.initialize(builder, location, IASLanguageConstants._int, literalNode);
                childList.add(intNode);
            }
            else if (element instanceof Long)
            {
                MXMLUintNode uintNode = new MXMLUintNode(this);
                MXMLLiteralNode literalNode = new MXMLLiteralNode(uintNode, element);
                literalNode.setLocation(location);
                uintNode.initialize(builder, location, IASLanguageConstants.uint, literalNode);
                childList.add(uintNode);
            }
            else if (element instanceof Number)
            {
                MXMLNumberNode numberNode = new MXMLNumberNode(this);
                MXMLLiteralNode literalNode = new MXMLLiteralNode(numberNode, element);
                literalNode.setLocation(location);
                numberNode.initialize(builder, location, IASLanguageConstants.Number, literalNode);
                childList.add(numberNode);
            }
            else if (element instanceof String)
            {
                MXMLStringNode stringNode = new MXMLStringNode(this);
                MXMLLiteralNode literalNode = new MXMLLiteralNode(stringNode, element);
                literalNode.setLocation(location);
                stringNode.initialize(builder, location, IASLanguageConstants.String, literalNode);
                childList.add(stringNode);
            }
        }

        setChildren(childList.toArray(new IMXMLNode[0]));
    }
}
