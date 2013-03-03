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
import org.apache.flex.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLIntNode;
import org.apache.flex.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNumberNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLUintNode;

/**
 * The base implementation for an MXML emitter.
 * 
 * @author Erik de Bruin
 */
public class MXMLEmitter extends Emitter implements IMXMLEmitter
{

    protected MXMLBlockWalker walker;

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

        emitPropertySpecifiers(node.getPropertySpecifierNodes(), true);

        writeNewline(">", true);
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

        // fx:declarations
        IMXMLDeclarationsNode[] dnodes = node.getDeclarationsNodes();
        if (dnodes != null)
        {
            for (IMXMLDeclarationsNode dnode : dnodes)
            {
                getMXMLWalker().walk(dnode);
            }
        }

        // fx:script
        IMXMLScriptNode[] snodes = node.getScriptNodes();
        if (snodes != null)
        {
            for (IMXMLScriptNode snode : snodes)
            {
                getMXMLWalker().walk(snode);
            }
        }

        // "regular" tags
        emitPropertySpecifiers(node.getPropertySpecifierNodes(), false);
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitInstance(IMXMLInstanceNode node)
    {
        IClassDefinition cdef = node
                .getClassReference((ICompilerProject) getMXMLWalker()
                        .getProject());

        String cname = cdef.getBaseName();

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

        // attributes
        emitPropertySpecifiers(pnodes, true);

        write(">");

        // child nodes
        emitPropertySpecifiers(pnodes, false);

        write("<");
        write("/");
        write(cname);
        write(">");
    }

    @Override
    public void emitPropertySpecifier(IMXMLPropertySpecifierNode node)
    {
        if (!isMXMLContentNode(node)) // only for attributes
        {
            write(node.getName());
            write(ASEmitterTokens.EQUAL);
        }

        getMXMLWalker().walk(node.getInstanceNode());
    }

    @Override
    public void emitStyleSpecifier(IMXMLStyleSpecifierNode node)
    {
        if (!isMXMLContentNode(node)) // only for attributes
        {
            write(node.getName());
            write(ASEmitterTokens.EQUAL);
        }

        getMXMLWalker().walk(node.getInstanceNode());
    }

    @Override
    public void emitScript(IMXMLScriptNode node)
    {
        // TODO (erikdebruin) handle AS script parsing...
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitArray(IMXMLArrayNode node)
    {
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            IASNode child = node.getChild(i);

            getMXMLWalker().walk(child);

            if (child instanceof IMXMLInstanceNode && i < len - 1)
                writeNewline();
        }
    }

    @Override
    public void emitBoolean(IMXMLBooleanNode node)
    {
        emitAttributeValue(node);
    }

    @Override
    public void emitInt(IMXMLIntNode node)
    {
        emitAttributeValue(node);
    }

    @Override
    public void emitNumber(IMXMLNumberNode node)
    {
        emitAttributeValue(node);
    }
    
    @Override
    public void emitString(IMXMLStringNode node)
    {
        emitAttributeValue(node);
    }
    
    @Override
    public void emitUint(IMXMLUintNode node)
    {
        emitAttributeValue(node);
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitLiteral(IMXMLLiteralNode node)
    {
        write(node.getValue().toString());
    }

    //--------------------------------------------------------------------------
    //  utils
    //--------------------------------------------------------------------------

    public void emitPropertySpecifiers(IMXMLPropertySpecifierNode[] nodes,
            boolean emitAttributes)
    {
        if (nodes != null)
        {
            for (IMXMLPropertySpecifierNode cnode : nodes)
            {
                if (!isMXMLContentNode(cnode) && emitAttributes)
                {
                    write(ASEmitterTokens.SPACE);
                    getMXMLWalker().walk(cnode);
                }
                else if (isMXMLContentNode(cnode) && !emitAttributes)
                {
                    getMXMLWalker().walk(cnode);
                }
            }
        }
    }

    protected void emitAttributeValue(IASNode node)
    {
        IMXMLLiteralNode cnode = (IMXMLLiteralNode) node.getChild(0);
        
        if (cnode.getValue() != null)
        {
            write("\"");
    
            getMXMLWalker().walk((IASNode) cnode); // Literal
    
            write("\"");
        }
    }

    protected boolean isMXMLContentNode(IMXMLNode node)
    {
        return node.getName().equals("mxmlContentFactory")
                || node.getName().equals("mxmlContent");
    }

}
