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
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;

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
        	if (((JSRoyaleEmitter)getEmitter()).isXMLish((IdentifierNode)obj))
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
            if (((JSRoyaleEmitter)getEmitter()).isXMLList((IMemberAccessExpressionNode)obj))
            {
                write(".elementNames()");
                isXML = true;
            }
        }
        else if (obj.getNodeID() == ASTNodeID.MemberAccessExpressionID)
        {
            if (((JSRoyaleEmitter)getEmitter()).isXMLList((IMemberAccessExpressionNode)obj))
            {
                write(".elementNames()");
                isXML = true;
            }
            if (((JSRoyaleEmitter)getEmitter()).isProxy((IMemberAccessExpressionNode)obj))
            {
                write(".propertyNames()");
                isProxy = true;
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
        	} else if (funcName instanceof IMemberAccessExpressionNode) {
                IFunctionDefinition funcDef = (IFunctionDefinition) ((IMemberAccessExpressionNode) funcName).getRightOperandNode().resolve(getProject());
                if (funcDef == null) {
                    //we need to check the LHS for XMLishness, and then resolve the method name against the determined XMLish definition (XML or XMLList),
                    // and then check its return type once we find the public FunctionDefinition for the method name
                    // (because although it is a member of something XMLish, it may not return something that is also XMLish, such as a QName, a String, a uint, or a Namespace etc)
                    IDefinitionSet matchingDefinitions = null;
                    if (EmitterUtils.isLeftNodeXML(((IMemberAccessExpressionNode) funcName).getLeftOperandNode(), getProject())) {
                        if (((IMemberAccessExpressionNode) funcName).getRightOperandNode().getNodeID() == ASTNodeID.IdentifierID) {
                            matchingDefinitions = getProject().getBuiltinType(IASLanguageConstants.BuiltinType.XML).getContainedScope().getLocalDefinitionSetByName(((IIdentifierNode)((IMemberAccessExpressionNode) funcName).getRightOperandNode()).getName());
                        }
                    } else if (EmitterUtils.isLeftNodeXMLList(((IMemberAccessExpressionNode) funcName).getLeftOperandNode(), getProject())) {
                        if (((IMemberAccessExpressionNode) funcName).getRightOperandNode().getNodeID() == ASTNodeID.IdentifierID) {
                            matchingDefinitions = getProject().getBuiltinType(IASLanguageConstants.BuiltinType.XMLLIST).getContainedScope().getLocalDefinitionSetByName(((IIdentifierNode)((IMemberAccessExpressionNode) funcName).getRightOperandNode()).getName());
                        }
                    }
                    if (matchingDefinitions != null) {
                        for (int i = 0; i< matchingDefinitions.getSize(); i++) {
                            IDefinition functionDefinition = matchingDefinitions.getDefinition(i);
                            if (functionDefinition instanceof IFunctionDefinition) {
                                if (functionDefinition.isPublic()) {
                                    isXML = SemanticUtils.isXMLish((((IFunctionDefinition) functionDefinition).resolveReturnType(getProject())), getProject());
                                    break;
                                }
                            }
                        }
                    }

                    //@todo should we emit a warning here if wasXMLish (from either of the first 2 checks) && !isXML (from the matchingDefinitions check)?
                    // results will not be consistent in this case.
                    // e.g. looping over a QName or Namespace instance
                    // It is probably rare and ill-advised, but it definitely won't work well in javascript currently for those classes, for example.

                } else {
                    isXML = SemanticUtils.isXMLish(funcDef.resolveReturnType(getProject()), getProject());
                }
                if (isXML) {
                    write(".elementNames()");
                }
            } //@todo what about dynamic access node for function call? e.g. myXML[string_Value_Here]() ... not so easy really, would likely need a runtime helper/wrapper.
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
