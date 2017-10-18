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

package org.apache.royale.compiler.internal.fxg.dom.fills;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.fxg.FXGVersion;
import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.DOMParserHelper;
import org.apache.royale.compiler.internal.fxg.dom.transforms.MatrixNode;
import org.apache.royale.compiler.internal.fxg.dom.types.FillMode;
import org.apache.royale.compiler.problems.ICompilerProblem;

public class BitmapFillNode extends AbstractFillNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public double x = Double.NaN;
    public double y = Double.NaN;
    public boolean repeat = true;
    public double rotation = 0.0;
    public double scaleX = Double.NaN;
    public double scaleY = Double.NaN;
    public String source;
    public FillMode fillMode = FillMode.SCALE;

    //--------------------------------------------------------------------------
    //
    // Children
    //
    //--------------------------------------------------------------------------

    public MatrixNode matrix;

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof MatrixNode)
            matrix = (MatrixNode)child;
        else
            super.addChild(child, problems);
    }
    
    @Override
    public List<IFXGNode> getChildren()
    {
        List<IFXGNode> children = new ArrayList<IFXGNode>();
        children.addAll(super.getChildren());
        children.add(matrix);
        return children;
    }

    /**
     * @return The unqualified name of a BitmapFill node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_BITMAPFILL_ELEMENT;
    }

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_X_ATTRIBUTE.equals(name))
            x = DOMParserHelper.parseDouble(this, value, name, x, problems);
        else if (FXG_Y_ATTRIBUTE.equals(name))
            y = DOMParserHelper.parseDouble(this, value, name, y, problems);
        else if ((getFileVersion().equalTo(FXGVersion.v1_0)) && (FXG_REPEAT_ATTRIBUTE.equals(name)))
            repeat = DOMParserHelper.parseBoolean(this, value, name, repeat, problems);
        else if (FXG_ROTATION_ATTRIBUTE.equals(name))
            rotation = DOMParserHelper.parseDouble(this, value, name, rotation, problems);
        else if (FXG_SCALEX_ATTRIBUTE.equals(name))
            scaleX = DOMParserHelper.parseDouble(this, value, name, scaleX, problems);
        else if (FXG_SCALEY_ATTRIBUTE.equals(name))
            scaleY = DOMParserHelper.parseDouble(this, value, name, scaleY, problems);
        else if (FXG_SOURCE_ATTRIBUTE.equals(name))
            source = value;
        else if (!(getFileVersion().equalTo(FXGVersion.v1_0)) && (FXG_FILLMODE_ATTRIBUTE.equals(name)))
            fillMode = DOMParserHelper.parseFillMode(this, value, fillMode, problems);
        else
            super.setAttribute(name, value, problems);
    }
    
}
