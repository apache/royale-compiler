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
 *  A config namespace must be declared in a top level of a program or package.
 *  
 *  Error when a config namespace is defined inside a class:
 *  
 *  package
 *  {
 *      class C
 *      {
 *         config namespace FOO;
 *      }
 *  }
 *
 */
public class InvalidConfigLocationProblem extends SemanticProblem
{
    public static final String DESCRIPTION =
        "A configuration value must be declared at the top level of a program or package.";
    
    public static final int errorCode = 1210;

    public InvalidConfigLocationProblem (ISourceLocation site)
    {
        super(site);
    }
    
}

