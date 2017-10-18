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

/**
 * Tree node type of CSS DOM tree.
 */
public enum CSSModelTreeType
{
    COMBINATOR,
    DOCUMENT,
    FONT_FACE,
    FONT_FACE_LIST,
    KEYFRAMES,
    KEYFRAMES_WEBKIT,
    MEDIA_QUERY_CONDITION,
    MEDIA_QUERY,
    NAMESPACE_DEFINITION,
    NAMESPACE_LIST,
    PROPERTY,
    PROPERTY_VALUE,
    PROPERTY_LIST,
    RULE,
    RULE_LIST,
    SELECTOR,
    SELECTOR_CONDITION,
    SELECTOR_GROUP,
    STRING;
}
