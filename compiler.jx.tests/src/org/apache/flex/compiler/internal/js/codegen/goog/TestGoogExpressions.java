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

package org.apache.flex.compiler.internal.js.codegen.goog;

import org.apache.flex.compiler.clients.IBackend;
import org.apache.flex.compiler.internal.as.codegen.TestExpressions;
import org.apache.flex.compiler.internal.js.driver.goog.GoogBackend;
import org.apache.flex.compiler.internal.tree.as.NamespaceAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IIfNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
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
        visitor.visitFunction(node);
        assertOut("A.prototype.foo = function() {\n\tif (a)\n\t\tgoog.base(this, 'foo');\n}");
    }

    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_2()
    {
        IFunctionNode node = getMethod("function foo(){if (a) super.foo(a, b, c);}");
        visitor.visitFunction(node);
        assertOut("A.prototype.foo = function() {\n\tif (a)\n\t\tgoog.base(this, 'foo', a, b, c);\n}");
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
        visitor.visitBinaryOperator(node);
        assertOut("a = a && b");
    }

    @Override
    @Test
    public void testVisitBinaryOperatorNode_LogicalOrAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a ||= b");
        visitor.visitBinaryOperator(node);
        assertOut("a = a || b");
    }

    //----------------------------------
    // Other
    //----------------------------------

    @Override
    @Test
    public void testAnonymousFunction()
    {
        IVariableNode node = (IVariableNode) getNode("var a = function(){};",
                IVariableNode.class);
        visitor.visitVariable(node);
        assertOut("var /** @type {*} */ a = function() {\n}");
    }

    @Override
    @Test
    public void testAnonymousFunctionWithParamsReturn()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a:Object = function(foo:int, bar:String = 'goo'):int{return -1;};",
                IVariableNode.class);
        visitor.visitVariable(node);
        assertOut("var /** @type {Object} */ a = function(foo, bar) {\n\tbar = typeof bar !== 'undefined' ? bar : 'goo';\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testAnonymousFunctionAsArgument()
    {
        // TODO (mschmalle) using IIfNode in expressions test, any other way to do this without statement?
        IIfNode node = (IIfNode) getNode(
                "if (a) {addListener('foo', function(event:Object):void{doit();});}",
                IIfNode.class);
        visitor.visitIf(node);
        assertOut("if (a) {\n\tthis.addListener('foo', function(event) {\n\t\tthis.doit();\n\t});\n}");
    }

    @Override
    @Test
    public void testVisitAs()
    {
    	// TODO (erikdebruin) the assert is a placeholder for the eventual workaround
        IBinaryOperatorNode node = getBinaryNode("a as b");
        visitor.visitBinaryOperator(node);
        assertOut("as(a, b)");
    }

    @Override
    @Test
    public void testVisitBinaryOperator_Instancof()
    {
    	// TODO (erikdebruin) check if the AS and JS implementations match
        IBinaryOperatorNode node = getBinaryNode("a instanceof b");
        visitor.visitBinaryOperator(node);
        assertOut("a instanceof b");
    }

    @Override
    @Test
    public void testVisitBinaryOperator_Is()
    {
    	// TODO (erikdebruin) the assert is a placeholder for the eventual workaround
        IBinaryOperatorNode node = getBinaryNode("a is b");
        visitor.visitBinaryOperator(node);
        assertOut("is(a, b)");
    }

    @Override
    @Test
    public void testVisitBinaryOperator_NamespaceAccess_1()
    {
        // TODO (mschmalle) this needs INamespaceAccessExpressionNode interface
    	// TODO (erikdebruin) we need a 'goog.require("a")' in the header
    	NamespaceAccessExpressionNode node = (NamespaceAccessExpressionNode) getExpressionNode(
                "a::b", NamespaceAccessExpressionNode.class);
        visitor.visitNamespaceAccessExpression(node);
        assertOut("a.b");
    }

    @Override
    @Test
    public void testVisitBinaryOperator_NamespaceAccess_2()
    {
        // TODO (mschmalle) this needs INamespaceAccessExpressionNode interface
    	// TODO (erikdebruin) we need a 'goog.require("a.b")' in the header
        NamespaceAccessExpressionNode node = (NamespaceAccessExpressionNode) getExpressionNode(
                "a::b::c", NamespaceAccessExpressionNode.class);
        visitor.visitNamespaceAccessExpression(node);
        assertOut("a.b.c");
    }

    protected IBackend createBackend()
    {
        return new GoogBackend();
    }

}
