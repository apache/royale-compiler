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

import java.util.ArrayList;

import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.ImplicitBindableImplementation;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.utils.ASTUtil;

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
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(new ArrayList<ICompilerProblem>());

        ICompilerProject project = getWalker().getProject();

        fjs.getDocEmitter().emitMethodDoc(node, project);
        ASTUtil.processFunctionNode(fn, project);
        
        boolean isConstructor = node.isConstructor();

        boolean addingBindableImplementsSupport = isConstructor &&
                getModel().getImplicitBindableImplementation() == ImplicitBindableImplementation.IMPLEMENTS;

        boolean addingBindableExtendsSupport = !addingBindableImplementsSupport
                                        && isConstructor
                                        && getModel().getImplicitBindableImplementation() == ImplicitBindableImplementation.EXTENDS;

        String qname = null;
        IFunctionDefinition.FunctionClassification classification = fn.getFunctionClassification();
        if(classification == IFunctionDefinition.FunctionClassification.FILE_MEMBER ||
                classification == IFunctionDefinition.FunctionClassification.PACKAGE_MEMBER)
        {
            String qualifiedName = node.getQualifiedName();
            if (fjs.getModel().isExterns && node.getName().equals(qualifiedName))
            {
                writeToken(ASEmitterTokens.VAR);
            }
            write(fjs.formatQualifiedName(qualifiedName));
        }
        else
        {
            startMapping(node.getNameExpressionNode());
            ITypeDefinition typeDef = EmitterUtils.getTypeDefinition(node);
            if (typeDef != null)
            {
                qname = typeDef.getQualifiedName();
            }
            if (qname != null && !qname.equals(""))
            {
                if (isConstructor && fjs.getModel().isExterns && typeDef.getBaseName().equals(qname))
                {
                    writeToken(ASEmitterTokens.VAR);
                }
                write(fjs.formatQualifiedName(qname));
                if (!isConstructor)
                {
                    if (!fn.hasModifier(ASModifier.STATIC))
                    {
                        write(ASEmitterTokens.MEMBER_ACCESS);
                        write(JSEmitterTokens.PROTOTYPE);
                    }
                    if (!fjs.isCustomNamespace(fn))
                    	write(ASEmitterTokens.MEMBER_ACCESS);
                }
            }
            if (!isConstructor)
            {
                fjs.emitMemberName(node);
            }
            endMapping(node.getNameExpressionNode());
        }
        if (node.getMetaTags() != null) {
            //offset mapping by any metadata tags that will be in the first child node
            startMapping(node.getChild(1));
        } else {
            startMapping(node);
        }
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.FUNCTION);
        endMapping(node);

        fjs.emitParameters(node.getParametersContainerNode());

        boolean hasSuperClass = EmitterUtils.hasSuperClass(project, node);

        if (isConstructor && node.getScopedNode().getChildCount() == 0)
        {
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.BLOCK_OPEN);
            if (hasSuperClass && !getEmitter().getModel().isExterns)
                fjs.emitSuperCall(node, JSSessionModel.CONSTRUCTOR_EMPTY);
            //add whatever variant of the bindable implementation is necessary inside the constructor
            if (addingBindableImplementsSupport) {
                writeNewline("",true);
                fjs.getBindableEmitter().emitBindableImplementsConstructorCode(true);
            } else if (addingBindableExtendsSupport) {
                IClassDefinition classDefinition = (IClassDefinition) node.getDefinition().getAncestorOfType(IClassDefinition.class);
                fjs.getBindableEmitter().emitBindableExtendsConstructorCode(classDefinition.getQualifiedName(),true);
            } else
                writeNewline();
            IClassNode cnode = (IClassNode) node.getAncestorOfType(IClassNode.class);
            fjs.emitComplexInitializers(cnode);

            write(ASEmitterTokens.BLOCK_CLOSE);
        }

        if (!isConstructor || node.getScopedNode().getChildCount() > 0)
        {
            fjs.emitMethodScope(node.getScopedNode());
        }

        if (isConstructor && !getEmitter().getModel().isExterns)
        {
            if (hasSuperClass) {
                writeNewline(ASEmitterTokens.SEMICOLON);
                write(JSGoogEmitterTokens.GOOG_INHERITS);
                write(ASEmitterTokens.PAREN_OPEN);
                write(fjs.formatQualifiedName(qname));
                writeToken(ASEmitterTokens.COMMA);
                String sname = EmitterUtils.getSuperClassDefinition(node, project)
                        .getQualifiedName();
                write(fjs.formatQualifiedName(sname));
                write(ASEmitterTokens.PAREN_CLOSE);
            } else if (addingBindableExtendsSupport) {
                //add goog.inherits for the 'extends' bindable implementation support
                writeNewline(ASEmitterTokens.SEMICOLON);
                writeNewline("// Compiler generated Binding support implementation:");
                write(JSGoogEmitterTokens.GOOG_INHERITS);
                write(ASEmitterTokens.PAREN_OPEN);
                write(fjs.formatQualifiedName(qname));
                writeToken(ASEmitterTokens.COMMA);
                write(fjs.formatQualifiedName(BindableEmitter.DISPATCHER_CLASS_QNAME));
                write(ASEmitterTokens.PAREN_CLOSE);
            }
        }
    }
}
