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
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLConcatenatedDataBindingNode;

/**
 * Implementation of {@code IMXMLConcatenatedDataBindingNode}.
 */
class MXMLConcatenatedDataBindingNode extends MXMLInstanceNode implements IMXMLConcatenatedDataBindingNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLConcatenatedDataBindingNode(NodeBase parent)
    {
        super(parent);
    }

    private IASNode[] children;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLConcatenatedDataBindingID;
    }

    @Override
    public String getName()
    {
        return "ConcatenatedDataBinding";
    }

    @Override
    public IASNode getChild(int i)
    {
        return children != null ? children[i] : null;
    }

    @Override
    public int getChildCount()
    {
        return children != null ? children.length : 0;
    }

    public void setChildren(IASNode[] children)
    {
        this.children = children;
    }
}
