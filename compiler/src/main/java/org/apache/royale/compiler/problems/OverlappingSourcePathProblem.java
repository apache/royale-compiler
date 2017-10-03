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

import java.io.File;

import org.apache.royale.compiler.problems.annotations.DefaultSeverity;

/**
 * {@link org.apache.royale.compiler.problems.CompilerProblem} subclass for overlapping source path entries.
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public final class OverlappingSourcePathProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "Overlapping source path entries: ${ancestor} is an ancestor of ${descendant}";

    public static final int warningCode = 5020;
    public OverlappingSourcePathProblem(File ancestor, File descendant)
    {
        super();
        this.ancestor = ancestor.toString();
        this.descendant = descendant.toString();
    }

    public final String ancestor;
    public final String descendant;
}

