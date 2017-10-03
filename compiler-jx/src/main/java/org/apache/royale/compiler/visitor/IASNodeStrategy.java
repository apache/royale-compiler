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

import org.apache.royale.compiler.tree.as.IASNode;

/**
 * A simple strategy to allow composition of {@link IASNode} handling when
 * walking the AST node tree.
 * 
 * @author Michael Schmalle
 */
public interface IASNodeStrategy
{
    /**
     * The strategy will handle the specific {@link IASNode}.
     * 
     * @param node The {@link IASNode} to handle.
     */
    void handle(IASNode node);
}
