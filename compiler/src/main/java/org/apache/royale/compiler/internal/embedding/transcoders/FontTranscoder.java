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

package org.apache.royale.compiler.internal.embedding.transcoders;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.filespecs.IBinaryFileSpecification;
import org.apache.royale.compiler.fonts.FontDescription;
import org.apache.royale.compiler.fonts.FontFace;
import org.apache.royale.compiler.fonts.FontManager;
import org.apache.royale.compiler.fonts.JREFontManager;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.CffFontEmbeddingNotSupportedProblem;
import org.apache.royale.compiler.problems.EmbedCouldNotDetermineFontAliasProblem;
import org.apache.royale.compiler.problems.EmbedCouldNotDetermineFontLocationProblem;
import org.apache.royale.compiler.problems.EmbedInvalidUnicodeRangeProblem;
import org.apache.royale.compiler.problems.FileNotFoundProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.NoFontManagerProblem;
import org.apache.royale.compiler.problems.SystemFontEmbeddingNotSupportedProblem;
import org.apache.royale.compiler.problems.UnableToBuildFontProblem;
import org.apache.royale.swf.TagType;
import org.apache.royale.swf.tags.DefineFontTag;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.utils.Trace;

/**
 * Handle the embedding of fonts
 */
public class FontTranscoder extends TranscoderBase
{
    private boolean advAntiAliasing;
    private boolean embedAsCff;
    private boolean flashType;
    private String fontFamily;
    private String fontName;
    private String fontStyle;
    private String fontWeight;
    private String systemFont;
    private String unicodeRange;

    private String alias;
    private List<Serializable> locations;

    /**
     * Constructor.
     * 
     * @param data The embedding data.
     * @param workspace The workspace.
     */
    public FontTranscoder(EmbedData data, Workspace workspace)
    {
        super(data, workspace);
        advAntiAliasing = true;
        embedAsCff = false;
        flashType = true;
    }

    @Override
    public boolean analyze(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        boolean result = super.analyze(location, problems);
        baseClassQName = "flash.text.Font";
        return result;
    }

    @Override
    protected boolean setAttribute(EmbedAttribute attribute)
    {
        boolean isSupported = true;
        switch (attribute)
        {
            case ADV_ANTI_ALIASING:
                advAntiAliasing = (Boolean)data.getAttribute(EmbedAttribute.ADV_ANTI_ALIASING);
                break;
            case EMBED_AS_CFF:
                embedAsCff = (Boolean)data.getAttribute(EmbedAttribute.EMBED_AS_CFF);
                break;
            case FLASH_TYPE:
                flashType = (Boolean)data.getAttribute(EmbedAttribute.FLASH_TYPE);
                break;
            case FONT_NAME:
                fontName = (String)data.getAttribute(EmbedAttribute.FONT_NAME);
                break;
            case FONT_FAMILY:
                fontFamily = (String)data.getAttribute(EmbedAttribute.FONT_FAMILY);
                break;
            case FONT_STYLE:
                fontStyle = (String)data.getAttribute(EmbedAttribute.FONT_STYLE);
                break;
            case FONT_WEIGHT:
                fontWeight = (String)data.getAttribute(EmbedAttribute.FONT_WEIGHT);
                break;
            case SYSTEM_FONT:
                systemFont = (String)data.getAttribute(EmbedAttribute.SYSTEM_FONT);
                break;
            case UNICODE_RANGE:
                unicodeRange = (String)data.getAttribute(EmbedAttribute.UNICODE_RANGE);
                break;
            default:
                isSupported = super.setAttribute(attribute);
        }

        return isSupported;
    }

    @Override
    protected boolean checkAttributeValues(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        boolean result = super.checkAttributeValues(location, problems);
        if (!result)
            return false;

        alias = fontName;
        if (alias == null)
        {
            alias = systemFont;
        }
        if (alias == null)
        {
            alias = fontFamily;
        }
        if (alias == null)
        {
            problems.add(new EmbedCouldNotDetermineFontAliasProblem(location));
            return false;
        }

        if (source != null)
        {
            if (systemFont == null)
            {
                IBinaryFileSpecification fileSpec = workspace.getLatestBinaryFileSpecification(source);
                if (fileSpec != null)
                {
                    File file = new File(fileSpec.getPath());
                    if (!file.exists())
                    {
                        problems.add(new FileNotFoundProblem(location, file.getAbsolutePath()));
                        return false;
                    }
                    URI uri = file.toURI();
                    try
                    {
                        URL url = uri.toURL();
                        locations = new LinkedList<Serializable>();
                        locations.add(url);
                    }
                    catch (MalformedURLException e)
                    {
                        problems.add(new UnableToBuildFontProblem(alias));
                        return false;
                    }
                }
            }
        }
        else if (systemFont != null)
        {
            // the compilation unit needs a file path, which is normally
            // derived from source.
            problems.add(new SystemFontEmbeddingNotSupportedProblem(location, systemFont));
            return false;
        }

        if (locations == null)
        {
            problems.add(new EmbedCouldNotDetermineFontLocationProblem(location));
            return false;
        }

        return result;
    }

    @Override
    protected Map<String, ICharacterTag> doTranscode(Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        FontDescription fontDesc = new FontDescription();
        fontDesc.alias = alias;
        fontDesc.style = getFontStyle(fontWeight, fontStyle);
        fontDesc.unicodeRanges = unicodeRange;
        fontDesc.advancedAntiAliasing = data.getAttribute(EmbedAttribute.ADV_ANTI_ALIASING) != null ? advAntiAliasing : flashType;
        fontDesc.compactFontFormat = embedAsCff;

        DefineFontTag assetTag = buildFont(fontDesc, locations, problems);
        if (assetTag == null)
            return null;

        Map<String, ICharacterTag> symbolTags = Collections.singletonMap(data.getQName(), (ICharacterTag)assetTag);
        return symbolTags;
    }

    private DefineFontTag buildFont(FontDescription fontDesc, List<Serializable> locations, Collection<ICompilerProblem> problems)
    {
        FontManager fontManager = new JREFontManager();

        DefineFontTag defineFontTag = null;
        for (Iterator<Serializable> it = locations.iterator(); it.hasNext();)
        {
            Object fontSource = it.next();
            try
            {
                // For now, keep the Flex 3 behavior of throwing errors for each 
                // location when no FontManager exists.
                if (fontManager == null)
                {
                    problems.add(new NoFontManagerProblem());
                    return null;
                }

                fontDesc.source = fontSource;
                defineFontTag = fontManager.createDefineFont(TagType.DefineFont3.getValue(), fontDesc);
            }
            catch (FontManager.EmbedAsCffNotSupportedException e)
            {
                problems.add(new CffFontEmbeddingNotSupportedProblem(alias));
                return null;
            }
            catch (FontManager.SystemFontNotSupportedException e)
            {
                problems.add(new SystemFontEmbeddingNotSupportedProblem(systemFont));
                return null;
            }
            catch (FontManager.InvalidUnicodeRangeException e)
            {
                // For now, keep the Flex 3 error message for invalid unicode
                // ranges...
                problems.add(new EmbedInvalidUnicodeRangeProblem(e.range));
                return null;
            }
            catch (Exception e)
            {
	            if (Trace.error)
	            {
		            e.printStackTrace();
	            }
                problems.add(new UnableToBuildFontProblem(fontDesc.alias));
                return null;
            }

        }

        if (defineFontTag == null)
        {
            problems.add(new UnableToBuildFontProblem(fontDesc.alias));
        }
        return defineFontTag;
    }

    public static int getFontStyle(String weight, String style)
    {
        int s = FontFace.PLAIN;

        if (style == null)
            style = "normal";

        if (weight == null)
            weight = "normal";

        if (isBold( weight ))
            s += FontFace.BOLD;

        if (isItalic( style ))
            s += FontFace.ITALIC;

        return s;
    }

    public static boolean isBold(String value)
    {
        boolean bold = false;

        if (value != null)
        {
            String b = value.trim().toLowerCase();
            if (b.startsWith("bold"))
            {
                bold = true;
            }
            else
            {
                try
                {
                    int w = Integer.parseInt(b);
                    if (w >= 700)
                        bold = true;
                }
                catch (Throwable t)
                {
                }
            }
        }

        return bold;
    }

    public static boolean isItalic(String value)
    {
        boolean italic = false;

        if (value != null)
        {
            String ital = value.trim().toLowerCase();
            if (ital.equals("italic") || ital.equals("oblique"))
                italic = true;
        }

        return italic;
    }

}

