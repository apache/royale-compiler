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

public final class DependencyNotCompatibleProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "The dependency ${definition} from ${swc} has a minimum supported version of ${swcMinimumVersion}, which is higher than the compatibility version, ${compatibilityVersion}.";

    public static final int errorCode = 1330;
    
    public DependencyNotCompatibleProblem(String definition, String swc,
                                          String swcMinimumVersion,
                                          String compatibilityVersion)
    {
        super();
        this.definition = definition;
        this.swc = swc;
        this.swcMinimumVersion = swcMinimumVersion;
        this.compatibilityVersion = compatibilityVersion;
    }    

    public final String definition;
    public final String swc;
    public final String swcMinimumVersion;
    public final String compatibilityVersion;
}
