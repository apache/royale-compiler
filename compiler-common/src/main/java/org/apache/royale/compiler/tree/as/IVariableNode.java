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

import org.apache.royale.compiler.definitions.IVariableDefinition.VariableClassification;

/**
 * An AST node representing a <code>var</code> or <code>const</code> declaration.
 * <p>
 * The shape of this node is:
 * <pre>
 * IVariableNode
 *   IMetaTagsNode            <-- getMetaTagsNode()
 *   INamespaceDecorationNode <-- getNamespaceNode()
 *   IKeywordNode             <-- getKeywordNode()
 *   IExpressionNode          <-- getNameExpressionNode()
 *   IExpressionNode          <-- getVariableTypeNode()
 *   IExpressionNode          <-- getAssignedValueNode()
 *   IVariableNode            <-- ???
 *   IVariableNode
 *   ...
 * </pre>
 * For example,
 * <pre>
 * [Foo]
 * [Bar]
 * public var i:int = 1, j:int = 2;
 * </pre>
 * is represented as
 * <pre>
 * IVariableNode
 *   IMetaTagsNode
 *     IMetaTagNode "Foo"
 *     IMetaTagNode "Bar"
 *   INamespaceDecorationNode "public"
 *   IKeywordNode "var"
 *   IIdentifierNode "i"
 *   IIdentifierNode "int"
 *   INumericLiteralNode 1
 *   IVariableNode
 *     IIdentifierNode "j"
 *     IIdentifierNode "int"
 *     INumericLiteralNode 2
 * </pre>
 * If there is no metadata, the corresponding child node is not present.
 * <p>
 * If there is no namespace, the corresponding child node is not present.
 * <p>
 * If there is no type annotation, an implicit <code>IIdentifierNode</code>
 * for <code>"*"</code> is produced.
 * <p>
 * If there is no initial value, the corresponding child node is not present.
 * <p>
 * If multiple variables or constants are declared, then additional children
 * of type <code>IVariable</code> are present. These will not have metadata,
 * or a namespace, or a keyword.
 */
public interface IVariableNode extends IDefinitionNode, IDocumentableDefinitionNode
{
    /**
     * Returns the type of this variable as it exist in source. If a variable
     * does not have an explicit type, <code>*</code> is returned
     * 
     * @return type of variable as seen in source
     */
    String getVariableType();

    /**
     * Returns the {@link IExpressionNode} that corresponds to the type node of
     * this {@link IVariableNode}
     * 
     * @return an {@link IExpressionNode} or null
     */
    // TODO Should this return IIdentifierNode?
    IExpressionNode getVariableTypeNode();

    IExpressionNode getAssignedValueNode();

    /**
     * Get the classification for this variable (local, argument, class member,
     * etc)
     * 
     * @return variable classification
     */
    VariableClassification getVariableClassification();

    /**
     * Returns if this variable is decorated with <code>const</code>
     * 
     * @return true if <code>const</code> exists
     */
    boolean isConst();

    /**
     * Get the local end offset of this variable declaration. For single
     * variables such as var i:int = 10; getEnd() will be returned For multiple
     * variables such as var i:int = 10, j:int = 20, k:int = 30; we create a
     * variable node for variable 'i'. Variable 'j' and 'k' are created as
     * chained nodes with parent as variable node 'i' So, if we call this api
     * for 'i', then the offset where the variable 'i' declaration ends will be
     * returned. Note that in this case, getEnd() returns the end of the entire
     * statement. This api is different from getEnd() only when variable node
     * contains chained variables
     * 
     * @return the local offset where the variable declaration ends
     */
    int getDeclarationEnd();
}
