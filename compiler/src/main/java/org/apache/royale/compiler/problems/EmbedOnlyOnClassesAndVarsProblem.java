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
 * This error is created when embed metadata decorates a declaration that is not a
 * member variable or class.
 */
public class EmbedOnlyOnClassesAndVarsProblem extends CompilerProblem
{
    public static final String DESCRIPTION = "Embed is only supported on classes and member variables.";

    public static final int errorCode = 5035;

    public EmbedOnlyOnClassesAndVarsProblem(ISourceLocation site)
    {
        super(site);
    }
}
