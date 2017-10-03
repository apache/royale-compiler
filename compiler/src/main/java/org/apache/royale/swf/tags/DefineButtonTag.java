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
import org.apache.royale.swf.types.ButtonRecord;

/**
 * Represents a <code>DefineButton</code> tag in a SWF file.
 * <p>
 * The DefineButton tag defines a button character for later use by control tags
 * such as PlaceObject.
 * <p>
 * DefineButton includes an array of Button records that represent the four
 * button shapes: an up character, a mouse-over character, a down character, and
 * a hit-area character. It is not necessary to define all four states, but at
 * least one button record must be present. For example, if the same button
 * record defines both the up and over states, only three button records are
 * required to describe the button.
 * <p>
 * More than one button record per state is allowed. If two button records refer
 * to the same state, both are displayed for that state.
 * <p>
 * DefineButton also includes an array of ACTIONRECORDs, which are performed
 * when the button is clicked and released.
 */
public class DefineButtonTag extends CharacterTag
{
    /**
     * Constructor.
     */
    public DefineButtonTag()
    {
        super(TagType.DefineButton);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected DefineButtonTag(TagType type)
    {
        super(type);
    }

    private ButtonRecord[] characters;
    private byte[] actions;

    /**
     * @return the characters
     */
    public ButtonRecord[] getCharacters()
    {
        return characters;
    }

    /**
     * @param characters the characters to set
     */
    public void setCharacters(ButtonRecord[] characters)
    {
        this.characters = characters;
    }

    /**
     * @return the actions
     */
    public byte[] getActions()
    {
        return actions;
    }

    /**
     * @param actions the actions to set
     */
    public void setActions(byte[] actions)
    {
        this.actions = actions;
    }
}
