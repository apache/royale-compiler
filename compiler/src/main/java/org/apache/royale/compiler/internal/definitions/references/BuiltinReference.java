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
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.references.IReferenceMName;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * Implementation of {@link IReference} representing one of the builtin types,
 * such as <code>Object</code>, <code>String</code> or <code>Array</code>.
 * <p>
 * This reference will always resolve to the builtins for a particular project.
 * It is used for things like the type of a <code>FunctionDefinition</code>,
 * which should be the global <code>Function</code> type, and not some
 * <code>Function</code> type defined in some other random namespace.
 */
public class BuiltinReference implements IReferenceMName
{
    /**
     * Constructor.
     */
    public BuiltinReference(IASLanguageConstants.BuiltinType type)
    {
        builtinType = type;
    }

    private final IASLanguageConstants.BuiltinType builtinType;

    @Override
    public String getName()
    {
        return builtinType.getName();
    }

    @Override
    public IDefinition resolve(ICompilerProject project, IASScope scope,
                               DependencyType dependencyType,
                               boolean canEscapeWith)
    {
        IDefinition definition = project.getBuiltinType(builtinType);

        ((ASScope)scope).addDependencyOnBuiltinType(project, builtinType, dependencyType);

        return definition;
    }

    @Override
    public String getDisplayString()
    {
        return builtinType.getName();
    }

    @Override
    public Name getMName(ICompilerProject project, IASScope scope)
    {
        IDefinition def = project.getBuiltinType(builtinType);
        if (def == null)
            return null;

        return ((DefinitionBase)def).getMName(project);
    }
}
