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

import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 *  BURMPatternMatchFailure is an internal failure that
 *  is generated when the code generator's pattern matcher
 *  cannot find any valid reduction for an AST.
 */
public class BURMPatternMatchFailureProblem extends CodegenInternalProblem
{
    public static String DESCRIPTION =
        "Unable to generate code for ${node}";
    
    public static final int errorCode = 1313;

    public BURMPatternMatchFailureProblem(IASNode site)
    {
        super(site, "");
        node = SemanticUtils.getDiagnosticString(site);
    }
    
    public final String node;
}
