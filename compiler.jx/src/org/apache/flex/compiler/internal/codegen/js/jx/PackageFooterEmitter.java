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
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSDocEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.ITypeNode;

public class PackageFooterEmitter extends JSSubEmitter implements
        ISubEmitter<IPackageDefinition>
{

    public PackageFooterEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IPackageDefinition definition)
    {
        IASScope containedScope = definition.getContainedScope();
        ITypeDefinition type = EmitterUtils.findType(containedScope
                .getAllLocalDefinitions());
        if (type == null)
            return;

    }

    public void emitClassInfo(ITypeNode tnode)
    {
        JSFlexJSDocEmitter doc = (JSFlexJSDocEmitter) getEmitter()
        .getDocEmitter();

	    /*
	     * Metadata
	     * 
	     * @type {Object.<string, Array.<Object>>}
	     */
	    writeNewline();
	    writeNewline();
	    writeNewline();
	    doc.begin();
	    writeNewline(" * Metadata");
	    writeNewline(" *");
	    writeNewline(" * @type {Object.<string, Array.<Object>>}");
	    doc.end();
	
	    // a.B.prototype.AFJS_CLASS_INFO = {  };
	    write(getEmitter().formatQualifiedName(tnode.getQualifiedName()));
	    write(ASEmitterTokens.MEMBER_ACCESS);
	    write(JSEmitterTokens.PROTOTYPE);
	    write(ASEmitterTokens.MEMBER_ACCESS);
	    writeToken(JSFlexJSEmitterTokens.FLEXJS_CLASS_INFO);
	    writeToken(ASEmitterTokens.EQUAL);
	    writeToken(ASEmitterTokens.BLOCK_OPEN);
	
	    // names: [{ name: '', qName: '' }]
	    write(JSFlexJSEmitterTokens.NAMES);
	    writeToken(ASEmitterTokens.COLON);
	    write(ASEmitterTokens.SQUARE_OPEN);
	    writeToken(ASEmitterTokens.BLOCK_OPEN);
	    write(JSFlexJSEmitterTokens.NAME);
	    writeToken(ASEmitterTokens.COLON);
	    write(ASEmitterTokens.SINGLE_QUOTE);
	    write(tnode.getName());
	    write(ASEmitterTokens.SINGLE_QUOTE);
	    writeToken(ASEmitterTokens.COMMA);
	    write(JSFlexJSEmitterTokens.QNAME);
	    writeToken(ASEmitterTokens.COLON);
	    write(ASEmitterTokens.SINGLE_QUOTE);
	    write(getEmitter().formatQualifiedName(tnode.getQualifiedName()));
	    write(ASEmitterTokens.SINGLE_QUOTE);
	    write(ASEmitterTokens.BLOCK_CLOSE);
	    write(ASEmitterTokens.SQUARE_CLOSE);
	
	    IExpressionNode[] enodes;
	    if (tnode instanceof IClassNode)
	        enodes = ((IClassNode) tnode).getImplementedInterfaceNodes();
	    else
	        enodes = ((IInterfaceNode) tnode).getExtendedInterfaceNodes();
	
	    if (enodes.length > 0)
	    {
	        writeToken(ASEmitterTokens.COMMA);
	
	        // interfaces: [a.IC, a.ID]
	        write(JSFlexJSEmitterTokens.INTERFACES);
	        writeToken(ASEmitterTokens.COLON);
	        write(ASEmitterTokens.SQUARE_OPEN);
	        int i = 0;
	        for (IExpressionNode enode : enodes)
	        {
	        	IDefinition edef = enode.resolve(getProject());
	        	if (edef == null)
	        		continue;
	            write(getEmitter().formatQualifiedName(
	                    edef.getQualifiedName()));
	            if (i < enodes.length - 1)
	                writeToken(ASEmitterTokens.COMMA);
	            i++;
	        }
	        write(ASEmitterTokens.SQUARE_CLOSE);
	    }
	
	    write(ASEmitterTokens.SPACE);
	    write(ASEmitterTokens.BLOCK_CLOSE);
	    writeNewline(ASEmitterTokens.SEMICOLON);

    }
}
