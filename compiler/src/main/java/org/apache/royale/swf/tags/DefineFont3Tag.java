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
 * Represents a <code>DefineFont3</code> tag in a SWF file.
 * <p>
 * The DefineFont3 tag is introduced along with the DefineFontAlignZones tag in
 * SWF 8. The DefineFontAlignZones tag is optional but recommended for SWF files
 * using advanced anti-aliasing, and it modifies the DefineFont3 tag.
 * <p>
 * The DefineFont3 tag extends the functionality of DefineFont2 by expressing
 * the SHAPE coordinates in the GlyphShapeTable at 20 times the resolution. All
 * the EMSquare coordinates are multiplied by 20 at export, allowing fractional
 * resolution to 1/20 of a unit. This allows for more precisely defined glyphs
 * and results in better visual quality.
 */
public class DefineFont3Tag extends DefineFont2Tag
{
    /**
     * Constructor.
     */
    public DefineFont3Tag()
    {
        super(TagType.DefineFont3);
    }

    private DefineFontAlignZonesTag zones;

    /**
     * @return the font alignment zones
     */
    public DefineFontAlignZonesTag getZones()
    {
        return zones;
    }

    /**
     * @param zones The font alignment zones to set
     */
    public void setZones(DefineFontAlignZonesTag zones)
    {
        this.zones = zones;
    }
}
