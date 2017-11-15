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

package org.apache.royale.compiler.clients.problems;

import org.apache.royale.compiler.problems.ICompilerProblem;
 
/**
 *  IProblemFilter defines the interface used by problem filtering
 *  objects; a problem filter must be able to accept (or, by returning
 *  false, reject) a candidate ICompilerProblem.
 */
public interface IProblemFilter
{
    /**
     * Determines if the specified {@link ICompilerProblem} passes the
     * filter.
     * 
     * @param p {@link ICompilerProblem} to check
     * @return true if specified {@link ICompilerProblem} passes the filter,
     * false otherwise.
     */
    boolean accept(ICompilerProblem p);
}
