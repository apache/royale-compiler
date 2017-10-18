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
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IOperatorNode;
import org.apache.royale.compiler.tree.as.ITernaryOperatorNode;
import org.apache.royale.compiler.utils.ASNodeUtils;

public class TernaryOperatorEmitter extends JSSubEmitter implements
        ISubEmitter<ITernaryOperatorNode>
{
    public TernaryOperatorEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(ITernaryOperatorNode node)
    {
        if (ASNodeUtils.hasParenOpen((IOperatorNode) node))
            write(ASEmitterTokens.PAREN_OPEN);

        IExpressionNode conditionalNode = node.getConditionalNode();
        getWalker().walk(conditionalNode);

        IExpressionNode leftOperandNode = node.getLeftOperandNode();
        startMapping(node, conditionalNode);
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.TERNARY);
        endMapping(node);

        getWalker().walk(leftOperandNode);

        IExpressionNode rightOperandNode = node.getRightOperandNode();
        startMapping(node, leftOperandNode);
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.COLON);
        endMapping(node);

        getWalker().walk(rightOperandNode);

        if (ASNodeUtils.hasParenClose((IOperatorNode) node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }
}
