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
 * The MATRIX record represents a standard 2x3 transformation matrix of the sort
 * commonly used in 2D graphics. It is used to describe the scale, rotation, and
 * translation of a graphic object.
 */
public class Matrix implements IDataType
{
    private boolean hasScale, hasRotate;
    private double scaleX, scaleY, rotateSkew0, rotateSkew1;
    private int translateX, translateY;

    public Matrix()
    {
        hasScale = false;
        hasRotate = false;
    }

    public void setScale(double scaleX, double scaleY)
    {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.hasScale = true;
    }

    public void setRotate(double rotateSkew0, double rotateSkew1)
    {
        this.rotateSkew0 = rotateSkew0;
        this.rotateSkew1 = rotateSkew1;
        this.hasRotate = true;
    }

    public void setTranslate(int translateX, int translateY)
    {
        this.translateX = translateX;
        this.translateY = translateY;
    }

    public boolean hasScale()
    {
        return hasScale;
    }

    public boolean hasRotate()
    {
        return hasRotate;
    }

    public double getScaleX()
    {
        return scaleX;
    }

    public double getScaleY()
    {
        return scaleY;
    }

    public int getTranslateX()
    {
        return translateX;
    }

    public int getTranslateY()
    {
        return translateY;
    }

    public double getRotateSkew0()
    {
        return rotateSkew0;
    }

    public double getRotateSkew1()
    {
        return rotateSkew1;
    }
    
    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder();

        if (hasScale)
        {
            b.append("s");
            b.append((float)scaleX).append(",").append((float)scaleY);
            b.append(" ");
        }

        if (hasRotate)
        {
            b.append("r");
            b.append((float)rotateSkew0).append(",").append((float)rotateSkew1);
            b.append(" " );
        }

        b.append("t");
        b.append(translateX).append(",").append(translateY);

        return b.toString();
    }

}
