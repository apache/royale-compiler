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
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.definitions.AccessorDefinition;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.flex.compiler.internal.tree.as.UnaryOperatorAtNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.utils.ASNodeUtils;

public class BinaryOperatorEmitter extends JSSubEmitter implements
        ISubEmitter<IBinaryOperatorNode>
{

    public BinaryOperatorEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IBinaryOperatorNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) getEmitter();

        ASTNodeID id = node.getNodeID();
        /*
        if (id == ASTNodeID.Op_InID
                || id == ASTNodeID.Op_LogicalAndAssignID
                || id == ASTNodeID.Op_LogicalOrAssignID)
        {
            super.emitBinaryOperator(node);
        }
        else */if (id == ASTNodeID.Op_IsID || id == ASTNodeID.Op_AsID)
        {
            fjs.emitIsAs(node.getLeftOperandNode(), node.getRightOperandNode(),
                    id, false);
        }
        else if (id == ASTNodeID.Op_InstanceOfID)
        {
            getWalker().walk(node.getLeftOperandNode());

            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.INSTANCEOF);

            IDefinition dnode = (node.getRightOperandNode())
                    .resolve(getProject());
            if (dnode != null)
                write(getEmitter()
                        .formatQualifiedName(dnode.getQualifiedName()));
            else
                getWalker().walk(node.getRightOperandNode());
        }
        else
        {
            IExpressionNode leftSide = node.getLeftOperandNode();
            if (leftSide.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            {
                IASNode lnode = leftSide.getChild(0);
                IASNode rnode = leftSide.getChild(1);
                IDefinition rnodeDef = (rnode instanceof IIdentifierNode) ? 
                		((IIdentifierNode) rnode).resolve(getWalker().getProject()) :
                		null;
                if (lnode.getNodeID() == ASTNodeID.SuperID
                        && rnodeDef instanceof AccessorDefinition)
                {
                    String op = node.getOperator().getOperatorText();
                    boolean isAssignment = op.contains("=")
                            && !op.contains("==")
                            && !(op.startsWith("<") || op.startsWith(">") || op
                                    .startsWith("!"));
                    if (isAssignment)
                    {
                        ICompilerProject project = this.getProject();
                        if (project instanceof FlexJSProject)
                        	((FlexJSProject)project).needLanguage = true;
                        
                        write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSFlexJSEmitterTokens.SUPERSETTER);
                        write(ASEmitterTokens.PAREN_OPEN);
                        IClassNode cnode = (IClassNode) node
                                .getAncestorOfType(IClassNode.class);
                        write(getEmitter().formatQualifiedName(
                                cnode.getQualifiedName()));
                        writeToken(ASEmitterTokens.COMMA);
                        write(ASEmitterTokens.THIS);
                        writeToken(ASEmitterTokens.COMMA);
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        write(rnodeDef.getBaseName());
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        writeToken(ASEmitterTokens.COMMA);

                        if (op.length() > 1) // += and things like that
                        {
                            write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
                            write(ASEmitterTokens.MEMBER_ACCESS);
                            write(JSFlexJSEmitterTokens.SUPERSETTER);
                            write(ASEmitterTokens.PAREN_OPEN);
                            write(getEmitter().formatQualifiedName(
                                    cnode.getQualifiedName()));
                            writeToken(ASEmitterTokens.COMMA);
                            write(ASEmitterTokens.THIS);
                            writeToken(ASEmitterTokens.COMMA);
                            write(ASEmitterTokens.SINGLE_QUOTE);
                            write(rnodeDef.getBaseName());
                            write(ASEmitterTokens.SINGLE_QUOTE);
                            write(ASEmitterTokens.PAREN_CLOSE);
                            write(op.substring(0, 1));
                        }

                        getWalker().walk(node.getRightOperandNode());
                        write(ASEmitterTokens.PAREN_CLOSE);
                        return;
                    }
                }
                else if (((JSFlexJSEmitter)getEmitter()).isXMLList((MemberAccessExpressionNode)leftSide))
                {
                	MemberAccessExpressionNode xmlNode = (MemberAccessExpressionNode)leftSide;
                	if (node.getNodeID() == ASTNodeID.Op_AssignId)
                	{
	                    getWalker().walk(xmlNode.getLeftOperandNode());
	                    IExpressionNode rightSide = xmlNode.getRightOperandNode();
	                    if (rightSide instanceof UnaryOperatorAtNode)
	                    {
		                    write(".setAttribute('");
		                    getWalker().walk(((UnaryOperatorAtNode)rightSide).getChild(0));
	                    }
	                    else
	                    {
		                    write(".setChild('");
		                    getWalker().walk(rightSide);
	                    }
	                    write("', ");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                	else if (node.getNodeID() == ASTNodeID.Op_AddAssignID)
                	{
	                    getWalker().walk(xmlNode);
	                    write(".concat(");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                	else if (node.getNodeID() == ASTNodeID.Op_AddID)
                	{
	                    getWalker().walk(xmlNode);
	                    write(".copy().concat(");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                }
                else if (((JSFlexJSEmitter)getEmitter()).isProxy((MemberAccessExpressionNode)leftSide))
                {
                	MemberAccessExpressionNode proxyNode = (MemberAccessExpressionNode)leftSide;
                	if (node.getNodeID() == ASTNodeID.Op_AssignId)
                	{
	                    getWalker().walk(proxyNode.getLeftOperandNode());
	                    IExpressionNode rightSide = proxyNode.getRightOperandNode();
	                    write(".setProperty('");
	                    getWalker().walk(rightSide);
	                    write("', ");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                	else if (node.getNodeID() == ASTNodeID.Op_AddAssignID)
                	{
	                    IExpressionNode rightSide = proxyNode.getRightOperandNode();
	                    getWalker().walk(proxyNode.getLeftOperandNode());
	                    write(".setProperty('");
	                    getWalker().walk(rightSide);
	                    write("', ");
	                    getWalker().walk(proxyNode.getLeftOperandNode());
	                    write(".getProperty(");
	                    write(ASEmitterTokens.SINGLE_QUOTE);
	                    getWalker().walk(rightSide);
	                    write(ASEmitterTokens.SINGLE_QUOTE);
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    write(" + ");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                }
            }

            super_emitBinaryOperator(node);
            /*
            IExpressionNode leftSide = node.getLeftOperandNode();

            IExpressionNode property = null;
            int leftSideChildCount = leftSide.getChildCount();
            if (leftSideChildCount > 0)
            {
                IASNode childNode = leftSide.getChild(leftSideChildCount - 1);
                if (childNode instanceof IExpressionNode)
                    property = (IExpressionNode) childNode;
                else
                    property = leftSide;
            }
            else
                property = leftSide;

            IDefinition def = null;
            if (property instanceof IIdentifierNode)
                def = ((IIdentifierNode) property).resolve(getWalker()
                        .getProject());

            boolean isSuper = false;
            if (leftSide.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            {
                IASNode cnode = leftSide.getChild(0);
                ASTNodeID cId = cnode.getNodeID();

                isSuper = cId == ASTNodeID.SuperID;
            }

            String op = node.getOperator().getOperatorText();
            boolean isAssignment = op.contains("=") && !op.contains("==") && 
                                                    !(op.startsWith("<") || 
                                                            op.startsWith(">") || 
                                                            op.startsWith("!"));

            if (def instanceof AccessorDefinition && isAssignment)
            {
                // this will make the set_foo call
                getWalker().walk(leftSide);
            }
            else if (isSuper) 
            {
                emitSuperCall(node, "");
            }
            else
            {
                if (ASNodeUtils.hasParenOpen(node))
                    write(ASEmitterTokens.PAREN_OPEN);

                getWalker().walk(leftSide);

                if (node.getNodeID() != ASTNodeID.Op_CommaID)
                    write(ASEmitterTokens.SPACE);

                writeToken(node.getOperator().getOperatorText());

                getWalker().walk(node.getRightOperandNode());

                if (ASNodeUtils.hasParenClose(node))
                    write(ASEmitterTokens.PAREN_CLOSE);
            }
            */
        }
    }

    private void super_emitBinaryOperator(IBinaryOperatorNode node)
    {
        if (ASNodeUtils.hasParenOpen(node))
            write(ASEmitterTokens.PAREN_OPEN);

        ASTNodeID id = node.getNodeID();

        if (id == ASTNodeID.Op_IsID)
        {
            write(ASEmitterTokens.IS);
            write(ASEmitterTokens.PAREN_OPEN);
            getWalker().walk(node.getLeftOperandNode());
            writeToken(ASEmitterTokens.COMMA);
            getWalker().walk(node.getRightOperandNode());
            write(ASEmitterTokens.PAREN_CLOSE);
        }
        else if (id == ASTNodeID.Op_AsID)
        {
            // (is(a, b) ? a : null)
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.IS);
            write(ASEmitterTokens.PAREN_OPEN);
            getWalker().walk(node.getLeftOperandNode());
            writeToken(ASEmitterTokens.COMMA);
            getWalker().walk(node.getRightOperandNode());
            writeToken(ASEmitterTokens.PAREN_CLOSE);
            writeToken(ASEmitterTokens.TERNARY);
            getWalker().walk(node.getLeftOperandNode());
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.COLON);
            write(ASEmitterTokens.NULL);
            write(ASEmitterTokens.PAREN_CLOSE);
        }
        else
        {
            getWalker().walk(node.getLeftOperandNode());

            if (id != ASTNodeID.Op_CommaID)
                write(ASEmitterTokens.SPACE);

            // (erikdebruin) rewrite 'a &&= b' to 'a = a && b'
            if (id == ASTNodeID.Op_LogicalAndAssignID
                    || id == ASTNodeID.Op_LogicalOrAssignID)
            {
                IIdentifierNode lnode = (IIdentifierNode) node
                        .getLeftOperandNode();

                writeToken(ASEmitterTokens.EQUAL);
                writeToken(lnode.getName());
                write((id == ASTNodeID.Op_LogicalAndAssignID) ? ASEmitterTokens.LOGICAL_AND
                        : ASEmitterTokens.LOGICAL_OR);
            }
            else
            {
                write(node.getOperator().getOperatorText());
            }

            write(ASEmitterTokens.SPACE);

            /*
            IDefinition definition = node.getRightOperandNode().resolve(getProject());
        	if (definition instanceof FunctionDefinition &&
        			(!(definition instanceof AccessorDefinition)))
        	{
        	}
        	else */
        		getWalker().walk(node.getRightOperandNode());
                if (node.getNodeID() == ASTNodeID.Op_InID &&
                        ((JSFlexJSEmitter)getEmitter()).isXML(node.getRightOperandNode()))
                {
                	write(".elementNames()");
                }   
                else if (node.getNodeID() == ASTNodeID.Op_InID &&
                        ((JSFlexJSEmitter)getEmitter()).isProxy(node.getRightOperandNode()))
                {
                	write(".propertyNames()");
                }   
        }

        if (ASNodeUtils.hasParenOpen(node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }
}
