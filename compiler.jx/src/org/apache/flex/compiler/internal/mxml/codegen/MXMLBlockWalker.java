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

import org.apache.flex.compiler.as.codegen.IASEmitter;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.flex.compiler.visitor.IASBlockWalker;
import org.apache.flex.compiler.visitor.IASNodeStrategy;
import org.apache.flex.compiler.visitor.IMXMLBlockVisitor;
import org.apache.flex.compiler.visitor.IMXMLBlockWalker;

/**
 * @author Michael Schmalle
 */
public class MXMLBlockWalker implements IMXMLBlockVisitor, IMXMLBlockWalker
{
    private List<ICompilerProblem> errors;

    private IASBlockWalker visitor;

    private IASEmitter emitter;

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

    public MXMLBlockWalker(IASEmitter emitter, IASProject project,
            List<ICompilerProblem> errors)
    {
        this.emitter = emitter;
        this.project = project;
        this.errors = errors;

        this.visitor = emitter.getWalker();
    }

    public void visitFile(IMXMLFileNode node)
    {
        debug("visitFile()");
        IMXMLClassDefinitionNode cnode = node.getDocumentNode();
        walk(cnode);
    }

    public void visitDocument(IMXMLDocumentNode node)
    {
        debug("visitDocument()");
        // this might eventually need to check for parent being the file node
        visitMainClassDefinition(node);
    }

    public void visitMainClassDefinition(IMXMLClassDefinitionNode node)
    {
        debug("visitMainClassDefinition()");
        // we are at the root tags
        // events, properties, declarations, 

        IClassDefinition definition = node.getClassDefinition();
        IPackageDefinition definition2 = (IPackageDefinition) definition
                .getContainingScope().getDefinition();

        emitter.emitPackageHeader(definition2);
        emitter.emitPackageHeaderContents(definition2);

        // main contents of MXML are produced
        IMXMLScriptNode[] snodes = node.getScriptNodes();
        for (IMXMLScriptNode snode : snodes)
        {
            for (IASNode asnode : snode.getASNodes())
            {
                visitor.walk(asnode);
            }
        }
    }

    //--------------------------------------------------------------------------

    public void visitPropertySpecifier(IMXMLPropertySpecifierNode node)
    {
        debug("visitPropertySpecifier()");
    }

    public void visitInstance(IMXMLInstanceNode node)
    {
        debug("visitInstance()");
    }

    public void visitEventSpecifier(IMXMLEventSpecifierNode node)
    {
        debug("visitEventSpecifier()");
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    protected void debug(String message)
    {
        System.out.println(message);
    }
}
