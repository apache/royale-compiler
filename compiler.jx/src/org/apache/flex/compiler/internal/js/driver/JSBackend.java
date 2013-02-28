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

package org.apache.flex.compiler.internal.js.driver;

import java.io.FilterWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.flex.compiler.as.codegen.IASEmitter;
import org.apache.flex.compiler.clients.JSConfiguration;
import org.apache.flex.compiler.common.codegen.IDocEmitter;
import org.apache.flex.compiler.common.driver.IBackend;
import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.internal.as.codegen.ASAfterNodeStrategy;
import org.apache.flex.compiler.internal.as.codegen.ASBeforeNodeStrategy;
import org.apache.flex.compiler.internal.as.codegen.ASBlockWalker;
import org.apache.flex.compiler.internal.as.visitor.ASNodeSwitch;
import org.apache.flex.compiler.internal.as.visitor.BeforeAfterStrategy;
import org.apache.flex.compiler.internal.js.codegen.JSDocEmitter;
import org.apache.flex.compiler.internal.js.codegen.JSEmitter;
import org.apache.flex.compiler.internal.js.codegen.JSFilterWriter;
import org.apache.flex.compiler.internal.js.codegen.JSWriter;
import org.apache.flex.compiler.internal.projects.ISourceFileHandler;
import org.apache.flex.compiler.internal.targets.JSTarget;
import org.apache.flex.compiler.js.codegen.IJSEmitter;
import org.apache.flex.compiler.js.codegen.IJSWriter;
import org.apache.flex.compiler.mxml.codegen.IMXMLEmitter;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.targets.ITargetProgressMonitor;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.visitor.IASBlockWalker;
import org.apache.flex.compiler.visitor.IMXMLBlockWalker;

/**
 * A concrete implementation of the {@link IBackend} API where the
 * {@link ASBlockWalker} is used to traverse the {@link IFileNode} AST.
 * 
 * @author Michael Schmalle
 */
public class JSBackend implements IBackend
{

    @Override
    public String getOutputExtension()
    {
        return "js";
    }

    @Override
    public ISourceFileHandler getSourceFileHandlerInstance()
    {
        return JSSourceFileHandler.INSTANCE;
    }

    @Override
    public Configurator createConfigurator()
    {
        return new Configurator(JSConfiguration.class);
    }

    @Override
    public JSTarget createTarget(IASProject project, ITargetSettings settings,
            ITargetProgressMonitor monitor)
    {
        return new JSTarget(project, settings, monitor);
    }

    @Override
    public IASBlockWalker createWalker(IASProject project,
            List<ICompilerProblem> errors, IASEmitter emitter)
    {
        ASBlockWalker walker = new ASBlockWalker(errors, project, emitter);

        BeforeAfterStrategy strategy = new BeforeAfterStrategy(
                new ASNodeSwitch(walker), new ASBeforeNodeStrategy(emitter),
                new ASAfterNodeStrategy(emitter));

        walker.setStrategy(strategy);

        return walker;
    }

    @Override
    public IMXMLBlockWalker createMXMLWalker(IASProject project,
            List<ICompilerProblem> errors, IMXMLEmitter emitter)
    {
        return null;
    }

    @Override
    public JSFilterWriter createWriterBuffer(IASProject project)
    {
        StringWriter out = new StringWriter();
        JSFilterWriter writer = new JSFilterWriter(out);
        return writer;
    }

    @Override
    public IJSWriter createWriter(IASProject project,
            List<ICompilerProblem> problems, ICompilationUnit compilationUnit,
            boolean enableDebug)
    {
        return new JSWriter(project, problems, compilationUnit, enableDebug);
    }

    @Override
    public IDocEmitter createDocEmitter(IASEmitter emitter)
    {
        return new JSDocEmitter((IJSEmitter) emitter);
    }

    @Override
    public IASEmitter createEmitter(FilterWriter out)
    {
        return new JSEmitter(out);
    }

    @Override
    public IMXMLEmitter createMXMLEmitter(FilterWriter out)
    {
        return null;
    }

}
