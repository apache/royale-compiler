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

package org.apache.royale.compiler.internal.fxg.dom.strokes;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

import java.util.Collection;

import org.apache.royale.compiler.fxg.FXGVersion;
import org.apache.royale.compiler.internal.fxg.dom.AbstractFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.DOMParserHelper;
import org.apache.royale.compiler.internal.fxg.dom.GraphicNode;
import org.apache.royale.compiler.internal.fxg.dom.IStrokeNode;
import org.apache.royale.compiler.internal.fxg.dom.types.Caps;
import org.apache.royale.compiler.internal.fxg.dom.types.Joints;
import org.apache.royale.compiler.internal.fxg.dom.types.ScaleMode;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Base class for all FXG stroke nodes.
 */
public abstract class AbstractStrokeNode extends AbstractFXGNode implements IStrokeNode
{
    protected static final double MITERLIMIT_MIN_INCLUSIVE = 1.0;
    protected static final double MITERLIMIT_MAX_INCLUSIVE = 255.0;
    protected static final double WEIGHT_MIN_INCLUSIVE = 0.0;
    protected static final double WEIGHT_MAX_INCLUSIVE = 255.0;

    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    //------------
    // id
    //------------

    protected String id;

    /**
     * An id attribute provides a well defined name to a content node.
     */
    @Override
    public String getId()
    {
        return id;
    }

    /**
     * Sets the node id.
     * @param value - the node id as a String.
     */
    @Override
    public void setId(String value)
    {
        id = value;
    }

    public ScaleMode scaleMode = ScaleMode.NORMAL;
    public Caps caps = Caps.ROUND;
    public boolean pixelHinting = false;
    public Joints joints = Joints.ROUND;
    public double miterLimit = 3.0;
    
    private double weight = Double.NaN;
    protected double weight_v_1 = 0.0;
    protected double weight_v_1_later = 1.0;
    
    /**
     * Stroke weight. Default value is FXG Version specific.
     * FXG 1.0 - default "0.0"
     * FXG 2.0 - default "2.0"
     */
    public double getWeight()
    {
    	if (Double.isNaN(weight))
    	{
        	if (((GraphicNode)this.getDocumentNode()).getVersion().equals(FXGVersion.v1_0))
        		weight = weight_v_1;       
        	else
        		weight = weight_v_1_later;
    	}
    	return weight;
    }
    
    /**
     * Get scaleX. 
     * @return Double.NaN as default.
     */
    public double getScaleX()
    {
        return Double.NaN;
    }

    /**
     * Get scaleY. 
     * @return Double.NaN as default.
     */
    public double getScaleY()
    {
        return Double.NaN;
    }
    
    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * Sets an FXG attribute on this stroke node.
     * 
     * @param name - the unqualified attribute name.
     * @param value - the attribute value.
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_SCALEMODE_ATTRIBUTE.equals(name))
            scaleMode = getScaleMode(value, problems);
        else if (FXG_CAPS_ATTRIBUTE.equals(name))
            caps = getCaps(value, problems);
        else if (FXG_WEIGHT_ATTRIBUTE.equals(name))
            weight = DOMParserHelper.parseDouble(this, value, name, WEIGHT_MIN_INCLUSIVE, WEIGHT_MAX_INCLUSIVE, weight, problems);
        else if (FXG_PIXELHINTING_ATTRIBUTE.equals(name))
            pixelHinting = DOMParserHelper.parseBoolean(this, value, name, pixelHinting, problems);
        else if (FXG_JOINTS_ATTRIBUTE.equals(name))
            joints = getJoints(value, problems);
        else if (FXG_MITERLIMIT_ATTRIBUTE.equals(name))
            miterLimit = DOMParserHelper.parseDouble(this, value, name, MITERLIMIT_MIN_INCLUSIVE, MITERLIMIT_MAX_INCLUSIVE, miterLimit, problems);
        else if (FXG_ID_ATTRIBUTE.equals(name))
            id = value;
        else
            super.setAttribute(name, value, problems);
    }

    //--------------------------------------------------------------------------
    //
    // Helper Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Convert an FXG String value to a Caps enumeration.
     * 
     * @param value - the FXG String value.
     * @return the matching Caps type.
     */
    protected Caps getCaps(String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_CAPS_ROUND_VALUE.equals(value))
            return Caps.ROUND;
        else if (FXG_CAPS_SQUARE_VALUE.equals(value))
            return Caps.SQUARE;
        else if (FXG_CAPS_NONE_VALUE.equals(value))
            return Caps.NONE;
        
        //string did not match a known caps type.
        problems.add(new FXGUnknownAttributeValueProblem(getDocumentPath(), getStartLine(), 
                getStartColumn(), FXG_CAPS_ATTRIBUTE, value));
        return caps;
    }

    /**
     * Convert an FXG String value to a Joints enumeration.
     * 
     * @param value - the FXG String value.
     * @return the matching Joints type.
     */
    protected Joints getJoints(String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_JOINTS_ROUND_VALUE.equals(value))
            return Joints.ROUND;
        if (FXG_JOINTS_MITER_VALUE.equals(value))
            return Joints.MITER;
        if (FXG_JOINTS_BEVEL_VALUE.equals(value))
            return Joints.BEVEL;
        
        //string did not match a known Joints type.
        problems.add(new FXGUnknownAttributeValueProblem(getDocumentPath(), getStartLine(), 
                getStartColumn(), FXG_JOINTS_ATTRIBUTE, value));
        return joints;
    }

    /**
     * Convert an FXG String value to a ScaleMode enumeration.
     * 
     * @param value - the FXG String value.
     * @return the matching ScaleMode.
     */
    protected ScaleMode getScaleMode(String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_SCALEMODE_NONE_VALUE.equals(value))
            return ScaleMode.NONE;
        else if (FXG_SCALEMODE_VERTICAL_VALUE.equals(value))
            return ScaleMode.VERTICAL;
        else if (FXG_SCALEMODE_NORMAL_VALUE.equals(value))
            return ScaleMode.NORMAL;
        else if (FXG_SCALEMODE_HORIZONTAL_VALUE.equals(value))
            return ScaleMode.HORIZONTAL;
        
        //string did not match a known Joints type.
        problems.add(new FXGUnknownAttributeValueProblem(getDocumentPath(), getStartLine(), 
                getStartColumn(), FXG_SCALEMODE_ATTRIBUTE, value));
        return scaleMode;
    }
}
