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

import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * An AST node representing a function type expression.
 * <p>
 * Function type expressions must have parentheses, a "fat" arrow (=>) and
 * a return type. All parameters within parentheses must have names, but
 * parameter types are optional (defaulting to the any (*) type).
 */
public interface IFunctionTypeExpressionNode extends IExpressionNode
{
    /**
     * Returns the container for parameters of this function
     */
	ContainerNode getParametersContainerNode();

	/**
     * Get the parameters of this function as an array of {@link IParameterNode}
     * elements
     * 
     * @return the parameters of this function
     */
	IParameterNode[] getParameterNodes();

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
     * Returns the signature of this function as it exist in source.
     * 
     * @return signature of function as seen in source
     */
    public String resolveSignature(ICompilerProject project);
}
