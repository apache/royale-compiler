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
 * Morph gradient record data type.
 */
public class MorphGradRecord implements IDataType
{
    private int startRatio;
    private RGBA startColor;
    private int endRatio;
    private RGBA endColor;

    /**
     * Get the ratio value for start shape.
     * 
     * @return the start ratio
     */
    public int getStartRatio()
    {
        return startRatio;
    }

    /**
     * Set the ratio value for start shape.
     * 
     * @param value the start ratio
     */
    public void setStartRatio(int value)
    {
        startRatio = value;
    }

    /**
     * Get the color of gradient for start shape.
     * 
     * @return the start color
     */
    public RGBA getStartColor()
    {
        return startColor;
    }

    /**
     * Set the color of gradient for start shape.
     * 
     * @param value the start color
     */
    public void setStartColor(RGBA value)
    {
        startColor = value;
    }

    /**
     * Get the ratio value for end shape.
     * 
     * @return end ratio
     */
    public int getEndRatio()
    {
        return endRatio;
    }

    /**
     * Set the ratio value for end shape.
     * 
     * @param value the endRatio to set
     */
    public void setEndRatio(int value)
    {
        endRatio = value;
    }

    /**
     * Get color of gradient for end shape.
     * 
     * @return the end color
     */
    public RGBA getEndColor()
    {
        return endColor;
    }

    /**
     * Set color of gradient for end shape.
     * 
     * @param value the end color
     */
    public void setEndColor(RGBA value)
    {
        endColor = value;
    }

}
