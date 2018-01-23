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
import org.apache.royale.compiler.definitions.IEventDefinition;
import org.apache.royale.compiler.tree.metadata.IEventTagNode;

/**
 * Each instance of this class represents the definition of an MXML event in the
 * symbol table.
 * <p>
 * MXML events are defined by <code>[Event]</code> metadata on ActionScript
 * class declarations.
 * <p>
 * After an event definition is in the symbol table, it should always be
 * accessed through the read-only <code>IEventDefinition</code> interface.
 */
public class EventDefinition extends MetadataDefinitionBase implements IEventDefinition
{
    public EventDefinition(String name, IClassDefinition decoratedDefinition)
    {
        super(name, IMetaAttributeConstants.ATTRIBUTE_EVENT, decoratedDefinition);
    }

    @Override
    public IEventTagNode getNode()
    {
        return (IEventTagNode)super.getNode();
    }
}
