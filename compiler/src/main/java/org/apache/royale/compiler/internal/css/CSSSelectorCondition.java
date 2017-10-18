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

import org.apache.royale.compiler.css.ConditionType;
import org.apache.royale.compiler.css.ICSSSelectorCondition;
import com.google.common.base.Strings;

/**
 * Implementation for CSS conditional selector.
 */
public class CSSSelectorCondition extends CSSNodeBase implements ICSSSelectorCondition
{

    protected CSSSelectorCondition(final String value,
                                   final ConditionType type,
                                   final CommonTree tree,
                                   final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.SELECTOR_CONDITION);
        assert !Strings.isNullOrEmpty(value) : "Selector condition value can't be empty.";
        assert type != null : "Selector condition type can't be null.";
        this.value = value;
        this.type = type;
    }

    private final String value;
    private final ConditionType type;

    @Override
    public String getValue()
    {
        return value;
    }

    @Override
    public ConditionType getConditionType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return type.prefix.concat(value);
    }
}
