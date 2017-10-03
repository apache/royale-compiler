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
 * Represents a <code>DefineShape4</code> tag in a SWF file.
 * <p>
 * {@code DefineShape4} extends the capabilities of {@link DefineShape3Tag} by
 * using a new line style record in the shape. {@code LINESTYLE2} allows new
 * types of joins and caps as well as scaling options and the ability to fill a
 * stroke.
 */
public class DefineShape4Tag extends DefineShape3Tag
{
    /**
     * Constructor.
     */
    public DefineShape4Tag()
    {
        super(TagType.DefineShape4);
    }

    private Rect edgeBounds;
    private boolean usesFillWindingRule;
    private boolean usesNonScalingStrokes;
    private boolean usesScalingStrokes;

    public Rect getEdgeBounds()
    {
        return edgeBounds;
    }

    public void setEdgeBounds(Rect edgeBounds)
    {
        this.edgeBounds = edgeBounds;
    }

    public boolean isUsesFillWindingRule()
    {
        return usesFillWindingRule;
    }

    public void setUsesFillWindingRule(boolean usesFillWindingRule)
    {
        this.usesFillWindingRule = usesFillWindingRule;
    }

    public boolean isUsesNonScalingStrokes()
    {
        return usesNonScalingStrokes;
    }

    public void setUsesNonScalingStrokes(boolean usesNonScalingStrokes)
    {
        this.usesNonScalingStrokes = usesNonScalingStrokes;
    }

    public boolean isUsesScalingStrokes()
    {
        return usesScalingStrokes;
    }

    public void setUsesScalingStrokes(boolean usesScalingStrokes)
    {
        this.usesScalingStrokes = usesScalingStrokes;
    }
}
