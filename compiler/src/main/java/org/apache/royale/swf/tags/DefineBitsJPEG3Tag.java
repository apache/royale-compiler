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
 * Represents a <code>DefineBitsJPEG3</code> tag in a SWF file.
 * <p>
 * This tag defines a bitmap character with JPEG compression. This tag extends
 * DefineBitsJPEG2, adding alpha channel (opacity) data. Opacity/transparency
 * information is not a standard feature in JPEG images, so the alpha channel
 * information is encoded separately from the JPEG data, and compressed using
 * the ZLIB standard for compression. The data format used by the ZLIB library
 * is described by Request for Comments (RFCs) documents 1950 to 1952.
 * <p>
 * The data in this tag begins with the JPEG SOI marker 0xFF, 0xD8 and ends with
 * the EOI marker 0xFF, 0xD9. Before version 8 of the SWF file format, SWF files
 * could contain an erroneous header of 0xFF, 0xD9, 0xFF, 0xD8 before the JPEG
 * SOI marker.
 * <p>
 * In addition to specifying JPEG data, DefineBitsJPEG2 can also contain PNG
 * image data and non-animated GIF89a image data.
 * <ul>
 * <li>If ImageData begins with the eight bytes 0x89 0x50 0x4E 0x47 0x0D 0x0A
 * 0x1A 0x0A, the ImageData contains PNG data.</li>
 * <li>If ImageData begins with the six bytes 0x47 0x49 0x46 0x38 0x39 0x61, the
 * ImageData contains GIF89a data.</li>
 * </ul>
 * If ImageData contains PNG or GIF89a data, the optional BitmapAlphaData is not
 * supported.
 */
public class DefineBitsJPEG3Tag extends DefineBitsJPEG2Tag
{
    /**
     * Constructor.
     */
    public DefineBitsJPEG3Tag()
    {
        super(TagType.DefineBitsJPEG3);
    }

    private long alphaDataOffset;
    private byte[] bitmapAlphaData;

    /**
     * Get count of bytes in ImageData.
     * 
     * @return image data size
     */
    public long getAlphaDataOffset()
    {
        return alphaDataOffset;
    }

    /**
     * Set count of bytes in ImageData.
     * 
     * @param value image data size
     */
    public void setAlphaDataOffset(long value)
    {
        alphaDataOffset = value;
    }

    /**
     * ZLIB compressed array of alpha data. Only supported when tag contains
     * JPEG data. One byte per pixel. Total size after decompression must equal
     * (width * height) of JPEG image.
     * 
     * @return bitmap alpha data
     */
    public byte[] getBitmapAlphaData()
    {
        return bitmapAlphaData;
    }

    /**
     * ZLIB compressed array of alpha data. Only supported when tag contains
     * JPEG data. One byte per pixel. Total size after decompression must equal
     * (width * height) of JPEG image.
     * 
     * @param bitmapAlphaData bitmap alpha data
     */
    public void setBitmapAlphaData(byte[] bitmapAlphaData)
    {
        this.bitmapAlphaData = bitmapAlphaData;
    }
}
