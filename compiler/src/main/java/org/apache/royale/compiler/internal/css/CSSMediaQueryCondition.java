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

import org.apache.royale.compiler.css.ICSSMediaQueryCondition;
import org.apache.royale.compiler.css.ICSSPropertyValue;
import com.google.common.base.Joiner;

/**
 * Implementation for a media query condition.
 */
public class CSSMediaQueryCondition extends CSSNodeBase implements ICSSMediaQueryCondition
{

    /**
     * Create a media query condition from a keyword. For example: {@code all}.
     */
    protected CSSMediaQueryCondition(final CommonTree tree,
                                     final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.MEDIA_QUERY_CONDITION);
        this.value = new CSSKeywordPropertyValue(tree, tokenStream);
        this.key = null;
    }

    /**
     * Create a media query condition from a key-value pair.
     * 
     * @param key name of the condition
     * @param value value of the condition
     */
    protected CSSMediaQueryCondition(final String key,
                                     final CSSPropertyValue value,
                                     final CommonTree tree,
                                     final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.MEDIA_QUERY_CONDITION);
        assert value != null : "Do not create empty media query condition.";
        this.value = value;
        this.key = key;
    }

    private final CSSPropertyValue value;
    private final String key;

    @Override
    public ICSSPropertyValue getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        if (key == null)
            return value.toString();
        else
            return Joiner.on("").join("(", key, ":", value.toString(), ")");
    }

    @Override
    public String getKey()
    {
        return key;
    }
}
