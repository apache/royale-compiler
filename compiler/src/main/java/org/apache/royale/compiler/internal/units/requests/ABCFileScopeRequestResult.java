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

package org.apache.royale.compiler.internal.units.requests;

import java.util.Collection;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.units.ABCCompilationUnit;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;

/**
 * {@link FileScopeRequestResultBase} for {@link ABCCompilationUnit}.
 */
public class ABCFileScopeRequestResult extends FileScopeRequestResultBase
{

    /**
     * {@link ABCCompilationUnit} allows multiple public visible definitions.
     * 
     * @param problems compiler problems
     * @param scopes file scope
     */
    public ABCFileScopeRequestResult(Collection<ICompilerProblem> problems, Collection<IASScope> scopes)
    {
        super(problems, scopes);
    }

    /**
     * ABC compilation unit can have multiple public definitions in a script, so
     * it doesn't know which one is the "main" definition. We need to manually
     * iterate over all the public definitions to find the match.
     */
    @Override
    public IDefinition getMainDefinition(String qname)
    {
        for (final IDefinition def : definitions)
        {
            if (qname.equals(def.getQualifiedName()))
            {
                return def;
            }
        }
        return null;
    }

}
