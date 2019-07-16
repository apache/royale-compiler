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

package org.apache.royale.compiler.internal.codegen.as;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.as.IASWriter;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.visitor.as.IASBlockWalker;

public class ASWriter implements IASWriter
{
    private RoyaleJSProject project;

    private List<ICompilerProblem> problems;

    private ICompilationUnit compilationUnit;

    @SuppressWarnings("unused")
    private boolean enableDebug;

    /**
     * Create a JSApplication writer.
     * 
     * @param application the JSApplication model to be encoded
     * @param useCompression use ZLIB compression if true
     */
    public ASWriter(RoyaleJSProject project, List<ICompilerProblem> problems,
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
        ASFilterWriter writer = project.getBackend().createWriterBuffer(project);
        IASEmitter emitter = project.getBackend().createEmitter(writer);
        IASBlockWalker walker = project.getBackend().createWalker(project, problems, emitter);

        walker.visitCompilationUnit(compilationUnit);

        System.out.println(writer.toString());

        try
        {
            out.write(writer.toString().getBytes("utf8"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int writeTo(File out) throws FileNotFoundException, IOException
    {
        return 0;
    }

}
