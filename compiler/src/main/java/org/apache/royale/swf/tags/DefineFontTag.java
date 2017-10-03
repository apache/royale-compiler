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
import org.apache.royale.swf.types.Shape;

/**
 * Represents a <code>DefineFont</code> tag in a SWF file.
 * <p>
 * The DefineFont tag defines the shape outlines of each glyph used in a
 * particular font. Only the glyphs that are used by subsequent DefineText tags
 * are actually defined.
 */
public class DefineFontTag extends CharacterTag implements IDefineFontTag
{
    /**
     * Constructor.
     */
    public DefineFontTag()
    {
        this(TagType.DefineFont);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected DefineFontTag(TagType tagType)
    {
        super(tagType);
    }

    private long[] offsetTable;
    private Shape[] glyphShapeTable;
    private DefineFontNameTag license;

    /**
     * @return the offsetTable
     */
    public long[] getOffsetTable()
    {
        return offsetTable;
    }

    /**
     * @param offsetTable the offsetTable to set
     */
    public void setOffsetTable(long[] offsetTable)
    {
        this.offsetTable = offsetTable;
    }

    /**
     * @return the glyphShapeTable
     */
    public Shape[] getGlyphShapeTable()
    {
        return glyphShapeTable;
    }

    /**
     * @param glyphShapeTable the glyphShapeTable to set
     */
    public void setGlyphShapeTable(Shape[] glyphShapeTable)
    {
        this.glyphShapeTable = glyphShapeTable;
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
