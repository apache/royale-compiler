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
import org.apache.flex.compiler.definitions.IFunctionDefinition.FunctionClassification;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.internal.definitions.AccessorDefinition;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionObjectNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.utils.NativeUtils;

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
        IDefinition nodeDef = ((IIdentifierNode) node).resolve(getProject());

        IASNode parentNode = node.getParent();
        ASTNodeID parentNodeId = parentNode.getNodeID();

        boolean identifierIsAccessorFunction = nodeDef instanceof AccessorDefinition;
        boolean identifierIsPlainFunction = nodeDef instanceof FunctionDefinition
                && !identifierIsAccessorFunction;

        boolean emitName = true;

        if (nodeDef != null && nodeDef.isStatic())
        {
            String sname = nodeDef.getParent().getQualifiedName();
            if (sname.length() > 0)
            {
                write(getEmitter().formatQualifiedName(sname));
                write(ASEmitterTokens.MEMBER_ACCESS);
            }
        }
        else if (!NativeUtils.isNative(node.getName()))
        {
            // an instance method as a parameter or
        	// an instance method assigned to a variable or
            // a local function
            boolean useGoogBind = (parentNodeId == ASTNodeID.ContainerID
                    && identifierIsPlainFunction && ((FunctionDefinition) nodeDef)
                    .getFunctionClassification() == FunctionClassification.CLASS_MEMBER)
                    || (identifierIsPlainFunction && ((FunctionDefinition) nodeDef)
                    .getFunctionClassification() == FunctionClassification.CLASS_MEMBER &&
                       isBeingAssignedToVariable(node))
                    || (identifierIsPlainFunction && ((FunctionDefinition) nodeDef)
                            .getFunctionClassification() == FunctionClassification.LOCAL);

            if (useGoogBind)
            {
                write(JSGoogEmitterTokens.GOOG_BIND);
                write(ASEmitterTokens.PAREN_OPEN);
            }

            if (EmitterUtils.writeThis(getProject(), getModel(), node))
            {
                IFunctionObjectNode functionObjectNode = (IFunctionObjectNode) node
                        .getParent().getAncestorOfType(
                                IFunctionObjectNode.class);

                if (functionObjectNode != null)
                    write(JSGoogEmitterTokens.SELF);
                else
                    write(ASEmitterTokens.THIS);

                write(ASEmitterTokens.MEMBER_ACCESS);
            }

            if (useGoogBind)
            {
                write(node.getName());

                writeToken(ASEmitterTokens.COMMA);
                write(ASEmitterTokens.THIS);
                write(ASEmitterTokens.PAREN_CLOSE);

                emitName = false;
            }
        }

        //IDefinition parentDef = (nodeDef != null) ? nodeDef.getParent() : null;
        //boolean isNative = (parentDef != null)
        //        && NativeUtils.isNative(parentDef.getBaseName());
        if (emitName)
        {
            if (nodeDef != null)
                write(getEmitter().formatQualifiedName(nodeDef.getQualifiedName()));
            else
                write(node.getName());
        }
    }
    
    private boolean isBeingAssignedToVariable(IIdentifierNode node)
    {
    	IVariableNode varNode = (IVariableNode) node
        .getParent().getAncestorOfType(
        		IVariableNode.class);
    	if (varNode == null) return false;
    	
    	IExpressionNode avnode = varNode.getAssignedValueNode();
    	IASNode parent = node.getParent();
    	IMemberAccessExpressionNode parentMAE = null;
        IFunctionCallNode fnNode = null;
    	while (parent != varNode)
    	{
    		if (parent instanceof IMemberAccessExpressionNode)
            {
                parentMAE = (IMemberAccessExpressionNode) parent;
            }
    		else if (parent instanceof IFunctionCallNode)
            {
                fnNode = (IFunctionCallNode) parent;
            }
    		if (parent == avnode)
    		{
    			if (parentMAE != null)
    			{
                    // do one final check that this is the right node in
                    // a member access expression
    				return (node == parentMAE.getRightOperandNode());
    			}
    			return fnNode == null;
    		}
    		else     		
    			parent = parent.getParent();
    	}
    	return false;
    }

}
