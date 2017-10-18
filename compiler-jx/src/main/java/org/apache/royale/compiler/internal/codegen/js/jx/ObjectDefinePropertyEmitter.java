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
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.SetterNode;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IAccessorNode;

public class ObjectDefinePropertyEmitter extends JSSubEmitter implements
        ISubEmitter<IAccessorNode>
{

    public ObjectDefinePropertyEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IAccessorNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();

        //TODO: ajh  is this method needed anymore?

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(fjs.getProblems());

        IFunctionDefinition definition = node.getDefinition();
        ITypeDefinition type = (ITypeDefinition) definition.getParent();

        if (type == null)
            return;

        boolean isBindableSetter = false;
        if (node instanceof SetterNode)
        {
            IMetaInfo[] metaInfos = null;
            metaInfos = node.getMetaInfos();
            for (IMetaInfo metaInfo : metaInfos)
            {
                String name = metaInfo.getTagName();
                if (name.equals("Bindable")
                        && metaInfo.getAllAttributes().length == 0)
                {
                    isBindableSetter = true;
                    break;
                }
            }
        }
        if (isBindableSetter)
        {
            fjs.getDocEmitter().emitMethodDoc(fn, getWalker().getProject());
            write(fjs.formatQualifiedName(type.getQualifiedName()));
            if (!node.hasModifier(ASModifier.STATIC))
            {
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSEmitterTokens.PROTOTYPE);
            }

            write(ASEmitterTokens.MEMBER_ACCESS);
            write("__bindingWrappedSetter__");
            writeToken(node.getName());
            writeToken(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.FUNCTION);
            fjs.emitParameters(node.getParametersContainerNode());
            //writeNewline();
            fjs.emitMethodScope(node.getScopedNode());
        }

        super_emitObjectDefineProperty(node);
    }

    protected void super_emitObjectDefineProperty(IAccessorNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();

        /*
        Object.defineProperty(
            A.prototype, 
            'foo', 
            {get: function() {return -1;}, 
            configurable: true}
         );
        */

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(fjs.getProblems());

        // head
        write(JSGoogEmitterTokens.OBJECT);
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSEmitterTokens.DEFINE_PROPERTY);
        fjs.writeNewline(ASEmitterTokens.PAREN_OPEN, true);

        // Type
        IFunctionDefinition definition = node.getDefinition();
        ITypeDefinition type = (ITypeDefinition) definition.getParent();
        write(type.getQualifiedName());
        if (!node.hasModifier(ASModifier.STATIC))
        {
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.PROTOTYPE);
        }
        writeToken(ASEmitterTokens.COMMA);
        writeNewline();

        // name
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(definition.getBaseName());
        write(ASEmitterTokens.SINGLE_QUOTE);
        writeToken(ASEmitterTokens.COMMA);
        writeNewline();

        // info object
        // declaration
        write(ASEmitterTokens.BLOCK_OPEN);
        write(node.getNodeID() == ASTNodeID.GetterID ? ASEmitterTokens.GET
                : ASEmitterTokens.SET);
        write(ASEmitterTokens.COLON);
        write(ASEmitterTokens.FUNCTION);
        fjs.emitParameters(node.getParametersContainerNode());

        fjs.emitDefinePropertyFunction(node);

        writeToken(ASEmitterTokens.COMMA);
        write(JSEmitterTokens.CONFIGURABLE);
        write(ASEmitterTokens.COLON);
        write(ASEmitterTokens.TRUE);
        fjs.writeNewline(ASEmitterTokens.BLOCK_CLOSE, false);

        // tail, no colon; parent container will add it
        write(ASEmitterTokens.PAREN_CLOSE);
    }
}
