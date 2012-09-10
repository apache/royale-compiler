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

package org.apache.flex.compiler.internal.targets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.flex.compiler.constants.IMetaAttributeConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.metadata.IMetaTag;
import org.apache.flex.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.flex.compiler.definitions.references.ReferenceFactory;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.units.ASCompilationUnit;
import org.apache.flex.compiler.internal.units.MXMLCompilationUnit;
import org.apache.flex.compiler.mxml.IMXMLTypeConstants;
import org.apache.flex.compiler.problems.CompiledAsAComponentProblem;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.units.ICompilationUnit.UnitType;
import org.apache.flex.compiler.units.requests.IFileScopeRequestResult;
import com.google.common.collect.ImmutableSet;

/**
 * Concrete sub-class of {@code FlexFrame1Info} that contains information about code that uses the Flex
 * framework that is needed by the compiler to generate code in the first frame
 * of Flex application SWFs.
 * <p>
 * This information is collected by looking at all the {@link ICompilationUnit}s
 * that will be built into a Flex application SWF.
 */
final class FlexApplicationFrame1Info extends FlexFrame1Info
{
    FlexApplicationFrame1Info(FlexProject flexProject,
            ITargetSettings targetSettings,
            IClassDefinition mainApplicationClassDefinition,
            boolean generateSystemManagerAndFlexInit,
            ImmutableSet<ICompilationUnit> builtCompilationUnits) throws InterruptedException
    {
        super(flexProject);
        this.targetSettings = targetSettings;
        this.mainApplicationClassDefinition = mainApplicationClassDefinition;
        this.generateSystemManagerAndFlexInit = generateSystemManagerAndFlexInit;
        
        // sorted set of mixin classes
        mixinClassNames = new TreeSet<String>();
        
        contributingSWCs = new HashSet<String>();
        
        appAndModuleDefs = new ApplicationAndModuleDefinitions();
        
        collectFromCompilationUnits(builtCompilationUnits);
    }
    
    private final ITargetSettings targetSettings;
    private final IClassDefinition mainApplicationClassDefinition;
    private final boolean generateSystemManagerAndFlexInit;
    private final ApplicationAndModuleDefinitions appAndModuleDefs;
    
    /**
     * sorted {@link Set} of mixin classes.
     */
    final TreeSet<String> mixinClassNames;
    
    final HashSet<String> contributingSWCs;
    
    /**
     * {@inheritDoc}
     * <p>
     * In addition to the information collected by the base class (
     * {@link FlexFrame1Info} ), this class collects:
     * <ul>
     * <li>Mix-in class names</li>
     * <li>File names of SWCs that contributed to code the flex application SWF</li>
     * <li>Problems about application sub-classes that are not the root class of
     * a flex application SWF</li>
     * </ul>
     */
    protected void collectFromCompilationUnit(ICompilationUnit cu) throws InterruptedException
    {
        super.collectFromCompilationUnit(cu);
        if (generateSystemManagerAndFlexInit)
        {
            collectMixinMetaData(mixinClassNames, cu);
            collectContributingSWCs(contributingSWCs, cu);
            checkForCompiledAsAComponentProblem(appAndModuleDefs, problems, cu);
        }
    }
    
    /**
     * Collects names of classes that are mix-ins.
     * 
     * @param compilationUnit
     * @throws InterruptedException
     */
    private void collectMixinMetaData(TreeSet<String> mixinClassNames, ICompilationUnit compilationUnit) throws InterruptedException
    {
        IFileScopeRequestResult result = compilationUnit.getFileScopeRequest().get();

        for(IDefinition def : result.getExternallyVisibleDefinitions()) 
        {
            IMetaTag md = def.getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_MIXIN);
            if (md != null)
                mixinClassNames.add(def.getQualifiedName());
        }
    }
    
    /**
     * Collect the swcs that are contributing compilation units to this swf.
     * 
     * @param cu
     */
    private void collectContributingSWCs(HashSet<String> contributingSWCs, ICompilationUnit cu)
    {
        // If we are not removing unused RSLs, then we don't need to
        // collect the swcs.
        if (!targetSettings.removeUnusedRuntimeSharedLibraryPaths())
            return;
        
        if (cu.getCompilationUnitType() == UnitType.SWC_UNIT)
            contributingSWCs.add(cu.getAbsoluteFilename());
    }
    
    /**
     * Check if the compilation unit extends IModule, mx:Application, or s:Application and it not the
     * main class of the application.
     * 
     * @param cu
     * @throws InterruptedException 
     */
    private void checkForCompiledAsAComponentProblem(ApplicationAndModuleDefinitions appAndModuleDefs, ArrayList<ICompilerProblem> problems, ICompilationUnit cu) throws InterruptedException
    {
        
        // only check compiled source.
        if (!(cu instanceof MXMLCompilationUnit || cu instanceof ASCompilationUnit) ||
                mainApplicationClassDefinition == null)
            return;

        IFileScopeRequestResult result = cu.getFileScopeRequest().get();
        for (IDefinition def : result.getExternallyVisibleDefinitions())
        {
            if (appAndModuleDefs. isApplicationOrModule(def))
            {
                assert def instanceof ITypeDefinition;
                final ITypeDefinition typeDef = (ITypeDefinition)def;
                // If the mainClass extends this class then don't report a problem
                assert mainApplicationClassDefinition instanceof ITypeDefinition;
                if (!mainApplicationClassDefinition.isInstanceOf(typeDef, flexProject))
                {
                    problems.add(new CompiledAsAComponentProblem(cu.getName(), 
                            mainApplicationClassDefinition.getBaseName()));
                }
            }
        }
    }
    
    /**
     * @return A {@link Set} of {@link String}s containing the dotted fully
     * qualified names of mix-in classes.
     */
    Set<String> getMixins()
    {
        return this.mixinClassNames;
    }
    
    /**
     * Helper class that holds {@link ITypeDefinition}s for:
     * <ul>
     * <li>{@code spark.components.Application}</li>
     * <li>{@code mx.core.Application}</li>
     * <li>{@code mx.modules.IModule}</li>
     * </ul>
     */
    private class ApplicationAndModuleDefinitions
    {
        ApplicationAndModuleDefinitions()
        {
            IResolvedQualifiersReference sparkAppRef = ReferenceFactory.packageQualifiedReference(
                    flexProject.getWorkspace(), IMXMLTypeConstants.SparkApplication);
            IResolvedQualifiersReference haloAppRef = ReferenceFactory.packageQualifiedReference(
                  flexProject.getWorkspace(), IMXMLTypeConstants.HaloApplication);
            IResolvedQualifiersReference iModuleRef = ReferenceFactory.packageQualifiedReference(
                  flexProject.getWorkspace(), IMXMLTypeConstants.IModule);
        
            sparkApplication = resolveType(sparkAppRef);
            haloApplication = resolveType(haloAppRef);
            iModule = resolveType(iModuleRef);
        }
        
        final ITypeDefinition sparkApplication;
        final ITypeDefinition haloApplication;
        final ITypeDefinition iModule;
        
        /**
         * Test if the definition inherits from IModule, Spark Application,
         * or Halo Application.
         * 
         * @param def {@link IDefinition} to check.
         * @return true if the definition inherits from IModule, Spark
         * Application, and Halo Application but is itself not an IModule,
         * Spark Application, or Halo Application.
         */
        boolean isApplicationOrModule(IDefinition def)
        {
            if (!(def instanceof ITypeDefinition))
                return false;
            ITypeDefinition typeDef = (ITypeDefinition)def;
            if (typeDef == sparkApplication ||
                typeDef == iModule ||
                typeDef == haloApplication)
            {
                return false;
            }
                
                return ((sparkApplication != null && typeDef.isInstanceOf(sparkApplication, flexProject)) ||
                        (iModule != null && typeDef.isInstanceOf(iModule, flexProject)) ||
                        (haloApplication != null && typeDef.isInstanceOf(iModule, flexProject)));
        }
        
        private ITypeDefinition resolveType(IResolvedQualifiersReference ref)
        {
            IDefinition def = ref.resolve(flexProject);
            if (!(def instanceof ITypeDefinition))
                return null;
            return (ITypeDefinition)def;
        }
    }
}