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

package org.apache.flex.compiler.internal.codegen.mxml;

import java.util.List;

import org.apache.flex.compiler.codegen.as.IASEmitter;
import org.apache.flex.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.flex.compiler.css.ICSSDocument;
import org.apache.flex.compiler.css.ICSSRule;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.flex.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeferredInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFactoryNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLIntNode;
import org.apache.flex.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNumberNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLUintNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.visitor.IASNodeStrategy;
import org.apache.flex.compiler.visitor.IBlockWalker;
import org.apache.flex.compiler.visitor.mxml.IMXMLBlockVisitor;
import org.apache.flex.compiler.visitor.mxml.IMXMLBlockWalker;

import com.google.common.collect.ImmutableList;

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
        if (node instanceof IMXMLNode)
            mxmlStrategy.handle(node);
        else
            asStrategy.handle(node);
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

        //
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
        ICSSDocument css = node.getCSSDocument(errors);
        StringBuilder sb = new StringBuilder();
        ImmutableList<ICSSRule> rules = css.getRules();
        for (ICSSRule rule : rules)
        {
            sb.append(rule.toString());
            sb.append("\n\n");
        }
        ((FlexJSProject)project).cssDocument += sb.toString();
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
        debug("visitLiteral()");

        mxmlEmitter.emitFactory(node);
    }

    //--------------------------------------------------------------------------

    protected void debug(String message)
    {
        //System.out.println(message);
    }

}
