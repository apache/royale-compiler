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

package org.apache.flex.compiler.driver;

import java.io.File;
import java.io.FilterWriter;
import java.util.List;

import org.apache.flex.compiler.clients.MXMLJSC;
import org.apache.flex.compiler.codegen.IDocEmitter;
import org.apache.flex.compiler.codegen.as.IASEmitter;
import org.apache.flex.compiler.codegen.as.IASWriter;
import org.apache.flex.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.internal.codegen.as.ASFilterWriter;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.projects.ISourceFileHandler;
import org.apache.flex.compiler.internal.targets.JSTarget;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.targets.ITarget;
import org.apache.flex.compiler.targets.ITargetProgressMonitor;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.visitor.IBlockWalker;
import org.apache.flex.compiler.visitor.as.IASBlockWalker;
import org.apache.flex.compiler.visitor.mxml.IMXMLBlockWalker;

/**
 * The backend strategy for the {@link MXMLJSC} javascript compiler.
 * 
 * @author Michael Schmalle
 */
public interface IBackend
{

    /**
     * Returns the instance that is used to manage what type of
     * {@link ICompilationUnit} is created during parsing.
     * 
     * @return The implemented {@link ISourceFileHandler}.
     */
    ISourceFileHandler getSourceFileHandlerInstance();

    /**
     * Returns the {@link File} extension used when saving compiled code.
     */
    String getOutputExtension();

    /**
     * Creates a {@link Configurator} for the specific compile session.
     */
    Configurator createConfigurator();

    /**
     * Creates a javascript target that will be used to build the compiled
     * javascript source file.
     * 
     * @param project The current {@link FlexJSProject}.
     * @param settings The target's custom settings.
     * @param monitor The compilation monitor used during asynchronous parsing
     *        of {@link ICompilationUnit}s.
     * @return A new {@link JSTarget} used during compilation.
     */
    ITarget createTarget(FlexJSProject project, ITargetSettings settings,
            ITargetProgressMonitor monitor);

    IDocEmitter createDocEmitter(IASEmitter emitter);

    IASEmitter createEmitter(FilterWriter writer);

    IMXMLEmitter createMXMLEmitter(FilterWriter writer);

    ASFilterWriter createWriterBuffer(FlexJSProject project);

    IASWriter createWriter(FlexJSProject project, List<ICompilerProblem> errors,
                           ICompilationUnit compilationUnit, boolean enableDebug);

    IASWriter createMXMLWriter(FlexJSProject project,
            List<ICompilerProblem> errors, ICompilationUnit compilationUnit,
            boolean enableDebug);

    IASBlockWalker createWalker(FlexJSProject project,
            List<ICompilerProblem> errors, IASEmitter emitter);

    IPublisher createPublisher(FlexJSProject project,
            List<ICompilerProblem> errors, Configuration config);

    /**
     * Creates an AST walker capable of traversing MXML AST and calling back to
     * the {@link IASBlockWalker} for ActionScript source code production.
     * <p>
     * Use the {@link #createWalker(FlexJSProject, List, ASFilterWriter)} method
     * first and pass that instance into this method's <code>walker</code>
     * parameter.
     * 
     * @param project The current {@link FlexJSProject}.
     * @param errors The current {@link ICompilerProblem} list.
     * @param emitter The current {@link IASEmitter} that is used for it's
     *        emitter and is available for callbacks to it's visit methods.
     */
    IMXMLBlockWalker createMXMLWalker(FlexJSProject project,
            List<ICompilerProblem> errors, IMXMLEmitter mxmlEmitter,
            IASEmitter asEmitter, IBlockWalker asBlockWalker);

}
