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

import static org.apache.royale.abc.ABCConstants.CONSTANT_Multiname;
import static org.apache.royale.abc.ABCConstants.CONSTANT_MultinameA;
import static org.apache.royale.abc.ABCConstants.CONSTANT_Qname;
import static org.apache.royale.abc.ABCConstants.CONSTANT_QnameA;
import static org.apache.royale.abc.ABCConstants.CONSTANT_RTQname;
import static org.apache.royale.abc.ABCConstants.CONSTANT_RTQnameA;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import antlr.Token;

import com.google.common.collect.ImmutableSet;

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IQualifiers;
import org.apache.royale.compiler.definitions.IVariableDefinition.VariableClassification;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.INamespaceResolvedReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.definitions.AmbiguousDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinitionBase;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.metadata.DefaultPropertyTagNode;
import org.apache.royale.compiler.internal.tree.as.metadata.EffectTagNode;
import org.apache.royale.compiler.internal.tree.as.metadata.EventTagNode;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.internal.tree.as.metadata.StyleTagNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.IScopedDefinitionNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.mxml.IMXMLMetadataNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * ActionScript parse tree node representing an identifier
 */
public class IdentifierNode extends ExpressionNodeBase implements IIdentifierNode
{
    /**
     * Create a dummy {@code IdentifierNode} after the given token. This is used
     * to repair the AST where an identifier node is expected.
     * 
     * @param token The empty ID node will have start offset following this
     * token.
     * @return Dummy identifier node.
     */
    public static IdentifierNode createEmptyIdentifierNodeAfterToken(final Token token)
    {
        final IdentifierNode result = new IdentifierNode("");
        result.startAfter(token);
        result.endAfter(token);
        return result;
    }

    /**
     * Constructor.
     */
    public IdentifierNode(String name)
    {
        this.name = name;
    }

    /**
     * Constructor.
     */
    public IdentifierNode(String name, Token token)
    {
        this.name = name;
        span(token);
    }

    /**
     * Constructor.
     */
    public IdentifierNode(String name, IASToken token)
    {
        this.name = name;
        span(token);
    }

    /**
     * Constructor.
     */
    public IdentifierNode(IASToken token)
    {
        name = token.getText();
        span(token);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected IdentifierNode(IdentifierNode other)
    {
        super(other);
        
        this.name = other.name;
    }
    
    /**
     * The name of this identifier.
     */
    private String name;

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.IdentifierID;
    }

    @Override
    public boolean isTerminal()
    {
        return true;
    }

    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (IASLanguageConstants.arguments.equals(name))
        {
            if (!this.isMemberRef() && !this.isQualifiedRef() && set.contains(PostProcessStep.POPULATE_SCOPE))
            {
                FunctionNode fn = (FunctionNode)getAncestorOfType(FunctionNode.class);
                if (fn != null)
                    fn.needsArguments = true;
            }
        }
    }
    
    @Override
    public void normalize(boolean fillInOffsets)
    {
        // do nothing
    }

    @Override
    protected void fillInOffsets()
    {
        //we gain our offsets externally, no reason to calculate this
    }

    @Override
    public IScopedNode getScopeNode()
    {
        IASNode parent = getParent();
        
        if (parent instanceof IterationFlowNode)
        {
            // we're a label for a break statement
            return null;
        }
        
        // If our parent is a DefaultPropertTag [DefaultProperty("property")]
        // then it is referencing a property that is not in the same scope;
        // so find the MemberedNode it is above and use that scope instead.
        if (parent instanceof DefaultPropertyTagNode)
        {
            // Get containing block.
            IASNode node = parent.getAncestorOfType(BlockNode.class);
            if (node instanceof BlockNode)
            {
                int childCount = node.getChildCount();
                boolean descend = false;
                for (int i = 0; i < childCount; i++)
                {
                    IASNode child = node.getChild(i);
                    if (!descend && child instanceof MetaTagsNode)
                    {
                        IMetaTagNode[] allTags = ((MetaTagsNode)child).getAllTags();
                        for (int t = 0; t < allTags.length; t++)
                        {
                            if (allTags[t] == parent)
                            {
                                descend = true;
                                break;
                            }
                        }
                    }
                    if (descend && child instanceof MemberedNode)
                    {
                        return ((MemberedNode)child).getScopedNode().getScopeNode();
                    }
                }
            }
        }
        
        else if (parent instanceof EventTagNode ||
                 parent instanceof EffectTagNode ||
                 parent instanceof StyleTagNode)
        {
            IASNode parentNode = parent.getParent();
            if (parentNode instanceof MetaTagsNode)
            {
                IASNode decoratedNode = ((MetaTagsNode)parentNode).getDecoratedDefinition();
                if (decoratedNode instanceof MemberedNode)
                    return ((MemberedNode)decoratedNode).getScopedNode().getScopeNode();
            }
            else if (parentNode instanceof IMXMLMetadataNode)
            {
                return ((IScopedDefinitionNode)((IMXMLMetadataNode)parentNode).getParent()).getScopedNode();
            }
            // for these case, we don't need to scan locals so skip up to the membered node here
        }
        
        else if ((parent instanceof ParameterNode &&
                 ((ParameterNode)parent).getTypeNode() != null &&
                 ((ParameterNode)parent).getTypeNode().equals(this)) ||
                 (parent instanceof VariableNode && ((VariableNode)parent).getTypeNode() != null &&
                  ((VariableNode)parent).getTypeNode().equals(this)))
        {
            while (!(parent instanceof MemberedNode) && !(parent instanceof IFileNode))
            {
                parent = parent.getParent();
            }
            return ((NodeBase)parent).getScopeNode();
        }
        
        else if (parent instanceof ParameterNode &&
                 parent.getParent() instanceof CatchNode)
        {
            return ((CatchNode)parent.getParent());
        }
        
        else if (parent instanceof ClassNode &&
                 ((ClassNode)parent).getBaseClassNode() != null &&
                 ((ClassNode)parent).getBaseClassNode().equals(this))
        {
            while (!(parent instanceof PackageNode) && !(parent instanceof IFileNode))
            {
                parent = parent.getParent();
            }
            return ((NodeBase)parent).getScopeNode();
        }
        
        return super.getScopeNode();
    }

    /*
     * For debugging only. Builds a string such as <code>"i"</code> from the
     * name of the identifier.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getName());
        sb.append('"');

        return true;
    }

    private IDefinition idDef = null;
    //
    // ExpressionNodeBase overrides
    //

    @Override
    public IDefinition resolve(ICompilerProject project)
    {
    	if (DefinitionBase.getPerformanceCachingEnabled() && idDef != null)
    		return idDef;
    	
        ASScope asScope = getASScope();

        if (asScope == null)
            return null;

        // attributes are not statically knowable
        if (this.isAttributeIdentifier())
            return null;

        // If this is a reference to a known package, then we can't resolve to anything
        if (isPartOfPackageReference())
            return null;

        IDefinition result = null;
        final String name = getName();
        IQualifiers qualifier = null;

        if (isQualifiedRef())
        {
            qualifier = resolveQualifier(project);

            // If we can't resolve the qualifier, then we can't resolve the entire expression
            if (qualifier == null)
                return null;
        }

        boolean isMemberRef = isMemberRef();
        boolean wasMemberRef = isMemberRef;

        if (isMemberRef && baseIsPackage())
        {
            // If our base refers to a package, then we're really a qualified name
            // and not a regular member acces (a.b.C means find C in package a.b, if a.b is a package)
            // convert the name to a fully qualified name, and look that up via findProperty
            ExpressionNodeBase base = getBaseExpression();

            String packageName = base.computeSimpleReference();
            packageName = project.getActualPackageName(packageName);
            Workspace workspace = (Workspace)project.getWorkspace();
            qualifier = workspace.getPackageNamespaceDefinitionCache().get(packageName, false);

            // Set this to false, so we'll fall through to the findProperty case
            // below, instead of trying to call resolveMemberRef
            isMemberRef = false;
        }
        if (isNameNode())
        {
            // If we are the name node for a declaration, just grab the definition
            // don't have to look anywhere else 
            IDefinitionNode defNode = getParentAsDefinition();
            if (defNode != null)
                result = defNode.getDefinition();
        }
        else if (isMemberRef)
        {
            result = resolveMemberRef(project, asScope, name, qualifier);
        }
        else
        {
            if (qualifier == null)
            {
            	DependencyType dt = getDependencyType();
                result = asScope.findProperty(project, name, dt, isTypeRef());
                if (result != null && name.equals("graphics") && (result.getParent() instanceof ITypeDefinition) && ((ITypeDefinition)(result.getParent())).isInstanceOf("mx.core.UIComponent", project))
                	result = asScope.findProperty(project, "royalegraphics", getDependencyType(), isTypeRef());
                // ASVariableTests_localVarSameNameAsPrivateMethod
                if (isLegacyCodegen(project) && result != null && getParent().getNodeID() == ASTNodeID.FunctionCallID && result instanceof VariableDefinition)
                {
                    VariableDefinition varDef = (VariableDefinition)result;
                    if (varDef.getVariableClassification() == VariableClassification.LOCAL)
                    {
                        ClassDefinitionBase cdef = asScope.getContainingClass();
                        IDefinition memberResult = asScope.getPropertyFromDef(project, cdef, name, false);
                        if (memberResult instanceof FunctionDefinition)
                            result = memberResult;
                    }
                }
            }
            else {
                result = asScope.findPropertyQualified(project, qualifier, name, getDependencyType(), isTypeRef());
                if (result == null && wasMemberRef && baseIsPackage())
                {
                    // if we get here it was because there is a memberaccessexpression like "a.b.foo"
                    // that did not resolve because a.b is a package but foo isn't a class.  There is a chance that
                    // "a" by itself is a package and there is a class "b" with a member called "foo" so
                    // look for that
                    result = resolveMemberRef(project, asScope, name, null);
                }
            }
        }

        if (result != null && project instanceof RoyaleProject && ((RoyaleProject)project).apiReportFile != null)
        {
        	if (isMemberRef())
        	{
        		// if member ref, try to resolve the left side, because otherwise the parent definition
        		// will be the class that defined this property like ListCollectionView.sort instead
        		// of the subclass the developer was using on, like ArrayCollection.  We want to log
        		// ArrayCollection.sort in case we don't need to implement full class hierarchy.
        		// A Royale ArrayCollection might not have to subclass ListCollectionView
        		if (parent.getNodeID() == ASTNodeID.MemberAccessExpressionID)
        		{
        			MemberAccessExpressionNode mae = (MemberAccessExpressionNode)parent;
        			if (mae.getRightOperandNode() == this && mae.getLeftOperandNode().getNodeID() == ASTNodeID.IdentifierID)
        			{
        				// if the member access expression left side is not an identifier
        				IDefinition parentDef = mae.getLeftOperandNode().resolveType(project);
        				if (parentDef instanceof IClassDefinition)
        				{
        	        		((RoyaleProject)project).addToAPIReport((IClassDefinition)parentDef, result);        					
        				}
        				else
        				{
        	        		((RoyaleProject)project).addToAPIReport(result);        					
        				}
        			}
        			else
    				{
    	        		((RoyaleProject)project).addToAPIReport(result);        					
    				}
        		}
    			else
				{
	        		((RoyaleProject)project).addToAPIReport(result);        					
				}
        	}
        	else
        		((RoyaleProject)project).addToAPIReport(result);
        }
        
        idDef = result;
        return result;
    }

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        IDefinition def = resolve(project);

        if (def != null)
        {
            if (SemanticUtils.isXMLish(def.getParent(), project))
            {
                // XML and XMLList members should be treated as '*' because any access could
                // resolve to some content inside the XML (i.e. it has a child tag named 'name').
                // return '*' since we can't statically know what the type will be.
                // Compat with ASC behavior.
                return project.getBuiltinType(IASLanguageConstants.BuiltinType.ANY_TYPE);
            }
            return def.resolveType(project);
        }

        return null;
    }
    
    @Override
    protected IdentifierNode copy()
    {
        return new IdentifierNode(this);
    }
    
    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        // If this is a package reference, we're not dynamic.
        if (isPackageReference())
            return false;

        return super.isDynamicExpression(project);
    }

    @Override
    public Name getMName(ICompilerProject project)
    {
        // If we can resolve the reference, then just use the QName
        // of whatever we resolved to
        IDefinition def = resolve(project);

        if (canEarlyBind(project, def))
            return ((DefinitionBase)def).getMName(project);

        if (def == null)
        {
            // no def? see if this is dynamic
            // or a missing member on a known and non-dynamic def
            if (getParent().getNodeID() == ASTNodeID.MemberAccessExpressionID)
            {
                // ok it is a member expression, are we on the right?
                MemberAccessExpressionNode mae = (MemberAccessExpressionNode) getParent();
                if (mae.getRightOperandNode() == this)
                {
                    // we're on the right, what is the left?
                    ITypeDefinition leftDef = mae.getLeftOperandNode().resolveType(project);
                    if (leftDef != null && leftDef.isDynamic() == false)
                    {
                        INamespaceReference nr = leftDef.getNamespaceReference();
                        INamespaceDefinition nd = nr.resolveNamespaceReference(project);
                        Set<INamespaceDefinition> nsset = ImmutableSet.of((INamespaceDefinition)nd);
                        return makeName(nsset, getName(), isAttributeIdentifier());
                    }
                }
            }
        }
        ASScope scope = getASScope();

        if (isQualifiedRef())
        {
            // If we're a qualified name, and the qualifier resolves to a compile
            // time constant, return a QName
            // otherwise emit a RTQname - the CG will have to take care of generating the code to 
            // evaluate the qualifier and place it on the stack.
            IQualifiers qual = resolveQualifier(project);

            Nsset namespaceSet;
            int nameKind;

            if (qual != null )
            {
                if( qual.getNamespaceCount() == 1 )
                {
                    // Qualifier resolved to 1 namespace, so we can emit a QName
                    NamespaceDefinition ns = (NamespaceDefinition)qual.getFirst();
                    nameKind = isAttributeIdentifier() ? CONSTANT_QnameA : CONSTANT_Qname;
                    if (isMemberRef())
                    {
                        ExpressionNodeBase baseExpr = getBaseExpression();
                        if (baseExpr instanceof LanguageIdentifierNode &&
                                ((LanguageIdentifierNode)baseExpr).getKind() == LanguageIdentifierKind.SUPER)
                        {
                            // If we're a super expression, adjust the namespace in case it's the protected namespace
                            IDefinition baseType = baseExpr.resolveType(project);
                            Set<INamespaceDefinition> nsset = ImmutableSet.of((INamespaceDefinition)ns);
                            nsset = scope.adjustNamespaceSetForSuper(baseType, nsset);
                            // We only started with 1 namespace, so we know that's how many we have
                            ns = (NamespaceDefinition)nsset.iterator().next();
                        }
                    }
                    // If the qualifier is the any namespace, then we want a null nsset
                    // instead of a nsset of length 1, with a null namespace in it.
                    if( ns == NamespaceDefinition.getAnyNamespaceReference() )
                        namespaceSet = null;
                    else
                        namespaceSet = new Nsset(ns.getAETNamespace());
                }
                else
                {
                    // qualifier resolve to 1+ namespaces, so emit a multiname
                    Set<INamespaceDefinition> nsset = qual.getNamespaceSet();
                    nameKind = isAttributeIdentifier() ? CONSTANT_MultinameA : CONSTANT_Multiname;

                    namespaceSet = SemanticUtils.convertSetINamespaceToNsset(nsset);
                }
            }
            else
            {
                namespaceSet = null;
                nameKind = isAttributeIdentifier() ? CONSTANT_RTQnameA : CONSTANT_RTQname;
            }

            return new Name(nameKind, namespaceSet, getName());
        }

        Name name = null;

        if (isMemberRef())
        {
            ExpressionNodeBase baseExpr = getBaseExpression();
            if (baseExpr != null)
            {
                // Handle the case where we look like a member expression, but the base expression is really
                // a reference to a package.  For example 'a.b.c.Foo' where a.b.c is a known package name.
                // This needs to generate a QName with a.b.c as the qualifier.
                if (baseIsPackage())
                {
                    String packageName = baseExpr.computeSimpleReference();
                    Workspace workspace = (Workspace)project.getWorkspace();
                    INamespaceReference qualifier = workspace.getPackageNamespaceDefinitionCache().get(packageName, false);

                    return new Name(isAttributeIdentifier() ? CONSTANT_QnameA : CONSTANT_Qname,
                            new Nsset(((INamespaceResolvedReference)qualifier).resolveAETNamespace(project)), getName());
                }

                Set<INamespaceDefinition> namespaceSet = null;
                IDefinition baseType = baseExpr.resolveType(project);

                // If our base type is an interface,  then we need to use the special
                // interface namespace set (the namespace of the interface, plus the namespaces for all the interfaces
                // it extends)
                if (baseType instanceof InterfaceDefinition)
                {
                    namespaceSet = ((InterfaceDefinition)baseType).getInterfaceNamespaceSet(project);
                }
                else if (baseExpr instanceof LanguageIdentifierNode &&
                         ((LanguageIdentifierNode)baseExpr).getKind() == LanguageIdentifierKind.SUPER)
                {
                    namespaceSet = scope.getNamespaceSetForSuper(project, baseType);
                }

                if (namespaceSet != null)
                    return makeName(namespaceSet, getName(), isAttributeIdentifier());
            }
        }

        if (isNameNode())
        {
            BaseDefinitionNode defNode = getParentAsDefinition();
            name = defNode.getDefinition().getMName(project);
        }
        else if (scope != null)
        {

            Set<INamespaceDefinition> namespaceSet = null;
            if (isMemberRef())
            {
                // Member refs just use the open namespace set
                namespaceSet = scope.getNamespaceSet(project);
            }
            else
            {
                // lexical refs may be influenced by the imports
                namespaceSet = scope.getNamespaceSetForName(project, getName());
            }

            name = makeName(namespaceSet, getName(), isAttributeIdentifier());
        }

        return name;
    }

    @Override
    String computeSimpleReference()
    {
        return getName();
    }
    
    @Override
    IReference computeTypeReference()
    {
        // Parser creates IdentifierNodes for "" for some error cases of improperly written code,
        // like class C extends {} - it gets an IDNode of "" for the extends clause
        if (name == "")
            return null;

        IReference typeRef = null;

        IWorkspace w = getWorkspace();

        switch (getRefType())
        {
            case PACKAGE_QUALIFIED:
            {
                ExpressionNodeBase baseExpr = getBaseExpression();
                typeRef = ReferenceFactory.packageQualifiedReference(w, baseExpr.computeSimpleReference(), computeSimpleReference(), false);
                break;
            }
            case LEXICAL:
            {
                typeRef = ReferenceFactory.lexicalReference(w, computeSimpleReference());
                break;
            }
            default:
            {
                typeRef = ReferenceFactory.notATypeReference(w, computeSimpleReference());
                break;
            }
        }

        return typeRef;
    }

    @Override
    public INamespaceReference computeNamespaceReference()
    {
        // Parser creates IdentifierNodes for "" for some error cases
        // of improperly written code,
        if (name == "")
            return null;
        
        INamespaceReference nsRef = null;
        Workspace w = (Workspace)getWorkspace();

        switch (getRefType())
        {
            case PACKAGE_QUALIFIED:
            {
                // Create a reference with the package name as the qualifier
                //    a.b.ns1
                // where a.b is a package name
                ExpressionNodeBase baseExpr = getBaseExpression();
                nsRef = NamespaceDefinition.createNamespaceReference(
                     getASScope(), getName(),
                     w.getPackageNamespaceDefinitionCache().get(baseExpr.computeSimpleReference(), false));
                break;
            }
            case LEXICAL:
            {
                // no qualifer, just a lexical ref
                nsRef = NamespaceDefinition.createNamespaceReference(getASScope(), getName(), null);
                break;
            }
            case NAMESPACE_QUALIFIED:
            {
                // Create a reference with the namespace from the qualifier expression as the qualifier
                // handles code like:
                //    ns1::ns2
                ExpressionNodeBase qualExpr = getQualifierExpression();
                nsRef = NamespaceDefinition.createNamespaceReference(getASScope(), getName(), qualExpr.computeNamespaceReference());
                break;
            }
            case MEMBER:
            {
                ExpressionNodeBase baseExpr = getBaseExpression();
                IReference base = baseExpr.computeTypeReference();
                nsRef = NamespaceDefinition.createNamespaceReference(getASScope(), getName(), null, base);
                break;
            }
            default:
            {
                break;
            }
        }
        
        return nsRef;
    }
    
    // IIdentifierNode implementations

    @Override
    public String getName()
    {
        return name;
    }
    
    @Override
    public IdentifierType getIdentifierType()
    {
        return IdentifierType.NAME;
    }

    // Other methods

    public void setReferenceValue(IDefinition definition)
    {
    }

    /**
     * Determines if this identifier is actually implicit and does not exist in source.
     */
    public boolean isImplicit()
    {
        return getAbsoluteStart() == getAbsoluteEnd();
    }

    private REF_TYPE getRefType()
    {
        if (isMemberRef())
        {
            // If this is a member expr, then the stem better be a package;
            // otherwise we can't possibly be a type ref.
            if (baseIsPackage())
                return REF_TYPE.PACKAGE_QUALIFIED;
            else
                return REF_TYPE.MEMBER;
        }
        else if (isQualifiedRef())
        {
            return REF_TYPE.NAMESPACE_QUALIFIED;    
        }
        else
        {
            return REF_TYPE.LEXICAL;
        }
    }

     /**
     * Determine if this identifier is an attribute reference (e.g. @name).
     * 
     * @return True if this node is an attribute
     */
    protected boolean isAttributeIdentifier()
    {
        ExpressionNodeBase p = getParentExpression();
        if (p != null)
            return p.isAttributeExpr(this);
        return false;
    }

    /**
     * Is this a reference that has an explicit qualifier. e.g. the node for 'b'
     * in a::b woul return ture.
     * 
     * @return true if this node should be resolved with an explicit qualifier
     */
    private boolean isQualifiedRef()
    {
        ExpressionNodeBase expr = getParentExpression();

        if (expr != null)
            return expr.isQualifiedExpr(this);

        return false;
    }

    /**
     * Determine if this identifier node is really part of a package reference.
     * this would be the node for b in 'a.b.c' if a.b.c was a known package name.
     * @return  true if this identifier is used as part of a package reference.
     */
    private boolean isPartOfPackageReference()
    {
        ExpressionNodeBase expr = this.getParentExpression();
        MemberAccessExpressionNode memExpr = null;

        while (expr instanceof MemberAccessExpressionNode)
        {
            memExpr = (MemberAccessExpressionNode)expr;
            expr = expr.getParentExpression();
        }

        if (memExpr != null)
            return memExpr.isPackageReference();

        return false;
    }
    
    /**
     * Is this a reference that should be resolve in another object e.g. the
     * node for 'b' in a.b would return true
     * 
     * @return true if this node should be resolved in another object
     */
    public boolean isMemberRef()
    {
        ExpressionNodeBase expr = getParentExpression();

        boolean memberRef = false;
        
        if (expr != null)
            return expr.isPartOfMemberRef(this);

        return memberRef;
    }

    /**
     * This method resolves an identifier that is the right-hand-side of a member access expression
     * (i.e., <code>b</code> in <code>a.b</code> or <code>a.ns::b</code>).
     */
    private IDefinition resolveMemberRef(ICompilerProject project, ASScope asScope, String name, IQualifiers qualifier)
    {
        IDefinition result = null;

        // Determine baseType, the type of 'a' (the left-hand-side of the member access operator).
        ITypeDefinition baseType = null;
        ExpressionNodeBase baseExpr = getBaseExpression();
        if (baseExpr != null)
        {
            baseType = baseExpr.resolveType(project);

            if (baseType != null)
            {
                boolean isSuper = false;
                if (baseType instanceof IClassDefinition)
                {
                    // If the base type is XML or XMLList,
                    // don't resolve a member reference to any definition.
                    // As in the old compiler, this avoids possibly bogus
                    // -strict type-coercion errors.                   
                    // For example, we don't want a declared property like .name
                    // to resolve to the method slot, because it might actually
                    // be referring to a dynamically-defined XML attribute or child tag.
                    // And if we did resolve it to the name() method, which returns Object,
                    // then when doing s = x.name() where s is type String
                    // and x is type XML you would get a can't-convert-Object-to-String
                    // problem, but there is lots of existing source code that expects
                    // this to compile with no cast.
                    if (!((RoyaleProject)project).useStrictXML() && SemanticUtils.isXMLish(baseType, project))
                        return null;
                    
                    if (baseExpr instanceof IdentifierNode)
                    {
                        IdentifierNode idBase = (IdentifierNode)baseExpr;
                        if (idBase instanceof ILanguageIdentifierNode &&
                                (((ILanguageIdentifierNode)idBase).getKind() == LanguageIdentifierKind.THIS
                                || ((ILanguageIdentifierNode)idBase).getKind() == LanguageIdentifierKind.SUPER))
                        {
                            if (((ILanguageIdentifierNode)idBase).getKind() == LanguageIdentifierKind.SUPER)
                            {
                                isSuper = true;
                            }
                        }
                    }
                }
                if (qualifier != null)
                    result = asScope.getQualifiedPropertyFromDef(project, baseType, name, qualifier, isSuper);
                else if (name.equals("graphics") && baseType.isInstanceOf("mx.core.UIComponent", project))
                {
                	result = asScope.getPropertyFromDef(project, baseType, "royalegraphics", isSuper);
                    if (result == null)
                        result = asScope.getPropertyFromDef(project, baseType, name, isSuper);
                }
                else
                    result = asScope.getPropertyFromDef(project, baseType, name, isSuper);
            }
        }

        return result;
    }

    /**
     * Resolve the qualifier of this IdentifierNode, if the IDentifierNode is
     * part of a QualifiedExpression
     * 
     * @param project The {@link ICompilerProject} to use to do lookups.
     * @return The IQualifiers the qualifier resolved to, or null if it
     * was unresolved.  A single qualifier may resolve to multiple namespaces
     */
    private IQualifiers resolveQualifier(ICompilerProject project)
    {
        IQualifiers qual = null;

        ExpressionNodeBase qualExpr = getQualifierExpression();
        if (qualExpr != null)
        {
            if( qualExpr instanceof NamespaceIdentifierNode )
            {
                // namespace reference, have the namespace
                // node resolve as a qualifier
                qual = ((NamespaceIdentifierNode) qualExpr).resolveQualifier(project);
            }
            else
            {
                // Some random expression, just try and resolve it
                IDefinition def = qualExpr.resolve(project);
                if (def instanceof NamespaceDefinition)
                    qual = (NamespaceDefinition)def;
            }
        }

        return qual;
    }

    /**
     * Get the ExpressionNodeBase of the qualifier if this Node is part of a
     * qualified expression
     */
    private ExpressionNodeBase getQualifierExpression()
    {
        ExpressionNodeBase expr = getParentExpression();
        if (expr != null)
            return expr.getQualifier(this);
        return null;
    }

    /**
     * For member refs, determine if the base expression is really a reference
     * to a known package name.
     * 
     * @return true, if the base expression should be treated as a package name
     */
    private boolean baseIsPackage()
    {
        ExpressionNodeBase expr = getParentExpression();
        if (expr instanceof MemberAccessExpressionNode)
            return ((MemberAccessExpressionNode)expr).stemIsPackage();
        return false;
    }

    /**
     * Helper method to get the parent node as an IDefinitionNode
     * 
     * @return the parent IDefinitionNode, or null if the parent is not an
     * IDefinitionNode
     */
    private BaseDefinitionNode getParentAsDefinition()
    {
        IASNode p = getParent();
        BaseDefinitionNode def = p instanceof BaseDefinitionNode ? (BaseDefinitionNode)p : null;
        return def;
    }

    /**
     * Helper method to determine if this node is the name node of a definition.
     * 
     * @return true if this Node is the Name node of a definition
     */
    private boolean isNameNode()
    {
        IDefinitionNode parent = getParentAsDefinition();
        if (parent != null)
        {
            if (parent.getNameExpressionNode() == this)
                return true;
        }
        return false;
    }

    /**
     * Can this Node be early bound to the Definition it refers to.
     * We can early bind if we resolve to a definition, the definition is not ambiguous, and
     * the definition is not something defined in one of the XML classes (XML or XMLList).
     * @param project   project to resolve things in
     * @param def       the definition this node resolved to
     * @return          true if we can early bind, otherwise false
     */
    protected boolean canEarlyBind(ICompilerProject project, IDefinition def)
    {
        if (def instanceof DefinitionBase && !AmbiguousDefinition.isAmbiguous(def))
        {
            // Can't early bind to XML/XMLList properties as they may be hidden by the unknown contents
            // of the XML itself, i.e. a child tag named 'parent'
            // Matches ASC behavior.
            if (!SemanticUtils.isXMLish(def.getParent(), project))
                return true;
        }
        return false;
    }

    /**
     * Helper method to make an AET name from a Set<INamespaceDefinition> and a
     * name.
     * 
     * @param namespaceSet the set of namespaces to use for the name
     * @param name the name to use for the AET Name.
     * @return the AET Name
     */
    private static Name makeName(Set<INamespaceDefinition> namespaceSet, String name, boolean isAttr)
    {
        int nameKind = isAttr ? CONSTANT_MultinameA : CONSTANT_Multiname;
        Nsset nsSet = SemanticUtils.convertSetINamespaceToNsset(namespaceSet);
        Name n = new Name(nameKind, nsSet, name);
        return n;
    }

    /**
     * Is this a type reference - e.g. a base class ref, type anno, etc
     * 
     * @return true if this is a type reference
     */
    protected boolean isTypeRef()
    {
        DependencyType dt = getDependencyType();
        if (dt == DependencyType.SIGNATURE || dt == DependencyType.INHERITANCE)
            return true;
        if (dt == DependencyType.EXPRESSION)
        {
            final IASNode parent = getParent();
            if (parent instanceof BaseTypedDefinitionNode && this == ((BaseTypedDefinitionNode)parent).getTypeNode())
                return true;
        }

        return false;
    }

    public boolean isLegacyCodegen(ICompilerProject project)
    {
        if (!(project instanceof RoyaleProject))
            return false;
        final Integer compatibilityVersion = ((RoyaleProject)project).getCompatibilityVersion();
        if (compatibilityVersion == null)
            return false;
        else if (compatibilityVersion <= Configuration.MXML_VERSION_4_6)
            return true;
        else
            return false;
    }
    
    //
    // Inner types
    //
    
    private static enum REF_TYPE
    {
        /**
         * A Member expression where the stem is a package name
         */
        PACKAGE_QUALIFIED,

        /**
         * A lexical ref
         */
        LEXICAL,

        /**
         * A name where the base/qualifier is a runtime value
         */
        RUNTIME,

        /**
         * A reference qualified by a namespace expression, such as ns1::x
         */
        NAMESPACE_QUALIFIED,

        /**
         * A member reference, such as a.b.Foo
         */
        MEMBER
    }
}
