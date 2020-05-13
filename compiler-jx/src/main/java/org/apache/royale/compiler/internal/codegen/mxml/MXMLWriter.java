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

package org.apache.royale.compiler.internal.codegen.mxml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.royale.compiler.codegen.ISourceMapEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.codegen.js.IMappingEmitter;
import org.apache.royale.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.royale.compiler.driver.js.IJSBackend;
import org.apache.royale.compiler.internal.codegen.js.JSFilterWriter;
import org.apache.royale.compiler.internal.codegen.js.JSWriter;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.visitor.as.IASBlockWalker;
import org.apache.royale.compiler.visitor.mxml.IMXMLBlockWalker;

public class MXMLWriter extends JSWriter
{
    /**
     * Create a JSApplication writer.
     * 
     * @param application the JSApplication model to be encoded
     * @param useCompression use ZLIB compression if true
     */
    public MXMLWriter(RoyaleJSProject project, List<ICompilerProblem> problems,
                      ICompilationUnit compilationUnit, boolean enableDebug)
    {
        super(project, problems, compilationUnit, enableDebug);
    }

    @Override
    public void writeTo(OutputStream out, OutputStream sourceMapOut, File sourceMapFile)
    {
        IJSBackend backend = (IJSBackend) project.getBackend();
        JSFilterWriter writer = (JSFilterWriter) backend.createWriterBuffer(project);

        IJSEmitter asEmitter = (IJSEmitter) backend.createEmitter(writer);
        IASBlockWalker asBlockWalker = backend.createWalker(
                project, problems, asEmitter);

        IMXMLEmitter mxmlEmitter = backend.createMXMLEmitter(writer);
        IMXMLBlockWalker mxmlBlockWalker = backend.createMXMLWalker(
                project, problems, mxmlEmitter, asEmitter, asBlockWalker);
        asEmitter.setParentEmitter(mxmlEmitter);

        mxmlBlockWalker.visitCompilationUnit(compilationUnit);

        try
        {
            out.write(mxmlEmitter.postProcess(writer.toString()).getBytes("utf8"));
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }

        if (sourceMapOut != null)
        {
            String sourceMapFilePath = null;
            String sourceRoot = null;
            if (sourceMapFile != null)
            {
                sourceMapFilePath = sourceMapFile.getAbsolutePath();
                convertMappingSourcePathsToRelative((IMappingEmitter) mxmlEmitter, sourceMapFile);
            }
            else
            {
                sourceRoot = System.getProperty("user.dir");
                convertMappingSourcePathsToRelative((IMappingEmitter) mxmlEmitter, new File(sourceRoot, "test.js.map"));
                sourceRoot = convertSourcePathToURI(sourceRoot);
            }
            convertMappingSourcePathsToURI((IMappingEmitter) mxmlEmitter);

            File compilationUnitFile = new File(compilationUnit.getAbsoluteFilename());
            ISourceMapEmitter sourceMapEmitter = backend.createSourceMapEmitter((IMappingEmitter) mxmlEmitter);
            try
            {
                String fileName = compilationUnitFile.getName();
                fileName = fileName.replace(".mxml", ".js");
                String sourceMap = sourceMapEmitter.emitSourceMap(fileName, sourceMapFilePath, sourceRoot);
                sourceMapOut.write(sourceMap.getBytes("utf8"));
            } catch (Exception e)
            {
                e.printStackTrace(System.err);
            }
        }
    }

}
