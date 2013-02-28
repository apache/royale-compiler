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

import java.util.List;

import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.mxml.codegen.IMXMLEmitter;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import org.apache.flex.compiler.visitor.IASNodeStrategy;
import org.apache.flex.compiler.visitor.IMXMLBlockVisitor;
import org.apache.flex.compiler.visitor.IMXMLBlockWalker;

/**
 * @author Michael Schmalle
 */
public class MXMLBlockWalker implements IMXMLBlockVisitor, IMXMLBlockWalker
{
    private List<ICompilerProblem> errors;

    private IMXMLEmitter emitter;

    //----------------------------------
    // project
    //----------------------------------

    private IASProject project;

    public IASProject getProject()
    {
        return project;
    }

    //----------------------------------
    // strategy
    //----------------------------------

    private IASNodeStrategy strategy;

    public IASNodeStrategy getStrategy()
    {
        return strategy;
    }

    public void setStrategy(IASNodeStrategy value)
    {
        strategy = value;
    }

    List<ICompilerProblem> getErrors()
    {
        return errors;
    }

    @Override
    public void walk(IASNode node)
    {
        strategy.handle(node);
    }

    public MXMLBlockWalker(List<ICompilerProblem> errors, IASProject project,
            IMXMLEmitter emitter)
    {
        this.emitter = emitter;
        this.project = project;
        this.errors = errors;
        emitter.setMXMLWalker(this);
    }

    public void visitFile(IMXMLFileNode node)
    {
        debug("visitFile()");

        walk(node.getDocumentNode());
    }

    public void visitDeclarations(IMXMLDeclarationsNode node)
    {
        debug("visitDeclarations()");

        //
    }

    public void visitDocument(IMXMLDocumentNode node)
    {
        debug("visitDocument()");

        IClassDefinition cdef = node.getClassReference((ICompilerProject) project);

        emitter.emitDocumentHeader(cdef);
        visitClassDefinition(node);
        emitter.emitDocumentFooter(cdef);
    }

    public void visitClassDefinition(IMXMLClassDefinitionNode node)
    {
        debug("visitClassDefinition()");

        emitter.emitClass(node);
    }

    //--------------------------------------------------------------------------

    public void visitPropertySpecifier(IMXMLPropertySpecifierNode node)
    {
        debug("visitPropertySpecifier()");

        emitter.emitPropertySpecifier(node);
    }

    public void visitEventSpecifier(IMXMLEventSpecifierNode node)
    {
        debug("visitEventSpecifier()");
    }

    @Override
    public void visitStyleSpecifier(IMXMLStyleSpecifierNode node)
    {
        debug("visitStyleSpecifier()");
    }

    public void visitInstance(IMXMLInstanceNode node)
    {
        debug("visitInstance()");
        
        emitter.emitInstance(node);
    }

    @Override
    public void visitScript(IMXMLScriptNode node)
    {
        debug("visitScript()");
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitLiteral(IMXMLLiteralNode node)
    {
        debug("visitLiteral()");
        
        emitter.emitLiteral(node);
    }
    
    @Override
    public void visitString(IMXMLStringNode node)
    {
        debug("visitString()");
        
        emitter.emitString(node);
    }

    //--------------------------------------------------------------------------

    protected void debug(String message)
    {
        //System.out.println(message);
    }

}
