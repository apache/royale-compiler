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

package org.apache.royale.compiler.internal.codegen.js.goog;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.as.TestStatements;
import org.apache.royale.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IIfNode;
import org.apache.royale.compiler.tree.as.ISwitchNode;
import org.apache.royale.compiler.tree.as.ITryNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

/**
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class TestGoogStatements extends TestStatements
{
    //----------------------------------
    // var declaration
    //----------------------------------

    @Override
    @Test
    public void testVarDeclaration()
    {
        IVariableNode node = (IVariableNode) getNode("var a;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {*} */ a");
    }

    @Override
    @Test
    public void testVarDeclaration_withType()
    {
        IVariableNode node = (IVariableNode) getNode("var a:int;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a");
    }

    @Override
    @Test
    public void testVarDeclaration_withTypeAssignedValue()
    {
        IVariableNode node = (IVariableNode) getNode("var a:int = 42;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 42");
    }

    @Override
    @Test
    public void testVarDeclaration_withTypeAssignedValueComplex()
    {
        IVariableNode node = (IVariableNode) getNode(
                "class A { public function b():void { var a:Foo = new Foo(42, 'goo');}} class Foo {}", IVariableNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Foo} */ a = new Foo(42, 'goo')");
    }

    @Override
    @Test
    public void testVarDeclaration_withList()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a:int = 4, b:int = 11, c:int = 42;", IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 4, /** @type {number} */ b = 11, /** @type {number} */ c = 42");
    }

    //----------------------------------
    // const declaration
    //----------------------------------

    @Override
    @Test
    public void testConstDeclaration()
    {
        IVariableNode node = (IVariableNode) getNode("const a = 42;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("\n/**\n * @const\n * @type {*}\n */\na = 42");
    }

    @Override
    @Test
    public void testConstDeclaration_withType()
    {
        IVariableNode node = (IVariableNode) getNode("const a:int = 42;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("\n/**\n * @const\n * @type {number}\n */\na = 42");
    }

    @Override
    @Test
    public void testConstDeclaration_withList()
    {
        IVariableNode node = (IVariableNode) getNode(
                "const a:int = 4, b:int = 11, c:int = 42;", IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("\n/**\n * @const\n * @type {number}\n */\na = 4, \n/**\n * @const\n * @type {number}\n */\nb = 11, \n/**\n * @const\n * @type {number}\n */\nc = 42");
    }

    //----------------------------------
    // for () { }
    //----------------------------------

    @Override
    @Test
    public void testVisitFor_1a()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int = 0; i < len; i++) { break; }",
                IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {number} */ i = 0; i < len; i++) {\n\tbreak;\n}");
    }

    @Override
    @Test
    public void testVisitFor_1b()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int = 0; i < len; i++) break;", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {number} */ i = 0; i < len; i++)\n\tbreak;");
    }

    @Test
    public void testVisitFor_1c()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int = 0, len:int = 10; i < len; i++) break;", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {number} */ i = 0, /** @type {number} */ len = 10; i < len; i++)\n\tbreak;");
    }
    
    @Override
    @Test
    public void testVisitForIn_1()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int in obj) { break; }", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {number} */ i in obj) {\n\tbreak;\n}");
    }

    @Override
    @Test
    public void testVisitForIn_1a()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int in obj)  break; ", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {number} */ i in obj)\n\tbreak;");
    }

    @Override
    @Test
    public void testVisitForEach_1()
    {
        // TODO (erikdebruin) we need to insert a "goog.require('goog.array')"
        //                    into the header
        IForLoopNode node = (IForLoopNode) getNode(
                "for each(var i:int in obj) { break; }", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("goog.array.forEach(obj, function (i) {\n\tbreak;\n})");
    }

    @Override
    @Test
    public void testVisitForEach_1a()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for each(var i:int in obj)  break; ", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("goog.array.forEach(obj, function (i) {\n\tbreak;\n})");
    }

    //----------------------------------
    // try {} catch () {} finally {}
    //----------------------------------

    @Override
    @Test
    public void testVisitTry_Catch()
    {
        ITryNode node = (ITryNode) getNode("try { a; } catch (e:Error) { b; }",
                ITryNode.class);
        asBlockWalker.visitTry(node);
        assertOut("try {\n\ta;\n} catch (e) {\n\tb;\n}");
    }

    @Override
    @Test
    public void testVisitTry_Catch_Finally()
    {
        ITryNode node = (ITryNode) getNode(
                "try { a; } catch (e:Error) { b; } finally { c; }",
                ITryNode.class);
        asBlockWalker.visitTry(node);
        assertOut("try {\n\ta;\n} catch (e) {\n\tb;\n} finally {\n\tc;\n}");
    }

    @Override
    @Test
    public void testVisitTry_Catch_Catch_Finally()
    {
        // TODO (erikdebruin) handle multiple 'catch' statements (FW in Wiki)
        ITryNode node = (ITryNode) getNode(
                "try { a; } catch (e:Error) { b; } catch (f:Error) { c; } finally { d; }",
                ITryNode.class);
        asBlockWalker.visitTry(node);
        assertOut("try {\n\ta;\n} catch (e) {\n\tb;\n} catch (f) {\n\tc;\n} finally {\n\td;\n}");
    }

    @Override
    @Test
    public void testVisitTry_CatchEmpty_FinallyEmpty_()
    {
        ITryNode node = (ITryNode) getNode(
                "try { a; } catch (e:Error) {  } finally {  }", ITryNode.class);
        asBlockWalker.visitTry(node);
        assertOut("try {\n\ta;\n} catch (e) {\n} finally {\n}");
    }

    //----------------------------------
    // switch {}
    //----------------------------------

    @Test
    public void testVisitSwitch_3()
    {
        ISwitchNode node = (ISwitchNode) getNode(
                "switch(i){case 1: { var x:int = 42; break; }; case 2: { var y:int = 66; break; }}", ISwitchNode.class);
        asBlockWalker.visitSwitch(node);
        assertOut("switch (i) {\n\tcase 1:\n\t\tvar /** @type {number} */ x = 42;\n\t\tbreak;\n\tcase 2:\n\t\tvar /** @type {number} */ y = 66;\n\t\tbreak;\n}");
    }

    //----------------------------------
    // label : for () {}
    //----------------------------------

    @Override
    @Test
    public void testVisitLabel_1()
    {
        LabeledStatementNode node = (LabeledStatementNode) getNode(
                "foo: for each(var i:int in obj) { break foo; }",
                LabeledStatementNode.class);
        asBlockWalker.visitLabeledStatement(node);
        assertOut("foo : goog.array.forEach(obj, function (i) {\n\tbreak foo;\n})");
    }

    @Override
    @Test
    public void testVisitLabel_1a()
    {
        LabeledStatementNode node = (LabeledStatementNode) getNode(
                "foo: for each(var i:int in obj) break foo;",
                LabeledStatementNode.class);
        asBlockWalker.visitLabeledStatement(node);
        assertOut("foo : goog.array.forEach(obj, function (i) {\n\tbreak foo;\n})");
    }

    //----------------------------------
    // all together now!
    //----------------------------------

    @Override
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
                        + " } finally { "
                        + "  d;  var a:Object = function(foo:int, bar:String = 'goo'):int{return -1;};"
                        + "  eee.dd; eee.dd; eee.dd; eee.dd;} }"
                        + "foo: for each(var i:int in obj) break foo;",
                IFileNode.class);
        asBlockWalker.visitFile(node);
        assertOut("goog.provide('RoyaleTest_A');\n\n/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\nRoyaleTest_A.prototype.royaleTest_a = function() {\n\tvar self = this;\n\ttry {\n\t\ta;\n\t} catch (e) {\n\t\tif (a) {\n\t\t\tif (b) {\n\t\t\t\tif (c)\n\t\t\t\t\tb;\n\t\t\t\telse if (f)\n\t\t\t\t\ta;\n\t\t\t\telse\n\t\t\t\t\te;\n\t\t\t}\n\t\t}\n\t} finally {\n\t}\n\tif (d)\n\t\tfor (var /** @type {number} */ i = 0; i < len; i++)\n\t\t\tbreak;\n\tif (a) {\n\t\twith (ab) {\n\t\t\tc();\n\t\t}\n\t\tdo {\n\t\t\ta++;\n\t\t\tdo\n\t\t\t\ta++;\n\t\t\twhile (a > b);\n\t\t} while (c > d);\n\t}\n\tif (b) {\n\t\ttry {\n\t\t\ta;\n\t\t\tthrow new Error('foo');\n\t\t} catch (e) {\n\t\t\tswitch (i) {\n\t\t\t\tcase 1:\n\t\t\t\t\tbreak;\n\t\t\t\tdefault:\n\t\t\t\t\treturn;\n\t\t\t}\n\t\t} finally {\n\t\t\td;\n\t\t\tvar /** @type {Object} */ a = function(foo, bar) {\n\t\t\t\tbar = typeof bar !== 'undefined' ? bar : 'goo';\n\t\t\t\treturn -1;\n\t\t\t};\n\t\t\teee.dd;\n\t\t\teee.dd;\n\t\t\teee.dd;\n\t\t\teee.dd;\n\t\t}\n\t}\n\tfoo : goog.array.forEach(obj, function (i) {\n\t\tbreak foo;\n\t});\n};");
    }

    @Test
    public void testVisitIf_NoClauses()
    {
        IIfNode node = (IIfNode) getNode(
                "if (a) ;", IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a)\n{}\n");
    }


    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }

}
