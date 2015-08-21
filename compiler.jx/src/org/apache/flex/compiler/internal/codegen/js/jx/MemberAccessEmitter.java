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
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.definitions.AccessorDefinition;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.tree.as.GetterNode;
import org.apache.flex.compiler.internal.tree.as.UnaryOperatorAtNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
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

        IDefinition def = node.resolve(getProject());
        boolean isStatic = false;
        if (def != null && def.isStatic())
            isStatic = true;
        boolean needClosure = false;
        if (def instanceof FunctionDefinition && (!(def instanceof AccessorDefinition)))
        {
        	IASNode parentNode = node.getParent();
        	if (parentNode != null)
        	{
				ASTNodeID parentNodeId = parentNode.getNodeID();
				// we need a closure if this MAE is the top-level in a chain
				// of MAE and not in a function call.
				needClosure = parentNodeId != ASTNodeID.FunctionCallID &&
							parentNodeId != ASTNodeID.MemberAccessExpressionID;
        		
        	}
        }

        boolean continueWalk = true;
        if (!isStatic)
        {
        	if (needClosure)
        		getEmitter().emitClosureStart();
        	
        	continueWalk = writeLeftSide(node, leftNode, rightNode);
            if (continueWalk)
                write(node.getOperator().getOperatorText());
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

            if (rightNode instanceof UnaryOperatorAtNode)
            {
                // ToDo (erikdebruin): properly handle E4X

                write(ASEmitterTokens.THIS);
                write(ASEmitterTokens.MEMBER_ACCESS);
                getWalker().walk(node.getLeftOperandNode());
                write(ASEmitterTokens.SQUARE_OPEN);
                write(ASEmitterTokens.SINGLE_QUOTE);
                write("E4XOperator");
                write(ASEmitterTokens.SINGLE_QUOTE);
                write(ASEmitterTokens.SQUARE_CLOSE);
                return false;
            }
            else if (node.getNodeID() == ASTNodeID.Op_DescendantsID)
            {
                // ToDo (erikdebruin): properly handle E4X

                write(ASEmitterTokens.THIS);
                write(ASEmitterTokens.MEMBER_ACCESS);
                getWalker().walk(node.getLeftOperandNode());
                write(ASEmitterTokens.SQUARE_OPEN);
                write(ASEmitterTokens.SINGLE_QUOTE);
                write("E4XSelector");
                write(ASEmitterTokens.SINGLE_QUOTE);
                write(ASEmitterTokens.SQUARE_CLOSE);
                return false;
            }
            else if (leftNode.getNodeID() != ASTNodeID.SuperID)
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
            write(ASEmitterTokens.THIS);
        }
        return true;
    }
    	
}
