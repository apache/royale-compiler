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

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.mxml.IStateDefinition;
import org.apache.royale.compiler.mxml.IStateGroupDefinition;

/**
 * {@code StateGroupDefinition} represents a state group in an MXML class.
 */
public class StateGroupDefinition extends StateDefinitionBase implements IStateGroupDefinition
{
    /**
     * Constructor.
     */
    public StateGroupDefinition(String name, IClassDefinition containingClass)
    {
        super(name, containingClass, containingClass.getContainedScope(), -1, -1);
    }

    /**
     * A map mapping state names to {@code IStateDefinition} objects. This is
     * effectively the set of states included in this group, but we use a map
     * because we're interested in this set both as a set of state names (the
     * String keys) and a set of state objects (the {@code IStateDefinition}
     * values).
     */
    private Map<String, IStateDefinition> stateMap;
    
    //
    // IStateGroupDefinition implementations
    //

    @Override
    public String[] getIncludedStates()
    {
        return stateMap.keySet().toArray(new String[0]);
    }

    @Override
    public IStateDefinition[] resolveIncludedStates()
    {
        return stateMap.values().toArray(new IStateDefinition[0]);
    }

    @Override
    public boolean isStateIncluded(String state)
    {
        return stateMap.containsKey(state);
    }
    
    //
    // Other methods
    //

    /**
     * Records that this group includes a specified state.
     * 
     * @param state An {@code IStateDefinition} that this group includes.
     */
    public void addState(IStateDefinition state)
    {
        if (stateMap == null)
            stateMap = new HashMap<String, IStateDefinition>(0);

        stateMap.put(state.getBaseName(), state);
    }
}
