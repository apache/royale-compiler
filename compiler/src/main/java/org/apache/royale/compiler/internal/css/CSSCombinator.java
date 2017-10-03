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
import org.apache.royale.compiler.css.CombinatorType;
import org.apache.royale.compiler.css.ICSSCombinator;
import org.apache.royale.compiler.css.ICSSSelector;

/**
 * Implementation of {@link ICSSCombinator}.
 */
public class CSSCombinator extends CSSNodeBase implements ICSSCombinator
{
    protected CSSCombinator(final CSSSelector selector, final CombinatorType type, 
            final CommonTree tree, final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.COMBINATOR);   
        assert selector != null : "Selector can't be null.";
        assert type != null : "Combinator type can't be null.";
        this.selector = selector;
        this.type = type;
    }

    private final CSSSelector selector;
    private final CombinatorType type;

    @Override
    public ICSSSelector getSelector()
    {
        return selector;
    }

    @Override
    public CombinatorType getCombinatorType()
    {
        return type;
    }
}
