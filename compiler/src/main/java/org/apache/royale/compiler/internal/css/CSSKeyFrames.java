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

import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

/**
 * Implementation for {@code @keyframe} statement DOM.
 */
public class CSSKeyFrames extends CSSNodeBase
{

    /**
     * Construct a {@code CSSFontFace} from a list of properties. The parser
     * doesn't validate if the properties are acceptable by the
     * {@code @font-face} statement, so that we don't need to update the grammar
     * when new properties are added to {@code @font-face} statement.
     * 
     * @param properties key value pairs inside the {@code @font-face} block.
     */
    protected CSSKeyFrames(final String id,
                          final CSSModelTreeType type,
                          final CommonTree tree,
                          final TokenStream tokenStream)
    {
        super(tree, tokenStream, type);

        this.id = id;
    }

    private final String id;

    @SuppressWarnings("incomplete-switch")
	@Override
    public String toString()
    {
        final StringBuilder result = new StringBuilder();
        switch (getOperator())
        {
            case KEYFRAMES:
                result.append("@keyframes ");
                break;
            case KEYFRAMES_WEBKIT:
                result.append("@-webkit-keyframes ");
                break;
        }
        result.append(id);
        result.append(" {");
        return result.toString();
    }
}
