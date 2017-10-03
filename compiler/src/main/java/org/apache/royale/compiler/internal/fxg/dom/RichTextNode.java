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
import org.apache.royale.compiler.internal.fxg.dom.richtext.AbstractRichTextNode;
import org.apache.royale.compiler.internal.fxg.dom.richtext.BRNode;
import org.apache.royale.compiler.internal.fxg.dom.richtext.DivNode;
import org.apache.royale.compiler.internal.fxg.dom.richtext.ImgNode;
import org.apache.royale.compiler.internal.fxg.dom.richtext.LinkNode;
import org.apache.royale.compiler.internal.fxg.dom.richtext.ParagraphNode;
import org.apache.royale.compiler.internal.fxg.dom.richtext.SpanNode;
import org.apache.royale.compiler.internal.fxg.dom.richtext.TCYNode;
import org.apache.royale.compiler.internal.fxg.dom.richtext.TabNode;
import org.apache.royale.compiler.internal.fxg.dom.richtext.TextHelper;
import org.apache.royale.compiler.internal.fxg.dom.richtext.TextLayoutFormatNode;
import org.apache.royale.compiler.internal.fxg.dom.types.AlignmentBaseline;
import org.apache.royale.compiler.internal.fxg.dom.types.BaselineOffset;
import org.apache.royale.compiler.internal.fxg.dom.types.BaselineShift;
import org.apache.royale.compiler.internal.fxg.dom.types.BlockProgression;
import org.apache.royale.compiler.internal.fxg.dom.types.BreakOpportunity;
import org.apache.royale.compiler.internal.fxg.dom.types.ColorWithEnum;
import org.apache.royale.compiler.internal.fxg.dom.types.DigitCase;
import org.apache.royale.compiler.internal.fxg.dom.types.DigitWidth;
import org.apache.royale.compiler.internal.fxg.dom.types.Direction;
import org.apache.royale.compiler.internal.fxg.dom.types.DominantBaseline;
import org.apache.royale.compiler.internal.fxg.dom.types.FontStyle;
import org.apache.royale.compiler.internal.fxg.dom.types.FontWeight;
import org.apache.royale.compiler.internal.fxg.dom.types.JustificationRule;
import org.apache.royale.compiler.internal.fxg.dom.types.JustificationStyle;
import org.apache.royale.compiler.internal.fxg.dom.types.Kerning;
import org.apache.royale.compiler.internal.fxg.dom.types.LeadingModel;
import org.apache.royale.compiler.internal.fxg.dom.types.LigatureLevel;
import org.apache.royale.compiler.internal.fxg.dom.types.LineBreak;
import org.apache.royale.compiler.internal.fxg.dom.types.NumberAuto;
import org.apache.royale.compiler.internal.fxg.dom.types.NumberInherit;
import org.apache.royale.compiler.internal.fxg.dom.types.TextAlign;
import org.apache.royale.compiler.internal.fxg.dom.types.TextDecoration;
import org.apache.royale.compiler.internal.fxg.dom.types.TextJustify;
import org.apache.royale.compiler.internal.fxg.dom.types.TextRotation;
import org.apache.royale.compiler.internal.fxg.dom.types.TypographicCase;
import org.apache.royale.compiler.internal.fxg.dom.types.VerticalAlign;
import org.apache.royale.compiler.internal.fxg.dom.types.WhiteSpaceCollapse;
import org.apache.royale.compiler.internal.fxg.dom.types.BaselineOffset.BaselineOffsetAsEnum;
import org.apache.royale.compiler.internal.fxg.dom.types.BaselineShift.BaselineShiftAsEnum;
import org.apache.royale.compiler.internal.fxg.dom.types.ColorWithEnum.ColorEnum;
import org.apache.royale.compiler.internal.fxg.dom.types.NumberAuto.NumberAutoAsEnum;
import org.apache.royale.compiler.internal.fxg.dom.types.NumberInherit.NumberInheritAsEnum;
import org.apache.royale.compiler.problems.FXGContentNotContiguousProblem;
import org.apache.royale.compiler.problems.FXGInvalidChildNodeProblem;
import org.apache.royale.compiler.problems.FXGMissingAttributeProblem;
import org.apache.royale.compiler.problems.FXGMultipleElementProblem;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Represents a &lt;RichText&gt; element of an FXG Document.
 */
public class RichTextNode extends GraphicContentNode implements ITextNode
{
    protected static final double FONTSIZE_MIN_INCLUSIVE = 1.0;
    protected static final double FONTSIZE_MAX_INCLUSIVE = 720.0;
    protected static final double PADDING_MIN_INCLUSIVE = 0.0;
    protected static final double PADDING_MAX_INCLUSIVE = 1000.0;    
    protected static final double BASELINEOFFSET_MIN_INCLUSIVE = 0.0;
    protected static final double BASELINEOFFSET_MAX_INCLUSIVE = 1000.0; 
    protected static final double BASELINESHIFT_MIN_INCLUSIVE = -1000.0;
    protected static final double BASELINESHIFT_MAX_INCLUSIVE = 1000.0; 
    protected static final int COLUMNCOUNT_MIN_INCLUSIVE = 0;
    protected static final int COLUMNCOUNT_MAX_INCLUSIVE = 50; 
    protected static final double COLUMNGAP_MIN_INCLUSIVE = 0.0;
    protected static final double COLUMNGAP_MAX_INCLUSIVE = 1000.0; 
    protected static final double COLUMNWIDTH_MIN_INCLUSIVE = 0.0;
    protected static final double COLUMNWIDTH_MAX_INCLUSIVE = 8000.0; 
    protected static final double LINEHEIGHT_PERCENT_MIN_INCLUSIVE = -1000.0;
    protected static final double LINEHEIGHT_PERCENT_MAX_INCLUSIVE = 1000.0; 
    protected static final double LINEHEIGHT_PIXEL_MIN_INCLUSIVE = -720.0;
    protected static final double LINEHEIGHT_PIXEL_MAX_INCLUSIVE = 720.0; 
    protected static final double PARAGRAPH_INDENT_MIN_INCLUSIVE = 0.0;
    protected static final double PARAGRAPH_INDENT_MAX_INCLUSIVE = 1000.00;    
    protected static final double PARAGRAPH_SPACE_MIN_INCLUSIVE = 0.0;
    protected static final double PARAGRAPH_SPACE_MAX_INCLUSIVE = 1000.00;    
    protected static final double TEXTINDENT_MIN_INCLUSIVE = -1000.0;
    protected static final double TEXTINDENT_MAX_INCLUSIVE = 1000.0; 
    protected static final double TRACKING_MIN_INCLUSIVE = -1000.0;
    protected static final double TRACKING_MAX_INCLUSIVE = 1000.0;     
    
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public double width = 0.0;
    public double height = 0.0;

    // Text Flow Attributes
    public BlockProgression blockProgression = BlockProgression.TB;
    public NumberInherit paddingLeft = NumberInherit.newInstance(0.0);
    public NumberInherit paddingRight = NumberInherit.newInstance(0.0);
    public NumberInherit paddingTop = NumberInherit.newInstance(0.0);
    public NumberInherit paddingBottom = NumberInherit.newInstance(0.0);
    public LineBreak lineBreak = LineBreak.TOFIT;
    public NumberInherit columnGap = NumberInherit.newInstance(20.0);
    public NumberAuto columnCount = NumberAuto.newInstance(NumberAutoAsEnum.AUTO);
    public NumberAuto columnWidth = NumberAuto.newInstance(NumberAutoAsEnum.AUTO);
    public BaselineOffset firstBaselineOffset = BaselineOffset.newInstance(BaselineOffsetAsEnum.AUTO);
    public VerticalAlign verticalAlign = VerticalAlign.TOP;

    // Paragraph Attributes
    public TextAlign textAlign = TextAlign.START;
    public TextAlign textAlignLast = TextAlign.START;
    public double textIndent = 0.0;
    public double paragraphStartIndent = 0.0;
    public double paragraphEndIndent = 0.0;
    public double paragraphSpaceBefore = 0.0;
    public double paragraphSpaceAfter = 0.0;
    public Direction direction = Direction.LTR;
    public JustificationRule justificationRule = JustificationRule.AUTO;
    public JustificationStyle justificationStyle = JustificationStyle.PRIORITIZELEASTADJUSTMENT;
    public TextJustify textJustify = TextJustify.INTERWORD;
    public LeadingModel leadingModel = LeadingModel.AUTO;
    public String tabStops = "";
    
    // Text Leaf Attributes
    public String fontFamily = "Arial";
    public double fontSize = 12.0;
    public FontStyle fontStyle = FontStyle.NORMAL;
    public FontWeight fontWeight = FontWeight.NORMAL;
    public Kerning kerning = Kerning.AUTO;
    public double lineHeight = 120.0;
    public TextDecoration textDecoration = TextDecoration.NONE;
    public boolean lineThrough = false;
    public int color = AbstractFXGNode.COLOR_BLACK;
    public double textAlpha = 1.0;
    public WhiteSpaceCollapse whiteSpaceCollapse = WhiteSpaceCollapse.COLLAPSE;
    public NumberInherit backgroundAlpha = NumberInherit.newInstance(1.0);;
    public ColorWithEnum backgroundColor = ColorWithEnum.newInstance(ColorEnum.TRANSPARENT);
    public BaselineShift baselineShift = BaselineShift.newInstance(0.0);
    public BreakOpportunity breakOpportunity = BreakOpportunity.AUTO;
    public DigitCase digitCase = DigitCase.DEFAULT;
    public DigitWidth digitWidth = DigitWidth.DEFAULT;
    public DominantBaseline dominantBaseline = DominantBaseline.AUTO;
    public AlignmentBaseline alignmentBaseline = AlignmentBaseline.USEDOMINANTBASELINE;
    public LigatureLevel ligatureLevel = LigatureLevel.COMMON;
    public String locale = "en";
    public TypographicCase typographicCase = TypographicCase.DEFAULT;
    public double trackingLeft = 0.0;
    public double trackingRight = 0.0;
    public TextRotation textRotation = TextRotation.AUTO;

    // Link format properties
    public TextLayoutFormatNode linkNormalFormat = null;
    public TextLayoutFormatNode linkHoverFormat = null;
    public TextLayoutFormatNode linkActiveFormat = null;    
    
    private boolean contiguous = false;
    
    //--------------------------------------------------------------------------
    //
    // ITextNode Helpers
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
     * This node's child text nodes.
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
     * This node's child property nodes.
     */
    protected Map<String, ITextNode> properties;

    /**
     * @return The List of child property nodes of this text node.
     */
    @Override
    public Map<String, ITextNode> getTextProperties()
    {
        return properties;
    }

    /**
     * A RichText node can also have special child property nodes that represent
     * complex property values that cannot be set via a simple attribute.
     */
    @Override
    public void addTextProperty(String propertyName, ITextNode node, Collection<ICompilerProblem> problems)
    {
        if (node instanceof TextLayoutFormatNode)
        {
            if (FXG_LINKACTIVEFORMAT_PROPERTY_ELEMENT.equals(propertyName))
            {
                if (linkActiveFormat == null)
                {
                    linkActiveFormat = (TextLayoutFormatNode)node;
                    linkActiveFormat.setParent(this);

                    if (properties == null)
                        properties = new HashMap<String, ITextNode>(3);
                    properties.put(propertyName, linkActiveFormat);
                }
                else
                {
                    // Multiple LinkFormat elements are not allowed.
                    problems.add(new FXGMultipleElementProblem(getDocumentPath(), getStartLine(), 
                            getStartColumn(), propertyName));
                }
            }
            else if (FXG_LINKHOVERFORMAT_PROPERTY_ELEMENT.equals(propertyName))
            {
                if (linkHoverFormat == null)
                {
                    linkHoverFormat = (TextLayoutFormatNode)node;
                    linkHoverFormat.setParent(this);

                    if (properties == null)
                        properties = new HashMap<String, ITextNode>(3);
                    properties.put(propertyName, linkHoverFormat);
                }
                else
                {
                    // Multiple LinkFormat elements are not allowed.
                    problems.add(new FXGMultipleElementProblem(getDocumentPath(), getStartLine(), 
                            getStartColumn(), propertyName));
                }
            }
            else if (FXG_LINKNORMALFORMAT_PROPERTY_ELEMENT.equals(propertyName))
            {
                if (linkNormalFormat == null)
                {
                    linkNormalFormat = (TextLayoutFormatNode)node;
                    linkNormalFormat.setParent(this);

                    if (properties == null)
                        properties = new HashMap<String, ITextNode>(3);
                    properties.put(propertyName, linkNormalFormat);
                }
                else
                {
                    // Multiple LinkFormat elements are not allowed. 
                    problems.add(new FXGMultipleElementProblem(getDocumentPath(), getStartLine(), 
                            getStartColumn(), propertyName));
                }
            }
            else
            {
                // Unknown LinkFormat element. 
                problems.add(new FXGUnknownAttributeValueProblem(getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), node.getNodeName(), propertyName));
            }
        }
        else
        {
            addChild(node, problems);
        }
    }

    /**
     * &lt;RichText&gt; content allows child &lt;p&gt;, &lt;span&gt; and
     * &lt;br /&gt; tags, as well as character data (text content).
     * 
     * @param child - a child FXG node to be added to this node.
     * @param problems problem collection used to collect problems occurred within this method
     */
    public void addContentChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof ParagraphNode
                || child instanceof DivNode
                || child instanceof SpanNode
                || child instanceof BRNode
                || child instanceof TabNode
                || child instanceof TCYNode
                || child instanceof LinkNode
                || child instanceof ImgNode
                || child instanceof CDATANode)
        {
            if (child instanceof LinkNode && (((LinkNode)child).href == null))
            {
                //Missing href attribute in <a> element.
                problems.add(new FXGMissingAttributeProblem(getDocumentPath(), getStartLine(), 
                        getStartColumn(), FXG_HREF_ATTRIBUTE, child.getNodeName()));
                return;              
            }   
            
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
        else
        {
            problems.add(new FXGInvalidChildNodeProblem(child.getDocumentPath(), 
                    child.getStartLine(), child.getStartColumn(), child.getNodeName(), getNodeName()));
            return;
        }

        if (child instanceof AbstractRichTextNode)
            ((AbstractRichTextNode)child).setParent(this);       
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
            if (TextHelper.ignorableWhitespace(((CDATANode)child).content))
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
            return;
        }
    }

    @Override
    public List<IFXGNode> getChildren()
    {
        List<IFXGNode> children = new ArrayList<IFXGNode>();
        children.addAll(super.getChildren());
        if(content != null)
            children.addAll(content);
        if(getTextProperties() != null)
        {
            children.addAll(getTextProperties().values());
        }
        return children;
    }
    
    /**
     * @return The unqualified name of a RichText node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_RICHTEXT_ELEMENT;
    }

    /**
     * Sets an FXG attribute on this RichText node.
     * 
     * In addition to the attributes supported by all graphic content nodes,
     * RichText supports the following attributes.
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
        else if (FXG_BLOCKPROGRESSION_ATTRIBUTE.equals(name))
        {
            blockProgression = TextHelper.getBlockProgression(this, value, blockProgression, problems);
        }
        else if (FXG_PADDINGLEFT_ATTRIBUTE.equals(name))
        {
            paddingLeft = getNumberInherit(this, name, value, PADDING_MIN_INCLUSIVE, PADDING_MAX_INCLUSIVE, paddingLeft.getNumberInheritAsDbl(), problems);
        }
        else if (FXG_PADDINGRIGHT_ATTRIBUTE.equals(name))
        {
            paddingRight = getNumberInherit(this, name, value, PADDING_MIN_INCLUSIVE, PADDING_MAX_INCLUSIVE, paddingRight.getNumberInheritAsDbl(), problems);
        }
        else if (FXG_PADDINGTOP_ATTRIBUTE.equals(name))
        {
            paddingTop = getNumberInherit(this, name, value, PADDING_MIN_INCLUSIVE, PADDING_MAX_INCLUSIVE, paddingTop.getNumberInheritAsDbl(), problems);
        }
        else if (FXG_PADDINGBOTTOM_ATTRIBUTE.equals(name))
        {
            paddingBottom = getNumberInherit(this, name, value, PADDING_MIN_INCLUSIVE, PADDING_MAX_INCLUSIVE, paddingBottom.getNumberInheritAsDbl(), problems);
        }
        else if (FXG_LINEBREAK_ATTRIBUTE.equals(name))
        {
            lineBreak = TextHelper.getLineBreak(this, value, lineBreak, problems);
        }        
        else if (FXG_COLUMNGAP_ATTRIBUTE.equals(name))
        {
            columnGap = getNumberInherit(this, name, value, COLUMNGAP_MIN_INCLUSIVE, COLUMNGAP_MAX_INCLUSIVE, columnGap.getNumberInheritAsDbl(), problems);
        }
        else if (FXG_COLUMNCOUNT_ATTRIBUTE.equals(name))
        {
            columnCount = getNumberAutoInt(this, name, value, COLUMNCOUNT_MIN_INCLUSIVE, COLUMNCOUNT_MAX_INCLUSIVE, columnCount.getNumberAutoAsInt(), problems);
        }
        else if (FXG_COLUMNWIDTH_ATTRIBUTE.equals(name))
        {
            columnWidth = getNumberAutoDbl(this, name, value, COLUMNWIDTH_MIN_INCLUSIVE, COLUMNWIDTH_MAX_INCLUSIVE, columnWidth.getNumberAutoAsDbl(), problems);
        }
        else if (FXG_FIRSTBASELINEOFFSET_ATTRIBUTE.equals(name))
        {
            firstBaselineOffset = getFirstBaselineOffset(this, name, value, BASELINEOFFSET_MIN_INCLUSIVE, BASELINEOFFSET_MAX_INCLUSIVE, firstBaselineOffset.getBaselineOffsetAsDbl(), problems);
        }
        else if (FXG_VERTICALALIGN_ATTRIBUTE.equals(name))
        {
            verticalAlign = TextHelper.getVerticalAlign(this, value, verticalAlign, problems);
        } 
        else if (FXG_TEXTALIGN_ATTRIBUTE.equals(name))
        {
            textAlign = TextHelper.getTextAlign(this, value, textAlign, problems);
        }
        else if (FXG_TEXTALIGNLAST_ATTRIBUTE.equals(name))
        {
            textAlignLast = TextHelper.getTextAlign(this, value, textAlignLast, problems);
        }
        else if (FXG_TEXTINDENT_ATTRIBUTE.equals(name))
        {
            textIndent = DOMParserHelper.parseDouble(this, value, name, TEXTINDENT_MIN_INCLUSIVE, TEXTINDENT_MAX_INCLUSIVE, textIndent, problems);
        }
        else if (FXG_PARAGRAPHSTARTINDENT_ATTRIBUTE.equals(name))
        {
            paragraphStartIndent = DOMParserHelper.parseDouble(this, value, name, PARAGRAPH_INDENT_MIN_INCLUSIVE, PARAGRAPH_INDENT_MAX_INCLUSIVE, paragraphStartIndent, problems);
        }
        else if (FXG_PARAGRAPHENDINDENT_ATTRIBUTE.equals(name))
        {
            paragraphEndIndent = DOMParserHelper.parseDouble(this, value, name, PARAGRAPH_INDENT_MIN_INCLUSIVE, PARAGRAPH_INDENT_MAX_INCLUSIVE, paragraphEndIndent, problems);
        }
        else if (FXG_PARAGRAPHSPACEBEFORE_ATTRIBUTE.equals(name))
        {
            paragraphSpaceBefore = DOMParserHelper.parseDouble(this, value, name, PARAGRAPH_SPACE_MIN_INCLUSIVE, PARAGRAPH_SPACE_MAX_INCLUSIVE, paragraphSpaceBefore, problems);
        }
        else if (FXG_PARAGRAPHSPACEAFTER_ATTRIBUTE.equals(name))
        {
            paragraphSpaceAfter = DOMParserHelper.parseDouble(this, value, name, PARAGRAPH_SPACE_MIN_INCLUSIVE, PARAGRAPH_SPACE_MAX_INCLUSIVE, paragraphSpaceAfter, problems);
        }
        else if (FXG_DIRECTION_ATTRIBUTE.equals(name))
        {
            direction = TextHelper.getDirection(this, value, direction, problems);
        }
        else if (FXG_JUSTIFICATIONRULE_ATTRIBUTE.equals(name))
        {
            justificationRule = TextHelper.getJustificationRule(this, value, justificationRule, problems);
        }
        else if (FXG_JUSTIFICATIONSTYLE_ATTRIBUTE.equals(name))
        {
            justificationStyle = TextHelper.getJustificationStyle(this, value, justificationStyle, problems);
        }
        else if (FXG_TEXTJUSTIFY_ATTRIBUTE.equals(name))
        {
            textJustify = TextHelper.getTextJustify(this, value, textJustify, problems);
        }
        else if (FXG_LEADINGMODEL_ATTRIBUTE.equals(name))
        {
            leadingModel = TextHelper.getLeadingModel(this, value, leadingModel, problems);
        }        
        else if (FXG_TABSTOPS_ATTRIBUTE.equals(name))
        {
            tabStops = TextHelper.parseTabStops(this, value, tabStops, problems);
        } 
        else if (FXG_FONTFAMILY_ATTRIBUTE.equals(name))
        {
            fontFamily = value;
        }
        else if (FXG_FONTSIZE_ATTRIBUTE.equals(name))
        {
            fontSize = DOMParserHelper.parseDouble(this, value, name, FONTSIZE_MIN_INCLUSIVE, FONTSIZE_MAX_INCLUSIVE, fontSize, problems);
        }
        else if (FXG_FONTSTYLE_ATTRIBUTE.equals(name))
        {
            fontStyle = TextHelper.getFontStyle(this, value, fontStyle, problems);
        }
        else if (FXG_FONTWEIGHT_ATTRIBUTE.equals(name))
        {
            fontWeight = TextHelper.getFontWeight(this, value, fontWeight, problems);
        }
        else if (FXG_KERNING_ATTRIBUTE.equals(name))
        {
            kerning = TextHelper.getKerning(this, value, kerning, problems);
        }        
        else if (FXG_LINEHEIGHT_ATTRIBUTE.equals(name))
        {
            lineHeight = DOMParserHelper.parseNumberPercentWithSeparateRange(this, value, name, 
                    LINEHEIGHT_PIXEL_MIN_INCLUSIVE, LINEHEIGHT_PIXEL_MAX_INCLUSIVE,
                    LINEHEIGHT_PERCENT_MIN_INCLUSIVE, LINEHEIGHT_PERCENT_MAX_INCLUSIVE, lineHeight, problems); 
        }
        else if (FXG_TEXTDECORATION_ATTRIBUTE.equals(name))
        {
            textDecoration = TextHelper.getTextDecoration(this, value, textDecoration, problems);
        }
        else if ( FXG_LINETHROUGH_ATTRIBUTE.equals(name))
        {
            lineThrough = DOMParserHelper.parseBoolean(this, value, name, lineThrough, problems);
        }                   
        else if (FXG_COLOR_ATTRIBUTE.equals(name))
        {
            color = DOMParserHelper.parseRGB(this, value, name, color, problems);
        }
        else if (FXG_TEXTALPHA_ATTRIBUTE.equals(name))
        {
            textAlpha = DOMParserHelper.parseDouble(this, value, name, ALPHA_MIN_INCLUSIVE, ALPHA_MAX_INCLUSIVE, textAlpha, problems);
        }
        else if (FXG_WHITESPACECOLLAPSE_ATTRIBUTE.equals(name))
        {
            whiteSpaceCollapse = TextHelper.getWhiteSpaceCollapse(this, value, whiteSpaceCollapse, problems);
        }
        else if (FXG_BACKGROUNDALPHA_ATTRIBUTE.equals(name))
        {
        	backgroundAlpha = getAlphaInherit(this, name, value, ALPHA_MIN_INCLUSIVE, ALPHA_MAX_INCLUSIVE, backgroundAlpha.getNumberInheritAsDbl(), problems);
        }
        else if (FXG_BACKGROUNDCOLOR_ATTRIBUTE.equals(name))
        {
            backgroundColor = getColorWithEnum(this, name, value, backgroundColor.getColorWithEnumAsString(), problems);
        }
        else if (FXG_BASELINESHIFT_ATTRIBUTE.equals(name))
        {
            baselineShift = getBaselineShift(this, name, value, BASELINESHIFT_MIN_INCLUSIVE, BASELINESHIFT_MAX_INCLUSIVE, baselineShift.getBaselineShiftAsDbl(), problems);
        }
        else if (FXG_BREAKOPPORTUNITY_ATTRIBUTE.equals(name))
        {
            breakOpportunity = TextHelper.getBreakOpportunity(this, value, breakOpportunity, problems);
        }
        else if (FXG_DIGITCASE_ATTRIBUTE.equals(name))
        {
            digitCase = TextHelper.getDigitCase(this, value, digitCase, problems);
        }
        else if (FXG_DIGITWIDTH_ATTRIBUTE.equals(name))
        {
            digitWidth = TextHelper.getDigitWidth(this, value, digitWidth, problems);
        }
        else if (FXG_DOMINANTBASELINE_ATTRIBUTE.equals(name))
        {
            dominantBaseline = TextHelper.getDominantBaseline(this, value, dominantBaseline, problems);
        }
        else if (FXG_ALIGNMENTBASELINE_ATTRIBUTE.equals(name))
        {
            alignmentBaseline = TextHelper.getAlignmentBaseline(this, value, alignmentBaseline, problems);
        }
        else if (FXG_LIGATURELEVEL_ATTRIBUTE.equals(name))
        {
            ligatureLevel = TextHelper.getLigatureLevel(this, value, ligatureLevel, problems);
        }
        else if (FXG_LOCALE_ATTRIBUTE.equals(name))
        {
            locale = value;
        }
        else if (FXG_TYPOGRAPHICCASE_ATTRIBUTE.equals(name))
        {
            typographicCase = TextHelper.getTypographicCase(this, value, typographicCase, problems);
        }        
        else if (FXG_TRACKINGLEFT_ATTRIBUTE.equals(name))
        {
            trackingLeft = DOMParserHelper.parseNumberPercent(this, value, name, TRACKING_MIN_INCLUSIVE, TRACKING_MAX_INCLUSIVE, trackingLeft, problems);
        }
        else if (FXG_TRACKINGRIGHT_ATTRIBUTE.equals(name))
        {
            trackingRight = DOMParserHelper.parseNumberPercent(this, value, name, TRACKING_MIN_INCLUSIVE, TRACKING_MAX_INCLUSIVE, trackingRight, problems);
        } 
        else if (FXG_TEXTROTATION_ATTRIBUTE.equals(name))
        {
        	textRotation = TextHelper.getTextRotation(this, value, textRotation, problems);
        }
        else if (FXG_ID_ATTRIBUTE.equals(name))
        {
            //id = value;
        }        
        else
        {
        	super.setAttribute(name, value, problems);
        }

        // Remember that this attribute was set on this node.
        rememberAttribute(name, value);
    }

    //--------------------------------------------------------------------------
    //
    // Helper Methods
    //
    //--------------------------------------------------------------------------
    
    /**
     * Convert an FXG String value to a BaselineOffset object.
     * 
     * @param value - the FXG String value.
     * @param name - the FXG attribute name.
     * @param min - the smallest double value that the result must be greater
     * or equal to.
     * @param max - the largest double value that the result must be smaller
     * than or equal to.
     * @param defaultValue - the default double value; if the encountered minor 
     * version is later than the supported minor version and the attribute value
     *  is out-of-range, the default value is returned.
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching BaselineOffset rule.
     */
    private BaselineOffset getFirstBaselineOffset(IFXGNode node, String name, String value, double min, double max, double defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_BASELINEOFFSET_AUTO_VALUE.equals(value))
        {
            return BaselineOffset.newInstance(BaselineOffsetAsEnum.AUTO);
        }
        else if (FXG_BASELINEOFFSET_ASCENT_VALUE.equals(value))
        {
            return BaselineOffset.newInstance(BaselineOffsetAsEnum.ASCENT);
        }
        else if (FXG_BASELINEOFFSET_LINEHEIGHT_VALUE.equals(value))
        {
            return BaselineOffset.newInstance(BaselineOffsetAsEnum.LINEHEIGHT);
        }
        else
        {
        	try
        	{
        		return BaselineOffset.newInstance(DOMParserHelper.parseDouble(this, value, min, max));
        	}
        	catch(Exception e)
        	{
	            //Exception: Unknown first baseline offset: {0}
                problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), name, value)); 
	            return BaselineOffset.newInstance(defaultValue);
        	}
        }
    }
    
    /**
     * Convert an FXG String value to a NumberAuto object.
     * 
     * @param value - the FXG String value.
     * @param name - the FXG attribute name.
     * @param min - the smallest double value that the result must be greater
     * or equal to.
     * @param max - the largest double value that the result must be smaller
     * than or equal to.
     * @param defaultValue - the default double value; if the encountered minor 
     * version is later than the supported minor version and the attribute value
     *  is out-of-range, the default value is returned.
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching NumberAuto rule.
     */
    private NumberAuto getNumberAutoDbl(IFXGNode node, String name, String value, double min, double max, double defaultValue, Collection<ICompilerProblem> problems)
    {
        try
        {
            return NumberAuto.newInstance(DOMParserHelper.parseDouble(this, value, min, max));            
        }
        catch(Exception e)
        {
            if (FXG_NUMBERAUTO_AUTO_VALUE.equals(value))
                return NumberAuto.newInstance(NumberAutoAsEnum.AUTO);
            else if (FXG_INHERIT_VALUE.equals(value))
            	return NumberAuto.newInstance(NumberAutoAsEnum.INHERIT);
            
            //Exception: Unknown number auto: {0}
            problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), name, value));    
            return NumberAuto.newInstance(defaultValue);
        }
    }
    
    /**
     * Convert an FXG String value to a NumberAuto object.
     * 
     * @param value - the FXG String value.
     * @param name - the FXG attribute name.
     * @param min - the smallest int value that the result must be greater
     * or equal to.
     * @param max - the largest int value that the result must be smaller
     * than or equal to.
     * @param defaultValue - the default int value; if the encountered minor 
     * version is later than the supported minor version and the attribute value
     *  is out-of-range, the default value is returned.
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching NumberAuto rule.
     */
    private NumberAuto getNumberAutoInt(IFXGNode node, String name, String value, int min, int max, int defaultValue, Collection<ICompilerProblem> problems)
    {
        try
        {
            return NumberAuto.newInstance(DOMParserHelper.parseInt(this, value, min, max));            
        }
        catch(Exception e)
        {
            if (FXG_NUMBERAUTO_AUTO_VALUE.equals(value))
                return NumberAuto.newInstance(NumberAutoAsEnum.AUTO);
            else if (FXG_INHERIT_VALUE.equals(value))
            	return NumberAuto.newInstance(NumberAutoAsEnum.INHERIT);
            
            //Exception: Unknown number auto: {0}
            problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), name, value)); 
            
            return NumberAuto.newInstance(defaultValue);
        }
    }
    
    /**
     * Convert an FXG String value to a NumberInherit enumeration.
     * 
     * @param value - the FXG String value.
     * @param name - the FXG attribute name.
     * @param min - the smallest double value that the result must be greater
     * or equal to.
     * @param max - the largest double value that the result must be smaller
     * than or equal to.
     * @param defaultValue - the default double value; if the encountered minor 
     * version is later than the supported minor version and the attribute value
     *  is out-of-range, the default value is returned.
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching NumberInherit rule.
     */
    private NumberInherit getNumberInherit(IFXGNode node, String name, String value, double min, double max, double defaultValue, Collection<ICompilerProblem> problems)
    {
        try
        {
            return NumberInherit.newInstance(DOMParserHelper.parseDouble(this, value, min, max));            
        }
        catch(Exception e)
        {
            if (FXG_INHERIT_VALUE.equals(value))
                return NumberInherit.newInstance(NumberInheritAsEnum.INHERIT);
            
            //Exception: Unknown number inherit: {0}
            problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), name, value));    
            
            return NumberInherit.newInstance(defaultValue);
        }
    }
    
    /**
     * Convert an FXG String value to a BaselineShift enumeration.
     * 
     * @param value - the FXG String value.
     * @param name - the FXG attribute name.
     * @param min - the smallest double value that the result must be greater
     * or equal to.
     * @param max - the largest double value that the result must be smaller
     * than or equal to.
     * @param defaultValue - the default double value; if the encountered minor 
     * version is later than the supported minor version and the attribute value
     *  is out-of-range, the default value is returned.
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching BaselineShift rule.
     */
    private BaselineShift getBaselineShift(IFXGNode node, String name, String value, double min, double max, double defaultValue, Collection<ICompilerProblem> problems)
    {
        try
        {
        	
            return BaselineShift.newInstance(DOMParserHelper.parseNumberPercent(this, value, min, max));            
        }
        catch(Exception e)
        {
            if (FXG_BASELINESHIFT_SUPERSCRIPT_VALUE.equals(value))
            {
                return BaselineShift.newInstance(BaselineShiftAsEnum.SUPERSCRIPT);
            }
            else if (FXG_BASELINESHIFT_SUBSCRIPT_VALUE.equals(value))
            {
                return BaselineShift.newInstance(BaselineShiftAsEnum.SUBSCRIPT);
            }

            //Exception: Unknown baseline shift: {0}
            problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), name, value));    
            
            return BaselineShift.newInstance(defaultValue);
        }
    }
    
    /**
     * Convert an FXG String value to a NumberInherit object.
     * 
     * @param value - the FXG String value.
     * @param name - the FXG attribute name.
     * @param min - the smallest double value that the result must be greater
     * or equal to.
     * @param max - the largest double value that the result must be smaller
     * than or equal to.
     * @param defaultValue - the default double value; if the encountered minor 
     * version is later than the supported minor version and the attribute value
     *  is out-of-range, the default value is returned.
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching NumberInherit rule.
     */
    private NumberInherit getAlphaInherit(IFXGNode node, String name, String value, double min, double max, double defaultValue, Collection<ICompilerProblem> problems)        
    {
        try
        {
            return NumberInherit.newInstance(DOMParserHelper.parseDouble(this, value, ALPHA_MIN_INCLUSIVE, ALPHA_MAX_INCLUSIVE));           
        }
        catch(Exception e)
        {
            if (FXG_INHERIT_VALUE.equals(value))
            {
                return NumberInherit.newInstance(NumberInheritAsEnum.INHERIT);
            }
            
            //Exception: Unknown number inherit: {0}
            problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), name, value)); 
            
            return NumberInherit.newInstance(defaultValue);
        }
    }
    
    /**
     * Convert an FXG String value to a NumberInherit object.
     * 
     * @param node - the FXG node.
     * @param attribute - the FXG attribute name.
     * @param value - the FXG String value.
     * @param defaultValue - default color value
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching NumberInherit rule.
     */
    private ColorWithEnum getColorWithEnum(IFXGNode node, String attribute, String value, int defaultValue, Collection<ICompilerProblem> problems)        
    {
        if (FXG_COLORWITHENUM_TRANSPARENT_VALUE.equals(value))
        {
            return ColorWithEnum.newInstance(ColorEnum.TRANSPARENT);
        }
        else if (FXG_INHERIT_VALUE.equals(value))
        {
            return ColorWithEnum.newInstance(ColorEnum.INHERIT);
        }
        else
        {
            return ColorWithEnum.newInstance(DOMParserHelper.parseRGB(this, value, attribute, defaultValue, problems));           
        }
    }
}

