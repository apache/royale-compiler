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

package org.apache.royale.compiler.css;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Model for CSS3 selectors. A {@link ICSSRule} has one or many selectors
 * connected by combinators. Currently, only <i>descendant combinator</i> is
 * supported.
 * <p>
 * A selector can optionally have one or many {@link ICSSSelectorCondition}'s.
 * <p>
 * Some of the W3C specifications and API might call this a "simple selector".
 * Since we do not implement the whole W3C SAC API stack, there's no concept of
 * "complex" selector. That's why it is called a "selector" instead of a
 * "simple selector".
 * 
 * @see <a href="http://www.w3.org/TR/css3-selectors/">css3 selectors</a>
 */
public interface ICSSSelector extends ICSSNode
{

    /**
     * Get the combinator of this selector if it exists. A combinator is the
     * selector on the left-hand side of the current selector.
     * 
     * @return A combinator or null;
     */
    ICSSCombinator getCombinator();

    /**
     * Get the element name. For example, <code>Label</code> is the element name
     * for "<code>s|Label.noBorder</code>".
     * 
     * @return Element name or null.
     */
    String getElementName();

    /**
     * Get the namespace prefix. For example, <code>mx</code> is the namespace
     * prefix for "<code>mx|Button</code>".
     * 
     * @return Namespace prefix or null.
     */
    String getNamespacePrefix();

    /**
     * Get selector conditions.
     * <p>
     * <i>Example: selector conditions.</i>
     * 
     * <pre>
     * s|Button.rounded#main:up
     * </pre>
     * 
     * In this selector, the conditions are "{@code .rounded}", "{@code #main}"
     * and "{@code :up}".
     * 
     * @return A list of conditions.
     */
    ImmutableList<ICSSSelectorCondition> getConditions();

    /**
     * This API is explicitly added so that no CSS compilation logic will depend
     * on {@code toString()} value of an {@link ICSSNode}.
     * 
     * @return The CSS text from which this selector is generated.
     */
    String getCSSSyntax();

    /**
     * This API is explicitly added so that no CSS compilation logic will depend
     * on {@code toString()} value of an {@link ICSSNode}.
     * This version is used so that Basic|SomeClass in one css file 
     * will match SomeClass in another css file.  The code that uses this has already resolved
     * the namespace prefix to an actual uri.
     * 
     * @return The CSS text from which this selector is generated without any namespace prefixes
     */
    String getCSSSyntaxNoNamespaces();

    /**
     * This API is explicitly added so that no CSS compilation logic will depend
     * on {@code toString()} value of an {@link ICSSNode}.
     * 
     * @return The CSS text from which this selector is generated.
     */
    String stringifyConditions(List<ICSSSelectorCondition> conditions);

    /**
     * Check if a selector uses Flex 4 advanced syntax. An advanced selector
     * can't be used in {@code -compatibility-version=3} mode.
     * 
     * @return True if the selector has advanced syntax.
     */
    boolean isAdvanced();
}
