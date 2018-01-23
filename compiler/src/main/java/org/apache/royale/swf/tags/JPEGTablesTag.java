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
 * Represents a <code>JPEGTables</code> tag in a SWF file.
 * <p>
 * This tag defines the JPEG encoding table (the Tables/Misc segment) for all
 * JPEG images defined using the {@link DefineBitsTag} tag. There may only be one
 * JPEGTables tag in a SWF file.
 */
public class JPEGTablesTag extends Tag
{
    /**
     * Constructor.
     */
    public JPEGTablesTag()
    {
        super(TagType.JPEGTables);
    }

    private byte[] jpegData;

    /**
     * Get JPEG compressed image.
     * 
     * @return JPEG data
     */
    public byte[] getJpegData()
    {
        return jpegData;
    }

    /**
     * Set JPEG compressed image.
     * 
     * @param value JPEG data
     */
    public void setJpegData(byte[] value)
    {
        jpegData = value;
    }
}
