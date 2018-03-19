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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.problems.DuplicateSourceFileProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnsupportedSourceFileProblem;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.utils.FilenameNormalization;

/**
 * Manages the include sources list of a {@link ASProject}.
 * 
 * TODO Revisit this class, it is trying to be too atomic.
 */
final class SourceListManager
{
    SourceListManager(ASProject project, SourcePathManager sourcePathManager)
    {
        this.project = project;
        this.sourcePathManager = sourcePathManager;
        sources = new LinkedHashSet<File>();
        problems = Collections.emptyList();
    }

    private final ASProject project;
    private final SourcePathManager sourcePathManager;
    private Set<File> sources;
    private Collection<ICompilerProblem> problems;

    void setSourceList(File[] newSources) throws InterruptedException
    {
        setSourceList(newSources, false);
    }
    /**
     * Sets the source list. This method will remove the existing source list
     * {@link ICompilationUnit}'s from the project and add new
     * {@link ICompilationUnit}'s to the project.
     * 
     * @param newSources New source list.
     * @param overrideSourcePath Set to true to add the file to the source list
     * @throws InterruptedException
     */
    void setSourceList(File[] newSources, boolean overrideSourcePath) throws InterruptedException
    {
        newSources = FilenameNormalization.normalize(newSources);
        if (newSources.equals(sources))
            return;

        Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        Set<File> newSourcesSet = new LinkedHashSet<File>();
        List<File> newSourcesToCreate = new ArrayList<File>();
        for (File file : newSources)
        {
            assert !file.isDirectory();
            if (project.getSourceCompilationUnitFactory().canCreateCompilationUnit(file))
            {
                if (!overrideSourcePath && sourcePathManager.isFileOnSourcePath(file))
                    continue;
                
                if (newSourcesSet.contains(file))
                {
                    problems.add(new DuplicateSourceFileProblem(file));                    
                }
                else
                {
                    newSourcesSet.add(file);
                    // if a file doesn't exist in the sources, then it needs
                    // creating, otherwise don't re-create same source
                    if (!sources.contains(file))
                    {
                        newSourcesToCreate.add(file);
                    }
                }
            }
            else
            {
                problems.add(new UnsupportedSourceFileProblem(file));
            }
        }
        
        // if an existing source is not in the newSources, it needs to be removed.
        Set<ICompilationUnit> unitsToRemove = new HashSet<ICompilationUnit>();
        for (File existingSource : sources)
        {
            if (!newSourcesSet.contains(existingSource))
            {
                unitsToRemove.addAll(project.getCompilationUnits(existingSource.getAbsolutePath()));
            }
        }

        // set the new sources
        sources = newSourcesSet;

        List<ICompilationUnit> unitsToAdd = Collections.emptyList();
        if (!newSourcesToCreate.isEmpty())
        {
            unitsToAdd = new ArrayList<ICompilationUnit>(newSourcesToCreate.size());
            for (File file : newSourcesToCreate)
            {
            	File sourcePath = sourcePathManager.getSourcePath(file);
            	String qname = null;
            	if (sourcePath != null)
            		qname = SourcePathManager.computeQName(sourcePath, file);
                ICompilationUnit unit = project.getSourceCompilationUnitFactory().createCompilationUnit(
                    file, DefinitionPriority.BasePriority.SOURCE_LIST, 0, qname, null);
                
                //It can be null in some cases, see #ResourceBundleSourceFileHandler
                if(unit != null)
                    unitsToAdd.add(unit);
            }
        }

        List<ICompilerProblem> emptyList = Collections.emptyList();
        this.problems = problems.size() == 0 ? emptyList : problems;

        project.sourceListChange(unitsToRemove, unitsToAdd);
    }

    /**
     * Returns true if the source is already contained within the SourceListManager
     * 
     * @param source A source file.
     */
    public boolean containsSource(File source)
    {
        assert source.equals(FilenameNormalization.normalize(source));
        return sources.contains(source);
    }

    /**
     * Adds a file to the source list unless it is on the source path.
     * <p>
     * Adding the same file multiple times or adding files that don't exist will
     * generate ICompilerProblems that can be retrieved via
     * {@code collectProblems(Collection)}.
     * <p>
     * If the same file is added multiple times, the file may be removed multiple times
     * with {@code removeSource(File)}.
     * 
     * @param newSource File to add to the source list.
     */
    public void addSource(File newSource) throws InterruptedException
    {
        addSource(newSource, false);
    }

    /**
     * Adds a file to the source list unless it is on the source path.
     * <p>
     * Adding the same file multiple times or adding files that don't exist will
     * generate ICompilerProblems that can be retrieved via
     * {@code collectProblems(Collection)}.
     * <p>
     * If the same file is added multiple times, the file may be removed
     * multiple times with {@code removeSource(File)}.
     * 
     * @param newSource File to add to the source list.
     * @param overrideSourcePath Set to true to add the file to the source list
     * even if it exists on the source path.
     */
    public void addSource(File newSource, boolean overrideSourcePath) throws InterruptedException
    {
        File[] newSources = sources.toArray(new File[sources.size() + 1]);
        newSources[sources.size()] = newSource;
        setSourceList(newSources, overrideSourcePath);
    }

    /**
     * Removes a file from the source list.
     * <p>
     * If the specified file occurs more than once in the source list, the last
     * entry is removed.
     * 
     * @param sourceToRemove File to remove from the source list.
     */
    public void removeSource(File sourceToRemove) throws InterruptedException
    {
        if (sources.isEmpty())
            return;

        sourceToRemove = FilenameNormalization.normalize(sourceToRemove);
        
        if (!sources.contains(sourceToRemove))
            return;

        Set<File> newSources = new LinkedHashSet<File>(sources);
        newSources.remove(sourceToRemove);

        setSourceList(newSources.toArray(new File[newSources.size()]));
    }

    /**
     * Adds {@link ICompilerProblem}'s found in the current
     * source list to the specified collection.
     * <p>
     * These problems are with the source list itself, not with sources
     * discovered in the source list. For example the returned collection would
     * not contain syntax error problems, put will contain
     * {@link DuplicateSourceFileProblem} problems.
     */
    void collectProblems(Collection<ICompilerProblem> problems)
    {
        problems.addAll(this.problems);
    }
    
    /**
     * Adds all the {@link ICompilationUnit}'s whose root source file is the
     * specified File to the specified collection.
     * 
     * @param rootSourceFile File to search for.
     * @param units Collection to add to.
     */
    public void collectionCompilationUnitsForRootSourceFile(File rootSourceFile, Collection<ICompilationUnit> units)
    {
        Collection<ICompilationUnit> compilationUnits = project.getCompilationUnits(rootSourceFile.getAbsolutePath());
        units.addAll(compilationUnits);
    }
    
    /**
     * Determines of the specified file is the root source file of any
     * {@link ICompilationUnit} created by this {@code SourceListManager}.
     * 
     * @param rootSourceFile File to search for.
     * @return true if the specified file is the root source file of any
     * {@link ICompilationUnit}'s created by this {@code SourceListManager}.
     */
    public boolean hasCompilationUnitsForRootSourceFile(File rootSourceFile)
    {
        Collection<ICompilationUnit> compilationUnits = project.getCompilationUnits(rootSourceFile.getAbsolutePath());
        return compilationUnits.size() > 0;
    }
}
