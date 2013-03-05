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

package org.apache.flex.compiler.internal.mxml.driver.flexjs;

import java.io.FilterWriter;
import java.util.List;

import org.apache.flex.compiler.as.codegen.IASEmitter;
import org.apache.flex.compiler.common.driver.IBackend;
import org.apache.flex.compiler.internal.as.visitor.ASNodeSwitch;
import org.apache.flex.compiler.internal.mxml.codegen.MXMLBlockWalker;
import org.apache.flex.compiler.internal.mxml.codegen.flexjs.MXMLFlexJSBlockWalker;
import org.apache.flex.compiler.internal.mxml.codegen.flexjs.MXMLFlexJSEmitter;
import org.apache.flex.compiler.internal.mxml.driver.MXMLBackend;
import org.apache.flex.compiler.internal.mxml.visitor.MXMLNodeSwitch;
import org.apache.flex.compiler.mxml.codegen.IMXMLEmitter;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.visitor.IBlockVisitor;
import org.apache.flex.compiler.visitor.IBlockWalker;
import org.apache.flex.compiler.visitor.IMXMLBlockWalker;

/**
 * A concrete implementation of the {@link IBackend} API where the
 * {@link MXMLBlockWalker} is used to traverse the {@link IMXMLFileNode} AST.
 * 
 * @author Erik de Bruin
 */
public class MXMLFlexJSBackend extends MXMLBackend
{

    @Override
    public IMXMLEmitter createMXMLEmitter(FilterWriter out)
    {
        return new MXMLFlexJSEmitter(out);
    }

    @Override
    public IMXMLBlockWalker createMXMLWalker(IASProject project,
            List<ICompilerProblem> errors, IMXMLEmitter mxmlEmitter,
            IASEmitter asEmitter, IBlockWalker asBlockWalker)
    {
        MXMLBlockWalker walker = new MXMLFlexJSBlockWalker(errors, project,
                mxmlEmitter, asEmitter, asBlockWalker);

        ASNodeSwitch asStrategy = new ASNodeSwitch(
                (IBlockVisitor) asBlockWalker);
        walker.setASStrategy(asStrategy);

        MXMLNodeSwitch mxmlStrategy = new MXMLNodeSwitch(walker);
        walker.setMXMLStrategy(mxmlStrategy);

        return walker;
    }

}
