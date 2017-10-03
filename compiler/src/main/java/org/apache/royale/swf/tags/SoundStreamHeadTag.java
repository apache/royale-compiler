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
 * Represents a <code>SoundStreamHead</code> tag in a SWF file.
 * <p>
 * If a timeline contains streaming sound data, there must be a SoundStreamHead
 * or SoundStreamHead2 tag before the first sound data block . The
 * SoundStreamHead tag defines the data format of the sound data, the
 * recommended playback format, and the average number of samples per
 * SoundStreamBlock.
 * 
 * @see SoundStreamBlockTag
 */
public class SoundStreamHeadTag extends Tag
{
    // StreamSoundCompression
    public static final int SSC_ADPCM = 1;
    public static final int SSC_MP3 = 2;

    /**
     * Constructor.
     */
    public SoundStreamHeadTag()
    {
        super(TagType.SoundStreamHead);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     * @param tagType
     */
    protected SoundStreamHeadTag(TagType tagType)
    {
        super(tagType);
    }

    private int playbackSoundRate;
    private int playbackSoundSize;
    private int playbackSoundType;
    private int streamSoundCompression;
    private int streamSoundRate;
    private int streamSoundSize;
    private int streamSoundType;
    private int streamSoundSampleCount;
    private int latencySeek;

    /**
     * @return the playbackSoundRate
     */
    public int getPlaybackSoundRate()
    {
        return playbackSoundRate;
    }

    /**
     * @param playbackSoundRate the playbackSoundRate to set
     */
    public void setPlaybackSoundRate(int playbackSoundRate)
    {
        this.playbackSoundRate = playbackSoundRate;
    }

    /**
     * @return the playbackSoundSize
     */
    public int getPlaybackSoundSize()
    {
        return playbackSoundSize;
    }

    /**
     * @param playbackSoundSize the playbackSoundSize to set
     */
    public void setPlaybackSoundSize(int playbackSoundSize)
    {
        this.playbackSoundSize = playbackSoundSize;
    }

    /**
     * @return the playbackSoundType
     */
    public int getPlaybackSoundType()
    {
        return playbackSoundType;
    }

    /**
     * @param playbackSoundType the playbackSoundType to set
     */
    public void setPlaybackSoundType(int playbackSoundType)
    {
        this.playbackSoundType = playbackSoundType;
    }

    /**
     * @return the streamSoundCompression
     */
    public int getStreamSoundCompression()
    {
        return streamSoundCompression;
    }

    /**
     * @param streamSoundCompression the streamSoundCompression to set
     */
    public void setStreamSoundCompression(int streamSoundCompression)
    {
        this.streamSoundCompression = streamSoundCompression;
    }

    /**
     * @return the streamSoundRate
     */
    public int getStreamSoundRate()
    {
        return streamSoundRate;
    }

    /**
     * @param streamSoundRate the streamSoundRate to set
     */
    public void setStreamSoundRate(int streamSoundRate)
    {
        this.streamSoundRate = streamSoundRate;
    }

    /**
     * @return the streamSoundSize
     */
    public int getStreamSoundSize()
    {
        return streamSoundSize;
    }

    /**
     * @param streamSoundSize the streamSoundSize to set
     */
    public void setStreamSoundSize(int streamSoundSize)
    {
        this.streamSoundSize = streamSoundSize;
    }

    /**
     * @return the streamSoundType
     */
    public int getStreamSoundType()
    {
        return streamSoundType;
    }

    /**
     * @param streamSoundType the streamSoundType to set
     */
    public void setStreamSoundType(int streamSoundType)
    {
        this.streamSoundType = streamSoundType;
    }

    /**
     * @return the streamSoundSampleCount
     */
    public int getStreamSoundSampleCount()
    {
        return streamSoundSampleCount;
    }

    /**
     * @param streamSoundSampleCount the streamSoundSampleCount to set
     */
    public void setStreamSoundSampleCount(int streamSoundSampleCount)
    {
        this.streamSoundSampleCount = streamSoundSampleCount;
    }

    /**
     * @return the latencySeek
     */
    public int getLatencySeek()
    {
        return latencySeek;
    }

    /**
     * @param latencySeek the latencySeek to set
     */
    public void setLatencySeek(int latencySeek)
    {
        this.latencySeek = latencySeek;
    }

    /**
     * @return the sscAdpcm
     */
    public static int getSscAdpcm()
    {
        return SSC_ADPCM;
    }

    /**
     * @return the sscMp3
     */
    public static int getSscMp3()
    {
        return SSC_MP3;
    }
}
