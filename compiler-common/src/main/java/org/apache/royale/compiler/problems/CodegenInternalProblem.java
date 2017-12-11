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

package org.apache.royale.compiler.problems;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.royale.compiler.problems.annotations.ProblemClassification;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 *  A CodegenInternalProblem represents a
 *  logic error or caught exception. 
 *  @note CodegenInternalProblem is issued by
 *  subsystems other than the code generator.
 */

@ProblemClassification(CompilerProblemClassification.INTERNAL_ERROR)
public class CodegenInternalProblem extends CodegenProblem
{
    public static String DESCRIPTION =
        "${diagnostic}";
    
    public static final int errorCode = 1317;

    public CodegenInternalProblem(IASNode site, String diagnostic)
    {
        super(site);
        this.diagnostic = diagnostic;
    }

    public CodegenInternalProblem(IASNode site, Throwable throwable)
    {
        super(site);

        StringWriter stackTraceBuffer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTraceBuffer));
        diagnostic = stackTraceBuffer.toString();
    }
    
    public final String diagnostic;
}
