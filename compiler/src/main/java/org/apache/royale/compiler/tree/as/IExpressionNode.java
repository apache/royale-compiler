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

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * An AST node representing any kind of expression.
 * <p>
 * Expressions include literal values, identifiers, and operator expressions.
 * See {@link ILiteralNode}, {@link IIdentifierNode}, and {@link IOperatorNode}.
 */
public interface IExpressionNode extends IASNode
{
    /**
     * The type of the result of this expression. This method returns the IDefinition
     * representing the type that is the result of this expression. This can
     * differ from the IDefinition you get from resolve(), such as when called on
     * a FunctionCallNode: resolve() will return the IDefinition for the Function,
     * whereas resolveType() will return the return type of the Function.
     * 
     * @param project The {@link ICompilerProject} to use to do lookups.
     * @return The {@link ITypeDefinition} of the type this expression results in.
     */
    ITypeDefinition resolveType(ICompilerProject project);

    /**
     * If this expression is statically known to refer to a definition return a
     * reference to that definition. In many cases this will return the same
     * definition as resolveType, but when an identifier resolves to an member
     * variable or member function of a class, this method will return the
     * IDefinition for the member variable or member function not the definition
     * for the type of the member variable or the member function.
     * 
     * @param project The {@link ICompilerProject} to use to do lookups.
     * @return The {@link IDefinition} this expression refers to.
     */
    IDefinition resolve(ICompilerProject project);

    /**
     * Does this expression evaluate to a dynamic value
     * 
     * @param project The {@link ICompilerProject} to use to do lookups.
     * @return <code>true</code> if we evaluate to something dynamic
     */
    boolean isDynamicExpression(ICompilerProject project);

    /**
     * Determines whether this expression is surrounded by parenthesis. Nested
     * parenthesis are reduced to the smallest number needed to maintain code
     * meaning
     * 
     * @return <code>true</code> if surrounded by parenthesis
     */
    boolean hasParenthesis();

    /**
     * Return a copy of this IExpressionNode and all it's children that can be used
     * by VariableDefinition to resolve it's initial value without having to hold onto
     * the entire syntax tree for a source file.
     *
     * Not all expressions will be copy-able - e.g. a function expression won't be successfully
     * copied, but that is OK because we can't constant evaluate a function expression anyways.
     *
     * @return  a new {@link IExpressionNode} that is not tied to the syntax tree it came
     * from. If a copy can not be generated, <code>null</code> is returned.
     */
    IExpressionNode copyForInitializer(IScopedNode scopeNode);
}
