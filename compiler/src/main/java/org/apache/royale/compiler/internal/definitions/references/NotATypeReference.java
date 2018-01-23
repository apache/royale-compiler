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
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * Implementation of {@link IReference} representing the presence of a type
 * annotation that can not possibly be resolved to a type. An example would be:
 * 
 * <pre>
 * class C extends a.b.c.d.Foo
 * {
 * }
 * </pre>
 * 
 * If <code>a.b.c.d</code> is not a package name, then a
 * <code>NotATypeReference</code> is used to represent the reference. This is
 * because a property access will be an error, but we have to remember that
 * something was specified for the base class so we can report the correct error
 * when we try and resolve it.
 */
public class NotATypeReference implements IReferenceMName
{
    /**
     * Constructor.
     */
    public NotATypeReference(String name)
    {
        this.name = name;
    }

    private final String name;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public IDefinition resolve(ICompilerProject project, IASScope scope,
                               DependencyType dependencyType,
                               boolean canEscapeWith)
    {
        return null;
    }

    @Override
    public String getDisplayString()
    {
        return getName();
    }

    @Override
    public Name getMName(ICompilerProject project, IASScope scope)
    {
        return null;
    }
}
