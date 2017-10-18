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

package org.apache.royale.compiler.internal.driver.mxml;

import java.io.FilterWriter;
import java.util.List;

import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.mxml.MXMLBlockWalker;
import org.apache.royale.compiler.internal.codegen.mxml.MXMLEmitter;
import org.apache.royale.compiler.internal.driver.js.JSBackend;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.projects.ISourceFileHandler;
import org.apache.royale.compiler.internal.visitor.as.ASNodeSwitch;
import org.apache.royale.compiler.internal.visitor.mxml.MXMLNodeSwitch;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.visitor.IBlockVisitor;
import org.apache.royale.compiler.visitor.IBlockWalker;
import org.apache.royale.compiler.visitor.mxml.IMXMLBlockWalker;

/**
 * A concrete implementation of the {@link IBackend} API where the
 * {@link MXMLBlockWalker} is used to traverse the {@link IMXMLFileNode} AST.
 * 
 * @author Erik de Bruin
 */
public class MXMLBackend extends JSBackend
{

    @Override
    public ISourceFileHandler getSourceFileHandlerInstance()
    {
        return MXMLSourceFileHandler.INSTANCE;
    }

    @Override
    public IMXMLEmitter createMXMLEmitter(FilterWriter out)
    {
        return new MXMLEmitter(out);
    }

    @Override
    public IMXMLBlockWalker createMXMLWalker(RoyaleJSProject project,
            List<ICompilerProblem> errors, IMXMLEmitter mxmlEmitter,
            IASEmitter asEmitter, IBlockWalker asBlockWalker)
    {
        MXMLBlockWalker walker = new MXMLBlockWalker(errors, project,
                mxmlEmitter, asEmitter, asBlockWalker);

        ASNodeSwitch asStrategy = new ASNodeSwitch(
                (IBlockVisitor) asBlockWalker);
        walker.setASStrategy(asStrategy);

        MXMLNodeSwitch strategy = new MXMLNodeSwitch(walker);
        walker.setMXMLStrategy(strategy);

        return walker;
    }

}
