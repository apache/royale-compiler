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
 * Supported condition types for {@link ICSSSelectorCondition}.
 */
public enum ConditionType
{
    /**
     * For example: <code>s|Label.className</code>
     */
    CLASS("."),

    /**
     * For example: <code>s|Label#idValue</code>
     */
    ID("#"),

    /**
     * For example: <code>s|Label:loadingState</code>
     */
    PSEUDO(":"),

    /**
     * For example: <code>s|Label:loadingState</code>
     */
    PSEUDO_ELEMENT("::"),

    /**
     * For example: <code>s|Panel:not(:first-child)</code>
     */
    NOT("not"),

    /**
     * For example: <code>s|Label[loadingState]</code>
     */
    ATTRIBUTE("[");

    /**
     * Prefix character of the condition type.
     */
    public final String prefix;

    private ConditionType(String prefix)
    {
        this.prefix = prefix;
    }
}
