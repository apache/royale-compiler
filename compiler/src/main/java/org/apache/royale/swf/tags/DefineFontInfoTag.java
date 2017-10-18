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
 * Represents a <code>DefineFontInfo</code> tag in a SWF file.
 * <p>
 * The DefineFontInfo tag defines a mapping from a glyph font (defined with
 * DefineFont) to a device font. It provides a font name and style to pass to
 * the playback platform's text engine, and a table of character codes that
 * identifies the character represented by each glyph in the corresponding
 * DefineFont tag, allowing the glyph indices of a DefineText tag to be
 * converted to character strings.
 * <p>
 * The presence of a DefineFontInfo tag does not force a glyph font to become a
 * device font; it merely makes the option available. The actual choice between
 * glyph and device usage is made according to the value of device font (see the
 * introduction) or the value of UseOutlines in a DefineEditText tag. If a
 * device font is unavailable on a playback platform, Flash Player will fall
 * back to glyph text.
 */
public class DefineFontInfoTag extends Tag implements ICharacterReferrer, IFontInfo
{
    /**
     * Constructor.
     */
    public DefineFontInfoTag()
    {
        super(TagType.DefineFontInfo);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected DefineFontInfoTag(TagType tagType)
    {
        super(tagType);
    }

    private ICharacterTag fontTag;
    private String fontName;
    private int fontFlagsReserved;
    private boolean fontFlagsSmallText;
    private boolean fontFlagsShiftJIS;
    private boolean fontFlagsANSI;
    private boolean fontFlagsItalic;
    private boolean fontFlagsBold;
    private boolean fontFlagsWideCodes;
    private int[] codeTable;

    /**
     * Get the Font tag this information is for.
     */
    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        assert fontTag != null;
        return CharacterIterableFactory.from(fontTag);
    }

    @Override
    public ICharacterTag getFontTag()
    {
        return fontTag;
    }

    @Override
    public void setFontTag(ICharacterTag fontTag)
    {
        this.fontTag = fontTag;
    }

    @Override
    public String getFontName()
    {
        return fontName;
    }

    @Override
    public void setFontName(String fontName)
    {
        this.fontName = fontName;
    }

    @Override
    public int getFontFlagsReserved()
    {
        return fontFlagsReserved;
    }

    @Override
    public void setFontFlagsReserved(int fontFlagsReserved)
    {
        this.fontFlagsReserved = fontFlagsReserved;
    }

    @Override
    public boolean isFontFlagsSmallText()
    {
        return fontFlagsSmallText;
    }

    @Override
    public void setFontFlagsSmallText(boolean fontFlagsSmallText)
    {
        this.fontFlagsSmallText = fontFlagsSmallText;
    }

    @Override
    public boolean isFontFlagsShiftJIS()
    {
        return fontFlagsShiftJIS;
    }

    @Override
    public void setFontFlagsShiftJIS(boolean fontFlagsShiftJIS)
    {
        this.fontFlagsShiftJIS = fontFlagsShiftJIS;
    }

    @Override
    public boolean isFontFlagsANSI()
    {
        return fontFlagsANSI;
    }

    @Override
    public void setFontFlagsANSI(boolean fontFlagsANSI)
    {
        this.fontFlagsANSI = fontFlagsANSI;
    }

    @Override
    public boolean isFontFlagsItalic()
    {
        return fontFlagsItalic;
    }

    @Override
    public void setFontFlagsItalic(boolean fontFlagsItalic)
    {
        this.fontFlagsItalic = fontFlagsItalic;
    }

    @Override
    public boolean isFontFlagsBold()
    {
        return fontFlagsBold;
    }

    @Override
    public void setFontFlagsBold(boolean fontFlagsBold)
    {
        this.fontFlagsBold = fontFlagsBold;
    }

    @Override
    public boolean isFontFlagsWideCodes()
    {
        return fontFlagsWideCodes;
    }

    @Override
    public void setFontFlagsWideCodes(boolean fontFlagsWideCodes)
    {
        this.fontFlagsWideCodes = fontFlagsWideCodes;
    }

    @Override
    public int[] getCodeTable()
    {
        return codeTable;
    }

    @Override
    public void setCodeTable(int[] codeTable)
    {
        this.codeTable = codeTable;
    }
}
