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

import antlr.Token;

import org.apache.royale.compiler.tree.ASTNodeID;

/**
 * A collapsed version of a FullNameNode.
 */
public final class QualifiedNameExpressionNode extends IdentifierNode
{
    /**
     * Constructor.
     * 
     * @param node The node representing the qualified name.
     */
    public QualifiedNameExpressionNode(FullNameNode node)
    {
        super(node.getName());
        
        setSourcePath(node.getSourcePath());
        setStart(node.getAbsoluteStart());
        setEnd(node.getAbsoluteEnd());
    }
    
    /**
     * Constructor.
     * 
     * @param token QName token.
     */
    public QualifiedNameExpressionNode(Token token)
    {
        super(token.getText());
        
        span(token);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected QualifiedNameExpressionNode(QualifiedNameExpressionNode other)
    {
        super(other);
    }
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.QualifiedNameExpressionID;
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    protected QualifiedNameExpressionNode copy()
    {
        return new QualifiedNameExpressionNode(this);
    }
    
    //
    // IdentifierNode overrides
    //

    @Override
    public IdentifierType getIdentifierType()
    {
        return IdentifierType.FULLY_QUALIFIED_NAME;
    }
}
