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
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.*;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.jx.BinaryOperatorEmitter.DatePropertiesGetters;
import org.apache.royale.compiler.internal.definitions.AccessorDefinition;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.scopes.FunctionScope;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.tree.as.IOperatorNode.OperatorType;
import org.apache.royale.compiler.tree.mxml.IMXMLSingleDataBindingNode;
import org.apache.royale.compiler.utils.ASNodeUtils;

import java.util.ArrayList;

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

        IExpressionNode leftNode = node.getLeftOperandNode();
        IASNode rightNode = node.getRightOperandNode();

    	JSRoyaleEmitter fjs = (JSRoyaleEmitter)getEmitter();
        if (fjs.isDateProperty(node, false))
        {
    		writeLeftSide(node, leftNode, rightNode);
            String rightName = ((IIdentifierNode)rightNode).getName();
            DatePropertiesGetters propGetter = DatePropertiesGetters.valueOf(rightName.toUpperCase());
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(propGetter.getFunctionName());
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.PAREN_CLOSE);
    		return;
        }
        IDefinition def = node.resolve(getProject());
        //extra check to cope with e4x member access identifier nodes that resolve
		//to instance member function definitions
		//but should not be interpreted as such. e.g. xml.child.descendant
		//should be output as xml.child('child').child('descendant')
		//we also need to check we are not currently compiling the XML or XMLList class to avoid any
		//possible internal references being treated incorrectly
        boolean forceXmlCheck =(def != null
				&& node.getRightOperandNode().getNodeID() == ASTNodeID.IdentifierID
				&& SemanticUtils.isXMLish(def.getParent(), getProject())
				&& def instanceof IFunctionDefinition
				&& !def.isStatic()
				&& !(getModel().getCurrentClass() != null && (getModel().getCurrentClass().getQualifiedName().equals("XML") || getModel().getCurrentClass().getQualifiedName().equals("XMLList")))
		);
        if (def == null || forceXmlCheck)
        {
        	IASNode parentNode = node.getParent();
        	// could be XML
        	boolean isXML = false;
        	boolean isProxy = false;
        	if (leftNode instanceof MemberAccessExpressionNode)
        		isXML = fjs.isLeftNodeXMLish(leftNode);
        	else if (leftNode != null)
        		isXML = fjs.isXMLish(leftNode);

			if (!isXML) {
				if (leftNode instanceof MemberAccessExpressionNode)
					isProxy = fjs.isProxy(leftNode);
				else if (leftNode instanceof IExpressionNode)
					isProxy = fjs.isProxy((IExpressionNode)leftNode);
			}
			
        	if (isXML)
        	{
        		boolean descendant = (node.getOperator() == OperatorType.DESCENDANT_ACCESS);
        		boolean child = !descendant && (node.getOperator() == OperatorType.MEMBER_ACCESS) &&
        							(!(parentNode instanceof FunctionCallNode)) &&
        							rightNode.getNodeID() != ASTNodeID.Op_AtID &&
        							!((rightNode.getNodeID() == ASTNodeID.ArrayIndexExpressionID) && 
        									(((DynamicAccessNode)rightNode).getLeftOperandNode().getNodeID() == ASTNodeID.Op_AtID));
        		if (descendant || child) {
					writeLeftSide(node, leftNode, rightNode);
					if (descendant)
						write(".descendants(");
					if (child)
						write(".child(");
					String closeMethodCall = "')";
					String s = "";
					boolean isNamespaceAccessNode = rightNode instanceof INamespaceAccessExpressionNode;
					ArrayList<IDefinition> usedNamespaceDefs = null;
					if (!isNamespaceAccessNode) {
						//check for open namespaces
						NamespaceDefinition.INamespaceDirective item = ((NodeBase) node).getASScope().getFirstNamespaceDirective();
						while(item != null) {
							if (item instanceof NamespaceDefinition.IUseNamespaceDirective) {
								
								INamespaceDefinition itemDef = ((NamespaceDefinition.IUseNamespaceDirective) item).resolveNamespaceReference(getProject());
								if (itemDef == null) {
									//@todo - either resolve this or make it an actual Warning.
								//	System.out.println("Ambiguous 'use namespace "+((NamespaceDefinition.IUseNamespaceDirective) item).getBaseName()+ "', probably conflicts with local var name:"+node.getSourcePath()+":"+node.getLine()+":"+node.getColumn());
									IDefinition lookupDef = ((NodeBase) node).getASScope().findProperty(getProject(), ((NamespaceDefinition.IUseNamespaceDirective) item).getBaseName(), DependencyType.NAMESPACE);
									if (lookupDef instanceof IVariableDefinition) {
										//it seems that swf ignores this too...adding it in creates a different result
										/*if (usedNamespaceDefs == null) {
											usedNamespaceDefs = new ArrayList<IDefinition>();
										}
										
										if (!usedNamespaceDefs.contains(lookupDef)) {
											usedNamespaceDefs.add(lookupDef);
										}*/
									}
									
								} else {
									if (usedNamespaceDefs == null) {
										usedNamespaceDefs = new ArrayList<IDefinition>();
									}
									if (!usedNamespaceDefs.contains(itemDef)) usedNamespaceDefs.add(itemDef);
								}
							}
							item = item.getNext();
						}
					}
					
					if (isNamespaceAccessNode || usedNamespaceDefs != null) {
						if (isNamespaceAccessNode) {
							NamespaceIdentifierNode namespaceIdentifierNode = (NamespaceIdentifierNode) ((INamespaceAccessExpressionNode) rightNode).getLeftOperandNode();
							IDefinition nsDef =  namespaceIdentifierNode.resolve(getProject());
							if (nsDef instanceof INamespaceDefinition
									&& ((INamespaceDefinition)nsDef).getNamespaceClassification().equals(INamespaceDefinition.NamespaceClassification.LANGUAGE)) {
								//deal with built-ins
								String name = ((NamespaceIdentifierNode) ((INamespaceAccessExpressionNode) rightNode).getLeftOperandNode()).getName();
								if (name.equals(INamespaceConstants.ANY)) {
									//let the internal support within 'QName' class deal with it
									write("new QName(null,'");
									//only stringify the right node at the next step (it is the localName part)
									rightNode = ((INamespaceAccessExpressionNode) rightNode).getRightOperandNode();
									closeMethodCall = "'))";
								} else if (name.equals(IASKeywordConstants.PUBLIC)
										|| name.equals(IASKeywordConstants.PROTECTED)) {
									//@todo check this, but both public and protected appear to have the effect of skipping the namespace part in swf, so just use default namespace
									write("/* as3 " + name + " */ '");
									//skip the namespace to just output the name
									rightNode = ((INamespaceAccessExpressionNode) rightNode).getRightOperandNode();
								} else {
									//this is an unlikely condition, but do something that should give same results as swf...
									//private, internal namespaces used in an XML context (I don't think this makes sense, but is possible to do in code)
									//@todo check this, but it seems like it should never match anything in a valid XML query
									write("new QName('");
									//provide an 'unlikely' 'uri':
									write("_as3Lang_" + fjs.stringifyNode(namespaceIdentifierNode));
									write(s + "','");
									//only stringify the right node at the next step (it is the localName part)
									rightNode = ((INamespaceAccessExpressionNode) rightNode).getRightOperandNode();
									closeMethodCall = "'))";
								}
							} else {
								write("new QName(");
								s = fjs.stringifyNode(namespaceIdentifierNode);
								write(s + ",'");
								//only stringify the right node at the next step (it is the localName part)
								rightNode = ((INamespaceAccessExpressionNode) rightNode).getRightOperandNode();
								closeMethodCall = "'))";
							}
						} else {
							//use a special MultiQName compiler support method
							//to simulate a MultiName for the used namespaces (which includes 'no namespace')
							write("XML.multiQName([");
							int count = 0;
							for (IDefinition nsDef:usedNamespaceDefs) {
								if (count > 0) write(",");
								if (nsDef instanceof INamespaceDefinition) {
									write("'"+((INamespaceDefinition)nsDef).getURI()+"'");
								} else {
									String varName = getEmitter().stringifyNode(((IVariableDefinition) nsDef).getVariableNode().getNameExpressionNode());
									write(varName);
								}
								count++;
							}
							write("]");
							write(", '");
							closeMethodCall = "'))";
						}
					} else if (getModel().defaultXMLNamespaceActive
						&& ((MemberAccessExpressionNode) node).getASScope() instanceof FunctionScope
						&& getModel().getDefaultXMLNamespace((FunctionScope)((MemberAccessExpressionNode) node).getASScope()) != null) {
						//new QName('contextualDefaultNameSpace','originalValueHere')
						write("new QName(");
						getEmitter().getWalker().walk(getModel().getDefaultXMLNamespace((FunctionScope)((MemberAccessExpressionNode) node).getASScope()));
						write(",'");
						closeMethodCall = "'))";
					} else {
						//regular string value
						write("'"); //normal string name for child
					}
					
			
					s = fjs.stringifyNode(rightNode);
					int dot = s.indexOf('.');
					if (dot != -1) {
						String name = s.substring(0, dot);
						String afterDot = s.substring(dot);
						write(name);
						write(closeMethodCall);
						write(afterDot);
					} else {
						write(s);
						write(closeMethodCall);
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
				//exception: variable member access needs to have literal output, because there is no guarantee that string access will work in release mode after renaming
				if (((NamespaceAccessExpressionNode) rightNode).resolve(getProject()) instanceof IVariableDefinition) {
					write(JSRoyaleEmitter.formatNamespacedProperty(d.toString(), r.getName(),true));
				} else {
					write(ASEmitterTokens.SQUARE_OPEN);
					write(ASEmitterTokens.NEW);
					write(ASEmitterTokens.SPACE);
					write(IASLanguageConstants.QName);
					write(ASEmitterTokens.PAREN_OPEN);
					write(fjs.formatQualifiedName(d.getQualifiedName()));
					write(ASEmitterTokens.COMMA);
					write(ASEmitterTokens.SPACE);
					write(ASEmitterTokens.SINGLE_QUOTE);
					write(r.getName());
					write(ASEmitterTokens.SINGLE_QUOTE);
					write(ASEmitterTokens.PAREN_CLOSE);
					write(".objectAccessFormat()");
					write(ASEmitterTokens.SQUARE_CLOSE);
				}
        		return;
        	}
        }
		else if(def.getParent() instanceof IPackageDefinition)
		{
			//this is a fully qualified name, and we should output it directly
			//because we don't want it to be treated as dynamic access
			write(fjs.formatQualifiedName(def.getQualifiedName()));
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
			
			if (needClosure
					&& getEmitter().getDocEmitter() instanceof JSRoyaleDocEmitter
					&& ((JSRoyaleDocEmitter)getEmitter().getDocEmitter()).getSuppressClosure())
				needClosure = false;
        	if (needClosure)
        		getEmitter().emitClosureStart();

    		NamespaceAccessExpressionNode naen = (NamespaceAccessExpressionNode)rightNode;
    		IDefinition d = naen.getLeftOperandNode().resolve(getProject());
    		IdentifierNode r = (IdentifierNode)(naen.getRightOperandNode());
    		// output bracket access with QName
    		writeLeftSide(node, leftNode, rightNode);
    		if (!d.getBaseName().equals(ASEmitterTokens.PRIVATE.getToken()))
    		{
				//exception: variable member access needs to have literal output, because there is no guarantee that string access will work in release mode after renaming
    			if (naen.resolve(getProject()) instanceof IVariableDefinition) {
					write(JSRoyaleEmitter.formatNamespacedProperty(d.toString(), r.getName(),true));
				} else {
					write(ASEmitterTokens.SQUARE_OPEN);
					write(ASEmitterTokens.NEW);
					write(ASEmitterTokens.SPACE);
					write(IASLanguageConstants.QName);
					write(ASEmitterTokens.PAREN_OPEN);
					write(fjs.formatQualifiedName(d.getQualifiedName()));
					write(ASEmitterTokens.COMMA);
					write(ASEmitterTokens.SPACE);
					write(ASEmitterTokens.SINGLE_QUOTE);
					write(r.getName());
					write(ASEmitterTokens.SINGLE_QUOTE);
					write(ASEmitterTokens.PAREN_CLOSE);
					write(".objectAccessFormat()");
					write(ASEmitterTokens.SQUARE_CLOSE);
				}
    		}
    		else
    		{
                write(node.getOperator().getOperatorText());
	    		write(r.getName());    			
    		}
        
			if (needClosure)
			{
				write(ASEmitterTokens.COMMA);
				write(ASEmitterTokens.SPACE);
				if (leftNode.getNodeID() == ASTNodeID.SuperID)
					write(ASEmitterTokens.THIS);
				else
					writeLeftSide(node, leftNode, rightNode);
				getEmitter().emitClosureEnd(leftNode, def);
			}
    		return;
		}
        boolean isCustomNamespace = false;
        if (def instanceof FunctionDefinition && node.getOperator() == OperatorType.MEMBER_ACCESS)
        	isCustomNamespace = fjs.isCustomNamespace((FunctionDefinition)def);
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

				//If binding getterFunctions ever need closures, this seems to be where it would be done (so far not needed, @todo review and remove this when certain)
				/*if (!needClosure && !isStatic && parentNodeId == ASTNodeID.FunctionCallID) {
					if (node.getParent().getParent() instanceof IMXMLSingleDataBindingNode) {
						needClosure = true;
					}
				}*/
		
				if (needClosure
						&& getEmitter().getDocEmitter() instanceof JSRoyaleDocEmitter
						&& ((JSRoyaleDocEmitter)getEmitter().getDocEmitter()).getSuppressClosure())
					needClosure = false;
        		
        	}
        }

        boolean continueWalk = true;
        if (!isStatic)
        {
        	if (needClosure)
        		getEmitter().emitClosureStart();
        	
        	continueWalk = writeLeftSide(node, leftNode, rightNode);
        }

        if (continueWalk)
        {
			boolean emitDynamicAccess = false;
            boolean dynamicAccessUnknownMembers = false;
            ICompilerProject project = getProject();
            if(project instanceof RoyaleJSProject)
            {
                RoyaleJSProject fjsProject = (RoyaleJSProject) project;
                if(fjsProject.config != null)
                {
                    dynamicAccessUnknownMembers = fjsProject.config.getJsDynamicAccessUnknownMembers();
                }
                if (!dynamicAccessUnknownMembers) {
                	//for <fx:Object declarations in mxml, we need to do this by default, because initialization values are set this way already, as are destination bindings, for example.
					IIdentifierNode checkNode = null;
					if (leftNode instanceof IIdentifierNode) {
						//we might be dealing with the direct child member access of an fx:Object
						checkNode = (IIdentifierNode) leftNode;
					} else {
						if (leftNode instanceof MemberAccessExpressionNode) {
							//if we are nested, check upwards for topmost Identifier node and verify that it is mxml variable of type Object, verifying that we are considered 'untyped' along the way
							MemberAccessExpressionNode mae = (MemberAccessExpressionNode) leftNode;
							while (mae != null) {
								if (mae.getRightOperandNode().resolve(getProject()) == null) {
									if (mae.getLeftOperandNode() instanceof IIdentifierNode) {
										checkNode = (IIdentifierNode) mae.getLeftOperandNode();
										break;
									} else if (mae.getLeftOperandNode() instanceof MemberAccessExpressionNode) {
										mae = (MemberAccessExpressionNode) mae.getLeftOperandNode();
									} else {
										mae = null;
									}
								} else mae = null;
							}
						}
					}

					if (checkNode != null &&
							checkNode.resolve(getProject()) instanceof VariableDefinition) {
						VariableDefinition varDef = ((VariableDefinition) (checkNode.resolve(getProject())));
						if (varDef.isMXMLDeclared()) {
							IDefinition type = varDef.resolveType(getProject());
							if (type instanceof IClassDefinition && type.equals(getProject().getBuiltinType(IASLanguageConstants.BuiltinType.OBJECT))) {
								dynamicAccessUnknownMembers = true;
							}
						}
					}
				}
            }
			if (dynamicAccessUnknownMembers && rightNode instanceof IIdentifierNode)
			{
				IIdentifierNode identifierNode = (IIdentifierNode) node.getRightOperandNode();
				IDefinition resolvedDefinition = identifierNode.resolve(getProject());
				emitDynamicAccess = resolvedDefinition == null;
			}
			if (emitDynamicAccess)
			{
				IIdentifierNode identifierNode = (IIdentifierNode) node.getRightOperandNode();
				startMapping(node, rightNode);
				write(ASEmitterTokens.SQUARE_OPEN);
				write(ASEmitterTokens.DOUBLE_QUOTE);
				write(identifierNode.getName());
				write(ASEmitterTokens.DOUBLE_QUOTE);
				write(ASEmitterTokens.SQUARE_CLOSE);
				endMapping(node);
			}
			else
			{
				if (!isStatic && !isCustomNamespace)
				{
					startMapping(node, node.getLeftOperandNode());
					write(node.getOperator().getOperatorText());
					endMapping(node);
				}
				getWalker().walk(node.getRightOperandNode());
			}
        }
        
        if (needClosure)
        {
        	write(ASEmitterTokens.COMMA);
        	write(ASEmitterTokens.SPACE);
        	if (leftNode.getNodeID() == ASTNodeID.SuperID)
        		write(ASEmitterTokens.THIS);
        	else
        		writeLeftSide(node, leftNode, rightNode);
        	getEmitter().emitClosureEnd(node, def);
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
                write(getEmitter().formatQualifiedName(
                        getEmitter().getModel().getCurrentClass().getQualifiedName()));
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSGoogEmitterTokens.SUPERCLASS);
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSRoyaleEmitterTokens.GETTER_PREFIX);
                if (rightDef != null)
                    write(rightDef.getBaseName());
                else
                    write(((GetterNode) rightNode).getName());
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSEmitterTokens.APPLY);
                write(ASEmitterTokens.PAREN_OPEN);
                write(ASEmitterTokens.THIS);
                write(ASEmitterTokens.PAREN_CLOSE);
                return false;
            }
            else if (leftNode.getNodeID() == ASTNodeID.SuperID
                    && (rightDef != null && rightDef instanceof FunctionDefinition))
            {
                write(getEmitter().formatQualifiedName(
                        getEmitter().getModel().getCurrentClass().getQualifiedName()));
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSGoogEmitterTokens.SUPERCLASS);
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(rightDef.getBaseName());
                return false;
            }
        }
        else
        {
            startMapping(leftNode);
            write(ASEmitterTokens.THIS);
            endMapping(leftNode);
        }
        return true;
    }
    	
}
