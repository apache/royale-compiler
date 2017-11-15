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

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * A printer utility class for {@link ICompilerProblem}s.
 */
public final class ProblemPrinter
{
    public ProblemPrinter(ProblemFormatter formatter, OutputStream outputStream)
    {
        this.formatter = formatter;
        this.outputStream = outputStream;
    }
    
    public ProblemPrinter(ProblemFormatter formatter)
    {
        this(formatter, System.err);
    }
    
    private ProblemFormatter formatter;

    private OutputStream outputStream;

    /**
     * Print problems and return number of problems printed after applying the
     * filter.
     * 
     * @param problems compiler problems
     * @param filter problem filter
     * @return count of printed problems
     */
    public int printProblems(final Collection<ICompilerProblem> problems, IProblemFilter filter)
    {
        if (problems == null || problems.isEmpty())
            return 0;

        List<ICompilerProblem> filteredProblems = new ArrayList<ICompilerProblem>();
        for (final ICompilerProblem problem : problems)
        {
            if ((filter == null) || (filter.accept(problem)))
            {
                filteredProblems.add(problem);
            }
        }
        
        return printProblems(filteredProblems);
    }

    /**
     *  Print a pre-filtered sequence of problems.
     *  
     *  @param problems compiler problems
     *  @return count of printed problems
     */
    public int printProblems(final Iterable<ICompilerProblem> problems)
    {
        int problemCount = 0;

        PrintWriter writer = new PrintWriter(new PrintStream(outputStream));
        
        for (final ICompilerProblem problem : problems)
        {
            writer.println(formatter.format(problem));
            problemCount++;
        }

        writer.flush();
        return problemCount;
    }

}
