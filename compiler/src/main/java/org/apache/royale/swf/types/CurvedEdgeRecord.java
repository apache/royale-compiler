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
 * The curved-edge record stores the edge as two X-Y deltas. The three points
 * that define the Quadratic Bezier are calculated like this:
 * <ol>
 * <li>The first anchor point is the current drawing position.</li>
 * <li>The control point is the current drawing position + ControlDelta.</li>
 * <li>The last anchor point is the current drawing position + ControlDelta +
 * AnchorDelta.</li>
 * </ol>
 * The last anchor point becomes the current drawing position.
 */
public class CurvedEdgeRecord extends EdgeRecord
{
    public CurvedEdgeRecord()
    {
        super(ShapeRecordType.CURVED_EDGE);
    }

    private int controlDeltaX;
    private int controlDeltaY;
    private int anchorDeltaX;
    private int anchorDeltaY;

    public int getControlDeltaX()
    {
        return controlDeltaX;
    }

    public void setControlDeltaX(int controlDeltaX)
    {
        this.controlDeltaX = controlDeltaX;
    }

    public int getControlDeltaY()
    {
        return controlDeltaY;
    }

    public void setControlDeltaY(int controlDeltaY)
    {
        this.controlDeltaY = controlDeltaY;
    }

    public int getAnchorDeltaX()
    {
        return anchorDeltaX;
    }

    public void setAnchorDeltaX(int anchorDeltaX)
    {
        this.anchorDeltaX = anchorDeltaX;
    }

    public int getAnchorDeltaY()
    {
        return anchorDeltaY;
    }

    public void setAnchorDeltaY(int anchorDeltaY)
    {
        this.anchorDeltaY = anchorDeltaY;
    }

    /**
     * Get number of bits required to store anchor delta x/y and control delta
     * x/y. The result value is 2 less than the actual number.
     * 
     * @return number of bits per value
     */
    public int getNumBits()
    {
        int nBits = SWFWriter.requireSBCount(SWFWriter.maxNum(controlDeltaX, controlDeltaY, anchorDeltaX, anchorDeltaY));
        return (nBits < 2) ? 0 : nBits - 2;
    }
}
