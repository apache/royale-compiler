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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.utils.CheapArray;

public abstract class TreeNode extends NodeBase
{
    /**
     * Constructor.
     */
    public TreeNode()
    {
        super();
        
        children = CheapArray.create(getInitialChildCount());
    }

    /**
     * Constructor.
     * 
     * @param size The initial size of the array containing the children.
     */
    public TreeNode(int size)
    {
        children = CheapArray.create(size);
    }

    private Object children;
    
    //
    // NodeBase overrides
    //
    
    @Override
    public int getChildCount()
    {
        return CheapArray.size(children);
    }

    @Override
    public IASNode getChild(int i)
    {
        return (IASNode)CheapArray.get(i, children);
    }
    
    @Override
    protected void replaceChild(NodeBase child, NodeBase target)
    {
        if (child != null)
        {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++)
            {
                if (getChild(i) == child)
                {
                    CheapArray.replace(i, target, children);
                    target.setParent(this);
                    child.setParent(null);
                    return;
                }
            }
        }
    }

    @Override
    protected void swapChildren(NodeBase child, NodeBase target)
    {
        if (child != null && target != null && child.getParent() == target.getParent())
        {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++)
            {
                IASNode child2 = getChild(i);
                if (child2 == child)
                    CheapArray.replace(i, target, children);
                else if (getChild(i) == target)
                    CheapArray.replace(i, child, children);
            }
        }
    }

    @Override
    public void normalize(boolean fillInOffsets)
    {
        super.normalize(fillInOffsets);
        
        optimizeChildren(children);
    }

    //
    // Other methods
    //

    /**
     * Child nodes This collection is declared as Object, but is manipulated
     * exclusively using the functions in CheapArray.
     */
    /**
     * Take a guess as to an efficient initial child count. We can override this
     * in nodes that have a fixed number of children.
     * 
     * @return initial size to use when creating children
     */
    protected int getInitialChildCount()
    {
        return 10;
    }

    /**
     * Add the node as a child. Used during parsing.
     * 
     * @param child child node to add
     */
    public void addChild(NodeBase child)
    {
        if (child != null)
        {
            CheapArray.add(child, children);
            child.setParent(this);
        }
    }

    public void addChild(NodeBase child, int position)
    {
        if (children instanceof List)
        {
            ((List) children).add(position, child);
        }
        else
        {
            ArrayList<IASNode> newArray = new ArrayList<IASNode>();
            Collections.addAll(newArray, (IASNode[])children);
            newArray.add(position, child);
            children = CheapArray.toArray(newArray, emptyNodeArray);
        }
        child.setParent(this);
    }

    /**
     * Add the node as a child after normalization has occurred. Used after
     * parsing.
     * 
     * @param child child node to add
     */
    protected void addChildPostNormalize(NodeBase child)
    {
        if (child != null)
        {
            children = CheapArray.add(child, children, emptyNodeArray);
            child.setParent(this);
        }
    }

    /**
     * Add the node as a child, making sure it's in offset order with the other
     * children. This adds children from back to front, so it's most efficient
     * to add the children in order. Used during parsing.
     * 
     * @param newChild child node to add
     * @param fillInOffsets true if we should fill in offsets as we go
     */
    protected void addChildInOrder(NodeBase newChild, boolean fillInOffsets)
    {
        if (newChild != null)
        {
            // This child is new to the tree hierarchy, so it hasn't been normalized already
            newChild.normalize(fillInOffsets);
            int start = newChild.getAbsoluteStart() != -1 ? newChild.getAbsoluteStart() : newChild.getAbsoluteEnd();
            if (start != -1)
            {
                int childrenSize = getChildCount();

                if (childrenSize > 0)
                {
                    if (start < (getChild(0)).getAbsoluteStart())
                    {
                        CheapArray.add(0, newChild, children);
                        newChild.setParent(this);
                        return;
                    }
                }

                for (int i = childrenSize - 1; i >= 0; i--)
                {
                    IASNode sibling = getChild(i);
                    if (sibling.getAbsoluteEnd() == -1 ||
                        sibling.getAbsoluteEnd() <= start)
                    {
                        // add child after sibling
                        CheapArray.add(i + 1, newChild, children);
                        newChild.setParent(this);
                        return;
                    }

                }
            }
            CheapArray.add(newChild, children);
            newChild.setParent(this);
        }
    }


    /**
     * Adds a node to the list of children WITHOUT changing their real parent.
     * 
     * @param child new child node
     */
    protected void addTemporaryChild(NodeBase child)
    {
        CheapArray.add(child, children);
    }

    /**
     * Remove the node as a child. Used during parsing.
     * 
     * @param child child node to remove
     */
    protected void removeChild(NodeBase child)
    {
        if (child != null)
        {
            //use safe remove call
            children = CheapArray.remove(child, children, emptyNodeArray);
            child.setParent(null);
        }
    }

    /**
     * Removes a node from the list of children WITHOUT changing their real
     * parent.
     * 
     * @param child new child node
     */
    protected void removeTemporaryChild(NodeBase child)
    {
        CheapArray.remove(child, children);
    }

    /**
     * Removes all of the children of this node
     */
    @SuppressWarnings("rawtypes")
    public void removeAllChildren()
    {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            if (getChild(i) instanceof NodeBase)
                ((NodeBase)getChild(i)).setParent(null);
        }
        if (children instanceof ArrayList)
            children = new ArrayList(getInitialChildCount());
        else
            children = emptyNodeArray;
    }

    /**
     * Sets our children to an optimized version of newChildren.
     * 
     * @param newChildren
     */
    protected void optimizeChildren(Object newChildren)
    {
        children = CheapArray.optimize(newChildren, emptyNodeArray);
    }

    protected void sortChildren(boolean updateBounds)
    {
        if (CheapArray.size(children) > 0)
        {
            CheapArray.sort(children, new Comparator<Object>()
            {

                @Override
                public int compare(Object o1, Object o2)
                {
                    if (o1 instanceof NodeBase && o2 instanceof NodeBase)
                    {
                        if (((NodeBase)o1).getAbsoluteStart() < ((NodeBase)o2).getAbsoluteStart())
                            return -1;
                        else if (((NodeBase)o1).getAbsoluteStart() == ((NodeBase)o2).getAbsoluteStart())
                            return 0;
                        else
                            return 1;
                    }
                    return -1;
                }
            });
            if (updateBounds)
            {
                setStart(((NodeBase)CheapArray.get(0, children)).getAbsoluteStart());
                setEnd(((NodeBase)CheapArray.get(CheapArray.size(children) - 1, children)).getAbsoluteEnd());
            }
        }
    }
}
