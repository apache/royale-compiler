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
 * Diagnostic emitted when a constructor is declared static
 */
public final class ConstructorIsStaticProblem extends CodegenProblem
{
    // TODO: ErrorMSG maybe something like "A constructor cannot be static"?

    public static final String DESCRIPTION =
        "Constructor functions must be instance methods";

    public static final int errorCode = 1026;

    public ConstructorIsStaticProblem(IASNode site)
    {
        super(site);
    }
}
