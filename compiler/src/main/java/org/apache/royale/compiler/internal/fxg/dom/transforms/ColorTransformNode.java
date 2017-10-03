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

package org.apache.royale.compiler.internal.fxg.dom.transforms;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

import java.util.Collection;

import org.apache.royale.compiler.internal.fxg.dom.DOMParserHelper;
import org.apache.royale.compiler.problems.ICompilerProblem;

public class ColorTransformNode extends AbstractTransformNode implements Cloneable
{
    private static final double MIN_OFFSET_INCLUSIVE = -255.0;
    private static final double MAX_OFFSET_INCLUSIVE = 255.0;

    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public double alphaMultiplier = 1.0;
    public double redMultiplier = 1.0;
    public double blueMultiplier = 1.0;
    public double greenMultiplier = 1.0;
    public double alphaOffset = 0.0;
    public double redOffset = 0.0;
    public double blueOffset = 0.0;
    public double greenOffset = 0.0;

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * @return The unqualified name of a ColorTransform node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_COLORTRANSFORM_ELEMENT;
    }

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_ALPHAMULTIPLIER_ATTRIBUTE.equals(name))
            alphaMultiplier = DOMParserHelper.parseDouble(this, value, name, alphaMultiplier, problems);
        else if (FXG_REDMULTIPLIER_ATTRIBUTE.equals(name))
            redMultiplier = DOMParserHelper.parseDouble(this, value, name, redMultiplier, problems);
        else if (FXG_BLUEMULTIPLIER_ATTRIBUTE.equals(name))
            blueMultiplier = DOMParserHelper.parseDouble(this, value, name, blueMultiplier, problems);
        else if (FXG_GREENMULTIPLIER_ATTRIBUTE.equals(name))
            greenMultiplier = DOMParserHelper.parseDouble(this, value, name, greenMultiplier, problems);
        else if (FXG_ALPHAOFFSET_ATTRIBUTE.equals(name))
            alphaOffset = DOMParserHelper.parseDouble(this, value, name, MIN_OFFSET_INCLUSIVE, MAX_OFFSET_INCLUSIVE, alphaOffset, problems);
        else if (FXG_REDOFFSET_ATTRIBUTE.equals(name))
            redOffset = DOMParserHelper.parseDouble(this, value, name, MIN_OFFSET_INCLUSIVE, MAX_OFFSET_INCLUSIVE, redOffset, problems);
        else if (FXG_BLUEOFFSET_ATTRIBUTE.equals(name))
            blueOffset = DOMParserHelper.parseDouble(this, value, name, MIN_OFFSET_INCLUSIVE, MAX_OFFSET_INCLUSIVE, blueOffset, problems);
        else if (FXG_GREENOFFSET_ATTRIBUTE.equals(name))
            greenOffset = DOMParserHelper.parseDouble(this, value, name, MIN_OFFSET_INCLUSIVE, MAX_OFFSET_INCLUSIVE, greenOffset, problems);
    }

    //--------------------------------------------------------------------------
    //
    // Cloneable Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        ColorTransformNode copy = (ColorTransformNode)super.clone();
        copy.alphaMultiplier = alphaMultiplier;
        copy.redMultiplier = redMultiplier;
        copy.blueMultiplier = blueMultiplier;
        copy.greenMultiplier = greenMultiplier;
        copy.alphaOffset = alphaOffset;
        copy.redOffset = redOffset;
        copy.blueOffset = blueOffset;
        copy.greenOffset = greenOffset;
 
        return copy;
    }
}
