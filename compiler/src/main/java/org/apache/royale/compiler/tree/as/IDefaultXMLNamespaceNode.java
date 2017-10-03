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

/**
 * An AST node representing a <code>default xml namespace</code> statement.
 * <p>
 * The typical shape of this node is:
 * <pre>
 * IDefaultXMLNamespaceNode
 *   IExpressionNode <-- getExpressionNode()
 * </pre>
 * For example, <code>default xml namespace = "http://ns.whatever.com"</code> is represented as
 * <pre>
 * IDefaultXMLNamespaceNode
 *   ILiteralNode "http://ns.whatever.com"
 * </pre>
 */
public interface IDefaultXMLNamespaceNode extends IASNode
{
    /**
     * Gets the node representing the
     * <code>default xml namespace</code> keyword.
     * <p>
     * This is not considered a child node.
     * 
     * @return An {@link IKeywordNode}.
     */
    IKeywordNode getKeywordNode();

    /**
     * Gets the child node representing the namespace expression
     * on the right-hand-side of the statement.
     * <p>
     * This is the sole child node.
     * 
     * @return An {@link IExpressionNode}.
     */
    IExpressionNode getExpressionNode();
}
