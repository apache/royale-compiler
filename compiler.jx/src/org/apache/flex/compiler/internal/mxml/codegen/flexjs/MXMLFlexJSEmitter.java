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

package org.apache.flex.compiler.internal.mxml.codegen.flexjs;

import java.io.FilterWriter;

import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.internal.as.codegen.ASEmitterTokens;
import org.apache.flex.compiler.internal.mxml.codegen.MXMLEmitter;
import org.apache.flex.compiler.mxml.codegen.flexjs.IMXMLFlexJSEmitter;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;

/**
 * The base implementation for an MXML emitter.
 * 
 * @author Erik de Bruin
 */
public class MXMLFlexJSEmitter extends MXMLEmitter implements IMXMLFlexJSEmitter
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

        writeNewline("<" + cdef.getBaseName() + ">", true);
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IMXMLClassDefinitionNode node)
    {
        // "regular" tags
        IMXMLPropertySpecifierNode[] pnodes = node.getPropertySpecifierNodes();
        if (pnodes != null)
        {
            for (IMXMLPropertySpecifierNode pnode : pnodes)
            {
                getMXMLWalker().walk(pnode);
            }
        }
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitInstance(IMXMLInstanceNode node)
    {
        String cname = node.getName();

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
//
//        IMXMLPropertySpecifierNode[] pnodes = node.getPropertySpecifierNodes();
//
//        // attributes
//        emitPropertySpecifiers(pnodes, true);

        write(">");

        // child nodes
        int len = node.getChildCount();
        if (len > 0)
        {
            for (int i = 0; i < len; i++)
            {
                IASNode cnode = node.getChild(i);
                
                getMXMLWalker().walk(cnode);
            }
        }

        write("<");
        write("/");
        write(cname);
        write(">");
    }

    @Override
    public void emitPropertySpecifier(IMXMLPropertySpecifierNode  node)
    {
        String cname = node.getName();

        write("<");
        write(cname);
//        if (node.getID() != null && node.getID() != "")
//        {
//            write(ASEmitterTokens.SPACE);
//            write("id");
//            write(ASEmitterTokens.EQUAL);
//            write("\"");
//            write(node.getID());
//            write("\"");
//        }
//
//        IMXMLPropertySpecifierNode[] pnodes = node.getPropertySpecifierNodes();
//
//        // attributes
//        emitPropertySpecifiers(pnodes, true);

        write(">");

        // child nodes
        int len = node.getChildCount();
        if (len > 0)
        {
            for (int i = 0; i < len; i++)
            {
                IASNode cnode = node.getChild(i);
                
                getMXMLWalker().walk(cnode);
            }
        }

        write("<");
        write("/");
        write(cname);
        write(">");
    }

}
