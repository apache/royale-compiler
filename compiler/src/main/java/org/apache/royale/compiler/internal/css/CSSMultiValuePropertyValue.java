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

import java.util.List;

import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

import org.apache.royale.compiler.css.ICSSPropertyValue;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * Array type property values are comma-separated values in CSS properties.
 * <p>
 * For example:<br>
 * <code>fillColors: #FFFFFF, #CCCCCC, #FFFFFF, #EEEEEE;</code>
 */
public class CSSMultiValuePropertyValue extends CSSPropertyValue
{
    public CSSMultiValuePropertyValue(final List<CSSPropertyValue> elements,
                                 final CommonTree ast,
                                 final TokenStream tokens)
    {
        super(ast, tokens, CSSModelTreeType.PROPERTY_VALUE);
        this.elements = ImmutableList.copyOf(elements);
        super.children.addAll(elements);
    }

    private final ImmutableList<CSSPropertyValue> elements;

    /**
     * @return Elements in the array property value.
     */
    public ImmutableList<? extends ICSSPropertyValue> getElements()
    {
        return elements;
    }

    @Override
    public String toString()
    {
        return Joiner.on(" ").join(elements);
    }
}
