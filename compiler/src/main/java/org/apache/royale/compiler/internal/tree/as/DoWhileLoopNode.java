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
import org.apache.royale.compiler.tree.as.IASNode;

public class DoWhileLoopNode extends WhileLoopNode
{
    /**
     * Constructor.
     */
    public DoWhileLoopNode(IASToken keyword)
    {
        super(keyword);
    }
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.DoWhileLoopID;
    }

    @Override
    public int getChildCount()
    {
        int count = 0;
        
        if (contentsNode != null)
            count++;
        
        if (conditionalNode != null)
            count++;
        
        return count;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0 && contentsNode != null)
            return contentsNode;
        
        else if (i == 0 || (i == 1 && contentsNode != null))
            return conditionalNode;
        
        return null;
    }
    
    //
    // WhileLoopNode overrides
    //

    @Override
    public WhileLoopKind getKind()
    {
        return WhileLoopKind.DO;
    }
}
