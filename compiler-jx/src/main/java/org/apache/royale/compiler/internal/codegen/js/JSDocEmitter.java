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

package org.apache.royale.compiler.internal.codegen.js;

import org.apache.royale.compiler.codegen.IDocEmitter;
import org.apache.royale.compiler.codegen.IEmitter;
import org.apache.royale.compiler.codegen.IEmitterTokens;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.tree.as.IASNode;

public class JSDocEmitter implements IDocEmitter, IEmitter
{

    @SuppressWarnings("unused")
	private int currentIndent = 0;

    protected IEmitter emitter;

    private StringBuilder builder;

    protected StringBuilder getBuilder()
    {
        return builder;
    }

    private boolean bufferWrite;

    public boolean isBufferWrite()
    {
        return bufferWrite;
    }

    public void setBufferWrite(boolean value)
    {
        bufferWrite = value;
    }

    public String flushBuffer()
    {
        setBufferWrite(false);
        String result = builder.toString();
        builder.setLength(0);
        return result;
    }

    public JSDocEmitter(IJSEmitter emitter)
    {
        this.emitter = (IEmitter) emitter;
        
        builder = new StringBuilder();
    }

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
        if (!bufferWrite)
            emitter.write(value);
        else
            builder.append(value);
    }

    @Override
    public void writeNewline()
    {
        write(ASEmitterTokens.NEW_LINE);
    }

    @Override
    public void writeNewline(String value)
    {
        write(value);
        writeNewline();
    }

    @Override
    public void writeNewline(IEmitterTokens value)
    {
        writeNewline(value.getToken());
    }

    @Override
    public void writeNewline(String value, boolean pushIndent)
    {
        if (pushIndent)
            indentPush();
        else
            indentPop();
        write(value);
        writeNewline();
    }

    @Override
    public void writeNewline(IEmitterTokens value, boolean pushIndent)
    {
        writeNewline(value.getToken(), pushIndent);
    }

    @Override
    public void writeToken(IEmitterTokens value)
    {
        writeToken(value.getToken());
    }

    @Override
    public void writeToken(String value)
    {
        write(value);
        write(ASEmitterTokens.SPACE);
    }

    public void writeBlockClose()
    {
        write(ASEmitterTokens.BLOCK_CLOSE);
    }

    public void writeBlockOpen()
    {
        write(ASEmitterTokens.BLOCK_OPEN);
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
    
    @Override
    public String stringifyNode(IASNode node)
    {
        return null;
    }
}
