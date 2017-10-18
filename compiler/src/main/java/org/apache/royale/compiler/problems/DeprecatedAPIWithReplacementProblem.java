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
 * Reported when a class, interface, function, variable, constant, or namespace
 * annotated with <code>[Deprecated(replacement="...")]</code> metadata is used.
 */
public final class DeprecatedAPIWithReplacementProblem extends AbstractDeprecatedAPIProblem
{
    public static final String DESCRIPTION =
        "'${name}' has been deprecated. Please use '${replacement}'.";

    public static final int warningCode = 3606;
    public DeprecatedAPIWithReplacementProblem(IASNode site, String name, String replacement)
    {
        super(site);
        this.name = name;
        this.replacement = replacement;
    }
    
    public final String name;
    public final String replacement;
}
