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

import java.util.LinkedHashMap;

import org.apache.royale.swf.TagType;

/**
 * Represents a <code>DefineSceneAndFrameLabelData</code> tag in a SWF file.
 * <p>
 * The DefineSceneAndFrameLabelData tag contains scene and frame label data for
 * a MovieClip. Scenes are supported for the main timeline only, for all other
 * movie clips a single scene is exported.
 */
public class DefineSceneAndFrameLabelDataTag extends Tag implements IManagedTag
{
    /**
     * Constructor.
     */
    public DefineSceneAndFrameLabelDataTag()
    {
        super(TagType.DefineSceneAndFrameLabelData);
        scenes = new LinkedHashMap<String, Long>();
        frames = new LinkedHashMap<String, Long>();
    }

    private final LinkedHashMap<String, Long> scenes;
    private final LinkedHashMap<String, Long> frames;

    /**
     * Add a scene definition.
     * 
     * @param name name of the scene
     * @param offset frame offset for the scene
     */
    public void addScene(String name, long offset)
    {
        scenes.put(name, offset);
    }

    /**
     * Add a frame label definition.
     * 
     * @param label frame label string of the frame
     * @param number frame number of the frame (zero based)
     */
    public void addFrame(String label, long number)
    {
        frames.put(label, number);
    }
}
