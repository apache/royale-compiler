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

package org.apache.royale.compiler.mxml;

/**
 * Represents the base definition of either a state or a state group defined
 * with an MXML 4 document
 */
public interface IStateDefinition extends IStateDefinitionBase
{
    /**
     * Returns any state groups that this IStateDefinitionBase is a member of
     * 
     * @return an array of IStates
     */
    IStateGroupDefinition[] resolveStateGroups();

    /**
     * Returns a list of the names of any state groups this State is a member of
     * 
     * @return an array of Strings
     */
    String[] getStateGroups();

    /**
     * Determines if this state is included in a specific group
     * 
     * @param group the state group
     * @return true if included
     */
    boolean isIncludedInStateGroup(String group);
}
