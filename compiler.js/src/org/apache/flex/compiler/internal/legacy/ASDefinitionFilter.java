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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.ModifiersSet;
import org.apache.flex.compiler.common.RecursionGuard;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.constants.IMetaAttributeConstants;
import org.apache.flex.compiler.constants.INamespaceConstants;
import org.apache.flex.compiler.definitions.IAccessorDefinition;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IGetterDefinition;
import org.apache.flex.compiler.definitions.IInterfaceDefinition;
import org.apache.flex.compiler.definitions.IMetadataDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ISetterDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.IVariableDefinition;
import org.apache.flex.compiler.definitions.metadata.IMetaTag;
import org.apache.flex.compiler.definitions.references.INamespaceReference;
import org.apache.flex.compiler.internal.definitions.AppliedVectorDefinition;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.definitions.ClassDefinitionBase;
import org.apache.flex.compiler.internal.definitions.InterfaceDefinition;
import org.apache.flex.compiler.internal.definitions.NamespaceDefinition;
import org.apache.flex.compiler.internal.definitions.ScopedDefinitionBase;
import org.apache.flex.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.flex.compiler.internal.scopes.ASFileScope;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.scopes.ASScopeBase;
import org.apache.flex.compiler.internal.scopes.MXMLFileScope;
import org.apache.flex.compiler.internal.scopes.ScopeView;
import org.apache.flex.compiler.internal.scopes.TypeScope;
import org.apache.flex.compiler.internal.tree.as.FileNode;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.units.ICompilationUnit;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * An {@link ASDefinitionFilter} provides the ability to restrict and search
 * specifically for various types of definitions encountered when looking
 * through symbol tables kept inside of scopes.
 * <p>
 * an {@link ASDefinitionFilter} provides the ability to search based on:
 * <ul>
 * <li>Classification of the {@link IDefinition} through
 * {@link ClassificationValue}. For example, we can search for functions but not
 * getters</li>
 * <li>Scope to search in, such as containing, inherited, etc</li>
 * <li>Modifiers on an {@link IDefinition}</li>
 * <li>Namespace of an {@link IDefinition}</li>
 * </ul>
 * </p>
 * <p>
 * While it is possible to build an {@link ASDefinitionFilter} directly, a
 * number of static factory methods exist to build specific filters, such as
 * filters that know how to find members of types, or find all classes for
 * example. When possible, those filters should be used to guarantee correct
 * filtering behavior.
 * </p>
 */
public class ASDefinitionFilter
{
    /**
     * Cache object that preserves a specific filter for use later, removing any
     * references to {@link IASNode} contexts. When finished with a query, the
     * context should be removed by calling clearContext(()
     */
    public static final class CachedDefinitionFilter
    {
        private ASDefinitionFilter fFilter;

        CachedDefinitionFilter(ASDefinitionFilter filter)
        {
            fFilter = filter;
            fFilter.fContext = null;
        }

        /**
         * Returns the underlying {@link ASDefinitionFilter} and sets the
         * current context.
         * 
         * @param context the {@link IASNode} representing the current context
         * @return the underlying {@link ASDefinitionFilter}
         */
        public ASDefinitionFilter getDefinitionFilter(IASNode context)
        {
            fFilter.fContext = new NodeFilterContext(context);
            return fFilter;
        }

        public ASDefinitionFilter getDefinitionFilter(IDefinition context)
        {
            fFilter.fContext = new DefinitionFilterContext(context);
            return fFilter;
        }

        /**
         * Removes any context associated with this filter
         */
        public void clearContext()
        {
            fFilter.fContext = null;
        }
    }

    /**
     * Enum that determines where a search should look
     */
    public static enum SearchScopeValue
    {
        /**
         * Search for definitions in this scope
         */
        IMMEDIATE_MEMBERS,

        /**
         * Search this scope plus the base class scope
         */
        INHERITED_MEMBERS,

        /**
         * Search all available scopes
         */
        ALL_SCOPES
        {
            @Override
            public boolean searchAllNamespaces()
            {
                return false;
            }
        },

        /**
         * Search scopes up to the parent container
         */
        CONTAINING_SCOPES
        {
            @Override
            public boolean searchAllNamespaces()
            {
                return false;
            }
        };

        public boolean searchAllNamespaces()
        {
            return true;
        }
    }

    protected SearchScopeValue fSearchScopeRule;

    /**
     * Classification of definitions that guides an {@link ASDefinitionFilter}
     */
    public static enum ClassificationValue
    {
        FUNCTIONS,
        GETTERS,
        SETTERS,
        ACCESSORS,
        VARIABLES,
        META_TAG_TYPES,
        CLASSES,
        INTERFACES,
        CLASSES_AND_INTERFACES,
        PACKAGES,
        CLASSES_INTERFACES_AND_PACKAGES,
        CLASSES_INTERFACES_AND_NAMESPACES,
        CLASSES_AND_PACKAGES,
        INTERFACES_AND_PACKAGES,
        OBJECTS, // variables, classes, or packages
        ALL, // functions, variables, classes, or packages 
        ALL_NO_ACCESSORS, // functions, variables, classes, or packages, but no accessor functions
        NAMESPACES,
        MEMBERS_AND_TYPES,
        VARIABLES_AND_FUNCTIONS,
        TYPES_FUNCTIONS_AND_VARIABLES,
        CLASSES_INTERFACES_AND_FUNCTIONS
    }

    /**
     * The classification that we are searching for
     */
    protected ClassificationValue fClassificationRule;

    /**
     * IFilter for namespaces
     */
    public static class AccessValue
    {
        private static class SpecialAccessValue extends AccessValue
        {
            public SpecialAccessValue(String name)
            {
                super((INamespaceDefinition)null);
                this.name = name;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean equals(Object obj)
            {
                return this == obj;
            }

            private final String name;

            /**
             * {@inheritDoc}
             * <p>
             * For debugging.
             */
            @Override
            public String toString()
            {
                return "SpecialAccessValue(" + name + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        public final static AccessValue ALL = new SpecialAccessValue("all"); //$NON-NLS-1$

        public final static AccessValue INCLUDED = new SpecialAccessValue("included"); //$NON-NLS-1$

        public final static AccessValue INTERNAL = new SpecialAccessValue(INamespaceConstants.internal_);

        public final static AccessValue PRIVATE = new SpecialAccessValue(INamespaceConstants.private_);

        public final static AccessValue PROTECTED = new SpecialAccessValue(INamespaceConstants.protected_);

        public final static AccessValue PUBLIC = new SpecialAccessValue(INamespaceConstants.public_);

        //make this the same as public
        public final static AccessValue AS3 = new AccessValue(NamespaceDefinition.getAS3NamespaceDefinition());

        INamespaceDefinition namespaceDef = null;

        public AccessValue(INamespaceDefinition n)
        {
            this.namespaceDef = n;
        }

        /**
         * Return a namespace for the access value. May be null
         * 
         * @return INamespaceDefinition
         */
        public INamespaceDefinition getNamespaceDef()
        {
            return namespaceDef;
        }

        /**
         * {@inheritDoc}
         * <p>
         * For debugging.
         */
        @Override
        public String toString()
        {
            return "AccessValue(" + (namespaceDef != null ? namespaceDef.toString() : "null") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            if (namespaceDef == null)
                return super.hashCode();
            return namespaceDef.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;
            if (obj instanceof SpecialAccessValue)
                return obj.equals(this);
            if (obj instanceof AccessValue)
            {
                AccessValue that = (AccessValue)obj;
                if ((that.namespaceDef == namespaceDef) ||
                     ((namespaceDef != null) && (namespaceDef.equals(that.namespaceDef))))
                    return true;
            }
            return false;
        }

        @Deprecated
        public static AccessValue unionAccessRules(AccessValue oldAccessValue, AccessValue newAccessValue)
        {
            if (!(oldAccessValue instanceof AccessValue.SpecialAccessValue))
                return oldAccessValue;
            if (!(newAccessValue instanceof AccessValue.SpecialAccessValue))
                return newAccessValue;

            if ((oldAccessValue == AccessValue.ALL) || (newAccessValue == AccessValue.ALL))
                return AccessValue.ALL;

            if ((oldAccessValue == AccessValue.INTERNAL) || (newAccessValue == AccessValue.INTERNAL))
                return AccessValue.INTERNAL;

            if ((oldAccessValue == AccessValue.PRIVATE) || (newAccessValue == AccessValue.PRIVATE))
                return AccessValue.PRIVATE;

            if ((oldAccessValue == AccessValue.PROTECTED) || (newAccessValue == AccessValue.PROTECTED))
                return AccessValue.PROTECTED;

            if ((oldAccessValue == AccessValue.PUBLIC) || (newAccessValue == AccessValue.PUBLIC))
                return AccessValue.PUBLIC;
            return newAccessValue;
        }

        static public AccessValue createAccessRule(INamespaceDefinition ns)
        {
            if (ns != null && ns instanceof NamespaceDefinition)
            {
                if (ns instanceof NamespaceDefinition.IPublicNamespaceDefinition)
                    return AccessValue.PUBLIC;
                else if (ns instanceof NamespaceDefinition.IPrivateNamespaceDefinition)
                    return AccessValue.PRIVATE;
                else if (ns instanceof NamespaceDefinition.IProtectedNamespaceDefinition)
                    return AccessValue.PROTECTED;
                else if (ns instanceof NamespaceDefinition.IInternalNamespaceDefinition)
                    return AccessValue.INTERNAL;
                else if (ns == NamespaceDefinition.getAS3NamespaceDefinition())
                    return AccessValue.AS3;

                AccessValue accessValue = new AccessValue(ns);
                return accessValue;
            }
            return AccessValue.PUBLIC;
        }
    }

    /**
     * AccessRule to be used by this filter
     */
    protected AccessValue fAccessRule;

    /**
     * AccessRule to be used by this filter in project-level scope. If not set,
     * the normal access value will be used
     */
    protected AccessValue fProjectAccessRule;

    /**
     * Indicate whether or not we require imports (and for which definitions)
     */
    public static enum RequireImportsValue
    {
        YES
        {
            @Override
            public boolean searchAllNamespaces()
            {
                return false;
            }
        },
        NO,
        ONLY_FOR_FUNCTIONS_AND_VARIABLES;

        public boolean searchAllNamespaces()
        {
            return true;
        }
    }

    protected RequireImportsValue fRequireImportsRule;

    /**
     * Current package name (so that internals from the given package can be
     * included) based on the context
     */
    private String fPackageName;

    /**
     * Context (for determining imports)
     */
    protected IFilterContext fContext;

    /**
     * Flag indicating whether or not we should include implicit types (this,
     * super, cast functions) and constructors
     */
    protected boolean fIncludeImplicitsAndConstructors;

    /**
     * Modifiers (e.g. static, final, override) that must be either present or
     * missing on all filtered definitions
     */
    protected HashMap<ASModifier, Boolean> fRequiredAndExcludedModifiers;

    /**
     * True if we should include excluded items. Off by default
     */
    protected Boolean fIncludeExcluded = null;

    /**
     * Constructor.
     * 
     * @param classification classification (functions, classes, all, etc)
     * @param searchScope search scope (all, immediate members, etc)
     * @param access access filter (private, public, etc)
     * @param context the context node (to be used to determine the current
     * imports and package)
     */
    @Deprecated
    public ASDefinitionFilter(ClassificationValue classification,
                              SearchScopeValue searchScope,
                              AccessValue access, IASNode context)
    {
        this(classification, searchScope, access, new NodeFilterContext(context));
    }

    /**
     * Constructor.
     * 
     * @param classification classification (functions, classes, all, etc)
     * @param searchScope search scope (all, immediate members, etc)
     * @param access access filter (private, public, etc)
     * @param context the context node (to be used to determine the current
     * imports and package)
     */
    public ASDefinitionFilter(ClassificationValue classification,
                              SearchScopeValue searchScope,
                              AccessValue access, IDefinition context)
    {
        this(classification, searchScope, access, new DefinitionFilterContext(context));
    }

    /**
     * Constructor.
     * 
     * @param classification classification (functions, classes, all, etc)
     * @param searchScope search scope (all, immediate members, etc)
     * @param access access filter (private, public, etc)
     * @param context the context node (to be used to determine the current
     * imports and package)
     */
    public ASDefinitionFilter(ClassificationValue classification,
                              SearchScopeValue searchScope,
                              AccessValue access, IFilterContext context)
    {
        fClassificationRule = classification;
        fSearchScopeRule = searchScope;
        fAccessRule = access;
        fContext = context;
        fRequireImportsRule = RequireImportsValue.YES;
        fIncludeImplicitsAndConstructors = true;
    }

    /**
     * Builds a filter that will find the given definition
     * 
     * @param definition the {@link IDefinition} we want to eventually find
     */
    public ASDefinitionFilter(IDefinition definition, SearchScopeValue scope)
    {
        fIncludeImplicitsAndConstructors = true;
        fSearchScopeRule = scope;
        fContext = new DefinitionFilterContext(definition);

        if (definition instanceof IClassDefinition)
            fClassificationRule = ClassificationValue.CLASSES;
        else if (definition instanceof IInterfaceDefinition)
            fClassificationRule = ClassificationValue.INTERFACES;
        else if (definition instanceof IPackageDefinition)
            fClassificationRule = ClassificationValue.PACKAGES;
        else if (definition instanceof ISetterDefinition)
            fClassificationRule = ClassificationValue.SETTERS;
        else if (definition instanceof IGetterDefinition)
            fClassificationRule = ClassificationValue.GETTERS;
        else if (definition instanceof IVariableDefinition)
            fClassificationRule = ClassificationValue.VARIABLES;
        else if (definition instanceof INamespaceDefinition)
            fClassificationRule = ClassificationValue.NAMESPACES;
        else if (definition instanceof IFunctionDefinition)
            fClassificationRule = ClassificationValue.FUNCTIONS;
        else if (definition instanceof IMetadataDefinition)
            fClassificationRule = ClassificationValue.META_TAG_TYPES;

        ICompilerProject compilerProject = findProjectForDefinition(definition);
        fAccessRule = AccessValue.createAccessRule(definition.resolveNamespace(compilerProject));

        ModifiersSet modifiersSet = definition.getModifiers();
        if (modifiersSet != null)
        {
            ASModifier[] modifiers = modifiersSet.getAllModifiers();
            for (int i = 0; i < modifiers.length; i++)
            {
                setRequiredModifier(modifiers[i]);
            }
        }
    }

    /**
     * Copy constructor
     * 
     * @param other definition filter to copy
     */
    public ASDefinitionFilter(ASDefinitionFilter other)
    {
        fClassificationRule = other.fClassificationRule;
        fSearchScopeRule = other.fSearchScopeRule;
        fAccessRule = other.fAccessRule;
        fPackageName = other.fPackageName;
        fContext = other.fContext;
        fRequireImportsRule = other.fRequireImportsRule;
        fIncludeImplicitsAndConstructors = true; //TODO [dz] FIX THIS IT SEEMS WRONG
        if (other.fRequiredAndExcludedModifiers != null)
        {
            fRequiredAndExcludedModifiers =
                    new HashMap<ASModifier, Boolean>(other.fRequiredAndExcludedModifiers);
        }
        fProjectAccessRule = other.fProjectAccessRule;
        fIncludeExcluded = other.fIncludeExcluded;
    }

    /**
     * caches the current {@link ASDefinitionFilter} so that it can be stored
     * and used later
     * 
     * @return a {@link CachedDefinitionFilter}
     */
    public CachedDefinitionFilter cacheFilter()
    {
        return new CachedDefinitionFilter(this);
    }

    /**
     * Indicate whether to limit the search to this scope, this scope plus the
     * base class scope, or all available scopes
     * 
     * @param searchScopeRule e.g. inherited members, containing scope, etc
     */
    public void setSearchScopeRule(SearchScopeValue searchScopeRule)
    {
        fSearchScopeRule = searchScopeRule;
    }

    /**
     * Indicate which definitions of symbols to search for
     * 
     * @param classification e.g. variables, functions, etc
     */
    public void setClassification(ClassificationValue classification)
    {
        fClassificationRule = classification;
    }

    /**
     * Get which definitions of symbols this filter is searching for
     * 
     * @return e.g. variables, functions, etc
     */
    public ClassificationValue getClassification()
    {
        return fClassificationRule;
    }

    /**
     * Indicate whether to find private members, public members, namespace
     * members, etc
     * 
     * @param accessRule e.g. private, public, etc
     */
    public void setPrimaryAccessRule(AccessValue accessRule)
    {
        fAccessRule = accessRule;
    }

    /**
     * * Indicate whether to find private members, public members, namespace
     * members, etc in project-level scope
     * 
     * @param accessRule
     */
    public void setProjectAccessRule(AccessValue accessRule)
    {
        fProjectAccessRule = accessRule;
    }

    public AccessValue getProjectAccessRule()
    {
        return fProjectAccessRule;
    }

    /**
     * Returns the AccessValue used by this specific filter
     * 
     * @return an {@link AccessValue}
     */
    public AccessValue getPrimaryAccessRule()
    {
        return fAccessRule;
    }

    public Set<INamespaceDefinition> getNamespaceSet(ICompilerProject project, ASScope scope)
    {
        return getNamespaceSet(project, scope, null);
    }

    Set<INamespaceDefinition> getNamespaceSet(ICompilerProject project, ASScope scope, String name)
    {
        if (fAccessRule != null)
        {
            // Check for user defined namespace access value.
            if (!(fAccessRule instanceof AccessValue.SpecialAccessValue))
            {
                return Collections.singleton(fAccessRule.namespaceDef);
            }

            if (fAccessRule == AccessValue.ALL
                    // If we are looking for "ALL", but want to require imports, then build
                    // the normal namespace set
                    && (fRequireImportsRule.searchAllNamespaces() || fSearchScopeRule.searchAllNamespaces()))
                return ASScopeBase.allNamespacesSet;

            Set<INamespaceDefinition> contextNamespaceSet = fContext.getNamespaceSet(project, name);
            // NOTE: We do NOT need a LinkedHashSet here, because this
            // namespace set is not used for code generation, just for
            // code model compatibility.
            Set<INamespaceDefinition> nsSet = new HashSet<INamespaceDefinition>(contextNamespaceSet.size());
            nsSet.addAll(contextNamespaceSet);

            if ((fAccessRule == AccessValue.PROTECTED) && (scope instanceof TypeScope))
            {
                TypeScope typeScope = (TypeScope)scope;
                IDefinition typeIDefinition = typeScope.getDefinition();
                if (typeIDefinition instanceof ClassDefinitionBase)
                {
                    ClassDefinitionBase classDef = (ClassDefinitionBase)typeIDefinition;
                    nsSet.add(classDef.getProtectedNamespaceReference());
                }
            }

            if (scope != null)
            {
                // Add all the interface namespaces, becuase the interface namespaces
                // are mostly like "public" (i.e. they should be findable when we're searching
                // for "public" stuff.
                nsSet.addAll(getInterfaceNamespaceSets(project, scope));
            }

            if (shouldIncludeImplicitsAndConstructors())
                nsSet.add(NamespaceDefinition.getCodeModelImplicitDefinitionNamespace());

            return nsSet;
        }

        return fContext.getNamespaceSet(project, name);
    }

    public Set<INamespaceDefinition> getNamespaceSetForName(ICompilerProject project, ASScope scope, String name)
    {
        return getNamespaceSet(project, scope, name);
    }

    /**
     * Helper method to grab the interface namespaces when the DefinitionFilter
     * is trying to find "public" definitions. Since the interface members get
     * put in a special namespace, we have to add all those namespaces to the
     * namespace set when the DefinitionFilter wants to find "public" because
     * code model considers interface members as "public".
     * 
     * @param project Project to use to resolve interfaces
     * @param scope the scope we're looking in
     * @return The interface namespace set.
     */
    private Set<INamespaceDefinition> getInterfaceNamespaceSets(ICompilerProject project, ASScope scope)
    {
        Set<INamespaceDefinition> nsSet = new HashSet<INamespaceDefinition>();
        while (scope != null)
        {
            ScopedDefinitionBase sdb = scope.getDefinition();
            if (sdb instanceof InterfaceDefinition)
            {
                // If we find an interface, then just add it's interface namespace set
                nsSet.addAll(((InterfaceDefinition)sdb).getInterfaceNamespaceSet(project));
                break;
            }
            else if (sdb instanceof ClassDefinition)
            {
                // If we find a class, resolve it's interfaces, and then add all of their interface namespace
                // sets
                IInterfaceDefinition[] interfs = ((ClassDefinition)sdb).resolveImplementedInterfaces(project);
                for (int i = 0, l = interfs.length; i < l; ++i)
                {
                    if (interfs[i] != null)
                        nsSet.addAll(((InterfaceDefinition)interfs[i]).getInterfaceNamespaceSet(project));
                }
                break;
            }
            scope = scope.getContainingScope();
        }
        return nsSet;
    }

    /**
     * Indicate whether to require that definitions be imported before including
     * them in results
     * 
     * @param requireImportsRule e.g. yes, no, only_functions_and_variables
     */
    public void setRequireImports(RequireImportsValue requireImportsRule)
    {
        fRequireImportsRule = requireImportsRule;
    }

    /**
     * Flag that determines if we should include classes marked with
     * [ExcludeClass] metadata. This is only relevant when searching for members
     * of a package Defaults to false
     * 
     * @param includeExcludedClasses true if we want to include excluded
     * metadata
     */
    public void setIncludeExcludedClasses(boolean includeExcludedClasses)
    {
        fIncludeExcluded = includeExcludedClasses;
    }

    /**
     * True if included classes should be included in this filter
     * 
     * @return true if they are being included
     */
    public boolean includeExcludedClasses()
    {
        return fIncludeExcluded != null && fIncludeExcluded == Boolean.TRUE;
    }

    @Deprecated
    public void setFindOpenNamespacesInScope(boolean find)
    {
        // NOP  We always know when to look for open namespaces.
    }

    /**
     * Add a modifier to the list of modifiers that must be present in order for
     * a definition to get through the filter
     * 
     * @param modifier modifier (e.g. static, final, or override)
     */
    public void setRequiredModifier(ASModifier modifier)
    {
        if (fRequiredAndExcludedModifiers == null)
            fRequiredAndExcludedModifiers = new HashMap<ASModifier, Boolean>(4);
        fRequiredAndExcludedModifiers.put(modifier, Boolean.TRUE);
    }

    /**
     * Add a modifier to the list of modifiers that must NOT be present in order
     * for a definition to get through the filter
     * 
     * @param modifier modifier (e.g. static, final, or override)
     */
    public void setExcludedModifier(ASModifier modifier)
    {
        if (fRequiredAndExcludedModifiers == null)
            fRequiredAndExcludedModifiers = new HashMap<ASModifier, Boolean>(4);
        fRequiredAndExcludedModifiers.put(modifier, Boolean.FALSE);
    }

    /**
     * Removes a modifier from this list of this either required or excluded
     * 
     * @param modifier the modifier
     */
    public void removeRequiredOrExcludedModifier(ASModifier modifier)
    {
        if (fRequiredAndExcludedModifiers != null)
            fRequiredAndExcludedModifiers.remove(modifier);
    }

    /**
     * Determines if the modifier is required by our filter to indicate a match
     * 
     * @param modifier the modifier we are looking for
     * @return true if we require a modifer
     */
    public boolean requiresModifier(ASModifier modifier)
    {
        if (fRequiredAndExcludedModifiers != null)
        {
            Object object = fRequiredAndExcludedModifiers.get(modifier);
            if (object instanceof Boolean)
                return ((Boolean)object).booleanValue();
        }
        return false;
    }

    /**
     * Determines if the modifier is excluded by our filter to indicate a match
     * 
     * @param modifier the modifier we are looking for
     * @return true if we exclude a modifer
     */
    public boolean excludesModifier(ASModifier modifier)
    {
        if (fRequiredAndExcludedModifiers != null)
        {
            Object object = fRequiredAndExcludedModifiers.get(modifier);
            if (object instanceof Boolean)
                return !((Boolean)object).booleanValue();
        }
        return false;
    }

    public void setIncludeImplicitsAndConstructors(boolean includeImplicitsAndConstructors)
    {
        fIncludeImplicitsAndConstructors = includeImplicitsAndConstructors;
    }

    public boolean shouldIncludeImplicitsAndConstructors()
    {
        return fIncludeImplicitsAndConstructors;
    }

    private boolean isClassMember(IDefinition definition)
    {
        return definition.getParent() instanceof IClassDefinition;
    }

    /**
     * Determines if this filter is looking to match against a user-defined
     * namespace
     * 
     * @return true if this is not a built-in namespace
     */
    public boolean isUserDefinedNamespace()
    {
        return (fAccessRule != null) && (fAccessRule != AccessValue.AS3) && (fAccessRule.namespaceDef != null);
    }

    /**
     * Does the definition match the lists of required/excluded modifiers in
     * this filter?
     * 
     * @param definition definition to test
     * @return true if this definition matches the modifier requirements
     */
    public boolean matchesModifierRules(IDefinition definition)
    {
        if (fIncludeExcluded != null && !fIncludeExcluded)
        {
            boolean exclude = shouldBeExcluded(definition);
            if (exclude)
                return false;
        }

        if (fRequiredAndExcludedModifiers == null)
            return true;

        Iterator<ASModifier> modifiers = fRequiredAndExcludedModifiers.keySet().iterator();
        while (modifiers.hasNext())
        {
            ASModifier modifier = modifiers.next();

            // static only applies to class variables and class methods, so ignore
            // all other cases
            if (modifier.equals(ASModifier.STATIC))
            {
                //if we're a member, keep going unless we're a constructor of a class
                if (!isClassMember(definition) ||
                    (isClassMember(definition) &&
                     definition instanceof IFunctionDefinition &&
                     ((IFunctionDefinition)definition).isConstructor()))
                {
                    continue;
                }
            }
            // final only applies to classes and class methods, so ignore all other cases
            else if (modifier.equals(ASModifier.FINAL))
            {
                if (!(definition instanceof IClassDefinition) &&
                    (!(definition instanceof IFunctionDefinition) || !isClassMember(definition)))
                {
                    continue;
                }
            }
            // override only applies to class methods, so ignore all other cases
            else if (modifier.equals(ASModifier.OVERRIDE))
            {
                if (!(definition instanceof IFunctionDefinition) || !isClassMember(definition))
                    continue;
            }
            // native only applies to functions, so ignore all other cases
            else if (modifier.equals(ASModifier.NATIVE))
            {
                if (!(definition instanceof IFunctionDefinition))
                    continue;
            }
            // dynamic only applies to classes, so ignore all other cases
            else if (modifier.equals(ASModifier.DYNAMIC))
            {
                if (!(definition instanceof IClassDefinition))
                    continue;
            }

            if (fRequiredAndExcludedModifiers.get(modifier) == Boolean.TRUE)
            {
                if (!definition.hasModifier(modifier))
                    return false;
            }
            else
            {
                if (definition.hasModifier(modifier))
                    return false;
            }
        }

        return true;
    }

    /**
     * Should the definition be excluded from lookup results because it has
     * [ExcludeClass] metadata. This method does not check the includeExcluded
     * flag, it simply checks the definition for the presence of the metadata
     * 
     * @param definition the definition to check
     * @return true, if the definition should be excluded based on ExcludeClass
     * metadata
     */
    private static boolean shouldBeExcluded(IDefinition definition)
    {
        if (definition instanceof IClassDefinition)
        {
            // Skip any package members that are marked [ExcludeClass]
            IMetaTag[] metaAttributes =
                    ((IClassDefinition)definition).getMetaTagsByName(
                            IMetaAttributeConstants.ATTRIBUTE_EXCLUDECLASS);
            if (metaAttributes.length > 0)
                return true;
        }
        else if (definition instanceof IInterfaceDefinition)
        {
            IMetaTag[] tags = ((IInterfaceDefinition)definition).getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_EXCLUDECLASS);
            if (tags.length > 0)
            {
                return true;
            }
        }
        else if (definition instanceof IFunctionDefinition)
        {
            if (((IFunctionDefinition)definition).isConstructor())
            {
                IDefinition type = definition.getAncestorOfType(IClassDefinition.class);
                if (type instanceof IClassDefinition)
                {
                    IMetaTag[] metaAttributes =
                            ((IClassDefinition)type).getMetaTagsByName(
                                    IMetaAttributeConstants.ATTRIBUTE_EXCLUDECLASS);
                    if (metaAttributes.length > 0)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Does the definition match the classification portion of this filter?
     * 
     * @param definition definition to test
     * @return true iff this definition matches the classification rule
     */
    public boolean matchesClassificationRule(IDefinition definition)
    {
        if (fClassificationRule == ClassificationValue.ALL)
        {
            return true;
        }
        else if (fClassificationRule == ClassificationValue.ALL_NO_ACCESSORS)
        {
            if (definition instanceof IGetterDefinition ||
                definition instanceof ISetterDefinition)
            {
                return false;
            }
            return true;
        }
        else if (fClassificationRule == ClassificationValue.OBJECTS)
        {
            // don't exclude getters and setters
            return !(definition instanceof IFunctionDefinition) ||
                     definition instanceof IVariableDefinition;
        }
        else if (fClassificationRule == ClassificationValue.ACCESSORS)
        {
            return definition instanceof IAccessorDefinition;
        }
        else if (fClassificationRule == ClassificationValue.VARIABLES_AND_FUNCTIONS)
        {
            return definition instanceof IVariableDefinition ||
                   definition instanceof IFunctionDefinition ||
                   definition instanceof IAccessorDefinition;
        }
        else if (fClassificationRule == ClassificationValue.GETTERS)
        {
            return definition instanceof IGetterDefinition;
        }
        else if (fClassificationRule == ClassificationValue.SETTERS)
        {
            return definition instanceof ISetterDefinition;
        }
        else if (fClassificationRule == ClassificationValue.VARIABLES)
        {
            return definition instanceof IVariableDefinition;
        }
        else if (fClassificationRule == ClassificationValue.META_TAG_TYPES)
        {
            return definition instanceof IMetadataDefinition;
        }
        else if (fClassificationRule == ClassificationValue.FUNCTIONS ||
                 fClassificationRule == ClassificationValue.VARIABLES_AND_FUNCTIONS)
        {
            //we might have an anonymous function, and if that's the case, if we have a variable, see if it's of type function
            //if it is, return true
            if (definition instanceof IVariableDefinition)
            {
                if (definition.getTypeAsDisplayString().equals(IASLanguageConstants.Function))
                    return true;
            }

            if (definition instanceof IFunctionDefinition)
            {
                return true;
            }

            // if we have a class, but we're filtering on functions, check for
            // a constructor
            if (definition instanceof ClassDefinition)
            {
                IFunctionDefinition ctor = ((ClassDefinition)definition).getConstructor();
                return ctor != null;
            }

            return false;
        }
        else if (fClassificationRule == ClassificationValue.NAMESPACES)
        {
            return definition instanceof INamespaceDefinition;
        }
        else if (fClassificationRule == ClassificationValue.CLASSES)
        {
            return definition instanceof IClassDefinition;
        }
        else if (fClassificationRule == ClassificationValue.INTERFACES)
        {
            return definition instanceof IInterfaceDefinition;
        }
        else if (fClassificationRule == ClassificationValue.CLASSES_AND_INTERFACES)
        {
            return (definition instanceof IClassDefinition || definition instanceof IInterfaceDefinition);
        }
        else if (fClassificationRule == ClassificationValue.PACKAGES)
        {
            return definition instanceof IPackageDefinition;
        }
        else if (fClassificationRule == ClassificationValue.CLASSES_INTERFACES_AND_PACKAGES)
        {
            return (definition instanceof IClassDefinition ||
                    definition instanceof IInterfaceDefinition || definition instanceof IPackageDefinition);
        }
        else if (fClassificationRule == ClassificationValue.CLASSES_INTERFACES_AND_NAMESPACES)
        {
            return (definition instanceof IClassDefinition ||
                    definition instanceof IInterfaceDefinition || definition instanceof INamespaceDefinition);
        }
        else if (fClassificationRule == ClassificationValue.CLASSES_AND_PACKAGES)
        {
            return (definition instanceof IClassDefinition || definition instanceof IPackageDefinition);
        }
        else if (fClassificationRule == ClassificationValue.INTERFACES_AND_PACKAGES)
        {
            return (definition instanceof IInterfaceDefinition || definition instanceof IPackageDefinition);
        }
        else if (fClassificationRule == ClassificationValue.MEMBERS_AND_TYPES)
        {
            return definition instanceof IClassDefinition ||
                   definition instanceof IFunctionDefinition ||
                   definition instanceof IInterfaceDefinition ||
                   definition instanceof IVariableDefinition ||
                   definition instanceof INamespaceDefinition;
        }
        else if (fClassificationRule == ClassificationValue.TYPES_FUNCTIONS_AND_VARIABLES)
        {
            return definition instanceof IClassDefinition ||
                   (definition instanceof IFunctionDefinition &&
                     !((IFunctionDefinition)definition).isCastFunction() &&
                     !((IFunctionDefinition)definition).isConstructor() &&
                     !(definition instanceof IGetterDefinition) &&
                     !(definition instanceof ISetterDefinition)) ||
                    definition instanceof IVariableDefinition ||
                    definition instanceof IInterfaceDefinition;
        }
        else if (fClassificationRule == ClassificationValue.CLASSES_INTERFACES_AND_FUNCTIONS)
        {
            return definition instanceof IClassDefinition ||
                   definition instanceof IFunctionDefinition ||
                   definition instanceof IInterfaceDefinition;
        }
        else
        {
            // If we get here, the filter has been created with an invalid value.
            return false;
        }
    }

    /**
     * Does this definition match the implicits/constructors part of this
     * filter?
     * 
     * @param definition node to test
     * @param scope {@link ASScope} containing the original reference that we
     * are resolving, can be null for project leve lookups.
     * @return true iff this node matches the implicits/constructors rule
     */
    public boolean matchesIncludeImplicitsAndConstructorsRule(IDefinition definition, ASScope scope)
    {
        boolean isImplicit = definition.isImplicit();
        boolean isConstructor = (definition instanceof IFunctionDefinition) && ((IFunctionDefinition)definition).isConstructor();

        if ((!isImplicit) && (!isConstructor))
            return true;

        if (fIncludeImplicitsAndConstructors)
        {
            // implicit definition and constructors that
            // are inherited by the main name resolution code
            // should always be filtered out.
            // See http://bugs.adobe.com/jira/browse/CMP-1064 for
            // an example.

            // If this lookup does not originate from an ASScope, then
            // the implicit definitions could not have been inherited so
            // return true.
            if (scope == null)
                return true;

            // if the definition does not have a containing
            // scope it can not be inherited, so return true.
            if (definition.getContainingScope() == null)
                return true;

            // if the definition associated with the definition is
            // not a type definition ( like a class or interface ), then
            // the definition can not be inherited, so return true.
            if (!(definition.getContainingScope().getDefinition() instanceof TypeDefinitionBase))
                return true;

            // At this point the definition is known to be a member of a class
            // or interface.
            // We need to determine if the scope containing the
            // definition is an instance scope that is on the scope
            // chain.

            ASScope currentScope = scope;

            // If the scope chain starts at a type scope
            // switch to the instance scope view of that type scope.
            if (currentScope instanceof TypeScope)
            {
                currentScope = ((TypeScope)currentScope).getInstanceScope();
            }
            else
            {
                // Walk up the scope chain until we find a the instance scope for the enclosing
                // type if there is one.
                while ((currentScope != null) && (!(currentScope.getDefinition() instanceof TypeDefinitionBase)))
                    currentScope = currentScope.getContainingScope();

                // There is no enclosing type scope on the scope chain, so we should
                // filter out the definition ( by returning false ).
                if (currentScope == null)
                    return false;
            }

            assert (currentScope.getDefinition() instanceof TypeDefinitionBase);
            if (currentScope == definition.getContainingScope())
                return true;
        }

        return false;

    }

    /**
     * Get the require imports value
     */
    public RequireImportsValue getRequireImportsValue()
    {
        return fRequireImportsRule;
    }

    /**
     * Get the context node or null
     * 
     * @return context node
     */
    public IFilterContext getContext()
    {
        return fContext;
    }

    /**
     * Does this filter indicate that we should look in base classes and
     * interfaces?
     * 
     * @return true iff we should search inherited scopes
     */
    public boolean searchInheritedScopes()
    {
        return fSearchScopeRule == SearchScopeValue.ALL_SCOPES ||
               fSearchScopeRule == SearchScopeValue.INHERITED_MEMBERS;
    }

    /**
     * Does this filter indicate that we should look in enclosing blocks
     * (functions, classes, packages, etc.)?
     * 
     * @return true if we should search in containing scopes
     */
    public boolean searchContainingScope()
    {
        return fSearchScopeRule == SearchScopeValue.ALL_SCOPES ||
               fSearchScopeRule == SearchScopeValue.CONTAINING_SCOPES;
    }

    /**
     * Create a member filter that's appropriate for the given object
     * definition.
     * 
     * @param object the object whose members we want
     * @param objectDefinition the definition of the object type
     * @param classificationRule type of data we're searching for (function,
     * object, variable, whatever)
     * @param project
     * @param context the context node (to be used to determine the current
     * imports and package)
     * @return appropriate ASDefinitionFilter for member lookup
     */
    static public ASDefinitionFilter createMemberFilter(IExpressionNode object,
                                                        IDefinition objectDefinition,
                                                        ClassificationValue classificationRule,
                                                        ICompilerProject project, IFilterContext context)
    {
        boolean requireStatic = true;
        boolean excludeStatic = false;
        AccessValue accessRule = ASDefinitionFilter.AccessValue.PUBLIC;

        if (objectDefinition instanceof IVariableDefinition)
        {
            requireStatic = false;
            excludeStatic = true;

            ITypeDefinition variableType = objectDefinition.resolveType(project);
            if (variableType instanceof IClassDefinition)
            {
                // If this member access expression is located inside a class definition, we should allow
                // the user to see private members of variables of that class type and protected members
                // of variables of base class types.
                accessRule = adjustAccessRuleForContainingClass(object, (IClassDefinition)variableType, accessRule, project);
            }
        }
        else if (objectDefinition instanceof IClassDefinition ||
                 objectDefinition instanceof IInterfaceDefinition)
        {
            if (!(object instanceof IdentifierNode) &&
                !(object instanceof MemberAccessExpressionNode))
            {
                // If the thing on the left is just some random expression, it can't
                // be a static.
                requireStatic = false;
                excludeStatic = true;
            }

            if (objectDefinition instanceof IClassDefinition)
            {
                accessRule = adjustAccessRuleForContainingClass(object, (IClassDefinition)objectDefinition, accessRule, project);
            }
        }
        else if (objectDefinition instanceof IPackageDefinition ||
                 objectDefinition instanceof IFunctionDefinition)
        {
            requireStatic = false;
            excludeStatic = false;
        }

        SearchScopeValue searchRule = ASDefinitionFilter.SearchScopeValue.INHERITED_MEMBERS;

        ASDefinitionFilter filter = new ASDefinitionFilter(classificationRule, searchRule, accessRule, context);
        if (requireStatic)
            filter.setRequiredModifier(ASModifier.STATIC);
        if (excludeStatic)
            filter.setExcludedModifier(ASModifier.STATIC);

        return filter;
    }

    /**
     * Get the containing class. This particular version will even find the
     * class if the current file is included by a host file and their scopes are
     * temporarily connected (see Project.connectToIncluder).
     * 
     * @param node node to test
     * @return enclosing class (or null, if no such class can be found)
     */
    static private IClassNode getContainingClass(IASNode node)
    {
        IClassNode containingClass = (IClassNode)node.getAncestorOfType(IClassNode.class);
        while (node != null && containingClass == null)
        {
            FileNode containingFile = (FileNode)node.getAncestorOfType(FileNode.class);
            IASScope scope = null;
            if (containingFile != null)
                scope = containingFile.getTemporaryEnclosingScope(new RecursionGuard());

            node = containingFile != null ? (scope != null ? scope.getScopeNode() : null) : null;
            containingClass = node != null ? containingClass = (IClassNode)node.getAncestorOfType(IClassNode.class) : null;
        }
        return containingClass;
    }

    /**
     * If the member access is taking place inside a class definition, adjust
     * the access rule to allow private members (if the object's type matches
     * the class) or protected members (if the object's type matches a base
     * class of the class).
     * 
     * @param memberAccessExpression member access expression for which to
     * generate the filter
     * @param resolvedObjectType type of the object in the member access
     * expression
     * @param accessRule current access rule
     * @return new access rule, combining the current value with whatever new
     * members are allowed (see unionAccessRules)
     */
    static private AccessValue adjustAccessRuleForContainingClass(IASNode memberAccessExpression, IClassDefinition resolvedObjectType, AccessValue accessRule, ICompilerProject project)
    {
        // If this member access expression is located inside a class definition, we should allow
        // the user to see private members of variables of that class type and protected members
        // of variables of base class types.
        AccessValue newVal = accessRule;

        IClassNode containingClass = getContainingClass(memberAccessExpression);
        if (containingClass != null)
        {
            IClassDefinition containingClassDefinition = containingClass.getDefinition();
            if (containingClassDefinition.getQualifiedName().equals(resolvedObjectType.getQualifiedName()))
            {
                newVal = AccessValue.PRIVATE;
            }
            else
            {
                IClassDefinition[] ancestors = containingClassDefinition.resolveAncestry(project);
                for (IClassDefinition ancestor : ancestors)
                {
                    if (ancestor.equals(resolvedObjectType))
                    {
                        newVal = AccessValue.PROTECTED;
                        break;
                    }
                }
            }
        }

        return newVal;
    }

    /**
     * Create a filter that matches all functions in all scopes (including
     * private members)
     * 
     * @param context the context node (to be used to determine the current
     * imports and package and static/not)
     * @return the filter
     */
    @Deprecated
    static public ASDefinitionFilter createAllFunctionsFilter(IASNode context)
    {
        return createAllFunctionsFilter(new NodeFilterContext(context));
    }

    /**
     * Create a filter that matches all functions in all scopes (including
     * private members)
     * 
     * @param context the context node (to be used to determine the current
     * imports and package and static/not)
     * @return the filter
     */
    @Deprecated
    static public ASDefinitionFilter createAllFunctionsFilter(IDefinition context)
    {
        return createAllFunctionsFilter(new DefinitionFilterContext(context));
    }

    /**
     * Create a filter that matches all functions in all scopes (including
     * private members)
     * 
     * @param context the context filter (to be used to determine the current
     * imports and package and static/not)
     * @return the filter
     */
    static public ASDefinitionFilter createAllFunctionsFilter(IFilterContext context)
    {
        boolean contextIsStatic = false;
        if (context != null)
            contextIsStatic = context.isInStaticContext();
        ASDefinitionFilter filter = new ASDefinitionFilter(ClassificationValue.FUNCTIONS, SearchScopeValue.ALL_SCOPES, AccessValue.PRIVATE, context);
        if (contextIsStatic)
            filter.setRequiredModifier(ASModifier.STATIC);
        return filter;
    }

    /**
     * Create a filter that matches all objects (variables, classes, interfaces,
     * or packages) in all scopes (including private members)
     * 
     * @param context the context node (to be used to determine the current
     * imports and package and static/not)
     * @return the filter
     */
    @Deprecated
    static public ASDefinitionFilter createAllObjectsFilter(IASNode context)
    {
        return createAllObjectsFilter(new NodeFilterContext(context));
    }

    /**
     * Create a filter that matches all objects (variables, classes, interfaces,
     * or packages) in all scopes (including private members)
     * 
     * @param context the context node (to be used to determine the current
     * imports and package and static/not)
     * @return the filter
     */
    @Deprecated
    static public ASDefinitionFilter createAllObjectsFilter(IDefinition context)
    {
        return createAllObjectsFilter(new DefinitionFilterContext(context));
    }

    /**
     * Create a filter that matches all objects (variables, classes, interfaces,
     * or packages) in all scopes (including private members)
     * 
     * @param context the context node (to be used to determine the current
     * imports and package and static/not)
     * @return the filter
     */
    static public ASDefinitionFilter createAllObjectsFilter(IFilterContext context)
    {
        boolean contextIsStatic = false;
        if (context != null)
            contextIsStatic = context.isInStaticContext();
        ASDefinitionFilter filter = new ASDefinitionFilter(ClassificationValue.OBJECTS, SearchScopeValue.ALL_SCOPES, AccessValue.PRIVATE, context);
        if (contextIsStatic)
            filter.setRequiredModifier(ASModifier.STATIC);
        return filter;
    }

    /**
     * Create a filter that matches all objects (variables, functions classes,
     * interfaces, or packages) in all scopes (including private members)
     * 
     * @param context the context node (to be used to determine the current
     * imports and package and static/not)
     * @return the filter
     */
    @Deprecated
    static public ASDefinitionFilter createAllSymbolsFilter(IASNode context)
    {
        return createAllSymbolsFilter(new NodeFilterContext(context));
    }

    /**
     * Create a filter that matches all objects (variables, functions classes,
     * interfaces, or packages) in all scopes (including private members)
     * 
     * @param context the context node (to be used to determine the current
     * imports and package and static/not)
     * @return the filter
     */
    static public ASDefinitionFilter createAllSymbolsFilter(IFilterContext context)
    {
        boolean contextIsStatic = false;
        if (context != null)
            contextIsStatic = context.isInStaticContext();
        ASDefinitionFilter filter = new ASDefinitionFilter(ClassificationValue.ALL, SearchScopeValue.ALL_SCOPES, AccessValue.PRIVATE, context);
        if (contextIsStatic)
            filter.setRequiredModifier(ASModifier.STATIC);
        filter.setIncludeExcludedClasses(true);
        return filter;
    }

    /**
     * Create a filter that matches all classes in all scopes
     * 
     * @param context the filter context (to be used to determine the current
     * imports and package)
     * @return the filter
     */
    static public ASDefinitionFilter createAllClassesFilter(IFilterContext context)
    {
        ASDefinitionFilter definitionFilter = new ASDefinitionFilter(ClassificationValue.CLASSES, SearchScopeValue.ALL_SCOPES, AccessValue.PUBLIC, context);
        return definitionFilter;
    }

    /**
     * Create a filter that matches all classes in all scopes
     * 
     * @param context the context node (to be used to determine the current
     * imports and package)
     * @return the filter
     */
    static public ASDefinitionFilter createAllClassesFilter(IASNode context)
    {
        return createAllClassesFilter(new NodeFilterContext(context));
    }

    /**
     * Create a filter that matches all interfaces in all scopes
     * 
     * @param context the filter context (to be used to determine the current
     * imports and package)
     * @return the filter
     */
    static public ASDefinitionFilter createAllInterfacesFilter(IFilterContext context)
    {
        ASDefinitionFilter definitionFilter = new ASDefinitionFilter(ClassificationValue.INTERFACES, SearchScopeValue.ALL_SCOPES, AccessValue.PUBLIC, context);
        return definitionFilter;
    }

    /**
     * Create a filter that matches all interfaces in all scopes
     * 
     * @param context the context node (to be used to determine the current
     * imports and package)
     * @return the filter
     */
    static public ASDefinitionFilter createAllInterfacesFilter(IASNode context)
    {
        return createAllInterfacesFilter(new NodeFilterContext(context));
    }

    /**
     * Create a filter that matches all classes and all interfaces in all scopes
     * 
     * @param context the context node (to be used to determine the current
     * imports and package)
     * @return the filter
     */
    static public ASDefinitionFilter createAllClassesAndInterfacesFilter(IASNode context)
    {
        ASDefinitionFilter definitionFilter = new ASDefinitionFilter(ClassificationValue.CLASSES_AND_INTERFACES, SearchScopeValue.ALL_SCOPES, AccessValue.PUBLIC, context);
        return definitionFilter;
    }

    /**
     * Create a filter that matches all classes, packages and all interfaces in
     * all scopes
     * 
     * @param context the context node (to be used to determine the current
     * imports and package)
     * @return the filter
     */
    static public ASDefinitionFilter createAllClassesInterfacesAndPackagesFilter(IASNode context)
    {
        ASDefinitionFilter definitionFilter = new ASDefinitionFilter(ClassificationValue.CLASSES_INTERFACES_AND_PACKAGES, SearchScopeValue.ALL_SCOPES, AccessValue.PUBLIC, context);
        return definitionFilter;
    }

    /**
     * Create a filter that matches all namespaces in all scopes
     * 
     * @param context the context node (to be used to determine the current
     * imports and package)
     * @return the filter
     */
    static public ASDefinitionFilter createAllNamespacesFilter(IASNode context)
    {
        return createAllNamespacesFilter(new NodeFilterContext(context));
    }

    /**
     * Create a filter that matches all namespaces in all scopes
     * 
     * @param context the context node (to be used to determine the current
     * imports and package)
     * @return the filter
     */
    static public ASDefinitionFilter createAllNamespacesFilter(IFilterContext context)
    {
        ASDefinitionFilter definitionFilter = new ASDefinitionFilter(ClassificationValue.NAMESPACES, SearchScopeValue.ALL_SCOPES, AccessValue.PUBLIC, context);
        return definitionFilter;
    }

    /**
     * Create a filter that matches immediate members of a package, class, or
     * interface
     * 
     * @param memberedDefinition definition whose members will be retrieved
     * using this filter
     * @param classificationValue classifications (CLASS, FUNCTION, ALL)
     * @param includePrivateInternalAndNamespaceMembers if true, this filter
     * will include private, protected, internal, custom namespaced and public
     * members if false, this filter will only include public members
     * @return definition filter for retrieving the members
     */
    static public ASDefinitionFilter createImmediateMemberFilter(IDefinitionNode memberedDefinition, ClassificationValue classificationValue, boolean includePrivateInternalAndNamespaceMembers)
    {
        // If we're including private and internal members, then we should
        // set the access value to PRIVATE and make sure that our context has
        // the same package name as the class.  If not, then we should set the
        // access value to PUBLIC and make sure that our context does not have
        // the same package name as the class.
        AccessValue accessValue = AccessValue.PUBLIC;
        IASNode context = null;
        boolean dontRequireImports = false;
        if (includePrivateInternalAndNamespaceMembers)
        {
            accessValue = AccessValue.ALL;
            context = memberedDefinition;
            // don't require imports, so that we won't do namespace filtering.  This
            // is a problem, as member methods within a different namespace weren't
            // being included in the members
            dontRequireImports = true;
        }
        ASDefinitionFilter definitionFilter = new ASDefinitionFilter(classificationValue, SearchScopeValue.IMMEDIATE_MEMBERS, accessValue, context);
        if (dontRequireImports)
            definitionFilter.setRequireImports(RequireImportsValue.NO);

        return definitionFilter;
    }

    static public ASDefinitionFilter createImmediateMemberFilter(IDefinition memberedDefinition, ClassificationValue classificationValue, boolean includePrivateInternalAndNamespaceMembers)
    {
        // If we're including private and internal members, then we should
        // set the access value to PRIVATE and make sure that our context has
        // the same package name as the class.  If not, then we should set the
        // access value to PUBLIC and make sure that our context does not have
        // the same package name as the class.
        AccessValue accessValue = AccessValue.PUBLIC;
        IDefinition context = null;
        boolean dontRequireImports = false;
        if (includePrivateInternalAndNamespaceMembers)
        {
            accessValue = AccessValue.ALL;
            context = memberedDefinition;
            // don't require imports, so that we won't do namespace filtering.  This
            // is a problem, as member methods within a different namespace weren't
            // being included in the members
            dontRequireImports = true;
        }
        ASDefinitionFilter definitionFilter = new ASDefinitionFilter(classificationValue, SearchScopeValue.IMMEDIATE_MEMBERS, accessValue, context);
        if (dontRequireImports)
            definitionFilter.setRequireImports(RequireImportsValue.NO);

        return definitionFilter;
    }

    /**
     * Create a filter that matches members that are included from a code
     * fragment
     * 
     * @param classificationValue classifications (CLASS, FUNCTION, ALL)
     * @return definition filter for retrieving the members
     */
    static public ASDefinitionFilter createIncludedSymbolsFilter(ClassificationValue classificationValue, IASNode context)
    {
        return ASDefinitionFilter.createIncludedSymbolsFilter(classificationValue, new NodeFilterContext(context));
    }

    /**
     * Create a filter that matches members that are included from a code
     * fragment
     * 
     * @param classificationValue classifications (CLASS, FUNCTION, ALL)
     * @return definition filter for retrieving the members
     */
    static public ASDefinitionFilter createIncludedSymbolsFilter(ClassificationValue classificationValue, IFilterContext context)
    {
        ASDefinitionFilter definitionFilter = new ASDefinitionFilter(classificationValue, SearchScopeValue.IMMEDIATE_MEMBERS, AccessValue.INCLUDED, context);
        return definitionFilter;
    }

    /**
     * Create a filter that matches all members of a package, class, or
     * interface (including inherited ones
     * 
     * @param memberedDefinition definition whose members will be retrieved
     * using this filter
     * @param classificationValue classifications (CLASS, FUNCTION, ALL)
     * @param includePrivateAndInternalMembers if true, this filter will include
     * private, protected, internal, and public members if false, this filter
     * will only include public members
     * @return definition filter for retrieving the members
     */
    static public ASDefinitionFilter createInheritedMemberFilter(IDefinitionNode memberedDefinition, ClassificationValue classificationValue, boolean includePrivateAndInternalMembers)
    {
        // If we're including private and internal members, then we should
        // set the access value to PRIVATE and make sure that our context has
        // the same package name as the class.  If not, then we should set the
        // access value to PUBLIC and make sure that our context does not have
        // the same package name as the class.
        AccessValue accessValue = AccessValue.PUBLIC;
        IASNode context = null;
        if (includePrivateAndInternalMembers)
        {
            accessValue = AccessValue.PRIVATE;
            context = memberedDefinition;
        }
        return new ASDefinitionFilter(classificationValue, SearchScopeValue.INHERITED_MEMBERS, accessValue, context);
    }

    static public ASDefinitionFilter createInheritedMemberFilter(IDefinition memberedDefinition, ClassificationValue classificationValue, boolean includePrivateAndInternalMembers)
    {
        // If we're including private and internal members, then we should
        // set the access value to PRIVATE and make sure that our context has
        // the same package name as the class.  If not, then we should set the
        // access value to PUBLIC and make sure that our context does not have
        // the same package name as the class.
        AccessValue accessValue = AccessValue.PUBLIC;
        IDefinition context = null;
        if (includePrivateAndInternalMembers)
        {
            accessValue = AccessValue.PRIVATE;
            context = memberedDefinition;
        }
        return new ASDefinitionFilter(classificationValue, SearchScopeValue.INHERITED_MEMBERS, accessValue, context);
    }

    /**
     * Does this ASDefinitionFilter require the use of a different predicate to
     * filter results found at the project level
     * 
     * @return true if the definitions found at the project level should use a
     * different filter from the rest of the lookup
     */
    public boolean needsDifferentProjectPredicate()
    {
        return !includeExcludedClasses() || this.getProjectAccessRule() != null;
    }

    /**
     * Generate a predicate that will apply the appropriate filtering for
     * definitions found at the project level
     * 
     * @param project the Project the lookup is occurring in
     * @param scope the scope where we are performing the lookup
     * @return a Predicate that will correctly filter definitions found at the
     * project level.
     */
    public Predicate<IDefinition> computeProjectPredicate(ICompilerProject project, ASScope scope)
    {
        Predicate<IDefinition> projectPredicate = null;
        if (getProjectAccessRule() != null)
        {
            ASDefinitionFilter projectFilter = new ASDefinitionFilter(this);
            projectFilter.setPrimaryAccessRule(this.getProjectAccessRule());
            projectPredicate = projectFilter.computePredicate(project, scope);
        }
        else
        {
            projectPredicate = this.computePredicate(project, scope);
        }
        return projectPredicate;
    }

    /**
     * Generate a predicate that will filter based on the various flags set on
     * the ASDefinitionFilter.
     * 
     * @param project the Project the lookup is occurring in
     * @param scope the scope where we are performing the lookup
     * @return a Predicate that can be used to filter lookup results according
     * to the settings of the ASDefinitionFilter
     */
    public Predicate<IDefinition> computePredicate(ICompilerProject project, ASScope scope)
    {
        Predicate<IDefinition> pred = null;

        pred = new FilterPredicate(project, scope, this);

        if (fAccessRule instanceof AccessValue.SpecialAccessValue)
        {
            Predicate<IDefinition> accessValPredicate = computeAccessValuePredicate(project, scope);
            if (accessValPredicate != null)
            {
                pred = Predicates.and(pred, accessValPredicate);
            }
        }
        if (!includeExcludedClasses())
        {
            Predicate<IDefinition> excludePred = new ExcludedPredicate(scope);
            pred = Predicates.and(pred, excludePred);
        }
        return pred;
    }

    /**
     * Generate a predicate based on the AccessValue of the ASDefinitionFilter.
     * This predicate will do the right thing for private, protected, internal,
     * or public access values.
     */
    public Predicate<IDefinition> computeAccessValuePredicate(ICompilerProject project, ASScope scope)
    {
        Predicate<IDefinition> pred = null;
        if (fAccessRule == AccessValue.ALL)
        {
            pred = null;
        }
        else if (fAccessRule == AccessValue.PRIVATE)
        {
            pred = new PrivateAccessValuePredicate(project, fContext.getNamespaceSet(project));
        }
        else
        {
            if (fAccessRule == AccessValue.PUBLIC)
            {
                pred = new PublicAccessValuePredicate();
            }
            else if (fAccessRule == AccessValue.INTERNAL)
            {
                pred = new InternalAccessValuePredicate();
            }
            else if (fAccessRule == AccessValue.PROTECTED)
            {
                pred = new ProtectedAccessValuePredicate();
            }
            // CM expects these rules to match the public/internal/protected namespaces,
            // but to also match stuff in the open namespaces wherever the lookup was occurring
            if (pred != null)
                pred = Predicates.or(pred, new NamespaceSetPredicate(project, fContext.getNamespaceSet(project)));
        }
        return pred;
    }

    /**
     * Predicate used for filters set up with a "PUBLIC" access value.
     */
    private static class PublicAccessValuePredicate implements Predicate<IDefinition>
    {
        @Override
        public boolean apply(IDefinition d)
        {
            INamespaceReference ns = d.getNamespaceReference();
            if (ns.isLanguageNamespace())
            {
                boolean match = ns instanceof INamespaceDefinition.IPublicNamespaceDefinition ||
                                ns instanceof INamespaceDefinition.IFilePrivateNamespaceDefinition ||
                                ns instanceof INamespaceDefinition.IInterfaceNamespaceDefinition;
                return match;
            }
            // user defined namespaces will be handled by another predicate
            return false;
        }
    }

    /**
     * Predicate used for filters set up with a "INTERNAL" access value.
     */
    private static class InternalAccessValuePredicate implements Predicate<IDefinition>
    {
        @Override
        public boolean apply(IDefinition d)
        {
            INamespaceReference ns = d.getNamespaceReference();
            if (ns.isLanguageNamespace())
            {
                boolean match = ns.isPublicOrInternalNamespace() ||
                                ns instanceof INamespaceDefinition.IFilePrivateNamespaceDefinition ||
                                ns instanceof INamespaceDefinition.IInterfaceNamespaceDefinition;
                return match;
            }

            // user defined namespaces will be handled by another predicate
            return false;
        }
    }

    /**
     * Predicate used for filters set up with a "PROTECTED" access value.
     */
    private static class ProtectedAccessValuePredicate implements Predicate<IDefinition>
    {
        @Override
        public boolean apply(IDefinition d)
        {
            INamespaceReference nsRef = d.getNamespaceReference();
            if (nsRef.isLanguageNamespace())
            {
                boolean match = nsRef.isPublicOrInternalNamespace() ||
                                nsRef instanceof INamespaceDefinition.IProtectedNamespaceDefinition ||
                                nsRef instanceof INamespaceDefinition.IFilePrivateNamespaceDefinition ||
                                nsRef instanceof INamespaceDefinition.IInterfaceNamespaceDefinition;
                return match;
            }

            // user defined namespaces will be handled by another predicate
            return false;
        }
    }

    /**
     * This implements a very simple namespace set predicate for use in
     * combination with the various AccessValue Predicates. It does not do the
     * right thing with respect to protected namespaces - however, when it is
     * used as intended with the AccessValue predicates this is not an issue as
     * CM clients construct filters with "PROTECTED" AccessValues only where
     * they want to find all protecteds, and this does the right thing in that
     * case.
     */
    private static class NamespaceSetPredicate implements Predicate<IDefinition>
    {
        protected Set<INamespaceDefinition> nsSet;
        protected ICompilerProject project;

        NamespaceSetPredicate(ICompilerProject project, Set<INamespaceDefinition> set)
        {
            nsSet = set;
            this.project = project;
        }

        @Override
        public boolean apply(IDefinition d)
        {
            INamespaceDefinition namespace = d.resolveNamespace(project);
            if (nsSet.contains(namespace))
                return true;

            return false;

        }

    }

    /**
     * Predicate used for filters set up with a "PRIVATE" access value. This
     * will find the private members of the containing class, but no private
     * members from base classes, which is what CM clients expect.
     */
    private static class PrivateAccessValuePredicate extends NamespaceSetPredicate
    {
        PrivateAccessValuePredicate(ICompilerProject project, Set<INamespaceDefinition> set)
        {
            super(project, set);
        }

        @Override
        public boolean apply(IDefinition d)
        {
            INamespaceReference nsRef = d.getNamespaceReference();
            if (nsRef instanceof INamespaceDefinition.IPrivateNamespaceDefinition || !nsRef.isLanguageNamespace())
            {
                // If it's a private namespace, or a user defined namespace, then check if it is in our namespace set
                // FB says it wants private properties, but it really means it only wants private properties
                // that are in the open namespace set
                return super.apply(d);
            }
            // If it's any other kind of language namespace, then CM wants it included
            return true;
        }

    }

    /**
     * Predicate to filter out classes and interfaces with [ExcludeClass]
     * metadata
     */
    private static class ExcludedPredicate implements Predicate<IDefinition>
    {
        private String filePath;

        /**
         * @param scope
         */
        public ExcludedPredicate(ASScope scope)
        {
            if (scope != null)
            {
                ASScope fileScope = scope.getFileScope();
                if (fileScope instanceof ASFileScope)
                {
                    filePath = ((ASFileScope)fileScope).getContainingPath();
                }
            }
        }

        @Override
        public boolean apply(IDefinition arg0)
        {
            // check whether the definition is in the same file as the scope
            // if the files are same, then apply filter only in case of
            // mxml files and definitions present directly under file 
            // scope (Component tag in mxml)

            IASScope containingScope = arg0.getContainingScope();
            if (containingScope != null && containingScope instanceof ASScope)
            {
                IASScope fileScope = ((ASScope)containingScope).getFileScope();
                String defPath = ""; //$NON-NLS-1$
                if (fileScope instanceof ASFileScope)
                {
                    defPath = ((ASFileScope)fileScope).getContainingPath();
                }
                if (filePath != null && defPath.equals(filePath))
                {
                    // same file, apply filter only in case of mxml
                    if (containingScope instanceof MXMLFileScope)
                        return !ASDefinitionFilter.shouldBeExcluded(arg0);

                    return true;
                }
            }

            if (ASDefinitionFilter.shouldBeExcluded(arg0))
                return false;
            return true;
        }
    }

    private ICompilerProject findPropertForContainingPathAndScope(final String containingPath, final IASScope scope, final Iterable<WeakReference<ICompilationUnit>> units)
    {
        for (WeakReference<ICompilationUnit> unitRef : units)
        {
            ICompilationUnit unit = unitRef.get();
            if (unit != null)
            {
                try
                {
                    IASScope[] scopes = unit.getFileScopeRequest().get().getScopes();
                    for (IASScope cuScope : scopes)
                    {
                        if (scope == cuScope)
                            return unit.getProject();
                    }
                }
                catch (InterruptedException e)
                {
                }
            }
        }
        return null;
    }

    private ICompilerProject findProjectForScope(IASScope scope)
    {
        while ((scope != null) && (!(scope instanceof ASFileScope)))
        {
            // Check for TypeScope's since the TypeScope
            // might be a scope for a Vector.
            // Vector definitions ( aka AppliedVectorDefinition's ) have a reference
            // to the project, but are not contained by a file scope.
            if (scope instanceof TypeScope || scope instanceof ScopeView)
            {
                ASScope typeScope = (ASScope)scope;
                ITypeDefinition typeDefinition = (ITypeDefinition)typeScope.getDefinition();
                if (typeDefinition instanceof AppliedVectorDefinition)
                    return ((AppliedVectorDefinition)typeDefinition).getProject();
            }
            scope = scope.getContainingScope();
        }
        if (scope == null)
            return null;
        final ASFileScope fileScope = (ASFileScope)scope;
        final Workspace workspace = (Workspace)fileScope.getWorkspace();
        final String containingPath = fileScope.getContainingPath();
        ICompilerProject result = findPropertForContainingPathAndScope(containingPath, scope, workspace.getInvisibleCompilationUnits(containingPath));
        if (result != null)
            return result;
        result = findPropertForContainingPathAndScope(containingPath, scope, workspace.getCompilationUnits(containingPath));
        return result;
    }

    private ICompilerProject findProjectForDefinition(IDefinition def)
    {
        // first check for cases where we can get the project directly,
        // rather than getting the scope
        if (def instanceof AppliedVectorDefinition)
            return ((AppliedVectorDefinition)def).getProject();

        // If we need to, get the scope and find the project from that.
        // Warning: this can be slow.
        return findProjectForScope(def.getContainingScope());
    }
}
