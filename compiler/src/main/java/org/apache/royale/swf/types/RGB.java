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
 * The RGB record represents a color as a 24-bit red, green, and blue value.
 */
public class RGB implements IDataType
{
    protected final short red;
    protected final short green;
    protected final short blue;

    /**
     * Create an {@code RGB} record.
     * 
     * @param red red value
     * @param green green value
     * @param blue blue value
     */
    public RGB(int red, int green, int blue)
    {
        this.red = (short)red;
        this.green = (short)green;
        this.blue = (short)blue;
    }

    /**
     * Create an {@code RGB} record.
     * 
     * @param rgb rgb integer value
     */
    public RGB(int rgb)
    {
        this.red = (short)((rgb >>> 16) & 0xff);
        this.green = (short)((rgb >>> 8) & 0xff);
        this.blue = (short)(rgb & 0xff);
    }

    /**
     * Get red component.
     * 
     * @return red component.
     */
    public short getRed()
    {
        return red;
    }

    /**
     * Get green component.
     * 
     * @return green component.
     */
    public short getGreen()
    {
        return green;
    }

    /**
     * Get blue component.
     * 
     * @return blue component.
     */
    public short getBlue()
    {
        return blue;
    }

    @Override
    public String toString()
    {
        return "#"
                + RGBA.toByte(red)
                + RGBA.toByte(green)
                + RGBA.toByte(blue);
    }
}
