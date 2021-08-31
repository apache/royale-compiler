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

package org.apache.royale.compiler.internal.css;

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
        this.value = convertEscapes(stripQuotes(stringWithQuotes));
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
    
    private static String convertEscapes(String value)
    {
        int idx = 0;
        int c = value.indexOf('\\');

        while (c != -1 && c + 1 < value.length())
        {
            char cnext = value.charAt(c + 1);
            if (cnext != '\\' && cnext != 'n' && cnext != 't' && cnext != 'r')
            {
                int n = value.length() - (c + 1);
                if (n > 6)
                    n = 6;
                StringBuilder sub = new StringBuilder();
                int i = 0;
                for (; i < n; i++)
                {
                    char cc = value.charAt(c + i + 1);
                    if (cc != ' ') {
                        sub.append(cc);
                        idx++; //this represents each 'digit' char
                    }
                    else
                        break;
                }
                idx++; //this represents the first escape char
                int ccode = Integer.parseInt(sub.toString(), 16);
                String insert;

                if (ccode == /* nbsp */ 0xA0) {
                    insert = "\\a0"; //restore nbsp as the encoded form
                } else if (ccode == /* ZERO WIDTH SPACE */ 0x200B) {
                    insert = "\\200b"; //restore zero-width space as the encoded form
                } else {
                    sub = new StringBuilder();
                    sub.append(Character.toChars(ccode));
                    insert = sub.toString();
                }

                value = value.substring(0, c) + insert + value.substring(c + i + 1);
            }
            else
                idx += 2;
            c = value.indexOf('\\', idx);
        }
        return value;
    }
}
