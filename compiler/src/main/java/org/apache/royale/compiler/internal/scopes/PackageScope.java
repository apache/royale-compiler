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

import java.util.Set;

import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.caches.PackageNamespaceDefinitionCache;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.ScopedDefinitionBase;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 * An {@link ASScope} subclass for package scope's.
 */
public final class PackageScope extends ASScope
{

    /**
     * Constructor
     * 
     * @param containingScope The {@link ASScope} that contains the new package
     * scope. Usually an {@link ASFileScope}.
     * @param packageName Name of the package that the new package scope is for.
     */
    public PackageScope(ASScope containingScope, String packageName)
    {
        this(containingScope, packageName, null);
    }

    /**
     * Constructor
     * 
     * @param containingScope The {@link ASScope} that contains the new package
     * scope. Usually an {@link ASFileScope}.
     * @param packageName Name of the package that the new package scope is for.
     * @param contentsNode The {@link ScopedBlockNode} that will contain all the
     * {@link IASNode}'s for the contents of the package.
     */
    public PackageScope(ASScope containingScope, String packageName, ScopedBlockNode contentsNode)
    {
        super(containingScope, contentsNode);
        Workspace workspace = (Workspace)containingScope.getWorkspace();
        PackageNamespaceDefinitionCache packageNSCache = workspace.getPackageNamespaceDefinitionCache();
        internalNamespaceReference = packageNSCache.get(packageName, true);
        publicNamespaceReference = packageNSCache.get(packageName, false);

        if( containingScope instanceof ASFileScope )
        {
            // Add the implicit imports, so they still work inside packages,
            // since we won't consult the containing file scope for actionscript files
            for( String s : ASFileScope.getImplicitImportsForAS() )
            {
                addImport(s);
            }
        }
    }

    private final NamespaceDefinition.ILanguageNamespaceDefinition internalNamespaceReference;
    private final NamespaceDefinition.ILanguageNamespaceDefinition publicNamespaceReference;

    /**
     * Gets the {@link INamespaceReference} that resolves to the internal
     * namespace for this {@link IPackageDefinition}.
     * 
     * @return The {@link INamespaceReference} that resolves to the internal
     * namespace for this {@link IPackageDefinition}.
     */
    public NamespaceDefinition.ILanguageNamespaceDefinition getInternalNamespace()
    {
        return internalNamespaceReference;
    }

    /**
     * Gets the {@link INamespaceReference} that resolves to the public
     * namespace for this {@link PackageScope}.
     * 
     * @return The {@link INamespaceReference} that resolves to the public
     * namespace for this {@link PackageScope}.
     */
    public NamespaceDefinition.ILanguageNamespaceDefinition getPublicNamespace()
    {
        return publicNamespaceReference;
    }

    @Override
    public void addImplicitOpenNamespaces(CompilerProject compilerProject, Set<INamespaceDefinition> result)
    {
        result.add(getPublicNamespace());
        result.add(getInternalNamespace());
    }

    @Override
    public ScopedDefinitionBase getContainingDefinition()
    {
        return null;
    }

    /**
     * Add the open namespaces from the containing scope to the namespace set passed in
     * Package scopes will only add the implicit namespaces from the file scope if this is a .as file
     * @param compilerProject   the active project
     * @param result            the Namespace set to add the namespaces to
     */
    protected void addNamespacesFromContainingScope(CompilerProject compilerProject, Set<INamespaceDefinition> result)
    {
        ASScope containingScope = this.getContainingScope();
        if (containingScope != null)
        {
            // ASFileScopes only contribute the implicit open namespaces to any packages in them
            // but MXMLFileScopes contribute all thier open namespaces
            if( !(containingScope instanceof MXMLFileScope) )
            {
                containingScope.addImplicitOpenNamespaces(compilerProject, result);
            }
            else
            {
                result.addAll(containingScope.getNamespaceSet(compilerProject));
            }
        }
    }

    /**
     * Get the additional namespaces for a reference, if the name has been explicitly imported in
     * a containing scope.
     * For a package scope in an .as file, this will do nothing, as the imports in the containing file
     * scope won't affect references in the package.
     * @param project   the active project
     * @param name      the name of the reference
     * @param nsSet     the namespace set to add the namespaces to
     */
    protected void getContainingScopeExplicitImports (CompilerProject project, String name, Set<INamespaceDefinition> nsSet)
    {
        ASScope containingScope = getContainingScope();
        if (containingScope instanceof MXMLFileScope)
        {
            // ASFileScope imports don't affect the names in a package
            // but MXMLFileScopes do
            nsSet.addAll(containingScope.getExplicitImportQualifiers(project, name));
        }
    }

}
