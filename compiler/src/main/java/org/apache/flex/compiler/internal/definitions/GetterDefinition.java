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

package org.apache.flex.compiler.internal.definitions;

import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.definitions.IGetterDefinition;
import org.apache.flex.compiler.definitions.ISetterDefinition;
import org.apache.flex.compiler.definitions.metadata.IMetaTag;
import org.apache.flex.compiler.projects.ICompilerProject;

public class GetterDefinition extends AccessorDefinition implements IGetterDefinition
{
    public GetterDefinition(String name)
    {
        super(name);
    }

    @Override
    public ISetterDefinition resolveSetter(ICompilerProject project)
    {
        return (ISetterDefinition)resolveCorrespondingAccessor(project);
    }

    @Override
    public boolean isSkinPart()
    {
        return getSkinPart() != null;
    }

    @Override
    public boolean isRequiredSkinPart()
    {
        IMetaTag skinPart = getSkinPart();
        if (skinPart == null)
            return false;

        return isRequiredSkinPart(skinPart);
    }

    /**
     * For debugging only.
     */
    @Override
    public void buildInnerString(StringBuilder sb)
    {
        sb.append(getNamespaceReferenceAsString());
        sb.append(' ');

        sb.append(IASKeywordConstants.FUNCTION);
        sb.append(' ');

        if (isStatic())
        {
            sb.append(IASKeywordConstants.STATIC);
            sb.append(' ');
        }

        sb.append(IASKeywordConstants.GET);
        sb.append(' ');

        sb.append(getBaseName());
        sb.append('(');
        sb.append(')');

        String type = getTypeAsDisplayString();
        if (!type.isEmpty())
        {
            sb.append(':');
            sb.append(type);
        }
    }

    /**
     * Will return the getter this overrides, if any.
     */
    @Override
    public FunctionDefinition resolveOverriddenFunction(ICompilerProject project)
    {
        FunctionDefinition override = super.resolveOverriddenFunction(project);

        // Name res returns a getter or a setter for getter/setters - make sure we get the getter
        // in case a setter came back
        if (override instanceof SetterDefinition)
            override = ((SetterDefinition)override).resolveCorrespondingAccessor(project);

        return override;
    }
}
