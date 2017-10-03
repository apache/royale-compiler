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
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IParameterNode;

public class ParametersEmitter extends JSSubEmitter implements
        ISubEmitter<IContainerNode>
{
    public ParametersEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IContainerNode node)
    {
        startMapping(node);
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);

        int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            IParameterNode parameterNode = (IParameterNode) node.getChild(i);
            getWalker().walk(parameterNode); //emitParameter
            if (i < len - 1)
            {
                //we're mapping the comma to the container, but we use the
                //parameter line/column in case the comma is not on the same
                //line as the opening (
                startMapping(node, parameterNode);
                writeToken(ASEmitterTokens.COMMA);
                endMapping(node);
            }
        }

        startMapping(node, node.getLine(), node.getColumn() + node.getAbsoluteEnd() - node.getAbsoluteStart() - 1);
        write(ASEmitterTokens.PAREN_CLOSE);
        endMapping(node);
    }
}
