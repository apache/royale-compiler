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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSManager;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.css.semantics.ActivatedStyleSheets;
import org.apache.royale.compiler.internal.driver.js.royale.JSCSSCompilationSession;
import org.apache.royale.compiler.internal.projects.DependencyGraph;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.units.SWCCompilationUnit;
import org.apache.royale.compiler.problems.FileNotFoundProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.targets.IJSTarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit.UnitType;

import com.google.common.collect.ImmutableList;

public class RoyaleJSTarget extends JSTarget implements IJSTarget
{
    /**
     * Initialize a JS target with the owner project and root compilation units.
     * 
     * @param project the owner project
     */
    public RoyaleJSTarget(RoyaleJSProject project, ITargetSettings targetSettings,
            ITargetProgressMonitor progressMonitor)
    {
        super(project, targetSettings, progressMonitor);
        royaleProject = project;
    }
    
    private final RoyaleJSProject royaleProject;

    ///////////
    //
    //  Copied from RoyaleAppSWFTarget.java then modified
    //
    ///////////
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
        JSCSSCompilationSession cssCompilationSession = (JSCSSCompilationSession) royaleProject.getCSSCompilationSession();
        cssCompilationSession.setKeepAllTypeSelectors(targetSettings.keepAllTypeSelectors());
        
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
            // activate rules in user specified CSS
            for (ICSSDocument cssDocument : cssCompilationSession.cssDocuments)
            {
                // Side-effects of this method:
                // 1. Resolve all type selectors in the CSS model to IClassDefinition definitions.
                // 2. Activate CSS rules whose subject is in the definition set.
                final Collection<ICompilationUnit> dependentCUListFromCSS =
                        cssManager.getDependentCompilationUnitsFromCSS(
                                cssCompilationSession,
                                cssDocument,
                                definitions,
                                problems);
                cssDependencies.addAll(dependentCUListFromCSS);        	
            }
            for (final ICSSDocument cssDocument : activatedStyleSheets.all())
            {
                // Side-effects of this method:
                // 1. Resolve all type selectors in the CSS model to IClassDefinition definitions.
                // 2. Activate CSS rules whose subject is in the definition set.
                final Collection<ICompilationUnit> dependentCUListFromCSS =
                        cssManager.getDependentCompilationUnitsFromCSS(
                                cssCompilationSession,
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
	            DependencyGraph graph = royaleProject.getDependencyGraph();
	            for (ICompilationUnit cu : cssDependencies)
	            {
	            	graph.addDependency(mainCU, cu, DependencyType.EXPRESSION);
	            }
            }
        }

        // add to front so user specified css overrides defaults
        cssCompilationSession.cssDocuments.addAll(0, activatedStyleSheets.sort());
        
        return super.findAllCompilationUnitsToLink(compilationUnits, problems);
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
                {
                    result.put(defaultCSS, swcFile);
                }
            }
        }
        return result;
    }
    
    public void collectMixinMetaData(TreeSet<String> mixinClassNames, List<ICompilationUnit> units)
    {
    	for (ICompilationUnit unit : units)
    	{
        	try {
				RoyaleApplicationFrame1Info.collectMixinMetaData(mixinClassNames, unit);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    public void collectRemoteClassMetaData(Map<String, String> remoteClassAliasMap, List<ICompilationUnit> units)
    {
    	for (ICompilationUnit unit : units)
    	{
        	try {
				RoyaleApplicationFrame1Info.collectRemoteClassMetaData(remoteClassAliasMap, unit);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

}
