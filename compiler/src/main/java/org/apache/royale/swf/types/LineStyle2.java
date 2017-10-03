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
 * {@code LineStyle2} builds upon the capabilities of the {@code LineStyle}
 * record by allowing the use of new types of joins and caps as well as scaling
 * options and the ability to fill a stroke. In order to use {@code LineStyle2},
 * the shape must be defined with {@link org.apache.royale.swf.tags.DefineShape4Tag}.
 */
public class LineStyle2 extends LineStyle
{
    /* Start Cap Style */
    public static final int SCS_ROUND_CAP = 0,
                            SCS_NO_CAP = 1,
                            SCS_SQUARE_CAP = 2;

    /* Join Style */
    public static final int JS_ROUND_JOIN = 0,
                            JS_BEVEL_JOIN = 1,
                            JS_MITER_JOIN = 2;

    /* End Cap Style */
    public static final int ECS_ROUND_CAP = 0,
                            ECS_NO_CAP = 1,
                            ECS_SQUARE_CAP = 2;

    private int startCapStyle;
    private int joinStyle;
    private boolean hasFillFlag;
    private boolean noHScaleFlag;
    private boolean noVScaleFlag;
    private boolean pixelHintingFlag;
    private boolean noClose;
    private int endCapStyle;
    private float miterLimitFactor;
    private FillStyle fillType;

    public int getStartCapStyle()
    {
        return startCapStyle;
    }

    public void setStartCapStyle(int startCapStyle)
    {
        this.startCapStyle = startCapStyle;
    }

    public int getJoinStyle()
    {
        return joinStyle;
    }

    public void setJoinStyle(int joinStyle)
    {
        this.joinStyle = joinStyle;
    }

    public boolean isHasFillFlag()
    {
        return hasFillFlag;
    }

    public void setHasFillFlag(boolean hasFillFlag)
    {
        this.hasFillFlag = hasFillFlag;
    }

    public boolean isNoHScaleFlag()
    {
        return noHScaleFlag;
    }

    public void setNoHScaleFlag(boolean noHScaleFlag)
    {
        this.noHScaleFlag = noHScaleFlag;
    }

    public boolean isNoVScaleFlag()
    {
        return noVScaleFlag;
    }

    public void setNoVScaleFlag(boolean noVScaleFlag)
    {
        this.noVScaleFlag = noVScaleFlag;
    }

    public boolean isPixelHintingFlag()
    {
        return pixelHintingFlag;
    }

    public void setPixelHintingFlag(boolean pixelHintingFlag)
    {
        this.pixelHintingFlag = pixelHintingFlag;
    }

    public boolean isNoClose()
    {
        return noClose;
    }

    public void setNoClose(boolean noClose)
    {
        this.noClose = noClose;
    }

    public int getEndCapStyle()
    {
        return endCapStyle;
    }

    public void setEndCapStyle(int endCapStyle)
    {
        this.endCapStyle = endCapStyle;
    }

    public float getMiterLimitFactor()
    {
        return miterLimitFactor;
    }

    public void setMiterLimitFactor(float miterLimitFactor)
    {
        this.miterLimitFactor = miterLimitFactor;
    }

    public FillStyle getFillType()
    {
        return fillType;
    }

    public void setFillType(FillStyle fillType)
    {
        this.fillType = fillType;
    }

}
