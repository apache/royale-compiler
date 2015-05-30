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
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;

public class BindableEmitter extends JSSubEmitter implements
        ISubEmitter<IClassDefinition>
{
    public BindableEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IClassDefinition definition)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) getEmitter();

        if (getModel().hasBindableVars())
        {
            write(JSGoogEmitterTokens.OBJECT);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.DEFINE_PROPERTIES);
            write(ASEmitterTokens.PAREN_OPEN);
            String qname = definition.getQualifiedName();
            write(fjs.formatQualifiedName(qname));
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.PROTOTYPE);
            write(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.SPACE);
            write("/** @lends {" + fjs.formatQualifiedName(qname)
                    + ".prototype} */ ");
            writeNewline(ASEmitterTokens.BLOCK_OPEN);

            boolean firstTime = true;
            for (String varName : getModel().getBindableVars())
            {
                if (firstTime)
                    firstTime = false;
                else
                    write(ASEmitterTokens.COMMA);

                fjs.emitBindableVarDefineProperty(varName, definition);
            }
            writeNewline(ASEmitterTokens.BLOCK_CLOSE);
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SEMICOLON);
        }
    }
}
