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

package org.apache.royale.compiler.internal.fxg.dom.filters;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

import java.util.Collection;

import org.apache.royale.compiler.internal.fxg.dom.DOMParserHelper;
import org.apache.royale.compiler.internal.fxg.dom.types.BevelType;
import org.apache.royale.compiler.problems.ICompilerProblem;

public class BevelFilterNode extends AbstractFilterNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public double blurX = 4.0;
    public double blurY = 4.0;
    public int quality = 1;
    public double angle = 45.0;
    public double distance = 4.0;
    public double highlightAlpha = 1.0;
    public int highlightColor = COLOR_WHITE;
    public boolean knockout = false;
    public double shadowAlpha = 1.0;
    public int shadowColor = COLOR_BLACK;
    public double strength = 1.0;
    public BevelType type = BevelType.INNER;

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * @return The unqualified name of a BevelFilter node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_BEVELFILTER_ELEMENT;
    }

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_BLURX_ATTRIBUTE.equals(name))
            blurX = DOMParserHelper.parseDouble(this, value, name, blurX, problems);
        else if (FXG_BLURY_ATTRIBUTE.equals(name))
            blurY = DOMParserHelper.parseDouble(this, value, name, blurY, problems);
        else if (FXG_QUALITY_ATTRIBUTE.equals(name))
            quality = DOMParserHelper.parseInt(this, value, name, QUALITY_MIN_INCLUSIVE, QUALITY_MAX_INCLUSIVE, quality, problems);
        else if (FXG_ANGLE_ATTRIBUTE.equals(name))
            angle = DOMParserHelper.parseDouble(this, value, name, angle, problems);
        else if (FXG_DISTANCE_ATTRIBUTE.equals(name))
            distance = DOMParserHelper.parseDouble(this, value, name, distance, problems);
        else if (FXG_HIGHLIGHTALPHA_ATTRIBUTE.equals(name))
            highlightAlpha = DOMParserHelper.parseDouble(this, value, name, ALPHA_MIN_INCLUSIVE, ALPHA_MAX_INCLUSIVE, highlightAlpha, problems);
        else if (FXG_HIGHLIGHTCOLOR_ATTRIBUTE.equals(name))
            highlightColor = DOMParserHelper.parseRGB(this, value, name, highlightColor, problems);
        else if (FXG_KNOCKOUT_ATTRIBUTE.equals(name))
            knockout = DOMParserHelper.parseBoolean(this, value, name, knockout, problems);
        else if (FXG_SHADOWALPHA_ATTRIBUTE.equals(name))
            shadowAlpha = DOMParserHelper.parseDouble(this, value, name, ALPHA_MIN_INCLUSIVE, ALPHA_MAX_INCLUSIVE, shadowAlpha, problems);
        else if (FXG_SHADOWCOLOR_ATTRIBUTE.equals(name))
            shadowColor = DOMParserHelper.parseRGB(this, value, name, shadowColor, problems);
        else if (FXG_STRENGTH_ATTRIBUTE.equals(name))
            strength = DOMParserHelper.parseDouble(this, value, name, strength, problems);
        else if (FXG_TYPE_ATTRIBUTE.equals(name))
            type = getBevelType(value, problems);
		else
			super.setAttribute(name, value, problems);
    }
}
