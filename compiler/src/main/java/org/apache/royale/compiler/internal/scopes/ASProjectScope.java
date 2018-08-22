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

package org.apache.royale.compiler.internal.scopes;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.IDefinitionPriority;
import org.apache.royale.compiler.common.Multiname;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.definitions.AppliedVectorDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.PackageDefinition;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.scopes.SWCFileScopeProvider.SWCFileScope;
import org.apache.royale.compiler.internal.tree.as.ImportNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.scopes.IFileScope;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IRequest;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * An ASProjectScope is the topmost scope for a particular project.
 * <p>
 * A project scope keeps track of, but does not own, all the externally-visible
 * definitions for all the compilation units of a single project, so that
 * multiple compilation units can resolve an identifier to the same definition.
 * <p>
 * For example, the ClassDefinition for the Sprite class in playerglobal.swc is
 * owned by a single ASFileScope produced from that SWC. But this definition is
 * then shared into the ASProjectScope for each project that uses
 * playerglobal.swc.
 * <p>
 * Note that a workspace might have some projects whose project scope maps
 * "flash.display.Sprite" to the Sprite class in playerglobal.swc and other
 * projects whose project scope maps this same qualified name to the Sprite
 * class in airglobal.swc.
 * <p>
 * Unlike other ASScopes, an ASProjectScope does not store information about
 * <code>import</code> or <code>use namespace</code> directives.
 * <p>
 * Since multiple compilation units need to concurrently access a project scope,
 * it uses a ReadWriteLock to allow either multiple readers with no writer or a
 * single writer with no readers.
 * <p>
 * A project scope can store a special kind of definition called a <i>definition
 * promise</i>, represented by <code>ASProjectScope.DefinitionPromise</code>.
 * A definition promise is a small object that serves as a placeholder for an
 * actual definition, which it can produce on demand but at significant expense.
 * A promise knows only its qualified name and the compilation unit that produced it;
 * it has no idea whether the actual definition it can produce will turn out
 * to be a class definition, an interface definition, a function/getter/setter
 * definition, a variable/constant definition, or a namespace definition.
 * Promises are created by compilation units corresponding to files on the
 * source path or library path, but not for files on the source list.
 * The files on the source path and library path are recursively enumerated,
 * compilation units are created for each source file and each SWC script,
 * and each such compilation unit produces a promise to initially populate
 * the project scope. For example, the file <code>com/whatever/Foo.as</code>
 * will produce a promise named <code>com.whatever.Foo</code>.
 * If code refers to <code>Foo</code>, the promise will be converted
 * to an actual definition by parsing the file <code>com/whatever/Foo.as</code>,
 * building a syntax tree, building a file scope, etc.
 * <p>
 * Project scopes support <i>definition shadowing</i> since multiple
 * definitions with the same qualified name can exist in them
 * without this being an error.
 * (For example, monkey-patching UIComponent would cause it to be
 * on the source path and also in framework.swc.)
 * Definition priorities, represented by <code>IDefinitionPriority</code>,
 * determine which one of the definitions is made visible to the name
 * resolution algorithm by being stored in the scope's <i>definition store</i>.
 * The others are stored, invisible to name resolution, in <i>shadow sets</i>
 * of definitions, in a map that maps a qualified name to a shadow set.
 * As definitions with a given qualified name are added to and removed from
 * the scope, which definition is visible, and which are shadowed, can change.
 * If a compilation unit initially produces definition promises rather than
 * actual definitions, then it puts promises into the shadow sets rather than
 * actual definitions; this allows the <code>removeDefinition</code> method
 * to be able to remove a definition promise from the project scope
 * without it ever being converted to an actual definition.
 */
public class ASProjectScope extends ASScopeBase
{
    /**
     * Helper method used by getLocalProperty(). Currently our scope APIs use
     * collections of definitions that are not necessarily sets and therefore
     * allow duplicates. However, we don't want duplicates occurring when we
     * search the project scope and find a definition that we already found in a
     * package scope or a file scope. So until it becomes a set, we simply walk
     * the collection (which should be small) and avoid adding a duplicate.
     */
    private static void accumulateDefinitions(ICompilationUnit referencingCU, Collection<IDefinition> defs, IDefinition def)
    {
        boolean invisible = referencingCU != null ? referencingCU.isInvisible() : false;

        for (IDefinition d : defs)
        {
            if (d == def)
                return;
            else if (invisible && d.getQualifiedName().equals(def.getQualifiedName()))
                return;

        }

        defs.add(def);
    }

    /**
     * Adds public and internal definitions of the specified scope to this
     * ASProjectScope scope.
     * 
     * @param cu {@link ICompilationUnit} that contains the specified scope.
     * @param scopes ASScopes from which to collect externally-visible
     * definitions.
     */
    private void addExternallyVisibleDefinitionsToProjectScope(ICompilationUnit cu, IASScope[] scopes)
    {
        if (scopes != null)
        {
            for (IASScope iasScope : scopes)
            {
                IFileScope scope = (IFileScope)iasScope;
                if (scope != null)
                {
                    final Collection<IASScope> compilationUnitScopeList = compilationUnitToScopeList.getUnchecked(cu);
                    assert compilationUnitScopeList != null;
                    compilationUnitScopeList.add(scope);
                    ArrayList<IDefinition> externallVisibleDefs = new ArrayList<IDefinition>();
                    scope.collectExternallyVisibleDefinitions(externallVisibleDefs, false);
                    for (IDefinition d : externallVisibleDefs)
                        addDefinition(d);
                }
            }
        }
    }

    /**
     * Adds public and internal definitions of the specified scope requests to
     * this ASProjectScope scope.
     * 
     * @param scopeRequests a list of scope requests that will have their
     * externally visible definitions added to this project scope.
     */
    public void addAllExternallyVisibleDefinitions(ArrayList<IRequest<IFileScopeRequestResult, ICompilationUnit>> scopeRequests) throws InterruptedException
    {
        int size = scopeRequests.size();
        ICompilationUnit[] comps = new ICompilationUnit[size];
        IASScope[][] scopes = new IASScope[size][];

        for (int i = 0; i < size; ++i)
        {
            // Have to run this outside of the lock to prevent deadlocks
            IRequest<IFileScopeRequestResult, ICompilationUnit> scopeRequest = scopeRequests.get(i);
            scopes[i] = scopeRequest.get().getScopes();
            comps[i] = scopeRequest.getRequestee();
        }
        // Hold the lock until we're done adding all the definitions
        // so that no look ups occur when we're in the middle of adding definitions to the project.
        writeLock.lock();
        try
        {
            for (int i = 0; i < size; ++i)
                addExternallyVisibleDefinitionsToProjectScope(comps[i], scopes[i]);
            
        }
        finally
        {
            writeLock.unlock();
        }
    }

    private final CompilerProject project;

    // Locks for controlling concurrent access to a project scope.
    // When accessing this project scope's fSymbolsToDefinitionSets map
    // or its scopeToCompilationUnitMap map, we lock readLock to allow
    // multiple-readers-and-no-writers or lock writeLock to allow
    // no-readers-and-one-writer.
    private final ReadWriteLock readWriteLock;
    private final Lock readLock;
    private final Lock writeLock;
    private final Lock newVectorClassLock;

    /**
     * The value is a WeakReference to a ICompilationUnit, as the
     * DependencyGraph should have the only long held hard reference to a
     * ICompilationUnit to ease memory profiling.
     * Only SWCFileScopes are stored in a map, as SWC scopes are shared across projects
     * but other ASFileScopes aren't
     */
    private Map<SWCFileScope, ICompilationUnit> swcFileScopeToCompilationUnitMap = new MapMaker()
    .weakKeys()
    .weakValues()
    .makeMap();

    private final Map<ITypeDefinition, AppliedVectorDefinition> vectorElementTypeToVectorClassMap = new MapMaker()
    .weakKeys()
    .weakValues()
    .makeMap();

    /**
     * This map implements definition-shadowing.
     * It maps a qualified name like "mx.core.UIComponent"
     * to a "shadow set": a set of definitions that are
     * stored here rather than in the definition store,
     * and therefore are invisible to the name resolution
     * algorithm.
     */
    private HashMap<String, Set<IDefinition>> qnameToShadowedDefinitions;

    /**
     * This set of strings (e.g., "Object", "flash.display.Sprite",
     * "flash.display.*") is created on demand by isValidImport() from the names
     * of all the definitions in this scope. It is thrown away every time a new
     * definition is added or removed. Note that the set contains a "wildcard"
     * version of every dotted name.
     */
    private Set<String> validImports;
    
    private final LoadingCache<ICompilationUnit, Collection<IASScope>> compilationUnitToScopeList =
        CacheBuilder.newBuilder()
            .weakKeys()
            .build(
            new CacheLoader<ICompilationUnit, Collection<IASScope>>()
            {
                @Override
                public Collection<IASScope> load(ICompilationUnit unit)
                {
                    return new ConcurrentLinkedQueue<IASScope>();
                }
            });

    /**
     * Constructor.
     * 
     * @param project The project that owns this project scope.
     */
    public ASProjectScope(CompilerProject project)
    {
        this.project = project;

        readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
        newVectorClassLock = new ReentrantReadWriteLock().writeLock();

        qnameToShadowedDefinitions = null;

        super.addDefinitionToStore(ClassDefinition.getAnyTypeClassDefinition());
        super.addDefinitionToStore(ClassDefinition.getVoidClassDefinition());
    }

    /**
     * Gets the {@link CompilerProject} that owns this scope.
     * 
     * @return The {@link CompilerProject} that owns this scope.
     */
    public CompilerProject getProject()
    {
        return project;
    }

    public static DefinitionPromise createDefinitionPromise(String qname, ICompilationUnit compilationUnit)
    {
        DefinitionPromise definitionPromise = new DefinitionPromise(qname, compilationUnit);
        return definitionPromise;
    }

    private IDefinitionSet replacePromisesWithDefinitions(IDefinitionSet definitionSet)
    {
        // If the existing definition set is a SmallDefinitionSet or a LargeDefinitionSet,
        // we can return the same set with the promises replaced by actual definitions.
        // If the existing definition set is a promise acting as its own set-of-size-1,
        // then we have to return a different set (the actual definition acting as its
        // own set-of-size-1.
        IDefinitionSet returnedDefinitionSet = definitionSet;
        
        int n = definitionSet.getSize();
        for (int i = 0; i < n; i++)
        {
            IDefinition definition = definitionSet.getDefinition(i);
            if (definition instanceof DefinitionPromise)
            {
                DefinitionPromise promise = (DefinitionPromise)definition;

                // Release the writeLock before calling getActualDefinition(),
                // otherwise we can deadlock on the getFileScopeRequest().
                writeLock.unlock();
                try
                {
                    definition = promise.getActualDefinition();
                }
                finally
                {
                    writeLock.lock();
                }

                if (definition != null)
                {
                    // Before replacing the promise, we need to check that the
                    // promise hasn't already been replaced by another thread
                    // between giving up the write lock, parsing, and getting
                    // the lock again.
                    if (definitionSet.getDefinition(i) == promise)
                    {
                        if (definitionSet.getMaxSize() == 1)
                            returnedDefinitionSet = (DefinitionBase)definition;
                        else
                            ((IMutableDefinitionSet)definitionSet).replaceDefinition(i, definition);
                        
                        if (shouldBeCached(definition))
                            setBuiltinDefinition(definition);
                    }
                }
            }
        }
        
        return returnedDefinitionSet;
    }

    @Override
    public IDefinitionSet getLocalDefinitionSetByName(String name)
    {
        IDefinitionSet definitionSet = null;
        boolean containsPromise = false;

        readLock.lock();
        try
        {
            // Get the definition set from the store.
            definitionSet = super.getLocalDefinitionSetByName(name);

            // Does it contain any promises?
            if (definitionSet != null)
            {
                int n = definitionSet.getSize();
                for (int i = 0; i < n; i++)
                {
                    IDefinition definition = definitionSet.getDefinition(i);
                    if (definition instanceof DefinitionPromise)
                    {
                        containsPromise = true;
                        break;
                    }
                }
            }
        }
        finally
        {
            readLock.unlock();
        }

        IDefinitionSet returnedDefinitionSet = definitionSet;

        if (containsPromise)
        {
            // Note that we lock for writing only if there is a promise to replace.
            writeLock.lock();
            try
            {
                returnedDefinitionSet = replacePromisesWithDefinitions(definitionSet);
                if (returnedDefinitionSet != definitionSet)
                    definitionStore.putDefinitionSetByName(name, returnedDefinitionSet);
            }
            finally
            {
                writeLock.unlock();
            }
        }

        return returnedDefinitionSet;
    }

    private static boolean referenceMatchesQName(IWorkspace workspace, IResolvedQualifiersReference reference, String qualifiedName)
    {
        IResolvedQualifiersReference qualifiedNameReference = ReferenceFactory.packageQualifiedReference(workspace, qualifiedName, true);
        ImmutableSet<INamespaceDefinition> referenceQualifiers = reference.getQualifiers();
        for (INamespaceDefinition qNameNS : qualifiedNameReference.getQualifiers())
        {
            if (referenceQualifiers.contains(qNameNS))
                return true;
        }
        return false;
    }

    /**
     * Find the {@link ICompilationUnit}'s in the project that define or should
     * define the definitions that the specified list of references refer to.
     * <p>
     * This method will not cause any processing for any compilation unit to be
     * started.
     * 
     * @param references A list of references.
     * @return The set of {@link ICompilationUnit}'s in the project that define
     * or should define the definitions the specified list of references refer
     * to.
     */
    public Set<ICompilationUnit> getCompilationUnitsForReferences(Iterable<IResolvedQualifiersReference> references)
    {
        return getCompilationUnitsForReferences(references, null);
    }

    /**
     * Find the {@link ICompilationUnit}'s in the project that define or should
     * define the definitions that the specified list of references refer to.
     * <p>
     * This method will not cause any processing for any compilation unit to be
     * started.
     * 
     * @param references A list of references.
     * @param unresolvedReferences A collection of references that were not
     * resolved to compilation units. May be null if the caller is not
     * interested.
     * @return The set of {@link ICompilationUnit}'s in the project that define
     * or should define the definitions the specified list of references refer
     * to.
     */
    public Set<ICompilationUnit> getCompilationUnitsForReferences(Iterable<IResolvedQualifiersReference> references,
            Collection<IResolvedQualifiersReference> unresolvedReferences)
    {
        Set<ICompilationUnit> compilationUnits = new HashSet<ICompilationUnit>();
        IWorkspace workspace = project.getWorkspace();
        readLock.lock();
        try
        {
            for (IResolvedQualifiersReference reference : references)
            {
                String baseName = reference.getName();
                // use super.getDefinitionSetByName so we don't turn definition promises into
                // actual definitions.  Doing so would require parsing files which would be slow.
                IDefinitionSet definitionSet = super.getLocalDefinitionSetByName(baseName);
                ICompilationUnit cu = null;
                if (definitionSet != null)
                {
                    int n = definitionSet.getSize();
                    for (int i = 0; i < n; i++)
                    {
                        IDefinition definition = definitionSet.getDefinition(i);
                        String definitionQualifiedName = definition.getQualifiedName();
                        if (referenceMatchesQName(workspace, reference, definitionQualifiedName))
                        {
                            cu = getCompilationUnitForDefinition(definition);
                            assert cu != null : "All symbol table entries should have a corresponding CU!";
                            compilationUnits.add(cu);
                        }

                    }
                }
                if (cu == null && unresolvedReferences != null)
                    unresolvedReferences.add(reference);
            }
        }
        finally
        {
            readLock.unlock();
        }
        return compilationUnits;
    }

    /**
     * Finds all the compilation units in the project that define or should
     * define a symbol with the specified base name.
     * <p>
     * This method will not cause any processing for any compilation unit to be
     * started.
     * 
     * @param name Base name of a symbol. For example if the definition's
     * qualified name is: <code>spark.components.Button</code> its base name is
     * <code>Button</code>.
     * @return A set of compilation units that define or should define a symbol
     * with the specified base name.
     */
    public Set<ICompilationUnit> getCompilationUnitsByDefinitionName(String name)
    {
        Set<ICompilationUnit> compilationUnits = new HashSet<ICompilationUnit>();
        readLock.lock();
        try
        {
            IDefinitionSet definitionSet = super.getLocalDefinitionSetByName(name);
            if (definitionSet != null)
            {
                int n = definitionSet.getSize();
                for (int i = 0; i < n; i++)
                {
                    IDefinition definition = definitionSet.getDefinition(i);
                    ICompilationUnit compilationUnit;
                    if (definition instanceof DefinitionPromise)
                    {
                        compilationUnit = ((DefinitionPromise)definition).getCompilationUnit();
                    }
                    else
                    {
                        IASScope containingScope = definition.getContainingScope();
                        compilationUnit = getCompilationUnitForScope(containingScope);
                    }
                    assert (compilationUnit != null); // should always be able to find our compilation unit
                    compilationUnits.add(compilationUnit);
                }
            }
        }
        finally
        {
            readLock.unlock();
        }

        return compilationUnits;
    }

    /**
     * Find all the {@link ICompilationUnit}s that define definitions with the same qualified name as the definitions defined by the
     * specified {@link Iterable} of {@link ICompilationUnit}s.
     * @param workspace {@link IWorkspace} that the {@link ICompilationUnit}s.
     * @param units {@link Iterable} of {@link ICompilationUnit}s.
     * @return The {@link Set} of {@link ICompilationUnit}s that define definitions with the same qualified name as the definitions defined by the
     * specified {@link Iterable} of {@link ICompilationUnit}s.
     */
    public static Set<ICompilationUnit> getCompilationUnitsWithConflictingDefinitions(final IWorkspace workspace, Iterable<ICompilationUnit> units)
    {
        // Set of project scopes we have encountered while traversing all the compilation units
        HashSet<ASProjectScope> projectScopes = new HashSet<ASProjectScope>();
        try
        {
            HashSet<ICompilationUnit> result = new HashSet<ICompilationUnit>();
            for (ICompilationUnit unit : units)
            {
                if (unit.isInvisible())
                {
                    result.add(unit);
                    continue;
                }

                CompilerProject project = (CompilerProject)unit.getProject();
                ASProjectScope projectScope = project.getScope();
                // If this is the first time we have encountered the project scope
                // then grab its read lock, we'll release all the read locks below
                // in the finally block.
                if (projectScopes.add(projectScope))
                    projectScope.readLock.lock();
                
                List<String> qNames;
                try
                {
                    qNames = unit.getQualifiedNames();
                }
                catch (InterruptedException e)
                {
                    qNames = Collections.emptyList();
                }
                
                Set<ICompilationUnit> visibleDefinitionCompilationUnits =
                    projectScope.getCompilationUnitsForReferences(Iterables.transform(qNames, new Function<String, IResolvedQualifiersReference>() {
                        @Override
                        public IResolvedQualifiersReference apply(String qname)
                        {
                            return ReferenceFactory.packageQualifiedReference(workspace, qname);
                        }}));
                result.addAll(visibleDefinitionCompilationUnits);
                
                for (String qName : qNames)
                {
                    Set<IDefinition> shadowedDefinitions = projectScope.getShadowedDefinitionsByQName(qName);
                    if (shadowedDefinitions != null)
                    {
                        for (IDefinition shadowedDefinition : shadowedDefinitions)
                        {
                            ICompilationUnit shadowedCompilationUnit =
                                projectScope.getCompilationUnitForDefinition(shadowedDefinition);
                            result.add(shadowedCompilationUnit);
                        }
                    }
                }
                result.add(unit);
            }
            return result;
        }
        finally
        {
            for (ASProjectScope projectScope : projectScopes)
                projectScope.readLock.unlock();
        }
    }
    
    @Override
    public Collection<IDefinitionSet> getAllLocalDefinitionSets()
    {
        Collection<String> names = getAllLocalNames();
        
        ArrayList<IDefinitionSet> result = new ArrayList<IDefinitionSet>(names.size());

        for (String name : names)
        {
            // Note: The override of getLocalDefinitionSetByName() in this class
            // will convert definition promises to actual definitions.
            IDefinitionSet set = getLocalDefinitionSetByName(name);
            result.add(set);
        }

        return result;
    }

    @Override
    public Collection<IDefinition> getAllLocalDefinitions()
    {
        Collection<String> names = getAllLocalNames();
        
        ArrayList<IDefinition> result = new ArrayList<IDefinition>(names.size());

        for (String name : names)
        {
            // Note: The override of getLocalDefinitionSetByName() in this class
            // will convert definition promises to actual definitions.
            IDefinitionSet set = getLocalDefinitionSetByName(name);
            int n = set.getSize();
            for (int i = 0; i < n; i++)
            {
                IDefinition definition = set.getDefinition(i);
                result.add(definition);
            }
        }

        return result;
    }

    public IDefinition findDefinitionByName(String definitionName)
    {
        Multiname multiname = Multiname.crackDottedQName(project, definitionName);
        return findDefinitionByName(multiname, false);
    }

    public IDefinition[] findAllDefinitionsByName(Multiname multiname)
    {
        ArrayList<IDefinition> defs = new ArrayList<IDefinition>(1);
        getLocalProperty(project, defs, multiname.getBaseName(), multiname.getNamespaceSet());
        return defs.toArray(new IDefinition[defs.size()]);
    }

    public IDefinition findDefinitionByName(Multiname multiname, boolean ignoreAmbiguous)
    {
        ArrayList<IDefinition> defs = new ArrayList<IDefinition>(1);
        getLocalProperty(project, defs, multiname.getBaseName(), multiname.getNamespaceSet());
        if ((defs.size() == 1) || (ignoreAmbiguous && (defs.size() > 0)))
            return defs.get(0);
        return null;
    }

    /**
     * Finds a definition currently visible in the symbol table with the same
     * qualified name as the specified definition. Definitions can be in the
     * symbol table but be shadowed by other definitions with the same qualified
     * name.
     * 
     * @param definition {@link IDefinition} whose qualified name is used to
     * search the symbol table.
     * @return An existing definition in the symbol table with the same
     * qualified name as the specified {@link IDefinition}. Null if no such
     * {@link IDefinition} exists.
     */
    private DefinitionBase findVisibleDefinition(IDefinition definition)
    {
        readLock.lock();
        try
        {
            String newDefinitionQName = definition.getQualifiedName();
            String newDefBaseName = definition.getBaseName();
            // It's important that we use super.getLocalDefinitionSetByName() here;
            // we don't want to cause getActualDefinition() to be called on
            // any definition promises.
            IDefinitionSet defSet = super.getLocalDefinitionSetByName(newDefBaseName);
            if (defSet != null)
            {
                int nDefs = defSet.getSize();
                for (int i = 0; i < nDefs; ++i)
                {
                    IDefinition existingDef = defSet.getDefinition(i);
                    String existingDefQName = existingDef.getQualifiedName();
                    if (existingDefQName.equals(newDefinitionQName))
                        return (DefinitionBase)existingDef;
                }
            }
        }
        finally
        {
            readLock.unlock();
        }
        return null;
    }

    /**
     * Compares two compilation units by their definition priority.
     * <p>
     * This method is used by the definition-shadowing logic.
     */
    private int compareCompilationUnits(ICompilationUnit a, ICompilationUnit b)
    {
        IDefinitionPriority priorityA = a.getDefinitionPriority();
        IDefinitionPriority priorityB = b.getDefinitionPriority();
        return priorityA.compareTo(priorityB);
    }

    /**
     * Compares two definitions by the definition priority of their
     * compilation units.
     * <p>
     * This method is used by the definition-shadowing logic.
     */
    private int compareDefinitions(IDefinition a, IDefinition b)
    {
        ICompilationUnit cuA = getCompilationUnitForDefinition(a);
        ICompilationUnit cuB = getCompilationUnitForDefinition(b);
        return compareCompilationUnits(cuA, cuB);
    }

    /**
     * Helper method used by <code>addDefinition</code> to handle
     * definition-shadowing.
     * <p>
     * This method adds shadowed definitions to the shadow sets
     * in the <code>qnameToShadowedDefinitions</code> map.
     * 
     * @param visibleDefinition The currently visible definition, if any,
     * with the same qualified name as the definition being added to the scope.
     * @param newDefinition The new definition being added to the scope.
     * @return <code>true</code> if the new definition should be added to the store.
     */
    private boolean shadowDefinitionIfNeeded(IDefinition visibleDefinition, IDefinition newDefinition)
    {
        // If there is no visible definition that could be shadowed by the new one,
        // then the new one should simply be added to the store.
        if (visibleDefinition == null)
            return true;
        
        // Otherwise, one of the two definitions will shadow the other,
        // so get the set of already-shadowed definitions with the same qname.

        String qname = visibleDefinition.getQualifiedName();
        assert qname.equals(newDefinition.getQualifiedName());

        if (qnameToShadowedDefinitions == null)
            qnameToShadowedDefinitions = new HashMap<String, Set<IDefinition>>();

        Set<IDefinition> shadowedDefinitions = qnameToShadowedDefinitions.get(qname);
        if (shadowedDefinitions == null)
        {
            shadowedDefinitions = new HashSet<IDefinition>(1);
            qnameToShadowedDefinitions.put(qname, shadowedDefinitions);
        }
        
        // Compare the definition priorities of the two definitions'
        // compilation units to determine which goes into the shadowed set
        // and which goes into the visible store.
        
        ICompilationUnit visibleDefinitionCU = getCompilationUnitForDefinition(visibleDefinition);
        ICompilationUnit newDefinitionCU = getCompilationUnitForDefinition(newDefinition);
        
        if (compareCompilationUnits(visibleDefinitionCU, newDefinitionCU) >= 0)
        {
            // Old shadows new.
            // Put the new definition into the shadow set,
            // and return a flag saying to leave the old definition visible.
            shadowedDefinitions.add(newDefinition);
            return false;
        }
        else
        {
            // New shadows old.
            // Put the old visible definition into the shadow set,
            // and return a flag saying the make the new definition visible.
            // If the visible compilation unit is using promises,
            // we need to put a promise into the shadow set rather
            // than an actual definition.
            List<IDefinition> promises = visibleDefinitionCU.getDefinitionPromises();
            if ((!promises.isEmpty()) && (!(visibleDefinition instanceof DefinitionPromise)))
            {
                IDefinition promiseForVisibleDefinition = null;
                for (IDefinition promise : promises)
                {
                    if (promise.getQualifiedName().equals(qname))
                    {
                        promiseForVisibleDefinition = promise;
                        break;
                    }
                }
                assert promiseForVisibleDefinition != null;
                assert ((DefinitionPromise)promiseForVisibleDefinition).getCompilationUnit() == visibleDefinitionCU;
                assert ((DefinitionPromise)promiseForVisibleDefinition).getActualDefinition() == visibleDefinition;
                shadowedDefinitions.add(promiseForVisibleDefinition);
            }
            else
            {
                // The compilation unit does not use definition promises,
                // so we just shadow the visible definition directly.
                shadowedDefinitions.add(visibleDefinition);
            }
            return true;
        }
    }
    
    private Set<IDefinition> getShadowedDefinitionsByQName(String qName)
    {
        if (qnameToShadowedDefinitions == null)
            return null;
        
        final Set<IDefinition> shadowedDefs = qnameToShadowedDefinitions.get(qName);
        if (shadowedDefs != null)
            return shadowedDefs;
        return null;
    }

    /**
     * Gets the set of definitions that have the same qname as specified definition
     * but are lower priority and thus not visible.
     * 
     * @param def The definition to get shadowed definitions for. May not be
     * null.
     * @return A set of shadowed definitions. Returns null if there are no
     * shadowed definitions.
     * @throws NullPointerException if def is null.
     */
    public Set<IDefinition> getShadowedDefinitions(IDefinition def)
    {
        Set<IDefinition> shadowedDefs = null;

        if (def == null)
            throw new NullPointerException("def may not be null");

        readLock.lock();

        try
        {
            assert getCompilationUnitForDefinition(def) != null : "def must either be a definition promise or addScopeForCompilationUnit must be called before addDefinition";
            if (qnameToShadowedDefinitions != null)
            {
                String qname = def.getQualifiedName();
                shadowedDefs = qnameToShadowedDefinitions.get(qname);
            }
        }
        finally
        {
            readLock.unlock();
        }

        return shadowedDefs;
    }

    @Override
    public void addDefinition(IDefinition def)
    {
        writeLock.lock();
        try
        {
            assert getCompilationUnitForDefinition(def) != null : "def must either be a definition promise or addScopeForCompilationUnit must be called before addDefinition";

            // Find the visible definition, if any, with the same qname
            // as the definition being added.
            IDefinition existingDef = findVisibleDefinition(def);
            
            // Handle the fact that this visible definition might shadow,
            // or might be shadowed by, the definition being added.
            // This method will update the shadow sets if necessary,
            // and return a flag telling us whether to put def into
            // definition store.
            boolean shouldAddDef = shadowDefinitionIfNeeded(existingDef, def);

            if (shouldAddDef)
            {
                if (existingDef != null)
                    super.removeDefinition(existingDef);
                addDefinitionToStore(def);
            }

            assert (def instanceof DefinitionPromise) || (getCompilationUnitForScope(def.getContainingScope()) != null);
        }
        finally
        {
            // Clear the validImports when a definition is added to the project,
            // so that it will get rebuilt then next time isValidImport() is called.
            // But don't bother doing this if the definition is for an embed class;
            // these get added late to the project scope, but they can't be imported
            // and therefore shouldn't cause pointless rebuilding of validImports.
            if (!def.isGeneratedEmbedClass())
                validImports = null;

            writeLock.unlock();
        }
    }

    @Override
    protected void addDefinitionToStore(IDefinition def)
    {
        super.addDefinitionToStore(def);

        if (!(def instanceof DefinitionPromise) && shouldBeCached(def))
            setBuiltinDefinition(def);

        // Clear the validImports when a definition is added to the project,
        // so that it will get rebuilt then next time isValidImport() is called.
        // But don't bother doing this if the definition is for an embed class;
        // these get added late to the project scope, but they can't be imported
        // and therefore shouldn't cause pointless rebuilding of validImports.
        if (!def.isGeneratedEmbedClass())
            validImports = null;
    }

    /**
     * Set one of the builtin definitions for fast access when they're needed
     * 
     * @param def the builtin definition to set
     */
    private void setBuiltinDefinition(IDefinition def)
    {
        String defName = def.getBaseName();
        if (def.getNamespaceReference() == NamespaceDefinition.getPublicNamespaceDefinition())
        {
            if (defName.equals(IASLanguageConstants.BuiltinType.OBJECT.getName()))
                objectDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.BuiltinType.STRING.getName()))
                stringDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.BuiltinType.ARRAY.getName()))
                arrayDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.BuiltinType.XML.getName()))
                xmlDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.BuiltinType.XMLLIST.getName()))
                xmllistDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.BuiltinType.BOOLEAN.getName()))
                booleanDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.BuiltinType.INT.getName()))
                intDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.BuiltinType.UINT.getName()))
                uintDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.BuiltinType.NUMBER.getName()))
                numberDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.BuiltinType.CLASS.getName()))
                classDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.BuiltinType.FUNCTION.getName()))
                functionDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.BuiltinType.NAMESPACE.getName()))
                namespaceDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.UNDEFINED))
                undefinedValueDefinition = def;
            else
                assert false;       // precondition is that this function only called with cachable defs.
                                    // If we see one we don't recognize, probably just need to add a handler for it
        }
        else if (def.getPackageName().equals(IASLanguageConstants.Vector_impl_package))
        {
            if (defName.equals(IASLanguageConstants.BuiltinType.VECTOR.getName()))
                vectorDefinition = (ITypeDefinition)def;
            else if (defName.equals(IASLanguageConstants.Vector_object))
                updateAppliedVectorDefinitionBaseClasses(IASLanguageConstants.Vector_object);
            else if (defName.equals(IASLanguageConstants.Vector_double))
                updateAppliedVectorDefinitionBaseClasses(IASLanguageConstants.Vector_double);
            else if (defName.equals(IASLanguageConstants.Vector_int))
                updateAppliedVectorDefinitionBaseClasses(IASLanguageConstants.Vector_int);
            else if (defName.equals(IASLanguageConstants.Vector_uint))
                updateAppliedVectorDefinitionBaseClasses(IASLanguageConstants.Vector_uint);
        }
    }
    
    private void updateAppliedVectorDefinitionBaseClasses(String changedVectorImplClass)
    {
        for (AppliedVectorDefinition appliedVectorDefinition : vectorElementTypeToVectorClassMap.values())
            appliedVectorDefinition.updateBaseClass(changedVectorImplClass);
    }

    /**
     * remove one of the builtin definitions
     * 
     * @param visibleDefinition the definition to remove
     */
    private void removeBuiltinDefinition(IDefinition visibleDefinition)
    {
        if (visibleDefinition == objectDefinition)
            objectDefinition = null;
        else if (visibleDefinition == stringDefinition)
            stringDefinition = null;
        else if (visibleDefinition == arrayDefinition)
            arrayDefinition = null;
        else if (visibleDefinition == xmlDefinition)
            xmlDefinition = null;
        else if (visibleDefinition == xmllistDefinition)
            xmllistDefinition = null;
        else if (visibleDefinition == booleanDefinition)
            booleanDefinition = null;
        else if (visibleDefinition == intDefinition)
            intDefinition = null;
        else if (visibleDefinition == uintDefinition)
            uintDefinition = null;
        else if (visibleDefinition == numberDefinition)
            numberDefinition = null;
        else if (visibleDefinition == classDefinition)
            classDefinition = null;
        else if (visibleDefinition == functionDefinition)
            functionDefinition = null;
        else if (visibleDefinition == namespaceDefinition)
            namespaceDefinition = null;
        else if (visibleDefinition == vectorDefinition)
            vectorDefinition = null;
        else if (visibleDefinition == undefinedValueDefinition)
            undefinedValueDefinition = null;
        else assert false;          // precondition is that this function only called with cachable defs.
                                    // If we see one we don't recognize, probably just need to add a handler for it
    }

    @Override
    public void removeDefinition(IDefinition definition)
    {
        writeLock.lock();
        try
        {
            // Find the visible definition with the same qname
            // as the definition being added.
            IDefinition visibleDefinition = findVisibleDefinition(definition);
            assert visibleDefinition != null : "Can't remove a definition that is not in the symbol table.";
            
            if (visibleDefinition == definition)
            {
                // The definition we're removing is visible, not shadowed.
                
                if (!(visibleDefinition instanceof DefinitionPromise) && shouldBeCached(visibleDefinition))
                    removeBuiltinDefinition(visibleDefinition);

                // The definition we are removing is not shadowed by another
                // definition, so remove the definition from the definition store.
                super.removeDefinition(definition);

                // Next we need to see if the definition we are removing
                // shadows some other definition.
                Set<IDefinition> shadowedDefs = getShadowedDefinitions(definition);
                if (shadowedDefs != null)
                {
                    // The definition we are removing shadows at least one other
                    // definition, so we need find the definition that was shadowed
                    // with the highest priority, remove that definition from the
                    // shadow set, and add that definition to the definition store.
                    IDefinition nextDef;
                    if (shadowedDefs.size() == 1)
                    {
                        nextDef = shadowedDefs.iterator().next();
                        // There are no longer any shadowed definitions for this qname.
                        qnameToShadowedDefinitions.remove(definition.getQualifiedName());
                    }
                    else
                    {
                        // Sort all the shadowed definitions for this qname
                        // by the definition priority of the compilation units
                        // that contain those definitions.
                        IDefinition[] shadowedDefsArr =
                                shadowedDefs.toArray(new IDefinition[0]);
                        Arrays.sort(shadowedDefsArr, new Comparator<IDefinition>()
                        {
                            @Override
                            public int compare(IDefinition d1, IDefinition d2)
                            {
                                assert d1 != null;
                                assert d2 != null;
                                return 0 - compareDefinitions(d1, d2);
                            }
                        });
                        nextDef = shadowedDefsArr[0];
                        shadowedDefs.remove(nextDef);
                    }
                    addDefinitionToStore(nextDef);
                }
                return;
            }
            
            // The definition we're removing is shadowed.
            // It might be a promise or an actual definition.
            
            if ((!(visibleDefinition instanceof DefinitionPromise)) && (definition instanceof DefinitionPromise))
            {
                // visibleDefinition might be the actual definition of the
                // promise we are trying to remove.
                ICompilationUnit visibleDefinitionCU = getCompilationUnitForDefinition(visibleDefinition);
                if (visibleDefinitionCU == ((DefinitionPromise)definition).getCompilationUnit())
                {
                    // visibleDefinition is the actual definition for the definition we are trying to remove.
                    // We'll just remove the actual definition for our promise, by calling this method recursively.
                    assert visibleDefinition == findVisibleDefinition(visibleDefinition) : "assert if we are about to start infinite recursion";
                    removeDefinition(visibleDefinition); // recursion!
                    return;
                }
            }

            // Remove the definition from the shadow set.
            String qName = definition.getQualifiedName();
            Set<IDefinition> shadowedDefinitions = qnameToShadowedDefinitions.get(qName);
            assert shadowedDefinitions != null : "If the def to remove is shadowed, we should have a set of shadowed defs.";
            assert shadowedDefinitions.contains(definition) : "If the def to remove is shadowed it should be in this set.";
            if (shadowedDefinitions.size() == 1)
                qnameToShadowedDefinitions.remove(qName);
            else
                shadowedDefinitions.remove(definition);

        }
        finally
        {
            validImports = null;
            writeLock.unlock();
        }
    }

    @Override
    public void compact()
    {
        writeLock.lock();
        try
        {
            super.compact();
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public Collection<String> getAllLocalNames()
    {
        readLock.lock();
        try
        {
            return super.getAllLocalNames();
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Gets a collection of the qualified names for all the definitions in this
     * project scope. Each qualified name is a dotted qualified name of the
     * form: flash.display.DisplayObject
     * 
     * @return A collection of dotted qualified names
     */
    public Collection<String> getAllQualifiedNames()
    {
        readLock.lock();
        try
        {
            Collection<String> result = new ArrayList<String>();
            // Note: The inherited version of getAllLocalDefinitionSets() will not
            // convert definition promises to actual definitions.
            for (IDefinitionSet definitionSet : super.getAllLocalDefinitionSets())
            {
                int nDefinitionsInSet = definitionSet.getSize();
                for (int i = 0; i < nDefinitionsInSet; ++i)
                    result.add(definitionSet.getDefinition(i).getQualifiedName());
            }
            return result;
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Associates an {@link IASScope} for a file or package scope with a
     * {@link ICompilationUnit}. This association is used to establish
     * dependencies between {@link ICompilationUnit}'s, when resolving
     * definition across {@link ICompilationUnit} boundaries.
     * 
     * @param cu A compilation unit.
     * @param scope An {@link ASFileScope} for a file
     */
    public void addScopeForCompilationUnit(ICompilationUnit cu, ASFileScope scope)
    {
        if (scope.setCompilationUnit(cu))
            return;

        writeLock.lock();
        try
        {
            assert scope instanceof SWCFileScope : "only SWCFileScope should be added to swcFileScopeToCompilationUnitMap";
            swcFileScopeToCompilationUnitMap.put((SWCFileScope)scope, cu);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    private void removeScopeForCompilationUnit(ASFileScope scope)
    {
        if (scope.setCompilationUnit(null))
        {
            assert (!swcFileScopeToCompilationUnitMap.containsKey(scope)) : "scope should not be in the scopeToCompilationUnitMap when setCompilationUnit returns true";
            return;
        }

        // there is no need to grab the lock here, as removeScopeForCompilationUnit()
        // should only be called from removeCompilationUnits() which already
        // has the lock
        assert scope instanceof SWCFileScope : "only SWCFileScope should be in swcFileScopeToCompilationUnitMap";
        swcFileScopeToCompilationUnitMap.remove(scope);
    }

    /**
     * Get the ICompilationUnit from which the scope is declared
     * 
     * @param scope The scope in question
     * @return The ICompilationUnit in which the scope is declared
     */
    public ICompilationUnit getCompilationUnitForScope(IASScope scope)
    {
        while ((scope != null) && (!(scope instanceof ASFileScope)))
        {
            scope = scope.getContainingScope();
        }

        if (scope == null)
            return null;

        ASFileScope fileScope = (ASFileScope)scope;
        ICompilationUnit compilationUnit = fileScope.getCompilationUnit();
        if (compilationUnit != null)
        {
            assert compilationUnit.getProject() == getProject();
            return compilationUnit;
        }
        readLock.lock();
        try
        {
            assert fileScope instanceof SWCFileScope : "only SWCFileScope should be in swcFileScopeToCompilationUnitMap";
            ICompilationUnit swcCompilationUnit = swcFileScopeToCompilationUnitMap.get(fileScope);
            assert (swcCompilationUnit == null) || (swcCompilationUnit.getProject() == getProject());
            return swcCompilationUnit;
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    /**
     * Adds the specified {@link ASScope} to the list of {@link IASScope}s
     * associated with the {@link ICompilationUnit} that contains the specified
     * {@link ASScope}.  This method is called for when a {@link ASScopeCache} is
     * created for an {@link ASScope} or when {@link IDefinition}s from a {@link ICompilationUnit} does
     * not have {@link DefinitionPromise}s are added to the {@link ASProjectScope}.
     * @param scope The scope to be added.
     */
    public void addScopeToCompilationUnitScopeList(ASScope scope)
    {
        // Scopes inside of Vector types ( like Vector.<String> ),
        // do not have an associated compilation unit.
        if (AppliedVectorDefinition.isVectorScope(scope))
            return;
        
        // find the compilation unit which corresponds to the scope, and maintain a mapping
        // of compilation unit to scopes, so we can easily invalidate the scope caches
        ICompilationUnit compilationUnit = getCompilationUnitForScope(scope);
        assert compilationUnit != null;
        Collection<IASScope> relatedScopes = compilationUnitToScopeList.getUnchecked(compilationUnit);
        relatedScopes.add(scope);
        
    }
    
    /**
     * Clears the list of {@link IASScope}s associated with the specified
     * {@link ICompilationUnit}.
     * 
     * @param compilationUnit The {@link ICompilationUnit} whose associated list
     * of {@link IASScope}s should be cleared.
     * @return The {@link List} of {@link IASScope}s associated with the
     * specified {@link ICompilationUnit} before this method was called.
     */
    public Collection<IASScope> clearCompilationUnitScopeList(ICompilationUnit compilationUnit)
    {
        Collection<IASScope> scopeList = compilationUnitToScopeList.getUnchecked(compilationUnit);
        compilationUnitToScopeList.invalidate(compilationUnit);
        return scopeList;
    }
    
    /**
     * Gets the {@link List} of {@link IASScope}s that have an associated {@link ASScopeCache} or define
     * {@link IDefinition}s currently in the {@link ASProjectScope} for the specified {@link ICompilationUnit}.
     * 
     * @param compilationUnit {@link ICompilationUnit} of the scopes to query
     * @return List of scopes for the compilation unit.  Never null.
     */
    public Collection<IASScope> getCompilationUnitScopeList(ICompilationUnit compilationUnit)
    {
        if (compilationUnitToScopeList.getIfPresent(compilationUnit) == null)
            return Collections.emptyList();
        Collection<IASScope> result = compilationUnitToScopeList.getUnchecked(compilationUnit);
        assert result != null;
        assert !result.isEmpty();
        return result;
    }

    /**
     * Gets the {@link ICompilationUnit} that contains the specified
     * {@link IDefinition}.
     * 
     * @param def {@link IDefinition} whose containing {@link ICompilationUnit}
     * should be returned.
     * @return {@link ICompilationUnit} that contains the specified
     * {@link IDefinition} or null.
     */
    public ICompilationUnit getCompilationUnitForDefinition(IDefinition def)
    {
        if (def == null)
            return null;

        // This check is really important because this method is called by other methods
        // in this class that do not want to cause compilation units to do work.
        if (def instanceof DefinitionPromise)
            return ((DefinitionPromise)def).getCompilationUnit();

        return getCompilationUnitForScope(def.getContainingScope());
    }

    /**
     * Removes a set of {@link ICompilationUnit}'s from the project scope.
     * 
     * @param compilationUnitsToRemove The collection of compilation units to remove.
     */
    public void removeCompilationUnits(Collection<ICompilationUnit> compilationUnitsToRemove)
    {
        if ((compilationUnitsToRemove == null) || (compilationUnitsToRemove.size() == 0))
            return;

        writeLock.lock();
        try
        {
            // build up collections of items to remove so we are not
            // changing data structures as we iterate them.
            Collection<IDefinition> defsToRemove = new HashSet<IDefinition>(compilationUnitsToRemove.size());
            Collection<IASScope> scopesToRemove = new ArrayList<IASScope>(compilationUnitsToRemove.size());

            for (ICompilationUnit compilationUnit : compilationUnitsToRemove)
            {
                Collection<IASScope> relatedScopes = getCompilationUnitScopeList(compilationUnit);
                List<IDefinition> definitionPromises = compilationUnit.getDefinitionPromises();
                if (definitionPromises.isEmpty())
                {
                    Collection<IDefinition> relatedDefs = new ArrayList<IDefinition>();
                    for (IASScope scope : relatedScopes)
                    {
                        if (scope instanceof ASFileScope)
                            ((ASFileScope)scope).collectExternallyVisibleDefinitions(relatedDefs, false);
                    }

                    defsToRemove.addAll(relatedDefs);
                }
                else
                {
                    defsToRemove.addAll(definitionPromises);
                }

                scopesToRemove.addAll(relatedScopes);
                project.clearScopeCacheForCompilationUnit(compilationUnit);
            }
            
            for (IDefinition defToRemove : defsToRemove)
                removeDefinition(defToRemove);

            for (IASScope scope : scopesToRemove)
            {
                if (scope instanceof ASFileScope)
                    removeScopeForCompilationUnit((ASFileScope)scope);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public IASScope getContainingScope()
    {
        return null;
    }

    private AppliedVectorDefinition getExistingVectorClass(ITypeDefinition elementType)
    {
        readLock.lock();
        try
        {
            return vectorElementTypeToVectorClassMap.get(elementType);
        }
        finally
        {
            readLock.unlock();
        }
    }

    public AppliedVectorDefinition newVectorClass(ITypeDefinition elementType)
    {
        // First try to get an existing vector class
        // just using the read lock.
        AppliedVectorDefinition existingVectorClass = getExistingVectorClass(elementType);
        if (existingVectorClass != null)
            return existingVectorClass;

        // No dice, looks like we might have to create the
        // vector class.
        newVectorClassLock.lock();
        try
        {
            // Now that we have the write lock, make sure nobody created
            // the vector class we need while we were waiting for the write lock.
            existingVectorClass = getExistingVectorClass(elementType);
            if (existingVectorClass != null)
                return existingVectorClass;

            // ok.  Now we know for sure we'll have to create the new vector class.

            //Default to creating a subclass of Vector$object.    		
            String vectorTypeName = null;

            // We need to see if the elementType is int, Number, or uint.
            // If the elementType is int, Number, or uint, then the vector class is a special
            // class that we just need to look up in the project symbol table, otherwise we
            // need to sub-class Vector$object.

            // Some short cuts here to avoid extra lookups.
            INamespaceReference elementTypeNSRef = elementType.getNamespaceReference();
            if ((elementTypeNSRef.equals(NamespaceDefinition.getPublicNamespaceDefinition())) && // int, Number, and uint are all public classes.
            (elementType.getPackageName().length() == 0) && // int, Number, and uint are all in the unnamed package.
            (elementType instanceof IClassDefinition) && // int, Number, and uint are all classes.
            (!(elementType instanceof AppliedVectorDefinition))) // int, Number and uint are not Vector classes.
            {
                // Now just compare the elementType's string base name to
                // int, uint, and Number.  If one of those matches, then
                // we'll lookup the corresponding type and see if it matches
                // the specified element type.
                String elementTypeName = elementType.getBaseName();
                if (elementTypeName.equals(IASLanguageConstants._int))
                    vectorTypeName = IASLanguageConstants.Vector_int;
                else if (elementTypeName.equals(IASLanguageConstants.uint))
                    vectorTypeName = IASLanguageConstants.Vector_uint;
                else if (elementTypeName.equals(IASLanguageConstants.Number))
                    vectorTypeName = IASLanguageConstants.Vector_double;

                if (vectorTypeName != null)
                {
                    IDefinition numberTypeClassDef = findDefinitionByName(elementTypeName);
                    assert numberTypeClassDef != null : "Unable to lookup: " + elementTypeName;
                    if (!numberTypeClassDef.equals(elementType))
                    {
                        // The element type was not actually a number class
                        // back to square 1.
                        vectorTypeName = null;
                    }
                }

            }
            if (vectorTypeName == null)
                vectorTypeName = IASLanguageConstants.Vector_object;
            
            IClassDefinition vectorImplClass = AppliedVectorDefinition.lookupVectorImplClass(project, vectorTypeName);
            AppliedVectorDefinition result = new AppliedVectorDefinition(project, vectorImplClass, elementType);
            vectorElementTypeToVectorClassMap.put(elementType, result);

            if (vectorTypeName == IASLanguageConstants.Vector_object)
                result.adjustVectorMethods(this.project);

            return result;
        }
        finally
        {
            newVectorClassLock.unlock();
        }
    }

    /**
     * When doing name resolution in an {@link IInvisibleCompilationUnit} the
     * file and package scopes need to be consulted to determine if they contain
     * the definition we are looking for. This extra code is needed because the
     * package level definitions in an {@link IInvisibleCompilationUnit} are not
     * registered with the {@link ASProjectScope}.
     * 
     * @param referencingCU
     * @param defs
     * @param baseName
     * @param namespaceSet
     */
    private void getPropertyForScopeChainInvisibleCompilationUnit(ICompilationUnit referencingCU, Collection<IDefinition> defs, String baseName, Set<INamespaceDefinition> namespaceSet)
    {
        assert referencingCU.isInvisible();
        try
        {
            final Collection<IDefinition> externallyVisibleDefs =
                referencingCU.getFileScopeRequest().get().getExternallyVisibleDefinitions();
            final ICompilerProject project = referencingCU.getProject();
            for (IDefinition externallyVisibleDefinition : externallyVisibleDefs)
            {
                if (baseName.equals(externallyVisibleDefinition.getBaseName()))
                    accumulateMatchingDefinitions(referencingCU, project, defs, namespaceSet, true, null, externallyVisibleDefinition);
            }
        }
        catch (InterruptedException e)
        {
            // If we get interrupted we'll just pretend there were no definitions
            // in the file of interest.
        }
        
    }
    
    public void getPropertyForScopeChain(ASScope referencingScope, Collection<IDefinition> defs, String baseName, Set<INamespaceDefinition> namespaceSet, DependencyType dt)
    {
        final ICompilationUnit referencingCU = getCompilationUnitForScope(referencingScope);
        
        if ((referencingCU != null) && (referencingCU.isInvisible()))
        {
            getPropertyForScopeChainInvisibleCompilationUnit(referencingCU, defs, baseName, namespaceSet);
        }
        
        if ((dt != null) && !AppliedVectorDefinition.isVectorScope(referencingScope))
        {
            assert referencingCU != null : "this can only be null if the ref scope is a vector scope, which we guard against.";
            findDefinitionByName(referencingCU, defs, baseName, namespaceSet, dt);
        }
        else
        {
            getLocalProperty(referencingCU, project, defs, baseName, namespaceSet, true, null);
        }
    }

    public void findDefinitionByName(ICompilationUnit referencingCU, Collection<IDefinition> defs, String baseName, Set<INamespaceDefinition> namespaceSet, DependencyType dt)
    {
        getLocalProperty(referencingCU, project, defs, baseName, namespaceSet, true, null);
        if (dt != null)
        {
            for (IDefinition def : defs)
            {
                assert def != null;
                if (!def.isImplicit())
                {
                    assert referencingCU != null;
                    assert def.getContainingScope() != null : "null containing scope: " + def.getBaseName();
                    assert (def.getContainingScope() instanceof ASFileScope) || (def.getContainingScope().getDefinition() instanceof PackageDefinition);
                    ICompilationUnit referencedCU = getCompilationUnitForScope(def.getContainingScope());
                    assert referencedCU != null;
                    project.addDependency(referencingCU, referencedCU, dt, def.getQualifiedName());
                }
            }
        }

        // if a definition could not be found, keep track of base definition name
        // and the CU which referenced the definition, so if at a later stage the
        // definition is added to the project, we can updated the referencing CU.
        if (defs.isEmpty())
        {
            project.addUnfoundDefinitionDependency(baseName, referencingCU);
        }
    }

    /**
     * Determines if the specified definition is visible in the project scope.
     * <p>
     * A definition is a project can be hidden by other definitions with same
     * qualified name that have a higher definition priority. A definition's
     * priority is influenced by the following: Whether the definition is from
     * source or library The time stamp associated with a definition
     */
    public boolean isActiveDefinition(IDefinition definition)
    {
        assert definition != null;
        //Only definitions that are in a scope are allowed
        assert definition.getContainingScope() instanceof ASFileScope || definition.getContainingScope() instanceof PackageScope;

        IDefinition visibleDefinition = findVisibleDefinition(definition);
        if (definition == visibleDefinition)
            return true;
        if (visibleDefinition instanceof DefinitionPromise)
        {
            DefinitionPromise defPromise = (DefinitionPromise)visibleDefinition;
            if (definition == defPromise.getActualDefinition())
                return true;
        }
        return false;
    }

    public void findDefinitionByName(Collection<IDefinition> defs, String baseName, Set<INamespaceDefinition> namespaceSet)
    {
        getLocalProperty(project, defs, baseName, namespaceSet);
    }

    /**
     * Determines whether a specified import name such as <code>"MyClass"</code>
     * , <code>"flash.display.Sprite"</code> or <code>"flash.display.*"</code>
     * is valid, by determining whether it matches the name of any definition in
     * this project scope.
     */
    public boolean isValidImport(String importName)
    {
        // Determine whether we need to rebuild the validImports Set.
        // To get validImports, we need the read lock.
        boolean needToBuildValidImports = false;
        readLock.lock();
        try
        {
            if (validImports == null)
                needToBuildValidImports = true;
        }
        finally
        {
            readLock.unlock();
        }

        if (needToBuildValidImports)
        {
            // We need to rebuild the validImports Set.
            // To set validImports, we need the write lock.
            writeLock.lock();
            try
            {
                // Make sure we still need to rebuild it.
                // Another thread might have done it
                // between the read lock and the write kicj,
                if (validImports == null)
                {
                    validImports = new HashSet<String>();
                    for (String name : getAllQualifiedNames())
                    {
                        validImports.add(name);
                        validImports.add(ImportNode.makeWildcardName(name));
                    }
                }
            }
            finally
            {
                writeLock.unlock();
            }
        }

        // Query the validImports Set.
        // This requires the read lock.
        boolean isValid = false;
        readLock.lock();
        try
        {
            isValid = validImports.contains(importName);
        }
        finally
        {
            readLock.unlock();
        }
        return isValid;
    }

    /**
     * Adds all definitions ( including definitions from base types ) in the
     * current scope to the specified collections of definitions that have a
     * namespace qualifier in the specified definition set.
     * 
     * @param project {@link CompilerProject} used to resolve reference to
     * definitions outside of the {@link ICompilationUnit} that contains this
     * scope.
     * @param defs Collection that found {@link IDefinition}'s are added to.
     * @param namespaceSet Namespace set in which the qualifier of any matching
     * definition must exist to be considered a match.
     */
    public void getAllProperties(CompilerProject project, Collection<IDefinition> defs, Set<INamespaceDefinition> namespaceSet)
    {
        getAllLocalProperties(project, defs, namespaceSet, null);
    }

    /*
     * Definitions with these qnames are cached in the fields objectDefinition,
     * stringDefinition, etc. of an ASProjectScope for quick retrieval.
     */
    private static final HashSet<String> CACHED_DEFINITION_QNAMES = new HashSet<String>();
    static
    {
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.Object);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.String);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.Array);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.XML);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.XMLList);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.Boolean);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants._int);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.uint);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.Number);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.Class);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.Function);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.Namespace);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.Vector_qname);
        CACHED_DEFINITION_QNAMES.add(IASLanguageConstants.UNDEFINED);       // Note: this is the VALUE "undefined"
    }
    
    /*
     * Returns true if the specified definition gets cached in a project scope.
     */
    private static boolean shouldBeCached(IDefinition definition)
    {
        String qname = definition.getQualifiedName();
        return CACHED_DEFINITION_QNAMES.contains(qname);
    }
   
    private ITypeDefinition objectDefinition;
    private ITypeDefinition stringDefinition;
    private ITypeDefinition arrayDefinition;
    private ITypeDefinition xmlDefinition;
    private ITypeDefinition xmllistDefinition;
    private ITypeDefinition booleanDefinition;
    private ITypeDefinition intDefinition;
    private ITypeDefinition uintDefinition;
    private ITypeDefinition numberDefinition;
    private ITypeDefinition classDefinition;
    private ITypeDefinition functionDefinition;
    private ITypeDefinition namespaceDefinition;
    private ITypeDefinition vectorDefinition;
    
    private IDefinition undefinedValueDefinition;  // This is not class def, it's a variable def. ex: if (foo == undefined) {}
    
    public final ITypeDefinition getObjectDefinition()
    {
        if (objectDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the objectDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.OBJECT.getName());
        }

        return objectDefinition;
    }

    public final ITypeDefinition getStringDefinition()
    {
        if (stringDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the stringDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.STRING.getName());
        }

        return stringDefinition;
    }

    public final ITypeDefinition getArrayDefinition()
    {
        if (arrayDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the arrayDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.ARRAY.getName());
        }

        return arrayDefinition;
    }

    public final ITypeDefinition getXMLDefinition()
    {
        if (xmlDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the arrayDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.XML.getName());
        }

        return xmlDefinition;
    }

    public final ITypeDefinition getXMLListDefinition()
    {
        if (xmllistDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the arrayDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.XMLLIST.getName());
        }

        return xmllistDefinition;
    }

    public final ITypeDefinition getBooleanDefinition()
    {
        if (booleanDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the booleanDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.BOOLEAN.getName());
        }

        return booleanDefinition;
    }

    public final ITypeDefinition getIntDefinition()
    {
        if (intDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the intDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.INT.getName());
        }

        return intDefinition;
    }

    public final ITypeDefinition getUIntDefinition()
    {
        if (uintDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the uintDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.UINT.getName());
        }

        return uintDefinition;
    }

    public final ITypeDefinition getNumberDefinition()
    {
        if (numberDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the numberDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.NUMBER.getName());
        }

        return numberDefinition;
    }

    public final ITypeDefinition getClassDefinition()
    {
        if (classDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the classDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.CLASS.getName());
        }

        return classDefinition;
    }

    public final ITypeDefinition getFunctionDefinition()
    {
        if (functionDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the functionDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.FUNCTION.getName());
        }

        return functionDefinition;
    }

    public final ITypeDefinition getNamespaceDefinition()
    {
        if (namespaceDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the namespaceDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.NAMESPACE.getName());
        }

        return namespaceDefinition;
    }

    public final ITypeDefinition getVectorDefinition()
    {
        if (vectorDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the classDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.VECTOR.getName());
        }

        return vectorDefinition;
    }

    public final IDefinition getUndefinedValueDefinition()
    {
        if (undefinedValueDefinition == null)
        {
            // If we don't have an Definition for Object yet, call getDefinitionSetByName,
            // which will transform any DefinitionPromises -> Definitions, and set the classDefinition member.
            getLocalDefinitionSetByName(IASLanguageConstants.BuiltinType.Undefined.getName());
        }

        return undefinedValueDefinition;
    }

    // TODO This method is a copy-and-paste of its supermethod in ASScopeBase, 
    // with defs.add(definition) replaced by accumulateDefinitions(defs, definition) 
    // in order to enforce the Set-ness of the collection to which the project scope 
    // is contributing. See comment on accumulateDefinitions() for more info. 
    // Remove this override when we start using Set<IDefinition>. 
    // We could have made accumulateDefinitions() virtual instead of getLocalProperty(), 
    // but that would have had worse performance. 
    private void getLocalProperty(ICompilationUnit referencingCU, ICompilerProject project, Collection<IDefinition> defs, String baseName, Set<INamespaceDefinition> namespaceSet, boolean getContingents, INamespaceDefinition extraNamespace)
    {
        IDefinitionSet defSet = getLocalDefinitionSetByName(baseName);
        if (defSet != null)
        {
            int nDefs = defSet.getSize();
            for (int i = 0; i < nDefs; ++i)
            {
                IDefinition definition = defSet.getDefinition(i);
                accumulateMatchingDefinitions(referencingCU, project, defs, namespaceSet, getContingents, extraNamespace, definition);
            }
        }
    }

    private final void accumulateMatchingDefinitions(ICompilationUnit referencingCU, ICompilerProject project, Collection<IDefinition> defs, Set<INamespaceDefinition> namespaceSet, boolean getContingents, INamespaceDefinition extraNamespace, IDefinition definition)
    {
        if ((!definition.isContingent() || (getContingents && isContingentDefinitionNeeded(project, definition))))
        {
            if ((extraNamespace != null) && (extraNamespace == definition.getNamespaceReference()))
            {
                accumulateDefinitions(referencingCU, defs, definition);
            }
            else if (namespaceSet == null)
            {
                accumulateDefinitions(referencingCU, defs, definition);
            }
            else
            {
                INamespaceDefinition ns = definition.resolveNamespace(project);
                if ((extraNamespace != null) && ((ns == extraNamespace) || (extraNamespace.equals(ns))))
                    accumulateDefinitions(referencingCU, defs, definition);
                else if (namespaceSet.contains(ns))
                    accumulateDefinitions(referencingCU, defs, definition);
            }
        }
    }
    
    /**
     * Calling this method from an assert will cause an assertion failure if
     * there is any {@link IDefinition} in this {@link ASProjectScope} for which an
     * {@link ICompilationUnit} can <b><em>not</em></b> be found.
     * @return false if there is any {@link IDefinition} in this {@link ASProjectScope} for which an
     * {@link ICompilationUnit} can <b><em>not</em></b> be found, true otherwise.
     */
    @Override
    public boolean verify()
    {
        readLock.lock();
        try
        {
            for (IDefinitionSet defSet : this.definitionStore.getAllDefinitionSets())
            {
                int n = defSet.getSize();
                for (int i = 0; i < n; ++i)
                {
                    IDefinition def = defSet.getDefinition(i);
                    if (def.isImplicit())
                        continue;
                    if (getCompilationUnitForDefinition(def) == null)
                        return false;
                }
            }
        }
        finally
        {
            readLock.unlock();
        }
        return true;
    }

    /**
     * Represents a promise to provide an {@link IDefinition} in the future.
     */
    public static final class DefinitionPromise extends DefinitionBase
    {
        /**
         * Constructor.
         * 
         * @param qname The fully-qualified name of the promise,
         * such as <code>"flash.display.Sprite"</code>.
         * @param compilationUnit The {@link ICompilationUnit} contributing this promise.
         */
        private DefinitionPromise(String qname, ICompilationUnit compilationUnit)
        {
            // Unlike most definitions, which store an undotted name
            // in the 'storageName' field, a DefinitionPromise stores
            // its dotted qualified name (e.g. "flash.display.Sprite").
            super(qname);
            assert qname.charAt(qname.length() - 1) != '.' : "Qualified names must not end in '.'";
            
            compilationUnitWeakRef = new WeakReference<ICompilationUnit>(compilationUnit);
            
            actualDefinition = null;
        }

        /**
         * This is a WeakReference to the ICompilationUnit, as the
         * DependencyGraph should have the only long held hard reference to a
         * ICompilationUnit to ease memory profiling.
         */
        private WeakReference<ICompilationUnit> compilationUnitWeakRef;
        
        // TODO Consider eliminating this field.
        private IDefinition actualDefinition;

        public IDefinition getActualDefinition()
        {
            if (actualDefinition != null)
                return actualDefinition;

            final String qname = getQualifiedName();

            try
            {
                ICompilationUnit compilationUnit = compilationUnitWeakRef.get();
                assert (compilationUnit != null);

                final IFileScopeRequestResult fileScopeRequestResult = compilationUnit.getFileScopeRequest().get();
                actualDefinition = fileScopeRequestResult.getMainDefinition(qname);
            }
            catch (InterruptedException e)
            {
                actualDefinition = null;
            }

            return actualDefinition;
        }

        /**
         * Gets the {@link ICompilationUnit} that can satisfy this promise.
         * 
         * @return {@link ICompilationUnit} that can satisfy this promise.
         */
        public ICompilationUnit getCompilationUnit()
        {
            return compilationUnitWeakRef.get();
        }

        /**
         * Resets the DefinitionPromise to it's original state
         */
        public void clean()
        {
            actualDefinition = null;
        }

        @Override
        public String toString()
        {
            return "DefinitionPromise \"" + getQualifiedName() + "\"";
        }
        
        @Override
        protected String toStorageName(String name)
        {
            return name;
        }

        @Override
        public final String getPackageName()
        {
            // Unlike most definitions, which store an undotted base name in the
            // 'storageName' field, a DefinitionPromise stores its dotted qualified name.
            String storageName = getStorageName();
            int lastDotIndex = storageName.lastIndexOf('.');
            return lastDotIndex != -1 ? storageName.substring(0, lastDotIndex) : "";
        }

        @Override
        public final String getQualifiedName()
        {
            // Unlike most definitions, which store an undotted base name in the
            // 'storageName' field, a DefinitionPromise stores its dotted qualified name.
            return getStorageName();
        }

        @Override
        public final String getBaseName()
        {
            // Unlike most definitions, which store an undotted base name in the
            // 'storageName' field, a DefinitionPromise stores its dotted qualified name.
            String storageName = getStorageName();
            int lastDotIndex = storageName.lastIndexOf('.');
            return lastDotIndex != -1 ? storageName.substring(lastDotIndex + 1) : storageName;
        }

        @Override
        public boolean isInProject(ICompilerProject project)
        {
            // Always return false, because instances of this
            // class should never leak out of the name resolution APIs.
            return false;
        }
    }
}
