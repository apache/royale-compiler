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

import org.apache.royale.compiler.codegen.IDocEmitter;
import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.definitions.AppliedVectorDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.tree.as.BinaryOperatorAssignmentNode;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IDynamicAccessNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.IOperatorNode.OperatorType;
import org.apache.royale.compiler.utils.NativeUtils;

public class DynamicAccessEmitter extends JSSubEmitter implements
        ISubEmitter<IDynamicAccessNode>
{
    public DynamicAccessEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IDynamicAccessNode node)
    {
        IExpressionNode leftOperandNode = node.getLeftOperandNode();
        getWalker().walk(leftOperandNode);
        if (leftOperandNode.getNodeID() == ASTNodeID.Op_AtID)
        	return;

        IExpressionNode rightOperandNode = node.getRightOperandNode();
		ITypeDefinition type = rightOperandNode.resolveType(getProject());
        IJSEmitter ijs = getEmitter();
    	JSRoyaleEmitter fjs = (ijs instanceof JSRoyaleEmitter) ? 
    							(JSRoyaleEmitter)ijs : null;
    	if (fjs != null)
    	{
        	boolean isProxy = false;
	    	boolean isXML = false;
	    	if (leftOperandNode instanceof MemberAccessExpressionNode)
	    		isXML = fjs.isLeftNodeXMLish((MemberAccessExpressionNode)leftOperandNode);
	    	else if (leftOperandNode instanceof IExpressionNode)
	    		isXML = fjs.isXMLish((IExpressionNode)leftOperandNode);
        	if (leftOperandNode instanceof MemberAccessExpressionNode)
        		isProxy = fjs.isProxy((MemberAccessExpressionNode)leftOperandNode);
        	else if (leftOperandNode instanceof IExpressionNode)
        		isProxy = fjs.isProxy((IExpressionNode)leftOperandNode);
	    	if (isXML)
	    	{
				if (type == null) {
	    			//this can happen if myThing is of type Object or AnyType (*)
					//with example: myXml.somethingChild[myThing.id]
	    			//use Stringify with 'child' method, which has support for attributes vs elements
					write(".child('' +");
						getWalker().walk(rightOperandNode);
					write(")");
					return;
				}
				if (type.isInstanceOf("String", getProject()))
				{
					String field = fjs.stringifyNode(rightOperandNode);
					if (field.startsWith("\"@"))
					{
						field = field.replace("@", "");
						write(".attribute(" + field + ")");
					}
					else
						write(".child(" + field + ")");
					return;
				}
				else if (type.isInstanceOf("QName", getProject()))
				{
					String field = fjs.stringifyNode(rightOperandNode);					
					write(".child(" + field + ")");
					return;
				}
	    	}
        	else if (isProxy)
        	{
        		boolean isLiteral = rightOperandNode instanceof ILiteralNode;
        		write(".getProperty(");
        		if (isLiteral) write("'");
        		String s = fjs.stringifyNode(rightOperandNode);
        		write(s);
        		if (isLiteral) write("'");
        		write(")");
        		return;
        	}
    	}
    	
        startMapping(node, leftOperandNode);
        write(ASEmitterTokens.SQUARE_OPEN);
        endMapping(node);
        boolean wrapVectorIndex = false;
        if (getProject() instanceof RoyaleJSProject) {
			if (node.getNodeID().equals(ASTNodeID.ArrayIndexExpressionID)){
				if (node.getParent() instanceof BinaryOperatorAssignmentNode) {
					if (node.getLeftOperandNode().resolveType(getProject()) instanceof AppliedVectorDefinition) {
						boolean suppressVectorIndexCheck = !(((RoyaleJSProject)getProject()).config.getJsVectorIndexChecks());

						IDocEmitter docEmitter = getEmitter().getDocEmitter();
						if (docEmitter instanceof JSRoyaleDocEmitter)
						{
							JSRoyaleDocEmitter royaleDocEmitter = (JSRoyaleDocEmitter) docEmitter;
							//check for local toggle
							suppressVectorIndexCheck = royaleDocEmitter.getLocalSettingAsBoolean(
									JSRoyaleEmitterTokens.SUPPRESS_VECTOR_INDEX_CHECK, suppressVectorIndexCheck);
							
							if (!suppressVectorIndexCheck) {
								//check for individual specified suppression, based on variable name
								if (leftOperandNode instanceof IdentifierNode) {
									if (royaleDocEmitter.getLocalSettingIncludesString(
											JSRoyaleEmitterTokens.SUPPRESS_VECTOR_INDEX_CHECK,
											((IdentifierNode) leftOperandNode).getName()
									)){
										suppressVectorIndexCheck = true;
									}
								}
							}
						}
						if (!suppressVectorIndexCheck) {
							getModel().needLanguage = true;
							((RoyaleJSProject) getProject()).needLanguage = true;
							getWalker().walk(leftOperandNode);
							write(ASEmitterTokens.SQUARE_OPEN);
							write(JSRoyaleEmitterTokens.VECTOR_INDEX_CHECK_METHOD_NAME);
							write(ASEmitterTokens.SQUARE_CLOSE);
							write(ASEmitterTokens.PAREN_OPEN);
							wrapVectorIndex = true;
						}
					}
				}
			}
		}
        
        getWalker().walk(rightOperandNode);
        if (wrapVectorIndex) {
        	write(ASEmitterTokens.PAREN_CLOSE);
		}
        if (type != null && type.getQualifiedName().contentEquals(IASLanguageConstants.QName))
        	write(".objectAccessFormat()");
        startMapping(node, rightOperandNode);
        write(ASEmitterTokens.SQUARE_CLOSE);
        endMapping(node);
    }
}
