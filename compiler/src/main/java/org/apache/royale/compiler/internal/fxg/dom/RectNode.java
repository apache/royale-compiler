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

import static org.apache.royale.compiler.fxg.FXGConstants.*;

import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.problems.FXGInvalidRectRadiusXRadiusYAttributeProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.ISWFConstants;
import org.apache.royale.swf.types.LineStyle;
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.ShapeRecord;

public class RectNode extends AbstractShapeNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public double width = 0.0;
    public double height = 0.0;
    public double radiusX = 0.0;
    public double radiusY = 0.0;
    public double topLeftRadiusX = Double.NaN;
    public double topLeftRadiusY = Double.NaN;
    public double topRightRadiusY = Double.NaN;
    public double topRightRadiusX = Double.NaN;
    public double bottomRightRadiusX = Double.NaN;
    public double bottomRightRadiusY = Double.NaN;
    public double bottomLeftRadiusX = Double.NaN;
    public double bottomLeftRadiusY = Double.NaN;    

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * @return The unqualified name of a Rect node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_RECT_ELEMENT;
    }

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_WIDTH_ATTRIBUTE.equals(name))
        {
            width = DOMParserHelper.parseDouble(this, value, name, width, problems);
        }
        else if (FXG_HEIGHT_ATTRIBUTE.equals(name))
        {
            height = DOMParserHelper.parseDouble(this, value, name, height, problems);
        }
        else if (FXG_RADIUSX_ATTRIBUTE.equals(name))
        {
            radiusX = DOMParserHelper.parseDouble(this, value, name, -1, problems);
            if (radiusX < 0) 
                // RadiusX, RadiusY, TopLeftRadiusX, TopLeftRadiusY, 
            	// TopRightRadiusX, TopRightRadiusY, BottomRightRadiusX, 
            	// BottomRightRadiusY, BottomLeftRadiusX, BottomLeftRadiusX 
            	// must be greater than 0.
                problems.add(new FXGInvalidRectRadiusXRadiusYAttributeProblem(getDocumentPath(), 
                        getStartLine(), getStartColumn()));
        }
        else if (FXG_RADIUSY_ATTRIBUTE.equals(name))
        {
            radiusY = DOMParserHelper.parseDouble(this, value, name, -1, problems);
            if (radiusY < 0)
                // RadiusX, RadiusY, TopLeftRadiusX, TopLeftRadiusY, 
            	// TopRightRadiusX, TopRightRadiusY, BottomRightRadiusX, 
            	// BottomRightRadiusY, BottomLeftRadiusX, BottomLeftRadiusX 
            	// must be greater than 0.
                problems.add(new FXGInvalidRectRadiusXRadiusYAttributeProblem(getDocumentPath(), 
                        getStartLine(), getStartColumn()));
        }
        else if (FXG_TOPLEFTRADIUSX_ATTRIBUTE.equals(name))
        {
        	topLeftRadiusX = DOMParserHelper.parseDouble(this, value, name, -1, problems);
            if (topLeftRadiusX < 0) 
                // RadiusX and RadiusY must be greater than 0.
                problems.add(new FXGInvalidRectRadiusXRadiusYAttributeProblem(getDocumentPath(), 
                        getStartLine(), getStartColumn()));
        }
        else if (FXG_TOPLEFTRADIUSY_ATTRIBUTE.equals(name))
        {
        	topLeftRadiusY = DOMParserHelper.parseDouble(this, value, name, -1, problems);
            if (topLeftRadiusY < 0)
                // RadiusX and RadiusY must be greater than 0.
                problems.add(new FXGInvalidRectRadiusXRadiusYAttributeProblem(getDocumentPath(), 
                        getStartLine(), getStartColumn()));
        }
        else if (FXG_TOPRIGHTRADIUSX_ATTRIBUTE.equals(name))
        {
        	topRightRadiusX = DOMParserHelper.parseDouble(this, value, name, -1, problems);
            if (topRightRadiusX < 0) 
                // RadiusX and RadiusY must be greater than 0.
                problems.add(new FXGInvalidRectRadiusXRadiusYAttributeProblem(getDocumentPath(), 
                        getStartLine(), getStartColumn()));
        }
        else if (FXG_TOPRIGHTRADIUSY_ATTRIBUTE.equals(name))
        {
        	topRightRadiusY = DOMParserHelper.parseDouble(this, value, name, -1, problems);
            if (topRightRadiusY < 0)
                // RadiusX and RadiusY must be greater than 0.
                problems.add(new FXGInvalidRectRadiusXRadiusYAttributeProblem(getDocumentPath(), 
                        getStartLine(), getStartColumn()));
        }
        else if (FXG_BOTTOMLEFTRADIUSX_ATTRIBUTE.equals(name))
        {
        	bottomLeftRadiusX = DOMParserHelper.parseDouble(this, value, name, -1, problems);
            if (bottomLeftRadiusX < 0) 
                // RadiusX and RadiusY must be greater than 0.
                problems.add(new FXGInvalidRectRadiusXRadiusYAttributeProblem(getDocumentPath(), 
                        getStartLine(), getStartColumn()));
        }
        else if (FXG_BOTTOMLEFTRADIUSY_ATTRIBUTE.equals(name))
        {
        	bottomLeftRadiusY = DOMParserHelper.parseDouble(this, value, name, -1, problems);
            if (bottomLeftRadiusY < 0)
                // RadiusX and RadiusY must be greater than 0.
                problems.add(new FXGInvalidRectRadiusXRadiusYAttributeProblem(getDocumentPath(), 
                        getStartLine(), getStartColumn()));
        }
        else if (FXG_BOTTOMRIGHTRADIUSX_ATTRIBUTE.equals(name))
        {
        	bottomRightRadiusX = DOMParserHelper.parseDouble(this, value, name, -1, problems);
            if (bottomRightRadiusX < 0) 
                // RadiusX and RadiusY must be greater than 0.
                problems.add(new FXGInvalidRectRadiusXRadiusYAttributeProblem(getDocumentPath(), 
                        getStartLine(), getStartColumn()));
        }
        else if (FXG_BOTTOMRIGHTRADIUSY_ATTRIBUTE.equals(name))
        {
            bottomRightRadiusY = DOMParserHelper.parseDouble(this, value, name, -1, problems);
            if (bottomRightRadiusY < 0)
                // RadiusX and RadiusY must be greater than 0.
                problems.add(new FXGInvalidRectRadiusXRadiusYAttributeProblem(getDocumentPath(), 
                        getStartLine(), getStartColumn()));
        }
        else
        {
            super.setAttribute(name, value, problems);
        }
    }
    
    /**
     * Returns the bounds of the rect
     */
    @Override
    public Rect getBounds(List<ShapeRecord> records, LineStyle ls)
    {
        int x1 = 0;
        int y1 = 0;
        int x2 = (int) (width*ISWFConstants.TWIPS_PER_PIXEL);
        int y2 = (int) (height*ISWFConstants.TWIPS_PER_PIXEL);
        if (ls != null)
        {
            int width = ISWFConstants.TWIPS_PER_PIXEL;
            if (width < ls.getWidth())
            	width = ls.getWidth();
            int stroke = (int)Math.rint(width / 2.0);
            x1 = x1 - stroke;
            y1 = y1 - stroke;
            x2 = x2 + stroke;
            y2 = y2 + stroke;
        }

    	return new Rect(x1, x2, y1, y2);
    }
}
