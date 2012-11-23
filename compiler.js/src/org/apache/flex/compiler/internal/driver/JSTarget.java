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

package org.apache.flex.compiler.internal.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.flex.abc.ABCLinker.ABCLinkerSettings;
import org.apache.flex.compiler.clients.MXMLJSC;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.exceptions.BuildCanceledException;
import org.apache.flex.compiler.internal.as.codegen.JSGeneratingReducer;
import org.apache.flex.compiler.internal.as.codegen.JSSharedData;
import org.apache.flex.compiler.internal.definitions.DefinitionBase;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.targets.AppSWFTarget;
import org.apache.flex.compiler.internal.targets.Target;
import org.apache.flex.compiler.internal.units.ResourceBundleCompilationUnit;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.UnableToBuildSWFTagProblem;
import org.apache.flex.compiler.targets.ITargetProgressMonitor;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.swf.ISWF;
import org.apache.flex.swf.SWFFrame;
import org.apache.flex.swf.tags.DoABCTag;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Concrete implementation of ITarget for building a collection of source files
 * into a SWF.
 */

public class JSTarget extends AppSWFTarget
{
    private ICompilationUnit mainCU;
    
    /**
     * Initialize a SWF target with the owner project and root compilation
     * units.
     * 
     * @param project the owner project
     */

    public JSTarget(CompilerProject project, ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor)
    {
        super(project, targetSettings, progressMonitor);
    }

    /*
     * private void printDefinitionsFromCompilationUnit( ICompilationUnit cu )
     * throws InterruptedException { final List<IDefinition> defs =
     * MXMLJSC.getDefinitions(cu, true); String s = cu.toString() + ": " +
     * cu.getShortNames(); JSSharedData.instance.verboseMessage(s); }
     */

    protected void buildAndCollectProblems(
            final Set<ICompilationUnit> compilationUnits,
            final Collection<ICompilerProblem> problems)
            throws InterruptedException
    {
        final JSSharedData sharedData = JSSharedData.instance;
        sharedData.beginCodeGen();

        BuiltCompilationUnitSet builtCompilationUnits = getBuiltCompilationUnitSet();

        if (JSSharedData.OUTPUT_ISOLATED)
        {
            final ICompilationUnit rootCU = getRootClassCompilationUnit();
            compilationUnits.clear();
            compilationUnits.add(rootCU);
        }
        else
        {
            final List<ICompilationUnit> allUnits = new ArrayList<ICompilationUnit>();
            allUnits.addAll(project.getReachableCompilationUnitsInSWFOrder(builtCompilationUnits.compilationUnits));
            final List<ICompilationUnit> cuList = sortCompilationUnits(allUnits);
            compilationUnits.clear();
            for (ICompilationUnit cu : cuList)
                compilationUnits.add(cu);
        }
        sharedData.endCodeGen();
    }

    public ISWF build(ICompilationUnit mainCU, Collection<ICompilerProblem> problems)
    {
        this.mainCU = mainCU;
        return build(problems);
    }
    
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
            Iterables.addAll(problems, rootedCompilationUnits.getProblems());

            compilationUnitSet.addAll(rootedCompilationUnits.getUnits());

            buildAndCollectProblems(compilationUnitSet, problems);

            List<ICompilationUnit> reachableCompilationUnits = project.getReachableCompilationUnitsInSWFOrder(rootedCompilationUnits.getUnits());
            ISWF swf = initializeSWF(reachableCompilationUnits);

            // make main frame for DoABC tags
            final SWFFrame mainFrame = new SWFFrame();
            swf.addFrame(mainFrame);

            // Add definitions.
            for (final ICompilationUnit cu : compilationUnitSet)
            {
                // ignore externals
                if (isLinkageExternal(cu, targetSettings))
                    continue;

                // ignore any resource bundles
                if (cu instanceof ResourceBundleCompilationUnit)
                    continue;

                // Create a DoABC tag per compilation unit.

                // Please add this API to SWFTarget. Thx.
                // protected Boolean addToFrame(ICompilationUnit cu, SWFFrame mainFrame) throws InterruptedException
                // final boolean tagsAdded = cu.getSWFTagsRequest().get().addToFrame(mainFrame);
                final boolean tagsAdded = addToFrame(cu, mainFrame);
                if (!tagsAdded)
                {
                    ICompilerProblem problem = new UnableToBuildSWFTagProblem(cu.getAbsoluteFilename());
                    problems.add(problem);
                }
            }

            createLinkReport(problems);

            return swf;
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

    // Please add this API to SWFTarget. Thx.
    protected Boolean addToFrame(ICompilationUnit cu, SWFFrame mainFrame) throws InterruptedException
    {
        // SWFTarget's implementation:
        // return cu.getSWFTagsRequest().get().addToFrame(mainFrame);

        final JSSharedData sharedData = JSSharedData.instance;

        String code = "";
        final List<IDefinition> defs = MXMLJSC.getDefinitions(cu, false);
        for (IDefinition def : defs)
        {
            final String fullName = def.getQualifiedName();
            if (sharedData.hasJavaScript(fullName))
            {
                final String jsCode = sharedData.getJavaScript(fullName);
                code += jsCode;
            }
        }

        if (!code.isEmpty())
        {
            final DoABCTag abcTag = new DoABCTag();
            abcTag.setABCData(code.getBytes());
            mainFrame.addTag(abcTag);
        }
        else
        {
            return cu.getSWFTagsRequest().get().addToFrame(mainFrame);
        }
        sharedData.registerSWFFrame(mainFrame, cu);
        return true;
    }

    /**
     * sortCompilationUnits() is a workaround for DependencyGraph bugs. There
     * are three problem areas: 1. The order of the CUs is somewhat random
     * depending on the thread that compiled a CU. 2. Dependencies through
     * static initializers are not always correctly detected. 3. Dependencies to
     * classes provided by SWCs are not correctly detected.
     */
    public static List<ICompilationUnit> sortCompilationUnits(List<ICompilationUnit> frameCompilationUnits) throws InterruptedException
    {
        ICompilationUnit frameWorkCU = null;
        ICompilationUnit xmlCU = null;
        ICompilationUnit xmlListCU = null;

        // extract framework CU, AS3XML CU, and AS3XMLList CU from frameCompilationUnits
        Iterator<ICompilationUnit> it = frameCompilationUnits.iterator();
        while (it.hasNext())
        {
            // get class name for compilation unit.
            ICompilationUnit cu = it.next();
            final List<IDefinition> defs = MXMLJSC.getDefinitions(cu, false);
            for (IDefinition def : defs)
            {
                final String fullName = JSGeneratingReducer.createFullNameFromDefinition(cu.getProject(), def);
                if (frameWorkCU == null && fullName.equals(JSSharedData.JS_FRAMEWORK_NAME))
                {
                    frameWorkCU = cu;
                    it.remove();
                }
                else if (xmlCU == null && fullName.equals(JSSharedData.AS3XML))
                {
                    xmlCU = cu;
                    it.remove();
                }
                else if (xmlListCU == null && fullName.equals(JSSharedData.AS3XMLList))
                {
                    xmlListCU = cu;
                    it.remove();
                }

                if (def instanceof DefinitionBase)
                {
                    JSSharedData.instance.registerReferencedDefinition(def.getQualifiedName());
                }

                JSSharedData.instance.registerPackage(def.getPackageName());
            }
        }

        // insist on framework CU
        if (frameWorkCU == null)
            throw JSSharedData.backend.createException("JSTarget: cannot find " + JSSharedData.JS_FRAMEWORK_NAME + " compilation unit.");

        // add the framework CU at pos 0
        frameCompilationUnits.add(0, frameWorkCU);

        // add AS3XML and AS3XMLList framework CUs if necessary
        if (xmlCU != null)
        {
            // add the AS3XML CU at pos 1
            frameCompilationUnits.add(1, xmlCU);

            // insist on AS3XMLList CU
            if (xmlListCU == null)
                throw JSSharedData.backend.createException("JSTarget: cannot find " + JSSharedData.AS3XMLList + " compilation unit.");

            // add the AS3XMLList CU at pos 2
            frameCompilationUnits.add(2, xmlListCU);
        }

        return frameCompilationUnits;
    }

    @Override
    public Target.RootedCompilationUnits computeRootedCompilationUnits() throws InterruptedException
    {
        if (mainCU != null)
        {
            return new Target.RootedCompilationUnits(ImmutableSet.of(mainCU), Collections.<ICompilerProblem> emptyList());
        }
        
        assert false;
        return new Target.RootedCompilationUnits(Collections.<ICompilationUnit> emptySet(), Collections.<ICompilerProblem> emptyList());
    }

    @Override
    protected FramesInformation computeFramesInformation() throws InterruptedException
    {
        assert false;
        return null;
    }

    @Override
    protected void addLinkedABCToFrame(SWFFrame targetFrame, Iterable<DoABCTag> inputABCs, ABCLinkerSettings linkSettings) throws Exception
    {
        assert false;
    }

    @Override
    protected void setKeepAS3MetadataLinkerSetting(ABCLinkerSettings linkSettings)
    {
        assert false;
    }

}
