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

package org.apache.royale.compiler.internal.codegen.js.royale;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.js.goog.TestGoogStatements;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IIfNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.INamespaceNode;
import org.apache.royale.compiler.tree.as.ISwitchNode;
import org.apache.royale.compiler.tree.as.ITryNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.tree.as.IWhileLoopNode;
import org.apache.royale.compiler.tree.as.IWithNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestRoyaleStatements extends TestGoogStatements
{
    @Override
    public void setUp()
    {
        backend = createBackend();
        project = new RoyaleJSProject(workspace, backend);
        project.config = new JSGoogConfiguration();
        super.setUp();
    }
    
    @Test
    public void testNamespaceStatement()
    {
        INamespaceNode node = (INamespaceNode) getNode("public namespace foo;",
        		INamespaceNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitNamespace(node);
        assertOut("foo = new Namespace('foo')");
    }
    
    @Test
    public void testNamespaceStatementWithURI()
    {
        INamespaceNode node = (INamespaceNode) getNode("public namespace foo = 'bar';",
        		INamespaceNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitNamespace(node);
        assertOut("foo = new Namespace('bar')");
    }
    
    @Test
    public void testObjectListeral_withPropertyNameMatchingConst()
    {
        ILiteralNode node = (ILiteralNode) getNode("static const myConst:int; function royaleTest_a():Object { return { myConst : myConst } }",
        		ILiteralNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitLiteral(node);
        assertOut("{myConst:RoyaleTest_A.myConst}");
    }
    
    @Test
    public void testVarDeclaration_withTypeAssignedStringWithNewLine()
    {
        IVariableNode node = (IVariableNode) getNode("var a:String = \"\\n\"",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = \"\\n\"");
    }

    @Override
    @Test
    public void testVarDeclaration_withType()
    {
        IFunctionNode node = (IFunctionNode) getNode("var a:int;",
            IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {number} */ a = 0;\n  //var /** @type {number} */ a = 0;\n}");
    }

    @Test
    public void testVarDeclaration_withTypeBooleanAndAssignedPositiveNumber()
    {
        IVariableNode node = (IVariableNode) getNode("var a:Boolean = 123.4;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = true");
    }

    @Test
    public void testVarDeclaration_withTypeBooleanAndAssignedNegativeNumber()
    {
        IVariableNode node = (IVariableNode) getNode("var a:Boolean = -123;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = true");
    }

    @Test
    public void testVarDeclaration_withTypeBooleanAndAssignedZeroNumber()
    {
        IVariableNode node = (IVariableNode) getNode("var a:Boolean = 0;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = false");
    }

    @Test
    public void testVarDeclaration_withTypeBooleanAndAssignedDecimalNumber()
    {
        IVariableNode node = (IVariableNode) getNode("var a:Boolean = 0.123;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = true");
    }

    @Test
    public void testVarDeclaration_withTypeBooleanAndAssignedNull()
    {
        IVariableNode node = (IVariableNode) getNode("var a:Boolean = null;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = false");
    }

    @Test
    public void testVarDeclaration_withTypeBooleanAndAssignedUndefined()
    {
        IVariableNode node = (IVariableNode) getNode("var a:Boolean = undefined;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = false");
    }

    @Test
    public void testVarDeclaration_withTypeIntAndAssignedHex()
    {
        IVariableNode node = (IVariableNode) getNode("var a:int = 0xabc;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 0xabc");
    }

    @Test
    public void testVarDeclaration_withTypeUintAndAssignedHex()
    {
        IVariableNode node = (IVariableNode) getNode("var a:uint = 0xabc;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 0xabc");
    }

    @Test
    public void testVarDeclaration_withTypeNumberAndAssignedHex()
    {
        IVariableNode node = (IVariableNode) getNode("var a:Number = 0xabc;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 0xabc");
    }

    @Test
    public void testVarDeclaration_withTypeIntAndAssignedNumber()
    {
        IVariableNode node = (IVariableNode) getNode("var a:int = 123.4;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 123");
    }

    @Test
    public void testVarDeclaration_withTypeUintAndAssignedNumber()
    {
        IVariableNode node = (IVariableNode) getNode("var a:uint = 123.4;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 123");
    }

    @Test
    public void testVarDeclaration_withTypeUintAndAssignedNegative()
    {
        IVariableNode node = (IVariableNode) getNode("var a:uint = -123;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 4294967173");
    }

    @Test
    public void testVarDeclaration_withTypeStringAndAssignedStringLiteral()
    {
        IVariableNode node = (IVariableNode) getNode("var a:String = \"hi\";",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = \"hi\"");
    }

    @Test
    public void testVarDeclaration_withTypeStringAndAssignedNull()
    {
        IVariableNode node = (IVariableNode) getNode("var a:String = null;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = null");
    }

    @Test
    public void testVarDeclaration_withTypeStringAndAssignedUndefined()
    {
        IVariableNode node = (IVariableNode) getNode("var a:String = undefined;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = null");
    }

    @Test
    public void testVarDeclaration_withTypeStringAndAssignedStringVar()
    {
        IVariableNode node = (IVariableNode) getNode("function royaleTest_a():Object { var a:String = b }var b:String;",
            IVariableNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = this.b");
    }

    @Test
    public void testVarDeclaration_withTypeStringAndAssignedAnyTypeVar()
    {
        IVariableNode node = (IVariableNode) getNode("function royaleTest_a():Object { var a:String = b }var b:*;",
            IVariableNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = org.apache.royale.utils.Language.string(this.b)");
    }

    @Test
    public void testVarDeclaration_withTypeStringAndAssignedToStringFunctionCall()
    {
        IVariableNode node = (IVariableNode) getNode("function royaleTest_a():Object { var a:String = b.toString(); }var b:Object;",
            IVariableNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = this.b.toString()");
    }

    @Test
    public void testVarDeclaration_withTypeNumberAndAssignedDateProperty()
    {
        IVariableNode node = (IVariableNode) getNode("function royaleTest_a():Object { var a:Number = b.fullYear; }var b:Date;",
            IVariableNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = this.b.getFullYear()");
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
                "var len:int;for (var i:int = 0; i < len; i++) { break; }",
                IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {number} */ i = 0; i < len; i++) {\n  break;\n}");
    }

    @Override
    @Test
    public void testVisitFor_1b()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "var len:int;for (var i:int = 0; i < len; i++) break;", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {number} */ i = 0; i < len; i++)\n  break;");
    }

    @Override
    @Test
    public void testVisitFor_1c()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for (var i:int = 0, len:int = 10; i < len; i++) break;", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {number} */ i = 0, /** @type {number} */ len = 10; i < len; i++)\n  break;");
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

    //----------------------------------
    // import a.b.C
    //----------------------------------

    @Test
    public void testVisitImport()
    {
        IImportNode node = (IImportNode) getNode("import custom.TestImplementation;", IImportNode.class);
        asBlockWalker.visitImport(node);
        assertOut("");
    }

    @Override
    @Test
    public void testVisit()
    {
        IFileNode node = (IFileNode) getNode(
                "try { a; } catch (e:Error) { if (a) { if (b) { if (c) b; else if (f) a; else e; }} } finally {  }"
                        + "if (d) { var len:int; for (var i:int = 0; i < len; i++) break; }"
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
        		              " * RoyaleTest_A\n" +
        		              " *\n" +
        		              " * @fileoverview\n" +
        		              " *\n" +
        		              " * @suppress {checkTypes|accessControls}\n" +
        		              " */\n" +
        		              "\n" +
        		              "goog.provide('RoyaleTest_A');\n" +
        		              "\n\n\n" +
        		              "/**\n" +
        		              " * @constructor\n" +
        		              " */\n" +
        		              "RoyaleTest_A = function() {\n" +
        		              "};\n\n\n" +
        		              "RoyaleTest_A.prototype.royaleTest_a = function() {\n" +
                              "  var self = this;\n" +
                              "  var /** @type {number} */ len = 0;\n" +
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
        		              "  if (d) {\n" +
        		              "    //var /** @type {number} */ len = 0;\n" +
        		              "    for (var /** @type {number} */ i = 0; i < len; i++)\n" +
        		              "      break;\n" +
        		              "  }\n" +
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
        		              "      var /** @type {Object} */ a = function(foo, bar) {\n" +
            		          "        bar = typeof bar !== 'undefined' ? bar : 'goo';\n" +
            		          "        return -1;\n" +
            		          "      };\n" +
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
        		              "RoyaleTest_A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'RoyaleTest_A', qName: 'RoyaleTest_A', kind: 'class' }] };\n" +
        		          		"\n" +
        		        		"\n" +
        		        		"\n" +
        		        		"/**\n" +
        		        		" * Reflection\n" +
        		        		" *\n" +
        		        		" * @return {Object.<string, Function>}\n" +
        		        		" */\n" +
        		        		"RoyaleTest_A.prototype.ROYALE_REFLECTION_INFO = function () {\n" +
        		        		"return {};\n" +
                                "};\n" +
                                "/**\n" +
                                " * @const\n" +
                                " * @type {number}\n" +
                                " */\n" +
                                "RoyaleTest_A.prototype.ROYALE_COMPILE_FLAGS = 9;\n");
    }

    @Override
    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }

}
