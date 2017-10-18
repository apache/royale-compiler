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

package org.apache.royale.compiler.internal.tree.as;

import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.as.IDefinitionNode;

/**
 * AST node for a definition that can have a initializer. Such as variable
 * definition ({@code var x = 100}), namespace definition (
 * {@code namespace ns = "foo"}).
 * <p>
 * This interface provides a mutable view of the implementing AST node, so it is
 * hidden in the internal package.
 */
public interface IInitializableDefinitionNode extends IDefinitionNode
{
    /**
     * Set the assigned value. Used during parsing.
     * 
     * @param eq ASToken containing the equals operator {@code =}.
     * @param value node containing the assigned value
     */
    void setAssignedValue(IASToken eq, ExpressionNodeBase value);
}
