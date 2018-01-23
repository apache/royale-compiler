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

package org.apache.royale.compiler.internal.scopes;

import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.scopes.IASScope;

/**
 * {@link ASScope} subclass for Catch block scopes.
 */
public final class CatchScope extends NoDefinitionScope implements IASScope
{
    /**
     * 
     */
    public CatchScope(ASScope containingScope)
    {
        super(containingScope);
    }

    /**
     * Add the parameter for the catch scope to this scope - can't use
     * addDefinition because it will hoist the defn to the containing scope.
     * 
     * @param param The parameter definition.
     */
    public void setParameterDefinition(IParameterDefinition param)
    {
        this.addDefinitionToThisScope(param);
    }
}
