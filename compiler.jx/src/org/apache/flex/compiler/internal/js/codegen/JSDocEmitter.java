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

import org.apache.flex.compiler.internal.as.codegen.ASEmitter;
import org.apache.flex.compiler.js.codegen.IJSDocEmitter;
import org.apache.flex.compiler.js.codegen.IJSEmitter;

public class JSDocEmitter implements IJSDocEmitter
{
    public static final String JSDOC_CLOSE = "*/";
    public static final String JSDOC_OPEN = "/**";

    public void write(String value)
    {
        emitter.write(value);
    }

    public void writeBlockClose()
    {
    	emitter.write(ASEmitter.CURLYBRACE_CLOSE);
    }

    public void writeBlockOpen()
    {
    	emitter.write(ASEmitter.CURLYBRACE_OPEN);
    }
    
    public void writeNewline()
    {
    	emitter.writeNewline();
    }

    public void writeSpace()
    {
    	emitter.write(ASEmitter.SPACE);
    }

    private IJSEmitter emitter;

    public JSDocEmitter(IJSEmitter emitter)
    {
        this.emitter = emitter;
    }

    @Override
    public void begin()
    {
        write(JSDOC_OPEN);
        writeNewline();
    }

    @Override
    public void end()
    {
    	writeSpace();
        write(JSDOC_CLOSE);
        writeNewline();
    }

}
