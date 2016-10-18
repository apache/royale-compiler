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

package org.apache.flex.compiler.internal.driver.mxml.flexjs;

import java.io.FilterWriter;
import java.util.List;

import org.apache.flex.compiler.codegen.IDocEmitter;
import org.apache.flex.compiler.codegen.as.IASEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.codegen.js.IJSWriter;
import org.apache.flex.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSASDocDITAEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSASDocEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogDocEmitter;
import org.apache.flex.compiler.internal.codegen.mxml.MXMLBlockWalker;
import org.apache.flex.compiler.internal.codegen.mxml.MXMLWriter;
import org.apache.flex.compiler.internal.codegen.mxml.flexjs.MXMLFlexJSASDocEmitter;
import org.apache.flex.compiler.internal.codegen.mxml.flexjs.MXMLFlexJSBlockWalker;
import org.apache.flex.compiler.internal.driver.js.goog.ASDocConfiguration;
import org.apache.flex.compiler.internal.driver.mxml.ASDocASSourceFileHandler;
import org.apache.flex.compiler.internal.projects.ISourceFileHandler;
import org.apache.flex.compiler.internal.targets.FlexJSSWCTarget;
import org.apache.flex.compiler.internal.targets.JSTarget;
import org.apache.flex.compiler.internal.visitor.as.ASNodeSwitch;
import org.apache.flex.compiler.internal.visitor.mxml.MXMLNodeSwitch;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.targets.ITargetProgressMonitor;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.visitor.IBlockVisitor;
import org.apache.flex.compiler.visitor.IBlockWalker;
import org.apache.flex.compiler.visitor.mxml.IMXMLBlockWalker;

/**
 * A concrete implementation of the {@link IBackend} API where the
 * {@link MXMLBlockWalker} is used to traverse the {@link IMXMLFileNode} AST.
 * 
 * @author Erik de Bruin
 */
public class MXMLFlexJSASDocDITABackend extends MXMLFlexJSSWCBackend
{

    @Override
    public Configurator createConfigurator()
    {
        return new Configurator(ASDocConfiguration.class);
    }

    @Override
    public IMXMLEmitter createMXMLEmitter(FilterWriter out)
    {
        return new MXMLFlexJSASDocEmitter(out);
    }

    @Override
    public IMXMLBlockWalker createMXMLWalker(IASProject project,
            List<ICompilerProblem> errors, IMXMLEmitter mxmlEmitter,
            IASEmitter asEmitter, IBlockWalker asBlockWalker)
    {
        MXMLBlockWalker walker = new MXMLFlexJSBlockWalker(errors, project,
                mxmlEmitter, asEmitter, asBlockWalker);

        ASNodeSwitch asStrategy = new ASNodeSwitch(
                (IBlockVisitor) asBlockWalker);
        walker.setASStrategy(asStrategy);

        MXMLNodeSwitch mxmlStrategy = new MXMLNodeSwitch(walker);
        walker.setMXMLStrategy(mxmlStrategy);

        return walker;
    }

    @Override
    public IDocEmitter createDocEmitter(IASEmitter emitter)
    {
        return new JSGoogDocEmitter((IJSEmitter) emitter);
    }

    @Override
    public IJSEmitter createEmitter(FilterWriter out)
    {
        IJSEmitter emitter = new JSFlexJSASDocDITAEmitter(out);
        emitter.setDocEmitter(createDocEmitter(emitter));
        return emitter;
    }
    
    @Override
    public IJSWriter createMXMLWriter(IASProject project,
            List<ICompilerProblem> problems, ICompilationUnit compilationUnit,
            boolean enableDebug)
    {
        return new MXMLWriter(project, problems, compilationUnit, enableDebug);
    }

    @Override
    public JSTarget createTarget(IASProject project, ITargetSettings settings,
            ITargetProgressMonitor monitor)
    {
        return new FlexJSSWCTarget(project, settings, monitor);
    }
    
    @Override
    public ISourceFileHandler getSourceFileHandlerInstance()
    {
        return ASDocASSourceFileHandler.INSTANCE;
    }

    @Override
    public String getOutputExtension()
    {
        return "xml";
    }

}
