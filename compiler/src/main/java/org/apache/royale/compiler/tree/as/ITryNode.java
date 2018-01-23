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
 * An AST node representing a <code>try</code> statement.
 * <p>
 * The shape of this node is:
 * <pre>
 * ITryNode 
 *   IBlockNode    <-- getStatementContentsNode()
 *   ICatchNode    <-- getCatchNode(0)
 *   ICatchNode    <-- getCatchNode(1)
 *   ...
 *   ITerminalNode <-- getFinallyNode()
 * </pre>
 * For example,
 * <pre>
 * try
 * {
 *     ...
 * }
 * catch (e:SomeError)
 * {
 *     ...
 * }
 * catch (e:Error)
 * {
 *     ...
 * }
 * finally
 * {
 *     ...
 * }
 * </pre>
 * is represented as
 * <pre>
 * ITryNode
 *   IBlockNode
 *     ...
 *   ICatchNode
 *     IParameterNode
 *       IIdentifierNode "e"
 *       IIdentifierNode "Error"
 *     IBlockNode
 *       ...
 *   ICatchNode
 *     IParameterNode
 *       IIdentifierNode "e"
 *       IIdentifierNode "Error"
 *     IBlockNode
 *       ...
 *   ITerminalNode "finally"
 *     IBlockNode
 *       ...
 * </pre>
 */
public interface ITryNode extends IStatementNode
{
    /**
     * Returns the number of <code>catch</code> clauses for this
     * <code>try</code> statement.
     */
    int getCatchNodeCount();

    /**
     * Returns the <code>i</code>th <code>catch</code> clause for this
     * <code>try</code> statement.
     * 
     * @return an {@link ICatchNode} or <code>null</code>
     */
    ICatchNode getCatchNode(int i);

    /**
     * Returns the <code>finally</code> clause of this <code>try</code>
     * statement.
     * 
     * @return an {@link ITerminalNode} or <code>null</code>
     */
    ITerminalNode getFinallyNode();
}
