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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.fxg.logging.FXGLog;
import org.apache.royale.compiler.fxg.logging.IFXGLogger;
import org.apache.royale.compiler.internal.fxg.dom.DOMParserHelper;
import org.apache.royale.compiler.internal.fxg.dom.GradientEntryNode;
import org.apache.royale.compiler.internal.fxg.dom.types.BevelType;
import org.apache.royale.compiler.problems.ICompilerProblem;

public class GradientBevelFilterNode extends AbstractFilterNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public double angle = 45.0;
    public double blurX = 4.0;
    public double blurY = 4.0;
    public int quality = 1;
    public double distance = 4.0;
    public boolean knockout = false;
    public double strength = 1.0;
    public BevelType type = BevelType.INNER;

    //--------------------------------------------------------------------------
    //
    // Children
    //
    //--------------------------------------------------------------------------

    public List<GradientEntryNode> entries;

    @Override
    public List<IFXGNode> getChildren()
    {
        List<IFXGNode> children = new ArrayList<IFXGNode>();
        children.addAll(super.getChildren());
        if(entries != null)
            children.addAll(entries);
        return children;
    }

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof GradientEntryNode)
        {
            if (entries == null)
            {
                entries = new ArrayList<GradientEntryNode>(4);
            }
            else if (entries.size() >= GRADIENT_ENTRIES_MAX_INCLUSIVE)
            {
            	//Log warning:A GradientBevelFilter cannot define more than 15 GradientEntry elements - extra elements ignored.
                FXGLog.getLogger().log(IFXGLogger.WARN, "InvalidGradientBevelFilterNumElements", null, getDocumentPath(), startLine, startColumn);
                return;
            }

            entries.add((GradientEntryNode)child);
        }
        else
        {
            super.addChild(child, problems);
        }
    }

    /**
     * @return The unqualified name of a GradientBevelFilter node, without tag
     * markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_GRADIENTBEVELFILTER_ELEMENT;
    }

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_ANGLE_ATTRIBUTE.equals(name))
            angle = DOMParserHelper.parseDouble(this, value, name, angle, problems);
        else if (FXG_BLURX_ATTRIBUTE.equals(name))
            blurX = DOMParserHelper.parseDouble(this, value, name, blurX, problems);
        else if (FXG_BLURY_ATTRIBUTE.equals(name))
            blurY = DOMParserHelper.parseDouble(this, value, name, blurY, problems);
        else if (FXG_QUALITY_ATTRIBUTE.equals(name))
            quality = DOMParserHelper.parseInt(this, value, name, QUALITY_MIN_INCLUSIVE, QUALITY_MAX_INCLUSIVE, quality, problems);
        else if (FXG_DISTANCE_ATTRIBUTE.equals(name))
            distance = DOMParserHelper.parseDouble(this, value, name, distance, problems);
        else if (FXG_KNOCKOUT_ATTRIBUTE.equals(name))
            knockout = DOMParserHelper.parseBoolean(this, value, name, knockout, problems);
        else if (FXG_STRENGTH_ATTRIBUTE.equals(name))
            strength = DOMParserHelper.parseDouble(this, value, name, strength, problems);
        else if (FXG_TYPE_ATTRIBUTE.equals(name))
            type = getBevelType(value, problems);
        else
            super.setAttribute(name, value, problems);
    }
}
