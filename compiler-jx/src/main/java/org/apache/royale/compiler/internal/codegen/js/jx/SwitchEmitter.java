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
import org.apache.royale.compiler.tree.as.IConditionalNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.ISwitchNode;
import org.apache.royale.compiler.tree.as.ITerminalNode;
import org.apache.royale.compiler.utils.ASNodeUtils;

public class SwitchEmitter extends JSSubEmitter implements
        ISubEmitter<ISwitchNode>
{
    public SwitchEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(ISwitchNode node)
    {
        startMapping(node);
        writeToken(ASEmitterTokens.SWITCH);
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);
        IASNode expressionNode = node.getChild(0);
        getWalker().walk(expressionNode);
        startMapping(node, expressionNode);
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        endMapping(node);
        IASNode statementContentsNode = node.getStatementContentsNode();
        startMapping(statementContentsNode);
        write(ASEmitterTokens.BLOCK_OPEN);
        endMapping(statementContentsNode);
        indentPush();
        writeNewline();

        IConditionalNode[] cnodes = ASNodeUtils.getCaseNodes(node);
        ITerminalNode dnode = ASNodeUtils.getDefaultNode(node);
        
        for (int i = 0; i < cnodes.length; i++)
        {
            IConditionalNode casen = cnodes[i];
            IContainerNode cnode = (IContainerNode) casen.getChild(1);
            startMapping(casen);
            writeToken(ASEmitterTokens.CASE);
            endMapping(casen);
            IExpressionNode conditionalExpressionNode = casen.getConditionalExpressionNode();
            getWalker().walk(conditionalExpressionNode);
            startMapping(casen, conditionalExpressionNode);
            write(ASEmitterTokens.COLON);
            if (!EmitterUtils.isImplicit(cnode))
                write(ASEmitterTokens.SPACE);
            endMapping(casen);
            getWalker().walk(casen.getStatementContentsNode());
            if (i == cnodes.length - 1 && dnode == null)
            {
                indentPop();
                writeNewline();
            }
            else
                writeNewline();
        }
        if (dnode != null)
        {
            IContainerNode cnode = (IContainerNode) dnode.getChild(0);
            startMapping(dnode);
            write(ASEmitterTokens.DEFAULT);
            write(ASEmitterTokens.COLON);
            if (!EmitterUtils.isImplicit(cnode))
                write(ASEmitterTokens.SPACE);
            endMapping(dnode);
            getWalker().walk(dnode);
            indentPop();
            writeNewline();
        }
        startMapping(node, node.getEndLine(), node.getEndColumn() - 1);
        write(ASEmitterTokens.BLOCK_CLOSE);
        endMapping(node);
    }
}
