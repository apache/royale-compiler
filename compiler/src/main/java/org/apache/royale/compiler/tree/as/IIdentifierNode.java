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

/**
 * An AST node representing an identifier.
 * <p>
 * Identifiers may be short (such as <code>width</code>,
 * <code>int</code>, or <code>public</code>), dotted
 * (such as <code>flash.display.Sprite</code>), or parameterized
 * (such as {@code Vector.<Event>}).
 * <p>
 * Identifiers serve two purposes: in a declaration of a class, interface,
 * function, variable, constant, or namespace they provide the name
 * of the thing being defined; otherwise they provide a reference
 * to that definition.
 * <p>
 * In some cases, such as for packages and language identifiers,
 * the declaration is implicit rather than existing in source code
 * or in byte code.
 */
public interface IIdentifierNode extends IExpressionNode
{
    /**
     * Represents different types of identifiers
     */
    static enum IdentifierType
    {
        /**
         * Represents a fully qualified name: flash.events.EventDispatcher
         */
        FULLY_QUALIFIED_NAME,
        
        /**
         * Represents a namespace identifier: public, private, internal,
         * protected, etc
         */
        NAMESPACE,

        /**
         * Represents a standard short name, that is not qualified
         */
        NAME,

        /**
         * Represents a typed name, found in typed collections like Vector
         */
        TYPED_NAME
    }

    /**
     * Gets the full name of this identifier
     * 
     * @return a String representing the name of this identifier
     */
    String getName();

    /**
     * Returns of the type of this identifier.
     * 
     * @return the type of this identifier
     * @see IdentifierType
     */
    IdentifierType getIdentifierType();
}
