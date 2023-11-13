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

import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.royale.compiler.asdoc.royale.ASDocComment;
import org.apache.royale.compiler.codegen.IASGlobalFunctionConstants;
import org.apache.royale.compiler.codegen.IEmitterTokens;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.codegen.js.royale.IJSRoyaleDocEmitter;
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
import org.apache.royale.compiler.internal.codegen.js.JSDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.JSDocEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel;
import org.apache.royale.compiler.internal.codegen.js.JSSharedData;
import org.apache.royale.compiler.internal.codegen.js.jx.BindableEmitter;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.TypedExpressionNode;
import org.apache.royale.compiler.problems.PublicVarWarningProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.royale.compiler.utils.ASNodeUtils;

public class JSRoyaleDocEmitter extends JSDocEmitter implements IJSRoyaleDocEmitter
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
        	return convertASTypeToJSType(name, pname);
        }
        
        name = convertASTypeToJSType(name, pname);
        if (name.equals(IASLanguageConstants.Boolean.toLowerCase())
                || name.equals(IASLanguageConstants.String.toLowerCase())
                || name.equals(IASLanguageConstants.Number.toLowerCase()))
            return name;

        return formatQualifiedName(name);
    }
    
    public static String convertASTypeToJSType(String name, String pname)
    {
        String result = "";

        if (name.equals(""))
            result = ASEmitterTokens.ANY_TYPE.getToken();
        else if (name.equals(IASLanguageConstants.Class))
            result = IASLanguageConstants.Object;
        else if (name.equals(IASLanguageConstants.Boolean)
                || name.equals(IASLanguageConstants.String)
                || name.equals(IASLanguageConstants.Number))
            result = name.toLowerCase();
        else if (name.equals(IASLanguageConstants._int)
                || name.equals(IASLanguageConstants.uint))
            result = IASLanguageConstants.Number.toLowerCase();

        boolean isBuiltinFunction = name.matches("Vector\\.<.*>");
        if (isBuiltinFunction)
        {
        	// is a vector so convert the element type
        	String elementType = name.substring(8, name.length() - 1);
        	elementType = convertASTypeToJSType(elementType, pname);
        	name = "Array.<" + elementType + ">";
        }
        else {
            IASGlobalFunctionConstants.BuiltinType[] builtinTypes = IASGlobalFunctionConstants.BuiltinType
                    .values();
            for (IASGlobalFunctionConstants.BuiltinType builtinType : builtinTypes)
            {
                if (name.equalsIgnoreCase(builtinType.getName()))
                {
                    isBuiltinFunction = true;
                    break;
                }
            }
        }

        if (result == "")
            result = (pname != "" && !isBuiltinFunction && name.indexOf(".") < 0) ? pname
                    + ASEmitterTokens.MEMBER_ACCESS.getToken() + name
                    : name;

        return result;
    }

    private boolean usedNames = false;
    
    protected String formatQualifiedName(String name)
    {
    	return ((JSRoyaleEmitter)emitter).formatQualifiedName(name, !usedNames);
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
                emitJSDocLine(ASEmitterTokens.EXTENDS,
                        formatQualifiedName(dnode.getQualifiedName()));
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

                if (ASNodeUtils.hasExportSuppressed(node)) {
                    emitExports = false;
                    if (IASKeywordConstants.PUBLIC.equals(ns)) // suppress it for reflection data checks:
                        ((JSRoyaleEmitter) (emitter)).getModel().suppressedExportNodes.add(node);
                }

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
	
	                emitParam(pnode, project.getActualPackageName(tdef.getPackageName()), project);
	            }
            }
            
            if (!node.isConstructor())
            {
            	if (!override)
            	{
	                // @return
	                String returnType = node.getReturnType();
                    if (project.getInferTypes() && (returnType == null || returnType.isEmpty()))
                    {
                        ITypeDefinition resolvedTypeDef = SemanticUtils.resolveFunctionInferredReturnType(node, project);
                        if (resolvedTypeDef != null)
                        {
                            returnType = resolvedTypeDef.getQualifiedName();
                        }
                        else
                        {
                            returnType = "*";
                        }
                    }
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
	
	                    emitReturn(node, project.getActualPackageName(packageName), project);
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

            emitReturn((IFunctionNode) node, tdef.getPackageName(), project);
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
            emitParam(pnode, enode.resolveType(project).getPackageName(), project);
        }

        if (hasDoc)
            end();
    }

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

    @Override
    public void emitVarDoc(IVariableNode node, IDefinition def, ICompilerProject project)
    {
        String packageName = "";
        if (def != null)
            packageName = def.getPackageName();

        if (!node.isConst())
        {
            IDefinition ndef = node.getDefinition();
            if (emitter != null && emitter instanceof JSRoyaleEmitter)
            {
                ITypeDefinition type = ndef.resolveType(project);
                if (type != null)
                {
                    packageName = ((ITypeDefinition) type).getPackageName();
                }
            }
            emitTypeShort(node, project.getActualPackageName(packageName), project);
        }
        else
        {
            writeNewline();
            begin();
            emitConst(node);
            emitType(node, project.getActualPackageName(packageName), project);
            end();
        }
    }

    @Override
    public void emitConst(IVariableNode node)
    {
        emitJSDocLine(ASEmitterTokens.CONST);
    }

    @Override
    public void emitExtends(IClassDefinition superDefinition, String packageName)
    {
        emitJSDocLine(ASEmitterTokens.EXTENDS,
                formatQualifiedName(superDefinition.getQualifiedName()));
    }

    @Override
    public void emitImplements(ITypeDefinition definition, String packageName)
    {
        emitJSDocLine(ASEmitterTokens.IMPLEMENTS,
                formatQualifiedName(definition.getQualifiedName()));
    }

    @Override
    public void emitOverride(IFunctionNode node)
    {
        emitJSDocLine(ASEmitterTokens.OVERRIDE);
    }

    @Override
    public void emitParam(IParameterNode node, String packageName, ICompilerProject project)
    {
        String postfix = (node.getDefaultValue() == null) ? ""
                : ASEmitterTokens.EQUAL.getToken();

        String paramType = "";
        if (node.isRest())
        {
            paramType = ASEmitterTokens.ELLIPSIS.getToken();
        }
        else
        {
            String typeName = node.getVariableType();
            if (project.getInferTypes() && typeName.isEmpty())
            {
                ITypeDefinition resolvedTypeDef = SemanticUtils.resolveVariableInferredType(node, project);
                if (resolvedTypeDef != null)
                {
                    typeName = resolvedTypeDef.getQualifiedName();
                }
            }
            if (packageName.length() > 0 && typeName.indexOf(packageName) > -1)
            {
                String[] parts = typeName.split("\\.");
                if (parts.length > 0)
                {
                    typeName = parts[parts.length - 1];
                }
            }
            paramType = convertASTypeToJS(typeName, packageName);
        }

        emitJSDocLine(JSRoyaleDocEmitterTokens.PARAM, paramType + postfix,
                node.getName());
    }

    @Override
    public void emitReturn(IFunctionNode node, String packageName, ICompilerProject project)
    {
        String rtype = node.getReturnType();
        if (project.getInferTypes() && (rtype == null || rtype.isEmpty()))
        {
            ITypeDefinition resolvedTypeDef = SemanticUtils.resolveFunctionInferredReturnType(node, project);
            if (resolvedTypeDef != null)
            {
                rtype = resolvedTypeDef.getQualifiedName();
            }
            else
            {
                rtype = "*";
            }
        }
        if (rtype != null && rtype != ASEmitterTokens.VOID.getToken())
        {
            emitJSDocLine(ASEmitterTokens.RETURN,
                    convertASTypeToJS(rtype, packageName));
        }
    }

    @Override
    public void emitThis(ITypeDefinition type, String packageName)
    {
        emitJSDocLine(ASEmitterTokens.THIS.getToken(), type.getQualifiedName());
    }

    @Override
    public void emitType(IASNode node, String packageName, ICompilerProject project)
    {
        IVariableNode varNode = (IVariableNode) node;
        String type = varNode.getVariableType();
        if (project.getInferTypes() && type.isEmpty())
        {
            ITypeDefinition resolvedTypeDef = SemanticUtils.resolveVariableInferredType(varNode, project);
            if (resolvedTypeDef != null)
            {
                type = resolvedTypeDef.getQualifiedName();
            }
        }
        if (varNode.getVariableTypeNode() instanceof TypedExpressionNode) {
            ITypeDefinition elemenTypeDef = ((TypedExpressionNode)(varNode.getVariableTypeNode())).getTypeNode().resolveType(project);
            if (elemenTypeDef != null) {
                type = "Vector.<" +
                        convertASTypeToJS(elemenTypeDef.getQualifiedName(),"")
                        +">";
                packageName = "";
            }
        }
        emitJSDocLine(JSRoyaleDocEmitterTokens.TYPE.getToken(),
                convertASTypeToJS(type, packageName));
    }

    @Override
    public void emitType(String type, String packageName)
    {
        emitJSDocLine(JSRoyaleDocEmitterTokens.TYPE.getToken(),
                convertASTypeToJS(type, packageName));
    }

    public void emitTypeShort(IVariableNode node, String packageName, ICompilerProject project)
    {
        String type = node.getVariableType();
        if (project.getInferTypes() && type.isEmpty())
        {
            ITypeDefinition resolvedTypeDef = SemanticUtils.resolveVariableInferredType(node, project);
            if (resolvedTypeDef != null)
            {
                type = resolvedTypeDef.getQualifiedName();
            }
        }
        if (((IVariableNode) node).getVariableTypeNode() instanceof TypedExpressionNode) {
            ITypeDefinition elemenTypeDef = ((TypedExpressionNode)(((IVariableNode) node).getVariableTypeNode())).getTypeNode().resolveType(project);
            if (elemenTypeDef != null) {
                type = "Vector.<" +
                        convertASTypeToJS(elemenTypeDef.getQualifiedName(),"")
                        +">";
                packageName = "";
            }
        }
        writeToken(JSDocEmitterTokens.JSDOC_OPEN);
        write(ASEmitterTokens.ATSIGN);
        writeToken(JSRoyaleDocEmitterTokens.TYPE);
        writeBlockOpen();
        write(convertASTypeToJS(type, packageName));
        writeBlockClose();
        write(ASEmitterTokens.SPACE);
        writeToken(JSDocEmitterTokens.JSDOC_CLOSE);
    }

    private JSSessionModel getModel() {
        if (emitter instanceof IJSEmitter) {
            return ((IJSEmitter) emitter).getModel();
        }
        return null;
    }

    protected void emitNoCollapse(IDefinitionNode node)
    {
        if (!node.hasModifier(ASModifier.STATIC)
                || node instanceof IAccessorNode
                || IASKeywordConstants.PRIVATE.equals(node.getNamespace()))
        {
            return;
        }
        if (getModel() != null) {
            if (getModel().suppressExports || getModel().suppressedExportNodes.contains(node)) return;
        }
        //dynamically getting/setting a static field won't
        //work properly if it is collapsed in a release build,
        //even when it has been exported
        emitJSDocLine(JSRoyaleDocEmitterTokens.NOCOLLAPSE);
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
            boolean warnPublicVars = fjp.config != null
                    && fjp.config.getWarnPublicVars()
                    && !fjp.config.getPreventRenamePublicSymbols()
                    && !fjp.config.getMxmlReflectObjectProperty();
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
                {
                	fjp.getProblems().add(new PublicVarWarningProblem(node.getSourcePath(),
                            node.getStart(), node.getEnd(),
                            warningNode.getLine(), warningNode.getColumn(),
                            node.getEndLine(), node.getEndColumn()));
                }
            }
            boolean avoidExport = ASNodeUtils.hasExportSuppressed(node);
            
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
    public void emitPrivate(IASNode node)
    {
        emitJSDocLine(ASEmitterTokens.PRIVATE);
    }

    @Override
    public void emitProtected(IASNode node)
    {
    	if (exportProtected)
    		emitPublic(node);
    	else
            emitJSDocLine(ASEmitterTokens.PROTECTED);
    }

    @Override
    public void emitInternal(IASNode node)
    {
    	if (exportInternal)
    		emitPublic(node);
    	else
            emitJSDocLine(JSRoyaleDocEmitterTokens.PACKAGE);
    }
    
    @Override
    public void emitPublic(IASNode node)
    {
    	if (emitExports)
            emitJSDocLine(JSRoyaleDocEmitterTokens.EXPORT);
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

    //--------------------------------------------------------------------------

    public void emitPackageHeader(IPackageNode node)
    {
        begin();
        write(ASEmitterTokens.SPACE);
        writeToken(JSDocEmitterTokens.JSDOC_LINE_START);
        write(getTimeStampString());
        end();
    }

    //--------------------------------------------------------------------------

    protected void emitJSDocLine(IEmitterTokens name)
    {
        emitJSDocLine(name.getToken(), "");
    }

    private void emitJSDocLine(String name)
    {
        emitJSDocLine(name, "");
    }

    protected void emitJSDocLine(IEmitterTokens name, String type)
    {
        emitJSDocLine(name.getToken(), type, "");
    }

    private void emitJSDocLine(String name, String type)
    {
        emitJSDocLine(name, type, "");
    }

    private void emitJSDocLine(IEmitterTokens name, String type, String param)
    {
        emitJSDocLine(name.getToken(), type, param);
    }

    private void emitJSDocLine(String name, String type, String param)
    {
        write(ASEmitterTokens.SPACE);
        writeToken(JSDocEmitterTokens.JSDOC_LINE_START);
        write(ASEmitterTokens.ATSIGN);
        write(name);
        if (type != "")
        {
            write(ASEmitterTokens.SPACE);
            writeBlockOpen();
            write(type);
            writeBlockClose();
        }
        if (param != "")
        {
            write(ASEmitterTokens.SPACE);
            write(param);
        }
        writeNewline();
    }

    protected IClassDefinition resolveClassDefinition(IFunctionNode node)
    {
        IScopedNode scope = node.getContainingScope();
        if (scope instanceof IMXMLDocumentNode)
            return ((IMXMLDocumentNode) scope).getClassDefinition();

        IClassNode cnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);

        if (cnode == null)
            return null;

        return cnode.getDefinition();
    }

    public static String now() {
        final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(cal.getTime());
    }

    public static String getTimeStampString() {
        if (JSSharedData.OUTPUT_TIMESTAMPS) {
            return "CROSS-COMPILED BY " + JSSharedData.COMPILER_NAME + " ("
                    + JSSharedData.COMPILER_VERSION + ") ON "
                    + now() + "\n";
        } else {
            return "CROSS-COMPILED BY " + JSSharedData.COMPILER_NAME + "\n";
        }
    }

}
