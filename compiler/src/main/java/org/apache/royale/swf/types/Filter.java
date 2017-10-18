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
 * IFilter record.
 */
public class Filter implements IDataType
{
    public static final int DROP_SHADOW = 0;
    public static final int BLUR = 1;
    public static final int GLOW = 2;
    public static final int BEVEL = 3;
    public static final int GRADIENT_GLOW = 4;
    public static final int CONVOLUTION = 5;
    public static final int COLOR_MATRIX = 6;
    public static final int GRADIENT_BEVEL = 7;

    private int filterID;
    private DropShadowFilter dropShadowFilter;
    private BlurFilter blurFilter;
    private GlowFilter glowFilter;
    private BevelFilter bevelFilter;
    private GradientGlowFilter gradientGlowFilter;
    private ConvolutionFilter convolutionFilter;
    private float colorMatrixFilter[] = new float[20];
    private GradientBevelFilter gradientBevelFilter;

    /**
     * @return the filterID
     */
    public int getFilterID()
    {
        return filterID;
    }

    /**
     * @param filterID the filterID to set
     */
    public void setFilterID(int filterID)
    {
        this.filterID = filterID;
    }

    /**
     * @return the dropShadowFilter
     */
    public DropShadowFilter getDropShadowFilter()
    {
        return dropShadowFilter;
    }

    /**
     * @param dropShadowFilter the dropShadowFilter to set
     */
    public void setDropShadowFilter(DropShadowFilter dropShadowFilter)
    {
        this.dropShadowFilter = dropShadowFilter;
    }

    /**
     * @return the blurFilter
     */
    public BlurFilter getBlurFilter()
    {
        return blurFilter;
    }

    /**
     * @param blurFilter the blurFilter to set
     */
    public void setBlurFilter(BlurFilter blurFilter)
    {
        this.blurFilter = blurFilter;
    }

    /**
     * @return the glowFilter
     */
    public GlowFilter getGlowFilter()
    {
        return glowFilter;
    }

    /**
     * @param glowFilter the glowFilter to set
     */
    public void setGlowFilter(GlowFilter glowFilter)
    {
        this.glowFilter = glowFilter;
    }

    /**
     * @return the bevelFilter
     */
    public BevelFilter getBevelFilter()
    {
        return bevelFilter;
    }

    /**
     * @param bevelFilter the bevelFilter to set
     */
    public void setBevelFilter(BevelFilter bevelFilter)
    {
        this.bevelFilter = bevelFilter;
    }

    /**
     * @return the gradientGlowFilter
     */
    public GradientGlowFilter getGradientGlowFilter()
    {
        return gradientGlowFilter;
    }

    /**
     * @param gradientGlowFilter the gradientGlowFilter to set
     */
    public void setGradientGlowFilter(GradientGlowFilter gradientGlowFilter)
    {
        this.gradientGlowFilter = gradientGlowFilter;
    }

    /**
     * @return the convolutionFilter
     */
    public ConvolutionFilter getConvolutionFilter()
    {
        return convolutionFilter;
    }

    /**
     * @param convolutionFilter the convolutionFilter to set
     */
    public void setConvolutionFilter(ConvolutionFilter convolutionFilter)
    {
        this.convolutionFilter = convolutionFilter;
    }

    /**
     * @return the colorMatrixFilter
     */
    public float[] getColorMatrixFilter()
    {
        return colorMatrixFilter;
    }

    /**
     * @param colorMatrixFilter the colorMatrixFilter to set
     */
    public void setColorMatrixFilter(float[] colorMatrixFilter)
    {
        this.colorMatrixFilter = colorMatrixFilter;
    }

    /**
     * @return the gradientBevelFilter
     */
    public GradientBevelFilter getGradientBevelFilter()
    {
        return gradientBevelFilter;
    }

    /**
     * @param gradientBevelFilter the gradientBevelFilter to set
     */
    public void setGradientBevelFilter(GradientBevelFilter gradientBevelFilter)
    {
        this.gradientBevelFilter = gradientBevelFilter;
    }

    /**
     * for debugging purposes only returns the toString value of its filter.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        switch (filterID)
        {
            case DROP_SHADOW:
                sb.append(getDropShadowFilter().toString());
                break;
            case BLUR:
                sb.append(getBlurFilter().toString());
                break;
            case GLOW:
                sb.append(getGlowFilter()).toString();
                break;
            case BEVEL:
                sb.append(getBevelFilter().toString());
                break;
            case GRADIENT_GLOW:
                sb.append(getGradientGlowFilter().toString());
                break;
            case CONVOLUTION:
                sb.append(getConvolutionFilter().toString());
                break;
            case COLOR_MATRIX:
                float[] matrix = getColorMatrixFilter();
                for (float e : matrix)
                    sb.append(e);
                break;
            case GRADIENT_BEVEL:
                sb.append(getGradientBevelFilter().toString());
                break;
        }
        return sb.toString();
    }
}
