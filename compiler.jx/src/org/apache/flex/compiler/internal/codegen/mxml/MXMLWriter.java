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

package org.apache.flex.compiler.internal.codegen.mxml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.flex.compiler.driver.js.IJSBackend;
import org.apache.flex.compiler.internal.codegen.js.JSFilterWriter;
import org.apache.flex.compiler.internal.codegen.js.JSSharedData;
import org.apache.flex.compiler.internal.codegen.js.JSWriter;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.visitor.as.IASBlockWalker;
import org.apache.flex.compiler.visitor.mxml.IMXMLBlockWalker;

public class MXMLWriter extends JSWriter
{
    /**
     * Create a JSApplication writer.
     * 
     * @param application the JSApplication model to be encoded
     * @param useCompression use ZLIB compression if true
     */
    public MXMLWriter(IASProject project, List<ICompilerProblem> problems,
            ICompilationUnit compilationUnit, boolean enableDebug)
    {
        super(project, problems, compilationUnit, enableDebug);
    }

    @Override
    public void writeTo(OutputStream out, File sourceMapOut)
    {
        IJSBackend backend = (IJSBackend) JSSharedData.backend;
        JSFilterWriter writer = (JSFilterWriter) backend.createWriterBuffer(project);

        IJSEmitter asEmitter = (IJSEmitter) backend.createEmitter(writer);
        IASBlockWalker asBlockWalker = backend.createWalker(
                project, problems, asEmitter);

        IMXMLEmitter mxmlEmitter = backend.createMXMLEmitter(writer);
        IMXMLBlockWalker mxmlBlockWalker = backend.createMXMLWalker(
                project, problems, mxmlEmitter, asEmitter, asBlockWalker);

        mxmlBlockWalker.visitCompilationUnit(compilationUnit);

        try
        {
            out.write(mxmlEmitter.postProcess(writer.toString()).getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (sourceMapOut != null)
        {
            String fileName = new File(compilationUnit.getAbsoluteFilename()).getName();
            System.out.println("Source map cannot be generated for '" + fileName + "'.");
        }
    }

}
