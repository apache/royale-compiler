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
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLUintNode;

class MXMLUintNode extends MXMLExpressionNodeBase implements IMXMLUintNode
{
    private static final Integer DEFAULT = 0;

    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLUintNode(NodeBase parent)
    {
        super(parent);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLUintID;
    }

    @Override
    public String getName()
    {
        return IASLanguageConstants.uint;
    }

    @Override
    public long getValue()
    {
        assert getExpressionNode() instanceof MXMLLiteralNode : "getValue() shouldn't be getting called on a non-literal MXMLUintNode";

        MXMLLiteralNode literalNode = (MXMLLiteralNode)getExpressionNode();
        Object value = literalNode.getValue();
        if (value instanceof Integer)
            return ((Integer)literalNode.getValue()).intValue();
        return ((Long)literalNode.getValue()).longValue();
    }

    @Override
    public ExpressionType getExpressionType()
    {
        return ExpressionType.UINT;
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        NodeBase expressionNode = createExpressionNodeFromFragments(builder, tag, info, DEFAULT);
        setExpressionNode(expressionNode);

        super.initializationComplete(builder, tag, info);
    }
}
