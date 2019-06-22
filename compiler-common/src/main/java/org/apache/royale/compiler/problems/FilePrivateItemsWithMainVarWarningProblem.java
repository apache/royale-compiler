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
 *  When a variable is the externally accessible package scoped definition, there is a
 *  namespace implementation issue in javascript for other file-private members.
 *  This use case seems limited, as it is likely that the externally visible variable
 *  definition would need to depend on the file-private definitions, otherwise it is
 *  questionable why they would even exist. Given the likelihood that this is very limited,
 *  it is currently not recommended.
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public class FilePrivateItemsWithMainVarWarningProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "Using file-private definitions outside a package level variable is not recommended and may not work (javascript).";
    
    public static final int warningCode = 5045;
    
    public FilePrivateItemsWithMainVarWarningProblem(ISourceLocation site)
    {
        super(site);
    }
    
    public FilePrivateItemsWithMainVarWarningProblem(String sourcePath, int start, int end, int line, int column, int endLine, int endColumn)
    {
        super(sourcePath, start, end, line, column, endLine, endColumn);
    }
    
}
