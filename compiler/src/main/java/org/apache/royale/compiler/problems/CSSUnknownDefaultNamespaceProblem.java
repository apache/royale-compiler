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

/**
 * When a type selector doesn't specify namespace prefix, a default namespace
 * must be defined in the CSS.
 */
public final class CSSUnknownDefaultNamespaceProblem extends CSSProblem
{
    public static final String DESCRIPTION =
        "Type selector without namespace prefix requires a default namespace to be defined. ${selectorText}";

    public static final int errorCode = 1327;
    /**
     * Create a problem when a CSS document doesn't have a default namespace and
     * the selector doesn't specify a namespace prefix.
     * 
     * @param selector selector without explicit namespace
     */
    public CSSUnknownDefaultNamespaceProblem(ICSSSelector selector)
    {
        super(selector);
        assert selector.getNamespacePrefix() == null : "Expected null namespace prefix for such problem.";
        selectorText = selector.toString();
    }
    
    public final String selectorText;
}
