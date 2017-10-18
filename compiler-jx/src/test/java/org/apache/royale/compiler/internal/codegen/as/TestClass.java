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

package org.apache.royale.compiler.internal.codegen.as;

import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.junit.Test;

/**
 * This class tests the production of valid ActionScript3 code for Class
 * production.
 * 
 * @author Michael Schmalle
 */
public class TestClass extends ASTestBase
{
    //--------------------------------------------------------------------------
    // Class
    //--------------------------------------------------------------------------

    @Test
    public void testSimple()
    {
        IClassNode node = getClassNode("public class A{}");
        asBlockWalker.visitClass(node);
        assertOut("public class A {\n}");
    }

    @Test
    public void testSimpleInternal()
    {
        IClassNode node = getClassNode("internal class A{}");
        asBlockWalker.visitClass(node);
        assertOut("internal class A {\n}");
    }

    @Test
    public void testSimpleFinal()
    {
        IClassNode node = getClassNode("public final class A{}");
        asBlockWalker.visitClass(node);
        assertOut("public final class A {\n}");
    }

    @Test
    public void testSimpleDynamic()
    {
        IClassNode node = getClassNode("public dynamic class A{}");
        asBlockWalker.visitClass(node);
        assertOut("public dynamic class A {\n}");
    }

    @Test
    public void testSimpleExtends()
    {
        IClassNode node = getClassNode("public class A extends B {}");
        asBlockWalker.visitClass(node);
        assertOut("public class A extends B {\n}");
    }

    @Test
    public void testSimpleImplements()
    {
        IClassNode node = getClassNode("public class A implements IA {}");
        asBlockWalker.visitClass(node);
        assertOut("public class A implements IA {\n}");
    }

    @Test
    public void testSimpleImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A implements IA, IB, IC {}");
        asBlockWalker.visitClass(node);
        assertOut("public class A implements IA, IB, IC {\n}");
    }

    @Test
    public void testSimpleExtendsImplements()
    {
        IClassNode node = getClassNode("public class A extends B implements IA {}");
        asBlockWalker.visitClass(node);
        assertOut("public class A extends B implements IA {\n}");
    }

    @Test
    public void testSimpleExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A extends B implements IA, IB, IC {}");
        asBlockWalker.visitClass(node);
        assertOut("public class A extends B implements IA, IB, IC {\n}");
    }

    @Test
    public void testSimpleFinalExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public final class A extends B implements IA, IB, IC {}");
        asBlockWalker.visitClass(node);
        assertOut("public final class A extends B implements IA, IB, IC {\n}");
    }

    @Test
    public void testQualifiedExtendsImplementsMultiple()
    {
        IClassNode node = getClassNode("public class A extends goo.B implements foo.bar.IA, goo.foo.IB, baz.boo.IC {}");
        asBlockWalker.visitClass(node);
        assertOut("public class A extends goo.B implements foo.bar.IA, goo.foo.IB, baz.boo.IC {\n}");
    }

    @Test
    public void testConstructor()
    {
        IClassNode node = getClassNode("public class A {public function A(){super('foo', 42);}}");
        asBlockWalker.visitClass(node);
        assertOut("public class A {\n\tpublic function A() {\n\t\tsuper('foo', 42);\n\t}\n}");
    }

    @Test
    public void testConstructor_withArguments()
    {
        IClassNode node = getClassNode("public class A {public function A(arg1:String, arg2:int) {}}");
        asBlockWalker.visitClass(node);
        assertOut("public class A {\n\tpublic function A(arg1:String, arg2:int) {\n\t}\n}");
    }

    @Test
    public void testExtendsConstructor_withArguments()
    {
        IClassNode node = getClassNode("public class A extends B {public function A(arg1:String, arg2:int) {}}");
        asBlockWalker.visitClass(node);
        assertOut("public class A extends B {\n\tpublic function A(arg1:String, arg2:int) {\n\t}\n}");
    }

    @Test
    public void testFields()
    {
        IClassNode node = getClassNode("public class A {public var a:Object;protected var b:String; "
                + "private var c:int; internal var d:uint; var e:Number}");
        asBlockWalker.visitClass(node);
        assertOut("public class A {\n\tpublic var a:Object;\n\tprotected var b:String;"
                + "\n\tprivate var c:int;\n\tvar d:uint;\n\tvar e:Number;\n}");
    }

    @Test
    public void testConstants()
    {
        IClassNode node = getClassNode("public class A {"
                + "public static const A:int = 42;"
                + "protected static const B:Number = 42;"
                + "private static const C:Number = 42;"
                + "foo_bar static const C:String = 'me' + 'you';}");
        asBlockWalker.visitClass(node);
        assertOut("public class A {\n\tpublic static const A:int = 42;\n\t"
                + "protected static const B:Number = 42;\n\tprivate static const "
                + "C:Number = 42;\n\tfoo_bar static const C:String = 'me' + 'you';\n}");
    }

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
        assertOut("public class A {\n\tpublic function get foo1():Object {"
                + "\n\t\treturn null;\n\t}\n\tpublic function set foo1(value:Object)"
                + ":void {\n\t}\n\tprotected function get foo2():Object {\n\t\treturn "
                + "null;\n\t}\n\tprotected function set foo2(value:Object):void "
                + "{\n\t}\n\tprivate function get foo3():Object {\n\t\treturn null;"
                + "\n\t}\n\tprivate function set foo3(value:Object):void {\n\t}\n\t"
                + "function get foo5():Object {\n\t\treturn null;\n\t}\n\tfunction set "
                + "foo5(value:Object):void {\n\t}\n\tfoo_bar function get foo6():Object "
                + "{\n\t\treturn null;\n\t}\n\tfoo_bar function set "
                + "foo6(value:Object):void {\n\t}\n}");
    }

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
        assertOut("public class A {\n\tpublic function foo1():Object {\n\t\treturn "
                + "null;\n\t}\n\tpublic final function foo1a():Object {\n\t\treturn "
                + "null;\n\t}\n\tpublic override function foo1b():Object {\n\t\treturn "
                + "super.foo1b();\n\t}\n\tprotected function foo2(value:Object):void "
                + "{\n\t}\n\tprivate function foo3(value:Object):void {\n\t}\n\tfunction "
                + "foo5(value:Object):void {\n\t}\n\tfoo_bar function foo6(value:Object"
                + "):void {\n\t}\n\tpublic static function foo7(value:Object):void {\n\t}"
                + "\n\tfoo_bar static function foo7(value:Object):void {\n\t}\n}");
    }

    protected IClassNode getClassNode(String code)
    {
        String source = "package {" + code + "}";
        IFileNode node = compileAS(source);
        IClassNode child = (IClassNode) findFirstDescendantOfType(node,
                IClassNode.class);
        return child;
    }
}
