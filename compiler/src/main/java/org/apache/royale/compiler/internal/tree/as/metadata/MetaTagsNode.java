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

package org.apache.royale.compiler.internal.tree.as.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.definitions.metadata.MetaTag;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.BaseDefinitionNode;
import org.apache.royale.compiler.internal.tree.as.BlockNode;
import org.apache.royale.compiler.internal.tree.as.ClassNode;
import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.internal.tree.as.MemberedNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

public class MetaTagsNode extends ContainerNode implements IMetaTagsNode
{
    private IDefinitionNode decoratedNode;

    /**
     * Default constructor
     */
    public MetaTagsNode()
    {
        super();
        setContainerType(ContainerType.BRACKETS);
    }

    public void setDecorationTarget(IDefinitionNode node)
    {
        decoratedNode = node;
    }

    public IDefinitionNode getDecoratedDefinition()
    {
        if (decoratedNode == null)
        {
            IASNode parent = getParent();
            if (parent instanceof BlockNode)
            {
                IASNode node = parent.getParent();
                if (node instanceof MemberedNode)
                {
                    BlockNode contents = ((MemberedNode)node).getScopedNode();
                    int childCount = contents.getChildCount();
                    boolean found = false;
                    for (int i = 0; i < childCount; i++)
                    {
                        IASNode child = contents.getChild(i);
                        if (found)
                        {
                            if (child instanceof IDefinitionNode)
                            {
                                decoratedNode = (IDefinitionNode)child;
                                if (child instanceof ClassNode)
                                {
                                    analyze(EnumSet.of(PostProcessStep.POPULATE_SCOPE), ((ClassNode)child).getScopedNode().getASScope(), new ArrayList<ICompilerProblem>(0));
                                }
                                if (child instanceof BaseDefinitionNode)
                                {
                                    ((BaseDefinitionNode)child).setMetaTags(this);
                                }
                                break;
                            }
                        }
                        else
                        {
                            if (child == this)
                            {
                                found = true;
                            }
                        }
                    }
                    //we're still null, so let's loop through backwards to see if we can find the node we are decorating
                    //this could happen in MXML land when the mx:MetaData block comes after a script/event block
                    if (decoratedNode == null && found)
                    {
                        for (int i = childCount - 1; i >= 0; i--)
                        {
                            IASNode child = contents.getChild(i);
                            if (child instanceof IDefinitionNode)
                            {
                                decoratedNode = (IDefinitionNode)child;
                                if (child instanceof ClassNode)
                                {
                                    analyze(EnumSet.of(PostProcessStep.POPULATE_SCOPE), ((ClassNode)child).getScopedNode().getASScope(), new ArrayList<ICompilerProblem>(0));
                                }
                                if (child instanceof BaseDefinitionNode)
                                {
                                    ((BaseDefinitionNode)child).setMetaTags(this);
                                }
                                break;
                            }
                        }
                    }

                }
            }
            else if (parent instanceof IDefinitionNode)
            {
                decoratedNode = (IDefinitionNode)parent;
            }
        }

        return decoratedNode;
    }

    @Override
    protected int getInitialChildCount()
    {
        return 0;
    }

    public void addTag(MetaTagNode node)
    {
        if (node != null)
            addChild(node);
    }

    @Override
    public void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        // An "unbound" metadata will have "null" scope because it doesn't 
        // have a bound definition.
        if (scope == null)
            return;
        
        final IScopedNode scopeNode = scope.getScopeNode();

        if (scopeNode != null &&
            scopeNode.getParent() != null &&
            scopeNode.getParent().equals(getDecoratedDefinition()))
        {
            super.analyze(set, scope, problems);
        }
    }

    @Override
    public void addItem(NodeBase child)
    {
        if (child instanceof IMetaTagNode)
            super.addChildInOrder(child, true);
    }

    @Override
    public void addChild(NodeBase child)
    {
        if (child instanceof IMetaTagNode)
            super.addChildInOrder(child, true);
    }

    @Override
    protected void addChildInOrder(NodeBase newChild, boolean fillInOffsets)
    {
        if (newChild instanceof IMetaTagNode)
            super.addChildInOrder(newChild, fillInOffsets);
    }

    @Override
    protected boolean canContinueContainmentSearch(IASNode containingNode, IASNode currentNode, int childOffset, boolean offsetStillValid)
    {
        //we need to establish that it's safe to return the containing node here
        //especially, if the last node was a metadata node
        //check to see if we're metadata, and if we are generated from MXML
        //if we are generated from mx:Metadata, don't let us return the block since it's possible
        //that the metadata can span the entire contents of the class
        // <mx:Metadata><mx:Script><mx:MetaData>
        if (offsetStillValid)
        {
            if (isTransparent() &&
                    containingNode instanceof BlockNode && containingNode == currentNode)
            {
                return true;
            }
        }
        else
        {
            if (isTransparent())
                return true;
        }
        return false;
    }

    /**
     * Remove a meta attribute to the set. This is not commonly done... usually
     * this information is static after it is parsed. However, the ABCEvaluator
     * uses it to remove the __go_to_definition_help metadata after it has
     * gleaned the start offset from it.
     * 
     * @param node node being removed
     */
    public void removeAttribute(MetaTagNode node)
    {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            if (getChild(i) == node)
                removeChild(node);
        }
    }

    /**
     * Determine if this set of attributes is empty
     * 
     * @return true if there are no attributes
     */
    public boolean isEmpty()
    {
        return getChildCount() <= 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof MetaTagsNode))
            return false;

        MetaTagsNode other = (MetaTagsNode)obj;
        int attributesCount = getChildCount();
        int otherAttributesCount = other.getChildCount();

        if (other.getAbsoluteStart() != this.getAbsoluteStart())
            return false;

        if (other.getAbsoluteEnd() != this.getAbsoluteEnd())
            return false;

        if (attributesCount != otherAttributesCount)
            return false;
        if (attributesCount == 0)
            return true;
        //enforce order
        for (int i = 0; i < attributesCount; i++)
        {
            IASNode attribute = getChild(i);
            IASNode otherAttribute = other.getChild(i);
            if (!attribute.equals(otherAttribute))
                return false;
        }

        return true;
    }

    @Override
    public IMetaTagNode[] getAllTags()
    {
        int childCount = getChildCount();
        IMetaTagNode[] tags = new IMetaTagNode[childCount];
        for (int i = 0; i < childCount; i++)
        {
            tags[i] = (IMetaTagNode)getChild(i);
        }
        return tags;
    }

    @Override
    public IMetaTagNode[] getTagsByName(String name)
    {
        int size = getChildCount();
        ArrayList<IMetaTagNode> list = new ArrayList<IMetaTagNode>(size);
        for (int i = 0; i < size; i++)
        {
            IASNode child = getChild(i);
            if (child instanceof IMetaTagNode && ((IMetaTagNode)child).getTagName().compareTo(name) == 0)
            {
                list.add((IMetaTagNode)child);
            }
        }
        return list.toArray(new IMetaTagNode[0]);
    }

    @Override
    public boolean hasTagByName(String name)
    {
        IMetaTagNode[] byName = getTagsByName(name);
        return byName != null && byName.length > 0;
    }

    @Override
    public IMetaTagNode getTagByName(String name)
    {
        int size = getChildCount();
        for (int i = 0; i < size; i++)
        {
            if (((IMetaTagNode)getChild(i)).getTagName().compareTo(name) == 0)
            {
                return (IMetaTagNode)getChild(i);
            }
        }
        return null;
    }

    public IMetaTag[] buildMetaTags(IFileSpecification containingFileSpec, IDefinition definition)
    {
        assert containingFileSpec != null;
        assert definition != null;
        IMetaTagNode[] metaTagNodes = getAllTags();
        int n = metaTagNodes.length;

        IMetaTag[] metaTags = new MetaTag[n];
        for (int i = 0; i < n; i++)
        {
            metaTags[i] = ((MetaTagNode)metaTagNodes[i]).buildMetaTag(containingFileSpec, definition);
        }
        return metaTags;
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MetaTagsID;
    }
    
    
}
