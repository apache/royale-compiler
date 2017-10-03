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
 * Represents a <code>DefineBitsLossless</code> tag in a SWF file.
 * <p>
 * Defines a loss-less bitmap character that contains RGB bitmap data compressed
 * with ZLIB. The data format used by the ZLIB library is described by Request
 * for Comments (RFCs) documents 1950 to 1952.
 * <p>
 * Two kinds of bitmaps are supported.
 * <ul>
 * <li>Color-mapped images define a color-map of up to 256 colors, each
 * represented by a 24-bit RGB value, and then use 8-bit pixel values to index
 * into the color-map.</li>
 * <li>Direct images store actual pixel color values using 15 bits (32,768
 * colors) or 24 bits (about 17 million colors).</li>
 * </ul>
 */
public class DefineBitsLosslessTag extends CharacterTag
        implements IDefineBinaryImageTag, IAlwaysLongTag
{
    public static final int BF_8BIT_COLORMAPPED_IMAGE = 3;
    public static final int BF_15BIT_RGB_IMAGE = 4;
    public static final int BF_24BIT_RGB_IMAGE = 5;

    /**
     * Constructor.
     */
    public DefineBitsLosslessTag()
    {
        super(TagType.DefineBitsLossless);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected DefineBitsLosslessTag(TagType tagType)
    {
        super(tagType);
    }

    private int bitmapFormat;
    private int bitmapWidth;
    private int bitmapHeight;
    private int bitmapColorTableSize;
    private byte[] zlibBitmapData;

    public int getBitmapFormat()
    {
        return bitmapFormat;
    }

    public void setBitmapFormat(int bitmapFormat)
    {
        this.bitmapFormat = bitmapFormat;
    }

    public int getBitmapWidth()
    {
        return bitmapWidth;
    }

    public void setBitmapWidth(int bitmapWidth)
    {
        this.bitmapWidth = bitmapWidth;
    }

    public int getBitmapHeight()
    {
        return bitmapHeight;
    }

    public void setBitmapHeight(int bitmapHeight)
    {
        this.bitmapHeight = bitmapHeight;
    }

    public int getBitmapColorTableSize()
    {
        return bitmapColorTableSize;
    }

    public void setBitmapColorTableSize(int bitmapColorTableSize)
    {
        this.bitmapColorTableSize = bitmapColorTableSize;
    }

    public byte[] getZlibBitmapData()
    {
        return zlibBitmapData;
    }

    public void setZlibBitmapData(byte[] zlibBitmapData)
    {
        this.zlibBitmapData = zlibBitmapData;
    }

    public static int getBf8bitColormappedImage()
    {
        return BF_8BIT_COLORMAPPED_IMAGE;
    }

    public static int getBf15bitRgbImage()
    {
        return BF_15BIT_RGB_IMAGE;
    }

    public static int getBf24bitRgbImage()
    {
        return BF_24BIT_RGB_IMAGE;
    }

    @Override
    public byte[] getData()
    {
        return getZlibBitmapData();
    }
}
