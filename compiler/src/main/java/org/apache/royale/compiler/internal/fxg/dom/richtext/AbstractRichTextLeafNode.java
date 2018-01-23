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

package org.apache.royale.compiler.internal.fxg.dom.richtext;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

import java.util.Collection;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.AbstractFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.DOMParserHelper;
import org.apache.royale.compiler.internal.fxg.dom.types.AlignmentBaseline;
import org.apache.royale.compiler.internal.fxg.dom.types.BaselineShift;
import org.apache.royale.compiler.internal.fxg.dom.types.BreakOpportunity;
import org.apache.royale.compiler.internal.fxg.dom.types.ColorWithEnum;
import org.apache.royale.compiler.internal.fxg.dom.types.DigitCase;
import org.apache.royale.compiler.internal.fxg.dom.types.DigitWidth;
import org.apache.royale.compiler.internal.fxg.dom.types.DominantBaseline;
import org.apache.royale.compiler.internal.fxg.dom.types.FontStyle;
import org.apache.royale.compiler.internal.fxg.dom.types.FontWeight;
import org.apache.royale.compiler.internal.fxg.dom.types.Kerning;
import org.apache.royale.compiler.internal.fxg.dom.types.LigatureLevel;
import org.apache.royale.compiler.internal.fxg.dom.types.NumberInherit;
import org.apache.royale.compiler.internal.fxg.dom.types.TextDecoration;
import org.apache.royale.compiler.internal.fxg.dom.types.TextRotation;
import org.apache.royale.compiler.internal.fxg.dom.types.TypographicCase;
import org.apache.royale.compiler.internal.fxg.dom.types.WhiteSpaceCollapse;
import org.apache.royale.compiler.internal.fxg.dom.types.BaselineShift.BaselineShiftAsEnum;
import org.apache.royale.compiler.internal.fxg.dom.types.ColorWithEnum.ColorEnum;
import org.apache.royale.compiler.internal.fxg.dom.types.NumberInherit.NumberInheritAsEnum;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;


/**
 * A base text left node class that have character formatting.
 */
public abstract class AbstractRichTextLeafNode extends AbstractRichTextNode
{
    protected static final double FONTSIZE_MIN_INCLUSIVE = 1.0;
    protected static final double FONTSIZE_MAX_INCLUSIVE = 720.0;
    protected static final double BASELINESHIFT_MIN_INCLUSIVE = -1000.0;
    protected static final double BASELINESHIFT_MAX_INCLUSIVE = 1000.0; 
    protected static final double LINEHEIGHT_PERCENT_MIN_INCLUSIVE = -1000.0;
    protected static final double LINEHEIGHT_PERCENT_MAX_INCLUSIVE = 1000.0; 
    protected static final double LINEHEIGHT_PIXEL_MIN_INCLUSIVE = -720.0;
    protected static final double LINEHEIGHT_PIXEL_MAX_INCLUSIVE = 720.0; 
    protected static final double TRACKING_MIN_INCLUSIVE = -1000.0;
    protected static final double TRACKING_MAX_INCLUSIVE = 1000.0;     

    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

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
    
    /**
     * This implementation processes text leaf attributes that are common to
     * &lt;RichText&gt;, &lt;p&gt;, and &lt;span&gt;.
     * <p>
     * The right hand side of an ActionScript assignment is generated for
     * each property based on the expected type of the attribute.
     * </p>
     * <p>
     * Text leaf attributes include:
     * <ul>
     * <li><b>fontFamily</b> (String): The font family name used to render the
     * text. Default value is Times New Roman (Times on Mac OS X).</li>
     * <li><b>fontSize</b> (Number): The size of the glyphs that is used to
     * render the text, specified in point sizes. Default is 12. Minimum 1
     * point. Maximum 500 points.</li>
     * <li><b>fontStyle</b> (String): [normal, italic] The style of the glyphs
     * that is used to render the text. Legal values are 'normal' and 'italic'.
     * Default is normal.</li>
     * <li><b>fontWeight</b> (String): [normal, bold] The boldness or lightness
     * of the glyphs that is used to render the text. Default is normal.</li>
     * <li><b>lineHeight</b> (Percent | Number): The leading, or the distance
     * from the previous line's baseline to this one, in points. Default is
     * 120%. Minimum value for percent or number is 0.</li>
     * <li><b>tracking</b> (Percent): Space added to the advance after each
     * character, as a percentage of the current point size. Percentages can be
     * negative, to bring characters closer together. Default is 0.</li>
     * <li><b>textDecoration</b> (String): [none, underline]: The decoration to
     * apply to the text. Default is none.</li>
     * <li><b>lineThrough</b> (Boolean): true if text has strikethrough applied,
     * false otherwise. Default is false.</li>
     * <li><b>color</b> (Color): The color of the text. Default is 0x000000.</li>
     * <li><b>textAlpha</b> (alpha): The alpha value applied to the text.
     * Default is 1.0.</li>
     * <li><b>whiteSpaceCollapse</b> (String): [preserve, collapse] This is an
     * enumerated value. A value of "collapse" converts line feeds, newlines,
     * and tabs to spaces and collapses adjacent spaces to one. Leading and
     * trailing whitespace is trimmed. A value of "preserve" passes whitespace
     * through unchanged.</li>
     * <li><b>kerning</b> (String): [on, off, auto] If on, pair kerns are
     * honored. If off, there is no font-based kerning applied. If auto,
     * kerning is applied to all characters except Kanji, Hiragana or Katakana.
     * The default is auto.</li>
     * </ul>
     * </p>
     * @param name - the unqualified attribute name
     * @param value - the attribute value
     */
    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
    	if (FXG_FONTFAMILY_ATTRIBUTE.equals(name))
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
        else
        {
            super.setAttribute(name, value, problems);
            return;
        }
        
        // Remember attribute was set on this node.
        rememberAttribute(name, value);
    }

    //--------------------------------------------------------------------------
    //
    // Helper Methods
    //
    //--------------------------------------------------------------------------

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
                
            //Unknown baseline shift.
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

            //Unknown number inherit
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
