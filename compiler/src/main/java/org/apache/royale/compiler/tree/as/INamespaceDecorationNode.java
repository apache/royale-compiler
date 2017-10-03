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

import org.apache.royale.compiler.common.IDecoration;

/**
 * An AST node representing a namespace attribute on a <code>class</code>,
 * <code>interface</code>, <code>function</code>, <code>var</code>,
 * <code>const</code> or <code>namespace</code> declaration.
 */
public interface INamespaceDecorationNode extends IDecoration, IIdentifierNode
{
    enum NamespaceDecorationKind
    {
        NAME,
        QUALIFIED_NAME,
        CONFIG
    }

    /**
     * Returns the type of namespace decoration kind this item represents
     */
    NamespaceDecorationKind getNamespaceDecorationKind();

    /**
     * Is this namespace a qualifier in an expression
     * such as 'ns' in:
     *   ns::foo;
     * @return  true if this is a qualifier for an expression
     */
    boolean isExpressionQualifier();
}
