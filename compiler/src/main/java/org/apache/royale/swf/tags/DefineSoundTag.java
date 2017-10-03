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
 * Represents a <code>DefineSound</code> tag in a SWF file.
 * <p>
 * The DefineSound tag defines an event sound. It includes the audio coding
 * format, sampling rate, size of each sample (8 or 16 bit), a stereo/mono flag,
 * and an array of audio samples. Note that not all of these parameters will be
 * honored depending on the audio coding format.
 */
public class DefineSoundTag extends CharacterTag
{
    /**
     * Constructor.
     */
    public DefineSoundTag()
    {
        super(TagType.DefineSound);
    }

    private int soundFormat;
    private int soundRate;
    private int soundSize;
    private int soundType;
    private long soundSampleCount;
    private byte soundData[];

    /**
     * @return the soundFormat
     */
    public int getSoundFormat()
    {
        return soundFormat;
    }

    /**
     * @param soundFormat the soundFormat to set
     */
    public void setSoundFormat(int soundFormat)
    {
        this.soundFormat = soundFormat;
    }

    /**
     * @return the soundRate
     */
    public int getSoundRate()
    {
        return soundRate;
    }

    /**
     * @param soundRate the soundRate to set
     */
    public void setSoundRate(int soundRate)
    {
        this.soundRate = soundRate;
    }

    /**
     * @return the soundSize
     */
    public int getSoundSize()
    {
        return soundSize;
    }

    /**
     * @param soundSize the soundSize to set
     */
    public void setSoundSize(int soundSize)
    {
        this.soundSize = soundSize;
    }

    /**
     * @return the soundType
     */
    public int getSoundType()
    {
        return soundType;
    }

    /**
     * @param soundType the soundType to set
     */
    public void setSoundType(int soundType)
    {
        this.soundType = soundType;
    }

    /**
     * @return the soundSampleCount
     */
    public long getSoundSampleCount()
    {
        return soundSampleCount;
    }

    /**
     * @param soundSampleCount the soundSampleCount to set
     */
    public void setSoundSampleCount(long soundSampleCount)
    {
        this.soundSampleCount = soundSampleCount;
    }

    /**
     * @return the soundData
     */
    public byte[] getSoundData()
    {
        return soundData;
    }

    /**
     * @param soundData the soundData to set
     */
    public void setSoundData(byte[] soundData)
    {
        this.soundData = soundData;
    }
}
