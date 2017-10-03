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

import static org.apache.royale.compiler.fxg.FXGConstants.FXG_DATA_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_PATH_ELEMENT;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_WINDING_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_WINDING_EVENODD_VALUE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_WINDING_NONZERO_VALUE;

import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.internal.fxg.dom.strokes.AbstractStrokeNode;
import org.apache.royale.compiler.internal.fxg.dom.types.Winding;
import org.apache.royale.compiler.internal.fxg.swf.ShapeHelper;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.types.LineStyle;
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.ShapeRecord;

public class PathNode extends AbstractShapeNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public Winding winding = Winding.EVEN_ODD;
    public String data = "";

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_WINDING_ATTRIBUTE.equals(name))
            winding = getWinding(value, problems);
        else if (FXG_DATA_ATTRIBUTE.equals(name))
            data = value;
        else
            super.setAttribute(name, value, problems);
    }

    /**
     * @return The unqualified name of a Path node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_PATH_ELEMENT;
    }

    //--------------------------------------------------------------------------
    //
    // Helper Methods
    //
    //--------------------------------------------------------------------------
    
    protected Winding getWinding(String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_WINDING_EVENODD_VALUE.equals(value))
            return Winding.EVEN_ODD;
        else if (FXG_WINDING_NONZERO_VALUE.equals(value))
            return Winding.NON_ZERO;
        
        //Unknown winding value: {0}.
        problems.add(new FXGUnknownAttributeValueProblem(getDocumentPath(), getStartLine(), 
                getStartColumn(), FXG_WINDING_ATTRIBUTE, value));
        
        return winding;
    }
    
    /**
     * Returns the bounds of the path
     */
    @Override
    public Rect getBounds(List<ShapeRecord> records, LineStyle ls)
    {
    	return ShapeHelper.getBounds(records, ls, (AbstractStrokeNode)stroke);
    }
}
