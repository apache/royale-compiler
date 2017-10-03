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

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IScopedDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.ScopedDefinitionBase;
import org.apache.royale.compiler.projects.ICompilerProject;

import java.util.Set;

/**
 * Base class for catch and with scopes. Delegates most methods to the
 * containing scope.
 */
public class NoDefinitionScope extends ASScope
{
    public NoDefinitionScope(ASScope containingScope)
    {
        super(containingScope);
    }

    /**
     * add the namespace directive to the containing scope
     */
    @Override
    public void addNamespaceDirective(NamespaceDefinition.INamespaceDirective directive)
    {
        getContainingScope().addNamespaceDirective(directive);
    }

    /**
     * Add the import to the containing scope
     */
    @Override
    public void addImport(String target)
    {
        getContainingScope().addImport(target);
    }

    /**
     * Add the use directive to the containing scope
     */
    @Override
    public void addUseDirective(NamespaceDefinition.IUseNamespaceDirective useDirective)
    {
        getContainingScope().addUseDirective(useDirective);
    }

    /**
     * Get the imports from the containing scope
     */
    @Override
    public String[] getImports()
    {
        return getContainingScope().getImports();
    }

    /**
     * Always returns null, these scopes don't have associated defns
     * 
     */
    @Override
    public ScopedDefinitionBase getDefinition()
    {
        return null;
    }

    @Override
    protected boolean namespaceSetSameAsContainingScopeNamespaceSet()
    {
        return true;
    }

    /**
     * Get the namespace set of the containing scope
     */
    @Override
    public Set<INamespaceDefinition> getNamespaceSet(ICompilerProject project)
    {
        return getContainingScope().getNamespaceSet(project);
    }

    /**
     * Override addDefinition so that all defns get hoisted to the containing
     * scope Catch scopes will provide an alternate way to add the definition
     * for the catch parameter
     * 
     * @param d the definition to add
     */
    @Override
    public void addDefinition(IDefinition d)
    {
        getContainingScope().addDefinition(d);
    }

    /**
     * Add a definition to this scope, instead of hoisting the defn to the
     * containing scope. used by CatchScopes to add their parameter
     * 
     * @param d the definition to add
     */
    protected void addDefinitionToThisScope(IDefinition d)
    {
        super.addDefinition(d);
    }

    @Override
    public IScopedDefinition getContainingDefinition()
    {
        return getContainingScope().getContainingDefinition();
    }
}
