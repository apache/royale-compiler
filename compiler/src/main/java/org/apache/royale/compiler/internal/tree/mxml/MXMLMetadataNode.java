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

import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.mxml.IMXMLTextData.TextType;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.mxml.IMXMLMetadataNode;

class MXMLMetadataNode extends MXMLNodeBase implements IMXMLMetadataNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLMetadataNode(NodeBase parent)
    {
        super(parent);
    }

    private IMetaTagNode[] metaTagNodes;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLMetadataID;
    }

    @Override
    public IASNode getChild(int i)
    {
        return metaTagNodes != null ? metaTagNodes[i] : null;
    }

    @Override
    public int getChildCount()
    {
        return metaTagNodes != null ? metaTagNodes.length : 0;
    }

    @Override
    public String getName()
    {
        return IMXMLLanguageConstants.METADATA;
    }

    @Override
    public IMetaTagNode[] getMetaTagNodes()
    {
        return metaTagNodes;
    }

    @Override
    protected void processChildNonWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                                 IMXMLTextData text,
                                                 MXMLNodeInfo info)
    {
        // <fx:Metadata> allows metadata text, so don't do anything here.
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        // Parse the event handling code.
        for (IMXMLUnitData unit = tag.getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            if (unit instanceof IMXMLTextData)
            {
                final IMXMLTextData mxmlTextData = (IMXMLTextData)unit;
                if (mxmlTextData.getTextType() != TextType.WHITESPACE)
                {
                    MetaTagsNode metaTagsNode = ASParser.parseMetadata(builder.getWorkspace(),
                            mxmlTextData.getCompilableText(),
                            mxmlTextData.getSourcePath(),
                            mxmlTextData.getCompilableTextStart(),
                            mxmlTextData.getCompilableTextLine(),
                            mxmlTextData.getCompilableTextColumn(),
                            builder.getProblems());

                    if (metaTagsNode == null)
                    {
                        // This happens if we have an empty <fx:metadata/>
                        // It's OK for us to have no nodes (although clients may need to be wary)
                        metaTagNodes = null;
                        return;
                    }

                    // Make the statements inside the script tag the children of this node.
                    int n = metaTagsNode.getChildCount();
                    metaTagNodes = new IMetaTagNode[n];
                    for (int i = 0; i < n; i++)
                    {
                        IMetaTagNode child = (IMetaTagNode)metaTagsNode.getChild(i);
                        metaTagNodes[i] = child;
                        ((NodeBase)child).setParent(this);
                    }
                }
            }
        }
    }

}
