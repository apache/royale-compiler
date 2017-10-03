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

import java.util.Arrays;

import org.apache.royale.swf.TagType;
import org.apache.royale.swf.types.Matrix;
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.TextRecord;

/**
 * Represents a <code>DefineText</code> tag in a SWF file.
 * <p>
 * The DefineText tag defines a block of static text. It describes the font,
 * size, color, and exact position of every character in the text object.
 */
public class DefineTextTag extends CharacterTag implements ICharacterReferrer
{
    /**
     * Constructor.
     */
    public DefineTextTag()
    {
        super(TagType.DefineText);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected DefineTextTag(TagType tagType)
    {
        super(tagType);
    }

    private Rect textBounds;
    private Matrix textMatrix;
    private int glyphBits;
    private int advanceBits;
    private TextRecord[] textRecords;
    private CSMTextSettingsTag CSMTextSettings;

    /**
     * @return the textBounds
     */
    public Rect getTextBounds()
    {
        return textBounds;
    }

    /**
     * @param textBounds the textBounds to set
     */
    public void setTextBounds(Rect textBounds)
    {
        this.textBounds = textBounds;
    }

    /**
     * @return the textMatrix
     */
    public Matrix getTextMatrix()
    {
        return textMatrix;
    }

    /**
     * @param textMatrix the textMatrix to set
     */
    public void setTextMatrix(Matrix textMatrix)
    {
        this.textMatrix = textMatrix;
    }

    /**
     * @return the glyphBits
     */
    public int getGlyphBits()
    {
        return glyphBits;
    }

    /**
     * @param glyphBits the glyphBits to set
     */
    public void setGlyphBits(int glyphBits)
    {
        this.glyphBits = glyphBits;
    }

    /**
     * @return the advanceBits
     */
    public int getAdvanceBits()
    {
        return advanceBits;
    }

    /**
     * @param advanceBits the advanceBits to set
     */
    public void setAdvanceBits(int advanceBits)
    {
        this.advanceBits = advanceBits;
    }

    /**
     * @return the textRecords
     */
    public TextRecord[] getTextRecords()
    {
        return textRecords;
    }

    /**
     * @param textRecords the textRecords to set
     */
    public void setTextRecords(TextRecord[] textRecords)
    {
        this.textRecords = textRecords;
    }

    /**
     * @return the csmTextSettings
     */
    public CSMTextSettingsTag getCSMTextSettings()
    {
        return CSMTextSettings;
    }

    /**
     * @param csmTextSettings the csmTextSettings to set
     */
    public void setCSMTextSettings(CSMTextSettingsTag csmTextSettings)
    {
        this.CSMTextSettings = csmTextSettings;
    }

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        assert textRecords != null;
        return CharacterIterableFactory.collect(Arrays.asList(textRecords));
    }

}
