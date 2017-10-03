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

import com.google.common.collect.ImmutableList;

/**
 * Model for a CSS3 rule. A CSS rule has one or many selectors (selector group).
 * If the rule is enclosed in a media query, it has a list of media query
 * conditions.
 */
public interface ICSSRule extends ICSSNode
{
    /**
     * Get a list of media query conditions associated with this CSS rule.
     * 
     * @return A list of {@code ICSSMediaQueryCondition} objects; Null if the
     * rule is not enclosed in a media query.
     */
    ImmutableList<ICSSMediaQueryCondition> getMediaQueryConditions();

    /**
     * Get all the subjects in the selector group in their declared order.
     * <p>
     * A CSS rule can have one or many selectors separated by comma(s). Each
     * selector has a subject.
     * <p>
     * The <i>element</i> of the right most simple selector in an
     * {@code ICSSSelector} is the <i>subject</i> of the current selector
     * object.
     * <p>
     * <i>Example: selector subject.</i><br>
     * Subject for "{@code s|HBox s|Button.round s|Label.dark}" is "Label". <br>
     * Subject for "{@code s|Panel .noskin}" is "*". ("{@code .noskin}" is the
     * implicit form of "{@code *.noskin}").
     * <p>
     * The subject selector is the root of the combination of simple selectors
     * in this "chain".
     * <p>
     * <i>Example: selector combination.</i><br>
     * <code> A > B C ~ D E</code><br>
     * "E" is the subject. Its linked to "D" as <i>descendant</i>; "D" is linked
     * to "C" as <i>general sibling</i>; "B" is the <i>child</i> of "A".
     * <p>
     * Starting from the subject, you can walk up the chain right to left.
     * <p>
     * <i>Example: CSS rule with selector group.</i>
     * 
     * <pre>
     * s|HBox .rounded s|Label.big,
     * s|VBox .rounded s|Label.small { fontWeight:bold; }
     * </pre>
     * 
     * The return value for the above rule would be list
     * <code>[s|Label, s|Label]</code>.
     * 
     * @return All the subjects in the selector group in their declared order.
     */
    ImmutableList<ICSSSelector> getSelectorGroup();

    /**
     * Get the CSS properties in their declared order.
     * 
     * @return A list of CSS properties.
     */
    ImmutableList<ICSSProperty> getProperties();
}
