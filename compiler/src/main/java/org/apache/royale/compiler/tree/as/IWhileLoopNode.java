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
 * An AST node representing a <code>while</code>
 * or <code>do<code>-<code>while</code> statement.
 * <p>
 * The shape of this node is
 * <pre>
 * IWhileLoopNode
 *   IExpressionNode <-- getConditionalExpressionNode()
 *   IBlockNode      <-- getStatementContentsNode()
 * </pre>
 * in the case of a <code>while</code> statement and
 * <pre>
 * IWhileLoopNode
 *   IBlockNode      <-- getStatementContentsNode()
 *   IExpressionNode <-- getConditionalExpressionNode()
 * </pre>
 * in the case of a <code>do<code>-<code>while</code> statement.
 * Example 1:
 * <pre>
 * while (a > b)
 * {
 *     a--;
 *     b++;
 * }
 * </pre>
 * is represented as
 * <pre>
 * IWhileLoopNode
 *   IBinaryOperatorNode ">"
 *     IIdentifierNode "a"
 *     IIdentiferNode "b"
 *   IBlockNode
 *     IUnaryOperatorNode "--"
 *       IIdentifierNode "a"
 *     IUnaryOperatorNode "++"
 *       IIdentifierNode "b"
 * </pre>
 * Example 2:
 * <pre>
 * do
 * {
 *     a--;
 *     b++;
 * }
 * while (a > b)
 * </pre>
 * is represented as
 * <pre>
 * IWhileLoopNode
 *   IBlockNode
 *     IUnaryOperatorNode "--"
 *       IIdentifierNode "a"
 *     IUnaryOperatorNode "++"
 *       IIdentifierNode "b"
 *   IBinaryOperatorNode ">"
 *     IIdentifierNode "a"
 *     IIdentiferNode "b"
 * </pre>
 * An implicit <code>IBlockNode</code> is created
 * when there are no curly braces.
 */
public interface IWhileLoopNode extends IConditionalNode
{
    /**
     * A WhileLoopKind represents different kinds of while loops
     */
    enum WhileLoopKind
    {
        /**
         * A while loop
         */
        WHILE,
        
        /**
         * A do loop
         */
        DO
    }

    /**
     * Returns the kind of the while loop
     * 
     * @return a {@link WhileLoopKind}
     */
    WhileLoopKind getKind();
}
