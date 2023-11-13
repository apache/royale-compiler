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

package org.apache.royale.compiler.internal.driver.js.royale;

import java.io.FilterWriter;
import java.util.List;

import org.apache.royale.compiler.codegen.IDocEmitter;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.mxml.royale.MXMLRoyalePublisher;
import org.apache.royale.compiler.internal.driver.js.JSBackend;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.targets.RoyaleJSTarget;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.internal.targets.JSTarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetSettings;

/**
 * @author Erik de Bruin
 */
public class RoyaleBackend extends JSBackend
{

    @Override
    public IJSEmitter createEmitter(FilterWriter out)
    {
        IJSEmitter emitter = new JSRoyaleEmitter(out);
        emitter.setDocEmitter(createDocEmitter(emitter));
        return emitter;
    }

    @Override
    public JSTarget createTarget(RoyaleJSProject project, ITargetSettings settings,
                                 ITargetProgressMonitor monitor)
    {
        return new RoyaleJSTarget(project, settings, monitor);
    }


    @Override
    public Configurator createConfigurator()
    {
        return new Configurator(JSGoogConfiguration.class);
    }

    @Override
    public IDocEmitter createDocEmitter(IASEmitter emitter)
    {
        return new JSRoyaleDocEmitter((IJSEmitter) emitter);
    }

    @Override
    public MXMLRoyalePublisher createPublisher(RoyaleJSProject project,
            List<ICompilerProblem> errors, Configuration config)
    {
        return new MXMLRoyalePublisher(project, config);
    }
}
