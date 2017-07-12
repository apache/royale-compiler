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

import java.util.List;

import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.mxml.MXMLBlockWalker;
import org.apache.flex.compiler.internal.codegen.mxml.flexjs.MXMLFlexJSCordovaPublisher;
import org.apache.flex.compiler.internal.codegen.mxml.flexjs.MXMLFlexJSPublisher;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;

/**
 * A concrete implementation of the {@link IBackend} API where the
 * {@link MXMLBlockWalker} is used to traverse the {@link IMXMLFileNode} AST.
 * 
 * @author Erik de Bruin
 */
public class MXMLFlexJSCordovaBackend extends MXMLFlexJSBackend
{

    @Override
    public Configurator createConfigurator()
    {
        return new Configurator(JSGoogConfiguration.class);
    }

    @Override
    public MXMLFlexJSPublisher createPublisher(FlexJSProject project,
            List<ICompilerProblem> errors, Configuration config)
    {
        return new MXMLFlexJSCordovaPublisher(config, project);
    }
}
