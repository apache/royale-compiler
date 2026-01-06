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

package org.apache.royale.swf.builders;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.apache.royale.compiler.fonts.FSType;
import org.apache.royale.compiler.fonts.FontFace;
import org.apache.royale.compiler.fonts.FontManager;
import org.apache.royale.swf.TagType;
import org.apache.royale.swf.tags.DefineFont2Tag;
import org.apache.royale.swf.tags.DefineFont3Tag;
import org.apache.royale.swf.tags.DefineFontAlignZonesTag;
import org.apache.royale.swf.tags.DefineFontNameTag;
import org.apache.royale.swf.tags.DefineFontTag;
import org.apache.royale.swf.types.GlyphEntry;
import org.apache.royale.swf.types.KerningRecord;
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.Shape;
import org.apache.royale.swf.types.ZoneRecord;
import org.apache.royale.utils.IntMap;
import org.apache.royale.utils.Trace;

/**
 * A utility class to build a DefineFont2 or DefineFont3 tag.  One
 * must supply a font family name and style to establish a default
 * font face and a <code>FontManager</code> to locate and cache fonts
 * and glyphs.
 */
@SuppressWarnings("unchecked")
public final class FontBuilder implements ITagBuilder
{
    public DefineFont2Tag tag;

    private boolean flashType;
    private IntMap glyphEntryMap; // Code-point ordered collection of glyphs
    private FontFace defaultFace;
    private double fontHeight;
    private ZoneRecordBuilder zoneRecordBuilder;

    private static final Rect IDENTITY_RECT = new Rect(0, 0, 0, 0);
    private static boolean useLicenseTag = true;

    private FontBuilder(int code, boolean hasLayout, boolean useFlashType)
    {
        TagType tagType = TagType.getTagType(code);
        switch (tagType)
        {
            case DefineFont2:
                tag = new DefineFont2Tag();
                break;
            case DefineFont3:
                tag = new DefineFont3Tag();
                break;
            default:
                throw new SWFFontNotSupportedException("Cannot build DefineFont for SWF tag code " + code);
        }

        tag.setFontFlagsHasLayout(hasLayout);
        glyphEntryMap = new IntMap(100); //Sorted by code point order
	    flashType = useFlashType;
    }

    /**
     * Build a DefineFont2 or DefineFont3 tag for a given FontFace.
     * 
     * Note that with this constructor, hasLayout is assumed to be true
     * but flashType is assumed to be false.
     * 
     * @param code Determines the version of DefineFont SWF tag to use.
     * @param fontFace The FontFace to build into a DefineFont tag.
     * @param alias The name used to bind a DefineFont tag to other SWF tags
     * (such as DefineEditText).
     * @deprecated
     */
    public FontBuilder(int code, FontFace fontFace, String alias)
    {
       this(code, fontFace, alias, false); 
    }

    /**
     * Build a DefineFont2 or DefineFont3 tag for a given FontFace.
     * 
     * Note that with this constructor, hasLayout is assumed to be true.
     * 
     * @param code Determines the version of DefineFont SWF tag to use.
     * @param fontFace The FontFace to build into a DefineFont tag.
     * @param alias The name used to bind a DefineFont tag to other SWF tags
     * (such as DefineEditText).
     */
    public FontBuilder(int code, FontFace fontFace, String alias, boolean flashType)
    {
        this(code, true, flashType); 
        defaultFace = fontFace;

        if (Trace.font)
            Trace.trace("Initializing font '" + fontFace.getFamily() + "' as '" + alias + "'");

        init(alias);
    }
    
    /**
     * Build a DefineFont2 or DefineFont3 tag from a system font by family name.
     *
     * @param code Determines the version of DefineFont SWF tag to use.
     * @param manager A FontManager resolves the fontFamily and style to 
     * a FontFace.
     * @param alias The name used to bind a DefineFont tag to other SWF tags
     * (such as DefineEditText).
     * @param fontFamily The name of the font family.
     * @param style An integer describing the style variant of the FontFace, 
     * either plain, bold, italic, or bolditalic.
     * @param hasLayout Determines whether font layout metrics should be encoded.
     * @param flashType Determines whether FlashType advanced anti-aliasing
     * information should be included.
     */
    public FontBuilder(int code, FontManager manager, String alias,
            String fontFamily, int style, boolean hasLayout, boolean flashType)
    {
        this(code, hasLayout, flashType);

        if (manager == null)
            throw new NoFontManagerException();

        if (Trace.font)
            Trace.trace("Locating font using FontManager '" + manager.getClass().getName() + "'");

        TagType tagType = TagType.getTagType(code);
	    boolean useTwips = !TagType.DefineFont.equals(tagType) && !TagType.DefineFont2.equals(tagType);
        FontFace fontFace = manager.getEntryFromSystem(fontFamily, style, useTwips);

        if (fontFace == null)
            FontManager.throwFontNotFound(alias, fontFamily, style, null);

        if (Trace.font)
            Trace.trace("Initializing font '" + fontFamily + "' as '" + alias + "'");

        defaultFace = fontFace;

        init(alias);
    }

    /**
     * Load a font from a URL
     *
     * @param code
     * @param alias The name used to bind a DefineFont tag to a DefineEditText tag.
     * @param location remote url or a relative, local file path
     * @param style
     * @param hasLayout
     */
    public FontBuilder(int code, FontManager manager, String alias,
            URL location, int style, boolean hasLayout, boolean flashType)
    {
        this(code, hasLayout, flashType);

        if (manager == null)
            throw new NoFontManagerException();

        if (Trace.font)
            Trace.trace("Locating font using FontManager '" + manager.getClass().getName() + "'");

        TagType tagType = TagType.getTagType(code);
	    boolean useTwips = !TagType.DefineFont.equals(tagType) && !TagType.DefineFont2.equals(tagType);
        FontFace fontFace = manager.getEntryFromLocation(location, style, useTwips);

        if (fontFace == null)
            FontManager.throwFontNotFound(alias, null, style, location.toString());

        if (Trace.font)
            Trace.trace("Initializing font at '" + location.toString() + "' as '" + alias + "'");

        this.defaultFace = fontFace;

        init(alias);
    }

    private void init(String alias)
    {
        fontHeight = defaultFace.getPointSize();

        if (!TagType.DefineFont.equals(tag.getTagType()))
        {
            tag.setFontName(alias);
            tag.setFontFlagsBold(defaultFace.isBold());
            tag.setFontFlagsItalic(defaultFace.isItalic());

            if (tag.isFontFlagsHasLayout())
            {
                tag.setFontAscent(defaultFace.getAscent());
                tag.setFontDescent(defaultFace.getDescent());
                tag.setFontLeading(defaultFace.getLineGap());

                if (Trace.font)
                {
                    Trace.trace("\tBold: " + tag.isFontFlagsBold());
                    Trace.trace("\tItalic: " + tag.isFontFlagsItalic());
                    Trace.trace("\tAscent: " + tag.getFontAscent());
                    Trace.trace("\tDescent: " + tag.getFontDescent());
                    Trace.trace("\tLeading: " + tag.getFontLeading());
                }
            }
        }

        // If flashType enabled we must have z, Z, l, L
        if (flashType)
        {
            GlyphEntry adfGE = defaultFace.getGlyphEntry('z');
            if (adfGE == null)
                flashType = false;

            adfGE = defaultFace.getGlyphEntry('Z');
            if (adfGE == null)
                flashType = false;

            adfGE = defaultFace.getGlyphEntry('l');
            if (adfGE == null)
                flashType = false;

            adfGE = defaultFace.getGlyphEntry('L');
            if (adfGE == null)
                flashType = false;
        }

        if (flashType)
        {
            zoneRecordBuilder = ZoneRecordBuilder.createInstance();
            if (zoneRecordBuilder != null)
            {
                zoneRecordBuilder.setFontAlias(alias);
                zoneRecordBuilder.setFontBuilder(this);
                zoneRecordBuilder.setFontFace(defaultFace);
            }
            else
            {
                // FlashType Zone Records are not available, so we should
                // disable flashType
                flashType = false;
            }
        }

        addChar(' '); // Add at least a space char by default
    }

    /**
     * Creates a DefineFont2 or DefineFont3 tag depending on the code specified
     * on construction.
     */
    public DefineFontTag build()
    {
        int count = glyphEntryMap.size();

        if (Trace.font)
            Trace.trace("Building font '" + tag.getFontName() + "' with " + count + " characters.");

	    if (flashType && tag instanceof DefineFont3Tag)
	    {
	        DefineFont3Tag df3 = (DefineFont3Tag)tag;
            DefineFontAlignZonesTag zones = new DefineFontAlignZonesTag();
	        zones.setFontTag(df3);
	        zones.setZoneTable(new ZoneRecord[count]);
		    zones.setCsmTableHint(1);
	        df3.setZones(zones);
	    }

        tag.setGlyphShapeTable(new Shape[count]);

        if (!TagType.DefineFont.equals(tag.getTagType()))
        {
            tag.setCodeTable(new int[count]);
            tag.setNumGlyphs(count);

            if (tag.isFontFlagsHasLayout())
            {
                tag.setFontAdvanceTable(new int[count]);
                tag.setFontBoundsTable(new Rect[count]);
            }
        }

        // Process each GlyphEntry
        Iterator it = glyphEntryMap.iterator();
        int i = 0;

	    // long flashTypeTime = 0;
        while (it.hasNext() && i < count)
        {
            GlyphEntry ge = (GlyphEntry)((Map.Entry)it.next()).getValue();

            if (flashType && tag instanceof DefineFont3Tag)
            {
                ((DefineFont3Tag)tag).getZones().getZoneTable()[i] = ge.zoneRecord;
            }

            // Note: offsets to shape table entries calculated on encoding
            tag.getGlyphShapeTable()[i] = ge.shape;

            // IMPORTANT! Update GlyphEntry Index
            ge.setGlyphIndex(i);

            // DEFINEFONT2/3 specific properties
            if (!TagType.DefineFont.equals(tag.getTagType()))
            {
                tag.getCodeTable()[i] = ge.character; // unsigned code point

                // Layout information
                if (tag.isFontFlagsHasLayout())
                {
                    tag.getFontAdvanceTable()[i] = (short)ge.getGlyphAdvance(); //advance in emScale
	                // The player doesn't need ge.bounds, so we ignore it. 
                    // We must still generate this value, however, for ADF use.
                    tag.getFontBoundsTable()[i] = IDENTITY_RECT;
                }
                else
                {
                    if (Trace.font)
                        Trace.trace("Warning: font tag created without layout information.");
                }
            }

            i++;
        }

        if (tag.isFontFlagsHasLayout())
        {
            tag.setFontKerningTable(new KerningRecord[0]);
        }

        if (TagType.DefineFont3.equals(tag.getTagType()))
        {
            tag.setFontFlagsWideCodes(true);
        }
        else if (!tag.isFontFlagsWideCodes())
        {
            int[] codeTable = tag.getCodeTable();
            for (int j = 0; j < codeTable.length; j++)
            {
                if (codeTable[i] > 255)
                {
                    tag.setFontFlagsWideCodes(true);
                    break;
                }
            }
        }

	    // FIXME: we should allow the user to set the language code
	    //tag.langCode = 1;

	    // if we have any license info, create a DefineFontName tag
	    if (useLicenseTag && ((getFSType() != null && ! getFSType().installable) || getCopyright() != null || getName() != null))
	    {
            DefineFontNameTag license = new DefineFontNameTag();
		    license.setFontTag(tag);
		    license.setFontName(getName());
		    license.setFontCopyright(getCopyright());
		    tag.setLicense(license);
	    }

       return tag;
    }

    /**
     * Adds all supported characters from 0 to the highest glyph
     * contained in the default font face.
     */
    public void addAllChars()
    {
        addAllChars(defaultFace);
    }

    /**
     * Adds all supported characters from 0 to the highest glyph
     * contained in the given font face.
     *
     * @param face
     */
    public void addAllChars(FontFace face)
    {
        int min = face.getFirstChar();
        int count = face.getNumGlyphs();

        if (Trace.font)
            Trace.trace("\tAdding " + count + " chars, starting from " + min);

        addCharset(min, count);
    }

    /**
     * Adds supported characters in the specified range from the default
     * font face.
     *
     * @param fromChar
     * @param count
     */
    public void addCharset(int fromChar, int count)
    {
        addCharset(defaultFace, fromChar, count);
    }

    /**
     * Adds supported characters in the specified range from the given
     * font face.
     *
     * @param face
     * @param fromChar
     * @param count
     */
    public void addCharset(FontFace face, int fromChar, int count)
    {
        int remaining = count;

        for (int i = fromChar; remaining > 0 && i < Character.MAX_VALUE; i++)
        {
            char c = (char)i;
            GlyphEntry ge = addChar(face, c);
            if (ge != null)
            {
	            remaining--;
            }
        }
    }

	/**
	 * Adds all supported characters in the given array from the
	 * default font face.
	 *
	 * @param chars
	 */
	public void addCharset(char[] chars)
	{
	    addCharset(defaultFace, chars);
	}

    /**
     * Adds all supported characters in array from the given font face.
     *
     * @param face
     * @param chars
     */
    public void addCharset(FontFace face, char[] chars)
    {
        //TODO: Sort before adding to optimize IntMap addition
        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            addChar(face, c);
        }
    }

    /**
     * If supported, includes a given character from the default font face.
     *
     * @param c
     */
    public void addChar(char c)
    {
        addChar(defaultFace, c);
    }

    /**
     * If supported, includes a character from the given font face.
     *
     * @param c
     */
    public GlyphEntry addChar(FontFace face, char c)
    {
        GlyphEntry ge = (GlyphEntry)glyphEntryMap.get(c);

        if (ge == null)
        {
            ge = face.getGlyphEntry(c);

            if (ge != null)
            {
                //Add to this tag's collection
                glyphEntryMap.put(c, ge);
            }
        }

        if (flashType && ge != null && ge.zoneRecord == null && zoneRecordBuilder != null)
        {
            ge.zoneRecord = zoneRecordBuilder.build(c);
        }

        return ge;
    }

	public String getCopyright()
	{
		return defaultFace.getCopyright();
	}

	public String getName()
	{
		return defaultFace.getFamily();
	}

	public FSType getFSType()
	{
		return defaultFace.getFSType();
	}

    public void setLangcode(int code)
    {
        if (code >= 0 && code < 6)
            tag.setLanguageCode(code);
    }

    public GlyphEntry getGlyph(char c)
    {
        return (GlyphEntry)glyphEntryMap.get(c);
    }

    public double getFontHeight()
    {
        return fontHeight;
    }

    public int size()
    {
        return glyphEntryMap.size();
    }

    public static final class NoFontManagerException extends RuntimeException
    {
        private static final long serialVersionUID = 755054716704678420L;

        public NoFontManagerException()
        {
            super("No FontManager provided. Cannot build font.");
        }
    }

    public static final class SWFFontNotSupportedException extends RuntimeException
    {
        private static final long serialVersionUID = -7381079883711386211L;

        public SWFFontNotSupportedException(String message)
        {
            super(message);
        }
    }
}
