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
import org.apache.flex.compiler.internal.as.codegen.TestClass;
import org.apache.flex.compiler.internal.js.driver.goog.GoogBackend;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.junit.Ignore;
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
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};");
    }

	@Override
    @Test
    public void testSimpleInternal()
    {
		// TODO (erikdebruin) is there a 'goog' equivalent for the 
		//                    'internal' namespace?
        IClassNode node = getClassNode("internal class A{}");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};");
    }

	@Override
    @Test
    public void testSimpleFinal()
    {
		// TODO (erikdebruin) is there a 'goog' equivalent for the 
		//                    'final' keyword?
        IClassNode node = getClassNode("public final class A{}");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};");
    }

	@Override
    @Test
    public void testSimpleDynamic()
    {
		// TODO (erikdebruin) is there a 'goog' equivalent for the 
		//                    'dynamic' keyword?
        IClassNode node = getClassNode("public dynamic class A{}");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};");
    }

	@Override
    @Test
    public void testSimpleExtends()
    {
		// TODO (erikdebruin) why do we need to put use an 'actual' component 
		//                    (e.g. spark.components.Button) here if we want to 
		//                    trigger the '@extends' notation?
        IClassNode node = getClassNode("public class A extends Button {public function A() {}}");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n */\norg.apache.flex.A = function() {\n\tgoog.base(this);\n}\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

	@Override
    @Test
    public void testSimpleImplements()
    {
        IClassNode node = getClassNode("public class A implements IEventDispatcher {public function A() {}}");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {flash.events.IEventDispatcher}\n */\norg.apache.flex.A = function() {\n};");
    }

	@Override
    @Test
    public void testSimpleImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A implements IEventDispatcher, ILogger {public function A() {}}");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {flash.events.IEventDispatcher}\n * @implements {mx.logging.ILogger}\n */\norg.apache.flex.A = function() {\n};");
    }

	@Override
    @Test
    public void testSimpleExtendsImplements()
    {
        IClassNode node = getClassNode("public class A extends Button implements IEventDispatcher {public function A() {}}");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @implements {flash.events.IEventDispatcher}\n */\norg.apache.flex.A = function() {\n\tgoog.base(this);\n}\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

	@Override
    @Test
    public void testSimpleExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A extends Button implements IEventDispatcher, ILogger {public function A() {}}");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @implements {flash.events.IEventDispatcher}\n * @implements {mx.logging.ILogger}\n */\norg.apache.flex.A = function() {\n\tgoog.base(this);\n}\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

	@Override
    @Test
    public void testSimpleFinalExtendsImplementsMultiple()
    {
		// TODO (erikdebruin) 'final' keyword: see 'testSimpleFinal' above
        IClassNode node = getClassNode("public final class A extends Button implements IEventDispatcher, ILogger {public function A() {}}");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @implements {flash.events.IEventDispatcher}\n * @implements {mx.logging.ILogger}\n */\norg.apache.flex.A = function() {\n\tgoog.base(this);\n}\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

	@Override
    @Test
    public void testQualifiedExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A extends spark.components.Button implements flash.events.IEventDispatcher, mx.logging.ILogger {public function A() {}}");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {spark.components.Button}\n * @implements {flash.events.IEventDispatcher}\n * @implements {mx.logging.ILogger}\n */\norg.apache.flex.A = function() {\n\tgoog.base(this);\n}\ngoog.inherits(org.apache.flex.A, spark.components.Button);");
    }

	@Override
    @Test
    public void testConstructor()
    {
		// TODO (erikdebruin) replace 'super' call with 'goog.base()'... Can you
		//                    call 'super' if the class doesn't extend any other?
        IClassNode node = getClassNode("public class A {public function A() {super('foo', 42);}}");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n\tgoog.base(this, 'foo', 42);\n};");
    }
    
	@Override
    @Test
    public void testFields()
    {
        IClassNode node = getClassNode("public class A {public var a:Object;protected var b:String; "
                + "private var c:int; internal var d:uint; var e:Number}");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};\n\n/**\n * @type {Object}\n */\norg.apache.flex.A.prototype.a;\n\n/**\n * @protected\n * @type {string}\n */\norg.apache.flex.A.prototype.b;\n\n/**\n * @private\n * @type {number}\n */\norg.apache.flex.A.prototype.c;\n\n/**\n * @type {number}\n */\norg.apache.flex.A.prototype.d;\n\n/**\n * @type {number}\n */\norg.apache.flex.A.prototype.e;");
    }

	@Override
    @Test
    public void testConstants()
    {
        IClassNode node = getClassNode("public class A {" +
        		"public static const A:int = 42;" +
        		"protected static const B:Number = 42;" +
                "private static const C:Number = 42;" +
                "foo_bar static const C:String = 'me' + 'you';");
        visitor.visitClass(node);
        assertOut("/**\n * @constructor\n */\norg.apache.flex.A = function() {\n};\n\n/**\n * @const\n * @type {number}\n */\norg.apache.flex.A.A = 42;\n\n/**\n * @protected\n * @const\n * @type {number}\n */\norg.apache.flex.A.B = 42;\n\n/**\n * @private\n * @const\n * @type {number}\n */\norg.apache.flex.A.C = 42;\n\n/**\n * @const\n * @type {string}\n */\norg.apache.flex.A.C = 'me' + 'you';");
    }
    
	@Ignore
	@Override
    @Test
    public void testAccessors()
    {
		// TODO (erikdebruin) fix accessor handling first
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
        visitor.visitClass(node);
        assertOut("");
    }

	@Ignore
	@Override
    @Test
    public void testMethods()
    {
		// TODO (erikdebruin) 1) handle namespaces (private, protected, custom)
		//                    2) handle 'super' calls
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
        visitor.visitClass(node);
        assertOut("");
    }

	@Override
    protected IClassNode getClassNode(String code)
    {
        String source = "package org.apache.flex {import flash.events.IEventDispatcher;import mx.logging.ILogger;import spark.components.Button;" + code + "}";
        IFileNode node = getFileNode(source);
        IClassNode child = (IClassNode) findFirstDescendantOfType(node, IClassNode.class);
        return child;
    }
    
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }

}
