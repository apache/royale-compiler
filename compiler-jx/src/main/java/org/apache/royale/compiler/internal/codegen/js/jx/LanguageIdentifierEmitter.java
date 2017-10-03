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
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;

public class LanguageIdentifierEmitter extends JSSubEmitter implements
        ISubEmitter<ILanguageIdentifierNode>
{
    public LanguageIdentifierEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(ILanguageIdentifierNode node)
    {
        startMapping((ISourceLocation) node);
        if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.ANY_TYPE)
        {
            write(ASEmitterTokens.ANY_TYPE);
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.REST)
        {
            write(ASEmitterTokens.ELLIPSIS);
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.SUPER)
        {
            write(ASEmitterTokens.SUPER);
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.THIS)
        {
            write(ASEmitterTokens.THIS);
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.VOID)
        {
            write(ASEmitterTokens.VOID);
        }
        endMapping((ISourceLocation) node);
    }
}
