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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.DependencyTypeSet;
import org.apache.royale.compiler.common.Multiname;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.parsing.as.IProjectConfigVariables;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.ASScopeCache;
import org.apache.royale.compiler.internal.targets.AppSWFTarget;
import org.apache.royale.compiler.internal.targets.Target;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnit;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MissingBuiltinProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.targets.ISWFTarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IRequest;
import org.apache.royale.utils.FilenameNormalization;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Abstract class used to share implementation of some ICompilerProject methods
 * across multiple concrete implementation classes.
 */
public abstract class CompilerProject implements ICompilerProject
{
    private final ASProjectScope projectScope;
    private final Workspace workspace;
    protected Set<Target> targets;
    private final ReadWriteLock unfoundDependenciesLock;
    private final Map<String, Map<ICompilationUnit, Object>> unfoundDefinitionDependencies;
    protected Collection<ICompilerProblem> problems;
    
    /**
     * These are files that compilation units have tried to find and failed. At the moment these are
     * Embedded files and included files. Should these files be added to the Builder workspace, the project
     * will be notified and can in turn re-compiler the units that are missing these files
     */
    private final Map<String, Map<ICompilationUnit, Object>> unfoundReferencedSourceFileDependencies;
    private final Map<EmbedData, EmbedCompilationUnit> embedCompilationUnits;

    /**
     * Helper class to create new Scope Caches on the fly, when the scope cache map misses
     */
    static class ScopeCacheLoader extends CacheLoader<ASScope, ASScopeCache>
    {
        CompilerProject project;
        public ScopeCacheLoader(CompilerProject project)
        {
            this.project = project;
        }
        @Override
        public ASScopeCache load(ASScope scope)
        {
            ASScopeCache cache = new ASScopeCache(project, scope);
            project.projectScope.addScopeToCompilationUnitScopeList(scope);
            return cache;
        }
    }

    /**
     * Map that holds caches for each scope in the project - uses a Concurrent Map, with weak keys so that the caches
     * will go away once the corresponding scope has been gc'ed.  Uses soft values so the caches may be collected
     * if the VM is running out of memory
     */
    private LoadingCache<ASScope, ASScopeCache> scopeCaches = CacheBuilder.newBuilder()
        .weakKeys()
        .softValues()
        .build(new ScopeCacheLoader(this));
 
    /** This thread local is to avoid every thread contending for access to the scopeCaches map, which is shared
     *  across the entire Project.
     *  When a scope cache is requested, we first check the thread local cache.  If the entry is not present in
     *  the thread local, then we hit the shared scopeCaches map.  The result from the scopeCaches map is then also
     *  cached in the thread local.  This way, most of the time, we'll find the scope cache in the thread local, and 
     *  won't have to do any locking. 
     */    
    private ThreadLocal<Map<ASScope, WeakReference<ASScopeCache>> > threadLocalScopeCache; 
    
    /**
     * Dependency graph is used to keep track of which {@link ICompilationUnit}
     * 's are in this project. The dependency graph is in this shared base class
     * instead of being in {@link ASProject} because {@link ICompilationUnit}
     * implementations will want to update the dependency graph and
     * {@link ICompilationUnit} implementations should be able to behave the
     * same irrespective of what type of {@link CompilerProject} they are in.
     */
    protected final DependencyGraph dependencyGraph;
    
    /**
     * If true, use parallel code generation of method bodies.
     */
    private boolean useParallelCodeGen;
    
    /**
     * If true, use function inlining in code generation.
     */
    private boolean enableInlining;

    private final boolean useAS3;

    /**
     * used to track config variables and return config information back to clients, such as the parser
     */
    private final ConfigManager configManager;

    public CompilerProject(Workspace workspace, boolean useAS3)
    {
        this.workspace = workspace;
        targets = new HashSet<Target>();
        unfoundDependenciesLock = new ReentrantReadWriteLock();
        unfoundDefinitionDependencies = new HashMap<String, Map<ICompilationUnit, Object>>();
        unfoundReferencedSourceFileDependencies = new HashMap<String, Map<ICompilationUnit, Object>>();
        embedCompilationUnits = new HashMap<EmbedData, EmbedCompilationUnit>();
        dependencyGraph = new DependencyGraph();
        // ** TODO Instantiate real scope object.
        projectScope = initProjectScope(this);
        this.useAS3 = useAS3;
        initThreadLocalCaches();
        configManager = new ConfigManager();
        useParallelCodeGen = false;
        enableInlining = false;
        workspace.addProject(this);
    }
    
    /**
     * Init the project scope.  Allow subclasses to contribute their own if needed.  Default will create a new {@link ASProjectScope}
     * @param project the project to build the scope from
     * @return a new {@link ASProjectScope}
     */
    protected ASProjectScope initProjectScope(CompilerProject project) {
        return new ASProjectScope(project);
    }
 
    /**
     * Init the thread local cache - will reset the thread local cache to it's initial, empty state.
     */
    private void initThreadLocalCaches()
    {
        threadLocalScopeCache = new ThreadLocal<Map<ASScope, WeakReference<ASScopeCache>>>()
        {
            @Override
            protected Map<ASScope, WeakReference<ASScopeCache>> initialValue()
            {
                // Use a WeakHashMap with WeakRefs as values - the ASScopes and ASScopeCaches
                // will be kept alive or not by the Project
                return new WeakHashMap<ASScope, WeakReference<ASScopeCache>>();
            }
        };
    }

    @Override
    public ASProjectScope getScope()
    {
        return this.projectScope;
    }

    @Override
    public Workspace getWorkspace()
    {
        return workspace;
    }

    @Override
    public List<ICompilationUnit> getReachableCompilationUnitsInSWFOrder(Collection<ICompilationUnit> roots)
    {
        return dependencyGraph.topologicalSort(roots);
    }

    /**
     * Updates the {@link ASProjectScope} to contain reference to public and
     * internal definitions in those compilation units.
     * 
     * @param units {@link ICompilationUnit}'s to add to the project.
     */
    public void updatePublicAndInternalDefinitions(Collection<ICompilationUnit> units) throws InterruptedException
    {
        if (units.isEmpty())
            return;

        ArrayList<IRequest<IFileScopeRequestResult, ICompilationUnit>> scopeRequests = new ArrayList<IRequest<IFileScopeRequestResult, ICompilationUnit>>(units.size());
        for (ICompilationUnit unit : units)
        {
            ICompilerProject unitProject = unit.getProject();
            assert (unitProject == this) || (unitProject == null) : "ICompilationUnit should be in this project or not in any project";
            if (unitProject == this)
                scopeRequests.add(unit.getFileScopeRequest());
        }

        scopeCaches.invalidateAll();
        initThreadLocalCaches();
        
        projectScope.addAllExternallyVisibleDefinitions(scopeRequests);
    }

    /**
     * Adds compilation units to the project and updates the public and private
     * definitions. Eventually this method should be removed, but it is
     * convenient for testing because it allows custom created
     * {@link ICompilationUnit}'s to be injected into a project.
     * 
     * @param compilationUnits A collection of compilation units.
     */
    public void addCompilationUnitsAndUpdateDefinitions(Collection<ICompilationUnit> compilationUnits) throws InterruptedException
    {
        addCompilationUnits(compilationUnits);
        updatePublicAndInternalDefinitions(compilationUnits);
    }

    public void addEmbedCompilationUnit(EmbedCompilationUnit unit) throws InterruptedException
    {
        dependencyGraph.addEmbedCompilationUnit(unit);
        workspace.addCompilationUnit(unit);
        embedCompilationUnits.put(unit.getEmbedData(), unit);

        updatePublicAndInternalDefinitions(Collections.<ICompilationUnit>singletonList(unit));
    }

    /**
     * Add a {@link ICompilationUnit} to the project. Calling this method does not
     * trigger the {@link ICompilationUnit} to be compiled.
     * 
     * @param unit {@link ICompilationUnit} to add
     */
    public void addCompilationUnit(ICompilationUnit unit)
    {
        assert !(unit instanceof EmbedCompilationUnit) : "Embed compilation unit should be added with addEmbedCompilationUnit!";
        dependencyGraph.addCompilationUnit(unit);
        workspace.addCompilationUnit(unit);
    }

    /**
     * Add compilation units to the project. Calling this method does not
     * trigger the compilation units to be compiled.
     * 
     * @param units compilation units
     */
    public void addCompilationUnits(Collection<ICompilationUnit> units)
    {
        for (ICompilationUnit compilationUnit : units)
        {
            addCompilationUnit(compilationUnit);
        }
    }
    
    /**
     * Removes a {@link ICompilationUnit} from the project.
     * @param unit {@link ICompilationUnit} to remove.
     */
    public void removeCompilationUnit(ICompilationUnit unit)
    {
        dependencyGraph.removeCompilationUnit(unit);
        workspace.removeCompilationUnit(unit);
        unit.clearProject();

        if (unit instanceof EmbedCompilationUnit)
        {
            EmbedData embedData = ((EmbedCompilationUnit)unit).getEmbedData();
            embedCompilationUnits.remove(embedData);
        }
    }

    /**
     * Don't call this from production code. Normally library managers take care of this.
     * This function is only for unit tests.
     */
    public void unitTestingEntryPointForRemovingCompilationUnit(ICompilationUnit unit)
    {  
        removeCompilationUnits( Collections.singleton(unit) );
    }

    /**
     * Removes a set of {@link ICompilationUnit}'s from the project.
     * @param unitsToRemove {@link ICompilationUnit}'s to remove.
     */
    public void removeCompilationUnits(Collection<ICompilationUnit> unitsToRemove)
    {
        if ((unitsToRemove == null) || (unitsToRemove.size() == 0))
            return;

        // TODO pre-remove invalidation
        // Factor this into a method and un-comment at some point!
//        Set<ICompilationUnit> workSet = new HashSet<ICompilationUnit>(unitsToRemove);
//        Set<ICompilationUnit> visited = new HashSet<ICompilationUnit>(unitsToRemove.size());
//        EnumSet<DependencyGraph.DependencyType> inheritanceTypeSet =
//            EnumSet.of(DependencyGraph.DependencyType.INHERITANCE);
//        while (workSet.size() >= 0)
//        {
//            ICompilationUnit unit = workSet.iterator().next();
//            workSet.remove(unit);
//            if (!visited.contains(unit))
//            {
//                visited.add(unit);
//                unit.invalidate(DependencyGraph.DependencyType.INHERITANCE);
//                workSet.addAll(dependencyGraph.getDirectReverseDependencies(unit, inheritanceTypeSet));
//            }
//        }
        
        // TODO add code behind the invalidate all above or here to
        // invalidation name resolution caches.

        // unhook all the compilation units to remove from the project scope.
        projectScope.removeCompilationUnits(unitsToRemove);

        for (ICompilationUnit unit : unitsToRemove)
        {
            removeCompilationUnit(unit);
        }
    }

    @Override
    public Collection<ICompilationUnit> getCompilationUnits()
    {
        return dependencyGraph.getCompilationUnits();
    }

    @Override
    public Collection<ICompilationUnit> getCompilationUnits(String filename)
    {
        return workspace.getCompilationUnits(FilenameNormalization.normalize(filename), this);
    }

    @Override
    public Collection<ICompilationUnit> getIncludingCompilationUnits(String filename)
    {
        return workspace.getIncludingCompilationUnits(FilenameNormalization.normalize(filename), this);
    }

    public void addUnfoundDefinitionDependency(String definitionBaseName, ICompilationUnit compilationUnit)
    {
        unfoundDependenciesLock.writeLock().lock();
        try
        {
            Map<ICompilationUnit, Object> dependentUnits = unfoundDefinitionDependencies.get(definitionBaseName);
            if (dependentUnits == null)
            {
                dependentUnits = new WeakHashMap<ICompilationUnit, Object>();
                unfoundDefinitionDependencies.put(definitionBaseName, dependentUnits);
            }

            dependentUnits.put(compilationUnit, null);            
        }
        finally
        {
            unfoundDependenciesLock.writeLock().unlock();
        }
    }

    public Set<ICompilationUnit> getDependenciesOnUnfoundDefinition(String definitionBaseName)
    {
        unfoundDependenciesLock.readLock().lock();
        try
        {
            Map<ICompilationUnit, Object> dependentUnits = unfoundDefinitionDependencies.get(definitionBaseName);
            if (dependentUnits == null)
            {
                dependentUnits = Collections.emptyMap();
            }

            return dependentUnits.keySet();
        }
        finally
        {
            unfoundDependenciesLock.readLock().unlock();
        }
    }

    private void removeAnyUnfoundDefinitionDependency(ICompilationUnit compilationUnit)
    {
        for (Map<ICompilationUnit, Object> dependentUnits : unfoundDefinitionDependencies.values())
        {
            dependentUnits.remove(compilationUnit);
        }
    }

    /**
     * Notify project that a compilation unit could not find a referenced file.
     * 
     * "Referenced" here means that the compilation unit references the file in some way other than the normal
     * language level manner. For example, embedded and included files are "referenced", whereas files that
     * defined referenced types are not.
     * 
     * Calling this function will allow incremental re-compiation to be kicked off when/if the required
     * file comes into existence.
     * 
     * @param sourceFilename The full path to the required file
     * @param compilationUnit The compilation unit that requires the referenced file 
     */
    public void addUnfoundReferencedSourceFileDependency(String sourceFilename, ICompilationUnit compilationUnit)
    {
        unfoundDependenciesLock.writeLock().lock();
        try
        {
            String filenameNoPath = new File(sourceFilename).getName();
            Map<ICompilationUnit, Object> dependentUnits = unfoundReferencedSourceFileDependencies.get(filenameNoPath);
            if (dependentUnits == null)
            {
                dependentUnits = new WeakHashMap<ICompilationUnit, Object>();
                unfoundReferencedSourceFileDependencies.put(filenameNoPath, dependentUnits);
            }

            dependentUnits.put(compilationUnit, null);
        }
        finally
        {
            unfoundDependenciesLock.writeLock().unlock();
        }
    }

    /**
     * gets the set of all compilation units that have an unsatisfied reference to a given file.
     * @param sourceFilename The path to the file.
     */
    public Set<ICompilationUnit> getDependenciesOnUnfoundReferencedSourceFile(String sourceFilename)
    {
        unfoundDependenciesLock.readLock().lock();
        try
        {
            String filenameNoPath = new File(sourceFilename).getName();
            Map<ICompilationUnit, Object> dependentUnits = unfoundReferencedSourceFileDependencies.get(filenameNoPath);
            if (dependentUnits == null)
            {
                dependentUnits = Collections.emptyMap();
            }
            return dependentUnits.keySet();
        }
        finally
        {
            unfoundDependenciesLock.readLock().unlock();
        }
    }

    private void removeAnyUnfoundReferencedSourceFileDependency(ICompilationUnit compilationUnit)
    {
        for (Map<ICompilationUnit, Object> dependentUnits : unfoundReferencedSourceFileDependencies.values())
        {
            dependentUnits.remove(compilationUnit);
        }
    }

    public void removeAnyUnfoundDependencies(ICompilationUnit compilationUnit)
    {
        // we could probably get away without locking when a remove is done, as
        // this is only called from a call to clean() on a compilation unit, and
        // that should only happen in a single thread.  but let's just play it safe
        // and lock anyway in case someone in future calls this method from somewhere else.
        unfoundDependenciesLock.writeLock().lock();
        try
        {
            removeAnyUnfoundDefinitionDependency(compilationUnit);
            removeAnyUnfoundReferencedSourceFileDependency(compilationUnit);
        }
        finally
        {
            unfoundDependenciesLock.writeLock().unlock();
        }
    }

    public Set<ICompilationUnit> getDependenciesOnDefinition(String definitionBaseName)
    {
        Set<ICompilationUnit> dependentUnits = new HashSet<ICompilationUnit>();
        Set<ICompilationUnit> definingUnits = projectScope.getCompilationUnitsByDefinitionName(definitionBaseName);
        DependencyTypeSet dependencyTypes = DependencyTypeSet.allOf();
        for (ICompilationUnit definingUnit : definingUnits)
        {
            dependentUnits.addAll(dependencyGraph.getDirectReverseDependencies(definingUnit, dependencyTypes));
        }

        return dependentUnits;
    }

    @Override
    public void clean()
    {
        Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate = new HashMap<ICompilerProject, Set<ICompilationUnit>>();

        workspace.startIdleState();
        try
        {
            Collection<ICompilationUnit> compilationUnits = getCompilationUnits();

            for (ICompilationUnit compilationUnit : compilationUnits)
            {
                compilationUnit.clean(null, cusToUpdate, true);
            }

            scopeCaches.invalidateAll();
            initThreadLocalCaches();
        }
        finally
        {
            workspace.endIdleState(cusToUpdate);
        }
    }

    @Override
    public void delete()
    {
        Collection<ICompilationUnit> compilationUnits = getCompilationUnits();
        for (ICompilationUnit compilationUnit : compilationUnits)
        {
            workspace.removeCompilationUnit(compilationUnit);
        }

        workspace.deleteProject(this);
    }

    @Override
    public ISWFTarget createSWFTarget(ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor) throws InterruptedException
    {
        return new AppSWFTarget(this, targetSettings, progressMonitor);
    }
    
    /**
     * Adds a dependency from one {@link ICompilationUnit} to another
     * {@link ICompilationUnit}.
     * 
     * @param from {@link ICompilationUnit} that depends on "to".
     * @param to {@link ICompilationUnit} that is depended on by "from".
     * @param dt The types of dependencies to add.
     * @param qname The qname of the target dependency added
     */
    public void addDependency(ICompilationUnit from, ICompilationUnit to, DependencyTypeSet dt, String qname)
    {
        dependencyGraph.addDependency(from, to, dt, qname);
    }

    /**
     * Adds a dependency from one {@link ICompilationUnit} to another
     * {@link ICompilationUnit}.
     * 
     * @param from {@link ICompilationUnit} that depends on "to".
     * @param to {@link ICompilationUnit} that is depended on by "from".
     * @param dt The type of dependency to add.
     * @param qname The qname of the target dependency added
     */
    public void addDependency(ICompilationUnit from, ICompilationUnit to, DependencyType dt, String qname)
    {
        dependencyGraph.addDependency(from, to, dt, qname);
    }

    /**
     * Adds an anonymous dependency from one {@link ICompilationUnit} to another
     * {@link ICompilationUnit}. This dependency will not be used in the link report.
     * 
     * @param from {@link ICompilationUnit} that depends on "to".
     * @param to {@link ICompilationUnit} that is depended on by "from".
     * @param dt The type of dependency to add.
     */
    public void addDependency(ICompilationUnit from, ICompilationUnit to, DependencyType dt)
    {
        dependencyGraph.addDependency(from, to, dt);
    }
    
    /**
     * Removes the outgoing dependencies of a collection of {@link ICompilationUnit}.
     * 
     * @param units {@link ICompilationUnit} to remove dependencies from.
     */
    public void removeDependencies(Collection<ICompilationUnit> units)
    {
        for (ICompilationUnit unit : units)
            dependencyGraph.removeDependencies(unit);
    }
    
    /**
     * Gets the set of dependencies referenced by the specified
     * {@link ICompilationUnit}. in the absence of wierd things like
     * defaults/theme css, this is just the set of {@link ICompilationUnit}'s
     * that define definitions directly referenced by the specified
     * {@link ICompilationUnit}.
     * <p>
     * This method can be overridden by subclasses to add additional
     * dependencies that are not in the dependency graph.
     * 
     * @param cu {@link ICompilationUnit} to retrieve dependencies for.
     * @return A new Set of {@link ICompilationUnit}'s that the specified
     * {@link ICompilationUnit} directly depends on.
     */
    public Set<ICompilationUnit> getDependencies(ICompilationUnit cu) throws InterruptedException
    {
        // The process of finding semantic problems updates the dependency graph
        // so wait for semantic analysis pass to finish.
        // TODO possible revisit how we wait for the dependency graph to be updated
        // or at least better document it in ICompilationUnit.
        cu.getOutgoingDependenciesRequest().get();
        return dependencyGraph.getDirectDependencies(cu);
    }
    
    /**
     * Gets project level {@link ICompilerProblem}'s target are not specific to any
     * one target created by the project.
     */
    public abstract void collectProblems(Collection<ICompilerProblem> problems);


    /**
     * Gets the project level {@link ICompilerProblem}'s that come from parsing the
     * config options, from the command line, or a configuration file
     * @param problems  the Collection to add the config problems to.
     */
    protected void collectConfigProblems(Collection<ICompilerProblem> problems)
    {
        problems.addAll(configManager.getProjectConfig(this).getProblems());
    }

    /**
     * Get the cache for a particular scope
     * @param scope     the scope you want the cache for.
     * @return          the ASScopeCache for the scope
     */
    public ASScopeCache getCacheForScope(ASScope scope)
    {
        ASScopeCache scopeCache = null;

        // First check and see if we have the result cached in the thread local cache
        Map<ASScope, WeakReference<ASScopeCache>> cache = threadLocalScopeCache.get();
        WeakReference<ASScopeCache> ref = cache.get(scope);
        scopeCache = ref != null ? ref.get() : null;
        
        if( scopeCache == null )
        {
            // Didn't find it in the thread local, hit the shared cache, and cache those results
            // in the thread local so that we won't have to hit the shared cache next time.
            scopeCache = scopeCaches.getUnchecked(scope);
            cache.put(scope, new WeakReference<ASScopeCache>(scopeCache));
        }
        
        return scopeCache;
    }

    /**
     * Clears all the {@link ASScopeCache}s associated with the specified
     * {@link ICompilationUnit} and removes all the {@link IASScope}s build by
     * the {@link ICompilationUnit} from the list of scopes associated with the
     * {@link ICompilationUnit}. This method should only be called when a
     * {@link ICompilationUnit} is being removed from a project.
     * 
     * @param compilationUnit {@link ICompilationUnit} whose scope caches should
     * be cleared and whose scope list should be emptied.
     */
    public void clearScopeCacheForCompilationUnit(ICompilationUnit compilationUnit)
    {
        Collection<IASScope> relatedScopes = projectScope.clearCompilationUnitScopeList(compilationUnit);
        if (relatedScopes == null)
            return;
        resetScopeCaches(relatedScopes);
    }
    
    /**
     * Resets all the {@link ASScopeCache}s associated with the specified
     * {@link ICompilationUnit}.
     * 
     * @param compilationUnit {@link ICompilationUnit} whose scope caches should
     * be cleared
     */
    public void resetScopeCacheForCompilationUnit(ICompilationUnit compilationUnit)
    {
        Collection<IASScope> relatedScopes = projectScope.getCompilationUnitScopeList(compilationUnit);
        assert relatedScopes != null;
        if (relatedScopes.isEmpty())
            return;
        
        resetScopeCaches(relatedScopes);
    }
    
    private void resetScopeCaches(Iterable<IASScope> scopes)
    {
        assert scopes != null;
        for (IASScope scope : scopes)
        {
            scopeCaches.invalidate(scope);
        }
        initThreadLocalCaches();
    }

    public void addGlobalUsedNamespacesToNamespaceSet(Set<INamespaceDefinition> nsSet)
    {
        nsSet.add(NamespaceDefinition.getPublicNamespaceDefinition());
        if (this.useAS3)
            nsSet.add(NamespaceDefinition.getAS3NamespaceDefinition());
    }

    /**
     * Called by
     * {@link Workspace#fileAdded(org.apache.royale.compiler.filespecs.IFileSpecification)}
     * for each project in the workspace. Each subclass of this class must
     * decide when an added file is interesting or should be ignored.
     * 
     * @param addedFile File that was added.
     * @return true if any new compilation units were created as a result of
     * adding the file, false otherwise.
     */
    public abstract boolean handleAddedFile(File addedFile);

    /**
     * Return the compilation unit related this EmbedData, or it's equivalent.  If
     * there is no equiv., null will be returned
     * @param data The embedding data.
     * @return related EmbedCompilationUnit.  Can be null
     */
    public EmbedCompilationUnit getCompilationUnit(EmbedData data)
    {
        return embedCompilationUnits.get(data);
    }
    
    public final DependencyGraph getDependencyGraph()
    {
        return dependencyGraph;
	}
    /**
     * Sets project config variables for use on this project
     * @see org.apache.royale.compiler.internal.projects.ConfigManager#addConfigVariables(Map)
     * @param map the mapping of config names to their expressions
     */
    public void addConfigVariables(Map<String, String> map) {
        configManager.addConfigVariables(map);
        // TODO: in future we could be smarter about incrementally invalidating
        // the project based on what the config variables affect, but for now
        // just blow away the whole project.
        clean();
    }
    
    /**
     * Sets project config variable for use on this project
     * @see org.apache.royale.compiler.internal.projects.ConfigManager#addConfigVariables(Map)
     * @param namespace the config namespace
     * @param expression the config expression for the namespace 
     */
    public void addConfigVariable(String namespace, String expression) {
        configManager.addConfigVariable(namespace, expression);
        // TODO: in future we could be smarter about incrementally invalidating
        // the project based on what the config variables affect, but for now
        // just blow away the whole project.
        clean();
    }
    
    /**
     * Returns an {@link IProjectConfigVariables} struct that contains all the config data for this project
     * @return an {@link IProjectConfigVariables} object, never null
     */
    public IProjectConfigVariables getProjectConfigVariables() {
        return configManager.getProjectConfig(this);
    }

    @Override
    public IDefinition resolveQNameToDefinition(final String qName)
    {
        assert qName != null : "qName can't be null.";
        final Multiname multiname = Multiname.crackDottedQName(this, qName);
        return getScope().findDefinitionByName(multiname, false);
    }
    
    @Override
    public ICompilationUnit resolveQNameToCompilationUnit(final String qName)
    {
        IDefinition definition = resolveQNameToDefinition(qName);
        if (definition == null)
            return null;

        return projectScope.getCompilationUnitForDefinition(definition);        
    }

    // List of Builtin types that must be present for compilation
    // to succeed.
    private static final BuiltinType[] REQUIRED_BUILTINS = {
        BuiltinType.OBJECT,
        BuiltinType.ARRAY,
        BuiltinType.STRING,
        BuiltinType.ANY_TYPE,
        BuiltinType.VOID,
        BuiltinType.NUMBER,
        BuiltinType.INT,
        BuiltinType.UINT
    };
    
    /**
     * Check for fatal problems, that would prevent us from successfully compiling.
     * Currently this just checks that all of the builtin types are present.
     */
    public Collection<ICompilerProblem> getFatalProblems()
    {
        Collection<ICompilerProblem> fatalProblems = null;

        for(IASLanguageConstants.BuiltinType builtinType : REQUIRED_BUILTINS )
        {
            if (getBuiltinType(builtinType) == null)
            {
                if (fatalProblems == null)
                    fatalProblems = new ArrayList<ICompilerProblem>();

                fatalProblems.add(new MissingBuiltinProblem(builtinType.getName()));
                break;
            }
        }
        return fatalProblems == null ? Collections.<ICompilerProblem>emptyList() : fatalProblems;
    }

    @SuppressWarnings("incomplete-switch")
	@Override
    public ITypeDefinition getBuiltinType(IASLanguageConstants.BuiltinType type)
    {
        switch( type )
        {
            case OBJECT:
                return projectScope.getObjectDefinition();
            case STRING:
                return projectScope.getStringDefinition();
            case ARRAY:
                return projectScope.getArrayDefinition();
            case XML:
                return projectScope.getXMLDefinition();
            case XMLLIST:
                return projectScope.getXMLListDefinition();
            case BOOLEAN:
                return projectScope.getBooleanDefinition();
            case INT:
                return projectScope.getIntDefinition();
            case UINT:
                return projectScope.getUIntDefinition();
            case NUMBER:
                return projectScope.getNumberDefinition();
            case CLASS:
                return projectScope.getClassDefinition();
            case FUNCTION:
                return projectScope.getFunctionDefinition();
            case NAMESPACE:
                return projectScope.getNamespaceDefinition();
            case VECTOR:
                return projectScope.getVectorDefinition();
            case NULL:
                return ClassDefinition.getNullClassDefinition();
            case VOID:
                return ClassDefinition.getVoidClassDefinition();
            case Undefined:
                return ClassDefinition.getUndefinedClassDefinition();
            case ANY_TYPE:
                return ClassDefinition.getAnyTypeClassDefinition();
        }

        String name = type.getName();
        
        ITypeDefinition definition = (ITypeDefinition)getScope().findDefinitionByName(name);

        // Don't have playerglobal.abc/swc with float yet.
        assert definition != null
                : "Error, builtin type '"+ type.getName()+ "' can't be found!";

        return definition;
    }
    
    @Override
    public IDefinition getUndefinedValue()
    {
        return projectScope.getUndefinedValueDefinition();
    }
    
    /**
     * Gets a boolean that indicates whether or not parallel code generation of method bodies is enabled.
     * @return true if method bodies should be generated in parallel, false otherwise.
     */
    public boolean getUseParallelCodeGeneration()
    {
        return useParallelCodeGen;
    }
    
    @Override
    public void setUseParallelCodeGeneration(boolean useParallelCodeGeneration)
    {
        this.useParallelCodeGen = useParallelCodeGeneration;
    }
    
    @Override
    public Set<ICompilationUnit> getDirectDependencies(ICompilationUnit cu)
    {
        return dependencyGraph.getDirectDependencies(cu);
    }

    @Override
    public Set<ICompilationUnit> getDirectReverseDependencies(ICompilationUnit cu, DependencyTypeSet types)
    {
        return dependencyGraph.getDirectReverseDependencies(cu, types);
    }

    @Override
    public boolean isInliningEnabled()
    {
        return enableInlining;
    }

    /**
     * Set whether or not function inlining is enabled.
     * 
     * @param enableInlining true to enable inlining.
     */
    public void setEnableInlining(boolean enableInlining)
    {
        this.enableInlining = enableInlining;
        clean();
    }
    
    /**
     * Add AST to cache.  By default, not added to any cache.
     * 
     * @param ast The AST.
     */
    public void addToASTCache(IASNode ast)
    {
    }

    /**
     * Override this to permit package aliasing on imports and elsewhere
     * 
     * @param packageName The package name
     */
    public String getActualPackageName(String packageName)
    {
        return packageName;
    }
    
    /**
     * Override this to disambiguate between two ambiguous definitinos
     * 
     * @param scope The current scope.
     * @param name Definition name.
     * @param def1 One possibility.
     * @param def2 The other possibility.
     * @return null if still ambiguous or else def1 or def2.
     */
    public IDefinition doubleCheckAmbiguousDefinition(IASScope scope, String name, IDefinition def1, IDefinition def2)
    {
        return null;
    }

    /**
     * @return collection of compiler problems.
     */
    public Collection<ICompilerProblem> getProblems()
    {
        return problems;
    }

    /**
     * collection of compiler problems.
     */
    public void setProblems(Collection<ICompilerProblem> problems)
    {
        this.problems = problems;
    }
    
    public boolean isCompatibleOverrideReturnType(IFunctionDefinition func, ITypeDefinition overrideDefinition, ITypeDefinition baseDefinition)
    {
        return (baseDefinition == overrideDefinition);
    }
    
    public boolean isValidTypeConversion(IASNode node, IDefinition actualDefinition, IDefinition expectedDefinition, IFunctionDefinition func)
    {
        return false;
    }

	public boolean isCompatibleOverrideParameterType(IFunctionDefinition func,
			ITypeDefinition overrideDefinition, ITypeDefinition baseDefinition,
			int i) {
        return (baseDefinition == overrideDefinition);
	}

	public boolean isParameterCountMismatchAllowed(IFunctionDefinition func,
			int formalCount, int actualCount) {
        return false;
	}
}
