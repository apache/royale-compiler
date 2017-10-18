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
 * An AST node representing a <code>for</code>, <code>for</code>-<code>in</code>,
 * or <code>for</code>-<code>each</code>-<code>in</code> statement.
 * <p>
 * The shape of this node is
 * <pre>
 * IForLoopNode
 *   IContainerNode <-- getConditionalsContainerNode()
 *   IBlockNode     <-- getStatementContentsNode()
 * </pre>
 * In the case of a regular <code>for</code> statement,
 * the container node contains the three control expressions.
 * For example,
 * <pre>
 * for (i = 0; i < n; i++)
 * {
 *     ...
 * }
 * </pre>
 * is represented as
 * <pre>
 * IForLoopNode "for"
 *   IContainerNode
 *     IBinaryOperatorNode "="
 *       IIdentifierNode "i"
 *       INumericalLiteralNode 0
 *     IBinaryOperatorNode "<"
 *       IIdentifierNode "i"
 *       IIdentifierNode "n"
 *     IUnaryOperatorNode "++"
 *       IIdentifierNode "i"
 *   IBlockNode
 *     ...
 * </pre>
 * In the case of a <code>for</code>-<code>in</code>,
 * or <code>for</code>-<code>each</code>-<code>in</code> statement,
 * the container node contains a single binary operator node
 * for the <code>in</code> operator.
 * For example,
 * <pre>
 * for (p in o)
 * {
 *     ...
 * }
 * </pre>
 * is represented as
 * <pre>
 * IForLoopNode "for"
 *   IContainerNode
 *     IBinaryOperatorNode "in"
 *       IIdentifierNode "p"
 *       IIdentifierNode "o"
 *   IBlockNode
 *     ...
 * </pre>
 */
public interface IForLoopNode extends IStatementNode
{
    /**
     * A ForLoopKind represents different kinds of for loops
     */
    enum ForLoopKind
    {
        /**
         * A standard for loop
         */
        FOR,
        
        /**
         * A for each loop
         */
        FOR_EACH
    }
    
    /**
     * Returns the kind of the <code>for</code> statement.
     * 
     * @return a {@link ForLoopKind}
     */
    ForLoopKind getKind();
    
    /**
     * Returns the container node containing the conditional expression(s)
     * for the <code>for</code> statement.
     * 
     * @return An {@link IContainerNode} containing the conditional expressions,
     * or <code>null</code>.
     */
    IContainerNode getConditionalsContainerNode();

    /**
     * Returns an array containing the conditional expression(s)
     * for the <code>for</code> statement.
     * 
     * @return An array of {@link IExpressionNode} objects, or <code>null</code>.
     */
    IExpressionNode[] getConditionalExpressionNodes();
}
