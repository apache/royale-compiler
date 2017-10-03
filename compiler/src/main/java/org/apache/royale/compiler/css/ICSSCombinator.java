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

/**
 * A "combinator" represents a CSS selector that combines with a selector. It
 * has a type value and an associated selector. If selector "A" is written on
 * the left of selector "B", then "A" is the combinator of "B".
 * <p>
 * For example, in the following CSS rule:<br>
 * <code> s|HBox s|Button.rounded s|Label {...} </code><br>
 * {@code s|Label} has an {@code ICSSCombinator} whose combinator type is
 * "descendant" (space character) and the combined selector is
 * {@code s|Button.rounded}.
 */
public interface ICSSCombinator
{

    /**
     * Get the selector associated with the combinator. For example: <br>
     * <code>s|VBox s|Label</code><br>
     * Then, {@code s|Label} as a combinator whose selector is {@code s|VBox}.
     * 
     * @return The selector of the combinator.
     */
    ICSSSelector getSelector();

    /**
     * Get the combinator type.
     * 
     * @return Combinator type.
     */
    CombinatorType getCombinatorType();

}
