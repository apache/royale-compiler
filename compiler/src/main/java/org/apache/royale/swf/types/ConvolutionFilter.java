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
 * The Convolution filter is a two-dimensional discrete convolution. It is
 * applied on each pixel of a display object.
 * <p>
 * The convolution is applied on each of the RGBA color components and then
 * saturated, except when the PreserveAlpha flag is set; in this case, the alpha
 * channel value is not modified.
 * <p>
 * The clamping flag specifies how pixels outside of the input pixel plane are
 * handled. If set to false, the DefaultColor value is used, and otherwise, the
 * pixel is clamped to the closest valid input pixel.
 */
public class ConvolutionFilter implements IDataType
{
    private int matrixX;
    private int matrixY;
    private float divisor;
    private float bias;
    private float matrix[];
    private RGBA defaultColor;
    private boolean clamp;
    private boolean preserveAlpha;

    /**
     * @return the matrixX
     */
    public int getMatrixX()
    {
        return matrixX;
    }

    /**
     * @param matrixX the matrixX to set
     */
    public void setMatrixX(int matrixX)
    {
        this.matrixX = matrixX;
    }

    /**
     * @return the matrixY
     */
    public int getMatrixY()
    {
        return matrixY;
    }

    /**
     * @param matrixY the matrixY to set
     */
    public void setMatrixY(int matrixY)
    {
        this.matrixY = matrixY;
    }

    /**
     * @return the divisor
     */
    public float getDivisor()
    {
        return divisor;
    }

    /**
     * @param divisor the divisor to set
     */
    public void setDivisor(float divisor)
    {
        this.divisor = divisor;
    }

    /**
     * @return the bias
     */
    public float getBias()
    {
        return bias;
    }

    /**
     * @param bias the bias to set
     */
    public void setBias(float bias)
    {
        this.bias = bias;
    }

    /**
     * @return the matrix
     */
    public float[] getMatrix()
    {
        return matrix;
    }

    /**
     * @param matrix the matrix to set
     */
    public void setMatrix(float[] matrix)
    {
        this.matrix = matrix;
    }

    /**
     * @return the defaultColor
     */
    public RGBA getDefaultColor()
    {
        return defaultColor;
    }

    /**
     * @param defaultColor the defaultColor to set
     */
    public void setDefaultColor(RGBA defaultColor)
    {
        this.defaultColor = defaultColor;
    }

    /**
     * @return the clamp
     */
    public boolean isClamp()
    {
        return clamp;
    }

    /**
     * @param clamp the clamp to set
     */
    public void setClamp(boolean clamp)
    {
        this.clamp = clamp;
    }

    /**
     * @return the preserveAlpha
     */
    public boolean isPreserveAlpha()
    {
        return preserveAlpha;
    }

    /**
     * @param preserveAlpha the preserveAlpha to set
     */
    public void setPreserveAlpha(boolean preserveAlpha)
    {
        this.preserveAlpha = preserveAlpha;
    }
}
