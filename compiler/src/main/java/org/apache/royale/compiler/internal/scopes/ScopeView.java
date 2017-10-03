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

import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.ScopedDefinitionBase;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.scopes.TypeScope.ScopeKind;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.workspaces.IWorkspace;

import java.util.Collection;
import java.util.Set;

/**
 * Base class for instance and class scopes. This class wraps a TypeScope, to
 * make the TypeScope appear as an instance scope (itraits) or static scope
 * (ctraits). This is a common baseclass for the instance and static scopes -
 * those class must simply implement the getScopeKind() method to get the
 * correct behavior.
 */
public abstract class ScopeView extends ASScope
{
    ScopeView(ASScope containingScope, TypeScope typeScope)
    {
        super(containingScope);
        this.typeScope = typeScope;
    }

    // The TypeScope we're wrapping.
    TypeScope typeScope;

    /**
     * Derived classes must implement this method to report what kind of scope
     * they are (instance or static).
     * 
     * @return The kind of Scope this class represents
     */
    protected abstract TypeScope.ScopeKind getScopeKind();

    // Methods below are overrides of ASScope/ASScopeBase methods that will simply call the appropriate method in TypeScope
    // which will do the right thing based on the scope kind.

    @Override
    protected void getPropertyForScopeChain(CompilerProject project, Collection<IDefinition> defs,
                                            String baseName, NamespaceSetPredicate namespaceSet,
                                            boolean findAll)
    {
        typeScope.getPropertyForScopeChain(project, defs, baseName, namespaceSet, findAll, getScopeKind());
    }

    @Override
    protected void getPropertyForMemberAccess(CompilerProject project, Collection<IDefinition> defs,
                                           String baseName, NamespaceSetPredicate namespaceSet,
                                           boolean findAll)
    {

        typeScope.getPropertyForMemberAccess(project, defs, baseName, namespaceSet, findAll, getScopeKind());
    }

    @Override
    public void getAllPropertiesForScopeChain(CompilerProject project, Collection<IDefinition> defs,
                                              Set<INamespaceDefinition> namespaceSet)
    {
        typeScope.getAllPropertiesForScopeChain(project, defs, namespaceSet, getScopeKind());
    }

    @Override
    public void getAllPropertiesForMemberAccess(CompilerProject project, Collection<IDefinition> defs,
                                                Set<INamespaceDefinition> namespaceSet)
    {
        typeScope.getAllPropertiesForMemberAccess(project, defs, namespaceSet, getScopeKind());
    }

    @Override
    public final void getAllLocalProperties(CompilerProject project, Collection<IDefinition> defs,
                                            Set<INamespaceDefinition> namespaceSet,
                                            INamespaceDefinition extraNamespace)
    {
        typeScope.getAllLocalProperties(project, defs, namespaceSet, extraNamespace);
    }

    @Override
    protected boolean namespaceSetSameAsContainingScopeNamespaceSet()
    {
        return false;
    }

    @Override
    public void addImplicitOpenNamespaces(CompilerProject compilerProject, Set<INamespaceDefinition> result)
    {
        typeScope.addImplicitOpenNamespaces(compilerProject, result, getScopeKind());
    }

    @Override
    public void addDefinition(IDefinition d)
    {
        //  look for problems where the "staticness" of our scope doesn't match
        // our definition. This would indicate a bug in the scope building code
        // Note: namespaces inside classes are always static
        assert isStaticScope() == (d.hasModifier(ASModifier.STATIC)
                || d instanceof NamespaceDefinition
                // Variables declared in control flow will result in non-statics being placed in the
                // static scope.
                // We have a diagnostic for this, which will be issued later, so don't assert.
                || (d instanceof VariableDefinition && ((VariableDefinition) d).declaredInControlFlow()) );
  
        typeScope.addDefinition(d);
    }

    @Override
    public ScopedDefinitionBase getDefinition()
    {
        return typeScope.getDefinition();
    }

    @Override
    public IDefinitionSet getLocalDefinitionSetByName(String name)
    {
        IDefinitionSet d = typeScope.getLocalDefinitionSetByName(name);
        if (d != null)
        {
            // Return a definition set that will filter the real definition set based
            // on static modifiers.
            d = new FilteredDefinitionSet(d, getScopeKind());
            if (d.isEmpty())
                d = null; // If we filtered out everything, just return null
        }

        return d;
    }

    @Override
    public String getContainingSourcePath(String baseName, ICompilerProject project)
    {
        return typeScope.getContainingSourcePath(baseName, project);
    }

    @Override
    public void addImport(String target)
    {
        typeScope.addImport(target);
    }

    @Override
    public void addLocalImportsToNamespaceSet(IWorkspace workspace, Set<INamespaceDefinition> namespaceSet)
    {
        if (getScopeKind() == ScopeKind.STATIC)
        {
            // Only add imports for the static scopes - just avoids doing this work twice
            // since the instance scope will always be inside the static scope
            typeScope.addLocalImportsToNamespaceSet(workspace, namespaceSet);
        }
    }

    @Override
    public Set<INamespaceDefinition> getExplicitImportQualifiers(CompilerProject project, String name)
    {
        if (getScopeKind() == ScopeKind.STATIC)
        {
            // Only add imports for the static scopes - just avoids doing this work twice
            // since the instance scope will always be inside the static scope
            return typeScope.getExplicitImportQualifiers(project, name);
        }
        else
        {
            return super.getExplicitImportQualifiers(project, name);
        }
    }

    @Override
    protected INamespaceReference[] getUsedNamespaces()
    {
        // Only get used namespaces from the static scope - instance scope is always 
        // inside the static scope, so it will still see the open namespaces
        if (getScopeKind() == ScopeKind.STATIC)
        {
            return typeScope.getUsedNamespaces();
        }
        return EMPTY_USE_ARRAY;
    }

    @Override
    public void addUseDirective(NamespaceDefinition.IUseNamespaceDirective useDirective)
    {
        typeScope.addUseDirective(useDirective);
    }

    public void addNamespaceDirective(NamespaceDefinition.INamespaceDirective directive)
    {
        typeScope.addNamespaceDirective(directive);
    }
    /**
     * Gets the first namespace definition or use namespace directive in the
     * scope.
     * 
     * @return The first namespace definition or use namespace directive in the
     * scope.
     */
    @Override
    public NamespaceDefinition.INamespaceDirective getFirstNamespaceDirective()
    {
        // namespaces only avail from the static scope 
        if (getScopeKind() == ScopeKind.STATIC)
            return typeScope.getFirstNamespaceDirective();
        return null;
    }

    @Override
    public final ScopedDefinitionBase getContainingDefinition()
    {
        return typeScope.getDefinition();
    }

    @Override
    public final boolean isPackageName(String p)
    {
        return typeScope.isPackageName(p);
    }

    /**
     * @return true if this scope represents the instance scope of a class
     */
    public boolean isInstanceScope()
    {
        return getScopeKind() == ScopeKind.INSTANCE;
    }

    /**
     * @return true if this scope represents the static scope of a class
     */
    public boolean isStaticScope()
    {
        return getScopeKind() == ScopeKind.STATIC;
    }

    /**
     * Get the scope of the super class - this method will do the right thing depending on
     * if this scope is the instance of static scope.
     * @param project  the Project to resolve things in
     * @return         the scope to use as the scope of the super class.  This may be the instance
     *                 scope of 'Class' (for static scopes), or the instance scope of the base class (for instance scopes)
     */
    public ASScope resolveSuperScope(ICompilerProject project)
    {
        return typeScope.resolveSuperScope(project, getScopeKind());
    }

    /**
     * Implementation of {@link IDefinitionSet} which will filter another definition set
     * based on the static-ness of the definitions in the backing set.
     */
    private static class FilteredDefinitionSet implements IDefinitionSet
    {
        FilteredDefinitionSet(IDefinitionSet definitionSet, ScopeKind scopeKind)
        {
            this.definitionSet = definitionSet;
            this.scopeKind = scopeKind;

            int count = 0;
            int n = definitionSet.getSize();
            for (int i = 0; i < n; i++)
            {
                IDefinition definition = definitionSet.getDefinition(i);
                if (scopeKind.findDefinition(definition))
                    count++;
            }
            filteredSize = count;
        }

        IDefinitionSet definitionSet;
        ScopeKind scopeKind;
        int filteredSize;

        @Override
        public boolean isEmpty()
        {
            return filteredSize == 0;
        }

        @Override
        public int getSize()
        {
            return filteredSize;
        }
        
        @Override
        public int getMaxSize()
        {
            return definitionSet.getMaxSize();
        }

        @Override
        public IDefinition getDefinition(int i)
        {
            if (i < 0 || i >= filteredSize)
                return null;
            
            // As we loop through the definitions in the underlying set,
            // count how many match the scope kind.
            int count = 0;
            
            int m = definitionSet.getSize();
            for (int j = 0; j < m; j++)
            {
                IDefinition definition = definitionSet.getDefinition(j);
                if (scopeKind.findDefinition(definition))
                {
                    if (count++ == i)
                        return definition;
                }
            }
            
            return null;
        }
    }
}
