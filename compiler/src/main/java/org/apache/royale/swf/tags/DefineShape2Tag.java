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
 * Represents a <code>DefineShape2</code> tag in a SWF file.
 * <p>
 * {@code DefineShape2} extends the capabilities of {@link DefineShapeTag} with
 * the ability to support more than 255 styles in the style list and multiple
 * style lists in a single shape.
 */
public class DefineShape2Tag extends DefineShapeTag
{
    /**
     * Constructor.
     */
    public DefineShape2Tag()
    {
        this(TagType.DefineShape2);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected DefineShape2Tag(TagType tagType)
    {
        super(tagType);
    }
}
