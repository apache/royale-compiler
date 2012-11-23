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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.Multiname;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter.RequireImportsValue;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.scopes.ASScopeBase;
import org.apache.flex.compiler.internal.scopes.ASScopeBase.FilteredCollection;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.scopes.IASScope;

import com.google.common.base.Predicate;

/**
 * This class contains static methods that used to be instance methods of an
 * {@link IASScope}. They now take a new first parameter which is the old
 * <code>this</code> scope. The methods are no longer part of
 * <code>IASScope</code> because {@link ASDefinitionFilter} has been removed
 * from the compiler.
 */
public class ASScopeUtils
{
    /**
     * Finds all definitions in the specified scope (and possibly also its
     * enclosing and inherited scopes, depending on the filter) that meet the
     * filter criteria.
     * 
     * @param thisScope The {@link IASScope} in which the search starts.
     * @param project The {@link ICompilerProject} to use for resolving
     * references.
     * @param filter The {@Link ASDefinitionFilter} that determines the
     * scope chain and the criteria that the definitions must meet.
     * @param definitions The list to which the {@link IDefinition} objects are
     * added.
     */
    public static void findAllDefinitions(IASScope thisScope, ICompilerProject project,
                                          ASDefinitionFilter filter,
                                          List<IDefinition> definitions)
    {
        CompilerProject compilerProject = (CompilerProject)project;

        // Project scopes are a special case.
        if (thisScope instanceof ASProjectScope)
        {
            Set<INamespaceDefinition> namespaceSet = crackFilter(compilerProject, null, filter);

            Predicate<IDefinition> filterPredicate = filter.computePredicate(compilerProject, null);
            Collection<IDefinition> foundDefinitions = new FilteredCollection<IDefinition>(filterPredicate, definitions);

            ((ASProjectScope)thisScope).getAllProperties(compilerProject, foundDefinitions, namespaceSet);
            return;
        }

        // All other scopes use this logic.
        else
        {
            ASScope scope = (ASScope)thisScope;

            Set<INamespaceDefinition> namespaceSet = crackFilter(project, scope, filter);

            Predicate<IDefinition> filterPredicate = filter.computePredicate(project, scope);
            Collection<IDefinition> foundDefinitions = new FilteredCollection<IDefinition>(filterPredicate, definitions);

            if (filter.searchInheritedScopes() && filter.searchContainingScope())
            {
                // The filter wants to look at inherited scopes and containing scopes.
                // This must be a findprop variant of find all definitions.
                ASScope currentScope = scope;
                while (currentScope != null)
                {
                    currentScope.getAllPropertiesForScopeChain(compilerProject, foundDefinitions, namespaceSet);
                    currentScope = currentScope.getContainingScope();
                }

                if (filter.needsDifferentProjectPredicate())
                    foundDefinitions = adjustPredicateForProject(scope, project, filter, definitions);

                // Check the project scope.
                ASProjectScope projectScope = compilerProject.getScope();
                projectScope.getAllProperties(compilerProject, foundDefinitions, namespaceSet);
            }
            else if (filter.searchInheritedScopes())
            {
                // The filter wants to look at inherited scopes but not at containing scopes.
                if (!(filter.requiresModifier(ASModifier.STATIC) || filter.excludesModifier(ASModifier.STATIC)))
                {
                    // Find both static and instance properties.
                    // This is what CodeModel does to try and hint some stuff inside a class.
                    // It's basically a lexical lookup that stops at the file scope,
                    // so we use  the scope chain version of lookup.
                    scope.getAllPropertiesForScopeChain(compilerProject, foundDefinitions, namespaceSet);
                }
                else
                {
                    // This must be a getprop variant of find all definitions.
                    scope.getAllPropertiesForMemberAccess(compilerProject, foundDefinitions, namespaceSet);
                }
            }
            else if (filter.searchContainingScope())
            {
                // The filter wants to look at lexical scopes, but not at the inherited scopes.
                // This type of lookup has no meaning in AS3 but CodeModel uses it.
                boolean requireStatic = filter.requiresModifier(ASModifier.STATIC);
                boolean excludeStatic = filter.excludesModifier(ASModifier.STATIC);
                if (!requireStatic || !excludeStatic)
                {
                    ASScope currentScope = scope;
                    while (currentScope != null)
                    {
                        currentScope.getAllLocalProperties(compilerProject, foundDefinitions, namespaceSet, null);
                        currentScope = currentScope.getContainingScope();
                    }
                }

                if (filter.needsDifferentProjectPredicate())
                    foundDefinitions = adjustPredicateForProject(scope, project, filter, definitions);

                // Check the project scope.
                ASProjectScope projectScope = compilerProject.getScope();
                projectScope.getAllProperties(compilerProject, foundDefinitions, namespaceSet);
            }
            else
            {
                // The filter wants to look only at the specified scope
                // and not any containing or inherited scopes.
                scope.getAllLocalProperties(compilerProject, foundDefinitions, namespaceSet, null);
            }
        }
    }

    /**
     * Finds the definitions in the specified scope (and possibly also its
     * enclosing and inherited scopes, depending on the filter) that have the
     * specified base name and that meet the filter criteria.
     * 
     * @param thisScope The {@link IASScope} in which the search starts.
     * @param project The {@link ICompilerProject} to use for resolving
     * references.
     * @param name The base name of the definitions to be found.
     * @param filter The {@Link ASDefinitionFilter} that determines the
     * scope chain and the criteria that the definitions must meet.
     * @param definitions The list to which the {@link IDefinition} objects are
     * added.
     */
    public static void findAllDefinitionsByName(IASScope thisScope, ICompilerProject project,
                                                String name, ASDefinitionFilter filter,
                                                List<IDefinition> definitions)
    {
        findDefinitionsByNameImpl(thisScope, project, name, filter, definitions, true);
    }

    /**
     * Finds the first definition in the specified scope (and possibly also its
     * enclosing and inherited scopes, depending on the filter) that has the
     * specified base name and that meets the filter criteria.
     * 
     * @param thisScope The {@link IASScope} in which the search starts.
     * @param project The {@link ICompilerProject} to use for resolving
     * references.
     * @param name The base name of the definition to be found.
     * @param filter The {@Link ASDefinitionFilter} that determines the
     * scope chain and the criteria that the definition must meet.
     * @return definitions The {@link IDefinition}, if one was found, or
     * <code>null</code>.
     */
    public static IDefinition findDefinitionByName(IASScope thisScope, ICompilerProject project,
                                                   String name, ASDefinitionFilter filter)
    {
        List<IDefinition> definitions = new ArrayList<IDefinition>(1);
        findDefinitionsByNameImpl(thisScope, project, name, filter, definitions, false);
        return definitions.size() > 0 ? definitions.get(0) : null;
    }

    private static void findDefinitionsByNameImpl(IASScope thisScope, ICompilerProject project,
                                                  String name, ASDefinitionFilter filter,
                                                  List<IDefinition> definitions, boolean findAll)
    {
        CompilerProject compilerProject = (CompilerProject)project;

        if (thisScope instanceof ASProjectScope)
        {
            Multiname multiName = crackNameAndFilter(project, name, filter);
            String baseName = multiName.getBaseName();
            Set<INamespaceDefinition> namespaceSet = multiName.getNamespaceSet();

            Predicate<IDefinition> filterPredicate = filter.computePredicate(project, null);
            Collection<IDefinition> foundDefinitions = new FilteredCollection<IDefinition>(filterPredicate, definitions);

            ((ASProjectScope)thisScope).getLocalProperty(project, foundDefinitions, baseName, namespaceSet);
        }
        else
        {
            ASScope scope = (ASScope)thisScope;

            Multiname multiName = crackNameAndFilter(project, name, filter, scope);
            String baseName = multiName.getBaseName();
            Set<INamespaceDefinition> namespaceSet = multiName.getNamespaceSet();

            Predicate<IDefinition> predicate = filter.computePredicate(project, scope);
            Collection<IDefinition> foundDefinitions = new FilteredCollection<IDefinition>(predicate, definitions);

            if (filter.searchInheritedScopes() && filter.searchContainingScope())
            {
                // The filter wants to look at inherited scopes and containing scopes,
                // so this must be a findprop.
                // TODO If we adjust the ASScope cache to cache ambiguous definition
                // sets then we can call through to the ASScope cache here, if findAll is false.
                scope.findProperty(foundDefinitions, compilerProject, baseName, namespaceSet, null, findAll);
            }
            else if (filter.searchInheritedScopes())
            {
                // The filter wants to look at inherited scopes but not at containing scopes,
                // so this must be a getprop.
                scope.getPropertyForMemberAccess(compilerProject, foundDefinitions, baseName, namespaceSet, findAll);
            }
            else if (filter.searchContainingScope())
            {
                // The filter wants to look at containing scopes but not at inherited scopes.
                // This type of lookup has no meaning in AS3.
                ASScope currentScope = scope;
                while ((currentScope != null) && ((foundDefinitions.size() == 0) || findAll))
                {
                    currentScope.getLocalProperty(project, foundDefinitions, baseName, namespaceSet);
                    currentScope = currentScope.getContainingScope();
                }

                if ((foundDefinitions.size() == 0) || findAll)
                {
                    // Check project scope if we still don't have a definition.
                    // The project scope can not introduce ambiguities to the
                    // file scopes's definitions for the purpose of this method.
                    ASProjectScope projectScope = compilerProject.getScope();
                    projectScope.getLocalProperty(project, foundDefinitions, baseName, namespaceSet);
                }
            }
            else
            {
                // The filter wants to look only at the specified scope
                // and not any containing or inherited scopes.
                scope.getLocalProperty(project, foundDefinitions, baseName, namespaceSet);
            }
        }
    }

    /**
     * Constructs a {@link Multiname} by parsing the specified name string and
     * extracting information from the specified {@link ASDefinitionFilter}
     * 
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set in the
     * {@link ASDefnitionFilter}.
     * @param name Either a simple definition name or a dotted qname.
     * @param filter
     * @return A new {@link Multiname} created from information in the specified
     * name and {@link ASDefinitionFilter}.
     */
    private static Multiname crackNameAndFilter(ICompilerProject project, String name, ASDefinitionFilter filter)
    {
        return crackNameAndFilter(project, name, filter, null);
    }

    /**
     * Constructs a {@link Multiname} by parsing the specified name string and
     * extracting information from the specified {@link ASDefinitionFilter}
     * 
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set in the
     * {@link ASDefnitionFilter}.
     * @param name Either a simple definition name or a dotted qname.
     * @param filter
     * @param scope the scope we are doing the lookup in - this is used to
     * determine if any interface namespaces need to be added to the namespace
     * set
     * @return A new {@link Multiname} created from information in the specified
     * name and {@link ASDefinitionFilter}.
     */
    private static Multiname crackNameAndFilter(ICompilerProject project, String name,
                                                ASDefinitionFilter filter, ASScope scope)
    {
        Workspace workspace = (Workspace)project.getWorkspace();
        if (name != null)
        {
            final int lastIndexOfDot = name != null ? name.lastIndexOf('.') : -1;
            if (lastIndexOfDot != -1)
            {
                Set<INamespaceDefinition> namespaceSet = null;

                final String definitionName = name.substring(lastIndexOfDot + 1);

                String packageName = name.substring(0, lastIndexOfDot);
                INamespaceDefinition publicPackageNS =
                        workspace.getPackageNamespaceDefinitionCache().get(packageName, false);
                namespaceSet = new HashSet<INamespaceDefinition>(1);
                ASDefinitionFilter.AccessValue primaryAccessRule =
                        filter.getPrimaryAccessRule();
                if ((primaryAccessRule == ASDefinitionFilter.AccessValue.ALL) ||
                    (primaryAccessRule == ASDefinitionFilter.AccessValue.INTERNAL))
                {
                    INamespaceDefinition internalPackageNS =
                            workspace.getPackageNamespaceDefinitionCache().get(packageName, true);
                    namespaceSet.add(internalPackageNS);
                }
                namespaceSet.add(publicPackageNS);

                return new Multiname(namespaceSet, definitionName);
            }
        }

        // We will filter the funcs and vars in ASScopeBase.applyFilter
        if (filter.getRequireImportsValue() != RequireImportsValue.YES)
            return new Multiname(ASScopeBase.allNamespacesSet, name);
        else
            return new Multiname(filter.getNamespaceSetForName(project, scope, name), name);
    }

    /**
     * Constructs a set of {@link INamespaceDefinition} by extracting
     * information from the specified {@link ASDefinitionFilter}.
     * 
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve namespace references in the "use namespace" set in the
     * {@link ASDefnitionFilter}.
     * @param filter
     * @return A new set of {@link INamespaceDefinition} created from
     * information in the {@link ASDefinitionFilter}.
     */
    private static Set<INamespaceDefinition> crackFilter(ICompilerProject project, ASScope scope,
                                                         ASDefinitionFilter filter)
    {
        // Check for user defined namespace access value.
        ASDefinitionFilter.AccessValue accessValue = filter.getPrimaryAccessRule();
        if (accessValue != null)
        {
            // If there is a namespace definition on the access value, we're filtering
            // on this one specific namespace in the set.
            // No need to do instanceof SpecialAccessValue, as getNamespaceDef() is on the
            // base class, and will just return null when not of type SpecialAccessValue.
            INamespaceDefinition namespaceAccessValue = accessValue.getNamespaceDef();
            if (namespaceAccessValue != null)
                return Collections.singleton(namespaceAccessValue);
        }

        Set<INamespaceDefinition> namespaceSet = ASScopeBase.allNamespacesSet;
        if (filter.getRequireImportsValue() == ASDefinitionFilter.RequireImportsValue.YES)
            namespaceSet = filter.getNamespaceSet(project, scope);

        return namespaceSet;
    }

    private static Collection<IDefinition> adjustPredicateForProject(ASScope scope, ICompilerProject project,
                                                                     ASDefinitionFilter filter,
                                                                     List<IDefinition> definitions)
    {
        Predicate<IDefinition> projectPredicate = filter.computeProjectPredicate(project, scope);
        return new FilteredCollection<IDefinition>(projectPredicate, definitions);
    }
}
