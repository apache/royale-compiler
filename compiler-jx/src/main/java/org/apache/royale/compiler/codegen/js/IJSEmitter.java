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

package org.apache.royale.compiler.codegen.js;

import java.io.Writer;

import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.compiler.visitor.IASNodeStrategy;

/**
 * The {@link IJSEmitter} interface allows abstraction between the
 * {@link IASNodeStrategy} and the current output buffer {@link Writer}.
 * 
 * @author Michael Schmalle
 */
public interface IJSEmitter extends IASEmitter, IMappingEmitter
{
    JSSessionModel getModel();
    
    String formatQualifiedName(String name);
    String formatPrivateName(String className, String name);
    String formatPrivateName(String className, String name, Boolean nameFirst);
    
    void emitSourceMapDirective(ITypeNode node);
    
    void emitClosureStart();
    void emitClosureEnd(IASNode node, IDefinition nodeDef);

    void emitAssignmentCoercion(IExpressionNode assignedNode, IDefinition definition);
}
