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

package org.apache.royale.compiler.definitions;

import java.util.Iterator;

import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * A definition representing an <code>interface</code> declaration.
 * <p>
 * An <code>IInterfaceDefinition</code is created from an <code>IInterfaceNode</code>.
 * <p>
 * For example, the interface declaration
 * <pre>
 * public class I2 extends I1
 * {
 * }</pre>
 * creates an interface definition whose base name is <code>"I2"</code>,
 * whose namespace reference is to the <code>public</code> namespace,
 * and whose type reference is <code>null</code>.
 * It has an <code>IReference</code> named "I1" to the interface that it extends.
 * <p>
 * An interface definition is contained within a file scope or a package scope,
 * and contains an interface scope.
 * The members of the interface are represented by definitions in the interface scope.
 */
public interface IInterfaceDefinition extends ITypeDefinition
{
    /**
     * Determines the type of interface
     */
    static enum InterfaceClassification
    {
        /**
         * An interface contained with a package
         */
        PACKAGE_MEMBER,

        /**
         * An interface contained within a file, outside a package
         */
        FILE_MEMBER
    }

    /**
     * Returns the classification of this ActionScript interface
     * 
     * @return the {@link InterfaceClassification}
     */
    InterfaceClassification getInterfaceClassification();

    /**
     * Returns {@link IReference} objects that will resolve to any interface this
     * interface directly extends. This does not walk of the inheritance chain.
     * 
     * @return An array of interface {@link IReference} objects, or an empty array.
     */
    IReference[] getExtendedInterfaceReferences();

    /**
     * Get the definitions of the extended interfaces. This does not walk up the
     * inheritance chain, rather only looks at what is directly defined on the
     * interface.
     * 
     * @return implemented interface definitions
     */
    IInterfaceDefinition[] resolveExtendedInterfaces(ICompilerProject project);

    /**
     * Returns the names of any interfaces that this interface
     * directly references. This does not walk up the inheritance chain, rather
     * only looks at what is directly defined on the interface
     * 
     * @return An array of interface names, or an empty array.
     */
    String[] getExtendedInterfacesAsDisplayStrings();

    /**
     * Creates an iterator for enumerating all of the interfaces that this class
     * implements. The enumeration includes not just the interfaces that the
     * class directly implements, but the ones that they extend, and the ones
     * those extend, etc.
     * 
     * @param project The {@link ICompilerProject} within which references
     * should be resolved
     * @param includeThis A flag indicating whether the enumeration should start
     * with this interface rather with than its first superinterface.
     * @return An iterator that iterates over {@code IInterfaceDefinition}
     * objects.
     */
    Iterator<IInterfaceDefinition> interfaceIterator(ICompilerProject project, boolean includeThis);
}
