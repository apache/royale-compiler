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

import java.util.Collection;
import java.util.EnumSet;

import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.WithScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.IWithNode;

/**
 * ActionScript parse tree node representing a with block (with (x) {...})
 */
public class WithNode extends ConditionalNode implements IWithNode, IScopedNode
{
    /**
     * Constructor.
     * 
     * @param token The token representing the <code>with</code> keyword.
     */
    public WithNode(IASToken token)
    {
        super(token);
    }

    private WithScope withScope;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.WithID;
    }
    
    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.POPULATE_SCOPE) ||
            set.contains(PostProcessStep.RECONNECT_DEFINITIONS))
        {
            withScope = new WithScope(scope);
            withScope.setContainingScope(scope);
            // We know this is always a ScopedBlockNode because we created it above in initBlockNode
            ((ScopedBlockNode)contentsNode).setScope(withScope);
        }

        if (contentsNode != null)
            contentsNode.analyze(set, withScope, problems);
    }
    
    //
    // BaseStatementNode overrides
    //

    @Override
    protected BlockNode initBlockNode()
    {
        return new ScopedBlockNode();
    }
    
    //
    // IWithNode implementations
    //

    @Override
    public IASScope getScope()
    {
        return withScope;
    }

    @Override
    public void getAllImports(Collection<String> imports)
    {
        getContainingScope().getAllImports(imports);
    }

    @Override
    public void getAllImportNodes(Collection<IImportNode> imports)
    {
        getContainingScope().getAllImportNodes(imports);
    }
    
    @Override
    public IExpressionNode getTargetNode()
    {
        return conditionalNode;
    }
}
