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

/**
 * Problem generated when the value of an attribute which expects a boolean 
 * value is not in the correct boolean form.
 */
public final class FXGInvalidBooleanValueProblem extends FXGUnknownAttributeValueProblem
{
    public static final String DESCRIPTION = "Attribute '${attribute}' contains invalid boolean value '${value}' - must be 'true' or 'false', case sensitive.";
    
    public static final int errorCode = 1369;
    
    public FXGInvalidBooleanValueProblem(String filePath, int line, int column, String attribute, String value)
    {
        super(filePath, line, column, attribute, value);
    }
}
