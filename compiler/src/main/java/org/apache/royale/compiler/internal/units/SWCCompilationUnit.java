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

package org.apache.royale.compiler.internal.units;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.DependencyTypeSet;
import org.apache.royale.compiler.common.Multiname;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.internal.caches.AssetTagCache;
import org.apache.royale.compiler.internal.caches.CacheStoreKeyBase;
import org.apache.royale.compiler.internal.caches.FileScopeCache;
import org.apache.royale.compiler.internal.caches.SWFCache;
import org.apache.royale.compiler.internal.graph.LinkReportWriter.QNameComparator;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.resourcebundles.ResourceBundleUtils;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.targets.TagSorter;
import org.apache.royale.compiler.internal.units.requests.ABCBytesRequestResult;
import org.apache.royale.compiler.internal.units.requests.ABCFileScopeRequestResult;
import org.apache.royale.compiler.internal.units.requests.SyntaxTreeRequestResult;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.problems.InvalidABCByteCodeProblem;
import org.apache.royale.compiler.problems.NoDefinitionForSWCDependencyProblem;
import org.apache.royale.compiler.problems.NoScopesInABCCompilationUnitProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.royale.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCLibrary;
import org.apache.royale.swc.ISWCScript;
import org.apache.royale.swf.ITagContainer;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.tags.DoABCTag;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.swf.tags.SymbolClassTag;
import com.google.common.collect.SetMultimap;

/**
 * This is a compilation unit for a script in a SWC library. Every SWC library
 * has one or multiple libraries. (i.e. library.swf) Each library has multiple
 * script definition. A script has a name which maps to a named DoABC tag in the
 * library SWF.
 * <p>
 * A {@code SWCCompilationUnit} is similar to {@link ABCCompilationUnit} that
 * they both deal with ABC byte code. The difference is that
 * {@code ABCCompilationUnit} requires ABC byte code when created, while
 * {@code SWCCompilationUnit} only need a pointer to the script location.
 * {@code SWCCompilationUnit} does not request or digest the ABC byte code until
 * any of the handlers is called.
 */
public class SWCCompilationUnit extends CompilationUnitBase
{
    /**
     * Create a compilation unit from ABC byte code in a SWF tag. If a script
     * has multiple public definitions, they will share one
     * {@code SWCCompilationUnit}.
     * 
     * @param project compiler project
     * @param script script information
     * @param qnames of qnames within this swc
     * @param order the order the SWC was added to the project. The lower the 
     * order the higher the priority of the compilation unit. The order is 
     * compared only if the timestamps of two compilation units are equal.
     */
    public SWCCompilationUnit(final CompilerProject project,
                              final ISWC swc,
                              final ISWCLibrary library,
                              final ISWCScript script,
                              final List<String> qnames, 
                              final int order)
    {
        super(project, swc.getSWCFile().getPath(), DefinitionPriority.BasePriority.LIBRARY_PATH, qnames);
        this.swc = swc;
        this.library = library;
        this.script = script;
        this.resourceBundles = new HashSet<String>();

        DefinitionPriority dp = ((DefinitionPriority)getDefinitionPriority());
        dp.setTimestamp(script.getLastModified());
        dp.setOrder(order);
        
        name = computeName();       // now that definition promises are all set up, we can cache th
    }

    private final ISWC swc;
    private final ISWCLibrary library;
    private final ISWCScript script;

    /**
     * Set of ResourceBundles referenced in the associated script.
     */
    private final Set<String> resourceBundles;

    @Override
    public UnitType getCompilationUnitType()
    {
        return UnitType.SWC_UNIT;
    }

    @Override
    protected ISyntaxTreeRequestResult handleSyntaxTreeRequest() throws InterruptedException
    {
        IFileScopeRequestResult fileResult =  getFileScopeRequest().get();
        
        startProfile(Operation.GET_SYNTAX_TREE);
        try
        {
            List<ICompilerProblem> noProblems = Collections.emptyList();
            
            boolean isFlex = false;
            CompilerProject project = getProject();
            
            if ((project instanceof RoyaleProject) && ((RoyaleProject)project).isRoyale())
            {
                isFlex = true;
            }
            
            // Don't need to collect resource bundles for non-flex projects.
            if (isFlex)
            {
                //Find all the resource bundles required for this script
                for (IDefinition definition : fileResult.getExternallyVisibleDefinitions())
                {
                    for(IMetaTag rbTag :  definition.getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_RESOURCEBUNDLE)) 
                    {
                        resourceBundles.add(rbTag.getAllAttributes()[0].getValue());
                    }
                }                
            }
            
            return new SyntaxTreeRequestResult(script.getLastModified(), noProblems)
            {
                @Override
                public Set<String> getRequiredResourceBundles() 
                {
                    return resourceBundles;
                }
            };
        }
        finally
        {
            stopProfile(Operation.GET_SYNTAX_TREE);
        }
    }
    
    @Override
    protected IFileScopeRequestResult handleFileScopeRequest() throws InterruptedException
    {
        startProfile(Operation.GET_FILESCOPE);
        
        getProject().clearScopeCacheForCompilationUnit(this);
        
        final Collection<ICompilerProblem> problems = new HashSet<ICompilerProblem>();
        Collection<IASScope> scopeList = null;
        try
        {
            final FileScopeCache fsCache = (FileScopeCache)getProject().getWorkspace().getSWCManager().getFileScopeCache();
            final CacheStoreKeyBase key = FileScopeCache.createKey(swc, library.getPath(), script);
            scopeList = fsCache.get(key);
            if (scopeList.isEmpty())
            {
                final NoScopesInABCCompilationUnitProblem problem =
                    new NoScopesInABCCompilationUnitProblem(getRootFileSpecification().getPath());
                problems.add(problem);
            }
        }
        catch (Exception e)
        {
            final InvalidABCByteCodeProblem problem = new InvalidABCByteCodeProblem(getRootFileSpecification().getPath());
            problems.add(problem);
        }

        ABCFileScopeRequestResult result = new ABCFileScopeRequestResult(problems, scopeList);
        stopProfile(Operation.GET_FILESCOPE);

        return result;
    }

    /**
     * Find the {@code DoABC} tag from {@link SWFCache} by script name and
     * extract the ABC bytes.
     */
    @Override
    protected IABCBytesRequestResult handleABCBytesRequest() throws InterruptedException
    {
        byte[] abcBytes = null;
        final ArrayList<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();

        final CacheStoreKeyBase key = SWFCache.createKey(swc, library.getPath());
        final ITagContainer tags = ((SWFCache)getProject().getWorkspace().getSWCManager().getSWFCache()).get(key);

        startProfile(Operation.GET_ABC_BYTES);

        final DoABCTag doABC = SWFCache.findDoABCTagByName(tags, script.getName());
        if (doABC == null)
        {
            problems.add(new InternalCompilerProblem(
                    new RuntimeException("can't find ABC bytes for : " + script.getName())));
        }
        else
        {
            abcBytes = doABC.getABCData();
        }

        ABCBytesRequestResult result = new ABCBytesRequestResult(abcBytes);
        stopProfile(Operation.GET_ABC_BYTES);

        return result;
    }

    /**
     * Add a DoABC tag for the definition of this compilation unit and its asset
     * tags to the SWF. The DoABC tag is copied from the library SWF. The asset
     * tags are from {@link AssetTagCache}.
     */
    @Override
    protected ISWFTagsRequestResult handleSWFTagsRequest() throws InterruptedException
    {
        final ArrayList<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        final ArrayList<ITag> linkingTags = new ArrayList<ITag>();

        // link main definition
        final CacheStoreKeyBase key = SWFCache.createKey(swc, library.getPath());
        final ITagContainer tags = ((SWFCache)getProject().getWorkspace().getSWCManager().getSWFCache()).get(key);
        final DoABCTag doABC = SWFCache.findDoABCTagByName(tags, script.getName());
        if (doABC == null)
            throw new NullPointerException("can not find DoABC tag: " + script.getName());

        startProfile(Operation.GET_SWF_TAGS);

        linkingTags.add(doABC);

        // link assets for all the definitions in this script
        final HashMap<String, ICharacterTag> assetTags = new LinkedHashMap<String, ICharacterTag>();
        for (final String defQName : script.getDefinitions())
        {
            final CacheStoreKeyBase assetCacheKey = AssetTagCache.createKey(swc, library.getPath(), script, defQName);
            final AssetTagCache.AssetTagCacheValue assetCacheValue = ((AssetTagCache)getProject().getWorkspace().getSWCManager().getAssetTagCache()).get(assetCacheKey);
            if (assetCacheValue.assetTag != null)
            {
                linkingTags.add(assetCacheValue.assetTag);
                linkingTags.addAll(assetCacheValue.referredTags);
                assetTags.put(defQName, assetCacheValue.assetTag);
            }
        }

        final List<ITag> sortedTags = TagSorter.sortFullGraph(linkingTags);
        ISWFTagsRequestResult result = new ISWFTagsRequestResult()
        {
            @Override
            public boolean addToFrame(SWFFrame frame)
            {
                for (final ITag tag : sortedTags)
                    frame.addTag(tag);

                for (final Map.Entry<String, ICharacterTag> tag : assetTags.entrySet())
                    frame.defineSymbol(tag.getValue(), tag.getKey());

                return true;
            }

            @Override
            public ICompilerProblem[] getProblems()
            {
                return problems.toArray(new ICompilerProblem[0]);
            }

            @Override
            public String getDoABCTagName()
            {
                return script.getName();
            }
            
            @Override
            public DoABCTag getDoABCTag()
            {
                return doABC;
            }
        };
        stopProfile(Operation.GET_SWF_TAGS);

        return result;
    }

    @Override
    protected IOutgoingDependenciesRequestResult handleOutgoingDependenciesRequest () throws InterruptedException
    {
        getSyntaxTreeRequest().get();

        startProfile(Operation.GET_SEMANTIC_PROBLEMS);

        SetMultimap<String,DependencyType> dependencies = script.getDependencies();
        addAssetTagDependencies(dependencies);

        ASProjectScope projectScope = getProject().getScope();
        final Collection<ICompilerProblem> problems = new LinkedList<ICompilerProblem>();
        for (final Map.Entry<String, Collection<DependencyType>> dependencyEntry : dependencies.asMap().entrySet())
        {
            IDefinition[] defs = projectScope.findAllDefinitionsByName(Multiname.crackDottedQName(getProject(), dependencyEntry.getKey(), true));
            if (defs == null || defs.length == 0)
            {
                ICompilerProblem problem = new NoDefinitionForSWCDependencyProblem(
                    getRootFileSpecification().getPath(), getAbsoluteFilename(), dependencyEntry.getKey(),
                    script.getDefinitions().iterator().next());
                problems.add(problem);
            }
            else
            {
                assert (defs != null && defs.length <= 1) : "Lookups using a fully qualified name should find at most 1 definition";
                ICompilationUnit referencedCU = projectScope.getCompilationUnitForScope(defs[0].getContainingScope());
                DependencyTypeSet dependencyTypes = DependencyTypeSet.copyOf(dependencyEntry.getValue());
                getProject().addDependency(this, referencedCU, dependencyTypes, defs[0].getQualifiedName());
            }
        }

        // Add dependencies to the resource bundles used by this compilation unit
        for (String bundleName : resourceBundles)
        {
            ResourceBundleUtils.resolveDependencies(bundleName, this, getProject(), null, problems);
        }
        
        IOutgoingDependenciesRequestResult result = new IOutgoingDependenciesRequestResult()
        {
            @Override
            public ICompilerProblem[] getProblems()
            {
                return problems.toArray(new ICompilerProblem[problems.size()]);
            }
        };
        stopProfile(Operation.GET_SEMANTIC_PROBLEMS);

        return result;
    }

    private void addAssetTagDependencies(SetMultimap<String, DependencyType> dependencies)
    {
        final CacheStoreKeyBase key = SWFCache.createKey(swc, library.getPath());
        final ITagContainer swfTags = ((SWFCache)getProject().getWorkspace().getSWCManager().getSWFCache()).get(key);
        final Collection<SymbolClassTag> symbolTags = SWFCache.findAllSymbolClassTags(swfTags);

        for (final String defQName : script.getDefinitions())
        {
            final CacheStoreKeyBase assetCacheKey = AssetTagCache.createKey(swc, library.getPath(), script, defQName);
            final AssetTagCache.AssetTagCacheValue assetCacheValue = ((AssetTagCache)getProject().getWorkspace().getSWCManager().getAssetTagCache()).get(assetCacheKey);
            if (assetCacheValue.referredTags != null)
            {
                for (ITag referredTag : assetCacheValue.referredTags)
                {
                    if (referredTag instanceof ICharacterTag)
                    {
                        for (SymbolClassTag symbolTag : symbolTags)
                        {
                            String symbol = symbolTag.getSymbolName((ICharacterTag)referredTag);
                            if (symbol != null)
                                dependencies.put(symbol, DependencyType.EXPRESSION);
                        }
                    }
                }
            }
        }
    }

    /**
     * Test if this compilation unit comes from an ANE File.
     * 
     * @return true if the compilation unit comes from an ANE file, false
     * otherwise.
     */
    public boolean isANE()
    {
        return swc.isANE();
    }

    public ISWC getSWC()
    {
        return swc;
    }
    
    @Override
    public String getName()
    { 
      assert name.equals(computeName());
      return name;
    }
    
    private String computeName()
    {
        // NOTE: keep this in sync with CrossProjectCompilationUnit.getName()
        return SWCCompilationUnit.getLinkReportName(this);
    }
    
    private final String name;

    @Override
    protected void handleClean(boolean cleanFileScope, Map<ICompilerProject, Set<File>> invalidatedSWCFiles)
    {
        if (!cleanFileScope)
            return;
        File swcFile = swc.getSWCFile();
        addInvalidatedSWCtoList(invalidatedSWCFiles, swcFile, getProject());            
    }

    private void addInvalidatedSWCtoList(Map<ICompilerProject, Set<File>> invalidatedSWCFiles, 
            File swcFile, CompilerProject project)
    {
        if (invalidatedSWCFiles == null)
            return;

        Set<File> swcsInProject = invalidatedSWCFiles.get(project);
        if (swcsInProject == null)
        {
            swcsInProject = new HashSet<File>();
            invalidatedSWCFiles.put(project, swcsInProject);
        }

        swcsInProject.add(swcFile);
    }

    /**
     * @return A string for link reports
     */
    protected static String getLinkReportName(ICompilationUnit cu)
    {
    	String absoluteFileName = cu.getAbsoluteFilename();
    	ICompilerProject project = cu.getProject();
    	if (project instanceof RoyaleProject)
    	{
    		String alias = ((RoyaleProject)project).getSwfDebugfileAlias();
    		if (alias != null)
    		{
    			// clip off path to SWC
    			int slash = absoluteFileName.lastIndexOf("/");
    			int backslash = absoluteFileName.lastIndexOf("\\");
    			int lastSep = slash > backslash ? slash : backslash;
    			absoluteFileName = absoluteFileName.substring(lastSep + 1);
    		}
    	}
        StringBuilder reportBuilder = new StringBuilder(absoluteFileName);
        reportBuilder.append('(');
        
        ArrayList<String> definitionQnames = new ArrayList<String>(cu.getDefinitionPromises().size());
        
        for (IDefinition definition : cu.getDefinitionPromises())
            definitionQnames.add(definition.getQualifiedName());
        
        Collections.sort(definitionQnames, new QNameComparator());
        
        for (String qname : definitionQnames)
        {
            int lastOccurence = qname.lastIndexOf('.');
            
            String xmlStyleScriptName = qname;
            if(lastOccurence > -1)
                xmlStyleScriptName = qname.substring(0, lastOccurence) + ":" + qname.substring(lastOccurence + 1, qname.length()); 
            
            reportBuilder.append(xmlStyleScriptName);
            reportBuilder.append(", ");
        }
        reportBuilder.delete(reportBuilder.length()-2, reportBuilder.length());
        reportBuilder.append(')');
        
        return reportBuilder.toString();
    }

    /**
     * For debugging.
     */
    @Override
    public String toString()
    {
        return "SWC: " + getDefinitionPromises().toString();
    }
}
