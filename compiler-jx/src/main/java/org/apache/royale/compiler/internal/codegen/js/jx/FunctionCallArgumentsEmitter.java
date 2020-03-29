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
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.ISetterDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;

public class FunctionCallArgumentsEmitter extends JSSubEmitter implements
        ISubEmitter<IContainerNode>
{
    public FunctionCallArgumentsEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IContainerNode node)
    {
        startMapping(node);
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);

        emitContents(node);

        startMapping(node, node.getLine(), node.getColumn() + node.getAbsoluteEnd() - node.getAbsoluteStart() - 1);
        write(ASEmitterTokens.PAREN_CLOSE);
        endMapping(node);
    }
    
    public void emitContents(IContainerNode node) {
        IParameterDefinition[] paramDefs = null;
        IFunctionCallNode functionCallNode = (IFunctionCallNode) node.getAncestorOfType(IFunctionCallNode.class);
        if (functionCallNode != null)
        {
            IDefinition calledDef = functionCallNode.resolveCalledExpression(getProject());
            if (calledDef instanceof ISetterDefinition) {
                if (((ISetterDefinition)calledDef).resolveCorrespondingAccessor(getProject()) != null) {
                    calledDef = ((ISetterDefinition)calledDef).resolveCorrespondingAccessor(getProject());
                }
            }
            if (calledDef instanceof IFunctionDefinition)
            {
                IFunctionDefinition functionDef = (IFunctionDefinition) calledDef;
                paramDefs = functionDef.getParameters();
            }
        }
        //arguments needs patching to deal with something that seems off-spec (discussed in dev list)
        //check once for function, because it assumes a function with only one arg
        boolean needsQNamePatching = EmitterUtils.needsXMLQNameArgumentsPatch(functionCallNode, getProject());
    
        int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            IExpressionNode argumentNode = (IExpressionNode) node.getChild(i);
            IDefinition paramTypeDef = null;
            String postArgPatch = null;
            if (paramDefs != null && paramDefs.length > i)
            {
                IParameterDefinition paramDef = paramDefs[i];
                if (paramDef.isRest())
                {
                    paramDef = null;
                }
                if (paramDef != null)
                {
                    paramTypeDef = paramDef.resolveType(getProject());
                }
            }

            if (needsQNamePatching) {
                //check patch needed
                postArgPatch = ")";
                write("XML.swfCompatibleQuery(");
            }
            
            getEmitter().emitAssignmentCoercion(argumentNode, paramTypeDef);
        
            if (postArgPatch != null){
                write(postArgPatch); //reset to null on next iteration
            }
            
            if (i < len - 1)
            {
                //we're mapping the comma to the container, but we use the
                //parameter line/column in case the comma is not on the same
                //line as the opening (
                startMapping(node, argumentNode);
                writeToken(ASEmitterTokens.COMMA);
                endMapping(node);
            }
        }
    }
}
