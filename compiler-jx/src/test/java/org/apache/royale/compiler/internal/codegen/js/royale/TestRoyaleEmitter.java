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

package org.apache.royale.compiler.internal.codegen.js.royale;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.internal.codegen.js.goog.TestGoogEmitter;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.parsing.as.RoyaleASDocDelegate;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestRoyaleEmitter extends TestGoogEmitter
{
    @Override
    public void setUp()
    {
		backend = createBackend();
		project = new RoyaleJSProject(workspace, backend);

        super.setUp();
    }

    @Override
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
	        assertOutWithMetadata("/**\n" +
				" * com.example.components.MyEventTarget\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('com.example.components.MyEventTarget');\n" +
				"\n" +
				"goog.require('custom.TestImplementation');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" * @extends {custom.TestImplementation}\n" +
				" */\n" +
				"com.example.components.MyEventTarget = function() {\n" +
				"  com.example.components.MyEventTarget.base(this, 'constructor');\n" +
				"  if (foo() != 42) {\n" +
				"    bar();\n" +
				"  }\n" +
				"};\n" +
				"goog.inherits(com.example.components.MyEventTarget, custom.TestImplementation);\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @private\n" +
				" * @type {string}\n" +
				" */\n" +
				"com.example.components.MyEventTarget.prototype._privateVar = \"do \";\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @type {number}\n" +
				" */\n" +
				"com.example.components.MyEventTarget.prototype.publicProperty = 100;\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @param {string} value\n" +
				" * @return {string}\n" +
				" */\n" +
				"com.example.components.MyEventTarget.prototype.myFunction = function(value) {\n" +
				"  return \"Don't \" + this._privateVar + value;\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"com.example.components.MyEventTarget.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'MyEventTarget', qName: 'com.example.components.MyEventTarget', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"com.example.components.MyEventTarget.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    variables: function () {\n" +
				"      return {\n" +
				"        'publicProperty': { type: 'Number', get_set: function (/** com.example.components.MyEventTarget */ inst, /** * */ v) {return v !== undefined ? inst.publicProperty = v : inst.publicProperty;}}\n" +
				"      };\n" +
				"    },\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'MyEventTarget': { type: '', declaredBy: 'com.example.components.MyEventTarget'},\n" +
				"        'myFunction': { type: 'String', declaredBy: 'com.example.components.MyEventTarget', parameters: function () { return [ 'String', false ]; }}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"com.example.components.MyEventTarget.prototype.ROYALE_COMPILE_FLAGS = 9;\n");
    }

    @Override
    @Test
    public void testSimpleInterface()
    {
        String code = "package com.example.components {"
                + "public interface TestInterface { } }";
        IFileNode node = compileAS(code);
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
				" * com.example.components.TestInterface\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('com.example.components.TestInterface');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @interface\n" +
				" */\ncom.example.components.TestInterface = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"com.example.components.TestInterface.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'TestInterface', qName: 'com.example.components.TestInterface', kind: 'interface' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"com.example.components.TestInterface.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {};\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"com.example.components.TestInterface.prototype.ROYALE_COMPILE_FLAGS = 9;\n");
    }

    @Override
    @Test
    public void testSimpleClass()
    {
        String code = "package com.example.components {"
                + "public class TestClass { } }";
        IFileNode node = compileAS(code);
        asBlockWalker.visitFile(node);
		assertOutWithMetadata("/**\n" +
				" * com.example.components.TestClass\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('com.example.components.TestClass');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"com.example.components.TestClass = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"com.example.components.TestClass.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'TestClass', qName: 'com.example.components.TestClass', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"com.example.components.TestClass.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {};\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"com.example.components.TestClass.prototype.ROYALE_COMPILE_FLAGS = 9;\n");
    }
    
    @Test
    public void testInjectScript()
    {
        String code = "package com.example.components {\n"
        		+ "public class TestClass {"
        	    + "/**\n"
        	    + " * <inject_script>\n"
        		+ " * This will be injected.\n"
        		+ " * </inject_script>\n"
        		+ " */\n"
                + "public function TestClass() { } } }";
        IFileNode node = compileAS(code);
        asBlockWalker.visitFile(node);
		assertOutWithMetadata("/**\n" +
				" * com.example.components.TestClass\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('com.example.components.TestClass');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * <inject_script>\n" +
				" * This will be injected.\n" +
				" * </inject_script>\n" +
				" * @constructor\n" +
				" */\n" +
				"com.example.components.TestClass = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"com.example.components.TestClass.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'TestClass', qName: 'com.example.components.TestClass', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"com.example.components.TestClass.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'TestClass': { type: '', declaredBy: 'com.example.components.TestClass'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"com.example.components.TestClass.prototype.ROYALE_COMPILE_FLAGS = 9;\n");
    }
    

    @Override
    @Test
    public void testDefaultParameter()
    {
        IFunctionNode node = getMethodWithPackage("function method1(p1:Number, p2:Number, p3:Number = 3, p4:Number = 4):Number{return p1 + p2 + p3 + p4;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number} p1\n * @param {number} p2\n * @param {number=} p3\n * @param {number=} p4\n * @return {number}\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(p1, p2, p3, p4) {\n"
                + "  p3 = typeof p3 !== 'undefined' ? p3 : 3;\n"
                + "  p4 = typeof p4 !== 'undefined' ? p4 : 4;\n"
                + "  return p1 + p2 + p3 + p4;\n}");
    }

    @Override
    @Test
    public void testDefaultParameter_Body()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int = 42, bax:int = 4):void{if (a) foo();}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number=} bar\n * @param {number=} bax\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(bar, bax) {\n"
                + "  bar = typeof bar !== 'undefined' ? bar : 42;\n"
                + "  bax = typeof bax !== 'undefined' ? bax : 4;\n"
                + "  if (a)\n    foo();\n}");
    }

    @Override
    @Test
    public void testDefaultParameter_NoBody()
    {
        IFunctionNode node = getMethodWithPackage("function method1(p1:int, p2:int, p3:int = 3, p4:int = 4):int{}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number} p1\n * @param {number} p2\n * @param {number=} p3\n * @param {number=} p4\n * @return {number}\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(p1, p2, p3, p4) {\n"
                + "  p3 = typeof p3 !== 'undefined' ? p3 : 3;\n"
                + "  p4 = typeof p4 !== 'undefined' ? p4 : 4;\n}");
    }

    @Override
    @Test
    public void testSimpleParameterReturnType()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int):int{\n}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number} bar\n * @return {number}\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(bar) {\n}");
    }

    @Override
    @Test
    public void testSimpleMultipleParameter()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int, baz:String, goo:Array):void{\n}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number} bar\n * @param {string} baz\n * @param {Array} goo\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(bar, baz, goo) {\n}");
    }

    @Override
    @Test
    public void testSimpleMultipleParameter_JSDoc()
    {
        IFunctionNode node = getMethodWithPackage("/**\n * This is copied from ASDoc.\n */\nfunction method1(bar:int, baz:String, goo:Array):void{\n}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * This is copied from ASDoc.\n * @param {number} bar\n * @param {string} baz\n * @param {Array} goo\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(bar, baz, goo) {\n}");
    }

    @Test
    public void testSimpleMultipleParameter_JSDocSingleLine()
    {
        IFunctionNode node = getMethodWithPackage("/** This is copied from ASDoc. */\nfunction method1(bar:int, baz:String, goo:Array):void{\n}");
        asBlockWalker.visitFunction(node);
        assertOut("/** This is copied from ASDoc. \n * @param {number} bar\n * @param {string} baz\n * @param {Array} goo\n */\n"
                + "foo.bar.RoyaleTest_A.prototype.method1 = function(bar, baz, goo) {\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }
    
    @Override
    protected void addDependencies()
    {
        super.addDependencies();
        workspace.setASDocDelegate(new RoyaleASDocDelegate());
        if (project.config == null)
        	project.config = new JSGoogConfiguration();
        try {
			project.config.setKeepASDoc(null, true);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
