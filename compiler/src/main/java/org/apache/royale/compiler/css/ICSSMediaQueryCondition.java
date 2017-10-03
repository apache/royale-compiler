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
 * Model for a media query condition in CSS3. A CSS3 rule can be enclosed in a
 * media query directive with one or many {@code ICSSMediaQueryCondition}
 * objects. For example:
 * 
 * <pre>
 * &#64;media all and (application-dpi: 240) and (os-platform: "Android")
 * {
 *     s|Button { color: 13; }
 * }
 * </pre>
 * 
 * There are 3 media query conditions:
 * <ol>
 * <li>{@code all}<br>
 * key is null, value=all</li>
 * <li>{@code application-dpi: 240}<br>
 * key=application-dpi, value=240.</li>
 * <li>{@code os-platform: "Android"}<br>
 * key=os-platform, value="Android".</li>
 * </ol>
 * Media query conditions are key-value pairs. There are also special "key-less"
 * conditions like {@code all}.
 */
public interface ICSSMediaQueryCondition extends ICSSNode
{
    /**
     * Get the key of the media condition which is the left-hand side of the
     * colon character. For "key-less" conditions, the result is {@code null}.
     * 
     * @return Key of the media condition or {@code null}.
     */
    String getKey();

    /**
     * Get value of the media condition which is the right-hand side of the
     * colon character.
     * 
     * @return Value of the media condition.
     */
    ICSSPropertyValue getValue();
}
