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

package org.apache.royale.compiler.common;

import java.util.HashSet;
import java.util.Set;

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.workspaces.IWorkspace;

public final class Multiname
{
    /**
     * Constructs a {@link Multiname} by parsing the specified name string.
     * 
     * @param project {@link ICompilerProject} whose {@link IWorkspace} is used
     * to create package namespace definitions.
     * @param name Either a simple definition name or a dotted qname.
     * @return A new {@link Multiname} created from information in the specified
     * name
     */
    public static Multiname crackDottedQName(ICompilerProject project, String name)
    {
        return crackDottedQName(project, name, false);
    }
    
    /**
     * Constructs a {@link Multiname} by parsing the specified name string.
     * 
     * @param project {@link ICompilerProject} whose {@link IWorkspace} is used
     * to create package namespace definitions.
     * @param name Either a simple definition name or a dotted qname.
     * @param includeInternal true if package internal namespaces should be in
     * the namespace set in the multiname.
     * @return A new {@link Multiname} created from information in the specified
     * name
     */
    public static Multiname crackDottedQName(ICompilerProject project, String name, boolean includeInternal)
    {
        Workspace workspace = (Workspace)project.getWorkspace();
        final Set<INamespaceDefinition> namespaceSet = new HashSet<INamespaceDefinition>(includeInternal ? 2 : 1);
        if (name != null)
        {
            int lastIndexOfDot = name != null ? name.lastIndexOf('.') : -1;
            if (lastIndexOfDot != -1)
            {
                final String definitionName = name.substring(lastIndexOfDot + 1);
                final String packageName = name.substring(0, lastIndexOfDot);
                INamespaceDefinition publicPackageNS =
                    workspace.getPackageNamespaceDefinitionCache().get(packageName, false);
                namespaceSet.add(publicPackageNS);
                if (includeInternal)
                {
                    INamespaceDefinition internalPackageNS =
                        workspace.getPackageNamespaceDefinitionCache().get(packageName, true);
                    namespaceSet.add(internalPackageNS);            
                }
                return new Multiname(namespaceSet, definitionName);
            }
            else
            {
                // not in a package, could be Vector.
                if (name.equals(IASLanguageConstants.Vector))
                {
                    INamespaceDefinition vectorPackageNS =
                        workspace.getPackageNamespaceDefinitionCache().get(IASLanguageConstants.Vector_impl_package, false);
                    namespaceSet.add(vectorPackageNS);            
                    return new Multiname(namespaceSet, name);
                }
            }
        }

        final INamespaceDefinition publicPackageNS = NamespaceDefinition.getPublicNamespaceDefinition();
        namespaceSet.add(publicPackageNS);
        if (includeInternal)
        {
            INamespaceDefinition internalPackageNS =
                workspace.getPackageNamespaceDefinitionCache().get("", true);
            namespaceSet.add(internalPackageNS);            
        }
        return new Multiname(namespaceSet, name);
    }

    /**
     * Returns the package name part of a dotted fully qualified AS3 name. One
     * thing this method is used for is to compute package names of mxml
     * classes.
     * 
     * @param name A dotted fully qualified AS3 name
     * @return The package name in the specified name.
     */
    public static String getPackageNameForQName(String name)
    {
        if (name == null)
            return "";
        int lastIndexOfDot = name.lastIndexOf('.');
        if (lastIndexOfDot != -1)
            return name.substring(0, lastIndexOfDot);
        return "";
    }
    
    /**
     * Returns the base name part of a dotted fully qualified AS3 name. 
     * 
     * @param name A dotted fully qualified AS3 name
     * @return The base name in the specified name.
     */
    public static String getBaseNameForQName(String name)
    {
        int lastIndexOfDot = name.lastIndexOf('.');
        if (lastIndexOfDot != -1)
            return name.substring(lastIndexOfDot + 1);
        return name;
    }
    
    public static Multiname create(Set<INamespaceDefinition> nsset, String baseName)
    {
        return new Multiname(nsset, baseName);
    }
    
    public Multiname(Set<INamespaceDefinition> namespaceSet, String baseName)
    {
        this.namespaceSet = namespaceSet;
        this.baseName = baseName;
    }
    
    private final Set<INamespaceDefinition> namespaceSet;
    private final String baseName;
    
    /**
     * Gets the namespace set for this {@link Multiname}.
     * @return The namespace set for this {@link Multiname}.
     */
    public Set<INamespaceDefinition> getNamespaceSet()
    {
        return namespaceSet;
    }
    
    /**
     * Gets the base name for this {@link Multiname}.
     * @return The base name set for this {@link Multiname}.
     */
    public String getBaseName()
    {
        return baseName;
    }
    
    /**
     * Gets the AET name for this {@link Multiname}.
     * @return A {@link Name} object.
     */
    public Name getMName()
    {
        Set<Namespace> namespaces = new HashSet<Namespace>(namespaceSet.size());
        for ( INamespaceDefinition ns: namespaceSet )
            namespaces.add( ((NamespaceDefinition)ns).getAETNamespace() );

        return new Name(new Nsset(namespaces), getBaseName());
    }
    
    /**
     * For debugging only.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (INamespaceDefinition namespace : namespaceSet)
        {
            builder.append(namespace.toString());
            builder.append(", ");
        }
        builder.append("]::");
        builder.append(baseName);
        return builder.toString();
    }
}
