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

import org.apache.royale.compiler.problems.annotations.ProblemClassification;

/**
 * Report internal problems when no context information is available.
 * 
 * Note: if you have a source path, you should use InternalCompilerProblem2, as it 
 * reports more information.
 */
@ProblemClassification(CompilerProblemClassification.INTERNAL_ERROR)
public class InternalCompilerProblem extends CompilerProblem {

    public InternalCompilerProblem(Exception exception)
    {
        this(exception.toString(), exception.getStackTrace());
    }

    public InternalCompilerProblem(StackTraceElement[] stackTraceElements)
    {
    	this("", stackTraceElements);
    }
    
    public InternalCompilerProblem(String message, StackTraceElement[] stackTraceElements)
    {
        super();
        // Save off the stack trace from the exception/throwable
        if(stackTraceElements != null) {
            StringBuilder stacktraceBuilder = new StringBuilder();
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                stacktraceBuilder.append(stackTraceElement.toString());
            }
            this.stackTrace = message + " " + stacktraceBuilder.toString();
        } else {
            this.stackTrace = null;
        }
    }

    public static final String DESCRIPTION = "${stackTrace}";
    public static final int errorCode = 1309;

    public final String stackTrace;
  
}
