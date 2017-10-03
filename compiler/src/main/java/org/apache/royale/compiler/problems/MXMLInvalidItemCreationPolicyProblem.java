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
 * Problem generated when an invalid value is used for <code>itemCreationPolicy</code>.
 */
public final class MXMLInvalidItemCreationPolicyProblem extends MXMLSemanticProblem
{
    public static final String DESCRIPTION =
        "Valid values for '${ITEM_CREATION_POLICY}' are \"{IMMEDIATE}\" or \"${DEFERRED}\". This attribute will be ignored.";

    public static final int errorCode = 1418;
    public MXMLInvalidItemCreationPolicyProblem(IMXMLTagAttributeData site)
    {
        super(site);
    }
    
    // Prevent these from being localized.
    public final String ITEM_CREATION_POLICY = "itemCreationPolicy";
    public final String IMMEDIATE = "immediate";
    public final String DEFERRED = "deferred";
}
