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

package org.apache.flex.compiler.internal.codegen.externals;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.flex.compiler.internal.codegen.externals.emit.ReferenceEmitter;
import org.apache.flex.compiler.internal.codegen.externals.pass.ReferenceCompiler;
import org.apache.flex.compiler.internal.codegen.externals.reference.ReferenceModel;

import com.google.javascript.jscomp.Result;

public class ExternalsClient
{
    private ExternalsClientConfig configuration;
    private ReferenceModel model;
    private ReferenceCompiler compiler;
    private ReferenceEmitter emitter;

    public ReferenceModel getModel()
    {
        return model;
    }

    public ExternalsClient(ExternalsClientConfig config)
    {
        this.configuration = config;

        model = new ReferenceModel(config);
        compiler = new ReferenceCompiler(model);
        emitter = new ReferenceEmitter(model);
    }

    public void cleanOutput() throws IOException
    {
        FileUtils.deleteDirectory(configuration.getAsRoot());
    }

    public void emit() throws IOException
    {
        emitter.emit();
    }

    public Result compile() throws IOException
    {
        return compiler.compile();
    }

}
