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

package org.apache.royale.compiler.definitions.references;

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.units.ICompilationUnit;
import com.google.common.collect.ImmutableSet;

/**
 * A subinterface of {@link IReference} where all the qualifiers in the
 * reference are already resolved to namespace definitions.
 */
public interface IResolvedQualifiersReference extends IReference
{
    /**
     * Gets the resolved qualifiers for this reference.
     * 
     * @return An immutable set of the {@link INamespaceDefinition} qualifiers.
     */
    ImmutableSet<INamespaceDefinition> getQualifiers();

    /**
     * Resolves this reference to a definition in the given project.
     * 
     * @param project The {@link ICompilerProject} in which to resolve the
     * reference.
     * @param referencingCompilationUnit The {@link ICompilationUnit} where the
     * resolution occurs.
     * @param dependencyType The type of dependency to introduce if this
     * resolves.
     * @return The {@link IDefinition} the reference resolves to.
     */
    IDefinition resolve(ICompilerProject project,
                        ICompilationUnit referencingCompilationUnit,
                        DependencyType dependencyType);

    /**
     * Resolves this reference to a definition in the given project. This method
     * will not add any dependencies to the dependency graph in the project.
     * 
     * @param project The project in which to resolve the reference.
     */
    IDefinition resolve(ICompilerProject project);

    /**
     * Gets the AET {@link Name} that this reference represents.
     * 
     * @return An AET {@link Name} representing this reference.
     */
    Name getMName();
    
    /**
     * Determines if base name of this {@link IResolvedQualifiersReference}
     * matches the base name of the specified
     * {@link IResolvedQualifiersReference} <b>and</b> at least one qualifier in
     * the specified {@link IResolvedQualifiersReference} is in the set of
     * qualifiers in this {@link IResolvedQualifiersReference}.
     * 
     * @param toMatch {@link IResolvedQualifiersReference} to test.
     * @return false if the base name in this
     * {@link IResolvedQualifiersReference} is not equal to the base name in the
     * specified {@link IResolvedQualifiersReference}, false if none qualifiers
     * of the specified {@link IResolvedQualifiersReference} are in the set of
     * qualifiers in this {@link IResolvedQualifiersReference}, true otherwise.
     */
    boolean matches(IResolvedQualifiersReference toMatch);
}
