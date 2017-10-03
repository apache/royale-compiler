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

import static org.apache.royale.compiler.fxg.FXGConstants.FXG_DEFINITION_ELEMENT;
import static org.apache.royale.compiler.fxg.FXGConstants.FXG_NAME_ATTRIBUTE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.problems.FXGInvalidDefinitionNameProblem;
import org.apache.royale.compiler.problems.FXGMissingGroupChildNodeProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * A &lt;Definition&gt; is a special template node that is not itself rendered
 * but rather can be referenced by name in an FXG document.
 */
public class DefinitionNode extends AbstractFXGNode
{
    //--------------------------------------------------------------------------
    //
    // Attributes
    //
    //--------------------------------------------------------------------------

    public String name;

    //--------------------------------------------------------------------------
    //
    // Children
    //
    //--------------------------------------------------------------------------

    public GroupDefinitionNode groupDefinition;

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof GroupDefinitionNode)
        {
            if (groupDefinition != null)
            {
            	//Exception:Definitions must define a single Group child node.
                problems.add(new FXGMissingGroupChildNodeProblem(getDocumentPath(), 
                        child.getStartLine(), child.getStartColumn()));
                return;
            }

            groupDefinition = (GroupDefinitionNode)child;
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
        if(groupDefinition != null)
            children.add(groupDefinition);
        return children;
    }
    
    /**
     * @return The unqualified name of a Definition node, without tag markup.
     * i.e. literally 'Definition'. To retrieve the Definition name attribute,
     * refer to the name attribute itself.
     */
    @Override
    public String getNodeName()
    {
        return FXG_DEFINITION_ELEMENT;
    }

    @Override
    public void setAttribute(String name, String value, Collection<ICompilerProblem> problems)
    {
        if (FXG_NAME_ATTRIBUTE.equals(name))
        {
            this.name = DOMParserHelper.parseIdentifier(this, name, value, this.name, problems);
            if (((GraphicNode)this.getDocumentNode()).reservedNodes.containsKey(value))
            {
                problems.add(new FXGInvalidDefinitionNameProblem(getDocumentPath(), getStartLine(), getStartColumn(), value));
                return;
            }
        }
        else
        {
            super.setAttribute(name, value, problems);
        }
    }
}
