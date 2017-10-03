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
 * An AST node representing a <code>switch</code> statement.
 * <p>
 * The shape of this node is:
 * <pre>
 * ISwitchNode
 *   IExpressionNode    <-- getConditionalExpressionNode()
 *   IBlockNode         <-- getStatementContentsNode()
 *     IConditionalNode <-- getCaseNodes()[0]
 *     IConditionalNode <-- getCaseNodes()[1]
 *     ...
 *     ITerminalNode    <-- getDefaultNode()
 * </pre>
 * For example,
 * <pre>
 * switch (i)
 * {
 *     case 1:
 *         return a;
 *     case 3:
 *         return b;
 *     default:
 *         return c;
 * </pre>
 * is represented as
 * <pre>
 * ISwitchNode
 *   IIdentifierNode "i"
 *   IBlockNode
 *     IConditionalNode
 *       INumericLiteralNode 1
 *       IBlockNode
 *         IReturnNode
 *           IIdentifierNode "a"
 *     IConditionalNode
 *       INumericLiteralNode 2
 *         IBlockNode
 *           IReturnNode
 *             IIdentifierNode "b"
 *     ITerminalNode
 *       IBlockNode
 *         IReturnNode
 *           IIdentifierNode "c"
 * </pre>
 * An implicit <code>IBlockNode</code> is created for each <code>case</code>
 * and <code>default</code> clause that doesn't have curly braces.
 */
public interface ISwitchNode extends IConditionalNode
{
    /**
     * Returns an in-order array of case statements that are branches of this
     * switch statement
     * 
     * @return an array of {@link IConditionalNode} elements
     */
    IConditionalNode[] getCaseNodes();

    /**
     * Returns the default clause of this switch statement
     * 
     * @return an {@link ITerminalNode} or null
     */
    ITerminalNode getDefaultNode();
}
