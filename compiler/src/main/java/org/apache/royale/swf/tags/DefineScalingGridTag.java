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
import org.apache.royale.swf.types.Rect;

/**
 * Represents a <code>DefineScalingGrid</code> tag in a SWF file.
 * <p>
 * The {@code DefineScalingGrid} tag introduces the concept of 9-slice scaling,
 * which allows component-style scaling to be applied to a sprite or button
 * character.
 * <p>
 * When the {@code DefineScalingGrid} tag associates a character with a 9-slice
 * grid, Flash Player conceptually divides the sprite or button into nine
 * sections with a grid-like overlay. When the character is scaled, each of the
 * nine areas is scaled independently. To maintain the visual integrity of the
 * character, corners are not scaled, while the remaining areas of the image are
 * scaled larger or smaller, as needed.
 */
public class DefineScalingGridTag extends Tag implements ICharacterReferrer
{
    /**
     * Constructor.
     */
    public DefineScalingGridTag()
    {
        super(TagType.DefineScalingGrid);
    }

    /**
     * Constructor.
     * 
     * @param character associated character tag
     * @param splitter center region of 9-slice grid
     */
    public DefineScalingGridTag(ICharacterTag character, Rect splitter)
    {
        this();
        this.character = character;
        this.splitter = splitter;
    }

    private ICharacterTag character;
    private Rect splitter;

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        assert character != null;
        return CharacterIterableFactory.from(character);
    }

    public ICharacterTag getCharacter()
    {
        return character;
    }

    public void setCharacter(ICharacterTag character)
    {
        this.character = character;
    }

    public Rect getSplitter()
    {
        return splitter;
    }

    public void setSplitter(Rect splitter)
    {
        this.splitter = splitter;
    }

    @Override
    public String description()
    {
        return String.format("apply to %d(%s), %s",
                character.getCharacterID(),
                character.getTagType(),
                splitter);
    }

}
