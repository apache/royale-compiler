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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.ABCLinker;
import org.apache.royale.abc.ABCLinker.ABCLinkerSettings;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.config.FrameInfo;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.problems.FileNotFoundProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.ImproperlyConfiguredTargetProblem;
import org.apache.royale.compiler.problems.UnableToFindRootClassDefinitionProblem;
import org.apache.royale.compiler.targets.ITarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.IRequest;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.tags.DoABCTag;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Sub-class of {@link SWFTarget} that builds a SWF that is meant to be loaded
 * by the flash player ( as opposed to a library.swf in a SWC ).
 */
public class AppSWFTarget extends SWFTarget
{
    /**
     * Constructor
     * 
     * @param project {@link CompilerProject} that contains the code this
     * {@link AppSWFTarget} compiles.
     * @param targetSettings {@link ITargetSettings} that contains the
     * confuration of this {@link AppSWFTarget}.
     * @param progressMonitor {@link ITargetProgressMonitor} to which status is
     * reported as this {@link AppSWFTarget} is built.
     */
    public AppSWFTarget(CompilerProject project, ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor)
    {
        this(project, targetSettings, progressMonitor, Collections.<ICompilationUnit>emptySet());
    }
    
    /**
     * Constructor
     * 
     * @param project {@link CompilerProject} that contains the code this
     * {@link AppSWFTarget} compiles.
     * @param targetSettings {@link ITargetSettings} that contains the
     * confuration of this {@link AppSWFTarget}.
     * @param progressMonitor {@link ITargetProgressMonitor} to which status is
     * reported as this {@link AppSWFTarget} is built.
     * @param additionalRootedCompilationUnits {@link Set} of additional
     * {@link ICompilationUnit}s that will be built add added to the output of
     * this {@link AppSWFTarget}.
     */
    public AppSWFTarget(CompilerProject project, ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor,
            Set<ICompilationUnit> additionalRootedCompilationUnits)
    {
        super(project, targetSettings, progressMonitor);
        assert additionalRootedCompilationUnits != null;
        this.additionalRootedCompilationUnits = additionalRootedCompilationUnits;
    }
    
    private static final FramesInformation NO_EXPLICIT_FRAMES =
        new FramesInformation(Collections.<SWFFrameInfo>emptyList());
    
    /**
     * {@link Set} of additional {@link ICompilationUnit}s that will be built
     * add added to the output of this {@link AppSWFTarget}.
     */
    private final Set<ICompilationUnit> additionalRootedCompilationUnits;
    
    /**
     * List of {@link SWFFrameInfo}s for frames explicitly requested by the
     * {@link ITargetSettings} for this {@link ITarget}.
     * 
     * @see ITargetSettings#getFrameLabels()
     */
    private FramesInformation swfFrameInfosForExplicitFrames;
    
    /**
     * Cached reference to the {@link ClassDefinition} for the root class.
     */
    private ClassDefinition rootClassDefinition;
    
    @Override
    protected Iterable<ICompilerProblem> computeFatalProblems() throws InterruptedException
    {
        final Iterable<ICompilerProblem> fatalProblemsFromSuper = super.computeFatalProblems();
        if (!Iterables.isEmpty(fatalProblemsFromSuper))
            return fatalProblemsFromSuper;
        
        IResolvedQualifiersReference rootClassRef = getRootClassReference();
        if (rootClassRef == null)
            return ImmutableList.<ICompilerProblem>of(new ImproperlyConfiguredTargetProblem());
        
        String rootClassFileName = targetSettings.getRootSourceFileName();
        if (rootClassFileName == null)
            return ImmutableList.<ICompilerProblem>of(new ImproperlyConfiguredTargetProblem());
        
        Collection<ICompilationUnit> rootClassCompilationUnits = project.getCompilationUnits(rootClassFileName);
        assert rootClassCompilationUnits.isEmpty() || rootClassCompilationUnits.size() == 1;
        if (rootClassCompilationUnits.isEmpty())
            return ImmutableList.<ICompilerProblem>of(new FileNotFoundProblem(rootClassFileName));
        
        assert Iterables.getOnlyElement(rootClassCompilationUnits) != null : "The build should have been aborted before this point if there is no root class compilation unit.";
        
        IDefinition rootClassDefinition = rootClassRef.resolve(project);
        if (rootClassDefinition == null)
            return ImmutableList.<ICompilerProblem>of(new UnableToFindRootClassDefinitionProblem(targetSettings.getRootClassName()));
        
        return ImmutableList.<ICompilerProblem>of();
    }
    
    /**
     * Builds a {@link FramesInformation} object for any explicit
     * frames specified by {@link ITargetSettings#getFrameLabels()}.
     * @return A new {@link FramesInformation} 
     */
    private FramesInformation computeExplicitFramesInformation()
    {
        List<FrameInfo> explicitFrames = targetSettings.getFrameLabels();
        if (explicitFrames.isEmpty())
        {
            return NO_EXPLICIT_FRAMES;
        }
        
        ArrayList<SWFFrameInfo> frames = new ArrayList<SWFFrameInfo>(explicitFrames.size());
        for (FrameInfo frameInfo : explicitFrames)
        {
            ImmutableList.Builder<ICompilerProblem> problems = ImmutableList.builder();
            
            List<String> classes = frameInfo.getFrameClasses();
            List<ClassDefinition> resolvedClasses = new ArrayList<ClassDefinition>(classes.size());
            Set<ICompilationUnit> frameCompilationUnits = new HashSet<ICompilationUnit>(classes.size());
            for (String frameClass : classes)
            {
                IResolvedQualifiersReference ref = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), frameClass);
                IDefinition def = ref.resolve(project);
                if (def instanceof ClassDefinition)
                {
                    resolvedClasses.add((ClassDefinition)def);
                    
                    ICompilationUnit defCU = project.getScope().getCompilationUnitForDefinition(def);
                    assert (defCU != null) : "could not resolve def to CU";
                    frameCompilationUnits.add(defCU);
                }
                else
                {
                    // TODO add problem!!! http://bugs.adobe.com/jira/browse/CMP-2059
                }
            }
            frames.add(new SWFFrameInfo(frameInfo.getLabel(), SWFFrameInfo.EXTERNS_ALLOWED, frameCompilationUnits, problems.build()));
        }
        return new FramesInformation(frames);
    }
    
    /**
     * Get's a cached {@code FramesInformation} for frames created by the
     * -frames command line option.
     * 
     * @return a cached {@code FramesInformation} for frames created by the
     * -frames command line option.
     */
    protected final FramesInformation getExplicitFramesInformation()
    {
        if (swfFrameInfosForExplicitFrames != null)
            return swfFrameInfosForExplicitFrames;
        swfFrameInfosForExplicitFrames = computeExplicitFramesInformation();
        return swfFrameInfosForExplicitFrames;
    }
    
    @Override
    protected ITargetAttributes computeTargetAttributes() throws InterruptedException
    {

        ICompilationUnit mainUnit = getRootClassCompilationUnit();
        IRequest<ISyntaxTreeRequestResult, ICompilationUnit> request = mainUnit.getSyntaxTreeRequest();
        ISyntaxTreeRequestResult result = request.get();

        IASNode root = result.getAST();
           
        if (!(root instanceof IFileNode))
            return NilTargetAttributes.INSTANCE;

        final ITargetAttributes nodeTargetAttributes = ((IFileNode)root).getTargetAttributes(this.project);
        if (nodeTargetAttributes == null)
            return NilTargetAttributes.INSTANCE;
        return nodeTargetAttributes;
    }
    
    
    /**
     * Creates a {@link SWFFrameInfo} for the main frame.
     * 
     * @return A new {@link SWFFrameInfo}.
     * @throws InterruptedException
     */
    private SWFFrameInfo createMainFrameInfo() throws InterruptedException
    {
        final ImmutableSet.Builder<ICompilationUnit> compilationUnits =
            ImmutableSet.<ICompilationUnit>builder();
        
        ICompilationUnit rootCU = getRootClassCompilationUnit();
        
        compilationUnits.add(rootCU);
        final Iterable<ICompilationUnit> includesCompilationUnits =
            getIncludesCompilationUnits();
        compilationUnits.addAll(includesCompilationUnits);
        
        final Iterable<ICompilationUnit> includeLibrariesCompilationUnits =
            getIncludeLibrariesCompilationUnits();
        compilationUnits.addAll(includeLibrariesCompilationUnits);
        
        compilationUnits.addAll(additionalRootedCompilationUnits);
        
        Collection<ICompilerProblem> externallyVisibleDefinitionProblems =
            rootCU.getFileScopeRequest().get().checkExternallyVisibleDefinitions(targetSettings.getRootClassName());
        
        final SWFFrameInfo mainFrameInfo =
            new SWFFrameInfo(compilationUnits.build(), externallyVisibleDefinitionProblems);
        return mainFrameInfo;
    }
    
    /**
     * Create the {@code FramesInformation} which contains the skeleton for the frames
     * of this SWF. The actual frames will be create in doCreateFrames().
     * @throws InterruptedException 
     */
    protected FramesInformation computeFramesInformation() throws InterruptedException
    {
        final SWFFrameInfo mainFrameInfo = createMainFrameInfo();
        final FramesInformation explicitFrames =
            getExplicitFramesInformation();
        Iterable<SWFFrameInfo> frames =
            Iterables.concat(Collections.singletonList(mainFrameInfo), explicitFrames.frameInfos);
        return new AppFramesInformation(frames, targetSettings.getRootClassName());
    }
    
    @Override
    protected void addLinkedABCToFrame(SWFFrame targetFrame, Iterable<DoABCTag> inputABCs, ABCLinkerSettings linkSettings) throws Exception
    {
        Iterable<byte[]> inputABCsBytes = Iterables.transform(inputABCs, new Function<DoABCTag, byte[]>() {
            
            @Override
            public byte[] apply(DoABCTag arg0)
            {
                return arg0.getABCData();
            }});
        byte[] linkedBytes = 
            ABCLinker.linkABC(inputABCsBytes, ABCConstants.VERSION_ABC_MAJOR_FP10, ABCConstants.VERSION_ABC_MINOR_FP10, linkSettings);
        DoABCTag linkedTag = new DoABCTag(1, "merged", linkedBytes);
        targetFrame.addTag(linkedTag);
    }

    @Override
    protected void setKeepAS3MetadataLinkerSetting(ABCLinkerSettings linkSettings)
    {
        ITargetSettings settings = getTargetSettings();
        Collection<String> metadataNames = getASMetadataNames();
        if (settings.isDebugEnabled() && metadataNames != null)
        {
            Collection<String> names = new ArrayList<String>(metadataNames);
            names.add(IMetaAttributeConstants.ATTRIBUTE_GOTODEFINITIONHELP);
            names.add(IMetaAttributeConstants.ATTRIBUTE_GOTODEFINITION_CTOR_HELP);
            metadataNames = names;
        }
        linkSettings.setKeepMetadata(metadataNames);
    }
    
    /**
     * @return A {@link ClassDefinition} of the root class of this target if there
     * is one, null otherwise.
     */
    private ClassDefinition computeRootClassDefinition()
    {
        IResolvedQualifiersReference rootClassRef = getRootClassReference();
        if (rootClassRef == null)
            return null;

        IDefinition rootClassDef = rootClassRef.resolve(project);
        if (!(rootClassDef instanceof ClassDefinition))
            return null;

        return (ClassDefinition)rootClassDef;
    }
    
    protected ClassDefinition getRootClassDefinition()
    {
        if (rootClassDefinition != null)
            return rootClassDefinition;
        rootClassDefinition = computeRootClassDefinition();
        return rootClassDefinition;
    }
    
    /**
     * @return the root class compilation unit.  may be null if the root class
     * has not been specified
     */
    protected final ICompilationUnit getRootClassCompilationUnit()
    {
        String rootClassFileName = targetSettings.getRootSourceFileName();
        if (rootClassFileName == null)
            return null;
        
        Collection<ICompilationUnit> rootClassCompilationUnits = project.getCompilationUnits(rootClassFileName);
        assert rootClassCompilationUnits.size() == 1 : "There must only be a single compilation unit for the root source file!";
        return Iterables.getOnlyElement(rootClassCompilationUnits);
    }
    


    /**
     * @return A {@link IResolvedQualifiersReference} that resolves to the root
     * class of this target if there is one, null otherwise.
     */
    private IResolvedQualifiersReference getRootClassReference()
    {
        String rootClassName = targetSettings.getRootClassName();
        if (rootClassName == null)
            return null;
        return ReferenceFactory.packageQualifiedReference(project.getWorkspace(), rootClassName, true);
    }
    
    @Override
    protected ISWF linkSWF(ISWF unLinked)
    {
        if (!targetSettings.isOptimized())
            return unLinked;
        return super.linkSWF(unLinked);
    }

    private static final class AppFramesInformation extends FramesInformation
    {

        AppFramesInformation(Iterable<SWFFrameInfo> frameInfos, String rootClassName)
        {
            super(frameInfos);
            this.rootClassName = rootClassName;
        }
        
        private final String rootClassName;

        @Override
        protected void createFrames(SWFTarget swfTarget, ISWF swf, ImmutableSet<ICompilationUnit> builtCompilationUnits, Set<ICompilationUnit> emittedCompilationUnits, Collection<ICompilerProblem> problems) throws InterruptedException
        {
            super.createFrames(swfTarget, swf, builtCompilationUnits, emittedCompilationUnits, problems);
            swf.setTopLevelClass(rootClassName);
        }
    }
    
}
