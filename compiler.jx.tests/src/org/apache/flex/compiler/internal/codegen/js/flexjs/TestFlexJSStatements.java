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
import org.apache.flex.compiler.tree.as.IVariableNode;
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

    @Override
    @Test
    public void testVisitForEach_1()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for each(var i:int in obj) { break; }", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var foreachiter0 in obj) \n{\nvar i = obj[foreachiter0];\n{\n\tbreak;\n}}\n");
    }

    @Override
    @Test
    public void testVisitForEach_1a()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "for each(var i:int in obj)  break; ", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var foreachiter0 in obj) \n{\nvar i = obj[foreachiter0];\n\n\tbreak;}\n");
    }

    @Test
    public void testVisitForEach_HoistedVar()
    {
        IForLoopNode node = (IForLoopNode) getNode(
                "var i:int; for each(i in obj)  break; ", IForLoopNode.class);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var foreachiter0 in obj) \n{\ni = obj[foreachiter0];\n\n\tbreak;}\n");
    }

    @Override
    @Test
    public void testVisitLabel_1()
    {
        LabeledStatementNode node = (LabeledStatementNode) getNode(
                "foo: for each(var i:int in obj) { break foo; }",
                LabeledStatementNode.class);
        asBlockWalker.visitLabeledStatement(node);
        assertOut("foo : for (var foreachiter0 in obj) \n{\nvar i = obj[foreachiter0];\n{\n\tbreak foo;\n}}\n");
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
        assertOut("foo : for (var foreachiter0 in obj) \n{\nvar i = obj[foreachiter0];\n\n\tbreak foo;}\n");
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
        assertOut("goog.provide('FalconTest_A');\n\n/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\nFalconTest_A.prototype.falconTest_a = function() {\n\ttry {\n\t\ta;\n\t} catch (e) {\n\t\tif (a) {\n\t\t\tif (b) {\n\t\t\t\tif (c)\n\t\t\t\t\tb;\n\t\t\t\telse if (f)\n\t\t\t\t\ta;\n\t\t\t\telse\n\t\t\t\t\te;\n\t\t\t}\n\t\t}\n\t} finally {\n\t}\n\tif (d)\n\t\tfor (var /** @type {number} */ i = 0; i < len; i++)\n\t\t\tbreak;\n\tif (a) {\n\t\twith (ab) {\n\t\t\tc();\n\t\t}\n\t\tdo {\n\t\t\ta++;\n\t\t\tdo\n\t\t\t\ta++;\n\t\t\twhile (a > b);\n\t\t} while (c > d);\n\t}\n\tif (b) {\n\t\ttry {\n\t\t\ta;\n\t\t\tthrow new Error('foo');\n\t\t} catch (e) {\n\t\t\tswitch (i) {\n\t\t\t\tcase 1:\n\t\t\t\t\tbreak;\n\t\t\t\tdefault:\n\t\t\t\t\treturn;\n\t\t\t}\n\t\t} finally {\n\t\t\td;\n\t\t\tvar /** @type {Object} */ a = function(foo, bar) {\n\t\t\t\tbar = typeof bar !== 'undefined' ? bar : 'goo';\n\t\t\t\treturn -1;\n\t\t\t};\n\t\t\teee.dd;\n\t\t\teee.dd;\n\t\t\teee.dd;\n\t\t\teee.dd;\n\t\t}\n\t}\n\tfoo : for (var foreachiter0 in obj) \n\t{\n\tvar i = obj[foreachiter0];\n\t\n\t\tbreak foo;}\n\t;\n};");
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
