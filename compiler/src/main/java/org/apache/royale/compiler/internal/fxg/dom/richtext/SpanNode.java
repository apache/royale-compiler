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

import static org.apache.royale.compiler.fxg.FXGConstants.FXG_SPAN_ELEMENT;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.CDATANode;
import org.apache.royale.compiler.internal.fxg.dom.ITextNode;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Represents a &lt;p /&gt; child tag of FXG &lt;RichText&gt; content. A
 * &lt;p&gt; tag starts a new span in text content.
 */
public class SpanNode extends AbstractRichTextLeafNode
{    
    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * @return The unqualified name of a span node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_SPAN_ELEMENT;
    }
    
    /**
     * Adds an FXG child node to this span node. Supported child nodes
     * include text content nodes: br, tab. Number of instances: unbounded.
     * 
     * @param child - a child FXG node to be added to this node.
     * @param problems problem collection used to collect problems occurred within this method
     */
    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof BRNode
                || child instanceof TabNode
                || child instanceof CDATANode)
        {
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
}
