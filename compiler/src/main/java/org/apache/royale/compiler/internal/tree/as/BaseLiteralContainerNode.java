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

import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.ILiteralContainerNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.IContainerNode.ContainerType;

/**
 * Base container node for a literal that is made up of other literals, like XML
 * or arrays
 */
public abstract class BaseLiteralContainerNode extends ExpressionNodeBase
        implements ILiteralContainerNode
{
    /**
     * Constructor.
     */
    @SuppressWarnings("incomplete-switch")
	public BaseLiteralContainerNode(LiteralNode baseTypeNode)
    {
        this.baseTypeNode = baseTypeNode;
        baseTypeNode.setParent(this);

        contentsNode = new ContainerNode();
        switch (baseTypeNode.getLiteralType())
        {
            case ARRAY: 
            {
                contentsNode.setContainerType(ContainerType.BRACKETS);
                break;
            }
            case OBJECT:
            case VECTOR: 
            {
                contentsNode.setContainerType(ContainerType.BRACES);
                break;
            }
        }
    }

    /**
     * Copy constructor.
     * 
     * @param other The node to copy.
     */
    protected BaseLiteralContainerNode(BaseLiteralContainerNode other)
    {
        super(other);
    }

    protected LiteralNode baseTypeNode;

    protected ContainerNode contentsNode;

    //
    // NodeBase overrides
    //

    @Override
    public int getChildCount()
    {
        return contentsNode != null ? 1 : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 ? contentsNode : null;
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (contentsNode != null)
            contentsNode.setParent(this);
    }

    //
    // FixedChildrenNode overrides
    //

    @Override
    public boolean isTerminal()
    {
        return true;
    }

    //
    // ILiteralNode implementations
    //

    @Override
    public String getValue()
    {
        return getValue(false);
    }

    @Override
    public String getValue(boolean rawValue)
    {
        StringBuilder builder = new StringBuilder();
        int length = getContentsNode().getChildCount();
        for (int i = 0; i < length; i++)
        {
            IASNode child = getContentsNode().getChild(i);
            if (child instanceof ILiteralNode)
            {
                builder.append(((ILiteralNode) child).getValue(rawValue));
            }
        }
        return builder.toString();
    }

    //
    // ILiteralContainerNode implementations
    //

    @Override
    public LiteralNode getBaseTypeNode()
    {
        return baseTypeNode;
    }

    @Override
    public ContainerNode getContentsNode()
    {
        return contentsNode;
    }

    /**
     * Append XML literal token to the contents of this node. If the last
     * literal node of the contents is an XML literal (but not {@code <>}), the
     * token is appended to the last literal node. Otherwise, a new
     * {@link LiteralNode} is added.
     * 
     * @param token XML literal token.
     */
    public final void appendLiteralToken(final ASToken token)
    {
        assert token != null && token.isE4X() : "unexpected token: " + token;
        final ContainerNode contents = getContentsNode();
        final int size = contents.getChildCount();

        if (size > 0)
        {
            final IASNode lastLiteralNode = contents.getChild(size - 1);
            if (lastLiteralNode.getNodeID() == ASTNodeID.LiteralXMLID)
            {
                final LiteralNode appendTarget = (LiteralNode) lastLiteralNode;
                if (!appendTarget.value.equals("<>"))
                {
                    appendTarget.value = appendTarget.value.concat(token
                            .getText());
                    appendTarget.endAfter((IASToken) token);
                    return;
                }
            }
        }

        // Did not find a node to append the token to.
        final LiteralNode newNode = new LiteralNode(token, LiteralType.XML);
        contents.addItem(newNode);
    }
}
