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

package org.apache.flex.compiler.internal.codegen.js;

import org.apache.flex.compiler.codegen.IEmitterTokens;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.projects.ICompilerProject;

public class JSSubEmitter
{
    private IJSEmitter emitter;

    protected IJSEmitter getEmitter()
    {
        return emitter; 
    }

    protected ICompilerProject getProject()
    {
        return emitter.getWalker().getProject();
    }

    protected JSSessionModel getModel()
    {
        return emitter.getModel();
    }

    public JSSubEmitter(IJSEmitter emitter)
    {
        this.emitter = emitter;
    }

    protected void write(IEmitterTokens value)
    {
        emitter.write(value);
    }

    protected void write(String value)
    {
        emitter.write(value);
    }

    protected void writeToken(IEmitterTokens value)
    {
        emitter.writeToken(value);
    }

    protected void writeToken(String value)
    {
        emitter.writeToken(value);
    }

    protected void writeNewline()
    {
        emitter.writeNewline();
    }

    protected void writeNewline(IEmitterTokens value)
    {
        emitter.writeNewline(value);
    }

    protected void writeNewline(String value)
    {
        emitter.writeNewline(value);
    }

    protected void writeNewline(String value, boolean pushIndent)
    {
        emitter.writeNewline(value, pushIndent);
    }

    protected void indentPush()
    {
        emitter.indentPush();
    }

    protected void indentPop()
    {
        emitter.indentPop();
    }
}
