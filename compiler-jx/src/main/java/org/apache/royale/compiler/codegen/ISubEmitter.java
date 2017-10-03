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

package org.apache.royale.compiler.codegen;

import org.apache.royale.compiler.tree.as.IASNode;

/**
 * The {@link IEmitter} emitter can use composition for it's more
 * complicated production sequences with member, dynamic, binary expressions and
 * identifiers etc.
 * 
 * @author Michael Schmalle
 */
public interface ISubEmitter<T>
{
    /**
     * The main emitter will call this method of the sub emitter with the
     * correct generic type implemented.
     * <p>
     * The main idea here is abstraction. Producing JavaScript can get
     * complicated, the best way to avoid bugs is to avoid as much state and
     * interdependence between emit() calls of the main emitter.
     * 
     * @param node The current {@link IASNode} being emitted by the
     * {@link IEmitter}.
     */
    void emit(T node);
}
