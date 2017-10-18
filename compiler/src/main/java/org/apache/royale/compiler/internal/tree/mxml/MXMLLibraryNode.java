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

import org.apache.royale.compiler.internal.scopes.MXMLFileScope;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLLibraryNode;

/**
 * MXML syntax tree node for &lt;Library&gt; tags.
 */
class MXMLLibraryNode extends MXMLNodeBase implements IMXMLLibraryNode
{
    private static final IMXMLDefinitionNode[] NO_DEFINITION_NODES = new IMXMLDefinitionNode[0];

    /**
     * @param parent
     */
    MXMLLibraryNode(NodeBase parent)
    {
        super(parent);
    }

    private IMXMLDefinitionNode[] definitionNodes = NO_DEFINITION_NODES;

    @Override
    public String getName()
    {
        return IMXMLLanguageConstants.LIBRARY;
    }

    @Override
    public ASTNodeID getNodeID()
    {
        // TODO Auto-generated method stub
        return ASTNodeID.MXMLLibraryID;
    }

    @Override
    public int getChildCount()
    {
        return definitionNodes.length;
    }

    @Override
    public IASNode getChild(int i)
    {
        return definitionNodes[i];
    }

    @Override
    protected MXMLNodeInfo createNodeInfo(MXMLTreeBuilder builder)
    {
        return new MXMLNodeInfo(builder);
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag,
                                   MXMLNodeInfo info)
    {
        MXMLFileScope fileScope = builder.getFileScope();

        MXMLNodeBase childNode = null;

        if (fileScope.isDefinitionTag(childTag))
            childNode = new MXMLDefinitionNode(this);
        else
            super.processChildTag(builder, tag, childTag, info);

        if (childNode != null)
        {
            childNode.initializeFromTag(builder, childTag);
            info.addChildNode(childNode);
        }
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        definitionNodes = info.getChildNodeList().toArray(new IMXMLDefinitionNode[0]);
    }

    @Override
    public IMXMLDefinitionNode[] getDefinitionNodes()
    {
        return definitionNodes;
    }
}
