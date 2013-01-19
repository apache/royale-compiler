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

package org.apache.flex.compiler.internal.as.codegen;

import org.apache.flex.compiler.internal.tree.as.ArrayLiteralNode;
import org.apache.flex.compiler.internal.tree.as.NamespaceAccessExpressionNode;
import org.apache.flex.compiler.internal.tree.as.ObjectLiteralNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IDynamicAccessNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IIfNode;
import org.apache.flex.compiler.tree.as.IIterationFlowNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IReturnNode;
import org.apache.flex.compiler.tree.as.ITernaryOperatorNode;
import org.apache.flex.compiler.tree.as.IUnaryOperatorNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Schmalle
 */
public class TestExpressions extends TestWalkerBase
{

    // ILanguageIdentifierNode -> IIdentifierNode

    @Test
    public void testVisitLanguageIdentifierNode_This()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) this.a;", IMemberAccessExpressionNode.class);
        visitor.visitMemberAccessExpression(node);
        assertOut("this.a");
    }

    @Test
    public void testVisitLanguageIdentifierNode_SuperMember()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) super.foo;", IMemberAccessExpressionNode.class);
        visitor.visitMemberAccessExpression(node);
        assertOut("super.foo");
    }

    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_1()
    {
        // NOTE: This is here as an example that a method call to super
        // is always held within a IFunctionCallNode, here it's a plain member access
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) super.foo();", IMemberAccessExpressionNode.class);
        visitor.visitMemberAccessExpression(node);
        assertOut("super.foo");
    }

    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_2()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode(
                "if (a) super.foo(a, b, c);", IFunctionCallNode.class);
        visitor.visitFunctionCall(node);
        assertOut("super.foo(a, b, c)");
    }

    @Test
    public void testVisitLanguageIdentifierNode_This1()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) this.a;", IMemberAccessExpressionNode.class);

        visitor.visitMemberAccessExpression(node);
        assertOut("this.a");
    }

    @Test
    public void testVisitLanguageIdentifierNode_This2()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) this.a;", IMemberAccessExpressionNode.class);

        visitor.visitMemberAccessExpression(node);
        assertOut("this.a");
    }

    //----------------------------------
    // Primary expression keywords
    //----------------------------------

    //----------------------------------
    // Arithmetic
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_Plus()
    {
        IBinaryOperatorNode node = getBinaryNode("a + b");
        visitor.visitBinaryOperator(node);
        assertOut("a + b");
    }

    @Test
    public void testVisitBinaryOperatorNode_Minus()
    {
        IBinaryOperatorNode node = getBinaryNode("a - b");
        visitor.visitBinaryOperator(node);
        assertOut("a - b");
    }

    @Test
    public void testVisitBinaryOperatorNode_Divide()
    {
        IBinaryOperatorNode node = getBinaryNode("a / b");
        visitor.visitBinaryOperator(node);
        assertOut("a / b");
    }

    @Test
    public void testVisitBinaryOperatorNode_Modulo()
    {
        IBinaryOperatorNode node = getBinaryNode("a % b");
        visitor.visitBinaryOperator(node);
        assertOut("a % b");
    }

    @Test
    public void testVisitBinaryOperatorNode_Multiply()
    {
        IBinaryOperatorNode node = getBinaryNode("a * b");
        visitor.visitBinaryOperator(node);
        assertOut("a * b");
    }

    @Test
    public void testVisitUnaryOperatorNode_PostIncrement()
    {
        IUnaryOperatorNode node = getUnaryNode("a++");
        visitor.visitUnaryOperator(node);
        assertOut("a++");
    }

    @Test
    public void testVisitUnaryOperatorNode_PreIncrement()
    {
        IUnaryOperatorNode node = getUnaryNode("++a");
        visitor.visitUnaryOperator(node);
        assertOut("++a");
    }

    @Test
    public void testVisitUnaryOperatorNode_PostDecrement()
    {
        IUnaryOperatorNode node = getUnaryNode("a--");
        visitor.visitUnaryOperator(node);
        assertOut("a--");
    }

    @Test
    public void testVisitUnaryOperatorNode_PreDecrement()
    {
        IUnaryOperatorNode node = getUnaryNode("--a");
        visitor.visitUnaryOperator(node);
        assertOut("--a");
    }

    //----------------------------------
    // Arithmetic compound assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_PlusAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a += b");
        visitor.visitBinaryOperator(node);
        assertOut("a += b");
    }

    @Test
    public void testVisitBinaryOperatorNode_MinusAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a -= b");
        visitor.visitBinaryOperator(node);
        assertOut("a -= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_DivideAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a /= b");
        visitor.visitBinaryOperator(node);
        assertOut("a /= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_ModuloAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a %= b");
        visitor.visitBinaryOperator(node);
        assertOut("a %= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_MultiplyAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a *= b");
        visitor.visitBinaryOperator(node);
        assertOut("a *= b");
    }

    //----------------------------------
    // Assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_Assignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a = b");
        visitor.visitBinaryOperator(node);
        assertOut("a = b");
    }

    //----------------------------------
    // Bitwise
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_BitwiseAnd()
    {
        IBinaryOperatorNode node = getBinaryNode("a & b");
        visitor.visitBinaryOperator(node);
        assertOut("a & b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseLeftShift()
    {
        IBinaryOperatorNode node = getBinaryNode("a << b");
        visitor.visitBinaryOperator(node);
        assertOut("a << b");
    }

    @Test
    public void testVisitUnaryOperatorNode_BitwiseNot()
    {
        IUnaryOperatorNode node = getUnaryNode("~a");
        visitor.visitUnaryOperator(node);
        assertOut("~a");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseOr()
    {
        IBinaryOperatorNode node = getBinaryNode("a | b");
        visitor.visitBinaryOperator(node);
        assertOut("a | b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseRightShift()
    {
        IBinaryOperatorNode node = getBinaryNode("a >> b");
        visitor.visitBinaryOperator(node);
        assertOut("a >> b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseUnsignedRightShift()
    {
        IBinaryOperatorNode node = getBinaryNode("a >>> b");
        visitor.visitBinaryOperator(node);
        assertOut("a >>> b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseXOR()
    {
        IBinaryOperatorNode node = getBinaryNode("a ^ b");
        visitor.visitBinaryOperator(node);
        assertOut("a ^ b");
    }

    //----------------------------------
    // Bitwise compound assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_BitwiseAndAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a &= b");
        visitor.visitBinaryOperator(node);
        assertOut("a &= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseLeftShiftAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a <<= b");
        visitor.visitBinaryOperator(node);
        assertOut("a <<= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseOrAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a |= b");
        visitor.visitBinaryOperator(node);
        assertOut("a |= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseRightShiftAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a >>= b");
        visitor.visitBinaryOperator(node);
        assertOut("a >>= b");
    }

    @Test
    public void
            testVisitBinaryOperatorNode_BitwiseUnsignedRightShiftAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a >>>= b");
        visitor.visitBinaryOperator(node);
        assertOut("a >>>= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseXORAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a ^= b");
        visitor.visitBinaryOperator(node);
        assertOut("a ^= b");
    }

    //----------------------------------
    // Comparison
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_Equal()
    {
        IBinaryOperatorNode node = getBinaryNode("a == b");
        visitor.visitBinaryOperator(node);
        assertOut("a == b");
    }

    @Test
    public void testVisitBinaryOperatorNode_GreaterThan()
    {
        IBinaryOperatorNode node = getBinaryNode("a > b");
        visitor.visitBinaryOperator(node);
        assertOut("a > b");
    }

    @Test
    public void testVisitBinaryOperatorNode_GreaterThanEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a >= b");
        visitor.visitBinaryOperator(node);
        assertOut("a >= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_NotEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a != b");
        visitor.visitBinaryOperator(node);
        assertOut("a != b");
    }

    @Test
    public void testVisitBinaryOperatorNode_LessThan()
    {
        IBinaryOperatorNode node = getBinaryNode("a < b");
        visitor.visitBinaryOperator(node);
        assertOut("a < b");
    }

    @Test
    public void testVisitBinaryOperatorNode_LessThanEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a <= b");
        visitor.visitBinaryOperator(node);
        assertOut("a <= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_StrictEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a === b");
        visitor.visitBinaryOperator(node);
        assertOut("a === b");
    }

    @Test
    public void testVisitBinaryOperatorNode_StrictNotEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a !== b");
        visitor.visitBinaryOperator(node);
        assertOut("a !== b");
    }

    //----------------------------------
    // Logical
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_LogicalAnd()
    {
        IBinaryOperatorNode node = getBinaryNode("a && b");
        visitor.visitBinaryOperator(node);
        assertOut("a && b");
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalAndAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a &&= b");
        visitor.visitBinaryOperator(node);
        assertOut("a &&= b");
    }

    @Test
    public void testVisitUnaryOperatorNode_LogicalNot()
    {
        IUnaryOperatorNode node = getUnaryNode("!a");
        visitor.visitUnaryOperator(node);
        assertOut("!a");
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalOr()
    {
        IBinaryOperatorNode node = getBinaryNode("a || b");
        visitor.visitBinaryOperator(node);
        assertOut("a || b");
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalOrAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a ||= b");
        visitor.visitBinaryOperator(node);
        assertOut("a ||= b");
    }

    //----------------------------------
    // Other
    //----------------------------------

    @Ignore
    @Test
    public void testParentheses_1()
    {
    	// TODO (mschmalle) why aren't parentheses preserved, various math 
    	//                  will come out wrong if they aren't?
        IVariableNode node = (IVariableNode) getNode("var a = (a + b);",
                IVariableNode.class);
        visitor.visitVariable(node);
        assertOut("var a = (a + b)");
    }

    @Ignore
    @Test
    public void testParentheses_2()
    {
        IVariableNode node = (IVariableNode) getNode("var a = (a + b) - c;",
                IVariableNode.class);
        visitor.visitVariable(node);
        assertOut("var a = (a + b) - c");
    }

    @Ignore
    @Test
    public void testParentheses_3()
    {
        IVariableNode node = (IVariableNode) getNode("var a = ((a + b) - (c + d)) * e;",
                IVariableNode.class);
        visitor.visitVariable(node);
        assertOut("var a = ((a + b) - (c + d)) * e");
    }

    @Test
    public void testAnonymousFunction()
    {
        IVariableNode node = (IVariableNode) getNode("var a = function(){};",
                IVariableNode.class);
        visitor.visitVariable(node);
        assertOut("var a:* = function() {\n}");
    }

    @Test
    public void testAnonymousFunctionWithParamsReturn()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a:Object = function(foo:int, bar:String = 'goo'):int{return -1;};",
                IVariableNode.class);
        visitor.visitVariable(node);
        assertOut("var a:Object = function(foo:int, bar:String = 'goo'):int {\n\treturn -1;\n}");
    }

    @Test
    public void testAnonymousFunctionAsArgument()
    {
        // TODO (mschmalle) using IIfNode in expressions test, any other way to do this without statement?
        IIfNode node = (IIfNode) getNode(
                "if (a) {addListener('foo', function(event:Object):void{doit();});}",
                IIfNode.class);
        visitor.visitIf(node);
        assertOut("if (a) {\n\taddListener('foo', function(event:Object):void {\n\t\tdoit();\n\t});\n}");
    }

    @Test
    public void testVisitDynamicAccessNode_1()
    {
        IDynamicAccessNode node = getDynamicAccessNode("a[b]");
        visitor.visitDynamicAccess(node);
        assertOut("a[b]");
    }

    @Test
    public void testVisitDynamicAccessNode_2()
    {
        IDynamicAccessNode node = getDynamicAccessNode("a[b[c][d]]");
        visitor.visitDynamicAccess(node);
        assertOut("a[b[c][d]]");
    }

    @Test
    public void testVisitAs()
    {
        IBinaryOperatorNode node = getBinaryNode("a as b");
        visitor.visitBinaryOperator(node);
        assertOut("a as b");
    }

    @Test
    public void testVisitBinaryOperatorNode_Comma()
    {
        IBinaryOperatorNode node = getBinaryNode("a, b");
        visitor.visitBinaryOperator(node);
        assertOut("a, b");
    }

    @Test
    public void testVisitTernaryOperatorNode()
    {
        ITernaryOperatorNode node = (ITernaryOperatorNode) getExpressionNode(
                "a ? b : c", ITernaryOperatorNode.class);
        visitor.visitTernaryOperator(node);
        assertOut("a ? b : c");
    }

    @Test
    public void testVisitUnaryOperator_Delete()
    {
        IUnaryOperatorNode node = getUnaryNode("delete a");
        visitor.visitUnaryOperator(node);
        assertOut("delete a");
    }

    @Test
    public void testVisitMemberAccess_1()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getExpressionNode(
                "a.b", IMemberAccessExpressionNode.class);
        visitor.visitMemberAccessExpression(node);
        assertOut("a.b");
    }

    @Test
    public void testVisitMemberAccess_2()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getExpressionNode(
                "a.b.c.d", IMemberAccessExpressionNode.class);
        visitor.visitMemberAccessExpression(node);
        assertOut("a.b.c.d");
    }

    @Test
    public void testVisitBinaryOperator_In()
    {
        IBinaryOperatorNode node = getBinaryNode("a in b");
        visitor.visitBinaryOperator(node);
        assertOut("a in b");
    }

    @Test
    public void testVisitBinaryOperator_Instancof()
    {
        IBinaryOperatorNode node = getBinaryNode("a instanceof b");
        visitor.visitBinaryOperator(node);
        assertOut("a instanceof b");
    }

    @Test
    public void testVisitBinaryOperator_Is()
    {
        IBinaryOperatorNode node = getBinaryNode("a is b");
        visitor.visitBinaryOperator(node);
        assertOut("a is b");
    }

    @Test
    public void testVisitBinaryOperator_NamespaceAccess_1()
    {
        // TODO this needs INamespaceAccessExpressionNode interface
        NamespaceAccessExpressionNode node = (NamespaceAccessExpressionNode) getExpressionNode(
                "a::b", NamespaceAccessExpressionNode.class);
        visitor.visitNamespaceAccessExpression(node);
        assertOut("a::b");
    }

    @Test
    public void testVisitBinaryOperator_NamespaceAccess_2()
    {
        // TODO this needs INamespaceAccessExpressionNode interface
        NamespaceAccessExpressionNode node = (NamespaceAccessExpressionNode) getExpressionNode(
                "a::b::c", NamespaceAccessExpressionNode.class);
        visitor.visitNamespaceAccessExpression(node);
        assertOut("a::b::c");
    }

    @Test
    public void testVisitBinaryOperator_New()
    {
        IFunctionCallNode node = (IFunctionCallNode) getExpressionNode(
                "new Object()", IFunctionCallNode.class);
        visitor.visitFunctionCall(node);
        assertOut("new Object()");
    }

    @Test
    public void testVisitObjectLiteral_1()
    {
        ObjectLiteralNode node = (ObjectLiteralNode) getExpressionNode(
                "a = {a:1}", ObjectLiteralNode.class);
        visitor.visitLiteral(node);
        assertOut("{a:1}");
    }

    @Test
    public void testVisitObjectLiteral_2()
    {
        ObjectLiteralNode node = (ObjectLiteralNode) getExpressionNode(
                "a = {a:1,b:{c:2,d:{e:4}}}", ObjectLiteralNode.class);
        visitor.visitLiteral(node);
        assertOut("{a:1,b:{c:2,d:{e:4}}}");
    }

    @Test
    public void testVisitArrayLiteral_1()
    {
        ArrayLiteralNode node = (ArrayLiteralNode) getExpressionNode(
                "a = [0,1,2]", ArrayLiteralNode.class);
        visitor.visitLiteral(node);
        assertOut("[0,1,2]");
    }

    @Test
    public void testVisitArrayLiteral_2()
    {
        ArrayLiteralNode node = (ArrayLiteralNode) getExpressionNode(
                "a = [0,[0,1,[0,1]],2,[1,2]]", ArrayLiteralNode.class);
        visitor.visitLiteral(node);
        assertOut("[0,[0,1,[0,1]],2,[1,2]]");
    }

    @Test
    public void testVisitUnaryOperatorNode_Typeof()
    {
        IUnaryOperatorNode node = getUnaryNode("typeof(a)");
        visitor.visitUnaryOperator(node);
        assertOut("typeof(a)");
    }

    @Ignore
    @Test
    public void testVisitUnaryOperatorNode_Typeof_NoParens()
    {
    	// TODO (mschmalle) the notation without parenthesis is 
    	//                  also valid in AS/JS
        IUnaryOperatorNode node = getUnaryNode("typeof a");
        visitor.visitUnaryOperator(node);
        assertOut("typeof a");
    }

    @Test
    public void testVisitUnaryOperatorNode_Void()
    {
        IUnaryOperatorNode node = getUnaryNode("void a");
        visitor.visitUnaryOperator(node);
        assertOut("void a");
    }

    @Test
    public void testVisitUnaryOperatorNode_Concate_1()
    {
        IBinaryOperatorNode node = getBinaryNode("\"a\" + \"b\"");
        visitor.visitBinaryOperator(node);
        assertOut("\"a\" + \"b\"");
    }

    @Ignore
    @Test
    public void testVisitUnaryOperatorNode_Concate_2()
    {
        IBinaryOperatorNode node = getBinaryNode("\"a\\\"\" + \"\\\"b\"");
        visitor.visitBinaryOperator(node);
        assertOut("\"a\\\"\" + \"\\\"b\"");
    }

    @Test
    public void testVisitIterationFlowNode_Break()
    {
        IIterationFlowNode node = (IIterationFlowNode) getNode("break",
                IIterationFlowNode.class);
        visitor.visitIterationFlow(node);
        assertOut("break");
    }

    @Test
    public void testVisitIterationFlowNode_Continue()
    {
        IIterationFlowNode node = (IIterationFlowNode) getNode("continue",
                IIterationFlowNode.class);
        visitor.visitIterationFlow(node);
        assertOut("continue");
    }

    @Test
    public void testVisitReturn()
    {
        IReturnNode node = (IReturnNode) getNode("return", IReturnNode.class);
        visitor.visitReturn(node);
        assertOut("return");
    }
}
