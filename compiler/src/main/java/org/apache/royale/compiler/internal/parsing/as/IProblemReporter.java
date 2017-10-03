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

package org.apache.royale.compiler.internal.parsing.as;

import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Interface to allow adding problems that are found in different contexts
 */
public interface IProblemReporter
{
    /**
     * Adds an {@link ICompilerProblem} to this reporter
     * 
     * @param problem an {@link ICompilerProblem}
     */
    void addProblem(ICompilerProblem problem);

    /**
     * Gets the source file that we are reporting for
     * 
     * @return the source file
     */
    String getSourceFilePath();
}
