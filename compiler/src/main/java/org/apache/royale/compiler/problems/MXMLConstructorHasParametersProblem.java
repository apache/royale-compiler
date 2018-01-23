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
 * Problem generated when there is text before the root tag.
 */
public final class MXMLConstructorHasParametersProblem extends MXMLSemanticProblem
{
    public static final String DESCRIPTION =
        "This tag maps to a ${CLASS}, '${className}', whose constructor requires parameters. It will be ignored.";
    
    public static final int errorCode = 1407;

    public MXMLConstructorHasParametersProblem(ISourceLocation site, String className)
    {
        super(site);
        this.className = className;
    }
    
    public String className;
    
    // Prevent these from being localized.
    public final String CLASS = "class";
}
