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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.royale.compiler.config.ICompilerProblemSettings;
import org.apache.royale.compiler.problems.AbstractSemanticProblem;
import org.apache.royale.compiler.problems.CodegenInternalProblem;
import org.apache.royale.compiler.problems.CodegenProblem;
import org.apache.royale.compiler.problems.CompilerProblemSeverity;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.IParserProblem;
import org.apache.royale.compiler.problems.SemanticWarningProblem;
import org.apache.royale.compiler.problems.StrictSemanticsProblem;
import org.apache.royale.compiler.problems.UnfoundPropertyProblem;
import org.apache.royale.compiler.problems.collections.CompositeProblemFilter;
import org.apache.royale.compiler.problems.collections.FilteredIterator;
import com.google.common.collect.Iterables;

/**
 *  A ProblemQuery presents a higher-level view of a problem
 *  stream; it offers facilities to enable or disable diagnostics
 *  and presents filtered and (optionally) sorted views of the
 *  underlying problem stream.
 */
public class ProblemQuery
{
    /**
     *  Construct a ProblemQuery.
     */
    public ProblemQuery()
    {
        this(null);
    }
    
    /**
     * Construct a ProblemQuery with settings to control how compiler problems
     * are reported.
     *  
     * @param problemSettings configuration settings to control how compiler
     * problems are reported.
     */
    public ProblemQuery(ICompilerProblemSettings problemSettings)
    {
        this.problems = new ArrayList<ICompilerProblem>();

        //  An empty problem set is sorted.
        this.sorted = true;

        //  Start with no restrictions on the problems presented.
        this.problemFilter = new ProblemFilterClassCriteria();
        
        this.problemSettings = problemSettings;
    }

    /** The underlying problem collection. */
    private ArrayList<ICompilerProblem> problems;

    /** 
     *  Set to true when the problem collection 
     *  is known to be in sorted order.
     */
    private boolean sorted;

    /**
     *  Problems being rejected as a class.
     */
    private ProblemFilterClassCriteria problemFilter;

    /**
     * Configuration settings that control how errors are 
     * reported.
     */
    private ICompilerProblemSettings problemSettings;
    
    /**
     * Gets the list of compiler problems, with no filtering or sorting.
     */
    public List<ICompilerProblem> getProblems()
    {
        return problems;
    }
        
    /**
     *  Enable or disable strict semantics mode diagnostics.
     *  @param isStrict - if true, strict semantics mode 
     *    diagnostics will appear in the filtered diagnostics.
     */
    public void setShowStrictSemantics(boolean isStrict)
    {
        setShowProblemByClass(StrictSemanticsProblem.class, isStrict);
    }

    /**
     *  Enable or disable semantic warnings.
     *  @param showWarnings - if true, semantic warnings
     *    will appear in the filtered diagnostics.
     */
    public void setShowWarnings(boolean showWarnings)
    {
        setShowProblemByClass(SemanticWarningProblem.class, showWarnings);
    }

    /**
     *  Enable or disable internal error display.
     *  @param showInternalErrors - if true, internal error
     *    diagnostics will appear in the filtered diagnostics.
     */
    public void setShowInternalErrors(boolean showInternalErrors)
    {
        setShowProblemByClass(CodegenInternalProblem.class,showInternalErrors); 
    }

    /**
     *  Enable or disable display of "problems" that 
     *  are used for compiler internal processing but
     *  have no relevance to the user.
     *  @param showIrrelevantProblems - if true, non-user-relevant
     *    diagnostics will appear in the filtered diagnostics.
     */
    public void setShowIrrelevantProblems(boolean showIrrelevantProblems)
    {
        setShowProblemByClass(UnfoundPropertyProblem.class,showIrrelevantProblems); 
    }

    /**
     *  Enable or disable display of a specific problem class or superclass.
     *  @param problemClass - the problem class/superclass of interest.
     *  @param enable - if true, instances of this problem class 
     *    will appear in the filtered diagnostics.
     */
    public void setShowProblemByClass(Class<? extends ICompilerProblem> problemClass, final boolean enable)
    {
        if ( enable )
        {
            problemFilter.removeRejectedClass(problemClass);
        }
        else
        {
            problemFilter.addRejectedClass(problemClass);
        }
    }

    /**
     *  Add an array of compiler problems to the problems collection.
     *  @param newProblems - the problems to add.
     *  @post the problems collection is marked "not sorted."
     */
    public void addAll(ICompilerProblem[] newProblems)
    {
        addAll(Arrays.asList(newProblems));
    }

    /**
     *  Add a collection of compiler problems to the problems collection.
     *  @param newProblems - the problems to add.
     *  @post the problems collection is marked "not sorted."
     */
    public void addAll(Iterable<ICompilerProblem> newProblems)
    {
        this.sorted = false;
        Iterables.addAll(this.problems, newProblems);
    }

    /**
     *  Add a single problem to the problems collection.
     *  @param problem - the problem to add.
     *  @post the problems collection is marked "not sorted."
     */
    public void add(ICompilerProblem problem)
    {
        this.sorted = false;
        this.problems.add(problem);
    }

    /**
     *   Clear the underlying collection of problems.
     */
    public void clear()
    {
        this.sorted = true;
        this.problems.clear();
    }

    /**
     *  Get an iterator over the set of problems that
     *  are to be reported based on the current settings.
     *  Problem categorization is built into this filter so that problems 
     *  categorized as "ignore" are filtered out.
     *   
     *  @return an Iterable&lt;ICompilerProblem&gt; over the 
     *    subset of problems that are of interest to the client.
     */
    public Iterable<ICompilerProblem> getFilteredProblems()
    {
        IProblemFilter filter = CompositeProblemFilter.and(this.problemFilter, new SkipSemanticCascadesFilter());
        CompilerProblemCategorizer categorizer = new CompilerProblemCategorizer(problemSettings);
     
        CodeGenErrorFilter cgef = new CodeGenErrorFilter();
        if (cgef.hasOtherErrors(problems))
        {
            filter = CompositeProblemFilter.and(filter, cgef);
        }
        
        return getFilteredProblems(CompositeProblemFilter.and(filter, new ErrorsAndWarningsFilter(categorizer)));
    }

    /**
     * Used internally as a base filter without any categorization built in.
     * This is useful for clients that want to do the categorization themselves.
     * Gets an iterator over the set of problems that are to be reported based
     * on the current settings.
     * 
     * @return an Iterable&lt;ICompilerProblem&gt; over the subset of problems
     * that are of interest to the client.
     */
    private Iterable<ICompilerProblem> getFilteredProblemsUsingBaseFilter()
    {
        return getFilteredProblems(CompositeProblemFilter.and(this.problemFilter, new SkipSemanticCascadesFilter()));
    }
    
    /**
     *  Get an iterator over the set of problems that
     *  are to be reported based on the current settings.
     *  
     *  @param filter - the filter to apply.
     *  
     *  @return an Iterable&lt;ICompilerProblem&gt; over the 
     *    subset of problems that are of interest to the client.
     */
    private Iterable<ICompilerProblem> getFilteredProblems(IProblemFilter filter)
    {
        if (problemSettings != null)
            filter = CompositeProblemFilter.and(filter, new ProblemSettingsFilter(problemSettings));
        
        //  Sort the problems so that the semantic cascade
        //  filter can find semantic problems that occur
        //  on the same line as parser problems.
        sortProblems();
        return getProblemView(filter);
    }

    /**
     *  Any problems to report after filtering?
     *  @return true if there are filtered problems.
     */
    public boolean hasFilteredProblems()
    {
        return getFilteredProblems().iterator().hasNext();
    }

    /**
     *  Do any problems match the given filter?
     *  @param filter - the problem filter of interest.
     */
    public boolean hasFilteredProblems(final IProblemFilter filter)
    {
         return getProblemView(filter).iterator().hasNext();
    }

    /**
     *  Get a filtered view of the underlying problems.
     *  @param filter - the filter to apply.
     *  @return an Iterable that supplies a filtered iterator of the problems.
     */
    public Iterable<ICompilerProblem> getProblemView(final IProblemFilter filter)
    {
        return FilteredIterator.getFilteredIterable(problems, filter);
    }

    /**
     *  Get an iterator over all internal errors.
     */
    public Iterable<ICompilerProblem> getInternalErrors()
    {
        return getProblemView(new ProblemFilterClassCriteria(CodegenInternalProblem.class));
    }

    /**
     *  Sort the captured collection of problems so that we can filter out
     *  semantic problems that occur on the same line as parser problems.
     */
    public void sortProblems()
    {
        if ( ! this.sorted )
        {
            Collections.sort( this.problems, compareByPositionAndPhase);
        }

        this.sorted = true;
    }

    /**
     * Categorize the compiler problems into two bins, errors and warning.
     * 
     * @param errors the collection where the errors are added. 
     * @param warnings the collection where the warnings are added. 
     */
    public void getErrorsAndWarnings(Collection<ICompilerProblem> errors, 
            Collection<ICompilerProblem> warnings)
    {
        CompilerProblemCategorizer categorizer = new CompilerProblemCategorizer(problemSettings);
        
        // Get the filtered problems and classify the problems as either errors or 
        // warnings.
        for (ICompilerProblem problem : getFilteredProblemsUsingBaseFilter())
        {
            CompilerProblemSeverity severity = categorizer.getProblemSeverity(problem); 
            if (severity == CompilerProblemSeverity.ERROR)
                errors.add(problem);
            else if (severity == CompilerProblemSeverity.WARNING)
                warnings.add(problem);
        }
        
    }

    /**
     * Test if any of the problems are errors.
     * 
     * return true if any of the problems are errors, false otherwise.
     */
    public boolean hasErrors()
    {
        CompilerProblemCategorizer categorizer = new CompilerProblemCategorizer(problemSettings);
        
        for (ICompilerProblem problem : getFilteredProblemsUsingBaseFilter())
        {
            CompilerProblemSeverity severity = categorizer.getProblemSeverity(problem); 
            if (severity == CompilerProblemSeverity.ERROR)
                return true;
        }

        return false;
    }
    
    /**
     *  This Comparator compares problems based on three criteria:
     *  <li> file path
     *  <li> line number
     *  <li> problem class - IParserProblems are "less than" semantic problems.
     */
    public static final Comparator<ICompilerProblem> compareByPositionAndPhase =
        new Comparator<ICompilerProblem>()
        {
            @Override
            public int compare(ICompilerProblem p1, ICompilerProblem p2)
            {
                int result = compareStrings( p1.getSourcePath(), p2.getSourcePath());

                if ( result == 0 )
                    result = p1.getLine() - p2.getLine();

                if ( result == 0 )
                    result = compareProblemClasses(p1, p2);

                return result;
            }

            /**
             *  Look for configurations of problem classes that 
             *  imply a definite arrangement; problems from the 
             *  syntax analysis phase get ordered before problems
             *  from the semantic analysis phase so that the latter
             *  can be elided.
             */
            private int compareProblemClasses(ICompilerProblem p1, ICompilerProblem p2)
            {
                if ( p1 instanceof IParserProblem )
                {
                    if ( !(p2 instanceof IParserProblem) )
                        return -1;
                }
                else if ( p2 instanceof IParserProblem )
                {
                    if ( !(p1 instanceof IParserProblem) )
                        return 1;
                }

                return 0;
            }
        };


    /**
     *  SkipSemanticCascadesFilter accepts any problem except a 
     *  SemanticProblem or CodegenProblem on a line that has already
     *  reported some kind of parser problem.
     */
    private class SkipSemanticCascadesFilter implements IProblemFilter
    {
        private int lineNumber = -1;
        private String fileName = null;
        private boolean parserProblemOnLine = false;

        @Override
        public boolean accept(ICompilerProblem problem)
        {
            if ( problem.getLine() != lineNumber || compareStrings(this.fileName, problem.getSourcePath()) != 0 )
            {
                this.lineNumber = problem.getLine();
                this.fileName   = problem.getSourcePath();
            }
            else if ( parserProblemOnLine && (problem instanceof AbstractSemanticProblem || problem instanceof CodegenProblem) )
            {
                //  Skip this problem.
                return false;
            }

            this.parserProblemOnLine = problem instanceof IParserProblem;
            return true;
        }

    }

    /**
     *  ErrorsAndWarningsFilter accepts any problem that has a severity
     *  of error or warning.
     */
    private class ErrorsAndWarningsFilter implements IProblemFilter
    {

        ErrorsAndWarningsFilter(CompilerProblemCategorizer categorizer)
        {
            this.categorizer = categorizer;
        }
        
        private final CompilerProblemCategorizer categorizer;
        
        @Override
        public boolean accept(ICompilerProblem problem)
        {
            CompilerProblemSeverity severity = categorizer.getProblemSeverity(problem); 
            if (severity == CompilerProblemSeverity.ERROR ||
                severity == CompilerProblemSeverity.WARNING)
            {
                return true;
            }

            return false;
        }

    }

    /**
     *  Compare two strings, either or both of which may be null.
     *  @param s1 - the first string.
     *  @param s2 - the second string.
     *  @return -1 if s1 is less than s2, either in the string comparison 
     *    sense or because s1 is null and s2 is not; 0 if the strings
     *    compare equal; 1 if s1 is greater than s1, by the reasoning above.
     */
    private static int compareStrings(String s1, String s2)
    {
        if ( s1 == s2 )
            return 0;
        else if ( s1 != null && s2 != null )
            return s1.compareTo(s2);
        else
            //  One or the other is null, but not both.
            return s1 == null? -1: 1;
    }
}
