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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.richtext.TextHelper;
import org.apache.royale.compiler.internal.fxg.dom.text.BRNode;
import org.apache.royale.compiler.internal.fxg.dom.CDATANode;
import org.apache.royale.compiler.internal.fxg.dom.text.ParagraphNode;
import org.apache.royale.compiler.internal.fxg.dom.text.SpanNode;
import org.apache.royale.compiler.internal.fxg.dom.text.AbstractCharacterTextNode;
import org.apache.royale.compiler.internal.fxg.dom.types.Kerning;
import org.apache.royale.compiler.internal.fxg.dom.types.LineBreak;
import org.apache.royale.compiler.internal.fxg.dom.types.WhiteSpaceCollapse;
import org.apache.royale.compiler.problems.FXGInvalidNodeAttributeProblem;
import org.apache.royale.compiler.problems.FXGContentNotContiguousProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

public class TextGraphicNode extends GraphicContentNode implements ITextNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    // Text Attributes
    public double width = 0.0;
    public double height = 0.0;
    public double paddingLeft = 0.0;
    public double paddingRight = 0.0;
    public double paddingBottom = 0.0;
    public double paddingTop = 0.0;

    // Character Attributes
    public String fontFamily = "Times New Roman";
    public double fontSize = 12.0;
    public String fontStyle = "normal";
    public String fontWeight = "normal";
    public double lineHeight = 120.0;
    public String textDecoration = "none";
    public WhiteSpaceCollapse whiteSpaceCollapse = WhiteSpaceCollapse.PRESERVE;
    public LineBreak lineBreak = LineBreak.TOFIT;
    public boolean lineThrough = false;
    public double tracking = 0.0;
    public Kerning kerning = Kerning.AUTO;
    public double textAlpha = 1.0;
    public int color = COLOR_BLACK;

    // Paragraph Attributes
    public String textAlign = "left";
    public String textAlignLast = "left";
    public double textIndent = 0.0;
    public double marginLeft = 0.0;
    public double marginRight = 0.0;
    public double marginTop = 0.0;
    public double marginBottom = 0.0;
    public String direction = "ltr";
    public String blockProgression = "tb";
    
    private boolean contiguous = false;

    //--------------------------------------------------------------------------
    //
    // Text Node Attribute Helpers
    //
    //--------------------------------------------------------------------------
    /**
     * The attributes set on this node.
     */
    protected Map<String, String> textAttributes;

    /**
     * @return A Map recording the attribute names and values set on this
     * text node.
     */
    @Override
    public Map<String, String> getTextAttributes()
    {
        return textAttributes;
    }

    /**
     * This nodes child text nodes.
     */
    protected List<ITextNode> content;

    /**
     * @return The List of child nodes of this text node. 
     */
    @Override
    public List<ITextNode> getTextChildren()
    {
        return content;
    }

    /**
     * @return The List of child property nodes of this text node.
     */
    @Override
    public HashMap<String, ITextNode> getTextProperties()
    {
        return null;
    }

    /**
     * A text node may also have special child property nodes that represent
     * complex property values that cannot be set via a simple attribute.
     */
    @Override
    public void addTextProperty(String propertyName, ITextNode node, Collection<ICompilerProblem> problems)
    {
        addChild(node, problems);
    }

    /**
     * Remember that an attribute was set on this node.
     * 
     * @param name - the unqualified attribute name.
     * @param value - the attribute value.
     */
    protected void rememberAttribute(String name, String value)
    {
        if (textAttributes == null)
            textAttributes = new HashMap<String, String>(4);

        textAttributes.put(name, value);
    }

    /**
     * &lt;TextGraphic&gt; content allows child &lt;p&gt;, &lt;span&gt; and
     * &lt;br /&gt; tags, as well as character data (text content).
     * 
     * @param child - a child FXG node to be added to this node.
     * @param problems problem collection used to collect problems occurred within this method
     */
    public void addContentChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof ParagraphNode
                || child instanceof BRNode
                || child instanceof SpanNode
                || child instanceof CDATANode)
        {
            if (content == null)
            {
                content = new ArrayList<ITextNode>();
                contiguous = true;
            }
            
            if (!contiguous)
            {
                problems.add(new FXGContentNotContiguousProblem(child.getDocumentPath(), 
                        child.getStartLine(), child.getStartColumn(), getNodeName()));
            	return;           	
            }

            content.add((ITextNode)child);
        }
    }

    @Override
    public List<IFXGNode> getChildren()
    {
        List<IFXGNode> children = new ArrayList<IFXGNode>();
        children.addAll(super.getChildren());
        if(content != null)
            children.addAll(content);
        
        return children;
    }
    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * This method is invoked for only non-content children.
     * 
     * @param child - a child FXG node to be added to this node.
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
    	if (child instanceof CDATANode)
        {
            if(TextHelper.ignorableWhitespace(((CDATANode)child).content))
            {
            	/**
            	 * Ignorable white spaces don't break content contiguous 
            	 * rule and should be ignored.
            	 */
            	return;
            }
            else
            {
                problems.add(new FXGContentNotContiguousProblem(child.getDocumentPath(), 
                        child.getStartLine(), child.getStartColumn(), getNodeName()));
                return; 
            }
        }
        else 
        {
            super.addChild(child, problems);
            contiguous = false;
        }
    }

    /**
     * @return The unqualified name of a TextGraphic node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_TEXTGRAPHIC_ELEMENT;
    }

    /**
     * Sets an FXG attribute on this TextGraphic node.
     * 
     * In addition to the attributes supported by all graphic content nodes,
     * TextGraphic supports the following attributes.
     * 
     * <p>
     * <ul>
     * <li><b>width</b> (Number): The width of the text box to render text
     * in.</li>
     * <li><b>height</b> (Number): The height of the text box to render text
     * in.</li>
     * <li><b>paddingLeft</b> (Number): Inset from left edge to content area.
     * Units in pixels, defaults to 0.</li>
     * <li><b>paddingRight</b> (Number): Inset from right edge to content area.
     * Units in pixels, defaults to 0.</li>
     * <li><b>paddingTop</b> (Number): Inset from top edge to content area.
     * Units in pixels, defaults to 0.</li>
     * <li><b>paddingBottom</b> (Number): Inset from bottom edge to content
     * area. Units in pixels, defaults to 0.</li>
     * </ul>
     * </p>
     * @param name - the unqualified attribute name.
     * @param value - the attribute value.
     */
    @Override
    public void setAttribute(String name,  String value, Collection<ICompilerProblem> problems)
    {
        
        if (FXG_WIDTH_ATTRIBUTE.equals(name))
        {
            width = DOMParserHelper.parseDouble(this, value, name, width, problems);
        }
        else if (FXG_HEIGHT_ATTRIBUTE.equals(name))
        {
            height = DOMParserHelper.parseDouble(this, value, name, height, problems);
        }
        else if (FXG_PADDINGLEFT_ATTRIBUTE.equals(name))
        {
            paddingLeft = DOMParserHelper.parseDouble(this, value, name, paddingLeft, problems);
        }
        else if (FXG_PADDINGRIGHT_ATTRIBUTE.equals(name))
        {
            paddingRight = DOMParserHelper.parseDouble(this, value, name, paddingRight, problems);
         }
        else if (FXG_PADDINGBOTTOM_ATTRIBUTE.equals(name))
        {
            paddingBottom = DOMParserHelper.parseDouble(this, value, name, paddingBottom, problems);
        }
        else if (FXG_PADDINGTOP_ATTRIBUTE.equals(name))
        {
            paddingTop = DOMParserHelper.parseDouble(this, value, name, paddingTop, problems);
        }
        else if (FXG_FONTFAMILY_ATTRIBUTE.equals(name))
        {
            fontFamily = value;
        }
        else if (FXG_FONTSIZE_ATTRIBUTE.equals(name))
        {
            fontSize = DOMParserHelper.parseDouble(this, value, name, fontSize, problems);
        }
        else if (FXG_FONTSTYLE_ATTRIBUTE.equals(name))
        {
            fontStyle = value;
        }
        else if (FXG_FONTWEIGHT_ATTRIBUTE.equals(name))
        {
            fontWeight = value;
        }
        else if (FXG_LINEHEIGHT_ATTRIBUTE.equals(name))
        {
            lineHeight = DOMParserHelper.parsePercent(this, value, name, lineHeight, problems);
        }
        else if (FXG_TEXTDECORATION_ATTRIBUTE.equals(name))
        {
            textDecoration = value;
        }
        else if (FXG_WHITESPACECOLLAPSE_ATTRIBUTE.equals(name))
        {
            whiteSpaceCollapse = AbstractCharacterTextNode.getWhiteSpaceCollapse(this, value, problems);
        }
        else if (FXG_LINEBREAK_ATTRIBUTE.equals(name))
        {
            lineBreak = AbstractCharacterTextNode.getLineBreak(this, value, problems);
        }
        else if (FXG_TRACKING_ATTRIBUTE.equals(name))
        {
            tracking = DOMParserHelper.parsePercent(this, value, name, tracking, problems);
        }
        else if (FXG_KERNING_ATTRIBUTE.equals(name))
        {
            kerning = AbstractCharacterTextNode.getKerning(this, value, problems);
        }
        else if (FXG_TEXTALPHA_ATTRIBUTE.equals(name))
        {
            textAlpha = DOMParserHelper.parseDouble(this, value, name, ALPHA_MIN_INCLUSIVE, ALPHA_MAX_INCLUSIVE, textAlpha, problems);
        }
        else if (FXG_COLOR_ATTRIBUTE.equals(name))
        {
            color = DOMParserHelper.parseRGB(this, value, name, color, problems);
        }
        else if (FXG_TEXTALIGN_ATTRIBUTE.equals(name))
        {
            textAlign = value;
        }
        else if (FXG_TEXTALIGNLAST_ATTRIBUTE.equals(name))
        {
            textAlignLast = value;
        }
        else if (FXG_TEXTINDENT_ATTRIBUTE.equals(name))
        {
            textIndent = DOMParserHelper.parseDouble(this, value, name, textIndent, problems);
        }
        else if (FXG_MARGINLEFT_ATTRIBUTE.equals(name))
        {
            marginLeft = DOMParserHelper.parseDouble(this, value, name, marginLeft, problems);
        }
        else if (FXG_MARGINRIGHT_ATTRIBUTE.equals(name))
        {
            marginRight = DOMParserHelper.parseDouble(this, value, name, marginRight, problems);
        }
        else if (FXG_MARGINTOP_ATTRIBUTE.equals(name))
        {
            marginTop = DOMParserHelper.parseDouble(this, value, name, marginTop, problems);
        }
        
        else if (FXG_MARGINBOTTOM_ATTRIBUTE.equals(name))
        {
            marginBottom = DOMParserHelper.parseDouble(this, value, name, marginBottom, problems);
        }
        else if (FXG_DIRECTION_ATTRIBUTE.equals(name))
        {
            direction = value;
        }
        else if (FXG_BLOCKPROGRESSION_ATTRIBUTE.equals(name))
        {
            blockProgression = value;
        }
        else if (FXG_X_ATTRIBUTE.equals(name))
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
        else if (FXG_MASKTYPE_ATTRIBUTE.equals(name))
        {
            maskType = DOMParserHelper.parseMaskType(this, value, maskType, problems);
            maskTypeSet = true;
        }
        else if (FXG_VISIBLE_ATTRIBUTE.equals(name))
        {
            visible = DOMParserHelper.parseBoolean(this, value, name, visible, problems);
        }      
        else if ( FXG_LINETHROUGH_ATTRIBUTE.equals(name))
        {
            lineThrough = DOMParserHelper.parseBoolean(this, value, name, lineThrough, problems);
        }        
        else if (FXG_ID_ATTRIBUTE.equals(name))
        {
            //id = value;
        }
        else
        {
        	//Attribute, {0}, not supported by node: {1}.
            problems.add(new FXGInvalidNodeAttributeProblem(getDocumentPath(), 
                    getStartLine(), getStartColumn(), name, getNodeName()));
            return; 
        }
        
        // Remember attribute was set on this node.
        rememberAttribute(name, value);                
    }
}
