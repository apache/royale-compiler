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
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.definitions.AccessorDefinition;
import org.apache.flex.compiler.internal.tree.as.GetterNode;
import org.apache.flex.compiler.internal.tree.as.UnaryOperatorAtNode;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.utils.ASNodeUtils;

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

        IASNode leftNode = node.getLeftOperandNode();
        IASNode rightNode = node.getRightOperandNode();

        IDefinition def = node.resolve(getProject());
        boolean isStatic = false;
        if (def != null && def.isStatic())
            isStatic = true;

        boolean continueWalk = true;
        if (!isStatic)
        {
            if (!(leftNode instanceof ILanguageIdentifierNode && ((ILanguageIdentifierNode) leftNode)
                    .getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.THIS))
            {
                IDefinition rightDef = null;
                if (rightNode instanceof IIdentifierNode)
                    rightDef = ((IIdentifierNode) rightNode)
                            .resolve(getProject());

                if (rightNode instanceof UnaryOperatorAtNode)
                {
                    // ToDo (erikdebruin): properly handle E4X

                    write(ASEmitterTokens.THIS);
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    getWalker().walk(node.getLeftOperandNode());
                    write(ASEmitterTokens.SQUARE_OPEN);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write("E4XOperator");
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.SQUARE_CLOSE);
                    continueWalk = false;
                }
                else if (node.getNodeID() == ASTNodeID.Op_DescendantsID)
                {
                    // ToDo (erikdebruin): properly handle E4X

                    write(ASEmitterTokens.THIS);
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    getWalker().walk(node.getLeftOperandNode());
                    write(ASEmitterTokens.SQUARE_OPEN);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write("E4XSelector");
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.SQUARE_CLOSE);
                    continueWalk = false;
                }
                else if (leftNode.getNodeID() != ASTNodeID.SuperID)
                {
                    getWalker().walk(node.getLeftOperandNode());
                    write(node.getOperator().getOperatorText());
                }
                else if (leftNode.getNodeID() == ASTNodeID.SuperID
                        && (rightNode.getNodeID() == ASTNodeID.GetterID || (rightDef != null && rightDef instanceof AccessorDefinition)))
                {
                    // setter is handled in binaryOperator
                    write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(JSFlexJSEmitterTokens.SUPERGETTER);
                    write(ASEmitterTokens.PAREN_OPEN);
                    IClassNode cnode = (IClassNode) node
                            .getAncestorOfType(IClassNode.class);
                    write(getEmitter().formatQualifiedName(
                            cnode.getQualifiedName()));
                    writeToken(ASEmitterTokens.COMMA);
                    write(ASEmitterTokens.THIS);
                    writeToken(ASEmitterTokens.COMMA);
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    if (rightDef != null)
                        write(rightDef.getBaseName());
                    else
                        write(((GetterNode) rightNode).getName());
                    write(ASEmitterTokens.SINGLE_QUOTE);
                    write(ASEmitterTokens.PAREN_CLOSE);
                    continueWalk = false;
                }
            }
            else
            {
                write(ASEmitterTokens.THIS);
                write(node.getOperator().getOperatorText());
            }
        }

        if (continueWalk)
            getWalker().walk(node.getRightOperandNode());

        if (ASNodeUtils.hasParenClose(node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }

}
