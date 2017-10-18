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
 * Problem generated for multiple initializers as child tags on an MXML property tag.
 */
public final class MXMLMultipleInitializersProblem extends MXMLSemanticProblem
{
    public static final String DESCRIPTION =
        "In initializer for '${element}' multiple initializer values are not permitted for target type '${targetType}'.";

    public static final int errorCode = 1998;
    public MXMLMultipleInitializersProblem(IMXMLTagData tag, String targetType)
    {
        super(tag);
        childTag = tag.getShortName();
        childNamespace = tag.getURI() != null ? tag.getURI() : "";
        element = tag.getParentTag().getName();
        this.targetType = targetType;
    }

    public String childTag;
    public final String childNamespace;
    public String element;
    public String targetType;
}
