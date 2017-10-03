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
 * Problem generated when an invalid percentage expression
 * is found for the value of a numeric property
 * with <code>[PercentProxy(...)]</code> metadata.
 */
public final class MXMLInvalidPercentageProblem extends MXMLSemanticProblem
{
    public static final String DESCRIPTION =
        "Initializer for '${property}': invalid percentage expression: '${text}'.";

    public static final int errorCode = 1421;
    
    public MXMLInvalidPercentageProblem(ISourceLocation site, String property, String text)
    {
        super(site);
        this.property = property;
        this.text = text;
    }

    public final String property;
    public final String text;
}
