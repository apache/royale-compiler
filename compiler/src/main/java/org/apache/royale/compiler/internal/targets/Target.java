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

package org.apache.royale.compiler.internal.targets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.exceptions.BuildCanceledException;
import org.apache.royale.compiler.internal.graph.LinkReportWriter;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DependencyGraph;
import org.apache.royale.compiler.internal.projects.LibraryPathManager;
import org.apache.royale.compiler.internal.resourcebundles.ResourceBundleUtils;
import org.apache.royale.compiler.internal.units.SWCCompilationUnit;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.ResourceBundleNotFoundForLocaleProblem;
import org.apache.royale.compiler.problems.ResourceBundleNotFoundProblem;
import org.apache.royale.compiler.problems.UnableToCreateLinkReportProblem;
import org.apache.royale.compiler.targets.ITarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetReport;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCLibrary;
import org.apache.royale.swc.ISWCManager;
import org.apache.royale.swc.ISWCScript;
import org.apache.royale.utils.FileID;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Abstract base class to help subclasses implement ITarget.
 */
public abstract class Target implements ITarget
{
    /**
     * Find all the externally visible definitions in the given compilation unit
     * list.
     * 
     * @param compilationUnits A collection of compilation units.
     * @return All the externally visible definitions in the given compilation
     * unit list.
     * @throws InterruptedException Concurrency error.
     */
    public static ImmutableList<IDefinition> getAllExternallyVisibleDefinitions(
            final Collection<ICompilationUnit> compilationUnits)
            throws InterruptedException
    {
        assert compilationUnits != null : "Expected collection of compilation units.";

        final ImmutableList.Builder<IDefinition> builder = new ImmutableList.Builder<IDefinition>();
        for (final ICompilationUnit compilationUnit : compilationUnits)
        {
            final IFileScopeRequestResult result = compilationUnit.getFileScopeRequest().get();
            builder.addAll(result.getExternallyVisibleDefinitions());
        }
        return builder.build();
    }

    /**
     * Constructor
     * 
     * @param project {@link CompilerProject} that this target will be a part of.
     * @param targetSettings {@link ITargetSettings} for this target
     * @param progressMonitor {@link ITargetProgressMonitor} that will receive
     * progress information. Can be null, in which case no progress information
     * will be collected.
     */
    protected Target(CompilerProject project, ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor)
    {
        this.project = project;
        this.progressMonitor = progressMonitor;
        this.targetSettings = targetSettings;
 
        if (targetSettings.getASMetadataNames() != null)
            this.addASMetadataNames(targetSettings.getASMetadataNames());
    }

    protected final CompilerProject project;
    protected final ITargetProgressMonitor progressMonitor;
    protected final ITargetSettings targetSettings;

    private LinkageChecker linkageChecker;
    private Set<String> metadataNames;
    private BuiltCompilationUnitSet builtCompilationUnits;
    
    /**
     * Lazily initialized {@link Iterable} of fatal {@link ICompilerProblem}s
     */
    private Iterable<ICompilerProblem> fatalProblems;
    
    /**
     * Lazily initialized target report.
     */
    private ITargetReport targetReport;
    
    
    /**
     * Discovers dependent compilation units from a set of root compilation
     * units. The return collection includes all compilation units in the
     * argument collection.
     * <p>
     * This method delegates the dependency discovery to
     * {@link Target#getDependentCompilationUnits}. Subclasses should override
     * this method if they have more sophisticated dependency discovery
     * algorithms.
     * 
     * @param compilationUnits {@link ICompilationUnit}s known to be linked into
     * the target. Dependent compilation units will be added to this method.
     * @param problems Problems building compilation units.
     * @return All compilation units to link into the target. This collection
     * includes all elements in the {@code compilationUnits} collection from
     * argument.
     * @throws InterruptedException Concurrency error.
     */
    protected Set<ICompilationUnit> findAllCompilationUnitsToLink(final Collection<ICompilationUnit> compilationUnits,
                                                                  final Collection<ICompilerProblem> problems)
                                                                  throws InterruptedException
    {
        assert compilationUnits != null : "compilation units can't be null";
        assert problems != null : "problems can't be null";

        final Set<ICompilationUnit> allCompilationUnitsInTarget =
                new HashSet<ICompilationUnit>(compilationUnits);
        final Set<ICompilationUnit> dependencies =
                getDependentCompilationUnits(allCompilationUnitsInTarget, problems);
        allCompilationUnitsInTarget.addAll(dependencies);
        return allCompilationUnitsInTarget;
    }
    
    /**
     * Waits for the specified {@link ICompilationUnit} to finish building and
     * add any problems found in the specified {@link ICompilationUnit} to the
     * specified {@link Collection}.
     * <p>
     * This method exists for the sole purpose of allowing the
     * {@link RoyaleLibrarySWFTarget} to filter out
     * {@link ResourceBundleNotFoundProblem}s and
     * {@link ResourceBundleNotFoundForLocaleProblem}s from
     * {@link SWCCompilationUnit}'s that are externally linked.
     * <p>
     * If we rip out support for Flex or if we are willing to report missing
     * resource bundles from external SWCs when linking a SWC, this method can
     * be inlined at its call site.
     * <p>
     * If we plan on continuing to support Flex, a better way to do this would
     * be to have the {@link IOutgoingDependenciesRequestResult} interface have method
     * to get all the resource bundles referenced by an {@link ICompilationUnit}
     * and wait to do the final resolution of resource bundles in {@link Target}
     * or one of its sub-classes.
     * 
     * @param cu
     * @param problems
     * @throws InterruptedException
     */
    protected void waitForCompilationUnitToFinish(final ICompilationUnit cu, final Collection<ICompilerProblem> problems) throws InterruptedException
    {
        cu.waitForBuildFinish(problems, getTargetType());
    }

    /**
     * Triggers all the request method on all the compilation units and collect
     * problems from the project and the compilation units.
     * <p>
     * Although CSS can introduce dependent classes to be linked into the
     * target, the order of root classes and the dependencies doesn't matter. As
     * a result, there's no need to add dependency edges for
     * {@link DependencyGraph#topologicalSort()}.
     * 
     * @param problems Problems will be returned here.
     * @param compilationUnits Compilation units to compile.
     * @throws InterruptedException
     */
    private void buildCompilationUnits(
            final Collection<ICompilationUnit> compilationUnits,
            final Collection<ICompilerProblem> problems)
            throws InterruptedException
    {
        assert compilationUnits != null : "Expected compilation units.";
        assert problems != null : "Expected problem collection.";

        project.collectProblems(problems);
        
        // Parallelize the building of compilation units.
        for (final ICompilationUnit cu : compilationUnits)
        {
            if(isCanceled())
                throw new BuildCanceledException();

            cu.startBuildAsync(getTargetType());
        }

        int numCompilationUnit = compilationUnits.size();
        
        //As described in #updateProgress(int, int, int), each compilation unit 
        //is considered to have two steps. The first step is considered completed 
        //at the end of handle semantic analysis and the second is considered 
        //completed when a compilation unit is fully compiled. We know that at 
        //this moment, semantic analysis phase of all the known compilation units 
        //would be completed successfully. Therefore, we assume that half of the 
        //work we need to do in order to compile all the compilation units has 
        //been done. Therefore, set the value to be the number of compilation unit 
        //will be included in the build process.
        int totalCompUnitWorkCompleted = compilationUnits.size();

        // Wait for all compilation units to finish building.
        for (final ICompilationUnit cu : compilationUnits)
        {
            waitForCompilationUnitToFinish(cu, problems);
            
            //Update the progress as we finished compiling a compilation unit.
            if(!updateProgress(numCompilationUnit, totalCompUnitWorkCompleted++, 95))
                return;
        }
    }

    @Override
    public ITargetSettings getTargetSettings()
    {
        return targetSettings;
    }

    protected abstract ITargetReport computeTargetReport() throws InterruptedException;
    
    @Override
    public final ITargetReport getTargetReport() throws InterruptedException
    {
        if (targetReport == null)
        {
            project.getWorkspace().startBuilding();
            try
            {
                targetReport = computeTargetReport();
            }
            finally
            {
                project.getWorkspace().doneBuilding();
            }
        }
        return targetReport;
    }
    
    /**
     * Discovers all the compilation units (roots and dependencies) that will be
     * linked into the target. Then builds these compilation units for all
     * phases of processing.
     * 
     * @return A {@link BuiltCompilationUnitSet} that contains information about
     * all the {@link ICompilationUnit}s whose output contribute to this target
     * and {@link ICompilerProblem}s discovered while building all the
     * {@link ICompilationUnit}s.
     * @throws InterruptedException Concurrency error.
     */
    protected BuiltCompilationUnitSet buildAllCompilationUnits() throws InterruptedException
    {
        Iterable<ICompilerProblem> fatalProblems = getFatalProblems();
        if (!Iterables.isEmpty(fatalProblems))
            return new BuiltCompilationUnitSet(ImmutableSet.<ICompilationUnit>of(), Collections.<ICompilerProblem>emptyList());
        final HashSet<ICompilationUnit> compilationUnits = new HashSet<ICompilationUnit>();
        final ArrayList<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        RootedCompilationUnits rootedCompilationUnits = getRootedCompilationUnits();
        compilationUnits.addAll(rootedCompilationUnits.getUnits());
        compilationUnits.addAll(findAllCompilationUnitsToLink(compilationUnits, problems));
        buildCompilationUnits(compilationUnits, problems);
        
        return new BuiltCompilationUnitSet(ImmutableSet.<ICompilationUnit>copyOf(compilationUnits), problems);
    }
    
    protected BuiltCompilationUnitSet getBuiltCompilationUnitSet() throws InterruptedException
    {
        if (builtCompilationUnits == null)
            builtCompilationUnits = buildAllCompilationUnits();
        return builtCompilationUnits;
    }

    /**
     * Computes the set of all all {@link ICompilationUnit}'s whose output is
     * part of the output of this target. This method does <b>NOT</b> compute
     * the dependencies introduced in CSS.
     * 
     * @param compilationUnits A collection of root compilation units.
     * @param problems Problems building compilation units.
     * @return Set of all {@link ICompilationUnit}'s whose output is part of the
     * output of this target. The order of compilation units in this function
     * does not matter because we topologically sort the dependency graph later
     * when we are getting ready to add tags to the output SWF or SWC.
     * @throws InterruptedException
     */
    protected final Set<ICompilationUnit> getDependentCompilationUnits(
            final Collection<ICompilationUnit> compilationUnits,
            final Collection<ICompilerProblem> problems)
            throws InterruptedException
    {
       
        final HashSet<ICompilationUnit> workSet = new HashSet<ICompilationUnit>();
        final HashSet<ICompilationUnit> visitedSet = new HashSet<ICompilationUnit>();

        int numCompUnitRemoved = 0;
        
        workSet.addAll(compilationUnits);
        while (workSet.size() > 0)
        {
            final ICompilationUnit currentUnit = workSet.iterator().next();
            workSet.remove(currentUnit);

            if (visitedSet.add(currentUnit))
            {
                //Increment num of the comp units removed from the workset, 
                //so that we can calculate the total number of compilation units.
                //The reason we only increment it in this if block is that if 
                //we don't hit in this if block, it means that we had removed 
                //the same compilation unit before anyways, so we already 
                //have taken it into account.
                numCompUnitRemoved++;
                
                currentUnit.startBuildAsync(getTargetType());
                 
                final DirectDependencies currentUnitDependencies = getDirectDependencies(currentUnit);
                final Iterable<ICompilationUnit> newCompilationUnitWork = currentUnitDependencies.dependencies;
                Iterables.addAll(problems, currentUnitDependencies.problems);
                    
                for (ICompilationUnit cu : newCompilationUnitWork)
                {
                    workSet.add(cu);
                    cu.startBuildAsync(getTargetType());
                }
                
                
                //Update the progress since we completed semantic analysis for one 
                //compilation unit and possibly found more compilation units.
                if(!updateProgress((workSet.size()+numCompUnitRemoved), visitedSet.size(), 50))
                    return Collections.emptySet();
            }
        }
        TreeSet<ICompilationUnit> sortedSet = new TreeSet<ICompilationUnit>(new Comparator<ICompilationUnit>()
        {
            @Override
            public int compare(ICompilationUnit o1, ICompilationUnit o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        sortedSet.addAll(visitedSet);
//        System.out.println("visited set");
//        for (ICompilationUnit visited : sortedSet)
//        	System.out.println(visited.getName());
//        System.out.println("end visited set");
        return sortedSet;
    }
    
    protected DirectDependencies getDirectDependencies(ICompilationUnit cu) throws InterruptedException
    {
        final Set<ICompilationUnit> dependencies = project.getDependencies(cu);
        return new DirectDependencies(dependencies, Collections.<ICompilerProblem>emptyList());
    }
    
    
    /**
     * @return All the reachable compilation units in this job.
     */
    public ImmutableList<ICompilationUnit> getReachableCompilationUnits(Collection<ICompilerProblem> problems) throws InterruptedException
    {
        RootedCompilationUnits rootedCompilationUnits = getRootedCompilationUnits();
        final Set<ICompilationUnit> root = rootedCompilationUnits.getUnits();
        Iterables.addAll(problems, rootedCompilationUnits.getProblems());
        final List<ICompilationUnit> reachableCompilationUnitsInSWFOrder = project.getReachableCompilationUnitsInSWFOrder(root);
        final ImmutableList<ICompilationUnit> compilationUnits = ImmutableList.<ICompilationUnit> copyOf(reachableCompilationUnitsInSWFOrder);
        return compilationUnits;
    }

    /**
     * Create a link report at the path setup in the 
     * targetSettings.getLinkReportPath. This method may be called after a 
     * target has been built.
     * 
     * @param problems
     * @throws InterruptedException
     */
    protected void createLinkReport(Collection<ICompilerProblem> problems) throws InterruptedException
    {
        if (targetSettings.getLinkReport() != null)
        {
            OutputStream outStream;
            try
            {
                outStream = new FileOutputStream(targetSettings.getLinkReport());
                
                // Ignore the problems found by getReachableCompilationUnits(). Those problems
                // should have already been found by the build.
                Collection<ICompilerProblem> reachableProblems = new ArrayList<ICompilerProblem>();
                LinkReportWriter reportWriter = new LinkReportWriter(project.getDependencyGraph(), 
                        getReachableCompilationUnits(reachableProblems),
                        getLinkageChecker());
                reportWriter.writeToStream(outStream, problems);
            }
            catch (FileNotFoundException e)
            {
                final ICompilerProblem problem = new UnableToCreateLinkReportProblem(
                        targetSettings.getLinkReport().getAbsolutePath());
                problems.add(problem);
            }
        }
    }
    
    /**
     * Computes the set of compilation units that root the dependency walk. The
     * returned set of compilation units and their dependencies will be
     * compiled.
     * 
     * @return The set of rooted {@link ICompilationUnit}'s.
     */
    protected abstract RootedCompilationUnits computeRootedCompilationUnits() throws InterruptedException;

    public abstract RootedCompilationUnits getRootedCompilationUnits() throws InterruptedException;
    
    /**
     * Gets a set of {@link ICompilationUnit}s that are included into the build
     * process by -include-classes compiler argument.
     * <p>
     * This method is marked final until we have a use case to make it
     * non-final.
     * 
     * @return a set of {@link ICompilationUnit}s that are included into the
     * build process by -include-classes compiler argument.
     */
    public final Set<ICompilationUnit> getIncludesCompilationUnits() throws InterruptedException
    {
        Workspace workspace = project.getWorkspace();
        Set<IResolvedQualifiersReference> includesReferences = new HashSet<IResolvedQualifiersReference>();
        for (String className : targetSettings.getIncludes())
        {
            IResolvedQualifiersReference ref = ReferenceFactory.packageQualifiedReference(workspace, className);
            includesReferences.add(ref);
        }

        return project.getScope().getCompilationUnitsForReferences(includesReferences);
    }

    /**
     * @return a collection of resource bundle compilation units that are included
     * into the build process by -include-resource-bundles compiler argument.
     */
    public final Collection<ICompilationUnit> getIncludedResourceBundlesCompilationUnits(Collection<ICompilerProblem> problems) throws InterruptedException
    {
        Set<ICompilationUnit>includedResourceBundleCompUnits = new HashSet<ICompilationUnit>();
        for (String bundleName : targetSettings.getIncludeResourceBundles())
        {
            includedResourceBundleCompUnits.addAll(ResourceBundleUtils.findCompilationUnits(bundleName, project, problems));
        }

        return includedResourceBundleCompUnits;
    }

    /**
     * Get the set of metadata names.
     * 
     * @return The set of metadata names that will be preserved in a SWF 
     * target or recorded in a SWC target. May be null if all metadata
     * names should be kept.
     */
    @Override
    public final Set<String> getASMetadataNames()
    {
        return metadataNames;
    }
    
    /**
     * Add metadata names to the target.
     *  
     * @param metadataNames metadata names that should be kept. May not be null.
     */
    public void addASMetadataNames(Collection<String> metadataNames)
    {
        assert metadataNames != null;
        
        if (this.metadataNames == null)
            this.metadataNames = new HashSet<String>();
        
        this.metadataNames.addAll(metadataNames);
    }
    
    /**
     * The value that represents the percentage of the main task that has been
     * completed so far. It should be between 0 and 100.
     */
    private int percentCompleted = 0;
    
    /**
     * Gets called when build operation starts.
     */
    protected final void buildStarted() 
    {
        if(progressMonitor != null)
        {
            percentCompleted = 0;
        }
        project.getWorkspace().startBuilding();
    }
    
    /**
     * Updates the value that represents the percentage of the work completed so
     * far using the information specified and notifies the progress monitor
     * about this value. Each compilation unit is assumed to complete 2 steps in
     * order to be considered compiled. The first step is considered to be done
     * when the semantic analysis phase is done. ( @see
     * CompilationUnitBase#handleOutgoingDependenciesRequest ). The second step
     * starts with byte code generation ( @see
     * CompilationUnitBase#handleABCBytesRequest ) and ends when swf tags
     * request is completed. ( @see CompilationUnitBase#handleSWFTagsRequest )
     * 
     * @param numTotalCompilationUnits number of compilation units known to be
     * included in the build process
     * @param totalCompilationUnitsWorkCompleted total compilation unit work
     * completed so far. As described above, each compilation unit has two steps.
     * Therefore, this value would be equal to (2*numTotalCompilationUnits),
     * when we are done with compiling all the compilation units. 
     * @param maxPercentage the value that represents the maximum percentage of
     * the work can be considered completed so far in this method. This
     * method guarantees not to report a percentage value that is higher than
     * this value as completed to its clients via its progress monitor.
     * @return whether or not the build operation should continue.
     * <code>false</code> if the build operation has been requested to be
     * terminated, <code>true</code> otherwise.
     */
    protected boolean updateProgress(int numTotalCompilationUnits,
            int totalCompilationUnitsWorkCompleted, int maxPercentage)
    {
       if(progressMonitor != null)
       {
           int percentage = (50 * totalCompilationUnitsWorkCompleted) / numTotalCompilationUnits;
           
           if(percentage > maxPercentage)
               percentage = maxPercentage;
           
           if(percentCompleted < percentage)
           {
               percentCompleted = percentage;    
               progressMonitor.percentCompleted(this, percentCompleted);
           }
           
           return !isCanceled();
       }
       
       return true;
    }

    /**
     * Updates the value that represents the percentage of the main task that
     * has been completed so far and notifies the progress monitor about this
     * value if any.
     * 
     * @param percentageCompleted the percentage of the main task that has been
     * completed so far.
     * @return whether or not the build operation should continue.
     * <code>false</code> if the build operation has been requested to be
     * terminated, <code>true</code> otherwise.
     */
    protected boolean updateProgress(int percentageCompleted)
    {
        if(progressMonitor != null)
        {
            if(this.percentCompleted < percentageCompleted)
            {
                this.percentCompleted = percentageCompleted;
                progressMonitor.percentCompleted(this, percentageCompleted);
            }
            
            return !isCanceled();
        }
        
        return true;
    }
    
    /**
     * @return returns whether the current compilation operation has been 
     * requested to be canceled.
     */
    protected boolean isCanceled() 
    {
        if(progressMonitor != null)
            return progressMonitor.isCanceled(this);
        
        return false;
    }
    
    /**
     * Gets called when build operation finishes.
     */
    protected final void buildFinished() 
    {
        if(progressMonitor != null)
        {
            percentCompleted = 100;
            progressMonitor.done(this);
        }
        project.getWorkspace().doneBuilding();
    }
    
    /**
     * Determine if a compilation unit should be linked into the target.
     * 
     * @param cu The compilation unit to test.
     * @param targetSettings The target settings.
     * @return true if the compilation unit's linkage is external, false
     * otherwise.
     * @throws InterruptedException 
     */
    protected boolean isLinkageExternal(ICompilationUnit cu, 
            ITargetSettings targetSettings) throws InterruptedException
    {
        return getLinkageChecker().isExternal(cu);
    }

    /**
     * Get the target's linkage checker.
     *  
     * @return the target's linkage checker.
     */
    protected final LinkageChecker getLinkageChecker()
    {
        if (linkageChecker == null)
            linkageChecker = new LinkageChecker(project, targetSettings);
            
        return linkageChecker;
    }
    
    /**
     * Set the project's linkage checker.
     * 
     * @param linkageChecker linkage checker, may not be null.
     */
    protected void setLinkageChecker(LinkageChecker linkageChecker)
    {
        // validate we are using just one linkage checker for
        // the life of a target. 
        assert this.linkageChecker == null;
        
        this.linkageChecker = linkageChecker;
        
        assert this.linkageChecker != null;
    }
    
    protected Iterable<ICompilerProblem> getFatalProblems() throws InterruptedException
    {
        if (fatalProblems == null)
            fatalProblems = computeFatalProblems();
        return fatalProblems;
    }
    
    /**
     * Computes an {@link Iterable} of fatal {@link ICompilerProblem}s that
     * prevent this {@link Target} from being built.
     * <p>
     * Sub-classes override this method to check for additional fatal
     * {@link ICompilerProblem}s.
     * 
     * @return {@link Iterable} of fatal {@link ICompilerProblem}s
     * @throws InterruptedException
     */
    protected Iterable<ICompilerProblem> computeFatalProblems() throws InterruptedException
    {
        return ImmutableList.copyOf(project.getFatalProblems());
    }

    /**
     * Get a collection of compilation units for all of the classes found in 
     * all of the libraries found in the -include-libraries path.
     * 
     * @return collection of compilation units for all the classes found on
     * the -include-libraries path.
     */
    protected final Collection<ICompilationUnit> getIncludeLibrariesCompilationUnits()
    {
        //Collection<ICompilationUnit> units = new HashSet<ICompilationUnit>();
        Set<IResolvedQualifiersReference> includeLibrariesReferences = new HashSet<IResolvedQualifiersReference>();
        
        // Find all of the libraries on the -include-library path
        Set<FileID> swcs = LibraryPathManager.discoverSWCFilePaths(
                targetSettings.getIncludeLibraries().toArray(new File[0]));
        
        // For each library, get a compilation unit for every class in the
        // library.
        ISWCManager swcManager = project.getWorkspace().getSWCManager();
        Workspace w = project.getWorkspace();

        for (FileID swcPath : swcs)
        {
            File swcFile =swcPath.getFile();

            // If the SWC does not exist on disk, just ignore it for now, and a
            // problem will be created later on during the LibraryPathManager.collectionProblems() call.
            if (!swcFile.exists())
                continue;

            ISWC swc = swcManager.get(swcFile);
            for (ISWCLibrary library : swc.getLibraries())
            {
                for (ISWCScript script : library.getScripts())
                {
                    for (String def : script.getDefinitions())
                    {
                        IResolvedQualifiersReference definitionRef =
                                ReferenceFactory.packageQualifiedReference(w, def);
                        includeLibrariesReferences.add(definitionRef);
                    }
                }
            }
        }

        return project.getScope().getCompilationUnitsForReferences(includeLibrariesReferences);
    }
    
    /**
     * Wad that holds a set of {@link ICompilationUnit}s that have been built to
     * build the output of this target and any {@link ICompilerProblem}s found
     * building those {@link ICompilationUnit}s.
     */
    protected static final class BuiltCompilationUnitSet
    {
        BuiltCompilationUnitSet(ImmutableSet<ICompilationUnit> compilationUnits, Iterable<ICompilerProblem> problems)
        {
            this.compilationUnits = compilationUnits;
            this.problems = problems;
        }
        
        public final ImmutableSet<ICompilationUnit> compilationUnits;
        final Iterable<ICompilerProblem> problems;
    }
    
    /**
     * Wad containing the set of {@link ICompilationUnit}s that root the dependency walk
     * and any {@link ICompilerProblem}s encountered computing the roots of the
     * dependency walk. The returned set of compilation units and their
     * dependencies will be compiled.
     * 
     */
    public static final class RootedCompilationUnits
    {
        static RootedCompilationUnits concat(RootedCompilationUnits base, Set<ICompilationUnit> units, Iterable<ICompilerProblem> problems)
        {
            return new RootedCompilationUnits(Sets.union(base.units, units), Iterables.concat(base.problems, problems));
        }
        
        public RootedCompilationUnits(Set<ICompilationUnit> units, Iterable<ICompilerProblem> problems)
        {
            assert units != null;
            assert problems != null;
            this.units = new TreeSet<ICompilationUnit>(new Comparator<ICompilationUnit>()
            {
                @Override
                public int compare(ICompilationUnit o1, ICompilationUnit o2)
                {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            this.units.addAll(units);
            this.problems = problems;
        }
        
        private final SortedSet<ICompilationUnit> units;
        private final Iterable<ICompilerProblem> problems;
        
        public Set<ICompilationUnit> getUnits()
        {
            return units;
        }
        
        public Iterable<ICompilerProblem> getProblems()
        {
            return problems;
        }
    }
    
    /**
     * Wad containing an {@link Iterable} of {@link ICompilationUnit}s and an
     * {@link Iterable} of {@link ICompilerProblem}s found while constructing
     * the {@link Iterable} of {@link ICompilationUnit}s.
     * <p>
     * The {@link Iterable} of {@link ICompilationUnit}s iterates over all the
     * {@link ICompilationUnit}s that are directly depended on by another
     * {@link ICompilationUnit}.
     */
    public static class DirectDependencies
    {
        DirectDependencies(Iterable<ICompilationUnit> dependencies, Iterable<ICompilerProblem> problems)
        {
            this.dependencies = dependencies;
            this.problems = problems;
        }
        
        /**
         * Creates a new {@link DirectDependencies} object that is the concatenation of two other
         * {@link DirectDependencies} objects.
         * @param a
         * @param b
         * @return a new {@link DirectDependencies} object that is the concatenation of two other
         * {@link DirectDependencies} objects.
         */
        static DirectDependencies concat(DirectDependencies a, DirectDependencies b)
        {
            return new DirectDependencies(Iterables.concat(a.dependencies, b.dependencies), Iterables.concat(a.problems, b.problems));
        }
        
        final Iterable<ICompilationUnit> dependencies;
        final Iterable<ICompilerProblem> problems;
    }
}
