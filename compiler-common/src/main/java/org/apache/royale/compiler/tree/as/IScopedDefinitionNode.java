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
 * An AST node representing a declaration with a contained scope,
 * as for <code>package</code>, <code>class</code>,
 * <code>interface</code>, and <code>function</code> declarations.
 */
public interface IScopedDefinitionNode extends IDefinitionNode
{
    /**
     * Returns the scope that is contained with this definition. The scope will
     * contain all the children of this definition
     * 
     * @return the scope within this {@link IDefinitionNode}
     */
    IScopedNode getScopedNode();
}
