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
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IIfNode;
import org.apache.royale.compiler.tree.as.ISwitchNode;
import org.apache.royale.compiler.tree.as.IThrowNode;
import org.apache.royale.compiler.tree.as.ITryNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.tree.as.IWhileLoopNode;
import org.apache.royale.compiler.tree.as.IWithNode;
import org.junit.Test;

/**
 * @author Michael Schmalle
 */
public class TestStatements extends ASTestBase
{
    //--------------------------------------------------------------------------
    // if
    //--------------------------------------------------------------------------

    //----------------------------------
    // var declaration
    //----------------------------------

    @Test
    public void testVarDeclaration()
    {
        IVariableNode node = (IVariableNode) getNode("var a;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var a:*");
    }

    @Test
    public void testVarDeclaration_withType()
    {
        IVariableNode node = (IVariableNode) getNode("var a:int;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var a:int");
    }

    @Test
    public void testVarDeclaration_withTypeAssignedValue()
    {
        IVariableNode node = (IVariableNode) getNode("var a:int = 42;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var a:int = 42");
    }

    @Test
    public void testVarDeclaration_withTypeAssignedValueComplex()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a:Foo = new Foo(42, 'goo');", IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var a:Foo = new Foo(42, 'goo')");
    }

    @Test
    public void testVarDeclaration_withList()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a:int = 4, b:int = 11, c:int = 42;", IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var a:int = 4, b:int = 11, c:int = 42");
    }

    //----------------------------------
    // const declaration
    //----------------------------------

    @Test
    public void testConstDeclaration()
    {
        IVariableNode node = (IVariableNode) getNode("const a = 42;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("const a:* = 42");
    }

    @Test
    public void testConstDeclaration_withType()
    {
        IVariableNode node = (IVariableNode) getNode("const a:int = 42;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("const a:int = 42");
    }

    @Test
    public void testConstDeclaration_withList()
    {
        IVariableNode node = (IVariableNode) getNode(
                "const a:int = 4, b:int = 11, c:int = 42;", IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("const a:int = 4, b:int = 11, c:int = 42");
    }

    //----------------------------------
    // if ()
    //----------------------------------

    @Test
    public void testVisitIf_1()
    {
        IIfNode node = (IIfNode) getNode("if (a) b++;", IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a)\n\tb++;");
    }

    @Test
    public void testVisitIf_2()
    {
        IIfNode node = (IIfNode) getNode("if (a) b++; else c++;", IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a)\n\tb++;\nelse\n\tc++;");
    }

    @Test
    public void testVisitIf_4()
    {
        IIfNode node = (IIfNode) getNode(
                "if (a) b++; else if (c) d++; else if(e) --f;", IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a)\n\tb++;\nelse if (c)\n\td++;\nelse if (e)\n\t--f;");
    }

    //----------------------------------
    // if () { }
    //----------------------------------

    @Test
    public void testVisitIf_1a()
    {
        IIfNode node = (IIfNode) getNode("if (a) { b++; }", IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a) {\n\tb++;\n}");
    }

    @Test
    public void testVisitIf_1b()
    {
        IIfNode node = (IIfNode) getNode("if (a) { b++; } else { c++; }",
                IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a) {\n\tb++;\n} else {\n\tc++;\n}");
    }

    @Test
    public void testVisitIf_1c()
    {
        IIfNode node = (IIfNode) getNode(
                "if (a) { b++; } else if (b) { c++; } else { d++; }",
                IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a) {\n\tb++;\n} else if (b) {\n\tc++;\n} else {\n\td++;\n}");
    }

    @Test
    public void testVisitIf_3()
    {
        IIfNode node = (IIfNode) getNode(
                "if (a) b++; else if (c) d++; else --e;", IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a)\n\tb++;\nelse if (c)\n\td++;\nelse\n\t--e;");
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
        assertOut("for (var i:int = 0; i < len; i++) {\n\tbreak;\n}");
    }

    @Test
    public void testVisitFor_1b()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int = 0; i < len; i++) break;", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var i:int = 0; i < len; i++)\n\tbreak;");
    }

    @Test
    public void testVisitFor_2()
    {
        IForLoopNode node = (IForLoopNode) getNode("for (;;) { break; }",
                IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (;;) {\n\tbreak;\n}");
    }

    @Test
    public void testVisitForIn_1()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int in obj) { break; }", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var i:int in obj) {\n\tbreak;\n}");
    }

    @Test
    public void testVisitForIn_1a()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int in obj)  break; ", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var i:int in obj)\n\tbreak;");
    }

    @Test
    public void testVisitForEach_1()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for each(var i:int in obj) { break; }", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for each (var i:int in obj) {\n\tbreak;\n}");
    }

    @Test
    public void testVisitForEach_1a()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for each(var i:int in obj)  break; ", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for each (var i:int in obj)\n\tbreak;");
    }

    //----------------------------------
    // while () { }
    //----------------------------------

    @Test
    public void testVisitWhileLoop_1()
    {
        IWhileLoopNode node = (IWhileLoopNode) getNode(
                "while(a > b){a++;--b;}", IWhileLoopNode.class);
        asBlockWalker.visitWhileLoop(node);
        assertOut("while (a > b) {\n\ta++;\n\t--b;\n}");
    }

    @Test
    public void testVisitWhileLoop_1a()
    {
        IWhileLoopNode node = (IWhileLoopNode) getNode("while(a > b) a++;",
                IWhileLoopNode.class);
        asBlockWalker.visitWhileLoop(node);
        assertOut("while (a > b)\n\ta++;");
    }

    //----------------------------------
    // do {} while ()
    //----------------------------------

    @Test
    public void testVisitWhileLoop_Do_1()
    {
        IWhileLoopNode node = (IWhileLoopNode) getNode(
                "do {a++;--b;} while(a > b);", IWhileLoopNode.class);
        asBlockWalker.visitWhileLoop(node);
        assertOut("do {\n\ta++;\n\t--b;\n} while (a > b);");
    }

    @Test
    public void testVisitWhileLoop_Do_1a()
    {
        IWhileLoopNode node = (IWhileLoopNode) getNode("do a++; while(a > b);",
                IWhileLoopNode.class);
        asBlockWalker.visitWhileLoop(node);
        assertOut("do\n\ta++;\nwhile (a > b);");
    }

    //----------------------------------
    // throw ()
    //----------------------------------

    @Test
    public void testVisitThrow()
    {
        IThrowNode node = (IThrowNode) getNode("throw new Error('foo');",
                IThrowNode.class);
        asBlockWalker.visitThrow(node);
        assertOut("throw new Error('foo')");
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
        assertOut("try {\n\ta;\n} catch (e:Error) {\n\tb;\n}");
    }

    @Test
    public void testVisitTry_Catch_Finally()
    {
        ITryNode node = (ITryNode) getNode(
                "try { a; } catch (e:Error) { b; } finally { c; }",
                ITryNode.class);
        asBlockWalker.visitTry(node);
        assertOut("try {\n\ta;\n} catch (e:Error) {\n\tb;\n} finally {\n\tc;\n}");
    }

    @Test
    public void testVisitTry_Catch_Catch_Finally()
    {
        ITryNode node = (ITryNode) getNode(
                "try { a; } catch (e:Error) { b; } catch (f:Error) { c; } finally { d; }",
                ITryNode.class);
        asBlockWalker.visitTry(node);
        assertOut("try {\n\ta;\n} catch (e:Error) {\n\tb;\n} catch (f:Error) {\n\tc;\n} finally {\n\td;\n}");
    }

    @Test
    public void testVisitTry_CatchEmpty_FinallyEmpty_()
    {
        ITryNode node = (ITryNode) getNode(
                "try { a; } catch (e:Error) {  } finally {  }", ITryNode.class);
        asBlockWalker.visitTry(node);
        assertOut("try {\n\ta;\n} catch (e:Error) {\n} finally {\n}");
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
        assertOut("switch (i) {\n\tcase 1:\n\t\tbreak;\n}");
    }

    @Test
    public void testVisitSwitch_1a()
    {
        ISwitchNode node = (ISwitchNode) getNode(
                "switch(i){case 1: { break; }}", ISwitchNode.class);
        asBlockWalker.visitSwitch(node);
        // (erikdebruin) the code is valid without the extra braces, 
        //               i.e. we're good, we "don't care"
        assertOut("switch (i) {\n\tcase 1:\n\t\tbreak;\n}");
    }

    @Test
    public void testVisitSwitch_2()
    {
        ISwitchNode node = (ISwitchNode) getNode(
                "switch(i){case 1: break; default: return;}", ISwitchNode.class);
        asBlockWalker.visitSwitch(node);
        assertOut("switch (i) {\n\tcase 1:\n\t\tbreak;\n\tdefault:\n\t\treturn;\n}");
    }

    @Test
    public void testVisitSwitch_3()
    {
        ISwitchNode node = (ISwitchNode) getNode(
                "switch(i){case 1: { var x:int = 42; break; }; case 2: { var y:int = 66; break; }}", ISwitchNode.class);
        asBlockWalker.visitSwitch(node);
        assertOut("switch (i) {\n\tcase 1:\n\t\tvar x:int = 42;\n\t\tbreak;\n\tcase 2:\n\t\tvar y:int = 66;\n\t\tbreak;\n}");
    }

    //----------------------------------
    // label : for () {}
    //----------------------------------

    @Test
    public void testVisitLabel_1()
    {
        LabeledStatementNode node = (LabeledStatementNode) getNode(
                "foo: for each(var i:int in obj) { break foo; }",
                LabeledStatementNode.class);
        asBlockWalker.visitLabeledStatement(node);
        assertOut("foo : for each (var i:int in obj) {\n\tbreak foo;\n}");
    }

    @Test
    public void testVisitLabel_1a()
    {
        // ([unknown]) LabelStatement messes up in finally{} block, something is wrong there
        
        // (erikdebruin) I don't see a finally block in the test code and the 
        //               test passes... What's wrong?
        
        LabeledStatementNode node = (LabeledStatementNode) getNode(
                "foo: for each(var i:int in obj) break foo;",
                LabeledStatementNode.class);
        asBlockWalker.visitLabeledStatement(node);
        assertOut("foo : for each (var i:int in obj)\n\tbreak foo;");
    }

    //----------------------------------
    // with () {}
    //----------------------------------

    @Test
    public void testVisitWith()
    {
        IWithNode node = (IWithNode) getNode("with (a) { b; }", IWithNode.class);
        asBlockWalker.visitWith(node);
        assertOut("with (a) {\n\tb;\n}");
    }

    @Test
    public void testVisitWith_1a()
    {
        IWithNode node = (IWithNode) getNode("with (a) b;", IWithNode.class);
        asBlockWalker.visitWith(node);
        assertOut("with (a)\n\tb;");
    }

    @Test
    public void testVisit()
    {
        IFileNode node = (IFileNode) getNode(
                "try { a; } catch (e:Error) { if (a) { if (b) { if (c) b; else if (f) a; else e; }} } finally {  }"
                        + "if (d) for (var i:int = 0; i < len; i++) break;"
                        + "if (a) { with (ab) { c(); } "
                        + "do {a++;do a++; while(a > b);} while(c > d); }"
                        + "if (b) { try { a; throw new Error('foo'); } catch (e:Error) { "
                        + " switch(i){case 1: break; default: return;}"
                        + " } catch (f:Error) { c; eee.dd; } finally { "
                        + "  d;  var a:Object = function(foo:int, bar:String = 'goo'):int{return -1;};"
                        + "  eee.dd; eee.dd; eee.dd; eee.dd;} }"
                        + "foo: for each(var i:int in obj) break foo;",
                IFileNode.class);
        asBlockWalker.visitFile(node);
        assertOut("package {\n\tpublic class RoyaleTest_A {\n\t\tfunction royaleTest_a():void {\n\t\t\ttry {\n\t\t\t\ta;\n\t\t\t} catch (e:Error) {\n\t\t\t\tif (a) {\n\t\t\t\t\tif (b) {\n\t\t\t\t\t\tif (c)\n\t\t\t\t\t\t\tb;\n\t\t\t\t\t\telse if (f)\n\t\t\t\t\t\t\ta;\n\t\t\t\t\t\telse\n\t\t\t\t\t\t\te;\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t} finally {\n\t\t\t}\n\t\t\tif (d)\n\t\t\t\tfor (var i:int = 0; i < len; i++)\n\t\t\t\t\tbreak;\n\t\t\tif (a) {\n\t\t\t\twith (ab) {\n\t\t\t\t\tc();\n\t\t\t\t}\n\t\t\t\tdo {\n\t\t\t\t\ta++;\n\t\t\t\t\tdo\n\t\t\t\t\t\ta++;\n\t\t\t\t\twhile (a > b);\n\t\t\t\t} while (c > d);\n\t\t\t}\n\t\t\tif (b) {\n\t\t\t\ttry {\n\t\t\t\t\ta;\n\t\t\t\t\tthrow new Error('foo');\n\t\t\t\t} catch (e:Error) {\n\t\t\t\t\tswitch (i) {\n\t\t\t\t\t\tcase 1:\n\t\t\t\t\t\t\tbreak;\n\t\t\t\t\t\tdefault:\n\t\t\t\t\t\t\treturn;\n\t\t\t\t\t}\n\t\t\t\t} catch (f:Error) {\n\t\t\t\t\tc;\n\t\t\t\t\teee.dd;\n\t\t\t\t} finally {\n\t\t\t\t\td;\n\t\t\t\t\tvar a:Object = function(foo:int, bar:String = 'goo'):int {\n\t\t\t\t\t\treturn -1;\n\t\t\t\t\t};\n\t\t\t\t\teee.dd;\n\t\t\t\t\teee.dd;\n\t\t\t\t\teee.dd;\n\t\t\t\t\teee.dd;\n\t\t\t\t}\n\t\t\t}\n\t\t\tfoo : for each (var i:int in obj)\n\t\t\t\tbreak foo;;\n\t}\n}\n}");
    }
}
