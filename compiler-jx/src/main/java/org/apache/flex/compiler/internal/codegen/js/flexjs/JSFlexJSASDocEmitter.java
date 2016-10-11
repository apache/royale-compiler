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

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.flex.compiler.asdoc.flexjs.ASDocComment;
import org.apache.flex.compiler.codegen.js.flexjs.IJSFlexJSEmitter;
import org.apache.flex.compiler.codegen.js.goog.IJSGoogDocEmitter;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.constants.INamespaceConstants;
import org.apache.flex.compiler.definitions.IAccessorDefinition;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSessionModel.ImplicitBindableImplementation;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.jx.AccessorEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.AsIsEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.BinaryOperatorEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.BindableEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ClassEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.DefinePropertyFunctionEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.FieldEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ForEachEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.FunctionCallEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.IdentifierEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.InterfaceEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.LiteralEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.MemberAccessEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.MethodEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ObjectDefinePropertyEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.PackageFooterEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.PackageHeaderEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.SelfReferenceEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.SuperCallEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.VarDeclarationEmitter;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.internal.codegen.mxml.flexjs.MXMLFlexJSASDocEmitter;
import org.apache.flex.compiler.internal.codegen.mxml.flexjs.MXMLFlexJSEmitter;
import org.apache.flex.compiler.internal.definitions.AccessorDefinition;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.tree.as.BinaryOperatorAsNode;
import org.apache.flex.compiler.internal.tree.as.BlockNode;
import org.apache.flex.compiler.internal.tree.as.DynamicAccessNode;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.flex.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.flex.compiler.internal.tree.as.NumericLiteralNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IContainerNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IFunctionObjectNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.INamespaceDecorationNode;
import org.apache.flex.compiler.tree.as.INamespaceNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IScopedNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.IUnaryOperatorNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.utils.ASNodeUtils;

import com.google.common.base.Joiner;
import org.apache.flex.compiler.utils.NativeUtils;

/**
 * Concrete implementation of the 'FlexJS' JavaScript production.
 *
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class JSFlexJSASDocEmitter extends JSGoogEmitter implements IJSFlexJSEmitter
{

    @Override
    public String postProcess(String output)
    {
    	return output;
    }

    public JSFlexJSASDocEmitter(FilterWriter out)
    {
        super(out);
    }

    @Override
    protected void writeIndent()
    {
        write(JSFlexJSEmitterTokens.INDENT);
    }

    @Override
    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numIndent; i++)
            sb.append(JSFlexJSEmitterTokens.INDENT.getToken());
        return sb.toString();
    }
    
    @Override
    public void emitNamespace(INamespaceNode node)
    {
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        write("<");
        write(formatQualifiedName(node.getQualifiedName()));
        write(">");
        indentPush();
        if (asDoc != null)
        	write(asDoc.commentNoEnd());
        indentPop();
        write("</");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline(">");
    }


    @Override
    public String formatQualifiedName(String name)
    {
        return formatQualifiedName(name, false);
    }

    public MXMLFlexJSASDocEmitter mxmlEmitter = null;

    public String formatQualifiedName(String name, boolean isDoc)
    {
    	if (mxmlEmitter != null)
    		name = mxmlEmitter.formatQualifiedName(name);
        /*
        if (name.contains("goog.") || name.startsWith("Vector."))
            return name;
        name = name.replaceAll("\\.", "_");
        */
    	if (getModel().isInternalClass(name))
    		return getModel().getInternalClasses().get(name);
        if (NativeUtils.isJSNative(name)) return name;
    	if (name.startsWith("window."))
    		name = name.substring(7);
    	else if (!isDoc)
    	{
    	}
        return name;
    }

    public String convertASTypeToJS(String name)
    {
        String result = name;

        if (name.equals(""))
            result = IASLanguageConstants.Object;
        else if (name.equals(IASLanguageConstants.Class))
            result = IASLanguageConstants.Object;
        else if (name.equals(IASLanguageConstants._int)
                || name.equals(IASLanguageConstants.uint))
            result = IASLanguageConstants.Number;

        boolean isBuiltinFunction = name.matches("Vector\\.<.*>");
        if (isBuiltinFunction)
        {
        	result = IASLanguageConstants.Array;
        }
        return result;
    }

    //--------------------------------------------------------------------------
    // Package Level
    //--------------------------------------------------------------------------

    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
    	IPackageNode packageNode = definition.getNode();
    	IFileNode fileNode = (IFileNode) packageNode.getAncestorOfType(IFileNode.class);
        int nodeCount = fileNode.getChildCount();
        for (int i = 0; i < nodeCount; i++)
        {
	        IASNode pnode = fileNode.getChild(i);

	        if (pnode instanceof IPackageNode)
	        {
	        }
	        else if (pnode instanceof IClassNode)
	        {
	        	getWalker().walk(pnode);
	        }
	        else if (pnode instanceof IInterfaceNode)
	        {
	        	getWalker().walk(pnode);
	        }
            else if (pnode instanceof IFunctionNode)
            {
	        	getWalker().walk(pnode);
            }
            else if (pnode instanceof INamespaceNode)
            {
	        	getWalker().walk(pnode);
            }
            else if (pnode instanceof IVariableNode)
            {
	        	getWalker().walk(pnode);
            }
        }
    }

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
    }

    //--------------------------------------------------------------------------
    // Class
    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IClassNode node)
    {
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        write("<");
        write(formatQualifiedName(node.getQualifiedName()));
        write(">");
        indentPush();
        if (asDoc != null)
        	write(asDoc.commentNoEnd());
        final IDefinitionNode[] members = node.getAllMemberNodes();
        for (IDefinitionNode mnode : members)
        {
        	getWalker().walk(mnode);
        }
        indentPop();
        write("</");
        write(formatQualifiedName(node.getQualifiedName()));
        write(">");
    }

    @Override
    public void emitInterface(IInterfaceNode node)
    {
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        write("<");
        write(formatQualifiedName(node.getQualifiedName()));
        write(">");
        indentPush();
        if (asDoc != null)
        	write(asDoc.commentNoEnd());
        final IDefinitionNode[] members = node.getAllMemberDefinitionNodes();
        for (IDefinitionNode mnode : members)
        {
        	getWalker().walk(mnode);
        }
        indentPop();
        write("</");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline(">");
    }

    private ArrayList<String> accessors = new ArrayList<String>();
    
    @Override
    public void emitGetAccessor(IGetterNode node)
    {
    	if (node.getDefinition().isPrivate()) return;

    	String name = node.getName();
        if (accessors.contains(name)) return;
        accessors.add(name);
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc == null || asDoc.commentNoEnd().contains("@private"))
        {
        	IAccessorDefinition def = (IAccessorDefinition)node.getDefinition();
        	IAccessorDefinition otherDef = (IAccessorDefinition)def.resolveCorrespondingAccessor(getWalker().getProject());
        	if (otherDef != null)
        	{
            	IAccessorNode otherNode = (IAccessorNode)otherDef.getNode();
            	if (otherNode != null)
            		asDoc = (ASDocComment) otherNode.getASDocComment();        		
        	}
        }
        write("<");
        write(formatQualifiedName(node.getQualifiedName()));
        write(">");
        indentPush();
        if (asDoc != null)
        	write(asDoc.commentNoEnd());
        indentPop();
        write("</");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline(">");
    }

    @Override
    public void emitSetAccessor(ISetterNode node)
    {
    	if (node.getDefinition().isPrivate()) return;

    	String name = node.getName();
        if (accessors.contains(name)) return;
        accessors.add(name);
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc == null || asDoc.commentNoEnd().contains("@private"))
        {
        	IAccessorDefinition def = (IAccessorDefinition)node.getDefinition();
        	IAccessorDefinition otherDef = (IAccessorDefinition)def.resolveCorrespondingAccessor(getWalker().getProject());
        	if (otherDef != null)
        	{
            	IAccessorNode otherNode = (IAccessorNode)otherDef.getNode();
            	if (otherNode != null)
            		asDoc = (ASDocComment) otherNode.getASDocComment();        		
        	}
        }
        write("<");
        write(formatQualifiedName(node.getQualifiedName()));
        write(">");
        indentPush();
        if (asDoc != null)
        	write(asDoc.commentNoEnd());
        indentPop();
        write("</");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline(">");
    }
    
    @Override
    public void emitField(IVariableNode node)
    {
    	if (node.getDefinition().isPrivate()) return;
    	
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        write("<");
        write(formatQualifiedName(node.getQualifiedName()));
        write(">");
        indentPush();
        if (asDoc != null)
        	write(asDoc.commentNoEnd());
        indentPop();
        write("</");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline(">");
    }

    @Override
    public void emitVarDeclaration(IVariableNode node)
    {
    	if (node.getDefinition().isPrivate()) return;

    	ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        write("<");
        write(formatQualifiedName(node.getQualifiedName()));
        write(">");
        indentPush();
        if (asDoc != null)
        	write(asDoc.commentNoEnd());
        indentPop();
        write("</");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline(">");
    }

    @Override
    public void emitAccessors(IAccessorNode node)
    {
    	if (node.getDefinition().isPrivate()) return;

    	String name = node.getName();
        if (accessors.contains(name)) return;
        accessors.add(name);
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc == null || asDoc.commentNoEnd().contains("@private"))
        {
        	IAccessorDefinition def = (IAccessorDefinition)node.getDefinition();
        	IAccessorDefinition otherDef = (IAccessorDefinition)def.resolveCorrespondingAccessor(getWalker().getProject());
        	if (otherDef != null)
        	{
            	IAccessorNode otherNode = (IAccessorNode)otherDef.getNode();
            	if (otherNode != null)
            		asDoc = (ASDocComment) otherNode.getASDocComment();        		
        	}
        }
        write("<");
        write(formatQualifiedName(node.getQualifiedName()));
        write(">");
        indentPush();
        if (asDoc != null)
        	write(asDoc.commentNoEnd());
        indentPop();
        write("</");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline(">");
    }
    
    @Override
    public void emitMethod(IFunctionNode node)
    {
    	if (node.getDefinition().isPrivate()) return;

    	ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        write("<");
        write(formatQualifiedName(node.getQualifiedName()));
        write(">");
        indentPush();
        if (asDoc != null)
        	write(asDoc.commentNoEnd());
        indentPop();
        write("</");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline(">");
    }
}
