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
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.Shape;

/**
 * Represents a <code>DefineMorphShapeTag</code> tag in a SWF file.
 * <p>
 * The DefineMorphShape tag defines the start and end states of a morph
 * sequence. A morph object should be displayed with the PlaceObject2 tag, where
 * the ratio field specifies how far the morph has progressed.
 */
public class DefineMorphShapeTag extends CharacterTag implements ICharacterReferrer
{
    /**
     * Constructor.
     */
    public DefineMorphShapeTag()
    {
        super(TagType.DefineMorphShape);
    }

    /**
     * Protected constructor for use by subclasses with other tag types.
     */
    protected DefineMorphShapeTag(TagType tagType)
    {
        super(tagType);
    }

    private Rect startBounds;
    private Rect endBounds;
    private long offset;
    private Shape startEdges;
    private Shape endEdges;

    /**
     * Bounds of the start shape.
     * 
     * @return the startBounds
     */
    public Rect getStartBounds()
    {
        return startBounds;
    }

    /**
     * Set bounds of the start shape.
     * 
     * @param startBounds the startBounds to set
     */
    public void setStartBounds(Rect startBounds)
    {
        this.startBounds = startBounds;
    }

    /**
     * Bounds of the end shape.
     * 
     * @return the endBounds
     */
    public Rect getEndBounds()
    {
        return endBounds;
    }

    /**
     * Set the bounds of the end shape.
     * 
     * @param endBounds the endBounds to set
     */
    public void setEndBounds(Rect endBounds)
    {
        this.endBounds = endBounds;
    }

    /**
     * Indicates offset to EndEdges.
     * 
     * @return the offset
     */
    public long getOffset()
    {
        return offset;
    }

    /**
     * Set offset to EndEdges.
     * 
     * @param offset the offset to set
     */
    public void setOffset(long offset)
    {
        this.offset = offset;
    }

    /**
     * Get start edges.
     * <p>
     * Contains the set of edges and the style bits that indicate style changes
     * (for example, MoveTo, FillStyle, and LineStyle). Number of edges must
     * equal the number of edges in EndEdges.
     * 
     * @return the startEdges
     */
    public Shape getStartEdges()
    {
        return startEdges;
    }

    /**
     * Set start edges.
     * 
     * @param startEdges the startEdges to set
     */
    public void setStartEdges(Shape startEdges)
    {
        this.startEdges = startEdges;
    }

    /**
     * Get end edges.
     * <p>
     * Contains only the set of edges, with no style information. Number of
     * edges must equal the number of edges in StartEdges.
     * 
     * @return the endEdges
     */
    public Shape getEndEdges()
    {
        return endEdges;
    }

    /**
     * Set end edges.
     * 
     * @param endEdges the endEdges to set
     */
    public void setEndEdges(Shape endEdges)
    {
        this.endEdges = endEdges;
    }

    /**
     * Get the character tags referred by this DefineMorphShape tag.
     * <ul>
     * <li>DefineMorphShape.StartEdge.ShapeRecords(StyleChage).FillStyles.Bitmap
     * </li>
     * <li>DefineMorphShape.MorphFillStyles.Bitmap</li>
     * </ul>
     */
    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        return CharacterIterableFactory.collect(
                startEdges.getReferences(),
                endEdges.getReferences());
    }
}
