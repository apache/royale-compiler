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

import java.util.ArrayList;
import java.util.Collection;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.CDATANode;
import org.apache.royale.compiler.internal.fxg.dom.ITextNode;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Represents a &lt;span /&gt; child tag of FXG text content. A &lt;span&gt;
 * tag starts a new section of formatting in a paragraph of text content.
 */
public class SpanNode extends AbstractCharacterTextNode
{
    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    /**
     * A &lt;span&gt; allows child &lt;br /&gt; tags, as well as character
     * data (text content).
     */
    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof BRNode || child instanceof CDATANode)
        {
            if (content == null)
                content = new ArrayList<ITextNode>();

            content.add((ITextNode)child);
        }
        else 
        {
            super.addChild(child, problems);
        }
    }

    /**
     * @return The unqualified name of a span node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_SPAN_ELEMENT;
    }
}
