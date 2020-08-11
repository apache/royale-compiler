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

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.*;
import org.apache.royale.compiler.definitions.IFunctionDefinition.FunctionClassification;
import org.apache.royale.compiler.definitions.IVariableDefinition.VariableClassification;
import org.apache.royale.compiler.definitions.references.INamespaceResolvedReference;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.definitions.*;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.tree.as.NonResolvingIdentifierNode;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.utils.NativeUtils;

public class IdentifierEmitter extends JSSubEmitter implements
        ISubEmitter<IIdentifierNode>
{

    public IdentifierEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IIdentifierNode node)
    {
    	if (node instanceof NonResolvingIdentifierNode)
    	{
            startMapping(node);
    		write(node.getName());
            endMapping(node);
    		return;
    	}
        IDefinition nodeDef = ((IIdentifierNode) node).resolve(getProject());

        IASNode parentNode = node.getParent();
        ASTNodeID parentNodeId = parentNode.getNodeID();
        IASNode grandparentNode = parentNode.getParent();
  //      ASTNodeID grandparentNodeId = (parentNode != null) ? grandparentNode.getNodeID() : null;

        boolean identifierIsAccessorFunction = nodeDef instanceof AccessorDefinition;
        boolean identifierIsPlainFunction = nodeDef instanceof FunctionDefinition
                && !identifierIsAccessorFunction;
        boolean emitName = true;
        boolean wroteMemberAccess = false;
    	JSRoyaleEmitter fjs = (JSRoyaleEmitter)getEmitter();
        RoyaleJSProject project = (RoyaleJSProject)getWalker().getProject();
    	boolean isCustomNamespace = false;
    	boolean isStatic = nodeDef != null && nodeDef.isStatic();
        if (nodeDef instanceof FunctionDefinition &&
          	  fjs.isCustomNamespace((FunctionDefinition)nodeDef))
              isCustomNamespace = true;

        if (isStatic
                && nodeDef instanceof IConstantDefinition
                && project != null && project.config != null
                && project.config.getInlineConstants())
        {
            IConstantDefinition constDef = (IConstantDefinition) nodeDef;
            Object initialValue = constDef.resolveInitialValue(project);
            if (initialValue != null)
            {
                startMapping(parentNode);
                if(initialValue instanceof String)
                {
                    write(ASEmitterTokens.DOUBLE_QUOTE);
                    write((String) initialValue);
                    write(ASEmitterTokens.DOUBLE_QUOTE);
                }
                else if(initialValue == ABCConstants.UNDEFINED_VALUE)
                {
                    write(IASLanguageConstants.UNDEFINED);
                }
                else if(initialValue == ABCConstants.NULL_VALUE)
                {
                    write(IASLanguageConstants.NULL);
                }
                else
                {
                    write(initialValue.toString());
                }
                endMapping(parentNode);
                return;
            }
        }
        if (isStatic)
        {
            String sname = nodeDef.getParent().getQualifiedName();
            if (sname.equals("Array"))
            {
            	String baseName = nodeDef.getBaseName();
            	if (baseName.equals("CASEINSENSITIVE"))
            	{
                    startMapping(parentNode);
            		write("1");
                    endMapping(parentNode);
            		return;
            	}
            	else if (baseName.equals("DESCENDING"))
            	{
                    startMapping(parentNode);
            		write("2");
                    endMapping(parentNode);
            		return;
            	}
            	else if (baseName.equals("UNIQUESORT"))
            	{
                    startMapping(parentNode);
            		write("4");
                    endMapping(parentNode);
            		return;
            	}
            	else if (baseName.equals("RETURNINDEXEDARRAY"))
            	{
                    startMapping(parentNode);
            		write("8");
                    endMapping(parentNode);
            		return;
            	}
            	else if (baseName.equals("NUMERIC"))
            	{
                    startMapping(parentNode);
            		write("16");
                    endMapping(parentNode);
            		return;
            	}
            }
            else if (sname.equals("int"))
            {
            	String baseName = nodeDef.getBaseName();
            	if (baseName.equals("MAX_VALUE"))
            	{
                    startMapping(parentNode);
            		write("2147483648");
                    endMapping(parentNode);
            		return;
            	}
            	else if (baseName.equals("MIN_VALUE"))
            	{
                    startMapping(parentNode);
            		write("-2147483648");
                    endMapping(parentNode);
            		return;
            	}
            }
            else if (sname.equals("uint"))
            {
            	String baseName = nodeDef.getBaseName();
            	if (baseName.equals("MAX_VALUE"))
            	{
                    startMapping(parentNode);
            		write("4294967295");
                    endMapping(parentNode);
            		return;
            	}
            	else if (baseName.equals("MIN_VALUE"))
            	{
                    startMapping(parentNode);
            		write("0");
                    endMapping(parentNode);
            		return;
            	}
            }
            if (sname.length() > 0)
            {
                IASNode prevSibling = parentNode.getChild(0);
                if(prevSibling == node)
                {
                    startMapping(parentNode);
                }
                else
                {
                    startMapping(prevSibling);
                }
                write(getEmitter().formatQualifiedName(sname));
                if(prevSibling != node)
                {
                    endMapping(prevSibling);
                    startMapping(parentNode, prevSibling);
                }
                if (!isCustomNamespace) {
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    wroteMemberAccess = true;
                }
                
                endMapping(parentNode);
            }
        }
        else if (!NativeUtils.isNative(node.getName()))
        {
            // an instance method not in a function call or member access
            boolean generateClosure = identifierIsPlainFunction && ((FunctionDefinition) nodeDef)
                    .getFunctionClassification() == FunctionClassification.CLASS_MEMBER &&
                    (!(parentNodeId == ASTNodeID.FunctionCallID || parentNodeId == ASTNodeID.MemberAccessExpressionID));
            if (generateClosure)
            {
                getEmitter().emitClosureStart();
            }

            boolean wroteSelf = false;
            if (EmitterUtils.writeThis(getProject(), getModel(), node))
            {
                IFunctionObjectNode functionObjectNode = (IFunctionObjectNode) node
                        .getParent().getAncestorOfType(
                                IFunctionObjectNode.class);

                IFunctionNode functionNode = (IFunctionNode) node
                .getParent().getAncestorOfType(
                        IFunctionNode.class);
                IFunctionDefinition functionDef = null;
                if (functionNode != null)
                	functionDef = functionNode.getDefinition();

                startMapping(node);
                if (functionObjectNode != null)
                {
                    write(JSGoogEmitterTokens.SELF);
                    wroteSelf = true;
                }
                else if (functionNode != null && functionDef.getFunctionClassification() == FunctionClassification.LOCAL)
                {
                    write(JSGoogEmitterTokens.SELF);
                    wroteSelf = true;
                }
                else
                    write(ASEmitterTokens.THIS);

                if (!isCustomNamespace) {
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    wroteMemberAccess = true;
                }
                endMapping(node);
            }
            else if (EmitterUtils.writeE4xFilterNode(getProject(), getModel(), node))
            {
            	write("node.");
            }

            if (generateClosure)
            {
                if (isCustomNamespace)
                {
                	Namespace ns = (Namespace)((INamespaceResolvedReference)((FunctionDefinition)nodeDef).getNamespaceReference()).resolveAETNamespace(getProject());
                	INamespaceDefinition nsDef = ((FunctionDefinition)nodeDef).getNamespaceReference().resolveNamespaceReference(getProject());
        			fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
                	String nsName = ns.getName();
                	write(JSRoyaleEmitter.formatNamespacedProperty(nsName, node.getName(), true));
                }
                else
                {
            		String qname = node.getName();
                	if (nodeDef != null && !isStatic && (!(nodeDef instanceof IParameterDefinition)) && nodeDef.isPrivate() && getProject().getAllowPrivateNameConflicts())
                		qname = getEmitter().formatPrivateName(nodeDef.getParent().getQualifiedName(), qname);
            		write(qname);
                }

                writeToken(ASEmitterTokens.COMMA);
                if (wroteSelf)
                    write(JSGoogEmitterTokens.SELF);
                else
                	write(ASEmitterTokens.THIS);
                getEmitter().emitClosureEnd(node, nodeDef);
                emitName = false;
            }
        }
        
        if (emitName)
        {
            boolean accessWithNS = !wroteMemberAccess;
            if (nodeDef != null)
            {
                // this can be optimized but this way lets
                // us breakpoint on the node.getName() to make
                // sure it is ok to always use the short name in an MAE
                String qname = nodeDef.getQualifiedName();
                boolean isPackageOrFileMember = false;
                if (nodeDef instanceof IVariableDefinition)
                {
                    IVariableDefinition variable = (IVariableDefinition) nodeDef;
                    VariableClassification classification = variable.getVariableClassification();
                    if (classification == VariableClassification.PACKAGE_MEMBER ||
                            classification == VariableClassification.FILE_MEMBER)
                    {
                        isPackageOrFileMember = true;
                    }
                    String ns = nodeDef.getNamespaceReference().getBaseName();
                    isCustomNamespace = EmitterUtils.isCustomNamespace(ns);
                    if (isCustomNamespace && parentNode instanceof IMemberAccessExpressionNode
                            && ((IMemberAccessExpressionNode) parentNode).getRightOperandNode() == node) {
                        accessWithNS = true;
                        boolean wasCustomNamespace = false;
                        //duplicate some checks in MemberAccessEmitter to avoid double output of MEMBER_ACCESS
                        if (nodeDef instanceof IAccessorDefinition && ((IMemberAccessExpressionNode) parentNode).getOperator() == IOperatorNode.OperatorType.MEMBER_ACCESS)
                            wasCustomNamespace = fjs.isCustomNamespace((FunctionDefinition)nodeDef);
                        
                        if (!wasCustomNamespace) {
                            //we already wrote member access before walking to this node.
                            accessWithNS = false;
                        }
                    }
                }
                else if (nodeDef instanceof IFunctionDefinition)
                {
                    IFunctionDefinition func = (IFunctionDefinition) nodeDef;
                    FunctionClassification classification = func.getFunctionClassification();
                    if (classification == FunctionClassification.PACKAGE_MEMBER ||
                            classification == FunctionClassification.FILE_MEMBER)
                    {
                        isPackageOrFileMember = true;
                    }
                }
                boolean needsFormattedName = false;
                if (isPackageOrFileMember && parentNodeId == ASTNodeID.MemberAccessExpressionID)
                {
                    IMemberAccessExpressionNode parentMemberAccessNode = (IMemberAccessExpressionNode) parentNode;
                    //if the package or file member isn't on the left side of a
                    //member access expression, it shouldn't be fully qualified
                    needsFormattedName = parentMemberAccessNode.getLeftOperandNode() == node;
                }
                startMapping(node);
                if (parentNodeId == ASTNodeID.MemberAccessExpressionID)
                {
                	if (needsFormattedName)
                	{
                	    write(getEmitter().formatQualifiedName(qname));
                	}
                    else if (isCustomNamespace)
                    {
                    	String ns = ((INamespaceResolvedReference)(nodeDef.getNamespaceReference())).resolveAETNamespace(getProject()).getName();
                    	write(JSRoyaleEmitter.formatNamespacedProperty(ns, qname, accessWithNS));
                    }
                	else
                	{
                	    if (!(nodeDef.getParent() instanceof IPackageDefinition)) {
                            qname = node.getName();
                            if (nodeDef != null && !isStatic && (nodeDef.getParent() instanceof ClassDefinition) && (!(nodeDef instanceof IParameterDefinition)) && nodeDef.isPrivate() && getProject().getAllowPrivateNameConflicts())
                                qname = getEmitter().formatPrivateName(nodeDef.getParent().getQualifiedName(), qname);
                        }
                    	write(qname);
                	}
                }
                else if (isPackageOrFileMember)
                    write(getEmitter().formatQualifiedName(qname));
                else if (nodeDef instanceof TypeDefinitionBase)
                {
                    if (NativeUtils.isSyntheticJSType(qname) && !(parentNode instanceof IFunctionCallNode)) {
                        getEmitter().getModel().needLanguage = true;
                        write(JSRoyaleEmitterTokens.SYNTH_TYPE);
                        write(ASEmitterTokens.PAREN_OPEN);
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        write(getEmitter().formatQualifiedName(qname));
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        write(ASEmitterTokens.PAREN_CLOSE);
                    }
                    else write(getEmitter().formatQualifiedName(qname));
                }
                else if (isCustomNamespace)
                {
                	String ns = ((INamespaceResolvedReference)nodeDef.getNamespaceReference()).resolveAETNamespace(getProject()).getName();
                	write(JSRoyaleEmitter.formatNamespacedProperty(ns, qname, accessWithNS));
                }
                else
                {
                	if (nodeDef != null && !isStatic && (nodeDef.getParent() instanceof ClassDefinition) && (!(nodeDef instanceof IParameterDefinition)) && nodeDef.isPrivate() && getProject().getAllowPrivateNameConflicts())
                		qname = getEmitter().formatPrivateName(nodeDef.getParent().getQualifiedName(), qname);
                    write(qname);
                }
                endMapping(node);
            }
            else if (getModel().inE4xFilter && EmitterUtils.writeE4xFilterNode(getProject(), getModel(), node) /* swapped this out to allow for deeper nesting inside the filter expression ... instead of original:grandparentNodeId == ASTNodeID.E4XFilterID*/
            		&& (!(parentNodeId == ASTNodeID.MemberAccessExpressionID
                    || parentNodeId == ASTNodeID.Op_DescendantsID
                    || parentNodeId == ASTNodeID.FunctionCallID)))
            {
                startMapping(node);
                write("child('");
                write(node.getName());
                write("')");
                endMapping(node);
            }
            else
            {
                startMapping(node);
                write(node.getName());
                endMapping(node);
            }
        }
    }
    
    

}
