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

import static org.apache.royale.compiler.fxg.FXGConstants.FXG_BASELINEOFFSET_ASCENT_VALUE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_BASELINEOFFSET_AUTO_VALUE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_BASELINEOFFSET_LINEHEIGHT_VALUE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_BLOCKPROGRESSION_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_COLUMNCOUNT_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_COLUMNGAP_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_COLUMNWIDTH_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_FIRSTBASELINEOFFSET_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_INHERIT_VALUE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_LINEBREAK_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_NUMBERAUTO_AUTO_VALUE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_PADDINGBOTTOM_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_PADDINGLEFT_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_PADDINGRIGHT_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_PADDINGTOP_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_VERTICALALIGN_ATTRIBUTE;

import java.util.Collection;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.DOMParserHelper;
import org.apache.royale.compiler.internal.fxg.dom.types.BaselineOffset;
import org.apache.royale.compiler.internal.fxg.dom.types.BaselineOffset.BaselineOffsetAsEnum;
import org.apache.royale.compiler.internal.fxg.dom.types.BlockProgression;
import org.apache.royale.compiler.internal.fxg.dom.types.LineBreak;
import org.apache.royale.compiler.internal.fxg.dom.types.NumberAuto;
import org.apache.royale.compiler.internal.fxg.dom.types.NumberAuto.NumberAutoAsEnum;
import org.apache.royale.compiler.internal.fxg.dom.types.NumberInherit;
import org.apache.royale.compiler.internal.fxg.dom.types.NumberInherit.NumberInheritAsEnum;
import org.apache.royale.compiler.internal.fxg.dom.types.VerticalAlign;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;


/**
 * A base class that represents a block text.
 */
public abstract class AbstractRichBlockTextNode extends AbstractRichParagraphNode
{
    protected static final double PADDING_MIN_INCLUSIVE = 0.0;
    protected static final double PADDING_MAX_INCLUSIVE = 1000.0;
    protected static final double BASELINEOFFSET_MIN_INCLUSIVE = 0.0;
    protected static final double BASELINEOFFSET_MAX_INCLUSIVE = 1000.0;        
    protected static final int COLUMNCOUNT_MIN_INCLUSIVE = 0;
    protected static final int COLUMNCOUNT_MAX_INCLUSIVE = 50; 
    protected static final double COLUMNGAP_MIN_INCLUSIVE = 0.0;
    protected static final double COLUMNGAP_MAX_INCLUSIVE = 1000.0; 
    protected static final double COLUMNWIDTH_MIN_INCLUSIVE = 0.0;
    protected static final double COLUMNWIDTH_MAX_INCLUSIVE = 8000.0; 


    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

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
    
    /**
     * This implementation processes text flow extra attributes that are 
     * relevant to the &lt;p&gt; tag, as well as delegates to the parent class 
     * to process text leaf or paragraph attributes that are also relevant to 
     * the &lt;p&gt; tag.
     * 
     * @param name the attribute name
     * @param value the attribute value
     * @see AbstractRichParagraphNode#setAttribute(String, String, Collection)
     */
    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_BLOCKPROGRESSION_ATTRIBUTE.equals(name))
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
     * BaselineOffset rule or the value falls out of the specified range 
     * (inclusive).
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
                //Unknown first baseline offset.
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
     * NumberAuto rule.
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
            
            //Unknown number auto.
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
     * NumberAuto rule.
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

            // Unknown number auto.
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
     * NumberInherit rule or the value falls out of the specified range 
     * (inclusive).
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

            problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), name, value));  
            
            return NumberInherit.newInstance(defaultValue);
        }
    }
}
