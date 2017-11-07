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

package org.apache.royale.compiler.internal.driver.wast;

import java.io.FilterWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.royale.compiler.clients.WASTConfiguration;
import org.apache.royale.compiler.codegen.IDocEmitter;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.as.IASWriter;
import org.apache.royale.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.driver.IPublisher;
import org.apache.royale.compiler.driver.wast.IWASTBackend;
import org.apache.royale.compiler.internal.codegen.as.ASAfterNodeStrategy;
import org.apache.royale.compiler.internal.codegen.as.ASBeforeNodeStrategy;
import org.apache.royale.compiler.internal.codegen.as.ASBlockWalker;
import org.apache.royale.compiler.internal.codegen.as.ASFilterWriter;
import org.apache.royale.compiler.internal.codegen.wast.WASTEmitter;
import org.apache.royale.compiler.internal.codegen.wast.WASTPublisher;
import org.apache.royale.compiler.internal.codegen.wast.WASTWriter;
import org.apache.royale.compiler.internal.projects.ISourceFileHandler;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.targets.JSTarget;
import org.apache.royale.compiler.internal.visitor.as.ASNodeSwitch;
import org.apache.royale.compiler.internal.visitor.as.BeforeAfterStrategy;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.targets.ITarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.visitor.IBlockWalker;
import org.apache.royale.compiler.visitor.as.IASBlockWalker;
import org.apache.royale.compiler.visitor.mxml.IMXMLBlockWalker;

/**
 * A concrete implementation of the {@link IBackend} API where the
 * {@link ASBlockWalker} is used to traverse the {@link IFileNode} AST.
 * 
 * @author Erik de Bruin
 */
public class WASTBackend implements IWASTBackend {

	@Override
	public ISourceFileHandler getSourceFileHandlerInstance() {
        return WASTSourceFileHandler.INSTANCE;
	}

	@Override
	public String getOutputExtension() {
		return "wat";
	}

	@Override
	public Configurator createConfigurator() {
        return new Configurator(WASTConfiguration.class);
	}

	@Override
	public ITarget createTarget(RoyaleJSProject project, ITargetSettings settings, ITargetProgressMonitor monitor) {
        return new JSTarget(project, settings, monitor);
	}

	@Override
	public IDocEmitter createDocEmitter(IASEmitter emitter) {
        throw new UnsupportedOperationException();
	}

	@Override
	public IASEmitter createEmitter(FilterWriter writer) {
        return new WASTEmitter(writer);
	}

	@Override
	public IMXMLEmitter createMXMLEmitter(FilterWriter writer) {
        throw new UnsupportedOperationException();
	}

	@Override
	public ASFilterWriter createWriterBuffer(RoyaleJSProject project) {
        StringWriter out = new StringWriter();
        ASFilterWriter writer = new ASFilterWriter(out);
        return writer;
	}

	@Override
	public IASWriter createWriter(RoyaleJSProject project, List<ICompilerProblem> errors,
			ICompilationUnit compilationUnit, boolean enableDebug) {
        return new WASTWriter(project, errors, compilationUnit, enableDebug);
	}

	@Override
	public IASWriter createMXMLWriter(RoyaleJSProject project, List<ICompilerProblem> errors,
			ICompilationUnit compilationUnit, boolean enableDebug) {
        throw new UnsupportedOperationException();
	}

	@Override
	public IASBlockWalker createWalker(RoyaleJSProject project, List<ICompilerProblem> errors, IASEmitter emitter) {
        ASBlockWalker walker = new ASBlockWalker(errors, project, emitter);

        BeforeAfterStrategy strategy = new BeforeAfterStrategy(
                new ASNodeSwitch(walker), new ASBeforeNodeStrategy(emitter),
                new ASAfterNodeStrategy(emitter));

        walker.setStrategy(strategy);

        return walker;
	}

	@Override
	public IPublisher createPublisher(RoyaleJSProject project, List<ICompilerProblem> errors, Configuration config) {
        return new WASTPublisher(project, config);
	}

	@Override
	public IMXMLBlockWalker createMXMLWalker(RoyaleJSProject project, List<ICompilerProblem> errors,
			IMXMLEmitter mxmlEmitter, IASEmitter asEmitter, IBlockWalker asBlockWalker) {
        throw new UnsupportedOperationException();
	}

}
