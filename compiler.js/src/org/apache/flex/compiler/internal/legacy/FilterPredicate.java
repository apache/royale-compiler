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

import java.util.Set;

import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.IVariableDefinition;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.scopes.ASScopeBase;
import org.apache.flex.compiler.projects.ICompilerProject;

import com.google.common.base.Predicate;

/**
 * Predicate implementation that applies any filter constraints that are not
 * handled by the namespace set.
 */
public class FilterPredicate implements Predicate<IDefinition>
{
    /**
     * @param scope ASScope that is used to compute the namespace set, if the
     * filter forced the use of the {@link ASScopeBase#allNamespacesSet}. This
     * will be used when the require imports value is set to
     * ONLY_FOR_FUNCTIONS_AND_VARIABLES. The parameter can be null for scope
     * that can not have imports.
     * @param filter {@link com.adobe.flexbuilder.codemodel.definitions.filters.ASDefinitionFilter}
     * to apply
     */
    public FilterPredicate(ICompilerProject project,
                           ASScope scope,
                           ASDefinitionFilter filter)
    {
        requireImportsForFuncsAndVars = filter.getRequireImportsValue() == ASDefinitionFilter.RequireImportsValue.ONLY_FOR_FUNCTIONS_AND_VARIABLES;
        this.project = project;
        this.scope = scope;
        this.filter = filter;
    }

    private final boolean requireImportsForFuncsAndVars;
    private final ICompilerProject project;
    private final ASScope scope;
    private final ASDefinitionFilter filter;
    private Set<INamespaceDefinition> nsset;

    @Override
    public boolean apply(IDefinition def)
    {
        // incomplete code can create definitions with no name
        // for example, private var ; will create a variable definition with no name
        // CM clients (code-hinting) don't require these definitions
        // So, filter them out here
        if (def.getBaseName().isEmpty())
            return false;

        // CM clients never want to see package definitions that
        // inside of source file.  They only want to see package definitions
        // from the package name index
        if ((def instanceof IPackageDefinition) && (def.getContainingScope() != null))
            return false;
        if (filter.matchesClassificationRule(def) &&
                filter.matchesModifierRules(def) &&
                filter.matchesIncludeImplicitsAndConstructorsRule(def, scope))
        {
            // Do the filtering for funcs and vars, if neccessary
            if (requireImportsForFuncsAndVars && (def instanceof IFunctionDefinition || def instanceof IVariableDefinition))
            {
                if (nsset == null)
                    nsset = filter.getNamespaceSet(project, scope);
                if (nsset.contains(def.resolveNamespace(project)))
                    return true;
            }
            else
            {
                return true;
            }
        }
        return false;
    }
}
