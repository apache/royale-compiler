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

import static org.apache.royale.compiler.fxg.FXGConstants.FXG_GROUP_ELEMENT;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_ID_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_SCALEGRIDBOTTOM_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_SCALEGRIDLEFT_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_SCALEGRIDRIGHT_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_SCALEGRIDTOP_ATTRIBUTE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.types.ScalingGrid;
import org.apache.royale.compiler.problems.FXGInvalidGroupIDAttributeProblem;
import org.apache.royale.compiler.problems.FXGInvalidScaleGridGroupChildProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * A GroupDefinition represents the special use of Group as the basis for an
 * FXG Library Definition. It acts as the base graphic context for a symbol
 * definition. A GroupDefinition differs from a Group instance in that it
 * cannot define a transform, filters or have an id attribute.
 */
public class GroupDefinitionNode extends AbstractFXGNode
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

    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof GraphicContentNode)
        {
            if (child instanceof TextGraphicNode)
                textCount++;

            if (children == null)
                children = new ArrayList<GraphicContentNode>();

            children.add((GraphicContentNode)child);
        
            if (child instanceof GroupNode)
            {
                if (definesScaleGrid())
                {
                    // A child Group cannot exist in a Group that
                    // defines the scale grid
                    problems.add(new FXGInvalidScaleGridGroupChildProblem(getDocumentPath(), child.getStartLine(), child.getStartColumn()));
                    return;
                }
            }
        }
        else
        {
            super.addChild(child, problems);
        }
    }

    @Override
    public String getNodeName()
    {
        return FXG_GROUP_ELEMENT;
    }

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
        else if (FXG_ID_ATTRIBUTE.equals(name))
        {
        	//The id attribute is not allowed on the Group child a Definition.
            problems.add(new FXGInvalidGroupIDAttributeProblem(getDocumentPath(), getStartLine(), getStartColumn()));
        }
        else
        {
            super.setAttribute(name, value, problems);
        }
    }

    //--------------------------------------------------------------------------
    //
    // Helper Methods
    //
    //--------------------------------------------------------------------------

    public ScalingGrid getScalingGrid()
    {
        ScalingGrid scalingGrid = null;

        if (definesScaleGrid())
        {
            scalingGrid = new ScalingGrid();
            scalingGrid.scaleGridLeft = scaleGridLeft;
            scalingGrid.scaleGridTop = scaleGridTop;
            scalingGrid.scaleGridRight = scaleGridRight;
            scalingGrid.scaleGridBottom = scaleGridBottom;
        }

        return scalingGrid;
    }

    public boolean definesScaleGrid()
    {
        return definesScaleGrid;
    }

    public int getTextCount()
    {
        return textCount;
    }

    private boolean definesScaleGrid;
    private int textCount;}
