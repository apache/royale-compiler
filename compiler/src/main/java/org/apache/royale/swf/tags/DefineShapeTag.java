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

import java.util.ArrayList;

import org.apache.royale.swf.TagType;
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.ShapeWithStyle;

/**
 * Represents a <code>DefineShape</code> tag in a SWF file.
 * <p>
 * The {@code DefineShape} tag defines a shape for later use by control tags
 * such as {@code PlaceObject}. The {@code ShapeId} uniquely identifies this
 * shape as <i>character</i> in the <i>Dictionary</i>. The {@code ShapeBounds}
 * field is the rectangle that completely encloses the shape. The
 * {@code ShapeWithStyle} structure includes all the paths, fill styles and line
 * styles that make up the shape.
 */
public class DefineShapeTag extends CharacterTag implements ICharacterReferrer
{
    /**
     * Constructor.
     */
    public DefineShapeTag()
    {
        this(TagType.DefineShape);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected DefineShapeTag(TagType tagType)
    {
        super(tagType);
    }
    
    private Rect shapeBounds;
    private ShapeWithStyle shapes;

    public Rect getShapeBounds()
    {
        return shapeBounds;
    }

    public ShapeWithStyle getShapes()
    {
        return shapes;
    }

    public void setShapeBounds(Rect shapeBounds)
    {
        this.shapeBounds = shapeBounds;
    }

    public void setShapes(ShapeWithStyle shapes)
    {
        this.shapes = shapes;
    }

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        ArrayList<ICharacterTag> result = new ArrayList<ICharacterTag>();
        for (ICharacterTag tag : shapes.getReferences())
        {
            result.add(tag);
        }

        return result;
    }
}
