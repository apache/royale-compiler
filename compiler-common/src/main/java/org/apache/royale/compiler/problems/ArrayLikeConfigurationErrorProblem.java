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
 *  When a class or interface is annotated with [ArrayLike] metadata, it needs
 *  to provide the required minimum set of arguments to allow instances of the class
 *  to be treated as 'ArrayLike' when encountered in other source code.
 *  It also requires the concrete presence of the arrayLike iterator factory as a separate
 *  stand-alone dependency in the current project level compilation. This ensures that there
 *  is no dependency missing if the ArrayLike definition is in an external library: the supporting
 *  definition for iterator support must be in the same library
 *  (which might mean it is repeated across different libraries, but that we can be sure it is available)
 */
@DefaultSeverity(CompilerProblemSeverity.ERROR)
public class ArrayLikeConfigurationErrorProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "${description}. ArrayLike definition has an error with a required argument or dependency.";
    
    public static final int errorCode = 9999;
    
    public ArrayLikeConfigurationErrorProblem(ISourceLocation site, String description)
    {
        super(site);
        this.description = description;
    }
    
    public ArrayLikeConfigurationErrorProblem(String sourcePath, int start, int end, int line, int column, int endLine, int endColumn, String description)
    {
        super(sourcePath, start, end, line, column, endLine, endColumn);
        this.description = description;
    }
    public final String description;
}
