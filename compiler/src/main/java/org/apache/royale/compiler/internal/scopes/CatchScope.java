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

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.scopes.IDefinitionSet;

import java.util.Objects;

/**
 * {@link ASScope} subclass for Catch block scopes.
 */
public final class CatchScope extends NoDefinitionScope
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
        parameterDefinition = param;
    }

    private IParameterDefinition parameterDefinition;

    @Override
    public void addDefinition(IDefinition d)
    {
        if (Objects.equals(d.getBaseName(), parameterDefinition.getBaseName())) {
            //this should ensure that declarations inside a catch clause that conflict with the parameter definition (i.e. create ambiguity) cause a compiler error
            this.addDefinitionToThisScope(d);
        } else {
            super.addDefinition(d);
        }
    }


    /**
     * Advanced use only
     * Used only for implementations that require 're-writing' of the default behavior (for example in runtimes that don't
     * have native support for multiple catch clauses).
     * This removes a potential conflicting name definition from the local catch scope (which is usually only reserved for the
     * parameter definition).
     * @param d
     */
    public void displaceParameter(IDefinition d)
    {
        this.removeDefinition(parameterDefinition);
        this.addDefinitionToThisScope(d);
    }


    @Override
    public IDefinitionSet getLocalDefinitionSetByName(String baseName)
    {
        //overrides the base class to return baseName definition(s) from the local set when the catch param is being requested, otherwise get the definitions from the containing scope.
        IDefinitionSet returnSet;
        IDefinitionSet localSet = super.getLocalDefinitionSetByName(baseName);
        if (localSet != null && !localSet.isEmpty()) {
            returnSet = localSet;
        } else {
            returnSet = getContainingScope().getLocalDefinitionSetByName(baseName);
        }

        return returnSet;
    }
}
