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

/**
 * Error to indicate a config namespace cannot be resolved
 */
public final class CannotResolveProjectLevelConfigExpressionProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "config constant not specified correctly in project / command line: '${configName}'";
    
    public static final int errorCode = 5037;
    
    public CannotResolveProjectLevelConfigExpressionProblem(String configName)
    {
        super();
        this.configName = configName;
    }
    
    public final String configName;
}
