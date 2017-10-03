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

package org.apache.flex.compiler.internal.driver.as;

import java.io.FilterWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.flex.compiler.codegen.IDocEmitter;
import org.apache.flex.compiler.codegen.as.IASEmitter;
import org.apache.flex.compiler.codegen.as.IASWriter;
import org.apache.flex.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.driver.IPublisher;
import org.apache.flex.compiler.internal.codegen.as.ASAfterNodeStrategy;
import org.apache.flex.compiler.internal.codegen.as.ASBeforeNodeStrategy;
import org.apache.flex.compiler.internal.codegen.as.ASBlockWalker;
import org.apache.flex.compiler.internal.codegen.as.ASEmitter;
import org.apache.flex.compiler.internal.codegen.as.ASFilterWriter;
import org.apache.flex.compiler.internal.codegen.as.ASWriter;
import org.apache.flex.compiler.internal.codegen.mxml.MXMLEmitter;
import org.apache.flex.compiler.internal.projects.RoyaleProject;
import org.apache.flex.compiler.internal.projects.ISourceFileHandler;
import org.apache.flex.compiler.internal.visitor.as.ASNodeSwitch;
import org.apache.flex.compiler.internal.visitor.as.BeforeAfterStrategy;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.targets.ITarget;
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
public class ASBackend implements IBackend
{
    @Override
    public String getOutputExtension()
    {
        return "as";
    }

    @Override
    public ISourceFileHandler getSourceFileHandlerInstance()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configurator createConfigurator()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ITarget createTarget(RoyaleProject project, ITargetSettings settings,
                                ITargetProgressMonitor monitor)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public IASBlockWalker createWalker(RoyaleProject project,
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
    public IMXMLBlockWalker createMXMLWalker(RoyaleProject project,
            List<ICompilerProblem> errors, IMXMLEmitter mxmlEmitter,
            IASEmitter asEmitter, IBlockWalker asBlockWalker)
    {
        return null;
    }

    @Override
    public ASFilterWriter createWriterBuffer(RoyaleProject project)
    {
        StringWriter out = new StringWriter();
        ASFilterWriter writer = new ASFilterWriter(out);
        return writer;
    }

    @Override
    public IASEmitter createEmitter(FilterWriter writer)
    {
        return new ASEmitter(writer);
    }

    @Override
    public IMXMLEmitter createMXMLEmitter(FilterWriter writer)
    {
        return new MXMLEmitter(writer);
    }

    @Override
    public IASWriter createWriter(RoyaleProject project,
            List<ICompilerProblem> problems, ICompilationUnit compilationUnit,
            boolean enableDebug)
    {
        return new ASWriter(project, problems, compilationUnit, enableDebug);
    }

    @Override
    public IASWriter createMXMLWriter(RoyaleProject project,
            List<ICompilerProblem> problems, ICompilationUnit compilationUnit,
            boolean enableDebug)
    {
        return null;
    }

    @Override
    public IDocEmitter createDocEmitter(IASEmitter emitter)
    {
        return null;
    }

    @Override
    public IPublisher createPublisher(RoyaleProject project,
            List<ICompilerProblem> errors, Configuration config)
    {
        return null;
    }
}
