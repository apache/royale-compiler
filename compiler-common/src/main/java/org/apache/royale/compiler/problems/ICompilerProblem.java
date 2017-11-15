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
 * All classes representing problems reported by the compiler implement this
 * interface. Errors and warnings are generically called "problems", following
 * Eclipse terminology. If a problem has a well-defined location within a file,
 * it should have complete start/end/line/column information; otherwise, these
 * should all be -1. This interface extends Comparable<ICompilerProblem> so that
 * problems can be sorted for reporting in file/line/column order.
 */
public interface ICompilerProblem extends ISourceLocation
{
    /**
     * Returns a unique identifier for this type of problem.
     * <p>
     * Clients can use this identifier to look up, in a .properties file, a
     * localized template string describing the problem. The template string can
     * have named placeholders such as ${name} to be filled in, based on
     * correspondingly-named fields in the problem instance.
     * <p>
     * Clients can also use this identifier to decide whether the problem is an
     * error, a warning, or something else; for example, they might keep a list
     * of error ids and a list of warning ids.
     * <p>
     * The unique identifier happens to be the fully-qualified classname of the
     * problem class.
     * 
     * @return A unique identifier for the type of problem.
     */
    String getID();
}
