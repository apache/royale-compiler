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

import org.apache.royale.compiler.internal.parsing.as.ASToken;

/**
 * Expecting a definition keyword such as "function", "var", "class" after a
 * modifier attribute such as "static", "final", etc.
 */
public final class ExpectDefinitionKeywordAfterAttributeProblem extends SyntaxProblem
{
    public static final String DESCRIPTION =
            "expected a definition keyword (such as '${FUNCTION}') after attribute '${attributeName}', not '${tokenText}'.";

    public static final int errorCode = 1071;

    /**
     * Create a problem.
     * 
     * @param attributeName Name of the modifier attribute, such as "static",
     * "final", etc.
     */
    public ExpectDefinitionKeywordAfterAttributeProblem(final String attributeName, final ASToken offendingToken)
    {
        super(offendingToken);
        this.attributeName = attributeName;
    }

    public final String attributeName;
    public final String FUNCTION = "function";
}
