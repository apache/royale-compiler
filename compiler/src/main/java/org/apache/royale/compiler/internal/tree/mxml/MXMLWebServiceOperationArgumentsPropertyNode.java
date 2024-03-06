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
import java.util.List;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelPropertyContainerNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelPropertyNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLWebServiceOperationArgumentsPropertyNode;

/**
 * AST node for the {@code <arguments>} tag under the {@code <operation>} tag, which is under the {@code <WebService>} tag.
 */
class MXMLWebServiceOperationArgumentsPropertyNode extends MXMLPropertySpecifierNode implements IMXMLWebServiceOperationArgumentsPropertyNode
{
    /**
     * Create node for {@code <arguments>} tag.
     * 
     * @param parent Parent node.
     */
    MXMLWebServiceOperationArgumentsPropertyNode(MXMLWebServiceOperationNode parent)
    {
        super(parent);
        objectNode = new MXMLObjectNode(this);
    }

    private final MXMLObjectNode objectNode;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLPropertySpecifierID;
    }

    /**
     * {@code <arguments>} node only have one "instance" node of type "Object".
     */
    @Override
    public int getChildCount()
    {
        return objectNode != null ? 1 : 0;
    }

    /**
     * {@code <arguments>} node only have one "instance" node of type "Object".
     */
    @Override
    public IASNode getChild(int i)
    {
        if (i != 0)
        {
            throw new IndexOutOfBoundsException("Arguments node only have one child node.");
        }
        return objectNode;
    }

    @Override
    protected void initializeFromTag(MXMLTreeBuilder builder, IMXMLTagData tag)
    {
        setLocation(tag);

        MXMLNodeInfo info = createNodeInfo(builder);

        // parse it like <fx:Model>, but convert to property specifiers
        MXMLModelRootNode modelRootNode = new MXMLModelRootNode(this);
        modelRootNode.initializeFromTag(builder, tag);

        final RoyaleProject project = builder.getProject();
        for (MXMLPropertySpecifierNode specifierNode : getPropertySpecifiers(builder, modelRootNode, objectNode, this, project))
        {
            info.addChildNode(specifierNode);
        }

        // Do any final processing.
        initializationComplete(builder, tag, info);
    }

    protected MXMLPropertySpecifierNode[] getPropertySpecifiers(MXMLTreeBuilder builder, IMXMLModelPropertyContainerNode containerNode, MXMLObjectNode parentNode, MXMLPropertySpecifierNode parentSpecifierNode, RoyaleProject project)
    {
        List<MXMLPropertySpecifierNode> propSpecifiers = new ArrayList<MXMLPropertySpecifierNode>();
        for (String propertyName : containerNode.getPropertyNames()) {
            IMXMLModelPropertyNode[] propertyNodes = containerNode.getPropertyNodes(propertyName);
            MXMLPropertySpecifierNode specifierNode = getPropertySpecifier(builder, propertyName, propertyNodes, parentNode, parentSpecifierNode, project);
            propSpecifiers.add(specifierNode);
        }
        return propSpecifiers.toArray(new MXMLPropertySpecifierNode[0]);
    }

    protected MXMLPropertySpecifierNode getPropertySpecifier(MXMLTreeBuilder builder, String propertyName, IMXMLModelPropertyNode[] propertyNodes, MXMLObjectNode parentNode, MXMLPropertySpecifierNode parentSpecifierNode, RoyaleProject project)
    {
        final MXMLPropertySpecifierNode specifierNode = new MXMLPropertySpecifierNode(parentNode);
        specifierNode.setDynamicName(propertyName);
        if (propertyNodes.length > 1)
        {
            MXMLArrayNode argsArrayNode = new MXMLArrayNode(this);
            argsArrayNode.setClassReference(project, IASLanguageConstants.Array);

            List<IMXMLNode> argsChildNodes = new ArrayList<IMXMLNode>();
            for (IMXMLModelPropertyNode propNode : propertyNodes)
            {
                if (propNode.hasLeafValue())
                {
                    MXMLInstanceNode propInstanceNode = (MXMLInstanceNode) propNode.getInstanceNode();
                    propInstanceNode.setParent(argsArrayNode);
                    argsChildNodes.add(propInstanceNode);
                }
                else
                {
                    MXMLObjectNode propObjectNode = new MXMLObjectNode(this);
                    propObjectNode.setLocation(propNode);
                    propObjectNode.setClassReference(project, IASLanguageConstants.Object);
                    MXMLPropertySpecifierNode[] propSpecifiers = getPropertySpecifiers(builder, propNode, propObjectNode, specifierNode, project);
                    propObjectNode.setChildren(propSpecifiers);
                    argsChildNodes.add(propObjectNode);
                }
            }

            argsArrayNode.setChildren(argsChildNodes.toArray(new IMXMLNode[0]));
            specifierNode.setInstanceNode(argsArrayNode);
        }
        else
        {
            IMXMLModelPropertyNode propNode = propertyNodes[0];
            if (propNode.hasLeafValue())
            {
                MXMLInstanceNode propInstanceNode = (MXMLInstanceNode) propNode.getInstanceNode();
                propInstanceNode.setParent(parentSpecifierNode);
                specifierNode.setLocation(propNode);
                specifierNode.setInstanceNode(propInstanceNode);
            }
            else
            {
                MXMLObjectNode propObjectNode = new MXMLObjectNode(this);
                propObjectNode.setLocation(propNode);
                propObjectNode.setClassReference(project, IASLanguageConstants.Object);
                MXMLPropertySpecifierNode[] propSpecifiers = getPropertySpecifiers(builder, propNode, propObjectNode, specifierNode, project);
                propObjectNode.setChildren(propSpecifiers);
                specifierNode.setInstanceNode(propObjectNode);
            }
        }
        return specifierNode;
    }

    /**
     * Synthesize an "instance" node of type "Object" to own all the arguments
     * fields.
     */
    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag, MXMLNodeInfo info)
    {
        initializeObjectNode(builder, tag, info);
        setInstanceNode(objectNode);
        super.initializationComplete(builder, tag, info);
    }

    /**
     * Span "object" node's offset to the parent "arguments" node. Make the
     * dynamic request properties children of the "object" node.
     */
    private void initializeObjectNode(MXMLTreeBuilder builder, IMXMLTagData tag, MXMLNodeInfo info)
    {
        final RoyaleProject project = builder.getProject();
        objectNode.setClassReference(project, IASLanguageConstants.Object);
        objectNode.setChildren(info.getChildNodeList().toArray(new IMXMLNode[0]));
        objectNode.setLocation(tag);
    }
}
