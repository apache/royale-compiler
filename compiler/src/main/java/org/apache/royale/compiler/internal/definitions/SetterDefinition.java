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

import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.definitions.IGetterDefinition;
import org.apache.royale.compiler.definitions.ISetterDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.projects.ICompilerProject;

public class SetterDefinition extends AccessorDefinition implements ISetterDefinition
{
    public SetterDefinition(String name)
    {
        super(name);
    }

    @Override
    public IGetterDefinition resolveGetter(ICompilerProject project)
    {
        return (IGetterDefinition)resolveCorrespondingAccessor(project);
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

        if (isStatic())
        {
            sb.append(IASKeywordConstants.STATIC);
            sb.append(' ');
        }

        sb.append(IASKeywordConstants.FUNCTION);
        sb.append(' ');

        sb.append(IASKeywordConstants.SET);
        sb.append(' ');

        sb.append(getBaseName());

        sb.append('(');
        sb.append(getTypeAsDisplayString());
        sb.append(')');

        sb.append(':');
        sb.append(IASKeywordConstants.VOID);
    }

    /**
     * Will return the setter this overrides, if any.
     */
    @Override
    public FunctionDefinition resolveOverriddenFunction(ICompilerProject project)
    {
        FunctionDefinition override = super.resolveOverriddenFunction(project);

        // Name res returns a getter or a setter for getter/setters - make sure we get the getter
        // in case a setter came back
        if (override instanceof GetterDefinition)
            override = ((GetterDefinition)override).resolveCorrespondingAccessor(project);

        return override;
    }

}
