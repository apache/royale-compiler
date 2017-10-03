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
 * Represents a <code>DefineBitsJPEG2</code> tag in a SWF file.
 * <p>
 * This tag defines a bitmap character with JPEG compression. It differs from
 * {@link DefineBitsTag} in that it contains both the JPEG encoding table and the
 * JPEG image data. This tag allows multiple JPEG images with differing encoding
 * tables to be defined within a single SWF file.
 */
public class DefineBitsJPEG2Tag extends DefineBitsTag
{
    /**
     * Constructor.
     */
    public DefineBitsJPEG2Tag()
    {
        super(TagType.DefineBitsJPEG2);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected DefineBitsJPEG2Tag(TagType tagType)
    {
        super(tagType);
    }
}
