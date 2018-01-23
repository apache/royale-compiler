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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.asdoc.IASDocBundleDelegate;
import org.apache.royale.compiler.common.IFileSpecificationGetter;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.projects.SourcePathManager.QNameFile;
import org.apache.royale.compiler.internal.units.CompilationUnitBase;
import org.apache.royale.compiler.internal.units.InvisibleCompilationUnit;
import org.apache.royale.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.IASProject;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.IInvisibleCompilationUnit;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCFileEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Implementation base class to implement functionality common to all
 * IASProject implementations. DefinitionPromises ( aka
 * "provisional definitions" ) are added to the project scope in this class.
 */
public abstract class ASProject extends CompilerProject implements IASProject
{

    /**
     * Constructor
     * 
     * @param workspace The Workspace in which this project lives.
     * @param useAS3 Indicate whether or not the AS3 namespace should
     * be opened in all compilation units in the project.
     */
    public ASProject(Workspace workspace, boolean useAS3)
    {
        this(workspace, useAS3, IASDocBundleDelegate.NIL_DELEGATE);
    }
    
    public ASProject(Workspace workspace, boolean useAS3, IASDocBundleDelegate asDocBundleDelegate)
    {
        super(workspace, useAS3);
        sourcePathManager = new SourcePathManager(this);
        sourceListManager = new SourceListManager(this, sourcePathManager);
        sourceCompilationUnitFactory = new SourceCompilationUnitFactory(this);
        sourceCompilationUnitFactory.addHandler(ASSourceFileHandler.INSTANCE);
        sourceCompilationUnitFactory.addHandler(FXGSourceFileHandler.INSTANCE);
        libraryPathManager = new LibraryPathManager(this);
        projectDependencies = new HashMap<IASProject, String>();
        dependingProjects = new HashSet<IASProject>();
        this.asDocBundleDelegate = asDocBundleDelegate;
    }

    private final SourceListManager sourceListManager;
    private final SourcePathManager sourcePathManager;
    private final SourceCompilationUnitFactory sourceCompilationUnitFactory;
    private final LibraryPathManager libraryPathManager;
    private final Map<IASProject, String> projectDependencies;
    private final Set<IASProject> dependingProjects;
    private final IASDocBundleDelegate asDocBundleDelegate;
    private int compatibilityVersionMajor;
    private int compatibilityVersionMinor;
    private int compatibilityVersionRevision;

    @Override
    public List<ISWC> getLibraries()
    {
        final ImmutableList<ISWC> result = new ImmutableList.Builder<ISWC>()
        .addAll(libraryPathManager.getLibrarySWCs())
        .build();

        return result;
    }

    @Override
    public void attachInternalLibrarySourcePath(File library, File sourceDir)
    {
        libraryPathManager.setLibrarySourcePath(library, sourceDir);
    }

    @Override
    public void attachExternalLibrarySourcePath(File library, File sourceDir)
    {
        // TODO: rename this and attachInternalLibraySourcePath to be
        // just attachLibrarySourcePath.
        libraryPathManager.setLibrarySourcePath(library, sourceDir);
    }
    
    @Override
    public String getAttachedSourceDirectory(String libraryFilename)
    {
        String result = libraryPathManager.getAttachedSourceDirectory(libraryFilename);
        return result;
    }
    
    @Override
    public void setSourcePath(List<File> paths)
    {
        sourcePathManager.setSourcePath(paths.toArray(new File[paths.size()]));
    }

    @Override
    public List<File> getSourcePath()
    {
        return sourcePathManager.getSourcePath();
    }
    
    /**
     * Adds a file to the project if that file is on the project's
     * source path.
     * @param f The file to be added.
     * @return true if any new {@link ICompilationUnit}'s were created
     * as a result of this operation, false otherwise.
     */
    public boolean addSourcePathFile(File f)
    {
        return sourcePathManager.addFile(f);
    }

    /**
     * Test if a file is on the source path or not.
     * 
     * @param f The file to test. May not be null.
     * @return true if the file is on the source path, false otherwise.
     */
    public boolean isFileOnSourcePath(File f)
    {
        return sourcePathManager.isFileOnSourcePath(f);
    }

    @Override
    public void setIncludeSources(File files[]) throws InterruptedException
    {
        sourceListManager.setSourceList(files);
    }

    @Override
    public void addIncludeSourceFile(File file) throws InterruptedException
    {
        addIncludeSourceFile(file, false);
    }
    
    @Override
    public void addIncludeSourceFile(File file, boolean overrideSourcePath) throws InterruptedException
    {
        sourceListManager.addSource(file, overrideSourcePath);
    }

    @Override
    public void removeIncludeSourceFile(File file) throws InterruptedException
    {
        sourceListManager.removeSource(file);
    }

    /**
     * Removes a source file to the project, removing any references in the various
     * managers - SourcePathManager, SourceListManager etc
     * 
     * @see #addIncludeSourceFile(File)
     * @param f File to remove.
     */
    public void removeSourceFile(File f)
    {
        try
        {
            // TODO: this is mostly a place holder for now.  Need
            // to go through and cleanup the whole removal flow

            // note that the assumption is that these functions
            // will fail gracefully if the file is not included 
            // in the various managers

            // remove any reference to this file in the
            // source list manager
            sourceListManager.removeSource(f);

            // remove any reference to this file in the
            // source path manager
            sourcePathManager.removeFile(f);

        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public SourceCompilationUnitFactory getSourceCompilationUnitFactory()
    {
        return sourceCompilationUnitFactory;
    }
    
    /**
     * Called by {@link SourcePathManager} or {@link LibraryPathManager} when the source
     * or library path change.
     * @param toRemove {@link ICompilationUnit}'s to remove from the project.
     * @param toAdd {@link ICompilationUnit}'s to add to the project.
     */
    void updateCompilationUnitsForPathChange(Collection<ICompilationUnit> toRemove, Collection<ICompilationUnit> toAdd)
    {
        assert toRemove != null;
        assert toAdd != null;
        removeCompilationUnits(toRemove);
        addCompilationUnits(toAdd);

        for (ICompilationUnit unit : toAdd)
        {
            for (IDefinition definitionPromise : unit.getDefinitionPromises())
            {
                getScope().addDefinition(definitionPromise);
            }
        }
    }

    @Override
    public Set<IASProject> getDependingProjects()
    {
        return dependingProjects;
    }

    @Override
    public void addProjectDependeny(IASProject project, String swcFilename)
    {
        assert (project instanceof ASProject) : "project dependency must be of type ASProject";
        ((ASProject)project).dependingProjects.add(this);
        projectDependencies.put(project, swcFilename);
    }

    @Override
    public void removeProjectDependeny(IASProject project)
    {
        assert (project instanceof ASProject) : "project dependency must be of type ASProject";
        ((ASProject)project).dependingProjects.remove(this);
        projectDependencies.remove(project);
    }

    @Override
    public void setDependencies(Map<IASProject, String> newIProjectsDependencies)
    {
        Set<IASProject> newProjectDependencies = ImmutableSet.<IASProject> copyOf(newIProjectsDependencies.keySet());
        Set<IASProject> dependenciesToRemove = ImmutableSet.<IASProject> copyOf(Sets.difference(projectDependencies.keySet(), newProjectDependencies));
        Set<IASProject> dependenciesToAdd = ImmutableSet.<IASProject> copyOf(Sets.difference(newProjectDependencies, projectDependencies.keySet()));

        for (IASProject projectDependency : dependenciesToRemove)
        {
            removeProjectDependeny(projectDependency);
        }

        for (IASProject projectDependency : dependenciesToAdd)
        {
            String swcFilename = newIProjectsDependencies.get(projectDependency);
            addProjectDependeny(projectDependency, swcFilename);
        }
    }

    /**
     * Called by {@link SourceListManager} when the include sources list is changed.
     * @param toRemove {@link ICompilationUnit}'s to remove from the project.
     * @param toAdd {@link ICompilationUnit}'s to add to the project.
     */
    void sourceListChange(Collection<ICompilationUnit> toRemove, Collection<ICompilationUnit> toAdd) throws InterruptedException
    {
        // passing empty lists to removeCompilationUnits
        // and addCompilationUnitsAndUpdateDefinitions *is* harmless
        // but avoiding the call to sourcePathManager.sourceListChanged
        // will prevent us from enumerating a bunch of directories on disk
        if ((!toRemove.isEmpty()) || (!toAdd.isEmpty()))
        {
            removeCompilationUnits(toRemove);
            addCompilationUnitsAndUpdateDefinitions(toAdd);
        }
    }

    @Override
    public void collectProblems(Collection<ICompilerProblem> problems)
    {
        sourcePathManager.collectProblems(problems);
        sourceListManager.collectProblems(problems);
        libraryPathManager.collectProblems(problems);
        collectConfigProblems(problems);
    }

    /**
     * Finds all the {@link ICompilationUnit}'s in this project whose root source file is the specified file.
     */
    public void collectionCompilationUnitsForRootSourceFile(File rootSourceFile, Collection<ICompilationUnit> units)
    {
        sourcePathManager.collectionCompilationUnitsForRootSourceFile(rootSourceFile, units);
        sourceListManager.collectionCompilationUnitsForRootSourceFile(rootSourceFile, units);
    }
    
    public boolean hasCompilationUnitForRootSourceFile(File rootSourceFile)
    {
        return sourcePathManager.hasCompilationUnitsForRootSourceFile(rootSourceFile) ||
               sourceListManager.hasCompilationUnitsForRootSourceFile(rootSourceFile);
    }

    @Override
    public boolean invalidateLibraries(Collection<File> swcFiles)
    {
        return libraryPathManager.invalidate(swcFiles);
    }

    @Override
    public void invalidateLibrary(ISWC swc)
    {
        libraryPathManager.invalidate(swc);
    }

    @Override
    public boolean handleAddedFile(File addedFile)
    {
        return addSourcePathFile(addedFile);
    }

    @Override
    public String getSourceFileFromSourcePath(String file)
    {
        return sourcePathManager.getSourceFileFromSourcePath(file);
    }

    /**
     * Iterate through the library path list looking for the specified file.
     * @param file to look for
     * @return ISWCFileEntry to the file, or null if file not found
     */
    public ISWCFileEntry getSourceFileFromLibraryPath(String file)
    {
        return libraryPathManager.getFileEntryFromLibraryPath(file);
    }

    public IASDocBundleDelegate getASDocBundleDelegate()
    {
        return asDocBundleDelegate;
    }
    
    @Override
    public void setLibraries(List<File> libraries)
    {
        assert libraries != null : "Libraries may not be null";

        libraryPathManager.setLibraryPath(libraries.toArray(new File[libraries.size()]));
    }

    /**
     * Sets the SDK compatibility version. For this release, the only valid value is 2.0.1.
     * 
     * @param major The major version. For this release, this value must be 2.
     * @param minor The minor version. For this release, this value must be 0.
     * @param revision For this release, this value must be 1.
     */
    public void setCompatibilityVersion(int major, int minor, int revision)
    {
        compatibilityVersionMajor = major;
        compatibilityVersionMinor = minor;
        compatibilityVersionRevision = revision;
        clean();
    }

    @Override
    public Integer getCompatibilityVersion()
    {
        int compatibilityVersion = (compatibilityVersionMajor << 24) + (compatibilityVersionMinor << 16) +
                                   compatibilityVersionRevision;
        
        return compatibilityVersion != 0 ? compatibilityVersion : null;
    }

    @Override
    public String getCompatibilityVersionString()
    {
        int sum = compatibilityVersionMajor + compatibilityVersionMinor + compatibilityVersionRevision;

        if (sum == 0)
            return null;

        return compatibilityVersionMajor + "." + 
               compatibilityVersionMinor + "." + 
               compatibilityVersionRevision;
    }

    @Override
    public IInvisibleCompilationUnit createInvisibleCompilationUnit(String rootSourceFile, IFileSpecificationGetter fileSpecGetter)
    {
        QNameFile qNameFile = sourcePathManager.computeQNameForFilename(rootSourceFile);
        if (qNameFile == null)
            return null;

        CompilationUnitBase invisibleCUDelegate = createInvisibleCompilationUnit(qNameFile);
        if (invisibleCUDelegate == null)
            return null;

        return new InvisibleCompilationUnit(invisibleCUDelegate, fileSpecGetter);
    }

    @Override
    public IInvisibleCompilationUnit createInvisibleCompilationUnit(String rootSourceFile, IFileSpecificationGetter fileSpecGetter, String qName)
    {
        QNameFile qNameFile = new QNameFile(qName, new File(rootSourceFile), null, 0);
        CompilationUnitBase invisibleCUDelegate = createInvisibleCompilationUnit(qNameFile);
        if (invisibleCUDelegate == null)
            return null;

        return new InvisibleCompilationUnit(invisibleCUDelegate, fileSpecGetter);
    }

    /**
     * Create's a new {@link CompilationUnitBase} that is marked as being a
     * delegate for an {@link InvisibleCompilationUnit}.
     * 
     * @param qNameFile The qNameFile of the new {@link InvisibleCompilationUnit}.
     * @return A new {@link CompilationUnitBase} that is marked as being a
     * delegate for an {@link InvisibleCompilationUnit} or null if the specified
     * file name is not on the source path.
     */
    private CompilationUnitBase createInvisibleCompilationUnit(QNameFile qNameFile)
    {
        SourceCompilationUnitFactory compilationUnitFactory = getSourceCompilationUnitFactory();
        if (!compilationUnitFactory.canCreateCompilationUnit(qNameFile.file))
            return null;

        CompilationUnitBase newCU = (CompilationUnitBase)getSourceCompilationUnitFactory().createCompilationUnit(
                qNameFile.file, DefinitionPriority.BasePriority.SOURCE_PATH, 0, qNameFile.qName, qNameFile.locale);
        assert newCU != null : "canCreateCompilationUnit should have returned false if createCompilationUnit returns null!";
        addCompilationUnit(newCU);
        return newCU;
    }

    @Override
    public boolean isAssetEmbeddingSupported()
    {
        return true;
    }
    
    @Override
    public boolean isSupportedSourceFileType(String fileExtension)
    {
        return sourceCompilationUnitFactory.canCreateCompilationUnitForFileType(fileExtension);
    }
}
