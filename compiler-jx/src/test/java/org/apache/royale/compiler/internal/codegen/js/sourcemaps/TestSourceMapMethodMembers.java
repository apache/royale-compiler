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
package org.apache.royale.compiler.internal.codegen.js.sourcemaps;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.test.SourceMapTestBase;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.junit.Test;

public class TestSourceMapMethodMembers extends SourceMapTestBase
{
    @Override
    public void setUp()
    {
        super.setUp();

        project.setAllowPrivateNameConflicts(true);
    }

    @Test
    public void testMethod()
    {
        IFunctionNode node = getMethod("function foo(){}");
        asBlockWalker.visitFunction(node);
        //RoyaleTest_A.prototype.foo = function() {\n}
        assertMapping(node, 0, 9, 0, 0, 0, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 9, 0, 23, 0, 26); // foo
        assertMapping(node, 0, 0, 0, 26, 0, 37); // = function
        assertMapping(node, 0, 12, 0, 37, 0, 38); // (
        assertMapping(node, 0, 13, 0, 38, 0, 39); // )
        assertMapping(node, 0, 14, 0, 40, 0, 41); // {
        assertMapping(node, 0, 15, 1, 0, 1, 1);   // }
    }

    @Test
    public void testMethod_withReturnType()
    {
        IFunctionNode node = getMethod("function foo():int{  return -1;}");
        asBlockWalker.visitFunction(node);
        ///**\n @return {number}\n */\nRoyaleTest_A.prototype.foo = function() {\n  return -1;}
        assertMapping(node, 0, 9, 3, 0, 3, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 9, 3, 23, 3, 26); // foo
        assertMapping(node, 0, 0, 3, 26, 3, 37); // = function
        assertMapping(node, 0, 12, 3, 37, 3, 38); // (
        assertMapping(node, 0, 13, 3, 38, 3, 39); // )
        assertMapping(node, 0, 18, 3, 40, 3, 41); // {
        assertMapping(node, 0, 21, 4, 2, 4, 9); // return
        assertMapping(node, 0, 28, 4, 9, 4, 11); // -1
        assertMapping(node, 0, 30, 4, 11, 4, 12); // ;
        assertMapping(node, 0, 31, 5, 0, 5, 1);   // }
    }

    @Test
    public void testMethod_withDefaultParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String = \"baz\"):int{  return -1;}");
        asBlockWalker.visitFunction(node);
        ///**\n @param {string=} bar\n * @return {number}\n */\nRoyaleTest_A.prototype.foo = function(bar) {\n  bar = typeof bar !== 'undefined' ? bar : "baz";\nreturn -1;}
        assertMapping(node, 0, 9, 4, 0, 4, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 9, 4, 23, 4, 26); // foo
        assertMapping(node, 0, 0, 4, 26, 4, 37); // = function
        assertMapping(node, 0, 12, 4, 37, 4, 38); // (
        assertMapping(node, 0, 13, 4, 38, 4, 41); // bar
        assertMapping(node, 0, 31, 4, 41, 4, 42); // )
        assertMapping(node, 0, 36, 4, 43, 4, 44); // {
        assertMapping(node, 0, 13, 5, 2, 5, 43); // bar = typeof bar !== 'undefined' ? bar : 
        assertMapping(node, 0, 26, 5, 43, 5, 48); // "baz";
        assertMapping(node, 0, 13, 5, 48, 5, 49); // ;
        assertMapping(node, 0, 39, 6, 2, 6, 9); // return
        assertMapping(node, 0, 46, 6, 9, 6, 11); // -1
        assertMapping(node, 0, 48, 6, 11, 6, 12); // ;
        assertMapping(node, 0, 49, 7, 0, 7, 1);   // }
    }

    @Test
    public void testMethod_withPrivate()
    {
        IFunctionNode node = getMethod("private function foo(){}");
        asBlockWalker.visitFunction(node);
        ///**\n * @private\n */\nRoyaleTest_A.prototype.RoyaleTest_A_foo = function() {\n}
        assertMapping(node, 0, 17, 3, 0, 3, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 17, 3, 23, 3, 39, "foo"); // RoyaleTest_A_foo
        assertMapping(node, 0, 0, 3, 39, 3, 50); // = function
        assertMapping(node, 0, 20, 3, 50, 3, 51); // (
        assertMapping(node, 0, 21, 3, 51, 3, 52); // )
        assertMapping(node, 0, 22, 3, 53, 3, 54); // {
        assertMapping(node, 0, 23, 4, 0, 4, 1);   // }
    }

    @Test
    public void testMethod_withCustomNamespace()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.custom_namespace;use namespace custom_namespace;public class RoyaleTest_A {custom_namespace function foo(){}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        ///**\n */\nRoyaleTest_A.prototype.http_$$ns_apache_org$2017$custom$namespace__foo = function() {\n}
        assertMapping(node, 0, 26, 2, 0, 2, 22);  // RoyaleTest_A.prototype
        assertMapping(node, 0, 26, 2, 22, 2, 23);  // .
        assertMapping(node, 0, 26, 2, 23, 2, 70, "foo"); // http_$$ns_apache_org$2017$custom$namespace__foo
        assertMapping(node, 0, 0, 2, 70, 2, 81); // = function
        assertMapping(node, 0, 29, 2, 81, 2, 82); // (
        assertMapping(node, 0, 30, 2, 82, 2, 83); // )
        assertMapping(node, 0, 31, 2, 84, 2, 85); // {
        assertMapping(node, 0, 32, 3, 0, 3, 1);   // }
    }

    @Test
    public void testStaticMethod()
    {
        IFunctionNode node = getMethod("static function foo(){}");
        asBlockWalker.visitFunction(node);
        //RoyaleTest_A.foo = function() {\n}
        assertMapping(node, 0, 16, 0, 0, 0, 13);  // RoyaleTest_A.
        assertMapping(node, 0, 16, 0, 13, 0, 16); // foo
        assertMapping(node, 0, 0, 0, 16, 0, 27); // = function
        assertMapping(node, 0, 19, 0, 27, 0, 28); // (
        assertMapping(node, 0, 20, 0, 28, 0, 29); // )
        assertMapping(node, 0, 21, 0, 30, 0, 31); // {
        assertMapping(node, 0, 22, 1, 0, 1, 1);   // }
    }

    @Test
    public void testStaticMethod_withReturnType()
    {
        IFunctionNode node = getMethod("static function foo():int{  return -1;}");
        asBlockWalker.visitFunction(node);
        ///**\n @return {number}\n */\nRoyaleTest_A.foo = function() {\n  return -1;}
        assertMapping(node, 0, 16, 3, 0, 3, 13);  // RoyaleTest_A.
        assertMapping(node, 0, 16, 3, 13, 3, 16); // foo
        assertMapping(node, 0, 0, 3, 16, 3, 27); // = function
        assertMapping(node, 0, 19, 3, 27, 3, 28); // (
        assertMapping(node, 0, 20, 3, 28, 3, 29); // )
        assertMapping(node, 0, 25, 3, 30, 3, 31); // {
        assertMapping(node, 0, 28, 4, 2, 4, 9); // return
        assertMapping(node, 0, 35, 4, 9, 4, 11); // -1
        assertMapping(node, 0, 37, 4, 11, 4, 12); // ;
        assertMapping(node, 0, 38, 5, 0, 5, 1);   // }
    }

    @Test
    public void testStaticMethod_withDefaultParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("static function foo(bar:String = \"baz\"):int{  return -1;}");
        asBlockWalker.visitFunction(node);
        ///**\n @param {string=} bar\n * @return {number}\n */\nRoyaleTest_A.foo = function(bar) {\n  bar = typeof bar !== 'undefined' ? bar : "baz";\nreturn -1;}
        assertMapping(node, 0, 16, 4, 0, 4, 13);  // RoyaleTest_A.
        assertMapping(node, 0, 16, 4, 13, 4, 16); // foo
        assertMapping(node, 0, 0, 4, 16, 4, 27); // = function
        assertMapping(node, 0, 19, 4, 27, 4, 28); // (
        assertMapping(node, 0, 20, 4, 28, 4, 31); // bar
        assertMapping(node, 0, 38, 4, 31, 4, 32); // )
        assertMapping(node, 0, 43, 4, 33, 4, 34); // {
        assertMapping(node, 0, 20, 5, 2, 5, 43); // bar = typeof bar !== 'undefined' ? bar : 
        assertMapping(node, 0, 33, 5, 43, 5, 48); // "baz";
        assertMapping(node, 0, 20, 5, 48, 5, 49); // ;
        assertMapping(node, 0, 46, 6, 2, 6, 9); // return
        assertMapping(node, 0, 53, 6, 9, 6, 11); // -1
        assertMapping(node, 0, 55, 6, 11, 6, 12); // ;
        assertMapping(node, 0, 56, 7, 0, 7, 1);   // }
    }

    @Test
    public void testStaticMethod_withPrivate()
    {
        IFunctionNode node = getMethod("private static function foo(){}");
        asBlockWalker.visitFunction(node);
        ///**\n * @private\n */\nRoyaleTest_A.foo = function() {\n}
        assertMapping(node, 0, 24, 3, 0, 3, 13);  // RoyaleTest_A.
        assertMapping(node, 0, 24, 3, 13, 3, 16); // foo
        assertMapping(node, 0, 0, 3, 16, 3, 27); // = function
        assertMapping(node, 0, 27, 3, 27, 3, 28); // (
        assertMapping(node, 0, 28, 3, 28, 3, 29); // )
        assertMapping(node, 0, 29, 3, 30, 3, 31); // {
        assertMapping(node, 0, 30, 4, 0, 4, 1);   // }
    }

    @Test
    public void testStaticMethod_withCustomNamespace()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.custom_namespace;use namespace custom_namespace;public class RoyaleTest_A {custom_namespace static function foo(){}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        ///**\n */\nRoyaleTest_A.http_$$ns_apache_org$2017$custom$namespace__foo = function() {\n}
        assertMapping(node, 0, 33, 2, 0, 2, 12);  // RoyaleTest_A.
        assertMapping(node, 0, 33, 2, 12, 2, 13);  // .
        assertMapping(node, 0, 33, 2, 13, 2, 60, "foo"); // http_$$ns_apache_org$2017$custom$namespace__foo
        assertMapping(node, 0, 0, 2, 60, 2, 71); // = function
        assertMapping(node, 0, 36, 2, 71, 2, 72); // (
        assertMapping(node, 0, 37, 2, 72, 2, 73); // )
        assertMapping(node, 0, 38, 2, 74, 2, 75); // {
        assertMapping(node, 0, 39, 3, 0, 3, 1);   // }
    }

    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }
}
