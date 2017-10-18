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

package org.apache.royale.compiler.projects;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.common.IFileSpecificationGetter;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.IInvisibleCompilationUnit;
import org.apache.royale.swc.ISWC;

/**
 * Base interface for all project types that use a source manager and library
 * manager to create ICompilationUnits.
 */
public interface IASProject extends ICompilerProject
{
    /**
     * Sets the source path for the project. Calling this method can invalidate
     * a significant amount of incremental compilation results.
     * 
     * @param path List of directories that should be searched for source files.
     */
    void setSourcePath(List<File> path);

    /**
     * Get the list of source paths set.
     * 
     * @return a list of source paths.
     */
    List<File> getSourcePath();

    /**
     * Sets the include sources list for the project. Source files in this list
     * are unconditionally added to the symbol table. Calling this method can
     * invalidate a significant amount of incremental compilation results.
     * 
     * @param files
     */
    void setIncludeSources(File[] files) throws InterruptedException;

    /**
     * Adds a new include source file to the project. Include source file's are
     * files that should always be compiled into the project.
     * 
     * @param file File to add.
     * @throws InterruptedException
     */
    void addIncludeSourceFile(File file) throws InterruptedException;

    /**
     * Adds a new include source file to the project. Include source file's are
     * files that should always be compiled into the project.
     * 
     * @param file File to add.
     * @param overrideSourcePath Set to true to add the file to the source list
     * even if it exists on the source path.
     * @throws InterruptedException
     */
    void addIncludeSourceFile(File file, boolean overrideSourcePath) throws InterruptedException;

    /**
     * Removes an include source file to the project.
     * 
     * @see #addIncludeSourceFile(File)
     * @param file File to remove.
     * @throws InterruptedException
     */
    void removeIncludeSourceFile(File file) throws InterruptedException;

    /**
     * Attach the source directory to a SWC library.
     * 
     * @param library SWC library
     * @param sourceDir directory that contains the source for the SWC library
     */
    void attachInternalLibrarySourcePath(File library, File sourceDir);

    /**
     * Attach the source directory to an external SWC library.
     * 
     * @param library SWC library
     * @param sourceDir directory that contains the source for the SWC library
     */
    void attachExternalLibrarySourcePath(File library, File sourceDir);
    
    /**
     * Get the Directory for the specified library.
     * 
     * @return Full path to the specified library, or
     * null if there is no attached source directory for the specified library.
     */
    String getAttachedSourceDirectory(String libraryFilename);

    /**
     * Removes any calculated data related to a collection of libraries. Used by incremental
     * compilation when swc files have been modified.
     * 
     * @param swcFiles Collection of SWC files to invalidate.
     * @return true if a library referenced by this project was invalidated
     */
    boolean invalidateLibraries(Collection<File> swcFiles);

    /**
     * Removes any calculated data related to a SWC. Used by incremental
     * compilation when library projects have changed
     * 
     * @param swc ISWC to invalidate
     */
    void invalidateLibrary(ISWC swc);

    /**
     * @return Set of projects which depend on this project
     */
    Set<IASProject> getDependingProjects();

    /**
     * Adds a new project dependency to this project.
     * 
     * @param project {@link IASProject} dependency to add to this project.
     * @param swcFilename The SWC filename the project dependency generates
     */
    void addProjectDependeny(IASProject project, String swcFilename);

    /**
     * Removes a project dependency from this project.
     * 
     * @param project {@link IASProject} to remove from this projects dependencies.
     */
    void removeProjectDependeny(IASProject project);

    /**
     * Resets the set of projects that this project depends on.
     * 
     * @param dependencies The new map of {@link IASProject}s that
     * this project depends on.  The value to the map is the SWC filename the
     * IASProject generates.
     */
    void setDependencies(Map<IASProject, String> dependencies);

    /**
     * The libraries used to compile source files in a project. The order of 
     * the libraries sets the priority of class definitions when name and the
     * timestamp of a class are equal. Libraries at the beginning of the 
     * list have a higher priority than libraries at the end of the list.
     *
     * @param libraries The libraries in priority order, may not be null.
     */
    void setLibraries(List<File> libraries);

    /**
     * The libraries available to compiler source in this project.
     * 
     * @return the libraries are returned in priority order from highest to 
     * lowest. The list may not be modified.
     */
    List<ISWC> getLibraries();

    /**
     * Returns the compatibility version encoded as an integer.
     * @return the compatibility version encoded as an integer. Null if 
     * compatibility version has not been set.
     */
    Integer getCompatibilityVersion();

    /**
     * Returns the compatibility version encoded as a string.
     * @return the compatibility version encoded as a string. Null if
     * compatibility version has not been set.
     */
    String getCompatibilityVersionString();
    
    /**
     * Creates an {@link IInvisibleCompilationUnit} for the specified absolute
     * file name.
     * 
     * @param absoluteFileName The name of the file to create an
     * {@link IInvisibleCompilationUnit} for.
     * @param fileSpecGetter A {@link IFileSpecificationGetter} that can be used
     * by the new {@link IInvisibleCompilationUnit} to open files.
     * @return A new {@link IInvisibleCompilationUnit} or null if the specified.
     * file name is not found on the current source path of this
     * {@link IASProject}.
     */
    IInvisibleCompilationUnit createInvisibleCompilationUnit(String absoluteFileName, IFileSpecificationGetter fileSpecGetter);

    /**
     * Creates an {@link IInvisibleCompilationUnit} for the specified absolute
     * file name, specifying a qualified name.
     *
     * This version of createInvisibleCompilationUnit() is called when creating a
     * {@link IInvisibleCompilationUnit} for a source file which isn't contained
     * within the source path, as the qualified name of the {@link IInvisibleCompilationUnit}
     * to be created can't be computed automatically.  This happens when creating a
     * {@link IInvisibleCompilationUnit} for an external file.
     * 
     * @param rootSourceFile The name of the file to create an
     * {@link IInvisibleCompilationUnit} for.
     * @param fileSpecGetter A {@link IFileSpecificationGetter} that can be used
     * by the new {@link IInvisibleCompilationUnit} to open files.
     * @param qName The qualified name of the {@link IInvisibleCompilationUnit}
     * @return A new {@link IInvisibleCompilationUnit} or null if the specified.
     * file name is not found on the current source path of this
     * {@link IASProject}.
     */
    IInvisibleCompilationUnit createInvisibleCompilationUnit(String rootSourceFile, IFileSpecificationGetter fileSpecGetter, String qName);

    /**
     * Determines if the specified file extension ( without the dot eg:
     * {@code as}, not {@code .as} ) corresponds to a source file type that this
     * project can create {@link ICompilationUnit}s for.
     * 
     * @param fileExtension The file extension to check without a dot, for
     * example {@code mxml}.
     * @return true if the file type associated with the specified file
     * extension is supported by this project, false otherwise.
     */
    boolean isSupportedSourceFileType(String fileExtension);

    /**
     * Iterate through the source path list looking for the specified file.
     * 
     * @param file filename to look for
     * @return Absolute path to the file, or null if file not found
     */
    String getSourceFileFromSourcePath(String file);
}
