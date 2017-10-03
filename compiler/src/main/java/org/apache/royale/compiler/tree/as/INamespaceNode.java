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

package org.apache.royale.compiler.tree.as;

import org.apache.royale.compiler.definitions.INamespaceDefinition.NamespaceClassification;

/**
 * An AST node representing a <code>namespace</code> declaration.
 * <p>
 * The shape of this node is:
 * <pre>
 * INamespaceNode
 *   IExpressionNode <-- getNameExpressionNode()
 *   IExpressionNode <-- getNamespaceURINode()
 * </pre>
 * For example,
 * <pre>
 * [Foo]
 * [Bar]
 * public namespace ns = "http://whatever";
 * </pre>
 * is represented as
 * <pre>
 * INamespaceNode
 *   IIdentifierNode "ns"
 *   ILiteralNode "http://whatever"
 * </pre>
 * Currently the metadata and the namespace annotation are not exposed
 * as child nodes. This is inconsistent with, for example, <code>IVariableNode</code>.
 * <p>
 * If there is no initial value, the corresponding child node is not present.
 */
public interface INamespaceNode extends IDefinitionNode, IDocumentableDefinitionNode
{
    /**
     * Returns the optional URI associated with this namespace
     * 
     * @return the URI or an empty string
     */
    String getURI();

    /**
     * Get the classification for this namespace (local, package level, etc,
     * etc)
     * 
     * @return a {@link NamespaceClassification}
     */
    NamespaceClassification getNamespaceClassification();

    /**
     * Return the initializer for this namespace decl
     */
    IExpressionNode getNamespaceURINode();
}
