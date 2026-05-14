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

package org.apache.royale.compiler.internal.scopes;

import java.util.Collection;
import java.util.Set;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IScopedNode;

/**
 * Sub-class of ASScope used for function scopes. all definition in scope of
 * this type are local to a function.
 */
public class FunctionScope extends ASScope
{

    public FunctionScope(ASScope containingScope, ScopedBlockNode block)
    {
        super(containingScope, block);
    }

    public FunctionScope(ASScope containingScope)
    {
        super(containingScope);
    }

    @Override
    public void reconnectScopeNode(IScopedNode node)
    {
        IASNode parentNode = node.getParent();
        if (parentNode instanceof FunctionNode)
        {
            FunctionNode functionNode = (FunctionNode) parentNode;
            if (!functionNode.hasBeenParsed())
            {
                // the compiler holds weak references to nodes in certain places
                // which allows them to be garbage collected. however, from time
                // to time, the compiler may need the node again later, after it
                // has been garbage collected, so that node needs to be
                // recreated.
                // when a function node is recreated, its scope may still be
                // populated with definitions from the old function node's body,
                // such as local variables. upon recreation, those local
                // definitions should be considered invalid because they will
                // be replaced with new definitions after parsing the new
                // function node's body.
                // to reiterate, removing the definitions below is not the same
                // case where a function node's discardFunctionBody() method is
                // called and the local definitions need to be removed. instead,
                // this is a separate case where a function node is garbage
                // collected and the body was never discarded, so the scope
                // still contains definitions from the old function node.
                Collection<IDefinition> localDefs = getAllLocalDefinitions();
                for (IDefinition def : localDefs)
                {
                    if (! (def instanceof IParameterDefinition))
                    {
                        removeDefinition(def);
                    }
                }
            }
        }
        super.reconnectScopeNode(node);
    }

    
    @Override
    public IScopedNode getScopeNode()
    {
        IScopedNode node = super.getScopeNode();
        if (node instanceof ScopedBlockNode)
        {
            IASNode parentNode = node.getParent();
            if (parentNode instanceof FunctionNode)
            {
                FunctionNode functionNode = (FunctionNode) parentNode;
                if (functionNode.hasBeenParsed())
                {
                    reconnectScopeNode(node);
                }
            }
        }
        return node;
    }

    @Override
    public void getAllLocalProperties(CompilerProject project, Collection<IDefinition> defs, Set<INamespaceDefinition> namespaceSet, INamespaceDefinition extraNamespace)
    {
        // If the function body hasn't been parsed yet (or needs re-parsing after GC),
        // triggering getScopeNode() will ensure it is parsed and the scope is populated
        // with local variables.
        if (project.getWorkspace() != null && getFileScope() != null)
        {
            getScopeNode();
        }
        super.getAllLocalProperties(project, defs, namespaceSet, extraNamespace);
    }

    @Override
    protected void getPropertyForScopeChain(CompilerProject project, Collection<IDefinition> defs, String baseName, NamespaceSetPredicate namespaceSet, boolean findAll)
    {
        // If the function body hasn't been parsed yet (or needs re-parsing after GC),
        // triggering getScopeNode() will ensure it is parsed and the scope is populated
        // with local variables.
        if (project.getWorkspace() != null && getFileScope() != null)
        {
            getScopeNode();
        }
        super.getPropertyForScopeChain(project, defs, baseName, namespaceSet, findAll);
    }

}
