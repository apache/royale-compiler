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
import org.apache.royale.swf.tags.IDefineBinaryImageTag;

/**
 * The SWF file format supports three basic types of fills for a shape.
 * <ul>
 * <li><b>Solid fill</b> A simple RGB or RGBA color that fills a portion of a
 * shape. An alpha value of 255 means a completely opaque fill. An alpha value
 * of zero means a completely transparent fill. Any alpha between 0 and 255 will
 * be partially transparent.</li>
 * <li><b>Gradient Fill</b> A gradient fill can be either a linear or a radial
 * gradient. For an in-depth description of how gradients are defined, see
 * {@link Gradient}.</li>
 * <li><b>Bitmap fill</b> Bitmap fills refer to a bitmap characterId. There are
 * two styles: clipped and tiled. A clipped bitmap fill repeats the color on the
 * edge of a bitmap if the fill extends beyond the edge of the bitmap. A tiled
 * fill repeats the bitmap if the fill extends beyond the edge of the bitmap.</li>
 * </ul>
 */
public class FillStyle implements IFillStyle
{
    public static final int SOLID_FILL = 0x00;
    public static final int LINEAR_GRADIENT_FILL = 0x10;
    public static final int RADIAL_GRADIENT_FILL = 0x12;
    public static final int FOCAL_RADIAL_GRADIENT_FILL = 0x13;
    public static final int REPEATING_BITMAP_FILL = 0x40;
    public static final int CLIPPED_BITMAP_FILL = 0x41;
    public static final int NON_SMOOTHED_REPEATING_BITMAP = 0x42;
    public static final int NON_SMOOTHED_CLIPPED_BITMAP = 0x43;

    private int fillStyleType;
    private RGB color;
    private Matrix gradientMatrix;
    private Gradient gradient;
    private ICharacterTag bitmapCharacter;
    private Matrix bitmapMatrix;

    /**
     * Constructor for a bitmap filled style
     * 
     * @param fillStyleType The type of the fill tyle.
     * @param matrix The bitmap matrix.
     * @param tag The bitmap character tag.
     */
    public FillStyle(int fillStyleType, Matrix matrix, IDefineBinaryImageTag tag)
    {
        setFillStyleType(fillStyleType);
        setBitmapMatrix(matrix);
        setBitmapCharacter(tag);
    }

    public FillStyle()
    {
    }

    public int getFillStyleType()
    {
        return fillStyleType;
    }

    public void setFillStyleType(int fillStyleType)
    {
        this.fillStyleType = fillStyleType;
    }

    public RGB getColor()
    {
        return color;
    }

    public void setColor(RGB color)
    {
        this.color = color;
    }

    public Matrix getGradientMatrix()
    {
        return gradientMatrix;
    }

    public void setGradientMatrix(Matrix gradientMatrix)
    {
        this.gradientMatrix = gradientMatrix;
    }

    public Gradient getGradient()
    {
        return gradient;
    }

    public void setGradient(Gradient gradient)
    {
        this.gradient = gradient;
    }

    public ICharacterTag getBitmapCharacter()
    {
        return bitmapCharacter;
    }

    public void setBitmapCharacter(ICharacterTag bitmapCharacter)
    {
        this.bitmapCharacter = bitmapCharacter;
    }

    public Matrix getBitmapMatrix()
    {
        return bitmapMatrix;
    }

    public void setBitmapMatrix(Matrix bitmapMatrix)
    {
        this.bitmapMatrix = bitmapMatrix;
    }

    /**
     * If this fill style uses a bitmap fill, get the referred bitmap character.
     */
    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        int fillStyle = getFillStyleType();

        if (fillStyle == FillStyle.CLIPPED_BITMAP_FILL ||
            fillStyle == FillStyle.NON_SMOOTHED_CLIPPED_BITMAP ||
            fillStyle == FillStyle.NON_SMOOTHED_REPEATING_BITMAP ||
            fillStyle == FillStyle.REPEATING_BITMAP_FILL)
        {
            assert bitmapCharacter != null;
            return CharacterIterableFactory.from(bitmapCharacter);
        }

        return CharacterIterableFactory.empty();
    }
}
