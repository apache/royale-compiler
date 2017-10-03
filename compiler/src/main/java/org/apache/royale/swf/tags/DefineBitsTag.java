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
 * Represents a <code>DefineBits</code> tag in a SWF file.
 * <p>
 * This tag defines a bitmap character with JPEG compression. It contains only
 * the JPEG compressed image data (from the Frame Header onward). A separate
 * JPEGTables tag contains the JPEG encoding data used to encode this image (the
 * Tables/Misc segment).
 */
public class DefineBitsTag extends CharacterTag
        implements IDefineBinaryImageTag, IAlwaysLongTag
{
    /**
     * Constructor.
     */
    public DefineBitsTag()
    {
        super(TagType.DefineBits);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected DefineBitsTag(TagType tagType)
    {
        super(tagType);
    }

    private byte[] imageData;

    /**
     * Get JPEG compressed image.
     * 
     * @return JPEG data
     */
    public byte[] getImageData()
    {
        return imageData;
    }

    /**
     * Set JPEG compressed image.
     * 
     * @param value JPEG data
     */
    public void setImageData(byte[] value)
    {
        imageData = value;
    }

    @Override
    public byte[] getData()
    {
        return getImageData();
    }

}
