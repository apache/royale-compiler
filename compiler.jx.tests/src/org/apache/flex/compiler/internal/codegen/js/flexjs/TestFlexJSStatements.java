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
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogStatements;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IIfNode;
import org.apache.flex.compiler.tree.as.ISwitchNode;
import org.apache.flex.compiler.tree.as.ITryNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.tree.as.IWhileLoopNode;
import org.apache.flex.compiler.tree.as.IWithNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSStatements extends TestGoogStatements
{
    @Override
    public void setUp()
    {
    	project = new FlexJSProject(workspace);
        super.setUp();
    }
    
    @Test
    public void testVarDeclaration_withTypeAssignedStringWithNewLine()
    {
        IVariableNode node = (IVariableNode) getNode("var a:String = \"\\n\"",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = \"\\n\"");
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
        assertOut("\n/**\n * @const\n * @type {*}\n */\nvar a = 42");
    }

    @Override
    @Test
    public void testConstDeclaration_withType()
    {
        IVariableNode node = (IVariableNode) getNode("const a:int = 42;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("\n/**\n * @const\n * @type {number}\n */\nvar a = 42");
    }

    @Override
    @Test
    public void testConstDeclaration_withList()
    {
        IVariableNode node = (IVariableNode) getNode(
                "const a:int = 4, b:int = 11, c:int = 42;", IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("\n/**\n * @const\n * @type {number}\n */\nvar a = 4, \n/**\n * @const\n * @type {number}\n */\nb = 11, \n/**\n * @const\n * @type {number}\n */\nc = 42");
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
        assertOut("for (var /** @type {number} */ i = 0; i < len; i++) {\n  break;\n}");
    }

    @Override
    @Test
    public void testVisitFor_1b()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int = 0; i < len; i++) break;", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {number} */ i = 0; i < len; i++)\n  break;");
    }

    @Override
    @Test
    public void testVisitForIn_1()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int in obj) { break; }", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {number} */ i in obj) {\n  break;\n}");
    }

    @Override
    @Test
    public void testVisitForIn_1a()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int in obj)  break; ", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {number} */ i in obj)\n  break;");
    }

    @Override
    @Test
    public void testVisitForEach_1()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for each(var i:int in obj) { break; }", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("var foreachiter0_target = obj;\nfor (var foreachiter0 in foreachiter0_target) \n{\nvar i = foreachiter0_target[foreachiter0];\n{\n  break;\n}}\n");
    }

    @Override
    @Test
    public void testVisitForEach_1a()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for each(var i:int in obj)  break; ", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("var foreachiter0_target = obj;\nfor (var foreachiter0 in foreachiter0_target) \n{\nvar i = foreachiter0_target[foreachiter0];\n\n  break;}\n");
    }

    @Test
    public void testVisitForEach_2()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for each(var i:int in obj.foo()) { break; }", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("var foreachiter0_target = obj.foo();\nfor (var foreachiter0 in foreachiter0_target) \n{\nvar i = foreachiter0_target[foreachiter0];\n{\n  break;\n}}\n");
    }

    @Test
    public void testVisitForEach_HoistedVar()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "var i:int; for each(i in obj)  break; ", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("var foreachiter0_target = obj;\nfor (var foreachiter0 in foreachiter0_target) \n{\ni = foreachiter0_target[foreachiter0];\n\n  break;}\n");
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
        assertOut("try {\n  a;\n} catch (e) {\n  b;\n}");
    }

    @Override
    @Test
    public void testVisitTry_Catch_Finally()
    {
        ITryNode node = (ITryNode) getNode(
                "try { a; } catch (e:Error) { b; } finally { c; }",
                ITryNode.class);
        asBlockWalker.visitTry(node);
        assertOut("try {\n  a;\n} catch (e) {\n  b;\n} finally {\n  c;\n}");
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
        assertOut("try {\n  a;\n} catch (e) {\n  b;\n} catch (f) {\n  c;\n} finally {\n  d;\n}");
    }

    @Override
    @Test
    public void testVisitTry_CatchEmpty_FinallyEmpty_()
    {
        ITryNode node = (ITryNode) getNode(
                "try { a; } catch (e:Error) {  } finally {  }", ITryNode.class);
        asBlockWalker.visitTry(node);
        assertOut("try {\n  a;\n} catch (e) {\n} finally {\n}");
    }

    //----------------------------------
    // switch {}
    //----------------------------------

    @Override
    @Test
    public void testVisitSwitch_1()
    {
        ISwitchNode node = (ISwitchNode) getNode("switch(i){case 1: break;}",
                ISwitchNode.class);
        asBlockWalker.visitSwitch(node);
        assertOut("switch (i) {\n  case 1:\n    break;\n}");
    }

    @Override
    @Test
    public void testVisitSwitch_1a()
    {
        ISwitchNode node = (ISwitchNode) getNode(
                "switch(i){case 1: { break; }}", ISwitchNode.class);
        asBlockWalker.visitSwitch(node);
        // (erikdebruin) the code is valid without the extra braces, 
        //               i.e. we're good, we "don't care"
        assertOut("switch (i) {\n  case 1:\n    break;\n}");
    }

    @Override
    @Test
    public void testVisitSwitch_2()
    {
        ISwitchNode node = (ISwitchNode) getNode(
                "switch(i){case 1: break; default: return;}", ISwitchNode.class);
        asBlockWalker.visitSwitch(node);
        assertOut("switch (i) {\n  case 1:\n    break;\n  default:\n    return;\n}");
    }

    @Override
    @Test
    public void testVisitSwitch_3()
    {
        ISwitchNode node = (ISwitchNode) getNode(
                "switch(i){case 1: { var x:int = 42; break; }; case 2: { var y:int = 66; break; }}", ISwitchNode.class);
        asBlockWalker.visitSwitch(node);
        assertOut("switch (i) {\n  case 1:\n    var /** @type {number} */ x = 42;\n    break;\n  case 2:\n    var /** @type {number} */ y = 66;\n    break;\n}");
    }

    //----------------------------------
    // if ()
    //----------------------------------

    @Override
    @Test
    public void testVisitIf_1()
    {
        IIfNode node = (IIfNode) getNode("if (a) b++;", IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a)\n  b++;");
    }

    @Override
    @Test
    public void testVisitIf_2()
    {
        IIfNode node = (IIfNode) getNode("if (a) b++; else c++;", IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a)\n  b++;\nelse\n  c++;");
    }

    @Override
    @Test
    public void testVisitIf_4()
    {
        IIfNode node = (IIfNode) getNode(
                "if (a) b++; else if (c) d++; else if(e) --f;", IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a)\n  b++;\nelse if (c)\n  d++;\nelse if (e)\n  --f;");
    }

    //----------------------------------
    // if () { }
    //----------------------------------

    @Override
    @Test
    public void testVisitIf_1a()
    {
        IIfNode node = (IIfNode) getNode("if (a) { b++; }", IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a) {\n  b++;\n}");
    }

    @Override
    @Test
    public void testVisitIf_1b()
    {
        IIfNode node = (IIfNode) getNode("if (a) { b++; } else { c++; }",
                IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a) {\n  b++;\n} else {\n  c++;\n}");
    }

    @Override
    @Test
    public void testVisitIf_1c()
    {
        IIfNode node = (IIfNode) getNode(
                "if (a) { b++; } else if (b) { c++; } else { d++; }",
                IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a) {\n  b++;\n} else if (b) {\n  c++;\n} else {\n  d++;\n}");
    }

    @Override
    @Test
    public void testVisitIf_3()
    {
        IIfNode node = (IIfNode) getNode(
                "if (a) b++; else if (c) d++; else --e;", IIfNode.class);
        asBlockWalker.visitIf(node);
        assertOut("if (a)\n  b++;\nelse if (c)\n  d++;\nelse\n  --e;");
    }

    //----------------------------------
    // label : for () {}
    //----------------------------------

    @Override
    @Test
    public void testVisitFor_2()
    {
        IForLoopNode node = (IForLoopNode) getNode("for (;;) { break; }",
                IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (;;) {\n  break;\n}");
    }
    
    //----------------------------------
    // while () { }
    //----------------------------------

    @Override
    @Test
    public void testVisitWhileLoop_1()
    {
        IWhileLoopNode node = (IWhileLoopNode) getNode(
                "while(a > b){a++;--b;}", IWhileLoopNode.class);
        asBlockWalker.visitWhileLoop(node);
        assertOut("while (a > b) {\n  a++;\n  --b;\n}");
    }

    @Override
    @Test
    public void testVisitWhileLoop_1a()
    {
        IWhileLoopNode node = (IWhileLoopNode) getNode("while(a > b) a++;",
                IWhileLoopNode.class);
        asBlockWalker.visitWhileLoop(node);
        assertOut("while (a > b)\n  a++;");
    }

    //----------------------------------
    // do {} while ()
    //----------------------------------

    @Override
    @Test
    public void testVisitWhileLoop_Do_1()
    {
        IWhileLoopNode node = (IWhileLoopNode) getNode(
                "do {a++;--b;} while(a > b);", IWhileLoopNode.class);
        asBlockWalker.visitWhileLoop(node);
        assertOut("do {\n  a++;\n  --b;\n} while (a > b);");
    }

    @Override
    @Test
    public void testVisitWhileLoop_Do_1a()
    {
        IWhileLoopNode node = (IWhileLoopNode) getNode("do a++; while(a > b);",
                IWhileLoopNode.class);
        asBlockWalker.visitWhileLoop(node);
        assertOut("do\n  a++;\nwhile (a > b);");
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
        assertOut("var foreachiter0_target = obj;\nfoo : for (var foreachiter0 in foreachiter0_target) \n{\nvar i = foreachiter0_target[foreachiter0];\n{\n  break foo;\n}}\n");
    }

    @Override
    @Test
    public void testVisitLabel_1a()
    {
        // TODO (mschmalle) LabelStatement messes up in finally{} block, something is wrong there
        LabeledStatementNode node = (LabeledStatementNode) getNode(
                "foo: for each(var i:int in obj) break foo;",
                LabeledStatementNode.class);
        asBlockWalker.visitLabeledStatement(node);
        assertOut("var foreachiter0_target = obj;\nfoo : for (var foreachiter0 in foreachiter0_target) \n{\nvar i = foreachiter0_target[foreachiter0];\n\n  break foo;}\n");
    }

    //----------------------------------
    // with () {}
    //----------------------------------

    @Test
    public void testVisitWith()
    {
        IWithNode node = (IWithNode) getNode("with (a) { b; }", IWithNode.class);
        asBlockWalker.visitWith(node);
        assertOut("with (a) {\n  b;\n}");
    }

    @Test
    public void testVisitWith_1a()
    {
        IWithNode node = (IWithNode) getNode("with (a) b;", IWithNode.class);
        asBlockWalker.visitWith(node);
        assertOut("with (a)\n  b;");
    }

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
        assertOutWithMetadata("/**\n" +
        		              " * FalconTest_A\n" +
        		              " *\n" +
        		              " * @fileoverview\n" +
        		              " *\n" +
        		              " * @suppress {checkTypes|accessControls}\n" +
        		              " */\n" +
        		              "\n" +
        		              "goog.provide('FalconTest_A');\n" +
        		              "\n\n\n" +
        		              "/**\n" +
        		              " * @constructor\n" +
        		              " */\n" +
        		              "FalconTest_A = function() {\n" +
        		              "};\n\n\n" +
        		              "FalconTest_A.prototype.falconTest_a = function() {\n" +
        		              "  var self = this;\n" +
        		              "  var /** @type {Function} */ __localFn0__ = function(foo, bar) {\n" +
            		          "    bar = typeof bar !== 'undefined' ? bar : 'goo';\n" +
            		          "    return -1;\n" +
            		          "  }\n" +
            		          "  try {\n" +
        		              "    a;\n" +
        		              "  } catch (e) {\n" +
        		              "    if (a) {\n" +
        		              "      if (b) {\n" +
        		              "        if (c)\n" +
        		              "          b;\n" +
        		              "        else if (f)\n" +
        		              "          a;\n" +
        		              "        else\n" +
        		              "          e;\n" +
        		              "      }\n" +
        		              "    }\n" +
        		              "  } finally {\n" +
        		              "  }\n" +
        		              "  if (d)\n" +
        		              "    for (var /** @type {number} */ i = 0; i < len; i++)\n" +
        		              "      break;\n" +
        		              "  if (a) {\n" +
        		              "    with (ab) {\n" +
        		              "      c();\n" +
        		              "    }\n" +
        		              "    do {\n" +
        		              "      a++;\n" +
        		              "      do\n" +
        		              "        a++;\n" +
        		              "      while (a > b);\n" +
        		              "    } while (c > d);\n" +
        		              "  }\n" +
        		              "  if (b) {\n" +
        		              "    try {\n" +
        		              "      a;\n" +
        		              "      throw new Error('foo');\n" +
        		              "    } catch (e) {\n" +
        		              "      switch (i) {\n" +
        		              "        case 1:\n" +
        		              "          break;\n" +
        		              "        default:\n" +
        		              "          return;\n" +
        		              "      }\n" +
        		              "    } finally {\n" +
        		              "      d;\n" +
        		              "      var /** @type {Object} */ a = __localFn0__;\n" +
        		              "      eee.dd;\n" +
        		              "      eee.dd;\n" +
        		              "      eee.dd;\n" +
        		              "      eee.dd;\n" +
        		              "    }\n" +
        		              "  }\n" +
        		              "  var foreachiter0_target = obj;\n" +
        		              "  foo : for (var foreachiter0 in foreachiter0_target) \n" +
        		              "  {\n" +
        		              "  var i = foreachiter0_target[foreachiter0];\n" +
        		              "  \n" +
        		              "    break foo;}\n" +
        		              "  ;\n};\n\n\n" +
        		              "/**\n * Metadata\n" +
        		              " *\n" +
        		              " * @type {Object.<string, Array.<Object>>}\n" +
        		              " */\n" +
        		              "FalconTest_A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'FalconTest_A', qName: 'FalconTest_A'}] };\n");
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
