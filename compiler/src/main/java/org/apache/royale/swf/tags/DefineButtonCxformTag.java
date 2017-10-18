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
import org.apache.royale.swf.types.CXForm;

/**
 * Represents a <code>DefineButtonCxform</code> tag in a SWF file.
 * <p>
 * DefineButtonCxform defines the color transform for each shape and text
 * character in a button. This is not used for DefineButton2, which includes its
 * own CXFORM.
 */
public class DefineButtonCxformTag extends Tag implements ICharacterReferrer
{
    /**
     * Constructor.
     */
    public DefineButtonCxformTag()
    {
        super(TagType.DefineButtonCxform);
    }

    private ICharacterTag buttonTag;
    private CXForm colorTransform;

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        assert buttonTag != null;
        return CharacterIterableFactory.from(buttonTag);
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
        assert buttonTag.getTagType() == TagType.DefineButton;
        this.buttonTag = buttonTag;
    }

    /**
     * @return the colorTransform
     */
    public CXForm getColorTransform()
    {
        return colorTransform;
    }

    /**
     * @param colorTransform the colorTransform to set
     */
    public void setColorTransform(CXForm colorTransform)
    {
        this.colorTransform = colorTransform;
    }
}
