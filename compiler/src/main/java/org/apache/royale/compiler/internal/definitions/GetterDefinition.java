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
import org.apache.royale.compiler.problems.IncompatibleOverrideProblem;
import org.apache.royale.compiler.projects.ICompilerProject;

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
    
        if (override != null && this.getNamespaceReference().isLanguageNamespace()) {
            boolean valid = isProtected() && override.isProtected()
                    || isPublic() && override.isPublic()
                    || isInternal() && override.isInternal();
            
            if (!valid && override.getNamespaceReference() != this.getNamespaceReference()) {
                //if this is private and there is a non private 'override' that's a problem for local name references
                //but we can 'override' a private version in the base classes because there is no local naming conflict
                if (isPrivate() || !(project.getAllowPrivateNameConflicts() && override.isPrivate()))
                    project.getProblems().add(new IncompatibleOverrideProblem(getNode()));
            }
        }

        return override;
    }
}
