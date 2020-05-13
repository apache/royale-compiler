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
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IIfNode;
import org.apache.royale.compiler.tree.as.ISwitchNode;
import org.apache.royale.compiler.tree.as.ITryNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.tree.as.IWithNode;

import org.junit.Test;

public class TestSourceMapStatements extends SourceMapTestBase
{
    //----------------------------------
    // var declaration
    //----------------------------------

    @Test
    public void testVarDeclaration()
    {
        IVariableNode node = (IVariableNode) getNode("var a;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        //var /** @type {*} */ a
        assertMapping(node, 0, 0, 0, 0, 0, 4);   // var
        assertMapping(node, 0, 4, 0, 21, 0, 22); // a
        assertMapping(node, 0, 5, 0, 4, 0, 21);  // (type)
    }

    @Test
    public void testVarDeclaration_withAssignedValue()
    {
        IVariableNode node = (IVariableNode) getNode("var a = 42;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        //var /** @type {*} */ a = 42
        assertMapping(node, 0, 0, 0, 0, 0, 4);   // var
        assertMapping(node, 0, 4, 0, 21, 0, 22); // a
        assertMapping(node, 0, 5, 0, 4, 0, 21);  // (type)
        assertMapping(node, 0, 5, 0, 22, 0, 25); // =
        assertMapping(node, 0, 8, 0, 25, 0, 27); // 42
    }

    @Test
    public void testVarDeclaration_withType()
    {
        IVariableNode node = (IVariableNode) getNode("var a:Number;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        //var /** @type {number} */ a
        assertMapping(node, 0, 0, 0, 0, 0, 4);   // var
        assertMapping(node, 0, 4, 0, 26, 0, 27); // a
        assertMapping(node, 0, 5, 0, 4, 0, 26);  // :Number
    }

    @Test
    public void testVarDeclaration_withTypeAssignedValue()
    {
        IVariableNode node = (IVariableNode) getNode("var a:int = 42;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        //var /** @type {number} */ a = 42
        assertMapping(node, 0, 0, 0, 0, 0, 4);    // var
        assertMapping(node, 0, 4, 0, 26, 0, 27);  // a
        assertMapping(node, 0, 5, 0, 4, 0, 26);   // :int
        assertMapping(node, 0, 9, 0, 27, 0, 30);  // =
        assertMapping(node, 0, 12, 0, 30, 0, 32); // 42
    }

    @Test
    public void testVarDeclaration_withTypeAssignedValueComplex()
    {
        IVariableNode node = (IVariableNode) getNode(
                "class A { public function b():void { var a:Foo = new Foo(42, 'goo');}} class Foo {}", IVariableNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitVariable(node);
        //var /** @type {Foo} */ a = new Foo(42, 'goo')
        assertMapping(node, 0, 0, 0, 0, 0, 4);    // var
        assertMapping(node, 0, 4, 0, 23, 0, 24);  // a
        assertMapping(node, 0, 5, 0, 4, 0, 23);   // :Foo
        assertMapping(node, 0, 9, 0, 24, 0, 27);  // =
        assertMapping(node, 0, 12, 0, 27, 0, 31);  // new
        assertMapping(node, 0, 16, 0, 31, 0, 34);  // Foo
        assertMapping(node, 0, 19, 0, 34, 0, 35);  // (
        assertMapping(node, 0, 20, 0, 35, 0, 37);  // 42
        assertMapping(node, 0, 22, 0, 37, 0, 39);  // ,
        assertMapping(node, 0, 24, 0, 39, 0, 44);  // 'goo'
        assertMapping(node, 0, 29, 0, 44, 0, 45);  // )
    }

    @Test
    public void testVarDeclaration_withList()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a:int = 4, b:int = 11, c:int = 42;", IVariableNode.class);
        asBlockWalker.visitVariable(node);
        //var /** @type {number} */ a = 4, /** @type {number} */ b = 11, /** @type {number} */ c = 42
        assertMapping(node, 0, 0, 0, 0, 0, 4);    // var
        assertMapping(node, 0, 4, 0, 26, 0, 27);  // a
        assertMapping(node, 0, 5, 0, 4, 0, 26);   // :int
        assertMapping(node, 0, 9, 0, 27, 0, 30);  // =
        assertMapping(node, 0, 12, 0, 30, 0, 31); // 4
        assertMapping(node, 0, 13, 0, 31, 0, 33); // ,
        assertMapping(node, 0, 15, 0, 55, 0, 56); // b
        assertMapping(node, 0, 16, 0, 33, 0, 55); // :int
        assertMapping(node, 0, 20, 0, 56, 0, 59); // =
        assertMapping(node, 0, 23, 0, 59, 0, 61); // 11
        assertMapping(node, 0, 25, 0, 61, 0, 63); // ,
        assertMapping(node, 0, 27, 0, 85, 0, 86); // c
        assertMapping(node, 0, 28, 0, 63, 0, 85); // :int
        assertMapping(node, 0, 32, 0, 86, 0, 89); // =
        assertMapping(node, 0, 35, 0, 89, 0, 91); // 42
    }

    //----------------------------------
    // for () { }
    //----------------------------------

    @Test
    public void testVisitFor_1a()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "var len:int;for (var i:int = 0; i < len; i++) { break; }",
                IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        //for (var /** @type {number} */ i = 0; i < len; i++) {\n  break;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 5);    // for (
        assertMapping(node, 0, 18, 0, 36, 0, 38); // ;
        assertMapping(node, 0, 27, 0, 45, 0, 47); // ;
        assertMapping(node, 0, 32, 0, 50, 0, 52); // )
        assertMapping(node, 0, 34, 0, 52, 0, 53); // {
        assertMapping(node, 0, 43, 2, 0, 2, 1);   // }
    }

    @Test
    public void testVisitFor_1b()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "var len:int;for (var i:int = 0; i < len; i++) break;", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        //for (var /** @type {number} */ i = 0; i < len; i++)\n  break;
        assertMapping(node, 0, 0, 0, 0, 0, 5);    // for (
        assertMapping(node, 0, 18, 0, 36, 0, 38); // ;
        assertMapping(node, 0, 27, 0, 45, 0, 47); // ;
        assertMapping(node, 0, 32, 0, 50, 0, 51); // )
    }

    @Test
    public void testVisitFor_2()
    {
        IForLoopNode node = (IForLoopNode) getNode("for (;;) { break; }",
                IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        //for (;;) {\n  break;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 5);  // for (
        assertMapping(node, 0, 5, 0, 5, 0, 6);  // ;
        assertMapping(node, 0, 6, 0, 6, 0, 7);  // ;
        assertMapping(node, 0, 7, 0, 7, 0, 9);  // )
        assertMapping(node, 0, 9, 0, 9, 0, 10); // {
        assertMapping(node, 0, 18, 2, 0, 2, 1); // }
    }

    @Test
    public void testVisitForIn_1()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int in obj) { break; }", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        //for (var /** @type {number} */ i in obj) {\n  break;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 5);     // for (
        assertMapping(node, 0, 5, 0, 5, 0, 9);     // var
        assertMapping(node, 0, 9, 0, 31, 0, 32);   // i
        assertMapping(node, 0, 14, 0, 32, 0, 36);  // in
        assertMapping(node, 0, 18, 0, 36, 0, 39);  // obj
        assertMapping(node, 0, 21, 0, 39, 0, 41);  // )
        assertMapping(node, 0, 23, 0, 41, 0, 42);  // {
        assertMapping(node, 0, 25, 1, 2, 1, 7);    // break
        assertMapping(node, 0, 30, 1, 7, 1, 8);    // ;
        assertMapping(node, 0, 32, 2, 0, 2, 1);    // }
    }

    @Test
    public void testVisitForIn_1a()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int in obj)  break; ", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        //for (var /** @type {number} */ i in obj)\n  break;
        assertMapping(node, 0, 0, 0, 0, 0, 5);    // for (
        assertMapping(node, 0, 14, 0, 32, 0, 36); // in
        assertMapping(node, 0, 21, 0, 39, 0, 40); // )
    }

    @Test
    public void testVisitForEach_1()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for each(var i:int in obj) { break; }", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        //var foreachiter0_target = obj;\nfor (var foreachiter0 in foreachiter0_target) \n{\nvar i = foreachiter0_target[foreachiter0];\n{\n  break;\n}}\n
        assertMapping(node, 0, 22, 0, 0, 0, 26);   // var foreachiter0_target = 
        assertMapping(node, 0, 22, 0, 26, 0, 29);  // obj
        assertMapping(node, 0, 22, 0, 29, 0, 30);  // ;
        assertMapping(node, 0, 0, 1, 0, 1, 5);     // for (
        assertMapping(node, 0, 22, 1, 5, 1, 21);   // var foreachiter0
        assertMapping(node, 0, 22, 1, 25, 1, 44);  // foreachiter0_target
        assertMapping(node, 0, 25, 1, 44, 1, 46);  // )
        assertMapping(node, 0, 9, 3, 0, 3, 8);     // var i = 
        assertMapping(node, 0, 22, 3, 8, 3, 42);   // foreachiter0_target[foreachiter0];
        assertMapping(node, 0, 27, 4, 0, 4, 1);    // {
        assertMapping(node, 0, 29, 5, 2, 5, 7);    // break
        assertMapping(node, 0, 34, 5, 7, 5, 8);    // ;
        assertMapping(node, 0, 36, 6, 0, 6, 1);    // }
    }

    @Test
    public void testVisitForEach_1a()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for each(var i:int in obj)  break; ", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        //var foreachiter0_target = obj;\nfor (var foreachiter0 in foreachiter0_target) \n{\nvar i = foreachiter0_target[foreachiter0];\n\n  break;}\n
        assertMapping(node, 0, 22, 0, 0, 0, 26);   // var foreachiter0_target = 
        assertMapping(node, 0, 22, 0, 26, 0, 29);  // obj
        assertMapping(node, 0, 22, 0, 29, 0, 30);  // ;
        assertMapping(node, 0, 0, 1, 0, 1, 5);     // for (
        assertMapping(node, 0, 22, 1, 5, 1, 21);   // var foreachiter0
        assertMapping(node, 0, 22, 1, 25, 1, 44);  // foreachiter0_target
        assertMapping(node, 0, 25, 1, 44, 1, 46);  // )
        assertMapping(node, 0, 9, 3, 0, 3, 8);     // var i = 
        assertMapping(node, 0, 22, 3, 8, 3, 42);   // foreachiter0_target[foreachiter0];
        assertMapping(node, 0, 28, 5, 2, 5, 7);    // break
        assertMapping(node, 0, 33, 5, 7, 5, 8);    // ;
    }

    @Test
    public void testVisitForEach_2()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for each(var i:int in obj.foo()) { break; }", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        //var foreachiter0_target = obj.foo();\nfor (var foreachiter0 in foreachiter0_target) \n{\nvar i = foreachiter0_target[foreachiter0];\n{\n  break;\n}}\n
        assertMapping(node, 0, 22, 0, 0, 0, 26);   // var foreachiter0_target = 
        assertMapping(node, 0, 22, 0, 26, 0, 29);  // obj
        assertMapping(node, 0, 25, 0, 29, 0, 30);  // .
        assertMapping(node, 0, 26, 0, 30, 0, 33);  // foo
        assertMapping(node, 0, 22, 0, 35, 0, 36);  // ;
        assertMapping(node, 0, 0, 1, 0, 1, 5);     // for (
        assertMapping(node, 0, 22, 1, 5, 1, 21);   // var foreachiter0
        assertMapping(node, 0, 22, 1, 25, 1, 44);  // foreachiter0_target
        assertMapping(node, 0, 31, 1, 44, 1, 46);  // )
        assertMapping(node, 0, 9, 3, 0, 3, 8);     // var i = 
        assertMapping(node, 0, 22, 3, 8, 3, 42);   // foreachiter0_target[foreachiter0];
        assertMapping(node, 0, 33, 4, 0, 4, 1);    // {
        assertMapping(node, 0, 35, 5, 2, 5, 7);    // break
        assertMapping(node, 0, 40, 5, 7, 5, 8);    // ;
        assertMapping(node, 0, 42, 6, 0, 6, 1);    // }
    }

    @Test
    public void testVisitForEach_HoistedVar()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "var i:int; for each(i in obj)  break; ", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        //var foreachiter0_target = obj;\nfor (var foreachiter0 in foreachiter0_target) \n{\ni = foreachiter0_target[foreachiter0];\n\n  break;}\n
        assertMapping(node, 0, 14, 0, 0, 0, 26);   // var foreachiter0_target = 
        assertMapping(node, 0, 14, 0, 26, 0, 29);  // obj
        assertMapping(node, 0, 14, 0, 29, 0, 30);  // ;
        assertMapping(node, 0, 0, 1, 0, 1, 5);     // for (
        assertMapping(node, 0, 14, 1, 5, 1, 21);   // var foreachiter0
        assertMapping(node, 0, 14, 1, 25, 1, 44);  // foreachiter0_target
        assertMapping(node, 0, 17, 1, 44, 1, 46);  // )
        assertMapping(node, 0, 9, 3, 0, 3, 1);     // i
        assertMapping(node, 0, 9, 3, 1, 3, 4);     //  =
        assertMapping(node, 0, 14, 3, 4, 3, 38);   // foreachiter0_target[foreachiter0];
        assertMapping(node, 0, 20, 5, 2, 5, 7);    // break
        assertMapping(node, 0, 25, 5, 7, 5, 8);    // ;
    }

    //----------------------------------
    // try {} catch () {} finally {}
    //----------------------------------

    @Test
    public void testVisitTry_Catch()
    {
        ITryNode node = (ITryNode) getNode("try { a; } catch (e:Error) { b; }",
                ITryNode.class);
        asBlockWalker.visitTry(node);
        //try {\n  a;\n} catch (e) {\n  b;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 4);     // try
        assertMapping(node, 0, 4, 0, 4, 0, 5);     // {
        assertMapping(node, 0, 9, 2, 0, 2, 1);     // }
        assertMapping(node, 0, 11, 2, 1, 2, 9);    // catch(
        assertMapping(node, 0, 18, 2, 9, 2, 10);   // e
        assertMapping(node, 0, 25, 2, 10, 2, 12);  // )
        assertMapping(node, 0, 27, 2, 12, 2, 13);  // {
        assertMapping(node, 0, 32, 4, 0, 4, 1);    // }
    }

    @Test
    public void testVisitTry_Catch_Finally()
    {
        ITryNode node = (ITryNode) getNode(
                "try { a; } catch (e:Error) { b; } finally { c; }",
                ITryNode.class);
        asBlockWalker.visitTry(node);
        //try {\n  a;\n} catch (e) {\n  b;\n} finally {\n  c;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 4);     // try
        assertMapping(node, 0, 4, 0, 4, 0, 5);     // {
        assertMapping(node, 0, 9, 2, 0, 2, 1);     // }
        assertMapping(node, 0, 11, 2, 1, 2, 9);    // catch(
        assertMapping(node, 0, 18, 2, 9, 2, 10);   // e
        assertMapping(node, 0, 25, 2, 10, 2, 12);  // )
        assertMapping(node, 0, 27, 2, 12, 2, 13);  // {
        assertMapping(node, 0, 32, 4, 0, 4, 1);    // }
        assertMapping(node, 0, 34, 4, 1, 4, 10);   // finally
        assertMapping(node, 0, 42, 4, 10, 4, 11);  // {
        assertMapping(node, 0, 47, 6, 0, 6, 1);    // }
    }

    @Test
    public void testVisitTry_Catch_Catch_Finally()
    {
        ITryNode node = (ITryNode) getNode(
                "try { a; } catch (e:Error) { b; } catch (f:Error) { c; } finally { d; }",
                ITryNode.class);
        asBlockWalker.visitTry(node);
        //try {\n  a;\n} catch (e) {\n  b;\n} catch (f) {\n  c;\n} finally {\n  d;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 4);     // try
        assertMapping(node, 0, 4, 0, 4, 0, 5);     // {
        assertMapping(node, 0, 9, 2, 0, 2, 1);     // }
        assertMapping(node, 0, 11, 2, 1, 2, 9);    // catch(
        assertMapping(node, 0, 18, 2, 9, 2, 10);   // e
        assertMapping(node, 0, 25, 2, 10, 2, 12);  // )
        assertMapping(node, 0, 27, 2, 12, 2, 13);  // {
        assertMapping(node, 0, 32, 4, 0, 4, 1);    // }
        assertMapping(node, 0, 34, 4, 1, 4, 9);    // catch(
        assertMapping(node, 0, 41, 4, 9, 4, 10);   // f
        assertMapping(node, 0, 48, 4, 10, 4, 12);  // )
        assertMapping(node, 0, 50, 4, 12, 4, 13);  // {
        assertMapping(node, 0, 55, 6, 0, 6, 1);    // }
        assertMapping(node, 0, 57, 6, 1, 6, 10);   // finally
        assertMapping(node, 0, 65, 6, 10, 6, 11);  // {
        assertMapping(node, 0, 70, 8, 0, 8, 1);    // }
    }

    @Test
    public void testVisitTry_CatchEmpty_FinallyEmpty_()
    {
        ITryNode node = (ITryNode) getNode(
                "try { a; } catch (e:Error) {  } finally {  }", ITryNode.class);
        asBlockWalker.visitTry(node);
        //try {\n  a;\n} catch (e) {\n} finally {\n}
        assertMapping(node, 0, 0, 0, 0, 0, 4);     // try
        assertMapping(node, 0, 4, 0, 4, 0, 5);     // {
        assertMapping(node, 0, 9, 2, 0, 2, 1);     // }
        assertMapping(node, 0, 11, 2, 1, 2, 9);    // catch(
        assertMapping(node, 0, 18, 2, 9, 2, 10);   // e
        assertMapping(node, 0, 25, 2, 10, 2, 12);  // )
        assertMapping(node, 0, 27, 2, 12, 2, 13);  // {
        assertMapping(node, 0, 30, 3, 0, 3, 1);    // }
        assertMapping(node, 0, 32, 3, 1, 3, 10);   // finally
        assertMapping(node, 0, 40, 3, 10, 3, 11);  // {
        assertMapping(node, 0, 43, 4, 0, 4, 1);    // }
    }

    //----------------------------------
    // switch {}
    //----------------------------------

    @Test
    public void testVisitSwitch_1()
    {
        ISwitchNode node = (ISwitchNode) getNode("switch(i){case 1: break;}",
                ISwitchNode.class);
        asBlockWalker.visitSwitch(node);
        //switch (i) {\n  case 1:\n    break;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 8);     // switch (
        assertMapping(node, 0, 7, 0, 8, 0, 9);     // i
        assertMapping(node, 0, 8, 0, 9, 0, 11);    // )
        assertMapping(node, 0, 9, 0, 11, 0, 12);   // {
        assertMapping(node, 0, 10, 1, 2, 1, 7);    // case
        assertMapping(node, 0, 15, 1, 7, 1, 8);    // 1
        assertMapping(node, 0, 16, 1, 8, 1, 9);    // :
        assertMapping(node, 0, 18, 2, 4, 2, 9);    // break
        assertMapping(node, 0, 23, 2, 9, 2, 10);   // ;
        assertMapping(node, 0, 24, 3, 0, 3, 1);    // }
    }

    @Test
    public void testVisitSwitch_1a()
    {
        ISwitchNode node = (ISwitchNode) getNode(
                "switch(i){case 1: { break; }}", ISwitchNode.class);
        asBlockWalker.visitSwitch(node);
        //switch (i) {\n  case 1:\n    break;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 8);     // switch (
        assertMapping(node, 0, 7, 0, 8, 0, 9);     // i
        assertMapping(node, 0, 8, 0, 9, 0, 11);    // )
        assertMapping(node, 0, 9, 0, 11, 0, 12);   // {
        assertMapping(node, 0, 10, 1, 2, 1, 7);    // case
        assertMapping(node, 0, 15, 1, 7, 1, 8);    // 1
        assertMapping(node, 0, 16, 1, 8, 1, 9);    // :
        assertMapping(node, 0, 20, 2, 4, 2, 9);    // break
        assertMapping(node, 0, 25, 2, 9, 2, 10);   // ;
        assertMapping(node, 0, 28, 3, 0, 3, 1);    // }
    }

    @Test
    public void testVisitSwitch_2()
    {
        ISwitchNode node = (ISwitchNode) getNode(
                "switch(i){case 1: break; default: return;}", ISwitchNode.class);
        asBlockWalker.visitSwitch(node);
        //switch (i) {\n  case 1:\n    break;\n  default:\n    return;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 8);     // switch (
        assertMapping(node, 0, 7, 0, 8, 0, 9);     // i
        assertMapping(node, 0, 8, 0, 9, 0, 11);    // )
        assertMapping(node, 0, 9, 0, 11, 0, 12);   // {
        assertMapping(node, 0, 10, 1, 2, 1, 7);    // case
        assertMapping(node, 0, 15, 1, 7, 1, 8);    // 1
        assertMapping(node, 0, 16, 1, 8, 1, 9);    // :
        assertMapping(node, 0, 18, 2, 4, 2, 9);    // break
        assertMapping(node, 0, 23, 2, 9, 2, 10);   // ;
        assertMapping(node, 0, 25, 3, 2, 3, 10);   // default:
        assertMapping(node, 0, 34, 4, 4, 4, 10);   // return
        assertMapping(node, 0, 40, 4, 10, 4, 11);  // ;
        assertMapping(node, 0, 41, 5, 0, 5, 1);    // }
    }

    @Test
    public void testVisitSwitch_3()
    {
        ISwitchNode node = (ISwitchNode) getNode(
                "switch(i){case 1: { var x:int = 42; break; }; case 2: { var y:int = 66; break; }}", ISwitchNode.class);
        asBlockWalker.visitSwitch(node);
        //switch (i) {\n  case 1:\n    var /** @type {number} */ x = 42;\n    break;\n  case 2:\n    var /** @type {number} */ y = 66;\n    break;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 8);     // switch (
        assertMapping(node, 0, 7, 0, 8, 0, 9);     // i
        assertMapping(node, 0, 8, 0, 9, 0, 11);    // )
        assertMapping(node, 0, 9, 0, 11, 0, 12);   // {
        assertMapping(node, 0, 10, 1, 2, 1, 7);    // case
        assertMapping(node, 0, 15, 1, 7, 1, 8);    // 1
        assertMapping(node, 0, 16, 1, 8, 1, 9);    // :
        assertMapping(node, 0, 36, 3, 4, 3, 9);    // break
        assertMapping(node, 0, 41, 3, 9, 3, 10);   // ;
        assertMapping(node, 0, 46, 4, 2, 4, 7);    // case
        assertMapping(node, 0, 51, 4, 7, 4, 8);    // 2
        assertMapping(node, 0, 52, 4, 8, 4, 9);    // :
        assertMapping(node, 0, 72, 6, 4, 6, 9);    // break
        assertMapping(node, 0, 77, 6, 9, 6, 10);   // ;
        assertMapping(node, 0, 80, 7, 0, 7, 1);    // }
    }

    //----------------------------------
    // if ()
    //----------------------------------

    @Test
    public void testVisitIf_1()
    {
        IIfNode node = (IIfNode) getNode("if (a) b++;", IIfNode.class);
        asBlockWalker.visitIf(node);
        //if (a)\n  b++;
        assertMapping(node, 0, 0, 0, 0, 0, 4);    // if (
        assertMapping(node, 0, 4, 0, 4, 0, 5);    // a
        assertMapping(node, 0, 5, 0, 5, 0, 6);    // )
    }

    @Test
    public void testVisitIf_2()
    {
        IIfNode node = (IIfNode) getNode("if (a) b++; else c++;", IIfNode.class);
        asBlockWalker.visitIf(node);
        //if (a)\n  b++;\nelse\n  c++;
        assertMapping(node, 0, 0, 0, 0, 0, 4);    // if (
        assertMapping(node, 0, 4, 0, 4, 0, 5);    // a
        assertMapping(node, 0, 5, 0, 5, 0, 6);    // )
        assertMapping(node, 0, 12, 2, 0, 2, 4);   // else
    }

    @Test
    public void testVisitIf_4()
    {
        IIfNode node = (IIfNode) getNode(
                "if (a) b++; else if (c) d++; else if(e) --f;", IIfNode.class);
        asBlockWalker.visitIf(node);
        //if (a)\n  b++;\nelse if (c)\n  d++;\nelse if (e)\n  --f;
        assertMapping(node, 0, 0, 0, 0, 0, 4);    // if (
        assertMapping(node, 0, 4, 0, 4, 0, 5);    // a
        assertMapping(node, 0, 5, 0, 5, 0, 6);    // )
        assertMapping(node, 0, 12, 2, 0, 2, 9);   // else if (
        assertMapping(node, 0, 22, 2, 10, 2, 11); // )
        assertMapping(node, 0, 29, 4, 0, 4, 9);   // else if (
        assertMapping(node, 0, 38, 4, 10, 4, 11); // )
    }

    //----------------------------------
    // if () { }
    //----------------------------------

    @Test
    public void testVisitIf_1a()
    {
        IIfNode node = (IIfNode) getNode("if (a) { b++; }", IIfNode.class);
        asBlockWalker.visitIf(node);
        //if (a) {\n  b++;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 4);    // if (
        assertMapping(node, 0, 4, 0, 4, 0, 5);    // a
        assertMapping(node, 0, 5, 0, 5, 0, 7);    // )
        assertMapping(node, 0, 7, 0, 7, 0, 8);    // {
        assertMapping(node, 0, 14, 2, 0, 2, 1);   // }
    }

    @Test
    public void testVisitIf_1b()
    {
        IIfNode node = (IIfNode) getNode("if (a) { b++; } else { c++; }",
                IIfNode.class);
        asBlockWalker.visitIf(node);
        //if (a) {\n  b++;\n} else {\n  c++;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 4);    // if (
        assertMapping(node, 0, 4, 0, 4, 0, 5);    // a
        assertMapping(node, 0, 5, 0, 5, 0, 7);    // )
        assertMapping(node, 0, 7, 0, 7, 0, 8);    // {
        assertMapping(node, 0, 14, 2, 0, 2, 1);   // }
        assertMapping(node, 0, 16, 2, 2, 2, 7);   // else
        assertMapping(node, 0, 21, 2, 7, 2, 8);   // {
        assertMapping(node, 0, 28, 4, 0, 4, 1);   // }
    }

    @Test
    public void testVisitIf_1c()
    {
        IIfNode node = (IIfNode) getNode(
                "if (a) { b++; } else if (b) { c++; } else { d++; }",
                IIfNode.class);
        asBlockWalker.visitIf(node);
        //if (a) {\n  b++;\n} else if (b) {\n  c++;\n} else {\n  d++;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 4);    // if (
        assertMapping(node, 0, 4, 0, 4, 0, 5);    // a
        assertMapping(node, 0, 5, 0, 5, 0, 7);    // )
        assertMapping(node, 0, 7, 0, 7, 0, 8);    // {
        assertMapping(node, 0, 14, 2, 0, 2, 1);   // }
        assertMapping(node, 0, 16, 2, 2, 2, 11);  // else if(
        assertMapping(node, 0, 26, 2, 12, 2, 14); // )
        assertMapping(node, 0, 28, 2, 14, 2, 15); // {
        assertMapping(node, 0, 35, 4, 0, 4, 1);   // }
        assertMapping(node, 0, 37, 4, 2, 4, 7);   // else
        assertMapping(node, 0, 42, 4, 7, 4, 8);   // {
        assertMapping(node, 0, 49, 6, 0, 6, 1);   // {
    }

    @Test
    public void testVisitIf_3()
    {
        IIfNode node = (IIfNode) getNode(
                "if (a) b++; else if (c) d++; else --e;", IIfNode.class);
        asBlockWalker.visitIf(node);
        //if (a)\n  b++;\nelse if (c)\n  d++;\nelse\n  --e;
        assertMapping(node, 0, 0, 0, 0, 0, 4);     // if (
        assertMapping(node, 0, 4, 0, 4, 0, 5);     // a
        assertMapping(node, 0, 5, 0, 5, 0, 6);     // )
        assertMapping(node, 0, 12, 2, 0, 2, 9);    // else if (
        assertMapping(node, 0, 21, 2, 9, 2, 10);   // c
        assertMapping(node, 0, 22, 2, 10, 2, 11);  // )
        assertMapping(node, 0, 29, 4, 0, 4, 4);    // else
    }

    //----------------------------------
    // with () {}
    //----------------------------------

    @Test
    public void testVisitWith()
    {
        IWithNode node = (IWithNode) getNode("with (a) { b; }", IWithNode.class);
        asBlockWalker.visitWith(node);
        //with (a) {\n  b;\n}
        assertMapping(node, 0, 0, 0, 0, 0, 6);     // with (
        assertMapping(node, 0, 6, 0, 6, 0, 7);     // a
        assertMapping(node, 0, 7, 0, 7, 0, 9);     // )
        assertMapping(node, 0, 9, 0, 9, 0, 10);    // {
        assertMapping(node, 0, 11, 1, 2, 1, 3);     // b
        assertMapping(node, 0, 12, 1, 3, 1, 4);    // ;
        assertMapping(node, 0, 14, 2, 0, 2, 1);    // }
    }

    @Test
    public void testVisitWith_1a()
    {
        IWithNode node = (IWithNode) getNode("with (a) b;", IWithNode.class);
        asBlockWalker.visitWith(node);
        //with (a)\n  b;
        assertMapping(node, 0, 0, 0, 0, 0, 6);     // with (
        assertMapping(node, 0, 6, 0, 6, 0, 7);     // a
        assertMapping(node, 0, 7, 0, 7, 0, 8);     // )
        assertMapping(node, 0, 9, 1, 2, 1, 3);     // b
        assertMapping(node, 0, 10, 1, 3, 1, 4);    // ;
    }


    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }
}
