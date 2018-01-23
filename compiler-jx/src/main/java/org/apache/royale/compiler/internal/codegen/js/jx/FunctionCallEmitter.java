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

import org.apache.royale.compiler.codegen.IASGlobalFunctionConstants;
import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.definitions.AppliedVectorDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.internal.tree.as.VectorLiteralNode;
import org.apache.royale.compiler.problems.TooFewFunctionParametersProblem;
import org.apache.royale.compiler.problems.TooManyFunctionParametersProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.utils.NativeUtils;

public class FunctionCallEmitter extends JSSubEmitter implements ISubEmitter<IFunctionCallNode>
{

    public FunctionCallEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IFunctionCallNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();

        IASNode cnode = node.getChild(0);

        if (cnode.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            cnode = cnode.getChild(0);

        ASTNodeID id = cnode.getNodeID();
        if (id != ASTNodeID.SuperID)
        {
            IDefinition def = null;
            IExpressionNode nameNode = node.getNameNode();
            def = nameNode.resolve(getProject());

            boolean isClassCast = false;

            if (node.isNewExpression())
            {
                if (!(node.getChild(1) instanceof VectorLiteralNode))
                {
                    if (def == null || !(def.getBaseName().equals(IASGlobalFunctionConstants._int) || 
                    					 def.getBaseName().equals(IASGlobalFunctionConstants.uint) ||
                    					 def instanceof AppliedVectorDefinition))
                    {
	                    startMapping(node.getNewKeywordNode());
	                    writeToken(ASEmitterTokens.NEW);
	                    endMapping(node.getNewKeywordNode());
                    }
                }
                else
                {
                    VectorLiteralNode vectorLiteralNode = (VectorLiteralNode) node.getChild(1);
                    write("[");
                    ContainerNode contentsNode = vectorLiteralNode.getContentsNode();
                    int len = contentsNode.getChildCount();
                    for (int i = 0; i < len; i++)
                    {
                        getWalker().walk(contentsNode.getChild(i));
                        if (i < len - 1)
                        {
                            writeToken(ASEmitterTokens.COMMA);
                        }
                    }
                    write("]");
                    return;
                }
            }
            else
            {
                def = node.getNameNode().resolve(getProject());

                isClassCast = (def instanceof ClassDefinition || def instanceof InterfaceDefinition)
                        && !(NativeUtils.isJSNative(def.getBaseName()))
                        && !def.getBaseName().equals(IASLanguageConstants.XML)
                        && !def.getBaseName().equals(IASLanguageConstants.XMLList);
            }

            if (node.isNewExpression())
            {
                def = node.resolveCalledExpression(getProject());
                // all new calls to a class should be fully qualified names
                if (def instanceof ClassDefinition)
                {
                    startMapping(nameNode);
                    boolean isInt = def.getBaseName().equals(IASGlobalFunctionConstants._int);
                    if (isInt || def.getBaseName().equals(IASGlobalFunctionConstants.uint))
                    {
                        ICompilerProject project = this.getProject();
                        if (project instanceof RoyaleJSProject)
                            ((RoyaleJSProject) project).needLanguage = true;
                        getEmitter().getModel().needLanguage = true;
                        write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        if (isInt)
                            write(JSRoyaleEmitterTokens.UNDERSCORE);
                        write(def.getQualifiedName());
                        endMapping(nameNode);
                    }
                    else
                    {
	                    write(getEmitter().formatQualifiedName(def.getQualifiedName()));
	                    endMapping(nameNode);
                    }
                }
                else
                {
                    // wrap "new someFunctionCall(args)" in parens so the
                    // function call gets parsed and evaluated before new
                    // otherwise it just looks like any other "new function"
                    // in JS.
                    if (nameNode.hasParenthesis())
                        write(ASEmitterTokens.PAREN_OPEN);                        
                    // I think we still need this for "new someVarOfTypeClass"
                    getEmitter().getWalker().walk(nameNode);
                    if (nameNode.hasParenthesis())
                        write(ASEmitterTokens.PAREN_CLOSE);                        
                }
                
                if (def instanceof AppliedVectorDefinition)
                {
                	ContainerNode args = node.getArgumentsNode();
                	if (args.getChildCount() == 0)
                	{
                    	getEmitter().emitArguments(node.getArgumentsNode());
                	}
                	else
                	{
                        startMapping(node);
                        write(ASEmitterTokens.PAREN_OPEN);
                        endMapping(node);
                        getWalker().walk(args.getChild(0));
                        write(ASEmitterTokens.COMMA);
                        write(ASEmitterTokens.SPACE);
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        write(((AppliedVectorDefinition)def).resolveElementType(getWalker().getProject()).getBaseName());
                        write(ASEmitterTokens.SINGLE_QUOTE);
                        write(ASEmitterTokens.PAREN_CLOSE);
                	}
                }
                else
                	getEmitter().emitArguments(node.getArgumentsNode());
            }
            else if (!isClassCast)
            {
                if (def != null)
                {
                    boolean isInt = def.getBaseName().equals(IASGlobalFunctionConstants._int);
                    if (isInt || def.getBaseName().equals(IASGlobalFunctionConstants.trace)
                            || def.getBaseName().equals(IASGlobalFunctionConstants.uint))
                    {
                        ICompilerProject project = this.getProject();
                        if (project instanceof RoyaleJSProject)
                            ((RoyaleJSProject) project).needLanguage = true;
                        getEmitter().getModel().needLanguage = true;
                        startMapping(node.getNameNode());
                        write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        if (isInt)
                            write(JSRoyaleEmitterTokens.UNDERSCORE);
                        endMapping(node.getNameNode());
                    }
                    else if (def.getBaseName().equals("sortOn"))
                	{
                		if (def.getParent() != null &&
                    		def.getParent().getQualifiedName().equals("Array"))
                		{
                            ICompilerProject project = this.getProject();
                            if (project instanceof RoyaleJSProject)
                                ((RoyaleJSProject) project).needLanguage = true;
                            getEmitter().getModel().needLanguage = true;
                            write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
                            write(ASEmitterTokens.MEMBER_ACCESS);
                            write("sortOn");
                            IContainerNode newArgs = EmitterUtils.insertArgumentsBefore(node.getArgumentsNode(), cnode);
                            fjs.emitArguments(newArgs);
                            return;
            			}
            		}
                    else if (def.getBaseName().equals("sort"))
                	{
                		if (def.getParent() != null &&
                    		def.getParent().getQualifiedName().equals("Array"))
                		{
                			IExpressionNode args[] = node.getArgumentNodes();
                			if (args.length > 0)
                			{
	                			IExpressionNode param1 = args[0];
	                            ICompilerProject project = this.getProject();
	                			IDefinition paramDef1 = param1.resolveType(project);
	                			if (paramDef1.getBaseName().equals(IASLanguageConstants._int))
	                			{
		                            if (project instanceof RoyaleJSProject)
		                                ((RoyaleJSProject) project).needLanguage = true;
		                            getEmitter().getModel().needLanguage = true;
		                            write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
		                            write(ASEmitterTokens.MEMBER_ACCESS);
		                            write("sort");
		                            IContainerNode newArgs = EmitterUtils.insertArgumentsBefore(node.getArgumentsNode(), cnode);
		                            fjs.emitArguments(newArgs);
		                            return;
	                			}
                			}
            			}
            		}

                    else if (def instanceof AppliedVectorDefinition)
                    {
                        IExpressionNode[] argumentNodes = node.getArgumentNodes();
                        int len = argumentNodes.length;
                    	if (len == 0)
                    	{
                    		getWalker().getProject().getProblems().add(new TooFewFunctionParametersProblem(node, 1));
                    	}
                    	else if (len > 1)
                    	{
                    		getWalker().getProject().getProblems().add(new TooManyFunctionParametersProblem(node, 1));                    		
                    	}
                    	else
                    	{
                            IExpressionNode argumentNode = argumentNodes[0];
                            getWalker().walk(argumentNode);
                            write(".slice()");
                        }
                        return;
                    }
                }
            	getWalker().walk(node.getNameNode());

                getEmitter().emitArguments(node.getArgumentsNode());
            }
            else //function-style cast
            {
                fjs.emitIsAs(node, node.getArgumentNodes()[0], node.getNameNode(), ASTNodeID.Op_AsID, true);
            }
        }
        else
        {
            fjs.emitSuperCall(node, JSSessionModel.SUPER_FUNCTION_CALL);
        }
    }

}
