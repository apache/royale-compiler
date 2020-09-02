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

import java.util.*;

import org.apache.royale.compiler.asdoc.royale.ASDocComment;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition.FunctionClassification;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition.VariableClassification;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogDocEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.jx.BindableEmitter;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.problems.PublicVarWarningProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

public class JSRoyaleDocEmitter extends JSGoogDocEmitter
{
    private List<String> classIgnoreList;
    private List<String> ignoreList;
    private List<String> coercionList;
    private Map<String,List<String>> localSettings;
    public boolean emitStringConversions = true;
    private boolean emitExports = true;
    private boolean exportProtected = false;
    private boolean exportInternal = false;
    
    private boolean suppressClosure = false;

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
    
    public Boolean getSuppressClosure() {
        return suppressClosure;
    }
    
    public Boolean getEmitExports() {
        return emitExports;
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
        {
        	RoyaleJSProject fjp = (RoyaleJSProject)((IASEmitter)emitter).getWalker().getProject();
        	String vectorClassName = fjp.config == null ? null : fjp.config.getJsVectorEmulationClass();
        	if (vectorClassName != null) return vectorClassName;
        	return super.convertASTypeToJS(name, pname);
        }
        
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

        //exporting methods is handled dynamically in ClosureUtils
        emitExports = false;
        exportProtected = false;
        exportInternal = false;
        
        coercionList = null;
        ignoreList = null;
        localSettings = null;
        emitStringConversions = true;
        suppressClosure = false;

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
    
                        String noImplicitComplexCoercion = JSRoyaleEmitterTokens.SUPPRESS_COMPLEX_IMPLICIT_COERCION
                                .getToken();
                        if (docText.contains(noImplicitComplexCoercion))
                            loadLocalSettings(docText, noImplicitComplexCoercion, "true");
		                
                        String noResolveUncertain = JSRoyaleEmitterTokens.SUPPRESS_RESOLVE_UNCERTAIN
                                .getToken();
                        if (docText.contains(noResolveUncertain))
                            loadLocalSettings(docText,noResolveUncertain, "true");
		                
                        String suppressVectorIndexCheck = JSRoyaleEmitterTokens.SUPPRESS_VECTOR_INDEX_CHECK
                                .getToken();
                        if (docText.contains(suppressVectorIndexCheck))
                            loadLocalSettings(docText,suppressVectorIndexCheck, "true");
                        
                        String suppressClosureToken = JSRoyaleEmitterTokens.SUPPRESS_CLOSURE.getToken();
    
                        if (docText.contains(suppressClosureToken))
                            suppressClosure = true;
                        
                        String suppressExport = JSRoyaleEmitterTokens.SUPPRESS_EXPORT.getToken();
                        if (docText.contains(suppressExport)) {
                            emitExports = false;
                            if (IASKeywordConstants.PUBLIC.equals(ns)) // suppress it for reflection data checks:
                                ((JSRoyaleEmitter) (emitter)).getModel().suppressedExportNodes.add(node);
                        }
                        
                        write(changeAnnotations(asDoc.commentNoEnd()));
                    }
                    else
                        begin();
                    emitMethodAccess(node);
                    emitMethodNoCollapse(node, fjp);
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
                        emitMethodNoCollapse(node, fjp);
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
                            emitMethodNoCollapse(node, fjp);
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
                        emitMethodNoCollapse(node, fjp);
                        hasDoc = true;
                    }

                    emitOverride(node);
                }
            }

            if (hasDoc)
                end();
        }
    }
    
    private void loadLocalSettings(String doc, String settingToken, String defaultSetting)
    {
        if (localSettings == null) localSettings = new HashMap<String, List<String>>();
        int index = doc.indexOf(settingToken);
        List<String> settings = localSettings.containsKey(settingToken) ? localSettings.get(settingToken) : null;
        while (index != -1)
        {
            String setting = doc.substring(index + settingToken.length());
            int endIndex = setting.indexOf("\n");
            setting = setting.substring(0, endIndex);
            setting = setting.trim();
            if (settings == null) {
                settings = new ArrayList<String>();
                localSettings.put(settingToken, settings);
            }
            List<String> settingItems = null;
            if (setting.length() >0) {
                settingItems = Arrays.asList(setting.split("\\s*(,\\s*)+"));
            } else {
                settingItems =  Arrays.asList(defaultSetting);
            }
            for (String settingItem: settingItems) {
                if (settings.contains(settingItem)) {
                    //change the order to reflect the latest addition
                    settings.remove(settingItem);
                }
                settings.add(settingItem);
                //System.out.println("---Adding setting "+settingToken+":"+settingItem);
            }
            index = doc.indexOf(settingToken, index +  settingToken.length());
        }
    }
    
    public boolean hasLocalSetting(String settingToken) {
        if (localSettings == null) return false;
        return (localSettings.keySet().contains(settingToken));
    }
    
    public boolean getLocalSettingAsBoolean(JSRoyaleEmitterTokens token, Boolean defaultValue) {
        return getLocalSettingAsBoolean(token.getToken(), defaultValue);
    }
    
    public boolean getLocalSettingAsBoolean(String settingToken, Boolean defaultValue) {
        boolean setting = defaultValue;
        if (hasLocalSetting(settingToken)) {
            for (String stringVal: localSettings.get(settingToken)) {
                //don't bail out after finding a boolean-ish string val
                //'last one wins'
                if (stringVal.equals("false")) setting = false;
                else if (stringVal.equals("true")) setting = true;
            }
        }
        return setting;
    }
    
    public boolean getLocalSettingIncludesString(JSRoyaleEmitterTokens token, String searchValue) {
        return getLocalSettingIncludesString(token.getToken(), searchValue);
    }
    
    public boolean getLocalSettingIncludesString(String settingToken, String searchValue) {
        boolean hasValue = false;
        if (hasLocalSetting(settingToken)) {
            for (String stringVal: localSettings.get(settingToken)) {
                if (stringVal.equals(searchValue)) {
                    hasValue = true;
                    break;
                }
            }
        }
        return hasValue;
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
    
    public boolean hasIgnore(String qName) {
        return ignoreList !=null && qName != null && ignoreList.contains(qName);
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
        else if (ns == IASKeywordConstants.INTERNAL)
        {
            emitInternal(node);
        }
        else // public or custom namespace
        {
            emitPublic(node);
        }
    }

    protected void emitMethodNoCollapse(IFunctionNode node, RoyaleJSProject fjp)
    {
        String ns = node.getNamespace();
        if (ns == IASKeywordConstants.PROTECTED)
        {
            boolean preventRenameProtected = fjp.config != null && fjp.config.getPreventRenameProtectedSymbols();
            if (preventRenameProtected)
            {
                emitNoCollapse(node);
            }
        }
        else if (ns == IASKeywordConstants.INTERNAL)
        {
            boolean preventRenameInternal = fjp.config != null && fjp.config.getPreventRenameInternalSymbols();
            if (preventRenameInternal)
            {
                emitNoCollapse(node);
            }
        }
        else if(ns != IASKeywordConstants.PRIVATE) // public or custom namespace
        {
            boolean preventRenamePublic = fjp.config != null && fjp.config.getPreventRenamePublicSymbols();
            if (preventRenamePublic)
            {
                emitNoCollapse(node);
            }
        }
    }

    protected void emitNoCollapse(IDefinitionNode node)
    {
        if (!node.hasModifier(ASModifier.STATIC)
                || node instanceof IAccessorNode
                || IASKeywordConstants.PRIVATE.equals(node.getNamespace()))
        {
            return;
        }
        //dynamically getting/setting a static field won't
        //work properly if it is collapsed in a release build,
        //even when it has been exported
        emitJSDocLine(JSGoogDocEmitterTokens.NOCOLLAPSE);
    }

    @Override
    public void emitFieldDoc(IVariableNode node, IDefinition def, ICompilerProject project)
    {
        RoyaleJSProject fjp =  (RoyaleJSProject)project;

        //exporting fields is handled dynamically in ClosureUtils
        emitExports = false;
        exportProtected = false;
        exportInternal = false;

        begin();

        String ns = node.getNamespace();
        if (ns == IASKeywordConstants.PRIVATE)
        {
            emitPrivate(node);
        }
        else if (ns == IASKeywordConstants.PROTECTED)
        {
            emitProtected(node);
            boolean preventRename = fjp.config != null && fjp.config.getPreventRenameProtectedSymbols();
            if (preventRename)
            {
                emitNoCollapse(node);
            }
        }
        else if (ns == IASKeywordConstants.INTERNAL)
        {
            emitInternal(node);
            boolean preventRename = fjp.config != null && fjp.config.getPreventRenameInternalSymbols();
            if (preventRename)
            {
                emitNoCollapse(node);
            }
        }
        else
        {
            boolean warnPublicVars = fjp.config != null && fjp.config.getWarnPublicVars() && !fjp.config.getPreventRenamePublicSymbols();
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
                IASNode warningNode = node;
                //find "public" child node, which may not be the start of the IVariableNode node because of associated metadata
                int childCount = node.getChildCount();
                int index = 0;
                while (index < childCount) {
                    IASNode child = node.getChild(index);
                    if (child instanceof IIdentifierNode && ((IIdentifierNode) child).getName().equals("public")) {
                        warningNode = child;
                        break;
                    }
                    index++;
                }
                
                if (!suppressedWarning(node, fjp))
                	fjp.getProblems().add(new PublicVarWarningProblem(node.getSourcePath(),
                            node.getStart(), node.getEnd(),
                            warningNode.getLine(), warningNode.getColumn(),
                            node.getEndLine(), node.getEndColumn()));
               

            }
            boolean avoidExport = (node.getASDocComment() instanceof ASDocComment
                    && ((ASDocComment)node.getASDocComment()).commentNoEnd()
                    .contains(JSRoyaleEmitterTokens.SUPPRESS_EXPORT.getToken()));
            
            if (!avoidExport) {
                if (ns.equals(IASKeywordConstants.PUBLIC))
                {
                    emitPublic(node);
                    boolean preventRename = fjp.config != null && fjp.config.getPreventRenamePublicSymbols();
                    if(preventRename)
                    {
                        emitNoCollapse(node);
                    }
                }
            } else {
                //we should also remove it from reflection data... provide a check here for that.
                ((JSRoyaleEmitter)emitter).getModel().suppressedExportNodes.add(node);
            }
        }

        if (node.isConst())
            emitConst(node);

        String packageName = "";
        if (def != null)
            packageName = def.getPackageName();

        emitType(node, project.getActualPackageName(packageName), project);

        end();
    }

    @Override
    public void emitProtected(IASNode node)
    {
    	if (exportProtected)
    		super.emitPublic(node);
    	else
    		super.emitProtected(node);
    }

    @Override
    public void emitInternal(IASNode node)
    {
    	if (exportInternal)
    		super.emitPublic(node);
    	else
    		super.emitInternal(node);
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
