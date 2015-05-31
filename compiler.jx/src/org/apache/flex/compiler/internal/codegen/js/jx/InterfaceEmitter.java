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
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;

public class InterfaceEmitter extends JSSubEmitter implements
        ISubEmitter<IInterfaceNode>
{

    public InterfaceEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IInterfaceNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) getEmitter();

        ICompilerProject project = getWalker().getProject();

        fjs.getDocEmitter().emitInterfaceDoc(node, project);

        String qname = node.getQualifiedName();
        if (qname != null && !qname.equals(""))
        {
            write(getEmitter().formatQualifiedName(qname));
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.FUNCTION);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.BLOCK_OPEN);
            writeNewline();
            write(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.SEMICOLON);
        }

        final IDefinitionNode[] members = node.getAllMemberDefinitionNodes();
        for (IDefinitionNode mnode : members)
        {
            boolean isAccessor = mnode.getNodeID() == ASTNodeID.GetterID
                    || mnode.getNodeID() == ASTNodeID.SetterID;

            if (!isAccessor
                    || !getModel().getInterfacePropertyMap().contains(qname))
            {
                writeNewline();

                write(getEmitter().formatQualifiedName(qname));
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSEmitterTokens.PROTOTYPE);
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(mnode.getQualifiedName());

                if (isAccessor
                        && !getModel().getInterfacePropertyMap()
                                .contains(qname))
                {
                    getModel().getInterfacePropertyMap().add(qname);
                }
                else
                {
                    write(ASEmitterTokens.SPACE);
                    writeToken(ASEmitterTokens.EQUAL);
                    write(ASEmitterTokens.FUNCTION);

                    fjs.emitParameters(((IFunctionNode) mnode)
                            .getParameterNodes());

                    write(ASEmitterTokens.SPACE);
                    write(ASEmitterTokens.BLOCK_OPEN);
                    writeNewline();
                    write(ASEmitterTokens.BLOCK_CLOSE);
                }

                write(ASEmitterTokens.SEMICOLON);
            }
        }
    }

}
