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
public class StartSoundTag extends Tag implements ICharacterReferrer
{
    /**
     * Constructor.
     */
    public StartSoundTag()
    {
        super(TagType.StartSound);
    }

    private ICharacterTag soundTag;
    private SoundInfo soundInfo;

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        assert soundTag != null;
        return CharacterIterableFactory.from(soundTag);
    }

    /**
     * @return the soundTag
     */
    public ICharacterTag getSoundTag()
    {
        return soundTag;
    }

    /**
     * @param soundTag the soundTag to set
     */
    public void setSoundTag(ICharacterTag soundTag)
    {
        this.soundTag = soundTag;
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
}
