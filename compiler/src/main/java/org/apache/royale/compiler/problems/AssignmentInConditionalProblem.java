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
import org.apache.royale.compiler.tree.as.IASNode;

/**
 *  Diagnostic emitted when an assignment expression is a child of
 *  a conditional expression without a guarding equality-type test, e.g.,
 *  (x = 3) is a problem, but (y != x = 3) is not.
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public final class AssignmentInConditionalProblem extends SemanticWarningProblem
{ 
    public static final String DESCRIPTION =
        "Assignment within conditional.  Did you mean == instead of =?";
    
    public static final int warningCode = 1100;

    public AssignmentInConditionalProblem(IASNode site)
    {
        super(site);
    }
}
