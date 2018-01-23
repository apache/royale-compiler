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
 * Represents a <code>CSMTextSettings</code> tag in a SWF file.
 * <p>
 * The CSMTextSettings tag modifies a previously streamed DefineText,
 * DefineText2, or DefineEditText tag. The CSMTextSettings tag turns advanced
 * anti-aliasing on or off for a text field, and can also be used to define
 * quality and options.
 */
public class CSMTextSettingsTag extends Tag implements ICharacterReferrer
{
    public static final int UFT_NORMAL_RENDERER = 0;
    public static final int UFT_ADVANCED_TEXT = 1;
    public static final int GF_NONE = 0;
    public static final int GF_PIXEL = 1;
    public static final int GF_SUB_PIXEL = 2;

    /**
     * Constructor.
     */
    public CSMTextSettingsTag()
    {
        super(TagType.CSMTextSettings);
    }

    private ICharacterTag textTag;
    private int useFlashType;
    private int gridFit;
    private float thickness;
    private float sharpness;

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        assert textTag != null;
        return CharacterIterableFactory.from(textTag);
    }

    /**
     * @return the textTag
     */
    public ICharacterTag getTextTag()
    {
        return textTag;
    }

    /**
     * @param value the textTag to set
     */
    public void setTextTag(ICharacterTag value)
    {
        this.textTag = value;
    }

    /**
     * @return the useFlashType
     */
    public int getUseFlashType()
    {
        return useFlashType;
    }

    /**
     * @param value the useFlashType to set
     */
    public void setUseFlashType(int value)
    {
        this.useFlashType = value;
    }

    /**
     * @return the gridFit
     */
    public int getGridFit()
    {
        return gridFit;
    }

    /**
     * @param value the gridFit to set
     */
    public void setGridFit(int value)
    {
        this.gridFit = value;
    }

    /**
     * @return the thickness
     */
    public float getThickness()
    {
        return thickness;
    }

    /**
     * @param value the thickness to set
     */
    public void setThickness(float value)
    {
        this.thickness = value;
    }

    /**
     * @return the sharpness
     */
    public float getSharpness()
    {
        return sharpness;
    }

    /**
     * @param value the sharpness to set
     */
    public void setSharpness(float value)
    {
        this.sharpness = value;
    }
}
