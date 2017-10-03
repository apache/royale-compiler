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

package org.apache.royale.compiler.internal.tree.as.metadata;

import antlr.Token;

import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.parsing.TokenBase;
import org.apache.royale.compiler.internal.tree.as.QualifiedNameExpressionNode;
import org.apache.royale.compiler.tree.as.ICommonClassNode;
import org.apache.royale.compiler.tree.metadata.IEventTagNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

public class EventTagNode extends BaseDefinitionMetaTagNode implements IEventTagNode
{
    /**
     * Constructor.
     */
    public EventTagNode()
    {
        super(IMetaAttributeConstants.ATTRIBUTE_EVENT);
    }

    private QualifiedNameExpressionNode typeNode;

    public void setEvent(Token typeToken)
    {
        if (typeNode == null)
        {
            typeNode = new QualifiedNameExpressionNode((TokenBase)typeToken);
            typeNode.setParent(this);
            addToMap(IMetaAttributeConstants.NAME_EVENT_TYPE, getEventTypeName());
        }
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addChildInOrder(nameNode, fillInOffsets);
        addChildInOrder(typeNode, fillInOffsets);
    }

    @Override
    protected int getInitialChildCount()
    {
        return 2;
    }

    private String getEventTypeName()
    {
        return typeNode != null ? typeNode.getName() : "";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof EventTagNode)
        {
            if (!equals(((EventTagNode)obj).nameNode, nameNode))
                return false;
            if (!equals(((EventTagNode)obj).typeNode, typeNode))
                return false;
        }
        else
        {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public IDefinition getDefinition()
    {
        ICommonClassNode decoratedClassNode =
            (ICommonClassNode)getAncestorOfType(ICommonClassNode.class);
        if (decoratedClassNode == null)
            return null;
        
        IClassDefinition decoratedClassDefinition = decoratedClassNode.getDefinition();
        assert decoratedClassDefinition != null;

        IWorkspace workspace = getWorkspace();
        return decoratedClassDefinition.getEventDefinition(workspace, getName());
    }
}
