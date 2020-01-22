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

import java.util.List;

import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem2;
import org.apache.royale.compiler.projects.IASProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassNode;
import org.apache.royale.compiler.tree.mxml.IMXMLComponentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDeferredInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEmbedNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFactoryNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLImplementsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLIntNode;
import org.apache.royale.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.royale.compiler.tree.mxml.IMXMLMetadataNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNumberNode;
import org.apache.royale.compiler.tree.mxml.IMXMLObjectNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLRemoteObjectMethodNode;
import org.apache.royale.compiler.tree.mxml.IMXMLRemoteObjectNode;
import org.apache.royale.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStringNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStyleNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLUintNode;
import org.apache.royale.compiler.tree.mxml.IMXMLVectorNode;
import org.apache.royale.compiler.tree.mxml.IMXMLWebServiceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLWebServiceOperationNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.visitor.IASNodeStrategy;
import org.apache.royale.compiler.visitor.IBlockWalker;
import org.apache.royale.compiler.visitor.mxml.IMXMLBlockVisitor;
import org.apache.royale.compiler.visitor.mxml.IMXMLBlockWalker;

/**
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class MXMLBlockWalker implements IMXMLBlockVisitor, IMXMLBlockWalker
{

    //----------------------------------
    // emitter
    //----------------------------------

    private IASEmitter asEmitter;

    @Override
    public IASEmitter getASEmitter()
    {
        return asEmitter;
    }

    private IMXMLEmitter mxmlEmitter;

    @Override
    public IMXMLEmitter getMXMLEmitter()
    {
        return mxmlEmitter;
    }

    //----------------------------------
    // errors
    //----------------------------------

    protected List<ICompilerProblem> errors;

    List<ICompilerProblem> getErrors()
    {
        return errors;
    }

    //----------------------------------
    // project
    //----------------------------------

    protected IASProject project;

    public IASProject getProject()
    {
        return project;
    }

    //----------------------------------
    // strategy
    //----------------------------------

    private IASNodeStrategy mxmlStrategy;

    public IASNodeStrategy getMXMLStrategy()
    {
        return mxmlStrategy;
    }

    public void setMXMLStrategy(IASNodeStrategy value)
    {
        mxmlStrategy = value;
    }

    private IASNodeStrategy asStrategy;

    public IASNodeStrategy getASStrategy()
    {
        return asStrategy;
    }

    public void setASStrategy(IASNodeStrategy value)
    {
        asStrategy = value;
    }

    //----------------------------------
    // walk
    //----------------------------------

    @Override
    public void walk(IASNode node)
    {
    	try {
	        if (node instanceof IMXMLNode)
	            mxmlStrategy.handle(node);
	        else
	            asStrategy.handle(node);
    	}
    	catch (Exception e)
    	{
    		String sp = String.format("%s line %d column %d", node.getSourcePath(), node.getLine() + 1, node.getColumn());
    		if (node.getSourcePath() == null)
    		{
    			IASNode parent = node.getParent();
    			sp = String.format("%s line %d column %d", parent.getSourcePath(), parent.getLine() + 1, parent.getColumn());
    		}
    		InternalCompilerProblem2 problem = new InternalCompilerProblem2(sp, e, "ASBlockWalker");
    		errors.add(problem);
    	}
    }

    @Override
    public void visitCompilationUnit(ICompilationUnit unit)
    {
        debug("visitMXMLCompilationUnit()");
        IFileNode node = null;
        try
        {
            node = (IFileNode) unit.getSyntaxTreeRequest().get().getAST();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }

        walk(node);
    }

    public MXMLBlockWalker(List<ICompilerProblem> errors, IASProject project,
            IMXMLEmitter mxmlEmitter, IASEmitter asEmitter,
            IBlockWalker asBlockWalker)
    {
        this.asEmitter = asEmitter;
        this.mxmlEmitter = mxmlEmitter;
        this.project = project;
        this.errors = errors;

        asEmitter.setWalker(asBlockWalker);

        mxmlEmitter.setMXMLWalker((IBlockWalker) this);
    }

    @Override
    public void visitFile(IMXMLFileNode node)
    {
        debug("visitFile()");

        walk(node.getDocumentNode());
    }

    @Override
    public void visitDeclarations(IMXMLDeclarationsNode node)
    {
        debug("visitDeclarations()");

        mxmlEmitter.emitDeclarations(node);
    }

    @Override
    public void visitDocument(IMXMLDocumentNode node)
    {
        debug("visitDocument()");

        IMXMLFileNode fnode = (IMXMLFileNode) node.getParent();
        
        mxmlEmitter.emitDocumentHeader(fnode);
        visitClassDefinition(node);
        mxmlEmitter.emitDocumentFooter(fnode);
    }

    @Override
    public void visitClassDefinition(IMXMLClassDefinitionNode node)
    {
        debug("visitClassDefinition()");

        mxmlEmitter.emitClass(node);
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitDeferredInstance(IMXMLDeferredInstanceNode node)
    {
        debug("visitdeferredInstance()");

        walk(node.getChild(0));
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitEventSpecifier(IMXMLEventSpecifierNode node)
    {
        debug("visitEventSpecifier()");

        mxmlEmitter.emitEventSpecifier(node);
    }

    @Override
    public void visitInstance(IMXMLInstanceNode node)
    {
        debug("visitInstance()");

        mxmlEmitter.emitInstance(node);
    }

    @Override
    public void visitPropertySpecifier(IMXMLPropertySpecifierNode node)
    {
        debug("visitPropertySpecifier()");

        mxmlEmitter.emitPropertySpecifier(node);
    }

    @Override
    public void visitScript(IMXMLScriptNode node)
    {
        debug("visitScript()");

        mxmlEmitter.emitScript(node);
    }

    @Override
    public void visitStyleBlock(IMXMLStyleNode node)
    {
    	// don't do anything.  subclasses should.
    }
    
    @Override
    public void visitStyleSpecifier(IMXMLStyleSpecifierNode node)
    {
        debug("visitStyleSpecifier()");

        mxmlEmitter.emitStyleSpecifier(node);
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitArray(IMXMLArrayNode node)
    {
        debug("visitArray()");

        mxmlEmitter.emitArray(node);
    }

    @Override
    public void visitBoolean(IMXMLBooleanNode node)
    {
        debug("visitBoolean()");

        mxmlEmitter.emitBoolean(node);
    }

    @Override
    public void visitInt(IMXMLIntNode node)
    {
        debug("visitInt()");

        mxmlEmitter.emitInt(node);
    }

    @Override
    public void visitNumber(IMXMLNumberNode node)
    {
        debug("visitNumber()");

        mxmlEmitter.emitNumber(node);
    }

    @Override
    public void visitString(IMXMLStringNode node)
    {
        debug("visitString()");

        mxmlEmitter.emitString(node);
    }

    @Override
    public void visitUint(IMXMLUintNode node)
    {
        debug("visitUint()");

        mxmlEmitter.emitUint(node);
    }

    @Override
    public void visitMXMLClass(IMXMLClassNode node)
    {
        debug("visitMXMLClass()");

        mxmlEmitter.emitMXMLClass(node);
    }
    
    //--------------------------------------------------------------------------

    @Override
    public void visitLiteral(IMXMLLiteralNode node)
    {
        debug("visitLiteral()");

        mxmlEmitter.emitLiteral(node);
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitFactory(IMXMLFactoryNode node)
    {
        debug("visitFactory()");

        mxmlEmitter.emitFactory(node);
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitComponent(IMXMLComponentNode node)
    {
        debug("visitComponent()");

        mxmlEmitter.emitComponent(node);
    }

    //--------------------------------------------------------------------------
    
    @Override
    public void visitMetadata(IMXMLMetadataNode node)
    {
        debug("visitMetadata()");
        
        mxmlEmitter.emitMetadata(node);
    }
    
    //--------------------------------------------------------------------------
    
    @Override
    public void visitEmbed(IMXMLEmbedNode node)
    {
        debug("visitEmbed()");
        
        mxmlEmitter.emitEmbed(node);
    }
    
    //--------------------------------------------------------------------------
    
    @Override
    public void visitImplements(IMXMLImplementsNode node)
    {
        debug("visitImplements()");
        
        mxmlEmitter.emitImplements(node);
    }
    
    //--------------------------------------------------------------------------
    
    @Override
    public void visitVector(IMXMLVectorNode node)
    {
        debug("visitVector()");
        
        mxmlEmitter.emitVector(node);
    }
    
    //--------------------------------------------------------------------------
    
    @Override
    public void visitDatabinding(IMXMLDataBindingNode node)
    {
        debug("visitDatabinding()");
        
        mxmlEmitter.emitDatabinding(node);
    }
    
    //--------------------------------------------------------------------------
    
    @Override
    public void visitBinding(IMXMLBindingNode node)
    {
        debug("visitBinding()");
        
        //System.out.println("skipping fx:Binding in " + node.getSourcePath() + ". This node should be encoded in the binding data.");
    }
    
    //--------------------------------------------------------------------------
    
    @Override
    public void visitObject(IMXMLObjectNode node)
    {
        debug("visitObject()");
        
        mxmlEmitter.emitObject(node);
    }
    
    //--------------------------------------------------------------------------
    
    @Override
    public void visitRemoteObjectMethod(IMXMLRemoteObjectMethodNode node)
    {
        debug("visitRemoteObjectMethod()");
        
        mxmlEmitter.emitRemoteObjectMethod(node);
    }
    
	@Override
	public void visitRemoteObject(IMXMLRemoteObjectNode node) {
        debug("visitRemoteObjectMethod()");
        
        mxmlEmitter.emitRemoteObject(node);		
	}
    
    //--------------------------------------------------------------------------
    
    @Override
    public void visitWebServiceMethod(IMXMLWebServiceOperationNode node)
    {
        debug("visitWebServiceMethod()");
        
        mxmlEmitter.emitWebServiceMethod(node);
    }
    
	@Override
	public void visitWebService(IMXMLWebServiceNode node) {
        debug("visitWebService()");
        
        mxmlEmitter.emitWebService(node);		
	}
    
    //--------------------------------------------------------------------------

    protected void debug(String message)
    {
        //System.out.println(message);
    }


}
