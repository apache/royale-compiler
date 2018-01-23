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

import org.apache.royale.compiler.css.ICSSNode;
import com.google.common.collect.ImmutableList;

/**
 * A CSS node with text value.
 */
public class CSSTextNode extends CSSTypedNode
{
    /**
     * Create a text node with children.
     * @param text Text value.
     * @param childNodes Child nodes.
     */
    protected CSSTextNode(final String text, final ImmutableList<? extends ICSSNode> childNodes)
    {
        super(CSSModelTreeType.STRING, childNodes);
        assert text != null : "Expected text.";
        this.text = text;
    }

    /**
     * Node data is a string.
     */
    private final String text;

    /**
     * @return Node text.
     */
    public String getText()
    {
        return text;
    }
}
