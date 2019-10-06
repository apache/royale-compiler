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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.definitions.AppliedVectorDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinitionBase;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import com.google.common.base.Predicate;

/**
 * IASScope implementation for class & interface Necessary to implement AS3 name
 * resolution semantics correctly because we are using 1 scope to represent
 * instance and static scopes. Does not override findAllDefinition methods, as
 * they return everything that matches in the entire scope chain.
 */
public class TypeScope extends ASScope
{
    private static final StaticPredicate STATIC_ONLY_PREDICATE = new StaticPredicate(true);
    private static final StaticPredicate INSTANCE_ONLY_PREDICATE = new StaticPredicate(false);

    public TypeScope(ASScope containingScope, ScopedBlockNode block, TypeDefinitionBase owningType)
    {
        super(containingScope, block);
        this.owningType = owningType;

        initScopes(containingScope);
    }

    public TypeScope(ASScope containingScope, TypeDefinitionBase owningType)
    {
        super(containingScope);
        this.owningType = owningType;

        initScopes(containingScope);
    }

    private void initScopes(ASScope containingScope)
    {
        staticScope = createStaticScope(containingScope);
        instanceScope = createInstanceScope();
    }

    private StaticScope createStaticScope(ASScope containingScope)
    {
        // Static scope is contained by whatever scope contains the TypeScope
        return new StaticScope(containingScope, this);
    }

    private InstanceScope createInstanceScope()
    {
        // Instance scope always has the static scope as it's containing scope.
        return new InstanceScope(staticScope, this);
    }

    private final TypeDefinitionBase owningType;

    private StaticScope staticScope;
    private InstanceScope instanceScope;

    private boolean needsProtected;
    
    /**
     * Get the scope of the super class - this method will do the right thing depending on
     * if this scope is the instance of static scope.
     * @param project  the Project to resolve things in
     * @return         the scope to use as the scope of the super class.  This may be the instance
     *                 scope of 'Class' (for static scopes), or the instance scope of the base class (for instance scopes),
     *                 or the Type Scope of the baseclass if we are looking for static+instance scopes
     */
    public ASScope resolveSuperScope(ICompilerProject project, ScopeKind kind)
    {
        ASScope result = null;
        if ( owningType instanceof IClassDefinition)
        {
            if( kind == ScopeKind.STATIC )
            {
                // For a static scope, the base scope will be the instance scope of 'Class'
                ITypeDefinition typeDef = project.getBuiltinType(IASLanguageConstants.BuiltinType.CLASS);
                if( typeDef != null )
                {
                    IASScope s = typeDef.getContainedScope();
                    if( s instanceof TypeScope )
                        result = ((TypeScope) s).getInstanceScope();
                }
            }
            else
            {
                // Not static, so resolve the super class and get it's scope
                IClassDefinition superClass = ((IClassDefinition) owningType).resolveBaseClass(project);
                if( superClass instanceof TypeDefinitionBase)
                {
                    IASScope superScope = superClass.getContainedScope();
                    if( superScope instanceof TypeScope )
                    {
                        switch (kind)
                        {
                            case INSTANCE_AND_STATIC:
                                // CM only - if we started with a plain old TypeScope, then just return the super
                                // TypeScope
                                result = (ASScope) superScope;
                                break;
                            case INSTANCE:
                                // Want the instance scope, so grab the instance scope from the base classes contained
                                // scope
                                result = ((TypeScope) superScope).getInstanceScope();
                                break;
                            default:
                                // nothing to do - STATIC was handled above in the if check
                                break;
                        }
                    }
                }
            }
        }
        return result;
    }
    /**
     * Adds the specified definition to this scope.
     * 
     * @param definition The IDefinition to be added.
     */
    @Override
    public void addDefinition(IDefinition definition)
    {
        if (definition != null)
        {
            addDefinitionToStore(definition);

            // Set up the containing scope to correctly point to the static or instance scopes
            if (definition instanceof DefinitionBase)
            {
                boolean isStatic = definition.isStatic();
                final ASScope containingScope;
                if (isStatic)
                {
                    containingScope = getStaticScope();
                }
                else
                {
                    containingScope = getInstanceScope();
                    if (!needsProtected)
                    {
                        INamespaceDefinition protectedNameSpace = owningType.getProtectedNamespaceReference();
                        needsProtected = (protectedNameSpace != null) && (protectedNameSpace.equals(definition.getNamespaceReference()));
                    }
                }
                ((DefinitionBase)definition).setContainingScope(containingScope);

            }
        }
    }

    @Override
    protected void getPropertyForScopeChain(CompilerProject project, Collection<IDefinition> defs, String baseName, NamespaceSetPredicate namespaceSet, boolean findAll)
    {
        getPropertyForScopeChain(project, defs, baseName, namespaceSet, findAll, ScopeKind.INSTANCE_AND_STATIC);
    }

    /**
     * Enum to determine what kind of scope we are emulating.
     */
    static enum ScopeKind
    {
        /**
         * The classic CodeModel view of a class scope as a single scope
         * that contains both instance and static members.
         */
        INSTANCE_AND_STATIC,
        
        /**
         * A filtered view of a class scope that contains only instance members,
         * for use by the compiler.
         */
        INSTANCE,
        
        /**
         * A filtered view of a class scope that contains only static members,
         * for use by the compiler.
         */
        STATIC;

        /**
         * Should this kind of scope find instance members?
         */
        boolean findInstance()
        {
            return this == INSTANCE || this == INSTANCE_AND_STATIC;
        }

        /**
         * Should this kind of scope find static members?
         */
        boolean findStatics()
        {
            return this == STATIC || this == INSTANCE_AND_STATIC;
        }
        
        /**
         * Should this kind of scope find the specified definition?
         */
        boolean findDefinition(IDefinition definition)
        {
            if (this == INSTANCE)
            {
                // An INSTANCE scope sees only non-static members.
                return !definition.isStatic();
            }
            else if (this == STATIC)
            {
                // A STATIC scope sees only static members.
                return definition.isStatic();
            }
            else if (this == INSTANCE_AND_STATIC)
            {
                // An INSTANCE_AND_STATIC scope sees all members.
                return true;
            }
            else
            {
                assert false : "Unknown ScopeKind";
                return false;
            }
        }
    }

    void getPropertyForScopeChain(CompilerProject project, Collection<IDefinition> defs, String baseName, NamespaceSetPredicate nsPred, boolean findAll, ScopeKind lookupKind)
    {
        ArrayList<ITypeDefinition> types = new ArrayList<ITypeDefinition>();
        for (ITypeDefinition type : owningType.typeIteratable(project, false))
        {
            types.add(type);
        }

        int originalDefCount = defs.size();

        // Have to do instances first, in case we're looking for instance and statics
        if (lookupKind.findInstance())
        {
            // We will need to propagate protected namespaces up the chain, but only
            // if we were passed a namespace set that implies "protected" is open

            boolean needProtectedNS = nsPred.containsNamespace(owningType.getProtectedNamespaceReference());

            Collection<IDefinition> iDefs = new FilteredCollection<IDefinition>(INSTANCE_ONLY_PREDICATE, defs);
            for (ITypeDefinition type : types)
            {
                ASScope typeScope = (ASScope)type.getContainedScope();
                if( needProtectedNS )
                {
                    nsPred.setExtraNamespace(type.getProtectedNamespaceReference());
                }

                typeScope.getLocalProperty(project, iDefs, baseName, true);
                if ((!findAll) && (defs.size() > originalDefCount)) {
                    return;
                }
            }
        }
        if (lookupKind.findStatics())
        {
            Collection<IDefinition> sDefs = new FilteredCollection<IDefinition>(STATIC_ONLY_PREDICATE, defs);
            for (ITypeDefinition type : types)
            {
                ASScope typeScope = (ASScope)type.getContainedScope();
                typeScope.getLocalProperty(project, sDefs, baseName, true);
                if ((!findAll) && (defs.size() > originalDefCount)) {
                    return;
                }
            }
        }
    }

    @Override
    protected void getPropertyForMemberAccess(CompilerProject project, Collection<IDefinition> defs, String baseName, NamespaceSetPredicate namespaceSet, boolean findAll)
    {
        getPropertyForMemberAccess(project, defs, baseName, namespaceSet, findAll, ScopeKind.INSTANCE_AND_STATIC);
    }

    protected void getPropertyForMemberAccess(CompilerProject project, Collection<IDefinition> defs, String baseName, NamespaceSetPredicate namespaceSet, boolean findAll, ScopeKind lookupKind)
    {
        int originalDefCount = defs.size();
        boolean needProtectedNamespaces = namespaceSet == ASScopeBase.allNamespacesSet || namespaceSet.containsNamespace(owningType.getProtectedNamespaceReference());

        NamespaceSetPredicate nsPred = namespaceSet;

        Collection<IDefinition> iDefs = new FilteredCollection<IDefinition>(INSTANCE_ONLY_PREDICATE, defs);
        if (lookupKind.findInstance())
        {
            for (ITypeDefinition type : owningType.typeIteratable(project, false))
            {
                ASScope typeScope = (ASScope)type.getContainedScope();
                if (needProtectedNamespaces)
                {
                    nsPred.setExtraNamespace(type.getProtectedNamespaceReference());
                }

                typeScope.getLocalProperty(project,
                                            iDefs,
                                            baseName,
                                            true);

                if ((defs.size() > originalDefCount) && (!findAll))
                    return;
            }
        }
        if (lookupKind.findStatics())
        {
            Collection<IDefinition> sDefs = new FilteredCollection<IDefinition>(STATIC_ONLY_PREDICATE, defs);
            for (ITypeDefinition type : owningType.staticTypeIterable(project, false))
            {
                if (type == null)
                {
                    continue;
                }
                ASScope typeScope = (ASScope)type.getContainedScope();
                typeScope.getLocalProperty(project,
                                            // Only lookup static properties in this scope - for any inherited scopes, we should lookup instance properties
                                            // because C$ (class) extends Class (instance), not Class$ (class)
                                            (typeScope == this) ? sDefs : iDefs,
                                            baseName,
                                            true);

                if ((defs.size() > originalDefCount) && (!findAll))
                    return;
            }
        }
    }

    /**
     * Helper to calculate filtered collections for this scope and the base
     * scopes. This varies because for static lookups, we want to check this
     * scope for "static" properties, but we want to check the base scopes for
     * "instance" properties
     * 
     * @param defs The collection to wrap with the filter
     * @param lookupKind The kind of lookup we're performing
     * @return A MemberAccessCollections with 2 Collections: one to use for this
     * scope, and another to use for base scopes. When we are looking up
     * instance properties, or instance + static properties, then the 2
     * collections will be the same When we are looking up static properties,
     * then the 2 collections will differ (the first one will find only statics,
     * but the second will find only instance properties).
     */
    private MemberAccessCollections getFilteredCollectionsForMemberAccess(Collection<IDefinition> defs, ScopeKind lookupKind)
    {
        MemberAccessCollections p = null;
        if (lookupKind.findInstance() && lookupKind.findStatics())
        {
            // Do nothing, we want both statics and instances
            p = new MemberAccessCollections(defs, defs);
        }
        else if (lookupKind.findStatics())
        {
            // Only lookup static properties in this scope - for any inherited scopes, we should lookup instance properties
            // because C$ (class) extends Class (instance), not Class$ (class)
            p = new MemberAccessCollections(
                    new FilteredCollection<IDefinition>(STATIC_ONLY_PREDICATE, defs),
                    new FilteredCollection<IDefinition>(INSTANCE_ONLY_PREDICATE, defs));
        }
        else if (lookupKind.findInstance())
        {
            Collection<IDefinition> c = new FilteredCollection<IDefinition>(INSTANCE_ONLY_PREDICATE, defs);
            p = new MemberAccessCollections(c, c);
        }
        return p;
    }

    /**
     * Helper class to return 2 collections - my kingdom for a struct, or
     * generic Pair class
     */
    private static class MemberAccessCollections
    {
        public MemberAccessCollections(Collection<IDefinition> thisDefs, Collection<IDefinition> baseDefs)
        {
            this.thisScopeCollection = thisDefs;
            this.baseScopeCollection = baseDefs;
        }

        public final Collection<IDefinition> thisScopeCollection;
        public final Collection<IDefinition> baseScopeCollection;
    }

    @Override
    public void getAllPropertiesForScopeChain(CompilerProject project, Collection<IDefinition> defs, Set<INamespaceDefinition> namespaceSet)
    {
        getAllPropertiesForScopeChain(project, defs, namespaceSet, ScopeKind.INSTANCE_AND_STATIC);
    }

    @Override
    public void getAllPropertiesForMemberAccess(CompilerProject project, Collection<IDefinition> defs, Set<INamespaceDefinition> namespaceSet)
    {
        getAllPropertiesForMemberAccess(project, defs, namespaceSet, ScopeKind.INSTANCE_AND_STATIC);
    }

    protected void getAllPropertiesForMemberAccess(CompilerProject project, Collection<IDefinition> defs, Set<INamespaceDefinition> namespaceSet, ScopeKind lookupKind)
    {
        ArrayList<ITypeDefinition> types = new ArrayList<ITypeDefinition>();
        // For static property lookup we have a different inheritance chain.  
        // Class objects extend Class, not whatever is specified in the extends clause (that only applies to the instance objects)
        //
        // class C{}
        //   C$ (the class object) extends Class
        //   C (instance object) extends Object
        //

        for (ITypeDefinition type : lookupKind == ScopeKind.STATIC ?
                owningType.staticTypeIterable(project, false) :
                owningType.typeIteratable(project, false))
        {
            types.add(type);
        }

        MemberAccessCollections p = getFilteredCollectionsForMemberAccess(defs, lookupKind);
        Collection<IDefinition> thisScopeDefs = p.thisScopeCollection;
        Collection<IDefinition> baseScopeDefs = p.baseScopeCollection;

        boolean needsProtected = false;
        if (namespaceSet != null)
            needsProtected = namespaceSet.contains(owningType.getProtectedNamespaceReference());

        for (ITypeDefinition type : types)
        {
            ASScope typeScope = (ASScope)type.getContainedScope();
            INamespaceDefinition protectedNamespace = null;
            if (needsProtected)
            {
                protectedNamespace = type.getProtectedNamespaceReference();
            }
            // Only lookup static properties in this scope - for any inherited scopes, we should lookup instance properties
            // because C$ (class) extends Class (instance), not Class$ (class)
            typeScope.getAllLocalProperties(project, typeScope == this ? thisScopeDefs : baseScopeDefs, namespaceSet, protectedNamespace);
        }
    }

    protected void getAllPropertiesForScopeChain(CompilerProject project, Collection<IDefinition> defs, Set<INamespaceDefinition> namespaceSet, ScopeKind lookupKind)
    {
        ArrayList<ITypeDefinition> types = new ArrayList<ITypeDefinition>();
        for (ITypeDefinition type : owningType.typeIteratable(project, false))
        {
            types.add(type);
        }
        boolean needsProtected = false;
        if (namespaceSet != null)
            needsProtected = namespaceSet.contains(owningType.getProtectedNamespaceReference());

        defs = getFilteredCollectionForScopeChainLookup(defs, lookupKind);

        for (ITypeDefinition type : types)
        {
            ASScope typeScope = (ASScope)type.getContainedScope();
            INamespaceDefinition protectedNamespace = null;
            if (needsProtected)
            {
                protectedNamespace = type.getProtectedNamespaceReference();
            }
            typeScope.getAllLocalProperties(project, defs, namespaceSet, protectedNamespace);
        }
    }

    /**
     * Helper method to apply the appropriate predicate to the Collection passed
     * in.
     * 
     * @param defs the target collection
     * @param lookupKind what kind of lookup we're performing
     * @return A Collection with the appropriate filters to implement the lookup
     * 1. If looking for statics and instances, do nothing, just return the
     * original collection 2. If looking only for statics, then return a
     * filtered collection, which wraps the collection passed in, and will
     * filter out non-static definitions 3. If looking only for instance
     * properties, then return a filtered collection, which wraps the collection
     * passed in, and will filter out any static definitions
     */
    private Collection<IDefinition> getFilteredCollectionForScopeChainLookup(Collection<IDefinition> defs, ScopeKind lookupKind)
    {
        if (lookupKind.findInstance() && lookupKind.findStatics())
        {
            // do nothing
        }
        else if (lookupKind.findStatics())
        {
            defs = new FilteredCollection<IDefinition>(STATIC_ONLY_PREDICATE, defs);
        }
        else if (lookupKind.findInstance())
        {
            defs = new FilteredCollection<IDefinition>(INSTANCE_ONLY_PREDICATE, defs);
        }
        return defs;
    }

    @Override
    protected boolean namespaceSetSameAsContainingScopeNamespaceSet()
    {
        return false;
    }

    @Override
    public void addImplicitOpenNamespaces(CompilerProject compilerProject, Set<INamespaceDefinition> result)
    {
        addImplicitOpenNamespaces(compilerProject, result, ScopeKind.INSTANCE_AND_STATIC);
    }

    protected void addImplicitOpenNamespaces(CompilerProject compilerProject, Set<INamespaceDefinition> result, ScopeKind lookupKind)
    {
        IDefinition currentDefinition = this.getDefinition();
        if (currentDefinition instanceof ClassDefinition)
        {
            ClassDefinition initialClassDef = (ClassDefinition)currentDefinition;
            // Private namespace always added - same private namespace can be used for static and instance properties
            result.add(initialClassDef.getPrivateNamespaceReference());

            // Protected namespaces differ for static & instance scopes
            if (lookupKind.findInstance())
            {
                result.add(initialClassDef.getProtectedNamespaceReference());
            }
            // Add all the static protected namespaces to the namespace set.
            // Instance protected namespaces are added to the namespace set
            // in TypeScope as we walk up the class hierarchy.  The instance
            // protected namespace needs to change as we walk up the class hierarchy.
            if (lookupKind.findStatics())
            {
                for (IClassDefinition classDef : initialClassDef.classIterable(compilerProject, true))
                    result.add(((ClassDefinition)classDef).getStaticProtectedNamespaceReference());
            }

        }
        else if (currentDefinition instanceof InterfaceDefinition)
        {
            InterfaceDefinition interfaceDefinition = (InterfaceDefinition)currentDefinition;
            result.add(interfaceDefinition.getInterfaceNamespaceReference());
        }
        else if (currentDefinition instanceof AppliedVectorDefinition)
        {
            compilerProject.addGlobalUsedNamespacesToNamespaceSet(result);
            for (IClassDefinition classDef : ((AppliedVectorDefinition)currentDefinition).classIterable(compilerProject, true))
                result.add(((ClassDefinitionBase)classDef).getStaticProtectedNamespaceReference());
        }
    }

    @Override
    public String getContainingSourcePath(String baseName, ICompilerProject project)
    {
        return super.getContainingSourcePath(owningType.getQualifiedName(), project);
    }

    /**
     * Get the ASScope representing the itraits - this scope will only appear to
     * contain instance properties
     */
    public ASScope getInstanceScope()
    {
        return instanceScope;
    }

    /**
     * Get the ASScope representing the ctraits - this scope will only appear to
     * contain static properties
     */
    public ASScope getStaticScope()
    {
        return staticScope;
    }

    @Override
    public void setContainingScope(ASScope containingScope)
    {
        super.setContainingScope(containingScope);
        if (staticScope != null)
            staticScope.setContainingScope(containingScope);
    }

    /**
     * Wrapper class that makes a TypeScope appear to be an instance scope - it
     * will filter out all static properties, and appear to only contain
     * instance properties
     */
    private static class InstanceScope extends ScopeView
    {
        InstanceScope(ASScope containingScope, TypeScope typeScope)
        {
            super(containingScope, typeScope);
        }

        @Override
        protected ScopeKind getScopeKind()
        {
            return ScopeKind.INSTANCE;
        }
    }

    /**
     * Wrapper class that makes a TypeScope appear to be an static scope - it
     * will filter out all instance properties, and appear to only contain
     * static properties
     */
    private static class StaticScope extends ScopeView
    {
        StaticScope(ASScope containingScope, TypeScope typeScope)
        {
            super(containingScope, typeScope);
        }

        @Override
        protected ScopeKind getScopeKind()
        {
            return ScopeKind.STATIC;
        }

    }

    public boolean getNeedsProtected()
    {
        return needsProtected;
    }

    private static class StaticPredicate implements Predicate<IDefinition>
    {
        private boolean findStatics;

        public StaticPredicate(boolean b)
        {
            this.findStatics = b;
        }

        @Override
        public boolean apply(IDefinition definition)
        {
            return findStatics == definition.isStatic();
        }
        
        @Override
        public boolean test(IDefinition input)
        {
            return apply(input);
        }
    }
}
