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
 * An AST node representing a name/value pair in a <code>Object</code> literal.
 * <p>
 * The typical shape of this node is:
 * <pre>
 * IObjectLiteralValuePairNode 
 *   IExpressionNode <-- getNameNode()
 *   IExpressionNode <-- getValueNode()
 * </pre>
 * For example, <code>{ a: 1, b: 2 }</code> is represented as
 * <pre>
 * ILiteralNode "Object"
 *   IObjectLiteralValuePairNode
 *     IIdentifierNode "a"
 *     INumericaLiteralNode 1
 *   IObjectLiteralValuePairNode
 *     IIdentifierNode "b"
 *     INumericaLiteralNode 2
 * </pre>
 */
public interface IObjectLiteralValuePairNode
{
    /**
     * Represents the name part of a ObjectLiteralValuePair.
     * It will be either an ILiteralNode of type String or an IIdentifierNode.
     * 
     * @return an {@link IExpressionNode} or null
     */
    IExpressionNode getNameNode();

    /**
     * Represents the value part of a ObjectLiteralValuePair
     * 
     * @return an {@link IExpressionNode} or null
     */
    IExpressionNode getValueNode();
}
