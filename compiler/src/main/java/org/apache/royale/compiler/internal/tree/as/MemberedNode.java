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

import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IScopedDefinitionNode;

public abstract class MemberedNode extends BaseDefinitionNode implements IScopedDefinitionNode
{
    /**
     * Constructor.
     */
    public MemberedNode()
    {
        super();
        
        contentsNode = new ScopedBlockNode();
    }

    /**
     * Contents of this package or class (holds everything within the {})
     */
    protected ScopedBlockNode contentsNode;
    
    //
    // IScopedDefinitionNode implementations
    //
    
    @Override
    public ScopedBlockNode getScopedNode()
    {
        return contentsNode;
    }

    //
    // Other methods
    //

    public IDefinitionNode[] getAllMemberDefinitionNodes()
    {
        ArrayList<IDefinitionNode> names = new ArrayList<IDefinitionNode>();
        int childCount = contentsNode.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            if (contentsNode.getChild(i) instanceof IDefinitionNode)
                names.add(((IDefinitionNode)contentsNode.getChild(i)));
        }
        return names.toArray(new IDefinitionNode[0]);
    }
}
