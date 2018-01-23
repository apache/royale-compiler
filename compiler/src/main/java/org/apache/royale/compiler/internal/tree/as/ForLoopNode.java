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
import java.util.Collection;
import java.util.EnumSet;

import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;

public class ForLoopNode extends FixedChildrenNode implements IForLoopNode
{
    /**
     * Constructor.
     */
    public ForLoopNode(IASToken forKeyword)
    {
        super();

        if (forKeyword != null)
        {
            startBefore(forKeyword);
            if (forKeyword.getText().length() == 3)
                kind = ForLoopKind.FOR;
            else
                kind = ForLoopKind.FOR_EACH;
        }
        else
        {
            kind = ForLoopKind.FOR;
        }

        conditionsStatementsNode = new ContainerNode();

        contentsNode = new BlockNode();
    }

    /**
     * The statements in the loop header
     */
    protected ContainerNode conditionsStatementsNode;

    /**
     * The contents of this loop
     */
    protected BlockNode contentsNode;

    /**
     * For loop type
     */
    private ForLoopKind kind;
    
    //
    // NodeBase overrides
    //
   
    @Override
    public ASTNodeID getNodeID()
    {
        switch (kind)
        {
            case FOR:
                return ASTNodeID.ForLoopID;
                
            case FOR_EACH:
                return ASTNodeID.ForEachLoopID;
        }
        
        return ASTNodeID.ForLoopID;
    }

    @Override
    public int getChildCount()
    {
        int count = 0;
        
        if (conditionsStatementsNode != null)
            count++;
        
        if (contentsNode != null)
            count++;
        
        return count;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0)
            return conditionsStatementsNode;
        
        else if (i == 1)
            return contentsNode;
        
        return null;
    }
        
    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        // Put header statements into the current block
        conditionsStatementsNode.analyze(set, scope, problems);
        contentsNode.analyze(set, scope, problems);
    }
    
    //
    // IForLoopNode implementations
    //
    
    @Override
    public IASNode getStatementContentsNode()
    {
        return contentsNode;
    }

    @Override
    public ForLoopKind getKind()
    {
        return kind;
    }

    @Override
    public IExpressionNode[] getConditionalExpressionNodes()
    {
        int childCount = conditionsStatementsNode.getChildCount();
        ArrayList<IExpressionNode> retVal = new ArrayList<IExpressionNode>(3);
        for (int i = 0; i < childCount; i++)
        {
            IASNode child = conditionsStatementsNode.getChild(i);
            if (child instanceof IExpressionNode)
                retVal.add((IExpressionNode)child);
        }

        return retVal.toArray(new IExpressionNode[0]);
    }
    
    //
    // Other methods
    //

    public ContainerNode getConditionalsContainerNode()
    {
        return conditionsStatementsNode;
    }

    public BlockNode getContentsNode()
    {
        return contentsNode;
    }
}
