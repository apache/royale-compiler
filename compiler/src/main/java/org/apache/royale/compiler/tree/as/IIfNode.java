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
 * An AST node representing an <code>if</code> statement.
 * <p>
 * The shape of this node is
 * <pre>
 * IIfNode
 *   IConditionalNode <-- ???
 *   IConditionalNode <-- getElseIfNodes()[0]
 *   IConditionalNode <-- getElseIfNodes()[1]
 *   ...
 *   ITerminalNode    <-- getElseNode()
 * </pre>
 * Example 1:
 * <pre>
 * if (a > b)
 *     return 1;
 * </pre>
 * is represented as
 * <pre>
 * IIfNode
 *   IConditionalNode
 *     IBinaryOperatorNode ">"
 *       IIdentifierNode "a"
 *       IIdentifierNode "b"
 *     IBlockNode
 *       IReturnNode
 *         INumericLiteralNode 1
 * </pre>
 * Example 2:
 * <pre>
 * if (a > b)
 *     return 1;
 * else
 *     return 2;
 * </pre>
 * is represented as
 * <pre>
 * IIfNode
 *   IConditionalNode
 *     IBinaryOperatorNode ">"
 *       IIdentifierNode "a"
 *       IIdentifierNode "b"
 *     IBlockNode
 *       IReturnNode
 *         INumericLiteralNode 1
 *   ITerminalNode "else"
 *     IBlockNode
 *       IReturnNode
 *         INumericLiteralNode 2
 * </pre>
 * Example 3:
 * <pre>
 * if (a > b)
 *     return 1;
 * else if (a < b)
 *     return -1;
 * else
 *     return 0;
 * </pre>
 * is represented as
 * <pre>
 * IIfNode
 *   IConditionalNode
 *     IBinaryOperatorNode ">"
 *       IIdentifierNode "a"
 *       IIdentifierNode "b"
 *     IBlockNode
 *       IReturnNode
 *         INumericLiteralNode 1
 *   IConditionalNode
 *     IBinaryOperatorNode "<"
 *       IIdentifierNode "a"
 *       IIdentifierNode "b"
 *     IBlockNode
 *       IReturnNode
 *         INumericLiteralNode -1
 *   ITerminalNode "else"
 *     IBlockNode
 *       IReturnNode
 *         INumericLiteralNode 0
 * </pre>
 */
public interface IIfNode extends IConditionalNode
{
    /**
     * Returns an in-order array of else if statements that are branches of this
     * if statement
     * 
     * @return an array of {@link IConditionalNode} elements
     */
    IConditionalNode[] getElseIfNodes();

    /**
     * Returns the else clause of this if statement
     * 
     * @return an {@link ITerminalNode} or null
     */
    ITerminalNode getElseNode();
}
