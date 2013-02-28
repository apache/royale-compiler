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

package org.apache.flex.compiler.internal.js.codegen;

import org.apache.flex.compiler.common.codegen.IDocEmitter;
import org.apache.flex.compiler.common.codegen.IEmitter;
import org.apache.flex.compiler.common.codegen.IEmitterTokens;
import org.apache.flex.compiler.internal.as.codegen.ASEmitterTokens;
import org.apache.flex.compiler.js.codegen.IJSEmitter;

public class JSDocEmitter implements IDocEmitter, IEmitter
{

    private int currentIndent = 0;

    @Override
    public void indentPush()
    {
        currentIndent++;
    }

    @Override
    public void indentPop()
    {
        currentIndent--;
    }

    @Override
    public void write(IEmitterTokens value)
    {
        write(value.getToken());
    }

    @Override
    public void write(String value)
    {
        emitter.write(value);
    }

    @Override
    public void writeNewline()
    {
        emitter.writeNewline();
    }

    @Override
    public void writeNewline(String value)
    {
        emitter.writeNewline(value);
    }

    @Override
    public void writeNewline(IEmitterTokens value)
    {
        emitter.writeNewline(value);
    }

    @Override
    public void writeNewline(String value, boolean pushIndent)
    {
        emitter.writeNewline(value, pushIndent);
    }

    @Override
    public void writeNewline(IEmitterTokens value, boolean pushIndent)
    {
        emitter.writeNewline(value, pushIndent);
    }

    @Override
    public void writeToken(IEmitterTokens value)
    {
        emitter.writeToken(value);
    }

    @Override
    public void writeToken(String value)
    {
        emitter.writeToken(value);
    }

    public void writeBlockClose()
    {
        emitter.write(ASEmitterTokens.BLOCK_CLOSE);
    }

    public void writeBlockOpen()
    {
        emitter.write(ASEmitterTokens.BLOCK_OPEN);
    }

    private IEmitter emitter;

    public JSDocEmitter(IJSEmitter emitter)
    {
        this.emitter = (IEmitter) emitter;
    }

    @Override
    public void begin()
    {
        writeNewline(JSDocEmitterTokens.JSDOC_OPEN);
    }

    @Override
    public void end()
    {
        write(ASEmitterTokens.SPACE);
        writeNewline(JSDocEmitterTokens.JSDOC_CLOSE);
    }

}
