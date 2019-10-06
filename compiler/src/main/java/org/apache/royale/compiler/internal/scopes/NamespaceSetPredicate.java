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
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.projects.ICompilerProject;
import com.google.common.base.Predicate;

import java.util.Set;

/**
 * A predicate to implement namespace set checking.  This predicate
 * will return false for any definition whose namespace is not in the namespace set
 */
public class NamespaceSetPredicate implements Predicate<IDefinition>
{
    /**
     * Project to use to resolve namespace namespaces on definitions
     */
    private final ICompilerProject project;

    /**
     * The namespace set to use to determine if a given definition should
     * be included
     */
    private final Set<INamespaceDefinition> namespaceSet;

    /**
     * The "extra" namespace that classes can set as we move up the inheritance chain.
     * This is used to implement protected namespaces, as each class has it's own protected
     * namespace and we must add the base class protected as we traverse the inheritance chain
     * while looking for properties
     */
    private INamespaceDefinition extraNamespace;

    /**
     * Constructor
     * @param project       project to do any resolutions in
     * @param namespaceSet  the namespace set to use to do the filtering.
     */
    public NamespaceSetPredicate (ICompilerProject project, Set<INamespaceDefinition> namespaceSet)
    {
        this.project = project;
        this.namespaceSet = namespaceSet;
    }

    /**
     * Implement the namespace checking.
     * @param definition    the definition to check
     * @return              true if the namespace set says that the definition should be included
     *                      in the results based on the definitions namespace.
     */
    public boolean apply (IDefinition definition)
    {
        INamespaceReference nsRef = definition.getNamespaceReference();

        if( namespaceSet == ASScopeBase.allNamespacesSet )
            return true;

        if( this.extraNamespace != null && nsRef == this.extraNamespace )
            return true;

        INamespaceDefinition namespace = definition.resolveNamespace(project);
        if( namespaceSet.contains(namespace) )
            return true;
        else if( (extraNamespace != null) && ((namespace == extraNamespace) || (extraNamespace.equals(namespace))))
            return true;

        return false;
    }

    @Override
    public boolean test(IDefinition input)
    {
        return apply(input);
    }

    /**
     * Does the underlying namespace set contain the namespace passed in
     * @param d the namespace to check
     * @return  true if the underlying namespace set contains d
     */
    public boolean containsNamespace(INamespaceDefinition d)
    {
        return namespaceSet != null && namespaceSet.contains(d);
    }

    /**
     * Set the extra namespace.
     * This is used by TypeScope as it walks up the base classes
     * @param extra the current extra namespace to use
     */
    public void setExtraNamespace(INamespaceDefinition extra)
    {
        this.extraNamespace = extra;
    }

    /**
     * @return the Namespace Set this predicate is using
     */
    public Set<INamespaceDefinition> getNamespaceSet()
    {
        return this.namespaceSet;
    }
}
