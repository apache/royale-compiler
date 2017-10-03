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

/**
 * Generic "internal error" problem
 */
public final class InternalCompilerProblem2 extends CompilerProblem
{
    /**
     * @param sourcePath Name of source file for which code was being
     * generated when this problem was found. Must not be null.
     * @param throwable {@link Throwable} that caused this problem to be created. must not be null.
     * @param subSystemName is the Compiler module where the problem occurred. For example "ABC generator". Must not be null
     *
     * Note that if you do not have the required ctor arguments, you might use InternalCompilerProblem 
     * instead of this class
     */
    public InternalCompilerProblem2(String sourcePath, Throwable throwable, String subSystemName)
    {
        super(sourcePath);
        this.sourcePath = sourcePath;
        this.subSystemName = subSystemName;
        this.stackTrace = makeStackTrace(throwable);
    }
     
    public static final String DESCRIPTION = "Internal error in ${subSystemName} subsystem, when generating code for: ${sourcePath}: ${stackTrace}";
    public static final int errorCode = 1551;

    public final String sourcePath;
    public final String stackTrace;
    public final String subSystemName;
    
    static String makeStackTrace(Throwable throwable)
    {
        StringWriter stackTraceBuffer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTraceBuffer));
        return stackTraceBuffer.toString();
    }
}
