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

import org.apache.royale.compiler.css.ICSSMediaQueryCondition;
import org.apache.royale.compiler.css.ICSSProperty;
import org.apache.royale.compiler.css.ICSSRule;
import org.apache.royale.compiler.css.ICSSSelector;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * Implementation for CSS rules.
 */
public class CSSRule extends CSSNodeBase implements ICSSRule
{
    public CSSRule(final List<CSSMediaQueryCondition> mediaQueries,
                      final List<CSSSelector> selectorGroup,
                      final List<CSSProperty> properties,
                      final CommonTree tree,
                      final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.RULE);

        assert selectorGroup != null : "Subject can't be null";
        assert selectorGroup.size() > 0 : "Requires at least one selector.";
        this.selectorGroup = new ImmutableList.Builder<ICSSSelector>().addAll(selectorGroup).build();

        if (mediaQueries == null)
            this.mediaQueryList = ImmutableList.of();
        else
            this.mediaQueryList = new ImmutableList.Builder<ICSSMediaQueryCondition>().addAll(mediaQueries).build();

        if (properties == null)
            this.propertyList = ImmutableList.of();
        else
            this.propertyList = new ImmutableList.Builder<ICSSProperty>().addAll(properties).build();

        // setup tree
        this.children.add(new CSSTypedNode(CSSModelTreeType.SELECTOR_GROUP, this.selectorGroup));
        this.children.add(new CSSTypedNode(CSSModelTreeType.MEDIA_QUERY, this.mediaQueryList));
        this.children.add(new CSSTypedNode(CSSModelTreeType.PROPERTY_LIST, this.propertyList));
    }

    private final ImmutableList<ICSSMediaQueryCondition> mediaQueryList;
    private final ImmutableList<ICSSSelector> selectorGroup;
    private final ImmutableList<ICSSProperty> propertyList;

    @Override
    public ImmutableList<ICSSMediaQueryCondition> getMediaQueryConditions()
    {
        return mediaQueryList;
    }

    @Override
    public ImmutableList<ICSSSelector> getSelectorGroup()
    {
        return selectorGroup;
    }

    @Override
    public ImmutableList<ICSSProperty> getProperties()
    {
        return propertyList;
    }

    /**
     * Print out a rule in Flex CSS syntax.
     */
    @Override
    public String toString()
    {
        final StringBuilder result = new StringBuilder();

        if (hasMediaQuery())
        {
            result.append("@media ");
            result.append(Joiner.on(" and ").join(getMediaQueryConditions()));
            result.append(" {\n");
            result.append("    ");
        }

        final String selectors = Joiner.on(",\n").join(getSelectorGroup());
        result.append(selectors).append(" {\n");
        for (final ICSSProperty prop : getProperties())
        {
            if (!hasMediaQuery())
                result.append("    ");

            result.append("    ").append(prop.toString()).append("\n");
        }
        if (hasMediaQuery())
            result.append("    }\n");

        result.append("}\n");

        return result.toString();
    }

    /**
     * @return True if this rule has media query.
     */
    private boolean hasMediaQuery()
    {
        return !mediaQueryList.isEmpty();
    }

}
