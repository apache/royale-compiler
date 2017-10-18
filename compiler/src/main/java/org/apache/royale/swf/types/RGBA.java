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

package org.apache.royale.swf.types;

/**
 * The RGBA record represents a color as 32-bit red, green, blue and alpha
 * value. An RGBA color with an alpha value of 255 is completely opaque. An RGBA
 * color with an alpha value of zero is completely transparent. Alpha values
 * between zero and 255 are partially transparent.
 */
public class RGBA extends RGB
{
    protected short alpha;

    /**
     * Create an {@code RGBA} record.
     * 
     * @param red red component
     * @param green green component
     * @param blue blue component
     * @param alpha alpha component
     */
    public RGBA(int red, int green, int blue, int alpha)
    {
        super(red, green, blue);
        this.alpha = (short)alpha;
    }

    /**
     * Get alpha component.
     * 
     * @return alpha component
     */
    public short getAlpha()
    {
        return alpha;
    }

    /**
     * puts in leading zeros to make things come out right
     * 
     * @param val The value.
     */
    public static String toByte(int val)
    {
        if ((val < 0) || (val > 0xff))
        {
            assert false;
            return "!!";
        }
        if (val < 0x10)
        {
            return "0" + Integer.toHexString(val);
        }
        return Integer.toHexString(val);

    }

    @Override
    public String toString()
    {
        return "#"
                + toByte(red)
                + toByte(green)
                + toByte(blue)
                + toByte(alpha);
    }
}
