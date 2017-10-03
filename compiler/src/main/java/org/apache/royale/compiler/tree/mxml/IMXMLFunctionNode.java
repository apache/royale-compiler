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

import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * This AST node represents an MXML <code>&lt;Function&gt;</code> tag.
 * <p>
 * An {@link IMXMLFunctionNode} has no child nodes.
 */
public interface IMXMLFunctionNode extends IMXMLExpressionNode
{
    /**
     * Gets the value of the &lt;Function&gt; tag, which is the function
     * definition that its content refers to.
     * 
     * @param project An {@code ICompilerProject} object.
     * @return An {@code IFunctionDefinition} object representing an ActionScript
     * function definition.
     */
    IFunctionDefinition getValue(ICompilerProject project);
}
