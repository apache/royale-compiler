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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.ABCEmitter;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.IEmbedResolver;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSManager;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IEffectDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.ISetterDefinition;
import org.apache.royale.compiler.definitions.IStyleDefinition;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.abc.ClassGeneratorHelper;
import org.apache.royale.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.royale.compiler.internal.css.codegen.ICSSCodeGenResult;
import org.apache.royale.compiler.internal.css.semantics.ActivatedStyleSheets;
import org.apache.royale.compiler.internal.css.semantics.CSSSemanticAnalyzer;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.projects.DependencyGraph;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.tree.mxml.MXMLFileNode;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnit;
import org.apache.royale.compiler.internal.units.MXMLCompilationUnit;
import org.apache.royale.compiler.internal.units.SWCCompilationUnit;
import org.apache.royale.compiler.mxml.IMXMLTypeConstants;
import org.apache.royale.compiler.problems.CSSCodeGenProblem;
import org.apache.royale.compiler.problems.ClassesMappedToSameRemoteAliasProblem;
import org.apache.royale.compiler.problems.FileNotFoundProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MissingFactoryClassInFrameMetadataProblem;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetReport;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEmbedNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit.UnitType;
import org.apache.royale.compiler.units.requests.IRequest;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.tags.DoABCTag;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * Sub-class of {@link AppSWFTarget} that builds an application SWF that uses
 * the royale framework.
 */
public class RoyaleAppSWFTarget extends AppSWFTarget
{
    /**
     * Constructor
     * 
     * @param mainApplicationClass {@link IResolvedQualifiersReference} that
     * resolve to the main class for the royale application.
     * @param project {@link RoyaleProject} containing all the code that will be
     * compiled into the application.
     * @param targetSettings {@link ITargetSettings} with configuration
     * information for this target.
     * @param progressMonitor {@link ITargetProgressMonitor} to which status is
     * reported as this {@link AppSWFTarget} is built.
     */
    public RoyaleAppSWFTarget(IResolvedQualifiersReference mainApplicationClass, RoyaleProject project,
            ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor) throws InterruptedException
    {
        super(project, targetSettings, progressMonitor);
        royaleProject = project;
    }
    
    private boolean isFlexSDKInfo = targetSettings.getInfoFlex();
    
    private final RoyaleProject royaleProject;
    
    public boolean isFlexInfo()
    {
        return getDelegate().isFlexInfo(getRootClassDefinition());
    }
    
    private FlexDelegate delegate;
    
    private FlexDelegate getDelegate()
    {
        if (delegate != null)
            return delegate;
        final ClassDefinition mainAppClass = getRootClassDefinition();
        assert mainAppClass != null : "build should be abort before this point if root class does not exist!";
        delegate = new FlexDelegate(mainAppClass, targetSettings, royaleProject);
        return delegate;
    }
    
    /**
     * Helper method that resolves a {@link IResolvedQualifiersReference} to a
     * definition, finds the {@link ICompilationUnit} that defines the
     * definition, and adds that {@link ICompilationUnit} to the
     * {@link ImmutableSet.Builder}.
     * 
     * @param referenceToResolve {@link IResolvedQualifiersReference} to
     * resolve.
     * @param projectScope {@link ASProjectScope} that should be used to find a
     * {@link ICompilationUnit} for an {@link IDefinition}.
     * @param compilationUnits {@link ImmutableSet.Builder} to which the
     * {@link ICompilationUnit} that defines the {@link IDefinition} the
     * specified {@link IResolvedQualifiersReference} should be added.
     * @param problems {@link ImmutableList.Builder} to which any
     * {@link ICompilerProblem}s found by resolving the specified reference
     * should be added.
     */
    private void resolveReferenceToCompilationUnit(IResolvedQualifiersReference referenceToResolve,
            ASProjectScope projectScope,
            ImmutableSet.Builder<ICompilationUnit> compilationUnits,
            ImmutableList.Builder<ICompilerProblem> problems)
    {
        final IDefinition definition = referenceToResolve.resolve(royaleProject);
        // TODO add a problem if definition is not found
        if (definition != null)
        {
            ICompilationUnit moduleFactoryCU = projectScope.getCompilationUnitForDefinition(definition);
            assert moduleFactoryCU != null : "Unable to get compilation unit for definition!";
            compilationUnits.add(moduleFactoryCU);
        }
    }
    
    /**
     * Creates a {@link SWFFrameInfo} for a generated sub-class of the
     * {@code mx.managers.SystemManager} class from the royale framework.
     * 
     * @param factoryClass {@link ClassDefinition} for which a sub-class should
     * be generated, usually {@code mx.managers.SystemManager}.
     * @return A new {@link SWFFrameInfo}.
     * @throws InterruptedException
     */
    private SWFFrameInfo createFrameInfoForGeneratedSystemManager(ClassDefinition factoryClass) throws InterruptedException
    {
        final ICompilationUnit factoryClassCompilationUnit = royaleProject.getScope().getCompilationUnitForDefinition(factoryClass);
        assert factoryClassCompilationUnit != null :"Unable to find compilation unit for definiton!";
        
        final ImmutableSet.Builder<ICompilationUnit> compilationUnits =
            ImmutableSet.<ICompilationUnit>builder();
        final ImmutableList.Builder<ICompilerProblem> problems =
            ImmutableList.<ICompilerProblem>builder();
        
        compilationUnits.add(factoryClassCompilationUnit);
        
        final ASProjectScope projectScope = royaleProject.getScope();
        
        final FlexDelegate delegate = getDelegate();
        
        if (delegate.iModuleFactoryReference.resolve(royaleProject) != null)
        {
            resolveReferenceToCompilationUnit(delegate.iModuleFactoryReference,
                    projectScope,
                    compilationUnits,
                    problems);
        }
        
        if (delegate.iSWFContextReference.resolve(royaleProject) != null)
        {
            resolveReferenceToCompilationUnit(delegate.iSWFContextReference,
                    projectScope,
                    compilationUnits,
                    problems);
        }
        
        resolveReferenceToCompilationUnit(delegate.getPreloaderClassReference(),
                projectScope,
                compilationUnits,
                problems);
        
        final boolean hasCrossDomainRSL = !targetSettings.getRuntimeSharedLibraryPath().isEmpty();
        if (hasCrossDomainRSL)
            resolveReferenceToCompilationUnit(delegate.crossDomainRSLItemReference,
                    projectScope,
                    compilationUnits,
                    problems);
        
        final String compatibilityVersion = royaleProject.getCompatibilityVersionString();
        if (compatibilityVersion != null)
            resolveReferenceToCompilationUnit(delegate.royaleVersionReference,
                    projectScope,
                    compilationUnits,
                    problems);
        
        final IResolvedQualifiersReference runtimeDPIProviderRef = delegate.getRuntimeDPIProviderClassReference();
        if (runtimeDPIProviderRef != null)
            resolveReferenceToCompilationUnit(runtimeDPIProviderRef,
                    projectScope,
                    compilationUnits,
                    problems);
        
        final FlexSplashScreenImage splashScreen = delegate.getSplashScreenImage();
        if (splashScreen.compilationUnit != null)
            compilationUnits.add(splashScreen.compilationUnit);
        else if (splashScreen.generatedEmbedClassReference != null)
            resolveReferenceToCompilationUnit(splashScreen.generatedEmbedClassReference,
                    projectScope,
                    compilationUnits,
                    problems);
        
        final SWFFrameInfo systemManagerFrameInfo =
            new SWFFrameInfo(factoryClass.getQualifiedName(), SWFFrameInfo.EXTERNS_DISALLOWED, compilationUnits.build(), problems.build());
        
        return systemManagerFrameInfo;
    }
    
    /**
     * Creates a {@link SWFFrameInfo} for the frame in a royale application SWF
     * that defines the main application class.
     * 
     * @param rootClassDefinition {@link ClassDefinition} for the root
     * application class of a royale application SWF.
     * @return A new {@link SWFFrameInfo}.
     * @throws InterruptedException
     */
    private SWFFrameInfo createFrameInfoForApplicationFrame(ClassDefinition rootClassDefinition) throws InterruptedException
    {
        final ImmutableSet.Builder<ICompilationUnit> compilationUnits =
            ImmutableSet.<ICompilationUnit>builder();
        final ImmutableList.Builder<ClassDefinition> classes =
            ImmutableList.<ClassDefinition>builder();
        final ImmutableList.Builder<ICompilerProblem> problems =
            ImmutableList.<ICompilerProblem>builder();
        
        ICompilationUnit rootClassCU = getRootClassCompilationUnit();
        assert rootClassCU != null : "Unable to find compilation unit for definiton!";
        
        compilationUnits.add(rootClassCU);
        classes.add(rootClassDefinition);
        
        final Iterable<ICompilationUnit> includesCompilationUnits =
            getIncludesCompilationUnits();
        compilationUnits.addAll(includesCompilationUnits);
        
        final Iterable<ICompilationUnit> includeLibrariesCompilationUnits =
            getIncludeLibrariesCompilationUnits();
        compilationUnits.addAll(includeLibrariesCompilationUnits);
        
        final FlexDelegate delegate = getDelegate();
        final ASProjectScope projectScope = royaleProject.getScope();
        
        if (delegate.getGenerateSystemManagerAndFlexInit() && isFlexSDKInfo)
        {
            resolveReferenceToCompilationUnit(delegate.generateCSSStyleDeclarationsReference,
                    projectScope,
                    compilationUnits,
                    problems);
            
            resolveReferenceToCompilationUnit(delegate.childManagerReference,
                    projectScope,
                    compilationUnits,
                    problems);
            
            resolveReferenceToCompilationUnit(delegate.styleManagerImplReference,
                    projectScope,
                    compilationUnits,
                    problems);
            
            resolveReferenceToCompilationUnit(delegate.effectManagerReference,
                    projectScope,
                    compilationUnits,
                    problems);
            
            resolveReferenceToCompilationUnit(delegate.textFieldFactoryReference,
                    projectScope,
                    compilationUnits,
                    problems);
        }
        
        final SWFFrameInfo applicationFrameInfo =
            new SWFFrameInfo(rootClassDefinition.getQualifiedName(), SWFFrameInfo.EXTERNS_ALLOWED, compilationUnits.build(), problems.build());
        return applicationFrameInfo;
    }
    
    @Override
    protected FramesInformation computeFramesInformation() throws InterruptedException
    {
        LinkedList<SWFFrameInfo> frames = new LinkedList<SWFFrameInfo>();
        ClassDefinition rootClassDef = getRootClassDefinition();
        assert rootClassDef != null : "If the root class can not be resolved, the build should be aborted before this point";
        
        ICompilationUnit rootClassCU = getRootClassCompilationUnit();
        assert rootClassCU != null :"Unable to find compilation unit for definiton!";
        
        final SWFFrameInfo applicationFrame =
            createFrameInfoForApplicationFrame(rootClassDef);
        
        frames.addFirst(applicationFrame);
        
        final ClassDefinition initialFactoryClass =
            rootClassDef.resolveInheritedFactoryClass(royaleProject);
        ClassDefinition currentFrameClass = initialFactoryClass;
        
        SWFFrameInfo systemManagerFrame = null;
        
        final FlexDelegate delegate = getDelegate();
        if (delegate.getGenerateSystemManagerAndFlexInit())
        {
            final SWFFrameInfo frameInfo = createFrameInfoForGeneratedSystemManager(initialFactoryClass);
            systemManagerFrame = frameInfo;
            frames.addFirst(frameInfo);
            currentFrameClass = currentFrameClass.resolveInheritedFactoryClass(project);
        }
        
        while ((currentFrameClass != null) && (!currentFrameClass.isImplicit()))
        {
            ICompilationUnit currentFrameClassCompilationUnit = royaleProject.getScope().getCompilationUnitForDefinition(currentFrameClass);
            assert currentFrameClassCompilationUnit != null :"Unable to find compilation unit for definiton!";
            final SWFFrameInfo frameInfo = new SWFFrameInfo(currentFrameClass.getQualifiedName(), SWFFrameInfo.EXTERNS_DISALLOWED,
                    Collections.<ICompilationUnit>singleton(currentFrameClassCompilationUnit),
                    Collections.<ICompilerProblem>emptyList());
            frames.addFirst(frameInfo);
            currentFrameClass = currentFrameClass.resolveInheritedFactoryClass(project);
        }

        assert frames.getLast().rootedUnits.contains(rootClassCU) :
            "The main class definition for the last frame, must be the main class definition for the SWF.";
        final FramesInformation explicitFrames = getExplicitFramesInformation();
        return new RoyaleApplicationFramesInformation(frames, explicitFrames, initialFactoryClass, systemManagerFrame, applicationFrame);
    }
    
    @Override
    protected DirectDependencies getDirectDependencies(ICompilationUnit cu) throws InterruptedException
    {
        final DirectDependencies directDependencies =
            super.getDirectDependencies(cu);
        if (!targetSettings.isAccessible())
            return directDependencies;
        final FlexDelegate delegate = getDelegate();
        final DirectDependencies acccessibilityDependencies = 
            delegate.getAccessibilityDependencies(cu);
        return DirectDependencies.concat(directDependencies, acccessibilityDependencies);
    }
    
    /**
     * Discovers dependent compilation units from a set of root compilation
     * units.
     * <p>
     * For each public visible definition in all the compilation units, if
     * there's an applicable CSS rule, check if the CSS rule pulls in any
     * dependencies. (i.e. embedded assets, skin classes) Add the dependencies
     * to the list of compilation units, and check if they have any applicable
     * CSS rules which could pull in more dependencies. Loop until we reach a
     * stable set of compilation units.
     * <p>
     * CSS rules in these CSS documents can introduce class dependencies. If any
     * candidate rule matches a class known to be linked into the target, the
     * candidate rule's dependencies are selected for linking. Those selected
     * dependencies will be included in the next iteration of the dependency
     * discovery loop.
     * <p>
     * Once a CSS document is "activated", it stays in this collection and its
     * rules are tested against all classes introduced in the
     * "dependency discovery loop".
     * <p>
     * For example: Suppose in project P, there are "A.as" and "styles.css", and
     * class "A" is selected for linking.<br>
     * In "styles.css", there're two rules:
     * 
     * <pre>
     * A { xSkin : ClassReference("B"); }
     * K { xSkin : ClassReference("L"); }
     * </pre>
     * 
     * In the 1st iteration, rule "A" is matched, which introduces dependency on
     * "B". <br>
     * "B" is defined in a SWC library "myskins.swc", and there's a
     * "defaults.css" in "myskins.swc".
     * 
     * <pre>
     * B { ySkin : ClassReference("C"); }
     * A { ySkin : ClassReference("D"); }
     * K { ySkin : ClassReference("M"); }
     * </pre>
     * 
     * In the 2nd iteration, rule "A" and rule "B" in "defaults.css" are
     * matched, which introduces dependencies on "C" and "D". However, "K" has
     * not been selected so far, so "L" and "M" are not selected.
     * <p>
     * Now imagine, "C" is defined in "anotherSkin.swc", and there's a
     * "defaults.css" in "anotherSkin.swc" as well.
     * 
     * <pre>
     * C { zSkin : ClassReference("K"); }
     * </pre>
     * 
     * In the 3rd iteration, rule "C" is matched because "C" was selected in the
     * previous iteration, which makes "K" the selected dependency.
     * <p>
     * At the beginning of the 4th iteration, the classes selected for linking
     * are "A", "B", "C", "D" and "K". In this iteration, these classes will be
     * tested against all the "activated style sheets" - "styles.css" and two
     * "defaults.css" in "myskins.swc" and "anotherSkin.swc". "K" rules in
     * "styles.css" and "myskins.swc" are now matched, which introduces new
     * dependencies on "L" and "M".
     * <p>
     * In the 5th iteration, the classes to link are "A", "B", "C", "D", "K",
     * "L" and "M". They are tested against all the activate CSS. No more
     * dependencies are introduced by CSS rules, making it the last iteration of
     * the loop.
     * 
     * @param compilationUnits Collection of compilation units known to be
     * linked in.
     * @param problems Collection of {@link ICompilerProblem}'s that the each
     * found {@link ICompilationUnit} is added to.
     * @return All compilation units which were compiled
     * @throws InterruptedException
     */
    @Override
    protected Set<ICompilationUnit> findAllCompilationUnitsToLink(final Collection<ICompilationUnit> compilationUnits,
            final Collection<ICompilerProblem> problems)
            throws InterruptedException
    {
        
        final FlexDelegate delegate = getDelegate();
        
        if (!delegate.getGenerateSystemManagerAndFlexInit() && !delegate.isFlexInfo(getRootClassDefinition()))
            return super.findAllCompilationUnitsToLink(compilationUnits, problems);
        
        // Performance heuristic: let's start compilation on all of the compilation
        // units we know about up front. This is particularly useful on SWC projects where 
        // we are using "include-sources" to force a bunch of possibly unrelated classes to be
        // compiled.
        // Note that by putting the code here, we will start aggressive early compilation for 
        // all projects. Limited data so far shows this this is a good thing. But down the
        // road it's possible that we might find tests cases that force us to reconsider / refine
        // this "shotgun" approach.
        for (ICompilationUnit cu : compilationUnits)
            cu.startBuildAsync(getTargetType());
        

        assert compilationUnits != null : "compilation units can't be null";
        assert problems != null : "problems can't be null";

        // Collection of all the compilation units that will be linked in the target.
        final Set<ICompilationUnit> allCompilationUnitsInTarget =
                new HashSet<ICompilationUnit>(compilationUnits);

        // Collection of all the referenced CSS. Once a CSS is activated, it's always
        // included in the dependency checking, even none of its rules are matched.
        final ActivatedStyleSheets activatedStyleSheets = new ActivatedStyleSheets();

        final ICSSManager cssManager = royaleProject.getCSSManager();
        
        collectThemes(cssManager, activatedStyleSheets, problems);
        collectDefaultCSS(cssManager, activatedStyleSheets, problems);
        
        // The dependency discovery loop. 
        // It terminates when no more dependencies are introduced by CSS.
        boolean done = false;
        while (!done)
        {
            //LoggingProfiler.onStartIteration();
            
            // Get all non-CSS dependencies.
            final Set<ICompilationUnit> dependencies =
                    getDependentCompilationUnits(allCompilationUnitsInTarget, problems);
            //LoggingProfiler.onCompilationUnitDependenciesChanged(allCompilationUnitsInTarget, dependencies);
            allCompilationUnitsInTarget.addAll(dependencies);

            // Get all activated defaults.css from SWCs.
            final Map<ICSSDocument, File> activatedDefaultCSSList =
                        getAllDefaultCSS(cssManager, allCompilationUnitsInTarget);
            for (final Map.Entry<ICSSDocument, File> entry : activatedDefaultCSSList.entrySet())
            {
                activatedStyleSheets.addLibraryCSS(entry.getKey(), entry.getValue().getAbsolutePath());
            }
            //LoggingProfiler.onDefaultsCSSCollectionChanged(activatedStyleSheets);

            // Get all dependencies introduced by defaults.css from SWCs. 
            final ImmutableList<IDefinition> definitions =
                        Target.getAllExternallyVisibleDefinitions(allCompilationUnitsInTarget);
            final Collection<ICompilationUnit> cssDependencies = new HashSet<ICompilationUnit>();
            for (final ICSSDocument cssDocument : activatedStyleSheets.all())
            {
                // Side-effects of this method:
                // 1. Resolve all type selectors in the CSS model to IClassDefinition definitions.
                // 2. Activate CSS rules whose subject is in the definition set.
                final Collection<ICompilationUnit> dependentCUListFromCSS =
                        cssManager.getDependentCompilationUnitsFromCSS(
                                delegate.cssCompilationSession,
                                cssDocument,
                                definitions,
                                problems);
                cssDependencies.addAll(dependentCUListFromCSS);
                //LoggingProfiler.onCSSDependenciesChanged(dependentCUListFromCSS);
            }

            // If there's more dependencies introduced by CSS, the loop continues.
            done = !allCompilationUnitsInTarget.addAll(cssDependencies);
            if (done)
            {
            	ClassDefinition rootDef = getRootClassDefinition();
	            ICompilationUnit rootClassCompilationUnit = project.getScope().getCompilationUnitForDefinition(rootDef);
	            DependencyGraph graph = royaleProject.getDependencyGraph();
	            for (ICompilationUnit cu : cssDependencies)
	            {
	            	graph.addDependency(rootClassCompilationUnit, cu, DependencyType.EXPRESSION);
	            }
            }
        }

        delegate.cssCompilationSession.cssDocuments.addAll(activatedStyleSheets.sort());
        return allCompilationUnitsInTarget;
    }

    /**
     * Collect CSS from themes.
     */
    private void collectThemes(
            final ICSSManager cssManager,
            final ActivatedStyleSheets activatedStyleSheets,
            final Collection<ICompilerProblem> problems)
    {
        final Collection<ICSSDocument> cssFromThemes = cssManager.getCSSFromThemes(problems);
        for (final ICSSDocument themeCSS : cssFromThemes)
        {
            // Theme files are sorted by declaration order instead of filenames, so we needn't
            // their filenames here.
            activatedStyleSheets.addThemeCSS(themeCSS);
        }
    }
    
    /**
     * Collect CSS from 'defaults-css-files' configuration option.
     */
    private void collectDefaultCSS(
            final ICSSManager cssManager,
            final ActivatedStyleSheets activatedStyleSheets,
            final Collection<ICompilerProblem> problems)
    {
        for (final String defaultsCSSPath : getTargetSettings().getDefaultsCSSFiles())
        {
            final ICSSDocument defaultsCSSModel = cssManager.getCSS(defaultsCSSPath);
            if (defaultsCSSModel == null)
                problems.add(new FileNotFoundProblem(defaultsCSSPath));
            else
                activatedStyleSheets.addDefaultCSS(defaultsCSSModel);
        }
    }
    
    /**
     * 
     * @return true if this SWF is compiled with cross domain or legacy RSLs, false otherwise. 
     */
    private boolean hasRSLs()
    {
        List<RSLSettings> rslSettingsList = targetSettings.getRuntimeSharedLibraryPath();
        
        return rslSettingsList.size() > 0 || targetSettings.getRuntimeSharedLibraries().size() > 0;
    }
    
    @Override
    protected final ITargetReport computeTargetReport() throws InterruptedException
    {
        BuiltCompilationUnitSet builtCompilationUnits = getBuiltCompilationUnitSet();
        FlexRSLInfo rslInfo = getDelegate().getRSLInfo();
        return new TargetReport(project, builtCompilationUnits.compilationUnits, rslInfo.requiredRSLs, 
                getBackgroundColor(), targetSettings, getTargetAttributes(), getLinkageChecker());
    }

    /**
     * Find all the {@link SWCCompilationUnit}'s, and return the default CSS
     * model in the SWCs.
     * 
     * @param cssManager Project-level CSS manager.
     * @param compilationUnits All the compilation units. Non-SWC compilation
     * units are ignored.
     * @return Model of the default CSS in the SWCs. The map keys are CSS
     * models; the values are the enclosing SWC file.
     */
    private static Map<ICSSDocument, File> getAllDefaultCSS(
            final ICSSManager cssManager,
            final Collection<ICompilationUnit> compilationUnits)
    {
        assert cssManager != null : "Expected CSS manager.";
        assert compilationUnits != null : "Expected collection of compilation units.";

        final Map<ICSSDocument, File> result = new HashMap<ICSSDocument, File>();
        for (final ICompilationUnit compilationUnit : compilationUnits)
        {
            if (compilationUnit.getCompilationUnitType() == UnitType.SWC_UNIT)
            {
                final File swcFile = new File(compilationUnit.getAbsoluteFilename());
                final ICSSDocument defaultCSS = cssManager.getDefaultCSS(swcFile);
                if (defaultCSS != null)
                    result.put(defaultCSS, swcFile);
            }
        }
        return result;
    }
    
    @Override
    protected ITargetAttributes computeTargetAttributes() throws InterruptedException
    {
        return delegate.getTargetAttributes();
    }
    
    @Override
    protected Iterable<ICompilerProblem> computeFatalProblems() throws InterruptedException
    {
        final Iterable<ICompilerProblem> fatalProblemsFromSuper = super.computeFatalProblems();
        if (!Iterables.isEmpty(fatalProblemsFromSuper))
            return fatalProblemsFromSuper;
        
        final ICompilationUnit rootClassCompilationUnit = getRootClassCompilationUnit();
        
        Collection<ICompilerProblem> externallyVisibleDefinitionProblems =
            rootClassCompilationUnit.getFileScopeRequest().get().checkExternallyVisibleDefinitions(targetSettings.getRootClassName());
        assert (!externallyVisibleDefinitionProblems.isEmpty()) || checkRootDefinitionConsistency();
        
        return externallyVisibleDefinitionProblems;
    }
    
    @Override
    protected ISWF initializeSWF(List<ICompilationUnit> reachableCompilationUnits) throws InterruptedException
    {
        ISWF swf = super.initializeSWF(reachableCompilationUnits);
        delegate.addProductInfoToSWF(swf);
        
        return swf;
    }

    /**
     * Should only be called from an assert.
     * <p>
     * 
     * @return true if the root class definition can be found and the root class
     * definition is defined by the root {@link ICompilationUnit}, false
     * otherwise.
     */
    private boolean checkRootDefinitionConsistency()
    {
        IDefinition rootClassDefinition = getRootClassDefinition();
        if (rootClassDefinition == null)
            return false;
        ICompilationUnit rootCompilationUnit = getRootClassCompilationUnit();
        ICompilationUnit rootClassCompilationUnit = project.getScope().getCompilationUnitForDefinition(rootClassDefinition);
        return rootCompilationUnit == rootClassCompilationUnit;
    }



    /**
     * Sub-class of {@link FramesInformation} that can create {@link SWFFrame}s
     * for all the frames in a royale application SWF.
     */
    private class RoyaleApplicationFramesInformation extends FramesInformation
    {

        RoyaleApplicationFramesInformation(Iterable<SWFFrameInfo> implicitFrames,
                FramesInformation explicitFrames,
                ClassDefinition initialFactoryClass,
                SWFFrameInfo systemManagerFrame,
                SWFFrameInfo applicationFrame)
        {
            super(Iterables.concat(implicitFrames, explicitFrames.frameInfos));
            assert (systemManagerFrame == null) == (initialFactoryClass == null)
                : "If initial factory class is null, then there should be no system manager frame!";
            assert applicationFrame != null;
            this.initialFactoryClass = initialFactoryClass;
            this.systemManagerFrame = systemManagerFrame;
            this.applicationFrame = applicationFrame;
        }
        
        private final ClassDefinition initialFactoryClass;
        private final SWFFrameInfo systemManagerFrame;
        private final SWFFrameInfo applicationFrame;
        private RoyaleApplicationFrame1Info frame1Info;
        
        /**
         * If we are targeting flex 4.0 or greater and the root application
         * class is written in MXML, then this method will do some additional
         * analysis of the CSS type selectors in the root application class.
         * 
         * @param builtCompilationUnits An {@link ImmutableSet} of all the
         * {@link ICompilationUnit}s built by the {@link RoyaleAppSWFTarget}.
         * @param problems {@link Collection} of {@link ICompilerProblem}s to
         * which {@link ICompilerProblem}s found by this method will be added.
         * @throws InterruptedException
         */
        private void validateRootCompiltionUnitCSS(ImmutableSet<ICompilationUnit> builtCompilationUnits, Collection<ICompilerProblem> problems) throws InterruptedException
        {
            // Validate root MXML's style model if not in Flex 3 mode.
            final Integer compatibilityVersion = royaleProject.getCompatibilityVersion();
            if ((compatibilityVersion != null) && (compatibilityVersion < Configuration.MXML_VERSION_4_0))
                return;
            
            // Only validate root MXML compilation unit because only an application file can have type selectors.
            ICompilationUnit rootCompilationUnit = getRootClassCompilationUnit();
            if (rootCompilationUnit.getCompilationUnitType() != ICompilationUnit.UnitType.MXML_UNIT)
                return;
            final MXMLCompilationUnit rootMXMLCompilationUnit = (MXMLCompilationUnit)rootCompilationUnit;
                
            // Only validate root MXML compilation unit because only an application file can have type selectors.
            final MXMLFileNode mxmlFileNode = (MXMLFileNode)rootMXMLCompilationUnit.getSyntaxTreeRequest().get().getAST();
            CSSSemanticAnalyzer.validate(
                    builtCompilationUnits,
                    mxmlFileNode.getCSSCompilationSession(),
                    problems);
        }
        
        /**
         * {@inheritDoc}
         * <p>
         * Adds generated code to the frame containing a generated system
         * manager and to the frame containing the main application class.
         */
        @Override
        protected void createFrames(SWFTarget swfTarget, ISWF swf, ImmutableSet<ICompilationUnit> builtCompilationUnits, Set<ICompilationUnit> emittedCompilationUnits, Collection<ICompilerProblem> problems) throws InterruptedException
        {
            final FlexDelegate delegate = getDelegate();
            final ClassDefinition rootClassDefinition =
                getRootClassDefinition();
            frame1Info = new RoyaleApplicationFrame1Info(royaleProject,
                                targetSettings,
                                rootClassDefinition,
                                delegate.getGenerateSystemManagerAndFlexInit(),
                                delegate.isFlexInfo(rootClassDefinition),
                                builtCompilationUnits);
            SWFFrame applicationSWFFrame = null;
            for (final SWFFrameInfo frameInfo : frameInfos)
            {
                SWFFrame swfFrame = createFrame(swfTarget, frameInfo, builtCompilationUnits, emittedCompilationUnits, problems);
                if (frameInfo == systemManagerFrame)
                {
                    assert delegate.getGenerateSystemManagerAndFlexInit() : "systemManagerFrame should be null, unless we are generating a system manager.";
                    delegate.addGeneratedSystemManagerToFrame(swfFrame, frame1Info, initialFactoryClass, builtCompilationUnits, problems);
                }
                else if (frameInfo == applicationFrame)
                {
                    delegate.addGeneratedCodeToMainApplicationFrame(swfFrame, frame1Info, emittedCompilationUnits, problems);
                    applicationSWFFrame = swfFrame;
                }
                
                swf.addFrame(swfFrame);
            }
            
            if (delegate.getGenerateSystemManagerAndFlexInit())
            {
                final String generatedSystemManager = 
                    delegate.getGeneratedSystemManagerClassName(initialFactoryClass);
                swf.setTopLevelClass(generatedSystemManager);
                validateRootCompiltionUnitCSS(builtCompilationUnits, problems);
            }
            else
            {
                swf.setTopLevelClass(rootClassDefinition.getQualifiedName());
                
                // if we are not generating a system manager but have RSLs to load, generate
                // a warning.
                if (hasRSLs())
                {
                    final ICompilationUnit rootCompilationUnit = getRootClassCompilationUnit();
                    reportProblem(new MissingFactoryClassInFrameMetadataProblem(rootCompilationUnit.getAbsoluteFilename()));
                }
            }
            
            assert applicationSWFFrame != null;
            // Classes only reachable from CSS will end up in this
            // set.
            Set<ICompilationUnit> remainingCompilationUnitsToEmit =
                Sets.difference(builtCompilationUnits, emittedCompilationUnits);
            if (!remainingCompilationUnitsToEmit.isEmpty())
                addCompilationUnitsAndDependenciesToFrame(applicationSWFFrame, remainingCompilationUnitsToEmit,
                    true, emittedCompilationUnits);
        }
    }
    
    /**
     * Sub-class of {@link RoyaleTarget} that adds logic specific to building flex
     * applications.
     */
    private class FlexDelegate extends RoyaleTarget
    {
        FlexDelegate(IClassDefinition mainApplicationClassDefinition, ITargetSettings targetSettings, RoyaleProject project)
        {
            super(targetSettings, project);
            
            this.mainApplicationClassDefinition = mainApplicationClassDefinition;
            objectReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), "", IASLanguageConstants.Object, false);
            generateCSSStyleDeclarationsReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), "flex.compiler.support.generateCSSStyleDeclarations");
            iModuleFactoryReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), IMXMLTypeConstants.IFlexModuleFactory);
            childManagerReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), IMXMLTypeConstants.ChildManager);
            styleManagerImplReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), IMXMLTypeConstants.StyleManagerImpl);
            effectManagerReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), IMXMLTypeConstants.EffectManager);
            mxInternalReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), IMXMLTypeConstants.mx_internal);
            getClassByAliasReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), IASLanguageConstants.getClassByAlias);
            registerClassAliasReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), IASLanguageConstants.registerClassAlias);
            crossDomainRSLItemReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), IMXMLTypeConstants.CrossDomainRSLItem);
            royaleVersionReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), IMXMLTypeConstants.RoyaleVersion);
            capabilitiesReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), IASLanguageConstants.Capabilities);
            textFieldFactoryReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), IMXMLTypeConstants.TextFieldFactory);
            iSWFContextReference = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), IMXMLTypeConstants.ISWFContext);
            
            this.cssCompilationSession = new CSSCompilationSession();
            this.cssCompilationSession.setKeepAllTypeSelectors(targetSettings.keepAllTypeSelectors());
        }
        
        private final IClassDefinition mainApplicationClassDefinition;
        private final IResolvedQualifiersReference objectReference;
        private final IResolvedQualifiersReference generateCSSStyleDeclarationsReference;
        private final IResolvedQualifiersReference iModuleFactoryReference;
        private final IResolvedQualifiersReference childManagerReference;
        private final IResolvedQualifiersReference styleManagerImplReference;
        private final IResolvedQualifiersReference effectManagerReference;
        private final IResolvedQualifiersReference mxInternalReference;
        private final IResolvedQualifiersReference getClassByAliasReference;
        private final IResolvedQualifiersReference registerClassAliasReference;
        private final IResolvedQualifiersReference crossDomainRSLItemReference;
        private final IResolvedQualifiersReference royaleVersionReference;
        private final IResolvedQualifiersReference capabilitiesReference;
        private final IResolvedQualifiersReference textFieldFactoryReference;
        private final IResolvedQualifiersReference iSWFContextReference;
        
        private final CSSCompilationSession cssCompilationSession;
        
        /**
         * Cached boolean for whether or not the system manager
         * and royale init classes should be generated.
         */
        private Boolean generateSystemManagerAndFlexInit;
        
        /**
         * Cached information about the splash screen image.
         */
        private FlexSplashScreenImage splashScreenImage;
        
        /**
         *  From preloader attribute or configuration option. Defaults to 
         *  "mx.preloaders.SparkDownloadProgressBar" unless the 
         *  compatibility-version is less than 4.0.
         */
        private IResolvedQualifiersReference preloaderReference;
        
        private ITargetAttributes targetAttributes;
        
        /**
         * Cached root syntax tree node for the root class.
         */
        private IASNode rootNode;
        
        /**
         * Cached frame 1 information.
         */
        private RoyaleApplicationFrame1Info frame1Info;
        
        /**
         * Cached RSL information
         * 
         */
        private FlexRSLInfo rslInfo;
        

        private String getMainClassQName()
        {
            Name mainApplicationName = ((DefinitionBase)mainApplicationClassDefinition).getMName(royaleProject);
            String mainApplicationPackageName = Iterables.getFirst(mainApplicationName.getQualifiers(), null).getName();
            if (mainApplicationPackageName.length() != 0)
                mainApplicationPackageName = mainApplicationPackageName + ".";
            return mainApplicationPackageName + mainApplicationName.getBaseName();
        }
        
        private String getFlexInitClassName()
        {
            String royaleInitClassName = "_" + getMainClassQName() + "_FlexInit";
            royaleInitClassName = royaleInitClassName.replaceAll("[^a-zA-Z0-9]", "_");
            return royaleInitClassName;
        }
        
        private String getStylesClassName()
        {
            String royaleInitClassName = "_" + getMainClassQName() + "_Styles";
            royaleInitClassName = royaleInitClassName.replaceAll("[^a-zA-Z0-9]", "_");
            return royaleInitClassName;
        }
        
        private String getGeneratedSystemManagerClassName(ClassDefinition systemManagerClass)
        {
            String generatorSystemManagerName = "_" + getMainClassQName() + "_" + systemManagerClass.getQualifiedName();
            generatorSystemManagerName = generatorSystemManagerName.replaceAll("[^a-zA-Z0-9]", "_");
            return generatorSystemManagerName;
        }
        
        private boolean addGeneratedStylesClassToFrame(SWFFrame frame, Set<ICompilationUnit> emittedCompilationUnits) throws Exception
        {
            ABCEmitter emitter = new ABCEmitter();
            emitter.visit(ABCConstants.VERSION_ABC_MAJOR_FP10, ABCConstants.VERSION_ABC_MINOR_FP10);
            ICSSCodeGenResult cssCodeGenResult = cssCompilationSession.emitStyleDataClass(royaleProject, emitter);

            Name stylesClassName = new Name(getStylesClassName());

            IDefinition objectDef = objectReference.resolve(royaleProject);
            if ((objectDef == null) || (!(objectDef instanceof ClassDefinition)))
                return false;

            ClassDefinition objectClassDef = (ClassDefinition)objectDef;

            // Generates a Style's class
            // Generated class name will be of the form _MyApplication_Styles
            // Eg:
            // public class _MyApplication_Styles
            // {
            // }

            final InstructionList cinit = cssCodeGenResult.getClassInitializationInstructions();
            assert cinit.canFallThrough() : "CSSReducer should not append 'returnvoid' to the initialization instructions.";
            cinit.addInstruction(ABCConstants.OP_returnvoid);

            ClassGeneratorHelper classGenerator = new ClassGeneratorHelper(
                        royaleProject,
                        emitter,
                        stylesClassName,
                        objectClassDef,
                        Collections.<Name> emptyList(),
                        Collections.<Name> emptyList(),
                        ClassGeneratorHelper.returnVoid(),
                        cinit,
                        false);
            cssCodeGenResult.visitClassTraits(classGenerator.getCTraitsVisitor());

            classGenerator.finishScript();

            DoABCTag tag = new DoABCTag();
            tag.setABCData(emitter.emit());
            tag.setName("defaults.css and theme CSS data");
            frame.addTag(tag);

            return true;
        }
        
        private boolean computeGenerateSystemManagerAndFlexInit()
        {
            ClassDefinition rootClassDef = (ClassDefinition)mainApplicationClassDefinition;
            ClassDefinition rootFactoryClass = rootClassDef.resolveInheritedFactoryClass(royaleProject);
            generateSystemManagerAndFlexInit =
                (rootFactoryClass != null) && (!rootClassDef.hasOwnFactoryClass(royaleProject.getWorkspace()));

            return generateSystemManagerAndFlexInit;
        }
        
        public boolean isFlexInfo(ClassDefinition rootClassDef)
        {
            ClassDefinition superClass = (ClassDefinition)rootClassDef.resolveBaseClass(royaleProject);
            while (superClass != null && !superClass.getBaseName().equals(IASLanguageConstants.Object))
            {
                String impls[] = superClass.getImplementedInterfacesAsDisplayStrings();
                for (String impl : impls)
                {
                    if (impl.contains(".IFlexInfo"))
                    {
                        return true;
                    }
                }
                superClass = (ClassDefinition)superClass.resolveBaseClass(royaleProject);
            }
            return false;
        }
        
        private boolean getGenerateSystemManagerAndFlexInit()
        {
            if (generateSystemManagerAndFlexInit != null)
                return generateSystemManagerAndFlexInit;
            generateSystemManagerAndFlexInit = computeGenerateSystemManagerAndFlexInit();
            return generateSystemManagerAndFlexInit;
        }
        
        /**
         * Parses a "@" function expression and return's the name of the referenced
         * "@" function.
         * <p>
         * For example, an input of {@code @Embed("foo.jpg")} yields {@code Embed}.
         * <p>
         * If the input string is not an "@" function expression, this method
         * returns null.
         * 
         * @param value The string that possibly contains an "@" function
         * expression.
         * @return The function name of the referenced "@" function, or null if the
         * input is not an "@" function expression.
         */
        private String getAtFunctionName(String value)
        {
            value = value.trim();
            if (value.length() > 1 && value.charAt(0) == '@')
            {
                int openParen = value.indexOf('(');

                // A function must have an open paren and a close paren after the open paren.
                if (openParen > 1 && value.indexOf(')') > openParen)
                    return value.substring(1, openParen);
            }
            return null;
        }
        
        /**
         * Get the embedded compilation unit for a given attribute name.
         * 
         * @param attributeName name of attribute to find embed for.
         * @return Compilation unit, may be null.
         * @throws InterruptedException 
         */
        private EmbedCompilationUnit getEmbeddedCompilationUnit(String attributeName) throws InterruptedException
        {
            if (attributeName == null)
                throw new NullPointerException();
            
            ICompilationUnit splashUnit = null;
            IASNode asNode = getRootNode();
            
            if (!(asNode instanceof IMXMLFileNode))
                return null;
            
            IMXMLFileNode mxmlNode = (IMXMLFileNode)asNode;
            List<IEmbedResolver> embedNodes = mxmlNode.getEmbedNodes();
            for (IEmbedResolver node : embedNodes)
            {
                
                if (node instanceof IMXMLEmbedNode)
                {
                    IMXMLEmbedNode embedNode = (IMXMLEmbedNode)node;
                    IASNode parent = embedNode.getParent();
                    if (parent instanceof IMXMLPropertySpecifierNode)
                    {
                        IMXMLPropertySpecifierNode propertyNode = (IMXMLPropertySpecifierNode)parent;
                        IDefinition propertyDefinition = propertyNode.getDefinition();
                        if (propertyDefinition instanceof ISetterDefinition)
                        {
                            ISetterDefinition setter = (ISetterDefinition)propertyDefinition;
                            if (attributeName.equals(setter.getBaseName()))
                            {
                                Collection<ICompilerProblem> problems = null;
                                splashUnit = node.resolveCompilationUnit(royaleProject, problems);
                                break;
                            }
                        }
                    }
                }
            }

            if (splashUnit instanceof EmbedCompilationUnit)
                return (EmbedCompilationUnit)splashUnit;
            
            return null;    // not found
        }
        
        private FlexSplashScreenImage compuateSplashScreenImage() throws InterruptedException
        {
            final String splashScreenImageValue = getTargetAttributes().getSplashScreenImage();
            
            // Check if value is an @Embed or a class name.
            if (splashScreenImageValue == null)
                return new FlexSplashScreenImage(null, null);
            
            String className = null;      // name of class to get reference for
            
            EmbedCompilationUnit splashScreenImageEmbedUnit = null;
            String functionName = getAtFunctionName(splashScreenImageValue);
            IResolvedQualifiersReference splashScreenImageReference = null;
            if ("Embed".equals(functionName))
            {
                splashScreenImageEmbedUnit = getEmbeddedCompilationUnit(ATTRIBUTE_SPLASH_SCREEN_IMAGE);
                className = splashScreenImageEmbedUnit.getName();
                assert className != null;
            }
            else 
            {
                // class name
                className = splashScreenImageValue;
            }
            
            if (className != null)
                splashScreenImageReference = ReferenceFactory.packageQualifiedReference(royaleProject.getWorkspace(), className);
            return new FlexSplashScreenImage(splashScreenImageEmbedUnit, splashScreenImageReference);
        }
        
        /**
         * Resolve the splash screen image as a reference. The image may be a class reference or
         * an embed compilation unit.
         * @throws InterruptedException 
         */
        private FlexSplashScreenImage getSplashScreenImage() throws InterruptedException
        {
            if (splashScreenImage != null)
                return splashScreenImage;
            splashScreenImage = compuateSplashScreenImage();
            return splashScreenImage;
        }
        
 
        private ITargetAttributes computeTargetAttributes() throws InterruptedException
        {
            IASNode root = getRootNode();
            if (!(root instanceof IFileNode))
                return NilTargetAttributes.INSTANCE;

            final ITargetAttributes nodeTargetAttributes = ((IFileNode)root).getTargetAttributes(royaleProject);
            if (nodeTargetAttributes == null)
                return NilTargetAttributes.INSTANCE;
            return nodeTargetAttributes;
        }
        

        private ITargetAttributes getTargetAttributes() throws InterruptedException
        {
            if (targetAttributes != null)
                return targetAttributes;
            targetAttributes = computeTargetAttributes();
            return targetAttributes;
        }
        
        /**
         * 
         * @return the root node of a document. Will be null if the document has a
         *  factory class.
         * @throws InterruptedException 
         */
        protected IASNode getRootNode() throws InterruptedException
        {
            if (rootNode != null)
                return rootNode;

            ASProjectScope projectScope = royaleProject.getScope();
            
            ClassDefinition mainClassDef = (ClassDefinition) mainApplicationClassDefinition;
            if (mainClassDef.hasOwnFactoryClass(royaleProject.getWorkspace()))
                return null;

            ICompilationUnit mainUnit = projectScope.getCompilationUnitForDefinition(mainClassDef);
            IRequest<ISyntaxTreeRequestResult, ICompilationUnit> request = mainUnit.getSyntaxTreeRequest();
            ISyntaxTreeRequestResult result = request.get();

            rootNode = result.getAST();
            return rootNode;
        }
        
        final RoyaleApplicationFrame1Info getFrame1Info() throws InterruptedException
        {
            if (frame1Info != null)
                return frame1Info;
            frame1Info = new RoyaleApplicationFrame1Info(royaleProject,
                    targetSettings, mainApplicationClassDefinition, getGenerateSystemManagerAndFlexInit(), 
                    isFlexInfo((ClassDefinition)mainApplicationClassDefinition), getBuiltCompilationUnitSet().compilationUnits);
            return frame1Info;
        }
        
        final FlexRSLInfo getRSLInfo() throws InterruptedException
        {
            if (rslInfo != null)
                return rslInfo;
            
            rslInfo = new FlexRSLInfo(getFrame1Info(), royaleProject, targetSettings);
            return rslInfo;  
        }
        
        /**
         * Adds generated classes to the specified {@link SWFFrame}.
         * 
         * @param mainApplicationFrame {@link SWFFrame} that contains the main
         * application class of the royale application SWF being built.
         * @param frame1Info {@link RoyaleApplicationFrame1Info} containing frame
         * 1 related code generation information collected from all the
         * {@link ICompilationUnit}s being built into the royale application SWF.
         * @param emittedCompilationUnits
         * @param problems
         * @return true if code was generated and added to the specified
         * {@link SWFFrame}, false otherwise.
         * @throws InterruptedException
         */
        boolean addGeneratedCodeToMainApplicationFrame(SWFFrame mainApplicationFrame, RoyaleApplicationFrame1Info frame1Info,
                Set<ICompilationUnit> emittedCompilationUnits,
                Collection<ICompilerProblem> problems) throws InterruptedException
        {
            boolean isAppFlexInfo = isFlexInfo((ClassDefinition)mainApplicationClassDefinition);
            if (getGenerateSystemManagerAndFlexInit() || isAppFlexInfo)
            {
                try
                {
                    if (!addGeneratedStylesClassToFrame(mainApplicationFrame, emittedCompilationUnits))
                        return false;
                }
                catch (Exception e)
                {
                    final CSSCodeGenProblem problem = new CSSCodeGenProblem(e);
                    problems.add(problem);
                }
                FlexRSLInfo rslInfo = getDelegate().getRSLInfo();
                addGeneratedFlexInitToFrame(problems, mainApplicationFrame, emittedCompilationUnits, 
                        isAppFlexInfo, frame1Info, rslInfo);
            }
            else
            {
                //Generate _CompiledResourceBundleInfo class that holds the information 
                //about compiled locale(s) and resource bundle(s) if no SystemManager is
                //generated since we need a class to hold this information.
                addGeneratedCompiledResourceBundleInfoToFrame(frame1Info, mainApplicationFrame);
            }
            
            return true;
        }
        
        /**
         * Generates _CompiledResourceBundleInfo class that holds the information 
         * about the compiled locale(s) and resource bundle name(s). This class is only 
         * generated if we don't generate a SystemManager class. There is a logic in 
         * Flex SDK that looks up this class if it cannot find a generated SystemManager 
         * class in order to retrieve compiled locale(s) and resource bundle(s). 
         * Generated code looks like this:
         * 
         * package
         * {
         *     public class _CompiledResourceBundleInfo
         *     {
         *         public static function compiledLocales() : Array
         *         {
         *              return ["en_US","es_ES",...]; 
         *         }
         *         
         *         public static function compiledResourceBundleNames() : Array
         *         {
         *              return ["core","collections",... ];
         *         }
         *     }
         * }
         * 
         * @param frame frame to add the generated DoABC for this class.
         */
        private void addGeneratedCompiledResourceBundleInfoToFrame(RoyaleApplicationFrame1Info frame1Info, SWFFrame frame)
        {
            Collection<String> locales = royaleProject.getLocales();

            if (locales.size() == 0 || frame1Info.compiledResourceBundleNames.size() == 0)
            {
                //No need to create this class, so return from this method
                return;
            }

            String className = IMXMLTypeConstants._CompiledResourceBundleInfo;       
            IResolvedQualifiersReference mainClassRef = ReferenceFactory.packageQualifiedReference(
                     royaleProject.getWorkspace(), className);

            ABCEmitter emitter = new ABCEmitter();
            emitter.visit(ABCConstants.VERSION_ABC_MAJOR_FP10, ABCConstants.VERSION_ABC_MINOR_FP10);

            ClassGeneratorHelper classGen = new ClassGeneratorHelper(royaleProject, emitter,
                     mainClassRef.getMName(),
                     (ClassDefinition)royaleProject.getBuiltinType(BuiltinType.OBJECT),
                     ClassGeneratorHelper.returnVoid());

            //Create method body for compiledLocales getter
            InstructionList localesInstructionList = new InstructionList();

            for (String locale : locales)
            {
                localesInstructionList.addInstruction(ABCConstants.OP_pushstring, locale);
            }

            localesInstructionList.addInstruction(ABCConstants.OP_newarray, locales.size());
            localesInstructionList.addInstruction(ABCConstants.OP_returnvalue);

            classGen.addCTraitsGetter(new Name("compiledLocales"), 
                    new Name(IASLanguageConstants.Array), localesInstructionList);

            //Create method body for compiledLocales getter
            InstructionList bundlesInstructionList = new InstructionList();

            for (String bundleName : frame1Info.compiledResourceBundleNames)
            {
                bundlesInstructionList.addInstruction(ABCConstants.OP_pushstring, bundleName);
            }

            bundlesInstructionList.addInstruction(ABCConstants.OP_newarray, frame1Info.compiledResourceBundleNames.size());
            bundlesInstructionList.addInstruction(ABCConstants.OP_returnvalue);

            classGen.addCTraitsGetter(new Name("compiledResourceBundleNames"), 
                    new Name(IASLanguageConstants.Array), bundlesInstructionList);       
            
            //Generate script       
            classGen.finishScript();
            
            DoABCTag doABC = new DoABCTag();
            try
            {
                doABC.setABCData(emitter.emit());
            }
            catch (Exception e)
            {
                return;
            }
            
            doABC.setName(className);
            frame.addTag(doABC);
        }
        
        /**
         * Generates the royale initializer class and adds it to the specified
         * {@link SWFFrame}.
         * 
         * @param problemCollection {@link Collection} that any
         * {@link ICompilerProblem}s found during class generation will be added
         * to.
         * @param frame {@link SWFFrame} the generated class will be added to.
         * @param emittedCompilationUnits
         * @return true if the royale init class was successfully generated and
         * added to the speciifed {@link SWFFrame}.
         * @throws InterruptedException
         */
        private boolean addGeneratedFlexInitToFrame(final Collection<ICompilerProblem> problems, SWFFrame frame, Set<ICompilationUnit> emittedCompilationUnits, 
                boolean isAppFlexInfo, RoyaleApplicationFrame1Info frame1Info, FlexRSLInfo rslInfo) throws InterruptedException
        {
            ABCEmitter emitter = new ABCEmitter();
            emitter.visit(ABCConstants.VERSION_ABC_MAJOR_FP10, ABCConstants.VERSION_ABC_MINOR_FP10);
            
            
            String royaleInitClassNameString = getFlexInitClassName();
            
            Name royaleInitClassName = new Name(royaleInitClassNameString);
            
            Name stylesClassName = new Name(getStylesClassName());

            IDefinition objectDef = objectReference.resolve(royaleProject);
            if ((objectDef == null) || (!(objectDef instanceof ClassDefinition)))
                return false;
            ClassDefinition objectClassDef = (ClassDefinition)objectDef;
            
            Map<String, String> effectNameToTriggerMap = new TreeMap<String, String>();
            Map<String, Boolean> inheritingStyleMap = new TreeMap<String, Boolean>();            
            Map<ClassDefinition, String> remoteClassAliasMap =
                new TreeMap<ClassDefinition, String>(new Comparator<ClassDefinition>()
                {
                    @Override
                    public int compare(ClassDefinition o1, ClassDefinition o2)
                    {
                        return o1.getQualifiedName().compareTo(o2.getQualifiedName());
                    } 
                })
                {
                    private static final long serialVersionUID = 1L;

                    /**
                     *  Override so warning messages can be logged. 
                     */
                    @Override
                    public String put(ClassDefinition key, String value)
                    {
                        // check for duplicate values and log a warning if any remote 
                        // classes try to use the same alias.
                        if (containsValue(value))
                        {
                           for (Map.Entry<ClassDefinition,String> entry  : entrySet())
                           {
                               if (value != null && value.equals(entry.getValue()))
                               {
                                   problems.add(new ClassesMappedToSameRemoteAliasProblem(key.getQualifiedName(),
                                           entry.getKey().getQualifiedName(), value));
                                   break;
                               }
                           }
                        }
                        return super.put(key, value);
                    }
                };
                
            for (ICompilationUnit cu : emittedCompilationUnits)
            {
                Collection<IDefinition> visibleDefs = cu.getFileScopeRequest().get().getExternallyVisibleDefinitions();
                for (IDefinition visibleDef : visibleDefs)
                {
                    if (visibleDef instanceof ClassDefinition)
                    {
                        ClassDefinition visibleClass = (ClassDefinition) visibleDef;
                        IEffectDefinition[] effectDefinitions = visibleClass.getEffectDefinitions(royaleProject.getWorkspace());
                        for (IEffectDefinition effectDefinition : effectDefinitions)
                        {
                            // TODO create compiler problem if effect already has a trigger.
                            effectNameToTriggerMap.put(effectDefinition.getBaseName(), effectDefinition.getEvent());
                        }
                        
                        IStyleDefinition[] styleDefinitions = visibleClass.getStyleDefinitions(royaleProject.getWorkspace());
                        for (IStyleDefinition styleDefinition : styleDefinitions)
                        {
                            boolean isInheriting = styleDefinition.isInheriting();
                            // TODO create compiler problem if style definitions conflict
                            inheritingStyleMap.put(styleDefinition.getBaseName(), isInheriting);
                        }
                        
                        String remoteClassAlias = visibleClass.getRemoteClassAlias();
                        if (remoteClassAlias != null)
                            remoteClassAliasMap.put(visibleClass, remoteClassAlias);
                    }
                }
            }

            // Generate code for the constructor:
            // public function ClassName()
            // {
            //    super();
            // }
            InstructionList classITraitsInit = new InstructionList();
            classITraitsInit.addInstruction(ABCConstants.OP_getlocal0);
            classITraitsInit.addInstruction(ABCConstants.OP_constructsuper, 0);
            classITraitsInit.addInstruction(ABCConstants.OP_returnvoid);
            ClassGeneratorHelper classGen = new ClassGeneratorHelper(royaleProject, emitter, royaleInitClassName, 
                    objectClassDef, Collections.<Name>emptyList(), classITraitsInit);
            
            // Generate code for the static init method:
            // public static function init(mf : IFlexModuleFactory) : void
            // {
            //    new ChildManager(mf);
            //    var local2 : * = new StyleManagerImpl(mf);
            //
            //    // For each effect declared in the application:
            //    EffectManager.mx_internal::registerEffectTrigger(<effectName>, <eventName>);
            //
            //    // For each remote class alias declared in the application
            //    try 
            //    { 
            //        if (flash.net.getClassByAlias(<remote class alias>) != <class>) 
            //        { 
            //            flash.net.registerClassAlias(<remote class alias>, <class>); 
            //        } 
            //    } 
            //    catch (e:Error) 
            //    { 
            //        flash.net.registerClassAlias(<remote class alias>, <class>); 
            //    }
            //
            //    var local3 : * = [<names of all inheriting styles declared in the application>];
            //
            //    for each (var local0 : * in local3)
            //    {
            //        local2.registerInheritingStyle(local0);  // local2 is the style manager.
            //    }
            // }
            if (isAppFlexInfo)
            {
                MethodInfo initMethodInfo = new MethodInfo();
                initMethodInfo.setMethodName("FlexInit init method");
                initMethodInfo.setParamTypes(new Vector<Name>(Collections.singleton(new Name("Object"))));
                initMethodInfo.setReturnType(new Name(IASLanguageConstants.void_));
                IMethodVisitor initMethodVisitor = emitter.visitMethod(initMethodInfo);
                initMethodVisitor.visit();
                MethodBodyInfo initMethodBodyInfo = new MethodBodyInfo();
                initMethodBodyInfo.setMethodInfo(initMethodInfo);
                IMethodBodyVisitor initMethodBodyVisitor = initMethodVisitor.visitBody(initMethodBodyInfo);
                initMethodBodyVisitor.visit();
                
                // local0 = temp
                // local1 = module factory argument
                // local2 = style manager
                // local3 = inherited styles array
                InstructionList initMethod = new InstructionList();
                initMethod.addInstruction(ABCConstants.OP_returnvoid);
                
                initMethodBodyVisitor.visitInstructionList(initMethod);
                initMethodBodyVisitor.visitEnd();
                initMethodVisitor.visitEnd();
                
                ITraitVisitor initMethodTraitVisitor = 
                    classGen.getCTraitsVisitor().visitMethodTrait(ABCConstants.TRAIT_Method, new Name("init"), 0, initMethodInfo);
                initMethodTraitVisitor.visitStart();
                initMethodTraitVisitor.visitEnd();

                codegenInfoMethod(classGen, 
                        royaleProject.getCompatibilityVersion(),
                        getMainClassQName(),
                        getPreloaderClassReference(),
                        getRuntimeDPIProviderClassReference(),
                        splashScreenImage,
                        getRootNode(),
                        getTargetAttributes(),
                        royaleProject.getLocales(),
                        frame1Info,
                        accessibleClassNames,
                        getFlexInitClassName(),
                        getStylesClassName(),
                        targetSettings.getRuntimeSharedLibraries(),
                        rslInfo,
                        problems,
                        isAppFlexInfo,
                        isFlexSDKInfo,
                        remoteClassAliasMap);
                
            }
            else
            {
                MethodInfo initMethodInfo = new MethodInfo();
                initMethodInfo.setMethodName("FlexInit init method");
                initMethodInfo.setParamTypes(new Vector<Name>(Collections.singleton(iModuleFactoryReference.getMName())));
                initMethodInfo.setReturnType(new Name(IASLanguageConstants.void_));
                IMethodVisitor initMethodVisitor = emitter.visitMethod(initMethodInfo);
                initMethodVisitor.visit();
                MethodBodyInfo initMethodBodyInfo = new MethodBodyInfo();
                initMethodBodyInfo.setMethodInfo(initMethodInfo);
                IMethodBodyVisitor initMethodBodyVisitor = initMethodVisitor.visitBody(initMethodBodyInfo);
                initMethodBodyVisitor.visit();
                
                // local0 = temp
                // local1 = module factory argument
                // local2 = style manager
                // local3 = inherited styles array
                InstructionList initMethod = new InstructionList();
                
                // Since we don't need "this", we can kill local0, we'll use it later for something else.
                initMethod.addInstruction(ABCConstants.OP_kill, 0);
                initMethod.addInstruction(ABCConstants.OP_finddef, childManagerReference.getMName());
                initMethod.addInstruction(ABCConstants.OP_getlocal1);
                initMethod.addInstruction(ABCConstants.OP_constructprop, new Object[] { childManagerReference.getMName(), 1 });
                initMethod.addInstruction(ABCConstants.OP_pop);
                initMethod.addInstruction(ABCConstants.OP_finddef, styleManagerImplReference.getMName());
                initMethod.addInstruction(ABCConstants.OP_getlocal1);
                initMethod.addInstruction(ABCConstants.OP_constructprop, new Object[] { styleManagerImplReference.getMName(), 1 });
                initMethod.addInstruction(ABCConstants.OP_setlocal2);
                
                // register effects
                if (!effectNameToTriggerMap.isEmpty())
                {
                    IDefinition mxInternalDef = mxInternalReference.resolve(royaleProject);
                    if (!(mxInternalDef instanceof NamespaceDefinition))
                        return false;
                    
                    
                    IResolvedQualifiersReference registerEffectTriggerRef =
                        ReferenceFactory.resolvedQualifierQualifiedReference(royaleProject.getWorkspace(), (INamespaceDefinition)mxInternalDef, 
                                "registerEffectTrigger");
                    Name registerEffectTriggerName = registerEffectTriggerRef.getMName();
                    
                    initMethod.addInstruction(ABCConstants.OP_getlex, effectManagerReference.getMName());
                    
                    for (Map.Entry<String, String> effectEntry : effectNameToTriggerMap.entrySet())
                    {
                        initMethod.addInstruction(ABCConstants.OP_dup);  // copy the effectManager class closure
                        initMethod.addInstruction(ABCConstants.OP_pushstring, effectEntry.getKey());
                        initMethod.addInstruction(ABCConstants.OP_pushstring, effectEntry.getValue());
                        initMethod.addInstruction(ABCConstants.OP_callpropvoid, new Object[] { registerEffectTriggerName, 2 });
                    }
                    initMethod.addInstruction(ABCConstants.OP_pop);
                    
                }
                
                // Initialize AccessibilityClasses. Below is example code. Each
                // accessibility class found by the compiler will have its
                // enableAccessibility() method called.
                // 
                // if (Capabilities.hasAccessibility) {
                //    spark.accessibility.TextBaseAccImpl.enableAccessibility();
                //    mx.accessibility.UIComponentAccProps.enableAccessibility();
                //    spark.accessibility.ButtonBaseAccImpl.enableAccessibility();
                // }
                if (targetSettings.isAccessible())
                {
                    Name capabilitiesSlotName = capabilitiesReference.getMName();
                    initMethod.addInstruction(ABCConstants.OP_findpropstrict, capabilitiesSlotName);
                    initMethod.addInstruction(ABCConstants.OP_getproperty, capabilitiesSlotName);
                    initMethod.addInstruction(ABCConstants.OP_getproperty, new Name("hasAccessibility"));
                    Label accessibilityEnd = new Label();
                    initMethod.addInstruction(ABCConstants.OP_iffalse, accessibilityEnd);
    
                    IResolvedQualifiersReference enableAccessibilityReference = ReferenceFactory.packageQualifiedReference(royaleProject.getWorkspace(),
                            "enableAccessibility");            
                    Name enableAccessibilityName = enableAccessibilityReference.getMName();
                    Object[] enableAccessibilityCallPropOperands = new Object[] { enableAccessibilityName, 0 };
                    for (String accessibilityClassName : accessibleClassNames)
                    {
                        IResolvedQualifiersReference ref = ReferenceFactory.packageQualifiedReference(royaleProject.getWorkspace(),
                                accessibilityClassName);
                        Name accName = ref.getMName();
                        initMethod.addInstruction(ABCConstants.OP_getlex, accName);
                        initMethod.addInstruction(ABCConstants.OP_callproperty, enableAccessibilityCallPropOperands);
                        initMethod.addInstruction(ABCConstants.OP_pop);
                    }
    
                    initMethod.labelNext(accessibilityEnd);
                }
                
                // register class aliases
                if (!remoteClassAliasMap.isEmpty())
                {
                    Name getClassByAliasName = getClassByAliasReference.getMName();
                    Name registerClassAliasName = registerClassAliasReference.getMName();
                    Object[] getClassByAliasCallPropOperands = new Object[] { getClassByAliasName, 1 };
                    Object [] registerClassAliasCallPropOperands = new Object[] { registerClassAliasName, 2 };
                    for (Map.Entry<ClassDefinition, String> classAliasEntry : remoteClassAliasMap.entrySet())
                    {
                        Label tryLabel = new Label();
                        initMethod.labelNext(tryLabel);
                        initMethod.addInstruction(ABCConstants.OP_finddef, getClassByAliasName);
                        initMethod.addInstruction(ABCConstants.OP_pushstring, classAliasEntry.getValue());
                        initMethod.addInstruction(ABCConstants.OP_callproperty, getClassByAliasCallPropOperands);
                        Name classMName = classAliasEntry.getKey().getMName(royaleProject);
                        initMethod.addInstruction(ABCConstants.OP_getlex, classMName);
                        Label endTryLabel = new Label();
                        initMethod.addInstruction(ABCConstants.OP_ifeq, endTryLabel);
                        initMethod.addInstruction(ABCConstants.OP_finddef, registerClassAliasName);
                        initMethod.addInstruction(ABCConstants.OP_pushstring, classAliasEntry.getValue());
                        initMethod.addInstruction(ABCConstants.OP_getlex, classMName);
                        initMethod.addInstruction(ABCConstants.OP_callpropvoid, registerClassAliasCallPropOperands);
                        initMethod.labelNext(endTryLabel);
                        Label afterCatch = new Label();
                        initMethod.addInstruction(ABCConstants.OP_jump, afterCatch);
                        Label catchLabel = new Label();
                        initMethod.labelNext(catchLabel);
                        initMethod.addInstruction(ABCConstants.OP_pop);
                        initMethod.addInstruction(ABCConstants.OP_finddef, registerClassAliasName);
                        initMethod.addInstruction(ABCConstants.OP_pushstring, classAliasEntry.getValue());
                        initMethod.addInstruction(ABCConstants.OP_getlex, classMName);
                        initMethod.addInstruction(ABCConstants.OP_callpropvoid, registerClassAliasCallPropOperands);
                        initMethod.labelNext(afterCatch);
                        initMethodBodyVisitor.visitException(tryLabel, endTryLabel, catchLabel, 
                                new Name(IASLanguageConstants.Error), null);
                    }
                }
                
                // register inheriting styles
                if (!inheritingStyleMap.isEmpty())
                {
                    initMethod.addInstruction(ABCConstants.OP_getlex, stylesClassName);
                    int count = 0;
                    for (Map.Entry<String, Boolean> styleEntry : inheritingStyleMap.entrySet())
                    {
                        if (styleEntry.getValue().booleanValue())
                        {
                            ++count;
                            initMethod.addInstruction(ABCConstants.OP_pushstring, styleEntry.getKey());
                        }
                    }
                    
                    initMethod.addInstruction(ABCConstants.OP_newarray, count);
                    initMethod.addInstruction(ABCConstants.OP_setproperty, new Name("inheritingStyles"));
    
                }
    
                initMethod.addInstruction(ABCConstants.OP_returnvoid);
                
                initMethodBodyVisitor.visitInstructionList(initMethod);
                initMethodBodyVisitor.visitEnd();
                initMethodVisitor.visitEnd();
                
                ITraitVisitor initMethodTraitVisitor = 
                    classGen.getCTraitsVisitor().visitMethodTrait(ABCConstants.TRAIT_Method, new Name("init"), 0, initMethodInfo);
                initMethodTraitVisitor.visitStart();
                initMethodTraitVisitor.visitEnd();
            }
            classGen.finishScript();
            
            DoABCTag doABC = new DoABCTag();
            try
            {
                doABC.setABCData(emitter.emit());
            }
            catch (Exception e)
            {
                return false;
            }
            
            doABC.setName(getFlexInitClassName());
            frame.addTag(doABC);
            
            return true;
        }
        
        /**
         * Gets a reference to the pre-loader class.
         * 
         * @return {@link IResolvedQualifiersReference} that resolves to the
         * pre-loader class to use.
         * @throws InterruptedException
         */
        private IResolvedQualifiersReference getPreloaderClassReference() throws InterruptedException
        {
            if (preloaderReference != null)
                return preloaderReference;
            
            String preloaderClassName = getTargetAttributes().getPreloaderClassName();

            if (preloaderClassName == null)
                preloaderClassName = targetSettings.getPreloaderClassName();

            // set up the reference to downloadProgressBarRef
            preloaderReference = ReferenceFactory.packageQualifiedReference(royaleProject.getWorkspace(), 
                    preloaderClassName);
            return preloaderReference;
        }
        
        /**
         * Gets a reference to the runtime dpi provider class.
         * 
         * @return {@link IResolvedQualifiersReference} that resolves to the runtime
         * dpi provider class to use, or null if no runtime dpi provider class has been specified.
         * @throws InterruptedException
         */
        private IResolvedQualifiersReference getRuntimeDPIProviderClassReference() throws InterruptedException
        {
            final String runtimeDPIProviderClassName = getTargetAttributes().getRuntimeDPIProviderClassName();
            if (runtimeDPIProviderClassName == null)
                return null;
            
            return ReferenceFactory.packageQualifiedReference(royaleProject.getWorkspace(), runtimeDPIProviderClassName);
        }
        
        
        /**
         * Adds a generated sub-class of a specified factory class ( usually
         * {@code mx.managers.SystemManager} ) to the specified {@link SWFFrame}
         * .
         * 
         * @param frame {@link SWFFrame} to add the generated class to.
         * @param frame1Info {@link RoyaleApplicationFrame1Info}
         * @param systemManagerClass {@link ClassDefinition} for factory class
         * for which a generated sub-class should be created, usually
         * {@code mx.managers.SystemManager}.
         * @param builtCompilationUnits {@link ImmutableSet} of all
         * {@link ICompilationUnit}s being built and added to a generated SWF.
         * @param problemCollection {@link Collection} that any
         * {@link ICompilerProblem}s found during class generation should be
         * added to.
         * @return true if a class was succesfully generated and added to the
         * specified {@link SWFFrame}, false otherwise.
         * @throws InterruptedException
         */
        boolean addGeneratedSystemManagerToFrame(SWFFrame frame, RoyaleApplicationFrame1Info frame1Info, ClassDefinition systemManagerClass, 
                ImmutableSet<ICompilationUnit> builtCompilationUnits, 
                Collection<ICompilerProblem> problemCollection) throws InterruptedException
        {
            ABCEmitter emitter = new ABCEmitter();
            emitter.visit(ABCConstants.VERSION_ABC_MAJOR_FP10, ABCConstants.VERSION_ABC_MINOR_FP10);
            

            String generatedSystemManagerClassNameString = getGeneratedSystemManagerClassName(systemManagerClass);

            Name generatedSystemManagerName = new Name(generatedSystemManagerClassNameString);

            ImmutableList.Builder<Name> listOfInterfaces = new ImmutableList.Builder<Name>();
            if (iModuleFactoryReference.resolve(royaleProject) != null && isFlexSDKInfo)
            {
                listOfInterfaces.add(iModuleFactoryReference.getMName());
            }
            if (iSWFContextReference.resolve(royaleProject) != null && isFlexSDKInfo)
            {
                listOfInterfaces.add(iSWFContextReference.getMName());
            }
            Collection<Name> implementedInterfaces = listOfInterfaces.build();

            // Generate code for the constructor:
            // public function ClassName()
            // {
            //    RoyaleVersion.compatibilityVersionString = "4.5.0";
            //    super();
            // }
            final String compatibilityVersion = royaleProject.getCompatibilityVersionString();
            final InstructionList classITraitsInit = new InstructionList();
            if (compatibilityVersion != null && royaleVersionReference.resolve(royaleProject) != null && isFlexSDKInfo)
            {
                Name royaleVersionSlotName = royaleVersionReference.getMName();
                classITraitsInit.addInstruction(ABCConstants.OP_getlex, royaleVersionSlotName);
                classITraitsInit.addInstruction(ABCConstants.OP_pushstring, compatibilityVersion);
                classITraitsInit.addInstruction(ABCConstants.OP_setproperty, new Name("compatibilityVersionString"));
            }
            classITraitsInit.addInstruction(ABCConstants.OP_getlocal0);
            classITraitsInit.addInstruction(ABCConstants.OP_constructsuper, 0);
            classITraitsInit.addInstruction(ABCConstants.OP_returnvoid);
            ClassGeneratorHelper classGen = new ClassGeneratorHelper(royaleProject, emitter, generatedSystemManagerName, systemManagerClass, implementedInterfaces, classITraitsInit);

            final FlexRSLInfo rslInfo = getRSLInfo();
            
            final FlexSplashScreenImage splashScreenImage = getSplashScreenImage();
            
            // Codegen various methods
            if (iSWFContextReference.resolve(royaleProject) != null && isFlexSDKInfo)
                codegenCallInContextMethod(classGen, true);
            codegenCreateMethod(classGen, ((DefinitionBase)mainApplicationClassDefinition).getMName(royaleProject), isFlexSDKInfo);
            codegenInfoMethod(classGen, 
                    royaleProject.getCompatibilityVersion(),
                    getMainClassQName(),
                    getPreloaderClassReference(),
                    getRuntimeDPIProviderClassReference(),
                    splashScreenImage,
                    getRootNode(),
                    getTargetAttributes(),
                    royaleProject.getLocales(),
                    frame1Info,
                    accessibleClassNames,
                    getFlexInitClassName(),
                    getStylesClassName(),
                    targetSettings.getRuntimeSharedLibraries(),
                    rslInfo,
                    problemCollection,
                    false,
                    isFlexSDKInfo,
                    null);
            
            classGen.finishScript();

            DoABCTag doABC = new DoABCTag();
            try
            {
                doABC.setABCData(emitter.emit());
            }
            catch (Exception e)
            {
                return false;
            }

            // We pass "false" for the "allowExternals" parameter of 
            // addDefinitionAndDependenciesToFrame() because we know we are
            // creating the first frame of a two frame swf.
            // The first frame is the loader frame and the second frame is the
            // application frame. The loader frame shows the preloader to entertain
            // the user while it loads RSLs and waits for the second frame to load.
            // The first frame does not allow classes to be externalized because it
            // needs to link in some framework classes to load RSLs and show the 
            // preloader. But we need to make sure we don't link in native code
            // from playerglobal.swc so there is special code in 
            // isLinkageAlwaysExternal() to handle that case.
            
            

            doABC.setName(generatedSystemManagerClassNameString);
            frame.addTag(doABC);

            return true;
        }
        
        
        
    }
    
   
}
