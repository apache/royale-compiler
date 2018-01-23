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

import static org.apache.royale.compiler.fxg.FXGConstants.FXG_LIBRARY_ELEMENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.royale.compiler.fxg.FXGConstants;
import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.problems.FXGMissingAttributeProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Represents the special &lt;Library&gt; section of an FXG document.
 * <p>
 * A Library contains a series of named &lt;Definition&gt; nodes that themselves
 * do not contribute to the visual representation but rather serve as
 * 'templates' that can be referenced by name throughout the document. A
 * reference to a definition is known as an 'instance' and is represented in the
 * tree as a special PlaceObjectNode (the term PlaceObject refers to the SWF tag
 * that places an instance on the stage). Instances can provide their own values
 * that override the defaults from the definition.
 * </p>
 */
public class LibraryNode extends AbstractFXGNode
{
    //--------------------------------------------------------------------------
    //
    // Children
    //
    //--------------------------------------------------------------------------

    //---------------
    // <Definition>
    //---------------

    public HashMap<String, DefinitionNode> definitions;

    /**
     * Locates a Definition node in this Library by name.
     * 
     * @param name - the name of the definition
     * @return a Definition for the given name, or null if none exists.
     */
    public DefinitionNode getDefinition(String name)
    {
        if (definitions != null)
            return definitions.get(name);
        else
            return null;
    }

    //--------------------------------------------------------------------------
    //
    // IFXGNode Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public void addChild(IFXGNode child, Collection<ICompilerProblem> problems)
    {
        if (child instanceof DefinitionNode)
        {
            if (definitions == null)
                definitions = new HashMap<String, DefinitionNode>();

            DefinitionNode node = (DefinitionNode)child;
            if (node.name == null)
            {
                problems.add(new FXGMissingAttributeProblem(getDocumentPath(), child.getStartLine(), 
                        child.getStartColumn(), FXGConstants.FXG_NAME_ATTRIBUTE, child.getNodeName()));
                return;
            }
            definitions.put(node.name, node);
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
        if(definitions != null)
            children.addAll(definitions.values());
        
        return children;
    }

    /**
     * @return The unqualified name of a Library node, without tag markup.
     */
    @Override
    public String getNodeName()
    {
        return FXG_LIBRARY_ELEMENT;
    }
}
