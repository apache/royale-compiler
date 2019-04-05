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
import org.apache.royale.compiler.problems.annotations.DefaultSeverity;

/**
 *  Public vars don't work well in JS minified output.  The minifier
 *  creates a "name" reference then gives the var a new minified name.
 *  If the var is not read-only, the write to the var means the
 *  reference is obsolete.  Also, MXML and States access attributes
 *  by name which also breaks the reference.  Use getter/setters instead.
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public class PublicVarWarningProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "public var may not work in minified JS output.  Use getter/setter instead.";
    
    public static final int warningCode = 5044;
    
    public PublicVarWarningProblem(ISourceLocation site)
    {
        super(site);
    }
    
    public PublicVarWarningProblem(String sourcePath, int start, int end, int line, int column, int endLine, int endColumn)
    {
        super(sourcePath, start, end, line, column, endLine, endColumn);
    }
    
}
