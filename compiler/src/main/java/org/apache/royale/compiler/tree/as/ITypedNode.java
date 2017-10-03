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
 * An AST node representing a declaration that can have a type annotation.
 * <p>
 * The relevant subinterfaces are {@link IVariableNode}, {@link IFunctionNode},
 * and {@link IParameterNode}.
 */
public interface ITypedNode extends IDefinitionNode
{
    /**
     * Returns true if this item has an associated type. Types are optional in
     * AS3.
     * 
     * @return true if we have a type
     */
    boolean hasTypeOperator();

    /**
     * Returns the start offset of the type operator, which in AS3 is the ':'
     * char.
     * 
     * @return the start offset of the type operator
     */
    int getTypeOperatorStart();

    /**
     * Returns the end offset of the type operator, which in AS3 is the ':'
     * char.
     * 
     * @return the end offset of the type operator
     */
    int getTypeOperatorEnd();

    /**
     * Returns the child node that contains the type information for this node.
     * 
     * @return An {@link IExpressionNode} or <code>null</code>.
     */
    IExpressionNode getTypeNode();
}
