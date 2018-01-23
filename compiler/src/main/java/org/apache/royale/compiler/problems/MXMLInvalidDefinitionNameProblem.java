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

import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;

/**
 * Problem generated when the <code>name</code> of a {@code <Definition>} is invalid.
 */
public final class MXMLInvalidDefinitionNameProblem extends MXMLSemanticProblem
{
    public static final String DESCRIPTION =
        "'${name}' is not a valid ${name} for a <${DEFINITION}>. Since it is not a valid MXML tag name, you will not be able to use the class being defined.";

    public static final int errorCode = 1416;
    public MXMLInvalidDefinitionNameProblem(IMXMLTagAttributeData site, String name)
    {
        super(site);
        this.name = name;
    }
    
    public final String name;
    
    // Prevent these from being localized.
    public final String NAME = "name";
    public final String DEFINITION = "Definition";
}
