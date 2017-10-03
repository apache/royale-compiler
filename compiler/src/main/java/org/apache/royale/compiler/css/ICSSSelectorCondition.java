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
 * Conditions follows the "element" in a simple selector. The supported
 * conditions in Flex at the moment are:
 * <ul>
 * <li>class - {@code s|Label.styleName} {...}</li>
 * <li>ID - {@code s|Label#pageNumber} {...}</li>
 * <li>context state - {@code s|Label:login} {...}</li>
 * </ul>
 * If attribute selectors are to be supported in the future, this interface can
 * be extended to return compound value in addition to the string of the textual
 * declaration.
 */
public interface ICSSSelectorCondition extends ICSSNode
{
    /**
     * Get the normalized value of the condition declaration. Dot (for class),
     * hash (for ID) and colon (for state) are stripped from the return value.
     * 
     * @return Normalized condition value.
     */
    String getValue();

    /**
     * @return Condition type.
     */
    ConditionType getConditionType();
}
