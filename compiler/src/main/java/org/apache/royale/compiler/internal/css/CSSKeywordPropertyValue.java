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
 * Implementation for CSS keyword property value.
 */
public class CSSKeywordPropertyValue extends CSSPropertyValue
{

    /**
     * Create a CSS property value from keyword. The return value can be
     * different implementations depending on the keyword. For example, color
     * name keywords like {@code red} and {@code blue} will generate
     * {@link CSSColorPropertyValue} return values. Otherwise, the return type
     * is {@link CSSKeywordPropertyValue}.
     * 
     * @param tree AST node
     * @param tokenStream token stream
     * @return {@code ICSSPropertyValue} objects based on the keyword.
     */
    protected static CSSPropertyValue create(final CommonTree tree,
                                              final TokenStream tokenStream)
    {
        assert tree != null : "AST can't be null.";
        assert tokenStream != null : "TokenStream can't be null.";
        final CSSPropertyValue result;
        final String keyword = tree.getText();

        final Integer colorInt = CSSColorPropertyValue.COLOR_MAP.get(keyword.toLowerCase());
        if (colorInt != null)
            result = new CSSColorPropertyValue(colorInt, tree, tokenStream);
        else
            result = new CSSKeywordPropertyValue(tree, tokenStream);
        return result;
    }

    protected CSSKeywordPropertyValue(final CommonTree tree,
                                      final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.PROPERTY_VALUE);
        this.keyword = tree.getText();
    }

    private final String keyword;

    /**
     * @return the keyword
     */
    public String getKeyword()
    {
        return keyword;
    }

    @Override
    public String toString()
    {
        return keyword;
    }
}
