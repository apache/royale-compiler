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

package org.apache.flex.compiler.internal.as.driver;

import java.io.FilterWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.flex.compiler.as.codegen.IASEmitter;
import org.apache.flex.compiler.as.codegen.IASWriter;
import org.apache.flex.compiler.as.codegen.IDocEmitter;
import org.apache.flex.compiler.clients.IBackend;
import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.internal.as.codegen.ASBlockWalker;
import org.apache.flex.compiler.internal.as.codegen.ASEmitter;
import org.apache.flex.compiler.internal.as.codegen.ASFilterWriter;
import org.apache.flex.compiler.internal.as.codegen.ASWriter;
import org.apache.flex.compiler.internal.as.codegen.ASAfterNodeStrategy;
import org.apache.flex.compiler.internal.as.codegen.ASBeforeNodeStrategy;
import org.apache.flex.compiler.internal.as.visitor.ASNodeSwitch;
import org.apache.flex.compiler.internal.as.visitor.BeforeAfterStrategy;
import org.apache.flex.compiler.internal.projects.ISourceFileHandler;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.targets.ITarget;
import org.apache.flex.compiler.targets.ITargetProgressMonitor;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.visitor.IMXMLBlockWalker;

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
    public ITarget createTarget(IASProject project, ITargetSettings settings,
            ITargetProgressMonitor monitor)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ASBlockWalker createWalker(IASProject project,
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
    public ASFilterWriter createWriterBuffer(IASProject project)
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
    public IASWriter createWriter(IASProject project,
            List<ICompilerProblem> problems, ICompilationUnit compilationUnit,
            boolean enableDebug)
    {
        return new ASWriter(project, problems, compilationUnit, enableDebug);
    }

    @Override
    public IDocEmitter createDocEmitter(IASEmitter emitter)
    {
        return null;
    }

    @Override
    public IMXMLBlockWalker createMXMLWalker(IASEmitter emitter,
            IASProject project, List<ICompilerProblem> errors)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
