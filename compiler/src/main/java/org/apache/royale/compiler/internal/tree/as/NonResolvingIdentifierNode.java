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
import org.apache.royale.compiler.tree.as.INonResolvingIdentifierNode;
import org.apache.royale.compiler.tree.as.IScopedNode;

/**
 * Represents an identifier that we will currently not resolve, such as - field
 * in an ObjectLiteral. An Object literal takes the form of { field: expression,
 * }. Right now, we punt on this - label: shouldn't resolve to anything
 */
public class NonResolvingIdentifierNode extends IdentifierNode implements INonResolvingIdentifierNode
{
    /**
     * Constructor.
     * 
     * @param text The name of the identifier.
     */
    public NonResolvingIdentifierNode(String text)
    {
        super(text);
    }

    /**
     * Constructor.
     * 
     * @param text identifier text
     */
    public NonResolvingIdentifierNode(String text, Token token)
    {
        super(text, token);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected NonResolvingIdentifierNode(NonResolvingIdentifierNode other)
    {
        super(other);
    }

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.NonResolvingIdentifierID;
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    protected NonResolvingIdentifierNode copy()
    {
        return new NonResolvingIdentifierNode(this);
    }

    @Override
    public IScopedNode getScopeNode()
    {
        // Right now, punt on determining the correct scope
        // for an object literals label for the field.
        return null;
    }
}
