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
import java.util.HashSet;
import java.util.Set;

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IQualifiers;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import com.google.common.collect.ImmutableSet;

/**
 * Identifier representing a namespace
 */
public class NamespaceIdentifierNode extends IdentifierNode implements INamespaceDecorationNode
{
    /**
     * Constructor.
     * 
     * @param t The token representing the namespace.
     */
    public NamespaceIdentifierNode(IASToken t)
    {
        super(t.getText());
        
        span(t);
    }

    /**
     * Constructor.
     * 
     * @param text The namespace as a String.
     */
    public NamespaceIdentifierNode(String text)
    {
        super(text);
    }

    /**
     * Constructor.
     * 
     * @param node The node reprsenting the namespace.
     */
    public NamespaceIdentifierNode(IdentifierNode node)
    {
        super(node.getName());
        
        span(node.getAbsoluteStart(), node.getAbsoluteEnd(), node.getLine(), node.getColumn(), node.getEndLine(), node.getEndColumn());
        setSourcePath(node.getSourcePath());
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected NamespaceIdentifierNode(NamespaceIdentifierNode other)
    {
        super(other);
        
        this.isConfig = other.isConfig;
    }
    
    private IDefinitionNode decoratedDefinitionNode = null;

    /**
     * Flag used to indicate if we are a config name
     */
    private boolean isConfig;

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.NamespaceIdentifierID;
    }
    
    @Override
    // TODO What is special about this class
    // that requires it to override getParent()?
    public IASNode getParent()
    {
        if (parent == null && decoratedDefinitionNode != null)
        {
            // if we don't have a parent, try the IDefinition
            return decoratedDefinitionNode.getParent();
        }
        return super.getParent();
    }


    //
    // ExpressionNodeBase overrides
    //

    @Override
    public IDefinition resolve(ICompilerProject project)
    {
        boolean resolveToConcreteNs = true;
        return resolve(project, resolveToConcreteNs);
    }

    @Override
    protected NamespaceIdentifierNode copy()
    {
        return new NamespaceIdentifierNode(this);
    }
    
    @Override
    public IScopedNode getScopeNode()
    {
        return (IScopedNode)getAncestorOfType(IScopedNode.class);
    }

    @Override
    public ExpressionNodeBase getDecorationNode()
    {
        return this;
    }
    
    //
    // IdentifierNode overrides
    //
    
    @Override
    public Name getMName(ICompilerProject project)
    {
        // There is no AET name to refer to builtin identifiers.
        // This gets called with the builtin identifiers by the BURM,
        // but it is OK to return null - the names won't be used.
        if (isBuiltinNamespaceIdentifier())
            return null;

        // resolve without following any namespace reference chains
        // since we want the name for this definition, not whatever
        // namespace it ends up refering to.
        // for:
        //   namespace ns1 = ns2;
        //   ns1; // we want the name for ns1, not ns2.
        IDefinition def = resolve(project, false);
        if (canEarlyBind(project, def))
            return ((DefinitionBase)def).getMName(project);

        // If we can't early bind, then the super method does the right thing
        return super.getMName(project);
    }

    //
    // INamespaceDecorationNode implementations
    //
    
    @Override
    public IDefinitionNode getDecoratedDefinitionNode()
    {
        return decoratedDefinitionNode;
    }

    @Override
    public NamespaceDecorationKind getNamespaceDecorationKind()
    {
        return isConfig ? NamespaceDecorationKind.CONFIG : NamespaceDecorationKind.NAME;
    }
    
    //
    // Other methods
    //
 
    /**
     * Sets that this namespace is a config name used for conditional
     * compilation
     * 
     * @param isConfig trye if this namespace is part of a config expression
     */
    public void setIsConfigNamespace(boolean isConfig)
    {
        this.isConfig = isConfig;
    }

    public void setDecorationTarget(IDefinitionNode decoratingParent)
    {
        // to save space, use the parent slot for the definition we are decorating
        // we can use that to resolve our actual parent
        decoratedDefinitionNode = decoratingParent;
    }

    /**
     * Private implementation of resolve.  Can resolve to the Namespace this node refers to,
     * or it can resolve to the underlying namespace (in cases where the namespace is defined as
     * 'namespace ns1 = ns2'.
     *
     * For computing a Name for this Node, we want the namespace this node refers to (ns1 in the above example).
     * But for computing the URI for the namespace, we want to resolve to ns2, which is the actual namespace value
     * it will hold.
     * @param project               project to resolve things in
     * @param resolveToConcreteNs   true if this method should resolve to the actual underlying namespace,
     * @return                      the definition this node refers to.
     */
    private IDefinition resolve (ICompilerProject project, boolean resolveToConcreteNs)
    {
        if (isBuiltinNamespaceIdentifier())
        {
            // Resolve public, private, internal, or protected special-like
            // this is so exprs like public::x work correctly.
            ASScope scope = getASScope();
            INamespaceReference nsRef = NamespaceDefinition.createNamespaceReference(scope, this);
            return nsRef != null ? nsRef.resolveNamespaceReference(project) : null;
        }

        IDefinition d = super.resolve(project);

        if (resolveToConcreteNs &&
                d instanceof NamespaceDefinition.INamepaceDeclarationDirective)
        {
            // Resolve to the actual underlying namespace, in case this namespace was defined as:
            // namespace ns1 = otherNamespace;
            d = ((NamespaceDefinition.INamepaceDeclarationDirective)d).resolveConcreteDefinition(project);
        }
        else if (d instanceof InterfaceDefinition)
        {
            // If an interface was used as the qualifier, then we really want to use the interface
            // namespace as the qualifier.
            d = ((InterfaceDefinition)d).getInterfaceNamespaceReference();
        }
        return d;
    }

    /**
     * Helper method to determine if this node references one of the builtin
     * access namespaces these consist of public, private, protected, internal,
     * and the any namespace - '*'
     * Helper method to determine if this node references one of the built-in
     * access namespaces. These consist of <code>public</code>,
     * <code>private</code>, <code>protected</code>, <code>internal</code>,
     * and the 'any' namespace <code>*</code>.
     * Resolve this namespace reference to a set of 1 or more namespaces.
     *
     * Some qualifiers, such as "public" or "protected" may resolve to more than 1 namespace when they
     * are used as the qualifier of an expression, such as:
     *
     *   f.public::foo;
     *
     * @param project   The project to resolve things in
     * @return          The qualifier(s) that this namespace node referred to
     */
    public IQualifiers resolveQualifier(ICompilerProject project )
    {
        IQualifiers result = null;
        if( isBuiltinNamespaceIdentifier() )
        {
            ASScope scope = getASScope();
            // Only do the multi namespace processing if there is a decent chance
            // we will end up with many namespaces
            if( NamespaceDefinition.qualifierCouldBeManyNamespaces(scope, this) )
            {
                // Get all the namespace refs
                Collection<INamespaceReference> nsrefs = NamespaceDefinition.createNamespaceReferencesForQualifier(scope, this);
                Set<INamespaceDefinition> namespaces = new HashSet<INamespaceDefinition>(nsrefs.size());
                for( INamespaceReference ns : nsrefs )
                {
                    namespaces.add(ns.resolveNamespaceReference(project));
                }
                // Return an IQualifiers implementation that deals with multiple namespaces
                result = new MultiNamespaceQualifiers(namespaces);
            }
        }
        if( result == null )
        {
            // didn't find multiple namespaces, so just go down the normal resolution
            // path.
            IDefinition d = resolve(project);
            if( d instanceof IQualifiers )
                result = (IQualifiers)d;
        }
        return result;
    }

    /**
     * Helper class to return multiple namespaces
     */
    private static class MultiNamespaceQualifiers implements IQualifiers
    {
        private Set<INamespaceDefinition> namespaces;
        MultiNamespaceQualifiers(Set<INamespaceDefinition> namespaces)
        {
            this.namespaces = ImmutableSet.copyOf(namespaces);
        }
        public int getNamespaceCount ()
        {
            return namespaces.size();
        }

        public Set<INamespaceDefinition> getNamespaceSet ()
        {
            return namespaces;
        }

        public INamespaceDefinition getFirst ()
        {
            return namespaces.iterator().next();
        }
    }

    /**
     * Helper method to determine if this node references one of the builtin
     * access namespaces these consist of public, private, protected, internal,
     * and the any namespace - '*'
     * 
     * @return true if this node is a reference to the public, private,
     * protected, or internal namespace
     */
    private boolean isBuiltinNamespaceIdentifier()
    {
        String name = getName();

        return name == IASKeywordConstants.PUBLIC ||
               name == IASKeywordConstants.PRIVATE ||
               name == IASKeywordConstants.PROTECTED ||
               name == IASKeywordConstants.INTERNAL ||
               name == INamespaceConstants.ANY;
    }

    public boolean isExpressionQualifier()
    {
        IASNode p = getParent();
        if( p instanceof NamespaceAccessExpressionNode )
        {
            return ((NamespaceAccessExpressionNode) p).getLeftOperandNode() == this;
        }
        return false;
    }
}
