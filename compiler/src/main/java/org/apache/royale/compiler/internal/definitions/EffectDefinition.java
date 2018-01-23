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

package org.apache.royale.compiler.internal.definitions;

import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IEffectDefinition;
import org.apache.royale.compiler.definitions.IEventDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.metadata.IEffectTagNode;

/**
 * Each instance of this class represent the definition of an MXML effect in the
 * symbol table.
 * <p>
 * MXML effects are defined by <code>[Effect]</code> metadata on ActionScript
 * class declarations.
 * <p>
 * After an effect definition is in the symbol table, it should always be
 * accessed through the read-only <code>IEffectDefinition</code> interface.
 */
public class EffectDefinition extends MetadataDefinitionBase implements IEffectDefinition
{
    public EffectDefinition(String name, IClassDefinition decoratedDefinition)
    {
        super(name, IMetaAttributeConstants.ATTRIBUTE_EFFECT, decoratedDefinition);
    }

    private String event;

    @Override
    public IEffectTagNode getNode()
    {
        return (IEffectTagNode)super.getNode();
    }

    @Override
    public String getEvent()
    {
        return event;
    }

    /**
     * Sets the value of this effect's <code>event</code> attribute.
     * <p>
     * This is the name of the event that will trigger the effect.
     * 
     * @param event The event name as a String.
     */
    public void setEvent(String event)
    {
        this.event = event;
    }

    @Override
    public IEventDefinition resolveEvent(ICompilerProject project)
    {
        IDefinition decoratedDefinition = this.getDecoratedDefinition();
        if (!(decoratedDefinition instanceof IClassDefinition))
            return null;

        if (!(project instanceof RoyaleProject))
            return null;

        RoyaleProject royaleProject = (RoyaleProject)project;
        IClassDefinition decoratedClass = (IClassDefinition)decoratedDefinition;
        return royaleProject.resolveEvent(decoratedClass, event);
    }
}
