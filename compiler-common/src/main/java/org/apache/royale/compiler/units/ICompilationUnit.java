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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.royale.compiler.common.IDefinitionPriority;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.royale.compiler.units.requests.IRequest;
import org.apache.royale.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;


/**
 * A unit of compilation. Implementations provide methods to request definitions
 * ( getFileScopeRequest ), semantic errors/warnings (
 * getOutgoingDependenciesRequest ), abc bytes ( getABCBytesRequest ), and SWF tags
 * ( getSWFTagsRequest ). Implementations of this interface are the main means
 * that AS3, MXML, and associated source files are converted to bytes in SWF.
 */
public interface ICompilationUnit
{
    /**
     * An enumeration with an entry for each ICompilationUnit operation. Used in
     * tracking which operation results of another ICompilaitonUnit that an
     * ICompilationUnit depends on.
     */
    enum Operation
    {
        GET_SYNTAX_TREE,
        GET_FILESCOPE,
        GET_SEMANTIC_PROBLEMS,
        GET_ABC_BYTES,
        GET_SWF_TAGS,
        INVALIDATE_CU;
        
        Operation()
        {
            assert ordinal() < 31 : "We should not have more than 8 operations, let alone 31!";
            mask = 1 << ordinal();
        }
        
        /**
         * This mask is used by CompilationUnitBase  to update
         * a {@link AtomicInteger} that keeps track of which operations have
         * been completed.
         */
        public final int mask;
    }

    /**
     * An enumeration with an entry for each ICompilationUnit source type.
     */
    enum UnitType
    {
        AS_UNIT,
        ABC_UNIT,
        FXG_UNIT,
        MXML_UNIT,
        CSS_UNIT,
        SWC_UNIT,
        EMBED_UNIT,
        RESOURCE_UNIT
    }

    /**
     * @return The project that contains this ICompilationUnit.
     */
    ICompilerProject getProject();

    /**
     * Gets the request object from which the caller can extract the syntax tree
     * and any source file parsing {@link ICompilerProblem}'s
     * for this {@link ICompilationUnit}. Implementations may update the dependency
     * graph in the project as a side
     * affect of this operation.
     * 
     * @return The request object for the getFileScope operation.
     */
    IRequest<ISyntaxTreeRequestResult, ICompilationUnit> getSyntaxTreeRequest();
    
    /**
     * Gets the request object from which the caller can extract the root scope,
     * AST, and any parsing ICompilerProblems, for the CompilationUnit.
     * Implementations may update the dependency graph in the project as a side
     * affect of this operation.
     * 
     * @return The request object for the getFileScope operation.
     */
    IRequest<IFileScopeRequestResult, ICompilationUnit> getFileScopeRequest();

    /**
     * Gets the request object from which the caller cause the dependency graph to be updated
     * with all the dependencies from this compilation unit.
     * 
     * @return The request object for the getFileScope operation.
     */
    IRequest<IOutgoingDependenciesRequestResult, ICompilationUnit> getOutgoingDependenciesRequest ();

    /**
     * Gets the request object from which the caller can extract the abc bytes
     * generated for this ICompilationUnit. Implementations may update the
     * dependency graph in the project as a side affect of this operation.
     * 
     * @return The request object for the getFileScope operation.
     */
    IRequest<IABCBytesRequestResult, ICompilationUnit> getABCBytesRequest();

    /**
     * Gets the request object with which the caller can add the SWF tags
     * generated for this ICompilationUnit to a ISWF. Implementations may update
     * the dependency graph in the project as a side affect of this operation.
     * 
     * @return The request object for the getFileScope operation.
     */
    IRequest<ISWFTagsRequestResult, ICompilationUnit> getSWFTagsRequest();

    /**
     * @return The identifier names defined by the compilation unit
     * @throws InterruptedException 
     */
    List<String> getShortNames() throws InterruptedException;

    /**
     * @return The qualified names defined by the compilation unit
     * @throws InterruptedException 
     */
    List<String> getQualifiedNames() throws InterruptedException;

    /**
     * Gets a string that is unique to the compilation unit instance and can be
     * used to produce an ordering of compilation units that is stable across
     * different compilation runs.
     * 
     * Note that is is critical that everyone who implements this interface
     * implements this method correctly. If not, the compiler will act in
     * a non-deterministic way.
     * 
     * Also, note that this is the String that will be written to the link report as
     * the name of this compilation unit.
     * 
     * @return The unique name string for the compilation unit.
     */
    String getName();

    /**
     * Gets the path to the compilation unit instance. The string is unique to
     * the compilation unit instance. This string will also be used in reporting
     * errors.
     * 
     * @return The absolute path for the compilation unit.
     */
    String getAbsoluteFilename();

    /**
     * @return List of definition promises
     */
    List<IDefinition> getDefinitionPromises();

    /**
     * @return the compilation unit type
     */
    UnitType getCompilationUnitType();

    /**
     * Removes all data created during a compile Clients should not call this
     * method directly, as it can potentially leave the ICompilationUnit in an
     * invalid state. Read the CompilerProject.clean implementation for more
     * details.
     * 
     * @param invalidatedSWCFiles Map of SWC filenames and depending projects
     * which have been invalidated by this call
     * @param cusToUpdate {@link Map} from {@link ICompilerProject} to
     * {@link Set} of {@link ICompilationUnit}s in the project whose externally
     * visible symbols need to be re-registered with the symbol table of the
     * containing {@link ICompilerProject} when cleaning is complete.
     * @param clearFileScope Whether to clear the file scope
     * @return whether an invalidation was done
     */
    boolean clean(Map<ICompilerProject, Set<File>> invalidatedSWCFiles, Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate, boolean clearFileScope);

    /**
     * Gets the definition priority for definitions defined by the compilation
     * unit. The "priority" of a definition determines whether that definition
     * shadows definitions from other compilation units that have the same
     * qname.
     * 
     * @return The definition priority for definitions defined by the compilation
     * unit.
     */
    IDefinitionPriority getDefinitionPriority();

    /**
     * Called by {@link ICompilerProject}'s when an {@link ICompilationUnit}
     * is removed from a project.  Implementations should clear an references
     * to the containing project when this method is called.  After this
     * method is called {@link #getProject()} should return null.
     */
    void clearProject();

    /**
     * Wait till the compilation unit finishes building, and collect compiler
     * problems. After calling this method, this compilation unit will be in a
     * fully compiled state.
     * 
     * @see ICompilationUnit#startBuildAsync
     * @param problems Problems from executing the requests.
     * @param targetType type of the active Target
     * @throws InterruptedException Concurrency error.
     */
    void waitForBuildFinish(Collection<ICompilerProblem> problems, TargetType targetType) throws InterruptedException;
    
    /**
     * This method starts to build the compilation unit asynchronously. It kicks
     * off all the request methods on the compilation unit.
     * 
     * @param targetType type of the active Target
     */
    void startBuildAsync(TargetType targetType);

    /**
     * @return true if this {@link ICompilationUnit} does *not*
     * put its global definitions in the {@link ICompilerProject}'s
     * symbol table, false otherwise.
     */
    boolean isInvisible();

    /**
     * @return A collection of all filenames which are embedded in
     * this {@link ICompilationUnit} through Embed metadata. This
     * will never return null.
     */
    Collection<String> getEmbeddedFilenames();
}
