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
 * An AST node representing a <code>else</code> clause in an <code>if</code> statement,
 * a <code>default</code> clause</code> in a <code>switch</code> statement,
 * or a <code>finally</code> clause in a <code>try</code> statement.
 * <p>
 * The shape of this node is:
 * <pre>
 * ITerminalNode 
 *   IBlockNode <-- getStatementContentsNode()
 * </pre>
 * Example 1:
 * <pre>
 * if (flag)
 *   doThis();
 * else
 *   doThat();
 * }
 * </pre>
 * is represented as
 * <pre>
 * IIfNode
 *   IConditionalNode
 *     IIdentifierNode "flag"
 *     IBlockNode
 *       IFunctionCallNode
 *         ...
 *   ITerminalNode "else"
 *     IBlockNode
 *       IFunctionCallNode
 *         ...
 * </pre>
 * Example 2:
 * <pre>
 * switch(i)
 * {
 *     case 0:
 *       return a;
 *     default:
 *       return b;
 * }
 * </pre>
 * is represented as
 * <pre>
 * ISwitchNode
 *   IIdentifierNode "i"
 *   IBlockNode
 *     IConditionalNode
 *       INumericLiteralNode 0
 *       IBlockNode
 *         IReturnNode
 *           IIdentifierNode "a"
 *     ITerminalNode "default"
 *       IBlockNode
 *         IReturnNode
 *           IIdentifierNode "b"
 * </pre>
 * Example 3:
 * <pre>
 * finally
 * {
 *     ...
 * }
 * </pre>
 * is represented as
 * <pre>
 * ITerminalNode "finally"
 *   IBlockNode
 *     ...
 * </pre>
 */
public interface ITerminalNode extends IStatementNode
{
    /**
     * Represents a kind of terminal node
     */
    enum TerminalKind
    {
        /**
         * A default clause from a switch node
         */
        DEFAULT,
        
        /**
         * An else clause
         */
        ELSE,
        
        /**
         * a finally clause from a try/catch/finally
         */
        FINALLY
    }

    /**
     * Represents the kind of the terminal condition
     * 
     * @return the {@link TerminalKind}
     */
    TerminalKind getKind();
}
