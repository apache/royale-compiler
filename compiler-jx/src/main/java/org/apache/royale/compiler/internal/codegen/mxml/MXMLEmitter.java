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

package org.apache.royale.compiler.internal.codegen.mxml;

import java.io.FilterWriter;

import org.apache.royale.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.internal.codegen.Emitter;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.*;
import org.apache.royale.compiler.visitor.IBlockWalker;
import org.apache.royale.compiler.visitor.mxml.IMXMLBlockWalker;

/**
 * The base implementation for an MXML emitter.
 * 
 * @author Erik de Bruin
 */
public class MXMLEmitter extends Emitter implements IMXMLEmitter
{

    @Override
    public String postProcess(String output)
    {
        return output;
    }

    //--------------------------------------------------------------------------
    //    walkers
    //--------------------------------------------------------------------------

    protected IMXMLBlockWalker walker;

    @Override
    public IBlockWalker getMXMLWalker()
    {
        return (IBlockWalker) walker;
    }

    @Override
    public void setMXMLWalker(IBlockWalker value)
    {
        walker = (IMXMLBlockWalker) value;
    }

    public MXMLEmitter(FilterWriter out)
    {
        super(out);
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitDeclarations(IMXMLDeclarationsNode node)
    {
        // visit tags
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            getMXMLWalker().walk(node.getChild(i));
        }    
    }
    
    @Override
    public void emitDocumentHeader(IMXMLFileNode node)
    {
        IMXMLDocumentNode dnode = node.getDocumentNode();
        
        IClassDefinition cdef = dnode
                .getClassReference((ICompilerProject) walker.getProject());

        write("<" + cdef.getBaseName());

        emitPropertySpecifiers(dnode.getPropertySpecifierNodes(), true);

        writeNewline(">", true);
    }

    @Override
    public void emitDocumentFooter(IMXMLFileNode node)
    {
        IMXMLDocumentNode dnode = node.getDocumentNode();
        
        IClassDefinition cdef = dnode
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
    public void emitEventSpecifier(IMXMLEventSpecifierNode node)
    {
    }

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
        write("<script><![CDATA[");

        int len = node.getChildCount();
        if (len > 0)
        {
            writeNewline("", true);

            for (int i = 0; i < len; i++)
            {
                getMXMLWalker().walk(node.getChild(i));

                if (i == len - 1)
                    indentPop();

                writeNewline(ASEmitterTokens.SEMICOLON);
            }
        }

        write("]]></script>");
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitObject(IMXMLObjectNode node)
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
        if (node.getChildCount()>0 && (node.getChild(0) instanceof IMXMLSingleDataBindingNode)) {
            return; //@todo more investigation needed
        }
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

    @Override
    public void emitMXMLClass(IMXMLClassNode node)
    {    	
    	write(node.getValue(getMXMLWalker().getProject()).getQualifiedName());
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitLiteral(IMXMLLiteralNode node)
    {
        write(node.getValue().toString());
    }

    //--------------------------------------------------------------------------
    //  Utils
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

    public void emitFactory(IMXMLFactoryNode node)
    {
        IASNode cnode = node.getChild(0);

        write("\"");

        if (cnode instanceof IMXMLClassNode)
        {
            write(((IMXMLClassNode)cnode).getValue(getMXMLWalker().getProject()).getQualifiedName());
        }

        write("\"");
    }

    public void emitComponent(IMXMLComponentNode node)
    {
        IASNode cnode = node.getChild(0);

        write("<fx:Component>");

        if (cnode instanceof IMXMLClassNode)
        {
            getMXMLWalker().walk((IASNode) cnode); // Literal
        }

        write("</fx:Component>");
    }

    public void emitMetadata(IMXMLMetadataNode node)
    {
        // ToDo (erikdebruin): implement metadata output
    }

    public void emitEmbed(IMXMLEmbedNode node)
    {
        // ToDo (erikdebruin): implement embed output
    }
    
    public void emitImplements(IMXMLImplementsNode node)
    {
        // ToDo (erikdebruin): implement implements output
    }
    
    public void emitVector(IMXMLVectorNode node)
    {
        // ToDo (erikdebruin): implement vector output
    }
    
    public void emitDatabinding(IMXMLDataBindingNode node)
    {
    	// ToDo (erikdebruin): implement databinding output
    }

	@Override
	public void emitRemoteObjectMethod(IMXMLRemoteObjectMethodNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void emitRemoteObject(IMXMLRemoteObjectNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void emitWebServiceMethod(IMXMLWebServiceOperationNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void emitWebService(IMXMLWebServiceNode node) {
		// TODO Auto-generated method stub
		
	}
    
}
