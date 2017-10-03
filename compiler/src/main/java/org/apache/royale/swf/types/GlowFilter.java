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
 * The Glow filter works in the same way as the Drop Shadow filter, except that
 * it does not have a distance and angle parameter. Therefore, it can run
 * slightly faster.
 */
public class GlowFilter implements IDataType
{
    private RGBA glowColor;
    private float blurX;
    private float blurY;
    private float strength;
    private boolean innerGlow;
    private boolean knockout;
    private boolean compositeSource;
    private int passes;

    /**
     * @return the glowColor
     */
    public RGBA getGlowColor()
    {
        return glowColor;
    }

    /**
     * @param glowColor the glowColor to set
     */
    public void setGlowColor(RGBA glowColor)
    {
        this.glowColor = glowColor;
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
     * @return the innerGlow
     */
    public boolean isInnerGlow()
    {
        return innerGlow;
    }

    /**
     * @param innerGlow the innerGlow to set
     */
    public void setInnerGlow(boolean innerGlow)
    {
        this.innerGlow = innerGlow;
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
