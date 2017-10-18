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
import org.apache.royale.compiler.internal.fxg.dom.DOMParserHelper;
import org.apache.royale.compiler.internal.fxg.dom.types.NumberPercentAuto;
import org.apache.royale.compiler.internal.fxg.dom.types.NumberPercentAuto.NumberPercentAutoAsEnum;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Represents a &lt;p /&gt; FXG image node.
 */
public class ImgNode extends AbstractRichTextLeafNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    // Image attributes
    public NumberPercentAuto width = NumberPercentAuto.newInstance(NumberPercentAutoAsEnum.AUTO);
    public NumberPercentAuto height = NumberPercentAuto.newInstance(NumberPercentAutoAsEnum.AUTO);
    public String source = "";
        
    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public String getNodeName()
    {
        return FXG_IMG_ELEMENT;
    }

    /**
     * This implementation processes image attributes that are relevant to
     * the &lt;p&gt; tag, as well as delegates to the parent class to process
     * character attributes that are also relevant to the &lt;p&gt; tag.
     *  
     * @param name the attribute name
     * @param value the attribute value
     * @see AbstractRichTextNode#setAttribute(String, String, Collection)
     */
    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_WIDTH_ATTRIBUTE.equals(name))
        {
            width = getNumberPercentAuto(this, name, value, width.getNumberPercentAutoAsEnum(), problems);
        }
        else if(FXG_HEIGHT_ATTRIBUTE.equals(name))
        {
            height = getNumberPercentAuto(this, name, value, height.getNumberPercentAutoAsEnum(), problems);
        }
        else if(FXG_SOURCE_ATTRIBUTE.equals(name))
        {
            source = value;
        }
        else
        {
            super.setAttribute(name, value, problems);
            return;
        }
        
        // Remember attribute was set on this node.
        rememberAttribute(name, value);        
    }
    
    /**
     * 
     * @param node - the FXG node.
     * @param name - the FXG attribute.
     * @param errorCode - the error code when value is out-of-range.
	 * @param value - the FXG String value.
	 * 
     */
    private NumberPercentAuto getNumberPercentAuto(IFXGNode node, String name, String value, NumberPercentAutoAsEnum defaultValue, Collection<ICompilerProblem> problems)
    {
    	try
    	{
    		return NumberPercentAuto.newInstance(DOMParserHelper.parseNumberPercent(this, value, Double.MIN_VALUE, Double.MAX_VALUE));
    	}
    	catch (Exception e)
    	{
    		if (FXG_NUMBERPERCENAUTO_AUTO_VALUE.equals(value))
    		{
    			return NumberPercentAuto.newInstance(NumberPercentAutoAsEnum.AUTO);
            }

            //Unknown number percent auto.
            problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), name, value)); 
            
            return NumberPercentAuto.newInstance(defaultValue);
    	}
    }
}
