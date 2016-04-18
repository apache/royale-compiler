package org.apache.flex.compiler.internal.codegen.js.sourcemaps;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.test.SourceMapTestBase;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IIfNode;
import org.apache.flex.compiler.tree.as.IVariableNode;

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
        IVariableNode node = (IVariableNode) getNode("var a:int;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        //var /** @type {number} */ a
        assertMapping(node, 0, 0, 0, 0, 0, 4);   // var
        assertMapping(node, 0, 4, 0, 26, 0, 27); // a
        assertMapping(node, 0, 5, 0, 4, 0, 26);  // :int
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
                "for (var i:int = 0; i < len; i++) { break; }",
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
                "for (var i:int = 0; i < len; i++) break;", IForLoopNode.class);
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
        assertMapping(node, 0, 0, 0, 0, 0, 5);    // for (
        assertMapping(node, 0, 14, 0, 32, 0, 36); // in
        assertMapping(node, 0, 21, 0, 39, 0, 41); // )
        assertMapping(node, 0, 23, 0, 41, 0, 42); // {
        assertMapping(node, 0, 32, 2, 0, 2, 1);   // }
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

    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }
}
