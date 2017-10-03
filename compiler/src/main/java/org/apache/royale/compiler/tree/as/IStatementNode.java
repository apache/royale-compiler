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
 * An AST node representing a statement or clause
 * that contains a block of other statements.
 * <p>
 * These are:
 * <ul>
 * <li><code>if</code>, <code>else if</code>, and <code>else</code></li>
 * <li><code>for</code>, <code>for</code>-<code>in</code>,
 * and <code>for</code>-<code>each</code>-<code>in</code></li>
 * <li><code>while</code> and <code>do</code>-<code>while</code></li>
 * <li><code>switch</code>, <code>case</code>, and <code>default</code></li>
 * <li><code>try</code>, <code>catch</code>, and <code>finally</code></li>
 * <li><code>with</code></li>
 * </ul>
 */
public interface IStatementNode extends IASNode
{
    /**
     * returns the contents contained with the statement
     * 
     * @return an {@link IScopedNode}
     */
    IASNode getStatementContentsNode();
}
