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
import java.util.List;

import org.apache.royale.swf.TagType;

/**
 * Represents a <code>DefineSprite</code> tag in a SWF file.
 */
public class DefineSpriteTag extends CharacterTag implements ICharacterReferrer
{
    /**
     * Constructor.
     */
    public DefineSpriteTag(int frameCount, List<ITag> controlTags)
    {
        super(TagType.DefineSprite);
        
        this.frameCount = frameCount;
        this.controlTags = controlTags;
    }
    
    private final int frameCount;
    private final List<ITag> controlTags;

    public int getFrameCount()
    {
        return frameCount;
    }

    public List<ITag> getControlTags()
    {
        return controlTags;
    }

    @Override
    public String description()
    {
        final StringBuilder result = new StringBuilder();
        
        result.append(String.format("#%d, including %d frame(s)", getCharacterID(), frameCount));
        for (ITag tag : controlTags)
        {
            result.append("\n  >> ").append(tag);
        }
        
        return result.toString();
    }

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        final ArrayList<ICharacterTag> result = new ArrayList<ICharacterTag>();
        
        for (final ITag controlTag : controlTags)
        {
            if (controlTag instanceof ICharacterReferrer)
            {
                for (final ICharacterTag tag : ((ICharacterReferrer)controlTag).getReferences())
                {
                    assert tag != null;
                    result.add(tag);
                }
            }
        }
        
        return result;
    }
}
