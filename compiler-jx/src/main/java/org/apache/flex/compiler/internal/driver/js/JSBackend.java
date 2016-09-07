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

package org.apache.flex.compiler.internal.driver.js;

import java.io.FilterWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.flex.compiler.clients.JSConfiguration;
import org.apache.flex.compiler.codegen.IDocEmitter;
import org.apache.flex.compiler.codegen.ISourceMapEmitter;
import org.apache.flex.compiler.codegen.as.IASEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.codegen.js.IJSWriter;
import org.apache.flex.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.driver.IPublisher;
import org.apache.flex.compiler.driver.js.IJSBackend;
import org.apache.flex.compiler.internal.codegen.as.ASAfterNodeStrategy;
import org.apache.flex.compiler.internal.codegen.as.ASBeforeNodeStrategy;
import org.apache.flex.compiler.internal.codegen.as.ASBlockWalker;
import org.apache.flex.compiler.internal.codegen.js.JSDocEmitter;
import org.apache.flex.compiler.internal.codegen.js.JSEmitter;
import org.apache.flex.compiler.internal.codegen.js.JSFilterWriter;
import org.apache.flex.compiler.internal.codegen.js.JSPublisher;
import org.apache.flex.compiler.internal.codegen.js.JSSourceMapEmitter;
import org.apache.flex.compiler.internal.codegen.js.JSWriter;
import org.apache.flex.compiler.internal.projects.ISourceFileHandler;
import org.apache.flex.compiler.internal.targets.JSTarget;
import org.apache.flex.compiler.internal.visitor.as.ASNodeSwitch;
import org.apache.flex.compiler.internal.visitor.as.BeforeAfterStrategy;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.targets.ITargetProgressMonitor;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.visitor.IBlockWalker;
import org.apache.flex.compiler.visitor.as.IASBlockWalker;
import org.apache.flex.compiler.visitor.mxml.IMXMLBlockWalker;

/**
 * A concrete implementation of the {@link IBackend} API where the
 * {@link ASBlockWalker} is used to traverse the {@link IFileNode} AST.
 * 
 * @author Michael Schmalle
 */
public class JSBackend implements IJSBackend
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
            List<ICompilerProblem> errors, IMXMLEmitter mxmlEmitter,
            IASEmitter asEmitter, IBlockWalker asBlockWalker)
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
    public IJSWriter createMXMLWriter(IASProject project,
            List<ICompilerProblem> problems, ICompilationUnit compilationUnit,
            boolean enableDebug)
    {
        return null;
    }

    @Override
    public ISourceMapEmitter createSourceMapEmitter(IJSEmitter emitter)
    {
        return new JSSourceMapEmitter(emitter);
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

    @Override
    public IPublisher createPublisher(IASProject project,
            List<ICompilerProblem> errors, Configuration config)
    {
        return new JSPublisher(config);
    }
}
