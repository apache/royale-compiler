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
 * Problem generated when code attempts to bind to something that isn't bindable.
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public final class MXMLDatabindingSourceNotBindableProblem extends SemanticProblem
{
    public static final String DESCRIPTION =
        "Data binding will not be able to detect assignments to '${sourceName}'.";
    
    public static final int warningCode = 5011;
    
    /**
     * @param site should be the identifier node of the non-bindable identifier/thing that is causing the problem.
     * @param sourceName is the string that will be shown in the error message.
     */
    public MXMLDatabindingSourceNotBindableProblem(IASNode site, String sourceName)
    {
        super(site);
        this.sourceName = sourceName;
    }
    
    public final String sourceName;
}
