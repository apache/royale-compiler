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

package org.apache.flex.compiler.internal.codegen.js;

import java.io.FilterWriter;

import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.internal.codegen.as.ASEmitter;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IFunctionObjectNode;

/**
 * @author Michael Schmalle
 */
public class JSEmitter extends ASEmitter implements IJSEmitter
{
    private JSSessionModel model;
    
    @Override
    public JSSessionModel getModel()
    {
        return model;
    }

    public JSEmitter(FilterWriter out)
    {
        super(out);
        
        model = new JSSessionModel();
    }

    @Override
    public String formatQualifiedName(String name)
    {
        return name;
    }
    
    @Override
    public void emitLocalNamedFunction(IFunctionNode node)
    {
        FunctionNode fnode = (FunctionNode)node;
        write(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.SPACE);
        write(fnode.getName());
        emitParameters(fnode.getParameterNodes());
        emitFunctionScope(fnode.getScopedNode());
    }
    
    @Override
    public void emitFunctionObject(IFunctionObjectNode node)
    {
        FunctionNode fnode = node.getFunctionNode();
        write(ASEmitterTokens.FUNCTION);
        emitParameters(fnode.getParameterNodes());
        emitFunctionScope(fnode.getScopedNode());
    }

    public void emitClosureStart()
    {
    	
    }

    public void emitClosureEnd(IASNode node)
    {
    	
    }

}
