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
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition.FunctionClassification;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition.VariableClassification;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.definitions.AccessorDefinition;
import org.apache.royale.compiler.internal.definitions.AppliedVectorDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.utils.ASNodeUtils;

import java.util.ArrayList;

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
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();

        String op = node.getOperator().getOperatorText();
        boolean isAssignment = op.contains("=")
                && !op.contains("==")
                && !(op.startsWith("<") || op.startsWith(">") || op
                        .startsWith("!"));
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
            fjs.emitIsAs(node, node.getLeftOperandNode(), node.getRightOperandNode(),
                    id, false);
        }
        else if (id == ASTNodeID.Op_InstanceOfID)
        {
			if (ASNodeUtils.hasParenOpen(node)) {
				write(ASEmitterTokens.PAREN_OPEN);
			}
            getWalker().walk(node.getLeftOperandNode());

            startMapping(node, node.getLeftOperandNode());
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.INSTANCEOF);
            endMapping(node);

            IDefinition dnode = (node.getRightOperandNode())
                    .resolve(getProject());
			if (dnode != null)
			{
				String dnodeQname = dnode.getQualifiedName();
                boolean isPackageOrFileMember = false;
                if (dnode instanceof IVariableDefinition)
                {
                    IVariableDefinition variable = (IVariableDefinition) dnode;
                    VariableClassification classification = variable.getVariableClassification();
                    if (classification == VariableClassification.PACKAGE_MEMBER ||
                            classification == VariableClassification.FILE_MEMBER)
                    {
                        isPackageOrFileMember = true;
                    }
                }
                else if (dnode instanceof IFunctionDefinition)
                {
                    IFunctionDefinition func = (IFunctionDefinition) dnode;
                    FunctionClassification classification = func.getFunctionClassification();
                    if (classification == FunctionClassification.PACKAGE_MEMBER ||
                            classification == FunctionClassification.FILE_MEMBER)
                    {
                        isPackageOrFileMember = true;
                    }
                }
				else if(dnode instanceof ITypeDefinition)
				{
					isPackageOrFileMember = true;
				}
				if(isPackageOrFileMember)
				{
					dnodeQname = getEmitter().formatQualifiedName(dnodeQname);
				}
                write(dnodeQname);
			}
			else
			{
				getWalker().walk(node.getRightOperandNode());
			}
			if (ASNodeUtils.hasParenClose(node)) {
				write(ASEmitterTokens.PAREN_CLOSE);
			}
        }
        else
        {
            IExpressionNode leftSide = node.getLeftOperandNode();
            IDefinition leftDef = leftSide.resolveType(getWalker().getProject());
            if (leftSide.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            {
                IASNode lnode = leftSide.getChild(0);
                IASNode rnode = leftSide.getChild(1);
                IDefinition rnodeDef = (rnode instanceof IIdentifierNode) ?
                		((IIdentifierNode) rnode).resolve(getWalker().getProject()) :
                		null;
                boolean isDynamicAccess = rnode instanceof DynamicAccessNode;
                if (lnode.getNodeID() == ASTNodeID.SuperID
                        && rnodeDef instanceof AccessorDefinition)
                {
                    if (isAssignment)
                    {
						boolean propagateAssignedValue = !(node.getParent() instanceof IBlockNode);
						if (propagateAssignedValue)
						{
							write(ASEmitterTokens.PAREN_OPEN);
							write(ASEmitterTokens.FUNCTION);
							write(ASEmitterTokens.PAREN_OPEN);
							write("$value");
							write(ASEmitterTokens.PAREN_CLOSE);
							write(ASEmitterTokens.BLOCK_OPEN);
						}

						IClassNode cnode = (IClassNode) node
                                .getAncestorOfType(IClassNode.class);
                        if (cnode != null)
                        	write(getEmitter().formatQualifiedName(
                                cnode.getQualifiedName()));
                        else
                        	write(getEmitter().formatQualifiedName(
                        		getModel().getCurrentClass().getQualifiedName()));
                        		
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSGoogEmitterTokens.SUPERCLASS);
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(fjs.formatSetter(rnodeDef.getBaseName()));
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSEmitterTokens.APPLY);
                        write(ASEmitterTokens.PAREN_OPEN);
                        write(ASEmitterTokens.THIS);
                        writeToken(ASEmitterTokens.COMMA);
                        write(ASEmitterTokens.SQUARE_OPEN);
                        if (op.length() > 1) // += and things like that
                        {
                            write(getEmitter().formatQualifiedName(
                                    cnode.getQualifiedName()));
                            write(ASEmitterTokens.MEMBER_ACCESS);
                            write(JSGoogEmitterTokens.SUPERCLASS);
                            write(ASEmitterTokens.MEMBER_ACCESS);
							write(fjs.formatGetter(rnodeDef.getBaseName()));
                            write(ASEmitterTokens.MEMBER_ACCESS);
                            write(JSEmitterTokens.APPLY);
                            write(ASEmitterTokens.PAREN_OPEN);
                            write(ASEmitterTokens.THIS);
                            write(ASEmitterTokens.PAREN_CLOSE);
                            write(op.substring(0, 1));
                        }

						if (propagateAssignedValue)
						{
							write("$value");
							write(ASEmitterTokens.SQUARE_CLOSE);
							write(ASEmitterTokens.PAREN_CLOSE);
							write(ASEmitterTokens.SEMICOLON);
							// returning the original $value ensures that
							// chained assignments work properly
							// the getter should NOT be called!
							// x = super.y = z;
							writeToken(ASEmitterTokens.RETURN);
							write("$value");
							write(ASEmitterTokens.SEMICOLON);
							write(ASEmitterTokens.BLOCK_CLOSE);
							write(ASEmitterTokens.PAREN_CLOSE);
							write(ASEmitterTokens.MEMBER_ACCESS);
							write(JSEmitterTokens.APPLY);
							write(ASEmitterTokens.PAREN_OPEN);
							write(ASEmitterTokens.THIS);
							writeToken(ASEmitterTokens.COMMA);
							write(ASEmitterTokens.SQUARE_OPEN);
						}
                        getWalker().walk(node.getRightOperandNode());
                        write(ASEmitterTokens.SQUARE_CLOSE);
                        write(ASEmitterTokens.PAREN_CLOSE);
                        return;
                    }
                }
                else if (((JSRoyaleEmitter)getEmitter()).isXMLList((IMemberAccessExpressionNode)leftSide))
                {
                	MemberAccessExpressionNode xmlNode = (MemberAccessExpressionNode)leftSide;
                	if (node.getNodeID() == ASTNodeID.Op_AssignId)
                	{
                		boolean wrapQuotes = true;
	                    getWalker().walk(xmlNode.getLeftOperandNode());
	                    IExpressionNode rightSide = xmlNode.getRightOperandNode();
	                    if (rightSide instanceof UnaryOperatorAtNode)
	                    {
		                    write(".setAttribute('");
		                    getWalker().walk(rightSide.getChild(0));
	                    }
	                    else if (rightSide instanceof IDynamicAccessNode && ((IDynamicAccessNode) rightSide).getLeftOperandNode().getNodeID() == ASTNodeID.Op_AtID) {
							write(".setAttribute(");
							wrapQuotes = false;
							getWalker().walk(((IDynamicAccessNode)rightSide).getRightOperandNode());
						}
	                    else if (rightSide instanceof INamespaceAccessExpressionNode) {
							write(".setChild(");
							write("new QName(");
							getWalker().walk(((INamespaceAccessExpressionNode) rightSide).getLeftOperandNode());
							write(",'");
							getWalker().walk(((INamespaceAccessExpressionNode) rightSide).getRightOperandNode());
							write("')");
							wrapQuotes = false;
						}
	                    else
	                    {
		                    write(".setChild('");
		                    getWalker().walk(rightSide);
	                    }
	                    if (wrapQuotes) write("'");
	                    write(", ");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                	else if (node.getNodeID() == ASTNodeID.Op_AddAssignID)
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
                        getWalker().walk(node.getLeftOperandNode());
	                    write(".plus(");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                	else if (node.getNodeID() == ASTNodeID.Op_AddID)
                	{
	                    getWalker().walk(xmlNode);
	                    write(".plus(");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                }
                else if (isDynamicAccess && ((JSRoyaleEmitter)getEmitter()).isXMLish((IExpressionNode)lnode))
                {
                	DynamicAccessNode dyn = (DynamicAccessNode)rnode;
                	ITypeDefinition type = dyn.getRightOperandNode().resolveType(getProject());
                	if (type.isInstanceOf("String", getProject()) || type.isInstanceOf("Object", getProject())
                			|| type == getProject().getBuiltinType(BuiltinType.ANY_TYPE))
        			{
                		String field;
                    	if (node.getNodeID() == ASTNodeID.Op_AssignId)
                    	{
    	                    getWalker().walk(lnode);
    	                    IExpressionNode dynLeft = dyn.getLeftOperandNode();
    	                    IExpressionNode dynRight = dyn.getRightOperandNode();
    	                    if (dynLeft instanceof UnaryOperatorAtNode)
    	                    {
    		                    write(".setAttribute(");
    							field = fjs.stringifyNode(dyn.getRightOperandNode());
    	                    }
    	                    else if (dynRight instanceof UnaryOperatorAtNode)
    	                    {
    		                    write(".setAttribute(");
    							field = fjs.stringifyNode(dynRight.getChild(0));
    	                    }
    	                    else
    	                    {
    		                    write(".setChild(");
    							field = fjs.stringifyNode(dynLeft);
    	                    }
    	                    write(field + ", ");
    	                    getWalker().walk(node.getRightOperandNode());
    	                    write(ASEmitterTokens.PAREN_CLOSE);
    	                    return;
                    	}
                    	else if (node.getNodeID() == ASTNodeID.Op_AddAssignID)
                    	{
    	                    getWalker().walk(lnode);
    	                    IExpressionNode rightSide = dyn.getRightOperandNode();
    	                    if (rightSide instanceof UnaryOperatorAtNode)
    	                    {
    		                    write(".setAttribute('");
    							field = fjs.stringifyNode(((UnaryOperatorAtNode)rightSide).getChild(0));
    							field = field.replace("\"", ""); // remove wrapping double-quotes
    	                    }
    	                    else
    	                    {
    		                    write(".setChild('");
    							field = fjs.stringifyNode(rightSide);
    							field = field.replace("\"", ""); // remove wrapping double-quotes
    	                    }
    	                    write(field + "', ");
                            getWalker().walk(node.getLeftOperandNode());
    	                    write(".plus(");
    	                    getWalker().walk(node.getRightOperandNode());
    	                    write(ASEmitterTokens.PAREN_CLOSE);
    	                    write(ASEmitterTokens.PAREN_CLOSE);
    	                    return;
                    	}
                    	else if (node.getNodeID() == ASTNodeID.Op_AddID)
                    	{
    	                    getWalker().walk(dyn);
    	                    write(".plus(");
    	                    getWalker().walk(node.getRightOperandNode());
    	                    write(ASEmitterTokens.PAREN_CLOSE);
    	                    return;
                    	}
        			}
                }
                else if (((JSRoyaleEmitter)getEmitter()).isProxy(((MemberAccessExpressionNode)leftSide).getLeftOperandNode()) && leftDef == null)
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
                else if (((JSRoyaleEmitter)getEmitter()).isDateProperty((MemberAccessExpressionNode)leftSide, true))
                {
                	specialCaseDate(node, (MemberAccessExpressionNode)leftSide);
                    return;
                }
            }
            else if (leftSide.getNodeID() == ASTNodeID.IdentifierID)
            {
    			if ((leftDef != null)
    				&& SemanticUtils.isXMLish(leftDef, getWalker().getProject()))
    			{
                	if (node.getNodeID() == ASTNodeID.Op_AddAssignID)
                	{
	                    getWalker().walk(leftSide);
	                    write(" = ");
	                    getWalker().walk(leftSide);
	                    write(".plus(");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                }
            }
            else if (leftSide.getNodeID() == ASTNodeID.ArrayIndexExpressionID) // dynamic access
            {
            	DynamicAccessNode dyn = (DynamicAccessNode)leftSide;
            	IExpressionNode dynLeft = dyn.getLeftOperandNode();
            	ITypeDefinition type = dyn.getRightOperandNode().resolveType(getProject());
            	if (((JSRoyaleEmitter)getEmitter()).isXMLish(dynLeft)/* && !SemanticUtils.isNumericType(type, getProject())*/) //type.isInstanceOf("String", getProject())
    			{
            		String field;
                	if (node.getNodeID() == ASTNodeID.Op_AssignId)
                	{
	                    getWalker().walk(dynLeft);
	                    IExpressionNode rightSide = dyn.getRightOperandNode();
	                    if (rightSide instanceof UnaryOperatorAtNode)
	                    {
		                    write(".setAttribute(");
							field = fjs.stringifyNode((rightSide).getChild(0));
	                    }
	                    else
	                    {
							write(".setChild(");
							field = fjs.stringifyNode(rightSide);
	                    }
						if (field.startsWith("\"") && field.endsWith("\"")) {
							// remove wrapping double-quotes and swap to single quotes
							field = "'" + field.substring(1, field.length() - 1) + "'";
						}
	                    write(field + ", ");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                	else if (node.getNodeID() == ASTNodeID.Op_AddAssignID)
                	{
	                    getWalker().walk(dynLeft);
	                    IExpressionNode rightSide = dyn.getRightOperandNode();
	                    if (rightSide instanceof UnaryOperatorAtNode)
	                    {
		                    write(".setAttribute('");
							field = fjs.stringifyNode(((UnaryOperatorAtNode)rightSide).getChild(0));
							field = field.replace("\"", ""); // remove wrapping double-quotes
	                    }
	                    else
	                    {
		                    write(".setChild('");
							field = fjs.stringifyNode(rightSide);
							field = field.replace("\"", ""); // remove wrapping double-quotes
	                    }
	                    write(field + "', ");
                        getWalker().walk(node.getLeftOperandNode());
	                    write(".plus(");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}
                	else if (node.getNodeID() == ASTNodeID.Op_AddID)
                	{
	                    getWalker().walk(dyn);
	                    write(".plus(");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    return;
                	}

    			}
            	else if (((JSRoyaleEmitter)getEmitter()).isProxy(dynLeft))
            	{
            		if (isAssignment)
            		{
	                    getWalker().walk(dynLeft);
	                    IExpressionNode rightSide = dyn.getRightOperandNode();
	                    write(".setProperty(");
	                    getWalker().walk(rightSide);
	                    write(", ");
	                    getWalker().walk(node.getRightOperandNode());
	                    write(ASEmitterTokens.PAREN_CLOSE);
            		}
            		else
            		{
	                    getWalker().walk(dynLeft);
	                    IExpressionNode rightSide = dyn.getRightOperandNode();
	                    write(".getProperty(");
	                    getWalker().walk(rightSide);
	                    write(ASEmitterTokens.PAREN_CLOSE);
	                    write(ASEmitterTokens.SPACE);
	                    writeToken(op);
	                    getWalker().walk(node.getRightOperandNode());
            		}
                    return;            		
            	}
            }
            
			if (id == ASTNodeID.Op_EqualID || id ==ASTNodeID.Op_NotEqualID) {
				//QName == QName
				if (leftDef != null && leftDef.getQualifiedName().equals("QName")) {
					IDefinition rightDef = node.getRightOperandNode().resolveType(getProject());
					if (rightDef != null && rightDef.getQualifiedName().equals("QName")) {
						//handle non-strict equality/inequality a little differently via QName.equality method
						if (id == ASTNodeID.Op_NotEqualID) write("!");
						write("QName.equality(");
						getWalker().walk(node.getLeftOperandNode());
						write(",");
						getWalker().walk(node.getRightOperandNode());
						write(")");
						return;
					}
				} else {
					IASNode codeContext = node.getAncestorOfType(IClassNode.class);
					boolean isFrameworkXML = false;
					if (codeContext instanceof IClassNode) {
						if (((IClassNode) codeContext).getQualifiedName().equals("XML") || ((IClassNode) codeContext).getQualifiedName().equals("XMLList")) {
							//we will ignore the internal code of the emulation support classes for these cases
							isFrameworkXML = true;
						}
					}
					if (!isFrameworkXML) {
						boolean leftIsAny = (leftDef == null || getProject().getBuiltinType(BuiltinType.ANY_TYPE).equals(leftDef));
						IDefinition rightDef = node.getRightOperandNode().resolveType(getProject());
						boolean rightIsAny = (rightDef == null || getProject().getBuiltinType(BuiltinType.ANY_TYPE).equals(rightDef));
						boolean leftIsXMLish = (leftIsAny && SemanticUtils.isXMLish(node.getLeftOperandNode(), getProject())) || SemanticUtils.isXMLish(leftDef, getProject());
						boolean rightIsXMLish = (rightIsAny && SemanticUtils.isXMLish(node.getRightOperandNode(), getProject())) || SemanticUtils.isXMLish(rightDef, getProject());


						if (leftIsXMLish && rightIsXMLish) {


							startMapping(node, node.getLeftOperandNode());
							//handle non-strict equality/inequality for XMLish comparisons a little differently
							if (id == ASTNodeID.Op_NotEqualID) write("!");
							write("XML.equality(");
							endMapping(node);
							getWalker().walk(node.getLeftOperandNode());
							startMapping(node, node.getLeftOperandNode());
							write(",");
							endMapping(node);
							getWalker().walk(node.getRightOperandNode());
							startMapping(node, node.getLeftOperandNode());
							write(")");
							endMapping(node);
							return;
						} else {
							if (leftIsXMLish || rightIsXMLish) {

								IDefinition otherDef = leftIsXMLish ? rightDef : leftDef;
								boolean otherIsBoolean = getProject().getBuiltinType(BuiltinType.BOOLEAN).equals(otherDef);


								if (otherIsBoolean || (leftIsAny && rightIsXMLish) || (rightIsAny && leftIsXMLish)) {

									if (otherIsBoolean) {
										//cover xmlish ==/!= boolean and boolean ==/!= xmlish variations by converting the boolean value to string (xmlishBooleanEqualityOperand):
										if (leftIsXMLish) getWalker().walk(node.getLeftOperandNode());
										else xmlishBooleanEqualityOperand(node.getLeftOperandNode());
										write(" " + node.getOperator().getOperatorText() + " ");
										if (leftIsXMLish) xmlishBooleanEqualityOperand(node.getRightOperandNode());
										else getWalker().walk(node.getRightOperandNode());
										return;
									} else {
										//cover xmlish ==/!= * (typed) and * (typed) ==/!= xmlish variations:
										IExpressionNode lhs = leftIsXMLish ? node.getLeftOperandNode() : node.getRightOperandNode();
										IExpressionNode rhs = leftIsXMLish ? node.getRightOperandNode() : node.getLeftOperandNode();
										//@todo check the source-mapping to the equality/inequality operator, this could probably be improved:
										startMapping(node, node.getLeftOperandNode());
										//handle non-strict equality/inequality for XMLish comparisons a little differently via XML.mixedEquality method
										if (id == ASTNodeID.Op_NotEqualID) write("!");

										write("XML.mixedEquality(");
										endMapping(node);
										getWalker().walk(lhs);
										startMapping(node, node.getLeftOperandNode());
										write(",");
										endMapping(node);
										getWalker().walk(rhs);
										startMapping(node, node.getLeftOperandNode());
										write(")");
										endMapping(node);
										return;
									}
								}
							}
						}
					}
				}
			} else if (id == ASTNodeID.Op_AddID) {
				IDefinition rightDef = node.getRightOperandNode().resolveType(getProject());
				boolean leftIsXMLish = (SemanticUtils.isXMLish(node.getLeftOperandNode(), getProject())) || SemanticUtils.isXMLish(leftDef, getProject());
				boolean rightIsXMLish = (SemanticUtils.isXMLish(node.getRightOperandNode(), getProject())) || SemanticUtils.isXMLish(rightDef, getProject());
				boolean process;
				if (leftIsXMLish) {
					process = !rightIsXMLish;
				} else {
					process = rightIsXMLish;
				}
				if (process) {
					IASNode codeContext = node.getAncestorOfType(IClassNode.class);
					boolean isFrameworkXML = false;
					if (codeContext instanceof IClassNode) {
						if (((IClassNode) codeContext).getQualifiedName().equals("XML") || ((IClassNode) codeContext).getQualifiedName().equals("XMLList")) {
							//we will ignore the internal code of the emulation support classes for these cases
							isFrameworkXML = true;
						}
					}
					if (!isFrameworkXML) {
						IExpressionNode leftOperand = node.getLeftOperandNode();
						IExpressionNode rightOperand = node.getRightOperandNode();
						FunctionCallNode functionCallNode;
						if (leftIsXMLish) {
							//wrap in string coercion
							if (EmitterUtils.xmlRequiresNullCheck((NodeBase) leftOperand, getProject())) {
								//if it is a simple identifier, then it could be a null reference so use the XMLList.coerce_string method, which retains null
								functionCallNode = EmitterUtils.wrapXMLListStringCoercion((NodeBase) leftOperand);
							} else {
								//if it is a member access expression or something else then assume we don't have to check for null
								functionCallNode = EmitterUtils.wrapSimpleStringCoercion((NodeBase) leftOperand);
							}
							functionCallNode.setParent((NodeBase) node);
							leftOperand = functionCallNode;
						} else {
							//wrap in string coercion
							if (EmitterUtils.xmlRequiresNullCheck((NodeBase) rightOperand, getProject())) {
								//if it is a simple identifier, then it could be a null reference so use the XMLList.coerce_string method, which retains null
								functionCallNode = EmitterUtils.wrapXMLListStringCoercion((NodeBase) rightOperand);
							} else {
								//if it is a member access expression or something else then assume we don't have to check for null
								functionCallNode = EmitterUtils.wrapSimpleStringCoercion((NodeBase) rightOperand);
							}
							functionCallNode.setParent((NodeBase) node);
							rightOperand = functionCallNode;
						}
						getWalker().walk(leftOperand);
						write(ASEmitterTokens.SPACE);
						writeToken(ASEmitterTokens.PLUS);
						write(ASEmitterTokens.SPACE);
						getWalker().walk(rightOperand);
						return;
					}
				}

			}
			
		
			super_emitBinaryOperator(node, isAssignment);
        }
    }

	private void xmlishBooleanEqualityOperand(IExpressionNode booleanOperandNode) {
		boolean literalBool = booleanOperandNode.getNodeID() == ASTNodeID.LiteralBooleanID;
		if (literalBool) {
			write("'");
		} else write("('' + ");
		getWalker().walk(booleanOperandNode);
		if (literalBool) {
			write("'");
		} else write(")");
	}
    

    private void super_emitBinaryOperator(IBinaryOperatorNode node, boolean isAssignment)
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
			if (isAssignment
					&& (getProject() instanceof RoyaleJSProject && ((RoyaleJSProject) getProject()).config != null && ((RoyaleJSProject) getProject()).config.getJsVectorEmulationClass() == null)
					&& node.getLeftOperandNode() instanceof MemberAccessExpressionNode
					&& ((MemberAccessExpressionNode) node.getLeftOperandNode()).getRightOperandNode() instanceof IdentifierNode
					&& ((IdentifierNode) ((MemberAccessExpressionNode) node.getLeftOperandNode()).getRightOperandNode()).getName().equals("length")
					&& ((MemberAccessExpressionNode) node.getLeftOperandNode()).getLeftOperandNode().resolveType(getProject()) instanceof AppliedVectorDefinition)
			{
				//for default Vector implementation, when setting length, we need to set it on the associated 'synthType' instance which tags the native
				//Array representation of the Vector. This allows running 'setter' code because it is not possible to override the native length setter on Array
				//unless using a different approach, like es6 Proxy.
				//this code inserts the extra access name for setting length, e.g. myVectInstance['_synthType'].length = assignedValue
				//the dynamic access field name is a constant on Language, so it can be different/shorter in release build
				getWalker().walk(((MemberAccessExpressionNode) node.getLeftOperandNode()).getLeftOperandNode());
				write(ASEmitterTokens.SQUARE_OPEN);
				write(JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken());
				write(ASEmitterTokens.MEMBER_ACCESS);
				write(JSRoyaleEmitterTokens.ROYALE_SYNTH_TAG_FIELD_NAME);
				write(ASEmitterTokens.SQUARE_CLOSE);
				write(ASEmitterTokens.MEMBER_ACCESS);
				getWalker().walk(((MemberAccessExpressionNode) node.getLeftOperandNode()).getRightOperandNode());
			}
			else if (isAssignment && node.getLeftOperandNode() instanceof NamespaceAccessExpressionNode)
			{
				getWalker().walk(node.getLeftOperandNode().getChild(1));
			}
            else getWalker().walk(node.getLeftOperandNode());
            startMapping(node, node.getLeftOperandNode());
			boolean xmlAdd = false;
            if (id == ASTNodeID.Op_AddID && SemanticUtils.isXMLish(node.getLeftOperandNode(),getProject()) && SemanticUtils.isXMLish(node.getRightOperandNode(),getProject())) {
				//we need to use 'plus' method instead of '+'
				xmlAdd = true;
			}
            if (id != ASTNodeID.Op_CommaID && !xmlAdd)
                write(ASEmitterTokens.SPACE);

            // (erikdebruin) rewrite 'a &&= b' to 'a = a && b'
            if (id == ASTNodeID.Op_LogicalAndAssignID
                    || id == ASTNodeID.Op_LogicalOrAssignID)
            {
                IExpressionNode lnode = node
                        .getLeftOperandNode();

                writeToken(ASEmitterTokens.EQUAL);
                endMapping(node);

                getWalker().walk(lnode);

                startMapping(node, node.getLeftOperandNode());
                write(ASEmitterTokens.SPACE);
                write((id == ASTNodeID.Op_LogicalAndAssignID) ? ASEmitterTokens.LOGICAL_AND
                        : ASEmitterTokens.LOGICAL_OR);
            }
            else
            {
				if (xmlAdd) {
					write(".plus(");
				} else {
					write(node.getOperator().getOperatorText());
				}
            }

            write(ASEmitterTokens.SPACE);
            endMapping(node);

			if (isAssignment)
			{
				getEmitter().emitAssignmentCoercion(node.getRightOperandNode(), node.getLeftOperandNode().resolveType(getProject()));
			}
			else
			{
				
				getWalker().walk(node.getRightOperandNode());
				
				if (node.getNodeID() == ASTNodeID.Op_InID &&
						((JSRoyaleEmitter)getEmitter()).isXMLish(node.getRightOperandNode()))
				{
					write(".elementNames()");
				}
				else if (node.getNodeID() == ASTNodeID.Op_InID &&
						((JSRoyaleEmitter)getEmitter()).isProxy(node.getRightOperandNode()))
				{
					//@todo: add loop target null safety (see changes in ForEachEmitter)
					write(".propertyNames()");
				}
			}
			if (xmlAdd) {
				write(")");
			}
        }

        if (ASNodeUtils.hasParenOpen(node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }
    
    public static enum DatePropertiesGetters
    {
    	TIME("time", "getTime"),
    	FULLYEAR("fullYear", "getFullYear"),
    	MONTH("month", "getMonth"),
    	DATE("date", "getDate"),
    	DAY("day", "getDay"),
    	FULLYEARUTC("fullYearUTC", "getUTCFullYear"),
    	MONTHUTC("monthUTC", "getUTCMonth"),
    	DATEUTC("dateUTC", "getUTCDate"),
    	DAYUTC("dayUTC", "getUTCDay"),
    	HOURS("hours", "getHours"),
    	MINUTES("minutes", "getMinutes"),
    	SECONDS("seconds", "getSeconds"),
    	MILLISECONDS("milliseconds", "getMilliseconds"),
    	HOURSUTC("hoursUTC", "getUTCHours"),
    	MINUTESUTC("minutesUTC", "getUTCMinutes"),
    	SECONDSUTC("secondsUTC", "getUTCSeconds"),
    	MILLISECONDSUTC("millisecondsUTC", "getUTCMilliseconds"),
    	TIMEZONEOFFSET("timezoneOffset", "getTimezoneOffset");
    	
    	DatePropertiesGetters(String value, String functionName)
    	{
    		this.value = value;
    		this.functionName = functionName;
    	}
    	
    	private String value;
    	private String functionName;
    	
    	public String getFunctionName()
    	{
    		return functionName;
    	}
    	
    	public String getValue()
    	{
    		return value;
    	}
    }
    
    public static enum DatePropertiesSetters
    {
    	TIME("time", "setTime"),
    	FULLYEAR("fullYear", "setFullYear"),
    	MONTH("month", "setMonth"),
    	DATE("date", "setDate"),
    	DAY("day", "setDay"),
    	FULLYEARUTC("fullYearUTC", "setUTCFullYear"),
    	MONTHUTC("monthUTC", "setUTCMonth"),
    	DATEUTC("dateUTC", "setUTCDate"),
    	DAYUTC("day", "setUTCDay"),
    	HOURS("hours", "setHours"),
    	MINUTES("minutes", "setMinutes"),
    	SECONDS("seconds", "setSeconds"),
    	MILLISECONDS("milliseconds", "setMilliseconds"),
    	HOURSUTC("hoursUTC", "setUTCHours"),
    	MINUTESUTC("minutesUTC", "setUTCMinutes"),
    	SECONDSUTC("secondsUTC", "setUTCSeconds"),
    	MILLISECONDSUTC("millisecondsUTC", "setUTCMilliseconds");
    	
    	DatePropertiesSetters(String value, String functionName)
    	{
    		this.value = value;
    		this.functionName = functionName;
    	}
    	
    	private String value;
    	private String functionName;
    	
    	public String getFunctionName()
    	{
    		return functionName;
    	}
    	
    	public String getValue()
    	{
    		return value;
    	}
    }
    
    void specialCaseDate(IBinaryOperatorNode node, MemberAccessExpressionNode leftSide)
    {
        if (ASNodeUtils.hasParenOpen(node))
		{
            write(ASEmitterTokens.PAREN_OPEN);
		}

    	MemberAccessExpressionNode dateNode = (MemberAccessExpressionNode)leftSide;
        IIdentifierNode rightSide = (IIdentifierNode)dateNode.getRightOperandNode();
        String op = node.getOperator().getOperatorText();
        boolean isAssignment = op.contains("=")
                && !op.contains("==")
                && !(op.startsWith("<") || op.startsWith(">") || op
                        .startsWith("!"));
		boolean assignmentNeedsGetter = isAssignment && !(node.getParent() instanceof IBlockNode);
		if (assignmentNeedsGetter)
		{
            write(ASEmitterTokens.PAREN_OPEN);
		}
        getWalker().walk(dateNode.getLeftOperandNode());
        String rightName = rightSide.getName();
        if (isAssignment)
        {
            DatePropertiesSetters prop = DatePropertiesSetters.valueOf(rightName.toUpperCase());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(prop.getFunctionName());
	        write(ASEmitterTokens.PAREN_OPEN);
	        if (op.length() > 1)
	        {
	            DatePropertiesGetters propGetter = DatePropertiesGetters.valueOf(rightName.toUpperCase());
	            getWalker().walk(dateNode.getLeftOperandNode());
	            write(ASEmitterTokens.MEMBER_ACCESS);
	            write(propGetter.getFunctionName());
		        write(ASEmitterTokens.PAREN_OPEN);
		        write(ASEmitterTokens.PAREN_CLOSE);
	        	write(ASEmitterTokens.SPACE);
	        	write(op.substring(0, 1));
	        	write(ASEmitterTokens.SPACE);
	        }
	        getWalker().walk(node.getRightOperandNode());
	        write(ASEmitterTokens.PAREN_CLOSE);
			if (assignmentNeedsGetter)
			{
				write(ASEmitterTokens.COMMA);
	        	write(ASEmitterTokens.SPACE);
				getWalker().walk(dateNode.getLeftOperandNode());
				DatePropertiesGetters propGetter = DatePropertiesGetters.valueOf(rightName.toUpperCase());
				write(ASEmitterTokens.MEMBER_ACCESS);
				write(propGetter.getFunctionName());
				write(ASEmitterTokens.PAREN_OPEN);
				write(ASEmitterTokens.PAREN_CLOSE);
			}
        }
        else
        {
            DatePropertiesGetters propGetter = DatePropertiesGetters.valueOf(rightName.toUpperCase());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(propGetter.getFunctionName());
	        write(ASEmitterTokens.PAREN_OPEN);
	        write(ASEmitterTokens.PAREN_CLOSE);
        	write(ASEmitterTokens.SPACE);
        	write(op);
        	write(ASEmitterTokens.SPACE);
	        getWalker().walk(node.getRightOperandNode());
        }
		if (assignmentNeedsGetter)
		{
            write(ASEmitterTokens.PAREN_CLOSE);
		}
        if (ASNodeUtils.hasParenOpen(node))
		{
            write(ASEmitterTokens.PAREN_CLOSE);
		}
    }
}
