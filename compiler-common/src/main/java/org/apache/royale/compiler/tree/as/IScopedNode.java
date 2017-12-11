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

import java.util.Collection;

import org.apache.royale.compiler.scopes.IASScope;

/**
 * An AST node that has an attached scope.
 * <p>
 * Note that only nodes that actually create a new scope implement this interface.
 * Some nodes, like IBlockNode, may not generate a new scope,
 * so they do not implement this.
 */
public interface IScopedNode extends IASNode
{
    /**
     * Get the scope associated with this node as an IASScope
     * 
     * @return scope associated with this node
     */
    IASScope getScope();

    /**
     * Returns all the imports that are relevant in the current scope. This will
     * walk up the scope chain. If we are in a package context, this will avoid
     * adding imports from the a File scope.
     * 
     * @param imports an array of Strings representing all the imports
     */
    // TODO Make this return the collection.
    void getAllImports(Collection<String> imports);

    /**
     * Returns all the imports that are relevant in the current scope. This will
     * walk up the scope chain. If we are in a package context, this will avoid
     * adding imports from the a File scope.
     * 
     * @param imports an array of {@link IImportNode} representing all the
     * imports
     */
    // TODO Make this return the collection.
    void getAllImportNodes(Collection<IImportNode> imports);
}
