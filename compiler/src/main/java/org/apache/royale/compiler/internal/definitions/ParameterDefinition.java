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

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.scopes.CatchScope;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.IParameterNode;

/**
 * Each instance of this class represents the definition of an ActionScript
 * function parameter in the symbol table.
 * <p>
 * After a parameter definition is in the symbol table, it should always be
 * accessed through the read-only <code>IParameterDefinition</code> interface.
 */
public class ParameterDefinition extends VariableDefinition implements IParameterDefinition
{
    public ParameterDefinition(String name)
    {
        super(name);
    }

    @Override
    public boolean isRest()
    {
        return (flags & FLAG_REST) != 0;
    }

    public void setRest()
    {
        flags |= FLAG_REST;
        
        // The type of a '...' parameter is always Array.
        IReference arrayTypeReference = ReferenceFactory.builtinReference(BuiltinType.ARRAY);
        setTypeReference(arrayTypeReference);
    }

    @Override
    public boolean hasDefaultValue()
    {
        return (flags & FLAG_DEFAULT) != 0;
    }

    public void setHasDefault()
    {
        flags |= FLAG_DEFAULT;
    }

    public void setDefaultValue(Object value)
    {
        initValue = value;
        flags |= FLAG_DEFAULT;
    }

    @Override
    public Object resolveDefaultValue(ICompilerProject project)
    {
        // We can use VariableDefinitions initial value code, as it does
        // the same thing.
        if (hasDefaultValue())
            return resolveInitialValue(project);
        return null;
    }

    @Override
    protected DependencyType getTypeDependencyType()
    {
        // TODO If this a parameter to a function closure
        // then this method should return DependencyType.EXPRESSION.
        return DependencyType.SIGNATURE;
    }

    @Override
    public IParameterNode getNode()
    {
        return (IParameterNode)super.getNode();
    }

    @Override
    public boolean matches(DefinitionBase node)
    {
        boolean matches = super.matches(node);
        if (!matches)
            return false;
        if (node.getNameStart() != getNameStart() || node.getNameEnd() != getNameEnd())
            return false;
        return true;
    }

    /**
     * For debugging only.
     */
    @Override
    public void buildInnerString(StringBuilder sb)
    {
        sb.append(getBaseName());

        String type = getTypeAsDisplayString();
        if (!type.isEmpty())
        {
            sb.append(':');
            sb.append(type);
        }
    }

    @Override
    public final VariableClassification getVariableClassification()
    {
        IASScope containingScope = getContainingScope();
        if (containingScope instanceof CatchScope)
            return VariableClassification.LOCAL;
        return VariableClassification.PARAMETER;
    }
}
