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

package org.apache.flex.compiler.internal.codegen.js.jx;

import org.apache.flex.compiler.codegen.ISubEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.definitions.AccessorDefinition;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.GetterNode;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.flex.compiler.internal.tree.as.NamespaceAccessExpressionNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IOperatorNode.OperatorType;
import org.apache.flex.compiler.utils.ASNodeUtils;

public class MemberAccessEmitter extends JSSubEmitter implements
        ISubEmitter<IMemberAccessExpressionNode>
{

    public MemberAccessEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IMemberAccessExpressionNode node)
    {
        if (ASNodeUtils.hasParenOpen(node))
            write(ASEmitterTokens.PAREN_OPEN);

        IASNode leftNode = node.getLeftOperandNode();
        IASNode rightNode = node.getRightOperandNode();

    	JSFlexJSEmitter fjs = (JSFlexJSEmitter)getEmitter();
        IDefinition def = node.resolve(getProject());
        if (def == null)
        {
        	IASNode parentNode = node.getParent();
        	// could be XML
        	boolean isXML = false;
        	boolean isProxy = false;
        	if (leftNode instanceof MemberAccessExpressionNode)
        		isXML = fjs.isXMLList((MemberAccessExpressionNode)leftNode);
        	else if (leftNode instanceof IExpressionNode)
        		isXML = fjs.isXML((IExpressionNode)leftNode);
        	if (leftNode instanceof MemberAccessExpressionNode)
        		isProxy = fjs.isProxy((MemberAccessExpressionNode)leftNode);
        	else if (leftNode instanceof IExpressionNode)
        		isProxy = fjs.isProxy((IExpressionNode)leftNode);
        	if (isXML)
        	{
        		boolean descendant = (node.getOperator() == OperatorType.DESCENDANT_ACCESS);
        		boolean child = (node.getOperator() == OperatorType.MEMBER_ACCESS) && 
        							(!(parentNode instanceof FunctionCallNode)) &&
        							rightNode.getNodeID() != ASTNodeID.Op_AtID;
        		if (descendant || child)
	        	{
	        		writeLeftSide(node, leftNode, rightNode);
	        		if (descendant)
	        			write(".descendants('");
	        		if (child)
	        			write(".child('");	        			
	        		String s = fjs.stringifyNode(rightNode);
	        		int dot = s.indexOf('.');
	        		if (dot != -1)
	        		{
	        			String name = s.substring(0, dot);
	        			String afterDot = s.substring(dot);
	        			write(name);
	        			write("')");
	        			write(afterDot);
	        		}
	        		else
	        		{
	        			write(s);
	        			write("')");
	        		}
	        		return;
	        	}
        	}
        	else if (isProxy)
        	{
        		boolean child = (node.getOperator() == OperatorType.MEMBER_ACCESS) && 
        							(!(parentNode instanceof FunctionCallNode)) &&
        							rightNode.getNodeID() != ASTNodeID.Op_AtID;
        		if (child)
	        	{
	        		writeLeftSide(node, leftNode, rightNode);
	        		if (child)
	        			write(".getProperty('");
	        		String s = fjs.stringifyNode(rightNode);
	        		int dot = s.indexOf('.');
	        		if (dot != -1)
	        		{
	        			String name = s.substring(0, dot);
	        			String afterDot = s.substring(dot);
	        			write(name);
	        			write("')");
	        			write(afterDot);
	        		}
	        		else
	        		{
	        			write(s);
	        			write("')");
	        		}
	        		return;
	        	}
        	}
        	else if (rightNode instanceof NamespaceAccessExpressionNode)
        	{
        		// if you define a local variable with the same URI as a
        		// namespace that defines a namespaced property
        		// it doesn't resolve above so we handle it here
        		NamespaceAccessExpressionNode naen = (NamespaceAccessExpressionNode)rightNode;
        		IDefinition d = naen.getLeftOperandNode().resolve(getProject());
        		IdentifierNode r = (IdentifierNode)(naen.getRightOperandNode());
        		// output bracket access with QName
        		writeLeftSide(node, leftNode, rightNode);
        		write(ASEmitterTokens.SQUARE_OPEN);
        		write(ASEmitterTokens.NEW);
        		write(ASEmitterTokens.SPACE);
        		write(IASLanguageConstants.QName);
        		write(ASEmitterTokens.PAREN_OPEN);
        		write(d.getBaseName());
        		write(ASEmitterTokens.COMMA);
        		write(ASEmitterTokens.SPACE);
        		write(ASEmitterTokens.SINGLE_QUOTE);
        		write(r.getName());
        		write(ASEmitterTokens.SINGLE_QUOTE);
        		write(ASEmitterTokens.PAREN_CLOSE);
        		write(ASEmitterTokens.SQUARE_CLOSE);
        		return;
        	}
        }
        else if (fjs.isDateProperty(node))
        {
    		writeLeftSide(node, leftNode, rightNode);
            write(".get");
            String rightName = ((IIdentifierNode)rightNode).getName();
            String firstChar = rightName.substring(0, 1);
            firstChar = firstChar.toUpperCase();
            rightName = rightName.substring(1);
            write(firstChar);
            write(rightName);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.PAREN_CLOSE);
    		return;
        }
        else if (def.getParent() != null &&
        		def.getParent().getQualifiedName().equals("Array"))
        {
        	if (def.getBaseName().equals("removeAt"))
        	{
        		writeLeftSide(node, leftNode, rightNode);
        		write(".splice");
        		return;
        	}
        	else if (def.getBaseName().equals("insertAt"))
        	{
        		writeLeftSide(node, leftNode, rightNode);
        		write(".splice");
        		return;
        	}
        }
    	else if (rightNode instanceof NamespaceAccessExpressionNode)
    	{
    		NamespaceAccessExpressionNode naen = (NamespaceAccessExpressionNode)rightNode;
    		IDefinition d = naen.getLeftOperandNode().resolve(getProject());
    		IdentifierNode r = (IdentifierNode)(naen.getRightOperandNode());
    		// output bracket access with QName
    		writeLeftSide(node, leftNode, rightNode);
    		write(ASEmitterTokens.SQUARE_OPEN);
    		write(ASEmitterTokens.NEW);
    		write(ASEmitterTokens.SPACE);
    		write(IASLanguageConstants.QName);
    		write(ASEmitterTokens.PAREN_OPEN);
    		write(fjs.formatQualifiedName(d.getBaseName()));
    		write(ASEmitterTokens.COMMA);
    		write(ASEmitterTokens.SPACE);
    		write(ASEmitterTokens.SINGLE_QUOTE);
    		write(r.getName());
    		write(ASEmitterTokens.SINGLE_QUOTE);
    		write(ASEmitterTokens.PAREN_CLOSE);
    		write(ASEmitterTokens.SQUARE_CLOSE);
    		return;
    	}
        boolean isStatic = false;
        if (def != null && def.isStatic())
            isStatic = true;
        boolean needClosure = false;
        if (def instanceof FunctionDefinition && (!(def instanceof AccessorDefinition))
        		&& !def.getBaseName().equals("constructor")) // don't wrap references to obj.constructor
        {
        	IASNode parentNode = node.getParent();
        	if (parentNode != null)
        	{
				ASTNodeID parentNodeId = parentNode.getNodeID();
				// we need a closure if this MAE is the top-level in a chain
				// of MAE and not in a function call.
				needClosure = !isStatic && parentNodeId != ASTNodeID.FunctionCallID &&
							parentNodeId != ASTNodeID.MemberAccessExpressionID &&
							parentNodeId != ASTNodeID.ArrayIndexExpressionID;
        		
        	}
        }

        boolean continueWalk = true;
        if (!isStatic)
        {
        	if (needClosure)
        		getEmitter().emitClosureStart();
        	
        	continueWalk = writeLeftSide(node, leftNode, rightNode);
            if (continueWalk)
            {
                getEmitter().startMapping(node, node.getLeftOperandNode(), node.getRightOperandNode());
                write(node.getOperator().getOperatorText());
                getEmitter().endMapping(node);
            }
        }

        if (continueWalk)
        {
            getWalker().walk(node.getRightOperandNode());
        }
        
        if (needClosure)
        {
        	write(ASEmitterTokens.COMMA);
        	write(ASEmitterTokens.SPACE);
        	writeLeftSide(node, leftNode, rightNode);
        	getEmitter().emitClosureEnd(node);
        }
        
        if (ASNodeUtils.hasParenClose(node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }

    private boolean writeLeftSide(IMemberAccessExpressionNode node, IASNode leftNode, IASNode rightNode)
    {
        if (!(leftNode instanceof ILanguageIdentifierNode && ((ILanguageIdentifierNode) leftNode)
                .getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.THIS))
        {
            IDefinition rightDef = null;
            if (rightNode instanceof IIdentifierNode)
                rightDef = ((IIdentifierNode) rightNode)
                        .resolve(getProject());

            if (leftNode.getNodeID() != ASTNodeID.SuperID)
            {
                getWalker().walk(node.getLeftOperandNode());
            }
            else if (leftNode.getNodeID() == ASTNodeID.SuperID
                    && (rightNode.getNodeID() == ASTNodeID.GetterID || (rightDef != null && rightDef instanceof AccessorDefinition)))
            {
                ICompilerProject project = this.getProject();
                if (project instanceof FlexJSProject)
                	((FlexJSProject)project).needLanguage = true;
                // setter is handled in binaryOperator
                write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSFlexJSEmitterTokens.SUPERGETTER);
                write(ASEmitterTokens.PAREN_OPEN);
                IClassNode cnode = (IClassNode) node
                        .getAncestorOfType(IClassNode.class);
                write(getEmitter().formatQualifiedName(
                        cnode.getQualifiedName()));
                writeToken(ASEmitterTokens.COMMA);
                write(ASEmitterTokens.THIS);
                writeToken(ASEmitterTokens.COMMA);
                write(ASEmitterTokens.SINGLE_QUOTE);
                if (rightDef != null)
                    write(rightDef.getBaseName());
                else
                    write(((GetterNode) rightNode).getName());
                write(ASEmitterTokens.SINGLE_QUOTE);
                write(ASEmitterTokens.PAREN_CLOSE);
                return false;
            }
        }
        else
        {
            getEmitter().startMapping(leftNode);
            write(ASEmitterTokens.THIS);
            getEmitter().endMapping(leftNode);
        }
        return true;
    }
    	
}
