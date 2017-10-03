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
import java.util.Set;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.ABCLinker;
import org.apache.royale.abc.ABCLinker.ABCLinkerSettings;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.units.ResourceBundleCompilationUnit;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.tags.DoABCTag;
import com.google.common.collect.ImmutableSet;

/**
 * Class to build a library.swf for a SWC.
 */
public class LibrarySWFTarget extends SWFTarget implements ILibrarySWFTarget
{

    public LibrarySWFTarget(CompilerProject project, ITargetSettings targetSettings,
                            Set<ICompilationUnit> rootedCompilationUnits)
    {
        super(project, targetSettings, null);

        this.rootedCompilationUnits = rootedCompilationUnits;
        this.isLibrary = true;
    }

    protected final Set<ICompilationUnit> rootedCompilationUnits;
    private ImmutableSet<ICompilationUnit> compilationUnits;
    
    
    @Override
    protected ITargetAttributes computeTargetAttributes() throws InterruptedException
    {
        // Library SWF has no overridden target attributes
        return NilTargetAttributes.INSTANCE;
    }
    
    @Override
    protected FramesInformation computeFramesInformation() throws InterruptedException
    {
        final SWFFrameInfo frame =
            new SWFFrameInfo(rootedCompilationUnits, Collections.<ICompilerProblem>emptyList());
        return new FramesInformation(Collections.singletonList(frame));
    }

    @Override
    public ImmutableSet<ICompilationUnit> getCompilationUnits()
    {
        return compilationUnits != null ? compilationUnits : ImmutableSet.<ICompilationUnit>of();
    }

    @Override
    public String getRootClassName()
    {
        return null;
    }

    @Override
    public String getBaseClassQName()
    {
        return null;
    }

    /*
     * Override method because library SWFs do not include resource bundles.
     */
    @Override
    protected boolean testCompilationUnitLinkage(ICompilationUnit cu, boolean allowExternals) throws InterruptedException
    {
        return super.testCompilationUnitLinkage(cu, allowExternals) &&
               !(cu instanceof ResourceBundleCompilationUnit);
    }

    @Override
    protected void doPostBuildWork(ImmutableSet<ICompilationUnit> compilationUnits, Collection<ICompilerProblem> problems) throws InterruptedException
    {
        this.compilationUnits = compilationUnits;
    }

    @Override
    protected boolean shouldAddMetadataNamesToTarget(ICompilationUnit cu, boolean linkage)
    {
        // Library swfs only include the metadata names of compilation units that
        // are linked into the swf.
        return (linkage && super.shouldAddMetadataNamesToTarget(cu, linkage));
    }

    @Override
    protected final void addLinkedABCToFrame(SWFFrame targetFrame, Iterable<DoABCTag> inputABCs, ABCLinkerSettings linkSettings) throws Exception
    {
        for (DoABCTag inputABC : inputABCs)
        {
            byte[] linkedBytes = 
                ABCLinker.linkABC(Collections.singleton(inputABC.getABCData()), ABCConstants.VERSION_ABC_MAJOR_FP10, ABCConstants.VERSION_ABC_MINOR_FP10, linkSettings);
            DoABCTag linkedTag = new DoABCTag(1, inputABC.getName(), linkedBytes);
            targetFrame.addTag(linkedTag);
        }
    }

    @Override
    protected void setKeepAS3MetadataLinkerSetting(ABCLinkerSettings linkSettings)
    {
        linkSettings.setKeepMetadata(null);
    }
}
