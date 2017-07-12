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
import org.apache.flex.compiler.internal.codegen.js.JSSessionModel;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogDocEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.BindableEmitter;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
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
    private List<String> coercionList;
    public boolean emitStringConversions = true;

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
        if (coercionList != null)
        {
            if (!coercionList.contains(pname + "." + name))
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

    private boolean usedNames = false;
    
    @Override
    protected String formatQualifiedName(String name)
    {
    	return ((JSFlexJSEmitter)emitter).formatQualifiedName(name, !usedNames);
    }

    @Override
    public void emitMethodDoc(IFunctionNode node, ICompilerProject project)
    {
    	FlexJSProject fjp = (FlexJSProject)project;
        boolean keepASDoc = fjp.config != null && fjp.config.getKeepASDoc();
        
        coercionList = null;
        ignoreList = null;
        emitStringConversions = true;

        IClassDefinition classDefinition = resolveClassDefinition(node);

        ASDocComment asDoc = (ASDocComment) node.getASDocComment();

        if (node instanceof IFunctionNode)
        {
            boolean hasDoc = false;
            Boolean override = false;
            
            if (node.isConstructor())
            {
                if (asDoc != null && keepASDoc)
                    write(changeAnnotations(asDoc.commentNoEnd()));
                else
                    begin();
                hasDoc = true;

                emitJSDocLine(JSEmitterTokens.CONSTRUCTOR);

                IClassDefinition parent = (IClassDefinition) node
                        .getDefinition().getParent();
                IClassDefinition superClass = parent.resolveBaseClass(project);
                String qname = (superClass != null) ? project.getActualPackageName(superClass.getQualifiedName()) : null;

                //support implicit bindable implementation for 'Extends' EventDispatcher:
                if (superClass == null || qname.equals(IASLanguageConstants.Object)) {
                    if (((JSFlexJSEmitter)emitter).getModel().getImplicitBindableImplementation()
                            == JSSessionModel.ImplicitBindableImplementation.EXTENDS) {
                        superClass = (IClassDefinition) project.resolveQNameToDefinition(BindableEmitter.DISPATCHER_CLASS_QNAME);
                        if (superClass == null) {
                            System.out.println(BindableEmitter.DISPATCHER_CLASS_QNAME+" not resolved for implicit super class in "+classDefinition.getQualifiedName());
                        } else qname = BindableEmitter.DISPATCHER_CLASS_QNAME;
                    }
                }

                usedNames = true;
                
                if (superClass != null
                        && !qname.equals(IASLanguageConstants.Object))
                    emitExtends(superClass, superClass.getPackageName());

                IReference[] references = classDefinition
                        .getImplementedInterfaceReferences();

                Boolean sawIEventDispatcher = false;
                Boolean needsIEventDispatcher = ((JSFlexJSEmitter)emitter).getModel().getImplicitBindableImplementation()
                                                == JSSessionModel.ImplicitBindableImplementation.IMPLEMENTS;

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
                    if (type.getQualifiedName() == BindableEmitter.DISPATCHER_INTERFACE_QNAME)
                        sawIEventDispatcher=true;
                }
                //support implicit bindable implementation for 'implements' IEventDispatcher:
                if (needsIEventDispatcher && !sawIEventDispatcher) {
                    ITypeDefinition type = (ITypeDefinition) project.resolveQNameToDefinition(BindableEmitter.DISPATCHER_INTERFACE_QNAME);
                    if (type == null) {
                        System.out.println(BindableEmitter.DISPATCHER_INTERFACE_QNAME+" not resolved for implicit implementation in "+classDefinition.getQualifiedName());
                    } else {
                        emitImplements(type, project.getActualPackageName(type.getPackageName()));
                    }
                }
                usedNames = false;
            }
            else
            {
                // @override
                override = node.hasModifier(ASModifier.OVERRIDE);

                String ns = node.getNamespace();
                if (ns != null)
                {
                    if (asDoc != null && keepASDoc)
                    {
                        String docText = asDoc.commentNoEnd();
                        String keepToken = JSFlexJSEmitterTokens.EMIT_COERCION
                                .getToken();
                        if (docText.contains(keepToken))
                            loadKeepers(docText);
                        String ignoreToken = JSFlexJSEmitterTokens.IGNORE_COERCION
                        		.getToken();
		                if (docText.contains(ignoreToken))
		                    loadIgnores(docText);
                        String noStringToken = JSFlexJSEmitterTokens.IGNORE_STRING_COERCION
                        		.getToken();
		                if (docText.contains(noStringToken))
		                    emitStringConversions = false;
                        write(changeAnnotations(asDoc.commentNoEnd()));
                    }
                    else
                        begin();
                    emitMethodAccess(node);
                    hasDoc = true;
                }
            }

            if (!override)
            {
	            // @param
	            IParameterNode[] parameters = node.getParameterNodes();
	            for (IParameterNode pnode : parameters)
	            {
	                if (!hasDoc)
	                {
	                    if (asDoc != null && keepASDoc)
	                        write(changeAnnotations(asDoc.commentNoEnd()));
	                    else
	                        begin();
	                    emitMethodAccess(node);
	                    hasDoc = true;
	                }
	
	                IExpressionNode enode = pnode.getNameExpressionNode();
	
	                ITypeDefinition tdef = enode.resolveType(project);
	                if (tdef == null)
	                    continue;
	
	                emitParam(pnode, project.getActualPackageName(tdef.getPackageName()));
	            }
            }
            
            if (!node.isConstructor())
            {
            	if (!override)
            	{
	                // @return
	                String returnType = node.getReturnType();
	                if (returnType != ""
	                        && returnType != ASEmitterTokens.VOID.getToken())
	                {
	                    if (!hasDoc)
	                    {
	                        if (asDoc != null && keepASDoc)
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
            	}
            	
                if (override)
                {
                    if (!hasDoc)
                    {
                        if (asDoc != null && keepASDoc)
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
            String ignore = doc.substring(index + ignoreToken.length());
            int endIndex = ignore.indexOf("\n");
            ignore = ignore.substring(0, endIndex);
            ignore = ignore.trim();
            ignoreList.add(ignore);
            index = doc.indexOf(ignoreToken, index + endIndex);
        }
    }
    
    private void loadKeepers(String doc)
    {
    	coercionList = new ArrayList<String>();
        String keepToken = JSFlexJSEmitterTokens.EMIT_COERCION.getToken();
        int index = doc.indexOf(keepToken);
        while (index != -1)
        {
            String keeper = doc.substring(index + keepToken.length());
            int endIndex = keeper.indexOf("\n");
            keeper = keeper.substring(0, endIndex);
            keeper = keeper.trim();
            coercionList.add(keeper);
            index = doc.indexOf(keepToken, index + endIndex);
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
    	FlexJSProject fjp =  (FlexJSProject)project;
        boolean keepASDoc = fjp.config != null && fjp.config.getKeepASDoc();
        boolean hasDoc = false;

        ASDocComment asDoc = (ASDocComment) ((IFunctionNode) node)
                .getASDocComment();

        String returnType = ((IFunctionNode) node).getReturnType();
        if (returnType != "" && returnType != ASEmitterTokens.VOID.getToken()) // has return
        {
            if (asDoc != null && keepASDoc)
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
                if (asDoc != null && keepASDoc)
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
        else if (ns != null && ns == IASKeywordConstants.PUBLIC)
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
