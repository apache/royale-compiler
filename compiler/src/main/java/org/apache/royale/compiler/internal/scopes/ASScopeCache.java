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

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.internal.definitions.AmbiguousDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinitionBase;
import org.apache.royale.compiler.internal.definitions.ConstantDefinition;
import org.apache.royale.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.units.ICompilationUnit;

import com.google.common.collect.MapMaker;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Class to manage cached lookups in a given project. Each scope object will
 * have one of these per project the scope object is used in. This class is
 * intended to provide thread safe access to the various caches it maintains.
 * The caches will be maintained via SoftReferences - this is meant to make the
 * caches stick around unless the VM really needs the memory back. The caches
 * can be rebuilt on the fly, so in a worst case scenario where the VM is
 * constantly low on memory, the results should be correct, but performance will
 * be slower.
 */
public class ASScopeCache
{
    /**
     * The concurrency level to use for the various ConcurrentMaps in use by
     * this cache. If the concurrency level is too high, you waste time and
     * space, if it is too low, then you have contention on updates of the map.
     * 4 seems to be a good tradeoff performance wise, and it is unlikely that
     * any individual scope cache is being updated from more than 4 threads
     * simultaneously. If our threading strategy changes (like if we make the CG
     * multithreaded within one file), we may want to revisit this setting.
     */
    private static final int CONCURRENCY_LEVEL = 4;
    private static final MapMaker mapMaker = new MapMaker()
                                            .concurrencyLevel(CONCURRENCY_LEVEL);

    public ASScopeCache(CompilerProject project, ASScope scope)
    {
        this.scope = scope;
        this.project = project;
    }

    private final ASScope scope;
    private final CompilerProject project;

    /**
     * Cache results of unqualified lookups over the scope chain
     * (ASScopeBase.findProperty). This is for caching the results of
     * ASScope.findProperty().
     */
    private SoftReference<ConcurrentMap<String, IDefinition>> findPropCache;

    /**
     * Cache results of lookups of qualified names over the scope chain
     * (ASScopeBase.findPropertyQualified).
     */
    private SoftReference<ConcurrentMap<QName, IDefinition>> findPropQualifiedCache;

    /**
     * Cache the set of open namespaces
     */
    private SoftReference<Set<INamespaceDefinition>> openNamespaceCache = null;

    /**
     * Cache the open namespace set per name
     */
    private SoftReference<ConcurrentMap<String, Set<INamespaceDefinition>>> namespacesForNameCache;

    private SoftReference<ConcurrentMap<IResolvedQualifiersReference, IDefinition>> multinameLookupCache;

    /**
     * Cache the compile time values of constants
     */
    private SoftReference<ConcurrentMap<IDefinition, Object>> constValueLookupCache;

    /**
     * Cache the needs Event dispatch flag
     */
    private Boolean needsEventDispatcherCache;

    /**
     * Cache the extended or implemented interfaces of an interface or class.
     */
    private SoftReference<IInterfaceDefinition[]> interfacesCache;

    /**
     * Cache the builtin types we've already added dependencies on
     */
    private SoftReference<Set<IASLanguageConstants.BuiltinType>> builtinTypeDependencyCache;

    /**
     * Version of findProperty that uses a cache. Checks the cache first, and
     * only queries the scope if the we don't have a cached result.
     * 
     * @param scope The scope to perform the lookup in
     * @param name Name of the property to find
     * @param cache ASDefinitionCache to use for the lookup - this is only used
     * to get at the ICompilerProject
     * @param dt Which type of dependency to introduce when we do the lookup
     * @return The IDefinition for the property, or null if it wasn't found
     */
    IDefinition findProperty(String name, DependencyType dt, boolean favorTypes)
    {
        ConcurrentMap<String, IDefinition> map = getScopeChainMap();

        IDefinition result = map.get(name);
        if (result != null)
        {
            // We found a cached result - we're done
        	// after making sure it has a dependency
        	if (result instanceof ITypeDefinition)
        	{
	        	ICompilationUnit from = scope.getFileScope().getCompilationUnit();
	            assert result.isInProject(project);
	            
	            String qname = result.getQualifiedName();
	            ICompilationUnit to = ((ASProjectScope)project.getScope()).getCompilationUnitForDefinition(result);
	            if (to == null && !(qname.contentEquals("void") || qname.contentEquals("*")))
	            	System.out.println("No compilation unit for " + qname);	
	            if (to != null)
	            	project.addDependency(from, to, dt, qname);
        	}
            return result;
        }

        // It is possible for 2+ threads to get in here for the same name.
        // This is intentional - the worst that happens is that we duplicate the resolution work
        // the benefit is that we avoid any sort of locking, which was proving expensive (time wise,
        // and memory wise).

        boolean wasAmbiguous = false;
        IDefinition def = null;
        Set<INamespaceDefinition> namespaceSet = scope.getNamespaceSetForName(project, name);
        // Look for the definition in the scope
        List<IDefinition> defs = scope.findProperty(project, name, namespaceSet, dt);
        switch (defs.size())
        {
            case 0:
                // No definition found!
                def = null;
                break;
            case 1:
                // found single definition!
                def = defs.get(0);
                assert def.isInProject(project);
                break;
            default:
            	wasAmbiguous = true;
                IDefinition d = AmbiguousDefinition.resolveAmbiguities(project, defs, favorTypes);
                if (d != null)
                    def = d;
                else {
                    if (defs.size() == 2) 
                    {
                        def = project.doubleCheckAmbiguousDefinition(scope, name, defs.get(0), defs.get(1));
                        if (def != null)
                            return def;
                    }
                    def = AmbiguousDefinition.get();
                }
        }
        if (def != null)
        {
            assert def.isInProject(project);
            assert result == null;
            // If we have a non-null dependency type
            // then we can cache the result of the name resolution.
            // If the dependency type is null we can't cache the name
            // resolution result, because the name resolution cache will not
            // be properly invalidated when the file containing the definition changes.
            if (dt != null && !wasAmbiguous)
            {
                result = map.putIfAbsent(name, def);
                if (result == null)
                    result = def;
            }
            else
            {
                result = def;
            }
        }
        return result;

    }

    private ConcurrentMap<String, IDefinition> getScopeChainMap()
    {
        ConcurrentMap<String, IDefinition> map = findPropCache != null ? findPropCache.get() : null;
        if (map == null)
        {
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache waiting for lock in getScopeChainMap");
            synchronized (this)
            {
                // Check again, in case another thread updated the map first
                map = findPropCache != null ? findPropCache.get() : null;
                if (map == null)
                {
                    map = mapMaker.<String, IDefinition> makeMap();
                    findPropCache = new SoftReference<ConcurrentMap<String, IDefinition>>(map);
                }
            }
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache done with lock in getScopeChainMap");
        }
        return map;
    }

    private ConcurrentMap<QName, IDefinition> getQualifiedScopeChainMap()
    {
        ConcurrentMap<QName, IDefinition> map = findPropQualifiedCache != null ? findPropQualifiedCache.get() : null;
        if (map == null)
        {
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache waiting for lock in getQualifiedScopeChainMap");
            synchronized (this)
            {
                // Check again, in case another thread updated the map first
                map = findPropQualifiedCache != null ? findPropQualifiedCache.get() : null;
                if (map == null)
                {
                    map = mapMaker.<QName, IDefinition> makeMap();
                    findPropQualifiedCache = new SoftReference<ConcurrentMap<QName, IDefinition>>(map);
                }
            }
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache done with lock in getQualifiedScopeChainMap");
        }
        return map;
    }

    /**
     * Version of findPropertyQualified that uses a cache. Checks the cache
     * first, and only queries the scope if the we don't have a cached result.
     * 
     * @param scope The scope to perform the lookup in
     * @param name Name of the property to find
     * @param cache ASDefinitionCache to use for the lookup - this is only used
     * to get at the ICompilerProject
     * @param dt Which type of dependency to introduce when we do the lookup
     * @return The IDefinition for the property, or null if it wasn't found
     */
    IDefinition findPropertyQualified(INamespaceDefinition qualifier, String name,
                                      DependencyType dt)
    {
        QName qname = new QName(name, qualifier);
        ConcurrentMap<QName, IDefinition> map = getQualifiedScopeChainMap();

        IDefinition result = map.get(qname);
        if (result != null)
        {
            assert result.isInProject(project);
            // We found a cached result - we're done
            return result;
        }

        // If we get this far, then we did not find a cached entry
        // It is possible for 2+ threads to get in here for the same name.
        // This is intentional - the worst that happens is that we duplicate the resolution work
        // the benefit is that we avoid any sort of locking, which was proving expensive (time wise,
        // and memory wise).

        IDefinition def;
        // Look for the definition in the scope
        Set<INamespaceDefinition> namespaceSet = Collections.singleton(qualifier);
        List<IDefinition> defs = scope.findProperty(project, name, namespaceSet, dt);
        switch (defs.size())
        {
            case 0:
                def = null;
                break;

            case 1:
                def = defs.get(0);
                assert def.isInProject(project);
                break;

            default:
                IDefinition d = AmbiguousDefinition.resolveAmbiguities(project, defs, false);
                if (d != null)
                    def = d;
                else
                    def = AmbiguousDefinition.get();
                break;
        }
        if (def != null)
        {
            assert def.isInProject(project);
            assert result == null;
            // If we have a non-null dependency type
            // then we can cache the result of the name resolution.
            // If the dependency type is null we can't cache the name
            // resolution result, because the name resolution cache will not
            // be properly invalidated when the file containing the definition changes.
            if (dt != null)
            {
                result = map.putIfAbsent(qname, def);
                if (result == null)
                    result = def;
            }
            else
            {
                result = def;
            }
        }
        return result;
    }

    /**
     * Resolves the specified reference to a definition and adds a dependency to
     * the dependency graph if needed.
     * <p>
     * This method is only public so that the implementation of
     * IResolveQualifiersReference.resolve can call it. This method should only
     * be called from the implementation of {@link IResolvedQualifiersReference}.
     * 
     * @param ref The reference to resolve.
     * @param dt The type of dependency to add if a new edge needs to be added
     * to the dependency graph.
     * @return The definition the reference resolves to, null, or the ambiguous
     * definition.
     */
    public IDefinition findPropertyMultiname(IResolvedQualifiersReference ref, DependencyType dt)
    {
        ConcurrentMap<IResolvedQualifiersReference, IDefinition> cache = getMultinameLookupMap();
        IDefinition result = cache.get(ref);
        if (result != null)
            return result;

        IDefinition def;

        // Look for the definition in the scope
        List<IDefinition> defs = scope.findProperty(project, ref.getName(), ref.getQualifiers(), dt);
        switch (defs.size())
        {
            case 0:
                // No definition found!
                def = null;
                break;
            case 1:
                // found single definition!
                def = defs.get(0);
                assert def.isInProject(project);
                break;
            default:
                IDefinition d = AmbiguousDefinition.resolveAmbiguities(project, defs, false);
                if (d != null)
                    def = d;
                else
                    def = AmbiguousDefinition.get();
        }
        if (def != null)
        {
            assert def.isInProject(project);
            assert result == null;
            // If we have a non-null dependency type
            // then we can cache the result of the name resolution.
            // If the dependency type is null we can't cache the name
            // resolution result, because the name resolution cache will not
            // be properly invalidated when the file containing the definition changes.
            if (dt != null)
            {
                result = cache.putIfAbsent(ref, def);
                if (result == null)
                    result = def;
            }
            else
            {
                result = def;
            }
        }
        return result;
    }

    /**
     * Version of getNamespaceSet that caches the results.
     * 
     * @return the namespace set to use for unqualified lookups in this scope
     */
    Set<INamespaceDefinition> getNamespaceSet()
    {
        Set<INamespaceDefinition> nsSet = openNamespaceCache != null ? openNamespaceCache.get() : null;
        if (nsSet != null)
            return nsSet;

        nsSet = scope.getNamespaceSetImpl(project);
        openNamespaceCache = new SoftReference<Set<INamespaceDefinition>>(nsSet);
        return nsSet;
    }

    private ConcurrentMap<String, Set<INamespaceDefinition>> getNamespacesForNameMap()
    {
        ConcurrentMap<String, Set<INamespaceDefinition>> map = namespacesForNameCache != null ? namespacesForNameCache.get() : null;
        if (map == null)
        {
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache waiting for lock in getNamespacesForNameMap");
            synchronized (this)
            {
                // Check again, in case another thread updated the map first
                map = namespacesForNameCache != null ? namespacesForNameCache.get() : null;
                if (map == null)
                {
                    map = mapMaker.<String, Set<INamespaceDefinition>> makeMap();
                    namespacesForNameCache = new SoftReference<ConcurrentMap<String, Set<INamespaceDefinition>>>(map);
                }
            }
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache done with lock in getNamespacesForNameMap");
        }
        return map;
    }

    private ConcurrentMap<IResolvedQualifiersReference, IDefinition> getMultinameLookupMap()
    {
        ConcurrentMap<IResolvedQualifiersReference, IDefinition> map = multinameLookupCache != null ? multinameLookupCache.get() : null;
        if (map == null)
        {
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache waiting for lock in getMultinameLookupMap");
            synchronized (this)
            {
                // Check again, in case another thread updated the map first
                map = multinameLookupCache != null ? multinameLookupCache.get() : null;
                if (map == null)
                {
                    map = mapMaker.<IResolvedQualifiersReference, IDefinition> makeMap();
                    multinameLookupCache = new SoftReference<ConcurrentMap<IResolvedQualifiersReference, IDefinition>>(map);
                }
            }
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache done with lock in getMultinameLookupMap");
        }
        return map;
    }

    private ConcurrentMap<IDefinition, Object> getConstantValueLookupMap()
    {
        ConcurrentMap<IDefinition, Object> map = constValueLookupCache != null ? constValueLookupCache.get() : null;
        if (map == null)
        {
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache waiting for lock in getConstantValueLookupMap");
            synchronized (this)
            {
                // Check again, in case another thread updated the map first
                map = constValueLookupCache != null ? constValueLookupCache.get() : null;
                if (map == null)
                {
                    map = mapMaker.<IDefinition, Object> makeMap();
                    constValueLookupCache = new SoftReference<ConcurrentMap<IDefinition, Object>>(map);
                }
            }
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache done with lock in getConstantValueLookupMap");
        }
        return map;
    }

    private Set<IASLanguageConstants.BuiltinType> getBuiltinTypeMap()
    {
        Set<IASLanguageConstants.BuiltinType> set = builtinTypeDependencyCache != null ? builtinTypeDependencyCache.get() : null;
        if (set == null)
        {
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache waiting for lock in getBuiltinTypeMap");
            synchronized (this)
            {
                // Check again, in case another thread updated the set first
                set = builtinTypeDependencyCache != null ? builtinTypeDependencyCache.get() : null;
                if (set == null)
                {
                    set = Collections.newSetFromMap(mapMaker.<IASLanguageConstants.BuiltinType, Boolean> makeMap());
                    builtinTypeDependencyCache = new SoftReference<Set<IASLanguageConstants.BuiltinType>>(set);
                }
            }
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache done with lock in getBuiltinTypeMap");
        }
        return set;
    }

    /**
     * Determines if the {@link TypeScope} this cache is associated with needs
     * an implicit 'implements flash.events.IEventDispatcher' due to the class,
     * or some of its members being marked bindable. The result of this method
     * is cached.
     * <p>
     * Only
     * {@link ClassDefinitionBase#needsEventDispatcher(ICompilerProject)}
     * should call this method. All other code should call
     * {@link ClassDefinitionBase#needsEventDispatcher(ICompilerProject)}.
     * 
     * @return true if this class needs to add IEventDispatcher to its interface
     * list, and should implement the IEventDispatcher methods.
     */
    public boolean needsEventDispatcher()
    {
        assert scope instanceof TypeScope : "needsEventDispatcher should only be called on scope cache's for TypeScopes!";
        assert scope.getDefinition() instanceof ClassDefinitionBase : "needsEventDispatcher should only be called on scope cache's for the scopes contained by classes!";
        Boolean valueObject = needsEventDispatcherCache;
        if (valueObject != null)
            return valueObject.booleanValue();
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
    		System.out.println("ASScopeCache waiting for lock in needsEventDispatcher");
        synchronized (this)
        {
            // Check again, in case another thread updated the value first
            valueObject = needsEventDispatcherCache;
            if (valueObject != null)
            {
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
            		System.out.println("ASScopeCache done with lock in needsEventDispatcher");
                return valueObject.booleanValue();
            }
            boolean computedValue = ((ClassDefinitionBase)scope.getDefinition()).computeNeedsEventDispatcher(project);
            needsEventDispatcherCache = computedValue;
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache done with lock in needsEventDispatcher");
            return computedValue;
        }
    }

    public IInterfaceDefinition[] resolveInterfaces()
    {
        assert scope instanceof TypeScope : "resolveInterfacesImpl should only be called on scope cache's for TypeScopes!";
        assert scope.getDefinition() instanceof TypeDefinitionBase : "resolveInterfacesImpl should only be called on scope cache's for the scopes contained by types!";

        IInterfaceDefinition[] interfs = interfacesCache != null ? interfacesCache.get() : null;

        if( interfs != null )
            return interfs;

    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
    		System.out.println("ASScopeCache waiting for lock in resolveInterfaces");
        synchronized (this)
        {
            // check again in case another thread updated the value first
            interfs = interfacesCache != null ? interfacesCache.get() : null;
            if( interfs != null )
            {
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
            		System.out.println("ASScopeCache done with lock in resolveInterfaces");
                return interfs;
            }

            interfs = ((TypeDefinitionBase)scope.getDefinition()).resolveInterfacesImpl(project);
            interfacesCache = new SoftReference<IInterfaceDefinition[]>(interfs);
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.ASSCOPECACHE) == CompilerDiagnosticsConstants.ASSCOPECACHE)
        		System.out.println("ASScopeCache done with lock in resolveInterfaces");
            return interfs;
        }
    }
    /**
     * Version of getNamespaceSetForName that caches the results - this is used
     * to get the namespace set to use to lookup a name - If name is an
     * explicitly imported definition, then the namespace set will consist of
     * the package name from the import(s) plus the open namespace set. If name
     * was not explitly imported then the open namespace set will be returned
     * 
     * @param name the name to lookup
     * @return the namespace set to use to lookup name
     */
    Set<INamespaceDefinition> getNamespaceSetForName(String name)
    {
        ConcurrentMap<String, Set<INamespaceDefinition>> map = getNamespacesForNameMap();

        Set<INamespaceDefinition> result = map.get(name);
        if (result != null)
        {
            // We found a cached result - we're done
            return result;
        }

        // It is possible for 2+ threads to get in here for the same name.
        // This is intentional - the worst that happens is that we duplicate the resolution work
        // the benefit is that we avoid any sort of locking, which was proving expensive (time wise,
        // and memory wise).

        Set<INamespaceDefinition> newResult = scope.getNamespaceSetForNameImpl(project, name);
        result = map.putIfAbsent(name, newResult);
        if (result == null)
            result = newResult;
        return result;

    }

    /**
     * Version of addDependencyOnBuiltinType that uses a cache to determine if the dependency actually
     * needs to be added.  The act of adding a dependency is somewhat expensive, so using a cache
     * is much faster
     *
     * @param builtinType   the type to depend on
     * @param dt            the type of Dependency to add
     */
    void addDependencyOnBuiltinType(IASLanguageConstants.BuiltinType builtinType, DependencyType dt)
    {
        Set<IASLanguageConstants.BuiltinType> set = getBuiltinTypeMap();

        if( set.contains(builtinType) )
        {
            // We found a cached result - we're done
            return;
        }

        // It is possible for 2+ threads to get in here for the same name.
        // This is intentional - the worst that happens is that we duplicate the dependency work
        // the benefit is that we avoid any sort of locking, which was proving expensive (time wise,
        // and memory wise).
        scope.addDependencyOnBuiltinTypeImpl(project, builtinType, dt);
        set.add(builtinType);

        return;
    }

    /**
     * Used to cache no constant value results for the const value cache.
     * Computing the value is expensive whether there is a value or not, and
     * many constants will not have compile time constant values, so we want to
     * cache those as well.
     */
    private static final Object NO_CONST_VALUE = new Object();

    /**
     * get the constant value for the given const definition. If a compile time
     * constant can not be computed for the definition, this will return null.
     * 
     * @param constDef The constant definition you want the constant value of
     * @return The constant value, or null if a compie time constant could not
     * be computed
     */
    public Object getConstantValue(ConstantDefinition constDef)
    {
        ConcurrentMap<IDefinition, Object> map = getConstantValueLookupMap();

        Object result = map.get(constDef);
        if (result != null)
        {
            // We found a cached result - we're done
            if (result == NO_CONST_VALUE)
                return null;
            return result;
        }

        // It is possible for 2+ threads to get in here for the same name.
        // This is intentional - the worst that happens is that we duplicate the resolution work
        // the benefit is that we avoid any sort of locking, which was proving expensive (time wise,
        // and memory wise).

        Object newResult = constDef.resolveValueImpl(project);
        if (newResult == null)
            newResult = NO_CONST_VALUE;
        result = map.putIfAbsent(constDef, newResult);
        if (result == null)
            result = newResult;

        if (result == NO_CONST_VALUE)
            return null;
        else
            return result;
    }

    /**
     * Helper class - to be used as a key for caching qualified name lookups.
     */
    private static class QName
    {
        String name;
        INamespaceDefinition ns;

        QName(String name, INamespaceDefinition ns)
        {
            assert ns != null;
            this.name = name;
            this.ns = ns;
        }

        @Override
        public int hashCode()
        {
            return name.hashCode() ^ ns.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            if (o == this)
                return true;
            if (o instanceof QName)
            {
                QName other = (QName)o;
                return name.equals(other.name) && ns.equals(other.ns);
            }
            return false;
        }
    }
}
