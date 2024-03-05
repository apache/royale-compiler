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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLRemoteObjectMethodArgumentsPropertyNode;

/**
 * AST node for the {@code <arguments>} tag under the {@code <method>} tag, which is under the {@code <RemoteObject>} tag.
 */
class MXMLRemoteObjectMethodArgumentsPropertyNode extends MXMLPropertySpecifierNode implements IMXMLRemoteObjectMethodArgumentsPropertyNode
{
    /**
     * Create node for {@code <arguments>} tag.
     * 
     * @param parent Parent node.
     */
    MXMLRemoteObjectMethodArgumentsPropertyNode(MXMLRemoteObjectMethodNode parent)
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

        // look for duplicate property tags
        // if there's more than one of the same tag, the value will be an array
        Map<String, List<IMXMLTagData>> propertyNameToTags = new HashMap<>();
        for (IMXMLUnitData unit = tag.getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            if (unit instanceof IMXMLTagData)
            {
                IMXMLTagData childTag = (IMXMLTagData) unit;
                String propertyName = childTag.getShortName();
                List<IMXMLTagData> tagsForProperty = propertyNameToTags.get(propertyName);
                if (tagsForProperty == null)
                {
                    tagsForProperty = new ArrayList<IMXMLTagData>();
                    propertyNameToTags.put(propertyName, tagsForProperty);
                }
                tagsForProperty.add(childTag);
            }
        }

        // for each property found, initialize its tags
        for (String propertyName : propertyNameToTags.keySet())
        {
            final List<IMXMLTagData> tagsForProperty = propertyNameToTags.get(propertyName);
            final MXMLPropertySpecifierNode specifierNode = new MXMLPropertySpecifierNode(this);
            specifierNode.setDynamicName(propertyName);
            if (tagsForProperty.size() > 1)
            {
                List<IMXMLNode> argsChildNodes = new ArrayList<IMXMLNode>();
                for (IMXMLTagData childTag : tagsForProperty)
                {
                    final MXMLPropertySpecifierNode childSpecifierNode = new MXMLPropertySpecifierNode(this);
                    childSpecifierNode.setDynamicName(propertyName);
                    childSpecifierNode.initializeFromTag(builder, childTag);
                    argsChildNodes.add(childSpecifierNode.getInstanceNode());
                }

                MXMLArrayNode argsArrayNode = new MXMLArrayNode(objectNode);
                argsArrayNode.setChildren(argsChildNodes.toArray(new IMXMLNode[0]));
                specifierNode.setInstanceNode(argsArrayNode);
            }
            else
            {
                specifierNode.initializeFromTag(builder, tagsForProperty.get(0));
            }
            specifierNode.setParent(objectNode);
            info.addChildNode(specifierNode);
        }

        // Do any final processing.
        initializationComplete(builder, tag, info);
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
    }
}
