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

package org.apache.royale.compiler.internal.fxg.dom.text;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

import java.util.Collection;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.DOMParserHelper;
import org.apache.royale.compiler.internal.fxg.dom.GraphicNode;
import org.apache.royale.compiler.internal.fxg.dom.types.Kerning;
import org.apache.royale.compiler.internal.fxg.dom.types.LineBreak;
import org.apache.royale.compiler.internal.fxg.dom.types.WhiteSpaceCollapse;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * A base class for text nodes that have character formatting.
 */
public abstract class AbstractCharacterTextNode extends AbstractTextNode
{
    protected static final double FONTSIZE_MIN_INCLUSIVE = 1.0;
    protected static final double FONTSIZE_MAX_INCLUSIVE = 720.0;

    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    // Character Attributes
    public String fontFamily = "Times New Roman";
    public double fontSize = 12.0;
    public String fontStyle = "normal";
    public String fontWeight = "normal";
    public double lineHeight = 120.0;
    public String textDecoration = "none";
    public WhiteSpaceCollapse whiteSpaceCollapse = WhiteSpaceCollapse.PRESERVE;
    public LineBreak lineBreak = LineBreak.TOFIT;
    public double tracking = 0.0;
    public Kerning kerning = Kerning.AUTO;
    public double textAlpha = 1.0;
    public int color = COLOR_BLACK;
    public boolean lineThrough = false;

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * This implementation processes character attributes that are common to
     * &lt;TextGraphic&gt;, &lt;p&gt;, and &lt;span&gt;.
     * <p>
     * The right hand side of an ActionScript assignment is generated for
     * each property based on the expected type of the attribute.
     * </p>
     * <p>
     * Character attributes include:
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
     * <li><b>lineBreak</b> (String): [toFit, explicit] This is an enumeration.
     * A value of "toFit" wraps the lines at the edge of the enclosing
     * TextGraphic. A value of "explicit" breaks the lines only at a Unicode
     * line end character (such as a newline or line separator). toFit is the
     * default.</li>
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
            whiteSpaceCollapse = getWhiteSpaceCollapse(this, value, problems);
        }
        else if (FXG_LINEBREAK_ATTRIBUTE.equals(name))
        {
            lineBreak = getLineBreak(this, value, problems);
        }
        else if (FXG_TRACKING_ATTRIBUTE.equals(name))
        {
            tracking = DOMParserHelper.parsePercent(this, value, name, tracking, problems);
        }
        else if (FXG_KERNING_ATTRIBUTE.equals(name))
        {
            kerning = getKerning(this, value, problems);
        }
        else if (FXG_TEXTALPHA_ATTRIBUTE.equals(name))
        {
            textAlpha = DOMParserHelper.parseDouble(this, value, name, ALPHA_MIN_INCLUSIVE, ALPHA_MAX_INCLUSIVE, textAlpha, problems);
         }
        else if (FXG_COLOR_ATTRIBUTE.equals(name))
        {
            color = DOMParserHelper.parseRGB(this, value, name, color, problems);
        }
        else if (FXG_LINETHROUGH_ATTRIBUTE.equals(name))
        {
            lineThrough = DOMParserHelper.parseBoolean(this, value, name, lineThrough, problems);
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
     * Convert an FXG String value to a Kerning enumeration.
     * 
     * @param value - the FXG String value.
     * @return the matching Kerning value.
     */
    public static Kerning getKerning(IFXGNode node, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_KERNING_AUTO_VALUE.equals(value))
            return Kerning.AUTO;
        else if (FXG_KERNING_ON_VALUE.equals(value))
            return Kerning.ON;
        else if (FXG_KERNING_OFF_VALUE.equals(value))
            return Kerning.OFF;
        
        //string did not match a known Kerning value.
        problems.add(new FXGUnknownAttributeValueProblem(((GraphicNode)node.getDocumentNode()).getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_KERNING_ATTRIBUTE, value));
        
        return Kerning.AUTO;
    }

    /**
     * Convert an FXG String value to a LineBreak enumeration.
     * 
     * @param value - the FXG String value.
     * @return the matching LineBreak rule.
     */
    public static LineBreak getLineBreak(IFXGNode node, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_LINEBREAK_TOFIT_VALUE.equals(value))
            return LineBreak.TOFIT;
        else if (FXG_LINEBREAK_EXPLICIT_VALUE.equals(value))
            return LineBreak.EXPLICIT;
        
        //string did not match a known LineBreak rule.
        problems.add(new FXGUnknownAttributeValueProblem(((GraphicNode)node.getDocumentNode()).getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_LINEBREAK_ATTRIBUTE, value));
        
        return LineBreak.TOFIT;
    }

    /**
     * Convert an FXG String value to a WhiteSpaceCollapse enumeration.
     * 
     * @param value - the FXG String value.
     * @return the matching WhiteSpaceCollapse rule.
     */
    public static WhiteSpaceCollapse getWhiteSpaceCollapse(IFXGNode node, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_WHITESPACE_PRESERVE_VALUE.equals(value))
            return WhiteSpaceCollapse.PRESERVE;
        else if (FXG_WHITESPACE_COLLAPSE_VALUE.equals(value))
            return WhiteSpaceCollapse.COLLAPSE;
        
        //string did not match a known WhiteSpaceCollapse rule.
        problems.add(new FXGUnknownAttributeValueProblem(((GraphicNode)node.getDocumentNode()).getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_WHITESPACECOLLAPSE_ATTRIBUTE, value));
        
        return WhiteSpaceCollapse.PRESERVE;
    }

}
