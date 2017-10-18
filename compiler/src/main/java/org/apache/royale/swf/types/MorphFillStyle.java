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

import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.CharacterIterableFactory;

/**
 * A fill style represents how a closed shape is filled in.
 */
public class MorphFillStyle implements IFillStyle
{
    private int fillStyleType; // Constants are defined in FillStyle.java
    private RGBA startColor;
    private RGBA endColor;
    private Matrix startGradientMatrix;
    private Matrix endGradientMatrix;
    private MorphGradient gradient;
    private ICharacterTag bitmap;
    private Matrix startBitmapMatrix;
    private Matrix endBitmapMatrix;

    // MorphFillStyle for DefineMorphShape2
    private int ratio1;
    private int ratio2;

    /**
     * Get the type of fill style.
     * 
     * @return fill style type
     */
    public int getFillStyleType()
    {
        return fillStyleType;
    }

    /**
     * Set the type of fill style.
     * 
     * @param value fill style type
     */
    public void setFillStyleType(int value)
    {
        fillStyleType = value;
    }

    /**
     * Solid fill color with opacity information for start shape.
     * 
     * @return the startColor
     */
    public RGBA getStartColor()
    {
        return startColor;
    }

    /**
     * Set solid fill color with opacity information for start shape.
     * 
     * @param value the startColor to set
     */
    public void setStartColor(RGBA value)
    {
        startColor = value;
    }

    /**
     * Solid fill color with opacity information for end shape.
     * 
     * @return the endColor
     */
    public RGBA getEndColor()
    {
        return endColor;
    }

    /**
     * Set solid fill color with opacity information for end shape.
     * 
     * @param value the endColor to set
     */
    public void setEndColor(RGBA value)
    {
        endColor = value;
    }

    /**
     * Matrix for gradient fill for start shape.
     * 
     * @return the startGradientMatrix
     */
    public Matrix getStartGradientMatrix()
    {
        return startGradientMatrix;
    }

    /**
     * Set matrix for gradient fill for start shape.
     * 
     * @param value the startGradientMatrix to set
     */
    public void setStartGradientMatrix(Matrix value)
    {
        startGradientMatrix = value;
    }

    /**
     * Matrix for gradient fill for end shape.
     * 
     * @return the endGradientMatrix
     */
    public Matrix getEndGradientMatrix()
    {
        return endGradientMatrix;
    }

    /**
     * Set matrix for gradient fill for end shape.
     * 
     * @param value the endGradientMatrix to set
     */
    public void setEndGradientMatrix(Matrix value)
    {
        endGradientMatrix = value;
    }

    /**
     * Gradient fill.
     * 
     * @return the gradient
     */
    public MorphGradient getGradient()
    {
        return gradient;
    }

    /**
     * Set gradient fill.
     * 
     * @param value the gradient to set
     */
    public void setGradient(MorphGradient value)
    {
        gradient = value;
    }

    /**
     * Bitmap character for fill.
     * 
     * @return the bitmap
     */
    public ICharacterTag getBitmap()
    {
        return bitmap;
    }

    /**
     * Set bitmap character for fill.
     * 
     * @param bitmap the bitmap to set
     */
    public void setBitmap(ICharacterTag bitmap)
    {
        this.bitmap = bitmap;
    }

    /**
     * Matrix for bitmap fill for start shape.
     * 
     * @return the startBitmapMatrix
     */
    public Matrix getStartBitmapMatrix()
    {
        return startBitmapMatrix;
    }

    /**
     * Set matrix for bitmap fill for start shape.
     * 
     * @param value the startBitmapMatrix to set
     */
    public void setStartBitmapMatrix(Matrix value)
    {
        startBitmapMatrix = value;
    }

    /**
     * Matrix for bitmap fill for end shape.
     * 
     * @return the endBitmapMatrix
     */
    public Matrix getEndBitmapMatrix()
    {
        return endBitmapMatrix;
    }

    /**
     * Set matrix for bitmap fill for end shape.
     * 
     * @param value the endBitmapMatrix to set
     */
    public void setEndBitmapMatrix(Matrix value)
    {
        endBitmapMatrix = value;
    }

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        int fillStyle = getFillStyleType();

        if (fillStyle == FillStyle.CLIPPED_BITMAP_FILL ||
            fillStyle == FillStyle.NON_SMOOTHED_CLIPPED_BITMAP ||
            fillStyle == FillStyle.NON_SMOOTHED_REPEATING_BITMAP ||
            fillStyle == FillStyle.REPEATING_BITMAP_FILL)
        {
            assert bitmap != null;
            return CharacterIterableFactory.from(bitmap);
        }

        return CharacterIterableFactory.empty();

    }

    /**
     * TODO: ratio1 is not documented in the SWF 10 Specification.
     * 
     * @return ratio1
     */
    public int getRatio1()
    {
        return ratio1;
    }

    /**
     * TODO: ratio1 is not documented in the SWF 10 Specification.
     */
    public void setRatio1(int ratio1)
    {
        this.ratio1 = ratio1;
    }

    /**
     * TODO: ratio2 is not documented in the SWF 10 Specification.
     * 
     * @return ratio2
     */
    public int getRatio2()
    {
        return ratio2;
    }

    /**
     * TODO: ratio2 is not documented in the SWF 10 Specification.
     */
    public void setRatio2(int ratio2)
    {
        this.ratio2 = ratio2;
    }
}
