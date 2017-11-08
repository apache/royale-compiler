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

package org.apache.royale.compiler.internal.codegen.js.goog;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.as.TestMethodMembers;
import org.apache.royale.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.junit.Test;

/**
 * This class tests the production of 'goog'-ified JS code for Class Method
 * members.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class TestGoogMethodMembers extends TestMethodMembers
{
    @Override
    @Test
    public void testMethod()
    {
        IFunctionNode node = getMethod("function foo(){}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n}");
    }

    @Override
    @Test
    public void testMethod_withReturnType()
    {
        IFunctionNode node = getMethod("function foo():int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @return {number}\n */\nRoyaleTest_A.prototype.foo = function() {\n\tvar self = this;\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withParameterReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {*} bar\n * @return {number}\n */\nRoyaleTest_A.prototype.foo = function(bar) {\n\tvar self = this;\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string} bar\n * @return {number}\n */\nRoyaleTest_A.prototype.foo = function(bar) {\n\tvar self = this;\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withDefaultParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String = \"baz\"):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string=} bar\n * @return {number}\n */\nRoyaleTest_A.prototype.foo = function(bar) {\n\tvar self = this;\n\tbar = typeof bar !== 'undefined' ? bar : \"baz\";\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withMultipleDefaultParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String, baz:int = null):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string} bar\n * @param {number=} baz\n * @return {number}\n */\nRoyaleTest_A.prototype.foo = function(bar, baz) {\n\tvar self = this;\n\tbaz = typeof baz !== 'undefined' ? baz : null;\n\treturn -1;\n}");
    }

    @Test
    public void testMethod_withRestParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String, ...rest):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string} bar\n * @param {...} rest\n * @return {number}\n */\nRoyaleTest_A.prototype.foo = function(bar, rest) {\n\tvar self = this;\n\trest = Array.prototype.slice.call(arguments, 1);\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withNamespace()
    {
        IFunctionNode node = getMethod("public function foo(bar:String, baz:int = null):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        // we ignore the 'public' namespace completely
        assertOut("/**\n * @param {string} bar\n * @param {number=} baz\n * @return {number}\n */\nRoyaleTest_A.prototype.foo = function(bar, baz) {\n\tvar self = this;\n\tbaz = typeof baz !== 'undefined' ? baz : null;\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withNamespaceCustom()
    {
        IFunctionNode node = getMethod("mx_internal function foo(bar:String, baz:int = null):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string} bar\n * @param {number=} baz\n * @return {number}\n */\nRoyaleTest_A.prototype.foo = function(bar, baz) {\n\tvar self = this;\n\tbaz = typeof baz !== 'undefined' ? baz : null;\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withNamespaceModifiers()
    {
        IFunctionNode node = getMethod("public static function foo(bar:String, baz:int = null):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        // (erikdebruin) here we actually DO want to declare the method
        //               directly on the 'class' constructor instead of the
        //               prototype!
        assertOut("/**\n * @param {string} bar\n * @param {number=} baz\n * @return {number}\n */\nRoyaleTest_A.foo = function(bar, baz) {\n\tbaz = typeof baz !== 'undefined' ? baz : null;\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withNamespaceModifierOverride()
    {
        IFunctionNode node = getMethod("public override function foo(bar:String, baz:int = null):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string} bar\n * @param {number=} baz\n * @return {number}\n * @override\n */\nRoyaleTest_A.prototype.foo = function(bar, baz) {\n\tvar self = this;\n\tbaz = typeof baz !== 'undefined' ? baz : null;\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withNamespaceModifierOverrideBackwards()
    {
        IFunctionNode node = getMethod("override public function foo(bar:String, baz:int = null):int{return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string} bar\n * @param {number=} baz\n * @return {number}\n * @override\n */\nRoyaleTest_A.prototype.foo = function(bar, baz) {\n\tvar self = this;\n\tbaz = typeof baz !== 'undefined' ? baz : null;\n\treturn -1;\n}");
    }

    //--------------------------------------------------------------------------
    // Doc Specific Tests 
    //--------------------------------------------------------------------------

    @Test
    public void testConstructor_withThisInBody()
    {
        IClassNode node = (IClassNode) getNode("public function RoyaleTest_A() {this.foo;}", IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nRoyaleTest_A = function() {\n\tvar self = this;\n\tthis.foo;\n};");
    }

    @Test
    public void testMethod_withThisInBody()
    {
        IFunctionNode node = getMethod("function foo(){this.foo;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @this {RoyaleTest_A}\n */\nRoyaleTest_A.prototype.foo = function() {\n\tvar self = this;\n\tthis.foo;\n}");
    }

    @Test
    public void testMethod_withThisInBodyComplex()
    {
        IFunctionNode node = getMethod("function foo(){if(true){while(i){this.bar(42);}}}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @this {RoyaleTest_A}\n */\nRoyaleTest_A.prototype.foo = function() {\n\tvar self = this;\n\tif (true) "
                + "{\n\t\twhile (i) {\n\t\t\tthis.bar(42);\n\t\t}\n\t}\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }
}
