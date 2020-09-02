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

import java.util.ArrayList;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.js.goog.TestGoogPackage;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestRoyalePackage extends TestGoogPackage
{
    @Override
    public void setUp()
    {
		backend = createBackend();
		project = new RoyaleJSProject(workspace, backend);
    	JSGoogConfiguration config = new JSGoogConfiguration();
    	ArrayList<String> values = new ArrayList<String>();
    	values.add("Event");
    	values.add("Before");
    	config.setCompilerKeepAs3Metadata(null, values);
    	ArrayList<String> values2 = new ArrayList<String>();
    	values2.add("Before");
    	config.setCompilerKeepCodeWithMetadata(null, values2);
    	((RoyaleJSProject)project).config = config;
        super.setUp();
    }
    
    @Override
    @Test
    public void testPackageSimple_Class()
    {
        // does JS need a implicit constructor function? ... always?
        // All class nodes in AST get either an implicit or explicit constructor
        // this is an implicit and the way I have the before/after handler working
        // with block disallows implicit blocks from getting { }

        // (erikdebruin) the constuctor IS the class definition, in 'goog' JS,
        //               therefor we need to write out implicit constructors
        //               (if I understand the term correctly)

        IFileNode node = compileAS("package {public class A{}}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
        		" * A\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('A');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" */\n" +
        		"A = function() {\n" +
        		"};\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'A', kind: 'class' }] };\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {};\n" +
        		"};\n"+
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"A.prototype.ROYALE_COMPILE_FLAGS = 15;\n");
    }

    @Override
    @Test
    public void testPackageQualified_Class()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{}}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
        		" * foo.bar.baz.A\n" +
        		" *\n" +
        		" * @fileoverview\n" +
        		" *\n" +
        		" * @suppress {checkTypes|accessControls}\n" +
        		" */\n" +
        		"\n" +
        		"goog.provide('foo.bar.baz.A');\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * @constructor\n" +
        		" */\n" +
        		"foo.bar.baz.A = function() {\n" +
        		"};\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Metadata\n" +
        		" *\n" +
        		" * @type {Object.<string, Array.<Object>>}\n" +
        		" */\n" +
        		"foo.bar.baz.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A', kind: 'class' }] };\n" +
        		"\n" +
        		"\n" +
        		"\n" +
        		"/**\n" +
        		" * Reflection\n" +
        		" *\n" +
        		" * @return {Object.<string, Function>}\n" +
        		" */\n" +
        		"foo.bar.baz.A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		"  return {};\n" +
        		"};\n"+
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_COMPILE_FLAGS = 15;\n");
    }

    @Override
    @Test
    public void testPackageQualified_ClassBody()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{public function A(){}}}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
				" * foo.bar.baz.A\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('foo.bar.baz.A');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'A': { type: '', declaredBy: 'foo.bar.baz.A'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_COMPILE_FLAGS = 15;\n"
		
		);
    }

    @Override
    @Test
    public void testPackageQualified_ClassBodyMethodContents()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{public function A(){if (a){for (var i:Object in obj){doit();}}}}}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
				" * foo.bar.baz.A\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('foo.bar.baz.A');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A = function() {\n" +
				"  if (a) {\n" +
				"    for (var /** @type {Object} */ i in obj) {\n" +
				"      doit();\n" +
				"    }\n" +
				"  }\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'A': { type: '', declaredBy: 'foo.bar.baz.A'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_COMPILE_FLAGS = 15;\n");
    }

    @Test
    public void testPackageQualified_ClassBodyMetaData()
    {
        IFileNode node = compileAS("package foo.bar.baz {[Event(name='add', type='mx.events.FlexEvent')]\npublic class A{public function A(){}}}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
				" * foo.bar.baz.A\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('foo.bar.baz.A');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'A': { type: '', declaredBy: 'foo.bar.baz.A'}\n" +
				"      };\n" +
				"    },\n" +
				"    metadata: function () { return [ { name: 'Event', args: [ { key: 'name', value: 'add' }, { key: 'type', value: 'mx.events.FlexEvent' } ] } ]; }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_COMPILE_FLAGS = 15;\n");
    }

    @Test
    public void testPackageQualified_ExportPropertyForMetadata()
    {
        IFileNode node = compileAS("package foo.bar.baz {[Event(name='add', type='mx.events.FlexEvent')]\npublic class A{public function A(){}\n[Before]\npublic function foo() {}}}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
				" * foo.bar.baz.A\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('foo.bar.baz.A');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.foo = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'A': { type: '', declaredBy: 'foo.bar.baz.A'},\n" +
				"        'foo': { type: '', declaredBy: 'foo.bar.baz.A', metadata: function () { return [ { name: 'Before' } ]; }}\n" +
				"      };\n" +
				"    },\n" +
				"    metadata: function () { return [ { name: 'Event', args: [ { key: 'name', value: 'add' }, { key: 'type', value: 'mx.events.FlexEvent' } ] } ]; }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_COMPILE_FLAGS = 15;\n" +
				"goog.exportProperty(foo.bar.baz.A.prototype, 'foo', foo.bar.baz.A.prototype.foo);\n" );
    }

    @Test
    public void testPackageQualified_ClassAndInternalClass()
    {
        IFileNode node = compileAS("package foo.bar.baz {\n" +
        							  "public class A {\n" +
        							  "public function A(){\n" +
        							      "var internalClass:InternalClass = new InternalClass();\n" +
        							  "}}}\n" +
        							  "class InternalClass {\n" +
        							      "public function InternalClass(){}\n" +
        							  "}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
				" * foo.bar.baz.A\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('foo.bar.baz.A');\n" +
				"goog.provide('foo.bar.baz.A.InternalClass');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A = function() {\n" +
				"  var /** @type {foo.bar.baz.A.InternalClass} */ internalClass = new foo.bar.baz.A.InternalClass();\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'A': { type: '', declaredBy: 'foo.bar.baz.A'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_COMPILE_FLAGS = 15;\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'InternalClass', qName: 'foo.bar.baz.A.InternalClass', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'InternalClass': { type: '', declaredBy: 'foo.bar.baz.A.InternalClass'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_COMPILE_FLAGS = 15;\n"
		);
    }

	@Test
	public void testPackageQualified_ClassAndInternalFunction()
	{
		IFileNode node = compileAS("package foo.bar.baz {\n" +
				"public class A {\n" +
				"public function A(){\n" +
				"internalFunction();\n" +
				"}}}\n" +
				"function internalFunction(){}");
		asBlockWalker.visitFile(node);
		assertOutWithMetadata("/**\n" +
				" * foo.bar.baz.A\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('foo.bar.baz.A');\n" +
				"goog.provide('foo.bar.baz.A.internalFunction');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A = function() {\n" +
				"  foo.bar.baz.A.internalFunction();\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'A': { type: '', declaredBy: 'foo.bar.baz.A'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_COMPILE_FLAGS = 15;\n" +
				"\n" +
				"\n" +
				"\n" +
				"foo.bar.baz.A.internalFunction = function() {\n" +
				"}");
	}

	@Test
	public void testPackageQualified_ClassAndInternalVariable()
	{
		IFileNode node = compileAS("package foo.bar.baz {\n" +
				"public class A {\n" +
				"public function A(){\n" +
				"internalVar = 3;\n" +
				"}}}\n" +
				"var internalVar:Number = 2;");
		asBlockWalker.visitFile(node);
		assertOutWithMetadata("/**\n" +
				" * foo.bar.baz.A\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('foo.bar.baz.A');\n" +
				"goog.provide('foo.bar.baz.A.internalVar');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A = function() {\n" +
				"  foo.bar.baz.A.internalVar = 3;\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'A': { type: '', declaredBy: 'foo.bar.baz.A'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_COMPILE_FLAGS = 15;\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @package\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.internalVar = 2"
		);
	}

    @Test
    public void testPackageQualified_ClassAndInternalClassMethods()
    {
        IFileNode node = compileAS("package foo.bar.baz {\n" +
        							  "public class A {\n" +
        							  "public function A(){\n" +
        							      "var internalClass:InternalClass = new InternalClass();\n" +
        							      "var myString:String = InternalClass.someString;\n" +
        							      "myString = InternalClass.someStaticFunction();\n" +
        							      "myString = internalClass.someMethod();\n" +
        							  "}}}\n" +
        							  "class InternalClass {\n" +
        							      "public function InternalClass(){\n" +
        							      "}\n" +
       							          "public static var someString:String = \"foo\";\n" +
    							          "public static function someStaticFunction():String { return \"bar\";}\n" +
    							          "public function someMethod():String { return \"baz\";}\n" +
        							  "}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
				" * foo.bar.baz.A\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('foo.bar.baz.A');\n" +
				"goog.provide('foo.bar.baz.A.InternalClass');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A = function() {\n" +
				"  var /** @type {foo.bar.baz.A.InternalClass} */ internalClass = new foo.bar.baz.A.InternalClass();\n" +
				"  var /** @type {string} */ myString = foo.bar.baz.A.InternalClass.someString;\n" +
				"  myString = foo.bar.baz.A.InternalClass.someStaticFunction();\n" +
				"  myString = internalClass.someMethod();\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'A': { type: '', declaredBy: 'foo.bar.baz.A'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_COMPILE_FLAGS = 15;\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @nocollapse\n" +
				" * @type {string}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.someString = \"foo\";\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @nocollapse\n" + 
				" * @return {string}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.someStaticFunction = function() {\n" +
				"  return \"bar\";\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @return {string}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.someMethod = function() {\n" +
				"  return \"baz\";\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'InternalClass', qName: 'foo.bar.baz.A.InternalClass', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    variables: function () {\n" +
				"      return {\n" +
				"        '|someString': { type: 'String', get_set: function (/** * */ v) {return v !== undefined ? foo.bar.baz.A.InternalClass.someString = v : foo.bar.baz.A.InternalClass.someString;}}\n" +
				"      };\n" +
				"    },\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'InternalClass': { type: '', declaredBy: 'foo.bar.baz.A.InternalClass'},\n" +
				"        '|someStaticFunction': { type: 'String', declaredBy: 'foo.bar.baz.A.InternalClass'},\n" +
				"        'someMethod': { type: 'String', declaredBy: 'foo.bar.baz.A.InternalClass'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_COMPILE_FLAGS = 15;\n" +
				"/**\n" +
				" * Provide reflection support for distinguishing dynamic fields on class object (static)\n" +
				" * @const\n" +
				" * @type {Array<string>}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_INITIAL_STATICS = Object.keys(foo.bar.baz.A.InternalClass);\n"
		);
    }

    @Test
    public void testPackageQualified_ClassAndInternalGettersAndSetters()
    {
        IFileNode node = compileAS("package foo.bar.baz {\n" +
        							  "public class A {\n" +
        							  "public function A(){\n" +
        							      "var internalClass:InternalClass = new InternalClass();\n" +
        							      "myString = internalClass.someString;\n" +
        							      "internalClass.someString = myString;\n" +
        							  "}\n" +
        							  "public function get myString():String {\n" +
        							  "    return null;\n" +
        							  "}\n" +
        							  "public function set myString(value:String):void {}\n" +
        							  "}}\n" +
        							  "class InternalClass {\n" +
        							      "public function InternalClass(){\n" +
        							      "}\n" +
       							          "public function get someString():String {\n" +
       							          "    return null;\n" +
       							          "}\n" +
    							          "public function set someString(value:String):void {}\n" +
        							  "}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
				" * foo.bar.baz.A\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('foo.bar.baz.A');\n" +
				"goog.provide('foo.bar.baz.A.InternalClass');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A = function() {\n" +
				"  var /** @type {foo.bar.baz.A.InternalClass} */ internalClass = new foo.bar.baz.A.InternalClass();\n" +
				"  this.myString = internalClass.someString;\n" +
				"  internalClass.someString = this.myString;\n" +
				"};\n" +
				"\n" +
				"\n" +
				"foo.bar.baz.A.prototype.get__myString = function() {\n" +
				"  return null;\n" +
				"};\n" +
				"\n" +
				"\n" +
				"foo.bar.baz.A.prototype.set__myString = function(value) {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"Object.defineProperties(foo.bar.baz.A.prototype, /** @lends {foo.bar.baz.A.prototype} */ {\n" +
				"/**\n" +
				"  * @export\n" +
				"  * @type {string} */\n" +
				"myString: {\n" +
				"get: foo.bar.baz.A.prototype.get__myString,\n" +
				"set: foo.bar.baz.A.prototype.set__myString}}\n" +
				");\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    accessors: function () {\n" +
				"      return {\n" +
				"        'myString': { type: 'String', access: 'readwrite', declaredBy: 'foo.bar.baz.A'}\n" +
				"      };\n" +
				"    },\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'A': { type: '', declaredBy: 'foo.bar.baz.A'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_COMPILE_FLAGS = 15;\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"foo.bar.baz.A.InternalClass.prototype.get__someString = function() {\n" +
				"  return null;\n" +
				"};\n" +
				"\n" +
				"\n" +
				"foo.bar.baz.A.InternalClass.prototype.set__someString = function(value) {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"Object.defineProperties(foo.bar.baz.A.InternalClass.prototype, /** @lends {foo.bar.baz.A.InternalClass.prototype} */ {\n" +
				"/**\n" +
				"  * @export\n" +
				"  * @type {string} */\n" +
				"someString: {\n" +
				"get: foo.bar.baz.A.InternalClass.prototype.get__someString,\n" +
				"set: foo.bar.baz.A.InternalClass.prototype.set__someString}}\n" +
				");\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'InternalClass', qName: 'foo.bar.baz.A.InternalClass', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    accessors: function () {\n" +
				"      return {\n" +
				"        'someString': { type: 'String', access: 'readwrite', declaredBy: 'foo.bar.baz.A.InternalClass'}\n" +
				"      };\n" +
				"    },\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'InternalClass': { type: '', declaredBy: 'foo.bar.baz.A.InternalClass'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_COMPILE_FLAGS = 15;\n"
		);
    }

    @Test
    public void testPackageQualified_ClassAndInternalROYALE_CLASS_INFO()
    {
        IFileNode node = compileAS("package foo.bar.baz {\n" +
        							  "public class A {\n" +
        							  "public function A(){\n" +
        							      "var internalClass:ITestInterface = new InternalClass() as ITestInterface;\n" +
        							      "internalClass.test();\n" +
        							  "}\n" +
        							  "}}\n" +
        							  "interface ITestInterface {\n" +
        							  "function test():void;\n" +
        							  "}\n" +
        							  "class InternalClass implements ITestInterface {\n" +
        							      "public function InternalClass(){\n" +
        							      "}\n" +
       							          "public function test():void {}\n" +
        							  "}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
				" * foo.bar.baz.A\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('foo.bar.baz.A');\n" +
				"goog.provide('foo.bar.baz.A.ITestInterface');\n" +
				"goog.provide('foo.bar.baz.A.InternalClass');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.baz.A = function() {\n" +
				"  var /** @type {foo.bar.baz.A.ITestInterface} */ internalClass = org.apache.royale.utils.Language.as(new foo.bar.baz.A.InternalClass(), foo.bar.baz.A.ITestInterface);\n" +
				"  internalClass.test();\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'A': { type: '', declaredBy: 'foo.bar.baz.A'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.prototype.ROYALE_COMPILE_FLAGS = 15;\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @interface\n" +
				" */\n" +
				"foo.bar.baz.A.ITestInterface = function() {\n" +
				"};\n" +
				"foo.bar.baz.A.ITestInterface.prototype.test = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.ITestInterface.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'ITestInterface', qName: 'foo.bar.baz.A.ITestInterface', kind: 'interface' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.ITestInterface.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'test': { type: 'void', declaredBy: 'foo.bar.baz.A.ITestInterface'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.ITestInterface.prototype.ROYALE_COMPILE_FLAGS = 15;\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" * @implements {foo.bar.baz.A.ITestInterface}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.test = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'InternalClass', qName: 'foo.bar.baz.A.InternalClass', kind: 'class' }], interfaces: [foo.bar.baz.A.ITestInterface] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {\n" +
				"    methods: function () {\n" +
				"      return {\n" +
				"        'InternalClass': { type: '', declaredBy: 'foo.bar.baz.A.InternalClass'},\n" +
				"        'test': { type: 'void', declaredBy: 'foo.bar.baz.A.InternalClass'}\n" +
				"      };\n" +
				"    }\n" +
				"  };\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.baz.A.InternalClass.prototype.ROYALE_COMPILE_FLAGS = 15;\n"
		);
    }

	@Test
	public void testPackageQualified_ClassAndInternalStaticConst()
	{
		IFileNode node = compileAS("package foo.bar {\n" +
				//adding an unneeded import node here exposed a failing test at one point:
				"import foo.bar.*\n" +
				"public function A():Number {\n" +
				"    return Internal.x;\n" +
				"}}\n" +
				"internal class Internal {" +
				"public static const x:Number = 3;}");
		
		asBlockWalker.visitFile(node);
		assertOutWithMetadata("/**\n" +
				" * foo.bar.A\n" +
				" *\n" +
				" * @fileoverview\n" +
				" *\n" +
				" * @suppress {checkTypes|accessControls}\n" +
				" */\n" +
				"\n" +
				"goog.provide('foo.bar.A');\n" +
				"goog.provide('foo.bar.A.Internal');\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @return {number}\n" +
				" */\n" +
				"foo.bar.A = function() {\n" +
				"  return foo.bar.A.Internal.x;\n" +
				"}\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @constructor\n" +
				" */\n" +
				"foo.bar.A.Internal = function() {\n" +
				"};\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * @nocollapse\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.A.Internal.x = 3;\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Metadata\n" +
				" *\n" +
				" * @type {Object.<string, Array.<Object>>}\n" +
				" */\n" +
				"foo.bar.A.Internal.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'Internal', qName: 'foo.bar.A.Internal', kind: 'class' }] };\n" +
				"\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Reflection\n" +
				" *\n" +
				" * @return {Object.<string, Function>}\n" +
				" */\n" +
				"foo.bar.A.Internal.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
				"  return {};\n" +
				"};\n" +
				"/**\n" +
				" * @const\n" +
				" * @type {number}\n" +
				" */\n" +
				"foo.bar.A.Internal.prototype.ROYALE_COMPILE_FLAGS = 15;\n" +
				"/**\n" +
				" * Provide reflection support for distinguishing dynamic fields on class object (static)\n" +
				" * @const\n" +
				" * @type {Array<string>}\n" +
				" */\n" +
				"foo.bar.A.Internal.prototype.ROYALE_INITIAL_STATICS = Object.keys(foo.bar.A.Internal);\n"
		);
	}
	
	@Test
	public void testPackageSimple_Function()
	{
		IFileNode node = compileAS("package {public function A(){}}");
		asBlockWalker.visitFile(node);
		assertOutWithMetadata("/**\n * A\n *\n * @fileoverview\n *\n * @suppress {checkTypes|accessControls}\n */\n\ngoog.provide('A');\n\n\n\n/**\n */\nA = function() {\n}");
	}

	@Test
	public void testPackageQualified_Function()
	{
		IFileNode node = compileAS("package foo.bar.baz {public function A(){}}");
		asBlockWalker.visitFile(node);
		assertOutWithMetadata("/**\n * foo.bar.baz.A\n *\n * @fileoverview\n *\n * @suppress {checkTypes|accessControls}\n */\n\ngoog.provide('foo.bar.baz.A');\n\n\n\n/**\n */\nfoo.bar.baz.A = function() {\n}");
	}

	@Test
	public void testPackageSimple_Variable()
	{
		IFileNode node = compileAS("package {public var A:String = \"Hello\";}");
		asBlockWalker.visitFile(node);
		assertOutWithMetadata("/**\n * A\n *\n * @fileoverview\n *\n * @suppress {checkTypes|accessControls}\n */\n\ngoog.provide('A');\n\n\n\n/**\n * @type {string}\n */\nA = \"Hello\"");
	}

	@Test
	public void testPackageQualified_Variable()
	{
		IFileNode node = compileAS("package foo.bar.baz {public var A:String = \"Hello\";}");
		asBlockWalker.visitFile(node);
		assertOutWithMetadata("/**\n * foo.bar.baz.A\n *\n * @fileoverview\n *\n * @suppress {checkTypes|accessControls}\n */\n\ngoog.provide('foo.bar.baz.A');\n\n\n\n/**\n * @type {string}\n */\nfoo.bar.baz.A = \"Hello\"");
	}
    
    @Override
    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }

}
