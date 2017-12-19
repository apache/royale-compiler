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

package org.apache.royale.compiler.internal.codegen.wast;

import java.io.FilterWriter;

import org.apache.royale.compiler.codegen.IEmitterTokens;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitter;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IAccessorNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.ISetterNode;

public class WASTEmitter extends ASEmitter {

    public WASTEmitter(FilterWriter out)
    {
        super(out);
    }

    @Override
    public void writeToken(IEmitterTokens value)
    {
        writeToken(value, true);
    }

    public void writeToken(IEmitterTokens value, boolean insertSpace)
    {
        write(value.getToken());
        if (insertSpace) {
          write(ASEmitterTokens.SPACE);
        }
    }

    //--------------------------------------------------------------------------
    //  Package
    //--------------------------------------------------------------------------

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
     	//
    }

    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
     	writeToken("(module\n" + 
     			"  (func $add (param $lhs i32) (param $rhs i32) (result i32)\n" + 
     			"    get_local $lhs\n" + 
     			"    get_local $rhs\n" + 
     			"    i32.add)\n" + 
     			"  (export \"add\" (func $add))\n" + 
     			")");
    }

    //--------------------------------------------------------------------------
    //  Class
    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IClassNode node)
    {
	    writeToken(ASEmitterTokens.PAREN_OPEN, false);
	    writeToken(WASTEmitterTokens.MODULE, false);
	    writeToken(ASEmitterTokens.NEW_LINE);
	    writeToken(ASEmitterTokens.NEW_LINE);
	    writeToken(ASEmitterTokens.PAREN_CLOSE, false);
    }

    //--------------------------------------------------------------------------
    //  Method
    //--------------------------------------------------------------------------

    @Override
    public void emitMethod(IFunctionNode node)
    {
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(getProblems());

        IFunctionDefinition definition = node.getDefinition();

        emitNamespaceIdentifier(node);
        emitModifiers(definition);
        emitMemberKeyword(node);

        /*
        // see below, this is temp, I don't want a bunch of duplicated code
        // at them moment, subclasses can refine anyways, we are generalizing
        if (node instanceof IGetterNode)
        {
            emitGetAccessorDocumentation((IGetterNode) node);
        }
        else if (node instanceof ISetterNode)
        {
            emitSetAccessorDocumentation((ISetterNode) node);
        }
        else
        {
            emitMethodDocumentation(node);
        }

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(getProblems());

        IFunctionDefinition definition = node.getDefinition();

        emitNamespaceIdentifier(node);
        emitModifiers(definition);
        emitMemberKeyword(node);

        // I'm cheating right here, I haven't "seen" the light
        // on how to properly and efficiently deal with accessors since they are SO alike
        // I don't want to lump them in with methods because implementations in the
        // future need to know the difference without loopholes
        if (node instanceof IAccessorNode)
        {
            emitAccessorKeyword(((IAccessorNode) node).getAccessorKeywordNode());
        }

        emitMemberName(node);
        emitParameters(node.getParametersContainerNode());
        emitType(node.getReturnTypeNode());
        if (node.getParent().getParent().getNodeID() == ASTNodeID.ClassID)
        {
            emitMethodScope(node.getScopedNode());
        }

        // the client such as IASBlockWalker is responsible for the 
        // semi-colon and newline handling
        */
    }

}
