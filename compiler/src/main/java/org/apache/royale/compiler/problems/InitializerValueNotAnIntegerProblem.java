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
 * constant initializer that is a Number and the declared type of the var,
 * const, or parameter that contains the initializer is int or uint.
 * <p>
 * Example:
 * <p>
 * 
 * <pre>
 *  package
 *  {
 *     public const c : uint = 1.5; // initializer is not an integer
 *  }
 * </pre>
 */
public class InitializerValueNotAnIntegerProblem extends SemanticWarningProblem
{

    public static final String DESCRIPTION =
        "Initializer value ${initializerValue} is not valid for type '${initializerType}'. " +
        "An initial value of ${transformedValue} will be used instead.";
    
    
    public static final int warningCode = 5014;
    
    public InitializerValueNotAnIntegerProblem(ISourceLocation site, String initializerType, String initializerValue, String transformedValue)
    {
        super(site);
        this.initializerType = initializerType;
        this.initializerValue = initializerValue;
        this.transformedValue = transformedValue;
    }

    public final String initializerType;
    public final String initializerValue;
    public final String transformedValue;

}
