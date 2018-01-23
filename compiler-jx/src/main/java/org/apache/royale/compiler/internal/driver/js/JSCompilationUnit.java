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

package org.apache.royale.compiler.internal.driver.js;

import java.io.IOException;
import java.util.Collection;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.internal.units.ASCompilationUnit;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;

/**
 * JSCompilationUnit is the CompilationUnit for compiling ActionScript source
 * files to JavasScript.
 * <p>
 * JSCompilationUnit is derived from ASCompilationUnit and overrides the parts
 * that generate the code.
 */
public class JSCompilationUnit extends ASCompilationUnit
{
    @SuppressWarnings("unused")
    private Boolean inCodeGen = false;

    /**
     * Create a compilation unit from an ABC file.
     * 
     * @param project compiler project
     * @param path ABC file path
     * @throws IOException error
     */
    public JSCompilationUnit(CompilerProject project, String path)
            throws IOException
    {
        this(project, path, DefinitionPriority.BasePriority.LIBRARY_PATH);
    }

    public JSCompilationUnit(CompilerProject project, String path,
            DefinitionPriority.BasePriority basePriority)
    {
        super(project, path, basePriority);
    }

    public JSCompilationUnit(CompilerProject project, String path,
            DefinitionPriority.BasePriority basePriority, String qname)
    {
        super(project, path, basePriority, 0, qname);
    }

    //    protected IABCBytesRequestResult _handleABCBytesRequest(Operation buildPhase) throws InterruptedException
    //    {
    //        // If JSEmitter.needsSecondPass() returns true, JSGenerator.generate() will return null during scanning, 
    //        // which will result in JSCompilationUnit::handleSemanticProblemsRequest not caching any abcBytes for 
    //        // handleABCBytesRequest. The net result is that JSGenerator.generate() will be called again in handleABCBytesRequest. 
    //        // This mechanic will ensure selective two-pass compilation. 
    //        if (m_abcBytes != null &&
    //            !JSSharedData.instance.hasSymbols() && // Symbol support
    //            !JSSharedData.instance.hasAnyClassInit()) // support for class inits 
    //            return m_abcBytes;
    //
    //        JSGenerator jsGenerator = new JSGenerator();
    //        jsGenerator.m_compilationUnit = this;
    //        jsGenerator.setBuildPhase(buildPhase);
    //
    //        // Need to force the file scope request to happen first to get the ASFileScope
    //        // for this compilation unit registered with the project.
    //        // ** TODO this is a hack!
    //        getFileScopeRequest().get();
    //
    //        // This is also a hack!  If there are embed directives, need to ensure
    //        // semantic pass has finished, as that is what will generate the embed classes
    //        // which are needed by codegen
    //        if (buildPhase != Operation.GET_SEMANTIC_PROBLEMS)
    //        {
    //        	// AJH this was deadlocking as getOutgoingDependencies calls handleABCBytes
    //        	if (buildPhase != Operation.GET_ABC_BYTES)
    //        		getOutgoingDependenciesRequest().get();
    //        }
    //
    //        final ISyntaxTreeRequestResult fsr = getSyntaxTreeRequest().get();
    //        final IASNode rootNode = fsr.getAST();
    //
    //        startProfile(buildPhase);
    //        IABCBytesRequestResult result = jsGenerator.generate(getFilenameNoPath(), rootNode, this.getProject());
    //        stopProfile(buildPhase);
    //
    //        m_needsSecondPass = jsGenerator.needsSecondPass();
    //
    //        return result;
    //    }

    //   @Override
    //    protected IABCBytesRequestResult handleABCBytesRequest() throws InterruptedException
    //    {
    //        final IABCBytesRequestResult result = _handleABCBytesRequest(Operation.GET_ABC_BYTES);
    //
    //        /*
    //         * // explicitly reference all classes this class depends on if(
    //         * result.getProblems() == null || result.getProblems().length == 0 ) {
    //         * final String code = new String( result.getABCBytes() ); if(
    //         * code.contains(JSSharedData.REQUIRED_TAG_MARKER) ) { final
    //         * ICompilationUnit cu = this; final Set<ICompilationUnit> deps = new
    //         * HashSet<ICompilationUnit>(); deps.addAll(
    //         * getProject().getDependencies(cu) ); if( !deps.isEmpty() ) { String
    //         * depNames = ""; Boolean separator = false; final List<IDefinition>
    //         * defs = MXMLJSC.getClassDefinitions( cu ); for( IDefinition def: defs
    //         * ) { if( def instanceof ClassDefinition ) { final String defName =
    //         * JSGeneratingReducer.createFullNameFromDefinition(def); if( defName !=
    //         * null && !defName.isEmpty() ) { if( separator ) depNames += ":"; else
    //         * separator = true; depNames += defName; } } }
    //         * code.replaceFirst(JSSharedData.REQUIRED_TAG_MARKER, depNames); return
    //         * new ABCBytesRequestResult(code.getBytes(), result.getProblems()); } }
    //         * }
    //         */
    //        return result;
    //    }

    @Override
    protected IOutgoingDependenciesRequestResult handleOutgoingDependenciesRequest()
            throws InterruptedException
    {
        //        // Every CU is dependent on the class glue, which is implemented in browser.adobe.
        //        // Add dependency from this JSCompilationUnit to browser.adobe's JSCompilationUnit.
        //        addDependency(JSSharedData.JS_FRAMEWORK_NAME, DependencyType.INHERITANCE);
        //        addDependency(JSSharedData.FRAMEWORK_CLASS, DependencyType.INHERITANCE);

        IOutgoingDependenciesRequestResult result = super
                .handleOutgoingDependenciesRequest();

        //        // SWFTarget::startBuildAndFindAllCompilationUnits() is called by SWFTarget::collectProblems(), which is called by SWFTarget::addToSWF() in JSDriver::main().
        //        // This is our first pass. jsGenerator.generate() will return null if JSGeneratingReducer.getMember 
        //        // If JSEmitter.needsSecondPass() returns true, JSGenerator.generate() will return null during scanning, 
        //        // which will result in JSCompilationUnit::handleSemanticProblemsRequest not caching any abcBytes for 
        //        // handleABCBytesRequest. The net result is that JSGenerator.generate() will be called again in handleABCBytesRequest. 
        //        // This mechanic will ensure selective two-pass compilation. 
        //        if (result.getProblems().length == 0)
        //        {
        //            m_needsSecondPass = false;
        //            m_abcBytes = _handleABCBytesRequest(Operation.GET_SEMANTIC_PROBLEMS);
        //            if (m_needsSecondPass)
        //                m_abcBytes = null;
        //        }

        return result;
    }

    public Boolean addDependency(String className, DependencyType dt)
    {
        //        if (JSGeneratingReducer.isReservedDataType(className))
        //            return false;
        //
        //        final ICompilationUnit fromCU = this;
        //        final CompilerProject compilerProject = this.getProject();
        //        final ASProjectScope projectScope = compilerProject.getScope();
        //
        //        final IDefinition classDef = projectScope.findDefinitionByName(className);
        //        if (classDef == null)
        //            return false;
        //
        //        final ICompilationUnit toCU = projectScope.getCompilationUnitForDefinition(classDef);
        //        if (fromCU == toCU)
        //            return false;
        //
        //        // sharedData.verboseMessage( "Adding dependency: " + className );
        //        compilerProject.addDependency(fromCU, toCU, dt);

        return true;
    }

    @Override
    public void startBuildAsync(TargetType targetType)
    {
        // super.startBuildAsync(targetType);

        getSyntaxTreeRequest();
        getFileScopeRequest();
        getOutgoingDependenciesRequest();

        //        // scanning and code generating phases need to be separated
        //        // in order to create two distinct passes for m_needSecondPass.
        //        if (m_inCodeGen)
        //        {
        //            getABCBytesRequest();
        //            getSWFTagsRequest();
        //        }
    }

    @Override
    public void waitForBuildFinish(final Collection<ICompilerProblem> problems,
            TargetType targetType) throws InterruptedException
    {
        inCodeGen = true;
        super.waitForBuildFinish(problems, targetType);
        inCodeGen = false;
    }

}
