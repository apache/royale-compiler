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
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IKeywordNode;
import org.apache.royale.compiler.tree.as.IVariableNode;

public class MemberKeywordEmitter extends JSSubEmitter implements
        ISubEmitter<IDefinitionNode>
{
    public MemberKeywordEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IDefinitionNode node)
    {
        IKeywordNode keywordNode = null;
        for(int i = 0; i < node.getChildCount(); i++)
        {
            IASNode childNode = node.getChild(i);
            if (childNode instanceof IKeywordNode)
            {
                keywordNode = (IKeywordNode) childNode;
                break;
            }
        }
        if (keywordNode != null)
        {
            startMapping(keywordNode);
        }
        if (node instanceof IFunctionNode)
        {
            writeToken(ASEmitterTokens.FUNCTION);
        }
        else if (node instanceof IVariableNode)
        {
            writeToken(ASEmitterTokens.VAR);
        }
        if (keywordNode != null)
        {
            endMapping(keywordNode);
        }
    }
}
