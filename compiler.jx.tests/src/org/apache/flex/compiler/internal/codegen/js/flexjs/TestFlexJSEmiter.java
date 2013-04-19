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

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogEmiter;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSEmiter extends TestGoogEmiter
{
    @Override
    public void setUp()
    {
        project = new FlexJSProject(workspace);

        super.setUp();
    }

    @Override
    @Test
    public void testDefaultParameter_Body()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int = 42, bax:int = 4):void{if (a) foo();}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number=} bar\n * @param {number=} bax\n */\n"
                + "foo.bar.FalconTest_A.prototype.method1 = function(bar, bax) {\n"
                + "\tbar = typeof bar !== 'undefined' ? bar : 42;\n"
                + "\tbax = typeof bax !== 'undefined' ? bax : 4;\n"
                + "\tif (a)\n\t\tfoo();\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
