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
 * Diagnostic issued when a rest parameter has a type annotation other than '*' or 'Array'
 */
public final class InvalidRestParameterDeclarationProblem extends CodegenProblem
{
    // TODO ErrorMSG:  something less confusing, like: rest parameter may only be typed as 'Array'
    // TODO ErrorMSG:  this message does essentially say that, but it does so in a confusing manner - at first
    // TODO ErrorMSG:  it sounds like its talking about parameters after the rest parameter
    public static final String DESCRIPTION =
        "Parameters specified after the ...rest parameter definition keyword can only be an ${ARRAY} data type.";

    public static final int errorCode = 1140;

    public InvalidRestParameterDeclarationProblem(IASNode site)
    {
        super(site);
    }
    
    // Prevent these from being localized.
    public final String ARRAY = "Array";
}
