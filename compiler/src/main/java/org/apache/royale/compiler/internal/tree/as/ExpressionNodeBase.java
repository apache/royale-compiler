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

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.WithScope;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IScopedNode;

/**
 * ActionScript parse tree node representing an expression
 */
public abstract class ExpressionNodeBase extends FixedChildrenNode implements IExpressionNode
{
    private static final int FLAG_HAS_PARENS = 0x8000;
    
    private static final int FLAG_TREAT_AS_PACKAGE = 0x4000;
    
    /**
     * Constructor.
     */
    public ExpressionNodeBase()
    {
    }

    /**
     * Copy constructor.
     * 
     * @param other The node to copy.
     */
    protected ExpressionNodeBase(ExpressionNodeBase other)
    {
        this.flags = other.flags;
        //this.fParent = other.fParent;
        this.setSourcePath(other.getSourcePath());
        this.setLine(other.getLine());
        this.setColumn(other.getColumn());
        this.setStart(other.getAbsoluteStart());
        this.setEnd(other.getAbsoluteEnd());
        this.setEndLine(other.getEndLine());
        this.setEndColumn(other.getEndColumn());
    }

    protected int flags;
    
    //
    // NodeBase overrides
    //
    
    /**
     * Normalize the tree. Move custom children into the real child list and
     * fill in missing offsets so that offset lookup will work. Used during
     * parsing.
     */
    @Override
    public void normalize(boolean fillInOffsets)
    {
        setChildren(fillInOffsets);
        
        // The list of children doesn't change, so the child count should be constant throughout the loop
        int childrenSize = getChildCount();
        
        // Normalize the regular children
        for (int i = 0; i < childrenSize; i++)
        {
            IASNode child = getChild(i);
            if (child instanceof NodeBase)
                ((NodeBase)child).normalize(fillInOffsets);
        }
        
        if (fillInOffsets)
            fillInOffsets();
    }
    
    @Override
    public ASScope getASScope()
    {
        ASScope scope = super.getASScope();

        if (scope instanceof WithScope)
        {
            // If this expression is part of the target expression of a with node
            // then we want the scope containing the with scope, not the with scope itself.
            IExpressionNode baseExpr = this.getDecorationNode();
            IASNode parent = baseExpr.getParent();
            if (parent instanceof WithNode)
            {
                WithNode withParent = (WithNode)parent;
                if (withParent.getTargetNode() == baseExpr)
                {
                    return withParent.getASScope();
                }
            }
        }
        return scope;
    }
    
    //
    // IExpressionNode implementations
    //
    
    @Override
    public IDefinition resolve(ICompilerProject project)
    {
        // For all expression nodes except for the MemberAccessExpressionNode and the IdentifierNode,
        // this method returns null.
        return null;
    }

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        // TODO: implement for every expression type - currently only works for IdentifierNodes
        return null;
    }
    
    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        boolean isDynamic = true;

        ITypeDefinition type = resolveType(project);
        if (type != null)
            isDynamic = type.isDynamic();

        if (!isDynamic)
        {
        	String qName = type.getQualifiedName();
        	isDynamic = qName.equals(IASLanguageConstants.XML) || qName.equals(IASLanguageConstants.XMLList);
        }
        return isDynamic;
    }
    
    public ExpressionNodeBase copyForInitializer(IScopedNode scopedNode)
    {
        ExpressionNodeBase newExpr = copy();
        
        if (newExpr != null)
        {
            // If we copied the expression successfully,
            // set up all the parent pointers within the new subtree.
            if (scopedNode instanceof NodeBase)
            {
                NodeBase node = (NodeBase) scopedNode;
                newExpr.setParent(node);
            }
            newExpr.normalize(false);
        }
        
        return newExpr;
    }

    @Override
    public boolean hasParenthesis()
    {
        return (flags & FLAG_HAS_PARENS) != 0;
    }
    
    //
    // Other methods
    //

    /**
     * Copy the ExpressionNodeBase and its subtree.
     * 
     * @return  a new ExpressionNodeBase that represents the same content as the original node
     */
    protected abstract ExpressionNodeBase copy();

    /**
     * Gets the AET {@link Name} object to be used for an expression.
     * <p>
     * For all expression nodes except for the identifier nodes and member
     * access nodes, this method returns <code>null</code>.
     * <p>
     * This is the method that the code generator calls to determine what name
     * to emit into the ABC file. It will handle resolving the identifier if
     * necessary and generating the appropriate qname or multiname.
     * 
     * @param project The {@link ICompilerProject} to use to do lookups.
     * @return A AET {@link Name} object.
     */
    public Name getMName(ICompilerProject project)
    {
        return null;
    }

    public void setHasParenthesis(boolean hasParens)
    {
        if (hasParens)
            flags |= FLAG_HAS_PARENS;
        else
            flags &= ~FLAG_HAS_PARENS;
    }

    /**
     * Determine if this node is a reference to a package
     * 
     * @return true if this node is really a package reference.
     */
    public boolean isPackageReference()
    {
        // Return true if we're somewhere that MXML doesn't require packages to be imported
        if (getTreatAsPackage())
            return true;

        boolean isPackage = false;
        if (this.getBaseExpression() == null)
        {
            // Only check for packages if we don't have a base expression.
            // in a.b.c, 'a', 'a.b', or 'a.b.c' could be package names, but not 'c' or 'b.c'
            String ref = this.computeSimpleReference();
            if (ref != null)
            {
                ASScope scope = getASScope();

                if (scope.isPackageName(ref))
                    isPackage = true;
            }
        }
        return isPackage;
    }
    
    /**
     * Get the base expression, if any. In <code>a.b</code>, or
     * <code>a.@b</code>, or <code>a.c::b</code>, the base of <code>b</code> is
     * <code>a</code>.
     * 
     * @return The base as an ExpressionNodeBase, if it is one, otherwise null
     */
    public ExpressionNodeBase getBaseExpression()
    {
        ExpressionNodeBase parent = getParentExpression();

        if (parent != null)
            return parent.getBaseForMemberRef(this);

        return null;
    }

    /**
     * @return true if this ExpressionNodeBase is in a with scope.
     */
    public boolean inWith()
    {
        ASScope scope = getASScope();
        return scope != null ? scope.isInWith() : false;
    }

    /**
     * Get the appropriate node at which to start decoration. This returns the
     * largest containing expression node, which should provide enough context
     * to figure out what the definitions are.
     * 
     * @return the node that should be decorated
     */
    public ExpressionNodeBase getDecorationNode()
    {
        IASNode current = this;
        while (current.getParent() != null && current.getParent() instanceof ExpressionNodeBase)
            current = current.getParent();
        return (ExpressionNodeBase)current;
    }
    
    /**
     * Check if the expression is inside a filter expression. For example:
     * 
     * <pre>
     * xmlData.(getName() == "hello")
     * </pre>
     * 
     * {@code getName() == "hello"} is in a filter.
     * <p>
     * The implementation walks up the parent nodes till the first
     * {@link ScopedBlockNode}. If a {@link MemberAccessExpressionNode} with
     * {@link ASTNodeID#E4XFilterID} is found, this node is inside a filter
     * expression.
     * 
     * @return True if this node is in a filter expression.
     */
    public final boolean inFilter()
    {
        IASNode parent = this.getParent();
        while (parent != null && !(parent instanceof ScopedBlockNode))
        {
            if (parent.getNodeID() == ASTNodeID.E4XFilterID)
                return true;
            else
                parent = parent.getParent();
        }
        return false;
    }
    
    /**
     * Get the parent of this Node as an ExpressionNodeBase.
     * 
     * @return the parent of this node as an ExpressionNodeBase, or null if it isn't
     * an ExpressionNodeBase.
     */
    protected ExpressionNodeBase getParentExpression()
    {
        IASNode p = getParent();
        return p instanceof ExpressionNodeBase ? (ExpressionNodeBase)p : null;
    }

    /**
     * Generate a simple reference - this is a String representing a name that
     * can be resolved with the set of open namespaces. If such a simple
     * reference can't be generated for this expression, null is returned.
     * 
     * @return A String representing the simple name that represents this node,
     * or null if no simple name can be obtained.
     */
    String computeSimpleReference()
    {
        return null;
    }

    /**
     * Generate an IReference that can serve as the type reference for something
     * like a type annotation, baseclass reference, interface reference, etc.
     * 
     * @return An IReference that represents this expression node or null.
     */
    IReference computeTypeReference()
    {
        return null;
    }

    /**
     * Generate an INamespaceReference that can serve as the namespace reference for something
     * like a namespace initializer.
     *
     * @return An INamespaceReference that represents this expression node or null.
     */
    public INamespaceReference computeNamespaceReference()
    {
        return null;
    }

    boolean isPartOfMemberRef(ExpressionNodeBase e)
    {
        return false;
    }

    ExpressionNodeBase getBaseForMemberRef(ExpressionNodeBase e)
    {
        return null;
    }

    boolean isQualifiedExpr(ExpressionNodeBase e)
    {
        return false;
    }

    ExpressionNodeBase getQualifier(ExpressionNodeBase e)
    {
        return null;
    }

    /**
     * Determine if e is part of an attribute expression
     */
    boolean isAttributeExpr(ExpressionNodeBase e)
    {
        return false;
    }

    private boolean getTreatAsPackage()
    {
        return (flags & FLAG_TREAT_AS_PACKAGE) != 0;
    }

    /**
     * When set to true, this expression will always be treated as a package -
     * the set of imports will not be checked. This is needed for some MXML
     * processing that did not require a user to import a package before using
     * it as a package (instead everything to the left of the last dot was
     * considered a package) e.g. 'a.b.Foo' would treat 'a.b' as a package name.
     * 
     * @param b the new Value of treatAsPackage
     */
    void setTreatAsPackage(boolean b)
    {
        if (b)
            flags |= FLAG_TREAT_AS_PACKAGE;
        else
            flags &= ~FLAG_TREAT_AS_PACKAGE;
    }

    /**
     * Compute the type of dependency between two compilation unit this
     * expression node creates.
     *
     * @return The type of dependency between two compilation unit this
     * identifier node creates.
     */
    public DependencyType getDependencyType()
    {
        // TODO We'd really like to make this method not dependent on the AST shape.
        // In an ideal world the parser would use information it has while building the tree to
        // compute the dependency type or some other value that can be trivially used to compute the
        // dependency type.
        final IASNode parent = getParent();

        // 
        // If we're part of an expression, then ask our parent expression what type
        // of dependency we are.
        // should handle things like the node for 'Bar' in:
        //    var t:Vector.<Vector.<Bar>>;
        //
        if( parent instanceof ExpressionNodeBase )
            return ((ExpressionNodeBase)parent).getDependencyType();

        // If the this node is part of the extends or implements expressions of a class
        // then this node creates an inheritance dependency.
        if (parent instanceof ClassNode && this == ((ClassNode)parent).getBaseClassNode())
            return DependencyType.INHERITANCE;
        if (parent instanceof ContainerNode && parent.getParent() instanceof ClassNode && parent == ((ClassNode)parent.getParent()).getInterfacesNode())
            return DependencyType.INHERITANCE;
        if (parent instanceof ContainerNode && parent.getParent() instanceof InterfaceNode && parent == ((InterfaceNode)parent.getParent()).getBaseInterfacesNode())
            return DependencyType.INHERITANCE;

        // Why we are resolving the identifier node that is the name of a class is beyond me,
        // but might we'll treat that as a signature dependency.
        if (parent instanceof ClassNode && this == ((ClassNode)getParent()).getNameExpressionNode())
            return DependencyType.SIGNATURE;

        // Identifier nodes referred to directly by a FunctionNode
        // or ParameterNode create signature dependencies unless
        // the FunctionNode is itself inside of an other FunctionNode.
        FunctionNode functionContainingReference = null;
        if (parent instanceof FunctionNode)
        {
            FunctionNode parentFunction = (FunctionNode)parent;
            if (this == parentFunction.getReturnTypeNode())
                functionContainingReference = parentFunction;
        }
        else if (parent instanceof ParameterNode)
        {
            if (this == ((ParameterNode)parent).getTypeNode())
            {
                IASNode tempNode = parent.getParent().getParent();
                if (tempNode instanceof FunctionNode)
                    functionContainingReference = (FunctionNode)tempNode;
                    // We might be in a catch node, in which case we should be ane
                    // expression dep, as theres no way a catch could influence the signature
                else if (parent.getParent() instanceof CatchNode)
                    return DependencyType.EXPRESSION;
            }
        }
        if (functionContainingReference != null)
        {
            IASNode outerFunction = functionContainingReference.getAncestorOfType(FunctionNode.class);
            if (outerFunction != null)
                return DependencyType.EXPRESSION;
            else
                return DependencyType.SIGNATURE;
        }

        // Identifier nodes that are the type annotation of a definition
        // are signature dependencies unless they are inside of a FunctionNode.
        if (parent instanceof BaseTypedDefinitionNode && this == ((BaseTypedDefinitionNode)parent).getTypeNode())
        {
            assert !((parent instanceof FunctionNode) || (parent instanceof ParameterNode));
            // variable or const type annotation
            // If we are in a function this is an expression dep, otherwise it is a signature dep.
            FunctionNode functionContainingIdentifier = (FunctionNode)getAncestorOfType(FunctionNode.class);
            if (functionContainingIdentifier != null)
                return DependencyType.EXPRESSION;
            else
                return DependencyType.SIGNATURE;
        }

        // don't add a dependency because of an import stmt.  The parent will always be an ImportNode
        // regardless of import tree shape, such as:
        // import foo;
        // import p.foo;
        // import p.*;
        if (parent instanceof ImportNode)
            return null;

        // Anything not handled by the above cases is an expression dependency.
        return DependencyType.EXPRESSION;
    }
}
