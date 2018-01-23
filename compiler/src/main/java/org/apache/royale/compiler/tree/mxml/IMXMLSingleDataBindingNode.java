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
 * This AST node represents a single databinding expression (such as
 * <code>{name.first}</code>) in an MXML tree.
 * <p>
 * It has exactly one child: an {@link IExpressionNode} representing the
 * ActionScript expression within the curly braces.
 */
public interface IMXMLSingleDataBindingNode extends IMXMLDataBindingNode
{
    /**
     * Gets the sole child of this node, which represents the expression within
     * the databinding.
     * 
     * @return An {@link IExpressionNode} representing the expression.
     */
    IExpressionNode getExpressionNode();
}
