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

import org.apache.royale.compiler.tree.as.IASNode;

/**
 * Problem generated when 'override' is used outside of a class
 */
public final class InvalidOverrideProblem extends CodegenProblem
{
    // TODO ErrorMSG: sync this with StaticOutsideClassProblem & FinalOutsideClassProblem - they all use different wording to say the same thing
    // TODO ErrorMSG: this is basically the same problem  as error #1010 - OverrideOutsideClassProblem
    public static final String DESCRIPTION =
        "The ${OVERRIDE} attribute can only be used on a method defined in a ${CLASS}.";

    // bgf: why is this 1134? Maybe it should have been 1010, but I'm going to make up a 
    // new code 
    public static final int errorCode = 1305;

    public InvalidOverrideProblem(IASNode site)
    {
        super(site);
    }
    
    // Prevent these from being localized.
    public final String OVERRIDE = "override";
    public final String CLASS  = "class";
}
