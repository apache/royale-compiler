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

import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IContainerNode;

/**
 * ActionScript parse tree node representing a container. This is used to hold
 * collections like function arguments and metadata attributes that don't come
 * with a scope.
 */
public class ContainerNode extends TreeNode implements IContainerNode
{
    public static final ContainerNode EMPTY_CONTAINER = new ContainerNode(0);

    /**
     * Constructor.
     */
    public ContainerNode()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param size The initial size of the array containing the children.
     */
    public ContainerNode(int size)
    {
        super(size);
    }

    private ContainerType containerType = ContainerType.SYNTHESIZED;
    private boolean removedConditionalCompileNode = false;

    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.ContainerID;
    }
    
    @Override
    public boolean contains(int offset)
    {
        return getAbsoluteStart() <= offset && getAbsoluteEnd() >= offset;
    }
    
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append(containerType.name()).append(' ');
        return super.buildInnerString(sb);
    }

    //
    // IContainerNode implementations
    //
    
    @Override
    public ContainerType getContainerType()
    {
        return containerType;
    }
    
    //
    // Other methods
    //

    public void setContainerType(ContainerType containerType)
    {
        this.containerType = containerType;
    }

    public boolean getRemovedConditionalCompileNode()
    {
        return removedConditionalCompileNode;
    }
    
    //
    // Other methods
    //

    public void setRemovedConditionalCompileNode(boolean value)
    {
        this.removedConditionalCompileNode = value;
    }
    
    /**
     * Adds a node to the list of children (really just a public wrapper around
     * addChild)
     * 
     * @param child new child node
     */
    public void addItem(NodeBase child)
    {
        addChild(child);
    }

    /**
     * Adds a node to the list of children (really just a public wrapper around
     * addChild)
     * 
     * @param child new child node
     */
    public void addItemAfterNormalization(NodeBase child)
    {
        addChildPostNormalize(child);
    }

    /**
     * Adds a node to the list of children WITHOUT changing their real parent.
     * This is used by the PackageTypesNode (which is not a real parse tree
     * nodes) to hold onto various type nodes without mangling their original
     * parse tree relationships.
     * 
     * @param child new child node
     */
    public void addTemporaryItem(NodeBase child)
    {
        if (child != null)
            addTemporaryChild(child);
    }

    /**
     * Removes a node in the list of children (wrapper around removeChild)
     */
    public void removeItem(NodeBase child)
    {
        removeChild(child);
    }

    /**
     * Removes a node from the list of children WITHOUT changing their real
     * parent. This is used by the PackageTypesNode (which is not a real parse
     * tree nodes) to hold onto various type nodes without mangling their
     * original parse tree relationships.
     * 
     * @param child new child node
     */
    public void removeTemporaryItem(NodeBase child)
    {
        if (child != null)
            removeTemporaryChild(child);
    }
}
