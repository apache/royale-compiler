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

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLWebServiceOperationArgumentsPropertyNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;

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

        // Process each child tag.
        for (IMXMLUnitData unit = tag.getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            if (unit instanceof IMXMLTagData)
            {
                processChildTag(builder, tag, (IMXMLTagData) unit, info);
            }
        }

        // Do any final processing.
        initializationComplete(builder, tag, info);
    }

    /**
     * Add child tags as dynamic properties to the "object" node.
     */
    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag, IMXMLTagData childTag, MXMLNodeInfo info)
    {
        final MXMLPropertySpecifierNode specifierNode = new MXMLPropertySpecifierNode(this);
        specifierNode.setDynamicName(childTag.getShortName());
        specifierNode.initializeFromTag(builder, childTag);
        specifierNode.setParent(objectNode);
        info.addChildNode(specifierNode);
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
