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
 * Represents a <code>SoundStreamBlock</code> tag in a SWF file.
 * <p>
 * The SoundStreamBlock tag defines sound data that is interleaved with frame
 * data so that sounds can be played as the SWF file is streamed over a network
 * connection. The SoundStreamBlock tag must be preceded by a SoundStreamHead or
 * SoundStreamHead2 tag.
 * <p>
 * There may only be one SoundStreamBlock tag per SWF frame.
 */
public class SoundStreamBlockTag extends Tag implements IAlwaysLongTag
{
    /**
     * Constructor.
     */
    public SoundStreamBlockTag()
    {
        super(TagType.SoundStreamBlock);
    }

    private byte streamSoundData[];

    /**
     * @return the streamSoundData
     */
    public byte[] getStreamSoundData()
    {
        return streamSoundData;
    }

    /**
     * @param streamSoundData the streamSoundData to set
     */
    public void setStreamSoundData(byte[] streamSoundData)
    {
        this.streamSoundData = streamSoundData;
    }
}
