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
import org.apache.royale.swf.types.KerningRecord;
import org.apache.royale.swf.types.Rect;

/**
 * Represents a <code>DefineFont2</code> tag in a SWF file.
 * <p>
 * The DefineFont2 tag extends the functionality of DefineFont. Enhancements
 * include the following:
 * <ol>
 * <li>32-bit entries in the OffsetTable, for fonts with more than 64K glyphs.</li>
 * <li>Mapping to device fonts, by incorporating all the functionality of
 * DefineFontInfo.</li>
 * <li>Font metrics for improved layout of dynamic glyph text.</li>
 * </ol>
 * DefineFont2 tags are the only font definitions that can be used for dynamic
 * text.
 */
public class DefineFont2Tag extends DefineFontTag implements IFontInfo
{
    /**
     * Constructor.
     */
    public DefineFont2Tag()
    {
        this(TagType.DefineFont2);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected DefineFont2Tag(TagType tagType)
    {
        super(tagType);
        fontInfo = new DefineFontInfo2Tag();
    }

    private ICharacterTag fontTag;
    private IFontInfo fontInfo;
    private boolean fontFlagsHasLayout;
    private boolean fontFlagsWideOffsets;
    private int languageCode;
    private int numGlyphs;
    private long codeTableOffset;
    private int[] codeTable;
    private int fontAscent;
    private int fontDescent;
    private int fontLeading;
    private int[] fontAdvanceTable;
    private Rect[] fontBoundsTable;
    private int kerningCount;
    private KerningRecord[] fontKerningTable;

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
        return fontInfo.getFontName();
    }

    @Override
    public void setFontName(String fontName)
    {
        fontInfo.setFontName(fontName);
    }

    @Override
    public int getFontFlagsReserved()
    {
        return 0;
    }

    @Override
    public void setFontFlagsReserved(int fontFlagsReserved)
    {
    }

    @Override
    public boolean isFontFlagsSmallText()
    {
        return fontInfo.isFontFlagsSmallText();
    }

    @Override
    public void setFontFlagsSmallText(boolean fontFlagsSmallText)
    {
        fontInfo.setFontFlagsSmallText(fontFlagsSmallText);
    }

    @Override
    public boolean isFontFlagsShiftJIS()
    {
        return fontInfo.isFontFlagsShiftJIS();
    }

    @Override
    public void setFontFlagsShiftJIS(boolean fontFlagsShiftJIS)
    {
        fontInfo.setFontFlagsShiftJIS(fontFlagsShiftJIS);
    }

    @Override
    public boolean isFontFlagsANSI()
    {
        return fontInfo.isFontFlagsANSI();
    }

    @Override
    public void setFontFlagsANSI(boolean fontFlagsANSI)
    {
        fontInfo.setFontFlagsANSI(fontFlagsANSI);
    }

    @Override
    public boolean isFontFlagsItalic()
    {
        return fontInfo.isFontFlagsItalic();
    }

    @Override
    public void setFontFlagsItalic(boolean fontFlagsItalic)
    {
        fontInfo.setFontFlagsItalic(fontFlagsItalic);
    }

    @Override
    public boolean isFontFlagsBold()
    {
        return fontInfo.isFontFlagsBold();
    }

    @Override
    public void setFontFlagsBold(boolean fontFlagsBold)
    {
        fontInfo.setFontFlagsBold(fontFlagsBold);
    }

    @Override
    public boolean isFontFlagsWideCodes()
    {
        return fontInfo.isFontFlagsWideCodes();
    }

    @Override
    public void setFontFlagsWideCodes(boolean fontFlagsWideCodes)
    {
        fontInfo.setFontFlagsWideCodes(fontFlagsWideCodes);
    }

    /**
     * @return the fontFlagsHasLayout
     */
    public boolean isFontFlagsHasLayout()
    {
        return fontFlagsHasLayout;
    }

    /**
     * @param fontFlagsHasLayout the fontFlagsHasLayout to set
     */
    public void setFontFlagsHasLayout(boolean fontFlagsHasLayout)
    {
        this.fontFlagsHasLayout = fontFlagsHasLayout;
    }

    /**
     * @return the fontFlagsWideOffsets
     */
    public boolean isFontFlagsWideOffsets()
    {
        return fontFlagsWideOffsets;
    }

    /**
     * @param fontFlagsWideOffsets the fontFlagsWideOffsets to set
     */
    public void setFontFlagsWideOffsets(boolean fontFlagsWideOffsets)
    {
        this.fontFlagsWideOffsets = fontFlagsWideOffsets;
    }

    /**
     * @return the languageCode
     */
    public int getLanguageCode()
    {
        return languageCode;
    }

    /**
     * @param languageCode the languageCode to set
     */
    public void setLanguageCode(int languageCode)
    {
        this.languageCode = languageCode;
    }

    /**
     * @return the numGlyphs
     */
    public int getNumGlyphs()
    {
        return numGlyphs;
    }

    /**
     * @param numGlyphs the numGlyphs to set
     */
    public void setNumGlyphs(int numGlyphs)
    {
        this.numGlyphs = numGlyphs;
    }

    /**
     * @return the fontInfo
     */
    public IFontInfo getFontInfo()
    {
        return fontInfo;
    }

    /**
     * @param fontInfo the fontInfo to set
     */
    public void setFontInfo(IFontInfo fontInfo)
    {
        this.fontInfo = fontInfo;
    }

    /**
     * @return the codeTableOffset
     */
    public long getCodeTableOffset()
    {
        return codeTableOffset;
    }

    /**
     * @param value the codeTableOffset to set
     */
    public void setCodeTableOffset(long value)
    {
        this.codeTableOffset = value;
    }

    /**
     * @return the fontAscent
     */
    public int getFontAscent()
    {
        return fontAscent;
    }

    /**
     * @param value the fontAscent to set
     */
    public void setFontAscent(int value)
    {
        this.fontAscent = value;
    }

    /**
     * @return the fontDescent
     */
    public int getFontDescent()
    {
        return fontDescent;
    }

    /**
     * @param fontDescent the fontDescent to set
     */
    public void setFontDescent(int fontDescent)
    {
        this.fontDescent = fontDescent;
    }

    /**
     * @return the fontLeading
     */
    public int getFontLeading()
    {
        return fontLeading;
    }

    /**
     * @param fontLeading the fontLeading to set
     */
    public void setFontLeading(int fontLeading)
    {
        this.fontLeading = fontLeading;
    }

    /**
     * @return the kerningCount
     */
    public int getKerningCount()
    {
        return kerningCount;
    }

    /**
     * @param kerningCount the kerningCount to set
     */
    public void setKerningCount(int kerningCount)
    {
        this.kerningCount = kerningCount;
    }

    /**
     * @return the codeTable
     */
    @Override
    public int[] getCodeTable()
    {
        return codeTable;
    }

    /**
     * @param value the codeTable to set
     */
    @Override
    public void setCodeTable(int[] value)
    {
        this.codeTable = value;
    }

    /**
     * @return the fontAdvanceTable
     */
    public int[] getFontAdvanceTable()
    {
        return fontAdvanceTable;
    }

    /**
     * @param value the fontAdvanceTable to set
     */
    public void setFontAdvanceTable(int[] value)
    {
        this.fontAdvanceTable = value;
    }

    /**
     * @return the fontBoundsTable
     */
    public Rect[] getFontBoundsTable()
    {
        return fontBoundsTable;
    }

    /**
     * @param value the fontBoundsTable to set
     */
    public void setFontBoundsTable(Rect[] value)
    {
        this.fontBoundsTable = value;
    }

    /**
     * @return the fontKerningTable
     */
    public KerningRecord[] getFontKerningTable()
    {
        return fontKerningTable;
    }

    /**
     * @param value the fontKerningTable to set
     */
    public void setFontKerningTable(KerningRecord[] value)
    {
        this.fontKerningTable = value;
    }
}
