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
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IParameterNode;

public class ParameterEmitter extends JSSubEmitter implements
        ISubEmitter<IParameterNode>
{
    public ParameterEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IParameterNode node)
    {
        startMapping(node);
        if (node.isRest())
        {
            write(ASEmitterTokens.ELLIPSIS);
            write(node.getName());
        }
        else
        {
            getWalker().walk(node.getNameExpressionNode());
            write(ASEmitterTokens.COLON);
            getWalker().walk(node.getVariableTypeNode());
            IExpressionNode anode = node.getAssignedValueNode();
            if (anode != null)
            {
                write(ASEmitterTokens.SPACE);
                writeToken(ASEmitterTokens.EQUAL);
                getWalker().walk(anode);
            }
        }
        endMapping(node);
    }
}
