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

import java.util.Set;

import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.ITypeNode;

/**
 * The base interface for class and interface definitions,
 * including definitions of vector types.
 */
public interface ITypeDefinition extends IMemberedDefinition
{
    /**
     * Determines whether an "is-a" relationship exists between two classes or
     * interfaces.
     * <p>
     * If <code>this</code> is an {@code IClassDefinition} and <code>type</code>
     * is an {@code IClassDefinition}, this method determines whether
     * <code>this</code> is a subclass of <code>type</code>.
     * <p>
     * If <code>this</code> is an {@code IClassDefinition} and <code>type</code>
     * is an {@code IInterfaceDefinition}, this method determines whether
     * <code>this</code> is implements <code>type</code>.
     * <p>
     * If <code>this</code> is an {@code IInterfaceDefinition} and
     * <code>type</code> is an {@code IInterfaceDefinition}, this method
     * determines whether <code>this</code> is a subinterface of
     * <code>type</code>.
     * <p>
     * A class or interface is considered an instance of itself.
     * 
     * @param type An {@code ITypeDefinition} representing a class or interface.
     * @param project The {@link ICompilerProject} within which references should
     * be resolved
     * @return A flag indicating whether an "is-a" relationship exists between
     * the two types.
     */
    boolean isInstanceOf(ITypeDefinition type, ICompilerProject project);

    /**
     * A variant of <code>isInstanceOf</code> which takes a fully-qualified type
     * name rather than an {@code ITypeDefinition}.
     * <p>
     * While not @deprecated, this method may not do what you want because the
     * scope containing this type definition will be used to resolve the name
     * that is specified rather than some other scope which may actually contain
     * the name in source code.
     * 
     * @param qualifiedName A {@code String}, such as <code>"flash.display.Sprite"</code>
     * , that specified a fully-qualified type name.
     * @param project The {@link ICompilerProject} within which references should
     * be resolved
     * @return A flag indicating whether an "is-a" relationship exists between
     * the two types.
     */
    boolean isInstanceOf(String qualifiedName, ICompilerProject project);

    /**
     * Creates an Iterable for enumerating the types of this type.
     * 
     * @param project The {@link ICompilerProject} within which references
     * should be resolved.
     * @param skipThis A flag indicating whether the enumeration should start
     * with this type rather than with its supertype.
     * @return An Iterable that iterates over {@code ITypeDefinition} objects.
     */
    Iterable<ITypeDefinition> typeIteratable(final ICompilerProject project, final boolean skipThis);

    //todo add support for this
    /**
     * Get an array containing all of the members contained within this
     * {@link ITypeDefinition} nested inside this type. If the hierarchy of this
     * type allows for overriding member definitions, then this list could
     * contain shadowed member signatures, since this returns all members from
     * the hierarchy of this type. To guard against this behavior, the
     * excludeShadowedMembers parameter exists to ensure non-shadowed members
     * are returned
     * 
     * @param memberFilter ASDefinitionFilter describing the members that should
     * be included
     * @param excludeShadowedMembers true if we should only return non-shadowed
     * members. If A extends B, and B overrides c defined in A, only c from B
     * will be returned
     * @return an array containing all of the members matching the
     * {@link ASDefinitionFilter}
     */
    /*
     * public IDefinition[] getAllMembers(ASDefinitionFilter memberFilter,
     * boolean excludeShadowedMembers);
     */

    /**
     * Returns the {@link ITypeNode} from which this definition was created, if
     * the definition came from the AST for a source file.
     * <p>
     * This method may require the AST to be reloaded or regenerated and
     * therefore may be slow.
     */
    @Override
    ITypeNode getNode();

    Set<IInterfaceDefinition> resolveAllInterfaces(ICompilerProject project);

    /**
     * Gets the {@link INamespaceDefinition} that represents the protected
     * namespace for this type.
     * 
     * @return The {@link INamespaceDefinition} that represents the protected
     * namespace for this type.
     */
    INamespaceDefinition getProtectedNamespaceReference();

    /**
     * Gets the {@link INamespaceDefinition} that represents the static
     * protected namespace for this type.
     * 
     * @return The {@link INamespaceDefinition} that represents the static
     * protected namespace for this type.
     */
    INamespaceDefinition getStaticProtectedNamespaceReference();
}
