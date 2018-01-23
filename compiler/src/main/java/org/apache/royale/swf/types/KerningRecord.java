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
 * A Kerning Record defines the distance between two glyphs in EM square
 * coordinates. Certain pairs of glyphs appear more aesthetically pleasing if
 * they are moved closer together, or farther apart. The FontKerningCode1 and
 * FontKerningCode2 fields are the character codes for the left and right
 * characters. The FontKerningAdjustment field is a signed integer that defines
 * a value to be added to the advance value of the left character.
 */
public class KerningRecord implements IDataType
{
    private int code1;
    private int code2;
    private int adjustment;

    /**
     * @return the code1
     */
    public int getCode1()
    {
        return code1;
    }

    /**
     * @param code1 the code1 to set
     */
    public void setCode1(int code1)
    {
        this.code1 = code1;
    }

    /**
     * @return the code2
     */
    public int getCode2()
    {
        return code2;
    }

    /**
     * @param code2 the code2 to set
     */
    public void setCode2(int code2)
    {
        this.code2 = code2;
    }

    /**
     * @return the adjustment
     */
    public int getAdjustment()
    {
        return adjustment;
    }

    /**
     * @param adjustment the adjustment to set
     */
    public void setAdjustment(int adjustment)
    {
        this.adjustment = adjustment;
    }
}
