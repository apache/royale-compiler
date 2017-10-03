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
 * Semantic diagnostic emitted when the method body semantic checker detects a
 * numeric constant initializer can not be converted to the type of the var,
 * const, or paramter that contains the initializer without loss of information.
 * <p>
 * Example:
 * <p>
 * 
 * <pre>
 *  package
 *  {
 *     public const c : uint = 5000000000; // 5 billion does not fit in a uint.
 *  }
 * </pre>
 */
public class InitializerValueOutOfRangeProblem extends SemanticWarningProblem
{
    public static final String DESCRIPTION =
        "Initializer value ${initializerValue} for type '${initializerType}' must be between ${minValue} and ${maxValue} inclusive. " +
        "An initial value of ${transformedValue} will be used instead.";
    
    public static final int warningCode = 5015;
    
    public InitializerValueOutOfRangeProblem(ISourceLocation site, String initializerType, String initializerValue, String minValue, String maxValue, String transformedValue)
    {
        super(site);

        this.initializerType = initializerType;
        this.initializerValue = initializerValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.transformedValue = transformedValue;
    }
    
    public final String initializerType;
    public final String initializerValue;
    public final String minValue;
    public final String maxValue;
    public final String transformedValue;
}
