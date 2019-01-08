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

package org.apache.royale.compiler.internal.definitions.references;

import static org.apache.royale.abc.ABCConstants.CONSTANT_Multiname;
import static org.apache.royale.abc.ABCConstants.CONSTANT_Qname;

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.references.IReferenceMName;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.internal.definitions.AmbiguousDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.units.ICompilationUnit;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Implementation of {@link IReference} representing a reference where all the
 * qualifiers have been resolved to namespace definitions.
 */
public class ResolvedQualifiersReference implements IResolvedQualifiersReference, IReferenceMName
{
    /**
     * Constructor.
     */
    public ResolvedQualifiersReference(ImmutableSet<INamespaceDefinition> qualifiers, String baseName)
    {
        assert baseName != null;
        this.qualifiers = qualifiers;
        this.baseName = baseName;
    }

    private final ImmutableSet<INamespaceDefinition> qualifiers;

    private final String baseName;

    @Override
    public String getName()
    {
        return baseName;
    }

    @Override
    public IDefinition resolve(ICompilerProject project, IASScope scope,
                               DependencyType dependencyType,
                               boolean canEscapeWith)
    {
        if (qualifiers.size() == 1)
        {
            INamespaceDefinition qualifier = Iterables.getOnlyElement(qualifiers);
            return ((ASScope)scope).findPropertyQualified(project, qualifier, getName(), dependencyType, canEscapeWith);
        }

        return ((CompilerProject)project).getCacheForScope((ASScope)scope).findPropertyMultiname(this, dependencyType);
    }

    @Override
    public String getDisplayString()
    {
        // Look through the multiname set for a package name.
        // Although most names in SWCs seem to have only
        // one namespace in their namespace set, interfaces seem
        // to have true multinames with multiple namespaces.
        // For code mode purposes, just look for the package namespace.
        String packageName = null;
        for (INamespaceDefinition namespace : qualifiers)
        {
            if (namespace.isPublicOrInternalNamespace())
            {
                packageName = namespace.getURI();
                if (packageName.length() > 0)
                    break;
            }
        }

        return packageName != null && packageName.length() > 0 ?
                packageName + '.' + baseName :
                baseName;
    }

    @Override
    public Name getMName(ICompilerProject project, IASScope scope)
    {
        return getMName();
    }

    private IDefinition resolveAmbiguities(ICompilerProject project, List<IDefinition> defs)
    {
        switch (defs.size())
        {
            case 0:
            {
                return null;
            }
            case 1:
            {
                assert Iterables.getOnlyElement(defs).isInProject(project);
                return Iterables.getOnlyElement(defs);
            }
            default:
            {
                IDefinition d = AmbiguousDefinition.resolveAmbiguities(project, defs, false);
                if (d == null)
                    return AmbiguousDefinition.get();
                return d;
            }
        }
    }

    @Override
    public IDefinition resolve(ICompilerProject project, ICompilationUnit referencingCompilationUnit, DependencyType dt)
    {
        CompilerProject compilerProject = (CompilerProject)project;
        ArrayList<IDefinition> defs = new ArrayList<IDefinition>(2);
        compilerProject.getScope().findDefinitionByName(referencingCompilationUnit, defs, baseName, qualifiers, dt);
        return resolveAmbiguities(project, defs);
    }

    @Override
    public IDefinition resolve(ICompilerProject project)
    {
        CompilerProject compilerProject = (CompilerProject)project;
        ArrayList<IDefinition> defs = new ArrayList<IDefinition>(2);
        compilerProject.getScope().findDefinitionByName(defs, baseName, qualifiers);
        return resolveAmbiguities(project, defs);
    }

    @Override
    public Name getMName()
    {
        ArrayList<Namespace> ns_set = new ArrayList<Namespace>(qualifiers.size());
        for (INamespaceDefinition namespace : qualifiers)
            ns_set.add(((NamespaceDefinition)namespace).getAETNamespace());
        int nameType = ns_set.size() == 1 ? CONSTANT_Qname : CONSTANT_Multiname;
        return new Name(nameType, new Nsset(ns_set), baseName);
    }

    @Override
    public ImmutableSet<INamespaceDefinition> getQualifiers()
    {
        return qualifiers;
    }

    @Override
    public boolean matches(IResolvedQualifiersReference toMatch)
    {
        if (!baseName.equals(toMatch.getName()))
            return false;
        for (INamespaceDefinition qualifierToMatch : toMatch.getQualifiers())
        {
            if (qualifiers.contains(qualifierToMatch))
                return true;
        }
        return false;
    }
}
