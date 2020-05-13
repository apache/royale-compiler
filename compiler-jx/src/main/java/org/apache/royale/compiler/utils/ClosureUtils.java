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

package org.apache.royale.compiler.utils;

import java.util.Set;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope.DefinitionPromise;
import org.apache.royale.compiler.units.ICompilationUnit;

public class ClosureUtils
{
	public static void collectPropertyNamesToKeep(ICompilationUnit cu, RoyaleJSProject project, Set<String> result)
    {
        if (project.isExternalLinkage(cu))
        {
            return;
        }
		boolean preventRenamePublic = project.config.getPreventRenamePublicSymbols();
		boolean preventRenameProtected = project.config.getPreventRenameProtectedSymbols();
        for (IDefinition def : cu.getDefinitionPromises())
        {
            if(def instanceof DefinitionPromise)
            {
                def = ((DefinitionPromise) def).getActualDefinition();
            }
            if (def instanceof ITypeDefinition)
            {
                if (def.isImplicit() || def.isNative())
                {
                    continue;
                }
                ITypeDefinition typeDef = (ITypeDefinition) def;
                for (IDefinition localDef : typeDef.getContainedScope().getAllLocalDefinitions())
                {
                    if (localDef.isImplicit())
                    {
                        continue;
                    }
                    if (!localDef.isPublic() && !localDef.isProtected())
                    {
                        continue;
                    }
                    if (localDef.isProtected() && !preventRenameProtected)
                    {
                        continue;
                    }
                    if (localDef.isPublic() && !preventRenamePublic)
                    {
                        continue;
                    }
                    if (!(localDef instanceof IVariableDefinition))
                    {
                        continue;
                    }
                    if (localDef instanceof IFunctionDefinition)
                    {
                        continue;
                    }
                    result.add(localDef.getBaseName());
                }
            }
        }
    } 
}