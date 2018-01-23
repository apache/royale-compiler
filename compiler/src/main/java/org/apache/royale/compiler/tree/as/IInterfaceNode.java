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

import org.apache.royale.compiler.definitions.IInterfaceDefinition;

/**
 * An AST node representing a <code>interface</code> declaration.
 * <p>
 * The shape of this node is:
 * <pre>
 * IInterfaceNode
 *   IMetaTagsNode            <-- getMetaTagsNode()
 *   INamespaceDecorationNode <-- getNamespaceNode()
 *   IKeywordNode             <-- getKeywordNode()
 *   IExpressionNode          <-- getNameExpressionNode()
 *   IKeywordNode             <-- getExtendsKeywordNode()
 *   IContainerNode           <-- getInterfacesNode()
 *   IScopedNode              <-- getScopedNode()
 * </pre>
 * For example,
 * <pre>
 * [Foo]
 * [Bar]
 * public interface I extends I1, I2
 * {
 *     ...
 * }
 * </pre>
 * is represented as
 * <pre>
 * IInterfaceNode
 *   IMetaTagsNode
 *     IMetaTagNode "Foo"
 *     IMetaTagNode "Bar"
 *   INamespaceDecorationNode "public"
 *   IKeywordNode "interface"
 *   IIdentifierNode "I"
 *   IKeywordNode "extends"
 *     IContainerNode
 *       IIdentifierNode "I1"
 *       IIdentifierNode "I2"
 *   IScopedNode
 *     ...
 * </pre>
 * If there is no metadata, the corresponding child node is not present.
 * <p>
 * If there is no namespace, the corresponding child node is not present.
 * <p>
 * If there is no <code>extends</code> clause, the two corresponding
 * child nodes are not present.
 * <p>
 * If the interface extends only one interface,
 * there is still an <code>IContainerNode</code> containing the one
 * node for the interface being extended.
 */
public interface IInterfaceNode extends ITypeNode, IDocumentableDefinitionNode
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
        INNER_INTERFACE
    }

    /**
     * Returns the names of any interfaces that this {@link IInterfaceNode}
     * directly references. This does not walk up the inheritance chain, rather
     * only looks at what is directly defined on the interface
     * 
     * @return an array of interface names, or an empty array
     */
    String[] getExtendedInterfaces();

    /**
     * Returns the classification of this ActionScript interface
     * 
     * @return the {@link InterfaceClassification}
     */
    InterfaceClassification getInterfaceClassification();

    /**
     * Returns the names of any interfaces that this {@link IInterfaceNode}
     * directly references. This does not walk up the inheritance chain, rather
     * only looks at what is directly defined on the interface
     * 
     * @return an array of interface names, or an empty array
     */
    IExpressionNode[] getExtendedInterfaceNodes();

    /**
     * Returns an array of {@link IDefinitionNode} objects that represent all
     * the children of this interface that are members.
     * 
     * @return an array of {@link IDefinitionNode} children, or an empty array
     */
    IDefinitionNode[] getAllMemberDefinitionNodes();

    /**
     * Gets the symbol that represents this {@link IInterfaceNode}
     * 
     * @return an {@link IInterfaceDefinition} that is the symbolic
     * representation of this node
     */
    @Override
    IInterfaceDefinition getDefinition();
}
