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
import org.apache.royale.compiler.internal.tree.as.ArrayLiteralNode;
import org.apache.royale.compiler.internal.tree.as.ObjectLiteralNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IDynamicAccessNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IIterationFlowNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.INamespaceAccessExpressionNode;
import org.apache.royale.compiler.tree.as.IReturnNode;
import org.apache.royale.compiler.tree.as.ITernaryOperatorNode;
import org.apache.royale.compiler.tree.as.IThrowNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

/**
 * @author Michael Schmalle
 */
public class TestExpressions extends ASTestBase
{

    @Test
    public void testVisitLanguageIdentifierNode_This()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) this.a;", IMemberAccessExpressionNode.class);
        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("this.a");
    }

    @Test
    public void testVisitLanguageIdentifierNode_SuperMember()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) super.foo;", IMemberAccessExpressionNode.class);
        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("super.foo");
    }

    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_1()
    {
        // NOTE: This is here as an example that a method call to super
        // is always held within a IFunctionCallNode, here it's a plain member access
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) super.foo();", IMemberAccessExpressionNode.class);
        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("super.foo");
    }

    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_2()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode(
                "if (a) super.foo(a, b, c);", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("super.foo(a, b, c)");
    }

    @Test
    public void testVisitLanguageIdentifierNode_This1()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) this.a;", IMemberAccessExpressionNode.class);

        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("this.a");
    }

    @Test
    public void testVisitLanguageIdentifierNode_This2()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) this.a;", IMemberAccessExpressionNode.class);

        asBlockWalker.visitMemberAccessExpression(node);
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
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a + b");
    }

    @Test
    public void testVisitBinaryOperatorNode_Minus()
    {
        IBinaryOperatorNode node = getBinaryNode("a - b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a - b");
    }

    @Test
    public void testVisitBinaryOperatorNode_Divide()
    {
        IBinaryOperatorNode node = getBinaryNode("a / b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a / b");
    }

    @Test
    public void testVisitBinaryOperatorNode_Modulo()
    {
        IBinaryOperatorNode node = getBinaryNode("a % b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a % b");
    }

    @Test
    public void testVisitBinaryOperatorNode_Multiply()
    {
        IBinaryOperatorNode node = getBinaryNode("a * b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a * b");
    }

    @Test
    public void testVisitUnaryOperatorNode_PostIncrement()
    {
        IUnaryOperatorNode node = getUnaryNode("a++");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("a++");
    }

    @Test
    public void testVisitUnaryOperatorNode_PreIncrement()
    {
        IUnaryOperatorNode node = getUnaryNode("++a");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("++a");
    }

    @Test
    public void testVisitUnaryOperatorNode_PostDecrement()
    {
        IUnaryOperatorNode node = getUnaryNode("a--");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("a--");
    }

    @Test
    public void testVisitUnaryOperatorNode_PreDecrement()
    {
        IUnaryOperatorNode node = getUnaryNode("--a");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("--a");
    }

    //----------------------------------
    // Arithmetic compound assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_PlusAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a += b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a += b");
    }

    @Test
    public void testVisitBinaryOperatorNode_MinusAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a -= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a -= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_DivideAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a /= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a /= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_ModuloAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a %= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a %= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_MultiplyAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a *= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a *= b");
    }

    //----------------------------------
    // Assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_Assignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a = b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a = b");
    }

    //----------------------------------
    // Bitwise
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_BitwiseAnd()
    {
        IBinaryOperatorNode node = getBinaryNode("a & b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a & b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseLeftShift()
    {
        IBinaryOperatorNode node = getBinaryNode("a << b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a << b");
    }

    @Test
    public void testVisitUnaryOperatorNode_BitwiseNot()
    {
        IUnaryOperatorNode node = getUnaryNode("~a");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("~a");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseOr()
    {
        IBinaryOperatorNode node = getBinaryNode("a | b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a | b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseRightShift()
    {
        IBinaryOperatorNode node = getBinaryNode("a >> b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a >> b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseUnsignedRightShift()
    {
        IBinaryOperatorNode node = getBinaryNode("a >>> b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a >>> b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseXOR()
    {
        IBinaryOperatorNode node = getBinaryNode("a ^ b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a ^ b");
    }

    //----------------------------------
    // Bitwise compound assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_BitwiseAndAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a &= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a &= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseLeftShiftAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a <<= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a <<= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseOrAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a |= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a |= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseRightShiftAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a >>= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a >>= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseUnsignedRightShiftAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a >>>= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a >>>= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseXORAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a ^= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a ^= b");
    }

    //----------------------------------
    // Comparison
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_Equal()
    {
        IBinaryOperatorNode node = getBinaryNode("a == b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a == b");
    }

    @Test
    public void testVisitBinaryOperatorNode_GreaterThan()
    {
        IBinaryOperatorNode node = getBinaryNode("a > b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a > b");
    }

    @Test
    public void testVisitBinaryOperatorNode_GreaterThanEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a >= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a >= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_NotEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a != b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a != b");
    }

    @Test
    public void testVisitBinaryOperatorNode_LessThan()
    {
        IBinaryOperatorNode node = getBinaryNode("a < b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a < b");
    }

    @Test
    public void testVisitBinaryOperatorNode_LessThanEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a <= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a <= b");
    }

    @Test
    public void testVisitBinaryOperatorNode_StrictEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a === b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a === b");
    }

    @Test
    public void testVisitBinaryOperatorNode_StrictNotEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a !== b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a !== b");
    }

    //----------------------------------
    // Logical
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_LogicalAnd()
    {
        IBinaryOperatorNode node = getBinaryNode("a && b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a && b");
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalAndAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a &&= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a &&= b");
    }

    @Test
    public void testVisitUnaryOperatorNode_LogicalNot()
    {
        IUnaryOperatorNode node = getUnaryNode("!a");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("!a");
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalOr()
    {
        IBinaryOperatorNode node = getBinaryNode("a || b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a || b");
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalOrAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a ||= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a ||= b");
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
        assertOut("var a:* = (a + b)");
    }

    @Test
    public void testParentheses_2()
    {
        IVariableNode node = (IVariableNode) getNode("var a = (a + b) - c;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var a:* = (a + b) - c");
    }

    @Test
    public void testParentheses_3()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a = ((a + b) - (c + d)) * e;", IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var a:* = ((a + b) - (c + d)) * e");
    }

    @Test
    public void testAnonymousFunction()
    {
        IVariableNode node = (IVariableNode) getNode("var a = function(){};",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var a:* = function() {\n}");
    }

    @Test
    public void testAnonymousFunctionWithParamsReturn()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a:Object = function(foo:int, bar:String = 'goo'):int{return -1;};",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var a:Object = function(foo:int, bar:String = 'goo'):int {\n\treturn -1;\n}");
    }

    @Test
    public void testAnonymousFunctionAsArgument()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode(
                "addListener('foo', function(event:Object):void{doit();});",
                IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("addListener('foo', function(event:Object):void {\n\tdoit();\n})");
    }
    
    @Test
    public void testVisitLocalNamedFunction()
    {
        IFunctionNode node = (IFunctionNode) getLocalFunction("function a() {};");
        asBlockWalker.visitFunction(node);
        assertOut("function a() {\n}");
    }
    
    @Test
    public void testVisitLocalNamedFunctionWithParamsReturn()
    {
        IFunctionNode node = (IFunctionNode) getLocalFunction("function a(foo:int, bar:String = 'goo'):int{return -1;};");
        asBlockWalker.visitFunction(node);
        assertOut("function a(foo:int, bar:String = 'goo'):int {\n\treturn -1;\n}");
    }

    @Test
    public void testVisitDynamicAccessNode_1()
    {
        IDynamicAccessNode node = getDynamicAccessNode("a[b]");
        asBlockWalker.visitDynamicAccess(node);
        assertOut("a[b]");
    }

    @Test
    public void testVisitDynamicAccessNode_2()
    {
        IDynamicAccessNode node = getDynamicAccessNode("a[b[c][d]]");
        asBlockWalker.visitDynamicAccess(node);
        assertOut("a[b[c][d]]");
    }

    @Test
    public void testVisitAs()
    {
        IBinaryOperatorNode node = getBinaryNode("a as b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a as b");
    }

    @Test
    public void testVisitBinaryOperatorNode_Comma()
    {
        IBinaryOperatorNode node = getBinaryNode("a, b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a, b");
    }

    @Test
    public void testVisitTernaryOperatorNode()
    {
        ITernaryOperatorNode node = (ITernaryOperatorNode) getExpressionNode(
                "a ? b : c", ITernaryOperatorNode.class);
        asBlockWalker.visitTernaryOperator(node);
        assertOut("a ? b : c");
    }

    @Test
    public void testVisitUnaryOperator_Delete()
    {
        IUnaryOperatorNode node = getUnaryNode("delete a");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("delete a");
    }

    @Test
    public void testVisitMemberAccess_1()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getExpressionNode(
                "a.b", IMemberAccessExpressionNode.class);
        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("a.b");
    }

    @Test
    public void testVisitMemberAccess_2()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getExpressionNode(
                "a.b.c.d", IMemberAccessExpressionNode.class);
        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("a.b.c.d");
    }

    @Test
    public void testVisitBinaryOperator_In()
    {
        IBinaryOperatorNode node = getBinaryNode("a in b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a in b");
    }

    @Test
    public void testVisitBinaryOperator_Instancof()
    {
        IBinaryOperatorNode node = getBinaryNode("a instanceof b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a instanceof b");
    }

    @Test
    public void testVisitBinaryOperator_Is()
    {
        IBinaryOperatorNode node = getBinaryNode("a is b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a is b");
    }

    @Test
    public void testVisitBinaryOperator_NamespaceAccess_1()
    {
        INamespaceAccessExpressionNode node = getNamespaceAccessExpressionNode("a::b");
        asBlockWalker.visitNamespaceAccessExpression(node);
        assertOut("a::b");
    }

    @Test
    public void testVisitBinaryOperator_NamespaceAccess_2()
    {
        INamespaceAccessExpressionNode node = getNamespaceAccessExpressionNode("a::b::c");
        asBlockWalker.visitNamespaceAccessExpression(node);
        assertOut("a::b::c");
    }

    @Test
    public void testVisitBinaryOperator_New()
    {
        IFunctionCallNode node = (IFunctionCallNode) getExpressionNode(
                "new Object()", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("new Object()");
    }

    @Test
    public void testVisitObjectLiteral_1()
    {
        ObjectLiteralNode node = (ObjectLiteralNode) getExpressionNode(
                "a = {a:1}", ObjectLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("{a:1}");
    }

    @Test
    public void testVisitObjectLiteral_2()
    {
        ObjectLiteralNode node = (ObjectLiteralNode) getExpressionNode(
                "a = {a:1,b:{c:2,d:{e:4}}}", ObjectLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("{a:1, b:{c:2, d:{e:4}}}");
    }

    @Test
    public void testVisitArrayLiteral_1()
    {
        ArrayLiteralNode node = (ArrayLiteralNode) getExpressionNode(
                "a = [0,1,2]", ArrayLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("[0, 1, 2]");
    }

    @Test
    public void testVisitArrayLiteral_2()
    {
        ArrayLiteralNode node = (ArrayLiteralNode) getExpressionNode(
                "a = [0,[0,1,[0,1]],2,[1,2]]", ArrayLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("[0, [0, 1, [0, 1]], 2, [1, 2]]");
    }

    @Test
    public void testVisitUnaryOperatorNode_Typeof()
    {
        IUnaryOperatorNode node = getUnaryNode("typeof(a)");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("typeof(a)");
    }

    @Test
    public void testVisitUnaryOperatorNode_Typeof_NoParens()
    {
        // TODO (mschmalle) the notation without parenthesis is also valid in AS/JS
        IUnaryOperatorNode node = getUnaryNode("typeof a");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("typeof(a)");
    }

    @Test
    public void testVisitUnaryOperatorNode_Void()
    {
        IUnaryOperatorNode node = getUnaryNode("void a");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("void a");
    }

    @Test
    public void testVisitUnaryOperatorNode_Concate_1()
    {
        IBinaryOperatorNode node = getBinaryNode("\"a\" + \"b\"");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("\"a\" + \"b\"");
    }

    // TODO (mschmalle) what's up with the escaping of backslashes?
    @Test
    public void testVisitUnaryOperatorNode_Concate_2()
    {
//        IBinaryOperatorNode node = getBinaryNode("\"a\\\"\" + \"\\\"b\"");
//        asBlockWalker.visitBinaryOperator(node);
//        assertOut("\"a\\\"\" + \"\\\"b\"");
    }

    @Test
    public void testVisitIterationFlowNode_Break()
    {
        IIterationFlowNode node = (IIterationFlowNode) getNode("break",
                IIterationFlowNode.class);
        asBlockWalker.visitIterationFlow(node);
        assertOut("break");
    }

    @Test
    public void testVisitIterationFlowNode_Continue()
    {
        IIterationFlowNode node = (IIterationFlowNode) getNode("continue",
                IIterationFlowNode.class);
        asBlockWalker.visitIterationFlow(node);
        assertOut("continue");
    }

    @Test
    public void testVisitReturnWithoutValue()
    {
        IReturnNode node = (IReturnNode) getNode("return", IReturnNode.class);
        asBlockWalker.visitReturn(node);
        assertOut("return");
    }

    @Test
    public void testVisitReturnWithValue()
    {
        IReturnNode node = (IReturnNode) getNode("return a", IReturnNode.class);
        asBlockWalker.visitReturn(node);
        assertOut("return a");
    }

    @Test
    public void testVisitThrow()
    {
        IThrowNode node = (IThrowNode) getNode("throw a", IThrowNode.class);
        asBlockWalker.visitThrow(node);
        assertOut("throw a");
    }

    @Test
    public void testVisitFunctionCall_1()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode("a()", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("a()");
    }

    @Test
    public void testVisitFunctionCall_2()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode("a(b)", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("a(b)");
    }

    @Test
    public void testVisitFunctionCall_3()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode("a(b, c)", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("a(b, c)");
    }
}
