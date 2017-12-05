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

package org.apache.royale.compiler.workspaces;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.asdoc.IASDocDelegate;
import org.apache.royale.compiler.common.IFileSpecificationGetter;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.mxml.IMXMLDataManager;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.swc.ISWCManager;

/**
 * Implementations maintain a collection of ICompilerProject's in the workspace
 * and state that it is shared across multiple ICompilerProject's.
 * <p>
 * This interface extends {@link IFileSpecificationGetter} so that compilation
 * units that do not wish to virtualize access to the file system can just use a
 * workspace as their {@link IFileSpecificationGetter}.
 */
public interface IWorkspace extends IFileSpecificationGetter
{
    /**
     * Empty map that can be passed to
     * {@link #endIdleState(Map)} by callers who do not have
     * a map containing {@link ICompilationUnit}s whose updates to project
     * symbol tables have been deferred.
     */
    static final Map<ICompilerProject, Set<ICompilationUnit>> NIL_COMPILATIONUNITS_TO_UPDATE = Collections.emptyMap();
    
    /**
     * Waits for all pending operation on all {@link ICompilationUnit}'s in all
     * {@link ICompilerProject}s in this {@link IWorkspace} to complete. The
     * activity lock will also be acquired in this call, so once this method has
     * been called, no new work can be submitted until after the lock has been
     * released by calling {@link #endIdleState(Map)}.
     * <p>
     * This method must <b>never<b> be called from an {@link ICompilationUnit} operation
     * as that would result in dead lock.
     */
    void startIdleState();
    
    /**
     * Updates symbol table entries for all the {@link ICompilationUnit}s in the
     * specified map and unlocks the lock acquired by calling
     * {@link #startIdleState()}.
     * 
     * @param cusToUpdate {@link Map} from {@link ICompilerProject} to
     * {@link Set} of {@link ICompilationUnit}s in the project whose externally
     * visible symbols need to be re-registered with the symbol table of the
     * containing {@link ICompilerProject}.
     */
    void endIdleState(Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate);
    
    /**
     * Waits for the workspace to leave the idle state and prevents all threads
     * from putting the workspace into the idle state. Calls to this method must
     * be balanced with calls to {@link #doneBuilding()}.
     */
    void startBuilding();
    
    /**
     * Allows the workspace to enter the idle state once all pending operations
     * are completed. Each call to this method should succeed exactly one call
     * to {@link #startBuilding()}.
     */
    void doneBuilding();

    /**
     * Sets the {@link IWorkspaceProfilingDelegate} on the workspace.
     * 
     * @param profilingDelegate {@link IWorkspaceProfilingDelegate} that will receive
     * profiling information. Can be null, in which case no profiling
     * information is collected.
     */
    void setProfilingDelegate(IWorkspaceProfilingDelegate profilingDelegate);

    /**
     * Gets the {@link IWorkspaceProfilingDelegate} from the workspace.
     * 
     * @return The {@link IWorkspaceProfilingDelegate} for this IWorkspace. Can be null.
     */
    IWorkspaceProfilingDelegate getProfilingDelegate();
    
    /**
     * Adds a {@link IInvalidationListener} on the workspace.
     * @param invalidationListner {@link IInvalidationListener} that will receive
     * invalidation notifications.
     */
    void addInvalidationListener(IInvalidationListener invalidationListner);

    /**
     * Removes a {@link IInvalidationListener} on the workspace.
     * @param invalidationListner {@link IInvalidationListener} that will be removed.
     */
    void removeInvalidationListener(IInvalidationListener invalidationListner);

    /**
     * Get the SWC manager.
     * 
     * @return {@code ISWCManager} object
     */
    ISWCManager getSWCManager();

    /**
     * Gets the singleton that manages DOM-like {@code MXMLData} objects for
     * MXML files in the workspace.
     * 
     * @return {@code IMXMLDataManager} object
     */
    IMXMLDataManager getMXMLDataManager();

    /**
     * Called by clients of the driver object model when a file remove has been
     * detected. It is the duty of the caller to ensure no new threads are
     * created until after this call has completed.
     * 
     * @param removedFile An {@link IFileSpecification} that can be used to get
     * the new contents of the removed file.
     */
    void fileRemoved(IFileSpecification removedFile);

    /**
     * Called by clients of the driver object model when a file change has been
     * detected. It is the duty of the caller to ensure no new threads are
     * created until after this call has completed.
     * 
     * @param changedFile An {@link IFileSpecification} that can be used to get
     * the change filename.
     */
    void fileChanged(IFileSpecification changedFile);

    /**
     * Called by clients of the driver object model when a file add has been
     * detected. It is the duty of the caller to ensure no new threads are
     * created until after this call has completed.
     * 
     * @param addedFile An {@link IFileSpecification} that can be used to get the
     * name and contents of the added file.
     */
    void fileAdded(IFileSpecification addedFile);

    /**
     * Get all compilation units in the specified project related to the
     * specified file name. This method will not return any invisible
     * compilation units.
     * 
     * @param path String to source filename
     * @param project containing project
     */
    Collection<ICompilationUnit> getCompilationUnits(String path, ICompilerProject project);
    
    /**
     * Gets an iterator that iterates first over all invisible
     * {@link ICompilationUnit}'s for the specified file path in the specified
     * {@link ICompilerProject} and then over all visible
     * {@link ICompilationUnit}'s for the specified file path in the specified
     * {@link ICompilerProject}.
     * 
     * @param path Normalized absolute file name.
     * @param project {@link ICompilerProject} that will contains all the
     * {@link ICompilationUnit} in the returned {@link Iterable}.
     * @return An iterator that iterates first over all invisible
     * {@link ICompilationUnit}'s for the specified file path in the specified
     * {@link ICompilerProject} and then over all visible
     * {@link ICompilationUnit}'s for the specified file path in the specified
     * {@link ICompilerProject}
     */
    Iterable<ICompilationUnit> getInvisibleAndVisibleCompilationUnits(final String path, final ICompilerProject project);

    /**
     * Gets the {@link IASDocDelegate} that instances of
     * ASParser will use to record information about ASDoc comments.
     * 
     * @return The {@link IASDocDelegate} that instances of
     * ASParser will use to record information about ASDoc comments.
     */
    IASDocDelegate getASDocDelegate();
    
    /**
     * Sets the {@link IASDocDelegate} that instances of
     * ASParser will use to record information about ASDoc comments.
     * 
     * @param asDocDelegate {@link IASDocDelegate} that instances of
     * ASParser will use to record information about ASDoc comments.
     */
    void setASDocDelegate(IASDocDelegate asDocDelegate);
}
