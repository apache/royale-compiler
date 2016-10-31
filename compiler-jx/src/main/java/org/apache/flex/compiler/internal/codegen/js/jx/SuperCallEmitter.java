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

import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSessionModel;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;

public class SuperCallEmitter extends JSSubEmitter
{

    public SuperCallEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    public void emit(IASNode node, String type)
    {
        IFunctionNode fnode = (node instanceof IFunctionNode) ? (IFunctionNode) node
                : null;
        IFunctionCallNode fcnode = (node instanceof IFunctionCallNode) ? (FunctionCallNode) node
                : null;

        final IClassDefinition thisClass = getModel().getCurrentClass();

        if (JSSessionModel.SUPER_FUNCTION_CALL.equals(type))
        {
            // FIXME: This is obviously wrong.
            /*if (fnode == null) {
                fnode = (IFunctionNode) fcnode.getAncestorOfType(IFunctionNode.class);
            }*/

            if (fnode != null && fnode.isConstructor()
                    && !EmitterUtils.hasSuperClass(getProject(), fnode)) {
                return;
            }

            IClassNode cnode = (IClassNode) node.getAncestorOfType(IClassNode.class);

            if (fnode != null
                    && (fnode.getNodeID() == ASTNodeID.GetterID || fnode
                            .getNodeID() == ASTNodeID.SetterID))
            {
                if (cnode == null && thisClass != null) {
                    write(getEmitter().formatQualifiedName(
                            thisClass.getQualifiedName()));
                } else if(cnode != null) {
                    write(getEmitter().formatQualifiedName(
                            cnode.getQualifiedName()));
                }
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSGoogEmitterTokens.GOOG_BASE);
                write(ASEmitterTokens.PAREN_OPEN);
                write(ASEmitterTokens.THIS);
                writeToken(ASEmitterTokens.COMMA);
                write(ASEmitterTokens.SINGLE_QUOTE);
                if (fnode.getNodeID() == ASTNodeID.GetterID) {
                    write(JSFlexJSEmitterTokens.GETTER_PREFIX);
                } else {
                    write(JSFlexJSEmitterTokens.SETTER_PREFIX);
                }
                write(fnode.getName());
                write(ASEmitterTokens.SINGLE_QUOTE);

                IASNode[] anodes = null;
                boolean writeArguments = false;
                if (fcnode != null)
                {
                    anodes = fcnode.getArgumentNodes();

                    writeArguments = anodes.length > 0;
                }
                else if (fnode.isConstructor())
                {
                    anodes = fnode.getParameterNodes();

                    writeArguments = (anodes != null && anodes.length > 0);
                }

                if (writeArguments)
                {
                    for (IASNode anode : anodes) {
                        writeToken(ASEmitterTokens.COMMA);

                        getWalker().walk(anode);
                    }
                }

                write(ASEmitterTokens.PAREN_CLOSE);
                return;
            }
        }
        super_emitSuperCall(node, type);
    }

    protected void super_emitSuperCall(IASNode node, String type)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) getEmitter();

        IFunctionNode fnode = (node instanceof IFunctionNode) ? (IFunctionNode) node : null;
        IFunctionCallNode fcnode = (node instanceof IFunctionCallNode) ? (FunctionCallNode) node : null;

        if (JSSessionModel.CONSTRUCTOR_EMPTY.equals(type))
        {
            indentPush();
            writeNewline();
            indentPop();
        }
        else if (JSSessionModel.SUPER_FUNCTION_CALL.equals(type))
        {
            // FIXME: This is obviously wrong.
            if (fnode == null) {
                fnode = (IFunctionNode) fcnode.getAncestorOfType(IFunctionNode.class);
            }
        }

        if (fnode.isConstructor() && !EmitterUtils.hasSuperClass(getProject(), fnode)) {
            return;
        }

        IClassNode cnode = (IClassNode) node.getAncestorOfType(IClassNode.class);

        if (cnode == null)
        {
            IDefinition cdef = getModel().getCurrentClass();
            write(fjs.formatQualifiedName(cdef.getQualifiedName()));
        }
        else {
            write(fjs.formatQualifiedName(cnode.getQualifiedName()));
        }
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSGoogEmitterTokens.GOOG_BASE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.THIS);

        if (fnode.isConstructor())
        {
            writeToken(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(JSGoogEmitterTokens.GOOG_CONSTRUCTOR);
            write(ASEmitterTokens.SINGLE_QUOTE);
        }

        if (!fnode.isConstructor())
        {
            writeToken(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.SINGLE_QUOTE);
            IExpressionNode namenode = fcnode.getNameNode();
            IDefinition def = namenode.resolve(getWalker().getProject());
            String superName = fnode.getName();
            if (namenode instanceof MemberAccessExpressionNode)
            {
            	namenode = ((MemberAccessExpressionNode)namenode).getRightOperandNode();
            	if (namenode instanceof IdentifierNode)
            	{
            		superName = ((IdentifierNode)namenode).getName();
            	}
            }
            if (def instanceof FunctionDefinition && fjs.isCustomNamespace((FunctionDefinition) def))
            {
            	INamespaceDefinition nsDef = def.getNamespaceReference().resolveNamespaceReference(getProject());
            	if (nsDef.getContainingScope() != null) // was null for flash_proxy in unit test
            		fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
    			String s = nsDef.getURI();
    			superName = s + "::" + superName;
            }
            write(superName);
            write(ASEmitterTokens.SINGLE_QUOTE);
        }

        IASNode[] anodes = null;
        boolean writeArguments = false;
        if (fcnode != null)
        {
            anodes = fcnode.getArgumentNodes();

            writeArguments = anodes.length > 0;
        }
        else if (fnode.isConstructor())
        {
        	// I think we only get here for implicit super calls
        	// (when there is no mention of super() in the constructor
        	// code and the compiler autogenerates the super() call)
        	// and implicit super calls do not have parameters passed
        	// to them.
        	/*
            anodes = fnode.getParameterNodes();

            writeArguments = (anodes != null && anodes.length > 0);
            */
        }

        if (writeArguments)
        {
            for (IASNode anode : anodes) {
                writeToken(ASEmitterTokens.COMMA);

                getWalker().walk(anode);
            }
        }

        write(ASEmitterTokens.PAREN_CLOSE);

        if (JSSessionModel.CONSTRUCTOR_FULL.equals(type))
        {
            write(ASEmitterTokens.SEMICOLON);
            writeNewline();
        }
        else if (JSSessionModel.CONSTRUCTOR_EMPTY.equals(type))
        {
            write(ASEmitterTokens.SEMICOLON);
        }
    }
}
