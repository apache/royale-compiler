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

import org.apache.royale.compiler.tree.as.IASNode;

/**
 * <pre>
 * Example: 
 *      function foo(baz) : void {
 *      }
 * </pre>
 *          
 */
public final class ParameterHasNoTypeDeclarationProblem extends SemanticWarningProblem
{
    public static final String DESCRIPTION = "parameter '${paramName}' for function '${functionName}' has no type declaration.";
    
    public static final int warningCode = 1008;
    public ParameterHasNoTypeDeclarationProblem(IASNode site, String paramName, String functionName)
    {
        super(site);
		this.paramName = paramName;
		this.functionName = functionName;
    }
    
    public final String paramName;
    public final String functionName;
}
