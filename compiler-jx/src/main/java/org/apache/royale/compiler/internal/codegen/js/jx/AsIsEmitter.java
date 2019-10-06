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

import org.apache.royale.compiler.asdoc.royale.ASDocComment;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IAppliedVectorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.utils.NativeUtils;

public class AsIsEmitter extends JSSubEmitter
{

    public AsIsEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    public void emitIsAs(IExpressionNode node, IExpressionNode left, IExpressionNode right,
                         ASTNodeID id, boolean coercion)
    {
        // project is null in unit tests
        //IDefinition dnode = project != null ? (right).resolve(project) : null;
        IDefinition dnode = getProject() != null ? (right)
                .resolve(getProject()) : null;
        if (id != ASTNodeID.Op_IsID && dnode != null)
        {
            boolean emit = coercion ?
            		!((RoyaleJSProject)getProject()).config.getJSOutputOptimizations().contains(JSRoyaleEmitterTokens.SKIP_FUNCTION_COERCIONS.getToken()) :
                	!((RoyaleJSProject)getProject()).config.getJSOutputOptimizations().contains(JSRoyaleEmitterTokens.SKIP_AS_COERCIONS.getToken());
            			
            // find the function node
            IFunctionNode functionNode = (IFunctionNode) left
                    .getAncestorOfType(IFunctionNode.class);
            if (functionNode != null) // can be null in synthesized binding code
            {
                if (coercion)
                {
                	// see if the cast is inside a try/catch in this function. If so,
                	// assume that we want an exception.
                	IASNode child = left.getParent();
                	while (child != functionNode)
                	{
                		if (child.getNodeID() == ASTNodeID.TryID)
                		{
                			emit = true;
                			break;
                		}
                		child = child.getParent();
                	}
                }
                ASDocComment asDoc = (ASDocComment) functionNode
                        .getASDocComment();
                if (asDoc != null)
                {
                    String asDocString = asDoc.commentNoEnd();
                    String coercionToken = JSRoyaleEmitterTokens.EMIT_COERCION
                            .getToken();
                    int emitIndex = asDocString.indexOf(coercionToken);
                    while (emitIndex != -1)
                    {
                        String emitable = asDocString.substring(emitIndex
                                + coercionToken.length());
                        int endIndex = emitable.indexOf("\n");
                        emitable = emitable.substring(0, endIndex);
                        emitable = emitable.trim();
                        String rightSide = dnode.getQualifiedName();
                        if (emitable.equals(rightSide))
                        {
                            emit = true;
                            break;
                        }
                        emitIndex = asDocString.indexOf(coercionToken,
                        		emitIndex + coercionToken.length());
                    }
                    String ignoreToken = JSRoyaleEmitterTokens.IGNORE_COERCION
                    .getToken();
		            int ignoreIndex = asDocString.indexOf(ignoreToken);
		            while (ignoreIndex != -1)
		            {
		                String ignorable = asDocString.substring(ignoreIndex
		                        + ignoreToken.length());
		                int endIndex = ignorable.indexOf("\n");
		                ignorable = ignorable.substring(0, endIndex);
		                ignorable = ignorable.trim();
		                String rightSide = dnode.getQualifiedName();
		                if (ignorable.equals(rightSide))
		                {
		                    emit = false;
		                    break;
		                }
		                ignoreIndex = asDocString.indexOf(ignoreToken,
		                		ignoreIndex + ignoreToken.length());
		            }
                }
            }
            if (!emit)
            {
            	if (dnode.getQualifiedName().equals(IASLanguageConstants.Function))
            	{
            		write(" /** @type {Function} */ (");
            	}
            	else if (dnode.getQualifiedName().equals(IASLanguageConstants.Class))
            	{
            		write(" /** @type {Object|null} */ (");
            	}
                getWalker().walk(left);
            	if (dnode.getQualifiedName().equals(IASLanguageConstants.Function) || dnode.getQualifiedName().equals(IASLanguageConstants.Class))
            	{
            		write(")");
            	}
                return;
            }
        }

        ICompilerProject project = this.getProject();
        if (project instanceof RoyaleJSProject)
        	((RoyaleJSProject)project).needLanguage = true;
        getEmitter().getModel().needLanguage = true;
        if (node instanceof IBinaryOperatorNode)
        {
            IBinaryOperatorNode binaryOperatorNode = (IBinaryOperatorNode) node;
            startMapping(node, binaryOperatorNode.getLeftOperandNode());
        }
        else
        {
            startMapping(node);
        }
        write(JSRoyaleEmitterTokens.LANGUAGE_QNAME);
        write(ASEmitterTokens.MEMBER_ACCESS);

        if (id == ASTNodeID.Op_IsID)
            write(ASEmitterTokens.IS);
        else
            write(ASEmitterTokens.AS);

        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);
        
        getWalker().walk(left);
        if (node instanceof IBinaryOperatorNode)
        {
            IBinaryOperatorNode binaryOperatorNode = (IBinaryOperatorNode) node;
            startMapping(node, binaryOperatorNode.getLeftOperandNode());
        }
        else
        {
            startMapping(node);
        }
        writeToken(ASEmitterTokens.COMMA);
        endMapping(node);

        if (dnode instanceof IClassDefinition)
        {
            startMapping(right);
            if (NativeUtils.isSyntheticJSType(dnode.getQualifiedName())) {
                JSRoyaleEmitterTokens langMethod;
                String synthName;
                if (NativeUtils.isVector(dnode.getQualifiedName()) && dnode instanceof IAppliedVectorDefinition) {
                    langMethod = JSRoyaleEmitterTokens.SYNTH_VECTOR;
                    synthName = getEmitter().formatQualifiedName(((IAppliedVectorDefinition) dnode).resolveElementType(project).getQualifiedName());
                } else {
                    //non-vector, e.g. int/uint
                    langMethod = JSRoyaleEmitterTokens.SYNTH_TYPE;
                    synthName = getEmitter().formatQualifiedName(dnode.getQualifiedName());
                }
                write(langMethod);
                write(ASEmitterTokens.PAREN_OPEN);
                write(ASEmitterTokens.SINGLE_QUOTE);
                write(synthName);
                write(ASEmitterTokens.SINGLE_QUOTE);
                write(ASEmitterTokens.PAREN_CLOSE);
                if (project instanceof RoyaleJSProject)
                    ((RoyaleJSProject)project).needLanguage = true;
                getEmitter().getModel().needLanguage = true;
            } else {
                write(getEmitter().formatQualifiedName(((JSRoyaleEmitter)getEmitter()).convertASTypeToJS(dnode.getQualifiedName())));
            }
            endMapping(right);
        }
        else
        {
            getWalker().walk(right);
        }

        if (node instanceof IBinaryOperatorNode)
        {
            IBinaryOperatorNode binaryOperatorNode = (IBinaryOperatorNode) node;
            startMapping(node, binaryOperatorNode.getLeftOperandNode());
        }
        else
        {
            startMapping(node);
        }
        if (coercion)
        {
            writeToken(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.TRUE);
        }

        write(ASEmitterTokens.PAREN_CLOSE);
        endMapping(node);
    }

}
