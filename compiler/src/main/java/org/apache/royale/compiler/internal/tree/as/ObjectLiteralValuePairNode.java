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
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IObjectLiteralValuePairNode;

/**
 * ActionScript parse tree node representing a pair in an ObjectLiteral
 */
public final class ObjectLiteralValuePairNode extends ExpressionNodeBase implements IObjectLiteralValuePairNode
{
    private ExpressionNodeBase nameNode; // syntactically constrained to be a literal or identifier.

    private ExpressionNodeBase valueNode;

    /**
     * Constructor.
     */
    public ObjectLiteralValuePairNode(IASToken pos, ExpressionNodeBase nameNode, ExpressionNodeBase valueNode)
    {
        this.nameNode = nameNode;
        this.valueNode = valueNode;
        span(pos);
        setChildren(false);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected ObjectLiteralValuePairNode(ObjectLiteralValuePairNode other)
    {
        super(other);
        
        this.nameNode = other.nameNode != null ? other.nameNode.copy() : null;
        this.valueNode = other.valueNode != null ? other.valueNode.copy() : null;
    }

    //
    // NodeBase overrides
    //

    @Override
    public final ASTNodeID getNodeID()
    {
        return ASTNodeID.ObjectLiteralValuePairID;
    }

    @Override
    public final int getChildCount()
    {
        return (valueNode != null ? 1 : 0) +
               (nameNode != null ? 1 : 0);
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i > 1 || i < 0)
            return null;

        if (i == 0)
        {
            assert nameNode != null; // should be impossible. probably means name is null but value is not
            return nameNode;
        }

        return valueNode;
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (nameNode != null)
            nameNode.setParent(this);

        if (valueNode != null)
            valueNode.setParent(this);

    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    protected ObjectLiteralValuePairNode copy()
    {
        return new ObjectLiteralValuePairNode(this);
    }

    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        return false;
    }
    
    //
    // IObjectLiteralValuePairNode implementations
    //

    @Override
    public IExpressionNode getNameNode()
    {
        return nameNode;
    }

    @Override
    public IExpressionNode getValueNode()
    {
        return valueNode;
    }
}
