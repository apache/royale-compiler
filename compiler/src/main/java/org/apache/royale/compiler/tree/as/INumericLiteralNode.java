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
 * An AST node representing a numeric literal such as <code>3</code>,
 * <code>0x1FFF</code>, or <code>-1.23e10</code>.
 * <p>
 * This node has no children.
 */
public interface INumericLiteralNode
{
    /**
     * Maximum value of the AS3 <code>int</code> datatype
     */
    static final int MAX_INT_VALUE = Integer.MAX_VALUE;

    /**
     * Minimum value of the AS3 <code>int</code> datatype
     */
    static final int MIN_INT_VALUE = Integer.MIN_VALUE;

    /**
     * Maximum value of the AS3 <code>uint</code> datatype
     */
    static final long MAX_UINT_VALUE = (long)Math.pow(2, 32) - 1l;

    /**
     * Maximum value of the AS3 <code>Number</code> datatype
     */
    static final double MAX_NUMBER_VALUE = 1.79769313486231e+308;

    /**
     * Minimum value of the AS3 <code>Number</code> datatype
     */
    static final double MIN_NUMBER_VALUE = 4.940656458412467e-324;

    /**
     * Represents the value of a numeric literal found in AS3 source
     */
    static interface INumericValue
    {
        /**
         * Returns the assumed type of this numeric value based on the range of
         * the number. This does not take surrounding source, or program flow,
         * into consideration
         * 
         * @return either Number, int or uint
         */
        public BuiltinType getAssumedType();

        /**
         * Returns the integral value of this number as an AS3
         * <code>Number</code>
         * 
         * @return an <code>int</code> value
         */
        public double toInteger();

        /**
         * Returns the value of this number as an AS3 <code>int</code>
         * 
         * @return a <code>int</code> value
         */
        public int toInt32();

        /**
         * Returns the value of this number as an AS3 <code>uint</code>
         * 
         * @return an <code>uint</code> value
         */
        public long toUint32();

        /**
         * Returns the value of this number as an AS3 <code>Number</code>
         * 
         * @return an <code>Number</code> value
         */
        public double toNumber();

        /**
         * Returns the value of this number as a String
         * 
         * @return the orginal string value of this number
         */
        @Override
        public String toString();
    }

    INumericValue getNumericValue() throws NumberFormatException;

}
