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
 * The blur filter is based on a sub-pixel precise median filter (also known as
 * a box filter). The filter is applied on each of the RGBA color channels.
 * <p>
 * When the number of passes is set to three, it closely approximates a Gaussian
 * Blur filter. A higher number of passes is possible, but for performance
 * reasons, is not recommended.
 */
public class BlurFilter implements IDataType
{
    private float blurX;
    private float blurY;
    private int passes;

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
