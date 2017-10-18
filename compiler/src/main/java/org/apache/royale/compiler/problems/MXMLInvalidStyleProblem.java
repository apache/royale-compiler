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
import com.google.common.base.Joiner;

/**
 * Problem generated when a style is defined to work with a certain themes
 * and none of the themes are used in the current project.
 */
public final class MXMLInvalidStyleProblem extends MXMLSemanticProblem
{
    public static final String DESCRIPTION =
        "The style '${styleName}' is only supported by type '${componentType}' with the theme(s) '${themes}'.";

    public static final int errorCode = 1423;
    /**
     * Create an "invalid styles" problem.
     * 
     * @param site The source location of the invalid style.
     * @param styleName Style name.
     * @param componentType Component QName that defines the style.
     * @param themes Themes this style is applicable to.
     */
    public MXMLInvalidStyleProblem(ISourceLocation site, String styleName,
                                   String componentType, String[] themes)
    {
        super(site);
        this.styleName = styleName;
        this.componentType = componentType;
        this.themes = Joiner.on(',').skipNulls().join(themes);
    }

    public final String styleName;
    public final String componentType;
    public final String themes;
}
