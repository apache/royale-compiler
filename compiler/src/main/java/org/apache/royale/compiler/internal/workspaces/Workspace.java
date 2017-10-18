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

package org.apache.royale.compiler.internal.workspaces;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.royale.compiler.asdoc.IASDocDelegate;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.filespecs.IBinaryFileSpecification;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.caches.PackageNamespaceDefinitionCache;
import org.apache.royale.compiler.internal.definitions.references.ReferenceCache;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.mxml.MXMLDataManager;
import org.apache.royale.compiler.internal.parsing.as.NilASDocDelegate;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DependencyGraph;
import org.apache.royale.compiler.internal.projects.ASProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.units.CompilationUnitBase;
import org.apache.royale.compiler.internal.units.StringToCompilationUnitMap;
import org.apache.royale.compiler.internal.units.requests.RequestMaker;
import org.apache.royale.compiler.mxml.IMXMLDataManager;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.projects.IASProject;
import org.apache.royale.compiler.scopes.IFileScope;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit.UnitType;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.workspaces.IInvalidationListener;
import org.apache.royale.compiler.workspaces.IWorkspace;
import org.apache.royale.compiler.workspaces.IWorkspaceProfilingDelegate;
import org.apache.royale.compiler.workspaces.IInvalidationListener.InvalidatedDefinition;
import org.apache.royale.swc.ISWCManager;
import org.apache.royale.swc.SWCManager;
import org.apache.royale.utils.FilenameNormalization;

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;

/**
 * Maintains a collection of ICompilerProject's in the workspace and state that
 * is shared across multiple CompilerProject's
 */
public final class Workspace implements IWorkspace
{
    private static boolean assertionsEnabled = false;
    static
    {
        assert assertionsEnabled = true; // intentional side effect
    }

    private ExecutorService executorService;
    protected final Map<CompilerProject, Object> projects;
    private IWorkspaceProfilingDelegate profilingDelegate;
    private final Set<IInvalidationListener> invalidationListeners;
    private final SWCManager swcManager;
    private final MXMLDataManager mxmlDataManager;
    private final PackageNamespaceDefinitionCache packageNamespaceDefinitionCache;

    private final Map<String, IFileSpecification> pathToFileSpecMap;
    private final StringToCompilationUnitMap pathToCompilationUnitMapping;
    // Map of files which are included from another file to the compilation units
    // which are including the file.  This is separate from pathToCompilationUnitMapping
    // as when we lookup the compilation units to remove based on file name, we
    // should not remove compilation units which are including a file which is
    // being removed.
    private final StringToCompilationUnitMap includeFilesToIncludingCompilationUnitMapping;

    // The key of this map is also the value. As EmbedData uses the same hashCode()
    // we query if there is an equiv. EmbedData, and if there is, we return the
    // value - which is also the key!
    private final Map<EmbedData, EmbedData> embedDataCache;

    // A FinalizableReferenceQueue which is used InvisibleCompilationUnitRef to remove
    // any delegates from the project when an invisible CU is gc'd.
    private final FinalizableReferenceQueue invisibleCompilationUnitReferenceQueue;

    // This lock is needed as we can add EmbedCompilationUnits during a compile so
    // need wrap the creation/addition in EmbedCompilationUnitFactory around a lock.
    // Note that add addCompilationUnit() itself is not thread-safe at the moment,
    // and if we need to add any other compilation units in the future during compile
    // we should problem revisit this locking.
    public final ReadWriteLock embedLock;

    /**
     * Object that is used to synchronize build and file change notifications in
     * a workspace. A file change notification must not attempt to invalidate
     * compilation units or update project symbol tables while compilation doing
     * work.
     * 
     * @see IWorkspace#startBuilding()
     * @see IWorkspace#doneBuilding()
     * @see IWorkspace#startIdleState()
     * @see IWorkspace#endIdleState(Map)
     * @see #startRequest(boolean)
     * @see #endRequest()
     */
    private final BuildSynchronizationState buildSync;
    
    /**
     * {@link IASDocDelegate} that provides
     * the action script parser with call backs to generated objects
     * to contain ASDoc information.
     */
    private IASDocDelegate asDocDelegate;

    /**
     * Constructor
     */
    public Workspace()
    {
        // Limit the number of threads to the number of processors
        // If you want to run single threaded, update getNumberOfThreadToUse()
        this(new ThreadPoolExecutor(0, getNumberOfThreadToUse(),
                                    60L, TimeUnit.SECONDS,
                                    new SynchronousQueue<Runnable>(),
                                    new ThreadPoolExecutor.CallerRunsPolicy()));

//        this(Executors.newCachedThreadPool(), indexingDelegate);
    }

    /**
     * Constructor
     * 
     * @param es {@link ExecutorService} to use to do background work in this
     * workspace.
     */
    public Workspace(ExecutorService es)
    {
        executorService = es;

        profilingDelegate = null;
        invalidationListeners = new LinkedHashSet<IInvalidationListener>();

        swcManager = new SWCManager(this);
        mxmlDataManager = new MXMLDataManager();

        projects = new MapMaker().weakKeys().makeMap();
        pathToFileSpecMap = new HashMap<String, IFileSpecification>();
        pathToCompilationUnitMapping = new StringToCompilationUnitMap();
        includeFilesToIncludingCompilationUnitMapping = new StringToCompilationUnitMap();

        packageNamespaceDefinitionCache = new PackageNamespaceDefinitionCache();
        embedDataCache = new WeakHashMap<EmbedData, EmbedData>();
        embedLock = new ReentrantReadWriteLock();

        invisibleCompilationUnitReferenceQueue = new FinalizableReferenceQueue();

        asDocDelegate = NilASDocDelegate.get();
        
        buildSync = new BuildSynchronizationState();
    }

    private static int getNumberOfThreadToUse()
    {
        /*
         * OK, we needs some heuristic for how many threads to put in the work queue.
         * Our "version 1" heuristic was min ( num cores, 8).
         * That turned out to be sub-optimal. I believe there are two reasons for this:
         *      1) Often with apps like this that do a lot of disk I/O you want more threads than cores,
         *      because some threads will be waiting for disk I/O, so you need extra threads to keep the work
         *      going.
         *      2) Our current work queuing system will eat up threads as we run into compile time
         *      dependencies that fan out too far. This is perhaps a defect in the queuing system, but
         *      in any case it can be beneficial to have extra threads.
         *      
         * I tested on 1,2, and 4 core machines, and in most cases num cores was too few threads.
         * I also found that 16 was either optimal, or not noticeably worse than a smaller number.
         * 
         * TODO: we should run some more benchmark on a many core system and a many core + hyper-threading.
         * I suspect we may find that those systems can use more cores...
         */
        return 16;
    }

    /**
     * Gets the {@link ExecutorService} to use in this workspace.
     * 
     * @return The {@link ExecutorService} to use in this workspace.
     */
    public ExecutorService getExecutorService()
    {
        return executorService;
    }

    private CompilerProject[] getProjects()
    {
        return projects.keySet().toArray(new CompilerProject[0]);
    }
    
    @Override
    public void startIdleState()
    {
        buildSync.startIdleState();
    }

    @Override
    public void endIdleState(Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate)
    {
        buildSync.startAllowingFileScopeRequests();
        try
        {
            for (Entry<ICompilerProject, Set<ICompilationUnit>> e : cusToUpdate.entrySet())
            {
                ((CompilerProject)e.getKey()).updatePublicAndInternalDefinitions(e.getValue());
            }
        }
        catch (InterruptedException e1)
        {
            assert false : "unlockAndUpdateCompilationUnits() should not be interrupted";
        }
        finally
        {
            buildSync.endAllowingFileScopeRequests();
            buildSync.endIdleState();
        }
    }
    
    @Override
    public void startBuilding()
    {
        buildSync.startRequest(false);
    }

    @Override
    public void doneBuilding()
    {
        buildSync.endRequest();
    }
    
    /**
     * Called by code in {@link RequestMaker} when a request is about to be
     * submitted for execution. Blocks until the workspace has left the idle
     * state.
     * 
     * @param requestNeededForFileScope true if the request that is about to be
     * submitted for execution is needed to build an {@link IFileScope}.
     */
    public void startRequest(boolean requestNeededForFileScope)
    {
        buildSync.startRequest(requestNeededForFileScope);
    }
    
    /**
     * Called by code in {@link RequestMaker} when a request has completed execution.
     */
    public void endRequest()
    {
        buildSync.endRequest();
    }
    
    /**
     * Determines if there is any currently running build activity in the
     * workspace. This method should only be called from assert statements, it
     * is meant only for debugging.
     * 
     * @return true if there is any build activity, false otherwise.
     */
    public boolean isBuilding()
    {
        return buildSync.isBuilding();
    }

    @Override
    public void setProfilingDelegate(final IWorkspaceProfilingDelegate profilingDelegate)
    {
        this.profilingDelegate = profilingDelegate;
    }

    @Override
    public IWorkspaceProfilingDelegate getProfilingDelegate()
    {
        return profilingDelegate;
    }

    @Override
    public void addInvalidationListener(IInvalidationListener invalidationListner)
    {
        invalidationListeners.add(invalidationListner);
    }

    @Override
    public void removeInvalidationListener(IInvalidationListener invalidationListner)
    {
        invalidationListeners.remove(invalidationListner);
    }

    /**
     * @return the swcManager
     */
    @Override
    public ISWCManager getSWCManager()
    {
        return swcManager;
    }

    public PackageNamespaceDefinitionCache getPackageNamespaceDefinitionCache()
    {
        return packageNamespaceDefinitionCache;
    }

    /**
     * Close the workspace. It is illegal to use the workspace after it has been
     * closed.
     */
    public void close()
    {
        executorService.shutdown();
        executorService = null;
    }

    private final Collection<ICompilationUnit> collectAssociatedCompilationUnits(IFileSpecification file)
    {
        String filename = file.getPath();
        Collection<WeakReference<ICompilationUnit>> relatedCompilationUnits = pathToCompilationUnitMapping.getVisibleAndInvisible(filename);

        // relatedCompilationUnits should never be null, but it is OK for it to be empty, as
        // we can be null, if someone passes us in a arbitrary file which has no compilation
        // units associated with it.
        assert (relatedCompilationUnits != null) : "relatedCompilationUnits should never be null";

        // add any compilation units which include the file, as they need to be recompiled also
        Collection<WeakReference<ICompilationUnit>> includingCompilationUnits = includeFilesToIncludingCompilationUnitMapping.get(filename);

        Collection<WeakReference<ICompilationUnit>> allRelatedCompilationUnits = new HashSet<WeakReference<ICompilationUnit>>();
        allRelatedCompilationUnits.addAll(relatedCompilationUnits);
        allRelatedCompilationUnits.addAll(includingCompilationUnits);

        HashSet<ICompilationUnit> associatedCompilationUnits = new HashSet<ICompilationUnit>();
        for (WeakReference<ICompilationUnit> relatedCURef : allRelatedCompilationUnits)
        {
        	ICompilationUnit relatedCU = relatedCURef.get();
        	if (relatedCU != null)
        	{
        	    associatedCompilationUnits.add(relatedCU);
        	}
        }
        
        final Set<ICompilationUnit> associatedCompilationUnitsAccountingForConflictingDefinitions =
            ASProjectScope.getCompilationUnitsWithConflictingDefinitions(this, associatedCompilationUnits);
        
        return associatedCompilationUnitsAccountingForConflictingDefinitions;
    }

    private final void invalidate(IFileSpecification fileSpec, Collection<ICompilationUnit> compilationUnits, Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate)
    {
        mxmlDataManager.invalidate(fileSpec);
        // Tell the SWC manager the SWC file is invalid.
        getSWCManager().remove(new File(fileSpec.getPath()));
        
        if (compilationUnits.size() == 0)
            return;

        Set<ICompilationUnit> unitsToInvalidate = new HashSet<ICompilationUnit>();
        unitsToInvalidate.addAll(compilationUnits);
        Set<ICompilationUnit> unitsToClean = Sets.<ICompilationUnit>union(DependencyGraph.computeInvalidationSet(unitsToInvalidate), getCompilationUnitsDependingOnMissingDefinitions(unitsToInvalidate));

        
        notifyInvalidationListener(unitsToClean);
        
        // Do the actual invalidation
        Map<ICompilerProject, Set<File>> invalidatedSWCFiles = new HashMap<ICompilerProject, Set<File>>();
        for (ICompilationUnit compilationUnit : unitsToClean)
        {
            boolean clearCUFileScope = unitsToInvalidate.contains(compilationUnit);
            compilationUnit.clean(invalidatedSWCFiles, cusToUpdate, clearCUFileScope);
        }

        // invalidate any library files in the project
        for (Map.Entry<ICompilerProject, Set<File>> e : invalidatedSWCFiles.entrySet())
        {
            if (e.getKey() instanceof IASProject)
                ((IASProject)e.getKey()).invalidateLibraries(e.getValue());
        }
    }

    private void notifyInvalidationListener(Collection<ICompilationUnit> unitsToClean)
    {
        if (invalidationListeners.isEmpty())
            return;

        Map<ICompilerProject, Collection<InvalidatedDefinition>> invalidationMap =
                new HashMap<ICompilerProject, Collection<InvalidatedDefinition>>();

        for (ICompilationUnit compilationUnit : unitsToClean)
        {
            // Collect all definitions associated with the compilation unit
            Collection<IDefinition> definitions = compilationUnit.getDefinitionPromises();
            if (definitions.size() == 0)
            {
                // no definition promises, so getting the file scope should be cheap.
                try
                {
                    IFileScopeRequestResult fsr = compilationUnit.getFileScopeRequest().get();
                    definitions = fsr.getExternallyVisibleDefinitions();
                }
                catch (InterruptedException e1)
                {
                    assert false : "Since this is a single threaded method, we should never be interrupted";
                }
            }

            // for all the found definition, build up a map of projects to a list of InvalidatedDefinitions
            // and pass this map onto the registered invalidation listener to do with what it will
            if (definitions.size() > 0)
            {
                Collection<InvalidatedDefinition> invalidatedDefinitions = invalidationMap.get(compilationUnit.getProject());
                if (invalidatedDefinitions == null)
                {
                    invalidatedDefinitions = new LinkedList<InvalidatedDefinition>();
                    invalidationMap.put(compilationUnit.getProject(), invalidatedDefinitions);
                }

                String filename = compilationUnit.getAbsoluteFilename();
                for (IDefinition definition : definitions)
                {
                    String qName = definition.getQualifiedName();
                    InvalidatedDefinition invalidatedDefinition = new InvalidatedDefinition(qName, filename);
                    invalidatedDefinitions.add(invalidatedDefinition);
                }
            }
        }
        for (IInvalidationListener listener : invalidationListeners)
            listener.definitionsChanged(invalidationMap);
    }

    /**
     * When an ISWC has changed in memory, invalidate any compilation units which depend on the
     * units which depend on the SWC
     * 
     * @param unitsRemoved The collection of compilation units to be removed.
     * @param unitsAdded The collection compilation units to be added.
     */
    public void swcChanged(Collection<ICompilationUnit> unitsRemoved, Collection<ICompilationUnit> unitsAdded, Runnable runWhileIdle)
    {
        final Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate = new HashMap<ICompilerProject, Set<ICompilationUnit>>();
        final Set<ICompilationUnit> unitsRemoveSet = ImmutableSet.copyOf(unitsRemoved);
        startIdleState();
        try
        {
            // Find all the compilation units reference a definition with the same base
            // name as a definition defined by any of the compilation units in the SWC that is
            // changing.
            final Collection<ICompilationUnit> unitsDependingOnMissingDefinitions =
                getCompilationUnitsDependingOnMissingDefinitions(unitsAdded);
            
            // Compute the set of compilation units to invalidate by starting with the union of
            // the unitsDependingOnMissingDefinitions and the list of
            // compilation units we are removing.
            Set<ICompilationUnit> unitsToInvalidate =
                DependencyGraph.computeInvalidationSet(Iterables.concat(unitsRemoved, unitsDependingOnMissingDefinitions));
            notifyInvalidationListener(unitsToInvalidate);

            // Do the actual invalidation
            Map<ICompilerProject, Set<File>> invalidatedSWCFiles = new HashMap<ICompilerProject, Set<File>>();
            for (ICompilationUnit compilationUnit : unitsToInvalidate)
            {
                compilationUnit.clean(invalidatedSWCFiles, cusToUpdate, unitsRemoveSet.contains(compilationUnit));
            }
            
            runWhileIdle.run();
        }
        finally
        {
            endIdleState(cusToUpdate);
        }
    }

    @Override
    public void fileChanged(IFileSpecification changedFile)
    {
        // paths passed into this function need to have been normalized 
        assert (changedFile.getPath().equals(FilenameNormalization.normalize(changedFile.getPath()))) : "Path not normalized";
        Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate = new HashMap<ICompilerProject, Set<ICompilationUnit>>();
        startIdleState();
        try
        {
            Collection<ICompilationUnit> relatedCompilationUnits = collectAssociatedCompilationUnits(changedFile);
            HashSet<ICompilationUnit> compilationUnitsToInvalidate = new HashSet<ICompilationUnit>();
            compilationUnitsToInvalidate.addAll(relatedCompilationUnits);
            invalidate(changedFile, relatedCompilationUnits, cusToUpdate);

            pathToFileSpecMap.put(changedFile.getPath(), changedFile);        
        }
        finally
        {
            endIdleState(cusToUpdate);
        }
    }

    @Override
    public void fileRemoved(IFileSpecification removedFile)
    {
        // paths passed into this function need to have been normalized 
        assert (removedFile.getPath().equals(FilenameNormalization.normalize(removedFile.getPath()))) : "Path not normalized";
        final String path = removedFile.getPath();
        final Set<ASProject> affectedProjects = new HashSet<ASProject>();
        Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate = new HashMap<ICompilerProject, Set<ICompilationUnit>>();
        Collection<ICompilationUnit> relatedCompilationUnits = Collections.emptyList();
        
        startIdleState();
        try
        {
            relatedCompilationUnits = collectAssociatedCompilationUnits(removedFile);
            // collect the affected projects before invalidating the relatedCompilationUnits, as removed
            // compilation units will have their projects null'd out during invalidate, causing an NPE.
            for (ICompilationUnit compilationUnit : relatedCompilationUnits)
            {
                if (compilationUnit == null)
                    continue;

                ICompilerProject containingProject = compilationUnit.getProject();
                assert(containingProject instanceof ASProject);
                affectedProjects.add((ASProject)containingProject);
            }

            invalidate(removedFile, relatedCompilationUnits, cusToUpdate);           
        }
        finally
        {
            File f = new File(path);
            for (ASProject project : affectedProjects)
            {
                project.removeSourceFile(f);
            }

            // update the pathToCompilationUnitMapping CU by CU, rather than
            // just taking the whole path away, as we don't want to loose mappings
            // between SWC compilation units and the SWC path
            for (ICompilationUnit cu : relatedCompilationUnits)
            {
                if (cu.getCompilationUnitType() != UnitType.SWC_UNIT)
                {
                    pathToCompilationUnitMapping.remove(path, cu);
                    includeFilesToIncludingCompilationUnitMapping.remove(path, cu);
                }
            }

            pathToFileSpecMap.remove(path);

            endIdleState(cusToUpdate);
        }
    }

    @Override
    public void fileAdded(IFileSpecification addedFile)
    {
        // paths passed into this function need to have been normalized
        assert (addedFile.getPath().equals(FilenameNormalization.normalize(addedFile.getPath()))) : "Path not normalized";
        Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate = new HashMap<ICompilerProject, Set<ICompilationUnit>>();

        startIdleState();
        try
        {
            // It would be nice to be able to assert that the file being added
            // is not a known file, but this is not currently possible.  When builder project
            // settings are changed in ways such as adding a new source path, there are two notifications,
            // 1) The project has changed and Configurator.applyToProject() is called which adds any new files to the workspace.
            // 2) eclipse then sends file adds on a file which we know about because of 1).
            // Until the notification is straightened out, we can't have this assert.  This just means that
            // we're potentially doing a slightly more costly invalidation when fileAdded() is called instead
            // of fileChanged(), but as this only really happens when project settings are changed, it shouldn't
            // be a real performance hit.
            // assert (!isKnownFile(addedFile));

            String path = addedFile.getPath();
            pathToFileSpecMap.put(path, addedFile);
            getSWCManager().remove(new File(path));

            File f = new File(path);
            CompilerProject[] projects = getProjects();
            boolean compilationUnitAdded = false;
            for (CompilerProject project : projects)
            {
                compilationUnitAdded = project.handleAddedFile(f) || compilationUnitAdded;
                if (project instanceof ASProject)
                    compilationUnitAdded = ((ASProject)project).invalidateLibraries(Collections.singleton(f)) || compilationUnitAdded;
            }

            Set<ICompilationUnit> compilationUnitsToInvalidate = new HashSet<ICompilationUnit>();
            if (compilationUnitAdded)
            {
                // we now have compilation units from the newly added file, get it's
                // name, and see if there's either:
                // - any unresolved dependencies which could be resolved by this new name
                // - any compilation units which depend on the new name, and could now have
                //   an ambiguous reference
                Collection<ICompilationUnit> relatedCompilationUnits = collectAssociatedCompilationUnits(addedFile);
                compilationUnitsToInvalidate.addAll(relatedCompilationUnits);
                compilationUnitsToInvalidate.addAll(getCompilationUnitsDependingOnMissingDefinitions(relatedCompilationUnits));
            }

            // even if no compilation units were added, the added file may be a missing file which was
            // a source for an embed, so need to invalidate any CUs which have a dependency on the missing filename
            for (CompilerProject project : projects)
            {
                compilationUnitsToInvalidate.addAll(project.getDependenciesOnUnfoundReferencedSourceFile(addedFile.getPath()));
            }

            invalidate(addedFile, compilationUnitsToInvalidate, cusToUpdate);
        }
        finally
        {
            endIdleState(cusToUpdate);
        }
    }

    private Set<ICompilationUnit> getCompilationUnitsDependingOnMissingDefinitions(Collection<ICompilationUnit> addedUnits)
    {
        Set<ICompilationUnit> compilationUnitsToInvalidate = new HashSet<ICompilationUnit>();

        for (ICompilationUnit addedCompilationUnit : addedUnits)
        {
            try
            {
                CompilerProject project = (CompilerProject)addedCompilationUnit.getProject();
                List<String> newIdentifierNames = addedCompilationUnit.getShortNames();
                for (String newIdentifierName : newIdentifierNames)
                {
                    compilationUnitsToInvalidate.addAll(project.getDependenciesOnUnfoundDefinition(newIdentifierName));
                    compilationUnitsToInvalidate.addAll(project.getDependenciesOnDefinition(newIdentifierName));
                }
            }
            catch (InterruptedException e)
            {
                // should never happen, as all threads should be stopped
                e.printStackTrace();
            }
        }

        return compilationUnitsToInvalidate;
    }

    /**
     * Determine if a file is currently referenced by any part of any project
     * in the workspace.
     * 
     * @param fileSpecification An {@link IFileSpecification} that can be used
     * to get the name of the file to check.
     * @return true if the specified file is referenced by any part of any
     * project in the workspace, false otherwise.
     */
    @SuppressWarnings("unused")
    private boolean isKnownFile(IFileSpecification fileSpecification)
    {
        // make sure that nobody is calling isKnownFile outside of an assert
        if (!assertionsEnabled)
            throw new RuntimeException("isKnownFile() should only ever be called from an assert");

        // paths passed into this function need to have been normalized
        assert (fileSpecification.getPath().equals(FilenameNormalization.normalize(fileSpecification.getPath()))) : "Path not normalized";

        Collection<WeakReference<ICompilationUnit>> relatedCompilationUnits = pathToCompilationUnitMapping.get(fileSpecification.getPath());
        return (relatedCompilationUnits != null) && (relatedCompilationUnits.size() > 0);
    }

    /**
     * Gets the {@link IFileSpecification} for the root source file of the
     * specified {@link ICompilationUnit}.
     * 
     * @param compilationUnit A compilation unit.
     * @return Tthe {@link IFileSpecification} for the root source file of the
     * specified {@link ICompilationUnit}
     */
    public IFileSpecification getFileSpecificationForCompilationUnit(ICompilationUnit compilationUnit)
    {
        String path = compilationUnit.getAbsoluteFilename();
        // paths passed into this function need to have been normalized
        assert (path.equals(FilenameNormalization.normalize(path))) : "Path not normalized";
        
        // Make sure that we seen this path associated with a compilation unit before.
        assert pathToCompilationUnitMapping.get(path) != null;
        
        return getFileSpecification(path);
    }

    /**
     * Maintain a mapping between filenames and compilation units. Needed for
     * incremental compilation.
     */
    public void addCompilationUnit(ICompilationUnit compilationUnit)
    {
        String path = compilationUnit.getAbsoluteFilename();
        // paths passed into this function need to have been normalized
        assert (path.equals(FilenameNormalization.normalize(path))) : "Path not normalized";

        pathToCompilationUnitMapping.add(path, compilationUnit);
    }

    /**
     * Specifies that the specified compilation unit includes the specified list
     * of files. Called by {@link ICompilationUnit}'s when they discover
     * included files.
     * 
     * @param includingCompilationUnit {@link ICompilationUnit} that contains
     * include statements that reference the specified list of files.
     * @param includedFiles The included files.
     */
    public void addIncludedFilesToCompilationUnit(ICompilationUnit includingCompilationUnit, Collection<String> includedFiles)
    {
        includeFilesToIncludingCompilationUnitMapping.add(includedFiles.toArray(new String[includedFiles.size()]), includingCompilationUnit);
    }

    /**
     * Remove any references to the compilation unit to the collection of include files it includes.
     * 
     * @param includingCompilationUnit {@link ICompilationUnit} that contains
     * include statements that reference the specified list of files.
     * @param includedFiles The included files.
     */
    public void removeIncludedFilesToCompilationUnit(ICompilationUnit includingCompilationUnit, Collection<String> includedFiles)
    {
        for (String includedFile : includedFiles)
        {
            includeFilesToIncludingCompilationUnitMapping.remove(includedFile, includingCompilationUnit);
        }
    }

    /**
     * Remove a compilation unit from the filename to compilation unit map
     * 
     * @param compilationUnit The compilation unit to be removed.
     */
    public void removeCompilationUnit(ICompilationUnit compilationUnit)
    {
        String path = compilationUnit.getAbsoluteFilename();
        // paths passed into this function need to have been normalized
        assert (path.equals(FilenameNormalization.normalize(path))) : "Path not normalized";

        pathToCompilationUnitMapping.remove(path, compilationUnit);
        ((CompilationUnitBase)compilationUnit).clearIncludedFilesFromWorkspace();

        // only remove the file spec if there are no more remaining CUs tied
        // to that path
        if (pathToCompilationUnitMapping.get(path).isEmpty() && includeFilesToIncludingCompilationUnitMapping.get(path).isEmpty())
        {
            pathToFileSpecMap.remove(path);
        }
    }

    /**
     * Get all compilation units from the filename across all projects
     * 
     * @param path String to source filename
     */
    public Collection<WeakReference<ICompilationUnit>> getCompilationUnits(String path)
    {
        // paths passed into this function need to have been normalized
        assert (path.equals(FilenameNormalization.normalize(path))) : "Path not normalized";
        
        return pathToCompilationUnitMapping.get(path);
    }
    
    /**
     * Get all invisible compilation units from the filename across all projects
     * 
     * @param path String to source filename
     */
    public Collection<WeakReference<ICompilationUnit>> getInvisibleCompilationUnits(String path)
    {
        // paths passed into this function need to have been normalized
        assert (path.equals(FilenameNormalization.normalize(path))) : "Path not normalized";
        
        return pathToCompilationUnitMapping.getInvisible(path);
    }

    private static Collection<ICompilationUnit> getCompilationUnits(final StringToCompilationUnitMap compilationUnitMap, final String sortKey, ICompilerProject project)
    {
        Collection<WeakReference<ICompilationUnit>> compilationUnitRefs = compilationUnitMap.get(sortKey, project);
        ArrayList<ICompilationUnit> compilationUnits = new ArrayList<ICompilationUnit>(compilationUnitRefs.size());
        for (WeakReference<ICompilationUnit> cuRef : compilationUnitRefs)
        {
            final ICompilationUnit cu = cuRef.get();
            // The get method pathToCompilationUnitMapping will filter out compilation units
            // that have been removed from the project, so at this point
            // we can assert the weak references will always return non-null.
            assert cu != null
                : "ICompilerProject's dependency graph should be pinning all the compilation units in the collection.";
            compilationUnits.add(cu);
        }
        return compilationUnits;
    }
    
    private static Collection<ICompilationUnit> getInvisibleCompilationUnits(final StringToCompilationUnitMap compilationUnitMap, final String sortKey, ICompilerProject project)
    {
        Collection<WeakReference<ICompilationUnit>> compilationUnitRefs = compilationUnitMap.getInvisible(sortKey, project);
        ArrayList<ICompilationUnit> compilationUnits = new ArrayList<ICompilationUnit>(compilationUnitRefs.size());
        for (WeakReference<ICompilationUnit> cuRef : compilationUnitRefs)
        {
            final ICompilationUnit cu = cuRef.get();
            
            // Nothing in the compiler pins the invisible compilation units
            // so we have to check that the weak reference is still good.
            if (cu != null)
            {
                assert cu.isInvisible()
                    : "StringToCompilationUnitMap.getInvisible returned a visible compilation unit.";
                compilationUnits.add(cu);
            }
        }
        return compilationUnits;
    }
    
    @Override
    public Collection<ICompilationUnit> getCompilationUnits(String path, ICompilerProject project)
    {
        // paths passed into this function need to have been normalized
        assert (path.equals(FilenameNormalization.normalize(path))) : "Path not normalized";
        return getCompilationUnits(pathToCompilationUnitMapping, path, project);
    }
    
    private static Iterable<ICompilationUnit> getInvisibleAndVisibleCompilationUnits(final StringToCompilationUnitMap compilationUnitMap,
            final String sortKey, final ICompilerProject project)
    {
        final Iterable<ICompilerProject> projectIterable = Collections.singleton(project);
        final Iterable<Iterable<ICompilationUnit>> invisibleIterableIterable = Iterables.transform(projectIterable, new Function<ICompilerProject, Iterable<ICompilationUnit>>()
                {
                    @Override
                    public Iterable<ICompilationUnit> apply(final ICompilerProject input)
                    {
                        return getInvisibleCompilationUnits(compilationUnitMap, sortKey, input);
                    }
                });
        
        final Iterable<Iterable<ICompilationUnit>> visibleIterableIterable = Iterables.transform(projectIterable, new Function<ICompilerProject, Iterable<ICompilationUnit>>()
                {
                    @Override
                    public Iterable<ICompilationUnit> apply(final ICompilerProject input)
                    {
                        return getCompilationUnits(compilationUnitMap, sortKey, input);
                    }
                });
        return Iterables.concat(Iterables.concat(invisibleIterableIterable, visibleIterableIterable));
    }
    
    @Override
    public Iterable<ICompilationUnit> getInvisibleAndVisibleCompilationUnits(final String path, final ICompilerProject project)
    {
        // paths passed into this function need to have been normalized
        assert (path.equals(FilenameNormalization.normalize(path))) : "Path not normalized";
        return getInvisibleAndVisibleCompilationUnits(pathToCompilationUnitMapping, path, project);
    }

    /**
     * Get all compilation units from which the filename is included related to the specified
     * project
     * 
     * @param path String to source filename
     * @param project containing project
     */
    public Collection<ICompilationUnit> getIncludingCompilationUnits(String path, ICompilerProject project)
    {
        // paths passed into this function need to have been normalized
        assert (path.equals(FilenameNormalization.normalize(path))) : "Path not normalized";
        
        return getCompilationUnits(includeFilesToIncludingCompilationUnitMapping, path, project);     
    }
    
    /**
     * If there is an existing EmbedData which is equivalent to the data
     * EmbedData, return the equiv. otherwise return the entered EmbedData
     * @param data The embedding data.
     * @return the canonical EmbedData
     */
    public EmbedData getCanonicalEmbedData(EmbedData data)
    {
        EmbedData cachedData = embedDataCache.get(data);
        if (cachedData != null)
            return cachedData;

        embedDataCache.put(data, data);

        return data;
    }

    /**
     * Returns all EmbedDatas
     * 
     * @return map of EmbedDatas
     */
    public Map<EmbedData, EmbedData> getEmbedDatas()
    {
        return embedDataCache;
    }

    /**
     * Returns the invisibleCompilationUnitReferenceQueue which is used to remove
     * the backing Compilation Units when invisible compilation units get gc'd
     * 
     * @return the invisibleCompilationUnitReferenceQueue
     */
    public FinalizableReferenceQueue getInvisibleCompilationUnitReferenceQueue()
    {
        return invisibleCompilationUnitReferenceQueue;
    }

    @Override
    public IMXMLDataManager getMXMLDataManager()
    {
        return mxmlDataManager;
    }
    
    /**
     * Returns the most recent {@link IFileSpecification} given to the workspace
     * for a specified path.  If the workspace has not seen the specified path
     * before a new {@link FileSpecification} is returned.
     * @param path Path for which a {@link IFileSpecification} should be returned.
     * @return The most recent {@link IFileSpecification} given to the workspace
     * for a specified path.
     */
    @Override
    public synchronized IFileSpecification getFileSpecification(String path)
    {
        assert (path.equals(FilenameNormalization.normalize(path))) : "Path not normalized";

        IFileSpecification fileSpec = pathToFileSpecMap.get(path);
        if (fileSpec == null)
        {
            fileSpec = new FileSpecification(path);
            pathToFileSpecMap.put(path, fileSpec);
        }

        return fileSpec;
    }
    
    @Override
    public IWorkspace getWorkspace()
    {
        return this;
    }

    /**
     * Returns the most recent {@link IBinaryFileSpecification} given to the workspace
     * for a specified path.  If the workspace has not seen the specified path
     * before a new {@link IBinaryFileSpecification} is returned.
     * This method assumes that if there is an existing filespec, it is a binary
     * filespec.
     * @param path Path for which a {@link IBinaryFileSpecification} should be returned.
     * @return The most recent {@link IBinaryFileSpecification} given to the workspace
     * for a specified path.
     */
    public synchronized IBinaryFileSpecification getLatestBinaryFileSpecification(String path)
    {
        IFileSpecification fileSpec = getFileSpecification(path);

        IBinaryFileSpecification binaryFileSpec = null;
        if (fileSpec instanceof IBinaryFileSpecification)
        {
            binaryFileSpec = (IBinaryFileSpecification)fileSpec;
        }
        else
        {
            // TODO: may need to handle this case, and replace with a binary filespec, but for
            // now assert, and cross that bridge if we get to it.
            assert false : "requesting binary fileSpec but existing fileSpec not of binary type";
        }

        return binaryFileSpec;
    }

    private ReferenceCache refCache = new ReferenceCache();


    /**
     * Get the ReferenceCache used by this workspace.  This is to facilitate caching
     * of IReference objects across the workspace, as many references will be repeated
     * throughout a workspace.  Since the IReferences are immutable, they need not be tied
     * to a particular Project
     * @return  the ReferenceCache this workspace is using.
     */
    public ReferenceCache getReferenceCache()
    {
        return refCache;
    }

    /**
     * Delete the reference of compiler project from the workspace
     * 
     * @param compilerProject ICompilerProject
     */
    public void deleteProject(ICompilerProject compilerProject)
    {
        projects.remove(compilerProject);
    }

    @Override
    public IASDocDelegate getASDocDelegate()
    {
        return asDocDelegate;
    }

    @Override
    public void setASDocDelegate(IASDocDelegate asDocDelegate)
    {
        assert asDocDelegate != null : "ASDoc delegate can not be null, use default implementation instead!";
        this.asDocDelegate = asDocDelegate;
    }
    
    /**
     * Class that keeps track of:
     * <ul>
     * <li>how many build operations are currently pending</li>
     * <li>which threads have requested the idle state</li>
     * <li>which thread currently owns the idle state</li>
     * <li>whether or not build operations that build file scopes should be
     * allowed to proceed even if a thread owns the idle state.</li>
     */
    private static final class BuildSynchronizationState
    {
        private final ReentrantLock lock;
        private final Condition condition;
        /**
         * Used to allow pairs of startIdleState/endIdleState to nest.
         *
         */
        private int idleStateCount;
        /**
         * Count of how many build operations are currently pending.
         */
        private int activitiyCount;
        /**
         * True if build operations needed to build file scopes are allowed to
         * proceed.
         */
        private boolean allowFileScopeRequests;
        /**
         * Queue of threads that have requested to own the idle state.
         */
        private final LinkedList<Thread> threadsRequestingIdle;
        
        BuildSynchronizationState()
        {
            lock = new ReentrantLock();
            condition = lock.newCondition();
            threadsRequestingIdle = new LinkedList<Thread>();
        }
        
        void startRequest(boolean isFileScopeRequest)
        {
            lock.lock();   
            try
            {
                while (!canStartRequest(isFileScopeRequest))
                    condition.awaitUninterruptibly();
                ++activitiyCount;
            }
            finally
            {
                lock.unlock();
            }
        }
        
        void endRequest()
        {
            lock.lock();
            try
            {
                assert activitiyCount > 0;
                --activitiyCount;
                condition.signalAll();
            }
            finally
            {
                lock.unlock();
            }
        }
        
        void startIdleState()
        {
            lock.lock();
            try
            {
                final Thread currentThread = Thread.currentThread();
                final Thread currentIdleThread = threadsRequestingIdle.peekFirst();
                if (currentIdleThread == currentThread)
                {
                    assert idleStateCount > 0;
                    ++idleStateCount;
                    assert idleStateCount > 0;
                    return;
                }
                
                threadsRequestingIdle.add(currentThread);
                
                while ((activitiyCount > 0) || (threadsRequestingIdle.getFirst() != currentThread))
                    condition.awaitUninterruptibly();
                
                assert idleStateCount == 0;
                assert activitiyCount == 0;
                assert threadsRequestingIdle.getFirst() == currentThread;
                idleStateCount = 1;
                
            }
            finally
            {
                lock.unlock();
            }
        }
        
        void startAllowingFileScopeRequests()
        {
            lock.lock();
            try
            {
                final Thread currentThread = Thread.currentThread();
                final Thread currentIdleThread = threadsRequestingIdle.peekFirst();
                assert currentThread == currentIdleThread;
                assert !allowFileScopeRequests;
                allowFileScopeRequests = true;
                condition.signalAll();
            }
            finally
            {
                lock.unlock();
            }
        }
        
        void endAllowingFileScopeRequests()
        {
            lock.lock();
            try
            {
                final Thread currentThread = Thread.currentThread();
                final Thread currentIdleThread = threadsRequestingIdle.peekFirst();
                assert currentThread == currentIdleThread;
                assert allowFileScopeRequests;
                while (activitiyCount > 0)
                    condition.awaitUninterruptibly();
                allowFileScopeRequests = false;
            }
            finally
            {
                lock.unlock();
            }
        }
        
        void endIdleState()
        {
            lock.lock();
            try
            {
                final Thread currentThread = Thread.currentThread();
                assert currentThread == threadsRequestingIdle.getFirst();

                assert !allowFileScopeRequests;
                assert idleStateCount > 0;
                assert activitiyCount == 0;
                --idleStateCount;
                assert idleStateCount >= 0;
                if (idleStateCount == 0)
                {
                    threadsRequestingIdle.remove();
                    assert currentThread != threadsRequestingIdle.peekFirst();
                    condition.signalAll();
                }
            }
            finally
            {
                lock.unlock();
            }
        }
        
        boolean isBuilding()
        {
            lock.lock();
            try
            {
                return (!allowFileScopeRequests) && (activitiyCount > 0);
            }
            finally
            {
                lock.unlock();
            }
        }
        
        private boolean canStartRequest(boolean isFileScopeRequest)
        {
            if (allowFileScopeRequests)
            {
                assert idleStateCount > 0 : "workspace must be in an idle state if only file scope requests are allowed to proceed.";
                assert !threadsRequestingIdle.isEmpty();
                
                if (isFileScopeRequest)
                    return true;
                else
                    return false;
            }
            // A request can start if there is already at least on requesting running
            // or if no thread has requested the workspace to enter the idle state.
            return (activitiyCount > 0) || (threadsRequestingIdle.isEmpty());
        }
    }
    
    public void addProject(CompilerProject project)
    {
        // Need to give a non-null value, the class object for Object
        // is a good a non-value as anything.
        projects.put(project, Object.class);
    }
}
