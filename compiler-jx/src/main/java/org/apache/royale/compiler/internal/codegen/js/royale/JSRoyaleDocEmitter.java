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

package org.apache.royale.compiler.internal.codegen.js.royale;

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.asdoc.royale.ASDocComment;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.BindableEmitter;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.problems.PublicVarWarningProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

public class JSRoyaleDocEmitter extends JSGoogDocEmitter
{
    private List<String> classIgnoreList;
    private List<String> ignoreList;
    private List<String> coercionList;
    public boolean emitStringConversions = true;
    private boolean emitExports = true;

    public JSRoyaleDocEmitter(IJSEmitter emitter)
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
        if (name.equals(IASLanguageConstants.Boolean.toLowerCase())
                || name.equals(IASLanguageConstants.String.toLowerCase())
                || name.equals(IASLanguageConstants.Number.toLowerCase()))
            return name;

        return formatQualifiedName(name);
    }

    private boolean usedNames = false;
    
    @Override
    protected String formatQualifiedName(String name)
    {
    	return ((JSRoyaleEmitter)emitter).formatQualifiedName(name, !usedNames);
    }

    @Override
    public void emitMethodDoc(IFunctionNode node, ICompilerProject project)
    {
    	RoyaleJSProject fjp = (RoyaleJSProject)project;
        boolean keepASDoc = fjp.config != null && fjp.config.getKeepASDoc();
        if (fjp.config != null)
        	emitExports = fjp.config.getExportPublicSymbols();
        
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
                    if (((JSRoyaleEmitter)emitter).getModel().getImplicitBindableImplementation()
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
                Boolean needsIEventDispatcher = ((JSRoyaleEmitter)emitter).getModel().getImplicitBindableImplementation()
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
                        String keepToken = JSRoyaleEmitterTokens.EMIT_COERCION
                                .getToken();
                        if (docText.contains(keepToken))
                            loadKeepers(docText);
                        String ignoreToken = JSRoyaleEmitterTokens.IGNORE_COERCION
                        		.getToken();
		                if (docText.contains(ignoreToken))
		                    loadIgnores(docText);
                        String noStringToken = JSRoyaleEmitterTokens.IGNORE_STRING_COERCION
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
        String ignoreToken = JSRoyaleEmitterTokens.IGNORE_COERCION.getToken();
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
        String keepToken = JSRoyaleEmitterTokens.EMIT_COERCION.getToken();
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
    	RoyaleJSProject fjp =  (RoyaleJSProject)project;
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
        	RoyaleJSProject fjp =  (RoyaleJSProject)project;
            boolean warnPublicVars = fjp.config != null && fjp.config.getWarnPublicVars();
            IMetaTagsNode meta = node.getMetaTags();
            boolean bindable = false;
            if (meta != null)
            {
            	IMetaTagNode tag = meta.getTagByName("Bindable");
            	if (tag != null)
            		bindable = true;
            }
            if (warnPublicVars && !node.isConst() && !bindable && ns.contentEquals("public"))
            {
                if (!suppressedWarning(node, fjp))
                	fjp.getProblems().add(new PublicVarWarningProblem(node));
            }
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

    @Override
    public void emitPublic(IASNode node)
    {
    	if (emitExports)
    		super.emitPublic(node);
    }
    
    private boolean suppressedWarning(IVariableNode node, RoyaleJSProject fjp)
    {
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        boolean keepASDoc = fjp.config != null && fjp.config.getKeepASDoc();
        String suppressToken = JSRoyaleEmitterTokens.SUPPRESS_PUBLIC_VAR_WARNING
        .getToken();
        if (asDoc != null && keepASDoc)
        {
            String docText = asDoc.commentNoEnd();
            if (docText.contains(suppressToken))
                return true;
        }
    	IASNode classNode = node.getParent().getParent();
    	if (classNode == null)
    		return false;
    	if (classNode.getNodeID() == ASTNodeID.ClassID)
    	{
    		asDoc = (ASDocComment) ((IClassNode)classNode).getASDocComment();
            if (asDoc != null && keepASDoc)
            {
                String docText = asDoc.commentNoEnd();
                if (docText.contains(suppressToken))
                    return true;
            }
            IClassDefinition cdef = ((IClassNode)classNode).getDefinition();
            if (cdef.isBindable())
            	return true;
            if (!cdef.isPublic())
            	return true;
    	}
    	return false;
    }

}
