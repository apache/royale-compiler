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
 * Morph line style data type.
 */
public class MorphLineStyle implements ILineStyle
{
    private int startWidth;
    private int endWidth;
    private RGBA startColor;
    private RGBA endColor;

    /**
     * Width of line in start shape in twips.
     * 
     * @return the startWidth
     */
    public int getStartWidth()
    {
        return startWidth;
    }

    /**
     * Set start width.
     * 
     * @param value the startWidth to set
     */
    public void setStartWidth(int value)
    {
        startWidth = value;
    }

    /**
     * Width of line in end shape in twips.
     * 
     * @return the endWidth
     */
    public int getEndWidth()
    {
        return endWidth;
    }

    /**
     * Set end width.
     * 
     * @param value the endWidth to set
     */
    public void setEndWidth(int value)
    {
        endWidth = value;
    }

    /**
     * Color value including alpha channel information for start shape.
     * 
     * @return the startColor
     */
    public RGBA getStartColor()
    {
        return startColor;
    }

    /**
     * Set start color.
     * 
     * @param value the startColor to set
     */
    public void setStartColor(RGBA value)
    {
        startColor = value;
    }

    /**
     * Color value including alpha channel information for end shape.
     * 
     * @return the endColor
     */
    public RGBA getEndColor()
    {
        return endColor;
    }

    /**
     * Set end color.
     * 
     * @param value the endColor to set
     */
    public void setEndColor(RGBA value)
    {
        endColor = value;
    }

}
