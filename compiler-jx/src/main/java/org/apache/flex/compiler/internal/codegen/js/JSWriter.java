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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Stack;

import org.apache.flex.compiler.codegen.ISourceMapEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.codegen.js.IJSWriter;
import org.apache.flex.compiler.driver.js.IJSBackend;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.visitor.as.IASBlockWalker;

public class JSWriter implements IJSWriter
{
    protected FlexJSProject project;

    protected List<ICompilerProblem> problems;

    protected ICompilationUnit compilationUnit;

    @SuppressWarnings("unused")
    private boolean enableDebug;

    private boolean isExterns = false;

    public boolean isExterns()
    {
    	return this.isExterns;
    }
    
    /**
     * Create a JSApplication writer.
     * 
     * @param application the JSApplication model to be encoded
     * @param useCompression use ZLIB compression if true
     */
    public JSWriter(FlexJSProject project, List<ICompilerProblem> problems,
            ICompilationUnit compilationUnit, boolean enableDebug)
    {
        this.project = project;
        this.problems = problems;
        this.compilationUnit = compilationUnit;
        this.enableDebug = enableDebug;
    }

    @Override
    public void close() throws IOException
    {
        //outputBuffer.close();
    }

    @Override
    public void writeTo(OutputStream out)
    {
        writeTo(out, null);
    }

    @Override
    public int writeTo(File out) throws IOException
    {
        return 0;
    }

    public void writeTo(OutputStream jsOut, File sourceMapOut)
    {
        IJSBackend backend = (IJSBackend) project.getBackend();
        JSFilterWriter writer = (JSFilterWriter) backend.createWriterBuffer(project);
        IJSEmitter emitter = (IJSEmitter) backend.createEmitter(writer);
        IASBlockWalker walker = backend.createWalker(project, problems, emitter);

        walker.visitCompilationUnit(compilationUnit);

        isExterns = emitter.getModel().isExterns;
        
        try
        {
            jsOut.write(emitter.postProcess(writer.toString()).getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (sourceMapOut != null)
        {
            convertMappingSourcePathsToRelative(emitter, sourceMapOut);

            File compilationUnitFile = new File(compilationUnit.getAbsoluteFilename());
            ISourceMapEmitter sourceMapEmitter = backend.createSourceMapEmitter(emitter);
            try
            {
                String fileName = compilationUnitFile.getName();
                fileName = fileName.replace(".as", ".js");
                String sourceMap = sourceMapEmitter.emitSourceMap(fileName, sourceMapOut.getAbsolutePath(), null);
                BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(sourceMapOut));
                outStream.write(sourceMap.getBytes());
                outStream.flush();
                outStream.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    protected void convertMappingSourcePathsToRelative(IJSEmitter emitter, File relativeToFile)
    {
        List<IJSEmitter.SourceMapMapping> mappings = emitter.getSourceMapMappings();
        for (IJSEmitter.SourceMapMapping mapping : mappings)
        {
            mapping.sourcePath = relativePath(mapping.sourcePath, relativeToFile.getAbsolutePath());
        }
    }

    //if we ever support Java 7, the java.nio.file.Path relativize() method
    //should be able to replace this method
    private String relativePath(String filePath, String relativeToFilePath)
    {
        File currentFile = new File(filePath);
        Stack<String> stack = new Stack<String>();
        stack.push(currentFile.getName());
        currentFile = currentFile.getParentFile();
        while (currentFile != null)
        {
            String absoluteCurrentFile = currentFile.getAbsolutePath() + File.separator;
            if (relativeToFilePath.startsWith(absoluteCurrentFile))
            {
                String relativeRelativeToFile = relativeToFilePath.substring(absoluteCurrentFile.length());
                int separatorCount = relativeRelativeToFile.length() - relativeRelativeToFile.replace(File.separator, "").length();
                String result = "";
                while (separatorCount > 0)
                {
                    result += ".." + File.separator;
                    separatorCount--;
                }
                while (stack.size() > 0)
                {
                    result += stack.pop();
                }
                return result;
            }
            stack.push(currentFile.getName() + File.separator);
            currentFile = currentFile.getParentFile();
        }
        return null;
    }
}
