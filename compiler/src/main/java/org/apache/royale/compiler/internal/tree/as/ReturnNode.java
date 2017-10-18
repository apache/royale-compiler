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
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IReturnNode;

/**
 * Represents a return statement found in Actionscript. They are of the form:
 * <code> return expression(optional);
 */
public class ReturnNode extends BaseStatementExpressionNode implements IReturnNode
{
    /**
     * Constructor.
     */
    public ReturnNode(IASToken returnToken)
    {
        super(returnToken);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected ReturnNode(ReturnNode other)
    {
        super(other);
    }

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.ReturnStatementID;
    }

    //
    // ExpressionNodeBase overrides
    //
    
    // TODO Does this class need to override resolveType()?

    @Override
    protected ReturnNode copy()
    {
        return new ReturnNode(this);
    }
    
    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        return expressionNode.isDynamicExpression(project);
    }
    
    //
    // IReturnNode implementations
    //

    @Override
    public IExpressionNode getReturnValueNode()
    {
        return expressionNode;
    }
}
