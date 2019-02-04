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
import org.apache.royale.compiler.internal.codegen.as.TestExpressions;
import org.apache.royale.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.INamespaceAccessExpressionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

/**
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class TestGoogExpressions extends TestExpressions
{
    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_1()
    {
        IFunctionNode node = getMethod("function foo(){if (a) super.foo();}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n\tvar self = this;\n\tif (a)\n\t\tRoyaleTest_A.base(this, 'foo');\n}");
    }

    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_2()
    {
        IFunctionNode node = getMethod("function foo(){if (a) super.foo(a, b, c);}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n\tvar self = this;\n\tif (a)\n\t\tRoyaleTest_A.base(this, 'foo', a, b, c);\n}");
    }

    //----------------------------------
    // Primary expression keywords
    //----------------------------------

    //----------------------------------
    // Logical
    //----------------------------------

    @Override
    @Test
    public void testVisitBinaryOperatorNode_LogicalAndAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a &&= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a = a && b");
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalAndAssignmentInClass()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode)getNode("public var foo:Boolean;private function test(target:RoyaleTest_A):void { target.foo &&= foo }", IBinaryOperatorNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitBinaryOperator(node);
        // the last foo should probably be this.foo
        assertOut("target.foo = target.foo && foo");
    }

    @Override
    @Test
    public void testVisitBinaryOperatorNode_LogicalOrAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a ||= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a = a || b");
    }

    //----------------------------------
    // Other
    //----------------------------------

    @Test
    public void testParentheses_1()
    {
        IVariableNode node = (IVariableNode) getNode("var a = (a + b);",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {*} */ a = (a + b)");
    }

    @Test
    public void testParentheses_2()
    {
        IVariableNode node = (IVariableNode) getNode("var a = (a + b) - c;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {*} */ a = (a + b) - c");
    }

    @Test
    public void testParentheses_3()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a = ((a + b) - (c + d)) * e;", IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {*} */ a = ((a + b) - (c + d)) * e");
    }

    @Override
    @Test
    public void testAnonymousFunction()
    {
        IVariableNode node = (IVariableNode) getNode("var a = function(){};",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {*} */ a = function() {\n}");
    }

    @Override
    @Test
    public void testAnonymousFunctionWithParamsReturn()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a:Object = function(foo:int, bar:String = 'goo'):int{return -1;};",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Object} */ a = function(foo, bar) {\n\tbar = typeof bar !== 'undefined' ? bar : 'goo';\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testAnonymousFunctionAsArgument()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode(
                "addListener('foo', function(event:Object):void{doit();})",
                IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("addListener('foo', function(event) {\n\tdoit();\n})");
    }
    
    @Override
    @Test
    public void testVisitLocalNamedFunctionWithParamsReturn()
    {
        IFunctionNode node = (IFunctionNode) getLocalFunction("function a(foo:int, bar:String = 'goo'):int{return -1;};");
        asBlockWalker.visitFunction(node);
        assertOut("function a(foo, bar) {\n\tbar = typeof bar !== 'undefined' ? bar : 'goo';\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testVisitAs()
    {
        IBinaryOperatorNode node = getBinaryNode("a as b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("(is(a, b) ? a : null)");
    }

    @Override
    @Test
    public void testVisitBinaryOperator_Instancof()
    {
        IBinaryOperatorNode node = getBinaryNode("a instanceof b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a instanceof b");
    }

    @Override
    @Test
    public void testVisitBinaryOperator_Is()
    {
        IBinaryOperatorNode node = getBinaryNode("a is b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("is(a, b)");
    }

    @Override
    @Test
    public void testVisitBinaryOperator_NamespaceAccess_1()
    {
        INamespaceAccessExpressionNode node = getNamespaceAccessExpressionNode("a::b");
        asBlockWalker.visitNamespaceAccessExpression(node);
        assertOut("a.b");
    }

    @Override
    @Test
    public void testVisitBinaryOperator_NamespaceAccess_2()
    {
        INamespaceAccessExpressionNode node = getNamespaceAccessExpressionNode("a::b::c");
        asBlockWalker.visitNamespaceAccessExpression(node);
        assertOut("a.b.c");
    }

    protected IBackend createBackend()
    {
        return new GoogBackend();
    }

}
