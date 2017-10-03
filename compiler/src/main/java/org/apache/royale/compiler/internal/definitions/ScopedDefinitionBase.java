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

import org.apache.royale.compiler.definitions.IScopedDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.scopes.IASScope;

/**
 * This is the abstract base class for definitions in the symbol table that have
 * a associated scope object.
 */
public abstract class ScopedDefinitionBase extends DefinitionBase implements IScopedDefinition
{
    public ScopedDefinitionBase(String name)
    {
        super(name);
    }

    private ASScope containedScope;

    @Override
    public ASScope getContainedScope()
    {
        return containedScope;
    }

    public void setContainedScope(IASScope value)
    {

        assert value instanceof ASScope;
        containedScope = (ASScope)value;

        containedScope.setContainingDefinition(this);
    }

    /*
     * Used only in asserts.
     */
    @Override
    public boolean verify()
    {
        // Verify the name and source location.
        super.verify();

        // Verify the contained scope.
        if (containedScope != null)
            containedScope.verify();

        return true;
    }
}
