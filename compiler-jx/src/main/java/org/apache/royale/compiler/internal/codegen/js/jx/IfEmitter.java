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
import org.apache.royale.compiler.tree.as.IIfNode;
import org.apache.royale.compiler.tree.as.ITerminalNode;

public class IfEmitter extends JSSubEmitter implements
        ISubEmitter<IIfNode>
{
    public IfEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IIfNode node)
    {
        IConditionalNode conditional = (IConditionalNode) node.getChild(0);
        emitConditional(conditional, false);

        IConditionalNode[] nodes = node.getElseIfNodes();
        if (nodes.length > 0)
        {
            for (int i = 0; i < nodes.length; i++)
            {
                IConditionalNode enode = nodes[i];
                IContainerNode snode = (IContainerNode) enode
                        .getStatementContentsNode();

                final boolean isImplicit = EmitterUtils.isImplicit(snode);
                if (isImplicit)
                    writeNewline();
                else
                    write(ASEmitterTokens.SPACE);

                emitConditional(enode, true);
            }
        }

        ITerminalNode elseNode = node.getElseNode();
        if (elseNode != null)
        {
            emitElse(elseNode);
        }
        // if no actual work is done in the if clause, and there are no else/elseif causes
        // emit an empty block.  Closure doesn't like a plain semicolon.
        if (nodes.length == 0 && elseNode == null && conditional.getChild(1).getChildCount() == 0)
        {
        	write(ASEmitterTokens.BLOCK_OPEN);
        	writeNewline(ASEmitterTokens.BLOCK_CLOSE);
        }
        
    }

    protected void emitConditional(IConditionalNode node, boolean isElseIf)
    {
        startMapping(node);
        if (isElseIf)
        {
            writeToken(ASEmitterTokens.ELSE);
        }
        writeToken(ASEmitterTokens.IF);
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);

        IASNode conditionalExpression = node.getChild(0);
        getWalker().walk(conditionalExpression);

        startMapping(node, conditionalExpression);
        write(ASEmitterTokens.PAREN_CLOSE);
        IContainerNode xnode = (IContainerNode) node.getStatementContentsNode();
        if (!EmitterUtils.isImplicit(xnode))
            write(ASEmitterTokens.SPACE);
        endMapping(node);

        getWalker().walk(node.getChild(1)); // BlockNode
    }

    protected void emitElse(ITerminalNode node)
    {
        IContainerNode cnode = (IContainerNode) node.getChild(0);

        // if an implicit if, add a newline with no space
        final boolean isImplicit = EmitterUtils.isImplicit(cnode);
        if (isImplicit)
            writeNewline();
        else
            write(ASEmitterTokens.SPACE);

        startMapping(node);
        write(ASEmitterTokens.ELSE);
        if (!isImplicit)
            write(ASEmitterTokens.SPACE);
        endMapping(node);

        getWalker().walk(node); // TerminalNode
    }
}
