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
 * An AST node representing a parameter in a <code>function</code> declaration.
 * <p>
 * The typical shape of this node is:
 * <pre>
 * IParameterNode 
 *   IExpressionNode <-- getNameExpressionNode()
 *   IExpressionNode <-- getVariableTypeNode()
 *   IExpressionNode <-- getAssignedValueNode()
 * </pre>
 * If there is no type annotation, an implicit <code>IIdentifierNode</code>
 * for type <code>"*"</code> is present.
 * <p>
 * If there is no initial value, then the corresponding child node is not present.
 * <p>
 * For example,
 * <pre>
 * public function f(a, b:int = 0):void
 * {
 * }
 * </pre>
 * is represented as
 * <pre>
 * IFunctionNode
 *   IIdentifierNode "f"
 *   IContainerNode
 *     IParameterNode
 *       IIdentifierNode "a"
 *       IIdentifierNode "*"
 *     IParameterNode
 *       IIdentifierNode "b"
 *       IIdentifierNode "int"
 *       INumericLiteralNode 0
 *   IIdentiferNode "void"
 * </pre>
 */
public interface IParameterNode extends IVariableNode
{
    /**
     * Returns the optional default value specified by the default value passed
     * into an argument
     * 
     * @return the default value, or null
     */
    String getDefaultValue();

    /**
     * Returns true if this argument is a rest-style argument, signified by ...
     * 
     * @return true if we are a restful argument
     */
    boolean isRest();

    boolean hasDefaultValue();
}
