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

import org.apache.royale.compiler.fxg.FXGVersion;
import org.apache.royale.compiler.internal.fxg.dom.types.FillMode;
import org.apache.royale.compiler.problems.ICompilerProblem;

public class BitmapGraphicNode extends GraphicContentNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public double width = Double.NaN;
    public double height = Double.NaN;
    public String source;
    public boolean repeat = true;
    public FillMode fillMode = FillMode.SCALE;

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * @return The unqualified name of a BitmapGraphic node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
    	if (this.getFileVersion().equals(FXGVersion.v1_0) )
    		return FXG_BITMAPGRAPHIC_ELEMENT;
    	else
    		return FXG_BITMAPIMAGE_ELEMENT;
    }

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_WIDTH_ATTRIBUTE.equals(name))
            width = DOMParserHelper.parseDouble(this, value, name, width, problems);
        else if (FXG_HEIGHT_ATTRIBUTE.equals(name))
            height = DOMParserHelper.parseDouble(this, value, name, height, problems);
        else if (FXG_SOURCE_ATTRIBUTE.equals(name))
            source = value;
        else if ((getFileVersion().equalTo(FXGVersion.v1_0)) && (FXG_REPEAT_ATTRIBUTE.equals(name)))
            repeat = DOMParserHelper.parseBoolean(this, value, name, repeat, problems);
        else if (!(getFileVersion().equalTo(FXGVersion.v1_0)) && (FXG_FILLMODE_ATTRIBUTE.equals(name)))
            fillMode = DOMParserHelper.parseFillMode(this, value, fillMode, problems);
        else
            super.setAttribute(name, value, problems);
    }
    
}
