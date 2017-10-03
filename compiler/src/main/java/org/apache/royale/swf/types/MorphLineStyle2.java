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
 * MORPHLINESTYLE2 builds upon the capabilities of the MORPHLINESTYLE record by
 * allowing the use of new types of joins and caps as well as scaling options
 * and the ability to fill morph strokes. In order to use MORPHLINESTYLE2, the
 * shape must be defined with {@link org.apache.royale.swf.tags.DefineMorphShape2Tag}
 * not {@link org.apache.royale.swf.tags.DefineMorphShapeTag}.
 * <p>
 * While the MORPHLINESTYLE record permits only rounded joins and round caps,
 * MORPHLINESTYLE2 also supports miter and bevel joins, and square caps and no
 * caps. For an illustration of the available joins and caps, see the diagram in
 * the LINESTYLE2 description.
 * <p>
 * When using MORPHLINESTYLE for a miter join, a MiterLimitFactor must be
 * specified and is used along with StartWidth or EndWidth to calculate the
 * maximum miter length: Max miter length = MORPHLINESTYLE2 MiterLimitFactor *
 * MORPHLINESTYLE2 Width
 * <p>
 * If the miter join exceeds the maximum miter length, Flash Player will cut off
 * the miter. Note that MiterLimitFactor is an 8.8 fixed-point value.
 * <p>
 * MORPHLINESTYLE2 also includes the option for pixel hinting in order to
 * correct blurry vertical or horizontal lines.
 */
public class MorphLineStyle2 extends MorphLineStyle
{
    private int startCapStyle;
    private int joinStyle;
    private boolean hasFillFlag;
    private boolean noHScaleFlag;
    private boolean noVScaleFlag;
    private boolean pixelHintingFlag;
    private boolean noClose;
    private int endCapStyle;
    private int miterLimitFactor;
    private MorphFillStyle fillType;

    /**
     * Get start-cap style.
     * 
     * @return the startCapStyle
     */
    public int getStartCapStyle()
    {
        return startCapStyle;
    }

    /**
     * Set start-cap style.
     * 
     * @param value the startCapStyle to set
     */
    public void setStartCapStyle(int value)
    {
        assert value <= 2;
        startCapStyle = value;
    }

    /**
     * Get join style.
     * 
     * @return the joinStyle
     */
    public int getJoinStyle()
    {
        return joinStyle;
    }

    /**
     * Set join style.
     * 
     * @param value the joinStyle to set
     */
    public void setJoinStyle(int value)
    {
        assert value <= 2;
        joinStyle = value;
    }

    /**
     * Get fill flag.
     * 
     * @return the hasFillFlag
     */
    public boolean isHasFillFlag()
    {
        return hasFillFlag;
    }

    /**
     * Set has fill flag.
     * 
     * @param value the hasFillFlag to set
     */
    public void setHasFillFlag(boolean value)
    {
        hasFillFlag = value;
    }

    /**
     * If true, stroke thickness will not scale if the object is scaled
     * horizontally.
     * 
     * @return the noHScaleFlag
     */
    public boolean isNoHScaleFlag()
    {
        return noHScaleFlag;
    }

    /**
     * If true, stroke thickness will not scale if the object is scaled
     * horizontally.
     * 
     * @param value the noHScaleFlag to set
     */
    public void setNoHScaleFlag(boolean value)
    {
        noHScaleFlag = value;
    }

    /**
     * If true, stroke thickness will not scale if the object is scaled
     * vertically.
     * 
     * @return the noVScaleFlag
     */
    public boolean isNoVScaleFlag()
    {
        return noVScaleFlag;
    }

    /**
     * If true, stroke thickness will not scale if the object is scaled
     * vertically.
     * 
     * @param value the noVScaleFlag to set
     */
    public void setNoVScaleFlag(boolean value)
    {
        noVScaleFlag = value;
    }

    /**
     * Get pixel hinting flag.
     * 
     * @return the pixelHintingFlag
     */
    public boolean isPixelHintingFlag()
    {
        return pixelHintingFlag;
    }

    /**
     * Set pixel hinting flag.
     * 
     * @param value the pixelHintingFlag to set
     */
    public void setPixelHintingFlag(boolean value)
    {
        pixelHintingFlag = value;
    }

    /**
     * Get is not closed.
     * 
     * @return the noClose
     */
    public boolean isNoClose()
    {
        return noClose;
    }

    /**
     * Set no close.
     * 
     * @param value the noClose to set
     */
    public void setNoClose(boolean value)
    {
        noClose = value;
    }

    /**
     * Get end cap style.
     * 
     * @return the endCapStyle
     */
    public int getEndCapStyle()
    {
        return endCapStyle;
    }

    /**
     * Set end cap style.
     * 
     * @param value the endCapStyle to set
     */
    public void setEndCapStyle(int value)
    {
        assert value <= 2;
        endCapStyle = value;
    }

    /**
     * Get miter limit factor.
     * 
     * @return the miterLimitFactor
     */
    public int getMiterLimitFactor()
    {
        return miterLimitFactor;
    }

    /**
     * Set miter limit factor.
     * 
     * @param value the miterLimitFactor to set
     */
    public void setMiterLimitFactor(int value)
    {
        miterLimitFactor = value;
    }

    /**
     * Get fill type.
     * 
     * @return the fillType
     */
    public MorphFillStyle getFillType()
    {
        return fillType;
    }

    /**
     * Set fill type.
     * 
     * @param value the fillType to set
     */
    public void setFillType(MorphFillStyle value)
    {
        fillType = value;
    }

}
