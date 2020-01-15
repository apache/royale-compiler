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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.ABCEmitter;
import org.apache.royale.abc.ABCLinker;
import org.apache.royale.abc.ABCParser;
import org.apache.royale.abc.EntryOrderedStore;
import org.apache.royale.abc.ABCEmitter.EmitterClassVisitor;
import org.apache.royale.abc.Pool;
import org.apache.royale.abc.semantics.Metadata;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Trait;
import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.exceptions.BuildCanceledException;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.units.SWCCompilationUnit;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.compiler.targets.ISWFTarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetReport;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit.UnitType;
import org.apache.royale.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCLibrary;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.SWF;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.ISWFConstants;
import org.apache.royale.swf.tags.DoABCTag;
import org.apache.royale.swf.tags.IManagedTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.swf.tags.ScriptLimitsTag;
import org.apache.royale.swf.tags.SymbolClassTag;
import org.apache.royale.swf.types.RGB;
import org.apache.royale.swf.types.Rect;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Concrete implementation of ITarget for building a collection of source files
 * into a SWF.
 */
public abstract class SWFTarget extends Target implements ISWFTarget
{
    protected static final class SWFFrameInfo
    {
        public static final boolean EXTERNS_ALLOWED = true;
        public static final boolean EXTERNS_DISALLOWED = false;
        
        public SWFFrameInfo(String frameLabel, boolean allowExternals, Set<ICompilationUnit> rootedUnits, Iterable<ICompilerProblem> problems)
        {
            // character replaced came from the old compiler, so copy to match behavior
            this.frameLabel = frameLabel != null ? frameLabel.replaceAll( "[^A-Za-z0-9]", "_" ) : null;
            this.allowExternals = allowExternals;
            this.rootedUnits = rootedUnits;
            this.problems = problems;
        }
        
        public SWFFrameInfo(Set<ICompilationUnit> rootedUnits, Iterable<ICompilerProblem> problems)
        {
            this(null, EXTERNS_ALLOWED, rootedUnits, problems);
        }

        public final String frameLabel;
        public final boolean allowExternals;
        public final Set<ICompilationUnit> rootedUnits;
        public final Iterable<ICompilerProblem> problems;
    }

    public SWFTarget(CompilerProject project, ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor)
    {
        super(project, targetSettings, progressMonitor);
    }

    private Collection<ICompilerProblem> problemCollection;
    private Target.RootedCompilationUnits rootedCompilationUnits;
    protected Set<String> metadataDonators = new HashSet<String>();
    protected boolean isLibrary = false;
    	
    /** 
     * Cached list of compilation units. This is a performance optimization to keep us from
     * making redundant calls to topologicalSort.
     * 
     * Note that this optimization only gives a small boost in observed cases.
     * 
     * if non-null, this is the results of calling project.getReachableCompilationUnitsInSWFOrder(rootedCU);
     * We will assume it doesn't change
     */
    
    private List<ICompilationUnit> rootedCompilationUnitsAndDependenciesInSWFOrder;
    
    /**
     * Cached information about all the frames in the SWF.
     */
    private FramesInformation framesInformation;
    
    /**
     * Cached {@link RGB} value for the SWF's background color.
     */
    private RGB swfBackgroundColor;
    
    
    /**
     * Cached {@link ITargetAttributes} for the SWF
     */
    private ITargetAttributes _targetAttributes;
   
    /**
     * Gets the set of {@link ICompilationUnit}s that are the roots of the graph of
     * {@link ICompilationUnit}s whose output will be in the generate SWF.
     * @return The set of {@link ICompilationUnit}s that are the roots of the graph of
     * {@link ICompilationUnit}s whose output will be in the generate SWF
     */
    @Override
    public RootedCompilationUnits getRootedCompilationUnits() throws InterruptedException
    {
        if (rootedCompilationUnits == null)
            rootedCompilationUnits = computeRootedCompilationUnits();
        return rootedCompilationUnits;
    }
    
    /**
     * Same as project.getReachableCompilationUnitsInSWFOrder(), but with a cache for the case of rooted compilation units
     * @throws InterruptedException 
     */
    List<ICompilationUnit> getReachableCompilationUnitsInSWFOrder( Collection<ICompilationUnit> roots ) throws InterruptedException
    {
        List<ICompilationUnit> ret = null;
        
        final RootedCompilationUnits rootedCompilationUnits = getRootedCompilationUnits();
        
        // Determine if the "roots" in this call are the rooted compilation units for this target.
        // We only cached the results for this case
        boolean passedInRootedCompilationUnits = false;
        if (roots.size() == rootedCompilationUnits.getUnits().size())
        {
            passedInRootedCompilationUnits = true;
            for (ICompilationUnit cu : roots)
            {
                if (!rootedCompilationUnits.getUnits().contains(cu))
                    passedInRootedCompilationUnits = false;
            }
        }
        
        // If this is the case that is cached, then get/make the cache
        if (passedInRootedCompilationUnits)
        {
            if (rootedCompilationUnitsAndDependenciesInSWFOrder == null)
            {
                rootedCompilationUnitsAndDependenciesInSWFOrder =  project.getReachableCompilationUnitsInSWFOrder(roots);           
            }
            ret = rootedCompilationUnitsAndDependenciesInSWFOrder;
        }
        else
        {
            // If not the cached case, just call the function directly
            ret = project.getReachableCompilationUnitsInSWFOrder(roots);
        }
        assert ret != null;
        
        return ret;
    }
     
    /**
     * Absolute path of the path that contains the reference to Object.
     * We use this to determine which SWC contains native code so that
     * we always exclude all of the definitions from that SWC.
     */
    private String pathContainingObject;

    @Override
    public ISWF build(Collection<ICompilerProblem> problems)
    {
        buildStarted();
        try
        {
            Iterable<ICompilerProblem> fatalProblems = getFatalProblems();
            if (!Iterables.isEmpty(fatalProblems))
            {
                Iterables.addAll(problems, fatalProblems);
                return null;
            }

            Set<ICompilationUnit> compilationUnitSet = new HashSet<ICompilationUnit>();
            Target.RootedCompilationUnits rootedCompilationUnits = getRootedCompilationUnits();
            
            // no rooted compilation could be found, but still create an empty SWF
            // in this error case
            if (rootedCompilationUnits.getUnits().isEmpty())
                return buildEmptySWF();

            compilationUnitSet.addAll(rootedCompilationUnits.getUnits());

            this.problemCollection = problems;

            FramesInformation frames = getFramesInformation();
            
            BuiltCompilationUnitSet builtCompilationUnits =
                getBuiltCompilationUnitSet();
            Iterables.addAll(problems, builtCompilationUnits.problems);
            
            doPostBuildWork(builtCompilationUnits.compilationUnits, problems);
            
            ISWF swf = initializeSWF(getReachableCompilationUnitsInSWFOrder(rootedCompilationUnits.getUnits()));
            
            // now that everything is built, the dependency graph is populated enough to do a topological sort on
            // all compilation units needed by this target.
            // The compilation units that define bases classes must occurs in the swf before
            // compilation units that define classes that subclass those classes ( see
            // inheritance dependencies in the {@link DependencyGraph} ).
            Set<ICompilationUnit> emittedCompilationUnits = new HashSet<ICompilationUnit>();
            
            frames.createFrames(this, swf, builtCompilationUnits.compilationUnits, emittedCompilationUnits, problems);
            
            createLinkReport(problems);
            
            // "Link" the resulting swf, if the optimize flag is set
            return linkSWF(swf);
        }
        catch (BuildCanceledException bce)
        {
            return null;
        }
        catch (InterruptedException ie)
        {
            return null;
        }
        finally
        {
            buildFinished();
        }
    }

    @Override
    public TargetType getTargetType()
    {
        return TargetType.SWF;
    }
   
    protected final ITargetAttributes getTargetAttributes() throws InterruptedException
    {
        // if already computed, just return the cached value
        if (_targetAttributes == null)
        {
            // first time - delegate to subclass to compute
            _targetAttributes = computeTargetAttributes();
        }
        return _targetAttributes;
    }
    
    /**
     * round-up  user specified target attributes
     * All derived classes must provide one, but they are free to provide "do nothing" implementations
     * (like NilTargetAttributes)
     */
    protected abstract ITargetAttributes computeTargetAttributes() throws InterruptedException;
    
    /**
     * Create the {@link FramesInformation} which contains the skeleton for the frames
     * of this SWF. The actual frames will be create in doCreateFrames().
     * @throws InterruptedException 
     */
    protected abstract FramesInformation computeFramesInformation() throws InterruptedException;
    
    protected final FramesInformation getFramesInformation() throws InterruptedException
    {
        if (framesInformation != null)
            return framesInformation;
        framesInformation = computeFramesInformation();
        return framesInformation;
    }
    
    /**
     * Check the build and analyze the results before the SWF target is
     * initialized.
     * 
     * @param compilationUnits The set of compilation units after
     * buildAndCollectProblems() has run.
     * @param problems A collection where discovered problems are appended.
     */
    protected void doPostBuildWork(ImmutableSet<ICompilationUnit> compilationUnits,
            Collection<ICompilerProblem> problems) throws InterruptedException
    {
        
    }

    /**
     * Creates a new instance of a SWF.
     * 
     * @return a new instance of a SWF.
     */
    protected SWF buildEmptySWF()
    {
        return new SWF();
    }

    /**
     * Add to the collection of compiler problems.
     * 
     * @param problem
     */
    protected void reportProblem(ICompilerProblem problem)
    {
        assert problemCollection != null;
        
        problemCollection.add(problem);
    }

    /**
     * Add a set of root classes and its dependencies to a new frame or to an existing
     * frame.
     * @param frame if null a new frame will be created for the classes. Otherwise the 
     * classes will be added to the existing frame.
     * @param frameRootClasses
     * @param projectScope
     * @param allowExternals
     * @param emittedCompilationUnits
     * @return The SWF frame.
     * @throws InterruptedException
     */
    protected SWFFrame createWithClassesAndItsDependencies(SWFFrame frame, 
            Collection<ClassDefinition> frameRootClasses, ASProjectScope projectScope,
            boolean allowExternals,
            Set<ICompilationUnit> emittedCompilationUnits) throws InterruptedException
    {
        final List<ICompilationUnit> rootedUnitsForFrame = new LinkedList<ICompilationUnit>();
        for (ClassDefinition frameRootClass : frameRootClasses)
        {
            final Collection<IDefinition> extraDefinitions = frameRootClass.resolveExtraClasses(project);
            final ICompilationUnit frameFactoryClassCompilationUnit = projectScope.getCompilationUnitForDefinition(frameRootClass);
            assert frameFactoryClassCompilationUnit != null;
            rootedUnitsForFrame.add(frameFactoryClassCompilationUnit);
            for (IDefinition extraDef : extraDefinitions)
            {
                if (!extraDef.isImplicit())
                {
                    ICompilationUnit extraDefCompilationUnit = projectScope.getCompilationUnitForDefinition(extraDef);
                    assert extraDefCompilationUnit != null;
                    rootedUnitsForFrame.add(extraDefCompilationUnit);
                }
            }
        }

        if (frame == null)
            frame = new SWFFrame();

        if (!addCompilationUnitsAndDependenciesToFrame(frame, rootedUnitsForFrame, allowExternals, emittedCompilationUnits))
            return null;

        return frame;
    }

    protected boolean addCompilationUnitsAndDependenciesToFrame(SWFFrame frame,
            Collection<ICompilationUnit> rootedUnitsForFrame, 
            boolean allowExternals,
            Set<ICompilationUnit> emittedCompilationUnits) throws InterruptedException
    {
        List<ICompilationUnit> unitsForFrame = this.getReachableCompilationUnitsInSWFOrder(rootedUnitsForFrame);
        for (ICompilationUnit cu : unitsForFrame)
        {
            if (emittedCompilationUnits.add(cu))
            {
                boolean includeCu = testCompilationUnitLinkage(cu, allowExternals);
                doAddMetadataNamesToTarget(cu, includeCu);
                
                if (includeCu)
                {
                	ISWFTagsRequestResult swfTags = cu.getSWFTagsRequest().get();
                	if (targetSettings.allowSubclassOverrides() && !isLibrary)
                	{
                		// scan the ABC in each CU for overrides that need fixing.
                		// the override needs to be put back to the base override
                		// otherwise you will get a verify error at runtime
                		boolean changedABC = false;
                        final DoABCTag doABC = swfTags.getDoABCTag();
                        ABCParser parser = new ABCParser(doABC.getABCData());
                        ABCEmitter emitter = new ABCEmitter();
                        try {
                        	parser.parseABC(emitter);
	                        Collection<EmitterClassVisitor> classes = emitter.getDefinedClasses();
	                        for (EmitterClassVisitor clazz : classes)
	                        {
	                        	Iterator<Trait> instanceTraits = clazz.instanceTraits.iterator();
	                        	while (instanceTraits.hasNext())
	                        	{
	                        		Trait trait = instanceTraits.next();
	                        		Vector<Metadata> metas = trait.getMetadata();
	                        		metas:
	                        		for (Metadata meta : metas)
	                        		{
	                        			if (meta.getName().equals(IMetaAttributeConstants.ATTRIBUTE_SWFOVERRIDE))
	                        			{
	                                        EntryOrderedStore<MethodInfo> methods = emitter.getMethodInfos();
	                                        for (MethodInfo method : methods)
	                                        {
	                                        	String methodName = method.getMethodName();
	                                        	if (methodName == null) continue;
	                                        	// match getter with getter methodInfo
	                                        	if (trait.isGetter() && method.getReturnType().getBaseName().equals(IASLanguageConstants.void_)) continue;
	                                        	if (trait.isSetter() && (!method.getReturnType().getBaseName().equals(IASLanguageConstants.void_))) continue;
	                                        	if (methodName.equals(trait.getName().getBaseName()))
	                                        	{
	                                        		String[] keys = meta.getKeys();
	                                        		int n = keys.length;
	                                        		for (int i = 0; i < n; i++)
	                                        		{
	                                        			if (keys[i].equals(IMetaAttributeConstants.NAME_SWFOVERRIDE_RETURNS))
	                                        			{
	                                        				String returnString = meta.getValues()[i];
	                                        				int c = returnString.lastIndexOf(".");
	                                        				String packageName = "";
	                                        				String baseName = returnString;
	                                        				if (c != -1)
	                                        				{
	                                        					packageName = returnString.substring(0, c);
	                                        					baseName = returnString.substring(c + 1);
	                                        				}
	                                        				
	                                        				Pool<Name> namePool = emitter.getNamePool();
	                                        				List<Name> nameList = namePool.getValues();
	                                        				boolean foundName = false;
	                                        				for (Name name : nameList)
	                                        				{
	                                        					String base = name.getBaseName();
	                                        					if (base == null) continue;
	                                        					if (name.getQualifiers().length() != 1) continue;
	                                        					Namespace ns = name.getSingleQualifier();
	                                        					if (ns == null) continue;
	                                        					String nsName = ns.getName();
	                                        					if (nsName == null) continue;
	                                        					if (base.equals(baseName) &&
	                                        							nsName.equals(packageName))
	                                        					{
	                                                				method.setReturnType(name);
	                                                				foundName = true;
	                                                				changedABC = true;
	                                        					}
	                                        				}
	                                        				if (!foundName)
	                                        				{
	                                            				Pool<String> stringPool = emitter.getStringPool();
	                                            				stringPool.add(packageName);// theoretically, it won't be added if already there
	                                            				stringPool.add(baseName);	// theoretically, it won't be added if already there
	                                        					Namespace ns = new Namespace(ABCConstants.CONSTANT_PackageNs, packageName);
	                                        					Pool<Namespace> nsPool = emitter.getNamespacePool();
	                                        					nsPool.add(ns);
	                                        					Name name = new Name(ns, baseName);
	                                        					namePool.add(name);
	                                        					method.setReturnType(name);
	                                        					changedABC = true;
	                                        				}
	                                        			}
	                                        			else if (keys[i].equals(IMetaAttributeConstants.NAME_SWFOVERRIDE_PARAMS))
	                                        			{
	                                        				String paramList = meta.getValues()[i];
	                                    					String[] parts;
	                                    					if (paramList.contains(","))
	                                    						parts = paramList.split(",");
	                                    					else
	                                    					{
	                                    						parts = new String[1];
	                                    						parts[0] = paramList;
	                                    					}
	                                    					Vector<Name> newList = new Vector<Name>();
	                                    					for (String part : parts)
	                                    					{
		                                        				int c = part.lastIndexOf(".");
		                                        				String packageName = "";
		                                        				String baseName = part;
		                                        				if (c != -1)
		                                        				{
		                                        					packageName = part.substring(0, c);
		                                        					baseName = part.substring(c + 1);
		                                        				}
		                                        				
		                                        				Pool<Name> namePool = emitter.getNamePool();
		                                        				List<Name> nameList = namePool.getValues();
		                                        				boolean foundName = false;
		                                        				for (Name name : nameList)
		                                        				{
		                                        					String base = name.getBaseName();
		                                        					if (base == null) continue;
		                                        					if (name.getQualifiers().length() != 1) continue;
		                                        					Namespace ns = name.getSingleQualifier();
		                                        					if (ns == null) continue;
		                                        					String nsName = ns.getName();
		                                        					if (nsName == null) continue;
		                                        					if (base.equals(baseName) &&
		                                        							nsName.equals(packageName))
		                                        					{
		                                        						newList.add(name);
		                                                				foundName = true;
		                                                				changedABC = true;
		                                                				break;
		                                        					}
		                                        				}
		                                        				if (!foundName)
		                                        				{
		                                            				Pool<String> stringPool = emitter.getStringPool();
		                                            				stringPool.add(packageName);// theoretically, it won't be added if already there
		                                            				stringPool.add(baseName);	// theoretically, it won't be added if already there
		                                        					Namespace ns = new Namespace(ABCConstants.CONSTANT_PackageNs, packageName);
		                                        					Pool<Namespace> nsPool = emitter.getNamespacePool();
		                                        					nsPool.add(ns);
		                                        					Name name = new Name(ns, baseName);
		                                        					namePool.add(name);
		                                        					newList.add(name);
		                                        					changedABC = true;
		                                        				}
		                                        			}
	                                    					method.setParamTypes(newList);
	                                        			}
	                                        		}
	                                        		break metas;
	                                        	}
	                                        }
	                        			}
	                        		}
	                        	}
	                        }
                        }
                        catch (Exception ee) {}
                        if (changedABC)
                        {
                        	try {
								doABC.setABCData(emitter.emit());
							} catch (Exception e) {
								reportProblem(new UnexpectedExceptionProblem(e));
								return false;
							}
                        }
                	}
                    boolean tagsAdded = swfTags.addToFrame(frame);
                    if (!tagsAdded)
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * Add metadata names to the target for this compilation unit.
     * 
     * @param cu
     * @param linkedIn true if the compilation unit is linked in, false if
     * external.
     */
    protected void doAddMetadataNamesToTarget(ICompilationUnit cu, boolean linkedIn)
    {
        
        if (shouldAddMetadataNamesToTarget(cu, linkedIn))
        {
            if (metadataDonators.add(cu.getAbsoluteFilename()))
            {
                ISWC swc = project.getWorkspace().getSWCManager().
                        get(new File(cu.getAbsoluteFilename()));
                for (ISWCLibrary library : swc.getLibraries())
                {
                    addASMetadataNames(library.getKeepAS3MetadataSet());
                }
            }
                
        }
    }

    /**
     * Test if any metadata names associated with the compilation unit should be
     * added to the target.
     * 
     * @param cu
     * @param linkedIn true if the compilation unit is linked in, false if
     * external.
     * @return true if the metadata names should be included, false otherwise.
     */
    protected boolean shouldAddMetadataNamesToTarget(ICompilationUnit cu, boolean linkedIn)
    {
        return (cu.getCompilationUnitType() == UnitType.SWC_UNIT && 
                !isLinkageAlwaysExternal(cu));
    }

    /**
     * Test if a compilation unit should be include in this target.
     * 
     * @param cu
     * @param allowExternals
     * @return True if the compilation should be included, false otherwise.
     * @throws InterruptedException
     */
    protected boolean testCompilationUnitLinkage(ICompilationUnit cu, boolean allowExternals) throws InterruptedException
    {
        boolean includeCu = true;
        
        if (!allowExternals)
            includeCu = !isLinkageAlwaysExternal(cu);
        else if (isLinkageExternal(cu, targetSettings))
            includeCu = false;
        
        return includeCu;
    }

    /**
     * Test if this compilation unit should always be externalized. Native code
     * cannot be included in an application. We for test native code by checking
     * for the SWC that contains the definition of Object and externalize all of
     * the classes in that SWC. Compilation units that come from an ANE are also
     * always externalized.
     * 
     * @param cu
     * @return true if the compilation unit should always be externalized, false
     * otherwise.
     */
    private boolean isLinkageAlwaysExternal(ICompilationUnit cu)
    {
        if (cu.getCompilationUnitType() != UnitType.SWC_UNIT)
            return false;
        
        // Find the SWC that contains Object.
        if (pathContainingObject == null)
        {
            IResolvedQualifiersReference objectReference = ReferenceFactory.packageQualifiedReference(
                    project.getWorkspace(), 
                    IASLanguageConstants.Object);
            Set<ICompilationUnit> units = project.getScope().
                    getCompilationUnitsForReferences(Collections.singletonList(objectReference));
            assert units.size() == 1;
            
            pathContainingObject = units.iterator().next().getAbsoluteFilename();
            assert pathContainingObject != null;
        }
        
        // If this compilation unit comes from the same SWC as the SWC that
        // contains Object then we must always extern the class.
        if (pathContainingObject.equals(cu.getAbsoluteFilename()))
            return true;
        
        // Test if the compilation unit is from an ANE file.
        if (cu instanceof SWCCompilationUnit)
            return ((SWCCompilationUnit)cu).isANE();
        
        return false;
    }
    
    protected abstract void addLinkedABCToFrame(SWFFrame targetFrame, Iterable<DoABCTag> inputABCs, ABCLinker.ABCLinkerSettings linkSettings) throws Exception;
    
    protected abstract void setKeepAS3MetadataLinkerSetting(ABCLinker.ABCLinkerSettings linkSettings);
    
    /**
     * Link the swf - this handles merging the DoABC tags inside each frame, and will
     * also handle stripping debug opcodes, optimizing the abcs, and/or stripping metadata
     * @param unLinked  the SWF to process
     * @return          A SWF that is the resulting of merging, optimizing, etc.
     */
    protected ISWF linkSWF(ISWF unLinked)
    {
        SWF result = new SWF();
        if( unLinked.getBackgroundColor() != null )
            result.setBackgroundColor(unLinked.getBackgroundColor());
        result.setEnableDebugger2(unLinked.getEnableDebugger2());
        result.setFrameRate(unLinked.getFrameRate());
        result.setFrameSize(unLinked.getFrameSize());
        result.setMetadata(unLinked.getMetadata());
        ScriptLimitsTag scriptLimits = unLinked.getScriptLimits();
        if (scriptLimits != null)
            result.setScriptLimits(scriptLimits.getMaxRecursionDepth(), scriptLimits.getScriptTimeoutSeconds());
        result.setTopLevelClass(unLinked.getTopLevelClass());
        result.setUseAS3(unLinked.getUseAS3());
        result.setUseDirectBlit(unLinked.getUseDirectBlit());
        result.setUseGPU(unLinked.getUseGPU());
        result.setUseNetwork(unLinked.getUseNetwork());
        result.setVersion(unLinked.getVersion());
        result.setProductInfo(unLinked.getProductInfo());
        
        ITargetSettings settings = getTargetSettings();

        ABCLinker.ABCLinkerSettings linkSettings = new ABCLinker.ABCLinkerSettings();

        linkSettings.setOptimize(settings.isOptimized());
        linkSettings.setEnableInlining(project.isInliningEnabled());
        linkSettings.setStripDebugOpcodes(!settings.isDebugEnabled());
        linkSettings.setStripGotoDefinitionHelp(!settings.isDebugEnabled());
        linkSettings.setStripFileAttributeFromGotoDefinitionHelp(settings.isOptimized());
        linkSettings.setProblemsCollection(this.problemCollection);
        linkSettings.setRemoveDeadCode(settings.getRemoveDeadCode());
        
        Collection<String> metadataNames = getASMetadataNames();
        if (settings.isDebugEnabled() && metadataNames != null)
        {
            Collection<String> names = new ArrayList<String>(metadataNames);
            names.add(IMetaAttributeConstants.ATTRIBUTE_GOTODEFINITIONHELP);
            names.add(IMetaAttributeConstants.ATTRIBUTE_GOTODEFINITION_CTOR_HELP);
            metadataNames = names;
        }
        
        setKeepAS3MetadataLinkerSetting(linkSettings);

        for (int i = 0; i < unLinked.getFrameCount(); ++i)
        {
            SWFFrame unlinkedFrame = unLinked.getFrameAt(i);
            SWFFrame resultFrame = new SWFFrame();

            if( unlinkedFrame.getName() != null )
                resultFrame.setName(unlinkedFrame.getName(), unlinkedFrame.hasNamedAnchor());

            LinkedList<DoABCTag> accumulatedABC = new LinkedList<DoABCTag>();
            for (ITag unlinkedTag : unlinkedFrame)
            {
                if (unlinkedTag instanceof DoABCTag)
                {
                    final DoABCTag abcTag = (DoABCTag)unlinkedTag;
                    accumulatedABC.add(abcTag);                        
                }
                else
                {
                    if (!accumulatedABC.isEmpty())
                    {
                        try
                        {
                            addLinkedABCToFrame(resultFrame, accumulatedABC, linkSettings);
                        }
                        catch (Exception e)
                        {
                            return unLinked;
                        }
                        accumulatedABC.clear();
                    }
                    
                    if (!(unlinkedTag instanceof IManagedTag))
                    {
                        resultFrame.addTag(unlinkedTag);
                    }
                    else if( unlinkedTag instanceof SymbolClassTag )
                    {
                        SymbolClassTag s = (SymbolClassTag)unlinkedTag;
                        for( String symbol_name : s.getSymbolNames() )
                        {
                            resultFrame.defineSymbol(s.getSymbol(symbol_name), symbol_name);
                        }
                    }
                }
            }
            if (!accumulatedABC.isEmpty())
            {
                try
                {
                    addLinkedABCToFrame(resultFrame, accumulatedABC, linkSettings);
                }
                catch (Exception e)
                {
                    return unLinked;
                }
                accumulatedABC.clear();
            }
            result.addFrame(resultFrame);
        }
        
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * For {@link SWFTarget}'s the set of rooted {@link ICompilationUnit}s is computed
     * by enumerating all the frames and collecting all the {@link ICompilationUnit}s assigned
     * to each of the frames.
     */
    @Override
    protected RootedCompilationUnits computeRootedCompilationUnits() throws InterruptedException
    {
        final FramesInformation framesInfo = getFramesInformation();
        final RootedCompilationUnits rootedCompilationUnits =
            new RootedCompilationUnits(ImmutableSet.copyOf(framesInfo.getAllCompilationUnits()), framesInfo.getProblems());
        return rootedCompilationUnits;
    }
    
    /**
     * Initialize SWF model with default header values from the target settings
     * when not overridden by the target attributes.
     * 
     * @return SWF model.
     * @throws InterruptedException 
     */
    protected ISWF initializeSWF(List<ICompilationUnit> reachableCompilationUnits) throws InterruptedException
    {
        int swfVersion = targetSettings.getSWFVersion();

        int swfWidth = targetSettings.getDefaultWidth();
        
        ITargetAttributes targetAttributes = getTargetAttributes();
        Float attrWidth = targetAttributes.getWidth();
        if (attrWidth != null)
            swfWidth = attrWidth.intValue();

        int swfHeight = targetSettings.getDefaultHeight();
        Float attrHeight = targetAttributes.getHeight();
        if (attrHeight != null)
            swfHeight = attrHeight.intValue();

        Rect swfFrameSize = new Rect(ISWFConstants.TWIPS_PER_PIXEL * swfWidth,
                                  ISWFConstants.TWIPS_PER_PIXEL * swfHeight);

        float swfFrameRate = targetSettings.getDefaultFrameRate();
        Float attrFrameRate = targetAttributes.getFrameRate();
        if (attrFrameRate != null)
            swfFrameRate = attrFrameRate.floatValue();

        boolean swfUseDirectBlit = targetSettings.useDirectBlit();
        Boolean attrUseDirectBlit = targetAttributes.getUseDirectBlit();
        if (attrUseDirectBlit != null)
            swfUseDirectBlit = attrUseDirectBlit.booleanValue();

        boolean swfUseGPU = targetSettings.useGPU();
        Boolean attrUseGPU = targetAttributes.getUseGPU();
        if (attrUseGPU != null)
            swfUseGPU = attrUseGPU.booleanValue();

        
        final RGB swfBackgroundColorRGB = getBackgroundColor();

        SWF swf = new SWF();
        swf.setVersion(swfVersion);
        swf.setFrameSize(swfFrameSize);
        swf.setFrameRate(swfFrameRate);
        swf.setUseDirectBlit(swfUseDirectBlit);
        swf.setUseGPU(swfUseGPU);
        swf.setBackgroundColor(swfBackgroundColorRGB);
        swf.setUseAS3(swfVersion >= 9);
        swf.setUseNetwork(targetSettings.useNetwork());
        swf.setMetadata(targetSettings.getSWFMetadata());
        
        // Apply the ScriptLimits tag, but only if limits have been specified
        // either in the targetSettings or targetAttributes
        Integer attrScriptRecursionLimit = targetAttributes.getScriptRecursionLimit();
        Integer attrScriptTimeLimit = targetAttributes.getScriptTimeLimit();
        if (targetSettings.areDefaultScriptLimitsSet() || attrScriptRecursionLimit != null || attrScriptTimeLimit != null)
        {
            int swfMaxRecursionDepth = targetSettings.getDefaultScriptRecursionLimit();
            if (attrScriptRecursionLimit != null)
                swfMaxRecursionDepth = attrScriptRecursionLimit.intValue();

            int swfScriptTimeoutSeconds = targetSettings.getDefaultScriptTimeLimit();
            if (attrScriptTimeLimit != null)
                swfScriptTimeoutSeconds = attrScriptTimeLimit.intValue();

            swf.setScriptLimits(swfMaxRecursionDepth, swfScriptTimeoutSeconds);
        }

        return swf;
    }
    
    protected final RGB getBackgroundColor()  throws InterruptedException
    {
        if (swfBackgroundColor != null)
            return swfBackgroundColor;
        
        int swfBackgroundColorInt = targetSettings.getDefaultBackgroundColor();
        String attrBackgroundColorString = getTargetAttributes().getBackgroundColor();
        if (!Strings.isNullOrEmpty(attrBackgroundColorString))
        {
            if (project instanceof RoyaleProject)
                swfBackgroundColorInt = ((RoyaleProject)project).getColorAsInt(attrBackgroundColorString);
            else
                swfBackgroundColorInt = Integer.decode(attrBackgroundColorString).intValue();
        }
        swfBackgroundColor = new RGB(swfBackgroundColorInt);
        return swfBackgroundColor;
    }
    
    @Override
    protected ITargetReport computeTargetReport() throws InterruptedException
    {
        BuiltCompilationUnitSet builtCompilationUnits = getBuiltCompilationUnitSet();
        
        return new TargetReport(project, builtCompilationUnits.compilationUnits, Collections.<RSLSettings>emptyList(), 
                getBackgroundColor(), targetSettings, getTargetAttributes(), getLinkageChecker());
    }
    
    /**
     * Contains information about the skeleton of a SWF that is being built by
     * a {@link SWFTarget} and provides methods to create {@link SWFFrame}s and add them
     * to a {@link ISWF}.
     */
    protected static class FramesInformation
    {
        public FramesInformation(Iterable<SWFFrameInfo> frameInfos)
        {
            this.frameInfos = frameInfos;
        }
        
        /**
         * {@link Iterable} of {@link SWFFrameInfo}s which represents the
         * skeleton of a SWF being built by a {@link SWFTarget}.
         */
        public final Iterable<SWFFrameInfo> frameInfos;
        
        /**
         * @return An {@link Iterable} of {@link ICompilerProblem}s that can be
         * used to iterate all the {@link ICompilerProblem}s found while
         * building the skeleton of a SWF being built by a {@link SWFTarget}.
         */
        final Iterable<ICompilerProblem> getProblems()
        {
            Iterable<Iterable<ICompilerProblem>> problemIterables =
                Iterables.transform(frameInfos, new Function<SWFFrameInfo, Iterable<ICompilerProblem>>() {

                    @Override
                    public Iterable<ICompilerProblem> apply(SWFFrameInfo frame)
                    {
                        return frame.problems;
                    }});
            
            return Iterables.concat(problemIterables);
        }
        
        /**
         * @return An {@link Iterable} of {@link ICompilationUnit}s that are rooted by
         * the skeleton of a SWF buing built by a {@link SWFTarget}.
         */
        final Iterable<ICompilationUnit> getAllCompilationUnits()
        {
            Iterable<Iterable<ICompilationUnit>> compilationUnitIterables =
                Iterables.transform(frameInfos, new Function<SWFFrameInfo, Iterable<ICompilationUnit>>() {

                    @Override
                    public Iterable<ICompilationUnit> apply(SWFFrameInfo frame)
                    {
                        return frame.rootedUnits;
                    }});
            return Iterables.concat(compilationUnitIterables);
        }
        
        /**
         * Creates a {@link SWFFrame} for a {@link SWFFrameInfo}.
         * 
         * @param swfTarget The {@link SWFTarget} that is building the SWF to
         * which the newly created {@link SWFFrame} will be added.
         * @param frameInfo The {@link SWFFrameInfo} that represents the skeleton
         * of the SWF frame to create.
         * @param builtCompilationUnits The {@link ImmutableSet} of
         * {@link ICompilationUnit}s that have been built to create the SWF
         * being built by the specified {@link SWFTarget}. This {@link Set} is
         * used to write an assert.
         * @param emittedCompilationUnits The {@link Set} of
         * {@link ICompilationUnit}s that any {@link ICompilationUnit}s added to
         * this frame should be added to. This {@link Set} is used to ensure
         * that each {@link ICompilationUnit} is only added to a single frame in
         * a SWF.
         * @param problems {@link Collection} of {@link ICompilerProblem}s that
         * any {@link ICompilerProblem}s from any {@link ICompilationUnit} added
         * to the new {@link SWFFrame} should be added to.
         * @return A new {@link SWFFrame}.
         * @throws InterruptedException
         */
        protected final SWFFrame createFrame(SWFTarget swfTarget, SWFFrameInfo frameInfo, ImmutableSet<ICompilationUnit> builtCompilationUnits, Set<ICompilationUnit> emittedCompilationUnits, Collection<ICompilerProblem> problems) throws InterruptedException
        {
            Iterables.addAll(problems, frameInfo.problems);
            
            final SWFFrame swfFrame = new SWFFrame();
            
            if (frameInfo.frameLabel != null)
                swfFrame.setName(frameInfo.frameLabel, true);
            
            assert Sets.difference(frameInfo.rootedUnits, builtCompilationUnits).isEmpty()
                : "All compilation units to emit on this frame should have been built!";
            
            if (!swfTarget.addCompilationUnitsAndDependenciesToFrame(swfFrame, frameInfo.rootedUnits,
                    frameInfo.allowExternals, emittedCompilationUnits))
            {
                return null;                
            }
            return swfFrame;
        }
        
        /**
         * Creates all the {@link SWFFrame}s for the SWF skeleton represented by
         * this {@link FramesInformation} and adds them to the specified
         * {@link ISWF}.
         * <p>
         * This method is overridden by sub-classes of {@link FramesInformation}.
         * 
         * @param swfTarget The {@link SWFTarget} that is building the SWF to
         * which the newly created {@link SWFFrame}s will be added.
         * @param swf The {@link ISWF} to which the new created {@link SWFFrame}
         * s will be added.
         * @param builtCompilationUnits The {@link ImmutableSet} of
         * {@link ICompilationUnit}s that have been built to create the SWF
         * being built by the specified {@link SWFTarget}. Sub-classes use this
         * set to generate code that supports startup of the Flex framework.
         * @param emittedCompilationUnits The {@link Set} of
         * {@link ICompilationUnit}s that any {@link ICompilationUnit}s added to
         * this frame should be added to. This {@link Set} is used to ensure
         * that each {@link ICompilationUnit} is only added to a single frame in
         * a SWF.
         * @param problems {@link Collection} of {@link ICompilerProblem}s that
         * any {@link ICompilerProblem}s from any {@link ICompilationUnit} added
         * to the new {@link SWFFrame}s should be added to.
         * @throws InterruptedException
         */
        protected void createFrames(SWFTarget swfTarget, ISWF swf, ImmutableSet<ICompilationUnit> builtCompilationUnits, Set<ICompilationUnit> emittedCompilationUnits, Collection<ICompilerProblem> problems) throws InterruptedException
        {
            for (final SWFFrameInfo frameInfo : frameInfos)
            {
                SWFFrame swfFrame = createFrame(swfTarget, frameInfo, builtCompilationUnits, emittedCompilationUnits, problems);
                swf.addFrame(swfFrame);
            }
        }
    }
    
}
