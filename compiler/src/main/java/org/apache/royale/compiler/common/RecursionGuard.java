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

package org.apache.royale.compiler.common;

import java.util.HashSet;

import org.apache.royale.compiler.definitions.IDefinition;

/**
 * Recursion guard that can be used throughout code model to make sure we don't infinitely recurse when
 * performing type lookups, etc.  Since user code is not guaranteed to follow language rules, these are very important to avoid loops.
 */
public class RecursionGuard {
	
	public HashSet<IDefinition> visitedDefinitions;
	public boolean foundLoop = false;
	
	public RecursionGuard()
	{
		visitedDefinitions = null;
	}
	
	public RecursionGuard( RecursionGuard other)
	{
	    this.foundLoop = other.foundLoop;
	    if (other.visitedDefinitions != null)
	        this.visitedDefinitions = new HashSet<IDefinition>(other.visitedDefinitions);
	}
	
	/**
	 * Create a guard that is pre-populated with the passed in definition.  the definition will be considered visited
	 * @param initialDefinition the {@link IDefinition} to mark as visited
	 */
	public RecursionGuard(IDefinition initialDefinition)
	{
		visitedDefinitions = new HashSet<IDefinition>();
		visitedDefinitions.add(initialDefinition);
	}
	
	public boolean isLoop(IDefinition target) {
        if(visitedDefinitions == null)
        	visitedDefinitions = new HashSet<IDefinition>();
        if (visitedDefinitions.add(target))
        	return false;
        
        foundLoop = true;
        return true;
    }
}
