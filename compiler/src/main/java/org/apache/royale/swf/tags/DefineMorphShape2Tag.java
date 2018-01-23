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

/**
 * Represents a <code>DefineMorphShape2</code> tag in a SWF file.
 * <p>
 * The DefineMorphShape2 tag extends the capabilities of DefineMorphShape by
 * using a new morph line style record in the morph shape. MORPHLINESTYLE2
 * allows the use of new types of joins and caps as well as scaling options and
 * the ability to fill the strokes of the morph shape.
 * <p>
 * DefineMorphShape2 specifies not only the shape bounds but also the edge
 * bounds of the shape. While the shape bounds are calculated along the outside
 * of the strokes, the edge bounds are taken from the outside of the edges. For
 * an example of shape bounds versus edge bounds, see the diagram in
 * DefineShape4. The new StartEdgeBounds and EndEdgeBounds fields assist Flash
 * Player in accurately determining certain layouts.
 * <p>
 * In addition, DefineMorphShape2 includes new hinting information,
 * UsesNonScalingStrokes and UsesScalingStrokes. These flags assist Flash Player
 * in creating the best possible area for invalidation.
 */
public class DefineMorphShape2Tag extends DefineMorphShapeTag
{
    /**
     * Constructor.
     */
    public DefineMorphShape2Tag()
    {
        super(TagType.DefineMorphShape2);
    }

    private Rect startEdgeBounds;
    private Rect endEdgeBounds;
    private boolean usesNonScalingStrokes;
    private boolean usesScalingStrokes;

    /**
     * Bounds of the start shape, excluding strokes.
     * 
     * @return the startEdgeBounds
     */
    public Rect getStartEdgeBounds()
    {
        return startEdgeBounds;
    }

    /**
     * Set start edge bounds.
     * 
     * @param value the startEdgeBounds to set
     */
    public void setStartEdgeBounds(Rect value)
    {
        startEdgeBounds = value;
    }

    /**
     * Bounds of the end shape, excluding strokes.
     * 
     * @return the endEdgeBounds
     */
    public Rect getEndEdgeBounds()
    {
        return endEdgeBounds;
    }

    /**
     * Set end edge bounds.
     * 
     * @param value the endEdgeBounds to set
     */
    public void setEndEdgeBounds(Rect value)
    {
        endEdgeBounds = value;
    }

    /**
     * If true, the shape contains at least one non-scaling stroke.
     * 
     * @return the usesNonScalingStrokes
     */
    public boolean isUsesNonScalingStrokes()
    {
        return usesNonScalingStrokes;
    }

    /**
     * Set uses non-scaling strokes.
     * 
     * @param value the usesNonScalingStrokes to set
     */
    public void setUsesNonScalingStrokes(boolean value)
    {
        usesNonScalingStrokes = value;
    }

    /**
     * If true, the shape contains at least one scaling stroke.
     * 
     * @return the usesSCalingStrokes
     */
    public boolean isUsesScalingStrokes()
    {
        return usesScalingStrokes;
    }

    /**
     * Set uses scaling strokes.
     * 
     * @param value the usesSCalingStrokes to set
     */
    public void setUsesScalingStrokes(boolean value)
    {
        usesScalingStrokes = value;
    }
}
