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

import java.util.List;

import org.apache.flex.compiler.codegen.as.IASEmitter;
import org.apache.flex.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.flex.compiler.codegen.mxml.flexjs.IMXMLFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.mxml.MXMLBlockWalker;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.visitor.IBlockWalker;

/**
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class MXMLFlexJSBlockWalker extends MXMLBlockWalker
{

    private IMXMLEmitter mxmlEmitter;

    public MXMLFlexJSBlockWalker(List<ICompilerProblem> errors,
            IASProject project, IMXMLEmitter mxmlEmitter, IASEmitter asEmitter,
            IBlockWalker asBlockWalker)
    {
        super(errors, project, mxmlEmitter, asEmitter, asBlockWalker);

        this.mxmlEmitter = mxmlEmitter;
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitFile(IMXMLFileNode node)
    {
        debug("visitFile()");

        walk(node.getDocumentNode());
    }

    @Override
    public void visitDocument(IMXMLDocumentNode node)
    {
        debug("visitDocument()");

        ((IMXMLFlexJSEmitter) mxmlEmitter).emitDocument(node);
    }

}
