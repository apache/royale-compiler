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
 *  ForEachExpectInBURMDiagnostic signals the presence
 *  of a specific error condition -- a for each loop with no in clause.
 */
public final class BURMDiagnosticForEachExpectedInProblem extends BURMPatternMatchFailureProblem
{
    public static String DESCRIPTION =
        "the '${EACH}' keyword is not allowed without an '${IN}' operator";
    
    public static final int errorCode = 1311;

    public BURMDiagnosticForEachExpectedInProblem(IASNode site)
    {
        super(site);
    }
    
    public final String EACH = "each";
    public final String IN = "in";
}
