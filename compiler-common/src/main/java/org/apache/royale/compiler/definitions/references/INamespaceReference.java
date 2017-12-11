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

import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * INamespaceReferences are used in the symbol table to represent
 * references to namespaces in two contexts:
 * <ul>
 * <li>a namespace specified on a <code>const</code>, <code>var</code>,
 * <code>function</code>, and <code>namespace</code> definition
 * inside a <code>class</code> or <code>interface</code>;</li>
 * <li>a namespace specified in a <code>use namespace</code> directive.</li>
 * </ul>
 * <p>
 * In the first context, the INamespaceDefinition can represent a custom namespace
 * of the form <code>ns1</code>, <code>ns1::ns2</code>,
 * <code>(ns1::ns2)::ns3</code>, etc.,
 */
public interface INamespaceReference
{
    /**
     * Does this namespace reference refer to a builtin language namespace?
     * @return <code>true</code> if it does.
     */
    boolean isLanguageNamespace();

    /**
     * Resolves this namespace reference to a namespace definition.
     * @param project {@link ICompilerProject} whose symbol table will be used to resolve
     * references across ICompilationUnits.
     * @return The {@link INamespaceDefinition} representing the definition of the namespace.
     */
    INamespaceDefinition resolveNamespaceReference(ICompilerProject project);

    // TODO Remove this after we are using INamespaceReference correctly everywhere.
    String getBaseName();

    boolean isPublicOrInternalNamespace();
}
