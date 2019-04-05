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

import java.util.List;

import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition.FunctionClassification;

/**
 * An AST node representing a <code>function</code> declaration,
 * either for a regular function, a getter, or a setter.
 * <p>
 * The shape of this node is:
 * <pre>
 * IFunctionNode
 *   IMetaTagsNode            <-- getMetaTagsNode()
 *   INamespaceDecorationNode <-- getNamespaceNode()
 *   IKeywordNode             <-- getKeywordNode()
 *   IKeywordNode             <-- getAccessorKeywordNode()
 *   IExpressionNode          <-- getNameExpressionNode()
 *   IContainerNode           <-- getParametersContainerNode()
 *   IExpressionNode          <-- getReturnTypeNode()
 *   IScopedNode              <-- getScopedNode()
 * </pre>
 * For example,
 * <pre>
 * [Foo]
 * [Bar]
 * public function f(i:int, j:int = 0):void
 * {
 *     ...
 * }
 * </pre>
 * is represented as
 * <pre>
 * IVariableNode
 *   IMetaTagsNode
 *     IMetaTagNode "Foo"
 *     IMetaTagNode "Bar"
 *   INamespaceDecorationNode "public"
 *   IKeywordNode "function"
 *   IIdentifierNode "f"
 *   IContainerNode
 *     IParameterNode
 *       IIdentifierNode "a"
 *       IIdentifierNode "*"
 *     IParameterNode
 *       IIdentifierNode "b"
 *       IIdentifierNode "int"
 *       INumericLiteralNode 0
 *    IIdentifierNode "void"
 *   IScopedNode
 *     ...
 * </pre>
 * If there is no metadata, the corresponding child node is not present.
 * <p>
 * If there is no namespace, the corresponding child node is not present.
 * <p>
 * For a getter or setter, there are two child nodes which are <coded>IKeywordNode</code>,
 * one for <code>"function"</code> and one for <code>"get"</code> or <code>"set"</code>.
 * For a plain function, the second keyword node is not present.
 * <p>
 * If there are no parameters, there is an empty child <code>IContainerNode</code>.
 * <p>
 * If there is no return type, the corresponding child node is not present.
 */
public interface IFunctionNode extends IScopedDefinitionNode, IDocumentableDefinitionNode
{
    /**
     * Get the parameters of this function as an array of {@link IParameterNode}
     * elements
     * 
     * @return the parameters of this function
     */
    IParameterNode[] getParameterNodes();

    /**
     * Returns the container for parameters of this function
     */
    IContainerNode getParametersContainerNode();

    /**
     * Returns the type of this function as it exist in source. If a function
     * does not have an explicit type, <code>void</code> is returned
     * 
     * @return type of function as seen in source
     */
    String getReturnType();

    /**
     * Returns the {@link IExpressionNode} that corresponds to the return type
     * node of this {@link IFunctionNode}
     * 
     * @return an {@link IExpressionNode} or null
     */
    IExpressionNode getReturnTypeNode();

    /**
     * Is this a constructor?
     * 
     * @return true if the member is a constructor
     */
    boolean isConstructor();

    /**
     * Is this a cast function?
     * 
     * @return true if the member is a cast function
     */
    boolean isCastFunction();

    /**
     * Get the classification for this function (local, argument, class member,
     * etc)
     * 
     * @return function classification
     */
    FunctionClassification getFunctionClassification();

    /**
     * Is this function a getter?
     * 
     * @return true if the function is a getter
     */
    boolean isGetter();

    /**
     * Is this function a setter?
     * 
     * @return true if the function is a setter
     */
    boolean isSetter();

    @Override
    IFunctionDefinition getDefinition();

    /**
     * Does this function have a non-empty body
     */
    boolean hasBody();
    
    /**
     * Does this function have a local functions within
     */
    boolean containsLocalFunctions();
    
    /**
     * Get local functions within
     */
    List<IFunctionNode> getLocalFunctions();
    
    /**
     * Remember local Functions in this function node().
     * JS codegen needs to know about them.
     */
    void rememberLocalFunction(IFunctionNode localFunction);
}
