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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

import org.apache.royale.compiler.css.ConditionType;
import org.apache.royale.compiler.css.ICSSCombinator;
import org.apache.royale.compiler.css.ICSSSelector;
import org.apache.royale.compiler.css.ICSSSelectorCondition;
import com.google.common.collect.ImmutableList;

/**
 * Implementation for a simple selector.
 */
public class CSSSelector extends CSSNodeBase implements ICSSSelector
{

    protected CSSSelector(final CSSCombinator combinator,
                          final String elementName,
                          final String namespacePrefix,
                          final List<CSSSelectorCondition> conditions,
                          final CommonTree tree,
                          final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.SELECTOR);

        final boolean isEmptySelector =
                elementName == null && namespacePrefix == null && conditions == null;
        assert !isEmptySelector : "Invalid selector. Namespace, element and conditions are all null.";

        this.combinator = combinator;
        this.namespacePrefix = namespacePrefix;
        this.elementName = elementName;

        if (conditions == null || conditions.isEmpty())
            this.conditions = ImmutableList.of();
        else
            this.conditions = new ImmutableList.Builder<ICSSSelectorCondition>().addAll(conditions).build();

    }

    private final String elementName;
    private final String namespacePrefix;
    private final ImmutableList<ICSSSelectorCondition> conditions;
    private final CSSCombinator combinator;

    @Override
    public String getElementName()
    {
        return elementName;
    }

    @Override
    public String getNamespacePrefix()
    {
        return namespacePrefix;
    }

    @Override
    public ImmutableList<ICSSSelectorCondition> getConditions()
    {
        return conditions;
    }

    @Override
    public String toString()
    {
        return getCSSSyntax();
    }

    @Override
    public String getCSSSyntax()
    {
        final StringBuilder result = new StringBuilder();
        if (combinator != null)
            result.append(combinator.getSelector().toString()).append(combinator.getCombinatorType().text);
        if (namespacePrefix != null)
            result.append(namespacePrefix).append("|");
        if (elementName != null)
            result.append(elementName);
        result.append(stringifyConditions(conditions));
        return result.toString();
    }

    public String getCSSSyntaxNoNamespaces()
    {
        final StringBuilder result = new StringBuilder();
        if (combinator != null)
            result.append(combinator.getSelector().toString()).append(combinator.getCombinatorType().text);
        if (elementName != null)
            result.append(elementName);
        result.append(stringifyConditions(conditions));
        return result.toString();
    }

    @Override
    public String stringifyConditions(List<ICSSSelectorCondition> conditions)
    {
    	StringBuilder s = new StringBuilder();
    	int n = conditions.size();
    	if (n == 0) return "";
    	if (n == 1) return conditions.get(0).toString();
    	for (int i = 0; i < n; i++)
    	{
    		ICSSSelectorCondition condition = conditions.get(i);
    		s.append(condition.getConditionType().prefix);
			s.append(condition.getValue());
    	}
    	return s.toString();
    }
    @Override
    public ICSSCombinator getCombinator()
    {
        return combinator;
    }

    /**
     * Convert the selectors chained by combinators into a list of simple
     * selectors ordering from ancestors to descendants.
     * <p>
     * For example: <code>s|HBox s|VBox s|Label</code><br>
     * will be converted to:<br>
     * <code>List { HBox, VBox, Label }</code>
     * 
     * @param selector The subject selector in a descendant selector chain.
     * @return A list of selectors.
     */
    public static ImmutableList<ICSSSelector> getCombinedSelectorList(ICSSSelector selector)
    {
        final Deque<ICSSSelector> deque = new ArrayDeque<ICSSSelector>();
        while (selector != null)
        {
            deque.offerFirst(selector);
            if (selector.getCombinator() == null)
                selector = null;
            else
                selector = selector.getCombinator().getSelector();
        }
        return ImmutableList.copyOf(deque);
    }

    /**
     * This implementation is based on
     * {@code mx.styles::CSSStyleDeclaration.isAdvanced()} in Flex SDK 4.5.2.
     */
    @Override
    public boolean isAdvanced()
    {
        // Namespace prefix is advanced syntax.
        if (namespacePrefix != null)
            return true;

        // Non-class condition selector is advanced syntax.
        for (ICSSSelectorCondition condition : conditions)
        {
            if (condition.getConditionType() != ConditionType.CLASS)
                return true;
        }

        // Combined selector is advanced syntax.
        if (combinator != null)
            return true;

        // Universal selector is advanced syntax.
        if ("*".equals(elementName))
            return true;

        return false;
    }
}
