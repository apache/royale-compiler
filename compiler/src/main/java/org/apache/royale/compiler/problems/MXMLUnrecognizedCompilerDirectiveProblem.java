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

import org.apache.royale.compiler.common.ISourceLocation;

/**
 * Problem generated when a compiler directive of the format <code>@Name()</code>
 * is found, but <code>Name</code> is not one of the valid compiler directives.
 */
public final class MXMLUnrecognizedCompilerDirectiveProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "Function ${functionName} is not a recognized compile-time directive";

    public static final int errorCode = 1449;
    
    public MXMLUnrecognizedCompilerDirectiveProblem(ISourceLocation site, String functionName)
    {
        super(site);
        this.functionName = functionName;
    }

    public final String functionName;
}
