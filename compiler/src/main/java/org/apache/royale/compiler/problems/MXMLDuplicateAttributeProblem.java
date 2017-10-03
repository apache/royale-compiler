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
 * Problem generated for a duplicate attribute on an MXML tag.
 */
public final class MXMLDuplicateAttributeProblem extends MXMLSemanticProblem
{
    public static final String DESCRIPTION =
        "Attribute '${attributeName}' bound to namespace '${attributeNamespace}' is already specified for element '${element}'. It will be ignored.";

    public static final int errorCode = 1408;
    public MXMLDuplicateAttributeProblem(IMXMLTagAttributeData attribute)
    {
        super(attribute);
        attributeName = attribute.getShortName();
        attributeNamespace = attribute.getURI() != null ? attribute.getURI() : "";
        element = attribute.getParent().getName();
    }
    
    public final String attributeName;
    public final String attributeNamespace;
    public final String element;
}
