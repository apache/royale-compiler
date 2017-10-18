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
 * Problem generated when a a value of a particular type cannot be parsed
 * from the specified MXML text.
 */
public final class MXMLInvalidTextForTypeProblem extends MXMLSemanticProblem
{
    public static final String DESCRIPTION =
        "Cannot parse a value of type '${type}' from '${text}'.";

    public static final int errorCode = 1424;
    public MXMLInvalidTextForTypeProblem(ISourceLocation site, String text, String type)
    {
        super(site);
        this.text = text;
        this.type = type;
    }

    public final String text;
    public final String type;
}
