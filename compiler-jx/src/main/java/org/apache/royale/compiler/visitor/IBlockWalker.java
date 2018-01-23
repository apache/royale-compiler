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

package org.apache.royale.compiler.visitor;

import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public interface IBlockWalker
{

    /**
     * Returns the current {@link ICompilerProject} for the traverse state.
     */
    ICompilerProject getProject();

    /**
     * Traverses an {@link IASNode} based on the semantics of the known node.
     * <p>
     * Typically uses the {@link IASNodeStrategy#handle(IASNode)} to delegate
     * how the node will be traversed.
     * 
     * @param node The {@link IASNode} to traverse using the current strategy
     */
    void walk(IASNode node);

}
