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

/**
 * The GLYPHENTRY structure describes a single character in a line of text. It
 * is composed of an index into the current font's glyph table, and an advance
 * value. The advance value is the horizontal distance between the reference
 * point of this character and the reference point of the following character.
 */
public class GlyphEntry implements IDataType
{
    private int glyphIndex;
    private int glyphAdvance;

    /**
     * The character of the GlyphEntry. Utility member used by FontBuilder, but
     * not stored directly in the swf
     */
    public char character;
    /**
     * The bounds of the GlyphEntry. Utility member used by FontBuilder, but not
     * stored directly in the swf
     */
    public Rect bounds;
    /**
     * The zoneRecord of the GlyphEntry. Utility member used by FontBuilder, but
     * not stored directly in the swf
     */
    public ZoneRecord zoneRecord;
    /**
     * The shape of the GlyphEntry. Utility member used by FontBuilder, but not
     * stored directly in the swf
     */
    public Shape shape;

    /**
     * @return the glyphIndex
     */
    public int getGlyphIndex()
    {
        return glyphIndex;
    }

    /**
     * @param glyphIndex the glyphIndex to set
     */
    public void setGlyphIndex(int glyphIndex)
    {
        this.glyphIndex = glyphIndex;
    }

    /**
     * @return the glyphAdvance
     */
    public int getGlyphAdvance()
    {
        return glyphAdvance;
    }

    /**
     * @param glyphAdvance the glyphAdvance to set
     */
    public void setGlyphAdvance(int glyphAdvance)
    {
        this.glyphAdvance = glyphAdvance;
    }
}
