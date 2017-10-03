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

import org.apache.royale.compiler.mxml.IMXMLTagData;

/**
 * Problem generated when an element of a property value of type <code>Array</code>
 * is incompatible with the <code>[ArrayElementType]</code> metadata for that property.
 */
public final class MXMLIncompatibleArrayElementProblem extends MXMLSemanticProblem
{
    public static final String DESCRIPTION =
        "An array element of type '${actualType}' is incompatible with the expected [${ARRAY_ELEMENT_TYPE}] of '${expectedType}' for the '${propertyName}' property.";

    public static final int errorCode = 1414;
    public MXMLIncompatibleArrayElementProblem(IMXMLTagData site, String propertyName, String expectedType, String actualType)
    {
        super(site);
        this.propertyName = propertyName;
        this.expectedType = expectedType;
        this.actualType = actualType;
    }
    
    public final String propertyName;
    public final String expectedType;
    public final String actualType;
    
    // Prevent these from being localized.
    public final String ARRAY_ELEMENT_TYPE = "ArrayElementType";
}
