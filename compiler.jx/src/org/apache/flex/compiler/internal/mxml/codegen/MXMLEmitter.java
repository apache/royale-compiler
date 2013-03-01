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

import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.internal.as.codegen.ASEmitterTokens;
import org.apache.flex.compiler.internal.common.codegen.Emitter;
import org.apache.flex.compiler.mxml.codegen.IMXMLEmitter;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLIntNode;
import org.apache.flex.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;

/**
 * The base implementation for an MXML emitter.
 * 
 * @author Erik de Bruin
 */
public class MXMLEmitter extends Emitter implements IMXMLEmitter
{

    private MXMLBlockWalker walker;

    @Override
    public MXMLBlockWalker getMXMLWalker()
    {
        return walker;
    }

    @Override
    public void setMXMLWalker(MXMLBlockWalker value)
    {
        walker = value;
    }

    public MXMLEmitter(FilterWriter out)
    {
        super(out);
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitDocumentHeader(IMXMLDocumentNode node)
    {
        IClassDefinition cdef = node
                .getClassReference((ICompilerProject) walker.getProject());

        write("<" + cdef.getBaseName());

        IMXMLPropertySpecifierNode[] pnodes = node.getPropertySpecifierNodes();
        if (pnodes != null)
        {
            for (IMXMLPropertySpecifierNode pnode : pnodes)
            {
                if (!pnode.getName().equals("mxmlContentFactory"))
                    getMXMLWalker().walk(pnode);
            }
        }

        write(">");
        indentPush();
    }

    @Override
    public void emitDocumentFooter(IMXMLDocumentNode node)
    {
        IClassDefinition cdef = node
                .getClassReference((ICompilerProject) walker.getProject());

        writeNewline("", false);
        write("</" + cdef.getBaseName() + ">");
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IMXMLClassDefinitionNode node)
    {
        // fx:script tag contents
        IMXMLScriptNode[] snodes = node.getScriptNodes();
        if (snodes != null)
        {
            for (IMXMLScriptNode snode : snodes)
            {
                for (IASNode asnode : snode.getASNodes())
                {
                    getMXMLWalker().walk(asnode);
                }
            }
        }

        // "regular" tags (components)
        IMXMLPropertySpecifierNode[] pnodes = node.getPropertySpecifierNodes();
        if (pnodes != null)
        {
            for (IMXMLPropertySpecifierNode pnode : pnodes)
            {
                if (pnode.getName().equals("mxmlContentFactory"))
                    getMXMLWalker().walk(pnode);
            }
        }

        // fx:declarations tag contents
        IMXMLDeclarationsNode[] dnodes = node.getDeclarationsNodes();
        if (dnodes != null)
        {
            for (IMXMLDeclarationsNode dnode : dnodes)
            {
                getMXMLWalker().walk(dnode);
            }
        }
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitInstance(IMXMLInstanceNode node)
    {
        IClassDefinition cdef = node
                .getClassReference((ICompilerProject) getMXMLWalker()
                        .getProject());

        String cname = cdef.getBaseName();

        writeNewline();
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

        IMXMLPropertySpecifierNode[] pnodes = node.getPropertySpecifierNodes();
        final int len = pnodes.length;
        if (len != 0)
        {
            for (int i = 0; i < len; i++)
            {
                getMXMLWalker().walk(pnodes[i]);
            }
        }

        write(">");
        // TODO (erikdebruin) we need to parse any children, if present...
        write("<");
        write("/");
        write(cname);
        write(">");
    }

    @Override
    public void emitPropertySpecifier(IMXMLPropertySpecifierNode node)
    {
        boolean isMXMLContentFactory = node.getName().equals(
                "mxmlContentFactory");

        if (!isMXMLContentFactory)
        {
            write(ASEmitterTokens.SPACE);
            write(node.getName());
            write(ASEmitterTokens.EQUAL);
        }

        getMXMLWalker().walk(node.getInstanceNode());
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitArray(IMXMLArrayNode node)
    {
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            getMXMLWalker().walk(node.getChild(i));
        }
    }

    @Override
    public void emitInt(IMXMLIntNode node)
    {
        getMXMLWalker().walk(node.getChild(0));
    }

    @Override
    public void emitString(IMXMLStringNode node)
    {
        write("\"");

        getMXMLWalker().walk(node.getChild(0));

        write("\"");
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitLiteral(IMXMLLiteralNode node)
    {
        write(node.getValue().toString());
    }

}
