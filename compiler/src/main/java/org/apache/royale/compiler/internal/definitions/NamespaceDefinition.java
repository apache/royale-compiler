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

package org.apache.royale.compiler.internal.definitions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import org.apache.commons.io.FilenameUtils;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceResolvedReference;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.PackageScope;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.FunctionObjectNode;
import org.apache.royale.compiler.internal.tree.as.QualifiedNamespaceExpressionNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.as.INamespaceNode;
import org.apache.royale.utils.StringEncoder;

/**
 * Instances of this class represent definitions of ActionScript namespaces in
 * the symbol table.
 * <p>
 * The interfaces may have been declared with the <code>namespace</code> keyword
 * or may be ones that are implicit in the ActionScript language.
 * <p>
 * After a namespace definition is in the symbol table, it should always be
 * accessed through the read-only <code>INamespaceDefinition</code> interface.
 */
public abstract class NamespaceDefinition extends DefinitionBase implements INamespaceDefinition, INamespaceResolvedReference
{
    private static PublicNamespaceDefinition PUBLIC = new PublicNamespaceDefinition();
    private static UserDefinedNamespaceDefinition AS3 = new UserDefinedNamespaceDefinition(INamespaceConstants.AS3, INamespaceConstants.AS3URI);
    private static ICodeModelImplicitDefinitionNamespaceDefinition CM_IMPLICIT_DEF_NS = new CodeModelImplicitDefinitionNamespaceDefinition();

    /**
     * Namespace definition to represent the any namespace - '*'
     */
    private static AnyNamespaceDefinition ANY = new AnyNamespaceDefinition();

    /**
     * Gets the single public namespace definition for the whole process.
     * 
     * @return The single public namespace definition for the whole process.
     */
    public static IPublicNamespaceDefinition getPublicNamespaceDefinition()
    {
        return PUBLIC;
    }

    public static INamespaceDefinition getAS3NamespaceDefinition()
    {
        return AS3;
    }

    public static INamespaceReference getAS3NamespaceReference()
    {
        return AS3;
    }

    public static INamespaceReference getAnyNamespaceReference()
    {
        return ANY;
    }

    /**
     * We have a single global private namespace, in which we can place implicit
     * code model definitions that only exist for code model compatibility. this
     * and super definitions are in this namespace.
     * 
     * @return An {@code INamespaceDefinition.ICodeModelImplicitDefinitionNamespaceDefinition } for implicit code model
     * definitions.
     */
    public static ICodeModelImplicitDefinitionNamespaceDefinition getCodeModelImplicitDefinitionNamespace()
    {
        return CM_IMPLICIT_DEF_NS;
    }

    public static NamespaceDefinition createNamespaceDefinition(Namespace ns)
    {
        // Pass in null since that can't be a valid user defined namespace.
        return createNamespaceDefinition(null, ns);
    }

    /**
     * @param name name of the namespace definition if the namespace was
     * specified in source code or was read from a const slot in an ABC, null
     * otherwise.
     * @param ns An ABC namespace.
     * @return a namespace definition
     */
    public static NamespaceDefinition createNamespaceDefinition(String name, Namespace ns)
    {
        int namespaceKind = ns.getKind();
        switch (namespaceKind)
        {
            case ABCConstants.CONSTANT_PackageNs:
                if (ns.getName().length() == 0)
                    return PUBLIC;
                return new PublicNamespaceDefinition(ns);
            case ABCConstants.CONSTANT_PackageInternalNs:
                // "namespace foo;" makes a package internal name in an ABC, 
                // but we need to make a UserDefinedNamespace so the 
                // NamespaceDefinition will have the right base name.
                if (name == null)
                    return new InternalNamespaceDefinition(ns);
                else
                    return new UserDefinedNamespaceDefinition(name, ns);
            case ABCConstants.CONSTANT_PrivateNs:
                return new PrivateNamespaceDefinition(ns);
            case ABCConstants.CONSTANT_ProtectedNs:
                return new ProtectedNamespaceDefinition(ns);
            case ABCConstants.CONSTANT_StaticProtectedNs:
                return new StaticProtectedNamespaceDefinition(ns);
            case ABCConstants.CONSTANT_Namespace:
                // name will be non-null if we got the Namespace from source 
                // code *or* from a const slot in an ABC. However if we 
                // found the Namespace anywhere else in the ABC, name will 
                // be null.
                if (name == null)
                    name = "";
                return new UserDefinedNamespaceDefinition(name, ns);
            default:
                assert false : "Unknown namespace kind!";
                return null;
        }
    }

    /**
     * Create a new private namespace definition.
     * 
     * @return A new private namespace definition.
     */
    public static IPrivateNamespaceDefinition createPrivateNamespaceDefinition(String uri)
    {
        return new PrivateNamespaceDefinition(uri);
    }

    /**
     * Create a new file private namespace definition.
     * <p>
     * The distinction between file private namespaces and regular private
     * namespaces is purely for the benefit of code model clients. Once we have
     * implemented type analysis in Royale, CM clients may not need this
     * distinction anymore.
     * 
     * @return A new file private namespace definition.
     */
    public static IFilePrivateNamespaceDefinition createFilePrivateNamespaceDefinition(String uri)
    {
        return new FilePrivateNamespaceDefinition(uri);
    }

    /**
     * Create a new protected namespace definition.
     * 
     * @return A new protected namespace definition.
     */
    public static IProtectedNamespaceDefinition createProtectedNamespaceDefinition(String uri)
    {
        return new ProtectedNamespaceDefinition(uri);
    }

    public static IStaticProtectedNamespaceDefinition createStaticProtectedNamespaceDefinition(String uri)
    {
        return new StaticProtectedNamespaceDefinition(uri);
    }

    /**
     * Create a new internal namespace definition for a package.
     * 
     * @param owningPackageName The package definition the namespace definition
     * is for.
     * @return A new internal namespace definition.
     */
    public static IInternalNamespaceDefinition createInternalNamespaceDefinition(String owningPackageName)
    {
        return new InternalNamespaceDefinition(owningPackageName);
    }

    /**
     * Create a new interface namespace definition for an interface
     * 
     * @param owningInterface The interface definition the namespace definition
     * is for
     * @return A new interface namespace definition
     */
    public static IInterfaceNamespaceDefinition createInterfaceNamespaceDefinition(InterfaceDefinition owningInterface)
    {
        return new InterfaceNamespaceDefinition(owningInterface);
    }

    /**
     * @param packageName The name of the package for which a
     * {@code INamespaceDefinition.IPublicNamespaceDefinition} should be created.
     * @return A new {@code INamespaceDefinition.IPublicNamespaceDefinition} for the public namespace
     * in the specified package.
     */
    public static IPublicNamespaceDefinition createPackagePublicNamespaceDefinition(String packageName)
    {
        if (packageName.length() == 0)
            return PUBLIC;
        return new PublicNamespaceDefinition(packageName);
    }

    /**
     * Create a new user defined namespace definition.
     * 
     * @return A new user defined namespace definition.
     */
    public static NamespaceDefinition createUserDefinedNamespace(String name, String uri)
    {
        return new UserDefinedNamespaceDefinition(name, uri);
    }

    /**
     * Create a new usder defined namespace definition. For example:
     * <p>
     * {@code public namespace foo = "http://foo.com";}
     * 
     * @param qualifierNamespaceRef The {@link INamespaceReference} that
     * qualifies the namespace declaration. In the example above this would be a
     * {@link INamespaceReference} that resolves to the public namespace for the
     * containing scope.
     * @param scope The scope in which the namespace definition occurs.
     * @param name The name of the namespace definition. In the example above
     * this would be "foo".
     * @param uri The URI initializer of the namespace declaration or null if
     * there is not uri specified. In the example above, this would be
     * "http://foo.com".
     * @param initializer A NamespaceReference to another namespace that this namespace was initialized with
     *                    e.g.
     *                      namespace ns2 = ns1;
     * @return The new namespace definition.
     */
    public static NamespaceDefinition createNamespaceDefintionDirective(INamespaceReference qualifierNamespaceRef, IASScope scope, String name, String uri, INamespaceReference initializer)
    {
        NamespaceDefinitionDirective directive = new NamespaceDefinitionDirective(qualifierNamespaceRef, scope, name, uri, initializer);
        ((ASScope)scope).addNamespaceDirective(directive);
        return directive;
    }

    /**
     * @param scope The IASScope in which the node should be resolved
     * @param node The INamespaceDecorationNode that represents the namespace
     * reference - may not be null.
     */
    public static INamespaceReference createNamespaceReference(IASScope scope, INamespaceDecorationNode node)
    {
        return createNamespaceReference(scope, node, false);
    }

    /**
     * Set the containing definition of an {@link INamespaceReference}, if the {@link INamespaceReference} implementation
     * needs that information.  Currently, only a user defined namespace reference will care what its containing definition
     * is.
     * @param nsRef         the {@link INamespaceReference} to set the containing definition on
     * @param containingDef the {@link IDefinition} that contains the namespace reference
     */
    public static void setContainingDefinitionOfReference (INamespaceReference nsRef, IDefinition containingDef)
    {
        // only user defined namespace references need this info.
        if( nsRef instanceof UserDefinedNamespaceReference )
            ((UserDefinedNamespaceReference) nsRef).setContainingDefinition(containingDef);
    }

    /**
     * @param scope The IASScope in which the node should be resolved
     * @param node The INamespaceDecorationNode that represents the namespace
     * reference - may not be null.
     * @param isStatic whether we want to construct a namespace for a static
     * property - if true, and the baseName is "protected" then it will
     * construct a static-protected namespace instead of a protected namespace
     */
    public static INamespaceReference createNamespaceReference(IASScope scope, INamespaceDecorationNode node, boolean isStatic)
    {
        assert scope != null;

        String baseName = getBaseName(node);
        if (baseName.equals(INamespaceConstants.public_))
        {
            // public members of a class get placed in the global public package "".
            ClassDefinition classDefinition = getContainingClassDefinition(scope);
            if (classDefinition != null)
                return PUBLIC;

            PackageScope packageScope = getContainingPackageScope(scope);
            if (packageScope != null)
                return packageScope.getPublicNamespace();

            // If we see a referenc to public outside of a package, but it is used as
            // a qualifier, then return the unnamed public namespace
            if( node.isExpressionQualifier() )
                return PUBLIC;
            // If we see a reference to public outside of a package on a definition
            // that decorates a definition, then the parser should produce a problem.
            // For compatibility with older version of CM we'll also return a reference
            // to the code model implicit definition namespace which CM clients always
            // have in their namespace set, but is not in any namesapce set used by the
            // compiler.
            else
                return getCodeModelImplicitDefinitionNamespace();
        }

        if (baseName.equals(INamespaceConstants.internal_))
        {
            // "internal" is just a way to explicitly refer
            // to the default name space.
            return getDefaultNamespaceDefinition(scope);
        }

        if (baseName.equals(INamespaceConstants.private_))
        {
            ClassDefinition classDefinition = getContainingClassDefinition(scope);
            if (classDefinition != null)
                return classDefinition.getPrivateNamespaceReference();
            // If we see a reference to private outside of a class on a definition
            // that decorates a definition, then the parser should produce a problem.
            // If we see a reference to private outside of a class in an expression,
            // then we will generate a reference to the file private namespace for
            // better compatibility with older version of code model.

            // Grr... CM unit tests put private definitions in the file scope,
            // so we'll make:
            // private var foo : *;
            // outside of a class a codemodel implicit definition.
            return getCodeModelImplicitDefinitionNamespace();
        }

        if (baseName.equals(INamespaceConstants.protected_))
        {
            ClassDefinition classDefinition = getContainingClassDefinition(scope);
            if (classDefinition != null)
            {
                if (isStatic)
                    return classDefinition.getStaticProtectedNamespaceReference();
                else
                    return classDefinition.getProtectedNamespaceReference();
            }
            // If we see a reference to protected outside of a class on a definition
            // that decorates a definition, then the parser should produce a problem.
            // For compatibility with older version of CM we'll also return a reference
            // to the code model implicit definition namespace which CM clients always
            // have in their namespace set, but is not in any namesapce set used by the
            // compiler.
            return getCodeModelImplicitDefinitionNamespace();
        }

        if( baseName.equals(INamespaceConstants.ANY) )
        {
            return ANY;
        }

        if( node instanceof ExpressionNodeBase )
        {
            return ((ExpressionNodeBase)node).computeNamespaceReference();
        }
        else
        {
            assert false : "creating a namespace reference from an unknown node type";
            return new UserDefinedNamespaceReference((ASScope)scope, node);
        }
    }

    /**
     * Determine if the INamespaceDecorationNode passed in could refer to more than one namespace when
     * used as a qualifier.
     * @param scope     scope to resolve things in
     * @param node      the namespace node to check
     * @return          true if the node might resolve to multiple namespaces when it's used as a qualifier.
     */
    public static boolean qualifierCouldBeManyNamespaces(ASScope scope, INamespaceDecorationNode node)
    {
        String baseName = getBaseName(node);
        return ( baseName.equals(INamespaceConstants.public_) || baseName.equals(INamespaceConstants.protected_) );
    }

    /**
     * Generate a collection of INamespaceReferences that are all the namespace references that the given
     * INamespaceDecorationNode refers to, when that node is used as the qualifier in an expression, such as:
     *   qualifer::expr
     *
     * This returns a collection because some qualifiers (public, protected) may actually refer to multiple
     * namespaces depending on the context where they are used
     *
     * @param scope scope where the namespace reference ocurrs
     * @param node  the qualifier node
     * @return      A collection of 1 or more namespace references that the node refers to
     */
    public static Collection<INamespaceReference> createNamespaceReferencesForQualifier(ASScope scope, INamespaceDecorationNode node )
    {
        Collection<INamespaceReference> nsrefs = new ArrayList<INamespaceReference>();

        String baseName = getBaseName(node);

        INamespaceReference first = createNamespaceReference(scope, node);
        nsrefs.add(first);
        if( first instanceof IPublicNamespaceDefinition && baseName.equals(INamespaceConstants.public_) )
        {
            // Anything that resolve to the unnanmed namespace won't have multiple namespaces
            if( first != PUBLIC )
            {
                PackageScope packageScope = getContainingPackageScope(scope);
                if( packageScope != null && packageScope.getPublicNamespace() == first )
                {
                    // "public" inside a package
                    // need to add the unnamed public namespace
                    nsrefs.add(PUBLIC);
                }
            }
        }
        else if( first instanceof IProtectedNamespaceDefinition && baseName.equals(INamespaceConstants.protected_) )
        {
            ClassDefinition classDef = getContainingClassDefinition(scope);
            if( classDef != null && classDef.getProtectedNamespaceReference() == first )
            {
                // protected refers to both the instance protected
                // and static protected
                nsrefs.add(classDef.getStaticProtectedNamespaceReference());
            }
        }

        return nsrefs == null ? Collections.<INamespaceReference>emptyList() : nsrefs;
    }

    /**
     * Create a new UserDefinedNamespaceReference with the given scope, baseName, qualifier, and base reference
     * @param scope     The scope to resolve in
     * @param baseName  The base name of the namespace reference
     * @param qualifier The qualifier of the namespace reference, eg 'foo' in 'a.foo::b'.  May be null if there is
     *                  no qualifier
     * @param base      The base reference, 'a' in 'a.b'
     * @return          a new {@link INamespaceReference}
     */
    public static INamespaceReference createNamespaceReference(ASScope scope, String baseName, INamespaceReference qualifier, IReference base)
    {
        return new MemberNamespaceReference(scope, base, baseName, qualifier);
    }

    /**
     * Create a new UserDefinedNamespaceReference with the given scope, baseName, and qualifier
     * @param scope     The ASScope in which the node should be resolved
     * @param baseName  The string that represents the name of the namespace
     * @param qualifier An INamespaceReference for the qualifier, may be null if there is no explicit qualifier
     * @return          An INamespaceReference that can be used to resolve the namespace with the given name and qualifier
     */
    public static INamespaceReference createNamespaceReference(ASScope scope, String baseName, INamespaceReference qualifier)
    {
        return new UserDefinedNamespaceReference(scope, baseName, qualifier);
    }

    /**
     * @param scope The IASScope in which the node should be resolved
     * @param node The INamespaceDecorationNode that represents the namespace
     * reference - may not be null.
     */
    public static void addUseNamespaceDirectiveToScope(IASScope scope, INamespaceDecorationNode node)
    {
        assert scope != null;

        String baseName = getBaseName(node);
        if (baseName.equals(INamespaceConstants.public_))
        {
            // public will already be in scope.
            return;
        }

        UseNamespaceDirective directive = new UseNamespaceDirective((ASScope)scope, node);
        ((ASScope)scope).addUseDirective(directive);
    }

    private static String getBaseName(INamespaceDecorationNode node)
    {
        // if there is no namespace decoration, must be internal
        if (node == null)
            return INamespaceConstants.internal_;

        // if there is no name on the decoration, must be internal
        String baseName = node.getName();
        if (baseName == null)
            return INamespaceConstants.internal_;

        int index = baseName.lastIndexOf(".");
        if (index != -1)
        {
            baseName = baseName.substring(index + 1);
        }

        return baseName;
    }

    public static ILanguageNamespaceDefinition getDefaultNamespaceDefinition(IASScope scope)
    {
        InterfaceDefinition interfaceDef = scope.getDefinition() instanceof InterfaceDefinition ?
                (InterfaceDefinition)scope.getDefinition() : null;

        // Interface members go in their own special namespace
        if (interfaceDef != null)
            return interfaceDef.getInterfaceNamespaceReference();

        PackageScope packageScope = getContainingPackageScope(scope);
        if (packageScope != null)
            return packageScope.getInternalNamespace();

        return getFileScope(scope).getFilePrivateNamespaceReference();
    }

    private static ASFileScope getFileScope(IASScope scope)
    {
        IASScope currScope = scope;
        while ((currScope != null) && (!(currScope instanceof ASFileScope)))
            currScope = currScope.getContainingScope();
        assert currScope != null : "Could not traverse to a file scope!";
        return (ASFileScope)currScope;
    }

    private static PackageScope getContainingPackageScope(IASScope scope)
    {
        while ((!(scope instanceof PackageScope)) && (scope != null))
            scope = scope.getContainingScope();
        return (PackageScope)scope;
    }

    private static ClassDefinition getContainingClassDefinition(IASScope scope)
    {
        if (!(scope instanceof ASScope))
            return null;

        ASScope asScope = (ASScope)scope;
        IDefinition containingDef = asScope.getContainingDefinition();
        if (containingDef == null)
            return null;
        if (containingDef instanceof ClassDefinition)
            return (ClassDefinition)containingDef;

        ClassDefinition classDefinition = (ClassDefinition)containingDef.getAncestorOfType(ClassDefinition.class);
        return classDefinition;

    }

    /**
     * Private constructor called from private inner classes in this class.
     * 
     * @param name Name of the namespace definition.
     * @param kind ABC kind for the underlying ABC namespace.
     * @param uri URI for the underlying ABC namespace.
     */
    private NamespaceDefinition(String name, int kind, String uri)
    {
        this(name, new Namespace(kind, (uri == null) ? "" : uri));
    }

    /**
     * Private constructor called from private inner classes in this class.
     * 
     * @param name Name of the namespace definition.
     * @param ns An AET namespace for this namespace definition.
     */
    private NamespaceDefinition(String name, Namespace ns)
    {
        super(name);
        aetNamespace = ns;
    }

    /**
     * The AET namespace this namespace definition wraps.
     */
    private final Namespace aetNamespace;

    @Override
    public String getURI()
    {
        return aetNamespace.getName();
    }

    @Override
    public INamespaceNode getNode()
    {
        return (INamespaceNode)super.getNode();
    }

    @Override
    public boolean isLanguageNamespace()
    {
        return false;
    }

    @Override
    public INamespaceDefinition resolveNamespaceReference(ICompilerProject project)
    {
        return this;
    }

    @Override
    public boolean matches(DefinitionBase node)
    {
        boolean matches = super.matches(node);
        if (!matches)
            return matches;

        NamespaceDefinition nNode = (NamespaceDefinition)node;
        if (nNode.getURI().compareTo(getURI()) != 0)
        {
            return false;
        }

        NamespaceClassification classification = nNode.getNamespaceClassification();
        if (classification != getNamespaceClassification())
            return false;

        if (classification == NamespaceClassification.LOCAL || classification == NamespaceClassification.FILE_MEMBER)
        {
            if (nNode.getNameStart() != getNameStart() || nNode.getNameEnd() != getNameEnd())
                return false;
        }
        else if (classification == NamespaceClassification.CLASS_MEMBER)
        {
            IASScope type = node.getContainingScope();
            IASScope type2 = getContainingScope();
            if (type != type2)
            {
                return false;
            }
            return type == type2;
        }

        return true;
    }

    @Override
    public String getBaseName()
    {
        return getStorageName();
    }

    @Override
    public boolean isPublicOrInternalNamespace()
    {
        return false;
    }

    @Override
    public int getNamespaceCount()
    {
        return 1;
    }

    @Override
    public Set<INamespaceDefinition> getNamespaceSet()
    {
        return ImmutableSet.of((INamespaceDefinition)this);
    }

    @Override
    public INamespaceDefinition getFirst ()
    {
        return this;
    }


    /**
     * Gets the {@link Namespace} object for this {@link INamespaceDefinition}
     * needed by the code generator to generate an ABC namespace using the AET
     * library.
     * <p>
     * This method is used by the code generator to get the AET namespace from a
     * NamespaceDefinition.
     * <p>
     * TODO We should change AET to define an interface for Namespace that this
     * class can implement. Then this class would not need to contain an AET
     * namespace, but would *be* an AET namespace.
     * 
     * @return The {@link Namespace} object needed by the code generator
     */
    public Namespace getAETNamespace()
    {
        return aetNamespace;
    }

    @Override
    public int hashCode()
    {
        return aetNamespace.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean ret = false;
        if (obj instanceof INamespaceDefinition)
        {
            ret = equals((INamespaceDefinition)obj);
        }
        return ret;
    }

    @Override
    public boolean equals(INamespaceDefinition ns)
    {
        if (ns == null)
            return false;
        return ((NamespaceDefinition)ns).aetNamespace.equals(aetNamespace);
    }

    /**
     * Interface implemented by use namespace directives and by namespace
     * declarations. This interface can be used to traverse through the use
     * namespace directives and namespace declrations file order. This is needed
     * to resolve namespaces properly.
     */
    public interface INamespaceDirective
    {
        INamespaceDirective getNext();

        void setNext(INamespaceDirective next);

        void resolveDirective(NamespaceDirectiveResolver resolver);
    }

    /**
     * Implemented by namespace definitions constructed from namespace
     * definition directives found in source code.
     * <p>
     * eg: namespace ns1 = "http://foo.bar.com"; namespace ns2 = ns1;

     */
    public interface INamepaceDeclarationDirective extends INamespaceDirective, INamespaceDefinition
    {
        /**
         * Resolve the initializer if it refers to another namespace, eg:
         * namespace ns1 = SomeOtherNamespace; This will just return itself if
         * no additional resolution is necessary.
         * 
         * @param project the project to resolve things in
         * @return A Fully resolved namespace that has the right URI, and can be
         * used in name lookup
         */
        public INamespaceDefinition resolveConcreteDefinition(ICompilerProject project);

        /**
         * Resolve the initializer if it refers to another namespace, eg:
         * namespace ns1 = SomeOtherNamespace; This will just return itself if
         * no additional resolution is necessary.
         *
         * @param project   the project to resolve things in
         * @param pred      a {@code NamespaceDirectiveResolver.NamespaceForwardReferencePredicate}
         *                  to use to filter out forward references while computing the concrete definition
         * @return          A Fully resolved namespace that has the right URI, and can be
         *                  used in name lookup
         */
        public INamespaceDefinition resolveConcreteDefinition(ICompilerProject project, NamespaceDirectiveResolver.NamespaceForwardReferencePredicate pred);
    }

    /**
     * Implemented by all namespace references from constructed use namespace
     * directives found in source code.
     * <p>
     * eg: use namespace ns1;
     */
    public interface IUseNamespaceDirective extends INamespaceDirective, INamespaceReference
    {

    }

    /**
     * Private abstract base class for all langage namespace definitions.
     * Convenient place to make isLanguageNamespace return true and call
     * setImplicit in the constructor.
     */
    private abstract static class LanguageNamespaceDefinition extends NamespaceDefinition implements ILanguageNamespaceDefinition
    {

        private LanguageNamespaceDefinition(String name, int kind, String uri)
        {
            super(name, kind, uri);
            setImplicit();
        }

        private LanguageNamespaceDefinition(String name, Namespace ns)
        {
            super(name, ns);
            setImplicit();
        }

        @Override
        public boolean isLanguageNamespace()
        {
            return true;
        }

        @Override
        public Namespace resolveAETNamespace(ICompilerProject project)
        {
            return getAETNamespace();
        }

        @Override
        public NamespaceClassification getNamespaceClassification()
        {
            return NamespaceClassification.LANGUAGE;
        }

        @Override
        public IFileSpecification getFileSpecification()
        {
            return null;
        }

        @Override
        public int getStart()
        {
            return -1;
        }

        @Override
        public int getNameStart()
        {
            return -1;
        }

        @Override
        public int getNameEnd()
        {
            return -1;
        }

        @Override
        public String getContainingFilePath()
        {
            return null;
        }

        @Override
        public String getContainingSourceFilePath(ICompilerProject project)
        {
            return null;
        }

        @Override
        public ASFileScope getFileScope()
        {
            return null;
        }

        /**
         * Used only for debugging, as part of {@link #toString()}.
         */
        @Override
        protected void buildInnerString(StringBuilder sb)
        {
            sb.append(getBaseName());
            sb.append('(');
            sb.append(getURI());
            sb.append(')');
        }

        @Override
        public final String getPackageName()
        {
            return "";
        }
    }

    private static final class PublicNamespaceDefinition extends LanguageNamespaceDefinition implements IPublicNamespaceDefinition
    {
        private PublicNamespaceDefinition()
        {
            this("");
        }

        private PublicNamespaceDefinition(String packageName)
        {
            super(INamespaceConstants.public_, ABCConstants.CONSTANT_PackageNs, packageName);
            setPublic();
        }

        private PublicNamespaceDefinition(Namespace ns)
        {
            super(INamespaceConstants.public_, ns);
            setPublic();
            assert ns.getKind() == ABCConstants.CONSTANT_PackageNs;
        }

        @Override
        public boolean isPublicOrInternalNamespace()
        {
            return true;
        }

        @Override
        public String getGeneratedURIPrefix()
        {
            String uri = getURI();
            if (uri.length() == 0)
                return "";
            return uri + ":";
        }

        @Override
        public String getNamespacePackageName()
        {
            return getURI();
        }
    }

    private static class PrivateNamespaceDefinition extends LanguageNamespaceDefinition implements IPrivateNamespaceDefinition
    {
        private PrivateNamespaceDefinition(String uri)
        {
            super(INamespaceConstants.private_, ABCConstants.CONSTANT_PrivateNs, uri);
        }

        private PrivateNamespaceDefinition(Namespace ns)
        {
            super(INamespaceConstants.private_, ns);
            assert ns.getKind() == ABCConstants.CONSTANT_PrivateNs;
        }
    }

    private static final class FilePrivateNamespaceDefinition extends PrivateNamespaceDefinition implements IFilePrivateNamespaceDefinition
    {
        private FilePrivateNamespaceDefinition(String uri)
        {
            super(uri);
        }

        @Override
        public String getGeneratedURIPrefix()
        {
            return getURI() + ":";
        }

        @Override
        public String getNamespacePackageName()
        {
            return "";
        }
    }

    private static final class ProtectedNamespaceDefinition extends LanguageNamespaceDefinition implements IProtectedNamespaceDefinition
    {
        private ProtectedNamespaceDefinition(String uri)
        {
            super(INamespaceConstants.protected_, ABCConstants.CONSTANT_ProtectedNs, uri);
        }

        private ProtectedNamespaceDefinition(Namespace ns)
        {
            super(INamespaceConstants.protected_, ns);
            assert ns.getKind() == ABCConstants.CONSTANT_ProtectedNs;
        }
    }

    private static final class StaticProtectedNamespaceDefinition extends LanguageNamespaceDefinition implements IStaticProtectedNamespaceDefinition
    {
        private StaticProtectedNamespaceDefinition(String uri)
        {
            super(INamespaceConstants.protected_, ABCConstants.CONSTANT_StaticProtectedNs, uri);
        }

        private StaticProtectedNamespaceDefinition(Namespace ns)
        {
            super(INamespaceConstants.protected_, ns);
            assert ns.getKind() == ABCConstants.CONSTANT_StaticProtectedNs;
        }
    }

    private static final class InternalNamespaceDefinition extends LanguageNamespaceDefinition implements IInternalNamespaceDefinition
    {
        private InternalNamespaceDefinition(String owningPackage)
        {
            super(INamespaceConstants.internal_, ABCConstants.CONSTANT_PackageInternalNs, owningPackage);
        }

        private InternalNamespaceDefinition(Namespace ns)
        {
            super(INamespaceConstants.internal_, ns);
            assert ns.getKind() == ABCConstants.CONSTANT_PackageInternalNs;
        }

        @Override
        public boolean isPublicOrInternalNamespace()
        {
            return true;
        }

        @Override
        public String getGeneratedURIPrefix()
        {
            return getURI() + ":";
        }

        @Override
        public String getNamespacePackageName()
        {
            return getURI();
        }
    }

    /**
     * represents the Any namespace ('*')
     */
    private static class AnyNamespaceDefinition extends LanguageNamespaceDefinition implements IAnyNamespaceDefinition
    {
        private AnyNamespaceDefinition()
        {
            // Make a private namespace for the AET namespace, so it won't compare as equal to anything
            // except itself
            super(INamespaceConstants.ANY, ABCConstants.CONSTANT_PrivateNs, "*");
        }

        public Namespace getAETNamespace()
        {
            // There is not an AET Namespace for the any namespace
            // instead it usually requires special handling.
            // For example:
            //
            //   a.*::b
            //
            // requires a QName with a null namespace set be generated for the Name
            // for *::b.
            // Also there should be no way to have the Any namespace as part of a multiname
            // as there is no way to open the any namespace.
            // If you hit this assert you probably need to check for the Any namespace higher up
            // and do something special with it.
            assert false : "Can't get the Namespace for the any namespace!";
            return null;
        }

    }

    /**
     * Represents the namespace that all properties of an interface go into.
     * Properties of an interface need to go in a special interface namespace,
     * which has a URI of the fully qualified name of the interface. For more
     * details, see:
     * http://livedocs.adobe.com/specs/actionscript/3/wwhelp/wwhimpl
     * /common/html/
     * wwhelp.htm?context=LiveDocs_Parts&file=as3_specification98.html#wp127540
     */
    private static final class InterfaceNamespaceDefinition extends LanguageNamespaceDefinition implements IInterfaceNamespaceDefinition
    {
        private InterfaceNamespaceDefinition(InterfaceDefinition interf)
        {
            // FB likes to think of interface namespaces as public, which works well enough.  The compiler
            // doesn't really care what the "name" of the interface namespace is, so just make it public to
            // keep FB working.
            super(INamespaceConstants.public_, ABCConstants.CONSTANT_Namespace, interf.generateInterfaceURI());
        }

        private InterfaceNamespaceDefinition(Namespace ns)
        {
            // FB likes to think of interface namespaces as public, which works well enough.  The compiler
            // doesn't really care what the "name" of the interface namespace is, so just make it public to
            // keep FB working.
            super(INamespaceConstants.public_, ns);
            assert ns.getKind() == ABCConstants.CONSTANT_PrivateNs;
        }
    }

    /**
     * TODO: rename this something like invalid namespace definition, as it's
     * now used for flagging invalid namespaces TODO: such as 'private' used
     * outside of a class
     */
    private static final class CodeModelImplicitDefinitionNamespaceDefinition extends LanguageNamespaceDefinition implements ICodeModelImplicitDefinitionNamespaceDefinition
    {
        private CodeModelImplicitDefinitionNamespaceDefinition()
        {
            super("", ABCConstants.CONSTANT_PrivateNs, "CodeModelImplicitDefinitionsNS");
        }

        @Override
        public Namespace getAETNamespace()
        {
            // return a new private namespace so that the code generator can still generate code, but the code will not work
            // It is important that this is a new Private namespace each time, so the various private namespaces will never
            // compare as equal - otherwise you could write broken code that would appear to work.
            // the codegenerator will issue semantic problems when a CM Implicit namespace gets to it, so clients will know
            // not to trust the resulting ABC with one of these namespaces in it.
            // We used to assert, but that does not work now that semantics run during code gen.
            return new Namespace(ABCConstants.CONSTANT_PrivateNs, "<invalid-namespace>");
        }
    }

    /**
     * Subclass of {@link NamespaceDefinition} for user defined namespace
     * declarations like this:
     * <p>
     * {@code public namespace foo = "http://foo.com";}
     */
    private static class UserDefinedNamespaceDefinition extends NamespaceDefinition
    {
        /**
         * Generates a URI prefix for a specified qualifier in a specified
         * scope.
         * 
         * @param containingScope {@link IASScope} in which the specified
         * {@link INamespaceReference} occurrs.
         * @param qualifierNamespaceRef The {@link INamespaceReference} for
         * which a URI prefix should be generated.
         * @return URI prefix for a specified qualifier in a specified scope
         * @see #generateURI(INamespaceReference, IASScope, String)
         */
        private static String generateQualifierPrefixString(IASScope containingScope, INamespaceReference qualifierNamespaceRef)
        {
            if (!(qualifierNamespaceRef instanceof INamespaceDefinition))
                return qualifierNamespaceRef.getBaseName() + ":";
            // file private not handled in here, generatURI handles file private.
            if (qualifierNamespaceRef instanceof IFilePrivateNamespaceDefinition)
            {
                ASFileScope containingFileScope = ((ASScope)containingScope).getFileScope();
                assert containingFileScope != null;
                return generateQualifierPrefixStringForFilePrivate(qualifierNamespaceRef.getBaseName(), containingFileScope) + ":";
            }
            if (qualifierNamespaceRef instanceof IPrivateNamespaceDefinition)
                return "private:";
            INamespaceDefinition qualifierNamespace =
                    (INamespaceDefinition)qualifierNamespaceRef;
            String uri = qualifierNamespace.getURI();
            if (uri.isEmpty())
                return "";
            return uri + ":";
        }

        /**
         * Generates a URI prefix for a file private namespace in the specified
         * {@link ASFileScope}.
         * 
         * @param baseName The basename of the private namespace
         * @param fileScope The {@link ASFileScope} which contains the file
         * private namespace for which a URI prefix is to be generated.
         * @return The URI prefix for the file private namespace of the
         * specified {@link ASFileScope}.
         * @see #generateURI(INamespaceReference, IASScope, String)
         */
        private static String generateQualifierPrefixStringForFilePrivate(String baseName, ASFileScope fileScope)
        {
            String sourcePath = fileScope.getContainingPath();
            String dirName = FilenameUtils.getPathNoEndSeparator(sourcePath);
            String md5String = StringEncoder.stringToMD5String(dirName);
            return FilenameUtils.getName(baseName) + "$" + md5String;
        }

        /**
         * Finds the inner most non-package definition that contains the
         * specified {@link IASScope}.
         * 
         * @param scope {@link IASScope} whose containing non-package definition
         * should be returned.
         * @return The inner most non-package definition that contains the
         * specified {@link IASScope} or null if there is no such definition.
         */
        private static IDefinition getContaininDefinition(IASScope scope)
        {
            while (scope != null)
            {
                if (scope instanceof PackageScope)
                    return null;
                IDefinition result = scope.getDefinition();
                if (result != null)
                    return result;
                scope = scope.getContainingScope();
            }
            return null;
        }

        /**
         * Handles constructor and anonymous function when generating URI's for
         * namespace declarations without a URI initializer.
         * 
         * @param definition
         * @return The URI prefix for the specified definition or null if the
         * specified definition did not need special handling.
         * @see #generateURI(INamespaceReference, IASScope, String)
         */
        private static String generateSpecialCaseFunctionURIPrefix(IDefinition definition)
        {
            String baseName = null;
            if (!(definition instanceof FunctionDefinition))
                return null;

            FunctionDefinition functionDefinition = (FunctionDefinition)definition;
            FunctionNode functionNode = (FunctionNode)functionDefinition.getNode();
            if (functionNode == null)
                return null;

            if (functionNode.isConstructor())
            {
                baseName = "$construct/";
            }
            else if (functionNode.getParent() instanceof FunctionObjectNode)
            {
                String functionName = functionDefinition.getBaseName();
                if (functionName.isEmpty())
                    baseName = "anonymous/";
                else
                    baseName = functionName + "/";
            }
            else
                return null;

            assert baseName != null;
            return generateURI(PUBLIC, definition.getContainingScope(), baseName);

        }

        /**
         * Generates a URI for a namespace declaration that does not have a URI
         * initializer. For example:
         * <p>
         * {@code public namespace foo;}
         * </p>
         * 
         * @param qualifierNamespaceRef The {@link INamespaceReference} that
         * qualifies the namespace declaration. In the example above, this would
         * be a {@link INamespaceReference} that resolves to the public
         * namespace for the containing scope of the namespace declaration.
         * @param containingScope The {@link IASScope} that contains the
         * namespace declaration.
         * @param baseName The name of the namespace declaration. In the example
         * above, this would be "foo".
         * @return The generated URI for the namespace declaration.
         */
        private static String generateURI(INamespaceReference qualifierNamespaceRef, IASScope containingScope, String baseName)
        {
            IDefinition containingDef = getContaininDefinition(containingScope);
            String prefix = "";
            if (containingDef != null)
            {
                String specialCaseFunctionPrefix =
                        generateSpecialCaseFunctionURIPrefix(containingDef);
                if (specialCaseFunctionPrefix != null)
                {
                    prefix = specialCaseFunctionPrefix;
                }
                else
                {
                    prefix =
                            generateURI(containingDef.getNamespaceReference(), containingDef.getContainingScope(), containingDef.getBaseName()) + "/";
                }
            }

            String uri = generateQualifierPrefixString(containingScope, qualifierNamespaceRef) + baseName;
            return prefix + uri;
        }

        private UserDefinedNamespaceDefinition(INamespaceReference qualifierNamespaceRef, IASScope containingScope, String name, String uri)
        {
            super(name,
                    uri != null ? ABCConstants.CONSTANT_Namespace : ABCConstants.CONSTANT_PackageInternalNs,
                    uri != null ? uri : generateURI(qualifierNamespaceRef, containingScope, name));
            setNamespaceReference(qualifierNamespaceRef);
        }

        /**
         * Constructs a new namespace definition with a specified name and uri.
         * The uri parameter must not be null, if you need a constructor that
         * can deal with a null URI, call
         * {@link UserDefinedNamespaceDefinition#UserDefinedNamespaceDefinition(IASScope, String, String)}
         * instead.
         * 
         * @param name The base name of the new namespace definition.
         * @param uri The URI initializer of the new namespace definition. This
         * must not be null.
         */
        private UserDefinedNamespaceDefinition(String name, String uri)
        {

            //  If the URI of a namespace defintion is null,
            //  the namespace is a package internal namespace
            //  with URI taken from the name of the definition.
            super(name, ABCConstants.CONSTANT_Namespace, uri);
            // See javadoc for this constructor.
            assert uri != null;
        }

        private UserDefinedNamespaceDefinition(String name, Namespace ns)
        {
            super(name, ns);
            assert ns.getKind() == ABCConstants.CONSTANT_Namespace
                    // User defined namespaces may alias other user defined namespaces
                    // that will be PackageInternalNs if they were created without an initializer
                    || ns.getKind() == ABCConstants.CONSTANT_PackageInternalNs;
        }

        @Override
        public Namespace resolveAETNamespace(ICompilerProject project)
        {
            return getAETNamespace();
        }

        @Override
        public NamespaceClassification getNamespaceClassification()
        {
            IDefinition parent = getParent();

            if (parent instanceof IFunctionDefinition)
                return NamespaceClassification.LOCAL;
            if (parent instanceof IClassDefinition)
                return NamespaceClassification.CLASS_MEMBER;
            if (parent instanceof IPackageDefinition)
                return NamespaceClassification.PACKAGE_MEMBER;
            if (parent == null)
            {
                // Some namespaces from ABCs will not have a namespace reference, but this is ok
                if (getNamespaceReference() != null && inPackageNamespace())
                    return NamespaceClassification.PACKAGE_MEMBER;
                return NamespaceClassification.FILE_MEMBER;
            }
            return null;
        }

        /**
         * Used only for debugging, as part of {@link #toString()}.
         */
        @Override
        protected void buildInnerString(StringBuilder sb)
        {
            sb.append(getURI());
        }
    }

    private static final class NamespaceDefinitionDirective extends UserDefinedNamespaceDefinition implements INamepaceDeclarationDirective
    {
        private NamespaceDefinitionDirective(INamespaceReference qualifierNamespaceRef, IASScope scope, String name, String uri, INamespaceReference initializer)
        {
            super(qualifierNamespaceRef, scope, name, uri);
            
            if( initializer != null )
            {
                // a namespace initialized with another namespace ref should only ever
                // be initialized with a UserDefinedNamespaceReference
                assert initializer instanceof UserDefinedNamespaceReference;
                this.initializer = (UserDefinedNamespaceReference)initializer;
                this.initializer.setContainingDefinition(this);
            }
        }

        private INamespaceDirective next;
        private UserDefinedNamespaceReference initializer;

        @Override
        public INamespaceDirective getNext()
        {
            return next;
        }

        @Override
        public void setNext(INamespaceDirective next)
        {
            this.next = next;
        }

        @Override
        public void resolveDirective(NamespaceDirectiveResolver resolver)
        {
            NamespaceDefinition resolvedQualifier = resolver.resolveDirectiveReference(this.getNamespaceReference());
            // TODO at some point we'll need to support resolving the rhs of the namepace directive!
            // eg: namespace ns1 = ns2; // ns2 needs to be resolved too!
            NamespaceDirectiveResolver.ResolvedNamespaceDefinitionDirective resolvedDirective =
                    new NamespaceDirectiveResolver.ResolvedNamespaceDefinitionDirective(resolvedQualifier, this);
            resolver.addFullyResolvedNamespaceDefinitionDirective(resolvedDirective);
        }

        /**
         * Get the underlying AET Namespace object for this namespace. This
         * method may need to resolve the namespace initializer, which is why it
         * needs a project so it knows how to resolve things. This would only
         * happen for a case where the namespace was defined as: namespace ns1 =
         * otherNamespace;
         */
        @Override
        public Namespace resolveAETNamespace(ICompilerProject project)
        {
            NamespaceDirectiveResolver.NamespaceForwardReferencePredicate pred = new NamespaceDirectiveResolver.NamespaceForwardReferencePredicate();
            pred.addRef(this);
            return resolveAETNamespace(project, pred);
        }

        @Override
        protected String getNamespaceReferenceAsString ()
        {
            return super.getNamespaceReferenceAsString();    //To change body of overridden methods use File | Settings | File Templates.
        }

        public Namespace resolveAETNamespace(ICompilerProject project, NamespaceDirectiveResolver.NamespaceForwardReferencePredicate pred)
        {
            if (initializer != null )
            {
                INamespaceDefinition ns = NamespaceDirectiveResolver.resolveNamespaceReferenceInDirective(project, initializer, this, pred);
                if( ns instanceof NamespaceDefinition )
                    return ((NamespaceDefinition)ns).resolveAETNamespace(project);

                return null;
            }
            else
            {
                return super.resolveAETNamespace(project);
            }
        }

        /**
         * Resolve the initializer if it refers to another namespace, eg:
         * namespace ns1 = SomeOtherNamespace; This will just return itself if
         * no additional resolution is necessary.
         * 
         * @param project the project to resolve things in
         * @return A Fully resolved namespace that has the right URI, and can be
         * used in name lookup
         */
        @Override
        public INamespaceDefinition resolveConcreteDefinition(ICompilerProject project)
        {
            return resolveConcreteDefinition(project, new NamespaceDirectiveResolver.NamespaceForwardReferencePredicate());
        }
        public INamespaceDefinition resolveConcreteDefinition(ICompilerProject project,
                                                              NamespaceDirectiveResolver.NamespaceForwardReferencePredicate pred)
        {
            if (initializer != null)
            {
                Namespace aetNamespace = resolveAETNamespace(project, pred);

                if (aetNamespace != null)
                    // return a NamespaceDefinition that has the right URI.  This is ok, because a user defined namespace
                    // can only refer to other user defined namespaces, and the NamespaceDefinitions will compare as equal
                    // as long as the URIs match.
                    return new UserDefinedNamespaceDefinition("", aetNamespace);

                return null;
            }
            else
            {
                return this;
            }
        }
    }

    private static class UserDefinedNamespaceReference implements INamespaceResolvedReference
    {
        private static INamespaceReference getQualifierNamespaceIfExists(ASScope scope, INamespaceDecorationNode node)
        {
            if (!(node instanceof QualifiedNamespaceExpressionNode))
                return null;

            QualifiedNamespaceExpressionNode qNode = (QualifiedNamespaceExpressionNode)node;

            // TODO: this cast is kinda bad.  QualifiedNamespaceExpressionNode should have a getPackage() method
            IIdentifierNode prefix = (IIdentifierNode)qNode.getLeftOperandNode();
            Workspace w = (Workspace)scope.getWorkspace();
            INamespaceReference qualifedNamespace = w.getPackageNamespaceDefinitionCache().get(prefix.getName(), false);

            return qualifedNamespace;
        }

        private UserDefinedNamespaceReference(ASScope scope, INamespaceDecorationNode node)
        {
            this(scope, NamespaceDefinition.getBaseName(node), getQualifierNamespaceIfExists(scope, node));
        }

        private UserDefinedNamespaceReference(ASScope scope, String baseName, INamespaceReference qualifier)
        {
            assert scope != null;
            this.scope = scope;
            this.baseName = baseName;
            this.qualifierNamespace = qualifier;
        }

        /**
         * For a reference to a built-in namespace, this scope is null. For a
         * reference to a custom namespace like ns1, ns1::ns2, or
         * (ns1::ns2)::ns3, this scope is the scope in which "ns1", "ns2", and
         * "ns3" will be resolved.
         */
        private ASScope scope;

        /**
         * For a reference to a built-in namespace, this String is "public",
         * "private", "protected", or "internal". For a reference to a custom
         * namespace like ns1, this String is "ns1". For a reference to a custom
         * namespace like (ns1::ns2)::ns3, this String is "ns3".
         */
        private String baseName;

        /**
         * Reference to a qualifier namespace. May be null
         */
        private INamespaceReference qualifierNamespace;

        /**
         * The containing definition of this namespace reference
         */
        protected IDefinition def;

        @Override
        public final boolean isPublicOrInternalNamespace()
        {
            return false;
        }

        @Override
        public final String getBaseName()
        {
            return baseName;
        }

        /**
         */
        public final INamespaceReference getQualifierNamespace()
        {
            return qualifierNamespace;
        }

        @Override
        public final boolean isLanguageNamespace()
        {
            return false;
        }

        @Override
        public INamespaceDefinition resolveNamespaceReference(ICompilerProject project)
        {
            assert scope != null;

            IDefinition definition = null;
            if (qualifierNamespace != null)
            {
                INamespaceDefinition qualifier = qualifierNamespace.resolveNamespaceReference(project);
                if (qualifier != null)
                {
                    if( needsForwardRefPredicate())
                        definition = scope.findPropertyQualified(project, getForwardReferencePredicate(), qualifier, baseName, DependencyType.NAMESPACE);
                    else
                        definition = scope.findPropertyQualified(project, qualifier, baseName, DependencyType.NAMESPACE);
                }
            }
            else
            {
                if( needsForwardRefPredicate() )
                {
                    definition = scope.findProperty(project, baseName, getForwardReferencePredicate(), DependencyType.NAMESPACE, true);
                }
                else
                {
                    definition = scope.findProperty(project, baseName, DependencyType.NAMESPACE);
                }
            }

            INamespaceDefinition ns = definition instanceof INamespaceDefinition ? (INamespaceDefinition)definition : null;

            if (ns instanceof INamepaceDeclarationDirective)
            {
                ns = ((INamepaceDeclarationDirective)ns).resolveConcreteDefinition(project);
            }
            return ns;
        }

        /**
         * Does this reference need to use a forward reference predicate to resolve itself
         * @return  true, if the reference needs to pass down a forward ref predicate to resolve itself
         */
        protected boolean needsForwardRefPredicate()
        {
            if( this.def != null )
            {
                if( scope.getFirstNamespaceDirective() == null && scope.getLocalDefinitionSetByName(this.baseName) == null)
                {
                    // If the containing scope does not have any namespace directives,
                    // or local properties that match our base name
                    // then we don't have to worry about forward refs
                    // since this is a lexical ref, anything we find will be in a containing
                    // scope
                    return false;
                }

                return true;
            }
            return false;
        }

        /**
         * Get an appropriate forward reference predicate to use to resolve this reference
         * @return  A predicate that will prevent the resolution methods from resolving to definitions
         *          that would be forward references from this reference
         */
        protected NamespaceDirectiveResolver.NamespaceForwardReferencePredicate getForwardReferencePredicate ()
        {
            NamespaceDirectiveResolver.NamespaceForwardReferencePredicate pred = new NamespaceDirectiveResolver.NamespaceForwardReferencePredicate();
            pred.addRef(this.def);
            return pred;
        }

        /**
         * Set the containing definition of this reference
         * @param d the containing definition
         */
        public void setContainingDefinition(IDefinition d)
        {
            this.def = d;
        }

        @Override
        public final boolean equals(Object o)
        {
            assert false; // If someone is comparing a reference, they are probably making a mistake
                          // They probably meant to resolve to a definition first.
            if (o == this)
                return true;
            if (o instanceof UserDefinedNamespaceReference)
            {
                UserDefinedNamespaceReference other = (UserDefinedNamespaceReference)o;
                if (other.baseName.equals(this.baseName))
                    return true;
            }
            return false;
        }

        /**
         * For debugging only.
         */
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();

            if (qualifierNamespace != null)
            {
                sb.append(qualifierNamespace.toString());
                sb.append(':');
                sb.append(':');
            }
            sb.append(baseName);

            return sb.toString();
        }

        @Override
        public final Namespace resolveAETNamespace(ICompilerProject project)
        {
            INamespaceDefinition def = resolveNamespaceReference(project);
            assert (def == null) || (def instanceof NamespaceDefinition);

            if (def instanceof NamespaceDefinition)
            {
                NamespaceDefinition concreteDef = (NamespaceDefinition)def;
                return concreteDef.getAETNamespace();
            }

            return null;
        }

        protected final ASScope getScope()
        {
            return scope;
        }
    }

    /**
     * A namespace reference of the form 'a.b' where a is a member access, instead of a package
     * reference.
     */
    private static class MemberNamespaceReference extends UserDefinedNamespaceReference
    {
        IReference baseRef = null;
        private MemberNamespaceReference (ASScope scope, IReference base, String baseName, INamespaceReference qualifier)
        {
            super(scope, baseName, qualifier);

            this.baseRef = base;
        }

        @Override
        protected boolean needsForwardRefPredicate()
        {
            return this.def != null;
        }

        @Override
        public INamespaceDefinition resolveNamespaceReference(ICompilerProject project)
        {
            ASScope scope = getScope();
            assert getScope() != null;

            String baseName = getBaseName();

            IDefinition definition = null;
            IDefinition base = baseRef.resolve(project, scope, DependencyType.EXPRESSION, true);
            if( base != null )
            {
                IDefinition baseType = base.resolveType(project);
                if( baseType != null )
                {
                    if( needsForwardRefPredicate() )
                    {
                        definition = scope.getPropertyFromDef(project, baseType, baseName, getForwardReferencePredicate(), false);
                    }
                    else
                    {
                        definition = scope.getPropertyFromDef(project, baseType, baseName, false);
                    }
                }
            }

            INamespaceDefinition ns = definition instanceof INamespaceDefinition ? (INamespaceDefinition)definition : null;

            if (ns instanceof INamepaceDeclarationDirective)
            {
                ns = ((INamepaceDeclarationDirective)ns).resolveConcreteDefinition(project);
            }
            return ns;
        }
    }

    private static final class UseNamespaceDirective extends UserDefinedNamespaceReference implements IUseNamespaceDirective
    {
        private UseNamespaceDirective(ASScope scope, INamespaceDecorationNode node)
        {
            super(scope, node);
        }

        private INamespaceDirective next;

        @Override
        public INamespaceDirective getNext()
        {
            return next;
        }

        @Override
        public void setNext(INamespaceDirective next)
        {
            this.next = next;
        }

        @Override
        public void resolveDirective(NamespaceDirectiveResolver resolver)
        {
            NamespaceDefinition resolvedUsedNamespace = resolver.resolveDirectiveReference(this);
            resolver.addResolvedUsedNamespace(resolvedUsedNamespace);
        }

        @Override
        public final INamespaceDefinition resolveNamespaceReference(ICompilerProject project)
        {
            assert getScope() != null;
            return NamespaceDirectiveResolver.resolveNamespaceReferenceInDirective(project, this, this);
        }
    }

    /**
     * This class implements all the magic of resolving namespace references
     * within a local scope.
     * <p>
     * Instances of this class, are only created in
     * {@link NamespaceDirectiveResolver#resolveNamespaceReferenceInDirective(ICompilerProject, UserDefinedNamespaceReference, INamespaceDirective)}.
     * <p>
     * Instances of this class maintain state as we walk through the namespace
     * definition directives in a lexical scope.
     * <p>
     * To resolve namespace references in a lexical scope we walk through all
     * the namespace declarations and use directives in source file order. Each
     * declaration or use directive has its state applied to an instance of this
     * class. When we encounter the directive that contains the reference we
     * want to resolve we then use the state accumulated in this class to
     * resolve the reference.
     */
    private static final class NamespaceDirectiveResolver
    {
        /**
         * Resolves a namespace reference found in a particular namespace
         * directive.
         * 
         * @param project {@link ICompilerProject} whose symbol table we should
         * use to resolve reference outside of the file containing the
         * {@link INamespaceDirective}.
         * @param namespaceReference Namespace reference to resolve.
         * @param containingDirective {@link INamespaceDirective} containing the
         * specified reference.
         */
        public static INamespaceDefinition resolveNamespaceReferenceInDirective(ICompilerProject project,
                                                                      UserDefinedNamespaceReference namespaceReference,
                                                                      INamespaceDirective containingDirective)
        {
            assert containingDirective != null;
            NamespaceForwardReferencePredicate pred = null;
            pred = new NamespaceForwardReferencePredicate();

            return resolveNamespaceReferenceInDirective(project,
                    namespaceReference,
                    containingDirective,
                    pred);
        }

        /**
         * Resolves a namespace reference found in a particular namespace
         * directive, Using the given {@link NamespaceForwardReferencePredicate}
         *
         * @param project {@link ICompilerProject} whose symbol table we should
         * use to resolve reference outside of the file containing the
         * {@link INamespaceDirective}.
         * @param namespaceReference Namespace reference to resolve.
         * @param containingDirective {@link INamespaceDirective} containing the
         * specified reference.
         * @param pred The forward reference predicate to use for the resolution
         * @return A namespace definition.
         */
        public static INamespaceDefinition resolveNamespaceReferenceInDirective(ICompilerProject project,
                                                                        UserDefinedNamespaceReference namespaceReference,
                                                                        INamespaceDirective containingDirective,
                                                                        NamespaceForwardReferencePredicate pred)
        {
            ASScope scope = namespaceReference.getScope();

            assert scope != null : "All UserDefinedNamespaceReferences should have a scope!";

            pred = pred.copy();
            if( containingDirective instanceof IDefinition )
                pred.addRef((IDefinition)containingDirective);

            NamespaceDirectiveResolver resolver =
                    new NamespaceDirectiveResolver(project, scope, pred);

            INamespaceDirective currentDirective = scope.getFirstNamespaceDirective();
            // make a resolver to hold the resolution state as we walk through the directives.
            // loop over the directives till we find the directive containing the
            // reference are trying to resolve.
            while (containingDirective != currentDirective)
            {
                // accumulate namespace definition or use namespace information
                // into the resolver.
                currentDirective.resolveDirective(resolver);
                currentDirective = currentDirective.getNext();
                assert currentDirective != null;
            }

            return resolver.resolveDirectiveReference(namespaceReference);

        }

        /**
         * A predicate that will filter out definitions that are a forward reference, which should
         * make them invisible during namespace resolution.
         */
        private static class NamespaceForwardReferencePredicate implements Predicate<IDefinition>
        {
            /**
             * Keep track of all definitions in all the source files we are resolving
             * This is necessary since the chain of namespace references could be arbitrarily deep
             * and go back and forth between multiple files
             */
            private SetMultimap<String, IDefinition> refLocations;

            public NamespaceForwardReferencePredicate()
            {
                refLocations = HashMultimap.create();
            }

            /**
             * @return a new NAmespaceForwardReferencePredicate with the same data, which can be modified
             * without affecting the original.
             */
            NamespaceForwardReferencePredicate copy()
            {
                NamespaceForwardReferencePredicate newPred = new NamespaceForwardReferencePredicate();
                newPred.refLocations = HashMultimap.create();
                newPred.refLocations.putAll(this.refLocations);
                return newPred;
            }

            /**
             * Add a reference from a definition
             */
            void addRef(IDefinition source)
            {
                String sourcePath = source.getSourcePath();
                if( sourcePath != null )
                {
                    refLocations.put(sourcePath, source);
                }
            }

            public boolean apply (IDefinition def)
            {
                if( def != null )
                {
                    String sourceFile = def.getSourcePath();
                    // get the previous references from the same file
                    Set<IDefinition> previousRefs = refLocations.get(sourceFile);
                    if( previousRefs != null )
                    {
                        for( IDefinition d : previousRefs )
                        {
                            // fail if any of the references from the same file
                            // produce a forward reference
                            if( isForwardRef(d, def) )
                            {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
            
            @Override
            public boolean test(IDefinition input)
            {
                return apply(input);
            }

            /**
             * Determine if a reference from one definition to another should be considered a forward
             * reference
             * @param from  the definition the reference is from
             * @param to    the definition the reference is to
             * @return      true if the reference is a forward reference.
             */
            private boolean isForwardRef(IDefinition from, IDefinition to)
            {
                // occurred earlier in the file so its not a forward ref
                if( to.getAbsoluteStart() < from.getAbsoluteStart() )
                    return false;

                IASScope fromScope = from.getContainingScope();
                IASScope toScope = to.getContainingScope();

                // forward ref within the same scope
                // so it is a forward ref
                if( fromScope == toScope )
                    return true;

                IASScope current = fromScope;
                while( current != null )
                {
                    // Forward ref, but the to scope is contained by the from
                    // scope so it's ok
                    if( current == toScope )
                        return false;
                    current = current.getContainingScope();
                }

                // package scopes should count as global scope
                if( toScope instanceof PackageScope )
                    return false;

                // forward ref, and the from scope wasn't contained by the to
                // scope, so this is a forward ref
                return true;
            }
        }
        /**
         * {@link ICompilerProject} whose symbol table we should use to resolve
         * references.
         */
        private final ICompilerProject project;

        /**
         * {@link ASScope} that contains the namespace reference.
         */
        private final ASScope scope;

        /**
         * {@link ASSCope} that contains {@link #scope}. null if there is no
         * such scope.
         */
        private final ASScope containingScope;

        /**
         * Current set of open namespaces. These namespaces are fully resolved.
         */
        private Set<INamespaceDefinition> resolvedOpenNamespaces;

        /**
         * Current namespace definition symbol table. The qualifiers of these
         * namespace defintions are fully resolved.
         */
        private Map<String, List<ResolvedNamespaceDefinitionDirective>> resolvedNamespaceDirectiveSymbolTable;

        /**
         * The forward reference predicate in use by this directive resolver
         */
        private NamespaceForwardReferencePredicate forwardRefPred;

        /**
         * Constructed called only by
         * {@link #resolveNamespaceReferenceInDirective(ICompilerProject, UserDefinedNamespaceReference, INamespaceDirective)}
         * .
         * 
         * @param project {@link ICompilerProject} whose symbol table we should
         * use to resolve non-local symbols.
         * @param scope {@link ASScope} in which we are resolving namespace
         * references.
         * @param pred {@link NamespaceForwardReferencePredicate} used to resolve the directive
         */
        private NamespaceDirectiveResolver(ICompilerProject project, ASScope scope, NamespaceForwardReferencePredicate pred)
        {
            this.project = project;
            this.scope = scope;
            containingScope = scope.getContainingScope();
            resolvedOpenNamespaces = null;
            resolvedNamespaceDirectiveSymbolTable = null;
            forwardRefPred = pred;
        }

        /**
         * Gets the current set of resolved open namespaces.
         * 
         * @return The current set of resolved open namespaces.
         */
        private Set<INamespaceDefinition> getResolvedOpenNamespaces(String name)
        {
            if (resolvedOpenNamespaces == null)
            {
                resolvedOpenNamespaces = new HashSet<INamespaceDefinition>();
                scope.addLocalImportsToNamespaceSet(project.getWorkspace(), resolvedOpenNamespaces);
                scope.addImplicitOpenNamespaces((CompilerProject)project, resolvedOpenNamespaces);
                if (containingScope != null)
                {
                    Set<INamespaceDefinition> containingScopeNamespaceSet = containingScope.getNamespaceSet(project);
                    resolvedOpenNamespaces.addAll(containingScopeNamespaceSet);
                }
                else
                {
                    ((CompilerProject)project).addGlobalUsedNamespacesToNamespaceSet(resolvedOpenNamespaces);
                }
            }

            if (scope != null)
            {
                // If we are looking up a namespace that has been explicitly imported, then add the namespaces
                // from the imports.
                Set<INamespaceDefinition> additionalNamespaces = scope.getExplicitImportQualifiers((CompilerProject)this.project, name);
                if (additionalNamespaces != null)
                {
                    Set<INamespaceDefinition> set = new HashSet<INamespaceDefinition>();
                    set.addAll(resolvedOpenNamespaces);
                    set.addAll(additionalNamespaces);
                    return set;
                }
            }

            return resolvedOpenNamespaces;
        }

        /**
         * Called by
         * {@link NamespaceDefinitionDirective#resolveDirective(NamespaceDirectiveResolver)}
         * to add a fully resolved namespace defintion to the symbol table
         * accumulated by this object.
         * 
         * @param resolvedDirective A resolved namespace definition directive.
         */
        public void addFullyResolvedNamespaceDefinitionDirective(ResolvedNamespaceDefinitionDirective resolvedDirective)
        {
            final String baseName = resolvedDirective.getDirective().getBaseName();
            if (resolvedNamespaceDirectiveSymbolTable == null)
            {
                resolvedNamespaceDirectiveSymbolTable = new HashMap<String, List<ResolvedNamespaceDefinitionDirective>>();
                List<ResolvedNamespaceDefinitionDirective> directiveList = new ArrayList<ResolvedNamespaceDefinitionDirective>(1);
                directiveList.add(resolvedDirective);
                resolvedNamespaceDirectiveSymbolTable.put(baseName, directiveList);
            }
            else
            {
                List<ResolvedNamespaceDefinitionDirective> directiveList = resolvedNamespaceDirectiveSymbolTable.get(baseName);
                if (directiveList == null)
                {
                    directiveList = new ArrayList<ResolvedNamespaceDefinitionDirective>(1);
                    resolvedNamespaceDirectiveSymbolTable.put(baseName, directiveList);
                }
                directiveList.add(resolvedDirective);
            }
        }

        /**
         * Resolves an {@link INamespaceReference} in the current scope using
         * the current state accumulated in this object to a
         * {@link NamespaceDefinition}.
         * 
         * @param reference {@link INamespaceReference} to resolve using the
         * current resolver state.
         * @return An {@link NamespaceDefinition} to which the specified
         * reference resolves to.
         */
        public NamespaceDefinition resolveDirectiveReference(INamespaceReference reference)
        {
            if (reference instanceof NamespaceDefinition)
                return ((NamespaceDefinition)reference);

            assert reference instanceof UserDefinedNamespaceReference : "Unexpected implementation of INamespaceReference";
            UserDefinedNamespaceReference userDefinedNamespaceReference =
                    ((UserDefinedNamespaceReference)reference);

            final String baseName = userDefinedNamespaceReference.getBaseName();
            final INamespaceReference qualifier = userDefinedNamespaceReference.getQualifierNamespace();
            if (qualifier != null)
            {
                INamespaceDefinition resolvedQualifier = resolveDirectiveReference(qualifier);
                if (resolvedQualifier == null)
                    return null;
                return resolveQualifiedDirectiveReference(resolvedQualifier, baseName);
            }
            else
            {
                return resolveDirectiveReference(baseName);
            }
        }

        /**
         * Grr... Java needs dynamic_cast from C++.
         * 
         * @param definition {@link IDefinition} to cast to a
         * {@link NamespaceDefinition}.
         * @return {@link IDefinition} casted to a {@link NamespaceDefinition}
         * or null if the specified {@link IDefinition} was not a
         * {@link NamespaceDefinition}.
         */
        private final NamespaceDefinition getResolvedNamespace(ICompilerProject project, IDefinition definition)
        {
            if (definition instanceof NamespaceDefinition.INamepaceDeclarationDirective)
            {
                definition = ((NamespaceDefinition.INamepaceDeclarationDirective)definition).resolveConcreteDefinition(project, forwardRefPred);
            }
            if (definition instanceof NamespaceDefinition)
                return ((NamespaceDefinition)definition);
            return null;
        }

        /**
         * Resolves a reference to a {@link NamespaceDefinition} using the
         * current resolver state.
         * 
         * @param baseName name to resolve
         * @return {@link NamespaceDefinition} the specified name resolves to in
         * the current resolver state or null.
         */
        private NamespaceDefinition resolveDirectiveReference(String baseName)
        {
            List<IDefinition> definitions = scope.findProperty((CompilerProject)project, baseName, forwardRefPred, getResolvedOpenNamespaces(baseName), DependencyType.NAMESPACE);
            return definitions.size() == 1 ? getResolvedNamespace(project, definitions.get(0)) : null;
        }

        /**
         * Resolves a qualified reference using the current resolver state.
         * 
         * @param resolvedQualifier The reference qualifier
         * @param baseName name to resolve
         * @return The {@link NamespaceDefinition} the reference resolves to or
         * null.
         */
        private NamespaceDefinition resolveQualifiedDirectiveReference(INamespaceDefinition resolvedQualifier, String baseName)
        {

            IDefinition definition =
                    scope.findPropertyQualified(project, forwardRefPred, resolvedQualifier, baseName, DependencyType.NAMESPACE);
            return getResolvedNamespace(project, definition);
        }

        /**
         * Method called by implementation of {@link INamespaceDirective} to add
         * a resolved used namespace to the resolver state.
         * 
         * @param ns Resolved used namespace to add to the resolver state.
         */
        public void addResolvedUsedNamespace(NamespaceDefinition ns)
        {
            resolvedOpenNamespaces.add(ns);
        }

        /**
         * A pair class that stores a namespace definition and its resolved
         * qualifier.
         */
        private static final class ResolvedNamespaceDefinitionDirective
        {
            ResolvedNamespaceDefinitionDirective(NamespaceDefinition resolvedQualifier, NamespaceDefinitionDirective directive)
            {
                this.resolvedQualifier = resolvedQualifier;
                this.directive = directive;
            }

            private final NamespaceDefinition resolvedQualifier;
            private final NamespaceDefinitionDirective directive;

            @SuppressWarnings("unused")
            public NamespaceDefinition getResolvedQualifier()
            {
                return resolvedQualifier;
            }

            public NamespaceDefinitionDirective getDirective()
            {
                return directive;
            }
        }
    }

    @Override
    public boolean isStatic()
    {
        if (getParent() instanceof ClassDefinition)
            // Namespaces declared at class level are always static
            return true;

        return super.isStatic();
    }
}
