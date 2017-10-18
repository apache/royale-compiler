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

import org.apache.royale.compiler.definitions.IDefinition;

/**
 * Problem generated when there is an <code>outerDocument</code> property
 * already declared in the class hierarchy of a {@code <Component>} tag.
 */
public final class MXMLOuterDocumentAlreadyDeclaredProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "A property named ${OUTER_DOCUMENT} has already been declared, conflicting with the <${COMPONENT}> tag ${OUTER_DOCUMENT}.";

    public static final int errorCode = 1435;
    
    public MXMLOuterDocumentAlreadyDeclaredProblem(IDefinition site)
    {
        super(site);
    }
    
    // Prevent these from being localized.
    public final String OUTER_DOCUMENT = "outerDocument";
    public final String COMPONENT = "Component";
}
