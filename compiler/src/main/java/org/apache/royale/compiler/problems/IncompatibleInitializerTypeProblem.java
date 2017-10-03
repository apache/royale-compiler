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
 * constant initializer whose type does not match the declared type of the var,
 * const, or parameter that contains the initializer.
 * <p>
 * Example:
 * <p>
 * 
 * <pre>
 *  package
 *  {
 *     public const c : uint = "100"; // initializer is of type String, should be uint.
 *  }
 * </pre>
 */
public final class IncompatibleInitializerTypeProblem extends SemanticWarningProblem
{
    public static final String DESCRIPTION =
        "Incompatible initializer value of type '${sourceType}' where '${targetType}' is expected. " +
        "An initial value of ${transformedValue} will be used instead.";

    public static final int warningCode = 5007;

    public IncompatibleInitializerTypeProblem(ISourceLocation site, String sourceType, String targetType, String transformedValue)
    {
        super(site);

        this.sourceType = sourceType;
        this.targetType = targetType;
        this.transformedValue = transformedValue;
    }
    
    public final String sourceType;
    public final String targetType;
    public final String transformedValue;
}
