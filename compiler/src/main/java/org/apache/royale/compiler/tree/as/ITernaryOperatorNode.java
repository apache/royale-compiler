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
 * An AST node representing the ternary operator.
 * <p>
 * The typical shape of this node is:
 * <pre>
 * ITernaryOperatorNode 
 *   IExpressionNode <-- getConditionalNode()
 *   IExpressionNode <-- getLeftOperandNode()
 *   IExpressionNode <-- getRightOperandNode()
 * </pre>
 * For example, <code>a == b ? c : d</code> is represented as
 * <pre>
 * ITernaryOperatorNode "?"
 *   IBinaryOperatorNode "=="
 *     IIdentifierNode "a"
 *     IIdentifierNode "b"
 *   IIdentifierNode "c"
 *   IIdentiferNode "d"
 * </pre>
 */
public interface ITernaryOperatorNode extends IExpressionNode
{
    /**
     * returns the conditional expression that determines branch for this
     * expression
     * 
     * @return an {@link IExpressionNode} representing the conditional
     */
    IExpressionNode getConditionalNode();

    /**
     * Returns the left side of the expression
     * 
     * @return an {@link IExpressionNode} that represents the left side, or null
     */
    IExpressionNode getLeftOperandNode();

    /**
     * Returns the right side of the expression
     * 
     * @return an {@link IExpressionNode} that represents the right side, or
     * null
     */
    IExpressionNode getRightOperandNode();
}
