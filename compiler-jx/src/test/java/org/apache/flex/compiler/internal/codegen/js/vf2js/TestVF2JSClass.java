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

package org.apache.flex.compiler.internal.codegen.js.vf2js;

import java.io.File;
import java.util.List;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogClass;
import org.apache.flex.compiler.internal.driver.js.vf2js.VF2JSBackend;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestVF2JSClass extends TestGoogClass
{

    @Override
    @Test
    public void testSimple()
    {
        IClassNode node = getClassNode("public class A{}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {};");
    }

    @Override
    @Test
    public void testSimpleInternal()
    {
        // (erikdebruin) the AS compiler will enforce 'internal' namespace, 
        //               in JS we ignore it
        IClassNode node = getClassNode("internal class A{}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {};");
    }

    @Override
    @Test
    public void testSimpleFinal()
    {
        // (erikdebruin) the AS compiler will enforce the 'final' keyword, 
        //               in JS we ignore it
        IClassNode node = getClassNode("public final class A{}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {};");
    }

    @Override
    @Test
    public void testSimpleDynamic()
    {
        // (erikdebruin) all JS objects are 'dynamic' by design
        IClassNode node = getClassNode("public dynamic class A{}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {};");
    }

    @Override
    @Test
    public void testConstructor_super()
    {
        IClassNode node = getClassNode("public class A {public function A() { super(); }}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n  ;\n};");
    }

    @Override
    @Test
    public void testSimpleExtends()
    {
        IClassNode node = getClassNode("public class A extends Button {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n */\norg.apache.flex.A = function() {\n  org.apache.flex.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testSimpleImplements()
    {
        IClassNode node = getClassNode("public class A implements IEventDispatcher {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {flash.events.IEventDispatcher}\n */\norg.apache.flex.A = function() {};");
    }

    @Override
    @Test
    public void testSimpleImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A implements IEventDispatcher, ILogger {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {flash.events.IEventDispatcher}\n * @implements {mx.logging.ILogger}\n */\norg.apache.flex.A = function() {};");
    }

    @Override
    @Test
    public void testSimpleExtendsImplements()
    {
        IClassNode node = getClassNode("public class A extends Button implements IEventDispatcher {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @implements {flash.events.IEventDispatcher}\n */\norg.apache.flex.A = function() {\n  org.apache.flex.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testSimpleExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A extends Button implements IEventDispatcher, ILogger {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @implements {flash.events.IEventDispatcher}\n * @implements {mx.logging.ILogger}\n */\norg.apache.flex.A = function() {\n  org.apache.flex.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testSimpleFinalExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public final class A extends Button implements IEventDispatcher, ILogger {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @implements {flash.events.IEventDispatcher}\n * @implements {mx.logging.ILogger}\n */\norg.apache.flex.A = function() {\n  org.apache.flex.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testQualifiedExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A extends spark.components.Button implements flash.events.IEventDispatcher, mx.logging.ILogger {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @implements {flash.events.IEventDispatcher}\n * @implements {mx.logging.ILogger}\n */\norg.apache.flex.A = function() {\n  org.apache.flex.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testConstructor()
    {
        IClassNode node = getClassNode("public class A {public function A() { }}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {};");
    }

    @Override
    @Test
    public void testExtendsConstructor_super()
    {
        IClassNode node = getClassNode("public class A extends spark.components.Button { public function A() { super('foo', 42);}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n */\norg.apache.flex.A = function() {\n  org.apache.flex.A.base(this, 'constructor', 'foo', 42);\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testConstructor_withArguments()
    {
        IClassNode node = getClassNode("public class A {public function A(arg1:String, arg2:int) {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @param {string} arg1\n * @param {number} arg2\n */\norg.apache.flex.A = function(arg1, arg2) {};");
    }

    @Test
    public void testConstructor_withArgumentNameMatchingMemberName()
    {
        IClassNode node = getClassNode("public class B {public function B(arg1:String) {this.arg1 = arg1}; public var arg1:String;}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n * @param {string} arg1\n */\norg.apache.flex.B = function(arg1) {\n  this.arg1 = arg1;\n};\n\n\n/**\n * @type {string}\n */\norg.apache.flex.B.prototype.arg1;";
        assertOut(expected);
    }

    @Test
    public void testMethod_withImplicitSelfInReturnValue()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public var button:Button = new Button(); public function foo():String {return button.label;};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.flex.B = function() {\n  this.button = new spark.components.Button();\n};\n\n\n/**\n * @type {spark.components.Button}\n */\norg.apache.flex.B.prototype.button;\n\n\n/**\n * @export\n * @return {string}\n */\norg.apache.flex.B.prototype.foo = function() {\n  return this.button.get_label();\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_noArgsNoReturn()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function foo():void {};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.flex.B = function() {};\n\n\n/**\n * @export\n */\norg.apache.flex.B.prototype.foo = function() {\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_override()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; override public function foo():void {};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.flex.B = function() {};\n\n\n/**\n * @export\n * @override\n */\norg.apache.flex.B.prototype.foo = function() {\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_overrideWithFunctionBody()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; override public function foo(value:Object):void {baz = ''};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.flex.B = function() {};\n\n\n/**\n * @export\n * @param {Object} value\n * @override\n */\norg.apache.flex.B.prototype.foo = function(value) {\n  baz = '';\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_overrideSuperCall()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; override public function foo():void {super.foo();};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.flex.B = function() {};\n\n\n/**\n * @export\n * @override\n */\norg.apache.flex.B.prototype.foo = function() {\n  org.apache.flex.B.base(this, 'foo');\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_setterCall()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; public function set baz(value:Object):void {}; public function set foo(value:Object):void {baz = value;};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.flex.B = function() {};\n\n\n/**\n * @export\n * @param {Object} value\n */\norg.apache.flex.B.prototype.set_baz = function(value) {\n};\n\n\n/**\n * @export\n * @param {Object} value\n */\norg.apache.flex.B.prototype.set_foo = function(value) {\n  this.set_baz(value);\n};";
        assertOut(expected);
    }

    @Test
    public void testMethod_overrideSetterSuperCall()
    {
        IClassNode node = getClassNode("public class B {public function B() {}; override public function set foo(value:Object):void {super.foo = value;};}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\norg.apache.flex.B = function() {};\n\n\n/**\n * @export\n * @param {Object} value\n * @override\n */\norg.apache.flex.B.prototype.set_foo = function(value) {\n  org.apache.flex.B.base(this, 'set_foo', value);\n};";
        assertOut(expected);
    }

    @Override
    @Test
    public void testExtendsConstructor_withArguments()
    {
        IClassNode node = getClassNode("public class A extends spark.components.Button {public function A(arg1:String, arg2:int) {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @param {string} arg1\n * @param {number} arg2\n */\norg.apache.flex.A = function(arg1, arg2) {\n  org.apache.flex.A.base(this, 'constructor', arg1, arg2);\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testFields()
    {
        IClassNode node = getClassNode("public class A {public var a:Object;protected var b:String; "
                + "private var c:int; internal var d:uint; var e:Number}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {};\n\n\n/**\n * @type {Object}\n */\norg.apache.flex.A.prototype.a;\n\n\n/**\n * @protected\n * @type {string}\n */\norg.apache.flex.A.prototype.b;\n\n\n/**\n * @private\n * @type {number}\n */\norg.apache.flex.A.prototype.c;\n\n\n/**\n * @type {number}\n */\norg.apache.flex.A.prototype.d;\n\n\n/**\n * @type {number}\n */\norg.apache.flex.A.prototype.e;");
    }

    @Test
    public void testFieldWithEmbed()
    {
        IClassNode node = getClassNode("public class A {[Embed(source=\"LuminosityMaskFilter.pbj\", mimeType=\"application/octet-stream\")]\nprivate static var ShaderClass:Class;}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {};\n\n\n/**\n * @private\n * @type {Object}\n */\norg.apache.flex.A.ShaderClass;");
    }
    
    @Test
    public void testFieldWithObjectAssignment()
    {
    	IClassNode node = getClassNode("public class A {private var controlBarGroupProperties:Object = { visible: true }; private var _visible:Boolean; public function get visible():Boolean { return _visible; }; public function set visible(value:Boolean):void { _visible = value; };}");
    	asBlockWalker.visitClass(node);
    	assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {};\n\n\n/**\n * @private\n * @type {Object}\n */\norg.apache.flex.A.prototype.controlBarGroupProperties = {visible:true};\n\n\n/**\n * @private\n * @type {boolean}\n */\norg.apache.flex.A.prototype._visible;\n\n\n/**\n * @export\n * @return {boolean}\n */\norg.apache.flex.A.prototype.get_visible = function() {\n  return this._visible;\n};\n\n\n/**\n * @export\n * @param {boolean} value\n */\norg.apache.flex.A.prototype.set_visible = function(value) {\n  this._visible = value;\n};");
    }

    @Override
    @Test
    public void testConstants()
    {
        IClassNode node = getClassNode("public class A {"
                + "public static const A:int = 42;"
                + "protected static const B:Number = 42;"
                + "private static const C:Number = 42;"
                + "foo_bar static const C:String = 'me' + 'you';");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n  org.apache.flex.A.C = 'me' + 'you';\n};\n\n\n/**\n * @const\n * @type {number}\n */\norg.apache.flex.A.A = 42;\n\n\n/**\n * @protected\n * @const\n * @type {number}\n */\norg.apache.flex.A.B = 42;\n\n\n/**\n * @private\n * @const\n * @type {number}\n */\norg.apache.flex.A.C = 42;\n\n\n/**\n * @const\n * @type {string}\n */\norg.apache.flex.A.C;");
    }

    @Override
    @Test
    public void testAccessors()
    {
        IClassNode node = getClassNode("public class A {"
                + "public function get foo1():Object{return null;}"
                + "public function set foo1(value:Object):void{}"
                + "protected function get foo2():Object{return null;}"
                + "protected function set foo2(value:Object):void{}"
                + "private function get foo3():Object{return null;}"
                + "private function set foo3(value:Object):void{}"
                + "internal function get foo5():Object{return null;}"
                + "internal function set foo5(value:Object):void{}"
                + "foo_bar function get foo6():Object{return null;}"
                + "foo_bar function set foo6(value:Object):void{}" + "}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {};\n\n\n/**\n * @export\n * @return {Object}\n */\norg.apache.flex.A.prototype.get_foo1 = function() {\n  return null;\n};\n\n\n/**\n * @export\n * @param {Object} value\n */\norg.apache.flex.A.prototype.set_foo1 = function(value) {\n};\n\n\n/**\n * @protected\n * @return {Object}\n */\norg.apache.flex.A.prototype.get_foo2 = function() {\n  return null;\n};\n\n\n/**\n * @protected\n * @param {Object} value\n */\norg.apache.flex.A.prototype.set_foo2 = function(value) {\n};\n\n\n/**\n * @private\n * @return {Object}\n */\norg.apache.flex.A.prototype.get_foo3 = function() {\n  return null;\n};\n\n\n/**\n * @private\n * @param {Object} value\n */\norg.apache.flex.A.prototype.set_foo3 = function(value) {\n};\n\n\n/**\n * @return {Object}\n */\norg.apache.flex.A.prototype.get_foo5 = function() {\n  return null;\n};\n\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.prototype.set_foo5 = function(value) {\n};\n\n\n/**\n * @return {Object}\n */\norg.apache.flex.A.prototype.get_foo6 = function() {\n  return null;\n};\n\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.prototype.set_foo6 = function(value) {\n};");
    }

    @Override
    @Test
    public void testMethods()
    {
        IClassNode node = getClassNode("public class A {"
                + "public function foo1():Object{return null;}"
                + "public final function foo1a():Object{return null;}"
                + "override public function foo1b():Object{return super.foo1b();}"
                + "protected function foo2(value:Object):void{}"
                + "private function foo3(value:Object):void{}"
                + "internal function foo5(value:Object):void{}"
                + "foo_bar function foo6(value:Object):void{}"
                + "public static function foo7(value:Object):void{}"
                + "foo_bar static function foo7(value:Object):void{}" + "}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {};\n\n\n/**\n * @export\n * @return {Object}\n */\norg.apache.flex.A.prototype.foo1 = function() {\n  return null;\n};\n\n\n/**\n * @export\n * @return {Object}\n */\norg.apache.flex.A.prototype.foo1a = function() {\n  return null;\n};\n\n\n/**\n * @export\n * @return {Object}\n * @override\n */\norg.apache.flex.A.prototype.foo1b = function() {\n  return org.apache.flex.A.base(this, 'foo1b');\n};\n\n\n/**\n * @protected\n * @param {Object} value\n */\norg.apache.flex.A.prototype.foo2 = function(value) {\n};\n\n\n/**\n * @private\n * @param {Object} value\n */\norg.apache.flex.A.prototype.foo3 = function(value) {\n};\n\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.prototype.foo5 = function(value) {\n};\n\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.prototype.foo6 = function(value) {\n};\n\n\n/**\n * @export\n * @param {Object} value\n */\norg.apache.flex.A.foo7 = function(value) {\n};\n\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.foo7 = function(value) {\n};");
    }

    @Test
    public void testMethodsWithLocalFunctions()
    {
        IClassNode node = getClassNode("public class B {"
                + "public function foo1():Object{function bar1():Object {return null;}; return bar1()}"
                + "public function foo2():Object{function bar2(param1:Object):Object {return null;}; return bar2('foo');}"
                + "}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.B = function() {};\n\n\n/**\n * @export\n * @return {Object}\n */\norg.apache.flex.B.prototype.foo1 = function() {\n  function bar1() {\n    return null;\n  };\n  return goog.bind(bar1, this)();\n};\n\n\n/**\n * @export\n * @return {Object}\n */\norg.apache.flex.B.prototype.foo2 = function() {\n  function bar2(param1) {\n    return null;\n  };\n  return goog.bind(bar2, this)('foo');\n};");
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
        assertOut("/**\n * @constructor\n */\norg.apache.flex.B = function() {};\n\n\n/**\n * @type {string}\n */\norg.apache.flex.B.prototype.baz1;\n\n\n/**\n * @export\n * @return {string}\n */\norg.apache.flex.B.prototype.foo1 = function() {\n  function bar1() {\n    return this.baz1;\n  };\n  return goog.bind(bar1, this)();\n};\n\n\n/**\n * @export\n * @return {string}\n */\norg.apache.flex.B.prototype.foo2 = function() {\n  function bar2(param1) {\n    return param1 + this.baz1;\n  };\n  return goog.bind(bar2, this)('foo');\n};");
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
        assertOut("/**\n * @constructor\n */\norg.apache.flex.B = function() {};\n\n\n/**\n * @export\n * @return {org.apache.flex.B}\n */\norg.apache.flex.B.prototype.clone = function() {\n  return new org.apache.flex.B();\n};");
    }

    @Override
    protected void addLibraries(List<File> libraries)
    {
        libraries.addAll(testAdapter.getLibraries(true));
    }
    
    @Override
    protected IClassNode getClassNode(String code)
    {
        String source = "package org.apache.flex {import flash.events.IEventDispatcher;import mx.logging.ILogger;import spark.components.Button;"
                + code + "}";
        IFileNode node = compileAS(source);
        IClassNode child = (IClassNode) findFirstDescendantOfType(node,
                IClassNode.class);
        return child;
    }


    protected IBackend createBackend()
    {
        return new VF2JSBackend();
    }

}
