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

package org.apache.royale.compiler.driver.js;

import java.util.List;

import org.apache.royale.compiler.codegen.ISourceMapEmitter;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.as.IASWriter;
import org.apache.royale.compiler.codegen.js.IMappingEmitter;
import org.apache.royale.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.driver.IPublisher;
import org.apache.royale.compiler.internal.codegen.as.ASFilterWriter;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.targets.JSTarget;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.targets.ITarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.visitor.IBlockWalker;
import org.apache.royale.compiler.visitor.as.IASBlockWalker;
import org.apache.royale.compiler.visitor.mxml.IMXMLBlockWalker;

public interface IJSBackend extends IBackend
{
    /**
     * Creates an AST walker capable of traversing MXML AST and calling back to
     * the {@link IASBlockWalker} for ActionScript source code production.
     * <p>
     * Use the {@link #createWalker(RoyaleJSProject, List, ASFilterWriter)} method
     * first and pass that instance into this method's <code>walker</code>
     * parameter.
     * 
     * @param project The current {@link RoyaleJSProject}.
     * @param errors The current {@link ICompilerProblem} list.
     * @param emitter The current {@link IASEmitter} that is used for it's
     *        emitter and is available for callbacks to it's visit methods.
     */
    IMXMLBlockWalker createMXMLWalker(RoyaleJSProject project,
            List<ICompilerProblem> errors, IMXMLEmitter mxmlEmitter,
            IASEmitter asEmitter, IBlockWalker asBlockWalker);

    IASWriter createMXMLWriter(RoyaleJSProject project,
            List<ICompilerProblem> errors, ICompilationUnit compilationUnit,
            boolean enableDebug);

    IPublisher createPublisher(RoyaleJSProject project,
            List<ICompilerProblem> errors, Configuration config);

    ISourceMapEmitter createSourceMapEmitter(IMappingEmitter emitter);

    /**
     * Creates a javascript target that will be used to build the compiled
     * javascript source file.
     * 
     * @param project The current {@link RoyaleJSProject}.
     * @param settings The target's custom settings.
     * @param monitor The compilation monitor used during asynchronous parsing
     *        of {@link ICompilationUnit}s.
     * @return A new {@link JSTarget} used during compilation.
     */
    ITarget createTarget(RoyaleJSProject project, ITargetSettings settings,
            ITargetProgressMonitor monitor);

    IASBlockWalker createWalker(RoyaleJSProject project,
            List<ICompilerProblem> errors, IASEmitter emitter);

    IASWriter createWriter(RoyaleJSProject project, List<ICompilerProblem> errors,
                           ICompilationUnit compilationUnit, boolean enableDebug);

    ASFilterWriter createWriterBuffer(RoyaleJSProject project);

}
