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
 * The Gradient Glow filter is extension of the normal Glow IFilter and allow a
 * gradient to be specified instead of a single color. Instead of multiplying a
 * single color value by the shadow-pixel plane value, the shadow-pixel plane
 * value is mapped directly into the gradient ramp to obtain the resulting color
 * pixel value, which is then composited by using one of the specified
 * compositing modes.
 */
public class GradientGlowFilter extends GlowFilter
{
    private int numColors;
    private RGBA gradientColors[];
    private int gradientRatio[];
    private float angle;
    private float distance;
    private boolean onTop;

    /**
     * @return the numColors
     */
    public int getNumColors()
    {
        return numColors;
    }

    /**
     * @param numColors the numColors to set
     */
    public void setNumColors(int numColors)
    {
        this.numColors = numColors;
    }

    /**
     * @return the gradientColors
     */
    public RGBA[] getGradientColors()
    {
        return gradientColors;
    }

    /**
     * @param gradientColors the gradientColors to set
     */
    public void setGradientColors(RGBA[] gradientColors)
    {
        this.gradientColors = gradientColors;
    }

    /**
     * @return the gradientRatio
     */
    public int[] getGradientRatio()
    {
        return gradientRatio;
    }

    /**
     * @param gradientRatio the gradientRatio to set
     */
    public void setGradientRatio(int[] gradientRatio)
    {
        this.gradientRatio = gradientRatio;
    }

    /**
     * @return the angle
     */
    public float getAngle()
    {
        return angle;
    }

    /**
     * @param angle the angle to set
     */
    public void setAngle(float angle)
    {
        this.angle = angle;
    }

    /**
     * @return the distance
     */
    public float getDistance()
    {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(float distance)
    {
        this.distance = distance;
    }

    /**
     * @return the onTop
     */
    public boolean isOnTop()
    {
        return onTop;
    }

    /**
     * @param onTop the onTop to set
     */
    public void setOnTop(boolean onTop)
    {
        this.onTop = onTop;
    }

}
