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
import org.apache.royale.swf.types.ZoneRecord;

/**
 * Represents a <code>DefineFontAlignZones</code> tag in a SWF file.
 * <p>
 * The DefineFont3 tag can be modified by a DefineFontAlignZones tag. The
 * advanced text rendering engine uses alignment zones to establish the borders
 * of a glyph for pixel snapping. Alignment zones are critical for high-quality
 * display of fonts.
 * <p>
 * The alignment zone defines a bounding box for strong vertical and horizontal
 * components of a glyph. The box is described by a left coordinate, thickness,
 * baseline coordinate, and height. Small thicknesses or heights are often set
 * to 0.
 */
public class DefineFontAlignZonesTag extends Tag implements ICharacterReferrer
{
    /**
     * Constructor.
     */
    public DefineFontAlignZonesTag()
    {
        super(TagType.DefineFontAlignZones);
    }

    private ICharacterTag fontTag;
    private int csmTableHint;
    private ZoneRecord[] zoneTable;

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
     * @return the csmTableHint
     */
    public int getCsmTableHint()
    {
        return csmTableHint;
    }

    /**
     * @param csmTableHint the csmTableHint to set
     */
    public void setCsmTableHint(int csmTableHint)
    {
        if (csmTableHint < 0 || csmTableHint > 2)
            throw new IllegalArgumentException();

        this.csmTableHint = csmTableHint;
    }

    /**
     * @return the zoneTable
     */
    public ZoneRecord[] getZoneTable()
    {
        return zoneTable;
    }

    /**
     * @param zoneTable the zoneTable to set
     */
    public void setZoneTable(ZoneRecord[] zoneTable)
    {
        this.zoneTable = zoneTable;
    }
}
