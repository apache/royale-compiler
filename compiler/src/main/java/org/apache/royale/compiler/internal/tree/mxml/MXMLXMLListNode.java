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
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.problems.MXMLXMLListMixedContentProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLXMLListNode;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@code IMXMLXMLListNode} interface.
 */
class MXMLXMLListNode extends MXMLInstanceNode implements IMXMLXMLListNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLXMLListNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * Collect the children of the XMLList node - after we process them in
     * initializationComplete, we null out this collection so we don't pin the
     * MXMLTagData's in memory.
     */
    private ArrayList<IMXMLTagData> childTags = new ArrayList<IMXMLTagData>();

    private String xmlString;
    
    private List<IMXMLBindingNode> bindings = new ArrayList<IMXMLBindingNode>();

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLXMLListID;
    }

    @Override
    public String getName()
    {
        return IASLanguageConstants.XMLList;
    }

    @Override
    public String getXMLString()
    {
        return xmlString;
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag,
                                   MXMLNodeInfo info)
    {
        childTags.add(childTag);
    }

    @Override
    protected void processChildNonWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                      IMXMLTextData text,
                                      MXMLNodeInfo info)
    {
        builder.addProblem(new MXMLXMLListMixedContentProblem(tag));
    }

    /**
     * This method gives subclasses a chance to do final processing after
     * considering each attribute and content unit.
     * <p>
     * The base class version calls <code>adjustOffset</code> to translate the
     * node start and end offset from local to absolute offsets.
     */
    @Override
    protected void initializationComplete(MXMLTreeBuilder builder,
                                          IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {

        analyzeXML(builder);

        super.initializationComplete(builder, tag, info);

        if (bindings.size() > 0)
        {
        	int childCount = getChildCount();
        	List<IMXMLNode> children = new ArrayList<IMXMLNode>();
        	for (int i = 0; i < childCount; i++)
        	{
        		children.add((IMXMLNode)getChild(i));
        	}
        	children.addAll(bindings);
        	setChildren(children.toArray(new IMXMLNode[] {}));
        }
        // don't pin the MXMLTagDatas
        childTags = null;
    }

    private void analyzeXML(MXMLTreeBuilder builder)
    {
        StringWriter sw = new StringWriter();
        for (IMXMLTagData tag : childTags)
        {
        	IMXMLTagData parentTagData = tag.getParentTag();
        	XMLBuilder xmlBuilder = new XMLBuilder(this, parentTagData, tag.getCompositePrefixMap(), builder);
        	xmlBuilder.processNode(tag, sw);
        	List<IMXMLBindingNode> bindings = xmlBuilder.getDatabindings();
        	this.bindings.addAll(bindings);
        }
        xmlString = sw.toString();
    }
}
