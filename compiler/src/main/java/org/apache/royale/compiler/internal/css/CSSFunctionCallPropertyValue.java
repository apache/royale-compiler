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

import org.apache.royale.compiler.css.ICSSDocument;

/**
 * Base class for CSS function call property values.
 * <p>
 * For example: {@code Embed("bg.png")}.
 */
public class CSSFunctionCallPropertyValue extends CSSPropertyValue
{
    /** Function name for {@code ClassReference("")}. */
    public static final String CLASS_REFERENCE = "ClassReference";
    /** Function name for {@code PropetyReference("")}. */
    public static final String PROPERTY_REFERENCE = "PropertyReference";
    /** Function name for {@code Embed("")}. */
    public static final String EMBED = "Embed";

    /** Name of the function. */
    public final String name;

    /** Raw arguments text excluding the parentheses. */
    public final String rawArguments;

    /**
     * Initialize a {@link CSSFunctionCallPropertyValue}.
     * 
     * @param name Function name.
     * @param rawArguments Raw argument string with parentheses and quotes.
     * @param tree AST.
     * @param tokenStream Token stream.
     */
    public CSSFunctionCallPropertyValue(final String name,
                                        final String rawArguments,
                                        final CommonTree tree,
                                        final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.PROPERTY_VALUE);
        this.name = name;
        this.rawArguments = rawArguments.substring(1, rawArguments.length() - 1);
    }

    /**
     * Generate CSS code fragment for this model object. This is used by
     * recursively generating a CSS document from a {@link ICSSDocument} for
     * debugging and testing purposes.
     */
    @Override
    public String toString()
    {
        return String.format("%s(%s)", name, rawArguments);
    }

    /**
     * If {@code rawArguments} is of pattern {@code "argument"} 
     * or {@code 'argument'}, then return {@code argument}. Otherwise, 
     * return the original value.
     * 
     * @param rawArguments Raw argument from {@link #rawArguments}.
     * @return Single argument name.
     */
    public static String getSingleArgumentFromRaw(final String rawArguments)
    {
        if ( (rawArguments.startsWith("\"") && rawArguments.endsWith("\""))
                || (rawArguments.startsWith("'") && rawArguments.endsWith("'")) )
        {
            return rawArguments.substring(1, rawArguments.length() - 1);
        }
        else
        {
            return rawArguments;
        }
    }
}
