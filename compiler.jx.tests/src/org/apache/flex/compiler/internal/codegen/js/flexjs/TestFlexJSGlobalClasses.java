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
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogGlobalClasses;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSGlobalClasses extends TestGoogGlobalClasses
{

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }
    
    @Override
    @Test
    public void testArguments()
    {
        IFunctionNode node = getMethod("function a():void {\ttrace(arguments);}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.a = function() {\n\ttrace(arguments);\n}");
    }

    @Override
    @Test
    public void testVector()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Vector.<string>} */ a = new Array(['Hello', 'World'])");
    }

}
