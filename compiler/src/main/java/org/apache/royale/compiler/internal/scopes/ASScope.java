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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.common.ASImportTarget;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.IImportTarget;
import org.apache.royale.compiler.common.NodeReference;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IQualifiers;
import org.apache.royale.compiler.definitions.IScopedDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.definitions.AmbiguousDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinitionBase;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.ScopedDefinitionBase;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.workspaces.IWorkspace;
import org.apache.royale.utils.CheapArray;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

/**
 * IASScope implementation for class, interface, function, and 'with' scopes.
 */
public abstract class ASScope extends ASScopeBase
{
    private static String[] EMPTY_STRING_ARRAY = new String[0];

    protected static final NamespaceDefinition.IUseNamespaceDirective[] EMPTY_USE_ARRAY =
            new NamespaceDefinition.IUseNamespaceDirective[0];
    
    private static IForLoopNode[] EMPTY_LOOPCHECK_ARRAY = new IForLoopNode[0];

    /**
     * Constructor
     * 
     * @param block block node to which this scope belongs
     */
    public ASScope(ASScope containingScope, ScopedBlockNode block)
    {
        super();
        setContainingScope(containingScope);

        if (block != null)
        {
            block.setScope(this);
            // Node reference constructor only works
            // if the setContainingScope has already be called
            // above.
            scopedNodeRef = new NodeReference(block);
        }
    }

    public ASScope(ASScope containingScope)
    {
        this(containingScope, null);
    }

    private ASScope containingScope;

    /**
     * Weak ref back to the Block node to which this scope belongs TODO: Remove
     * once code model clients don't depend on this anymore
     */
    protected NodeReference scopedNodeRef = NodeReference.noReference;

    /**
     * List of all imports in scope
     */
    private Object importsInScope = null;

    /**
     * List of all aliases for imports in scope
     */
    private Map<String, String> aliasToImportQualifiedName = null;

    private Set<String> packageNames = null;

    private Object usedNamespaces = null;

    private NamespaceDefinition.INamespaceDirective firstNamespaceDirective;

    private NamespaceDefinition.INamespaceDirective lastNamespaceDirective;

    private boolean inWith = false;
    
    private Object loopChecks = null;
    public boolean getHasLoopCheck(){
        return loopChecks != null;
    }
    public void addLoopCheck(IForLoopNode value){
        if (loopChecks == null) loopChecks = CheapArray.create(1);
        else {
            int len = CheapArray.size(loopChecks);
            boolean exitEarly = false;
            //sometimes the same for loop exists as more than one instance for the same source locations.
            //this seems to happen somtimes in mxml code blocks.
            //The following makes sure that only one instance representing the same source code location is tracked
            //for ArrayLike inspection
            for (int i=0; i<len; i++) {
                IForLoopNode existing = (IForLoopNode) CheapArray.get(i, loopChecks);
                if (existing.getAbsoluteStart() == value.getAbsoluteStart()
                    && existing.getAbsoluteEnd() == value.getAbsoluteEnd()
                    && existing.getSourcePath().equals(value.getSourcePath())) {
                    CheapArray.replace(i, value, loopChecks);
                    exitEarly = true;
                    break;
                }
            }
            if (exitEarly) {
               return;
            }
        }
        CheapArray.add(value, loopChecks);
    }
    public IForLoopNode[] getLoopChecks(boolean remove){
        IForLoopNode[] returnLoopChecks = (IForLoopNode[]) CheapArray.toArray(loopChecks, EMPTY_LOOPCHECK_ARRAY);
        if (remove) loopChecks = null;
        return returnLoopChecks;
    }
    /**
     * Sets the scope which lexically contains this scope.
     * 
     * @param containingScope The containing scope.
     */
    public void setContainingScope(ASScope containingScope)
    {
        this.containingScope = containingScope;

        // calc this once, as it shouldn't change
        this.inWith = getContainingWithScope() != null;
    }

    /**
     * Compact the ArrayLists in this scope (so that they don't take up as much
     * space)
     */
    @Override
    public void compact()
    {
        super.compact();

        if (importsInScope != null)
            CheapArray.optimize(importsInScope, EMPTY_STRING_ARRAY);
        
        if (usedNamespaces != null)
            CheapArray.optimize(usedNamespaces, EMPTY_USE_ARRAY);

        if (loopChecks != null)
            CheapArray.optimize(loopChecks, EMPTY_LOOPCHECK_ARRAY);
    }

    public void addNamespaceDirective(NamespaceDefinition.INamespaceDirective directive)
    {
        if (lastNamespaceDirective != null)
        {
            lastNamespaceDirective.setNext(directive);
            lastNamespaceDirective = directive;
        }
        else
        {
            assert firstNamespaceDirective == null;
            firstNamespaceDirective = directive;
            lastNamespaceDirective = directive;
        }
    }

    public void addUseDirective(NamespaceDefinition.IUseNamespaceDirective useDirective)
    {
        addNamespaceDirective(useDirective);
        if (usedNamespaces == null)
            usedNamespaces = CheapArray.create(1);
        CheapArray.add(useDirective, usedNamespaces);
    }

    public boolean hasImportAlias(String alias)
    {
        return aliasToImportQualifiedName != null
                && aliasToImportQualifiedName.containsKey(alias);
    }

    public void addImport(String target, String alias)
    {
        if (aliasToImportQualifiedName == null)
        {
            aliasToImportQualifiedName = new HashMap<String, String>();
        }
        assert !hasImportAlias(alias) : "addImport() should not be called with an existing alias";
        addImport(target);
        aliasToImportQualifiedName.put(alias, target);
    }

    public void addImport(String target)
    {
        if (importsInScope == null)
        {
            importsInScope = CheapArray.create(20);
            packageNames = new HashSet<String>();
        }

        CheapArray.add(target, importsInScope);

        int idx = target.lastIndexOf('.');

        if (idx != -1)
        {
            String packName = target.substring(0, idx);
            if (!target.endsWith(".*"))
            {
                // If this is not a wildcard import, then add the imported name
                // to the importedNames table so we can construct the right namespace
                // set for that name when we see a reference to it.
                String defName = target.substring(idx + 1, target.length());

                if (importedNames == null)
                    importedNames = new HashMap<String, Set<String>>();

                Set<String> s = importedNames.get(defName);
                if (s == null)
                {
                    s = new LinkedHashSet<String>();
                    importedNames.put(defName, s);
                }
                s.add(packName);
            }
            // Whether the import was a wildcard or not, the packageName contributes to the
            // set of known package names.
            packageNames.add(packName);
        }
    }

    @Override
    public ASScope getContainingScope()
    {
        return containingScope;
    }

    /**
     * Re-connects this scope to the syntax tree node that corresponds to this
     * scope.
     * 
     * @param node {@link IScopedNode} that corresponds to this scope.
     */
    public void reconnectScopeNode(IScopedNode node)
    {
        scopedNodeRef.reconnectNode(node);
    }

    @Override
    public IScopedNode getScopeNode()
    {
        IWorkspace w = getWorkspace();
        return (IScopedNode)scopedNodeRef.getNode(w, this);
    }

    public String[] getImports()
    {
        return (String[])CheapArray.toArray(importsInScope, EMPTY_STRING_ARRAY);
    }

    private ScopedDefinitionBase containingDefinition;

    @Override
    public ScopedDefinitionBase getDefinition()
    {
        return containingDefinition;
    }

    public void setContainingDefinition(ScopedDefinitionBase value)
    {
        containingDefinition = value;
    }

    /**
     * For debugging only.
     */
    @Override
    protected String toStringHeader()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(super.toStringHeader());

        IDefinition definition = getDefinition();
        if (definition != null)
        {
            sb.append(" for ");
            sb.append(definition.toString());
        }

        return sb.toString();
    }

    /**
     * Determine whether the string passed in is a known package name The scope
     * will check if the package was introduced by any of it's imports, and if
     * not will delegate to its containing scope.
     * 
     * @param p the string to test
     * @return true is p is a package name
     */
    public boolean isPackageName(String p)
    {
        if (packageNames != null)
        {
            if (packageNames.contains(p))
            {
                return true;
            }
        }

        if (containingScope != null)
            return containingScope.isPackageName(p);

        return false;
    }

    /**
     * Return the additional namespaces for a reference, if the name has been
     * explicitly imported. If 'a.b.Foo' has been imported, and we see reference
     * to Foo, this will return the INamespaceDefinition for 'a.b'. If the name
     * has not been explicitly imported then this method will return the empty
     * set.
     * 
     * @param project CompilerProject to use to resolve the package INamespaces
     * @param name The name of the reference
     * @return A Set<INamespaceDefinition> representing the packages from the
     * imports if the name was explicitly imported. Returns the empty set if the
     * name was not explicitly imported.
     */
    public Set<INamespaceDefinition> getExplicitImportQualifiers(CompilerProject project, String name)
    {
        Set<INamespaceDefinition> nsSet = new LinkedHashSet<INamespaceDefinition>();

        Workspace workspace = project.getWorkspace();

        getContainingScopeExplicitImports(project, name, nsSet);

        if (importedNames != null)
        {
            // Was it an import in this scope
            Set<String> packages = importedNames.get(name);
            if (packages != null)
            {
                for (String s : packages)
                {
                    nsSet.add(workspace.getPackageNamespaceDefinitionCache().get(s, false));
                }
            }
        }
        return nsSet.size() > 0 ? nsSet : Collections.<INamespaceDefinition> emptySet();
    }

    /**
     * Get the additional namespaces for a reference, if the name has been explicitly imported in
     * a containing scope
     * @param project   the active project
     * @param name      the name of the reference
     * @param nsSet     the namespace set to add the namespaces to
     */
    protected void getContainingScopeExplicitImports (CompilerProject project, String name, Set<INamespaceDefinition> nsSet)
    {
        if (getContainingScope() != null)
        {
            // check any containing scopes
            nsSet.addAll(getContainingScope().getExplicitImportQualifiers(project, name));
        }
    }

    /**
     * Maps names to the package name used to look them up - this is used to
     * store explicit imports of definitions (import a.b.Foo)
     */
    private Map<String, Set<String>> importedNames;

    protected INamespaceReference[] getUsedNamespaces()
    {
        return (INamespaceReference[])CheapArray.toArray(usedNamespaces, EMPTY_USE_ARRAY);
    }

    /**
     * Gets the first namespace definition or use namespace directive in the
     * scope.
     * 
     * @return The first namespace definition or use namespace directive in the
     * scope.
     */
    public NamespaceDefinition.INamespaceDirective getFirstNamespaceDirective()
    {
        return firstNamespaceDirective;
    }

    /**
     * Adds {@link INamespaceDefinition}'s for each import in this scope to the
     * specified namespace set.
     * 
     * @param workspace {@link IWorkspace} used to construct
     * {@link INamespaceDefinition}'s for imported packages.
     * @param namespaceSet Namespace set to add namespaces to.
     */
    public void addLocalImportsToNamespaceSet(IWorkspace workspace, Set<INamespaceDefinition> namespaceSet)
    {
        String[] imports = getImports();
        if (imports != null)
        {
            for (String importStr : imports)
            {
                IImportTarget importTarget = ASImportTarget.get(workspace, importStr);
                // Only wildcard imports contribute to the namespace set
                // e.g. a.b.*, but not a.b.Foo
                if (importTarget.isWildcard())
                    namespaceSet.add(importTarget.getNamespace());
            }
        }
    }

    /**
     * Calculate the namespace set to use to resolve name. If name is an
     * explicitly imported definition, then the namespace set will consist of
     * the package name from the import(s) plus the open namespace set. If name
     * was not explitly imported then the open namespace set will be calculated
     * and returned
     * 
     * @param project The compiler project
     * @param name A name.
     * @return the namespace set to use to lookup name. This set should not be
     * modified
     */
    public Set<INamespaceDefinition> getNamespaceSetForName(ICompilerProject project, String name)
    {
        // if the name is an alias, we want the original name -JT
        name = resolveBaseNameFromAlias(name);
        if (namespaceSetSameAsContainingScopeNamespaceSet() && getContainingScope() != null)
        {
            // If this scope doesn't contribute anything to the namespace set, then just ask our containing
            // scope for the namespace set.  Doing this before we hit the cache has the benefit that the 
            // namespace set will only get cached in the containing scopes cache, instead of getting cached
            // in each individual scope cache (e.g. it will be cached in the class scope, instead of in each function
            // scope in the class).  This saves a lot of memory, as many functions will not affect the list 
            // of open namespaces.
            return getContainingScope().getNamespaceSetForName(project, name);
        }
        CompilerProject compilerProject = (CompilerProject)project;
        ASScopeCache scopeCache = compilerProject.getCacheForScope(this);
        return scopeCache.getNamespaceSetForName(name);
    }

    protected boolean namespaceSetSameAsContainingScopeNamespaceSet()
    {
        if ((getImports() != null) || (getUsedNamespaces() != null))
            return false;

        // function with no namespace set modifications, so reuse
        if (containingDefinition instanceof FunctionDefinition)
            return true;

        // TODO: can with scopes also be optimized here?

        return false;
    }

    /**
     * Implementation of getNamespaceSetForName method, above. The scope cache
     * will call this method when it does not already have the results cached
     * 
     * @param project
     * @param name
     * @return the namespace set to use to lookup name. This set should not be
     * modified
     */
    Set<INamespaceDefinition> getNamespaceSetForNameImpl(ICompilerProject project, String name)
    {
        if (namespaceSetSameAsContainingScopeNamespaceSet())
        {
            ASScope containingScope = getContainingScope();
            if (containingScope != null)
            {
                return containingScope.getNamespaceSetForName(project, name);
            }
        }

        Set<INamespaceDefinition> openNamespaces = getNamespaceSet(project);
        // If the reference has been explicitly imported, then we are a qualified name lookup
        // e.g. 'import a.b.Foo' means that any reference to Foo must have the package namespace of 'a.b' added
        // to its set of namespaces
        Set<INamespaceDefinition> additionalNamespaces = getExplicitImportQualifiers((CompilerProject)project, name);
        if (additionalNamespaces != null)
        {
            Set<INamespaceDefinition> newSet = new LinkedHashSet<INamespaceDefinition>();
            newSet.addAll(openNamespaces);
            newSet.addAll(additionalNamespaces);
            return newSet;
        }
        else
        {
            return openNamespaces;
        }
    }

    /**
     * Computes and returns the namespace set for this scope.
     * <p>
     * The returned set should not be modified.
     * 
     * @param project The compiler project.
     * @return The namespace set for this scope. The returned set should not be
     * modified
     */
    public Set<INamespaceDefinition> getNamespaceSet(ICompilerProject project)
    {
        CompilerProject compilerProject = (CompilerProject)project;
        ASScopeCache scopeCache = compilerProject.getCacheForScope(this);
        return scopeCache.getNamespaceSet();
    }

    /**
     * Computes and returns the namespace set for this scope. This is the
     * implementation of getNamespaceSet above. The scope cache will call this
     * method when it does not have a cached result for the namespace set.
     * <p>
     * The returned set should not be modified.
     * 
     * @param project
     * @return The namespace set for this scope. The returned set should not be
     * modified
     */
    Set<INamespaceDefinition> getNamespaceSetImpl(ICompilerProject project)
    {
        if (namespaceSetSameAsContainingScopeNamespaceSet())
        {
            ASScope containingScope = getContainingScope();
            if (containingScope != null)
            {
                return containingScope.getNamespaceSetImpl(project);
            }
        }

        CompilerProject compilerProject = (CompilerProject)project;
        IWorkspace workspace = compilerProject.getWorkspace();

        Set<INamespaceDefinition> result = new LinkedHashSet<INamespaceDefinition>();

        // First add the imports, use namespaces, etc from this scope
        this.addLocalImportsToNamespaceSet(workspace, result);
        INamespaceReference[] usedNamespaces = this.getUsedNamespaces();
        if (usedNamespaces != null)
        {
            for (INamespaceReference usedNamespaceReference : usedNamespaces)
            {
                INamespaceDefinition usedNamespace = usedNamespaceReference.resolveNamespaceReference(compilerProject);
                if (usedNamespace != null)
                    result.add(usedNamespace);
            }
        }

        this.addImplicitOpenNamespaces(compilerProject, result);

        // Next add the open namespaces from the containing scope
        addNamespacesFromContainingScope(compilerProject, result);

        Set<INamespaceDefinition> emptyNamespaceSet = Collections.emptySet();
        result = result.size() == 0 ? emptyNamespaceSet : result;
        return result;
    }

    /**
     * Add the open namespaces from the containing scope to the namespace set passed in
     * @param compilerProject   the active project
     * @param result            the Namespace Set to add namespaces to
     */
    protected void addNamespacesFromContainingScope (CompilerProject compilerProject, Set<INamespaceDefinition> result)
    {
        ASScope containingScope = this.getContainingScope();
        if (containingScope != null)
        {
            result.addAll(containingScope.getNamespaceSet(compilerProject));
        }
    }

    public void addImplicitOpenNamespaces(CompilerProject compilerProject, Set<INamespaceDefinition> result)
    {
        // By default there is nothing to do here. 
        // overrides in ASFileScope, PackageScope, and TypeScope.
    }

    /**
     * Adds all definitions ( including definitions from base types ) in the
     * current scope to the specified collections of definitions that have a
     * namespace qualifier in the specified definition set, when looking for
     * definitions in the scope chain.
     * 
     * @param project {@link CompilerProject} used to resolve reference to
     * definitions outside of the {@link ICompilationUnit} that contains this
     * scope.
     * @param defs Collection that found {@link IDefinition}'s are added to.
     * @param namespaceSet Namespace set in which the qualifier of any matching
     * definition must exist to be considered a match.
     */
    public void getAllPropertiesForScopeChain(CompilerProject project, Collection<IDefinition> defs, Set<INamespaceDefinition> namespaceSet)
    {
        getAllLocalProperties(project, defs, namespaceSet, null);
    }

    /**
     * Adds all definitions ( including definitions from base types ) in the
     * current scope to the specified collections of definitions that have a
     * namespace qualifier in the specified definition set, when looking for
     * definitions through a member access.
     * 
     * @param project {@link CompilerProject} used to resolve reference to
     * definitions outside of the {@link ICompilationUnit} that contains this
     * scope.
     * @param defs Collection that found {@link IDefinition}'s are added to.
     * @param namespaceSet Namespace set in which the qualifier of any matching
     * definition must exist to be considered a match.
     */
    public void getAllPropertiesForMemberAccess(CompilerProject project, Collection<IDefinition> defs, Set<INamespaceDefinition> namespaceSet)
    {
        getAllLocalProperties(project, defs, namespaceSet, null);
    }

    /**
     * Gets all definitions (including definitions from base types) that have
     * the specified name to the specified collections of definitions that have
     * a namespace qualifier in the specified definition set, when looking for
     * definitions through a member access.
     * 
     * @param project {@link CompilerProject} used to resolve reference to
     * definitions outside of the {@link ICompilationUnit} that contains this
     * scope.
     * @param memberName the name of the desired definition(s).
     * @param namespaceSet Namespace set in which the qualifier of any matching
     * definition must exist to be considered a match.
     * @return the collection of matching definitions.
     */
    public List<IDefinition> getPropertiesByNameForMemberAccess(CompilerProject project, String memberName, Set<INamespaceDefinition> namespaceSet)
    {
        //  Get the collection of all properties.
        List<IDefinition> result = new ArrayList<IDefinition>();

        getPropertyForMemberAccess(project, result, memberName, namespaceSet, true);

        return result;
    }

    /**
     * Gets the definition (including definitions from base types) that has
     * the specified name and that has a namespace qualifier in the specified
     * namespace set, when looking for definitions through a member access.
     *
     * @param project {@link CompilerProject} used to resolve reference to
     * definitions outside of the {@link ICompilationUnit} that contains this
     * scope.
     * @param memberName the name of the desired definition(s).
     * @param namespaceSet Namespace set in which the qualifier of any matching
     * definition must exist to be considered a match.
     * @return The first definition that matches the name and namespaceSet.  May return the AmbiguousDefinition
     *         if more than one definition in a scope matches.
     */
    public IDefinition getPropertyByNameForMemberAccess(CompilerProject project, String memberName, Set<INamespaceDefinition> namespaceSet)
    {
        List<IDefinition> defs = new ArrayList<IDefinition>();
        getPropertyForMemberAccess(project, defs, memberName, namespaceSet, false);
        return getSingleResult(project, defs);
    }

    /**
     * Finds all the definitions in this scope that match the specified
     * namespace set and base name. This method is intended to implement the
     * getproperty operation defined by AS3 and the VM.
     * <p>
     * If this scope is not for a class or interface definition then only
     * definitions in this scope are considered.
     * <p>
     * If this scope is for a class or interface definition then definitions in
     * this scope and the scope for any implemented or extended interfaces and
     * classes are also considered. Unless findAll is true, then this function
     * returns as soon as one or more definitions has been found that match the
     * namespace set and base name.
     * <p>
     * 
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param defs Collection of {@link IDefinition}'s to add found definitions
     * to.
     * @param baseName The name of the definition(s) to find.
     * @param namespaceSet The namespace set in which a found definition's
     * qualifier must be in.
     * @param findAll If true find all match definitions that match the baseName
     * and namespace set not just those in the first scope that had one or more
     * matches.
     */
    public void getPropertyForMemberAccess(CompilerProject project, Collection<IDefinition> defs, String baseName, Set<INamespaceDefinition> namespaceSet, boolean findAll)
    {
        NamespaceSetPredicate nsPred = new NamespaceSetPredicate(project, namespaceSet);
        Collection<IDefinition> filteredDefs = new FilteredCollection<IDefinition>(nsPred, defs);
        getPropertyForMemberAccess(project, filteredDefs, baseName, nsPred, findAll);
    }


    /**
     * Finds all the definitions in this scope that match the specified
     * namespace set and base name. This method is intended to implement the
     * getproperty operation defined by AS3 and the VM.
     * <p>
     * This version of the method expects that the Collection passed in will implement
     * whatever filtering is necessary, other than filtering based on the base name.
     * For most cases, this means the Collection will be an {@link ASScopeBase.FilteredCollection}
     * with a {@link NamespaceSetPredicate}.
     * The {@link NamespaceSetPredicate} must also be passed down as some name resolution
     * may need it to apply extra namespaces (i.e. deal with protected namespaces)
     *
     * <p>
     * If this scope is not for a class or interface definition then only
     * definitions in this scope are considered.
     * <p>
     * If this scope is for a class or interface definition then definitions in
     * this scope and the scope for any implemented or extended interfaces and
     * classes are also considered. Unless findAll is true, then this function
     * returns as soon as one or more definitions has been found that match the
     * namespace set and base name.
     * <p>
     *
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param defs Collection of {@link IDefinition}'s to add found definitions
     * to.  This collection must perform any necessary filtering of results, other than filtering
     * based on the baseName.
     * @param baseName The name of the definition(s) to find.
     * @param namespaceSet The {@link NamespaceSetPredicate} which the name resolution code
     *                     can use to modify the namespace set as necessary.
     * @param findAll If true find all match definitions that match the baseName
     * and namespace set not just those in the first scope that had one or more
     * matches.
     */
    protected void getPropertyForMemberAccess(CompilerProject project, Collection<IDefinition> defs, String baseName, NamespaceSetPredicate namespaceSet, boolean findAll)
    {
        getLocalProperty(project, defs, baseName, true);
    }

    /**
     * Helper method to get a namespace set for a member access
     */
    private Set<INamespaceDefinition> getNamespaceSetForMemberAccess(ICompilerProject project, IDefinition def, boolean isSuperRef)
    {
        Set<INamespaceDefinition> namespaceSet;
        if (def instanceof InterfaceDefinition)
            // If we are getting a property from an interface, use the special interface namespace set
            namespaceSet = ((InterfaceDefinition)def).getInterfaceNamespaceSet(project);
        else if (isSuperRef)
            namespaceSet = getNamespaceSetForSuper(project, def);
        else
        {
            namespaceSet = getNamespaceSet(project);

            // If the expression a.b occurs inside the class definition for a's type A,
            // then add A's protected namespace so that we can see a protected b.
            if (def == getContainingClass())
                namespaceSet.add(((IClassDefinition)def).getProtectedNamespaceReference());
        }
        return namespaceSet;
    }

    /**
     * Find a property in an IDefinition, using the open namespaces & packages
     * of this scope.
     *
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param def The definition to resolve the property in
     * @param name The name of the definition to find
     * @param isSuperRef whether this lookup is through a 'super' reference - if
     * it is then the namespace set will be adjusted to use the base classes
     * protected namespace instead of the containing classes protected namespace
     * @return The IDefinition for the property, or null if one is not found
     */
    public IDefinition getPropertyFromDef(ICompilerProject project, IDefinition def, String name, boolean isSuperRef)
    {
        CompilerProject compilerProject = (CompilerProject)project;
        Set<INamespaceDefinition> namespaceSet = getNamespaceSetForMemberAccess(project, def, isSuperRef);

        return getPropertyFromDef(compilerProject, def, name, namespaceSet, false);
    }

    /**
     * Find a property in an IDefinition, using the open namespaces & packages
     * of this scope, and any additional constraints that are passed in as a {@link Predicate}.
     *
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param def The definition to resolve the property in
     * @param name The name of the definition to find
     * @param additional A {@link Predicate} that will perform additional filtering of the results.
     *                   This {@link Predicate} will run before any namespace set checking.
     * @param isSuperRef whether this lookup is through a 'super' reference - if
     * it is then the namespace set will be adjusted to use the base classes
     * protected namespace instead of the containing classes protected namespace
     * @return The IDefinition for the property, or null if one is not found
     */
    public IDefinition getPropertyFromDef(ICompilerProject project,
                                          IDefinition def,
                                          String name,
                                          Predicate<IDefinition> additional,
                                          boolean isSuperRef)
    {
        NamespaceSetPredicate nsPred = new NamespaceSetPredicate(project, getNamespaceSetForMemberAccess(project, def, isSuperRef));
        Predicate<IDefinition> combinedPred = Predicates.and(additional, nsPred);
        return getPropertyFromDef((CompilerProject)project, def, name, combinedPred, nsPred, isSuperRef);
    }
    /**
     * Find a property in an IDefinition, using the namespace passed in as the
     * qualifier.
     *
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve references.
     * @param def The definition to resolve the property in
     * @param name The name of the definition to find
     * @param qualifier The namespace to us to look up name
     * @param isSuperRef whether this lookup is through a 'super' reference - if
     * it is then the namespace set will be adjusted to use the base classes
     * protected namespace instead of the containing classes protected namespace
     * @return The IDefinition for the property, or null if one is not found
     */
    public IDefinition getQualifiedPropertyFromDef(ICompilerProject project, IDefinition def, String name,
                                                   INamespaceDefinition qualifier, boolean isSuperRef)
    {
        Set<INamespaceDefinition> namespaceSet = ImmutableSet.of(qualifier);
        if (isSuperRef)
            namespaceSet = adjustNamespaceSetForSuper(def, namespaceSet);

        return getPropertyFromDef((CompilerProject)project, def, name, namespaceSet, false);
    }

    /**
     * Find a property in an IDefinition, using the qualifiers passed in to provide the namespace set.
     *
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve references.
     * @param def The definition to resolve the property in
     * @param name The name of the definition to find
     * @param qualifiers The namespace(s) to us to look up name
     * @param isSuperRef whether this lookup is through a 'super' reference - if
     * it is then the namespace set will be adjusted to use the base classes
     * protected namespace instead of the containing classes protected namespace
     * @return The IDefinition for the property, or null if one is not found
     */
    public IDefinition getQualifiedPropertyFromDef(ICompilerProject project, IDefinition def, String name,
                                                   IQualifiers qualifiers, boolean isSuperRef)
    {
        Set<INamespaceDefinition> namespaceSet = qualifiers.getNamespaceSet();
        if (isSuperRef)
            namespaceSet = adjustNamespaceSetForSuper(def, namespaceSet);

        return getPropertyFromDef((CompilerProject)project, def, name, namespaceSet, false);
    }

    /**
     * Implementation for getPropertyFromDef + getQualifiedPropertyFromDef
     */
    private IDefinition getPropertyFromDef(CompilerProject project, IDefinition def, String name, Set<INamespaceDefinition> namespaceSet, boolean lookForStatics)
    {
        NamespaceSetPredicate nsPred = new NamespaceSetPredicate(project, namespaceSet);
        return getPropertyFromDef(project, def, name, nsPred, nsPred, lookForStatics);
    }

    /**
     * Implementation of getPropertyFromDef + getQualifiedPropertyFromDef
     *
     * @param project           project to resolve references in
     * @param def               The {@link IDefinition} to get the property from
     * @param name              The name to look for
     * @param pred              The {@link Predicate} to use to perform the lookup
     * @param nsPred            The {@link NamespaceSetPredicate} to use if the namespace set needs to be modified
     *                          during lookup
     * @param lookForStatics    whether to find statics or not
     */
    private IDefinition getPropertyFromDef(CompilerProject project,
                                           IDefinition def,
                                           String name,
                                           Predicate<IDefinition> pred,
                                           NamespaceSetPredicate nsPred,
                                           boolean lookForStatics)
    {
        ASScope defScope = (ASScope)(def instanceof IScopedDefinition ? ((IScopedDefinition)def).getContainedScope() : null);

        // TODO: eliminate lookForStatics flag from getPropertyFromDef methods
        if (defScope instanceof TypeScope)
        {
            // Adjust scope if we are looking in a TypeScope
            TypeScope ts = (TypeScope)defScope;
            if (lookForStatics)
                defScope = ts.getStaticScope();
            else
                defScope = ts.getInstanceScope();
        }

        if (defScope != null)
        {
            ArrayList<IDefinition> defs = new ArrayList<IDefinition>(1);

            defScope.getPropertyForMemberAccess(project, new FilteredCollection<IDefinition>(pred, defs), name, nsPred, false);

            return getSingleResult(project, defs);
        }
        return null;
    }

    /**
     * Implementation of getPropertyForScopeChain.
     *
     * This method will filter results based on baseName only - any additional filtering
     * should be done by the {@link Collection} passed in.
     *
     * @param project       {@link CompilerProject} to resolve things in
     * @param defs          The {@link Collection} to add the results to
     * @param baseName      The name of the definition to find
     * @param namespaceSet  the {@link NamespaceSetPredicate} to use if the namespace set needs to be adjusted
     *                      during lookup
     */
    protected void getPropertyForScopeChain(CompilerProject project, Collection<IDefinition> defs, String baseName, NamespaceSetPredicate namespaceSet, boolean findAll)
    {
        getLocalProperty(project, defs, baseName, true);
    }

    protected String resolveBaseNameFromAlias(String possibleAlias)
    {
        if (aliasToImportQualifiedName != null
                && aliasToImportQualifiedName.containsKey(possibleAlias))
        {
            String qualifiedName = aliasToImportQualifiedName.get(possibleAlias);
            int index = qualifiedName.lastIndexOf(".");
            if (index != -1)
            {
                return qualifiedName.substring(index + 1);
            }
            return qualifiedName;
        }
        ASScope containingScope = getContainingScope();
        if (containingScope != null)
        {
            return containingScope.resolveBaseNameFromAlias(possibleAlias);
        }
        return possibleAlias;
    }

    protected String resolveAliasFromQualifiedImport(String qualifiedName)
    {
        if (aliasToImportQualifiedName != null
                && aliasToImportQualifiedName.containsValue(qualifiedName))
        {
            for (String key : aliasToImportQualifiedName.keySet())
            {
                if (aliasToImportQualifiedName.get(key).equals(qualifiedName))
                {
                    return key;
                }
            }
        }
        ASScope containingScope = getContainingScope();
        if (containingScope != null)
        {
            return containingScope.resolveAliasFromQualifiedImport(qualifiedName);
        }
        return null;
    }

    protected String resolveQualifiedNameFromAlias(String possibleAlias)
    {
        if (aliasToImportQualifiedName != null
                && aliasToImportQualifiedName.containsKey(possibleAlias))
        {
            return aliasToImportQualifiedName.get(possibleAlias);
        }
        ASScope containingScope = getContainingScope();
        if (containingScope != null)
        {
            return containingScope.resolveQualifiedNameFromAlias(possibleAlias);
        }
        return possibleAlias;
    }

    /**
     * This is called by {@link ASScopeCache} when there was a cache miss.
     * 
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param baseName base name of the property we are looking for.
     * @param namespaceSet Namespace set in which the qualifier of any found
     * definition must found.
     * @param dt The type of dependency that should be added to the dependency
     * graph when resolving this reference across a compilation unit boundary.
     * @return One or more {@link IDefinition}'s matched by the namespace set
     * and base name.
     */
    public List<IDefinition> findProperty(CompilerProject project, String baseName, Set<INamespaceDefinition> namespaceSet, DependencyType dt)
    {
        return findProperty(project, baseName, namespaceSet, dt, false);
    }

    /**
     * Version of findProperty that determine the results based on the namespace set passed in,
     * along with any additional constraints passed in via the {@link Predicate}.
     *
     *
     * @param project       The {@link CompilerProject} to resolve things in
     * @param baseName      The name to find
     * @param additional    Any additional constraints on the lookup.  This predicate will
     *                      run before any namespace checking occurs.
     * @param namespaceSet  The Namespace set to use for the lookup
     * @param dt            The dependency type to introduce if this resolves to something from
     *                      another compilation unit
     * @return              a List of IDefinition that matched the name, namespace set, and any
     *                      additional constraints specified by the predicate.
     */
    public List<IDefinition> findProperty(CompilerProject project,
                                          String baseName,
                                          Predicate<IDefinition> additional,
                                          Set<INamespaceDefinition> namespaceSet,
                                          DependencyType dt)
    {
        return findProperty(project, baseName, additional, namespaceSet, dt, false);
    }

    /**
     * This is the core <code>findproperty()</code> method. It implements the
     * equivalent of the <code>findprop</code> AVM instruction in Royale.
     * <p>
     * The algorithm searches up the scope chain, starting with this scope, for
     * definitions with the specified base name and namespace set.
     * <p>
     * After the file scope, the project scope is searched if necessary. If
     * definitions are found in the project scope, a dependency is created on
     * the compilation unit that produced them.
     * <p>
     * If the <code>findAll</code> parameter is <code>false</code>, the search
     * stops with the first scope that has one or more matching definition; if
     * it is <code>true</code>, the search continues to find all matching
     * definitions in the entire chain, including the project scope.
     * 
     * @param accumulator Collection to which definitions that match the
     * namespace set and base name are added.
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param baseName base name of the property we are looking for.
     * @param namespaceSet Namespace set in which the qualifier of any found
     * definition must found.
     * @param dt The type of dependency that should be added to the dependency
     * graph when resolving this reference across a compilation unit boundary.
     * @param findAll If true, then find all definitions that match the
     * namespace set and base name, not just those from the first scope with a
     * single match.
     */
    public void findProperty(Collection<IDefinition> accumulator, CompilerProject project,
                              String baseName, Set<INamespaceDefinition> namespaceSet,
                              DependencyType dt, boolean findAll)
    {
        NamespaceSetPredicate nsPred = new NamespaceSetPredicate(project, namespaceSet);
        FilteredCollection<IDefinition> filteredCollection = new FilteredCollection<IDefinition>(nsPred, accumulator);
        findProperty(filteredCollection, project, baseName, nsPred, dt, findAll);
    }

    /**
     * This is the implementation of the various <code>findproperty()</code> methods. It implements the
     * equivalent of the <code>findprop</code> AVM instruction in Royale.
     * <p>
     * The algorithm searches up the scope chain, starting with this scope, for
     * definitions with the specified base name.
     * <p>
     * If any additional constraints are required (e.g. filtering based on the namespace set), then
     * callers should pass in an {@link ASScopeBase.FilteredCollection} as the accumulator that will implement those
     * constraints.  For the common case, the accumulator will be an {@link ASScopeBase.FilteredCollection}
     * with a {@link NamespaceSetPredicate}.
     * <p>
     * After the file scope, the project scope is searched if necessary. If
     * definitions are found in the project scope, a dependency is created on
     * the compilation unit that produced them.
     * <p>
     * If the <code>findAll</code> parameter is <code>false</code>, the search
     * stops with the first scope that has one or more matching definition; if
     * it is <code>true</code>, the search continues to find all matching
     * definitions in the entire chain, including the project scope.
     *
     * @param accumulator Collection to which definitions that match the
     * base name are added.
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param baseName base name of the property we are looking for.
     * @param nsPred The {@link NamespaceSetPredicate}, if one is being used, that the lookup
     *               can modify as it walks up the scope chain (necessary to handle protected correctly).
     * @param dt The type of dependency that should be added to the dependency
     * graph when resolving this reference across a compilation unit boundary.
     * @param findAll If true, then find all definitions that match the
     * namespace set and base name, not just those from the first scope with a
     * single match.
     */
    protected void findProperty(Collection<IDefinition> accumulator, CompilerProject project,
                             String baseName, NamespaceSetPredicate nsPred,
                             DependencyType dt, boolean findAll)
    {

        assert accumulator.isEmpty() : "findProperty() should not be called with a non-empty collection";
        assert baseName.indexOf('.') == -1 : "baseName must not be any sort of qname";

        // Walk the scope chain starting with this scope.
        // This loop may go as far as the file scope, whose containing scope is null. 
        // But it may break out early; lastSearchScope will keep track of how far it went. 
        ASScope lastSearchedScope = null;
        String baseNameForAlias = this.resolveBaseNameFromAlias(baseName);
        for (ASScope currentScope = this; currentScope != null; currentScope = currentScope.getContainingScope())
        {
            // If we're not looking for all matching definitions, 
            // and we've already got some, stop walking. 
            if (!findAll && accumulator.size() != 0)
                break;

            // Search one scope for any definitions matching baseName and naamespaceSet. 
            currentScope.getPropertyForScopeChain(project, accumulator, baseNameForAlias, nsPred, findAll);

            // Keep track of the last scope that was searched. 
            lastSearchedScope = currentScope;
        }

        assert lastSearchedScope != null || accumulator.size() == 0 : "If accumulator is not empty, which searched scope added to it?";

        // Determine whether we need to search the project scope. 
        boolean searchProjectScope = false;

        // If we haven't found any matching definitions yet, 
        // we need to search the project scope. 
        if (accumulator.size() == 0)
            searchProjectScope = true;

        // If we're looking for all matching definitions, 
        // we need to search the project scope. 
        else if (findAll)
            searchProjectScope = true;

        // If the last scope we searched was a package scope or a file scope, 
        // we need to search the project scope because the project scope 
        // might have other definitions with the same name which should 
        // cause an ambiguity. 
        else if (lastSearchedScope instanceof PackageScope ||
                 lastSearchedScope instanceof ASFileScope)
        {
            searchProjectScope = true;
        }

        // Search the project scope if necessary. 
        if (searchProjectScope)
        {
            ASProjectScope projectScope = project.getScope();
            projectScope.getPropertyForScopeChain(this, accumulator, baseNameForAlias, nsPred.getNamespaceSet(), dt);
        }
        if(!baseName.equals(baseNameForAlias)) // has alias
        {
            // remove anything with the same base name that doesn't have an
            // alias, unless its base name is equal to the alias. that is the
            // only time where there will be ambiguity. -JT
            String alias = baseName; //for clarity
            ArrayList<IDefinition> toRemove = new ArrayList<IDefinition>();
            String qualifiedNameForAlias = resolveQualifiedNameFromAlias(alias);
            for(IDefinition definition : accumulator)
            {
                if(!definition.getBaseName().equals(alias)
                        && !definition.getQualifiedName().equals(qualifiedNameForAlias))
                {
                    toRemove.add(definition);
                }
            }
            // some collections can't remove while iterating, so do it after
            // collecting all of the definitions to remove -JT
            accumulator.removeAll(toRemove);
        }
        else // no alias
        {
            // remove anything that has an alias, unless its alias is equal to
            // the original base name.
            ArrayList<IDefinition> toRemove = new ArrayList<IDefinition>();
            for (IDefinition definition : accumulator)
            {
                String otherAlias = resolveAliasFromQualifiedImport(definition.getQualifiedName());
                if (otherAlias != null && !otherAlias.equals(baseName))
                {
                    toRemove.add(definition);
                }
            }
            accumulator.removeAll(toRemove);
        }
    }

    /**
     * For each {@link IProtectedNamespaceDefinition} in the given
     * {@code namespaceSet}, if it does not have a corresponding
     * {@link IStaticProtectedNamespaceDefinition}, create one and add it to the
     * {@code namespaceSet}.
     * 
     * @param namespaceSet Namespace definitions. New items might be added.
     * @return Updated namespace definitions.
     */
    @SuppressWarnings("unused")
    private static Set<INamespaceDefinition> addStaticProtectedNS(Set<INamespaceDefinition> namespaceSet)
    {
        if (namespaceSet == null)
            return null;

        // The keys are URI strings. The values are "protected" namespace definitions.
        final Map<String, INamespaceDefinition.IStaticProtectedNamespaceDefinition> staticProtectedNamespaces =
                new HashMap<String, INamespaceDefinition.IStaticProtectedNamespaceDefinition>();
        final Set<INamespaceDefinition.IProtectedNamespaceDefinition> protectedNamespaces =
                new HashSet<NamespaceDefinition.IProtectedNamespaceDefinition>();
        for (final INamespaceDefinition namespace : namespaceSet)
        {
            if (namespace instanceof INamespaceDefinition.IStaticProtectedNamespaceDefinition)
            {
                final INamespaceDefinition.IStaticProtectedNamespaceDefinition staticProtectedNamespace =
                        (INamespaceDefinition.IStaticProtectedNamespaceDefinition)namespace;
                staticProtectedNamespaces.put(
                        staticProtectedNamespace.getURI(),
                        staticProtectedNamespace);
            }
            else if (namespace instanceof INamespaceDefinition.IProtectedNamespaceDefinition)
            {
                protectedNamespaces.add((INamespaceDefinition.IProtectedNamespaceDefinition)namespace);
            }
        }

        // Find all "protected" namespace definitions that don't have their 
        // corresponding "static protected" namespace definitions.
        final Set<INamespaceDefinition.IStaticProtectedNamespaceDefinition> addedStaticProtectedNamespaces =
                new HashSet<NamespaceDefinition.IStaticProtectedNamespaceDefinition>();
        for (final INamespaceDefinition.IProtectedNamespaceDefinition protectedNamespace : protectedNamespaces)
        {
            if (!staticProtectedNamespaces.containsKey(protectedNamespace.getURI()))
            {
                addedStaticProtectedNamespaces.add(
                        NamespaceDefinition.createStaticProtectedNamespaceDefinition(
                                protectedNamespace.getURI()));
            }
        }

        final Set<INamespaceDefinition> result;
        if (addedStaticProtectedNamespaces.isEmpty())
        {
            result = namespaceSet;
        }
        else
        {
            result = new HashSet<INamespaceDefinition>();
            result.addAll(addedStaticProtectedNamespaces);
            result.addAll(namespaceSet);
        }
        return result;
    }

    /**
     * This is the core findproperty method. This method implements the
     * equivalent of the findprop AVM instruction in Royale.
     * 
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param baseName base name of the property we are looking for.
     * @param namespaceSet Namespace set in which the qualifier of any found
     * definition must found.
     * @param dt The type of dependency that should be added to the dependency
     * graph when resolving this reference across a compilation unit boundary.
     * @param findAll If true, then find all definitons that match the namespace
     * set and base name, not just those from the first scope with a single
     * match.
     * @return One or more {@link IDefinition}'s matched by the namespace set
     * and base name.
     */
    private List<IDefinition> findProperty(CompilerProject project, String baseName, Set<INamespaceDefinition> namespaceSet, DependencyType dt, boolean findAll)
    {
        ArrayList<IDefinition> defs = new ArrayList<IDefinition>(1);
        findProperty(defs, project, baseName, namespaceSet, dt, findAll);
        return defs;
    }

    /**
     * Version of findProperty that determine the results based on the namespace set passed in,
     * along with any additional constraints passed in via the {@link Predicate}.
     *
     *
     * @param project       The {@link CompilerProject} to resolve things in
     * @param baseName      The name to find
     * @param additional    Any additional constraints on the lookup.  This predicate will
     *                      run before any namespace checking occurs.
     * @param namespaceSet  The Namespace set to use for the lookup
     * @param dt            The dependency type to introduce if this resolves to something from
     *                      another compilation unit
     * @return              a List of IDefinition that matched the name, namespace set, and any
     *                      additional constraints specified by the predicate.
     */
    private List<IDefinition> findProperty(CompilerProject project,
                                           String baseName,
                                           Predicate<IDefinition> additional,
                                           Set<INamespaceDefinition> namespaceSet,
                                           DependencyType dt,
                                           boolean findAll)
    {
        ArrayList<IDefinition> defs = new ArrayList<IDefinition>(1);
        NamespaceSetPredicate nsPred = new NamespaceSetPredicate(project, namespaceSet);
        Predicate<IDefinition> pred = Predicates.and(additional, nsPred);
        FilteredCollection<IDefinition> filteredCollection = new FilteredCollection<IDefinition>(pred, defs);
        findProperty(filteredCollection, project, baseName, nsPred, dt, findAll);
        return defs;
    }

    /**
     * Is this scope inside a with scope.
     * 
     * @return true if this scope is nested in a with scope, or this scope is a
     * with scope
     */
    public boolean isInWith()
    {
        return inWith;
    }

    /**
     * Get any containing with scope
     * 
     * @return the ASScope that is the containing with scope, or null if there
     * is no containing with scope
     */
    ASScope getContainingWithScope()
    {
        ASScope scope = this;
        while (scope != null)
        {
            if (scope instanceof WithScope)
                return scope;
            scope = scope.getContainingScope();
        }
        return scope;
    }

    /**
     * IFilter the result of a findDefinition based on any containing with scopes
     * and if the lookup is allowed to escape a with scope This method is used
     * by findProperty, and findPropertyQualified to filter the results. By
     * default, the lookup will look past the with scopes (this is what code
     * model expects), so we will filter the results here, but only if we are in
     * a with scope. With scopes are rare enough that the performance hit
     * shouldn't be too bad, since we only do the filtering when we know we are
     * in a with scope. If we are not in a with scope, or the lookup is allowed
     * to escape the with block, then we immediately return the passed in
     * definition.
     * 
     * @param d the definition the lookup resolved to
     * @param canEscapeWith true if the lookup can esape a with, false if it
     * can't
     * @return the definition to use as the result of the lookup
     */
    IDefinition filterWith(IDefinition d, boolean canEscapeWith)
    {
        if (!inWith || canEscapeWith || d == null)
            return d;

        ASScope withScope = getContainingWithScope();
        if (withScope != null)
        {
            ASScope scope = this;
            while (scope != null)
            {
                // Didn't find the defns containing scope before we hit
                // the with, so act as if we couldn't resolve it.
                if (scope == withScope)
                    return null;
                // We found the declaring scope, and we haven't seen a with
                // scope yet, so we can return the definition
                if (scope == d.getContainingScope())
                    return d;

                scope = scope.getContainingScope();
            }
            return null;
        }
        return d;
    }

    /**
     * The main public entry point for the findprop operation in the compiler.
     * This method uses the {@link ASScopeCache} to improve performance.
     * 
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param baseName base name of the property we are looking for.
     * @param dt The type of dependency that should be added to the dependency
     * graph when resolving this reference across a compilation unit boundary.
     * @return A single {@link IDefinition} to which the specified base name
     * resolves to in this scope, or null. Null is returned when no definition
     * is found <b>and</b> when more than one definition is found.
     */
    public IDefinition findProperty(ICompilerProject project, String baseName, DependencyType dt)
    {
        return findProperty(project, baseName, dt, false);
    }

    /**
     * The main public entry point for the findprop operation in the compiler.
     * This method uses the {@link ASScopeCache} to improve performance.
     * 
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param baseName base name of the property we are looking for.
     * @param dt The type of dependency that should be added to the dependency
     * graph when resolving this reference across a compilation unit boundary.
     * @param canEscapeWith should this lookup find definitions that occur
     * outside of a containing with scope
     * @return A single {@link IDefinition} to which the specified base name
     * resolves to in this scope, or null. Null is returned when no definition
     * is found <b>and</b> when more than one definition is found.
     */
    public IDefinition findProperty(ICompilerProject project, String baseName, DependencyType dt, boolean canEscapeWith)
    {
        if (canDelegateLookupToContainingScope(baseName))
        {
            // If we know that we can't possibly find the property in this scope, just ask the containing scope
            // which may have already computed and cached the result.
            return getContainingScope().findProperty(project, baseName, dt, canEscapeWith);
        }
        assert baseName.indexOf('.') == -1 : "baseName must not be any sort of qname";
        CompilerProject compilerProject = (CompilerProject)project;
        ASScopeCache scopeCache = compilerProject.getCacheForScope(this);
        return filterWith(scopeCache.findProperty(baseName, dt, canEscapeWith), canEscapeWith);
    }

    /**
     * An alternate entry point for findprop operations.
     *
     * This method takes an addition Predicate that allows custom filtering of the results, instead
     * of just using the namespace set.  This method will still use the namespace set, but the predicate passed
     * in will be called first to filter the results.
     *
     * @param project       the active project
     * @param baseName      base name of the property we're looking for
     * @param additional    A Predicate that performs custom filtering on the results
     * @param dt            The dependency type that should be added to the dependency graph
     *                      when resolving this reference across a compilation boundary
     * @param canEscapeWith should this lookup find definitions that occur outside of a containing with scope
     * @return              A single {@link IDefinition} to which the specified base name resolves to in this
     *                      scope, given the additional constraints supplied by the additional Predicate.
     */
    public IDefinition findProperty(ICompilerProject project, String baseName, Predicate<IDefinition> additional, DependencyType dt, boolean canEscapeWith)
    {
        Set<INamespaceDefinition> nsSet = getNamespaceSetForName(project, baseName);
        return findProperty(project, baseName, additional, nsSet, dt, canEscapeWith);
    }

    public IDefinition findProperty (ICompilerProject project, String baseName, Predicate<IDefinition> additional, Set<INamespaceDefinition> nsSet, DependencyType dt, boolean canEscapeWith)
    {
        NamespaceSetPredicate nsPred = new NamespaceSetPredicate(project, nsSet);

        List<IDefinition> storage = new ArrayList<IDefinition>();
        Predicate<IDefinition> pred = Predicates.and(additional, nsPred);
        FilteredCollection<IDefinition> defs = new FilteredCollection<IDefinition>(pred, storage);
        findProperty(defs, (CompilerProject)project, baseName, nsPred, dt, false);
        IDefinition def = null;
        def = getSingleResult(project, storage);
        return filterWith(def, canEscapeWith);
    }

    /**
     *  Helper method to narrow a List of results down to one result for the methods
     *  that return only 1 result.
     *  @return null if there are no results,
     *          the first definition if there is 1 result
     *          one of the definitions if there are multiple results, and the ambiguities can be resolved
     *          an {@link AmbiguousDefinition} if there are multiple results and the ambiguities could
     *          not be resolved
     */
    static IDefinition getSingleResult (ICompilerProject project, List<IDefinition> defs)
    {
        IDefinition def;
        switch (defs.size())
        {
            case 0:
                // No definition found!
                def = null;
                break;
            case 1:
                // found single definition!
                def = defs.get(0);
                assert def.isInProject(project);
                break;
            default:
                IDefinition d = AmbiguousDefinition.resolveAmbiguities(project, defs, false);
                if (d != null)
                    def = d;
                else
                    def = AmbiguousDefinition.get();
        }
        return def;
    }

    /**
     * Is it ok to skip this scope, and just ask the containing scope to perform
     * the lookup. This is possible if this scope does not contribute to the set
     * of open namespaces, and if we have no definitions with the simple name we
     * are looking for.
     *
     * @return true if we can just ask the containing scope to perform the
     * lookup
     */
    protected boolean canDelegateLookupToContainingScope(String name)
    {
        return namespaceSetSameAsContainingScopeNamespaceSet()
                && getContainingScope() != null
                && getLocalDefinitionSetByName(name) == null;
    }

    /**
     * The main public entry point for the findprop operation in the compiler
     * with an explicit qualifier namespace. This method uses the
     * {@link ASScopeCache} to improve performance.
     * 
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param qual {@link INamespaceDefinition} which must match the qualifier
     * namespace of the found {@link IDefinition}.
     * @param baseName base name of the property we are looking for.
     * @param dt The type of dependency that should be added to the dependency
     * graph when resolving this reference across a compilation unit boundary.
     * @return A single {@link IDefinition} to which the specified qualifier and
     * base name resolves to in this scope, or null. Null is returned when no
     * definition is found <b>and</b> when more than one definition is found.
     */
    public IDefinition findPropertyQualified(ICompilerProject project, INamespaceDefinition qual, String baseName, DependencyType dt)
    {
        return findPropertyQualified(project, qual, baseName, dt, false);
    }

    /**
     * The main public entry point for the findprop operation in the compiler
     * with an explicit qualifier namespace. This method uses the
     * {@link ASScopeCache} to improve performance.
     *
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param qual {@link INamespaceDefinition} which must match the qualifier
     * namespace of the found {@link IDefinition}.
     * @param baseName base name of the property we are looking for.
     * @param dt The type of dependency that should be added to the dependency
     * graph when resolving this reference across a compilation unit boundary.
     * @return A single {@link IDefinition} to which the specified qualifier and
     * base name resolves to in this scope, or null. Null is returned when no
     * definition is found <b>and</b> when more than one definition is found.
     */
    public IDefinition findPropertyQualified(ICompilerProject project, Predicate<IDefinition> additional,
                                             INamespaceDefinition qual, String baseName, DependencyType dt)
    {
        if( qual == null )
            return null;

        NamespaceSetPredicate nsPred = new NamespaceSetPredicate(project, ImmutableSet.of(qual));
        Predicate<IDefinition> pred = Predicates.and(additional, nsPred);
        List<IDefinition> defs = new ArrayList<IDefinition>();
        FilteredCollection<IDefinition> filteredCollection = new FilteredCollection<IDefinition>(pred, defs);
        findProperty(filteredCollection, (CompilerProject)project, baseName, nsPred, dt, false);
        return getSingleResult(project, defs);
    }

    /**
     * The main public entry point for the findprop operation in the compiler
     * with an explicit qualifier namespace. This method uses the
     * {@link ASScopeCache} to improve performance.
     * 
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param qual The qualifier(s) to use to lookup the property.
     * @param baseName base name of the property we are looking for.
     * @param canEscapeWith should this lookup find definitions that occur
     * outside of a containing with scope
     * @param dt The type of dependency that should be added to the dependency
     * graph when resolving this reference across a compilation unit boundary.
     * @return A single {@link IDefinition} to which the specified qualifier and
     * base name resolves to in this scope, or null. Null is returned when no
     * definition is found <b>and</b> when more than one definition is found.
     */
    public IDefinition findPropertyQualified(ICompilerProject project, INamespaceDefinition qual, String baseName, DependencyType dt, boolean canEscapeWith)
    {
        assert baseName.indexOf('.') == -1 : "baseName must not be any sort of qname";

        // Can't find a property if we don't know what its qualifier is
        if( qual == null )
            return null;

        CompilerProject compilerProject = (CompilerProject)project;
        ASScopeCache scopeCache = compilerProject.getCacheForScope(this);

        IDefinition definition = scopeCache.findPropertyQualified(qual, baseName, dt);
        return filterWith(definition, canEscapeWith);
    }

    /**
     * The main public entry point for the findprop operation in the compiler
     * with an explicit set of qualifier namespaces. This method uses the
     * {@link ASScopeCache} to improve performance.
     *
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set this scope.
     * @param baseName base name of the property we are looking for.
     * @param qual The qualifier(s) to use to lookup the property.
     * @param canEscapeWith should this lookup find definitions that occur
     * outside of a containing with scope
     * @param dt The type of dependency that should be added to the dependency
     * graph when resolving this reference across a compilation unit boundary.
     * @return A single {@link IDefinition} to which the specified qualifier and
     * base name resolves to in this scope, or null. Null is returned when no
     * definition is found <b>and</b> when more than one definition is found.
     */
    public IDefinition findPropertyQualified(ICompilerProject project, IQualifiers qual, String baseName, DependencyType dt, boolean canEscapeWith)
    {
        if( qual == null || qual.getNamespaceCount() == 0 )
            return null ;

        if( qual.getNamespaceCount() == 1 )
        {
            return findPropertyQualified(project, qual.getFirst(), baseName, dt, canEscapeWith);
        }
        else
        {
            List<IDefinition> defs = findProperty((CompilerProject)project, baseName, qual.getNamespaceSet(), dt);
            return filterWith(getSingleResult(project, defs), canEscapeWith);
        }
    }

    /**
     * Helper method to get the namespace set to use for a super reference. This
     * will replace the protected namespace for this class with the protected
     * namespace for the super class in the returned namespace set.
     * 
     * @param project project used to resolve namespaces
     * @param superDef the IDefinition representing the base class
     * @return The correct namespace set to use for a super reference
     */
    public Set<INamespaceDefinition> getNamespaceSetForSuper(ICompilerProject project, IDefinition superDef)
    {
        Set<INamespaceDefinition> nsSet = getNamespaceSet(project);
        return adjustNamespaceSetForSuper(superDef, nsSet);
    }

    /**
     * Adjust the namespace set passed in so it's the right set for a super
     * access. This will replace the protected namespace for this class with the
     * protected namespace for the super class in the returned namespace set.
     * 
     * @param superDef the IDefinition representing the base class
     * @param nsSet the namespace set to adjust
     * @return The correct namespace set to use for a super reference
     */
    public Set<INamespaceDefinition> adjustNamespaceSetForSuper(IDefinition superDef, Set<INamespaceDefinition> nsSet)
    {
        ClassDefinitionBase containingClass = getContainingClass();

        if (superDef instanceof ClassDefinition &&
                nsSet.contains(containingClass.getProtectedNamespaceReference()))
        {
            Set<INamespaceDefinition> adjustedSet = new LinkedHashSet<INamespaceDefinition>();
            adjustedSet.addAll(nsSet);
            adjustedSet.remove(containingClass.getProtectedNamespaceReference());
            adjustedSet.add(((ClassDefinition)superDef).getProtectedNamespaceReference());
            return adjustedSet;
        }
        return nsSet;
    }

    /**
     * Helper method to return the ClassDefinition this scope is inside of, if
     * there is one.
     * 
     * @return the ClassDefinition that contains this scope, or null if this
     * scope is not in a ClassDefinition
     */
    public ClassDefinitionBase getContainingClass()
    {
        ASScope scope = this;
        ScopedDefinitionBase sdb = null;

        while (scope != null && sdb == null)
        {
            // Walk up the scope chain until we find the first scope with a definition.
            // stuff like catch, or with scopes will have no definition associated with them
            sdb = scope.getDefinition();
            scope = scope.getContainingScope();
        }

        if (sdb instanceof ClassDefinitionBase)
            return (ClassDefinitionBase)sdb;
        else if (sdb != null)
            return (ClassDefinitionBase)sdb.getAncestorOfType(ClassDefinitionBase.class);

        return null;

    }

    public ASFileScope getFileScope()
    {
        ASScope scope = this;
        while (!(scope instanceof ASFileScope))
        {
            scope = scope.getContainingScope();
            // instantiated Vectors may not have containing scopes
            if (scope == null)
                break;
        }
        return (ASFileScope)scope;
    }

    /**
     * Get's the {@link IWorkspace} in which this {@link ASScope} lives.
     * 
     * @return The {@link IWorkspace} in which this {@link ASScope} lives.
     */
    public IWorkspace getWorkspace()
    {
        return getFileScope().getWorkspace();
    }

    public String getContainingSourcePath(String qName, ICompilerProject project)
    {
        ASScope containingScope = getContainingScope();
        if (containingScope != null)
            return containingScope.getContainingSourcePath(qName, project);
        return null;
    }

    /**
     * Determine if any of the definitions in this scope are Bindable
     * 
     * @return true, if any non-static definitions in this scope are explicitly marked
     * bindable, false if there are none.
     */
    public boolean hasAnyBindableDefinitions()
    {
        for (IDefinitionSet set : getAllLocalDefinitionSets())
        {
            int n = set.getSize();
            for (int i = 0; i < n; i++)
            {
                IDefinition d = set.getDefinition(i);
                if (d.isBindable() && !d.isStatic())
                    return true;
            }
        }
        return false;
    }

    /**
     * Get's the {@link ScopedDefinitionBase} that contains this scope. This
     * method differs from {@link #getDefinition()} in that this method will
     * walk up the scope chain.
     * 
     * @return The {@link ScopedDefinitionBase} that contains this scope
     */
    public IScopedDefinition getContainingDefinition()
    {
        // sub-classes override this method.
        // This class just returns the definition attached this scope.
        return containingDefinition;
    }

    /**
     * Makes this scope be the containing scope of the specified anonymous
     * function. Anonymous functions do not get added to scopes, but they do
     * need to know which scope they are inside of.
     */
    public void setAsContainingScopeOfAnonymousFunction(FunctionDefinition anonymousFunction)
    {
        anonymousFunction.setContainingScope(this);
    }

    /**
     * Add a dependency to the given builtintype, from the compilation unit which contains this scope
     * @param project           the active project
     * @param builtinType       the builtin type to depend on
     * @param dependencyType    the type of dependency to add
     */
    public void addDependencyOnBuiltinType(ICompilerProject project, IASLanguageConstants.BuiltinType builtinType,
                                            DependencyType dependencyType)
    {
        // Just proxy up to the file scope, since dependencies are from CompilationUnit to CompilationUnit
        if( containingScope != null )
            containingScope.addDependencyOnBuiltinType(project, builtinType, dependencyType);
    }

    /**
     * Implementation of addDependencyOnBuiltinType that will actually add the dependency.
     * This will only be called if there is a cache miss.
     * @param project           the active project
     * @param builtinType       the builtin type to depend on
     * @param dependencyType    type of dependency to add
     */
    void addDependencyOnBuiltinTypeImpl(CompilerProject project, IASLanguageConstants.BuiltinType builtinType,
                                           DependencyType dependencyType)
    {
        IDefinition definition = project.getBuiltinType(builtinType);

        if( definition != null && 
                builtinType != IASLanguageConstants.BuiltinType.ANY_TYPE &&
                builtinType != IASLanguageConstants.BuiltinType.VOID)
        {
            ASProjectScope projectScope = project.getScope();

            ICompilationUnit from = projectScope.getCompilationUnitForScope(this);
            ICompilationUnit to = projectScope.getCompilationUnitForDefinition(definition);

            String qname = definition.getQualifiedName();
            project.addDependency(from, to, dependencyType, qname);
        }
    }
}
