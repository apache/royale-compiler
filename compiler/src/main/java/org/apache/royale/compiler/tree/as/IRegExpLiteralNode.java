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

package org.apache.royale.compiler.tree.as;

import java.util.Set;

/**
 * An AST node representing a <code>RegExp</code> literal such as <code>/abc/gi</code>.
 * <p>
 * This node has no children.
 */
public interface IRegExpLiteralNode extends ILiteralNode
{
    /**
     * Represents a flag set on a Regular Expression
     */
    enum RegExpFlag
    {
        /**
         * <code>s</code> - Specifies whether the dot character (.) in a regular
         * expression pattern matches new-line characters.
         */
        DOTALL('s'),

        /**
         * <code>x</code> - Specifies whether to use extended mode for the
         * regular expression. When a RegExp object is in extended mode, white
         * space characters in the constructor string are ignored.
         */
        EXTENDED('x'),

        /**
         * <code>g</code> -Specifies whether to use global matching for the
         * regular expression.
         */
        GLOBAL('g'),

        /**
         * <code>i</code> - Specifies whether the regular expression ignores
         * case sensitivity.
         */
        IGNORECASE('i'),

        /**
         * <code>m</code> - If it is set, the caret (^) and dollar sign ($) in a
         * regular expression match before and after new lines.
         */
        MULTILINE('m');

        private char code;

        RegExpFlag(char code)
        {
            this.code = code;
        }

        /**
         * Converts a char to its corresponding flag
         * 
         * @param flag the char representing the flag: g, i, m, s or x
         * @return the {@link RegExpFlag} or null
         */
        public static RegExpFlag toFlag(char flag)
        {
            return RegExpFlag.valueOf(String.valueOf(flag));
        }

        /**
         * Returns the char that represents this flag
         * 
         */
        public char getCode()
        {
            return code;
        }
    }

    /**
     * Returns a set of flags that are set on this regular expression, or an
     * empty set
     * 
     * @return a set of {@link RegExpFlag} options
     */
    Set<RegExpFlag> getFlags();

    /**
     * Returns a String of flags that are set on this regular expression, or an
     * empty String
     * 
     * @return a String of {@link RegExpFlag} options
     */
    String getFlagString();

    /**
     * Determines if a specific flag is set on this regular expression
     * 
     * @param flag the {@link RegExpFlag} whose existence we are testing for
     * @return true if the flag exists
     */
    boolean hasFlag(RegExpFlag flag);
}
