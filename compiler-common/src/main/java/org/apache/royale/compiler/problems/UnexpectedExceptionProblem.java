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

/**
 * Unexpected exception in the compiler code.
 */
public final class UnexpectedExceptionProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "Unexpected exception '${exceptionName}' at ${exceptionLocation}";

    public static final int errorCode = 1530;
    
    public UnexpectedExceptionProblem(Throwable throwable)
    {
        super();
        this.exceptionName = throwable.getClass().getName();
        StringBuilder exceptionLocation = new StringBuilder();
        boolean first = false;
        for (StackTraceElement element : throwable.getStackTrace())
        {
            if (first)
            {
                exceptionLocation.append("\n\t");
            }
            exceptionLocation.append(element.getClassName() + "." + element.getMethodName() + ":" + element.getLineNumber());
            first = true;
        }
        this.exceptionLocation = exceptionLocation.toString();
    }

    public final String exceptionName;
    public final String exceptionLocation;
}
