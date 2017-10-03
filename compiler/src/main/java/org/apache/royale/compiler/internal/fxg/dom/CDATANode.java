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

import static org.apache.royale.compiler.fxg.FXGConstants.FXG_ID_ATTRIBUTE;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * A class to determine whether a node constitutes an CData in 
 * a text flow.
 */
public class CDATANode extends AbstractFXGNode implements ITextNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------
    public String content = null; 
    
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
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * @return null, as character data has no name.
     */
    @Override
    public String getNodeName()
    {
        return null;
    }
    
    /**
     * @return A Map recording the attribute names and values set on this
     * text node.
     */
    @Override
    public Map<String, String> getTextAttributes()
    {
        return null;
    }

    /**
     * @return The List of child nodes of this text node. 
     */
    @Override
    public List<ITextNode> getTextChildren()
    {
        return null;
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
            id = value;
        else
            super.setAttribute(name, value, problems);
    }
}
