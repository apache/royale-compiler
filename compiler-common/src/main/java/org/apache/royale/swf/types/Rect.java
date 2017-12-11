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
 * A rectangle value represents a rectangular region defined by a minimum x- and
 * y-coordinate position and a maximum x- and y-coordinate position. The RECT
 * record must be byte aligned.
 */
public class Rect implements IDataType
{
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;

    /**
     * Create a rect record by size.
     * 
     * @param width width of rect
     * @param height height of rect
     */
    public Rect(int width, int height)
    {
        this(0, width, 0, height);
    }

    /**
     * Create a rect record by coordinates.
     * 
     * @param xMin x minimum position
     * @param xMax x maximum position
     * @param yMin y minimum position
     * @param yMax y maximum position
     */
    public Rect(int xMin, int xMax, int yMin, int yMax)
    {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    @Override
    public String toString()
    {
        if ((xMin != 0) || (yMin != 0))
        {
            return "(" + xMin + "," + yMin + "),(" + xMax + "," + yMax + ")";
        }
        else
        {
            return new StringBuilder().append(xMax).append('x').append(yMax).toString();
        }
    }

    public int getWidth()
    {
        return xMax - xMin;
    }

    public int getHeight()
    {
        return yMax - yMin;
    }

    public int xMin()
    {
        return xMin;
    }

    public int xMax()
    {
        return xMax;
    }

    public int yMin()
    {
        return yMin;
    }

    public int yMax()
    {
        return yMax;
    }
}
