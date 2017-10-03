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
 * Represents a <code>DefineFont4</code> tag in a SWF file.
 * <p>
 * DefineFont4 supports only the new Flash Text Engine. The storage of font data
 * for embedded fonts is in CFF format.
 */
public class DefineFont4Tag extends CharacterTag implements IDefineFontTag
{
    /**
     * Constructor.
     */
    public DefineFont4Tag()
    {
        super(TagType.DefineFont4);
    }

    private boolean fontFlagsHasFontData;
    private boolean fontFlagsItalic;
    private boolean fontFlagsBold;
    private String fontName;
    private byte[] fontData;
    private DefineFontNameTag license;

    /**
     * @return the fontFlagsHasFontData
     */
    public boolean isFontFlagsHasFontData()
    {
        return fontFlagsHasFontData;
    }

    /**
     * @param fontFlagsHasFontData the fontFlagsHasFontData to set
     */
    public void setFontFlagsHasFontData(boolean fontFlagsHasFontData)
    {
        this.fontFlagsHasFontData = fontFlagsHasFontData;
    }

    /**
     * @return the fontFlagsItalic
     */
    public boolean isFontFlagsItalic()
    {
        return fontFlagsItalic;
    }

    /**
     * @param fontFlagsItalic the fontFlagsItalic to set
     */
    public void setFontFlagsItalic(boolean fontFlagsItalic)
    {
        this.fontFlagsItalic = fontFlagsItalic;
    }

    /**
     * @return the fontFlagsBold
     */
    public boolean isFontFlagsBold()
    {
        return fontFlagsBold;
    }

    /**
     * @param value the fontFlagsBold to set
     */
    public void setFontFlagsBold(boolean value)
    {
        this.fontFlagsBold = value;
    }

    /**
     * @return the fontName
     */
    public String getFontName()
    {
        return fontName;
    }

    /**
     * @param value the fontName to set
     */
    public void setFontName(String value)
    {
        this.fontName = value;
    }

    /**
     * @return the fontData
     */
    public byte[] getFontData()
    {
        return fontData;
    }

    /**
     * @param value the fontData to set
     */
    public void setFontData(byte[] value)
    {
        this.fontData = value;
    }

    /**
     * @return the DefineFontNameTag
     */
    @Override
    public DefineFontNameTag getLicense()
    {
        return license;
    }

    /**
     * @param license the license to set
     */
    @Override
    public void setLicense(DefineFontNameTag license)
    {
        this.license = license;
    }
}
