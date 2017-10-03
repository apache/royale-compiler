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

import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.tree.as.ICommonClassNode;
import org.apache.royale.compiler.tree.metadata.IEffectTagNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

public final class EffectTagNode extends BaseDefinitionMetaTagNode implements IEffectTagNode
{
    /**
     * Constructor.
     */
    public EffectTagNode()
    {
        super(IMetaAttributeConstants.ATTRIBUTE_EFFECT);
    }

    private IdentifierNode eventName;

    public void setEvent(IdentifierNode event)
    {
        if (eventName == null)
        {
            eventName = event;
            if (eventName != null)
            {
                eventName.setParent(this);
                addToMap(IMetaAttributeConstants.NAME_EFFECT_EVENT, getEventName());
            }
        }
    }

    public String getEventName()
    {
        return eventName != null ? eventName.getName() : "";
    }

    public IdentifierNode getEventNode()
    {
        return eventName;
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addChildInOrder(nameNode, fillInOffsets);
        addChildInOrder(eventName, fillInOffsets);
    }

    @Override
    protected int getInitialChildCount()
    {
        return 2;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof EffectTagNode)
        {
            if (!equals(((EffectTagNode)obj).nameNode, nameNode))
                return false;
            if (!equals(((EffectTagNode)obj).eventName, eventName))
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
        ICommonClassNode decoratedClassNode = (ICommonClassNode)getAncestorOfType(ICommonClassNode.class);
        if (decoratedClassNode == null)
            return null;
        IWorkspace workspace = getWorkspace();
        IClassDefinition decoratedClassDefinition = decoratedClassNode.getDefinition();
        assert decoratedClassDefinition != null;
        return decoratedClassDefinition.getEffectDefinition(workspace, getName());
    }
}
