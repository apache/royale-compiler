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
import java.util.List;

import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.parts.FunctionContentsPart;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.IScopedNode;

/**
 * A BlockNode that is provided with a scope that collections definitions.
 */
public class ScopedBlockNode extends BlockNode implements IScopedNode
{
    /**
     * Constructor.
     */
    public ScopedBlockNode()
    {
        this(true);
    }

    /**
     * Constructor.
     * <p>
     * This variant is used by {@link FunctionContentsPart}.
     * 
     * @param compressChildrenOnNormalization False if
     * {@code optimizeChildren(Object)} should be skipped during
     * {@code normalize(boolean)}.
     */
    public ScopedBlockNode(boolean compressChildrenOnNormalization)
    {
        super();
        this.compressChildrenOnNormalization = compressChildrenOnNormalization;
    }

    private final boolean compressChildrenOnNormalization;

    /**
     * The scope associated with this block
     */
    protected ASScope scope;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASScope getASScope()
    {
        return scope;
    }

    @Override
    public IASScope getScope()
    {
        return scope;
    }
    
    @Override
    // TODO Remove unnecessary override.
    public Collection<ICompilerProblem> runPostProcess(EnumSet<PostProcessStep> set, ASScope containingScope)
    {
        return super.runPostProcess(set, containingScope);
    }
    
    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (scope instanceof TypeScope)
        {
            // TypeScopes need to pass the class scope, or instance scope to children based on if
            // the children are static or not so that the children will be set up with the correct scope chain
            TypeScope typeScope = (TypeScope)scope;
            ASScope classScope = typeScope.getStaticScope();
            ASScope instanceScope = typeScope.getInstanceScope();

            // Populate this scope with definitions found among the relevant descendants
            List <IASNode> children = getDescendantStatements(this);
            for (IASNode child : children)
            {
                if (child instanceof NodeBase)
                {
                    if (child.getParent() == null)
                        ((NodeBase)child).setParent(this);
                    ((NodeBase)child).analyze(set, childInStaticScope(child) ? classScope : instanceScope, problems);
                }
            }
            
            if (scope != null)
                scope.compact();
        }
        else
        {
            super.analyze(set, scope, problems);
        }

        if (scope != null)
            scope.compact();
    }
    
    /**
     * looks at children, but skips over non-statement nodes to get the closest descendant nodes that
     * represent statements.
     */
    private static List<IASNode> getDescendantStatements(IASNode parent)
    {
        ArrayList<IASNode> ret = new ArrayList<IASNode>();
        int childrenSize = parent.getChildCount();
        for (int i = 0; i < childrenSize; i++)
        {
            IASNode child = parent.getChild(i);
            if (child instanceof ConfigConditionBlockNode)
            {
                ret.addAll( getDescendantStatements(child));
            }
            else
            {
                ret.add(child);
            }
        }
        return ret;
    }
    
    //
    // TreeNode overrides
    //
    
    @Override
    protected final void optimizeChildren(Object newChildren)
    {
        // If this node is the content node of a {@code FunctionNode}, do not
        // compress the children field into an immutable array. The children will be
        // populated later when the body is rebuilt.

        if (compressChildrenOnNormalization)
            super.optimizeChildren(newChildren);
    }
    
    //
    // IScopedNode implementations
    //
    
    @Override
    public void getAllImportNodes(Collection<IImportNode> importNodes)
    {
        // Collect the import nodes that are descendants of this node
        // but which are not within other scoped nodes.
        collectImportNodes(importNodes);
        
        // Recurse up the chain of scoped nodes to collect import nodes
        // from higher scopes.
        IScopedNode parent = (IScopedNode)getAncestorOfType(IScopedNode.class);
        // if parent is package node, don't fetch imports from the file node
        // as file scope imports are not applicable in this scope
        if (parent != null )
        {
            if( !(getParent() instanceof PackageNode) )
                parent.getAllImportNodes(importNodes);
            // If we're a package, then just grab the implicit imports from the FileNode
            else if (parent instanceof FileNode )
                ((FileNode)parent).collectImplicitImportNodes(importNodes);
        }
    }

    @Override
    public void getAllImports(Collection<String> imports)
    {
        ArrayList<IImportNode> importNodes = new ArrayList<IImportNode>();
        getAllImportNodes(importNodes);
        for (IImportNode importNode : importNodes)
        {
            imports.add(importNode.getImportName());
        }
    }

    //
    // Other methods
    //

    void reconnectScope(ASScope scope)
    {
        this.scope = scope;
        scope.reconnectScopeNode(this);
    }

    public Collection<ICompilerProblem> runPostProcess(EnumSet<PostProcessStep> set)
    {
        return runPostProcess(set, null);
    }

    /**
     * Attaches this node to the scope that has been created for it.
     * 
     * @param scope The {@link ASScope} for this node.
     */
    public void setScope(ASScope scope)
    {
        this.scope = scope;
    }

    private boolean childInStaticScope(IASNode child)
    {
        // If the child is a defininition then static-ness is determined by the presence, or
        // lack of, the "static" modifier.
        if (child instanceof BaseDefinitionNode)
            return ((BaseDefinitionNode)child).hasModifier(ASModifier.STATIC)
                    // Namespaces are always static
                    || child instanceof NamespaceNode;
        // The child is not a definition, so it is loose code in the class body, so it is
        // static
        return true;
    }
}
