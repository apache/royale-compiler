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

package org.apache.royale.compiler.units.requests;

import java.util.Collection;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;

public interface IFileScopeRequestResult extends IRequestResult
{
    /**
     * @return The root IASScope instances for this CompilationUnit.
     * For a compilation unit that processes a source file,
     * the array will contain a single ASFileScope.
     * For a compilation unit that processes ABC,
     * the array will contain one ASFileScope for each "script" in the ABC.
     */
    IASScope[] getScopes();
    
    /**
     * @return The externally-visible definition in this compilation unit
     * whose fully-qualified name matches the filepath for the compilation unit,
     * (if one was specified), or null if no such definition exists.
     */
    IDefinition getMainDefinition(String qname);
    
    /**
     * @return all the externally visible definitions in this file scope.
     */
    Collection<IDefinition> getExternallyVisibleDefinitions();
    
    /**
     * Create compiler problems if the a definition with the specified dotted
     * qualified name is not defined in the {@link IASScope}s in this result
     * object or if there is an externally visible definition other than the
     * named definition.
     * 
     * @param dottedQName The dotted fully qualified definition name of the definition that should
     * be the only definition in the {@link IASScope}s in this result object.
     * @return {@link Collection} of {@link ICompilerProblem}s
     */
    Collection<ICompilerProblem> checkExternallyVisibleDefinitions(String dottedQName);
}
