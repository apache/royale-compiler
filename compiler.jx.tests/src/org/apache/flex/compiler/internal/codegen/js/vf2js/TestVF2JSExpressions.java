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

package org.apache.flex.compiler.internal.codegen.js.vf2js;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogExpressions;
import org.apache.flex.compiler.internal.driver.js.vf2js.VF2JSBackend;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestVF2JSExpressions extends TestGoogExpressions
{

    @Ignore
    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMember()
    {
        // (erikdebruin) this test doesn't make sense in VF2JS context
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) super.foo;", IMemberAccessExpressionNode.class);
        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("super.foo");
    }

    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_1()
    {
        IFunctionNode node = getMethod("function foo(){if (a) super.foo();}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  if (a)\n    FalconTest_A.base(this, 'foo');\n}");
    }

    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_2()
    {
        IFunctionNode node = getMethod("function foo(){if (a) super.foo(a, b, c);}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  if (a)\n    FalconTest_A.base(this, 'foo', a, b, c);\n}");
    }

    @Override
    @Test
    public void testAnonymousFunctionWithParamsReturn()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a:Object = function(foo:int, bar:String = 'goo'):int{return -1;};",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Object} */ a = function(foo, bar) {\n  bar = typeof bar !== 'undefined' ? bar : 'goo';\n  return -1;\n}");
    }

    @Override
    @Test
    public void testAnonymousFunctionAsArgument()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode(
                "addListener('foo', function(event:Object):void{doit();})",
                IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("addListener('foo', function(event) {\n  doit();\n})");
    }

    @Override
    @Test
    public void testVisitAs()
    {
        IBinaryOperatorNode node = getBinaryNode("a as b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("org.apache.flex.utils.Language.as(a, b)");
    }

    @Test
    public void testVisitAs2()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b(o:Object):int { var a:B; a = o as B; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @expose\n * @param {Object} o\n * @return {number}\n */\nfoo.bar.B.prototype.b = function(o) {\n  var /** @type {foo.bar.B} */ a;\n  a = org.apache.flex.utils.Language.as(o, foo.bar.B);\n}");
    }

    @Override
    @Test
    public void testVisitBinaryOperator_Is()
    {
        IBinaryOperatorNode node = getBinaryNode("a is b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("org.apache.flex.utils.Language.is(a, b)");
    }

    protected IBackend createBackend()
    {
        return new VF2JSBackend();
    }

}
