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

import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;

/**
 * This AST node represents an MXML <code>&lt;Class&gt;</code> tag.
 * <p>
 * An {@link IMXMLClassNode} has exactly one child node: an
 * {@link IExpressionNode} representing a <code>Class</code> value. It will be
 * either an {@link ILiteralNode} or an {@link IMXMLDataBindingNode}.
 */
public interface IMXMLClassNode extends IMXMLExpressionNode
{
    /**
     * Gets the value of the <code>&lt;Class&gt;</code> tag, which is the class
     * or interface that its content specifies.
     * 
     * @param project An {@link ICompilerProject} object.
     * @return An {@link ITypeDefinition} object representing an ActionScript
     * class or interface definition.
     */
    ITypeDefinition getValue(ICompilerProject project);
}
