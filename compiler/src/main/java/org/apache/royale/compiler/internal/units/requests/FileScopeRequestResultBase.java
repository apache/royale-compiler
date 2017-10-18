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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;

/**
 * Base implementation of an {@link IFileScopeRequestResult}.
 */
public class FileScopeRequestResultBase implements IFileScopeRequestResult
{
    private static IASScope[] EMPTY_SCOPES = new IASScope[0];
    private static ASFileScope[] EMPTY_FILE_SCOPES = new ASFileScope[0];
    
    private static Collection<ICompilerProblem> getProblemCollection(Collection<ICompilerProblem> problems)
    {
        if ((problems != null) && (!(problems.isEmpty())))
            return problems;
        return Collections.emptyList();
    }

    private static IASScope[] getScopesArray(Collection<? extends IASScope> scopes)
    {
        if ((scopes == null) || scopes.isEmpty())
            return EMPTY_SCOPES;

        return scopes.toArray(new IASScope[scopes.size()]);
    }

    private static ASFileScope[] getFileScopesArray(Collection<? extends IASScope> scopes)
    {
        if ((scopes == null) || scopes.isEmpty())
            return EMPTY_FILE_SCOPES;

        List<ASFileScope> fileScopes = new LinkedList<ASFileScope>();
        for (IASScope scope : scopes)
        {
            if (scope instanceof ASFileScope)
                fileScopes.add((ASFileScope)scope);
        }

        return fileScopes.toArray(new ASFileScope[fileScopes.size()]);
    }

    /**
     * Create an immutable {@link IFileScopeRequestResult} object.
     * 
     * @param problems All the compiler problems will be stored in this
     * collection.
     * @param scopes Top-level scopes in this request result. The public
     * definitions in the scopes will be collected and stored in field
     * {@code definitions}.
     */
    public FileScopeRequestResultBase(Collection<ICompilerProblem> problems,
                                      Collection<? extends IASScope> scopes)
    {
        this.problems = getProblemCollection(problems);
        this.scopes = getScopesArray(scopes);
        this.fileScopes = getFileScopesArray(scopes);
        this.definitions = new HashSet<IDefinition>();

        for (final ASFileScope scope : this.fileScopes)
            scope.collectExternallyVisibleDefinitions(definitions, false);
    }

    private Collection<ICompilerProblem> problems;
    private final IASScope[] scopes;
    private final ASFileScope[] fileScopes;

    /** All the public definitions in the given file scope. */
    protected final Collection<IDefinition> definitions;

    @Override
    public ICompilerProblem[] getProblems()
    {
        return problems.toArray(new ICompilerProblem[problems.size()]);
    }

    @Override
    public IASScope[] getScopes()
    {
        return scopes;
    }

    public ASFileScope[] getFileScopes()
    {
        return fileScopes;
    }

    @Override
    public IDefinition getMainDefinition(String qname)
    {
        return null;
    }

    /**
     * Get all the public definitions.
     * @return public definitions
     */
    @Override
    public Collection<IDefinition> getExternallyVisibleDefinitions()
    {
        return definitions;
    }
    
    /**
     * This method allows sub-classes to add problems to the problems collection
     * after running this classes constructor.
     * @param newProblems Collection of {@link ICompilerProblem}'s to add
     * to the problems list for this result object.
     */
    protected void addProblems(Collection<ICompilerProblem> newProblems)
    {
        // This looks goofy, but when the problems collection is empty
        // is could be an immutable empty collections returned by
        // Collections.emptyList or Collections.emptySet.
        // This code ensures that we end up with a collection we can add to.
        if (problems.isEmpty())
            problems = new ArrayList<ICompilerProblem>(newProblems.size());
        problems.addAll(newProblems);
    }

    @Override
    public Collection<ICompilerProblem> checkExternallyVisibleDefinitions(String dottedQName)
    {
        // by default just return an empty list.
        // sub-classes will override this method and return non-empty collections.
        return Collections.emptyList();
    }
}
