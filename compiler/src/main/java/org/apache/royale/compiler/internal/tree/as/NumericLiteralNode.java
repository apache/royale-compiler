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

package org.apache.royale.compiler.internal.tree.as;

import java.math.BigInteger;

import org.apache.royale.abc.semantics.ECMASupport;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.INumericLiteralNode;

public class NumericLiteralNode extends LiteralNode implements INumericLiteralNode
{
    private static final String HEX_PREFIX = "0x";
    private static final String POSITIVE_HEX_PREFIX = "+0x";
    private static final String NEGATIVE_HEX_PREFIX = "-0x";

    private static Number hexToDec(String unsignedHexString, boolean isNegative)
    {
        if (unsignedHexString.length() < 16)
        {
            final Long abs = Long.valueOf(unsignedHexString, 16);
            return isNegative ? -abs : abs;
        }

        final BigInteger bi = new BigInteger(unsignedHexString, 16);
        return isNegative ? -bi.doubleValue() : bi.doubleValue();
    }
    
    /**
     * Constructor.
     * 
     * @param text The text of the numeric literal.
     */
    public NumericLiteralNode(String text)
    {
        super(LiteralType.NUMBER, text);
        
        assert text != null && text.length() > 0 : "The text of a NumericLiteralNode cannot be null or empty";
        
        String lowerCaseText = text.toLowerCase();
        
        isHex = lowerCaseText.startsWith(HEX_PREFIX) ||
                lowerCaseText.startsWith(NEGATIVE_HEX_PREFIX) ||
                lowerCaseText.startsWith(POSITIVE_HEX_PREFIX);
    }

    /**
     * Constructor.
     */
    public NumericLiteralNode(ASToken t)
    {
        super(t, LiteralType.NUMBER);
        
        isHex = (t.getType() == ASTokenTypes.TOKEN_LITERAL_HEX_NUMBER);
    }
    
    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected NumericLiteralNode(NumericLiteralNode other)
    {
        super(other);
        
        this.numericValue = other.numericValue;
        this.isHex = other.isHex;
    }

    /**
     * Lazy evaluate numeric value from token text and cache the value.
     */
    private INumericValue numericValue;

    private final boolean isHex;

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        // AS3 defines INT / INT(0) = NaN. In order to make the constant folding in
        // CG phase be able to process "division by integer zero", we use a special
        // AST node ID {@code LiteralIntegerZeroID} for INT(0) constants.

        try
        {
            final INumericValue numeric = getNumericValue();
            
            switch (numeric.getAssumedType())
            {
                case INT:
                {
                    return numeric.toNumber() == 0 ?
                           ASTNodeID.LiteralIntegerZeroID :
                           ASTNodeID.LiteralIntegerID;
                }
                    
                case UINT:
                {
                    return numeric.toNumber() == 0 ?
                           ASTNodeID.LiteralIntegerZeroID :
                           ASTNodeID.LiteralUintID;
                }
                    
                default:
                {
                    return ASTNodeID.LiteralDoubleID;
                }
            }
        }
        catch (NumberFormatException cannot_convert)
        {
            return ASTNodeID.LiteralNumberID;
        }
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    protected NumericLiteralNode copy()
    {
        return new NumericLiteralNode(this);
    }

    //
    // INumericLiteralNode implementations
    //

    @Override
    public INumericValue getNumericValue() throws NumberFormatException
    {
        if (numericValue == null)
        {
            final Number decimal;
            
            final String lowerCaseValue = value.toLowerCase();
            
            if (isHex && lowerCaseValue.startsWith(HEX_PREFIX))
                decimal = hexToDec(value.substring(HEX_PREFIX.length()), false);
            
            else if (isHex && lowerCaseValue.startsWith(POSITIVE_HEX_PREFIX))
                decimal = hexToDec(value.substring(POSITIVE_HEX_PREFIX.length()), false);
            
            else if (isHex && lowerCaseValue.startsWith(NEGATIVE_HEX_PREFIX))
                decimal = hexToDec(value.substring(NEGATIVE_HEX_PREFIX.length()), true);
            
            else
                decimal = Double.parseDouble(value);
            
            numericValue = new NumericValue(decimal, value, isHex);
        }

        return numericValue;
    }

    //
    // Other methods
    //

    /**
     * @return <code>true</code> if the numeric literal is in hexadecimal format.
     */
    public boolean isHex()
    {
        return isHex;
    }

    //
    // Inner types
    //
    
    private static final class NumericValue implements INumericValue
    {
        /**
         * Raw text form of the numeric literal.
         */
        private final String text;

        /**
         * It's either a {@link Long} or {@link Double}.
         */
        private final Number number;

        /**
         * Inferred ABC type.
         */
        private final BuiltinType baseType;

        private NumericValue(Number number, String text, boolean isHex)
        {
            this.number = number;
            this.text = text.trim();

            final double doubleValue = number.doubleValue();
            if (text.contains(".") ||
                     number.equals(Double.POSITIVE_INFINITY) ||
                     number.equals(Double.NEGATIVE_INFINITY) ||
                     number.equals(Double.NaN))
            {
                baseType = BuiltinType.NUMBER;
            }
            else if (doubleValue == 0 && text.startsWith("-"))
            {
                baseType = BuiltinType.NUMBER;
            }
            else if (Math.floor(doubleValue) == doubleValue)
            {
                if (doubleValue >= MIN_INT_VALUE && doubleValue <= MAX_INT_VALUE)
                    baseType = BuiltinType.INT;
                else if (doubleValue >= 0 && doubleValue <= MAX_UINT_VALUE)
                    baseType = BuiltinType.UINT;
                else
                    baseType = BuiltinType.NUMBER;
            }
            else
            {
                baseType = BuiltinType.NUMBER;
            }
        }

        @Override
        public final BuiltinType getAssumedType()
        {
            return baseType;
        }

        @Override
        public final double toInteger()
        {
            return ECMASupport.toInteger(number.doubleValue());
        }

        @Override
        public final double toNumber()
        {
            return number.doubleValue();
        }

        @Override
        public final int toInt32()
        {
            return ECMASupport.toInt32(number.doubleValue());
        }

        @Override
        public final long toUint32()
        {
            if (number instanceof Long)
                return number.longValue();
            return ECMASupport.toUInt32(number.doubleValue());
        }

        @Override
        public final String toString()
        {
            return text;
        }
    }
}
