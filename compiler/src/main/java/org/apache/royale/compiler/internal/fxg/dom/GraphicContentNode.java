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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.fxg.FXGVersion;
import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.fxg.logging.FXGLog;
import org.apache.royale.compiler.fxg.logging.IFXGLogger;
import org.apache.royale.compiler.internal.fxg.dom.transforms.ColorTransformNode;
import org.apache.royale.compiler.internal.fxg.dom.transforms.MatrixNode;
import org.apache.royale.compiler.internal.fxg.dom.types.BlendMode;
import org.apache.royale.compiler.internal.fxg.dom.types.MaskType;
import org.apache.royale.compiler.internal.fxg.types.FXGMatrix;
import org.apache.royale.compiler.problems.FXGInvalidChildColorTransformNodeProblem;
import org.apache.royale.compiler.problems.FXGInvalidChildMatrixNodeProblem;
import org.apache.royale.compiler.problems.FXGInvalidNodeAttributeProblem;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Base class for all nodes that present graphic content or represent groups
 * of graphic content. Children inherit parent context information for
 * transforms, blend modes and masks.
 */
public abstract class GraphicContentNode extends AbstractFXGNode
        implements IMaskableNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    //------------
    // id
    //------------

    protected String id = "undefined";

    /**
     * An id attribute provides a well defined name to a text node.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets the node id.
     * @param value - the node id as a String.
     */
    public void setId(String value)
    {
        id = value;
    }

    public boolean visible = true;

    public double x = 0.0;
    public double y = 0.0;
    public double scaleX = 1.0;
    public double scaleY = 1.0;
    public double rotation = 0.0;
    public double alpha = 1.0;
    public BlendMode blendMode = BlendMode.AUTO;
    public MaskType maskType = MaskType.CLIP;
    public boolean luminosityClip = false;
    public boolean luminosityInvert = false;

    protected boolean translateSet;
    protected boolean scaleSet;
    protected boolean rotationSet;
    protected boolean alphaSet;
    protected boolean maskTypeSet;

    //is part of clip mask
    public boolean isPartofClipMask = false;

    //--------------------------------------------------------------------------
    //
    // Children
    //
    //--------------------------------------------------------------------------

    public List<IFilterNode> filters;
    public IMaskingNode mask;
    public MatrixNode matrix;
    public ColorTransformNode colorTransform;

    @Override
    public List<IFXGNode> getChildren()
    {
        List<IFXGNode> children = new ArrayList<IFXGNode>();
        children.addAll(super.getChildren());
        if(filters != null)
            children.addAll(filters);
        children.add(mask);
        children.add(matrix);
        children.add(colorTransform);
        
        return children;
    }
    
    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * Adds an FXG child node to this node.
     * <p>
     * Graphic content nodes support child property nodes &lt;filter&gt;,
     * &lt;mask&gt;, &lt;matrix&gt;, or &lt;colorTransform&gt;.
     * </p>
     * 
     * @param child - a child FXG node to be added to this node.
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof IFilterNode)
        {
            if (filters == null)
                filters = new ArrayList<IFilterNode>();

            filters.add((IFilterNode)child);
        }
        else if (child instanceof MaskPropertyNode)
        {
            mask = ((MaskPropertyNode)child).mask;
            if (mask instanceof GraphicContentNode)
            {
                ((GraphicContentNode)mask).setParentGraphicContext(createGraphicContext());
            }
        }
        else if (child instanceof MatrixNode)
        {
            if (translateSet || scaleSet || rotationSet)
            {
            	//Cannot supply a matrix child if transformation attributes were provided
                problems.add(new FXGInvalidChildMatrixNodeProblem(getDocumentPath(), child.getStartLine(), child.getStartColumn()));
                return;
            }

            matrix = (MatrixNode)child;
        }
        else if (child instanceof ColorTransformNode)
        {
            if (alphaSet)
            {
            	//Cannot supply a colorTransform child if alpha attribute was provided.
                problems.add(new FXGInvalidChildColorTransformNodeProblem(getDocumentPath(), child.getStartLine(), child.getStartColumn()));
                return;
            }

            colorTransform = (ColorTransformNode)child;
        }
        else
        {
            super.addChild(child, problems);
        }
    }

    /**
     * Sets an FXG attribute on this FXG node.
     * <p>
     * Graphic content nodes support the following attributes:
     * <ul>
     * <li><b>rotation</b> (ASDegrees): Defaults to 0.</li>
     * <li><b>scaleX</b> (Number): Defaults to 1.</li>
     * <li><b>scaleY</b> (Number): Defaults to 1.</li>
     * <li><b>x</b> (Number): The horizontal placement of the left edge of the
     * text box, relative to the parent grouping element. Defaults to 0.</li>
     * <li><b>y</b> (Number): The vertical placement of the top edge of the
     * text box, relative to the parent grouping element. Defaults to 0.</li>
     * <li><b>blendMode</b> (String): [normal, add, alpha, darken, difference,
     * erase, hardlight, invert, layer, lighten, multiply, normal, subtract,
     * screen, overlay, auto, colordodge, colorburn, exclusion, softlight, 
     * hue, saturation, color, luminosity] Defaults to auto.</li>
     * <li><b>alpha</b> (ASAlpha): Defaults to 1.</li>
     * <li><b>maskType</b> (String):[clip, alpha]: Defaults to clip.</li>
     * <li><b>visible</b> (Boolean): Whether or not the text box is visible.
     * Defaults to true.</li>
     * </ul>
     * </p>
     * <p>
     * Graphic content nodes also support an id attribute.
     * </p> 
     * 
     * @param name - the unqualified attribute name
     * @param value - the attribute value
     * @param problems problem collection used to collect problems occurred within this method
     */
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
        else if (FXG_ALPHA_ATTRIBUTE.equals(name))
        {
            alpha = DOMParserHelper.parseDouble(this, value, name, ALPHA_MIN_INCLUSIVE, ALPHA_MAX_INCLUSIVE, alpha, problems);
            alphaSet = true;
        }
        else if (FXG_BLENDMODE_ATTRIBUTE.equals(name))
        {
            blendMode = parseBlendMode(value, blendMode, problems);
        }
        else if (FXG_VISIBLE_ATTRIBUTE.equals(name))
        {
            visible = DOMParserHelper.parseBoolean(this, value, name, visible, problems);
        }
        else if (FXG_ID_ATTRIBUTE.equals(name))
        {
            id = value;
        }
        else if (FXG_MASKTYPE_ATTRIBUTE.equals(name))
        {
            maskType = DOMParserHelper.parseMaskType(this, value, maskType, problems);
            maskTypeSet = true;
        }
        else if (getFileVersion().equalTo(FXGVersion.v1_0))
        {
            // Rest of the attributes are not supported by FXG 1.0
            // Attribute {0} not supported by node {1}.
            problems.add(new FXGInvalidNodeAttributeProblem(getDocumentPath(), getStartLine(), getStartColumn(), name, getNodeName()));
            return;
        }
        else if (FXG_LUMINOSITYCLIP_ATTRIBUTE.equals(name))
        {
            luminosityClip = DOMParserHelper.parseBoolean(this, value, name, luminosityClip, problems);
        }
        else if (FXG_LUMINOSITYINVERT_ATTRIBUTE.equals(name))
        {
            luminosityInvert =  DOMParserHelper.parseBoolean(this, value, name, luminosityInvert, problems);            
        }
        else
        {
            super.setAttribute(name, value, problems);
        }
    }

    
    //--------------------------------------------------------------------------
    //
    // IMaskableNode Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public IMaskingNode getMask()
    {
        return mask;
    }

    @Override
    public MaskType getMaskType()
    {
        return maskType;
    }
    
    @Override
    public boolean getLuminosityClip()
    {
        return luminosityClip;
    }
    
    @Override
    public boolean getLuminosityInvert()
    {
        return luminosityInvert;
    }
    
    //--------------------------------------------------------------------------
    //
    // Helper Methods
    //
    //--------------------------------------------------------------------------

    private GraphicContext parentGraphicContext;

    public GraphicContext createGraphicContext()
    {
        GraphicContext graphicContext = new GraphicContext();

        if (parentGraphicContext != null)
            graphicContext.scalingGrid = parentGraphicContext.scalingGrid;

        FXGMatrix transform = graphicContext.getTransform(); 
        if (matrix != null)
        {
            FXGMatrix t = new FXGMatrix(matrix);
            transform.concat(t);
        }
        else
        { 

            if (scaleSet)
                transform.scale(scaleX, scaleY);

            if (rotationSet)
                transform.rotate(rotation);

            if (translateSet)
                transform.translate(x, y);

        }

        if (colorTransform != null)
        {
            graphicContext.colorTransform = colorTransform;
        }
        else if (alphaSet)
        {
            if (graphicContext.colorTransform == null)
                graphicContext.colorTransform = new ColorTransformNode();

            graphicContext.colorTransform.alphaMultiplier = alpha;
        }

        graphicContext.blendMode = blendMode;

        if (filters != null)
            graphicContext.addFilters(filters);

        if (maskTypeSet)
            graphicContext.maskType = maskType;
        else if (parentGraphicContext != null)
            graphicContext.maskType = parentGraphicContext.maskType;

        return graphicContext;
    }

    public void setParentGraphicContext(GraphicContext context)
    {
        parentGraphicContext = context;
    }

    /**
     * Convert an FXG String value to a BlendMode enumeration.
     * 
     * @param value - the FXG String value
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching BlendMode
     */
    protected BlendMode parseBlendMode(String value, BlendMode defMode, Collection<ICompilerProblem> problems)
    {
        FXGVersion fileVersion = ((GraphicNode)(this.getDocumentNode())).getVersion();
        
        if (FXG_BLENDMODE_ADD_VALUE.equals(value))
        {
            return BlendMode.ADD;
        }
        else if (FXG_BLENDMODE_ALPHA_VALUE.equals(value))
        {
            return BlendMode.ALPHA;
        }
        else if (FXG_BLENDMODE_DARKEN_VALUE.equals(value))
        {
            return BlendMode.DARKEN;
        }
        else if (FXG_BLENDMODE_DIFFERENCE_VALUE.equals(value))
        {
            return BlendMode.DIFFERENCE;
        }
        else if (FXG_BLENDMODE_ERASE_VALUE.equals(value))
        {
            return BlendMode.ERASE;
        }
        else if (FXG_BLENDMODE_HARDLIGHT_VALUE.equals(value))
        {
            return BlendMode.HARDLIGHT;
        }
        else if (FXG_BLENDMODE_INVERT_VALUE.equals(value))
        {
            return BlendMode.INVERT;
        }
        else if (FXG_BLENDMODE_LAYER_VALUE.equals(value))
        {
            return BlendMode.LAYER;
        }
        else if (FXG_BLENDMODE_LIGHTEN_VALUE.equals(value))
        {
            return BlendMode.LIGHTEN;
        }
        else if (FXG_BLENDMODE_MULTIPLY_VALUE.equals(value))
        {
            return BlendMode.MULTIPLY;
        }
        else if (FXG_BLENDMODE_NORMAL_VALUE.equals(value))
        {
            return BlendMode.NORMAL;
        }
        else if (FXG_BLENDMODE_OVERLAY_VALUE.equals(value))
        {
            return BlendMode.OVERLAY;
        }
        else if (FXG_BLENDMODE_SCREEN_VALUE.equals(value))
        {
            return BlendMode.SCREEN;
        }
        else if (FXG_BLENDMODE_SUBTRACT_VALUE.equals(value))
        {
            return BlendMode.SUBTRACT;
        }
        else if (fileVersion.equalTo(FXGVersion.v1_0))
        {
            // Rest of the blend modes are unknown for FXG 1.0
            //Unknown blend mode: {0}.
            problems.add(new FXGUnknownAttributeValueProblem(getDocumentPath(), 
                    getStartLine(), getStartColumn(), FXG_BLENDMODE_ATTRIBUTE, value));
        }
        else if (FXG_BLENDMODE_COLORDOGE_VALUE.equals(value))
        {
            return BlendMode.COLORDODGE;
        }
        else if (FXG_BLENDMODE_COLORBURN_VALUE.equals(value))
        {
            return BlendMode.COLORBURN;
        }
        else if (FXG_BLENDMODE_EXCLUSION_VALUE.equals(value))
        {
            return BlendMode.EXCLUSION;
        }
        else if (FXG_BLENDMODE_SOFTLIGHT_VALUE.equals(value))
        {
            return BlendMode.SOFTLIGHT;
        }
        else if (FXG_BLENDMODE_HUE_VALUE.equals(value))
        {
            return BlendMode.HUE;
        }
        else if (FXG_BLENDMODE_SATURATION_VALUE.equals(value))
        {
            return BlendMode.SATURATION;
        }
        else if (FXG_BLENDMODE_COLOR_VALUE.equals(value))
        {
            return BlendMode.COLOR;
        }
        else if (FXG_BLENDMODE_LUMINOSITY_VALUE.equals(value))
        {
            return BlendMode.LUMINOSITY;
        }
        else if (FXG_BLENDMODE_AUTO_VALUE.equals(value))
        {
            return BlendMode.AUTO;
        }
        else
        {
            if (isVersionGreaterThanCompiler())
            {
                // Warning: Minor version of this FXG file is greater than minor
                // version supported by this compiler. Log a warning for an unknown
                // blend mode.
                FXGLog.getLogger().log(IFXGLogger.WARN, "UnknownBlendMode", null, getDocumentPath(), startLine, startColumn);
            }
            else
            {
              //Unknown blend mode: {0} for FXGVersion 2.0.
                problems.add(new FXGUnknownAttributeValueProblem(getDocumentPath(), 
                        getStartLine(), getStartColumn(), FXG_BLENDMODE_ATTRIBUTE, value));
            }
        }
            

        return defMode;
    }
    
 
    /**
     * Convert discreet transform attributes to child matrix. This allows
     *  concatenation of another matrix.
     */
    public void convertTransformAttrToMatrix(Collection<ICompilerProblem> problems)
    {
        try
        {
            MatrixNode matrixNode = MatrixNode.class.newInstance();
            // Convert discreet transform attributes to FXGMatrix.
            FXGMatrix matrix = FXGMatrix.convertToMatrix(scaleX, scaleY, rotation, x, y);
            // Set matrix attributes to FXGMatrix values.
            matrix.setMatrixNodeValue(matrixNode);
            // Reset all discreet transform attributes since matrix 
            // and discreet transform attributes cannot coexist.
            resetTransformAttr();
            // Add child matrix to the node.
            this.addChild(matrixNode, problems);
        }
        catch (Exception e)
        {
            problems.add(new FXGInvalidChildMatrixNodeProblem(mask.getDocumentPath(), mask.getStartLine(), mask.getStartColumn()));
        }        

    }
    
    /**
     * Reset discreet transform attributes to their default value. This allows
     *  child matrix can be set instead.
     */
    private void resetTransformAttr()
    {
        x = 0.0;
        y = 0.0;
        scaleX = 1.0;
        scaleY = 1.0;
        rotation = 0.0;
        translateSet = false;
        scaleSet = false;
        rotationSet = false;        
    }
}
