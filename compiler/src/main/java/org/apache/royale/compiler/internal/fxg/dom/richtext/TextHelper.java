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

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.types.AlignmentBaseline;
import org.apache.royale.compiler.internal.fxg.dom.types.BlockProgression;
import org.apache.royale.compiler.internal.fxg.dom.types.BreakOpportunity;
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
import org.apache.royale.compiler.internal.fxg.dom.types.TextAlign;
import org.apache.royale.compiler.internal.fxg.dom.types.TextDecoration;
import org.apache.royale.compiler.internal.fxg.dom.types.TextJustify;
import org.apache.royale.compiler.internal.fxg.dom.types.TextRotation;
import org.apache.royale.compiler.internal.fxg.dom.types.TypographicCase;
import org.apache.royale.compiler.internal.fxg.dom.types.VerticalAlign;
import org.apache.royale.compiler.internal.fxg.dom.types.WhiteSpaceCollapse;
import org.apache.royale.compiler.problems.FXGInvalidTabStopsProblem;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Utilities to help create Text.
 */
public class TextHelper
{
    protected static final double ALPHA_MIN_INCLUSIVE = 0.0;
    protected static final double ALPHA_MAX_INCLUSIVE = 1.0;

    protected static Pattern whitespacePattern = Pattern.compile ("(\\s+)");
    protected static Pattern tabstopsExceptDNumericPattern = Pattern.compile ("([S s E e C c]?[0-9]*[.]?[0-9]*\\s*)");
    protected static Pattern tabstopsExceptDScientificPattern = Pattern.compile ("([S s E e C c]?[+ -]?[0-9]*[.][0-9]*[E e][+ -]?[0-9]+\\s*)");
    protected static Pattern tabstopsDNumericPattern = Pattern.compile ("([D d][0-9]*[.]?[0-9]*([|].+)?\\s*)");
    protected static Pattern tabstopsDScientificPattern = Pattern.compile ("([D d][-]?[0-9]*[.][0-9]*[E e][+][0-9]*([|].+)?\\s*)");    
    protected static Pattern tabstopsNumberPattern = Pattern.compile ("([+ -]?[0-9]*[.]?[0-9]*[E e]?[+ -]?[0-9]*)");
    
	/**
	 * Determine if a string contains only ignorable white spaces.
	 * 
	 * @param value - value to be checked.
	 * @return true if value contains only ignorable white spaces, else, return false.
	 */
	public static boolean ignorableWhitespace(String value)
    {
        Matcher m;

        m = whitespacePattern.matcher(value);
        if (m.matches ())
            return true; 
        else
            return false;
    }
	
    //--------------------------------------------------------------------------
    //
    // Text Leaf Attribute Helper Methods
    //
    //--------------------------------------------------------------------------
	
    /**
     * Convert an FXG String value to a FontStyle enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching FontStyle value.
     */
    public static FontStyle getFontStyle(IFXGNode node, String value, FontStyle defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_FONTSTYLE_NORMAL_VALUE.equals(value))
            return FontStyle.NORMAL;
        else if (FXG_FONTSTYLE_ITALIC_VALUE.equals(value))
            return FontStyle.ITALIC;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_FONTSTYLE_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to a FontWeight enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching FontWeight value.
     */
    public static FontWeight getFontWeight(IFXGNode node, String value, FontWeight defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_FONTWEIGHT_NORMAL_VALUE.equals(value))
            return FontWeight.NORMAL;
        else if (FXG_FONTWEIGHT_BOLD_VALUE.equals(value))
            return FontWeight.BOLD;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_FONTWEIGHT_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to a TextDecoration enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching TextDecoration value.
     */
    public static TextDecoration getTextDecoration(IFXGNode node, String value, TextDecoration defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_TEXTDECORATION_NONE_VALUE.equals(value))
            return TextDecoration.NONE;
        else if (FXG_TEXTDECORATION_UNDERLINE_VALUE.equals(value))
            return TextDecoration.UNDERLINE;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_TEXTDECORATION_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to a Kerning enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching Kerning value.
     */
    public static Kerning getKerning(IFXGNode node, String value, Kerning defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_KERNING_AUTO_VALUE.equals(value))
            return Kerning.AUTO;
        else if (FXG_KERNING_ON_VALUE.equals(value))
            return Kerning.ON;
        else if (FXG_KERNING_OFF_VALUE.equals(value))
            return Kerning.OFF;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_KERNING_ATTRIBUTE, value));
        
        return defaultValue;
    }

     /**
     * Convert an FXG String value to a WhiteSpaceCollapse enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching WhiteSpaceCollapse rule.
     */
    public static WhiteSpaceCollapse getWhiteSpaceCollapse(IFXGNode node, String value, WhiteSpaceCollapse defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_WHITESPACE_PRESERVE_VALUE.equals(value))
            return WhiteSpaceCollapse.PRESERVE;
        else if (FXG_WHITESPACE_COLLAPSE_VALUE.equals(value))
            return WhiteSpaceCollapse.COLLAPSE;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_WHITESPACECOLLAPSE_ATTRIBUTE, value));
        
        return defaultValue;
    }

    /**
     * Convert an FXG String value to a BreakOpportunity enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching BreakOpportunity rule.
     */
    public static BreakOpportunity getBreakOpportunity(IFXGNode node, String value, BreakOpportunity defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_BREAKOPPORTUNITY_AUTO_VALUE.equals(value))
            return BreakOpportunity.AUTO;
        else if (FXG_BREAKOPPORTUNITY_ANY_VALUE.equals(value))
            return BreakOpportunity.ANY;
        else if (FXG_BREAKOPPORTUNITY_NONE_VALUE.equals(value))
            return BreakOpportunity.NONE;
        else if (FXG_BREAKOPPORTUNITY_ALL_VALUE.equals(value))
            return BreakOpportunity.ALL;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_BREAKOPPORTUNITY_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to a DigitCase enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching DigitCase rule.
     */
    public static DigitCase getDigitCase(IFXGNode node, String value, DigitCase defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_DIGITCASE_DEFAULT_VALUE.equals(value))
            return DigitCase.DEFAULT;
        else if (FXG_DIGITCASE_LINING_VALUE.equals(value))
            return DigitCase.LINING;
        else if (FXG_DIGITCASE_OLDSTYLE_VALUE.equals(value))
            return DigitCase.OLDSTYLE;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_DIGITCASE_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to a DigitWidth enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching DigitWidth rule.
     */
    public static DigitWidth getDigitWidth(IFXGNode node, String value, DigitWidth defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_DIGITWIDTH_DEFAULT_VALUE.equals(value))
            return DigitWidth.DEFAULT;
        else if (FXG_DIGITWIDTH_PROPORTIONAL_VALUE.equals(value))
            return DigitWidth.PROPORTIONAL;
        else if (FXG_DIGITWIDTH_TABULAR_VALUE.equals(value))
            return DigitWidth.TABULAR;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_DIGITWIDTH_ATTRIBUTE, value));
        
        return defaultValue;
    }

    /**
     * Convert an FXG String value to a DominantBaseline enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching DominantBaseline rule.
     */
    public static DominantBaseline getDominantBaseline(IFXGNode node, String value, DominantBaseline defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_DOMINANTBASELINE_AUTO_VALUE.equals(value))
            return DominantBaseline.AUTO;
        else if (FXG_DOMINANTBASELINE_ROMAN_VALUE.equals(value))
            return DominantBaseline.ROMAN;
        else if (FXG_DOMINANTBASELINE_ASCENT_VALUE.equals(value))
            return DominantBaseline.ASCENT;
        else if (FXG_DOMINANTBASELINE_DESCENT_VALUE.equals(value))
            return DominantBaseline.DESCENT;
        else if (FXG_DOMINANTBASELINE_IDEOGRAPHICTOP_VALUE.equals(value))
            return DominantBaseline.IDEOGRAPHICTOP;
        else if (FXG_DOMINANTBASELINE_IDEOGRAPHICCENTER_VALUE.equals(value))
            return DominantBaseline.IDEOGRAPHICCENTER;
        else if (FXG_DOMINANTBASELINE_IDEOGRAPHICBOTTOM_VALUE.equals(value))
            return DominantBaseline.IDEOGRAPHICBOTTOM;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_DOMINANTBASELINE_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to a AlignmentBaseline enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching AlignmentBaseline rule.
     */
    public static AlignmentBaseline getAlignmentBaseline(IFXGNode node, String value, AlignmentBaseline defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_ALIGNMENTBASELINE_USEDOMINANTBASELINE_VALUE.equals(value))
            return AlignmentBaseline.USEDOMINANTBASELINE;
        else if (FXG_ALIGNMENTBASELINE_ROMAN_VALUE.equals(value))
            return AlignmentBaseline.ROMAN;
        else if (FXG_ALIGNMENTBASELINE_ASCENT_VALUE.equals(value))
            return AlignmentBaseline.ASCENT;
        else if (FXG_ALIGNMENTBASELINE_DESCENT_VALUE.equals(value))
            return AlignmentBaseline.DESCENT;
        else if (FXG_ALIGNMENTBASELINE_IDEOGRAPHICTOP_VALUE.equals(value))
            return AlignmentBaseline.IDEOGRAPHICTOP;
        else if (FXG_ALIGNMENTBASELINE_IDEOGRAPHICCENTER_VALUE.equals(value))
            return AlignmentBaseline.IDEOGRAPHICCENTER;
        else if (FXG_ALIGNMENTBASELINE_IDEOGRAPHICBOTTOM_VALUE.equals(value))
            return AlignmentBaseline.IDEOGRAPHICBOTTOM;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_ALIGNMENTBASELINE_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to a LigatureLevel enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching LigatureLevel rule.
     */
    public static LigatureLevel getLigatureLevel(IFXGNode node, String value, LigatureLevel defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_LIGATURELEVEL_MINIMUM_VALUE.equals(value))
            return LigatureLevel.MINIMUM;
        else if (FXG_LIGATURELEVEL_COMMON_VALUE.equals(value))
            return LigatureLevel.COMMON;
        else if (FXG_LIGATURELEVEL_UNCOMMON_VALUE.equals(value))
            return LigatureLevel.UNCOMMON;
        else if (FXG_LIGATURELEVEL_EXOTIC_VALUE.equals(value))
            return LigatureLevel.EXOTIC;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_LIGATURELEVEL_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    
    /**
     * Convert an FXG String value to a TypographicCase enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching TypographicCase rule.
     */
    public static TypographicCase getTypographicCase(IFXGNode node, String value, TypographicCase defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_TYPOGRAPHICCASE_DEFAULT_VALUE.equals(value))
            return TypographicCase.DEFAULT;
        else if (FXG_TYPOGRAPHICCASE_CAPSTOSMALLCAPS_VALUE.equals(value))
            return TypographicCase.CAPSTOSMALLCAPS;
        else if (FXG_TYPOGRAPHICCASE_UPPERCASE_VALUE.equals(value))
            return TypographicCase.UPPERCASE;
        else if (FXG_TYPOGRAPHICCASE_LOWERCASE_VALUE.equals(value))
            return TypographicCase.LOWERCASE;
        else if (FXG_TYPOGRAPHICCASE_LOWERCASETOSMALLCAPS_VALUE.equals(value))
            return TypographicCase.LOWERCASETOSMALLCAPS;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_TYPOGRAPHICCASE_ATTRIBUTE, value));
        
        return defaultValue;
    }
           
    /**
     * Convert an FXG String value to a TextRotation enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching TextRotation rule.
     */
    public static TextRotation getTextRotation(IFXGNode node, String value, TextRotation defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_TEXTROTATION_AUTO_VALUE.equals(value))
            return TextRotation.AUTO;
        else if (FXG_TEXTROTATION_ROTATE_0_VALUE.equals(value))
            return TextRotation.ROTATE_0;
        else if (FXG_TEXTROTATION_ROTATE_90_VALUE.equals(value))
            return TextRotation.ROTATE_90;
        else if (FXG_TEXTROTATION_ROTATE_180_VALUE.equals(value))
            return TextRotation.ROTATE_180;
        else if (FXG_TEXTROTATION_ROTATE_270_VALUE.equals(value))
            return TextRotation.ROTATE_270;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_TEXTROTATION_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    //--------------------------------------------------------------------------
    //
    // Text Paragraph Attribute Helper Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Convert an FXG String value to a TextAlign enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching TextAlign rule.
     */
    public static TextAlign getTextAlign(IFXGNode node, String value, TextAlign defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_TEXTALIGN_START_VALUE.equals(value))
            return TextAlign.START;
        else if (FXG_TEXTALIGN_END_VALUE.equals(value))
            return TextAlign.END;
        else if (FXG_TEXTALIGN_LEFT_VALUE.equals(value))
            return TextAlign.LEFT;
        else if (FXG_TEXTALIGN_CENTER_VALUE.equals(value))
            return TextAlign.CENTER;
        else if (FXG_TEXTALIGN_RIGHT_VALUE.equals(value))
            return TextAlign.RIGHT;
        else if (FXG_TEXTALIGN_JUSTIFY_VALUE.equals(value))
            return TextAlign.JUSTIFY;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_TEXTALIGN_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to a Direction enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching Direction rule.
     */
    public static Direction getDirection(IFXGNode node, String value, Direction defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_DIRECTION_LTR_VALUE.equals(value))
            return Direction.LTR;
        else if (FXG_DIRECTION_RTL_VALUE.equals(value))
            return Direction.RTL;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_DIRECTION_ATTRIBUTE, value));
        
        return defaultValue;
    }

    /**
     * Convert an FXG String value to a JustificationRule enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching JustificationRule rule.
     */
    public static JustificationRule getJustificationRule(IFXGNode node, String value, JustificationRule defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_JUSTIFICATIONRULE_AUTO_VALUE.equals(value))
            return JustificationRule.AUTO;
        else if (FXG_JUSTIFICATIONRULE_SPACE_VALUE.equals(value))
            return JustificationRule.SPACE;
        else if (FXG_JUSTIFICATIONRULE_EASTASIAN_VALUE.equals(value))
            return JustificationRule.EASTASIAN;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_JUSTIFICATIONRULE_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to a JustificationStyle enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching JustificationStyle rule.
     */
    public static JustificationStyle getJustificationStyle(IFXGNode node, String value, JustificationStyle defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_JUSTIFICATIONSTYLE_AUTO_VALUE.equals(value))
            return JustificationStyle.AUTO;
        else if (FXG_JUSTIFICATIONSTYLE_PRIORITIZELEASTADJUSTMENT_VALUE.equals(value))
            return JustificationStyle.PRIORITIZELEASTADJUSTMENT;
        else if (FXG_JUSTIFICATIONSTYLE_PUSHINKINSOKU_VALUE.equals(value))
            return JustificationStyle.PUSHINKINSOKU;
        else if (FXG_JUSTIFICATIONSTYLE_PUSHOUTONLY_VALUE.equals(value))
            return JustificationStyle.PUSHOUTONLY;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_JUSTIFICATIONSTYLE_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to a TextJustify enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching TextJustify rule.
     */
    public static TextJustify getTextJustify(IFXGNode node, String value, TextJustify defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_TEXTJUSTIFY_INTERWORD_VALUE.equals(value))
            return TextJustify.INTERWORD;
        else if (FXG_TEXTJUSTIFY_DISTRIBUTE_VALUE.equals(value))
            return TextJustify.DISTRIBUTE;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_TEXTJUSTIFY_ATTRIBUTE, value));
        
        return defaultValue;
    }    
    
    /**
     * Convert an FXG String value to a LeadingModel enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching LeadingModel rule.
     */
    public static LeadingModel getLeadingModel(IFXGNode node, String value, LeadingModel defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_LEADINGMODEL_AUTO_VALUE.equals(value))
            return LeadingModel.AUTO;
        else if (FXG_LEADINGMODEL_ROMANUP_VALUE.equals(value))
            return LeadingModel.ROMANUP;
        else if (FXG_LEADINGMODEL_IDEOGRAPHICTOPUP_VALUE.equals(value))
            return LeadingModel.IDEOGRAPHICTOPUP;
        else if (FXG_LEADINGMODEL_IDEOGRAPHICCENTERUP_VALUE.equals(value))
            return LeadingModel.IDEOGRAPHICCENTERUP;
        else if (FXG_LEADINGMODEL_ASCENTDESCENTUP_VALUE.equals(value))
            return LeadingModel.ASCENTDESCENTUP;
        else if (FXG_LEADINGMODEL_IDEOGRAPHICTOPDOWN_VALUE.equals(value))
            return LeadingModel.IDEOGRAPHICTOPDOWN;
        else if (FXG_LEADINGMODEL_IDEOGRAPHICCENTERDOWN_VALUE.equals(value))
            return LeadingModel.IDEOGRAPHICCENTERDOWN;
        else if (FXG_LEADINGMODEL_APPROXIMATETEXTFIELD_VALUE.equals(value))
            return LeadingModel.APPROXIMATETEXTFIELD;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_LEADINGMODEL_ATTRIBUTE, value));
        
        return defaultValue;
    }   
    
    //--------------------------------------------------------------------------
    //
    // Text Flow Attribute Helper Methods
    //
    //--------------------------------------------------------------------------
    
    /**
     * Convert an FXG String value to a BlockProgression enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching BlockProgression enum.
     */
    public static BlockProgression getBlockProgression(IFXGNode node, String value, BlockProgression defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_BLOCKPROGRESSION_TB_VALUE.equals(value))
            return BlockProgression.TB;
        else if (FXG_BLOCKPROGRESSION_RL_VALUE.equals(value))
            return BlockProgression.RL;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_BLOCKPROGRESSION_ATTRIBUTE, value));
        
        return defaultValue;
    }     
    
    /**
     * Convert an FXG String value to a LineBreak enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching LineBreak enum.
     */
    public static LineBreak getLineBreak(IFXGNode node, String value, LineBreak defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_LINEBREAK_TOFIT_VALUE.equals(value))
        {
            return LineBreak.TOFIT;
        }
        else if (FXG_LINEBREAK_EXPLICIT_VALUE.equals(value))
        {
            return LineBreak.EXPLICIT;
        }
        else if (FXG_INHERIT_VALUE.equals(value))
        {
            return LineBreak.INHERIT;
        }
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_LINEBREAK_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to a VerticalAlign enumeration.
     * 
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching VerticalAlign rule.
     */
    public static VerticalAlign getVerticalAlign(IFXGNode node, String value, VerticalAlign defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_VERTICALALIGN_TOP_VALUE.equals(value))
            return VerticalAlign.TOP;
        else if (FXG_VERTICALALIGN_BOTTOM_VALUE.equals(value))
            return VerticalAlign.BOTTOM;
        else if (FXG_VERTICALALIGN_MIDDLE_VALUE.equals(value))
            return VerticalAlign.MIDDLE;
        else if (FXG_VERTICALALIGN_JUSTIFY_VALUE.equals(value))
            return VerticalAlign.JUSTIFY;
        else if (FXG_INHERIT_VALUE.equals(value))
            return VerticalAlign.INHERIT;
        
        problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), FXG_VERTICALALIGN_ATTRIBUTE, value));
        
        return defaultValue;
    }
    
    public static String parseTabStops(IFXGNode node, String value, String defaultValue, Collection<ICompilerProblem> problems)
    {
        String[] tabStops = value.trim().split("\\s+");
        
        // A space may be part of the alignment token is it escaped with a 
        // backslash. We need to combine those backslash escaped space to
        // the string element in front of it.
        ArrayList<String> finalTabStops = new ArrayList<String>(tabStops.length);
        int iFinal = -1;
        boolean escaped = false;
        for (int i=0; i<tabStops.length; i++)
        {
            if (escaped)
            {
                finalTabStops.add(iFinal, finalTabStops.get(iFinal)+tabStops[i]);
            }
            else
            {
                finalTabStops.add(tabStops[i]);
                iFinal++;
            }
            escaped = tabStops[i].endsWith("\\")? true: false;
        }
        
        String tabStopsVal = null;
        for (int i=0; i<finalTabStops.size(); i++)
        {
            tabStopsVal = finalTabStops.get(i);
            if (!matchPattern(tabStopsVal, tabstopsExceptDNumericPattern))
            {
                if (!matchPattern(tabStopsVal, tabstopsExceptDScientificPattern))
                {
                    if (!matchPattern(tabStopsVal, tabstopsDNumericPattern))
                    {
                        if (!matchPattern(tabStopsVal, tabstopsDScientificPattern))
                        {
                            // Malformed tab stops ''{0}'' - must be 
                            // an array of tab stops where each tab stop is 
                            // delimited by one or more spaces. A tab stop 
                            // takes the following string-based form: 
                            // [alignment type][alignment position]|[alignment token]. 
                            problems.add(new FXGInvalidTabStopsProblem(node.getDocumentPath(), node.getStartLine(), 
                                    node.getStartColumn(), value));
                            return defaultValue;
                        }
                    }
                }
            }
        }
        return value;
    }
    
    private static boolean matchPattern(String value, Pattern pattern)
    {
        Matcher m = pattern.matcher(value);
        return m.matches();
    }
}
