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
import org.apache.royale.swf.types.SoundInfo;

/**
 * Represents a <code>StartSound</code> tag in a SWF file.
 * <p>
 * StartSound is a control tag that either starts (or stops) playing a sound
 * defined by DefineSound. The SoundId field identifies which sound is to be
 * played. The SoundInfo field defines how the sound is played. Stop a sound by
 * setting the SyncStop flag in the SOUNDINFO record.
 */
public class StartSound2Tag extends Tag
{
    /**
     * Constructor.
     */
    public StartSound2Tag()
    {
        super(TagType.StartSound2);
    }

    private String soundClassName;
    private SoundInfo soundInfo;

    /**
     * @return the soundClassName
     */
    public String getSoundClassName()
    {
        return soundClassName;
    }

    /**
     * @param soundClassName the soundClassName to set
     */
    public void setSoundClassName(String soundClassName)
    {
        this.soundClassName = soundClassName;
    }

    /**
     * @return the soundInfo
     */
    public SoundInfo getSoundInfo()
    {
        return soundInfo;
    }

    /**
     * @param soundInfo the soundInfo to set
     */
    public void setSoundInfo(SoundInfo soundInfo)
    {
        this.soundInfo = soundInfo;
    }

    /**
     * TODO: find sound tag by class name and implement ICharacterReferrer
     * interface.
     */
}
