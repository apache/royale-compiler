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

import org.apache.royale.compiler.problems.annotations.ProblemClassification;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 *  BURMDiagnosticNotAllowedHere is emitted by error
 *  analysis routines.  The problem may be considered
 *  "provisional" or "definite" depending on the pattern's
 *  confidence level.
 */
@ProblemClassification(CompilerProblemClassification.DEFAULT)
public final class BURMDiagnosticNotAllowedHereProblem extends BURMPatternMatchFailureProblem
{
    public static String DESCRIPTION =
        "${node} is not allowed here";
    
    public static final int errorCode = 1312;

    public BURMDiagnosticNotAllowedHereProblem(IASNode site)
    {
        super(site);
    }
}
