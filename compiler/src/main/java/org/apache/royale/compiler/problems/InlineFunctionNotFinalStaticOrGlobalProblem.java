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

public class InlineFunctionNotFinalStaticOrGlobalProblem extends CompilerProblem
{
    public static final String DESCRIPTION = "The function ${functionName} could not be inlined, as only functions marked as ${FINAL}, ${STATIC} or global functions can be inlined.";

    public static final int errorCode = 5041;

    public InlineFunctionNotFinalStaticOrGlobalProblem(ISourceLocation site, String functionName)
    {
        super(site);
        this.functionName = functionName;
    }

    public final String functionName;

    // Prevent these from being localized.
    public final String FINAL = "final";
    public final String STATIC = "static";
}
