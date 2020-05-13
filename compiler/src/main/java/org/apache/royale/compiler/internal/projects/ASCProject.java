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

package org.apache.royale.compiler.internal.projects;

import java.io.File;
import java.util.Collection;

import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.IASCProject;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * Implementation class for an asc style project.
 */
public class ASCProject extends CompilerProject implements IASCProject
{
    public ASCProject(Workspace w, boolean useAS3)
    {
        super(w, useAS3);
    }

    /**
     * Removes all {@link ICompilationUnit}'s that are currently in the project
     * and adds the {@link ICompilationUnit}'s in the specified collection to
     * the project.
     * <p>
     * This method will request the scopes for each compilation unit such that
     * the externally visible definitions in each compilation unit can be added
     * to the project's scope.
     * 
     * @param units Collection of {@link ICompilationUnit}'s to put in this
     * project.
     */
    public void setCompilationUnits(Collection<ICompilationUnit> units) throws InterruptedException
    {
        removeCompilationUnits(getCompilationUnits());
        addCompilationUnitsAndUpdateDefinitions(units);
    }

    @Override
    public void collectProblems(Collection<ICompilerProblem> problems)
    {
        collectConfigProblems(problems);
    }

    @Override
    public boolean handleAddedFile(File addedFile)
    {
        // do nothing.  We have no good way of deciding
        // if the added file is relevant.
        return false;
    }

    @Override
    public boolean isAssetEmbeddingSupported()
    {
        return false;
    }

	@Override
	public boolean getAllowPrivateNameConflicts() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getAllowImportAliases() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getAllowAbstractClasses() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getAllowPrivateConstructors() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getStrictIdentifierNames() {
		// TODO Auto-generated method stub
		return false;
	}
}
