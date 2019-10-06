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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.driver.js.IJSApplication;
import org.apache.royale.compiler.exceptions.BuildCanceledException;
import org.apache.royale.compiler.internal.driver.js.JSApplication;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.targets.IJSTarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetReport;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class JSTarget extends Target implements IJSTarget
{
    protected ICompilationUnit mainCU;
    protected RootedCompilationUnits rootedCompilationUnits;

    /**
     * Initialize a JS target with the owner project and root compilation units.
     * 
     * @param project the owner project
     */
    public JSTarget(RoyaleJSProject project, ITargetSettings targetSettings,
                    ITargetProgressMonitor progressMonitor)
    {
        super(project, targetSettings, progressMonitor);
    }

    @Override
    public TargetType getTargetType()
    {
        // can't do anything, TargetType is only swf|swc
        return null;
    }

    @Override
    protected ITargetReport computeTargetReport() throws InterruptedException
    {
        // TODO Should return a new TargetReport relating to the js app?
        return null;
    }

    @Override
    protected RootedCompilationUnits computeRootedCompilationUnits()
            throws InterruptedException
    {
        if (mainCU != null)
        {
            return new Target.RootedCompilationUnits(ImmutableSet.of(mainCU),
                    Collections.<ICompilerProblem> emptyList());
        }
        return new Target.RootedCompilationUnits(
                Collections.<ICompilationUnit> emptySet(),
                Collections.<ICompilerProblem> emptyList());
    }

    @Override
    public RootedCompilationUnits getRootedCompilationUnits()
            throws InterruptedException
    {
        if (rootedCompilationUnits == null)
            rootedCompilationUnits = computeRootedCompilationUnits();
        return rootedCompilationUnits;
    }

    @Override
    public IJSApplication build(Collection<ICompilerProblem> problems)
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

            List<ICompilationUnit> reachableCompilationUnits = project
                    .getReachableCompilationUnitsInSWFOrder(rootedCompilationUnits
                            .getUnits());

            IJSApplication application = initializeApplication(reachableCompilationUnits);

            //            ISWF swf = initializeSWF(reachableCompilationUnits);
            //
            //            // make main frame for DoABC tags
            //            final SWFFrame mainFrame = new SWFFrame();
            //            swf.addFrame(mainFrame);
            //
            //            // Add definitions.
            //            for (final ICompilationUnit cu : compilationUnitSet)
            //            {
            //                // ignore externals
            //                if (isLinkageExternal(cu, targetSettings))
            //                    continue;
            //
            //                // ignore any resource bundles
            //                if (cu instanceof ResourceBundleCompilationUnit)
            //                    continue;
            //
            //                // Create a DoABC tag per compilation unit.
            //
            //                // Please add this API to SWFTarget. Thx.
            //                // protected Boolean addToFrame(ICompilationUnit cu, SWFFrame mainFrame) throws InterruptedException
            //                // final boolean tagsAdded = cu.getSWFTagsRequest().get().addToFrame(mainFrame);
            //                final boolean tagsAdded = addToFrame(cu, mainFrame);
            //                if (!tagsAdded)
            //                {
            //                    ICompilerProblem problem = new UnableToBuildSWFTagProblem(cu.getAbsoluteFilename());
            //                    problems.add(problem);
            //                }
            //            }
            //
            createLinkReport(problems);

            return application;
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

    protected IJSApplication initializeApplication(
            List<ICompilationUnit> reachableCompilationUnits)
    {
        JSApplication result = new JSApplication();
        // TODO set properties of the application
        return result;
    }

    protected void buildAndCollectProblems(
            final Set<ICompilationUnit> compilationUnits,
            final Collection<ICompilerProblem> problems)
            throws InterruptedException
    {
/*        final JSSharedData sharedData = JSSharedData.instance;
        sharedData.beginCodeGen();*/

        BuiltCompilationUnitSet builtCompilationUnits = getBuiltCompilationUnitSet();

        //        if (JSSharedData.OUTPUT_ISOLATED)
        //        {
        final ICompilationUnit rootCU = getRootClassCompilationUnit();
        compilationUnits.clear();
        compilationUnits.add(rootCU);

        // i added
        Iterables.addAll(problems, builtCompilationUnits.problems);
        //        }
        //        else
        //        {
        //            final List<ICompilationUnit> allUnits = new ArrayList<ICompilationUnit>();
        //            allUnits.addAll(project
        //                    .getReachableCompilationUnitsInSWFOrder(builtCompilationUnits.compilationUnits));
        //            final List<ICompilationUnit> cuList = sortCompilationUnits(allUnits);
        //            compilationUnits.clear();
        //            for (ICompilationUnit cu : cuList)
        //                compilationUnits.add(cu);
        //        }
//        sharedData.endCodeGen();
    }

    private ICompilationUnit getRootClassCompilationUnit()
    {
        String rootClassFileName = targetSettings.getRootSourceFileName();
        if (rootClassFileName == null)
            return null;

        Collection<ICompilationUnit> rootClassCompilationUnits = project
                .getCompilationUnits(rootClassFileName);
        assert rootClassCompilationUnits.size() == 1 : "There must only be a single compilation unit for the root source file!";
        return Iterables.getOnlyElement(rootClassCompilationUnits);
    }

    public IJSApplication build(ICompilationUnit unit,
            Collection<ICompilerProblem> problems)
    {
        mainCU = unit;
        return build(problems);
    }

}
