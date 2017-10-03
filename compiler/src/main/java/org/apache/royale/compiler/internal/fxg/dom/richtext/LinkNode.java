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
import java.util.HashMap;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.CDATANode;
import org.apache.royale.compiler.internal.fxg.dom.ITextNode;
import org.apache.royale.compiler.internal.fxg.dom.text.AbstractTextNode;
import org.apache.royale.compiler.problems.FXGInvalidNestingElementsProblem;
import org.apache.royale.compiler.problems.FXGMultipleElementProblem;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Represents a &lt;p /&gt; FXG link node.
 */
public class LinkNode extends AbstractRichTextLeafNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    // Link attributes
    public String href;  // Required
    public String target = "";
    
    // Link format properties
    public TextLayoutFormatNode linkNormalFormat = null;
    public TextLayoutFormatNode linkHoverFormat = null;
    public TextLayoutFormatNode linkActiveFormat = null;    

    //--------------------------------------------------------------------------
    //
    // ITextNode Helpers
    //
    //--------------------------------------------------------------------------

    /**
     * This node's child property nodes.
     */
    protected HashMap<String, ITextNode> properties;

    /**
     * @return The List of child property nodes of this text node.
     */
    @Override
    public HashMap<String, ITextNode> getTextProperties()
    {
        return properties;
    }

    /**
     * A link node can also have special child property nodes that represent
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

    @Override
    public String getNodeName()
    {
        return FXG_A_ELEMENT;
    }

    /**
     * Adds an FXG child node to this link node. Supported child nodes
     * include text content nodes: span, br, tab, img.
     * 
     * @param child - a child FXG node to be added to this node.
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof SpanNode
                || child instanceof BRNode
                || child instanceof TabNode
                || child instanceof ImgNode
                || child instanceof TCYNode
                || child instanceof CDATANode)
        {
        	/**
        	 * When <a> has a <tcy> child, the <tcy> child is FORBIDDEN to have 
        	 * an <a> child of its own. AND vice versa. If a <tcy> has an <a> 
        	 * child, the <a> child is FORBIDDEN to have a <tcy> child.
        	 */
        	if (child instanceof TCYNode && this.parentNode instanceof TCYNode)
        	{
                // <tcy> element is forbidden as child of <a>, which 
        		// is child of another <tcy>. And vice versa.
                problems.add(new FXGInvalidNestingElementsProblem(getDocumentPath(), getStartLine(), 
                        getStartColumn()));
                return;
        	}
            if (content == null)
            {
                content = new ArrayList<ITextNode>();
            }
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

    /**
     * This implementation processes link attributes that are relevant to
     * the &lt;p&gt; tag, as well as delegates to the parent class to process
     * character attributes that are also relevant to the &lt;p&gt; tag.
     *  
     * @param name the attribute name
     * @param value the attribute value
     * @see AbstractTextNode#setAttribute(String, String, Collection)
     */
    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_HREF_ATTRIBUTE.equals(name))
        {
            href = value;
        }
        else if(FXG_TARGET_ATTRIBUTE.equals(name))
        {
            target = value;
        }
        else
        {
            super.setAttribute(name, value, problems);
            return;
        }

        // Remember attribute was set on this node.
        rememberAttribute(name, value);        
    }
}
