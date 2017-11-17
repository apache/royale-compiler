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

package org.apache.royale.compiler.internal.embedding.transcoders;

import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.EmbedScalingGridProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.tags.DefineScalingGridTag;
import org.apache.royale.swf.tags.DefineSpriteTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.swf.types.Rect;

/**
 * Abstract class which should be extended by any embedding transcoder
 * which supports scaling.  Currently images and movies.
 */
public abstract class ScalableTranscoder extends TranscoderBase
{
    /**
     * @param data
     * @param workspace
     */
    protected ScalableTranscoder(EmbedData data, Workspace workspace)
    {
        super(data, workspace);
        this.scaling = false;
    }

    protected boolean scaling;
    protected Integer scaleGridBottom;
    protected Integer scaleGridLeft;
    protected Integer scaleGridRight;
    protected Integer scaleGridTop;

    @Override
    protected boolean setAttribute(EmbedAttribute attribute)
    {
        boolean isSupported = true;
        switch (attribute)
        {
            case SCALE_GRID_BOTTOM:
                scaleGridBottom = (Integer)data.getAttribute(EmbedAttribute.SCALE_GRID_BOTTOM);
                break;
            case SCALE_GRID_LEFT:
                scaleGridLeft = (Integer)data.getAttribute(EmbedAttribute.SCALE_GRID_LEFT);
                break;
            case SCALE_GRID_RIGHT:
                scaleGridRight = (Integer)data.getAttribute(EmbedAttribute.SCALE_GRID_RIGHT);
                break;
            case SCALE_GRID_TOP:
                scaleGridTop = (Integer)data.getAttribute(EmbedAttribute.SCALE_GRID_TOP);
                break;
            default:
                isSupported = super.setAttribute(attribute);
        }

        return isSupported;
    }

    @Override
    protected boolean checkAttributeValues(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        boolean result = super.checkAttributeValues(location, problems);
        if (!result)
            return false;

        // if any of the scaling values are set, they all need to be set
        if (scaleGridBottom != null || scaleGridLeft != null || scaleGridRight != null || scaleGridTop != null)
        {
            if (scaleGridBottom == null || scaleGridLeft == null || scaleGridRight == null || scaleGridTop == null)
            {
                problems.add(new EmbedScalingGridProblem(location));
                result = false;
            }
            else
            {
                // values ok, so turn on scaling
                scaling = true;
            }
        }

        return result;
    }

    protected DefineScalingGridTag buildScalingGrid()
    {
        Rect rect = new Rect(scaleGridLeft, scaleGridRight, scaleGridTop, scaleGridBottom);
        DefineScalingGridTag scalingGrid = new DefineScalingGridTag();
        scalingGrid.setSplitter(rect); 
        return scalingGrid;
    }

    protected DefineSpriteTag buildSprite(List<ITag> spriteTags, int frameCount, DefineScalingGridTag scalingGrid, Collection<ITag> tags)
    {
        DefineSpriteTag sprite = new DefineSpriteTag(frameCount, spriteTags);

        if (scalingGrid != null)
        {
            scalingGrid.setCharacter(sprite);
            tags.add(scalingGrid);
        }

        return sprite;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!super.equals(o))
            return false;

        if (!(o instanceof ScalableTranscoder))
            return false;

        ScalableTranscoder t = (ScalableTranscoder)o;
        if (scaling != t.scaling)
            return false;

        // if scaling is enabled, the grid must match to be equal
        if (scaling)
        {
            if (!scaleGridBottom.equals(t.scaleGridBottom) ||
                !scaleGridLeft.equals(t.scaleGridLeft) ||
                !scaleGridRight.equals(t.scaleGridRight) ||
                !scaleGridTop.equals(t.scaleGridTop))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int hashCode = super.hashCode();

        hashCode += (scaling ? 1 : 0);

        if (scaleGridBottom != null)
            hashCode ^= scaleGridBottom.hashCode();

        if (scaleGridLeft != null)
            hashCode ^= scaleGridLeft.hashCode();

        if (scaleGridRight != null)
            hashCode ^= scaleGridRight.hashCode();

        if (scaleGridTop != null)
            hashCode ^= scaleGridTop.hashCode();

        return hashCode;
    }
}
