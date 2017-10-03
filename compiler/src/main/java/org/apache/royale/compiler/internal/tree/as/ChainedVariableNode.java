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

package org.apache.royale.compiler.internal.tree.as;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

public final class ChainedVariableNode extends VariableNode
{
    /**
     * Constructor.
     * 
     * @param nameNode The identifier node specifying the name of the variable.
     */
    public ChainedVariableNode(IdentifierNode nameNode)
    {
        super(nameNode);
    }
    
    //
    // NodeBase overrides
    //

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        //don't add decoration children here since they don't exist on this node, neither does the keyword
        addChildInOrder(nameNode, fillInOffsets);
        ensureTypeNode();
        addChildInOrder(typeNode, fillInOffsets);
        addChildInOrder(getAssignedValueNode(), fillInOffsets);
    }
    
    //
    // BaseDefinitionNode overrides
    //

    @Override
    public IASDocComment getASDocComment()
    {
        // Get the comment from the Containing var node - comments apply to all chained
        // variable in one decl
        VariableNode varNode = (VariableNode)this.getAncestorOfType(VariableNode.class);
        if (varNode != null)
            return varNode.getASDocComment();

        return super.getASDocComment();
    }

    @Override
    public boolean hasExplicitComment()
    {
        // Get the comment from the Containing var node - comments apply to all chained
        // variable in one decl
        VariableNode varNode = (VariableNode)this.getAncestorOfType(VariableNode.class);
        if (varNode != null)
            return varNode.hasExplicitComment();

        return super.hasExplicitComment();
    }

    @Override
    protected MetaTagsNode getMetaTagsNode()
    {
        // Get the metadata from the Containing var node - comments apply to all chained
        // variable in one decl
        VariableNode varNode = (VariableNode)this.getAncestorOfType(VariableNode.class);
        if (varNode != null)
            return varNode.getMetaTagsNode();

        return super.getMetaTagsNode();
    }

    @Override
    public IMetaTagsNode getMetaTags()
    {
        // Get the metadata from the Containing var node - comments apply to all chained
        // variable in one decl
        VariableNode varNode = (VariableNode)this.getAncestorOfType(VariableNode.class);
        if (varNode != null)
            return varNode.getMetaTags();

        return super.getMetaTags();
    }

    @Override
    public INamespaceDecorationNode getNamespaceNode()
    {
        VariableNode varNode = getMainVariableNode();
        if (varNode != null)
            return varNode.getNamespaceNode();
        
        return null;
    }

    @Override
    public String getNamespace()
    {
        VariableNode varNode = (VariableNode)this.getAncestorOfType(VariableNode.class);
        if (varNode != null)
            return varNode.getNamespace();

        return null;
    }
    
    @Override
    public ModifiersContainerNode getModifiersContainer()
    {
        VariableNode varNode = getMainVariableNode();
        if (varNode != null)
            return varNode.getModifiersContainer();
        
        return null;
    }

    @Override
    public ModifiersSet getModifiers()
    {
        VariableNode varNode = getMainVariableNode();
        if (varNode != null)
            return varNode.getModifiers();
        
        return null;
    }

    @Override
    public boolean hasModifier(ASModifier modifier)
    {
        VariableNode varNode = getMainVariableNode();
        if (varNode != null)
            return varNode.hasModifier(modifier);
        
        return false;
    }

    //
    // BaseVariableNode overrides
    //

    @Override
    public boolean isConst()
    {
        VariableNode varNode = (VariableNode)this.getAncestorOfType(VariableNode.class);
        if (varNode != null)
            return varNode.isConst();

        return false;
    }
    
    //
    // Other methods
    //

    private VariableNode getMainVariableNode()
    {
        return (VariableNode)this.getAncestorOfType(VariableNode.class);
    }
}
