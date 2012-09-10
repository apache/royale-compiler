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

package org.apache.flex.compiler.internal.css;

import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

/**
 * Implementation for CSS string property values. For example:<br>
 * <code>{ fontFamily: "Sans Serif"; }</code>
 */
public class CSSStringPropertyValue extends CSSPropertyValue
{

    protected CSSStringPropertyValue(final String stringWithQuotes,
                                     final CommonTree tree,
                                     final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.PROPERTY_VALUE);
        assert isQuoted(stringWithQuotes) : "Don't strip quotes in parser: [" + stringWithQuotes + "]";
        this.value = stripQuotes(stringWithQuotes);
    }

    private final String value;

    /**
     * @return The string value without the quotes.
     */
    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return String.format("\"%s\"", value);
    }

    /**
     * Check if the string is quoted with single quotes or double quotes.
     * 
     * @param testString string to test
     * @return True if the string is quoted.
     */
    public static boolean isQuoted(final String testString)
    {
        return (testString.startsWith("\"") && testString.endsWith("\"")) ||
               (testString.startsWith("'") && testString.endsWith("'"));
    }

    /**
     * Strip one pair of quotes around the string.
     * 
     * @param value string to strip
     * @return Stripped string.
     */
    public static String stripQuotes(final String value)
    {
        return value.substring(1, value.length() - 1);
    }
}
