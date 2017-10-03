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

package org.apache.royale.compiler.internal.driver.js.amd;

import java.io.FilterWriter;

import org.apache.royale.compiler.codegen.IDocEmitter;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.js.amd.JSAMDDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.amd.JSAMDEmitter;
import org.apache.royale.compiler.internal.driver.js.JSBackend;

/**
 * A concrete implementation of the {@link IBackend} API for 'AMD' code
 * production.
 * 
 * @author Michael Schmalle
 */
public class AMDBackend extends JSBackend
{
    @Override
    public IDocEmitter createDocEmitter(IASEmitter emitter)
    {
        return new JSAMDDocEmitter((IJSEmitter) emitter);
    }

    @Override
    public IJSEmitter createEmitter(FilterWriter out)
    {
        IJSEmitter emitter = new JSAMDEmitter(out);
        emitter.setDocEmitter(createDocEmitter(emitter));
        return emitter;
    }
}
