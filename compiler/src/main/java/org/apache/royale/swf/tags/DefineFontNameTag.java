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
 * Represents a <code>DefineFontName</code> tag in a SWF file.
 * <p>
 * The DefineFontName tag contains the name and copyright information for a font
 * embedded in the SWF file.
 */
public class DefineFontNameTag extends Tag implements ICharacterReferrer
{
    /**
     * Constructor.
     */
    public DefineFontNameTag()
    {
        super(TagType.DefineFontName);
    }

    private ICharacterTag fontTag;
    private String fontName;
    private String fontCopyright;

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        assert fontTag != null;
        return CharacterIterableFactory.from(fontTag);
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
     * @return the fontName
     */
    public String getFontName()
    {
        return fontName;
    }

    /**
     * @param fontName the fontName to set
     */
    public void setFontName(String fontName)
    {
        this.fontName = fontName;
    }

    /**
     * @return the fontCopyright
     */
    public String getFontCopyright()
    {
        return fontCopyright;
    }

    /**
     * @param fontCopyright the fontCopyright to set
     */
    public void setFontCopyright(String fontCopyright)
    {
        this.fontCopyright = fontCopyright;
    }
}
