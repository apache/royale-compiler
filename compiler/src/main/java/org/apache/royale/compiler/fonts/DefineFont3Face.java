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

package org.apache.royale.compiler.fonts;

import org.apache.royale.swf.tags.DefineFont3Tag;
import org.apache.royale.swf.types.GlyphEntry;

/**
 * A wrapper to make a DefineFont3 SWF Tag behave like a
 * CachedFontFace for use in the general Flex SDK FontManager
 * subsystem.
 */
public class DefineFont3Face extends CachedFontFace
{
    private final DefineFont3Tag tag;
    private final char[] indicies;
    private char firstChar;

    /**
     * Constructor.
     * @param tag The DefineFont. Must not be null and tag.codeTable must not
     * be null.
     */
    public DefineFont3Face(DefineFont3Tag tag)
    {
        super(tag.getCodeTable().length + 1);
        this.tag = tag;
        style = getStyle(tag);

        // Transpose the Array of chars into an Array of indicies into the 
        // other tables in the DefineFont tag...
        firstChar = (char) tag.getCodeTable()[0];
        int charCount = tag.getCodeTable().length;
        char lastChar = (char) tag.getCodeTable()[charCount - 1];
        indicies = new char[lastChar + 1];
        for (char i = 0; i < charCount; i++)
        {
            char c = (char) tag.getCodeTable()[i]; 
            indicies[c] = i;
        }

        if (tag.getLicense() != null)
        {
            copyright = tag.getLicense().getFontCopyright();
        }

        if (tag.getZones() != null && tag.getZones().getZoneTable() != null)
        {
            useTwips = true;
        }
    }

    //--------------------------------------------------------------------------
    // 
    // FontFace implementation
    //
    //--------------------------------------------------------------------------

    public boolean canDisplay(char c)
    {
        if (c < indicies.length)
        {
            int index = indicies[c];
            if (c == firstChar || index > 0)
                return true;
        }
        return false;
    }

    public int getAdvance(char c)
    {
        int index = indicies[c];
        return tag.getFontAdvanceTable()[index];
    }

    public int getAscent()
    {
        return tag.getFontAscent();
    }

    public int getDescent()
    {
        return tag.getFontDescent();
    }

    public double getEmScale()
    {
        return 1.0;
    }

    public String getFamily()
    {
        return getFamily(tag);
    }

    public int getFirstChar()
    {
        return firstChar;
    }
    
    public GlyphEntry getGlyphEntry(char c)
    {
        return (GlyphEntry)glyphCache.get(c);
    }

    public int getLineGap()
    {
        return tag.getFontLeading();
    }

    public int getMissingGlyphCode()
    {
        return 0;
    }

    public int getNumGlyphs()
    {
        return tag.getCodeTable().length;
    }

    public double getPointSize()
    {
        return 1.0f;
    }

    public String getPostscriptName()
    {
        return getFamily(tag);
    }

    public static String getFamily(DefineFont3Tag tag)
    {
        String family = tag.getFontName();
        if (tag.getLicense() != null)
        {
            String fontName = tag.getLicense().getFontName();
            if (fontName != null && !"".equals(fontName))
                family = tag.getLicense().getFontName();
        }
        return family;
    }
    
    public static int getStyle(DefineFont3Tag tag)
    {
        int style = 0;
        if (tag.isFontFlagsBold())
            style += FontFace.BOLD;
        if (tag.isFontFlagsItalic())
            style += FontFace.ITALIC;
        return style;
    }

    public static GlyphEntry createGlyphEntryFromDefineFont(char c, char index, DefineFont3Tag tag)
    {
        GlyphEntry ge = new GlyphEntry();
        ge.character = c;
        ge.setGlyphIndex(index);

        if (tag.getGlyphShapeTable() != null)
            ge.shape = tag.getGlyphShapeTable()[index];

        if (tag.getFontAdvanceTable() != null)
            ge.setGlyphAdvance(tag.getFontAdvanceTable()[index]);

        if (tag.getFontBoundsTable() != null)
            ge.bounds = tag.getFontBoundsTable()[index];

        if (tag.getZones() != null && tag.getZones().getZoneTable() != null)
            ge.zoneRecord = tag.getZones().getZoneTable()[index];

        return ge;
    }

    //--------------------------------------------------------------------------
    // 
    // CachedFontFace implementation
    //
    //--------------------------------------------------------------------------

    protected GlyphEntry createGlyphEntry(char c)
    {
        char index = indicies[c];
        return createGlyphEntryFromDefineFont(c, index, tag);
    }

    protected GlyphEntry createGlyphEntry(char c, char referenceChar)
    {
        // We don't use any glyph index offsets based on a reference char.
        return createGlyphEntry(c);
    }

}
