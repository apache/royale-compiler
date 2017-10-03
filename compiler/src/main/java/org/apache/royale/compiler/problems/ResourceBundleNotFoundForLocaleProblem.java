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
import org.apache.royale.compiler.problems.annotations.DefaultSeverity;

/**
 * This problem gets created when the bundle with a specified name cannot be
 * found for a specific locale.
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public final class ResourceBundleNotFoundForLocaleProblem extends ResourceBundleNotFoundProblem
{
    public static final String DESCRIPTION =
        "Unable to resolve resource bundle '${bundleName}' for locale '${locale}'";
    
    public static final int errorCode = 1490;
    public final String locale;
    
    /**
     * Constructor.
     */
    public ResourceBundleNotFoundForLocaleProblem(ISourceLocation site, String bundleName, String locale)
    {
        super(site, bundleName);
        this.locale = locale;
    }
    
    public ResourceBundleNotFoundForLocaleProblem(String bundleName, String locale)
    {
        super(bundleName);
        this.locale = locale;
    }
}
