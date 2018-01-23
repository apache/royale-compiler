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

import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 * Runtime name expression used in qualified namespace expressions:
 * <code> public::[name]  </code>
 */
public class RuntimeNameExpressionNode extends ExpressionNodeBase
{
    /**
     * Constructor.
     * 
     * @param nameNode The node representing the runtime name.
     */
    public RuntimeNameExpressionNode(ExpressionNodeBase nameNode)
    {
        this.nameNode = nameNode;
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected RuntimeNameExpressionNode(RuntimeNameExpressionNode other)
    {
        super(other);
        
        this.nameNode = other.nameNode != null ? other.nameNode.copy() : null;
    }

    /**
     * The runtime name
     */
    protected ExpressionNodeBase nameNode;

    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.RuntimeNameExpressionID;
    }

    @Override
    public int getChildCount()
    {
        return (nameNode != null) ? 1 : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0)
            return nameNode;
        
        return null;
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (nameNode != null)
            nameNode.setParent(this);
    }
    
    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    protected RuntimeNameExpressionNode copy()
    {
        return new RuntimeNameExpressionNode(this);
    }
    
    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        return true;
    }
    
    //
    // Other methods
    //

    /**
     * Returns the name expression found within this runtime name expression
     * 
     * @return the name
     */
    public ExpressionNodeBase getNameExpression()
    {
        return nameNode;
    }
}
