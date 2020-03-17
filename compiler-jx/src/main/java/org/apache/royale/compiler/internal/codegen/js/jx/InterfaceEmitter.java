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
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSDocEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.tree.as.TypedExpressionNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IAccessorNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;

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
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();

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

        JSRoyaleDocEmitter doc = (JSRoyaleDocEmitter) getEmitter().getDocEmitter();
  	    
        final IDefinitionNode[] members = node.getAllMemberDefinitionNodes();
        for (IDefinitionNode mnode : members)
        {
            boolean isAccessor = mnode.getNodeID() == ASTNodeID.GetterID
                    || mnode.getNodeID() == ASTNodeID.SetterID;

            String memberName = mnode.getQualifiedName();
            if (!isAccessor
                    || !getModel().getInterfacePropertyMap().contains(memberName))
            {
                writeNewline();

                if (isAccessor)
                {
                	IAccessorNode accessor = (IAccessorNode)mnode;
                	String propType = accessor.getVariableType();
                	IExpressionNode typeNode = accessor.getVariableTypeNode();
                	ITypeDefinition typeDef = typeNode.resolveType(project);
                	String packageName = typeDef.getPackageName();
                	packageName = project.getActualPackageName(packageName);
                    if (typeNode instanceof TypedExpressionNode) {
                        propType = "Vector.<" +
                                JSGoogDocEmitter.convertASTypeToJSType(
                                        ((TypedExpressionNode)typeNode).getTypeNode().resolveType(project).getQualifiedName(),
                                        "")
                                +">";
                        packageName = "";
                    }
                    write(JSDocEmitterTokens.JSDOC_OPEN);
                    write(ASEmitterTokens.SPACE);
                    fjs.getDocEmitter().emitType(propType, packageName);
                    write(ASEmitterTokens.SPACE);
                    write(JSDocEmitterTokens.JSDOC_CLOSE);
                }
                else
                {
                	doc.emitMethodDoc((IFunctionNode)mnode, project);
                }
                write(getEmitter().formatQualifiedName(qname));
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSEmitterTokens.PROTOTYPE);
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(memberName);

                if (isAccessor
                        && !getModel().getInterfacePropertyMap()
                                .contains(memberName))
                {
                    getModel().getInterfacePropertyMap().add(memberName);
                }
                else
                {
                    write(ASEmitterTokens.SPACE);
                    writeToken(ASEmitterTokens.EQUAL);
                    write(ASEmitterTokens.FUNCTION);

                    fjs.emitParameters(((IFunctionNode) mnode)
                            .getParametersContainerNode());

                    write(ASEmitterTokens.SPACE);
                    write(ASEmitterTokens.BLOCK_OPEN);
                    writeNewline();
                    write(ASEmitterTokens.BLOCK_CLOSE);
                }

                write(ASEmitterTokens.SEMICOLON);
            }
        }
        fjs.getPackageFooterEmitter().emitClassInfo(node);
    }

}
