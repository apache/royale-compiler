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

package org.apache.royale.compiler.internal.units;

import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority.BasePriority;
import org.apache.royale.compiler.internal.units.requests.ABCBytesRequestResult;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;

/**
 * A compilation unit for AS files that are imported into ASC using the
 * -import option.
 * 
 * This compilation unit differs from the ASCompilationUnit in that it doesn't
 * need to generate abc bytes or provide outgoing dependencies.
 */
public class ImportedASCompilationUnit extends ASCompilationUnit
{

    public ImportedASCompilationUnit(CompilerProject project, String path, BasePriority basePriority)
    {
        super(project, path, basePriority);
    }

    @Override
    protected IABCBytesRequestResult handleABCBytesRequest() throws InterruptedException
    {
        startProfile(Operation.GET_ABC_BYTES);
        IABCBytesRequestResult result = new ABCBytesRequestResult();
        stopProfile(Operation.GET_ABC_BYTES);
        return result;
    }

    @Override
    protected IOutgoingDependenciesRequestResult handleOutgoingDependenciesRequest() throws InterruptedException
    {
        startProfile(Operation.GET_SEMANTIC_PROBLEMS);
        IOutgoingDependenciesRequestResult result = new IOutgoingDependenciesRequestResult()
        {
            @Override
            public ICompilerProblem[] getProblems()
            {
                return IABCBytesRequestResult.ZEROPROBLEMS;
            }
        };
        stopProfile(Operation.GET_SEMANTIC_PROBLEMS);

        return result;
    }
    
}
