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

package org.apache.royale.compiler.internal.fxg.dom;


import java.util.List;

import org.apache.royale.compiler.internal.fxg.dom.transforms.ColorTransformNode;
import org.apache.royale.compiler.internal.fxg.dom.types.BlendMode;
import org.apache.royale.compiler.internal.fxg.dom.types.MaskType;
import org.apache.royale.compiler.internal.fxg.dom.types.ScalingGrid;
import org.apache.royale.compiler.internal.fxg.types.FXGMatrix;

/**
 * A simple context holding inheritable graphic transformation information to be
 * used for placing a symbol on stage.
 */
public class GraphicContext implements Cloneable
{
    private FXGMatrix transform;

    public GraphicContext()
    {
    }

    public BlendMode blendMode;
    public MaskType maskType;
    public List<IFilterNode> filters;
    public ColorTransformNode colorTransform;
    public ScalingGrid scalingGrid;

    public FXGMatrix getTransform()
    {
        if (transform == null)
            transform = new FXGMatrix();

        return transform;
    }

    public void setTransform(FXGMatrix matrix)
    {
    	transform = matrix;
    }
    
    public void addFilters(List<IFilterNode> list)
    {
        if (filters == null)
            filters = list;
        else
            filters.addAll(list);
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        GraphicContext copy = (GraphicContext)super.clone();
        copy.transform = null;
        if (colorTransform != null)
            copy.colorTransform = (ColorTransformNode)colorTransform.clone();
        copy.maskType = maskType;
        copy.blendMode = blendMode;
        copy.scalingGrid = scalingGrid;
        return copy;
    }
}
