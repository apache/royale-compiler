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

import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBlockNode;
import org.apache.royale.compiler.tree.as.IScopedNode;

/**
 * ActionScript parse tree node representing a block, but not one with an
 * explicit scope
 */
public class BlockNode extends ContainerNode implements IBlockNode
{
    /**
     * Constructor.
     */
    public BlockNode()
    {
    }
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.BlockID;
    }
    
    @Override
    // TODO Remove unnecessary override.
    public void replaceChild(NodeBase child, NodeBase target)
    {
        super.replaceChild(child, target);
    }
    
    //
    // Other methods
    //

    /**
     * Get the scope associated with this node as an IASScope
     * 
     * @return scope associated with this node
     */
    public IASScope getScope()
    {
        IASNode parent = getParent();
        
        while (parent != null)
        {
            if (parent instanceof IScopedNode)
                return ((IScopedNode)parent).getScope();

            parent = parent.getParent();
        }
        
        return null;
    }
}
