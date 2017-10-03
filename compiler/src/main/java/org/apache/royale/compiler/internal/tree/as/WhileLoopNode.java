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

import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IWhileLoopNode;

/**
 * Represents a while loop node, either starting with a <code>do</code> or with
 * a <code>while</code>.
 */
public class WhileLoopNode extends ConditionalNode implements IWhileLoopNode
{
    /**
     * Constructor.
     * 
     * @param keyword The token representing the <code>while</code> keyword.
     */
    public WhileLoopNode(IASToken keyword)
    {
        super(keyword);
        
        if (keyword != null)
            startBefore(keyword);
    }
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.WhileLoopID;
    }
    
    //
    // IWhileLoop implementations
    //

    @Override
    public WhileLoopKind getKind()
    {
        return WhileLoopKind.WHILE;
    }
}
