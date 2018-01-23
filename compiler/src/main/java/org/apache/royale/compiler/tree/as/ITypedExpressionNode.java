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
 * An AST node representing a parameterized type such as {@code Vector.<Sprite>}.
 * <p>
 * The shape of this node is:
 * <pre>
 * ITypedExpressionNode
 *   IExpressionNode <-- getTypeNode()
 *   IExpressionNode <-- getCollectionNode()
 * </pre>
 * For example, <pre>Vector.&lt;Sprite&gt;</pre> is represented as
 * <pre>
 * ITypedExpressionNode
 *   IIdentifierNode "Vector"
 *   IIdentifierNode "Sprite"
 * </pre>
 */
public interface ITypedExpressionNode extends IIdentifierNode
{
    /**
     * Returns the node representing the type of collection that this typed
     * collection contains
     * 
     * @return an {@link IExpressionNode} that will always resolve to Vector
     */
    IExpressionNode getCollectionNode();

    /**
     * Returns the node representing the type of this expression, which in AS3
     * will always resolve to Vector
     * 
     * @return an {@link IExpressionNode}
     */
    IExpressionNode getTypeNode();
    
    /**
     * Whether the node contains an actual existing operator location.
     * 
     * @return True if a the <code>.<</code> actually exists in source.
     */
    boolean hasTypedOperator();
}
