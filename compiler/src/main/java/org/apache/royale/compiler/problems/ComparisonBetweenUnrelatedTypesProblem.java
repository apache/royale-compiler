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
 * Example int == null.
 * We make this a warning, as it will generate correct code, althought it was an 
 * error with the old compiler.
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public class ComparisonBetweenUnrelatedTypesProblem extends StrictSemanticsProblem
{
    
    public static final String DESCRIPTION =
        "Comparison between a value of type ${firstType} and an unrelated type ${secondType}.";

    public static final int warningCode = 1176;

    public ComparisonBetweenUnrelatedTypesProblem(IASNode site, String firstType, String secondType)
    {
        super(site);
        this.firstType = firstType;
        this.secondType = secondType;
    }
    
    public final String firstType;
    public final String secondType;
}
