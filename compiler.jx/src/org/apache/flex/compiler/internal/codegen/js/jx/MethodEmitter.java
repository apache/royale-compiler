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

import java.util.ArrayList;

import org.apache.flex.compiler.codegen.ISubEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitter;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSessionModel;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;

public class MethodEmitter extends JSSubEmitter implements
        ISubEmitter<IFunctionNode>
{
    public MethodEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IFunctionNode node)
    {
    	getModel().getMethods().add(node);
    	
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) getEmitter();

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(new ArrayList<ICompilerProblem>());

        ICompilerProject project = getWalker().getProject();

        fjs.getDocEmitter().emitMethodDoc(node, project);

        boolean isConstructor = node.isConstructor();

        String qname = null;
        IFunctionDefinition.FunctionClassification classification = fn.getFunctionClassification();
        if(classification == IFunctionDefinition.FunctionClassification.FILE_MEMBER ||
                classification == IFunctionDefinition.FunctionClassification.PACKAGE_MEMBER)
        {
            write(fjs.formatQualifiedName(fn.getQualifiedName()));
        }
        else
        {
            ITypeDefinition typeDef = EmitterUtils.getTypeDefinition(node);
            if (typeDef != null)
            {
                qname = typeDef.getQualifiedName();
            }
            if (qname != null && !qname.equals(""))
            {
                if (isConstructor)
                {
                    getEmitter().startMapping(node);
                }
                write(fjs.formatQualifiedName(qname));
                if (isConstructor)
                {
                    getEmitter().endMapping(node);
                }
                else
                {
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    if (!fn.hasModifier(ASModifier.STATIC))
                    {
                        write(JSEmitterTokens.PROTOTYPE);
                        write(ASEmitterTokens.MEMBER_ACCESS);
                    }
                }
            }
            if (!isConstructor)
            {
                getEmitter().startMapping(node);
                fjs.emitMemberName(node);
                getEmitter().endMapping(node);
            }
        }

        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.FUNCTION);

        fjs.emitParameters(node.getParameterNodes());

        boolean hasSuperClass = EmitterUtils.hasSuperClass(project, node);

        if (isConstructor && node.getScopedNode().getChildCount() == 0)
        {
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.BLOCK_OPEN);
            if (hasSuperClass)
                fjs.emitSuperCall(node, JSSessionModel.CONSTRUCTOR_EMPTY);
            writeNewline();
            IClassNode cnode = (IClassNode) node
            .getAncestorOfType(IClassNode.class);
            fjs.emitComplexInitializers(cnode);
            write(ASEmitterTokens.BLOCK_CLOSE);
        }

        if (!isConstructor || node.getScopedNode().getChildCount() > 0)
            fjs.emitMethodScope(node.getScopedNode());

        if (isConstructor && hasSuperClass)
        {
            writeNewline(ASEmitterTokens.SEMICOLON);
            write(JSGoogEmitterTokens.GOOG_INHERITS);
            write(ASEmitterTokens.PAREN_OPEN);
            write(fjs.formatQualifiedName(qname));
            writeToken(ASEmitterTokens.COMMA);
            String sname = EmitterUtils.getSuperClassDefinition(node, project)
                    .getQualifiedName();
            write(fjs.formatQualifiedName(sname));
            write(ASEmitterTokens.PAREN_CLOSE);
        }
    }
}
