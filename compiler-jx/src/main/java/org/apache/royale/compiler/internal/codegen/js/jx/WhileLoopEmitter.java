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
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IWhileLoopNode;

public class WhileLoopEmitter extends JSSubEmitter implements
        ISubEmitter<IWhileLoopNode>
{
    public WhileLoopEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IWhileLoopNode node)
    {

        startMapping(node);
        writeToken(ASEmitterTokens.WHILE);
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);

        IASNode conditionalExpression = node.getConditionalExpressionNode();
        getWalker().walk(conditionalExpression);

        IContainerNode statementContentsNode = (IContainerNode) node.getStatementContentsNode();
        startMapping(node, conditionalExpression);
        write(ASEmitterTokens.PAREN_CLOSE);
        if (!EmitterUtils.isImplicit(statementContentsNode))
            write(ASEmitterTokens.SPACE);
        endMapping(node);
        //if we have a while loop that has no body, then emit it with an explicit 'empty block'.
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
}
