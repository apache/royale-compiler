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

package org.apache.royale.compiler.units;

import java.util.Collection;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.IASProject;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * A public interface clients may use to compile a transient version of a file.
 * None of the operations on an {@link IInvisibleCompilationUnit} will change
 * the state of an {@link IASProject} in a way that is visible to other
 * {@link ICompilationUnit}'s in the {@link IASProject}.
 */
public interface IInvisibleCompilationUnit extends ICompilationUnit
{
    /**
     * Finds all the {@link ICompilerProblem}'s in this {@link IInvisibleCompilationUnit}.
     * @param problems {@link Collection} the {@link ICompilerProblem}'s should be added to.
     * @throws InterruptedException
     */
    void getCompilerProblems(Collection<ICompilerProblem> problems) throws InterruptedException;

    /**
     * Invalidates the processing results of this
     * {@link IInvisibleCompilationUnit}. Should be called by clients when ever
     * any of the inputs of this {@link IInvisibleCompilationUnit} change.
     * Changes to the {@link IASProject}'s settings do *not* automatically
     * invalidate the processing results of this
     * {@link IInvisibleCompilationUnit}. Clients should manually call this
     * methods after changing a {@link IASProject} settings ( we should fix
     * this in the future... ).
     */
    void clean();
    
    /**
     * Explicitly removes this {@link IInvisibleCompilationUnit} from an
     * {@link IASProject}. This method will ensure that various cache entries
     * in the {@link IASProject} and {@link IWorkspace} are purged.
     * <p>
     * Clients don't need to call this, but calling it allows certain resource
     * cleanups to happen earlier than would normally.
     */
    void remove();
}
