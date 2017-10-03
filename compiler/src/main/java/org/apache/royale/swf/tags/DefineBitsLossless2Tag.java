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
import org.apache.royale.swf.tags.DefineBitsLosslessTag;

/**
 * Represents a <code>DefineBitsLossless2</code> tag in a SWF file.
 * <p>
 * {@code DefineBitsLossless2} extends {@link DefineBitsLosslessTag} with
 * support for opacity (alpha values). The color-map colors in color-mapped
 * images are defined using RGBA values, and direct images store 32-bit ARGB
 * colors for each pixel. The intermediate 15-bit color depth is not available
 * in {@code DefineBitsLossless2}. Note that, {@code ZlibBitmapData} is
 * implemented as raw bytes, so that this class exactly same as its ancestor.
 */
public class DefineBitsLossless2Tag extends DefineBitsLosslessTag
{
    /**
     * Constructor.
     */
    public DefineBitsLossless2Tag()
    {
        super(TagType.DefineBitsLossless2);
    }
}
