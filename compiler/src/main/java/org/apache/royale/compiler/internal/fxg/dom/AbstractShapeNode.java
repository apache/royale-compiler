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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.types.LineStyle;
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.ShapeRecord;

/**
 * A base class for all FXG nodes that represent a stroke.
 */
public abstract class AbstractShapeNode extends GraphicContentNode
{
    //--------------------------------------------------------------------------
    //
    // Children
    //
    //--------------------------------------------------------------------------

    public IFillNode fill;
    public IStrokeNode stroke;

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof IFillNode)
            fill = (IFillNode)child;
        else if (child instanceof IStrokeNode)
            stroke = (IStrokeNode)child;
        else
            super.addChild(child, problems);
    }
    
    @Override
    public List<IFXGNode> getChildren()
    {
        List<IFXGNode> children = new ArrayList<IFXGNode>();
        children.addAll(super.getChildren());
        children.add(fill);
        children.add(stroke);
        
        return children;
    }
    
    /**
     * Returns the bounds of the shapes
     * Default implementation - to be overridden by individual classes
     */
    public Rect getBounds(List<ShapeRecord> records, LineStyle ls)
    {
    	return null;
    }
}
