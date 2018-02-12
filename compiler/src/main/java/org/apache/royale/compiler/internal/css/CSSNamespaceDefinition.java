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

import static com.google.common.base.Preconditions.checkState;

import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

import org.apache.royale.compiler.css.ICSSNamespaceDefinition;
import com.google.common.base.Joiner;

/**
 * Implementation for {@code @namespace} statements.
 */
public class CSSNamespaceDefinition extends CSSNodeBase implements ICSSNamespaceDefinition
{

    /**
     * Create a node for {@code @namespace} statement.
     */
    protected CSSNamespaceDefinition(final String prefix,
                                     final String uri,
                                     final CommonTree tree,
                                     final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.NAMESPACE_DEFINITION);
        assert uri != null : "@namespace URI can't be null.";
        if (!uri.equals("<missing STRING>")) {
            assert CSSStringPropertyValue.isQuoted(uri) : "Do not strip namespace quotes in parser.";
        }

        this.prefix = prefix;
        this.uri = CSSStringPropertyValue.stripQuotes(uri);
        checkState(this.uri.length() > 0, "@namespace URI can't be empty.");
    }

    private final String prefix;
    private final String uri;

    @Override
    public String getPrefix()
    {
        return prefix;
    }

    @Override
    public String getURI()
    {
        return uri;
    }

    @Override
    public String toString()
    {
        return Joiner.on(" ")
                .skipNulls()
                .join("@namespace", prefix, '"' + uri + '"', ";");
    }
}
