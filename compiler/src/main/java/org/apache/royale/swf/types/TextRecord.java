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

package org.apache.royale.swf.types;

import java.util.Collections;

import org.apache.royale.swf.tags.ICharacterReferrer;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.CharacterIterableFactory;

/**
 * A TEXTRECORD sets text styles for subsequent characters. It can be used to
 * select a font, change the text color, change the point size, insert a line
 * break, or set the x and y position of the next character in the text. The new
 * text styles apply until another TEXTRECORD changes the styles.
 * <p>
 * The TEXTRECORD also defines the actual characters in a text object.
 * Characters are referred to by an index into the current font's glyph table,
 * not by a character code. Each TEXTRECORD contains a group of characters that
 * all share the same text style, and are on the same line of text.
 */
public class TextRecord implements IDataType, ICharacterReferrer
{
    private boolean styleFlagsHasFont;
    private boolean styleFlagsHasColor;
    private boolean styleFlagsHasYOffset;
    private boolean styleFlagsHasXOffset;
    private ICharacterTag fontTag;
    private RGB textColor;
    private int xOffset;
    private int yOffset;
    private int textHeight;
    private int glyphCount;
    private GlyphEntry[] glyphEntries;

    /**
     * @return the styleFlagsHasFont
     */
    public boolean isStyleFlagsHasFont()
    {
        return styleFlagsHasFont;
    }

    /**
     * @param styleFlagsHasFont the styleFlagsHasFont to set
     */
    public void setStyleFlagsHasFont(boolean styleFlagsHasFont)
    {
        this.styleFlagsHasFont = styleFlagsHasFont;
    }

    /**
     * @return the styleFlagsHasColor
     */
    public boolean isStyleFlagsHasColor()
    {
        return styleFlagsHasColor;
    }

    /**
     * @param styleFlagsHasColor the styleFlagsHasColor to set
     */
    public void setStyleFlagsHasColor(boolean styleFlagsHasColor)
    {
        this.styleFlagsHasColor = styleFlagsHasColor;
    }

    /**
     * @return the styleFlagsHasYOffset
     */
    public boolean isStyleFlagsHasYOffset()
    {
        return styleFlagsHasYOffset;
    }

    /**
     * @param styleFlagsHasYOffset the styleFlagsHasYOffset to set
     */
    public void setStyleFlagsHasYOffset(boolean styleFlagsHasYOffset)
    {
        this.styleFlagsHasYOffset = styleFlagsHasYOffset;
    }

    /**
     * @return the styleFlagsHasXOffset
     */
    public boolean isStyleFlagsHasXOffset()
    {
        return styleFlagsHasXOffset;
    }

    /**
     * @param styleFlagsHasXOffset the styleFlagsHasXOffset to set
     */
    public void setStyleFlagsHasXOffset(boolean styleFlagsHasXOffset)
    {
        this.styleFlagsHasXOffset = styleFlagsHasXOffset;
    }

    /**
     * @return the fontTag
     */
    public ICharacterTag getFontTag()
    {
        return fontTag;
    }

    /**
     * @param fontTag the fontTag to set
     */
    public void setFontTag(ICharacterTag fontTag)
    {
        this.fontTag = fontTag;
    }

    /**
     * @return the textColor
     */
    public RGB getTextColor()
    {
        return textColor;
    }

    /**
     * @param textColor the textColor to set
     */
    public void setTextColor(RGB textColor)
    {
        this.textColor = textColor;
    }

    /**
     * @return the xOffset
     */
    public int getxOffset()
    {
        return xOffset;
    }

    /**
     * @param xOffset the xOffset to set
     */
    public void setxOffset(int xOffset)
    {
        this.xOffset = xOffset;
    }

    /**
     * @return the yOffset
     */
    public int getyOffset()
    {
        return yOffset;
    }

    /**
     * @param yOffset the yOffset to set
     */
    public void setyOffset(int yOffset)
    {
        this.yOffset = yOffset;
    }

    /**
     * @return the textHeight
     */
    public int getTextHeight()
    {
        return textHeight;
    }

    /**
     * @param textHeight the textHeight to set
     */
    public void setTextHeight(int textHeight)
    {
        this.textHeight = textHeight;
    }

    /**
     * @return the glyphCount
     */
    public int getGlyphCount()
    {
        return glyphCount;
    }

    /**
     * @param glyphCount the glyphCount to set
     */
    public void setGlyphCount(int glyphCount)
    {
        this.glyphCount = glyphCount;
    }

    /**
     * @return the glyphEntries
     */
    public GlyphEntry[] getGlyphEntries()
    {
        return glyphEntries;
    }

    /**
     * @param glyphEntries the glyphEntries to set
     */
    public void setGlyphEntries(GlyphEntry[] glyphEntries)
    {
        this.glyphEntries = glyphEntries;
    }

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        if (styleFlagsHasFont)
            return CharacterIterableFactory.from(fontTag);
        else
            return Collections.emptyList();
    }
}
