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
import org.apache.royale.compiler.internal.fxg.dom.types.ScalingGrid;
import org.apache.royale.compiler.problems.FXGInvalidScaleGridGroupChildProblem;
import org.apache.royale.compiler.problems.FXGInvalidScaleGridRotationAttributeProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

public class GroupNode extends GraphicContentNode implements IMaskingNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public double scaleGridLeft = 0.0;
    public double scaleGridRight = 0.0;
    public double scaleGridTop = 0.0;
    public double scaleGridBottom = 0.0;

    //--------------------------------------------------------------------------
    //
    // Children
    //
    //--------------------------------------------------------------------------

    public List<GraphicContentNode> children;

    @Override
    public List<IFXGNode> getChildren()
    {
        List<IFXGNode> childrenRes = new ArrayList<IFXGNode>();
        childrenRes.addAll(super.getChildren());
        if(children != null)
            childrenRes.addAll(children);    
        return childrenRes;
    }
    
    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * Adds an FXG child node to this Group node.
     * 
     * @param child - a child FXG node to be added to this node.
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof GraphicContentNode)
        {
            if (children == null)
                children = new ArrayList<GraphicContentNode>();

            GraphicContentNode graphicContent = (GraphicContentNode)child;
            graphicContent.setParentGraphicContext(createGraphicContext());

            if (child instanceof GroupNode)
            {
                if (isInsideScaleGrid())
                {
                    // A child Group cannot exist in a Group that
                    // defines the scale grid
                    problems.add(new FXGInvalidScaleGridGroupChildProblem(getDocumentPath(), child.getStartLine(), child.getStartColumn()));
                    return;
                }
            }

            children.add(graphicContent);
        }
        else
        {
            super.addChild(child, problems);
        }
    }

    /**
     * @return The unqualified name of a Group node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_GROUP_ELEMENT;
    }

    /**
     * Sets an FXG attribute on this Group node.
     * 
     * @param name - the unqualified attribute name
     * @param value - the attribute value
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_SCALEGRIDLEFT_ATTRIBUTE.equals(name))
        {
            scaleGridLeft = DOMParserHelper.parseDouble(this, value, name, scaleGridLeft, problems);
            definesScaleGrid = true;
        }
        else if (FXG_SCALEGRIDTOP_ATTRIBUTE.equals(name))
        {
            scaleGridTop = DOMParserHelper.parseDouble(this, value, name, scaleGridTop, problems);
            definesScaleGrid = true;
        }
        else if (FXG_SCALEGRIDRIGHT_ATTRIBUTE.equals(name))
        {
            scaleGridRight = DOMParserHelper.parseDouble(this, value, name, scaleGridRight, problems);
            definesScaleGrid = true;
        }
        else if (FXG_SCALEGRIDBOTTOM_ATTRIBUTE.equals(name))
        {
            scaleGridBottom = DOMParserHelper.parseDouble(this, value, name, scaleGridBottom, problems);
            definesScaleGrid = true;
        }
        else
        {
            super.setAttribute(name, value, problems);
        }

        if ((definesScaleGrid) && (this.rotationSet))
        {
            problems.add(new FXGInvalidScaleGridRotationAttributeProblem(getDocumentPath(), getStartLine(), getStartColumn()));
            return;
        }

    }

    @Override
    public GraphicContext createGraphicContext()
    {
        GraphicContext context = super.createGraphicContext();

        if (definesScaleGrid())
        {
            ScalingGrid scalingGrid = new ScalingGrid();
            scalingGrid.scaleGridLeft = scaleGridLeft;
            scalingGrid.scaleGridTop = scaleGridTop;
            scalingGrid.scaleGridRight = scaleGridRight;
            scalingGrid.scaleGridBottom = scaleGridBottom;
            context.scalingGrid = scalingGrid;
        }

        return context;
    }

    public boolean definesScaleGrid()
    {
        return definesScaleGrid;
    }

    public boolean isInsideScaleGrid()
    {
        return insideScaleGrid || definesScaleGrid;
    }

    public void setInsideScaleGrid(boolean value)
    {
        insideScaleGrid = value;
    }

    private boolean definesScaleGrid;
    private boolean insideScaleGrid;

    //--------------------------------------------------------------------------
    //
    // IMaskingNode Implementation
    //
    //--------------------------------------------------------------------------

    private int maskIndex;

    /**
     * @return the index of a mask in a parent DisplayObject's list of children.
     * This can be used to access the mask programmatically at runtime.
     */
    @Override
    public int getMaskIndex()
    {
        return maskIndex;
    }

    /**
     * Records the index of this mask in the parent DisplayObject's list of
     * children. (Optional).
     * @param index - the child index to the mask  
     */
    @Override
    public void setMaskIndex(int index)
    {
        maskIndex = index;
    }
}
