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
 * An AST node representing an <code>with</code> statement.
 * <p>
 * The shape of this node is:
 * <pre>
 * IWithNode 
 *   IExpressionNode <-- getTargetNode()
 * </pre>
 * For example,
 * <pre>
 * with (r)
 *   trace(x);
 * </pre>
 * is represented as
 * <pre>
 * IWithNode
 *   IIdentifierNode "r"
 *   IBlockNode
 *     IFunctionCallNode
 *       ..
 * </pre>
 * Note that an implicit <code>IBlockNode</code> is created
 * even if there are no curly braces.
 */
public interface IWithNode extends IStatementNode
{
    /**
     * Returns the {@link IExpressionNode} that contains the target expression
     * within this with statement
     * 
     * @return an {@link IExpressionNode} or null
     */
    IExpressionNode getTargetNode();
}
