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
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.tree.as.ChainedVariableNode;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IEmbedNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;

public class VarDeclarationEmitter extends JSSubEmitter implements
        ISubEmitter<IVariableNode>
{

    public VarDeclarationEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IVariableNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) getEmitter();

        if (!(node instanceof ChainedVariableNode) && !node.isConst())
        {
            fjs.emitMemberKeyword(node);
        }

        IExpressionNode avnode = node.getAssignedValueNode();
        if (avnode != null)
        {
            IDefinition def = avnode.resolveType(getWalker().getProject());

            String opcode = avnode.getNodeID().getParaphrase();
            if (opcode != "AnonymousFunction")
                fjs.getDocEmitter().emitVarDoc(node, def);
        }
        else
        {
            fjs.getDocEmitter().emitVarDoc(node, null);
        }

        fjs.emitDeclarationName(node);
        if (!(avnode instanceof IEmbedNode))
            fjs.emitAssignedValue(avnode);

        if (!(node instanceof ChainedVariableNode))
        {
            // check for chained variables
            int len = node.getChildCount();
            for (int i = 0; i < len; i++)
            {
                IASNode child = node.getChild(i);
                if (child instanceof ChainedVariableNode)
                {
                    writeToken(ASEmitterTokens.COMMA);
                    fjs.emitVarDeclaration((IVariableNode) child);
                }
            }
        }
    }

}
