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

import java.util.HashSet;
import java.util.Set;

import org.apache.flex.compiler.common.ASImportTarget;
import org.apache.flex.compiler.common.IImportTarget;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.scopes.ASFileScope;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.projects.ICompilerProject;

public abstract class ScopeFilterContext implements IFilterContext
{
    protected ScopeFilterContext(ASScope scope)
    {
        this.scope = scope;
    }

    private final ASScope scope;

    public static Set<INamespaceDefinition> getNamespaceSet(ICompilerProject project, ASScope scope)
    {
        return getNamespaceSet(project, scope, null);
    }

    public static Set<INamespaceDefinition> getNamespaceSet(ICompilerProject project, ASScope scope, String name)
    {
        if (scope == null)
        {
            Set<INamespaceDefinition> nsSet = new HashSet<INamespaceDefinition>();

            ((CompilerProject)project).addGlobalUsedNamespacesToNamespaceSet(nsSet);

            for (String importStr : ASFileScope.getImplicitImportsForAS())
            {
                Workspace workspace = (Workspace)project.getWorkspace();
                IImportTarget importTarget = ASImportTarget.get(workspace, importStr);
                INamespaceDefinition packagePublicNamespace = importTarget.getNamespace();
                nsSet.add(packagePublicNamespace);
            }

            return nsSet;
        }

        return name != null ? scope.getNamespaceSetForName(project, name) : scope.getNamespaceSet(project);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<INamespaceDefinition> getNamespaceSet(ICompilerProject project)
    {
        return getNamespaceSet(project, scope);
    }

    @Override
    public Set<INamespaceDefinition> getNamespaceSet(ICompilerProject project, String name)
    {
        return getNamespaceSet(project, scope, name);
    }

    protected ASScope getScope()
    {
        return scope;
    }
}
