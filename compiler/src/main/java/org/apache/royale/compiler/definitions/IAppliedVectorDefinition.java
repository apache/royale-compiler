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

package org.apache.royale.compiler.definitions;

import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * A definition representing a specialized vector type.
 * <p>
 * Examples of vector types are <code>Vector.&lt;int&gt;</code>
 * or <code>Vector.&lt;<code>Vector.&lt;flash.events.IEventDispatcher&gt;</code>&gt;</code>.
 * <p>
 * Vector types are not declared; they come into existence simply by being used.
 */
public interface IAppliedVectorDefinition extends IClassDefinition
{
    /**
     * Resolves the class or interface definition that represents
     * the type of the vector's elements.
     * 
     * @param project The {@link ICompilerProject} to use for resolving references.
     * @return An {@link ITypeDefinition} for the class or interface definition
     * representing the type of the vector's elements.
     */
    ITypeDefinition resolveElementType(ICompilerProject project);
}
