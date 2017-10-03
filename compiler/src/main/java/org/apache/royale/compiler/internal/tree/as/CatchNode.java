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
import org.apache.royale.compiler.internal.scopes.CatchScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.ICatchNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IScopedNode;

/**
 * ActionScript parse tree node representing a catch block (catch (e:Exception)
 * {...})
 */
// TODO ICatchNode should extend IScopedNode
public class CatchNode extends BaseStatementNode implements ICatchNode, IScopedNode
{
    /**
     * Constructor
     */
    public CatchNode(ParameterNode argumentNode)
    {
        super();
        this.argumentNode = argumentNode;
    }

    /**
     * Argument of catch block (caught exception)
     */
    protected ParameterNode argumentNode;

    private ASScope catchScope;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.CatchID;
    }

    @Override
    public int getChildCount()
    {
        int count = 0;
        
        if (argumentNode != null)
            count++;
        
        if (contentsNode != null)
            count++;
        
        return count;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0 && argumentNode != null)
            return argumentNode;
        
        else if (i == 1 || (i == 0 && argumentNode == null))
            return contentsNode;
        
        return null;
    }
    
    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.POPULATE_SCOPE) ||
            set.contains(PostProcessStep.RECONNECT_DEFINITIONS))
        {
            catchScope = new CatchScope(scope);
            catchScope.setContainingScope(scope);
        }

        if (contentsNode != null)
        {
            argumentNode.analyze(set, catchScope, problems);
            contentsNode.analyze(set, catchScope, problems);
        }
    }

    //
    // ICatchNode implementations
    //

    @Override
    public IParameterNode getCatchParameterNode()
    {
        return argumentNode;
    }

    @Override
    public IASScope getScope()
    {
        return catchScope;
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
}
