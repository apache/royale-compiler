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
import org.apache.royale.swf.types.CXForm;
import org.apache.royale.swf.types.Matrix;

/**
 * Represents a <code>PlaceObject</code> tag in a SWF file.
 * <p>
 * The {@code PlaceObject} tag adds a character to the display list. The same
 * character can be added more than once to the display list with a different
 * depth and transformation matrix.
 * <p>
 * If a character does not change from frame to frame, you do not need to
 * replace the unchanged character after each frame.
 * <p>
 * <b>Note:</b> PlaceObject is rarely used in SWF 3 and later versions. It is
 * superseded by {@link PlaceObject2Tag} and {@link PlaceObject3Tag}.
 */
public class PlaceObjectTag extends Tag implements ICharacterReferrer
{
    /**
     * Constructor.
     */
    public PlaceObjectTag()
    {
        super(TagType.PlaceObject);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected PlaceObjectTag(TagType type)
    {
        super(type);
    }

    protected ICharacterTag character;
    protected int depth;
    protected Matrix matrix;
    protected CXForm colorTransform;

    public int getDepth()
    {
        return depth;
    }

    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    public Matrix getMatrix()
    {
        return matrix;
    }

    public void setMatrix(Matrix matrix)
    {
        this.matrix = matrix;
    }

    public CXForm getColorTransform()
    {
        return colorTransform;
    }

    public void setColorTransform(CXForm colorTransform)
    {
        this.colorTransform = colorTransform;
    }

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        assert character != null;
        return CharacterIterableFactory.from(character);
    }

    public void setCharacter(ICharacterTag character)
    {
        this.character = character;
    }

    public ICharacterTag getCharacter()
    {
        return character;
    }
}
