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
 * The Bevel filter creates a smooth bevel on display list objects.
 */
public class BevelFilter implements IDataType
{
    private RGBA shadowColor;
    private RGBA highlightColor;
    private float blurX;
    private float blurY;
    private float angle;
    private float distance;
    private float strength;
    private boolean innerShadow;
    private boolean knockout;
    private boolean compositeSource;
    private boolean onTop;
    private int passes;

    /**
     * @return the shadowColor
     */
    public RGBA getShadowColor()
    {
        return shadowColor;
    }

    /**
     * @param shadowColor the shadowColor to set
     */
    public void setShadowColor(RGBA shadowColor)
    {
        this.shadowColor = shadowColor;
    }

    /**
     * @return the highlightColor
     */
    public RGBA getHighlightColor()
    {
        return highlightColor;
    }

    /**
     * @param highlightColor the highlightColor to set
     */
    public void setHighlightColor(RGBA highlightColor)
    {
        this.highlightColor = highlightColor;
    }

    /**
     * @return the blurX
     */
    public float getBlurX()
    {
        return blurX;
    }

    /**
     * @param blurX the blurX to set
     */
    public void setBlurX(float blurX)
    {
        this.blurX = blurX;
    }

    /**
     * @return the blurY
     */
    public float getBlurY()
    {
        return blurY;
    }

    /**
     * @param blurY the blurY to set
     */
    public void setBlurY(float blurY)
    {
        this.blurY = blurY;
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
     * @return the strength
     */
    public float getStrength()
    {
        return strength;
    }

    /**
     * @param strength the strength to set
     */
    public void setStrength(float strength)
    {
        this.strength = strength;
    }

    /**
     * @return the innerShadow
     */
    public boolean isInnerShadow()
    {
        return innerShadow;
    }

    /**
     * @param innerShadow the innerShadow to set
     */
    public void setInnerShadow(boolean innerShadow)
    {
        this.innerShadow = innerShadow;
    }

    /**
     * @return the knockout
     */
    public boolean isKnockout()
    {
        return knockout;
    }

    /**
     * @param knockout the knockout to set
     */
    public void setKnockout(boolean knockout)
    {
        this.knockout = knockout;
    }

    /**
     * @return the compositeSource
     */
    public boolean isCompositeSource()
    {
        return compositeSource;
    }

    /**
     * @param compositeSource the compositeSource to set
     */
    public void setCompositeSource(boolean compositeSource)
    {
        this.compositeSource = compositeSource;
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

    /**
     * @return the passes
     */
    public int getPasses()
    {
        return passes;
    }

    /**
     * @param passes the passes to set
     */
    public void setPasses(int passes)
    {
        this.passes = passes;
    }
}
