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

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import java.util.ArrayList;
import java.util.List;

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
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IVariableNode;

public class JSFlexJSDocEmitter extends JSGoogDocEmitter
{
    private List<String> classIgnoreList;
    private List<String> ignoreList;

    public JSFlexJSDocEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    public List<String> getClassIgnoreList()
    {
        return classIgnoreList;
    }
    
    public void setClassIgnoreList(List<String> value)
    {
        this.classIgnoreList = value;
    }

    @Override
    protected String convertASTypeToJS(String name, String pname)
    {
        if (ignoreList != null)
        {
            if (ignoreList.contains(pname + "." + name))
                return IASLanguageConstants.Object;
        }
        if (classIgnoreList != null)
        {
            if (classIgnoreList.contains(pname + "." + name))
                return IASLanguageConstants.Object;
        }
        if (name.matches("Vector\\.<.*>"))
        	return IASLanguageConstants.Array;
        
        name = super.convertASTypeToJS(name, pname);
        return formatQualifiedName(name);
    }

    @Override
    protected String formatQualifiedName(String name)
    {
    	/*
        if (name.contains("goog.") || name.startsWith("Vector."))
            return name;
        name = name.replaceAll("\\.", "_");
        */
    	if (name.startsWith("window."))
    		name = name.substring(7);
        return name;
    }

    @Override
    public void emitMethodDoc(IFunctionNode node, ICompilerProject project)
    {
        ignoreList = null;

        IClassDefinition classDefinition = resolveClassDefinition(node);

        ASDocComment asDoc = (ASDocComment) node.getASDocComment();

        if (node instanceof IFunctionNode)
        {
            boolean hasDoc = false;

            if (node.isConstructor())
            {
                if (asDoc != null && MXMLJSC.keepASDoc)
                    write(changeAnnotations(asDoc.commentNoEnd()));
                else
                    begin();
                hasDoc = true;

                emitJSDocLine(JSEmitterTokens.CONSTRUCTOR);

                IClassDefinition parent = (IClassDefinition) node
                        .getDefinition().getParent();
                IClassDefinition superClass = parent.resolveBaseClass(project);
                String qname = (superClass != null) ? project.getActualPackageName(superClass.getQualifiedName()) : null;

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
                    if (type == null) {
                        System.out.println(iReference.getDisplayString()
                                + " not resolved in "
                                + classDefinition.getQualifiedName());
                    } else {
                        emitImplements(type, project.getActualPackageName(type.getPackageName()));
                    }
                }
            }
            else
            {
                String ns = node.getNamespace();
                if (ns != null)
                {
                    if (asDoc != null && MXMLJSC.keepASDoc)
                    {
                        String docText = asDoc.commentNoEnd();
                        String ignoreToken = JSFlexJSEmitterTokens.IGNORE_COERCION
                                .getToken();
                        if (docText.contains(ignoreToken))
                            loadIgnores(docText);
                        write(changeAnnotations(asDoc.commentNoEnd()));
                    }
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
                        write(changeAnnotations(asDoc.commentNoEnd()));
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

                emitParam(pnode, project.getActualPackageName(tdef.getPackageName()));
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
                            write(changeAnnotations(asDoc.commentNoEnd()));
                        else
                            begin();
                        emitMethodAccess(node);
                        hasDoc = true;
                    }

                    ITypeDefinition tdef = ((IFunctionDefinition) node
                            .getDefinition()).resolveReturnType(project);

                    String packageName = "";
                    packageName = tdef != null ? tdef.getPackageName() : "";

                    emitReturn(node, project.getActualPackageName(packageName));
                }

                // @override
                Boolean override = node.hasModifier(ASModifier.OVERRIDE);
                if (override)
                {
                    if (!hasDoc)
                    {
                        if (asDoc != null && MXMLJSC.keepASDoc)
                            write(changeAnnotations(asDoc.commentNoEnd()));
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

    private void loadIgnores(String doc)
    {
        ignoreList = new ArrayList<String>();
        String ignoreToken = JSFlexJSEmitterTokens.IGNORE_COERCION.getToken();
        int index = doc.indexOf(ignoreToken);
        while (index != -1)
        {
            String ignorable = doc.substring(index + ignoreToken.length());
            int endIndex = ignorable.indexOf("\n");
            ignorable = ignorable.substring(0, endIndex);
            ignorable = ignorable.trim();
            ignoreList.add(ignorable);
            index = doc.indexOf(ignoreToken, index + endIndex);
        }
    }

    private String changeAnnotations(String doc)
    {
        // rename these tags so they don't conflict with generated
        // jsdoc tags
        String pass1 = doc.replaceAll("@param", "@asparam");
        String pass2 = pass1.replaceAll("@return", "@asreturn");
        String pass3 = pass2.replaceAll("@private", "@asprivate");
        return pass3;
    }

    public void emitInterfaceMemberDoc(IDefinitionNode node,
            ICompilerProject project)
    {
        boolean hasDoc = false;

        ASDocComment asDoc = (ASDocComment) ((IFunctionNode) node)
                .getASDocComment();

        String returnType = ((IFunctionNode) node).getReturnType();
        if (returnType != "" && returnType != ASEmitterTokens.VOID.getToken()) // has return
        {
            if (asDoc != null && MXMLJSC.keepASDoc)
                write(changeAnnotations(asDoc.commentNoEnd()));
            else
                begin();
            hasDoc = true;

            ITypeDefinition tdef = ((IFunctionDefinition) node.getDefinition())
                    .resolveReturnType(project);

            emitReturn((IFunctionNode) node, tdef.getPackageName());
        }

        IParameterNode[] parameters = ((IFunctionNode) node)
                .getParameterNodes();
        for (IParameterNode pnode : parameters)
        {
            if (!hasDoc)
            {
                if (asDoc != null && MXMLJSC.keepASDoc)
                    write(changeAnnotations(asDoc.commentNoEnd()));
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
    public void emitFieldDoc(IVariableNode node, IDefinition def, ICompilerProject project)
    {
        begin();

        String ns = node.getNamespace();
        if (ns == IASKeywordConstants.PRIVATE)
        {
            emitPrivate(node);
        }
        else if (ns == IASKeywordConstants.PROTECTED)
        {
            emitProtected(node);
        }
        else
        {
            emitPublic(node);
        }

        if (node.isConst())
            emitConst(node);

        String packageName = "";
        if (def != null)
            packageName = def.getPackageName();

        emitType(node, project.getActualPackageName(packageName));

        end();
    }

}
