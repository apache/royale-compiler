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
 * Represents a state group defined with an MXML 4 document
 */
public interface IStateGroupDefinition extends IStateDefinitionBase
{
    /**
     * Returns an array of {@link IStateDefinition} objects that are found in
     * this IStateGroup
     * 
     * @return an array of {@link IStateDefinition} objects
     */
    IStateDefinition[] resolveIncludedStates();

    /**
     * Returns an array of {@link String} objects that represent the names found
     * in this IStateGroup
     * 
     * @return an array of {@link String} objects
     */
    String[] getIncludedStates();

    /**
     * Determines if the given state is contained within the state group
     * 
     * @param state the name of the state to locate
     * @return true if found in the state group
     */
    boolean isStateIncluded(String state);
}
