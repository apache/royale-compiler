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
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLSingleDataBindingNode;

/**
 * Implementation of {@code IMXMLDataBindingNode}.
 */
class MXMLSingleDataBindingNode extends MXMLInstanceNode implements IMXMLSingleDataBindingNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLSingleDataBindingNode(NodeBase parent)
    {
        super(parent);
    }

    private IExpressionNode expressionNode;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLDataBindingID;
    }

    @Override
    public String getName()
    {
        return "DataBinding";
    }

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 ? expressionNode : null;
    }

    @Override
    public int getChildCount()
    {
        return expressionNode != null ? 1 : 0;
    }

    @Override
    public IExpressionNode getExpressionNode()
    {
        return expressionNode;
    }

    public void setExpressionNode(IExpressionNode expressionNode)
    {
        this.expressionNode = expressionNode;
    }

}
