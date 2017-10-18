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

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.ISWFConstants;
import org.apache.royale.swf.types.LineStyle;
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.ShapeRecord;

public class EllipseNode extends AbstractShapeNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public double width = 0.0;
    public double height = 0.0;

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * @return The unqualified name of an Ellipse node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_ELLIPSE_ELEMENT;
    }

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_WIDTH_ATTRIBUTE.equals(name))
            width = DOMParserHelper.parseDouble(this, value, name, width, problems);
        else if (FXG_HEIGHT_ATTRIBUTE.equals(name))
            height = DOMParserHelper.parseDouble(this, value, name, height, problems);
        else
            super.setAttribute(name, value, problems);
    }
    
    /**
     * Returns the bounds of the ellipse
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
