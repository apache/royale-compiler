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

package org.apache.flex.compiler.internal.codegen.js.vf2js;

import org.apache.flex.compiler.asdoc.flexjs.ASDocComment;
import org.apache.flex.compiler.clients.MXMLJSC;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.references.IReference;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogDocEmitter;
import org.apache.flex.compiler.internal.definitions.InterfaceDefinition;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IVariableNode;

public class JSVF2JSDocEmitter extends JSGoogDocEmitter
{

    public JSVF2JSDocEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emitInterfaceDoc(IInterfaceNode node, ICompilerProject project)
    {
        begin();

        emitJSDocLine(JSEmitterTokens.INTERFACE.getToken());

        boolean hasQualifiedNames = true;
        IExpressionNode[] inodes = node.getExtendedInterfaceNodes();
        for (IExpressionNode inode : inodes)
        {
            IDefinition dnode = inode.resolve(project);
            if (dnode != null)
            {
                emitJSDocLine(ASEmitterTokens.EXTENDS, dnode.getQualifiedName());
            }
            else
            {
                hasQualifiedNames = false;
                break;
            }
        }
        
        if (!hasQualifiedNames)
        {
            String[] inames = node.getExtendedInterfaces();
            for (String iname : inames)
            {
                emitJSDocLine(ASEmitterTokens.EXTENDS, iname);
            }
        }

        end();
    }

    @Override
    public void emitMethodDoc(IFunctionNode node, ICompilerProject project)
    {
        IClassDefinition classDefinition = resolveClassDefinition(node);

        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        
        if (node instanceof IFunctionNode)
        {
            boolean hasDoc = false;

            if (node.isConstructor())
            {
                if (asDoc != null && MXMLJSC.keepASDoc)
                    write(asDoc.commentNoEnd());
                else
                    begin();
                hasDoc = true;

                emitJSDocLine(JSEmitterTokens.CONSTRUCTOR);

                IClassDefinition parent = (IClassDefinition) node
                        .getDefinition().getParent();
                IClassDefinition superClass = parent.resolveBaseClass(project);
                String qname = superClass.getQualifiedName();

                if (superClass != null
                        && !qname.equals(IASLanguageConstants.Object))
                    emitExtends(superClass, superClass.getPackageName());

                IReference[] references = classDefinition
                        .getImplementedInterfaceReferences();
                for (IReference iReference : references)
                {
                    ITypeDefinition type = (ITypeDefinition) iReference
                            .resolve(project, (ASScope) classDefinition
                                    .getContainingScope(),
                                    DependencyType.INHERITANCE, true);
                    if (type == null)
                    	System.out.println(iReference.getDisplayString() + " not resolved in " + classDefinition.getQualifiedName());
                    emitImplements(type, type.getPackageName());
                }
            }
            else
            {
                String ns = node.getNamespace();
                if (ns != null)
                {
                    if (asDoc != null && MXMLJSC.keepASDoc)
                        write(asDoc.commentNoEnd());
                    else
                        begin();
                    emitMethodAccess(node);
                    hasDoc = true;
                }
            }

            // @param
            IParameterNode[] parameters = node.getParameterNodes();
            for (IParameterNode pnode : parameters)
            {
                if (!hasDoc)
                {
                    if (asDoc != null && MXMLJSC.keepASDoc)
                        write(asDoc.commentNoEnd());
                    else
                        begin();
                    emitMethodAccess(node);
                    hasDoc = true;
                }

                IExpressionNode enode = pnode.getNameExpressionNode();

                // ToDo (erikdebruin): add VF2JS conditional -> only use check during full SDK compilation
                ITypeDefinition tdef = enode.resolveType(project);
                if (tdef == null)
                    continue;
                
                emitParam(pnode, tdef.getPackageName());
            }

            if (!node.isConstructor())
            {
                // @return
                String returnType = node.getReturnType();
                if (returnType != ""
                        && returnType != ASEmitterTokens.VOID.getToken())
                {
                    if (!hasDoc)
                    {
                        if (asDoc != null && MXMLJSC.keepASDoc)
                            write(asDoc.commentNoEnd());
                        else
                            begin();
                        emitMethodAccess(node);
                        hasDoc = true;
                    }

                    ITypeDefinition tdef = ((IFunctionDefinition)node.getDefinition())
                            .resolveReturnType(project);

                    String packageName = "";
                    if (tdef instanceof InterfaceDefinition)
                        packageName = tdef.getPackageName();
                    else
                        packageName = node.getPackageName();
                    
                    emitReturn(node, packageName);
                }

                // @override
                Boolean override = node.hasModifier(ASModifier.OVERRIDE);
                if (override)
                {
                    if (!hasDoc)
                    {
                        if (asDoc != null && MXMLJSC.keepASDoc)
                            write(asDoc.commentNoEnd());
                        else
                            begin();
                        emitMethodAccess(node);
                        hasDoc = true;
                    }

                    emitOverride(node);
                }
            }

            if (hasDoc)
                end();
        }
    }
    
    @Override
    public void emitVarDoc(IVariableNode node, IDefinition def)
    {
        String packageName = "";
        if (def != null)
            packageName = def.getPackageName();

        if (!node.isConst())
        {
            IDefinition ndef = node.getDefinition();
            if (emitter != null && emitter instanceof JSVF2JSEmitter)
            {
            	ICompilerProject project = ((JSVF2JSEmitter)emitter).project;
                if (project != null)
                {
                    packageName = ((ITypeDefinition)ndef.resolveType(project))
                            .getPackageName();
                }
            }
        }
        
        emitTypeShort(node, packageName);
    }

    
    public void emitInterfaceMemberDoc(IDefinitionNode node, ICompilerProject project)
    {
        boolean hasDoc = false;
        
        ASDocComment asDoc = (ASDocComment) ((IFunctionNode) node).getASDocComment();
        
        String returnType = ((IFunctionNode) node).getReturnType();
        if (returnType != ""
                && returnType != ASEmitterTokens.VOID.getToken()) // has return
        {
            if (asDoc != null && MXMLJSC.keepASDoc)
                write(asDoc.commentNoEnd());
            else
                begin();
            hasDoc = true;

            ITypeDefinition tdef = ((IFunctionDefinition)node.getDefinition())
                    .resolveReturnType(project);

            emitReturn((IFunctionNode) node, tdef.getPackageName());
        }

        IParameterNode[] parameters = ((IFunctionNode) node).getParameterNodes();
        for (IParameterNode pnode : parameters)
        {
            if (!hasDoc)
            {
                if (asDoc != null && MXMLJSC.keepASDoc)
                    write(asDoc.commentNoEnd());
                else
                    begin();
                hasDoc = true;
            }

            IExpressionNode enode = pnode.getNameExpressionNode();
            emitParam(pnode, enode.resolveType(project).getPackageName());
        }

        if (hasDoc)
            end();
    }

    @Override
    public void emitMethodAccess(IFunctionNode node)
    {
        String ns = node.getNamespace();
        if (ns == IASKeywordConstants.PRIVATE)
        {
            emitPrivate(node);
        }
        else if (ns == IASKeywordConstants.PROTECTED)
        {
            emitProtected(node);
        }
        else if (ns == IASKeywordConstants.PUBLIC)
        {
            emitPublic(node);
        }
    }

    @Override
    public void emitExtends(IClassDefinition superDefinition, String packageName)
    {
        emitJSDocLine(ASEmitterTokens.EXTENDS, superDefinition.getQualifiedName());
    }

    @Override
    public void emitImplements(ITypeDefinition definition, String packageName)
    {
        emitJSDocLine(ASEmitterTokens.IMPLEMENTS, definition.getQualifiedName());
    }
}
