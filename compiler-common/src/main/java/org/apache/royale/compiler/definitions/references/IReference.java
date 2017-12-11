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

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * An <code>IReference</code> represents a reference-by-name to an
 * {@link IDefinition}.
 * <p>
 * References are used by the various definition classes to refer to other
 * definitions; for example, <code>ClassDefinition</code> stores a reference to
 * its base class and <code>VariableDefinition</code> stores a reference to its
 * type.
 * <p>
 * A reference can resolve to different definitions in different projects. For
 * example, a reference to the <code>Object</code> class might resolve to the
 * <code>Object</code> class from <code>playerglobal.swc</code> in a web project
 * and to the <code>Object</code> class from <code>airglobal.swc</code> in an
 * AIR project.
 * <p>
 * There are a variety of implementations of this interface that represent
 * different kinds of references, such as unqualified names, multinames, etc.
 * <p>
 * To construct an {@link IReference}, use the methods in
 * {@link ReferenceFactory}.
 */
public interface IReference
{
    /**
     * Gets the base name for this reference.
     * 
     * @return The base name as a <code>String<code>.
     */
    String getName();

    /**
     * Resolves the reference to its {@link IDefinition} in the given project
     * and scope.
     * 
     * @param project The project in which to resolve the reference.
     * @param scope The scope where the resolution is occurring.
     * @param dependencyType The type of dependency to introduce if the reference
     * resolves outside of the compilation unit.
     * @param canEscapeWith Whether the resolution should look past a
     * <code>with</code> scope.
     * @return The {@link IDefinition} the reference resolves to.
     */
    IDefinition resolve(ICompilerProject project, IASScope scope,
                        DependencyType dependencyType,
                        boolean canEscapeWith);

    /**
     * Gets a string representation of this reference suitable for display
     * in the description of a compiler problem.
     */
    String getDisplayString();

}
