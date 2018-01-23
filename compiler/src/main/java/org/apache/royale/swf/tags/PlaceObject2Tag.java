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
import org.apache.royale.swf.types.CXFormWithAlpha;
import org.apache.royale.swf.types.ClipActions;

/**
 * Represents a <code>PlaceObject2</code> tag in a SWF file.
 * <p>
 * The {@code PlaceObject2} tag extends the functionality of the
 * {@code PlaceObject} tag. The {@code PlaceObject2} tag can both add a
 * character to the display list, and modify the attributes of a character that
 * is already on the display list. {@code The PlaceObject2} tag changed slightly
 * from SWF 4 to SWF 5. In SWF 5, clip actions were added.
 * <p>
 * The tag begins with a group of flags that indicate which fields are present
 * in the tag. The optional fields are <code>CharacterId, Matrix, 
 * ColorTransform, Ratio, ClipDepth, Name, ClipActions</code>. The Depth field
 * is the only field that is always required.
 * <p>
 * The depth value determines the stacking order of the character. Characters
 * with lower depth values are displayed underneath characters with higher depth
 * values. A depth value of 1 means the character is displayed at the bottom of
 * the stack. Any given depth can have only one character. This means a
 * character that is already on the display list can be identified by its depth
 * alone (that is, a CharacterId is not required).
 * <p>
 * The PlaceFlagMove and PlaceFlagHasCharacter tags indicate whether a new
 * character is being added to the display list, or a character already on the
 * display list is being modified.
 */
public class PlaceObject2Tag extends PlaceObjectTag
{
    /**
     * Constructor.
     */
    public PlaceObject2Tag()
    {
        super(TagType.PlaceObject2);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected PlaceObject2Tag(TagType type)
    {
        super(type);
    }

    private boolean hasClipActions;
    private boolean hasClipDepth;
    private boolean hasName;
    private boolean hasRatio;
    private boolean hasColorTransform;
    private boolean hasMatrix;
    private boolean hasCharacter;
    private boolean move;

    private int ratio;
    private int clipDepth;
    private String name;
    private ClipActions clipActions;
    private CXFormWithAlpha colorTransform;

    @Override
    public String description()
    {
        final StringBuilder result = new StringBuilder();
        result.append(String.format("ratio=%d, name=%s, id=%s, depth=%d (%s%s%s%s%s%s%s%s)",
                ratio, name, hasCharacter ? character.getCharacterID() : "", depth,
                hasClipActions ? "hasClipAction " : "",
                hasClipDepth ? "hasClipDepth " : "",
                hasName ? "hasName " : "",
                hasRatio ? "hasRatio " : "",
                hasColorTransform ? "hasColorTransform " : "",
                hasMatrix ? "hasMatrix " : "",
                move ? "move " : "",
                hasCharacter ? "hasCharacter " : ""));
        result.append("\n  >> ").append(character);
        return result.toString();
    }

    public boolean isHasClipActions()
    {
        return hasClipActions;
    }

    public void setHasClipActions(boolean hasClipActions)
    {
        this.hasClipActions = hasClipActions;
    }

    public boolean isHasClipDepth()
    {
        return hasClipDepth;
    }

    public void setHasClipDepth(boolean hasClipDepth)
    {
        this.hasClipDepth = hasClipDepth;
    }

    public boolean isHasName()
    {
        return hasName;
    }

    public void setHasName(boolean hasName)
    {
        this.hasName = hasName;
    }

    public boolean isHasRatio()
    {
        return hasRatio;
    }

    public void setHasRatio(boolean hasRatio)
    {
        this.hasRatio = hasRatio;
    }

    public boolean isHasColorTransform()
    {
        return hasColorTransform;
    }

    public void setHasColorTransform(boolean hasColorTransform)
    {
        this.hasColorTransform = hasColorTransform;
    }

    public boolean isHasMatrix()
    {
        return hasMatrix;
    }

    public void setHasMatrix(boolean hasMatrix)
    {
        this.hasMatrix = hasMatrix;
    }

    public boolean isHasCharacter()
    {
        return hasCharacter;
    }

    public void setHasCharacter(boolean hasCharacter)
    {
        this.hasCharacter = hasCharacter;
    }

    public boolean isMove()
    {
        return move;
    }

    public void setMove(boolean move)
    {
        this.move = move;
    }

    public int getRatio()
    {
        return ratio;
    }

    public void setRatio(int ratio)
    {
        this.ratio = ratio;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ClipActions getClipActions()
    {
        return clipActions;
    }

    public void setClipActions(ClipActions clipActions)
    {
        this.clipActions = clipActions;
    }

    @Override
    public CXFormWithAlpha getColorTransform()
    {
        return colorTransform;
    }

    public void setColorTransform(CXFormWithAlpha colorTransform)
    {
        this.colorTransform = colorTransform;
    }

    public int getClipDepth()
    {
        return clipDepth;
    }

    public void setClipDepth(int clipDepth)
    {
        this.clipDepth = clipDepth;
    }

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        if (!isHasCharacter())
            return CharacterIterableFactory.empty();

        return super.getReferences();
    }
}
