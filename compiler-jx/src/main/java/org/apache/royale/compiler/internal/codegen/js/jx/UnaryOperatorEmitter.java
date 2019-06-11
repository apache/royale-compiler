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

package org.apache.royale.compiler.internal.codegen.js.jx;

import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.definitions.AppliedVectorDefinition;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.utils.ASNodeUtils;

public class UnaryOperatorEmitter extends JSSubEmitter implements
        ISubEmitter<IUnaryOperatorNode>
{
    public UnaryOperatorEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IUnaryOperatorNode node)
    {
        if (ASNodeUtils.hasParenOpen(node))
            write(ASEmitterTokens.PAREN_OPEN);
        
        Boolean isAssignment = (node.getNodeID() == ASTNodeID.Op_PreIncrID
                || node.getNodeID() == ASTNodeID.Op_PreDecrID
                || node.getNodeID() == ASTNodeID.Op_PostIncrID
                || node.getNodeID() == ASTNodeID.Op_PostDecrID);
        
        if (isAssignment && (node.getOperandNode() instanceof MemberAccessExpressionNode)
                && (((MemberAccessExpressionNode)(node.getOperandNode())).getRightOperandNode() instanceof IdentifierNode)
                && ((IdentifierNode)(((MemberAccessExpressionNode)(node.getOperandNode())).getRightOperandNode())).getName().equals("length")
                && ((MemberAccessExpressionNode)(node.getOperandNode())).getLeftOperandNode().resolveType(getProject()) instanceof AppliedVectorDefinition
        ) {
            //support for output of alternate length setter, example: vectorInst.length++ as vectorInst['_synthType'].length++
            //likewise for pre/post increment/decrement
            
            String synthTagName = JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken() + ASEmitterTokens.MEMBER_ACCESS.getToken() + JSRoyaleEmitterTokens.ROYALE_SYNTH_TAG_FIELD_NAME.getToken();
            LiteralNode synthType = new LiteralNode(ILiteralNode.LiteralType.STRING, synthTagName);
            synthType.setSynthetic(true);
            DynamicAccessNode patchedVectorReference = new DynamicAccessNode(((ExpressionNodeBase)((MemberAccessExpressionNode) node.getOperandNode()).getLeftOperandNode()));
            ((ExpressionNodeBase)((MemberAccessExpressionNode) node.getOperandNode()).getLeftOperandNode()).setParent(patchedVectorReference);
            patchedVectorReference.setRightOperandNode(synthType);
            synthType.setParent(patchedVectorReference);
            patchedVectorReference.setParent((NodeBase) node.getOperandNode());
            patchedVectorReference.setSourceLocation(((MemberAccessExpressionNode) node.getOperandNode()).getLeftOperandNode());
            ((MemberAccessExpressionNode) node.getOperandNode()).setLeftOperandNode(patchedVectorReference);
        }

        if (node.getNodeID() == ASTNodeID.Op_PreIncrID
                || node.getNodeID() == ASTNodeID.Op_PreDecrID
                || node.getNodeID() == ASTNodeID.Op_BitwiseNotID
                || node.getNodeID() == ASTNodeID.Op_LogicalNotID
                || node.getNodeID() == ASTNodeID.Op_SubtractID
                || node.getNodeID() == ASTNodeID.Op_AddID)
        {
            emitPreUnaryOperator(node);
        }
        else if (node.getNodeID() == ASTNodeID.Op_PostIncrID
                || node.getNodeID() == ASTNodeID.Op_PostDecrID)
        {
            emitPostUnaryOperator(node);
        }
        else if (node.getNodeID() == ASTNodeID.Op_DeleteID)
        {
            emitDeleteOperator(node);
        }
        else if (node.getNodeID() == ASTNodeID.Op_VoidID)
        {
            emitVoidOperator(node);
        }
        else if (node.getNodeID() == ASTNodeID.Op_TypeOfID)
        {
            emitTypeOfOperator(node);
        }

        if (ASNodeUtils.hasParenClose(node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }

    public void emitPreUnaryOperator(IUnaryOperatorNode node)
    {
        startMapping(node);
        write(node.getOperator().getOperatorText());
        IExpressionNode opNode = node.getOperandNode();
        endMapping(node);
        getWalker().walk(opNode);
    }

    protected void emitPostUnaryOperator(IUnaryOperatorNode node)
    {
        IExpressionNode operandNode = node.getOperandNode();
        getWalker().walk(operandNode);
        startMapping(node, operandNode);
        write(node.getOperator().getOperatorText());
        endMapping(node);
    }

    protected void emitDeleteOperator(IUnaryOperatorNode node)
    {
        startMapping(node);
        writeToken(node.getOperator().getOperatorText());
        endMapping(node);
        getWalker().walk(node.getOperandNode());
    }

    protected void emitVoidOperator(IUnaryOperatorNode node)
    {
        startMapping(node);
        writeToken(node.getOperator().getOperatorText());
        endMapping(node);
        getWalker().walk(node.getOperandNode());
    }

    protected void emitTypeOfOperator(IUnaryOperatorNode node)
    {
        startMapping(node);
        write(node.getOperator().getOperatorText());
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);
        IExpressionNode operandNode = node.getOperandNode();
        getWalker().walk(operandNode);
        startMapping(node);
        write(ASEmitterTokens.PAREN_CLOSE);
        endMapping(node);
    }
}
