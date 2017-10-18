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
 * CSS3 selector combinators. Only <i>descendant</i> is supported at the moment.
 * 
 * @see <a href="http://www.w3.org/TR/css3-selectors/#combinators">css3 selectors : combinators</a>
 */
public enum CombinatorType
{
    DESCENDANT(" "),
    CHILD(">"),
    PRECEDED("+"),
    SIBLING("~");

    private CombinatorType(String text)
    {
        this.text = text;
    }

    /**
     * Symbol that represents the combinator type.
     */
    public final String text;

}
