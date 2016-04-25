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

import java.io.IOException;
import java.util.List;

import org.apache.flex.compiler.codegen.ISourceMapEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;

import com.google.debugging.sourcemap.SourceMapGeneratorV3;

public class JSSourceMapEmitter implements ISourceMapEmitter
{
    private IJSEmitter emitter;
    private SourceMapGeneratorV3 sourceMapGenerator;

    public JSSourceMapEmitter(IJSEmitter emitter)
    {
        this.emitter = emitter;
        sourceMapGenerator = new SourceMapGeneratorV3();
    }
    
    public String emitSourceMap(String fileName, String sourceMapPath, String sourceRoot)
    {
        List<IJSEmitter.SourceMapMapping> mappings = this.emitter.getSourceMapMappings();
        for (IJSEmitter.SourceMapMapping mapping : mappings)
        {
            sourceMapGenerator.addMapping(mapping.sourcePath, mapping.name,
                    mapping.sourceStartPosition,
                    mapping.destStartPosition, mapping.destEndPosition);
        }
        if (sourceRoot != null)
        {
            sourceMapGenerator.setSourceRoot(sourceRoot);
        }

        StringBuilder builder = new StringBuilder();
        try
        {
            sourceMapGenerator.appendTo(builder, fileName);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return builder.toString();
    }
}
