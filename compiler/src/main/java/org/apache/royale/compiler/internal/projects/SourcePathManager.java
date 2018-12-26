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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import org.apache.royale.compiler.problems.DuplicateQNameInSourcePathProblem;
import org.apache.royale.compiler.problems.DuplicateSourceFileProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.NonDirectoryInSourcePathProblem;
import org.apache.royale.compiler.problems.OverlappingSourcePathProblem;
import org.apache.royale.compiler.problems.SourcePathNotFoundProblem;
import org.apache.royale.compiler.problems.UnableToListFilesProblem;
import org.apache.royale.compiler.projects.IASProject;
import org.apache.royale.compiler.projects.IRoyaleProject;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.utils.DirectoryID;
import org.apache.royale.utils.FilenameNormalization;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Maintains a source path for a {@link ASProject}.
 */
public final class SourcePathManager
{
    /**
     * Constructor
     */
    public SourcePathManager(ASProject compilerProject)
    {
        this.compilerProject = compilerProject;
        sourcePaths = new LinkedHashMap<DirectoryID, HashSet<QNameFile>>();
        problems = Collections.emptyList();
    }

    private final ASProject compilerProject;
    
    // map where we keep all the paths and the qnames/files they define.
    // use DirectoryID as the key so we can avoid two "identical" paths
    // that only differ in case (upper vs lower)
    private LinkedHashMap<DirectoryID, HashSet<QNameFile>> sourcePaths;
    private Collection<ICompilerProblem> problems;
    private Collection<ICompilerProblem> duplicateQNameProblems;

    /**
     * Test if a file is on the source path or not.
     * 
     * @param file The file to test. May not be null.
     * @return true if the file is on the source path, false otherwise.
     */
    public boolean isFileOnSourcePath(File file)
    {
        for (final DirectoryID directory : sourcePaths.keySet())
        {
            if (directory.isParentOf(file))
                return true;
        }
        
        return false;
    }
    
    public File getSourcePath(File file)
    {
        for (final DirectoryID directory : sourcePaths.keySet())
        {
            if (directory.isParentOf(file))
                return directory.getFile();
        }
        
        return null;
    }
    
    private void accumulateQNameFiles(Set<QNameFile> qNameFiles, File directory, String baseQName, String locale, 
            Collection<ICompilerProblem> problems, int order)
    {
        assert directory.isDirectory();
        assert directory.equals(FilenameNormalization.normalize(directory));
        File[] files = directory.listFiles();
        if (files == null)
        {
            problems.add(new UnableToListFilesProblem(directory));
            return;
        }
        for (final File file : directory.listFiles())
        {
            assert file.equals(FilenameNormalization.normalize(file));

            if (file.isDirectory())
            {
                accumulateQNameFiles(qNameFiles, file, baseQName + file.getName() + ".", locale, 
                        problems, order);
            }
            else if (compilerProject.getSourceCompilationUnitFactory().canCreateCompilationUnit(file))
            {
                String className = FilenameUtils.getBaseName(file.getName());
                String qName = baseQName + className;
                qNameFiles.add(new QNameFile(qName, file, locale, order));
            }
        }
    }

    public static String computeQName(File ancestor, File descendent)
    {
        assert ancestor.equals(FilenameNormalization.normalize(ancestor));
        assert descendent.equals(FilenameNormalization.normalize(descendent));
        StringBuilder result = new StringBuilder();
        File current = descendent.getParentFile();
        result.insert(0, FilenameUtils.getBaseName(descendent.getPath()));
        while (current != null)
        {
            if (current.equals(ancestor))
                return result.toString();
            result.insert(0, '.');
            result.insert(0, FilenameUtils.getBaseName(current.getPath()));
            current = current.getParentFile();
        }
        return null;
    }

    private static boolean arePathsEqual(File[] newPaths, LinkedHashMap<DirectoryID, HashSet<QNameFile>> oldPaths)
    {
        if (newPaths.length != oldPaths.size())
            return false;

        int i = 0;
        for (DirectoryID oldPath : oldPaths.keySet())
        {
            if (!newPaths[i].isDirectory())
                return false;       // all the old paths are directories. If this isn't then it must not be equal
            
            DirectoryID newDir = new DirectoryID( newPaths[i] );
            
            if (!(newDir.equals(oldPath)))
                return false;
            i++;
        }

        return true;
    }

    private static boolean isAncestorOf(File ancestor, File descendent)
    {
        return computeQName(ancestor, descendent) != null;
    }

    void handleChangedSourcePath(File[] newSourcePath)
    {
        // used to check for duplicates and buildup a mapping of which source files
        // are contained within each sourcePath
        Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        LinkedHashMap<DirectoryID, HashSet<QNameFile>> newSourcePaths = new LinkedHashMap<DirectoryID, HashSet<QNameFile>>();
        List<QNameFile> newQNameFilesToCreate = new ArrayList<QNameFile>();
        int order = 0;
        for (File sourcePathEntry : newSourcePath)
        {
            // Make sure the entry is a directory
            if (!sourcePathEntry.isDirectory())
            {
                problems.add(new NonDirectoryInSourcePathProblem(sourcePathEntry));
            }
            else
            {

                DirectoryID directoryId = new DirectoryID(sourcePathEntry);
                if (!newSourcePaths.containsKey(directoryId))
                {
                    HashSet<QNameFile> filesInPath = new HashSet<QNameFile>();
                    newSourcePaths.put(directoryId, filesInPath);

                    // Check for overlapping source path entries.
                    for (File descendent : newSourcePath)
                    {
                        if ((sourcePathEntry != descendent) &&
                            (isAncestorOf(sourcePathEntry, descendent)))
                        {
                            problems.add(new OverlappingSourcePathProblem(sourcePathEntry, descendent));
                        }
                    }

                    String locale = null;
                    if (compilerProject instanceof IRoyaleProject)
                        locale = ((IRoyaleProject)compilerProject).getResourceLocale(sourcePathEntry.getAbsolutePath());

                    accumulateQNameFiles(filesInPath, sourcePathEntry, "", locale, 
                    problems, order);

                    // if the source path already exists, no need to re-add files which
                    // already exist
                    Set<QNameFile> existingEntriesForSourcePath =
                            MoreObjects.<Set<QNameFile>> firstNonNull(sourcePaths.get(directoryId), Collections.<QNameFile> emptySet());

                    // Any qname file that is in filesInPath, but not in existingEntriesForSourcePath
                    // is a new qname file that we need to create a compilation unit for.
                    newQNameFilesToCreate.addAll(Sets.difference(filesInPath, existingEntriesForSourcePath));

                }
            }
            ++order;
        }

        // if an existing path is not in the newPaths, it needs to be removed.
        // work out which compilation units need to be removed as a result of changing
        Set<ICompilationUnit> unitsToRemove = new HashSet<ICompilationUnit>();
        for (Map.Entry<DirectoryID, HashSet<QNameFile>> e : sourcePaths.entrySet())
        {
            Set<QNameFile> newSourcePathFiles =
                MoreObjects.<Set<QNameFile>>firstNonNull(newSourcePaths.get(e.getKey()), Collections.<QNameFile>emptySet());
            
            Set<QNameFile> filesToRemove = Sets.difference(e.getValue(), newSourcePathFiles);
            
            for (QNameFile qNameFile : filesToRemove)
            {
                File sourceFile = qNameFile.file;
                
                Collection<ICompilationUnit> sourcePathCompilationUnitsToRemove =
                    Collections2.filter(compilerProject.getCompilationUnits(sourceFile.getAbsolutePath()), new Predicate<ICompilationUnit>() {

                        @Override
                        public boolean apply(ICompilationUnit cu)
                        {
                            DefinitionPriority defPriority = (DefinitionPriority)cu.getDefinitionPriority();
                            return defPriority.getBasePriority() == DefinitionPriority.BasePriority.SOURCE_PATH;
                        }
                        @Override
                        public boolean test(ICompilationUnit input)
                        {
                            return apply(input);
                        }
                        });
                unitsToRemove.addAll(sourcePathCompilationUnitsToRemove);
            }
        }

        // set the new sources
        sourcePaths = newSourcePaths;

        List<ICompilationUnit> unitsToAdd = new ArrayList<ICompilationUnit>();
        if (!newQNameFilesToCreate.isEmpty())
        {
            for (QNameFile qNameFile : newQNameFilesToCreate)
            {
                ICompilationUnit newCU =
                    compilerProject.getSourceCompilationUnitFactory().createCompilationUnit(
                        qNameFile.file, DefinitionPriority.BasePriority.SOURCE_PATH, qNameFile.order, qNameFile.qName, qNameFile.locale);
                
                //It can be null in some cases, see #ResourceBundleSourceFileHandler
                if(newCU != null)
                    unitsToAdd.add(newCU);
            }
        }

        this.problems = problems;
        compilerProject.updateCompilationUnitsForPathChange(unitsToRemove, unitsToAdd);
        checkForDuplicateQNames();
    }
    
    private boolean foundAllCompilationUnits()
    {
        compilerProject.getWorkspace();
        for (Set<QNameFile> qNameFiles : sourcePaths.values())
        {
            for (QNameFile qNameFile : qNameFiles)
            {
                if (compilerProject.getWorkspace().getCompilationUnits(qNameFile.file.getAbsolutePath(), compilerProject).isEmpty())
                {
                    if (compilerProject.getSourceCompilationUnitFactory().needCompilationUnit(qNameFile.file, qNameFile.qName, qNameFile.locale))
                        return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Updates the list of directories that are the source path. This method may
     * add or remove {@link ICompilationUnit}'s from the {@link IASProject}
     * associated with this {@link SourcePathManager}.
     * 
     * @param newSourcePath
     */
    void setSourcePath(File[] newSourcePath)
    {
        newSourcePath = FilenameNormalization.normalize(newSourcePath);
        try
        {
            if (arePathsEqual(newSourcePath, sourcePaths))
                return;
    
            handleChangedSourcePath(newSourcePath);
        }
        finally
        {
            assert foundAllCompilationUnits();
        }
    }
    
    private Collection<ICompilationUnit> getCompilationUnits(File f)
    {
        assert FilenameNormalization.normalize(f).equals(f);
        return Collections2.filter(compilerProject.getCompilationUnits(f.getAbsolutePath()),
                new Predicate<ICompilationUnit>() {
                    @Override
                    public boolean apply(ICompilationUnit compilationUnit)
                    {
                        DefinitionPriority priority = ((DefinitionPriority)compilationUnit.getDefinitionPriority());
                        return priority.getBasePriority() == DefinitionPriority.BasePriority.SOURCE_PATH;
                    }
                    @Override
                    public boolean test(ICompilationUnit input)
                    {
                        return apply(input);
                    }
                });
    }
    
    /**
     * Notifies this {@link SourcePathManager} that a file as been added to the
     * file system.
     * <p>
     * This {@link SourcePathManager} will determine if any source path entries
     * contain the specified file and if so, will create new
     * {@link ICompilationUnit}'s and add them to the project.
     * 
     * @param f File that has been added to the file system.
     * @return true if any {@link ICompilationUnit}'s were created and added to
     * the project.
     */
    public boolean addFile(File f)
    {
        f = FilenameNormalization.normalize(f);

        if (!getCompilationUnits(f).isEmpty())
            return false;

        ArrayList<QNameFile> qNameFiles = new ArrayList<QNameFile>(1);
        int order = 0;
        for (Map.Entry<DirectoryID, HashSet<QNameFile>> sourcePathEntry : sourcePaths.entrySet())
        {
            DirectoryID dir = sourcePathEntry.getKey();
            String qname = computeQName(dir.getFile(), f);
            if (qname != null)
            {
                String locale = null;
                if(compilerProject instanceof IRoyaleProject)
                    locale = ((IRoyaleProject)compilerProject).getResourceLocale(dir.getFile().getAbsolutePath());
                
                QNameFile newQNameFile = new QNameFile(qname, f, locale, order);
                sourcePathEntry.getValue().add(newQNameFile);
                qNameFiles.add(newQNameFile);
            }
            ++order;
        }

        if (qNameFiles.isEmpty())
            return false;

        List<ICompilationUnit> unitsToAdd = new ArrayList<ICompilationUnit>();
        for (QNameFile qNameFile : qNameFiles)
        {
            ICompilationUnit newCU =
                    compilerProject.getSourceCompilationUnitFactory().createCompilationUnit(
                            qNameFile.file, DefinitionPriority.BasePriority.SOURCE_PATH, qNameFile.order, qNameFile.qName, qNameFile.locale);
            if (newCU != null)
                unitsToAdd.add(newCU);
        }

        // If none of the files had a file extension we knew how to make
        // a compilation unit for we might not have any new compilation
        // units to add to the project.
        if (unitsToAdd.size() == 0)
            return false;
        assert unitsToAdd != null;
        compilerProject.updateCompilationUnitsForPathChange(Collections.<ICompilationUnit>emptyList(), unitsToAdd);
        checkForDuplicateQNames();
        
        return true;
    }
    
    private void checkForDuplicateQNames()
    {
        Map<String, Set<QNameFile>> qNameMap = new HashMap<String, Set<QNameFile>>();
        for (HashSet<QNameFile> qNameFiles : sourcePaths.values())
        {
            for (QNameFile qNameFile : qNameFiles)
            {
                Set<QNameFile> qNameFilesForQName = qNameMap.get(qNameFile.qName);
                if (qNameFilesForQName == null)
                {
                    qNameFilesForQName = new HashSet<QNameFile>(1);
                    qNameMap.put(qNameFile.qName, qNameFilesForQName);
                }
                qNameFilesForQName.add(qNameFile);
            }
        }
        
        ArrayList<ICompilerProblem> duplicateQNameProblems = new ArrayList<ICompilerProblem>();
        for (Map.Entry<String, Set<QNameFile>> qNameMapEntry : qNameMap.entrySet())
        {
            Set<QNameFile> qNameFiles = qNameMapEntry.getValue();
            String qName = qNameMapEntry.getKey();
            if (qNameFiles.size() > 1)
            {
                StringBuilder listString = new StringBuilder();
                int found = 0;
                for (QNameFile qNameFile : qNameFiles)
                {
                    if(ResourceBundleSourceFileHandler.EXTENSION.equalsIgnoreCase(
                            FilenameUtils.getExtension(qNameFile.file.getAbsolutePath()))) 
                    {
                        //TODO: https://bugs.adobe.com/jira/browse/CMP-923
                        //As of now, we ignore the properties files while 
                        //checking the duplicate names until we find a sophisticated way 
                        //to this in the future.
                        continue;
                    }
                    
                    if (found++ > 0)
                        listString.append(", ");

                    assert qName.equals(qNameFile.qName);
                    listString.append(qNameFile.file.getAbsolutePath());
                }
                
                if(found > 1) //if we found more than one duplicate qname then report a problem
                {
                    ICompilerProblem problem = new DuplicateQNameInSourcePathProblem(listString.toString(), qName);
                    duplicateQNameProblems.add(problem);
                }
            }
        }
               
        if (duplicateQNameProblems.size() > 0)
            this.duplicateQNameProblems = duplicateQNameProblems;
        else
            this.duplicateQNameProblems = null;
    }

    /**
     * Notifies this {@link SourcePathManager} that a file as been deleted from
     * the file system.
     * <p>
     * This {@link SourcePathManager} will determine if any source path entries
     * contain the specified file and if so, will remove
     * {@link ICompilationUnit}'s from the project.
     * 
     * @param f File that has been removed from the file system.
     * @return true if any {@link ICompilationUnit}'s were removed from the
     * project.
     */
    public boolean removeFile(File f)
    {
        Collection<ICompilationUnit> unitsToRemove = getCompilationUnits(f);
        if (!unitsToRemove.isEmpty())
        {
            List<ICompilationUnit> unitsToAdd = Collections.emptyList();
            compilerProject.updateCompilationUnitsForPathChange(unitsToRemove, unitsToAdd);
            removeQNames(f);
            // if there are already duplicate names, after the remove, check for duplicates
            // again, as the remove may have fixed the problem.
            if (this.duplicateQNameProblems != null && !this.duplicateQNameProblems.isEmpty())
            {
                checkForDuplicateQNames();
            }
            return true;
        }
        return false;
    }

    private void removeQNames(File f)
    {
        for (Map.Entry<DirectoryID, HashSet<QNameFile>> sourcePathEntry : sourcePaths.entrySet())
        {
            DirectoryID dir = sourcePathEntry.getKey();
            String qname = computeQName(dir.getFile(), f);
            if (qname != null)
            {
                for (Iterator<QNameFile> iter = sourcePathEntry.getValue().iterator(); iter.hasNext();)
                {
                    QNameFile qNameFile = iter.next();
                    if (qNameFile.file.equals(f))
                    {
                        iter.remove();
                    }
                }
            }
        }
    }

    /**
     * Add {@link ICompilerProblem}'s found in the current source path to the
     * specified collection.
     * <p>
     * These problems are with the source path itself, not with sources
     * discovered in the source path. For example the returned collection would
     * not contain syntax error problems, put will contain
     * {@link DuplicateSourceFileProblem} problems.
     */
    void collectProblems(Collection<ICompilerProblem> problems)
    {
        problems.addAll(this.problems);
        if (duplicateQNameProblems != null)
            problems.addAll(duplicateQNameProblems);
        
        for (DirectoryID sourcePath : sourcePaths.keySet())
        {
            if (!sourcePath.getFile().exists())
            {
                problems.add(new SourcePathNotFoundProblem(sourcePath.getFile().getAbsolutePath()));
            }
        }
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
        Collection<ICompilationUnit> compilationUnits = compilerProject.getCompilationUnits(rootSourceFile.getAbsolutePath());
        units.addAll(compilationUnits);
    }

    /**
     * Determines of the specified file is the root source file of any
     * {@link ICompilationUnit} created by this {@link SourcePathManager}.
     * 
     * @param rootSourceFile File to search for.
     * @return true if the specified file is the root source file of any
     * {@link ICompilationUnit}'s created by this {@link SourcePathManager}.
     */
    public boolean hasCompilationUnitsForRootSourceFile(File rootSourceFile)
    {
        Collection<ICompilationUnit> compilationUnits = compilerProject.getCompilationUnits(rootSourceFile.getAbsolutePath());
        return compilationUnits.size() > 0;
    }

    public static class QNameFile
    {
        final String qName;
        final File file;
        final String locale;
        final int order;

        QNameFile(String qName, File file, String locale, int order)
        {
            this.qName = qName;
            this.file = file;
            this.locale = locale;
            this.order = order;
        }

        @Override
        public int hashCode()
        {
            return qName.hashCode() + file.hashCode();
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == this)
                return true;
            if (!(other instanceof QNameFile))
                return false;
            QNameFile otherQNameFile = (QNameFile)other;
            return qName.equals(otherQNameFile.qName) && file.equals(otherQNameFile.file);
        }
        
        @Override
        public String toString()
        {
            return "QNameFile qName:" + this.qName + " file:" + this.file;
        }
    }

    String getSourceFileFromSourcePath(String file)
    {
        String sourceFile = null;
        for (DirectoryID sourcePath : sourcePaths.keySet())
        {
            sourceFile = getSourceFileInPath(sourcePath.getFile(), file);
            if (sourceFile != null)
                break;
        }

        return sourceFile;
    }
    
    private List<QNameFile> createQNameFilesForFile(File f)
    {
        ArrayList<QNameFile> qNameFiles = new ArrayList<QNameFile>(1);
        int order = 0;
        for (Map.Entry<DirectoryID, HashSet<QNameFile>> sourcePathEntry : sourcePaths.entrySet())
        {
            DirectoryID dir = sourcePathEntry.getKey();
            String qname = computeQName(dir.getFile(), f);
            if (qname != null)
            {
                String locale = null;
                if(compilerProject instanceof IRoyaleProject)
                    locale = ((IRoyaleProject)compilerProject).getResourceLocale(dir.getFile().getAbsolutePath());
                
                QNameFile newQNameFile = new QNameFile(qname, f, locale, order);
                sourcePathEntry.getValue().add(newQNameFile);
                qNameFiles.add(newQNameFile);
            }
            ++order;
        }
        return qNameFiles;
    }

    /**
     * @param rootSourceFileName The absolute normalized file name for the root
     * source file of the new {@link QNameFile}.
     * @return A QNameFile for the rootSourceFileName, or null if it could not be computed
     */
    QNameFile computeQNameForFilename(String rootSourceFileName)
    {
        List<QNameFile> qNameFiles = createQNameFilesForFile(new File(rootSourceFileName));

        if (qNameFiles.isEmpty())
            return null;

        // If there is more than one qNameFile, just use the first one.
        return Iterables.getFirst(qNameFiles, null);
    }

    /**
     * @param path Path to search for file in.  May be null.
     * @param file Filename to search for.  Can't be null.
     * @return Full path to file.  null if not found
     */
    public static String getSourceFileInPath(File path, String file)
    {
        File sourceFile;
        if (path != null)
        {
            sourceFile = new File(path, file);
        }
        else
        {
            sourceFile = new File(file);
        }

        if (sourceFile.exists())
        {
            return FilenameNormalization.normalize(sourceFile.getAbsolutePath());
        }

        return null;
    }
    
    /**
     * 
     * @return the source path as a list of {@linkplain File}.
     */
    public List<File> getSourcePath()
    {
        List<File> paths = new ArrayList<File>(sourcePaths.keySet().size());
        for (DirectoryID path : sourcePaths.keySet())
        {
            paths.add(path.getFile());
        }
        
        return paths;
    }

    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        return Joiner.on('\n').join(Iterables.transform(sourcePaths.keySet(), new Function<DirectoryID, String>(){

            @Override
            public String apply(DirectoryID input)
            {
                return input.getFile().getAbsolutePath();
            }}));
    }
    
    
}
