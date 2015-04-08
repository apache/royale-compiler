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

package org.apache.flex.compiler.internal.codegen.js.goog;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.as.TestClass;
import org.apache.flex.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.junit.Test;

/**
 * This class tests the production of 'goog' JS code for Classes.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class TestGoogClass extends TestClass
{
    @Override
    @Test
    public void testSimple()
    {
        IClassNode node = getClassNode("public class A{}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};");
    }

    @Override
    @Test
    public void testSimpleInternal()
    {
        // (erikdebruin) the AS compiler will enforce 'internal' namespace, 
        //               in JS we ignore it
        IClassNode node = getClassNode("internal class A{}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};");
    }

    @Override
    @Test
    public void testSimpleFinal()
    {
        // (erikdebruin) the AS compiler will enforce the 'final' keyword, 
        //               in JS we ignore it
        IClassNode node = getClassNode("public final class A{}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};");
    }

    @Override
    @Test
    public void testSimpleDynamic()
    {
        // (erikdebruin) all JS objects are 'dynamic' by design
        IClassNode node = getClassNode("public dynamic class A{}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};");
    }

    @Override
    @Test
    public void testSimpleExtends()
    {
        IClassNode node = getClassNode("public class A extends Button {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n */\norg.apache.flex.A = function() {\n\torg.apache.flex.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testSimpleImplements()
    {
        IClassNode node = getClassNode("public class A implements IEventDispatcher {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {flash.events.IEventDispatcher}\n */\norg.apache.flex.A = function() {\n};");
    }

    @Override
    @Test
    public void testSimpleImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A implements IEventDispatcher, ILogger {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {flash.events.IEventDispatcher}\n * @implements {mx.logging.ILogger}\n */\norg.apache.flex.A = function() {\n};");
    }

    @Override
    @Test
    public void testSimpleExtendsImplements()
    {
        IClassNode node = getClassNode("public class A extends Button implements IEventDispatcher {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @implements {flash.events.IEventDispatcher}\n */\norg.apache.flex.A = function() {\n\torg.apache.flex.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testSimpleExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A extends Button implements IEventDispatcher, ILogger {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @implements {flash.events.IEventDispatcher}\n * @implements {mx.logging.ILogger}\n */\norg.apache.flex.A = function() {\n\torg.apache.flex.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testSimpleFinalExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public final class A extends Button implements IEventDispatcher, ILogger {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @implements {flash.events.IEventDispatcher}\n * @implements {mx.logging.ILogger}\n */\norg.apache.flex.A = function() {\n\torg.apache.flex.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testQualifiedExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A extends spark.components.Button implements flash.events.IEventDispatcher, mx.logging.ILogger {public function A() {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @implements {flash.events.IEventDispatcher}\n * @implements {mx.logging.ILogger}\n */\norg.apache.flex.A = function() {\n\torg.apache.flex.A.base(this, 'constructor');\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testConstructor()
    {
        IClassNode node = getClassNode("public class A {public function A() { }}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};");
    }

    @Test
    public void testConstructor_super()
    {
        IClassNode node = getClassNode("public class A {public function A() { super(); }}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n\tvar self = this;\n\t;\n};");
    }

    @Test
    public void testExtendsConstructor_super()
    {
        IClassNode node = getClassNode("public class A extends spark.components.Button { public function A() { super('foo', 42);}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n */\norg.apache.flex.A = function() {\n\tvar self = this;\n\torg.apache.flex.A.base(this, 'constructor', 'foo', 42);\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testConstructor_withArguments()
    {
        IClassNode node = getClassNode("public class A {public function A(arg1:String, arg2:int) {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @param {string} arg1\n * @param {number} arg2\n */\norg.apache.flex.A = function(arg1, arg2) {\n};");
    }

    @Override
    @Test
    public void testExtendsConstructor_withArguments()
    {
        IClassNode node = getClassNode("public class A extends spark.components.Button {public function A(arg1:String, arg2:int) {}}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @param {string} arg1\n * @param {number} arg2\n */\norg.apache.flex.A = function(arg1, arg2) {\n\torg.apache.flex.A.base(this, 'constructor', arg1, arg2);\n};\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

    @Override
    @Test
    public void testFields()
    {
        IClassNode node = getClassNode("public class A {public var a:Object;protected var b:String; "
                + "private var c:int; internal var d:uint; var e:Number}");
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};\n\n/**\n * @type {Object}\n */\norg.apache.flex.A.prototype.a;\n\n/**\n * @protected\n * @type {string}\n */\norg.apache.flex.A.prototype.b;\n\n/**\n * @private\n * @type {number}\n */\norg.apache.flex.A.prototype.c;\n\n/**\n * @type {number}\n */\norg.apache.flex.A.prototype.d;\n\n/**\n * @type {number}\n */\norg.apache.flex.A.prototype.e;");
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
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};\n\n/**\n * @const\n * @type {number}\n */\norg.apache.flex.A.A = 42;\n\n/**\n * @protected\n * @const\n * @type {number}\n */\norg.apache.flex.A.B = 42;\n\n/**\n * @private\n * @const\n * @type {number}\n */\norg.apache.flex.A.C = 42;\n\n/**\n * @const\n * @type {string}\n */\norg.apache.flex.A.C = 'me' + 'you';");
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
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};\n\n/**\n * @type {Object}\n */\norg.apache.flex.A.prototype.foo1;\n\nObject.defineProperty(\n\torg.apache.flex.A.prototype, \n\t'foo1', \n\t{get:function() {\n\t\tvar self = this;\n\t\treturn null;\n\t}, configurable:true}\n);\n\nObject.defineProperty(\n\torg.apache.flex.A.prototype, \n\t'foo1', \n\t{set:function(value) {\n\t}, configurable:true}\n);\n\n/**\n * @protected\n * @type {Object}\n */\norg.apache.flex.A.prototype.foo2;\n\nObject.defineProperty(\n\torg.apache.flex.A.prototype, \n\t'foo2', \n\t{get:function() {\n\t\tvar self = this;\n\t\treturn null;\n\t}, configurable:true}\n);\n\nObject.defineProperty(\n\torg.apache.flex.A.prototype, \n\t'foo2', \n\t{set:function(value) {\n\t}, configurable:true}\n);\n\n/**\n * @private\n * @type {Object}\n */\norg.apache.flex.A.prototype.foo3;\n\nObject.defineProperty(\n\torg.apache.flex.A.prototype, \n\t'foo3', \n\t{get:function() {\n\t\tvar self = this;\n\t\treturn null;\n\t}, configurable:true}\n);\n\nObject.defineProperty(\n\torg.apache.flex.A.prototype, \n\t'foo3', \n\t{set:function(value) {\n\t}, configurable:true}\n);\n\n/**\n * @type {Object}\n */\norg.apache.flex.A.prototype.foo5;\n\nObject.defineProperty(\n\torg.apache.flex.A.prototype, \n\t'foo5', \n\t{get:function() {\n\t\tvar self = this;\n\t\treturn null;\n\t}, configurable:true}\n);\n\nObject.defineProperty(\n\torg.apache.flex.A.prototype, \n\t'foo5', \n\t{set:function(value) {\n\t}, configurable:true}\n);\n\n/**\n * @type {Object}\n */\norg.apache.flex.A.prototype.foo6;\n\nObject.defineProperty(\n\torg.apache.flex.A.prototype, \n\t'foo6', \n\t{get:function() {\n\t\tvar self = this;\n\t\treturn null;\n\t}, configurable:true}\n);\n\nObject.defineProperty(\n\torg.apache.flex.A.prototype, \n\t'foo6', \n\t{set:function(value) {\n\t}, configurable:true}\n);");
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
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};\n\n/**\n * @return {Object}\n */\norg.apache.flex.A.prototype.foo1 = function() {\n\tvar self = this;\n\treturn null;\n};\n\n/**\n * @return {Object}\n */\norg.apache.flex.A.prototype.foo1a = function() {\n\tvar self = this;\n\treturn null;\n};\n\n/**\n * @return {Object}\n * @override\n */\norg.apache.flex.A.prototype.foo1b = function() {\n\tvar self = this;\n\treturn org.apache.flex.A.base(this, 'foo1b');\n};\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.prototype.foo2 = function(value) {\n};\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.prototype.foo3 = function(value) {\n};\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.prototype.foo5 = function(value) {\n};\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.prototype.foo6 = function(value) {\n};\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.foo7 = function(value) {\n};\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.foo7 = function(value) {\n};");
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
        return new GoogBackend();
    }

}
