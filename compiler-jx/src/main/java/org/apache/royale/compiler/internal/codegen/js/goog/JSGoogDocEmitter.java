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

package org.apache.royale.compiler.internal.codegen.js.goog;

import org.apache.royale.compiler.codegen.IASGlobalFunctionConstants;
import org.apache.royale.compiler.codegen.IEmitterTokens;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.codegen.js.goog.IJSGoogDocEmitter;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.JSDocEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSharedData;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.TypedExpressionNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.IPackageNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class JSGoogDocEmitter extends JSDocEmitter implements IJSGoogDocEmitter
{

    public JSGoogDocEmitter(IJSEmitter emitter)
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

    public void emitInterfaceMemberDoc(IDefinitionNode node,
            ICompilerProject project)
    {
        // (erikdebruin) placeholder method, so we don't have to further complicate
        //               the interface structure
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
        else if (ns == IASKeywordConstants.INTERNAL)
        {
            emitInternal(node);
        }

        if (node.isConst())
            emitConst(node);

        String packageName = "";
        if (def != null)
            packageName = def.getPackageName();

        emitType(node, packageName, project);

        end();
    }

    @Override
    public void emitMethodDoc(IFunctionNode node, ICompilerProject project)
    {
        IClassDefinition classDefinition = resolveClassDefinition(node);

        if (node instanceof IFunctionNode)
        {
            boolean hasDoc = false;

            if (node.isConstructor())
            {
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
                    emitImplements(type, type.getPackageName());
                }
            }
            else
            {
                // @this
                if (containsThisReference(node))
                {
                    begin();
                    emitMethodAccess(node);
                    hasDoc = true;

                    emitThis(classDefinition, classDefinition.getPackageName());
                }
            }

            // @param
            IParameterNode[] parameters = node.getParameterNodes();
            for (IParameterNode pnode : parameters)
            {
                if (!hasDoc)
                {
                    begin();
                    emitMethodAccess(node);
                    hasDoc = true;
                }

                IExpressionNode enode = pnode.getNameExpressionNode();
                emitParam(pnode, enode.resolveType(project).getPackageName());
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
                        begin();
                        emitMethodAccess(node);
                        hasDoc = true;
                    }

                    emitReturn(node, node.getPackageName());
                }

                // @override
                Boolean override = node.hasModifier(ASModifier.OVERRIDE);
                if (override)
                {
                    if (!hasDoc)
                    {
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

    public void emitMethodAccess(IFunctionNode node)
    {
        // do nothing
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
    public void emitParam(IParameterNode node, String packageName)
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

        emitJSDocLine(JSGoogDocEmitterTokens.PARAM, paramType + postfix,
                node.getName());
    }

    @Override
    public void emitPrivate(IASNode node)
    {
        emitJSDocLine(ASEmitterTokens.PRIVATE);
    }

    @Override
    public void emitProtected(IASNode node)
    {
        emitJSDocLine(ASEmitterTokens.PROTECTED);
    }

    @Override
    public void emitInternal(IASNode node)
    {
        emitJSDocLine(JSGoogDocEmitterTokens.PACKAGE);
    }

    @Override
    public void emitPublic(IASNode node)
    {
        emitJSDocLine(JSGoogDocEmitterTokens.EXPORT);
    }

    @Override
    public void emitReturn(IFunctionNode node, String packageName)
    {
        String rtype = node.getReturnType();
        if (rtype != null)
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
        String type = ((IVariableNode) node).getVariableType();
        if (((IVariableNode) node).getVariableTypeNode() instanceof TypedExpressionNode) {
            ITypeDefinition elemenTypeDef = ((TypedExpressionNode)(((IVariableNode) node).getVariableTypeNode())).getTypeNode().resolveType(project);
            if (elemenTypeDef != null) {
                type = "Vector.<" +
                        convertASTypeToJS(elemenTypeDef.getQualifiedName(),"")
                        +">";
                packageName = "";
            }
        }
        emitJSDocLine(JSGoogDocEmitterTokens.TYPE.getToken(),
                convertASTypeToJS(type, packageName));
    }

    @Override
    public void emitType(String type, String packageName)
    {
        emitJSDocLine(JSGoogDocEmitterTokens.TYPE.getToken(),
                convertASTypeToJS(type, packageName));
    }

    public void emitTypeShort(IASNode node, String packageName, ICompilerProject project)
    {
        String type = ((IVariableNode) node).getVariableType();
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
        writeToken(JSGoogDocEmitterTokens.TYPE);
        writeBlockOpen();
        write(convertASTypeToJS(type, packageName));
        writeBlockClose();
        write(ASEmitterTokens.SPACE);
        writeToken(JSDocEmitterTokens.JSDOC_CLOSE);
    }

    //--------------------------------------------------------------------------

    public void emmitPackageHeader(IPackageNode node)
    {
        begin();
        write(ASEmitterTokens.SPACE);
        writeToken(JSGoogDocEmitterTokens.STAR);
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
        writeToken(JSGoogDocEmitterTokens.STAR);
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

    protected boolean containsThisReference(IASNode node)
    {
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            final IASNode child = node.getChild(i);
            if (child.getChildCount() > 0)
            {
                return containsThisReference(child);
            }
            else
            {
                if (SemanticUtils.isThisKeyword(child))
                    return true;
            }
        }

        return false;
    }

    protected String convertASTypeToJS(String name, String pname)
    {
    	return convertASTypeToJSType(name, pname);
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

    protected String formatQualifiedName(String name)
    {
        return name;
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
