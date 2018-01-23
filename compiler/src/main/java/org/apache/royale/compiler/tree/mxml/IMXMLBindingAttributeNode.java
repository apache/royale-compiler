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

package org.apache.royale.compiler.tree.mxml;

import org.apache.royale.compiler.tree.as.IExpressionNode;

/**
 * This AST node represents the <code>source</code> or <code>destination</code>
 * attribute on an MXML <code>&lt;Binding&gt;</code> tag.
 * <p>
 * It has one child node, which is an {@link IExpressionNode} representing the
 * binding expression that is the attribute value.
 */
public interface IMXMLBindingAttributeNode extends IMXMLNode
{
    /**
     * Gets the sole child of this node, which represents a binding expression.
     * 
     * @return An {@link IExpressionNode} representing a binding expression.
     */
    IExpressionNode getExpressionNode();
}
