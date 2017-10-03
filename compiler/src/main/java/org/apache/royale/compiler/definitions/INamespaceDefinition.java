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

import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.tree.as.INamespaceNode;

/**
 * A definition representing a <code>namespace</code> declaration.
 * <p>
 * An <code>INamespaceDefinition</code is created from an <code>INamespaceNode</code>.
 * <p>
 * For example, the declaration
 * <pre>public namespace ns = "http://whatever";</pre>
 * creates a namespace definition whose base name is <code>"ns"</code>,
 * whose namespace reference is to the <code>public</code> namespace,
 * and whose type reference is <code>null</code>.
 * <p>
 * A user-defined namespace definition is contained within a file scope,
 * a package scope, a class scope, an interface scope, or a function scope;
 * it does not contain a scope.
 * <p>
 * There are also builtin namespace definitions for <code>public</code>,
 * <code>protected</code>, <code>private</code>, and <code>internal</code>
 * namespaces.
 * <p>
 * <code>NamespaceDefinition.getPublicNamespaceDefinition()</code>
 * is the singleton <code>IPublicNamespaceDefinition</code> representing
 * the <code>public</code> namespace.
 * All files share the same public namespace.
 * <p>
 * <code>NamespaceDefinition.createPrivateNamespaceDefinition()</code>
 * returns an <code>IPrivateNamespaceDefinition</code> representing
 * the <code>private</code> namespace for a particular file.
 * Different files have different private namespaces.
 * <p>
 * <code>NamespaceDefinition.createProtectedNamespaceDefinition()</code>
 * returns an <code>IProtectedNamespaceDefinition</code> representing
 * the <code>protected</code> namespace for a particular file.
 * Different fiels have different protected namespaces.
 * <p>
 * <code>NamespaceDefinition.createInternalNamespaceDefinition</code>
 * returns an <code>IInternalNamespaceDefinition</code> representing
 * the <code>internal</code> naemspace for a particular package.
 * Different packages have different internal namespaces.
 */
public interface INamespaceDefinition extends IDocumentableDefinition, IQualifiers
{
    /**
     * Determines the type of namespace
     */
    static enum NamespaceClassification
    {
        /**
         * Local namespace contained with a non-visible scope
         */
        LOCAL,

        /**
         * A namespace that is a member of a package
         */
        PACKAGE_MEMBER,

        /**
         * A namespace that is a member of a class
         */
        CLASS_MEMBER,

        /**
         * A namespace that is a member of a file
         */
        FILE_MEMBER,

        /**
         * A namespace that is part of the ActionScript language: public,
         * private, protected, internal
         */
        LANGUAGE
    }
    
    /**
     * Get the classification for this namespace (local, package level, etc,
     * etc)
     * 
     * @return a {@link NamespaceClassification}
     */
    NamespaceClassification getNamespaceClassification();

    /**
     * Returns the optional URI associated with this namespace
     * 
     * @return the URI or an empty string
     */
    String getURI();

    /**
     * Returns the {@link INamespaceNode} from which this definition was
     * created, if the definition came from the AST for a source file.
     * <p>
     * This method may require the AST to be reloaded or regenerated and
     * therefore may be slow.
     */
    @Override
    INamespaceNode getNode();

    /**
     * @param namespace
     * @return true if namespace and this resolve to the same URI This method is
     * a much better way of comparing namespaces than comparing them by name
     * (which should never be done).
     */
    boolean equals(INamespaceDefinition namespace);

    boolean isPublicOrInternalNamespace();
    
    /**
     * Interface implemented by all language namespace definitions.
     */
    interface ILanguageNamespaceDefinition extends INamespaceReference, INamespaceDefinition
    {
    }

    /**
     * Interface implemented by all language namespace definitions associated
     * with a package.
     */
    interface INamespaceWithPackageName extends ILanguageNamespaceDefinition
    {
        /**
         * Retrieves the string that should be used as the prefix for URI's in
         * generated protected and static protected namespace definitions in
         * classes.
         * 
         * @return The prefix for URI's of generated namespace definition's.
         */
        String getGeneratedURIPrefix();

        /**
         * Retrieves the package name associated with this namespace.
         * 
         * @return the package name associated with this namespace.
         */
        String getNamespacePackageName();
    }

    /**
     * Interface implemented by all public namespace definitions.
     */
    interface IPublicNamespaceDefinition extends INamespaceWithPackageName
    {
    }

    /**
     * Interface implemented by all internal namespace definitions.
     */
    interface IInternalNamespaceDefinition extends INamespaceWithPackageName
    {
    }

    /**
     * Interface implemented by the code model implicit definition namespace
     * definition.
     */
    interface ICodeModelImplicitDefinitionNamespaceDefinition extends ILanguageNamespaceDefinition
    {
    }

    /**
     * Interface implemented by all protected namespace definitions.
     */
    interface IProtectedNamespaceDefinition extends ILanguageNamespaceDefinition
    {
    }

    /**
     * Interface implemented by all static protected namespace definitions.
     */
    interface IStaticProtectedNamespaceDefinition extends ILanguageNamespaceDefinition
    {
    }

    /**
     * Interface implemented by all private namespace definitions.
     */
    interface IPrivateNamespaceDefinition extends ILanguageNamespaceDefinition
    {
    }

    /**
     * Interface implemented by the any namespace definition.
     */
    interface IAnyNamespaceDefinition extends ILanguageNamespaceDefinition
    {
    }

    /**
     * Interface implemented by all file private namespace definitions. The
     * distinction between file private namespaces and regular private
     * namespaces is purely for the benefit of code model clients.
     */
    interface IFilePrivateNamespaceDefinition extends IPrivateNamespaceDefinition, INamespaceWithPackageName
    {
    }

    /**
     * Interface implemented by all interface namespace definitions.
     */
    interface IInterfaceNamespaceDefinition extends ILanguageNamespaceDefinition
    {
    }
}
