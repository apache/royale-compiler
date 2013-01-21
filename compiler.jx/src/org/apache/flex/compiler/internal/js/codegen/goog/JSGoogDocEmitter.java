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

package org.apache.flex.compiler.internal.js.codegen.goog;

import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.references.IReference;
import org.apache.flex.compiler.internal.as.codegen.ASEmitter;
import org.apache.flex.compiler.internal.js.codegen.JSDocEmitter;
import org.apache.flex.compiler.internal.js.codegen.JSSharedData;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.semantics.SemanticUtils;
import org.apache.flex.compiler.js.codegen.IJSEmitter;
import org.apache.flex.compiler.js.codegen.goog.IJSGoogDocEmitter;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IScopedNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;

public class JSGoogDocEmitter extends JSDocEmitter implements IJSGoogDocEmitter
{
    public static final String AT = "@";
    public static final String PARAM = "param";
    public static final String STAR = "*";
    public static final String TYPE = "type";

    public JSGoogDocEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }
    
    @Override
    public void emitInterfaceDoc(IInterfaceNode node)
    {
        begin();
        
        emitJSDocLine(JSGoogEmitter.INTERFACE); 
        
        String[] inodes = node.getExtendedInterfaces();
        for (String inode : inodes)
        {
            emitJSDocLine(IASKeywordConstants.EXTENDS, inode);
        }
        
        end();
    }
    
    @Override
    public void emitFieldDoc(IVariableNode node)
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
    	
    	if (node.isConst())
    		emitConst(node);
    	
    	emitType(node, "");
    	
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
            	// TODO (erikdebruin) handle JSDOC for constructors with arguments
            	
                begin();
                hasDoc = true;
                
                emitJSDocLine(JSGoogEmitter.CONSTRUCTOR);
                
                IClassDefinition parent = (IClassDefinition) node
                        .getDefinition().getParent();
                IClassDefinition superClass = parent.resolveBaseClass(project);
                String qname = superClass.getQualifiedName();

                if (superClass != null
                        && !qname.equals(IASLanguageConstants.Object))
                    emitExtends(superClass);

                IReference[] references = classDefinition
                        .getImplementedInterfaceReferences();
                for (IReference iReference : references)
                {
                    emitImplements((ITypeDefinition) iReference.resolve(
                            project, (ASScope) classDefinition.getContainingScope(),
                            DependencyType.INHERITANCE, true));
                }

                //                IExpressionNode[] inodes = cnode.getImplementedInterfaceNodes();
                //                if (inodes.length > 0)
                //                {
                //                    for (IExpressionNode inode : inodes)
                //                    {
                //                        emitImplements(inode.resolveType(project));
                //                    }
                //                }
            }
            else
            {
                // @this
                if (containsThisReference(node))
                {
                    begin();
                    hasDoc = true;

                    emitThis(classDefinition);
                }

                // @param
                IParameterNode[] parameters = node.getParameterNodes();
                for (IParameterNode pnode : parameters)
                {
                    if (!hasDoc)
                    {
                        begin();
                        hasDoc = true;
                    }

                    emitParam(pnode);
                }

                // @return
                String returnType = node.getReturnType();
                if (returnType != "" && returnType != ASTNodeID.LiteralVoidID.getParaphrase())
                {
                    if (!hasDoc)
                    {
                        begin();
                        hasDoc = true;
                    }

                    emitReturn(node);
                }

                // @override
                Boolean override = node.hasModifier(ASModifier.OVERRIDE);
                if (override)
                {
                    if (!hasDoc)
                    {
                        begin();
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
    public void emitVarDoc(IVariableNode node, ITypeDefinition typedef)
    {
    	String packageName = "";
        if (typedef != null)
        	packageName = typedef.getPackageName();

        if (!node.isConst())
        {	
        	emitTypeShort(node, packageName);
        }
        else
        {
        	writeNewline();
        	begin();
        	emitConst(node);
        	emitType(node, packageName);
        	end();
        }
    }
    
    @Override
    public void emitConst(IVariableNode node)
    {
        emitJSDocLine(IASKeywordConstants.CONST);
    }

    @Override
    public void emitDefine(IVariableNode node)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void emitDeprecated(IASNode node)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void emitEnum(IClassNode node)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void emitExtends(IClassDefinition superDefinition)
    {
        emitJSDocLine(IASKeywordConstants.EXTENDS, superDefinition.getQualifiedName());
    }

    @Override
    public void emitImplements(ITypeDefinition definition)
    {
        emitJSDocLine(IASKeywordConstants.IMPLEMENTS, definition.getQualifiedName());
    }

    @Override
    public void emitInheritDoc(IClassNode node)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void emitLicense(IClassNode node)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void emitOverride(IFunctionNode node)
    {
        emitJSDocLine(IASKeywordConstants.OVERRIDE);
    }

    @Override
    public void emitParam(IParameterNode node)
    {
    	String postfix = (node.getDefaultValue() == null) ? "" : ASEmitter.EQUALS;
    	
    	String paramType = "";
    	if (node.isRest())
    		paramType = IASLanguageConstants.REST;
    	else
    		paramType = convertASTypeToJS(node.getVariableType());
    	
        emitJSDocLine(PARAM, paramType + postfix, node.getName());
    }

    @Override
    public void emitPrivate(IASNode node)
    {
        emitJSDocLine(IASKeywordConstants.PRIVATE);
    }

    @Override
    public void emitProtected(IASNode node)
    {
        emitJSDocLine(IASKeywordConstants.PROTECTED);
    }

    @Override
    public void emitReturn(IFunctionNode node)
    {
        String rtype = node.getReturnType();
        if (rtype != null)
        {
            emitJSDocLine(IASKeywordConstants.RETURN, convertASTypeToJS(rtype));
        }
    }

    @Override
    public void emitThis(ITypeDefinition type)
    {
        emitJSDocLine(IASKeywordConstants.THIS, type.getQualifiedName());
    }

    @Override
    public void emitType(IASNode node, String packageName)
    {
        String type = ((IVariableNode) node).getVariableType();
        emitJSDocLine(TYPE, convertASTypeToJS(type));
    }

    public void emitTypeShort(IASNode node, String packageName)
    {
        String type = ((IVariableNode) node).getVariableType(); 
        write(JSDOC_OPEN);
        writeSpace();
        write(AT);
        write(TYPE);
    	writeSpace();
        writeBlockOpen();
        if (packageName != "")
        {
        	write(packageName);
        	write(ASEmitter.PERIOD);
        }
        write(convertASTypeToJS(type));
        writeBlockClose();
        writeSpace();
        write(JSDOC_CLOSE);
        writeSpace();
    }

    @Override
    public void emitTypedef(IASNode node)
    {
        // TODO Auto-generated method stub

    }

    //--------------------------------------------------------------------------

    public void emmitPackageHeader(IPackageNode node)
    {
        begin();
        write(ASEmitter.SPACE);
        write(STAR);
        write(ASEmitter.SPACE);
        write(JSSharedData.getTimeStampString());
        end();
    }

    //--------------------------------------------------------------------------

    private void emitJSDocLine(String name)
    {
    	emitJSDocLine(name, "");
    }
    
    private void emitJSDocLine(String name, String type)
    {
    	emitJSDocLine(name, type, "");
    }
    
    private void emitJSDocLine(String name, String type, String param)
    {
        writeSpace();
        write(STAR);
        writeSpace();
        write(AT);
        write(name);
        if (type != "")
    	{
	    	writeSpace();
	        writeBlockOpen();
	        write(type);
	        writeBlockClose();
    	}
        if (param != "")
    	{
	    	writeSpace();
	        write(param);
    	}
        writeNewline();
    }
    
    private boolean containsThisReference(IASNode node)
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

    private String convertASTypeToJS(String name)
    {
    	String result = name;
    	
    	if (name.equals(""))
    		result = IASLanguageConstants.ANY_TYPE;
    	else if (name.equals(IASLanguageConstants.Boolean) || 
    			 name.equals(IASLanguageConstants.String) || 
    			 name.equals(IASLanguageConstants.Number))
    		result = result.toLowerCase();
    	else if (name.equals(IASLanguageConstants._int) || 
    			 name.equals(IASLanguageConstants.uint))
    		result = IASLanguageConstants.Number.toLowerCase();
       
    	result = result.replace(IASLanguageConstants.String, IASLanguageConstants.String.toLowerCase());
    	
        return result;
    }

    private IClassDefinition resolveClassDefinition(IFunctionNode node)
    {
        IScopedNode scope = node.getContainingScope();
        if (scope instanceof IMXMLDocumentNode)
            return ((IMXMLDocumentNode) scope).getClassDefinition();

        IClassNode cnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);
        return cnode.getDefinition();
    }
}
