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
 * Represents a <code>FrameLabel</code> tag in a SWF file.
 * <p>
 * The FrameLabel tag gives the specified Name to the current frame.
 * {@code ActionGoToLabel} uses this name to identify the frame.
 */
public class FrameLabelTag extends Tag implements IManagedTag
{
    /**
     * Constructor.
     * 
     * @param name frame name
     */
    public FrameLabelTag(String name)
    {
        super(TagType.FrameLabel);
        this.name = name;
        this.namedAnchorTag = false;
    }
    
    private String name;
    private boolean namedAnchorTag;



    /**
     * Get the frame name.
     * 
     * @return frame name
     */
    public String getName()
    {
        return name;
    }

    /**
     * In SWF files of version 6 or later, an extension to the FrameLabel tag
     * called named anchors is available. A named anchor is a special kind of
     * frame label that, in addition to labeling a frame for seeking using
     * {@code ActionGoToLabel}, labels the frame for seeking using HTML anchor
     * syntax.
     * 
     * @return true if this FrameLabel is a named anchor tag.
     */
    public boolean isNamedAnchorTag()
    {
        return namedAnchorTag;
    }

    /**
     * Set if the this FrameLabel is an named anchor tag.
     * <p>
     * In SWF files of version 6 or later, an extension to the FrameLabel tag
     * called named anchors is available. A named anchor is a special kind of
     * frame label that, in addition to labeling a frame for seeking using
     * {@code ActionGoToLabel}, labels the frame for seeking using HTML anchor
     * syntax.
     * 
     * @param value is new namedAnchorTag
     */
    public void setNamedAnchorTag(boolean value)
    {
        namedAnchorTag = value;
    }

    /**
     * Set the frame label name.
     * 
     * @param value frame label
     */
    public void setName(String value)
    {
        name = value;
    }

    @Override
    public String description()
    {
        return name;
    }

}
