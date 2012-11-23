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

import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.projects.ICompilerProject;

public interface IFilterContext
{
    /**
     * Determine whether this lookup is from a static context (e.g. somewhere
     * within a static method)
     * 
     * @return true if the lookup is being performed in a static context
     */
    boolean isInStaticContext();

    /**
     * Returns a Set of {@link INamespaceDefinition}'s which are "open" in this
     * context. This set should namespace's like:
     * <ul>
     * <li>public namespace's for imported packages</li>
     * <li>public namespace for the unnamed package</li>
     * <li>namespace's referenced from a "use namespace" directive in this or
     * any containing lexical scopes</li>
     * <li>file private name space for the file that contains this context</li>
     * <li>internal namespace for the package that contains this context</li>
     * <li>class private, protected and static protected namespace's for the
     * class that contains this context</li>
     * </ul>
     * 
     * @return The Set of {@link INamespaceDefinition}'s which are "open" in
     * this context.
     */
    Set<INamespaceDefinition> getNamespaceSet(ICompilerProject project);

    /**
     * Returns a Set of {@link INamespaceDefinition}'s which are "open" in this
     * context for the given name. This set should namespace's like:
     * <ul>
     * <li>public namespace's for imported packages, or imported definitions
     * (import a.b.Foo)</li>
     * <li>public namespace for the unnamed package</li>
     * <li>namespace's referenced from a "use namespace" directive in this or
     * any containing lexical scopes</li>
     * <li>file private name space for the file that contains this context</li>
     * <li>internal namespace for the package that contains this context</li>
     * <li>class private, protected and static protected namespace's for the
     * class that contains this context</li>
     * </ul>
     * 
     * @return The Set of {@link INamespaceDefinition}'s which are "open" in
     * this context.
     */
    Set<INamespaceDefinition> getNamespaceSet(ICompilerProject project, String name);
}
