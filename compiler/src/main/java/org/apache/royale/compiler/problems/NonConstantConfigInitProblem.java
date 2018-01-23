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
 * Error when a config string is initialized to a non-constant value.
 * 
 *  For example,
 *  
 *     package {
 * 
 *     CONFIG const str1="value1"
 *     CONFIG const str2="value2"
 *
 *     CONFIG const plus2=CONFI::str1+CONFIG::str2
 *     trace(CONFIG::plus2);
 *    }
 *    
 *
 *   will produce this error because "CONFI::str1+CONFIG::str2" is not a 
 *   constant value since "CONFI" is not a valid config namespace.
 *
 */
public class NonConstantConfigInitProblem extends SemanticProblem
{
    public static final String DESCRIPTION =
        "The initializer for a configuration value must be a compile time constant.";
    
    public static final int errorCode = 1208;

    public NonConstantConfigInitProblem(ISourceLocation site)
    {
        super(site);
    }
    
}
