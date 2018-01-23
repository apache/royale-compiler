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
import org.apache.royale.swf.types.RGB;

/**
 * Represents a <code>SetBackgroundColor</code> tag in a SWF file.
 * <p>
 * The SetBackgroundColor tag sets the background color of the display.
 */
public class SetBackgroundColorTag extends Tag implements IManagedTag
{
    /**
     * Constructor.
     */
    public SetBackgroundColorTag(RGB color)
    {
        super(TagType.SetBackgroundColor);
        
        this.backgroundColor = color;
    }
    
    private final RGB backgroundColor;

    /**
     * Create a {@code SetBackgroundColor} tag from RGB component values.
     */
    public SetBackgroundColorTag(short red, short green, short blue)
    {
        this(new RGB(red, green, blue));
    }

    /**
     * Get background color.
     * 
     * @return color
     */
    public RGB getColor()
    {
        return backgroundColor;
    }

    @Override
    public String description()
    {
        return backgroundColor.toString();
    }
}
