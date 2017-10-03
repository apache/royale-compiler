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

import static org.apache.royale.compiler.fxg.FXGConstants.FXG_GRAPHIC_ELEMENT;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_LUMINOSITYCLIP_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_LUMINOSITYINVERT_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_MASKTYPE_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_SCALEGRIDBOTTOM_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_SCALEGRIDLEFT_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_SCALEGRIDRIGHT_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_SCALEGRIDTOP_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_VERSION_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_VIEWHEIGHT_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_VIEWWIDTH_ATTRIBUTE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.fxg.FXGVersion;
import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.types.MaskType;
import org.apache.royale.compiler.internal.fxg.dom.types.ScalingGrid;
import org.apache.royale.compiler.problems.FXGInvalidLibraryElementProblem;
import org.apache.royale.compiler.problems.FXGInvalidMaskElementProblem;
import org.apache.royale.compiler.problems.FXGInvalidNodeAttributeProblem;
import org.apache.royale.compiler.problems.FXGInvalidVersionProblem;
import org.apache.royale.compiler.problems.FXGMultipleElementProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Represents the root &lt;Graphic&gt; element of an FXG Document.
 */
public class GraphicNode extends AbstractFXGNode implements IMaskableNode
{
    private String documentPath = null;
    private FXGVersion version = null; // The version of FXG being processed.
    public Map<String, Class<? extends IFXGNode>> reservedNodes;
    	
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------
	
	public double scaleGridLeft = 0.0;
    public double scaleGridTop = 0.0;
    public double scaleGridRight = 0.0;
    public double scaleGridBottom = 0.0;

    public double viewWidth = Double.NaN;
    public double viewHeight = Double.NaN;
    public MaskType maskType = MaskType.CLIP;

    protected boolean luminosityInvert=false;
    protected boolean luminosityClip=false;

    //Flag indicating whether the FXG version is newer than the compiler version.
    private boolean isVersionGreaterThanCompiler = false;

    //--------------------------------------------------------------------------
    //
    // Children
    //
    //--------------------------------------------------------------------------

    public List<GraphicContentNode> children;
    public LibraryNode library;
    public IMaskingNode mask;
    @Override
    public List<IFXGNode> getChildren()
    {
        List<IFXGNode> childrenRes = new ArrayList<IFXGNode>();
        childrenRes.addAll(super.getChildren());
        if(children != null)
            childrenRes.addAll(children);
        childrenRes.add(library);
        childrenRes.add(mask);
        return childrenRes;
    }
    /**
     * @return - true if version of the FXG file is greater than the compiler/IFXGVersionHandler
     * version. false otherwise.
     */
    @Override
    public boolean isVersionGreaterThanCompiler()
    {
        return isVersionGreaterThanCompiler;
    }
    
    /**
     * sets isVersionGreaterThanCompiler
     * @param versionGreaterThanCompiler The value to be set.
     */
    public void setVersionGreaterThanCompiler(boolean versionGreaterThanCompiler)
    {
        isVersionGreaterThanCompiler = versionGreaterThanCompiler;
    }
    
    /**
     * @return - the path of the FXG file being processed.
     */
    @Override
    public String getDocumentPath()
    {
        return documentPath;
    }
    
    /**
     * Set the path of the FXG file being processed.
     */
    public void setDocumentPath(String documentPath)
    {
        this.documentPath = documentPath;
    }

    /**
     * @return - version as FXGVersion.
     */
    public FXGVersion getVersion()
    {
        return version;
    }
    
    /**
     * Set the reserved nodes HashMap. Those XML element names are reserved and 
     * cannot be used as the definition name for a library element.
     */
    public void setReservedNodes(Map<String, Class<? extends IFXGNode>> reservedNodes)
    {
        this.reservedNodes = reservedNodes;
    }
    
    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * Adds an FXG child node to this Graphic node. Supported child nodes
     * include graphic content nodes (e.g. Group, BitmapGraphic, Ellipse, Line,
     * Path, Rect, TextGraphic), control nodes (e.g. Library, Private), or
     * property nodes (e.g. mask).
     * 
     * @param child - a child FXG node to be added to this node.
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof MaskPropertyNode)
        {
        	/**
        	 * According to FXG 2.0 spec., <mask> must be before any graphical element.
        	 */
        	if (children != null)
        	{
        	    problems.add(new FXGInvalidMaskElementProblem(getDocumentPath(), child.getStartLine(), child.getStartColumn()));
        	    return;
        	}	
        	if (mask == null)
        	{
        		mask = ((MaskPropertyNode)child).mask;
        	}
        	else
        	{
                problems.add(new FXGMultipleElementProblem(getDocumentPath(), child.getStartLine(), child.getStartColumn(), child.getNodeName()));
                return;
        	}           
        }
        else if (child instanceof LibraryNode)
        {   
        	/**
        	 * According to FXG 2.0 spec., <Library> must be before <mask> and any graphical element.
        	 */
        	if (mask != null || children != null)
        	{
                problems.add(new FXGInvalidLibraryElementProblem(getDocumentPath(), child.getStartLine(), child.getStartColumn()));
                return;
        	}	
        	
            if (library == null)
            {
                library = (LibraryNode)child;
            }
            else
            {
                problems.add(new FXGMultipleElementProblem(getDocumentPath(), child.getStartLine(), child.getStartColumn(), child.getNodeName()));
                return;
            }
        }
        else if (child instanceof GraphicContentNode)
        {
            if (children == null)
                children = new ArrayList<GraphicContentNode>();

            if (child instanceof GroupNode)
            {
                GroupNode group = (GroupNode)child;

                if (definesScaleGrid)
                {
                    group.setInsideScaleGrid(true);
                }
            }


            children.add((GraphicContentNode)child);
        }
        else
        {
            super.addChild(child, problems);
        }
    }

    /**
     * @return The unqualified name of a Graphic node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_GRAPHIC_ELEMENT;
    }

    /**
     * Sets an FXG attribute on this Graphic node.
     * 
     * @param name - the unqualified attribute name
     * @param value - the attribute value
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_SCALEGRIDLEFT_ATTRIBUTE.equals(name))
        {
            scaleGridLeft = DOMParserHelper.parseDouble(this, value, name, scaleGridLeft, problems);
            definesScaleGrid = true;
        }
        else if (FXG_SCALEGRIDTOP_ATTRIBUTE.equals(name))
        {
            scaleGridTop = DOMParserHelper.parseDouble(this, value, name, scaleGridTop, problems);
            definesScaleGrid = true;
        }
        else if (FXG_SCALEGRIDRIGHT_ATTRIBUTE.equals(name))
        {
            scaleGridRight = DOMParserHelper.parseDouble(this, value, name, scaleGridRight, problems);
            definesScaleGrid = true;
        }
        else if (FXG_SCALEGRIDBOTTOM_ATTRIBUTE.equals(name))
        {
            scaleGridBottom = DOMParserHelper.parseDouble(this, value, name, scaleGridBottom, problems);
            definesScaleGrid = true;
        }
        else if (FXG_VIEWWIDTH_ATTRIBUTE.equals(name))
        {
            viewWidth = DOMParserHelper.parseDouble(this, value, name, viewWidth, problems);
        }
        else if (FXG_VIEWHEIGHT_ATTRIBUTE.equals(name))
        {
            viewHeight = DOMParserHelper.parseDouble(this, value, name, viewHeight, problems);
        }
        else if (FXG_VERSION_ATTRIBUTE.equals(name))
        {
            try
            {
                version = FXGVersion.newInstance(DOMParserHelper.parseDouble(this, value, Double.MIN_VALUE, Double.MAX_VALUE));
            }
            catch (Exception e)
            {
                problems.add(new FXGInvalidVersionProblem(getDocumentPath(), 
                        getStartLine(), getStartColumn(), value));
                return;
            }
        }
        else if (FXG_MASKTYPE_ATTRIBUTE.equals(name))
        {
            maskType = DOMParserHelper.parseMaskType(this, value, maskType, problems);
        }
        else if ((version != null) && (version.equalTo(FXGVersion.v1_0)))
        {
            // Rest of the attributes are not supported by FXG 1.0
            // Attribute {0} not supported by node {1}. 
            problems.add(new FXGInvalidNodeAttributeProblem(getDocumentPath(), getStartLine(), getStartColumn(), name, getNodeName()));
            return;
        }
        else if (FXG_LUMINOSITYINVERT_ATTRIBUTE.equals(name))
        {
            luminosityInvert = DOMParserHelper.parseBoolean(this, value, name, luminosityInvert, problems);
        }        
        else if (FXG_LUMINOSITYCLIP_ATTRIBUTE.equals(name))
        {
            luminosityClip = DOMParserHelper.parseBoolean(this, value, name, luminosityClip, problems); 
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
    // Other Methods
    //
    //--------------------------------------------------------------------------

    public PlaceObjectNode getDefinitionInstance(String name)
    {
        PlaceObjectNode instance = null;

        if (library != null)
        {
            DefinitionNode definition = library.getDefinition(name);
            if (definition != null)
            {
                instance = new PlaceObjectNode();
                instance.definition = definition;
            }
        }

        return instance;
    }

    public ScalingGrid getScalingGrid()
    {
        ScalingGrid scalingGrid = null;

        if (definesScaleGrid())
        {
            scalingGrid = new ScalingGrid();
            scalingGrid.scaleGridLeft = scaleGridLeft;
            scalingGrid.scaleGridTop = scaleGridTop;
            scalingGrid.scaleGridRight = scaleGridRight;
            scalingGrid.scaleGridBottom = scaleGridBottom;
        }

        return scalingGrid;
    }

    public boolean definesScaleGrid()
    {
        return definesScaleGrid;
    }

    private boolean definesScaleGrid;
    
}
