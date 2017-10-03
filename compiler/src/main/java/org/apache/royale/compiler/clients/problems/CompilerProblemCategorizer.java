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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.royale.compiler.config.ICompilerProblemSettings;
import org.apache.royale.compiler.problems.CompilerProblemClassification;
import org.apache.royale.compiler.problems.CompilerProblemSeverity;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.annotations.DefaultSeverity;
import org.apache.royale.compiler.problems.annotations.ProblemClassification;

/**
 * Class used to determine if an {@link ICompilerProblem} should be reported
 * as an error or a warning.
 * 
 * A user can override the default severity of a problem by using the following
 * compiler options:
 *     -error-problems
 *     -warning-problems
 *     -ignore-problems
 */
public class CompilerProblemCategorizer
{

    /**
     * Default constructor.
     * 
     * This categorizer can only consider the default severity of problems.
     */
    public CompilerProblemCategorizer()
    {
        this(null);
    }
    
    /**
     * This categorizer can look at user configured overrides of problem
     * severity, falling back to the default severity.
     * 
     * @param problemSettings configuration settings that effect how problems
     * are categorized. If null, only the default severity of problems will
     * be used.
     */
    public CompilerProblemCategorizer(ICompilerProblemSettings problemSettings)
    {
        super();
        
        if (problemSettings != null)
        {
            addToUserSeverity(problemSettings.getErrorProblems(), CompilerProblemSeverity.ERROR);
            addToUserSeverity(problemSettings.getWarningProblems(), CompilerProblemSeverity.WARNING);
            addToUserSeverity(problemSettings.getIgnoreProblems(), CompilerProblemSeverity.IGNORE);
            showWarnings = problemSettings.showWarnings();
        }
        else
        {
            showWarnings = true;
        }
    }
    
    /**
     * User defined severity to override the default severity.
     */
    private Map<Class<? extends ICompilerProblem>, CompilerProblemSeverity> userSeverity;
    
    /**
     * If false, all problems with a severity of 'warning' will be changed to
     * 'ignore'. See ignoreWarningsIfRequired().
     */
    private final boolean showWarnings;
    
    /**
     * Get the severity of the compiler problem. Check if the user overrode
     * any of the problem severities. If not, return that severity. Otherwise
     * return the default severity.
     * 
     * @param problem the compiler problem. May not be null.
     * @return the severity of the problem.
     * 
     * @throws NullPointerException if problem is null.
     */
    public CompilerProblemSeverity getProblemSeverity(ICompilerProblem problem)
    {
        if (problem == null)
            throw new NullPointerException("problem may not be null");
        
        CompilerProblemSeverity severity = null;
        Class<?> problemClass = problem.getClass();
        
        if (userSeverity != null)
        {
            while (problemClass != null)
            {
                severity = userSeverity.get(problemClass);
                if (severity != null)
                    return ignoreWarningsIfRequired(severity);

                problemClass = problemClass.getSuperclass();
            }
        }

        // get the default severity
        DefaultSeverity defaultSeverity = problem.getClass().getAnnotation(DefaultSeverity.class);
        assert defaultSeverity != null;
        
        if (defaultSeverity != null)
            return ignoreWarningsIfRequired(defaultSeverity.value());
        
        return CompilerProblemSeverity.ERROR;
    }
    
    
    public CompilerProblemClassification getProblemClassification(ICompilerProblem problem)
    {
        if (problem == null)
            throw new NullPointerException("problem may not be null");
        
        // get the classification from the problem annotation
        ProblemClassification classification  = problem.getClass().getAnnotation(ProblemClassification.class);
        assert classification != null;
        
        CompilerProblemClassification ret = CompilerProblemClassification.DEFAULT;
        if (classification != null)
        {
           ret = classification.value();
        }
        
        return ret;
    }
    
    /**
     * Ignore a warning if we are not supposed to show warnings.
     * 
     * @param severity
     * @return if showWarnings is true return input severity. 
     * If showWarnings is false then if the severity is 'warning' 
     * then 'ignore' is returned.
     */
    private CompilerProblemSeverity ignoreWarningsIfRequired(CompilerProblemSeverity severity)
    {
        if (!showWarnings && severity == CompilerProblemSeverity.WARNING)
        {
            return CompilerProblemSeverity.IGNORE;
        }
        
        return severity;
    }
    
    /**
     * Add user defined severity to a collection that overrides the default 
     * severity.
     * 
     * @param problems
     * @param severity
     */
    private void addToUserSeverity(Collection<Class<ICompilerProblem>> problems, 
            CompilerProblemSeverity severity)
    {
        if (userSeverity == null)
            userSeverity = new HashMap<Class<? extends ICompilerProblem>, CompilerProblemSeverity>();

        for (Class<ICompilerProblem> problem : problems)
        {
            userSeverity.put(problem, severity);
        }
    }
}
