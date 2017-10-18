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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.AbstractFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.CDATANode;
import org.apache.royale.compiler.internal.fxg.dom.ITextNode;
import org.apache.royale.compiler.internal.fxg.dom.richtext.TextHelper;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * A base class for all FXG nodes concerned with formatted text.
 */
public abstract class AbstractTextNode extends AbstractFXGNode implements ITextNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    //------------
    // id
    //------------

    protected String id;

    /**
     * An id attribute provides a well defined name to a text node.
     */
    @Override
    public String getId()
    {
        return id;
    }

    /**
     * Sets the node id.
     * @param value - the node id as a String.
     */
    @Override
    public void setId(String value)
    {
        id = value;
    }

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

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * Check child node to ensure that exception isn't thrown for ignorable 
     * white spaces.
     * 
     * @param child - a child FXG node to be added to this node.
     */
    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (content == null)
        {
        	if (child instanceof CDATANode && TextHelper.ignorableWhitespace(((CDATANode)child).content))
        	{
                /**
                 * Ignorable white spaces don't break content contiguous 
                 * rule and should be ignored if they are at the beginning 
                 * of a element value.
                 */
        		return;
        	}
        }
        else 
        {
            super.addChild(child, problems);
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
    
    /**
     * Sets an FXG attribute on this text node.
     * 
     * @param name - the unqualified attribute name.
     * @param value - the attribute value.
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_ID_ATTRIBUTE.equals(name))
        {
            id = value;
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
