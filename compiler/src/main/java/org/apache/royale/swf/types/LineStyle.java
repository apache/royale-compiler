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
 * A line style represents a width and color of a line.
 */
public class LineStyle implements ILineStyle
{

    public LineStyle()
    {

    }

    public LineStyle(int width, RGB color)
    {
        this.width = width;
        this.color = color;
    }

    private int width;
    private RGB color;

    public int getWidth()
    {
        return width;
    }

    public RGB getColor()
    {
        return color;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public void setColor(RGB color)
    {
        this.color = color;
    }

    @Override
    public String toString()
    {
        return "LineStyle: " + width + "px, " + color;
    }
}
