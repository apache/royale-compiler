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
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IStatementNode;

public class StatementEmitter extends JSSubEmitter implements
        ISubEmitter<IASNode>
{
    public StatementEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IASNode node)
    {
        getWalker().walk(node);

        if(node.getNodeID() == ASTNodeID.ImportID)
        {
            //imports aren't emitted, so don't emit anything here either
            return;
        }

        // XXX (mschmalle) this should be in the after handler?
        if (node.getParent().getNodeID() != ASTNodeID.LabledStatementID
                && node.getNodeID() != ASTNodeID.ConfigBlockID
                && !(node instanceof IStatementNode))
        { //@todo - the following can generate a lonely ";\n" in the js output from 'super()' in the constructor call
            startMapping(node, node);
            write(ASEmitterTokens.SEMICOLON);
            endMapping(node);
        }

        if (!isLastStatement(node))
            writeNewline();
    }

    protected static boolean isLastStatement(IASNode node)
    {
        return getChildIndex(node.getParent(), node) == node.getParent()
                .getChildCount() - 1;
    }

    // this is not fair that we have to do this if (i < len - 1)
    private static int getChildIndex(IASNode parent, IASNode node)
    {
        final int len = parent.getChildCount();
        for (int i = 0; i < len; i++)
        {
            if (parent.getChild(i) == node)
                return i;
        }
        return -1;
    }
}
