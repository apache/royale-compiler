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

package org.apache.flex.compiler.internal.js.codegen.goog;

import org.apache.flex.compiler.clients.IBackend;
import org.apache.flex.compiler.internal.as.codegen.TestWalkerBase;
import org.apache.flex.compiler.internal.js.driver.goog.GoogBackend;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.junit.Test;

/**
 * This class tests the production of 'goog' JavaScript output.
 * <p>
 * Note; this is a complete prototype more used in figuring out where
 * abstraction and indirection is needed concerning the AS -> JS translations.
 * 
 * @author Michael Schmalle
 */
public class TestGoogEmiter extends TestWalkerBase
{
    @Test
    public void testSimple()
    {
        String code = "package com.example.components {"
                + "import spark.components.Button;"
                + "public class MyTextButton extends Button {"
                + "public function MyTextButton() {if (foo() != 42) { bar(); } }"
                + "private var _privateVar:String = \"do \";"
                + "public var publicProperty:Number = 100;"
                + "public function myFunction(value: String): String{"
                + "return \"Don't \" + _privateVar + value; }";
        IFileNode node = getFileNode(code);
        visitor.visitFile(node);
        assertOut("goog.provide('com.example.components.MyTextButton');\n\ngoog.require('spark.components.Button');\n\n/**\n * @constructor\n * @extends {spark.components.Button}\n */\ncom.example.components.MyTextButton = function() {\n\tvar self = this;\n\tgoog.base(this);\n\tif (foo() != 42) {\n\t\tbar();\n\t}\n}\ngoog.inherits(com.example.components.MyTextButton, spark.components.Button);\n\n/**\n * @private\n * @type {string}\n */\ncom.example.components.MyTextButton.prototype._privateVar = \"do \";\n\n/**\n * @type {number}\n */\ncom.example.components.MyTextButton.prototype.publicProperty = 100;\n\n/**\n * @param {string} value\n * @return {string}\n */\ncom.example.components.MyTextButton.prototype.myFunction = function(value) {\n\tvar self = this;\n\treturn \"Don't \" + self._privateVar + value;\n};");
    }

    @Test
    public void testSimpleMethod()
    {
        IFunctionNode node = getMethod("function method1():void{\n}");
        visitor.visitFunction(node);
        assertOut("A.prototype.method1 = function() {\n}");
    }

    @Test
    public void testSimpleParameterReturnType()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int):int{\n}");
        visitor.visitFunction(node);
        assertOut("/**\n * @param {number} bar\n * @return {number}\n */\n"
                + "foo.bar.A.prototype.method1 = function(bar) {\n}");
    }

    @Test
    public void testSimpleMultipleParameter()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int, baz:String, goo:A):void{\n}");
        visitor.visitFunction(node);
        assertOut("/**\n * @param {number} bar\n * @param {string} baz\n * @param {foo.bar.A} goo\n */\n"
                + "foo.bar.A.prototype.method1 = function(bar, baz, goo) {\n}");
    }

    @Test
    public void testSimpleMultipleParameter_JSDoc()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int, baz:String, goo:A):void{\n}");
        visitor.visitFunction(node);
        assertOut("/**\n * @param {number} bar\n * @param {string} baz\n * @param {foo.bar.A} goo\n */\n"
                + "foo.bar.A.prototype.method1 = function(bar, baz, goo) {\n}");
    }

    @Test
    public void testDefaultParameter()
    {
        IFunctionNode node = getMethodWithPackage("function method1(p1:int, p2:int, p3:int = 3, p4:int = 4):int{return p1 + p2 + p3 + p4;}");
        visitor.visitFunction(node);
        assertOut("/**\n * @param {number} p1\n * @param {number} p2\n * @param {number=} p3\n * @param {number=} p4\n * @return {number}\n */\n"
                + "foo.bar.A.prototype.method1 = function(p1, p2, p3, p4) {\n"
                + "\tvar self = this;\n"
                + "\tp3 = typeof p3 !== 'undefined' ? p3 : 3;\n"
                + "\tp4 = typeof p4 !== 'undefined' ? p4 : 4;\n"
                + "\treturn p1 + p2 + p3 + p4;\n}");
    }

    @Test
    public void testDefaultParameter_Body()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int = 42, bax:int = 4):void{if (a) foo();}");
        visitor.visitFunction(node);
        assertOut("/**\n * @param {number=} bar\n * @param {number=} bax\n */\n"
                + "foo.bar.A.prototype.method1 = function(bar, bax) {\n"
                + "\tvar self = this;\n"
                + "\tbar = typeof bar !== 'undefined' ? bar : 42;\n"
                + "\tbax = typeof bax !== 'undefined' ? bax : 4;\n"
                + "\tif (a)\n\t\tfoo();\n}");
    }

    @Test
    public void testDefaultParameter_NoBody()
    {
        IFunctionNode node = getMethodWithPackage("function method1(p1:int, p2:int, p3:int = 3, p4:int = 4):int{}");
        visitor.visitFunction(node);
        assertOut("/**\n * @param {number} p1\n * @param {number} p2\n * @param {number=} p3\n * @param {number=} p4\n * @return {number}\n */\n"
                + "foo.bar.A.prototype.method1 = function(p1, p2, p3, p4) {\n"
                + "\tp3 = typeof p3 !== 'undefined' ? p3 : 3;\n"
                + "\tp4 = typeof p4 !== 'undefined' ? p4 : 4;\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }
}
