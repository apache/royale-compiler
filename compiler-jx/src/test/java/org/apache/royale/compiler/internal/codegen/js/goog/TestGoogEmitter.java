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
import org.apache.royale.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.junit.Test;

/**
 * This class tests the production of 'goog' JavaScript output.
 * <p>
 * Note; this is a complete prototype more used in figuring out where
 * abstraction and indirection is needed concerning the AS -> JS translations.
 * 
 * @author Michael Schmalle
 */
public class TestGoogEmitter extends ASTestBase
{

    @Test
    public void testSimple()
    {
        String code = "package com.example.components {"
                + "import custom.TestImplementation;"
                + "public class MyEventTarget extends TestImplementation {"
                + "public function MyEventTarget() {if (foo() != 42) { bar(); } }"
                + "private var _privateVar:String = \"do \";"
                + "public var publicProperty:Number = 100;"
                + "public function myFunction(value: String): String{"
                + "return \"Don't \" + _privateVar + value; }}}";
        IFileNode node = compileAS(code);
        asBlockWalker.visitFile(node);
        assertOut("goog.provide('com.example.components.MyEventTarget');\n\ngoog.require('custom.TestImplementation');\n\n/**\n * @constructor\n * @extends {custom.TestImplementation}\n */\ncom.example.components.MyEventTarget = function() {\n\tvar self = this;\n\tcom.example.components.MyEventTarget.base(this, 'constructor');\n\tif (foo() != 42) {\n\t\tbar();\n\t}\n};\ngoog.inherits(com.example.components.MyEventTarget, custom.TestImplementation);\n\n/**\n * @private\n * @type {string}\n */\ncom.example.components.MyEventTarget.prototype._privateVar = \"do \";\n\n/**\n * @type {number}\n */\ncom.example.components.MyEventTarget.prototype.publicProperty = 100;\n\n/**\n * @param {string} value\n * @return {string}\n */\ncom.example.components.MyEventTarget.prototype.myFunction = function(value) {\n\tvar self = this;\n\treturn \"Don't \" + self._privateVar + value;\n};");
    }

    @Test
    public void testSimpleInterface()
    {
        String code = "package com.example.components {"
                + "public interface TestInterface { } }";
        IFileNode node = compileAS(code);
        asBlockWalker.visitFile(node);
        assertOut("goog.provide('com.example.components.TestInterface');\n\n/**\n * @interface\n */\ncom.example.components.TestInterface = function() {\n};");
    }

    @Test
    public void testSimpleClass()
    {
        String code = "package com.example.components {"
                + "public class TestClass { } }";
        IFileNode node = compileAS(code);
        asBlockWalker.visitFile(node);
        assertOut("goog.provide('com.example.components.TestClass');\n\n/**\n * @constructor\n */\ncom.example.components.TestClass = function() {\n};");
    }

    @Test
    public void testSimpleMethod()
    {
        IFunctionNode node = getMethod("function method1():void{\n}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.method1 = function() {\n}");
    }

    @Test
    public void testSimpleParameterReturnType()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int):int{\n}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number} bar\n * @return {number}\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(bar) {\n}");
    }

    @Test
    public void testSimpleMultipleParameter()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int, baz:String, goo:Array):void{\n}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number} bar\n * @param {string} baz\n * @param {Array} goo\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(bar, baz, goo) {\n}");
    }

    @Test
    public void testSimpleMultipleParameter_JSDoc()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int, baz:String, goo:Array):void{\n}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number} bar\n * @param {string} baz\n * @param {Array} goo\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(bar, baz, goo) {\n}");
    }

    @Test
    public void testDefaultParameter()
    {
        IFunctionNode node = getMethodWithPackage("function method1(p1:Number, p2:Number, p3:Number = 3, p4:Number = 4):Number{return p1 + p2 + p3 + p4;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number} p1\n * @param {number} p2\n * @param {number=} p3\n * @param {number=} p4\n * @return {number}\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(p1, p2, p3, p4) {\n"
                + "\tvar self = this;\n"
                + "\tp3 = typeof p3 !== 'undefined' ? p3 : 3;\n"
                + "\tp4 = typeof p4 !== 'undefined' ? p4 : 4;\n"
                + "\treturn p1 + p2 + p3 + p4;\n}");
    }

    @Test
    public void testDefaultParameter_Body()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int = 42, bax:int = 4):void{if (a) foo();}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number=} bar\n * @param {number=} bax\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(bar, bax) {\n"
                + "\tvar self = this;\n"
                + "\tbar = typeof bar !== 'undefined' ? bar : 42;\n"
                + "\tbax = typeof bax !== 'undefined' ? bax : 4;\n"
                + "\tif (a)\n\t\tfoo();\n}");
    }

    @Test
    public void testDefaultParameter_NoBody()
    {
        IFunctionNode node = getMethodWithPackage("function method1(p1:int, p2:int, p3:int = 3, p4:int = 4):int{}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number} p1\n * @param {number} p2\n * @param {number=} p3\n * @param {number=} p4\n * @return {number}\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(p1, p2, p3, p4) {\n"
                + "\tp3 = typeof p3 !== 'undefined' ? p3 : 3;\n"
                + "\tp4 = typeof p4 !== 'undefined' ? p4 : 4;\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }
    
}
