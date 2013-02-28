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

package org.apache.flex.compiler.internal.mxml.codegen;

import java.io.FilterWriter;

import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.internal.common.codegen.Emitter;
import org.apache.flex.compiler.mxml.codegen.IMXMLEmitter;

/**
 * The base implementation for an MXML emitter.
 * 
 * @author Erik de Bruin
 */
public class MXMLEmitter extends Emitter implements IMXMLEmitter
{

    private MXMLBlockWalker walker;

    public MXMLBlockWalker getMXMLWalker()
    {
        return walker;
    }

    public void setMXMLWalker(MXMLBlockWalker value)
    {
        walker = value;
    }

    public MXMLEmitter(FilterWriter out)
    {
        super(out);
    }

    //--------------------------------------------------------------------------

    public void emitPackageHeader(IPackageDefinition definition)
    {
        write("Hello World ;-)");

//        IPackageNode node = definition.getNode();
//        String name = node.getQualifiedName();
//        if (name != null && !name.equals(""))
//        {
//            write(MXMLEmitterTokens.SPACE);
//            getWalker().walk(node.getNameExpressionNode());
//        }
//
//        write(MXMLEmitterTokens.SPACE);
//        write(MXMLEmitterTokens.BLOCK_OPEN);
    }
    
}
