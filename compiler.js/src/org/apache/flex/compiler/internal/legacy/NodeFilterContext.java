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

package org.apache.flex.compiler.internal.legacy;

import java.lang.ref.WeakReference;
import java.util.Set;

import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.LanguageIdentifierNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.flex.compiler.tree.as.IScopedDefinitionNode;
import org.apache.flex.compiler.tree.as.IScopedNode;

/**
 * An IFilterContext implementation based on an IASNode. Provided for backwards
 * compatibility of ASDefinitionFilter methods which expect to operate on
 * IASNodes
 */
public class NodeFilterContext extends ScopeFilterContext implements IFilterContext
{
    private WeakReference<IASNode> nodeContext;

    /**
     * Find the scope that will determine the namespaces in effect for a node
     * example: for a ClassNode, we will get the block scope that is a direct
     * child of the class node. Other types of nodes may need to walk up the
     * tree...
     * 
     * @param node is the node whose scope we care about. may be null
     * @return the scope for the node. may be null. Note: I *think* the return
     * is only null if a null node is passed in.
     */
    private static ASScope getScopeOfNamespaceSetForNode(IASNode node)
    {

        ASScope ret = null;
        IScopedNode scopedNode = null;

        // search up the node tree until we find someone who can give us a scope,
        // either IScopedNode or IScopedDefinition
        // When loop is done, scopedNode should be set to the node we found
        while ((scopedNode == null) && (node != null))
        {
            if (node instanceof IScopedNode)
            {
                scopedNode = (IScopedNode)node;
            }
            else if (node instanceof IScopedDefinitionNode)
            {
                IScopedDefinitionNode scopedDefinitionNode = (IScopedDefinitionNode)node;
                scopedNode = scopedDefinitionNode.getScopedNode();
            }
            if (scopedNode == null)
            {
                node = node.getParent();
            }
        }

        // if we found a scoped node, get the scope out of it
        if (scopedNode != null)
        {
            IASScope scope = scopedNode.getScope();
            assert (scope instanceof ASScope);
            if (scope instanceof ASScope)
            {
                ret = (ASScope)scope;
            }
        }
        return ret;
    }

    public NodeFilterContext(IASNode context)
    {
        super(getScopeOfNamespaceSetForNode(context));
        this.nodeContext = new WeakReference<IASNode>(context);
    }

    /**
     * Determine whether this lookup is from a static context (e.g. somewhere
     * within a static method)
     * 
     * @return true if the lookup is being performed in a static context
     */
    @Override
    public boolean isInStaticContext()
    {
        IASNode node = nodeContext.get();
        if (node != null)
        {
            return isInStaticContext(node);
        }
        return false;
    }

    /**
     * Determine whether this node is in a static context (e.g. somewhere within
     * a static method)
     * 
     * @return true if the node is in a static context
     */
    public static boolean isInStaticContext(IASNode node)
    {
        IASNode current = node.getParent();
        while (current != null && !(current instanceof FunctionNode))
            current = current.getParent();
        if (current instanceof FunctionNode && ((FunctionNode)current).hasModifier(ASModifier.STATIC))
            return true;
        return false;
    }

    @Override
    public Set<INamespaceDefinition> getNamespaceSet(ICompilerProject project)
    {
        Set<INamespaceDefinition> nsset = super.getNamespaceSet(project);
        IASNode n = nodeContext.get();
        if (isSuper(n))
        {
            // If our node is 'super' then we need to adjust the namespace set to account for the super
            // classes protected namespace
            nsset = getScope().adjustNamespaceSetForSuper(((LanguageIdentifierNode)n).resolveType(project), nsset);
        }
        return nsset;
    }

    @Override
    public Set<INamespaceDefinition> getNamespaceSet(ICompilerProject project, String name)
    {
        Set<INamespaceDefinition> nsset = super.getNamespaceSet(project, name);
        IASNode n = nodeContext.get();
        if (isSuper(n))
        {
            // If our node is 'super' then we need to adjust the namespace set to account for the super
            // classes protected namespace
            nsset = getScope().adjustNamespaceSetForSuper(((LanguageIdentifierNode)n).resolveType(project), nsset);
        }
        return nsset;
    }

    /**
     * @return true if the node passed in is 'super'
     */
    private boolean isSuper(IASNode n)
    {
        boolean isSuper = false;
        if (n != null)
        {
            if (n instanceof LanguageIdentifierNode && ((LanguageIdentifierNode)n).getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.SUPER)
                isSuper = true;
        }
        return isSuper;
    }
}
