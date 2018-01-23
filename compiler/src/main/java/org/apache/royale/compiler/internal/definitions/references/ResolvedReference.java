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

package org.apache.royale.compiler.internal.definitions.references;

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.references.IReferenceMName;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * Implementation of {@link IReference} representing a reference that always
 * resolves to the definition passed to the constructor.
 * <p>
 * This is useful for <code>Vector</code> methods, where the return type is the
 * type parameter for the <code>Vector</code> (i.e., <code>T</code> in
 * {@code Vector.<T>}. If a <code>Vector</code> method returns <code>T</code>
 * then we know what it is; we don't need to walk up the scope chain to resolve
 * it.
 */
public class ResolvedReference implements IReferenceMName
{
    /**
     * Constructor.
     */
    public ResolvedReference(IDefinition definition)
    {
        this.definition = definition;
    }

    IDefinition definition;

    @Override
    public String getName()
    {
        return definition.getQualifiedName();
    }

    @Override
    public IDefinition resolve(ICompilerProject project, IASScope scope,
                               DependencyType dependencyType,
                               boolean canEscapeWith)
    {
        return definition;
    }

    @Override
    public String getDisplayString()
    {
        return definition.getQualifiedName();
    }

    @Override
    public Name getMName(ICompilerProject project, IASScope scope)
    {
        if (definition instanceof DefinitionBase)
            return ((DefinitionBase)definition).getMName(project);

        return null;
    }
}
