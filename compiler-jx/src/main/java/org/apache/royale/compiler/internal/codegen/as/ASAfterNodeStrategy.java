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

package org.apache.royale.compiler.internal.codegen.as;

import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IContainerNode.ContainerType;
import org.apache.royale.compiler.visitor.IASNodeStrategy;

/**
 * A concrete implementation of the {@link IASNodeStrategy} that allows
 * {@link IASNode} processing after the current node handler.
 * <p>
 * The class has access to the current {@link IJSEmitter} instance being used to
 * output source code to the current output buffer.
 * 
 * @author Michael Schmalle
 */
public class ASAfterNodeStrategy implements IASNodeStrategy
{
    private final IASEmitter emitter;

    public ASAfterNodeStrategy(IASEmitter emitter)
    {
        this.emitter = emitter;
    }

    @Override
    public void handle(IASNode node)
    {
        if (node.getNodeID() == ASTNodeID.BlockID)
        {
            IContainerNode container = (IContainerNode) node;
            ContainerType type = container.getContainerType();
            if (type != ContainerType.IMPLICIT
                    && type != ContainerType.SYNTHESIZED)
            {
                if (node.getChildCount() != 0)
                {
                    emitter.indentPop();
                    emitter.writeNewline();
                }

                emitter.emitBlockClose(container);
            }
            else if (type == ContainerType.IMPLICIT
                    || type == ContainerType.SYNTHESIZED)
            {
                if (node.getChildCount() != 0)
                {
                    emitter.indentPop();
                }
            }
        }
    }
}
