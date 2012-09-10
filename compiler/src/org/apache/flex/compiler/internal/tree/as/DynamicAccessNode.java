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

package org.apache.flex.compiler.internal.tree.as;

import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IDynamicAccessNode;

/**
 * ActionScript parse tree node representing an dynamic access expression
 * (e.g. <code>o[p]</code>).
 */
public class DynamicAccessNode extends BinaryOperatorNodeBase implements IDynamicAccessNode
{
    /**
     * Constructor.
     */
    public DynamicAccessNode(ExpressionNodeBase leftOperandNode)
    {
        super(null, leftOperandNode, null);
    }

    /**
     * Copy constructor.
     * 
     * @param other The node to copy.
     */
    protected DynamicAccessNode(DynamicAccessNode other)
    {
        super(other);
    }

     //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.ArrayIndexExpressionID;
    }

    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    protected DynamicAccessNode copy()
    {
        return new DynamicAccessNode(this);
    }
    
    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        return true;
    }
    
    //
    // OperatorNodeBase overrides
    //
    
    @Override
    public OperatorType getOperator()
    {
        return OperatorType.DYNAMIC_ACCESS;
    }
}

