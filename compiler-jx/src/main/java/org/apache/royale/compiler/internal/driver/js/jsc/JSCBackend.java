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

package org.apache.flex.compiler.internal.driver.js.jsc;

import java.util.List;

import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.jsc.JSCPublisher;
import org.apache.flex.compiler.internal.codegen.mxml.flexjs.MXMLRoyalePublisher;
import org.apache.flex.compiler.internal.driver.mxml.jsc.MXMLJSCJSBackend;
import org.apache.flex.compiler.internal.projects.RoyaleProject;
import org.apache.flex.compiler.problems.ICompilerProblem;

/**
 * A concrete implementation of the {@link IBackend} API for the 'jsc' code
 * production.
 * 
 * @author Michael Schmalle
 */
public class JSCBackend extends MXMLJSCJSBackend
{
    @Override
    public MXMLRoyalePublisher createPublisher(RoyaleProject project,
            List<ICompilerProblem> errors, Configuration config)
    {
        return new JSCPublisher(project, config);
    }
}
