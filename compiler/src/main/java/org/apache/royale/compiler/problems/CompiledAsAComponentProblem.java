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

import org.apache.royale.compiler.problems.annotations.DefaultSeverity;

/**
 * Warn developers when an application contains a hard reference to another
 * application or module. The result will be all the classes from the
 * application or module being linked into the target application. This problem
 * will likely result in a runtime error as well.
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public final class CompiledAsAComponentProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "${className} is a module or application that is directly referenced. This will cause ${className} and all of its dependencies to be linked in with ${mainDefinition}. Using an interface is the recommended practice to avoid this.";

    public static final int warningCode = 5000;
    public CompiledAsAComponentProblem(String className, String mainDefinition)
    {
        super();
        this.className = className;
        this.mainDefinition = mainDefinition;
    }
    
    public final String className;
    public final String mainDefinition;
}
