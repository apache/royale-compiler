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
 * An AST node representing a <code>break</code>, <code>continue</code>,
 * or <code>goto</code> statement.
 * <p>
 * The shape of this node is:
 * <pre>
 * IIterationFlowNode 
 *   IIdentifierNode <-- getLabelNode()
 * </pre>
 * For example, <pre>continue label1;</pre> is represented as
 * <pre>
 * IIterationFlowNode "continue"
 *   IIdentifierNode "label1"
 * </pre>
 * If there is no label, the child node is not present.
 */
public interface IIterationFlowNode extends IASNode
{
    /**
     * An IterationFlowKind represents a kind of iteration control statement
     */
    enum IterationFlowKind
    {
        /**
         * Represents a continue statement
         */
        CONTINUE,

        /**
         * Represents a break statement
         */
        BREAK,

        /**
         * Represents a goto statement
         */
        GOTO
    }

    /**
     * Represents an optional label on the IIterationFlowNode
     * 
     * @return an {@link IIdentifierNode} or null
     */
    IIdentifierNode getLabelNode();

    /**
     * Represents an {@link IterationFlowKind}
     * 
     * @return a {@link IterationFlowKind}
     */
    IterationFlowKind getKind();
}
