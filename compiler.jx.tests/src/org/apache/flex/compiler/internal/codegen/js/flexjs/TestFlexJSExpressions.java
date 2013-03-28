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
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogExpressions;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSExpressions extends TestGoogExpressions
{

    @Override
    @Test
    public void testVisitLanguageIdentifierNode_This()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) this.a;", IMemberAccessExpressionNode.class);
        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("a");
    }

    @Override
    @Test
    public void testVisitLanguageIdentifierNode_This1()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) this.a;", IMemberAccessExpressionNode.class);

        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("a");
    }

    @Override
    @Test
    public void testVisitLanguageIdentifierNode_This2()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) this.a;", IMemberAccessExpressionNode.class);

        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("a");
    }
    
    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_1()
    {
        IFunctionNode node = getMethod("function foo(){if (a) super.foo();}");
        asBlockWalker.visitFunction(node);
        assertOut("A.prototype.foo = function() {\n\tvar self = this;\n\tif (a)\n\t\tgoog.base(this, 'foo');\n}");
    }

    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_2()
    {
        IFunctionNode node = getMethod("function foo(){if (a) super.foo(a, b, c);}");
        asBlockWalker.visitFunction(node);
        assertOut("A.prototype.foo = function() {\n\tvar self = this;\n\tif (a)\n\t\tgoog.base(this, 'foo', a, b, c);\n}");
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
    public void testClassCast()
    {
        IFunctionNode node = getMethod("function foo(){A(b).text = '';}");
        asBlockWalker.visitFunction(node);
        assertOut("A.prototype.foo = function() {\n\tvar self = this;\n\tb /** Cast to A */.text = '';\n}");
    }
    
    @Test
    public void testFunctionCall()
    {
        IFunctionNode node = getMethod("function foo(){bar(b).text = '';}");
        asBlockWalker.visitFunction(node);
        assertOut("A.prototype.foo = function() {\n\tvar self = this;\n\tbar(b).text = '';\n}");
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
    public void testVisitBinaryOperator_Is()
    {
        IBinaryOperatorNode node = getBinaryNode("a is b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("is(a, b)");
    }

    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
