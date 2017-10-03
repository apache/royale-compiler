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

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IEventDefinition;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.metadata.IEventTriggerTagNode;

/**
 * Implementation of {@link IEventTriggerTagNode}.
 */
public class EventTriggerTagNode extends MetaTagNode implements IEventTriggerTagNode
{
    /**
     * Constructor.
     * 
     * @param tagName The name of the metaata tag.
     */
    public EventTriggerTagNode(String tagName)
    {
        super(tagName);
    }

    private IdentifierNode eventNameNode;

    public void setEventName(IdentifierNode eventNameNode)
    {
        this.eventNameNode = eventNameNode;
        eventNameNode.setParent(this);
        addToMap(SINGLE_VALUE, getEventName());
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addChildInOrder(eventNameNode, fillInOffsets);
    }

    @Override
    protected int getInitialChildCount()
    {
        return 1;
    }

    /**
     * Resolves this metadata tag to the event definition it produced.
     * 
     * @param project The {@link ICompilerProject} in which the
     * resolutions occurs.
     * @return An {@link IEventDefinition} for the event.
     */
    public IEventDefinition resolveEventTag(ICompilerProject project)
    {
        if (eventNameNode == null)
            return null;
        
        IDefinitionNode classNode = getDecoratedDefinitionNode();
        IClassDefinition classDefinition = (IClassDefinition)classNode.getDefinition();
        
        return classDefinition.getEventDefinition(project.getWorkspace(), getEventName());
    }

    public String getEventName()
    {
        return eventNameNode != null ? eventNameNode.getName() : "";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof EventTriggerTagNode)
        {
            if (!equals(((EventTriggerTagNode)obj).eventNameNode, eventNameNode))
                return false;

        }
        else
        {
            return false;
        }
        return super.equals(obj);
    }
}
