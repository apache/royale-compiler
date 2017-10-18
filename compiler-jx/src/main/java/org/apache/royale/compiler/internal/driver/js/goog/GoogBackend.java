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

package org.apache.royale.compiler.internal.driver.js.goog;

import java.io.FilterWriter;
import java.util.List;

import org.apache.royale.compiler.codegen.IDocEmitter;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogPublisher;
import org.apache.royale.compiler.internal.driver.js.JSBackend;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * A concrete implementation of the {@link IBackend} API for the 'goog' code
 * production.
 * 
 * @author Michael Schmalle
 */
public class GoogBackend extends JSBackend
{

    @Override
    public Configurator createConfigurator()
    {
        return new Configurator(JSGoogConfiguration.class);
    }

    @Override
    public IDocEmitter createDocEmitter(IASEmitter emitter)
    {
        return new JSGoogDocEmitter((IJSEmitter) emitter);
    }

    @Override
    public IJSEmitter createEmitter(FilterWriter out)
    {
        IJSEmitter emitter = new JSGoogEmitter(out);
        emitter.setDocEmitter(createDocEmitter(emitter));
        return emitter;
    }

    @Override
    public JSGoogPublisher createPublisher(RoyaleJSProject project,
            List<ICompilerProblem> errors, Configuration config)
    {
        return new JSGoogPublisher(project, config);
    }
}
