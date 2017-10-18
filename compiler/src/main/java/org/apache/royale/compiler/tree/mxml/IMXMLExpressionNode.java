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

import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;

/**
 * This is the base interface for AST nodes representing
 * <code>&lt;Boolean&gt;</code>, <code>&lt;int&gt;</code>,
 * <code>&lt;uint&gt;</code>, <code>&lt;Number&gt;</code>,
 * <code>&lt;String&gt;</code>, and <code>&lt;Class&gt;</code> tags. These AST
 * nodes represent a primitive ActionScript value.
 */
public interface IMXMLExpressionNode extends IMXMLInstanceNode
{
    /**
     * Gets the value of the node as an {@link IExpressionNode}. In a primitive
     * case such as <String>abc</String> the value will be an
     * {@link ILiteralNode}. In a databinding case such as
     * <String>{employee.name}</String> the value will be an
     * {@link IMXMLDataBindingNode}. In a mixed case such as
     * <String>Hello, {name}</String> the value will be an
     * {@link IMXMLConcatenatedDataBindingNode}.
     * 
     * @return An {@link IExpressionNode} representing the value of the tag.
     */
    IASNode getExpressionNode();
}
