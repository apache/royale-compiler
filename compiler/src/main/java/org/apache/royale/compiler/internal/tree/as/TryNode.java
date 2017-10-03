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

import antlr.Token;

import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.ICatchNode;
import org.apache.royale.compiler.tree.as.IStatementNode;
import org.apache.royale.compiler.tree.as.ITerminalNode;
import org.apache.royale.compiler.tree.as.ITryNode;
import org.apache.royale.compiler.tree.as.ITerminalNode.TerminalKind;

/**
 * Represents a try/catch/finally node.
 */
public class TryNode extends TreeNode implements ITryNode, IStatementNode
{
    /**
     * Constructor.
     * 
     * @param keyword The token representing the <code>try</code> keyword.
     */
    public TryNode(IASToken keyword)
    {
        super();
        
        startBefore(keyword);
        contentsBlockNode = new BlockNode();
        contentsBlockNode.startAfter((Token)keyword);
        contentsBlockNode.endAfter(keyword);
        addChild(contentsBlockNode);
    }

    private BlockNode contentsBlockNode;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.TryID;
    }
    
    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        // We handle all of this ourselves
    }

    //
    // ITryNode implementations
    //
    
    @Override
    public IASNode getStatementContentsNode()
    {
        return contentsBlockNode;
    }

    @Override
    public int getCatchNodeCount()
    {
        // All the children except for the contents node
        // and the finally node are catch nodes.
        
        int n = getChildCount();
        
        if (getContentsNode() != null)
            n--;
        
        if (getFinallyNode() != null)
            n--;
        
        return n;
    }

    @Override
    public ICatchNode getCatchNode(int i)
    {
        if (i < 0 || i >= getCatchNodeCount())
            return null;

        // The catch nodes follow the contents node.
        if (getContentsNode() != null)
            i++;
        
        return (ICatchNode)getChild(i);
    }
    
    @Override
    public ITerminalNode getFinallyNode()
    {
        int childCount = getChildCount();
        if (childCount < 1)
            return null;

        IASNode child = getChild(childCount - 1);

        if (child instanceof TerminalNode && ((TerminalNode)child).kind == TerminalKind.FINALLY)
            return (ITerminalNode)child;

        return null;
    }
    
    //
    // Other methods
    //

    /**
     * Adds a <code>catch</code> caluse in the order in which it was found
     * 
     * @param node The node representing a <code>catch</code> clause.
     */
    public void addCatchClause(CatchNode node)
    {
        addChild(node);
    }

    /**
     * Adds a <code>finally</code> clause in the order in which it was found
     * 
     * @param node The node representing the <code>finally</code> cluase.
     */
    public void addFinallyBlock(TerminalNode node)
    {
        addChild(node);
    }

    public BlockNode getContentsNode()
    {
        return contentsBlockNode;
    }
}
