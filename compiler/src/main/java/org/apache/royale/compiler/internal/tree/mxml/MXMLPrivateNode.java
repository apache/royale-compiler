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

import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLPrivateNode;

/**
 * Implementation of the {@code IMXMLPrivateNode} interface.
 */
class MXMLPrivateNode extends MXMLNodeBase implements IMXMLPrivateNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLPrivateNode(NodeBase parent)
    {
        super(parent);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLPrivateID;
    }

    @Override
    public String getName()
    {
        return IMXMLLanguageConstants.PRIVATE;
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag, MXMLNodeInfo info)
    {
        // Allow any child tag, but ignore it.
    }

    @Override
    protected void processChildNonWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                                 IMXMLTextData text, MXMLNodeInfo info)
    {
        // Allow child text, but ignore it.
    }
}
