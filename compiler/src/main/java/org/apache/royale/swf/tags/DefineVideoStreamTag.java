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

package org.apache.royale.swf.tags;

import org.apache.royale.swf.TagType;

/**
 * Represents a <code>DefineVideoStream</code> tag in a SWF file.
 * <p>
 * DefineVideoStream defines a video character that can later be placed on the
 * display list.
 */
public class DefineVideoStreamTag extends CharacterTag
{
    /**
     * Constructor.
     */
    public DefineVideoStreamTag()
    {
        super(TagType.DefineVideoStream);
    }

    private int numFrames;
    private int width;
    private int height;
    private int deblocking;
    private boolean smoothing;
    private int codecID;

    /**
     * @return the numFrames
     */
    public int getNumFrames()
    {
        return numFrames;
    }

    /**
     * @param numFrames the numFrames to set
     */
    public void setNumFrames(int numFrames)
    {
        this.numFrames = numFrames;
    }

    /**
     * @return the width
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width)
    {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height)
    {
        this.height = height;
    }

    /**
     * @return the deblocking
     */
    public int getDeblocking()
    {
        return deblocking;
    }

    /**
     * @param deblocking the deblocking to set
     */
    public void setDeblocking(int deblocking)
    {
        this.deblocking = deblocking;
    }

    /**
     * @return the smoothing
     */
    public boolean isSmoothing()
    {
        return smoothing;
    }

    /**
     * @param smoothing the smoothing to set
     */
    public void setSmoothing(boolean smoothing)
    {
        this.smoothing = smoothing;
    }

    /**
     * @return the codecID
     */
    public int getCodecID()
    {
        return codecID;
    }

    /**
     * @param codecID the codecID to set
     */
    public void setCodecID(int codecID)
    {
        this.codecID = codecID;
    }
}
