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
 * Represents a <code>Metadata</code> tag in a SWF file.
 * <p>
 * The Metadata tag is an optional tag to describe the SWF file to an external
 * process. The tag embeds XML metadata in the SWF file so that, for example, a
 * search engine can locate this tag, access a title for the SWF file, and
 * display that title in search results. Flash Player always ignores the
 * Metadata tag.
 */
public class MetadataTag extends Tag implements IManagedTag
{
    /**
     * Constructor.
     */
    public MetadataTag()
    {
        super(TagType.Metadata);
    }

    /**
     * Constructor to create a metadata tag with a specified metadata string.
     * 
     * @param metadata metadata text
     */
    public MetadataTag(String metadata)
    {
        this();
        this.metadata = metadata;
    }
    
    private String metadata;

    /**
     * Get metadata.
     * 
     * @return metadata
     */
    public String getMetadata()
    {
        return metadata;
    }

    /**
     * Set metadata.
     * 
     * @param value metadata text
     */
    public void setMetadata(String value)
    {
        metadata = value;
    }

}
