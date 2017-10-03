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

package org.apache.royale.abc.semantics;

import org.apache.royale.abc.ABCConstants;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Provides helper functions for for various operations as specified by ECMA.
 * 
 * @see <a href="http://www.ecma-international.org/publications/standards/Ecma-262.htm">ECMA 262 spec</a>
 */
public class ECMASupport
{
    private static final long MASK_32_BIT_UINT = 0xffffffffL;
    private static final double TWO_TO_THE_31ST = Math.pow(2, 31);
    private static final double TWO_TO_THE_32ND = Math.pow(2, 32);

    /**
     * The abstract operation ToInt32 converts its argument to one of 2^32
     * integer values in the range -2^31 through 2^31-1, inclusive. This
     * abstract operation functions as follows:
     * <ol>
     * <li>Let {@code number} be the result of calling ToNumber on the input
     * argument.</li>
     * <li>If {@code number} is NaN, +0, -0, positive infinity, or negative
     * infinity, return +0.</li>
     * <li>Let {@code posInt} be {@code sign(number) * floor(abs(number))}.</li>
     * <li>Let {@code int32bit} be {@code posInt modulo 2^ 32}; that is, a
     * finite integer value k of Number type with positive sign and less than
     * 2^32 in magnitude such that the mathematical difference of posInt and k
     * is mathematically an integer multiple of 2^32.</li>
     * <li>If {@code int32bit} is greater than or equal to 2^31, return
     * {@code int32bit - 2^32}, otherwise return {@code int32bit}.</li>
     * </ol>
     * <em>NOTE Given the above definition of {@code ToInt32}:</em>
     * <ul>
     * <li>The ToInt32 abstract operation is idempotent: if applied to a result
     * that it produced, the second application leaves that value unchanged.</li>
     * <li>ToInt32(ToUint32(x)) is equal to ToInt32(x) for all values of x. (It
     * is to preserve this latter property that positive infinity and negative
     * infinity are mapped to +0.)</li>
     * <li>ToInt32 maps -0 to +0.</li>
     * </ul>
     * 
     * @param number IEEE number.
     * @return 32-bit integer.
     */
    public static int toInt32(final double number)
    {
        final long int32bit = toUInt32(number);
        if (int32bit >= TWO_TO_THE_31ST)
            return (int)(int32bit - TWO_TO_THE_32ND);
        else
            return (int)int32bit;
    }

    public static int toInt32(final Number number)
    {
        return toInt32(number.doubleValue());
    }

    public static int toInt32(final Object value)
    {
        return toInt32(toNumeric(value));
    }

    /**
     * The abstract operation ToUInt32 converts its argument to one of 2^32
     * integer values in the range 0 through 2^32-1, inclusive. This abstract
     * operation functions as follows:
     * <ol>
     * <li>Let {@code number} be the result of calling ToNumber on the input
     * argument.</li>
     * <li>If {@code number} is NaN, +0, -0, positive infinity, or negative
     * infinity, return +0.</li>
     * <li>Let {@code posInt} be {@code sign(number) x floor(abs(number))}.</li>
     * <li>Let {@code int32bit} be {@code posInt modulo 2^ 32}; that is, a
     * finite integer value k of Number type with positive sign and less than
     * 2^32 in magnitude such that the mathematical difference of posInt and k
     * is mathematically an integer multiple of 2^32.</li>
     * <li>Return {@code int32bit}.</li>
     * </ol>
     * <em>NOTE Given the above definition of {@code ToInt32}:</em>
     * <ul>
     * <li>Step 5 is the only difference between {@link #toUInt32(double)} and
     * {@link #toInt32(double)}.</li>
     * <li>The ToUint32 abstract operation is idempotent: if applied to a result
     * that it produced, the second application leaves that value unchanged.</li>
     * <li>ToUint32 maps -0 to +0.</li>
     * </ul>
     * 
     * @param number IEEE number.
     * @return 32-bit unsigned integer.
     */
    public static long toUInt32(final double number)
    {
        final double posInt = toInteger(number);
        final double int32bit = posInt % TWO_TO_THE_32ND;
        return (long)int32bit & MASK_32_BIT_UINT;
    }

    /**
     * Version of toUInt32 that operates on Number
     */
    public static long toUInt32(final Number value)
    {
        return toUInt32(value.doubleValue());
    }

    /**
     * Version of toUInt32 that operates on Object
     */
    public static long toUInt32(final Object value)
    {
        return toUInt32(toNumeric(value));
    }

    /**
     * The abstract operation ToInteger converts its argument to an integral
     * numeric value. This abstract operation functions as follows:
     * <ol>
     * <li>Let {@code number} be the result of calling ToNumber on the input
     * argument.</li>
     * <li>If {@code number} is NaN, return +0.</li>
     * <li>If {@code number} is +0, -0, positive infinity, or negative infinity,
     * return {@code number}.</li>
     * <li>Return the result of computing
     * {@code sign(number) x floor(abs(number))}.</li>
     * </ol>
     * 
     * @param number IEEE number.
     * @return ECMA integer stored in a IEEE number.
     */
    public static double toInteger(final double number)
    {
        if (isNan(number) || 0 == number || Double.isInfinite(number))
            return 0;
        final double posInt = Math.signum(number) * Math.floor(Math.abs(number));
        return posInt;
    }

    /**
     * Converts "anything" to boolean using ECMA algorithm
     * 
     * @param <T> - Number or String
     */
    public static <T> boolean toBoolean(T value)
    {
        assert value != null : "to pass null use ABCConstants.NULL_VALUE";
        
        if (value == ABCConstants.NULL_VALUE) // ECMA: null is always false
            return false;
        if (value == ABCConstants.UNDEFINED_VALUE) // ECMA: undefined is always false
            return false;
        if (value instanceof Number)
            return toBoolean((Number)value);
        if (value instanceof String)
            return toBoolean((String)value);
        if (value instanceof Boolean)
            return (Boolean)value;

        return true; // ECMA: non-null object is true
    }

    /**
     * Converts a Number to boolean using ECMA algorithm The following rules
     * apply:
     * <ol>
     * <li>If number is zero, return false</li>
     * <li>If number is Nan, return false</li>
     * <li>Otherwise, return true</li>
     * </ol>
     */

    public static boolean toBoolean(Number value)
    {
        if(isNan(value.doubleValue()) )
            return false;
        return value.doubleValue() != 0; // works for all number types, because even if there is 
                                         // rounding, != 0 will still be correct
    }

    /**
     * Converts a String to boolean using ECMA algorithm The following rules
     * apply:
     * <ol>
     * <li>If value is empty, return false</li>
     * <li>Otherwise, return true</li>
     * </ol>
     */
    public static boolean toBoolean(String value)
    {
        return !value.isEmpty();
    }

    /**
     * Determines is a specific floating poing number is Nan
     */
    public static boolean isNan(final double number)
    {
        return Double.isNaN(number); // Luckily ECMA uses the standard IEEE conventions for NaN
    }

    /**
     * The Left Shift Operator ( << ) performs a bitwise left shift operation on
     * the left operand by the amount specified by the right operand. The
     * production
     * {@code ShiftExpression : ShiftExpression << AdditiveExpression} is
     * evaluated as follows:
     * <ol>
     * <li>Let lref be the result of evaluating ShiftExpression.</li>
     * <li>Let lval be GetValue(lref).</li>
     * <li>Let rref be the result of evaluating AdditiveExpression.</li>
     * <li>Let rval be GetValue(rref).</li>
     * <li>Let lnum be ToInt32(lval).</li>
     * <li>Let rnum be ToUint32(rval).</li>
     * <li>Let shiftCount be the result of masking out all but the least
     * significant 5 bits of rnum, that is, compute rnum & 0x1F.</li>
     * <li>Return the result of left shifting lnum by shiftCount bits. The
     * result is a signed 32-bit integer.</li>
     * </ol>
     * 
     * @param left Value of the left operand.
     * @param right Value of the right operand.
     * @return 32-bit signed integer.
     */
    public static int leftShiftOperation(final Number left, final Number right)
    {
        final double lval = left.doubleValue();
        final double rval = right.doubleValue();
        final int lnum = toInt32(lval);
        final long rnum = toUInt32(rval);
        final long shiftCount = rnum & 0x1F;
        return lnum << shiftCount;
    }

    /**
     * Performs a sign-filling bitwise right shift operation on the left operand
     * by the amount specified by the right operand. The production
     * {@code ShiftExpression : ShiftExpression >> AdditiveExpression} is
     * evaluated as follows:
     * <ol>
     * <li>Let lref be the result of evaluating ShiftExpression.</li>
     * <li>Let lval be GetValue(lref).</li>
     * <li>Let rref be the result of evaluating AdditiveExpression.</li>
     * <li>Let rval be GetValue(rref).</li>
     * <li>Let lnum be ToInt32(lval).</li>
     * <li>Let rnum be ToUint32(rval).</li>
     * <li>Let shiftCount be the result of masking out all but the least
     * significant 5 bits of rnum, that is, compute rnum & 0x1F.</li>
     * <li>Return the result of performing a sign-extending right shift of lnum
     * by shiftCount bits. The most significant bit is propagated. The result is
     * a signed 32-bit integer.</li>
     * </ol>
     * 
     * @param left Value of the left operand.
     * @param right Value of the right operand.
     * @return 32-bit signed integer.
     */
    public static int signedRightShiftOperation(final Number left, final Number right)
    {
        final double lval = left.doubleValue();
        final double rval = right.doubleValue();
        final int lnum = toInt32(lval);
        final long rnum = toUInt32(rval);
        final long shiftCount = rnum & 0x1F;
        return lnum >> shiftCount;
    }

    /**
     * Performs a zero-filling bitwise right shift operation on the left operand
     * by the amount specified by the right operand. The production
     * {@code ShiftExpression : ShiftExpression >>> AdditiveExpression} is
     * evaluated as follows:
     * <ol>
     * <li>Let lref be the result of evaluating ShiftExpression.</li>
     * <li>Let lval be GetValue(lref).</li>
     * <li>Let rref be the result of evaluating AdditiveExpression.</li>
     * <li>Let rval be GetValue(rref).</li>
     * <li>Let lnum be ToUInt32(lval).</li>
     * <li>Let rnum be ToUint32(rval).</li>
     * <li>Let shiftCount be the result of masking out all but the least
     * significant 5 bits of rnum, that is, compute rnum & 0x1F.</li>
     * <li>Return the result of performing a zero-filling right shift of lnum by
     * shiftCount bits. Vacated bits are filled with zero. The result is an
     * unsigned 32-bit integer.</li>
     * </ol>
     * 
     * @param left Value of the left operand.
     * @param right Value of the right operand.
     * @return 32-bit unsigned integer.
     */
    public static long unsignedRightShiftOperation(final Number left, final Number right)
    {
        final double lval = left.doubleValue();
        final double rval = right.doubleValue();
        final long lnum = toUInt32(lval);
        final long rnum = toUInt32(rval);
        final long shiftCount = rnum & 0x1F;
        return lnum >>> shiftCount;
    }

    /**
     * Performs the logical and (&&) operation on two objects. The production
     * {@code LogicalANDExpression : LogicalANDExpression && BitwiseORExpression}
     * is evaluated as follows:
     * <ol>
     * <li>Let lref be the result of evaluating LogicalANDExpression.</li>
     * <li>lval be GetValue(lref).</li>
     * <li>If ToBoolean(lval) is false, return lval.</li>
     * <li>Let rref be the result of evaluating BitwiseORExpression.</li>
     * <li>Return GetValue(rref).
     * 
     * @param <T> is the numeric type of the operands, and the return type
     * @param left Value of the left operand
     * @param right Value of the right operand
     */
    public static <T> T logicalAnd(T left, T right)
    {
        boolean b = toBoolean(left);
        return b ? right : left;
    }

    /**
     * Performs the logical or (||) operation on two objects. is evaluated as
     * follows:
     * <ol>
     * <li>Let lref be the result of evaluating LogicalANDExpression.</li>
     * <li>lval be GetValue(lref).</li>
     * <li>If ToBoolean(lval) is true, return lval.</li>
     * <li>Let rref be the result of evaluating BitwiseORExpression.</li>
     * <li>Return GetValue(rref).
     * 
     * @param <T> is the numeric type of the operands, and the return type
     * @param left Value of the left operand
     * @param right Value of the right operand
     */
    public static <T> T logicalOr(T left, T right)
    {
        boolean b = toBoolean(left);
        return b ? left : right;
    }

    /**
     * Perform the logical not (!) operation on an object As per the ECMA spec
     * this is done as follows
     * <ol>
     * <li>Let expr be the result of evaluating UnaryExpression</li>
     * <li>Let oldValue be ToBoolean(GetValue(expr)).</li>
     * <li>If oldValue is true, return false.</li>
     * <li>Return true.</li>
     * </ol>
     * 
     * @param <T> is the numeric type of the operand
     * @param e is the operand
     */
    public static <T> boolean logicalNot(T e)
    {
        return !toBoolean(e);
    }

    /**
     * Implement the ECMA ToNumber (ECMA 262, 3rd Edition section 9.3) algorithm.  This will convert any value
     * into a numeric value..
     *
     * @param value  the value to convert
     * @return       the numeric representation of the value, as specified by the ToNumber algorithm
     */
    public static <T> Number toNumeric(T value)
    {
        if( value == ABCConstants.UNDEFINED_VALUE )
            return Double.NaN;
        if( value == ABCConstants.NULL_VALUE )
            return 0;
        if( value instanceof Number )
            return (Number)value;
        if( value instanceof Boolean )
            return toNumeric((Boolean) value);
        if( value instanceof String )
            return toNumeric((String) value);

        return null;
    }

    /**
     * Implement ToNumber for boolean values - ECMA 262, 3rd edition section 9.3
     */
    private static int toNumeric (Boolean value)
    {
        return ((Boolean)value).booleanValue() ? 1 : 0;
    }


    /**
     * trim according to ECMA - will get rid of escaped unicode characters
     * that java's trim does not remove
     *
     * For use when converting Strings -> Number
     */
    private static String numberTrim(String str) {
        StringBuilder buf = new StringBuilder();
        for (int i=0; i<str.length(); i++) {
            Character c = str.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            buf.append(c);
        }
        str = new String(buf);
        buf = new StringBuilder();
        for (int i=str.length()-1; i>=0; i--) {
            Character c = str.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            buf.append(c);
        }
        str = new String(buf.reverse());
        return str;
    }

    /**
     * Implement ToNumber for string values - ECMA 262, 3rd edition section 9.3.1
     */
    private static Number toNumeric(String str )
    {
        double sign[] = new double[1];
        str = numberTrim(str);
        str = numberSign(str, sign);

        double num;
        if (str.equals("")) {
            num = 0;
        }
        else
        if (str.equals("Infinity")) {
            if (sign[0] > 0) {
                num = Double.POSITIVE_INFINITY;
            }
            else {
                num = Double.NEGATIVE_INFINITY;
            }
        }
        else
        if (str.equals("NaN")) {
            num = Double.NaN;
        }
        else if (str.startsWith("0x") || str.startsWith("0X")) {
            try {
                num = sign[0] * Long.valueOf(str.substring(2), 16);
            }
            catch (NumberFormatException e) {
                num = Double.NaN;
            }
        }
        else {
            if (!Character.isDigit(str.charAt(str.length()-1)) && str.charAt(str.length()-1) != '.') {  // localization?
                num = Double.NaN;   // "1f" "1F" "1d" "1D" are all NaN in AS3
            }
            else {
                try {
                    num = sign[0] * Double.valueOf(str);
                }
                catch (NumberFormatException e) {
                    num = Double.NaN;
                }
            }
        }
        return num;
    }

    /**
     * Helper method for converting strings to numbers - strips off any leaving '-' or '+' characters
     * and determines what the sign of the number should be
     */
    private static String numberSign(String str, double[] num) {
        if(str.length() == 0) {
            num[0] = +1;
        }
        else
        if (str.equals("-") || str.equals("+")) {
            num[0] = 0;  // leave it alone, to produce NaN later
        }
        else
        if(str.startsWith("-")) {
            num[0] = -1;
            str = str.substring(1);
        }
        else
        if(str.startsWith("+")) {
            num[0] = +1;
            str = str.substring(1);
        }
        else {
            num[0] = +1;
        }
        return str;
    }

    /**
     * Implement equality of numbers - see ECMA 262 3d Edition, section 11.9.3
     * @param l  the left number
     * @param r  the right number
     * @return   true if the numbers are equal according to the ECMA spec
     */
    public static boolean equals(Number l, Number r)
    {
        // NaN is not equal to anything, not even itself
        double lDouble = l.doubleValue();
        if( Double.isNaN(lDouble) )
            return false;

        return lDouble == r.doubleValue();
    }

    /**
     * Implement equality of strings - see ECMA 262 3d Edition, section 11.9.3
     * @param l  the left string
     * @param r  the right string
     * @return   true if the strings are equal according to the ECMA spec
     */
    public static boolean equals(String l, String r)
    {
        return l.equals(r);
    }

    /**
     * Implement equality of booleans - see ECMA 262 3d Edition, section 11.9.3
     * @param l  the left boolean
     * @param r  the right boolean
     * @return   true if the booleans are equal according to the ECMA spec
     */
    public static boolean equals(Boolean l, Boolean r)
    {
        return l.equals(r);
    }

    /**
     * Implement the Abstract Equality Comparison algorithm - see ECMA 262 3d Edition, section 11.9.3
     * @param l  the left value
     * @param r  the right value
     * @return   true if the value are equal according to the ECMA spec
     */
    public static boolean equals(Object l, Object r)
    {
        ECMAType leftType = getType(l);
        ECMAType rightType = getType(r);

        if( leftType == rightType)
        {
            switch (leftType)
            {
                case Boolean:
                    return equals((Boolean)l, (Boolean)r);
                case Number:
                    return equals((Number)l, (Number)r);
                case String:
                    return equals((String)l, (String)r);
                case Null:
                    return true;
                case Undefined:
                    return true;
            }
        }

        if( l == r )
            return true;

        switch (leftType)
        {
            case Boolean:
                return equals(toNumeric((Boolean)l), r);
            case Number:
                if( rightType == ECMAType.String )
                    return equals((Number)l, toNumeric((String)r));
                break;
            case String:
                if( rightType == ECMAType.Number )
                    return equals(toNumeric((String)l), (Number)r);
                break;
            case Null:
                if( rightType == ECMAType.Undefined )
                    return true;
                break;
            case Undefined:
                if( rightType == ECMAType.Null )
                    return true;
                break;
        }

        if( rightType == ECMAType.Boolean )
            return equals(l, toNumeric((Boolean)r));


        return false;
    }

    /**
     * Implement the strict equality comparison algorithm - see ECMA 262 3d Edition, section 11.9.6
     *
     * Used for '===', '!==', etc.
     *
     * @param l  the left value
     * @param r  the right value
     * @return   true if the values are equal according to the ECMA spec
     */
    public static boolean strictEquals(Object l, Object r)
    {
        ECMAType leftType = getType(l);
        ECMAType rightType = getType(r);

        if( leftType == rightType)
        {
            switch (leftType)
            {
                case Boolean:
                    return equals((Boolean)l, (Boolean)r);
                case Number:
                    return equals((Number)l, (Number)r);
                case String:
                    return equals((String)l, (String)r);
                case Null:
                    return true;
                case Undefined:
                    return true;
            }
        }
        return false;
    }

    /**
     * Helper method for the comparison methods - return an enum representing the type
     * of the value to avoid lots of if( instanceof ) checking.
     */
    private static ECMAType getType(Object o)
    {
        if( o instanceof String )
            return ECMAType.String;
        if( o instanceof Number )
            return ECMAType.Number;
        if( o instanceof Boolean )
            return ECMAType.Boolean;
        if( o == ABCConstants.NULL_VALUE )
            return ECMAType.Null;
        if( o == ABCConstants.UNDEFINED_VALUE )
            return ECMAType.Undefined;
        assert false : "unknown constant type";
        return null;
    }

    /**
     * Enum to represent the various ECMA types we may be folding
     */
    private static enum ECMAType
    {
        Boolean,
        Number,
        String,
        Null,
        Undefined
    }

    /**
     * Implement the abstract relational comparison algorithm - see ECMA 262 3rd edition, section 11.8.5
     *
     * less than, greater than, etc are implement in terms of this algorithm
     * @param l the first value to compare
     * @param r the second value to compare
     * @return  true, false, or java null (which means at least one operand was NaN)
     */
    private static Boolean relationalCompare(Object l, Object r)
    {
        ECMAType lType = getType(l);
        ECMAType rType = getType(r);

        if (lType == ECMAType.String && rType == ECMAType.String )
            return relationalCompare((String)l, (String)r);

        return relationalCompare(toNumeric(l), toNumeric(r));
    }

    /**
     * Specialized relational compare method for String.
     */
    private static Boolean relationalCompare(String l, String r)
    {
        if( l.compareTo(r) < 0 )
            return true;
        return false;
    }

    /**
     * Specialized relational compare method for Numbers.
     */
    private static Boolean relationalCompare(Number l, Number r)
    {
        double lVal = l.doubleValue();
        double rVal = r.doubleValue();
        if( isNan(lVal) || isNan(rVal) )
            return null;

        return lVal < rVal;
    }

    /**
     * ECMA less-than operator - ECMA 262 3rd edition, section 11.8.1
     */
    public static boolean lessThan(Number l, Number r)
    {
        Boolean res = relationalCompare(l, r);
        if( res == null )
            return false;

        return res;
    }

    /**
     * ECMA less-than operator - ECMA 262 3rd edition, section 11.8.1
     */
    public static boolean lessThan(Object l, Object r)
    {
        Boolean res = relationalCompare(l, r);
        if( res == null )
            return false;

        return res;
    }

    /**
     * ECMA less-than-or-equal operator - ECMA 262 3rd edition, section 11.8.3
     */
    public static boolean lessThanEquals(Number l, Number r)
    {
        Boolean res = relationalCompare(r, l);
        if( res == null || res == true )
            return false;

        return true;
    }

    /**
     * ECMA less-than-or-equal operator - ECMA 262 3rd edition, section 11.8.3
     */
    public static boolean lessThanEquals(Object l, Object r)
    {
        Boolean res = relationalCompare(r, l);
        if( res == null || res == true)
            return false;

        return true;
    }

    /**
     * ECMA greater-than - ECMA 262 3rd edition, section 11.8.2
     */
    public static boolean greaterThan(Number l, Number r)
    {
        Boolean res = relationalCompare(r, l);
        if( res == null )
            return false;

        return res;
    }

    /**
     * ECMA greater-than - ECMA 262 3rd edition, section 11.8.2
     */
    public static boolean greaterThan(Object l, Object r)
    {
        Boolean res = relationalCompare(r, l);
        if( res == null )
            return false;

        return res;
    }

    /**
     * ECMA greater-than-or-equal operator - ECMA 262 3rd edition, section 11.8.4
     */
    public static boolean greaterThanEquals(Number l, Number r)
    {
        Boolean res = relationalCompare(l, r);
        if( res == null || res == true )
            return false;

        return true;
    }

    /**
     * ECMA greater-than-or-equal operator - ECMA 262 3rd edition, section 11.8.4
     */
    public static boolean greaterThanEquals(Object l, Object r)
    {
        Boolean res = relationalCompare(l, r);
        if( res == null || res == true)
            return false;

        return true;
    }

    /**
     * Implement the ECMA ToString algorithm - ECMA 262 3rd edition, section 9.8
     * @param o the value to convert to a String
     * @return  the String representation of the value
     */
    public static String toString(Object o)
    {
        if( o instanceof String )
            return (String)o;
        if( o instanceof Double )
            return toString((Double) o);
        if( o instanceof Integer )
            return toString((Integer)o);
        if( o instanceof Long )
            return toString((Long)o);
        if( o instanceof Boolean )
            return toString((Boolean)o);
        if( o == ABCConstants.NULL_VALUE )
            return "null";
        if( o == ABCConstants.UNDEFINED_VALUE )
            return "undefined";

        return null;
    }

    /**
     * Implement the ECMA ToString algorithm - ECMA 262 3rd edition, section 9.8.1
     */
    public static String toString(Double d)
    {
        String str = "";
        if (d.isNaN() || d.isInfinite()) {
            str = "" + d;
        }
        else {
            // machinations for getting formatting to match AVM
            BigDecimal bd = new BigDecimal(d.doubleValue()).round(MathContext.DECIMAL64).stripTrailingZeros();
//            BigDecimal bd = new BigDecimal(d.doubleValue()).round(new MathContext(15asc , RoundingMode.DOWN)).stripTrailingZeros();
            //out.println("dval="+dval+" bd="+bd+" scale="+bd.scale()+" prec="+bd.precision());
            if (bd.scale() < 0 && bd.scale() > -21) {
                bd = bd.setScale(0);
            }
            str = "" + bd;
            str = str.replaceFirst("E", "e");
        }
        return str;
    }

    /**
     * Specialized toString for ints - ECMA 262 3rd edition, section 9.8.1
     */
    public static String toString(Integer i)
    {
        return "" + new BigDecimal(i);
    }

    /**
     * Specialized toString for uint's - ECMA 262 3rd edition, section 9.8.1
     */
    public static String toString(Long i)
    {
        return "" + new BigDecimal(i);
    }

    /**
     * Specialized toString for booleans - ECMA 262 3rd edition, section 9.8
     */
    public static String toString(Boolean b)
    {
        if( b.booleanValue() )
            return "true";
        return "false";
    }

}
