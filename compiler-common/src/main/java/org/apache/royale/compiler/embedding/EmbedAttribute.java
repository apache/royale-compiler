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

package org.apache.royale.compiler.embedding;

/**
 * enum to map all the attributes supported by the Embed directive
 */
public enum EmbedAttribute
{
    SOURCE("source"),
    MIME_TYPE("mimeType"),
    COMPRESSION("compression"),
    ENCODING("encoding"),
    EXPORT_SYMBOL("exportSymbol"),
    FLASH_TYPE("flashType"),
    ORIGINAL("original"),
    QUALITY("quality"),
    SCALE_GRID_BOTTOM("scaleGridBottom"),
    SCALE_GRID_LEFT("scaleGridLeft"),
    SCALE_GRID_RIGHT("scaleGridRight"),
    SCALE_GRID_TOP("scaleGridTop"),
    SKIN_CLASS("skinClass"),
    SMOOTHING("smoothing"),
    SYMBOL("symbol"),
    // obsolete font related attributes, which are kept
    // here so we don't report problems on them, and instead
    // report one problem that font embeddeding is not supported.
    ADV_ANTI_ALIASING("advancedAntiAliasing"),
    EMBED_AS_CFF("embedAsCFF"),
    UNICODE_RANGE("unicodeRange"),
    FONT_FAMILY("fontFamily"),
    FONT_NAME("fontName"),
    FONT_STYLE("fontStyle"),
    FONT_WEIGHT("fontWeight"),
    SYSTEM_FONT("systemFont"),
    SOURCE_LIST("sourceList");

    private EmbedAttribute(String attributeName)
    {
        this.attributeName = attributeName;
    }

    /**
     * Test whether an attribute string matches this enum value
     * @param attributeName Name of attribute to compare
     * @return true if matches
     */
    public boolean equals(String attributeName)
    {
        return this.attributeName.equals(attributeName);
    }

    @Override
    public String toString()
    {
        return attributeName;
    }

    private final String attributeName;
}
