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

package org.apache.royale.compiler.internal.abc;

import org.apache.royale.abc.visitors.IClassVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;

/**
 * Visit class_info and instance_info to build an ActionScript3 class or
 * interface definition.
 */
class TypeDefinitionBuilder implements IClassVisitor
{
    protected TypeDefinitionBuilder(final ABCScopeBuilder scopeBuilder, final TypeDefinitionBase typeDefinition)
    {
        assert typeDefinition instanceof ClassDefinition || typeDefinition instanceof InterfaceDefinition;
        this.scope = new TypeScope(null, typeDefinition);
        this.definition = typeDefinition;
        this.definition.setContainedScope(scope);
        this.scopeBuilder = scopeBuilder;
    }

    private final ABCScopeBuilder scopeBuilder;
    private final TypeDefinitionBase definition;
    private final ASScope scope;

    @Override
    public void visit()
    {
    }

    @Override
    public void visitEnd()
    {
    }

    /**
     * All the static members are on ClassTraits.
     */
    @Override
    public ITraitsVisitor visitClassTraits()
    {
        return new ScopedDefinitionTraitsVisitor(scopeBuilder, scope, true);
    }

    /**
     * All the non-static members are on InstanceTraits.
     */
    @Override
    public ITraitsVisitor visitInstanceTraits()
    {
        INamespaceReference interfNs = null;
        if( definition instanceof InterfaceDefinition )
            interfNs = ((InterfaceDefinition)definition).getInterfaceNamespaceReference();
        return new ScopedDefinitionTraitsVisitor(scopeBuilder, scope, false, interfNs);
    }
}
