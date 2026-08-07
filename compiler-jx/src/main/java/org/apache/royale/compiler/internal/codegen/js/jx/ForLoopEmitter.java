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
import org.apache.royale.compiler.constants.IJSMetaAttributeConstants;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IVariableExpressionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;

public class ForLoopEmitter extends JSSubEmitter implements
        ISubEmitter<IForLoopNode>
{
    public ForLoopEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IForLoopNode node)
    {
        IContainerNode statementContentsNode = (IContainerNode) node.getStatementContentsNode();
        IContainerNode cnode = node.getConditionalsContainerNode();
        final IASNode node0 = cnode.getChild(0);

        if (node0.getNodeID() == ASTNodeID.Op_InID)
        {
            IBinaryOperatorNode inNode = (IBinaryOperatorNode) node0;
            IExpressionNode rnode = inNode.getRightOperandNode();


            ITypeDefinition rtype = rnode.resolveType(getProject());
            if (rtype != null)
            {
                IMetaTag forInOverrideMeta = null;
                for (ITypeDefinition currentType : rtype.typeIteratable(getProject(), false))
                {
                    forInOverrideMeta = currentType.getMetaTagByName(IJSMetaAttributeConstants.ATTRIBUTE_FOR_IN_OVERRIDE);
                    if (forInOverrideMeta != null)
                    {
                        emitForInOverride(node, rtype, forInOverrideMeta);
                        return;
                    }
                }
            }
        }

        startMapping(node);
        writeToken(ASEmitterTokens.FOR);
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);

        if (node0.getNodeID() == ASTNodeID.Op_InID)
        {
            //for(in)
            getWalker().walk(cnode.getChild(0));
        }
        else //for(;;)
        {
            emitForStatements(cnode);
        }

        startMapping(node, cnode);
        write(ASEmitterTokens.PAREN_CLOSE);
        if (!EmitterUtils.isImplicit(statementContentsNode))
            write(ASEmitterTokens.SPACE);
        endMapping(node);
        //if we have a for loop that has no body, then emit it with an explicit 'empty block'.
        //Otherwise the loop body will be considered to be the following statement
        //the empty block is to avoid this from GCC: "WARNING - If this if/for/while really shouldn't have a body, use {}"
        if (EmitterUtils.isImplicit(statementContentsNode)
                && statementContentsNode.getChildCount() == 0) {
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.BLOCK_OPEN);
            write(ASEmitterTokens.BLOCK_CLOSE);
            writeToken(ASEmitterTokens.SEMICOLON);
        } else {
            getWalker().walk(statementContentsNode);
        }

    }

    protected void emitForStatements(IContainerNode node)
    {
        final IASNode node0 = node.getChild(0);
        final IASNode node1 = node.getChild(1);
        final IASNode node2 = node.getChild(2);

        int column = node.getColumn();
        // initializer
        if (node0 != null)
        {
            getWalker().walk(node0);

            if (node1.getNodeID() != ASTNodeID.NilID)
            {
                column += node0.getAbsoluteEnd() - node0.getAbsoluteStart();
            }
            startMapping(node, node.getLine(), column);
            write(ASEmitterTokens.SEMICOLON);
            column++;
            if (node1.getNodeID() != ASTNodeID.NilID)
            {
                write(ASEmitterTokens.SPACE);
                column++;
            }
            endMapping(node);
        }
        // condition or target
        if (node1 != null)
        {
            getWalker().walk(node1);
            
            if (node1.getNodeID() != ASTNodeID.NilID)
            {
                column += node1.getAbsoluteEnd() - node1.getAbsoluteStart();
            }
            startMapping(node, node.getLine(), column);
            write(ASEmitterTokens.SEMICOLON);
            if (node2.getNodeID() != ASTNodeID.NilID)
                write(ASEmitterTokens.SPACE);
            endMapping(node);
        }
        // iterator
        if (node2 != null)
        {
            getWalker().walk(node2);
        }
    }

    private void emitForInOverride(IForLoopNode node, ITypeDefinition rtype, IMetaTag forInOverrideMeta)
    {
        IContainerNode cnode = node.getConditionalsContainerNode();
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) cnode.getChild(0);
        IExpressionNode childNode = bnode.getLeftOperandNode();
        IExpressionNode rnode = bnode.getRightOperandNode();

        final String iterBaseName = getModel().getCurrentForeachName();
        getModel().incForeachLoopCount();
        final String iterTargetName = iterBaseName + "_target";
        final String iterResultName = iterBaseName + "_iterator";
        final String iterKeyName = iterBaseName + "_key";

        final String iteratorMethodName = forInOverrideMeta.getAttributeValue(IJSMetaAttributeConstants.NAME_FOR_IN_OVERRIDE_ITERATOR_METHOD);
        final String iteratorNextMethodName = forInOverrideMeta.getAttributeValue(IJSMetaAttributeConstants.NAME_FOR_IN_OVERRIDE_ITERATOR_NEXT_METHOD);
        final String iteratorHasNextMethodName = forInOverrideMeta.getAttributeValue(IJSMetaAttributeConstants.NAME_FOR_IN_OVERRIDE_ITERATOR_HAS_NEXT_METHOD);
        final String iteratorDoneMethodName = forInOverrideMeta.getAttributeValue(IJSMetaAttributeConstants.NAME_FOR_IN_OVERRIDE_ITERATOR_DONE_METHOD);

        if (iteratorMethodName == null || iteratorNextMethodName == null)
        {
            return;
        }

        writeToken(ASEmitterTokens.VAR);
        writeToken(iterTargetName);
        writeToken(ASEmitterTokens.EQUAL);
        getWalker().walk(rnode);
        write(ASEmitterTokens.SEMICOLON);
        writeNewline();

        // if the target is null, the loop will be skipped without any
        // exceptions at run-time
        writeToken(ASEmitterTokens.IF);
        write(ASEmitterTokens.PAREN_OPEN);
        write(iterTargetName);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline();
        write(ASEmitterTokens.BLOCK_OPEN);
        indentPush();
        writeNewline();

        writeToken(ASEmitterTokens.VAR);
        writeToken(iterResultName);
        writeToken(ASEmitterTokens.EQUAL);
        write(iterTargetName);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(iteratorMethodName);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SEMICOLON);
        writeNewline();

        writeToken(ASEmitterTokens.WHILE);
        write(ASEmitterTokens.PAREN_OPEN);
        if (iteratorHasNextMethodName != null)
        {
            write(iterResultName);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(iteratorHasNextMethodName);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.PAREN_CLOSE);
        }
        else if (iteratorDoneMethodName != null)
        {
            write("!");
            write(iterResultName);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(iteratorDoneMethodName);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.PAREN_CLOSE);
        }
        else
        {
            write(ASEmitterTokens.TRUE);
        }
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline();
        write(ASEmitterTokens.BLOCK_OPEN);
        indentPush();
        writeNewline();
        
        if (iteratorHasNextMethodName == null && iteratorDoneMethodName == null)
        {
            writeToken(ASEmitterTokens.VAR);
            writeToken(iterKeyName);
            writeToken(ASEmitterTokens.EQUAL);
            write(iterResultName);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(iteratorNextMethodName);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SEMICOLON);
            writeNewline();

            writeToken(ASEmitterTokens.IF);
            write(ASEmitterTokens.PAREN_OPEN);
            writeToken(iterKeyName);
            writeToken("==");
            write(ASEmitterTokens.UNDEFINED);
            writeToken(ASEmitterTokens.PAREN_CLOSE);
            write("break");
            write(ASEmitterTokens.SEMICOLON);
            writeNewline();
        }

        if (childNode instanceof IVariableExpressionNode)
        {
            startMapping(childNode);
            write(ASEmitterTokens.VAR);
            write(ASEmitterTokens.SPACE);
            write(((IVariableNode) childNode.getChild(0)).getName()); //it's always a local var
            //putting this in here instead of common code following the 2 blocks to keep sourcemap tests passing
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.SPACE);
            endMapping(childNode);
        }
        else { //IdentifierNode
            getWalker().walk(childNode); //we need to walk here, to deal with non-local var identifiers
            startMapping(childNode);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.SPACE);
            endMapping(childNode);
        }

        if (iteratorHasNextMethodName == null && iteratorDoneMethodName == null)
        {
            write(iterKeyName);
            write(ASEmitterTokens.SEMICOLON);
            writeNewline();
        }
        else
        {
            write(iterResultName);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(iteratorNextMethodName);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SEMICOLON);
            writeNewline();
        }

        getWalker().walk(node.getStatementContentsNode());

        write(ASEmitterTokens.BLOCK_CLOSE);
        indentPop();
        writeNewline();

        write(ASEmitterTokens.BLOCK_CLOSE);
        indentPop();
        writeNewline();
    }
}
