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
import java.util.List;
import java.util.Set;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.ABCEmitter;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.abc.ClassGeneratorHelper;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.ResourceBundleNotFoundForLocaleProblem;
import org.apache.royale.compiler.problems.ResourceBundleNotFoundProblem;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.tags.DoABCTag;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public final class RoyaleLibrarySWFTarget extends LibrarySWFTarget
{
    public RoyaleLibrarySWFTarget(RoyaleProject project, ITargetSettings targetSettings, Set<ICompilationUnit> rootedCompilationUnits)
    {
        super(project, targetSettings, rootedCompilationUnits);
        royaleProject = project;
        delegate = new FlexDelegate(targetSettings, project);
        isLibrary = true;
    }
    
    private final RoyaleProject royaleProject;
    
    private ModuleFactoryInfo moduleFactoryInfo;
    
    private final FlexDelegate delegate;
    
    private ModuleFactoryInfo computeModuleFactoryInfo()
    {
        final IResolvedQualifiersReference moduleFactoryBaseClassReference =
            ReferenceFactory.packageQualifiedReference(royaleProject.getWorkspace(), getBaseClassQName());
        final IDefinition moduleFactoryBaseClassDef =
            moduleFactoryBaseClassReference.resolve(royaleProject);
        if (!(moduleFactoryBaseClassDef instanceof ClassDefinition))
            return ModuleFactoryInfo.create(Collections.<ICompilerProblem>emptyList()); // TODO make a compiler problem here!
        
        final ClassDefinition moduleFactoryBaseClass =
            (ClassDefinition)moduleFactoryBaseClassDef;
        
        final ICompilationUnit moduleFactoryBaseClassCompilationUnit =
            royaleProject.getScope().getCompilationUnitForDefinition(moduleFactoryBaseClass);
        assert moduleFactoryBaseClassCompilationUnit != null : "Unable to find compilation unit for definition!";
        
        return ModuleFactoryInfo.create(getGeneratedModuleFactoryClassName(moduleFactoryBaseClass), moduleFactoryBaseClass, moduleFactoryBaseClassCompilationUnit);
    }
    
    private ModuleFactoryInfo getModuleFactoryInfo()
    {
        if (moduleFactoryInfo != null)
            return moduleFactoryInfo;
        moduleFactoryInfo = computeModuleFactoryInfo();
        return moduleFactoryInfo;
    }
    
    @Override
    public String getBaseClassQName()
    {
        // TODO: check configuration for a user defined class.
        // Defaults to an "empty" module factory to handle the case where fonts
        // are embedded in an RSL.
        return "EmptyModuleFactory";
    }
    
    @Override
    protected FramesInformation computeFramesInformation() throws InterruptedException
    {
        final ModuleFactoryInfo moduleFactoryInfo = getModuleFactoryInfo();
        if (!moduleFactoryInfo.generateModuleFactory())
            return super.computeFramesInformation();
        final Set<ICompilationUnit> compilationUnits =
            Sets.union(Collections.singleton(moduleFactoryInfo.moduleFactoryBaseClassCompilationUnit), this.rootedCompilationUnits);
        final SWFFrameInfo frameInfo =
            new SWFFrameInfo(null, SWFFrameInfo.EXTERNS_ALLOWED, compilationUnits, moduleFactoryInfo.problems);
        final RoyaleLibrarySWFFramesInformation framesInfo = new RoyaleLibrarySWFFramesInformation(frameInfo);
        return framesInfo;
    }

    /**
     * Generated a unique name for the root class name.
     * @param baseClass
     * @return unique class name for the library.swf in this SWC.
     */
    private String getGeneratedModuleFactoryClassName(IClassDefinition moduleFactoryBaseClass)
    {
        File outputFile = targetSettings.getOutput();
        String outputName = null;
        String absolutePath = null;
        if (outputFile != null)
        {
            absolutePath = outputFile.getAbsolutePath();
            String name = outputFile.getName();
            if (name != null)
            {
                int endIndex = name.lastIndexOf('.');
                if (endIndex != -1)
                {
                    name = name.substring(0, endIndex);
                }
            }
            
            // help make root class unique by using a hashcode
            // of the absolute path of the swc.
            outputName = name + "_" + absolutePath.hashCode();
        }
        
        assert outputName != null : "Provide an output name for the SWC by setting -output";
        
        // Use system time as a fall back for a unique name for the SWC.
        if (outputName == null)
            outputName = Long.toHexString(System.nanoTime());
        
        String generatedRootName = "_" + outputName + "_" + moduleFactoryBaseClass.getQualifiedName();
        generatedRootName = generatedRootName.replaceAll("[^a-zA-Z0-9]", "_");
        return generatedRootName;
    }
    
    @Override
    protected DirectDependencies getDirectDependencies(ICompilationUnit cu) throws InterruptedException
    {
        final DirectDependencies directDependencies =
            super.getDirectDependencies(cu);
        if (!targetSettings.isAccessible())
            return directDependencies;
        final DirectDependencies acccessibilityDependencies = 
            delegate.getAccessibilityDependencies(cu);
        return DirectDependencies.concat(directDependencies, acccessibilityDependencies);
    }
    
    @Override
    protected void waitForCompilationUnitToFinish(final ICompilationUnit cu, final Collection<ICompilerProblem> problems) throws InterruptedException
    {
        Collection<ICompilerProblem> problemsWithFilter = problems;
        
        // We we are externally linking a SWC into another SWC,
        // we need to filter out resource bundle not found problems
        // like the Flex 4.6.X compiler did.
        //
        // In an ideal world we would not need to do this
        // or we'd know we need to do it forever.  If we don't
        // need this filtering in the future, we can rip out this code.
        // If we continue to need this filter we should defer creation
        // of these problems until link time.
        if (getLinkageChecker().isExternal(cu))
        {
            // Collection implementation that drops
            // ICompilerProblems on the floor if they are instances of
            // ResourceBundleNotFoundProblem or
            // ResourceBundleNotFoundForLocaleProblem.
            problemsWithFilter = new ForwardingCollection<ICompilerProblem>()
            {
                @Override
                protected final Collection<ICompilerProblem> delegate()
                {
                    return problems;
                }

                @Override
                public final boolean add(ICompilerProblem element)
                {
                    if (element instanceof ResourceBundleNotFoundProblem)
                        return false;
                    if (element instanceof ResourceBundleNotFoundForLocaleProblem)
                        return false;
                    return super.add(element);
                }

                @Override
                public final boolean addAll(Collection<? extends ICompilerProblem> collection)
                {
                    boolean result = false;
                    for (ICompilerProblem problem : collection)
                    {
                       if (add(problem))
                           result = true;
                    }
                    return result;
                }
            };
        }
        super.waitForCompilationUnitToFinish(cu, problemsWithFilter);
    }
    
    @Override
    protected ISWF initializeSWF(List<ICompilationUnit> reachableCompilationUnits) throws InterruptedException
    {
        ISWF swf = super.initializeSWF(reachableCompilationUnits);
        delegate.addProductInfoToSWF(swf);
        
        return swf;
    }
    
    /**
     * Sub-class of {@link FramesInformation} that can create {@link SWFFrame}s
     * for all the frames in a library.swf in a royale SWC.
     * <p>
     * This class should only be constructed if we are also generating a module
     * factory.
     */
    private class RoyaleLibrarySWFFramesInformation extends FramesInformation
    {
        /**
         * Constructor
         * 
         * @param frameInfo The single {@link SWFFrameInfo} for the one frame in
         * a libary.swf in a royale SWC.
         */
        RoyaleLibrarySWFFramesInformation(SWFFrameInfo frameInfo)
        {
            super(Collections.singletonList(frameInfo));
        }

        @Override
        protected void createFrames(SWFTarget swfTarget, ISWF swf, ImmutableSet<ICompilationUnit> builtCompilationUnits, Set<ICompilationUnit> emittedCompilationUnits, Collection<ICompilerProblem> problems) throws InterruptedException
        {
            assert Iterables.size(frameInfos) == 1;
            SWFFrameInfo frameInfo = Iterables.getOnlyElement(frameInfos);
            
            final SWFFrame frame =
                createFrame(swfTarget, frameInfo, builtCompilationUnits, emittedCompilationUnits, problems);
            
            ModuleFactoryInfo moduleFactoryInfo = getModuleFactoryInfo();
            
            delegate.addGeneratedRootClassToSWFFrame(frame,
                    swf, moduleFactoryInfo, builtCompilationUnits, problems);
            swf.addFrame(frame);
            swf.setTopLevelClass(moduleFactoryInfo.generatedModuleFactoryClassName);
        }
    }

    /**
     * Helper class that keeps track of information about generating a module factory.
     * <p>
     * Module factories are generatign for library.swf's that can be used as RSLs.
     */
    private static class ModuleFactoryInfo
    {
        /**
         * Creates a {@link ModuleFactoryInfo} that will <em>not</em> cause a
         * module factory to be generated. The resulting library.swf will
         * <em>not</em> be suitable for loading as an RSL in a flex
         * application.
         * 
         * @param problems
         * @return A new {@link ModuleFactoryInfo}
         */
        static ModuleFactoryInfo create(Iterable<ICompilerProblem> problems)
        {
            return new ModuleFactoryInfo(null, null, null, problems);
        }
        
        /**
         * Creates a {@link ModuleFactoryInfo} that will cause a module factory
         * to be generated. The resulting library.swf will be suitable for
         * loading as an RSL in a royale application.
         * 
         * @param generatedModuleFactoryClassName The name of the module factory
         * to generate
         * @param moduleFactoryBaseClass The {@link ClassDefinition} for the
         * base class of the generated module factory.
         * @param moduleFactoryBaseClassCompilationUnit The
         * {@link ICompilationUnit} that defines the base class of the generated
         * module factory.
         * @return A new {@link ModuleFactoryInfo}
         */
        static ModuleFactoryInfo create(String generatedModuleFactoryClassName, IClassDefinition moduleFactoryBaseClass, ICompilationUnit moduleFactoryBaseClassCompilationUnit)
        {
            assert generatedModuleFactoryClassName != null;
            assert moduleFactoryBaseClass != null;
            assert moduleFactoryBaseClassCompilationUnit != null;
            return new ModuleFactoryInfo(generatedModuleFactoryClassName, moduleFactoryBaseClass, moduleFactoryBaseClassCompilationUnit, Collections.<ICompilerProblem>emptyList());
        }
        
        private ModuleFactoryInfo(String generatedModuleFactoryClassName, IClassDefinition moduleFactoryBaseClass, ICompilationUnit moduleFactoryBaseClassCompilationUnit, Iterable<ICompilerProblem> problems)
        {
            this.moduleFactoryBaseClass = moduleFactoryBaseClass;
            this.moduleFactoryBaseClassCompilationUnit = moduleFactoryBaseClassCompilationUnit;
            this.problems = problems;
            this.generatedModuleFactoryClassName = generatedModuleFactoryClassName;
        }
        
        /**
         * Determins if a module factory should be generated.
         * 
         * @return true if a module factory should be generated, false
         * otherwise.
         */
        boolean generateModuleFactory()
        {
            return moduleFactoryBaseClass != null;
        }
        
        final IClassDefinition moduleFactoryBaseClass;
        final ICompilationUnit moduleFactoryBaseClassCompilationUnit;
        final Iterable<ICompilerProblem> problems;
        final String generatedModuleFactoryClassName;
    }


    /**
     * Sub-class of {@link RoyaleTarget} that adds logic specific to building 
     * library.swf's in royale SWCs.
     */
    private class FlexDelegate extends RoyaleTarget
    {

        FlexDelegate(ITargetSettings targetSettings, RoyaleProject project)
        {
            super(targetSettings, project);
        }
        
        /**
         * Add the generated root class and its dependencies to the specified frame.
         * 
         * @param frame
         * @param swf
         * @param baseClass
         * @param projectScope
         * @param allowExternals if true, allow symbols to be externalized in this frame.
         * @param emittedCompilationUnits
         * @param problemCollection
         * @return
         * @throws InterruptedException
         */
        private boolean addGeneratedRootClassToSWFFrame(SWFFrame frame, ISWF swf, ModuleFactoryInfo moduleFactoryInfo, 
                ImmutableSet<ICompilationUnit> emittedCompilationUnits, 
                Collection<ICompilerProblem> problemCollection) throws InterruptedException
        {
            ABCEmitter emitter = new ABCEmitter();
            emitter.visit(ABCConstants.VERSION_ABC_MAJOR_FP10, ABCConstants.VERSION_ABC_MINOR_FP10);
            

            final String generatedRootClassNameString = moduleFactoryInfo.generatedModuleFactoryClassName;

            Name generatedRootName = new Name(generatedRootClassNameString);
            
            // Generate code for the constructor:
            // public function ClassName()
            // {
            //    super();
            // }
            InstructionList classITraitsInit = new InstructionList();
            classITraitsInit.addInstruction(ABCConstants.OP_getlocal0);
            classITraitsInit.addInstruction(ABCConstants.OP_constructsuper, 0);
            classITraitsInit.addInstruction(ABCConstants.OP_returnvoid);
            ClassGeneratorHelper classGen = new ClassGeneratorHelper(project,
                    emitter,
                    generatedRootName, 
                    (ClassDefinition)moduleFactoryInfo.moduleFactoryBaseClass,
                    Collections.<Name>emptyList(),
                    classITraitsInit);
            IResolvedQualifiersReference objectReference =
                ReferenceFactory.packageQualifiedReference(project.getWorkspace(), 
                    IASLanguageConstants.Object);
            
            // Codegen various methods
            // TODO: Determine whether a override is needed or not depending on the 
            // methods in the base class. Same deal for the Create and Info Methods.
            codegenCallInContextMethod(classGen, true);

            final RoyaleLibraryFrame1Info frame1Info =
                new RoyaleLibraryFrame1Info(royaleProject, emittedCompilationUnits);
            
            // Override the create() and info() methods if we have embedded fonts.
            if (!frame1Info.embeddedFonts.isEmpty())
            {
                codegenCreateMethod(classGen, objectReference.getMName(), true);
                codegenInfoMethod(classGen, 
                        IASLanguageConstants.Object,
                        frame1Info,
                        accessibleClassNames,
                        problemCollection);            
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

            doABC.setName(generatedRootClassNameString);
            frame.addTag(doABC);
            return true;
        }
        
        /**
         * Wrapping for the codegenInfoMethod.
         * Only exposing what library.swf needs.
         * 
         * @param classGen
         * @param rootClassQName
         * @param embeddedFonts
         * @param accessibleClassNames
         * @param problemCollection
         * @throws InterruptedException
         */
        private void codegenInfoMethod(ClassGeneratorHelper classGen, 
                String rootClassQName, 
                RoyaleFrame1Info frame1Info, 
                Set<String> accessibleClassNames, 
                Collection<ICompilerProblem> problemCollection) throws InterruptedException
        {
            codegenInfoMethod(classGen, 
                    null,
                    rootClassQName,
                    null, // preloader
                    null, // runtimeDPI
                    null, // splash screen
                    null, // root node
                    null, // target attributes
                    null, // locales
                    frame1Info,
                    accessibleClassNames,
                    null, // royale init
                    null, // styles class name
                    null, // rsls
                    null, // rslinof
                    problemCollection,
                    false, false, null);
            
        }
        
    }
    
    

}
