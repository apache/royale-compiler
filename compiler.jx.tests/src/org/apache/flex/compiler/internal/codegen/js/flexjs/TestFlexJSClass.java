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
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogClass;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSClass extends TestGoogClass
{

    @Test
    public void testConstructor_withArgumentNameMatchingMemberName()
    {
        IClassNode node = getClassNode("public class B {public function B(arg1:String) {this.arg1 = arg1}; public var arg1:String;}");
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n * @param {string} arg1\n */\norg.apache.flex.B = function(arg1) {\n\tvar self = this;\n\tself.arg1 = arg1;\n};\n\n/**\n * @type {string}\n */\norg.apache.flex.B.prototype.arg1;";
        assertOut(expected);
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
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};\n\norg.apache.flex.A.prototype.get_foo1 = function() {\n\tvar self = this;\n\treturn null;\n};\n\norg.apache.flex.A.prototype.set_foo1 = function(value) {\n};\n\norg.apache.flex.A.prototype.get_foo2 = function() {\n\tvar self = this;\n\treturn null;\n};\n\norg.apache.flex.A.prototype.set_foo2 = function(value) {\n};\n\norg.apache.flex.A.prototype.get_foo3 = function() {\n\tvar self = this;\n\treturn null;\n};\n\norg.apache.flex.A.prototype.set_foo3 = function(value) {\n};\n\norg.apache.flex.A.prototype.get_foo5 = function() {\n\tvar self = this;\n\treturn null;\n};\n\norg.apache.flex.A.prototype.set_foo5 = function(value) {\n};\n\norg.apache.flex.A.prototype.get_foo6 = function() {\n\tvar self = this;\n\treturn null;\n};\n\norg.apache.flex.A.prototype.set_foo6 = function(value) {\n};");
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
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};\n\n/**\n * @return {Object}\n */\norg.apache.flex.A.prototype.foo1 = function() {\n\tvar self = this;\n\treturn null;\n};\n\n/**\n * @return {Object}\n */\norg.apache.flex.A.prototype.foo1a = function() {\n\tvar self = this;\n\treturn null;\n};\n\n/**\n * @return {Object}\n * @override\n */\norg.apache.flex.A.prototype.foo1b = function() {\n\tvar self = this;\n\treturn goog.base(this, 'foo1b');\n};\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.prototype.foo2 = function(value) {\n};\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.prototype.foo3 = function(value) {\n};\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.prototype.foo5 = function(value) {\n};\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.prototype.foo6 = function(value) {\n};\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.foo7 = function(value) {\n};\n\n/**\n * @param {Object} value\n */\norg.apache.flex.A.foo7 = function(value) {\n};");
    }

    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
