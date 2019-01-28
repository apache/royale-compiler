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
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IReturnNode;

public class ReturnEmitter extends JSSubEmitter implements
        ISubEmitter<IReturnNode>
{
    public ReturnEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IReturnNode node)
    {
        IExpressionNode rnode = node.getReturnValueNode();
        boolean hasReturnValue = rnode != null && rnode.getNodeID() != ASTNodeID.NilID;

        startMapping(node);
        write(ASEmitterTokens.RETURN);
        if (hasReturnValue)
        {
            write(ASEmitterTokens.SPACE);
        }
        endMapping(node);

        if (hasReturnValue)
        {
            IDefinition returnDef = null;
            IFunctionNode parentFn = (IFunctionNode) node.getAncestorOfType(IFunctionNode.class);
            if (parentFn != null)
            {
                IExpressionNode returnTypeNode = parentFn.getReturnTypeNode();
                if (returnTypeNode != null)
                {
                    returnDef = returnTypeNode.resolve(getProject());
                }
            }
            getEmitter().emitAssignmentCoercion(rnode, returnDef);
        }
    }
}
