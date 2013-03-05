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

package org.apache.flex.compiler.internal.codegen.mxml.flexjs;

import java.io.FilterWriter;

import org.apache.flex.compiler.codegen.mxml.flexjs.IMXMLFlexJSEmitter;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.mxml.MXMLEmitter;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;

/**
 * The base implementation for an MXML emitter.
 * 
 * @author Erik de Bruin
 */
public class MXMLFlexJSEmitter extends MXMLEmitter implements
        IMXMLFlexJSEmitter
{

    public MXMLFlexJSEmitter(FilterWriter out)
    {
        super(out);
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitDocumentHeader(IMXMLDocumentNode node)
    {
        IClassDefinition cdef = node
                .getClassReference((ICompilerProject) walker.getProject());

        System.out.println("Emit Document Header: " + cdef.getBaseName()
                + " :: " + cdef.getQualifiedName());

        write("<" + cdef.getBaseName());

        IMXMLEventSpecifierNode[] enodes = node.getEventSpecifierNodes();
        if (enodes != null)
        {
            for (IMXMLEventSpecifierNode enode : enodes)
            {
                getMXMLWalker().walk(enode);
            }
        }

        write(">");
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitEventSpecifier(IMXMLEventSpecifierNode node)
    {
        IDefinition cdef = node.getDefinition();

        System.out.println("Emit Event Specifier: " + cdef.getBaseName()
                + " :: " + cdef.getQualifiedName());

        write(ASEmitterTokens.SPACE);
        write(cdef.getBaseName());
        write(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.DOUBLE_QUOTE);

        getMXMLWalker().walk(node.getChild(0));

        write(ASEmitterTokens.DOUBLE_QUOTE);
    }

    @Override
    public void emitInstance(IMXMLInstanceNode node)
    {
        IClassDefinition cdef = node
                .getClassReference((ICompilerProject) getMXMLWalker()
                        .getProject());

        System.out.println("Emit Instance: " + cdef.getBaseName() + " :: "
                + cdef.getQualifiedName());

        String cname = cdef.getBaseName();

        writeNewline("", true);
        write("<");
        write(cname);
        if (node.getID() != null && node.getID() != "")
        {
            write(ASEmitterTokens.SPACE);
            write("id");
            write(ASEmitterTokens.EQUAL);
            write("\"");
            write(node.getID());
            write("\"");
        }
        write(">");

        IMXMLPropertySpecifierNode[] pnodes = node.getPropertySpecifierNodes();
        if (pnodes != null)
        {
            for (IMXMLPropertySpecifierNode pnode : pnodes)
            {
                if (!isMXMLContentNode(pnode))
                    getMXMLWalker().walk(pnode); // Property Specifier
            }
        }

        writeNewline("");
        write("<");
        write("/");
        write(cname);
        writeNewline(">", false);
    }

    @Override
    public void emitPropertySpecifier(IMXMLPropertySpecifierNode node)
    {
        IDefinition cdef = node.getDefinition();

        System.out.println("Emit Property Specifier: " + cdef.getBaseName()
                + " :: " + cdef.getQualifiedName());

        writeNewline("", true);
        write("<" + node.getName() + ">");

        getMXMLWalker().walk(node.getChild(0)); // Array

        write("</" + node.getName() + ">");
        indentPop();
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitArray(IMXMLArrayNode node)
    {
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            getMXMLWalker().walk(node.getChild(i)); // Instance
        }
    }

}
