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

import org.apache.royale.compiler.css.ICSSSelector;
import org.apache.royale.compiler.problems.annotations.DefaultSeverity;

/**
 * Report unused type selector used in {@code <fx:Style>} in an MXML document.
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public final class CSSUnusedTypeSelectorProblem extends CSSProblem
{
    public static final String DESCRIPTION =
        "The CSS type selector '${type}' was not processed, because the type was not used in the application.";

    public static final int warningCode = 5002;
    public CSSUnusedTypeSelectorProblem(ICSSSelector selector)
    {
        super(selector);
        type = selector.getElementName();
    }

    public final String type;
}
