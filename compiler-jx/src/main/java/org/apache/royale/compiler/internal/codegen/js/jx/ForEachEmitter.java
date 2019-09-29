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
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IVariableExpressionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;

public class ForEachEmitter extends JSSubEmitter implements
        ISubEmitter<IForLoopNode>
{

    public ForEachEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IForLoopNode node)
    {
        IContainerNode cnode = node.getConditionalsContainerNode();
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) cnode.getChild(0);
        IExpressionNode childNode = bnode.getLeftOperandNode();
        IExpressionNode rnode = bnode.getRightOperandNode();

        final String iterName = getModel().getCurrentForeachName();
        getModel().incForeachLoopCount();
        final String targetName = iterName + "_target";
        
        startMapping(rnode);
        write(ASEmitterTokens.VAR);
        write(ASEmitterTokens.SPACE);
        write(targetName);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.SPACE);
        endMapping(rnode);
        IASNode obj = bnode.getChild(1);
        getWalker().walk(obj);
        startMapping(rnode);
        write(ASEmitterTokens.SEMICOLON);
        endMapping(rnode);
        writeNewline();

        if (node.getParent().getNodeID() == ASTNodeID.BlockID &&
        		node.getParent().getParent().getNodeID() == ASTNodeID.LabledStatementID)
        {
        	// emit label here
        	LabeledStatementNode labelNode = (LabeledStatementNode)node.getParent().getParent();
            writeToken(labelNode.getLabel());
            writeToken(ASEmitterTokens.COLON);

        }

        startMapping(node);
        write(ASEmitterTokens.FOR);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);
        startMapping(rnode);
        write(ASEmitterTokens.VAR);
        write(ASEmitterTokens.SPACE);
        write(iterName);
        endMapping(rnode);
        startMapping(bnode, childNode);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.IN);
        write(ASEmitterTokens.SPACE);
        endMapping(bnode);
        startMapping(rnode);
        write(targetName);
        boolean isXML = false;
        boolean isProxy = false;
        if (obj.getNodeID() == ASTNodeID.IdentifierID)
        {
        	if (((JSRoyaleEmitter)getEmitter()).isXML((IdentifierNode)obj))
        	{
        		write(".elementNames()");
        		isXML = true;
        	}
            if (((JSRoyaleEmitter)getEmitter()).isProxy((IdentifierNode)obj))
            {
                write(".propertyNames()");
                isProxy = true;
            }
        }
        else if (obj.getNodeID() == ASTNodeID.Op_DescendantsID)
        {
            //it should always be XMLList... but check anyway
            if (((JSRoyaleEmitter)getEmitter()).isXMLList((MemberAccessExpressionNode)obj))
            {
                write(".elementNames()");
                isXML = true;
            }
        }
        else if (obj.getNodeID() == ASTNodeID.MemberAccessExpressionID)
        {
            if (((JSRoyaleEmitter)getEmitter()).isXMLList((MemberAccessExpressionNode)obj))
            {
                write(".elementNames()");
                isXML = true;
            }
            if (((JSRoyaleEmitter)getEmitter()).isProxy((MemberAccessExpressionNode)obj))
            {
                write(".propertyNames()");
                isXML = true;
            }
        }
        else if (obj.getNodeID() == ASTNodeID.Op_AsID)
        {
        	IASNode asChild = obj.getChild(1);        	
        	if (asChild.getNodeID() == ASTNodeID.IdentifierID)
        	{
        		String asName = ((IdentifierNode)asChild).getName();
        		if (asName.equals(IASLanguageConstants.XML) || asName.equals(IASLanguageConstants.XMLList))
        		{
                    write(".elementNames()");
                    isXML = true;
        		}
        	}
        }
        else if (obj.getNodeID() == ASTNodeID.FunctionCallID)
        {
        	FunctionCallNode func = (FunctionCallNode)obj;
        	IExpressionNode funcName = func.getNameNode();
        	if (funcName.getNodeID() == ASTNodeID.IdentifierID)
        	{
        		String asName = ((IdentifierNode)funcName).getName();
        		if (asName.equals(IASLanguageConstants.XML) || asName.equals(IASLanguageConstants.XMLList))
        		{
                    write(".elementNames()");
                    isXML = true;
        		}        		
        	}
        }
        endMapping(rnode);
        startMapping(node, cnode);
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        endMapping(node);
        writeNewline();
        write(ASEmitterTokens.BLOCK_OPEN);
        writeNewline();
        
        if (childNode instanceof IVariableExpressionNode)
        {
            startMapping(childNode);
            write(ASEmitterTokens.VAR);
            write(ASEmitterTokens.SPACE);
            write(((IVariableNode) childNode.getChild(0)).getName()); //it's always a local var
            //putting this in here instead of common code following the 2 blocks to keep sourcemap tests passing
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.SPACE);
            endMapping(childNode);
        }
        else { //IdentifierNode
            getWalker().walk(childNode); //we need to walk here, to deal with non-local var identifiers
            startMapping(childNode);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.SPACE);
            endMapping(childNode);
        }
      
        startMapping(rnode);
        write(targetName);
        if (isXML)
        {
        	write("[");
        	write(iterName);
        	write("]");
        }
        else if (isProxy)
        {
            write(".getProperty(");
            write(iterName);
            write(")");
        }
        else
        {
	        write(ASEmitterTokens.SQUARE_OPEN);
	        write(iterName);
	        write(ASEmitterTokens.SQUARE_CLOSE);
        }
        write(ASEmitterTokens.SEMICOLON);
        endMapping(rnode);
        writeNewline();
        getWalker().walk(node.getStatementContentsNode());
        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline();
    }

}
