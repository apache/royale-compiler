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

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.fxg.logging.FXGLog;
import org.apache.royale.compiler.fxg.logging.IFXGLogger;
import org.apache.royale.compiler.internal.fxg.dom.DOMParserHelper;
import org.apache.royale.compiler.internal.fxg.dom.GradientEntryNode;
import org.apache.royale.compiler.internal.fxg.dom.IScalableGradientNode;
import org.apache.royale.compiler.internal.fxg.dom.transforms.MatrixNode;
import org.apache.royale.compiler.internal.fxg.dom.types.InterpolationMethod;
import org.apache.royale.compiler.internal.fxg.dom.types.SpreadMethod;
import org.apache.royale.compiler.problems.FXGInvalidChildMatrixNodeProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

public class RadialGradientFillNode extends AbstractFillNode implements IScalableGradientNode
{
    private static final double FOCAL_MIN_INCLUSIVE = -1.0;
    private static final double FOCAL_MAX_INCLUSIVE = 1.0;

    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public double x = Double.NaN;
    public double y = Double.NaN;
    public double scaleX = Double.NaN;
    public double scaleY = Double.NaN;
    public double rotation = 0.0;
    public SpreadMethod spreadMethod = SpreadMethod.PAD;
    public InterpolationMethod interpolationMethod = InterpolationMethod.RGB;
    public double focalPointRatio = 0.0;

    private boolean translateSet;
    private boolean scaleSet;
    private boolean rotationSet;

    //--------------------------------------------------------------------------
    //
    // Children
    //
    //--------------------------------------------------------------------------

    public MatrixNode matrix;
    public List<GradientEntryNode> entries;

    @Override
    public List<IFXGNode> getChildren()
    {
        List<IFXGNode> children = new ArrayList<IFXGNode>();
        children.addAll(super.getChildren());
        children.add(matrix);
        if(entries != null)
            children.addAll(entries);
        return children;
    }

    //--------------------------------------------------------------------------
    //
    // IScalableGradientNode Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public double getX()
    {
        return x;
    }

    @Override
    public double getY()
    {
        return y;
    }

    @Override
    public double getScaleX()
    {
         return scaleX;
    }

    @Override
    public double getScaleY()
    {
        return scaleY;
    }

    @Override
    public double getRotation()
    {
        return rotation;
    }

    @Override
    public MatrixNode getMatrixNode()
    {
        return matrix;
    }

    @Override
    public boolean isLinear()
    {
        return false;
    }

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof MatrixNode)
        {
            if (translateSet || scaleSet || rotationSet)
            {
                //Cannot supply a matrix child if transformation attributes were provided.
                problems.add(new FXGInvalidChildMatrixNodeProblem(getDocumentPath(), child.getStartLine(), child.getStartColumn()));
                return;
            }

            matrix = (MatrixNode)child;
        }
        else if (child instanceof GradientEntryNode)
        {
            if (entries == null)
            {
                entries = new ArrayList<GradientEntryNode>(4);
            }
            else if (entries.size() >= GRADIENT_ENTRIES_MAX_INCLUSIVE)
            {
                //Log warning:A RadialGradient cannot define more than 15 GradientEntry elements - extra elements ignored.
                FXGLog.getLogger().log(IFXGLogger.WARN, "InvalidRadialGradientNumElements", null, getDocumentPath(), startLine, startColumn);
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
     * @return The unqualified name of a RadialGradient node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_RADIALGRADIENT_ELEMENT;
    }

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_X_ATTRIBUTE.equals(name))
        {
            x = DOMParserHelper.parseDouble(this, value, name, x, problems);
            translateSet = true;
        }
        else if (FXG_Y_ATTRIBUTE.equals(name))
        {
            y = DOMParserHelper.parseDouble(this, value, name, y, problems);
            translateSet = true;
        }
        else if (FXG_ROTATION_ATTRIBUTE.equals(name))
        {
            rotation = DOMParserHelper.parseDouble(this, value, name, rotation, problems);
            rotationSet = true;
        }
        else if (FXG_SCALEX_ATTRIBUTE.equals(name))
        {
            scaleX = DOMParserHelper.parseDouble(this, value, name, scaleX, problems);
            scaleSet = true;
        }
        else if (FXG_SCALEY_ATTRIBUTE.equals(name))
        {
            scaleY = DOMParserHelper.parseDouble(this, value, name, scaleY, problems);
            scaleSet = true;
        }
        else if (FXG_SPREADMETHOD_ATTRIBUTE.equals(name))
        {
            spreadMethod = DOMParserHelper.parseSpreadMethod(this, value, spreadMethod, problems);
        }
        else if (FXG_INTERPOLATIONMETHOD_ATTRIBUTE.equals(name))
        {
            interpolationMethod = DOMParserHelper.parseInterpolationMethod(this, value, interpolationMethod, problems);
        }
        else if (FXG_FOCALPOINTRATIO_ATTRIBUTE.equals(name))
        {
            focalPointRatio = DOMParserHelper.parseDouble(this, value, name, FOCAL_MIN_INCLUSIVE, FOCAL_MAX_INCLUSIVE, focalPointRatio, problems);
        }
        else
        {
            super.setAttribute(name, value, problems);
        }
    }
}
