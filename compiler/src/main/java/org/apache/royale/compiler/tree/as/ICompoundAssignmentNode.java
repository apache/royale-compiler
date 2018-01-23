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

import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * An AST node representing one of the 13 compound assignment binary operators.
 * <p>
 * These are:
 * <ul>
 * <li><code>+=</code></li>
 * <li><code>-=</code></li>
 * <li><code>*=</code></li>
 * <li><code>/=</code></li>
 * <li><code>%=</code></li>
 * <li><code>&=</code></li>
 * <li><code>|=</code></li>
 * <li><code>^=</code></li>
 * <li><code>&&=</code></li>
 * <li><code>||=</code></li>
 * <li><code>&lt;&lt;=</code></li>
 * <li><code>&gt;&gt;=</code></li>
 * <li><code>&gt;&gt;&gt;=</code></li>
 * </ul>
 * <p>
 * The typical shape of this node is:
 * <pre>
 * ICompoundAssignmentNode 
 *   IExpressionNode <-- getLeftOperandNode()
 *   IExpressionNode <-- getRightOperandNode()
 * </pre>
 * For example, <code>a += b</code> is represented as
 * <pre>
 * ICompoundAssignmentNode "+="
 *   IIdentifierNode "a"
 *   IIdentifierNode "b"
 * </pre>
 */
public interface ICompoundAssignmentNode
{
    /**
     * Determines the compile-time type of the r-value
     * of the assignment.
     * <p>
     * For example, for <code>a += b</code> this is the type
     * of <code>a + b</code>.
     * 
     * @param project The {@link ICompilerProject} to use to do lookups.
     * @return The {@link ITypeDefinition} of the type of the r-value.
     */
    ITypeDefinition resolveTypeOfRValue(ICompilerProject project);
}
