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

package org.apache.royale.compiler.internal.tree.as;

import java.util.ArrayList;

import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IConditionalNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IIfNode;
import org.apache.royale.compiler.tree.as.ITerminalNode;

/**
 * Represents an if statement, of the form
 * <code> if(condition) doSomething(); else somethingElse();
 * if(condition) else if(otherCondition) else
 */
public class IfNode extends TreeNode implements IIfNode
{
    /**
     * Constructor.
     */
    public IfNode(IASToken keyword)
    {
        if (keyword != null)
            startBefore(keyword);
    }
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.IfStatementID;
    }
    
    //
    // IIfNode implementations
    //
    
    @Override
    public IASNode getStatementContentsNode()
    {
        return this;
    }

    @Override
    public IExpressionNode getConditionalExpressionNode()
    {
        IASNode child = getChild(0);
        if (child instanceof ConditionalNode)
            return ((ConditionalNode)child).getConditionalExpressionNode();
        return null;
    }

    @Override
    public IConditionalNode[] getElseIfNodes()
    {
        int childCount = getChildCount();
        ArrayList<IConditionalNode> retVal = new ArrayList<IConditionalNode>(childCount);
        for (int i = 1; i < childCount; i++)
        { //skip the first child, it will be the if conditional
            IASNode child = getChild(i);
            if (child instanceof IConditionalNode)
            {
                retVal.add((IConditionalNode)child);
            }
        }
        return retVal.toArray(new IConditionalNode[0]);
    }

    @Override
    public ITerminalNode getElseNode()
    {
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--)
        {
            IASNode child = getChild(i);
            if (child instanceof ITerminalNode)
                return (ITerminalNode)child;
        }
        return null;
    }

    //
    // Other methods
    //

    public void addBranch(ConditionalNode node)
    {
        addChild(node);
    }

    public void addBranch(TerminalNode node)
    {
        addChild(node);
    }
}
