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

import org.apache.royale.compiler.tree.metadata.IMetaTagNode;

/**
 * An AST node representing a <code>class</code> declaration.
 * <p>
 * The shape of this node is:
 * <pre>
 * IClassNode
 *   IMetaTagsNode            <-- getMetaTagsNode()
 *   INamespaceDecorationNode <-- getNamespaceNode()
 *   IKeywordNode             <-- getKeywordNode()
 *   IExpressionNode          <-- getNameExpressionNode()
 *   IKeywordNode             <-- getExtendsKeywordNode()
 *   IExpressionNode          <-- getBaseClassExpressionNode()
 *   IKeywordNode             <-- getImplementsKeywordNode()
 *   IContainerNode           <-- getInterfacesNode()
 *   IScopedNode              <-- getScopedNode()
 * </pre>
 * For example,
 * <pre>
 * [Foo]
 * [Bar]
 * public class B extends A implements I1, I2
 * {
 *     ...
 * }
 * </pre>
 * is represented as
 * <pre>
 * IClassNode
 *   IMetaTagsNode
 *     IMetaTagNode "Foo"
 *     IMetaTagNode "Bar"
 *   INamespaceDecorationNode "public"
 *   IKeywordNode "class"
 *   IIdentifierNode "B"
 *   IKeywordNode "extends"
 *   IIdentifierNode "A"
 *   IKeywordNode "implements"
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
 * If there is no <code>implements</code> clause, the two corresponding
 * child nodes are not present.
 * <p>
 * If the class implements only one interface,
 * there is still an <code>IContainerNode</code> containing the one
 * node for the interface.
 */
public interface IClassNode extends ICommonClassNode
{
    IExpressionNode getBaseClassExpressionNode();

    IExpressionNode[] getImplementedInterfaceNodes();

    /**
     * Returns an array of {@link IDefinitionNode} objects that represent all
     * the children of this class that are members. These include functions,
     * variables and namespaces
     * 
     * @return an array of {@link IDefinitionNode} children, or an empty array
     */
    IDefinitionNode[] getAllMemberNodes();

    /**
     * Retrieve all of the meta attributes (e.g. [Event("")]) that match the
     * given name in the metadata for this class or any of its base classes
     * 
     * @param name name of meta attributes to search for (e.g. Event or
     * IconFile)
     * @return array of meta attributes matching that name (or empty array)
     */
    IMetaTagNode[] getMetaTagNodesByName(String name);
}
