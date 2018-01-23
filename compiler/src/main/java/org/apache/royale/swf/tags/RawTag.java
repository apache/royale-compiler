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
 * A RawTag is a wrapper for the raw bytes of an unimplemented SWF tag.
 * <p>
 * It is not an actual SWF tag. 
 */
public final class RawTag extends Tag
{
    /**
     * Constructor.
     * 
     * @param tagType tag type
     */
    public RawTag(TagType tagType)
    {
        super(tagType);
    }

    private byte[] tagBody;

    /**
     * Get tag body raw bytes.
     * 
     * @return tag body raw bytes
     */
    public byte[] getTagBody()
    {
        return tagBody;
    }

    /**
     * Set tag body raw bytes.
     * 
     * @param tagBody raw bytes
     */
    public void setTagBody(byte[] tagBody)
    {
        this.tagBody = tagBody;
    }

    @Override
    public String toString()
    {
        return String.format("#RAW TAG# type=%s, size=%d",
                this.getTagType(),
                tagBody.length);
    }
}
