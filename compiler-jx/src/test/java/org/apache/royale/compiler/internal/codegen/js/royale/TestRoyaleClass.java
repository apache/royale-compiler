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
import org.apache.royale.compiler.internal.codegen.js.goog.TestGoogClass;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestRoyaleClass extends TestGoogClass
{
    @Override
    public void setUp()
    {
        backend = createBackend();
        project = new RoyaleJSProject(workspace, backend);
    	((RoyaleJSProject)project).config = new JSGoogConfiguration();
        super.setUp();
    }
    
    @Override
    @Test
    public void testConstructor_super()
    {
        IClassNode node = getClassNode("public class A {public function A() { super(); }}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.A = function() {\n  ;\n};");
    }

    @Override
    @Test
    public void testSimpleExtends()
    {
        IClassNode node = getClassNode("public class A extends TestImplementation {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {custom.TestImplementation}\n */\norg.apache.royale.A = function() {\n  org.apache.royale.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.royale.A, custom.TestImplementation);");
    }

    @Test
    public void testSimpleExtendsWithArgs()
    {
        IClassNode node = getClassNode("public class A extends TestImplementation {public function A(arg:String) { super(arg);}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {custom.TestImplementation}\n * @param {string} arg\n */\norg.apache.royale.A = function(arg) {\n  org.apache.royale.A.base(this, 'constructor', arg);\n};\ngoog.inherits(org.apache.royale.A, custom.TestImplementation);");
    }

    @Test
    public void testSimpleExtendsWithArgsImplicitSuper()
    {
        IClassNode node = getClassNode("public class A extends TestImplementation {public function A(arg:String) {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {custom.TestImplementation}\n * @param {string} arg\n */\norg.apache.royale.A = function(arg) {\n  org.apache.royale.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.royale.A, custom.TestImplementation);");
    }

    @Override
    @Test
    public void testSimpleExtendsImplements()
    {
        IClassNode node = getClassNode("public class A extends TestImplementation implements TestInterface {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {custom.TestImplementation}\n * @implements {custom.TestInterface}\n */\norg.apache.royale.A = function() {\n  org.apache.royale.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.royale.A, custom.TestImplementation);");
    }

    @Override
    @Test
    public void testSimpleExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A extends TestImplementation implements TestInterface, TestOtherInterface {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {custom.TestImplementation}\n * @implements {custom.TestInterface}\n * @implements {custom.TestOtherInterface}\n */\norg.apache.royale.A = function() {\n  org.apache.royale.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.royale.A, custom.TestImplementation);");
    }

    @Override
    @Test
    public void testSimpleFinalExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public final class A extends TestImplementation implements TestInterface, TestOtherInterface {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {custom.TestImplementation}\n * @implements {custom.TestInterface}\n * @implements {custom.TestOtherInterface}\n */\norg.apache.royale.A = function() {\n  org.apache.royale.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.royale.A, custom.TestImplementation);");
    }

    @Override
    @Test
    public void testQualifiedExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A extends custom.TestImplementation implements custom.TestInterface, custom.TestOtherInterface {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {custom.TestImplementation}\n * @implements {custom.TestInterface}\n * @implements {custom.TestOtherInterface}\n */\norg.apache.royale.A = function() {\n  org.apache.royale.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.royale.A, custom.TestImplementation);");
    }

    @Override
    @Test
    public void testExtendsConstructor_super()
    {
        IClassNode node = getClassNode("public class A extends custom.TestImplementation { public function A() { super('foo', 42);}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {custom.TestImplementation}\n */\norg.apache.royale.A = function() {\n  org.apache.royale.A.base(this, 'constructor', 'foo', 42);\n};\ngoog.inherits(org.apache.royale.A, custom.TestImplementation);");
    }

    @Test
    public void testConstructor_withArgumentNameMatchingMemberName()
    {
        IClassNode node = getClassNode("public class B {public function B(arg1:String) {this.arg1 = arg1}; public var arg1:String;}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n * @param {string} arg1\n */\norg.apache.royale.B = function(arg1) {\n  this.arg1 = arg1;\n};\n\n\n/**\n * @type {string}\n */\norg.apache.royale.B.prototype.arg1 = null;";
        assertOut(expected);
    }

    @Test
    public void testMethod_withImplicitSelfInReturnValue()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public var event:Event = new Event(); public function foo():String {return event.type;};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n\nthis.event = new Event();\n};\n\n\n/**\n * @type {Event}\n */\norg.apache.royale.B.prototype.event = null;\n\n\n/**\n * @return {string}\n */\norg.apache.royale.B.prototype.foo = function() {\n  return this.event.type;\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_noArgsNoReturn()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function foo():void {};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n */\norg.apache.royale.B.prototype.foo = function() {\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_returnIntWithVariableNoCoercion()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function foo():int { var a:int = 123; return a; };}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @return {number}\n */\norg.apache.royale.B.prototype.foo = function() {\n  var /** @type {number} */ a = 123;\n  return a;\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_returnIntWithVariableCoercion()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function foo():int { var a:Number = 123.4; return a; };}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @return {number}\n */\norg.apache.royale.B.prototype.foo = function() {\n  var /** @type {number} */ a = 123.4;\n  return (a) >> 0;\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_returnIntWithLiteralCoercion()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function foo():int { return 123.4 };}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @return {number}\n */\norg.apache.royale.B.prototype.foo = function() {\n  return 123;\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_returnUintWithVariableNoCoercion()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function foo():uint { var a:uint = 123; return a; };}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @return {number}\n */\norg.apache.royale.B.prototype.foo = function() {\n  var /** @type {number} */ a = 123;\n  return a;\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_returnUintWithVariableCoercion()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function foo():uint { var a:Number = 123.4; return a; };}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @return {number}\n */\norg.apache.royale.B.prototype.foo = function() {\n  var /** @type {number} */ a = 123.4;\n  return (a) >>> 0;\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_returnUintWithLiteralCoercion()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function foo():uint { return 123.4 };}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @return {number}\n */\norg.apache.royale.B.prototype.foo = function() {\n  return 123;\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_returnBooleanWithVariableNoCoercion()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function foo():Boolean { var a:Boolean = true; return a; };}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @return {boolean}\n */\norg.apache.royale.B.prototype.foo = function() {\n  var /** @type {boolean} */ a = true;\n  return a;\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_returnBooleanWithVariableCoercion()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function foo():Boolean { var a:Number = 123.4; return a; };}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @return {boolean}\n */\norg.apache.royale.B.prototype.foo = function() {\n  var /** @type {number} */ a = 123.4;\n  return !!(a);\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_returnBooleanWithLiteralNoCoercion()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function foo():Boolean { return 123.4 };}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @return {boolean}\n */\norg.apache.royale.B.prototype.foo = function() {\n  return true;\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_override()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; override public function foo():void {};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @override\n */\norg.apache.royale.B.prototype.foo = function() {\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_overrideWithFunctionBody()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; override public function foo(value:Object):void {baz = ''};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @override\n */\norg.apache.royale.B.prototype.foo = function(value) {\n  baz = '';\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_overrideSuperCall()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; override public function foo():void {super.foo();};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @override\n */\norg.apache.royale.B.prototype.foo = function() {\n  org.apache.royale.B.superClass_.foo.apply(this);\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_setterCall()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function set baz(value:Object):void {}; public function set foo(value:Object):void {baz = value;};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\norg.apache.royale.B.prototype.set__baz = function(value) {\n};\n\n\norg.apache.royale.B.prototype.set__foo = function(value) {\n  this.baz = value;\n};\n\n\nObject.defineProperties(org.apache.royale.B.prototype, /** @lends {org.apache.royale.B.prototype} */ {\n/**\n  * @export\n  * @type {Object} */\nbaz: {\nset: org.apache.royale.B.prototype.set__baz},\n/**\n  * @export\n  * @type {Object} */\nfoo: {\nset: org.apache.royale.B.prototype.set__foo}}\n);";
        assertOut(expected);
    }

    @Test
    public void testMethod_overrideSetterSuperCall()
    {
        IClassNode node = getClassNode("public class B extends A {public function B() {}; override public function set foo(value:Object):void {super.foo = value;};} class A {public function set foo(value:Object):void {}}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n * @extends {org.apache.royale.A}\n */\norg.apache.royale.B = function() {\n  org.apache.royale.B.base(this, 'constructor');\n};\ngoog.inherits(org.apache.royale.B, org.apache.royale.A);\n\n\norg.apache.royale.B.prototype.set__foo = function(value) {\n  org.apache.royale.B.superClass_.set__foo.apply(this, [ value] );\n};\n\n\nObject.defineProperties(org.apache.royale.B.prototype, /** @lends {org.apache.royale.B.prototype} */ {\n/**\n  * @export\n  * @type {Object} */\nfoo: {\nset: org.apache.royale.B.prototype.set__foo}}\n);";
        assertOut(expected);
    }

    @Test
    public void testMethod_customNamespace()
    {
        IClassNode node = getClassNode("import custom.custom_namespace; use namespace custom_namespace; public class B {public function B() {}; custom_namespace function foo():void {};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n */\norg.apache.royale.B.prototype.http_$$ns_apache_org$2017$custom$namespace__foo = function() {\n};";
        assertOut(expected);
    }

    @Test
    public void testInnerClassReferencingInnerClass()
    {
    	FileNode node = (FileNode)getNode("package org.apache.royale {\npublic class B {public function B() {}; }} class A {public function get a():A {return null}}", FileNode.class, 0);
        asBlockWalker.visitFile(node);
        String expected = "/**\n * org.apache.royale.B\n *\n * @fileoverview\n *\n * @suppress {checkTypes|accessControls}\n */\n\ngoog.provide('org.apache.royale.B');\ngoog.provide('org.apache.royale.B.A');\n\n\n\n" +
                          "/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};" +
                          "\n\n\n/**\n" + 
                          " * Metadata\n" + 
                          " *\n" + 
                          " * @type {Object.<string, Array.<Object>>}\n" + 
                          " */\n" + 
                          "org.apache.royale.B.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'B', qName: 'org.apache.royale.B', kind: 'class' }] };\n" + 
                          "\n" + 
                          "\n" + 
                          "\n" + 
                          "/**\n" + 
                          " * Reflection\n" + 
                          " *\n" + 
                          " * @return {Object.<string, Function>}\n" + 
                          " */\n" + 
                          "org.apache.royale.B.prototype.ROYALE_REFLECTION_INFO = function () {\n" + 
                          "  return {\n" + 
                          "    methods: function () {\n" + 
                          "      return {\n" + 
                          "        'B': { type: '', declaredBy: 'org.apache.royale.B'}\n" + 
                          "      };\n" + 
                          "    }\n" + 
                          "  };\n" + 
                          "};\n" + 
                          "/**\n" + 
                          " * @const\n" + 
                          " * @type {number}\n" + 
                          " */\n" + 
                          "org.apache.royale.B.prototype.ROYALE_COMPILE_FLAGS = 9;\n" +
                          "\n" + 
                          "\n" + 
                          "\n" + 
                          "/**\n" + 
                          " * @constructor\n" + 
                          " */\n" + 
                          "org.apache.royale.B.A = function() {\n" + 
                          "};\n" + 
                          "\n" + 
                          "\n" + 
                          "org.apache.royale.B.A.prototype.get__a = function() {\n" + 
                          "  return null;\n" + 
                          "};\n" + 
                          "\n" + 
                          "\n" + 
                          "Object.defineProperties(org.apache.royale.B.A.prototype, /** @lends {org.apache.royale.B.A.prototype} */ {\n" + 
                          "/**\n" + 
                          "  * @export\n" + 
                          "  * @type {org.apache.royale.B.A} */\n" + 
                          "a: {\n" + 
                          "get: org.apache.royale.B.A.prototype.get__a}}\n" + 
                          ");\n" + 
                          "\n" + 
                          "\n" + 
                          "/**\n" + 
                          " * Metadata\n" + 
                          " *\n" + 
                          " * @type {Object.<string, Array.<Object>>}\n" + 
                          " */\n" + 
                          "org.apache.royale.B.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'org.apache.royale.B.A', kind: 'class' }] };\n" + 
                          "\n" + 
                          "\n" + 
                          "\n" + 
                          "/**\n" + 
                          " * Reflection\n" + 
                          " *\n" + 
                          " * @return {Object.<string, Function>}\n" + 
                          " */\n" + 
                          "org.apache.royale.B.A.prototype.ROYALE_REFLECTION_INFO = function () {\n" + 
                          "  return {\n" + 
                          "    accessors: function () {\n" + 
                          "      return {\n" + 
                          "        'a': { type: 'org.apache.royale.B.A', access: 'readonly', declaredBy: 'org.apache.royale.B.A'}\n" + 
                          "      };\n" + 
                          "    }\n" + 
                          "  };\n" + 
                          "};\n" + 
                          "/**\n" + 
                          " * @const\n" + 
                          " * @type {number}\n" + 
                          " */\n" + 
                          "org.apache.royale.B.A.prototype.ROYALE_COMPILE_FLAGS = 9;\n" +
                          "";
        assertOutWithMetadata(expected);
    }

    @Override
    @Test
    public void testExtendsConstructor_withArguments()
    {
        IClassNode node = getClassNode("public class A extends custom.TestImplementation {public function A(arg1:String, arg2:int) {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {custom.TestImplementation}\n * @param {string} arg1\n * @param {number} arg2\n */\norg.apache.royale.A = function(arg1, arg2) {\n  org.apache.royale.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.royale.A, custom.TestImplementation);");
    }

    @Override
    @Test
    public void testFields()
    {
        IClassNode node = getClassNode("public class A {public var a:Object;protected var b:String; "
                + "private var c:int; internal var d:uint; var e:Number}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.A = function() {\n};\n\n\n/**\n * @type {Object}\n */\norg.apache.royale.A.prototype.a = null;\n\n\n/**\n * @protected\n * @type {string}\n */\norg.apache.royale.A.prototype.b = null;\n\n\n/**\n * @private\n * @type {number}\n */\norg.apache.royale.A.prototype.c = 0;\n\n\n/**\n * @package\n * @type {number}\n */\norg.apache.royale.A.prototype.d = 0;\n\n\n/**\n * @package\n * @type {number}\n */\norg.apache.royale.A.prototype.e = NaN;");
    }

    @Test
    public void testBindableFields()
    {
        IClassNode node = getClassNode("public class A {[Bindable] public var a:Object;[Bindable] protected var b:String; "
                + "[Bindable] private var c:int; internal var d:uint; var e:Number}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n" +
        		  " */\norg.apache.royale.A = function() {\n" +
        		  "};\n\n\n" +
        		  "/**\n" +
        		  " * @type {Object}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.a_ = null;\n\n\n" +
        		  "/**\n" +
        		  " * @protected\n" +
        		  " * @type {string}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.b_ = null;\n\n\n" +
        		  "/**\n" +
        		  " * @private\n" +
        		  " * @type {number}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.c_ = 0;\n\n\n" +
                  "/**\n" +
                  " * @package\n" + 
        		  " * @type {number}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.d = 0;\n\n\n" +
                  "/**\n" +
                  " * @package\n" +
        		  " * @type {number}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.e = NaN;Object.defineProperties(org.apache.royale.A.prototype, /** @lends {org.apache.royale.A.prototype} */ {\n" +
        		  "/** @export\n" +
    			  "  * @type {Object} */\n" +
    			  "a: {\n" +
    			  "/** @this {org.apache.royale.A} */\n" +
    			  "  get: function() {\n" +
    			  "  return this.a_;\n" +
    			  "  },\n" +
    			  "\n" +
    			  "/** @this {org.apache.royale.A} */\n" +
    			  "set: function(value) {\n" +
    			  "if (value != this.a_) {\n" +
    			  "    var oldValue = this.a_;\n" +
    			  "    this.a_ = value;\n" +
    			  "    this.dispatchEvent(org.apache.royale.events.ValueChangeEvent.createUpdateEvent(\n" +
    			  "         this, \"a\", oldValue, value));\n" +
    			  "}\n" +
    			  "}}," +
    			  "/** @export\n" +
        		  "  * @private\n" +
        		  "  * @type {string} */\n" +
        		  "b: {\n" +
        		  "/** @this {org.apache.royale.A} */\n" +
        		  "  get: function() {\n" +
        		  "  return this.b_;\n" +
    			  "  },\n" +
    			  "\n" +
    			  "/** @this {org.apache.royale.A} */\n" +
    			  "set: function(value) {\n" +
    			  "if (value != this.b_) {\n" +
    			  "    var oldValue = this.b_;\n" +
    			  "    this.b_ = value;\n" +
    			  "    this.dispatchEvent(org.apache.royale.events.ValueChangeEvent.createUpdateEvent(\n" +
    			  "         this, \"b\", oldValue, value));\n" +
    			  "}\n" +
    			  "}},/** @export\n" +
    			  "  * @private\n" +
    			  "  * @type {number} */\n" +
    			  "c: {\n" +
    			  "/** @this {org.apache.royale.A} */\n" +
    			  "  get: function() {\n" +
    			  "  return this.c_;\n" +
    			  "  },\n" +
    			  "\n" +
    			  "/** @this {org.apache.royale.A} */\n" +
    			  "set: function(value) {\n" +
    			  "if (value != this.c_) {\n" +
    			  "    var oldValue = this.c_;\n" +
    			  "    this.c_ = value;\n" +
    			  "    this.dispatchEvent(org.apache.royale.events.ValueChangeEvent.createUpdateEvent(\n" +
    			  "         this, \"c\", oldValue, value));\n" +
    			  "}\n" +
    			  "}}}\n" +
        		  ");");
    }

    @Test
    public void testBindableFieldsWithInitialComplexValue()
    {
        IClassNode node = getClassNode("public class A {[Bindable] public var a:Object = { foo: 1 };[Bindable] protected var b:String; "
                + "[Bindable] private var c:int; internal var d:uint; var e:Number}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n" +
        		  " */\norg.apache.royale.A = function() {\n\n" +
        		  "this.a_ = {foo:1};\n" +
        		  "};\n\n\n" +
        		  "/**\n" +
        		  " * @type {Object}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.a_ = null;\n\n\n" +
        		  "/**\n" +
        		  " * @protected\n" +
        		  " * @type {string}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.b_ = null;\n\n\n" +
        		  "/**\n" +
        		  " * @private\n" +
        		  " * @type {number}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.c_ = 0;\n\n\n" +
                  "/**\n" +
                  " * @package\n" + 
        		  " * @type {number}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.d = 0;\n\n\n" +
        		  "/**\n" +
                  " * @package\n" + 
        		  " * @type {number}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.e = NaN;Object.defineProperties(org.apache.royale.A.prototype, /** @lends {org.apache.royale.A.prototype} */ {\n" +
        		  "/** @export\n" +
    			  "  * @type {Object} */\n" +
    			  "a: {\n" +
    			  "/** @this {org.apache.royale.A} */\n" +
    			  "  get: function() {\n" +
    			  "  return this.a_;\n" +
    			  "  },\n" +
    			  "\n" +
    			  "/** @this {org.apache.royale.A} */\n" +
    			  "set: function(value) {\n" +
    			  "if (value != this.a_) {\n" +
    			  "    var oldValue = this.a_;\n" +
    			  "    this.a_ = value;\n" +
    			  "    this.dispatchEvent(org.apache.royale.events.ValueChangeEvent.createUpdateEvent(\n" +
    			  "         this, \"a\", oldValue, value));\n" +
    			  "}\n" +
    			  "}}," +
    			  "/** @export\n" +
        		  "  * @private\n" +
        		  "  * @type {string} */\n" +
        		  "b: {\n" +
        		  "/** @this {org.apache.royale.A} */\n" +
        		  "  get: function() {\n" +
        		  "  return this.b_;\n" +
    			  "  },\n" +
    			  "\n" +
    			  "/** @this {org.apache.royale.A} */\n" +
    			  "set: function(value) {\n" +
    			  "if (value != this.b_) {\n" +
    			  "    var oldValue = this.b_;\n" +
    			  "    this.b_ = value;\n" +
    			  "    this.dispatchEvent(org.apache.royale.events.ValueChangeEvent.createUpdateEvent(\n" +
    			  "         this, \"b\", oldValue, value));\n" +
    			  "}\n" +
    			  "}},/** @export\n" +
    			  "  * @private\n" +
    			  "  * @type {number} */\n" +
    			  "c: {\n" +
    			  "/** @this {org.apache.royale.A} */\n" +
    			  "  get: function() {\n" +
    			  "  return this.c_;\n" +
    			  "  },\n" +
    			  "\n" +
    			  "/** @this {org.apache.royale.A} */\n" +
    			  "set: function(value) {\n" +
    			  "if (value != this.c_) {\n" +
    			  "    var oldValue = this.c_;\n" +
    			  "    this.c_ = value;\n" +
    			  "    this.dispatchEvent(org.apache.royale.events.ValueChangeEvent.createUpdateEvent(\n" +
    			  "         this, \"c\", oldValue, value));\n" +
    			  "}\n" +
    			  "}}}\n" +
        		  ");");
    }

    @Test
    public void testBindableClass()
    {
        IClassNode node = getClassNode("[Bindable] public class A {public var a:Object;protected var b:String; "
                + "private var c:int; internal var d:uint; var e:Number}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n" +
        		  " */\norg.apache.royale.A = function() {\n" +
        		  "};\n\n\n" +
        		  "/**\n" +
        		  " * @type {Object}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.a_ = null;\n\n\n" +
        		  "/**\n" +
        		  " * @protected\n" +
        		  " * @type {string}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.b = null;\n\n\n" +
        		  "/**\n" +
        		  " * @private\n" +
        		  " * @type {number}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.c = 0;\n\n\n" +
        		  "/**\n" +
                  " * @package\n" + 
        		  " * @type {number}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.d = 0;\n\n\n" +
        		  "/**\n" +
                  " * @package\n" + 
        		  " * @type {number}\n" +
        		  " */\n" +
        		  "org.apache.royale.A.prototype.e = NaN;Object.defineProperties(org.apache.royale.A.prototype, /** @lends {org.apache.royale.A.prototype} */ {\n" +
        		  "/** @export\n" +
        		  "  * @type {Object} */\n" +
    			  "a: {\n" +
    			  "/** @this {org.apache.royale.A} */\n" +
    			  "  get: function() {\n" +
    			  "  return this.a_;\n" +
    			  "  },\n" +
    			  "\n" +
    			  "/** @this {org.apache.royale.A} */\n" +
    			  "set: function(value) {\n" +
    			  "if (value != this.a_) {\n" +
    			  "    var oldValue = this.a_;\n" +
    			  "    this.a_ = value;\n" +
    			  "    this.dispatchEvent(org.apache.royale.events.ValueChangeEvent.createUpdateEvent(\n" +
    			  "         this, \"a\", oldValue, value));\n" +
    			  "}\n" +
    			  "}}}\n" +
        		  ");");
    }

    @Test
    public void testFieldsWithStaticInitializers()
    {
        IClassNode node = getClassNode("public class A {public static var a:int = 10;public static var b:String = initStatic(); "
                + "private static function initStatic():String { return \"foo\"; }}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.A = function() {\n};\n\n\n/**\n * @nocollapse\n * @type {number}\n */\norg.apache.royale.A.a = 10;\n\n\n/**\n * @nocollapse\n * @type {string}\n */\norg.apache.royale.A.b;\n\n\n/**\n * @private\n * @return {string}\n */\norg.apache.royale.A.initStatic = function() {\n  return \"foo\";\n};\n\norg.apache.royale.A.b = org.apache.royale.A.initStatic();\n\n");
    }
    
    @Test
    public void testImportForceLinkingAsStaticInitializers()
    {
        FileNode node = (FileNode)getNode("package org.apache.royale {\npublic class A {\nimport flash.display.Sprite; Sprite;\n}}", FileNode.class, 0);
        asBlockWalker.visitFile(node);
        assertOut("/**\n * org.apache.royale.A\n *\n * @fileoverview\n *\n * @suppress {checkTypes|accessControls}\n */\n\ngoog.provide('org.apache.royale.A');\n\n\n\n/**\n * @constructor\n */\norg.apache.royale.A = function() {\n};");
    }
    
    @Override
    @Test
    public void testConstants()
    {
        IClassNode node = getClassNode("import custom.custom_namespace;public class A {"
                + "public static const A:int = 42;"
                + "protected static const B:Number = 42;"
                + "private static const C:Number = 42;"
                + "custom_namespace static const C:String = 'me' + 'you';}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.A = function() {\n};\n\n\n/**\n * @nocollapse\n * @const\n * @type {number}\n */\norg.apache.royale.A.A = 42;\n\n\n/**\n * @protected\n * @nocollapse\n * @const\n * @type {number}\n */\norg.apache.royale.A.B = 42;\n\n\n/**\n * @private\n * @const\n * @type {number}\n */\norg.apache.royale.A.C = 42;\n\n\n/**\n * @const\n * @type {string}\n */\norg.apache.royale.A.http_$$ns_apache_org$2017$custom$namespace__C = 'me' + 'you';");
    }

    @Override
    @Test
    public void testAccessors()
    {
        IClassNode node = getClassNode("import custom.custom_namespace;public class A {"
                + "public function get foo1():Object{return null;}"
                + "public function set foo1(value:Object):void{}"
                + "protected function get foo2():Object{return null;}"
                + "protected function set foo2(value:Object):void{}"
                + "private function get foo3():Object{return null;}"
                + "private function set foo3(value:Object):void{}"
                + "internal function get foo5():Object{return null;}"
                + "internal function set foo5(value:Object):void{}"
                + "custom_namespace function get foo6():Object{return null;}"
                + "custom_namespace function set foo6(value:Object):void{}" + "}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.A = function() {\n};\n\n\n" +
        		"org.apache.royale.A.prototype.get__foo1 = function() {\n  return null;\n};\n\n\n" +
        		"org.apache.royale.A.prototype.set__foo1 = function(value) {\n};\n\n\n" +
        		"org.apache.royale.A.prototype.get__foo2 = function() {\n  return null;\n};\n\n\n" +
        		"org.apache.royale.A.prototype.set__foo2 = function(value) {\n};\n\n\n" +
        		"org.apache.royale.A.prototype.get__foo3 = function() {\n  return null;\n};\n\n\n" +
        		"org.apache.royale.A.prototype.set__foo3 = function(value) {\n};\n\n\n" +
        		"org.apache.royale.A.prototype.get__foo5 = function() {\n  return null;\n};\n\n\n" +
        		"org.apache.royale.A.prototype.set__foo5 = function(value) {\n};\n\n\n" +
        		"org.apache.royale.A.prototype.http_$$ns_apache_org$2017$custom$namespace__get__foo6 = function() {\n  return null;\n};\n\n\n" +
        		"org.apache.royale.A.prototype.http_$$ns_apache_org$2017$custom$namespace__set__foo6 = function(value) {\n};\n\n\n" +
        		"Object.defineProperties(org.apache.royale.A.prototype, /** @lends {org.apache.royale.A.prototype} */ {\n/**\n  * @export\n  * @type {Object} */\n" +
        		    "foo1: {\nget: org.apache.royale.A.prototype.get__foo1,\nset: org.apache.royale.A.prototype.set__foo1},\n/**\n  * @type {Object} */\n" +
        		    "foo2: {\nget: org.apache.royale.A.prototype.get__foo2,\nset: org.apache.royale.A.prototype.set__foo2},\n/**\n  * @type {Object} */\n" +
        		    "foo3: {\nget: org.apache.royale.A.prototype.get__foo3,\nset: org.apache.royale.A.prototype.set__foo3},\n/**\n  * @type {Object} */\n" +
        		    "foo5: {\nget: org.apache.royale.A.prototype.get__foo5,\nset: org.apache.royale.A.prototype.set__foo5},\n/**\n  * @export\n  * @type {Object} */\n" +
        		    "http_$$ns_apache_org$2017$custom$namespace__foo6: {\nget: org.apache.royale.A.prototype.http_$$ns_apache_org$2017$custom$namespace__get__foo6,\n" +
        		    																"set: org.apache.royale.A.prototype.http_$$ns_apache_org$2017$custom$namespace__set__foo6}}\n);");
    }

    @Override
    @Test
    public void testMethods()
    {
        IClassNode node = getClassNode("import custom.custom_namespace;public class A {"
                + "public function foo1():Object{return null;}"
                + "public final function foo1a():Object{return null;}"
                + "override public function foo1b():Object{return super.foo1b();}"
                + "protected function foo2(value:Object):void{}"
                + "private function foo3(value:Object):void{}"
                + "internal function foo5(value:Object):void{}"
                + "custom_namespace function foo6(value:Object):void{}"
                + "public static function foo7(value:Object):void{}"
                + "custom_namespace static function foo7(value:Object):void{}" + "}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.A = function() {\n};\n\n\n/**\n * @return {Object}\n */\norg.apache.royale.A.prototype.foo1 = function() {\n  return null;\n};\n\n\n/**\n * @return {Object}\n */\norg.apache.royale.A.prototype.foo1a = function() {\n  return null;\n};\n\n\n/**\n * @override\n */\norg.apache.royale.A.prototype.foo1b = function() {\n  return org.apache.royale.A.superClass_.foo1b.apply(this);\n};\n\n\n/**\n * @protected\n * @param {Object} value\n */\norg.apache.royale.A.prototype.foo2 = function(value) {\n};\n\n\n/**\n * @private\n * @param {Object} value\n */\norg.apache.royale.A.prototype.foo3 = function(value) {\n};\n\n\n/**\n * @package\n * @param {Object} value\n */\norg.apache.royale.A.prototype.foo5 = function(value) {\n};\n\n\n/**\n * @param {Object} value\n */\norg.apache.royale.A.prototype.http_$$ns_apache_org$2017$custom$namespace__foo6 = function(value) {\n};\n\n\n/**\n * @nocollapse\n * @param {Object} value\n */\norg.apache.royale.A.foo7 = function(value) {\n};\n\n\n/**\n * @nocollapse\n * @param {Object} value\n */\norg.apache.royale.A.http_$$ns_apache_org$2017$custom$namespace__foo7 = function(value) {\n};");
    }

    @Test
    public void testMethodsWithLocalFunctions()
    {
        IClassNode node = getClassNode("public class B {"
                + "public function foo1():Object{function bar1():Object {return null;}; return bar1()}"
                + "public function foo2():Object{function bar2(param1:Object):Object {return null;}; return bar2('foo');}"
                + "}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @return {Object}\n */\norg.apache.royale.B.prototype.foo1 = function() {\n  var self = this;\n  function bar1() {\n    return null;\n  };\n  return bar1();\n};\n\n\n/**\n * @return {Object}\n */\norg.apache.royale.B.prototype.foo2 = function() {\n  var self = this;\n  function bar2(param1) {\n    return null;\n  };\n  return bar2('foo');\n};");
    }

    @Test
    public void testMethodsWithLocalFunctions2()
    {
        IClassNode node = getClassNode("public class B {"
                + "public var baz1:String;"
                + "public function foo1():String{function bar1():String {return baz1;}; return bar1()}"
                + "public function foo2():String{function bar2(param1:String):String {return param1 + baz1;}; return bar2('foo');}"
                + "}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @type {string}\n */\norg.apache.royale.B.prototype.baz1 = null;\n\n\n/**\n * @return {string}\n */\norg.apache.royale.B.prototype.foo1 = function() {\n  var self = this;\n  function bar1() {\n    return self.baz1;\n  };\n  return bar1();\n};\n\n\n/**\n * @return {string}\n */\norg.apache.royale.B.prototype.foo2 = function() {\n  var self = this;\n  function bar2(param1) {\n    return param1 + self.baz1;\n  };\n  return bar2('foo');\n};");
    }

    @Test
    public void testClassWithoutConstructor()
    {
        /* AJH couldn't find a way to reproduce the code paths
         * in a simple test case.  May require multiple compilation
         * units in the same package.
         */
        
        // (erikdebruin) what's wrong with this test case and/or the resulting code?
        
        // (erikdebruin) if you're looking for a way to test multiple cu's 
        //               (a project), look in 'TestGoogProject' for an example
        
        IClassNode node = getClassNode("public class B {"
                + "public function clone():B { return new B() }"
                + "}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.B = function() {\n};\n\n\n/**\n * @return {org.apache.royale.B}\n */\norg.apache.royale.B.prototype.clone = function() {\n  return new org.apache.royale.B();\n};");
    }

    @Override
    @Test
    public void testSimple()
    {
        IClassNode node = getClassNode("public class A{}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.A = function() {\n};");
    }

    @Override
    @Test
    public void testSimpleInternal()
    {
        // (erikdebruin) the AS compiler will enforce 'internal' namespace, 
        //               in JS we ignore it
        IClassNode node = getClassNode("internal class A{}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.A = function() {\n};");
    }

    @Override
    @Test
    public void testSimpleFinal()
    {
        // (erikdebruin) the AS compiler will enforce the 'final' keyword, 
        //               in JS we ignore it
        IClassNode node = getClassNode("public final class A{}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.A = function() {\n};");
    }

    @Override
    @Test
    public void testSimpleDynamic()
    {
        // (erikdebruin) all JS objects are 'dynamic' by design
        IClassNode node = getClassNode("public dynamic class A{}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.A = function() {\n};");
    }

    @Override
    @Test
    public void testSimpleImplements()
    {
        IClassNode node = getClassNode("public class A implements TestInterface {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {custom.TestInterface}\n */\norg.apache.royale.A = function() {\n};");
    }

    @Override
    @Test
    public void testSimpleImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A implements TestInterface, TestOtherInterface {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {custom.TestInterface}\n * @implements {custom.TestOtherInterface}\n */\norg.apache.royale.A = function() {\n};");
    }


    @Override
    @Test
    public void testConstructor()
    {
        IClassNode node = getClassNode("public class A {public function A() { }}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.royale.A = function() {\n};");
    }


    @Override
    @Test
    public void testConstructor_withArguments()
    {
        IClassNode node = getClassNode("public class A {public function A(arg1:String, arg2:int) {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @param {string} arg1\n * @param {number} arg2\n */\norg.apache.royale.A = function(arg1, arg2) {\n};");
    }

    @Test
    public void testConstructor_withBodyAndComplexInitializer()
    {
        IClassNode node = getClassNode("public class A {public function A(arg1:String, arg2:int) {arg2 = arg2 + 2;} public var foo:Array = [];}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @param {string} arg1\n * @param {number} arg2\n */\norg.apache.royale.A = function(arg1, arg2) {\n  \n  this.foo = [];\n  arg2 = (arg2 + 2) >> 0;\n};\n\n\n/**\n * @type {Array}\n */\norg.apache.royale.A.prototype.foo = null;");
    }

    @Test
    public void testConstructor_withBodyAndComplexCustomNamespaceInitializer()
    {
        IClassNode node = getClassNode("import custom.custom_namespace; use namespace custom_namespace; public class A {public function A(arg1:String, arg2:int) {arg2 = arg2 + 2;} custom_namespace var foo:Array = [];}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @param {string} arg1\n * @param {number} arg2\n */\norg.apache.royale.A = function(arg1, arg2) {\n  \n  this.http_$$ns_apache_org$2017$custom$namespace__foo = [];\n  arg2 = (arg2 + 2) >> 0;\n};\n\n\n/**\n * @type {Array}\n */\norg.apache.royale.A.prototype.http_$$ns_apache_org$2017$custom$namespace__foo = null;");
    }

    @Test
    public void testConstructor_withImplicitSuperAndBodyAndComplexInitializer()
    {
        IClassNode node = getClassNode("public class A extends TestImplementation {public function A(arg1:String, arg2:int) {arg2 = arg2 + 2;} public var foo:Array = [];}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {custom.TestImplementation}\n * @param {string} arg1\n * @param {number} arg2\n */\norg.apache.royale.A = function(arg1, arg2) {\n  org.apache.royale.A.base(this, 'constructor');\n  \n  this.foo = [];\n  arg2 = (arg2 + 2) >> 0;\n};\ngoog.inherits(org.apache.royale.A, custom.TestImplementation);\n\n\n/**\n * @type {Array}\n */\norg.apache.royale.A.prototype.foo = null;");
    }

    @Test
    public void testConstructor_withBodyAndStaticInitializer()
    {
        IClassNode node = getClassNode("public class A {public static const NAME:String = 'Dummy'; public function A(arg1:String = NAME) {_name = arg1;} private var _name:String;}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @param {string=} arg1\n */\norg.apache.royale.A = function(arg1) {\n  arg1 = typeof arg1 !== 'undefined' ? arg1 : org.apache.royale.A.NAME;\n  this._name = arg1;\n};\n\n\n/**\n * @nocollapse\n * @const\n * @type {string}\n */\norg.apache.royale.A.NAME = 'Dummy';\n\n\n/**\n * @private\n * @type {string}\n */\norg.apache.royale.A.prototype._name = null;");
    }
    
    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }

}
