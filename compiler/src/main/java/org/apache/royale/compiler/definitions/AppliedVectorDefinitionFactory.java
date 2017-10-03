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

import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * A factory for creating <code>Vector</code> types as they are encountered.
 * <p>
 * For example, when a variable is declared with type {@code Vector.<Whatever>},
 * a new <code>IAppliedVectorDefinition</code> is created to represent
 * this kind of <code>Vector</code>.
 */
public class AppliedVectorDefinitionFactory
{
    public static IAppliedVectorDefinition newVector(ICompilerProject project, ITypeDefinition elementType)
    {
        return ((CompilerProject)project).getScope().newVectorClass(elementType);
    }
}
