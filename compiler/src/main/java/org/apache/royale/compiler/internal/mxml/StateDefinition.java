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

package org.apache.royale.compiler.internal.mxml;

import java.util.HashMap;
import java.util.Map;

import org.apache.royale.compiler.mxml.IStateDefinition;
import org.apache.royale.compiler.mxml.IStateGroupDefinition;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.mxml.IMXMLStateNode;

/**
 * {@code StateDefinition} represents a state in MXML 2009 or later. States are
 * per-class, so these objects are owned by the {@code ClassDefinitionNodes}
 * that define classes in MXML.
 */
public class StateDefinition extends StateDefinitionBase implements IStateDefinition
{
    /**
     * Constructor.
     */
    public StateDefinition(IMXMLStateNode node, IASScope containingScope, String name, int nameStart, int nameEnd)
    {
        super(name, node.getClassDefinitionNode().getDefinition(), containingScope, nameStart, nameEnd);
        this.node = node;
    }

    private IMXMLStateNode node;

    /**
     * A map mapping group names to {@code IStateGroup} objects. This is
     * effectively the set of groups that include this state, but we use a map
     * because we're interested in this set both as a set of group names (the
     * String keys) and a set of group objects (the {@code IStateGroup} values).
     */
    private Map<String, IStateGroupDefinition> groupMap;
    
    //
    // DefinitionBase overrides
    //

    @Override
    public IMXMLStateNode getNode()
    {
        return node;
    }
    
    //
    // IStateDefinition implementations
    //

    @Override
    public String[] getStateGroups()
    {
        return groupMap != null ? groupMap.keySet().toArray(new String[0]) : new String[0];
    }

    @Override
    public IStateGroupDefinition[] resolveStateGroups()
    {
        return groupMap.values().toArray(new IStateGroupDefinition[0]);
    }

    @Override
    public boolean isIncludedInStateGroup(String group)
    {
        return groupMap.containsKey(group);
    }
    
    //
    // Other methods
    //

    /**
     * Records that this state belongs to a specified group.
     * 
     * @param group An {@code IStateGroup} to which this state belongs.
     */
    public void addGroup(IStateGroupDefinition group)
    {
        if (groupMap == null)
            groupMap = new HashMap<String, IStateGroupDefinition>(0);

        groupMap.put(group.getBaseName(), group);
    }
}
