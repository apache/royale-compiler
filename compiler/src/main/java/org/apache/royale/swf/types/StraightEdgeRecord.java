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

import org.apache.royale.swf.io.SWFWriter;

/**
 * The {@code StraightEdgeRecord} stores the edge as an X-Y delta. The delta is
 * added to the current drawing position, and this becomes the new drawing
 * position. The edge is rendered between the old and new drawing positions.
 * <p>
 * Straight edge records support three types of lines:
 * <ul>
 * <li>General lines.</li>
 * <li>Horizontal lines.</li>
 * <li>Vertical lines.</li>
 * </ul>
 * General lines store both X and Y deltas, the horizontal and vertical lines
 * store only the X delta and Y delta respectively.
 */
public class StraightEdgeRecord extends EdgeRecord
{
    /** Type of line. */
    public static enum LineType
    {
        GENERAL, HORIZONTAL, VERTICAL
    }

    private int deltaX = 0;
    private int deltaY = 0;

    public StraightEdgeRecord()
    {
        super(ShapeRecordType.STRAIGHT_EDGE);
    }

    public StraightEdgeRecord(int deltaX, int deltaY)
    {
        this();
        setDelta(deltaX, deltaY);
    }

    public int getDeltaX()
    {
        return deltaX;
    }

    public int getDeltaY()
    {
        return deltaY;
    }

    public void setDelta(int deltaX, int deltaY)
    {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    /**
     * Get number of bits required to store delta x/y. The result value is 2
     * less than the actual number.
     * 
     * @return number of bits per value
     */
    public int getNumBits()
    {
        final int nBits = SWFWriter.requireSBCount(SWFWriter.maxNum(deltaX, deltaY, 0, 0));
        return (nBits < 2) ? 0 : nBits - 2;
    }

    /**
     * Get line type.
     * 
     * @return line type
     */
    public LineType getLineType()
    {
        if (deltaX == 0)
        {
            return LineType.VERTICAL;
        }
        else if (deltaY == 0)
        {
            return LineType.HORIZONTAL;
        }
        else
        {
            return LineType.GENERAL;
        }
    }

}
