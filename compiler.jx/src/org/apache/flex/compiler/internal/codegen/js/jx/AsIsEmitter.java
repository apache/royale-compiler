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

import org.apache.flex.compiler.asdoc.flexjs.ASDocComment;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;

public class AsIsEmitter extends JSSubEmitter
{

    public AsIsEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    public void emitIsAs(IExpressionNode left, IExpressionNode right,
            ASTNodeID id, boolean coercion)
    {
        // project is null in unit tests
        //IDefinition dnode = project != null ? (right).resolve(project) : null;
        IDefinition dnode = getProject() != null ? (right)
                .resolve(getProject()) : null;
        if (id != ASTNodeID.Op_IsID && dnode != null)
        {
            boolean emit = false;
            // find the function node
            IFunctionNode functionNode = (IFunctionNode) left
                    .getAncestorOfType(IFunctionNode.class);
            if (functionNode != null) // can be null in synthesized binding code
            {
                ASDocComment asDoc = (ASDocComment) functionNode
                        .getASDocComment();
                if (asDoc != null)
                {
                    String asDocString = asDoc.commentNoEnd();
                    String coercionToken = JSFlexJSEmitterTokens.EMIT_COERCION
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
                }
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
            }
            if (!emit)
            {
                getWalker().walk(left);
                return;
            }
        }

        ICompilerProject project = this.getProject();
        if (project instanceof FlexJSProject)
        	((FlexJSProject)project).needLanguage = true;
        
        write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
        write(ASEmitterTokens.MEMBER_ACCESS);

        if (id == ASTNodeID.Op_IsID)
            write(ASEmitterTokens.IS);
        else
            write(ASEmitterTokens.AS);

        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(left);
        writeToken(ASEmitterTokens.COMMA);

        if (dnode instanceof ClassDefinition)
            write(getEmitter().formatQualifiedName(dnode.getQualifiedName()));
        else
            getWalker().walk(right);

        if (coercion)
        {
            writeToken(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.TRUE);
        }

        write(ASEmitterTokens.PAREN_CLOSE);
    }

}
