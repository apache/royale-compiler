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
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogMethodMembers;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSMethodMembers extends TestGoogMethodMembers
{

    //--------------------------------------------------------------------------
    // Doc Specific Tests 
    //--------------------------------------------------------------------------

    @Override
    @Test
    public void testConstructor_withThisInBody()
    {
        IFunctionNode node = getMethod("public function A(){this.foo;};");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n\tvar self = this;\n\tfoo;\n}");
    }

    @Override
    @Test
    public void testMethod_withThisInBody()
    {
        IFunctionNode node = getMethod("function foo(){this.foo;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @this {A}\n */\nA.prototype.foo = function() {\n\tvar self = this;\n\tfoo;\n}");
    }

    @Override
    @Test
    public void testMethod_withThisInBodyComplex()
    {
        IFunctionNode node = getMethod("function foo(){if(true){while(i){this.bar(42);}}}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @this {A}\n */\nA.prototype.foo = function() {\n\tvar self = this;\n\tif (true) "
                + "{\n\t\twhile (i) {\n\t\t\tbar(42);\n\t\t}\n\t}\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }
    
}
