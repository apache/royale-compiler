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
import org.apache.royale.compiler.definitions.AppliedVectorDefinitionFactory;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.IReferenceMName;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * Implementation of {@link IReferenceMName} representing a parameterized type, such
 * as {@code Vector.<Foo>}.
 */
public class ParameterizedReference implements IReferenceMName
{
    /**
     * Constructor.
     */
    public ParameterizedReference(IReference name, IReference param)
    {
        this.name = (IReferenceMName)name;
        this.param = (IReferenceMName)param;
    }

    private final IReferenceMName name;

    private final IReferenceMName param;

    @Override
    public String getName()
    {
        return name.getName();
    }

    @Override
    public IDefinition resolve(ICompilerProject project, IASScope scope,
                               DependencyType dependencyType,
                               boolean canEscapeWith)
    {
        IDefinition base = name.resolve(project, scope, dependencyType, canEscapeWith);
        IDefinition typeParam = param.resolve(project, scope, dependencyType, canEscapeWith);
        IDefinition resolvedType = null;
        if (base instanceof ITypeDefinition && typeParam instanceof ITypeDefinition)
        {
            ITypeDefinition baseType = (ITypeDefinition)base;
            IDefinition vectorType = project.getBuiltinType(IASLanguageConstants.BuiltinType.VECTOR);

            if (baseType == vectorType) //only vectors are parameterizable today
                resolvedType = AppliedVectorDefinitionFactory.newVector(project, (ITypeDefinition)typeParam);
        }
        return resolvedType;
    }

    @Override
    public String getDisplayString()
    {
        return name.getDisplayString() + ".<" + (param == null ? "" : param.getDisplayString()) + ">";
    }

    @Override
    public Name getMName(ICompilerProject project, IASScope scope)
    {
        Name baseName = name.getMName(project, scope);
        Name paramName = param.getMName(project, scope);

        return new Name(baseName, paramName);
    }
}
