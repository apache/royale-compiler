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

import static org.apache.royale.compiler.fxg.FXGConstants.FXG_HREF_ATTRIBUTE;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_LINKACTIVEFORMAT_PROPERTY_ELEMENT;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_LINKHOVERFORMAT_PROPERTY_ELEMENT;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_LINKNORMALFORMAT_PROPERTY_ELEMENT;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_P_ELEMENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.CDATANode;
import org.apache.royale.compiler.internal.fxg.dom.ITextNode;
import org.apache.royale.compiler.problems.FXGMissingAttributeProblem;
import org.apache.royale.compiler.problems.FXGMultipleElementProblem;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Represents a &lt;p /&gt; child tag of FXG &lt;RichText&gt; content. A
 * &lt;p&gt; tag starts a new paragraph in text content.
 */
public class ParagraphNode extends AbstractRichParagraphNode
{    
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    // Link format properties
    public TextLayoutFormatNode linkNormalFormat = null;
    public TextLayoutFormatNode linkHoverFormat = null;
    public TextLayoutFormatNode linkActiveFormat = null;    
    
    //--------------------------------------------------------------------------
    //
    // Text Node Attribute Helpers
    //
    //--------------------------------------------------------------------------

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
     * A paragraph node can also have special child property nodes that
     * represent complex property values that cannot be set via a simple
     * attribute.
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
                    //Multiple LinkFormat elements are not allowed.
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
                    // Exception: Multiple LinkFormat elements are not allowed.
                    //Multiple LinkFormat elements are not allowed.
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
                    //Multiple LinkFormat elements are not allowed.
                    problems.add(new FXGMultipleElementProblem(getDocumentPath(), getStartLine(), 
                            getStartColumn(), propertyName));
                }
            }
            else
            {
                //Unknown LinkFormat element.
                problems.add(new FXGUnknownAttributeValueProblem(getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), node.getNodeName(), propertyName));
            }
        }
        else
        {
            super.addTextProperty(propertyName, node, problems);
        }
    }

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * @return The unqualified name of a paragraph node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_P_ELEMENT;
    }

    /**
     * Adds an FXG child node to this Paragraph node. Supported child nodes
     * include text content nodes (e.g. tcy, a, span, tab, br, and img).
     * 
     * Note that link format nodes (e.g. linkNormalFormat, linkHoverFormat, and 
     * linkActiveFormat) are complex properties rather than child nodes.
     * 
     * @param child - a child FXG node to be added to this node.
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof TCYNode
                || child instanceof LinkNode
                || child instanceof SpanNode
                || child instanceof BRNode
                || child instanceof TabNode
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
                content = new ArrayList<ITextNode>();

            content.add((ITextNode)child);
        }
        else
        {
            super.addChild(child, problems);
            return;
        }

        if (child instanceof AbstractRichTextNode)
        	((AbstractRichTextNode)child).setParent(this);        
    }
}
