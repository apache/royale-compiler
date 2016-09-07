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
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.flex.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IContainerNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IVariableExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;

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
        	if (((JSFlexJSEmitter)getEmitter()).isXML((IdentifierNode)obj))
        	{
        		write(".elementNames()");
        		isXML = true;
        	}
            if (((JSFlexJSEmitter)getEmitter()).isProxy((IdentifierNode)obj))
            {
                write(".propertyNames()");
                isProxy = true;
            }
        }
        else if (obj.getNodeID() == ASTNodeID.MemberAccessExpressionID)
        {
            if (((JSFlexJSEmitter)getEmitter()).isXMLList((MemberAccessExpressionNode)obj))
            {
                write(".elementNames()");
                isXML = true;
            }
            if (((JSFlexJSEmitter)getEmitter()).isProxy((MemberAccessExpressionNode)obj))
            {
                write(".propertyNames()");
                isXML = true;
            }
        }
        endMapping(rnode);
        startMapping(node, cnode);
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        endMapping(node);
        writeNewline();
        write(ASEmitterTokens.BLOCK_OPEN);
        writeNewline();
        startMapping(childNode);
        if (childNode instanceof IVariableExpressionNode)
        {
            write(ASEmitterTokens.VAR);
            write(ASEmitterTokens.SPACE);
            write(((IVariableNode) childNode.getChild(0)).getName());
        }
        else
            write(((IIdentifierNode) childNode).getName());
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.SPACE);
        endMapping(childNode);
        startMapping(rnode);
        write(targetName);
        if (isXML)
        {
        	write(".child(");
        	write(iterName);
        	write(")");
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
