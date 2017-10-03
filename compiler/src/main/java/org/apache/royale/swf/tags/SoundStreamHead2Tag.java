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
 * Represents a <code>SoundStreamHead2</code> tag in a SWF file.
 * <p>
 * The SoundStreamHead2 tag is identical to the SoundStreamHead tag, except it
 * allows different values for StreamSoundCompression and StreamSoundSize
 */
public class SoundStreamHead2Tag extends SoundStreamHeadTag
{
    /**
     * Constructor.
     */
    public SoundStreamHead2Tag()
    {
        super(TagType.SoundStreamHead2);
    }
}
