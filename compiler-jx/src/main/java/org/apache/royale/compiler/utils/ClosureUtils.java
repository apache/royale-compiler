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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.royale.compiler.asdoc.royale.ASDocComment;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition.VariableClassification;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.codegen.js.utils.DocEmitterUtils;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.scopes.IFileScope;
import org.apache.royale.compiler.units.ICompilationUnit;

public class ClosureUtils
{
	public static void collectPropertyNamesToKeep(ICompilationUnit cu, RoyaleJSProject project, Set<String> result)
    {
        if (project.isExternalLinkage(cu))
        {
            return;
        }
		boolean preventRenamePublic = project.config != null && project.config.getPreventRenamePublicSymbols();
        boolean preventRenameProtected = project.config != null && project.config.getPreventRenameProtectedSymbols();
        boolean exportPublic = project.config != null && project.config.getExportPublicSymbols();
        boolean exportProtected = project.config != null && project.config.getExportProtectedSymbols();
        try
        {
            for(IASScope scope : cu.getFileScopeRequest().get().getScopes())
            {
                for(IDefinition def : scope.getAllLocalDefinitions())
                {
                    if(def instanceof IPackageDefinition)
                    {
                        //source files seem to return packages while SWC files
                        //return symbols inside the packages
                        //we want the symbols, so drill down into the package
                        IPackageDefinition packageDef = (IPackageDefinition) def;
                        def = packageDef.getContainedScope().getAllLocalDefinitions().iterator().next();
                    }
                    if(scope instanceof IFileScope && def.isPrivate())
                    {
                        //file-private symbols are emitted like static variables
                        result.add(def.getBaseName());
                    }
                    if (def instanceof IVariableDefinition
                            && !(def instanceof IAccessorDefinition))
                    {
                        IVariableDefinition varDef = (IVariableDefinition) def;
                        if (varDef.getVariableClassification().equals(VariableClassification.PACKAGE_MEMBER)) {
                            result.add(def.getBaseName());
                        }
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
                            INamespaceReference nsRef = localDef.getNamespaceReference();
                            boolean isPublic = nsRef instanceof INamespaceDefinition.IPublicNamespaceDefinition;
                            boolean isProtected = nsRef instanceof INamespaceDefinition.IProtectedNamespaceDefinition
                                    || nsRef instanceof INamespaceDefinition.IStaticProtectedNamespaceDefinition;
                            
                            if ((isPublic && preventRenamePublic) || (isProtected && preventRenameProtected))
                            {
                                if (localDef instanceof IAccessorDefinition)
                                {
                                    if ((isPublic && exportPublic) || (isProtected && exportProtected))
                                    {
                                        //if an accessor is exported, we don't
                                        //need to prevent renaming
                                        //(not true for other symbol types)
                                        continue;
                                    }
                                }
                                result.add(localDef.getBaseName());
                            }
                        }
                    }
                }
            }
        }
        catch(InterruptedException e) {}
    }

    //the result must be a LinkedHashSet so that it iterates over the keys in
    //the same order that they were added
    public static void collectSymbolNamesToExport(ICompilationUnit cu, RoyaleJSProject project, LinkedHashSet<String> symbolsResult)
    {
        if (project.isExternalLinkage(cu))
        {
            return;
        }
        boolean exportPublic = project.config != null && project.config.getExportPublicSymbols();
        boolean exportProtected = project.config != null && project.config.getExportProtectedSymbols();
        try
        {
            String parentQName = null;
            Set<String> filePrivateNames = new LinkedHashSet<String>();
            for(IASScope scope : cu.getFileScopeRequest().get().getScopes())
            {
                for(IDefinition def : scope.getAllLocalDefinitions())
                {
                    if(def instanceof IPackageDefinition)
                    {
                        //source files seem to return packages while SWC files
                        //return symbols inside the packages
                        //we want the symbols, so drill down into the package
                        IPackageDefinition packageDef = (IPackageDefinition) def;
                        def = packageDef.getContainedScope().getAllLocalDefinitions().iterator().next();
                    }
                    if (def.isImplicit() || def.isNative())
                    {
                        continue;
                    }

                    String qualifiedName = def.getQualifiedName();
                    boolean isFilePrivate = false;
                    if(scope instanceof IFileScope && def.isPrivate())
                    {
                        isFilePrivate = true;
                        filePrivateNames.add(qualifiedName);
                    }
                    else
                    {
                        if (project.isExterns(qualifiedName))
                        {
                            return;
                        }
                        symbolsResult.add(qualifiedName);
                        if(parentQName == null)
                        {
                            parentQName = qualifiedName;
                        }
                    }
                    if (def instanceof ITypeDefinition)
                    {
                        ITypeDefinition typeDef = (ITypeDefinition) def;
                        ASDocComment asDoc = (ASDocComment) typeDef.getExplicitSourceComment();
                        if (asDoc != null && DocEmitterUtils.hasSuppressExport(null, asDoc.commentNoEnd()))
                        {
                            continue;
                        }

                        for(IDefinition localDef : typeDef.getContainedScope().getAllLocalDefinitions())
                        {
                            if (localDef.isImplicit())
                            {
                                continue;
                            }
                            INamespaceReference nsRef = localDef.getNamespaceReference();
                            boolean isPublic = nsRef instanceof INamespaceDefinition.IPublicNamespaceDefinition;
                            boolean isProtected = nsRef instanceof INamespaceDefinition.IProtectedNamespaceDefinition
                                    || nsRef instanceof INamespaceDefinition.IStaticProtectedNamespaceDefinition;
                            if (localDef instanceof IFunctionDefinition
                                    && !(localDef instanceof IAccessorDefinition)
                                    // the next two conditions are temporary
                                    // and more symbols will be exported in the future
                                    && localDef.isStatic()
                                    && isPublic)
                            {
                                if ((isPublic && exportPublic) || (isProtected && exportProtected))
                                {
                                    if (isFilePrivate)
                                    {
                                        filePrivateNames.add(qualifiedName + (localDef.isStatic() ? "." : ".prototype.") + localDef.getBaseName());
                                    }
                                    else
                                    {
                                        symbolsResult.add(qualifiedName + (localDef.isStatic() ? "." : ".prototype.") + localDef.getBaseName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for(String filePrivateName : filePrivateNames)
            {
                symbolsResult.add(parentQName + "." + filePrivateName);
            }
        }
        catch(InterruptedException e) {}
    }
}