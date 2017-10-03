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

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.mxml.IStateDefinitionBase;
import org.apache.royale.compiler.scopes.IASScope;

/**
 * {@code StateDefinitionBase} is an abstract base class for
 * {@code StateDefinition} and {@code StateGroupDefinition}, which represent
 * states and state groups in MXML 2009 and later.
 */
public abstract class StateDefinitionBase extends DefinitionBase implements IStateDefinitionBase
{
    /**
     * Constructor.
     */
    public StateDefinitionBase(String name, IClassDefinition containingClass, IASScope containingScope, int nameStart, int nameEnd)
    {
        super(name);
        this.containingClass = containingClass;
        setNameLocation(nameStart, nameEnd);
        setContainingScope(containingScope);
    }

    private final IClassDefinition containingClass;
    
    //
    // DefinitionBase overrides
    //

    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        return getBaseName();
    }

    @Override
    public boolean isImplicit()
    {
        return true; //this node will always be implicit, even though it will have offsets
    }
    
    //
    // IStateDefinitionBase implementations
    //

    @Override
    public int compareTo(IStateDefinitionBase other)
    {
        return getNameStart() - other.getNameStart();
    }
    
    @Override
    public IClassDefinition getContainingClass()
    {
        return containingClass;
    }
}
