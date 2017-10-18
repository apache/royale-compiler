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

import java.util.ArrayList;

import org.apache.royale.swf.TagType;
import org.apache.royale.swf.types.SoundInfo;

/**
 * Represents a <code>DefineButtonSound</code> tag in a SWF file.
 * <p>
 * The DefineButtonSound tag defines which sounds (if any) are played on state
 * transitions.
 */
public class DefineButtonSoundTag extends Tag implements ICharacterReferrer
{
    public static final int TOTAL_SOUND_STYLE = 4;

    /**
     * Constructor.
     */
    public DefineButtonSoundTag()
    {
        super(TagType.DefineButtonSound);
        soundChar = new DefineSoundTag[TOTAL_SOUND_STYLE];
        soundInfo = new SoundInfo[4];
    }

    private ICharacterTag buttonTag;
    private DefineSoundTag[] soundChar;
    private SoundInfo[] soundInfo;

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        assert buttonTag != null;

        final ArrayList<ICharacterTag> result = new ArrayList<ICharacterTag>(TOTAL_SOUND_STYLE + 1);
        result.add(buttonTag);
        for (DefineSoundTag sound : soundChar)
            if (sound != null)
                result.add(sound);
        return result;
    }

    /**
     * @return the buttonTag
     */
    public ICharacterTag getButtonTag()
    {
        return buttonTag;
    }

    /**
     * @param buttonTag the buttonTag to set
     */
    public void setButtonTag(ICharacterTag buttonTag)
    {
        this.buttonTag = buttonTag;
    }

    /**
     * @return the soundChar
     */
    public DefineSoundTag[] getSoundChar()
    {
        return soundChar;
    }

    /**
     * @return the soundInfo
     */
    public SoundInfo[] getSoundInfo()
    {
        return soundInfo;
    }

    /**
     * @return the totalSoundStyle
     */
    public static int getTotalSoundStyle()
    {
        return TOTAL_SOUND_STYLE;
    }
}
