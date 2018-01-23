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

package org.apache.royale.compiler.clients.problems;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.IOperandStackUnderflowProblem;

/**
 * A problem filter that implements filtering out certain errors from
 * code gen, like StackUnderflow.  These errors are often a result
 * of some other error already reported.  So, the compiler should
 * only report these errors if there are no other errors to report.
 * 
 */
public class CodeGenErrorFilter implements IProblemFilter
{

    /**
     * Create a filter to exclude code gen errors.
     */
    public CodeGenErrorFilter()
    {
    }
        
    @Override
    public boolean accept(ICompilerProblem p)
    {
        if (p instanceof IOperandStackUnderflowProblem)
            return false;
        
        // accept it.
        return true;
    }

    public boolean hasOtherErrors(Iterable<ICompilerProblem> problems)
    {
        for (ICompilerProblem problem : problems)
        {
            if (!(problem instanceof IOperandStackUnderflowProblem))
                return true;
        }
        return false;
    }
}
