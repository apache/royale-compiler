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

package org.apache.royale.compiler.tree.as;

import org.apache.royale.compiler.internal.tree.as.LanguageIdentifierNode;

/**
 * An AST node representing an identifier that is part of the ActionScript language.
 * <p>
 * This kind of node is created for the following identifiers:
 * <ul>
 * <li><code>this</code></li>
 * <li><code>super</code></li>
 * <li><code>*</code></li>
 * <li><code>void</code></li>
 * <li><code>...</code></li>
 * </ul>
 * This node has no children.
 */
public interface ILanguageIdentifierNode
{
    /**
     * Represents the kind of language identifier that can be represented
     */
    enum LanguageIdentifierKind
    {
        /**
         * The <code>this</code> keyword
         */
        THIS,
        
        /**
         * The <code>super</code> keyword
         */
        SUPER,
        
        /**
         * The <code>*</code> type specification
         */
        ANY_TYPE,
        
        /**
         * The <code>...</code> parameter
         */
        REST,
        
        /**
         * The <code>void</code> keyword
         */
        VOID
    }

    /**
     * Returns the kind of the identifier that his
     * {@link LanguageIdentifierNode} represents
     * 
     * @return a {@link LanguageIdentifierKind}
     */
    LanguageIdentifierKind getKind();
}
