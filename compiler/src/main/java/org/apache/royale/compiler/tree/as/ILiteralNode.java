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

import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;

/**
 * An AST node representing a literal value.
 * <p>
 * An <code>ILiteralNode</code> can represent <code>null</code>, <code>void</code>,
 * and literal values of the following types:
 * <ul>
 * <li>Array</li>
 * <li>Boolean</li>
 * <li>int</li>
 * <li>Number</li>
 * <li>Object</li>
 * <li>RegExp</li>
 * <li>String</li>
 * <li>uint</li>
 * <li>Vector</li>
 * <li>XML</li>
 * <li>XMLList</li>
 * </ul>
 * The <code>int</code>, <code>uint</code>, and <code>Number</code>
 * types are represented more completely by the {@link INumericLiteralNode} subinterface.
 * The <code>RegExp</code> type is represented more completely by the
 * {@link IRegExpLiteralNode} subinterrface.
 * <p>
 * The shape of an <code>Array</code> literal is
 * <pre>
 * ILiteralNode "Array"
 *   IExpressionNode <-- getChild(0)
 *   IExpressionNode <-- getChild(1)
 *   ...
 * </pre>
 * For example, <code>[ 1, 2, 3 ]</code> is represented as
 * <pre>
 * ILiteralNode "Array"
 *   INumericLiteralNode 1
 *   INumericLiteralNode 2
 *   INumericLiteralNode 3
 * </pre>
 * The shape of an <code>Object</code> literal is
 * <pre>
 * ILiteralNode "Object"
 *   IObjectLIteralValuePairNode <-- getChild(0)
 *   IObjectLiteralValuePairNode <-- getChild(1)
 *   ..
 * </pre>
 * For example, <code>{ a: 1, b: 2 } is represented as
 * <pre>
 * ILiteralNode "Object"
 *   IObjectLiteralValuePairNode
 *     IIdentifierNode "a"
 *     INumericLiteralNode 1
 *   IObjectLiteralValuePairNode
 *     IIdentifierNode "b"
 *     INumericLiteralNode 2
 * </pre>
 * The other types of literals nodes do not have children.
 */
public interface ILiteralNode extends IExpressionNode
{
    /**
     * Represents a kind of ActionScript literal
     */
    enum LiteralType
    {
        /**
         * A string literal, designated by either <code>'</code> or
         * <code>"</code> <br>
         * For example: <code>"The ActionScript 3 language"</code> or
         * <code>'This is a test'</code>
         */
        STRING(BuiltinType.STRING),

        /**
         * a numeric literal of any type supported by the ActionScript language <br>
         * For example: <code> 10 </code> or <code>3.14159265</code>
         */
        NUMBER(BuiltinType.NUMBER),

        /**
         * A boolean, either <code>true<code> or <code>false</code>
         */
        BOOLEAN(BuiltinType.BOOLEAN),

        /**
         * an XML literal as defined by E4X
         */
        XML(BuiltinType.XML),

        /**
         * an XMLList literal as defined by E4X
         */
        XMLLIST(BuiltinType.XMLLIST),

        /**
         * a regular expression, designated by <code>&#047;<code>.
         * <br>For example: <code> &#047;[a-zA-Z]*&#047;</code>
         */
        REGEXP(BuiltinType.REGEXP),

        /**
         * an object literal For example: <code>null</code> or
         * <code>void 0</code>
         */
        OBJECT(BuiltinType.OBJECT),

        /**
         * an array literal
         */
        ARRAY(BuiltinType.ARRAY),

        /**
         * a vector literal
         */
        VECTOR(BuiltinType.VECTOR),

        /**
         * a null literal
         */
        NULL(BuiltinType.NULL),

        /**
         * a null literal
         */
        VOID(BuiltinType.VOID);

        private BuiltinType builtinType;

        LiteralType(BuiltinType builtinType)
        {
            this.builtinType = builtinType;
        }

        /**
         * Returns the type of the literal this node represents
         * 
         * @return the type of this literal
         */
        public BuiltinType getType()
        {
            return builtinType;
        }
    }

    /**
     * Returns the value of this literal as a String
     * 
     * @return the value of this literal as a String
     */
    String getValue();

    /**
     * Returns the value of this literal as a String
     * 
     * @param rawValue True if you want the raw value, otherwise some massaging
     * of the value will be done before returning the value if it's a String
     * (enclosing quotes will be removed).
     * @return the value of this literal as a String
     */
    String getValue(boolean rawValue);

    /**
     * Returns the {@link LiteralType} that this node represents
     */
    LiteralType getLiteralType();
}
