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

import org.apache.royale.compiler.internal.fxg.dom.strokes.AbstractStrokeNode;
import org.apache.royale.compiler.internal.fxg.swf.ShapeHelper;
import org.apache.royale.compiler.problems.ICompilerProblem;

import org.apache.royale.swf.types.LineStyle;
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.ShapeRecord;

public class LineNode extends AbstractShapeNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public double xFrom = 0.0;
    public double yFrom = 0.0;
    public double xTo = 0.0;
    public double yTo = 0.0;

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * @return The unqualified name of a Line node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_LINE_ELEMENT;
    }

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_XFROM_ATTRIBUTE.equals(name))
            xFrom = DOMParserHelper.parseDouble(this, value, name, xFrom, problems);
        else if (FXG_YFROM_ATTRIBUTE.equals(name))
            yFrom = DOMParserHelper.parseDouble(this, value, name, yFrom, problems);
        else if (FXG_XTO_ATTRIBUTE.equals(name))
            xTo = DOMParserHelper.parseDouble(this, value, name, xTo, problems);
        else if (FXG_YTO_ATTRIBUTE.equals(name))
            yTo = DOMParserHelper.parseDouble(this, value, name, yTo, problems);
        else
            super.setAttribute(name, value, problems);
    }
    
    /**
     * Returns the bounds of the line
     */
    @Override
    public Rect getBounds(List<ShapeRecord> records, LineStyle ls)
    {
    	return ShapeHelper.getBounds(records, ls, (AbstractStrokeNode)stroke);
    }
}
