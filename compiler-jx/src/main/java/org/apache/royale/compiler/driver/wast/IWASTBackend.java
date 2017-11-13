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

package org.apache.royale.compiler.driver.wast;

import java.util.List;

import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.as.IASWriter;
import org.apache.royale.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.royale.compiler.codegen.wast.IWASTPublisher;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.as.ASFilterWriter;
import org.apache.royale.compiler.internal.projects.RoyaleWASTProject;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.targets.ITarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.visitor.IBlockWalker;
import org.apache.royale.compiler.visitor.as.IASBlockWalker;
import org.apache.royale.compiler.visitor.mxml.IMXMLBlockWalker;

public interface IWASTBackend extends IBackend {

    IMXMLBlockWalker createMXMLWalker(RoyaleWASTProject project,
            List<ICompilerProblem> errors, IMXMLEmitter mxmlEmitter,
            IASEmitter asEmitter, IBlockWalker asBlockWalker);

    IASWriter createMXMLWriter(RoyaleWASTProject project,
            List<ICompilerProblem> errors, ICompilationUnit compilationUnit,
            boolean enableDebug);

    IWASTPublisher createPublisher(RoyaleWASTProject project,
            List<ICompilerProblem> errors, Configuration config);

    ITarget createTarget(RoyaleWASTProject project, ITargetSettings settings,
            ITargetProgressMonitor monitor);

    IASBlockWalker createWalker(RoyaleWASTProject project,
            List<ICompilerProblem> errors, IASEmitter emitter);

    IASWriter createWriter(RoyaleWASTProject project, List<ICompilerProblem> errors,
                           ICompilationUnit compilationUnit, boolean enableDebug);

    ASFilterWriter createWriterBuffer(RoyaleWASTProject project);

}
